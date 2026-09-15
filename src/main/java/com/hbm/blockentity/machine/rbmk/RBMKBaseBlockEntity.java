package com.hbm.blockentity.machine.rbmk;

import api.hbm.fluidmk2.FluidNetMK2;
import api.hbm.fluidmk2.FluidNode;
import api.hbm.fluidmk2.IFluidReceiverMK2;
import com.hbm.blockentity.IOverpressurable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.rbmk.RBMKBaseBlock;
import com.hbm.blocks.machine.rbmk.RBMKLid;
import com.hbm.entity.projectile.RBMKDebris;
import com.hbm.entity.projectile.RBMKDebris.DebrisType;
import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Compat;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKBase.
 *
 * Grundlage aller RBMK-Saeulen. Kuemmert sich um Waerme und den Ablauf der Kernschmelze.
 */
public abstract class RBMKBaseBlockEntity extends LoadedBaseBlockEntity implements ITickable {

    public double heat;

    public int reasimWater;
    public static final int MAX_WATER = 16000;
    public int reasimSteam;
    public static final int MAX_STEAM = 16000;
    public int craneIndicator;

    public RBMKBaseBlockEntity(BlockEntityType<? extends LoadedBaseBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public RBMKLid getLid() {
        BlockState state = this.getBlockState();
        if(state.hasProperty(RBMKBaseBlock.LID)) return state.getValue(RBMKBaseBlock.LID);
        return RBMKLid.NONE;
    }

    public boolean hasLid() {
        if(!isLidRemovable()) return true;
        return this.getLid() != RBMKLid.NONE;
    }

    public boolean isLidRemovable() {
        return true;
    }

    /**
     * Ungefaehrer Schmelzpunkt von Stahl. Brennstoffe werden oft deutlich heisser, das schlaegt
     * wegen der geringen Diffusion aber kaum auf die Saeule durch.
     */
    public double maxHeat() {
        return 1500D;
    }

    /** Die Zahl der angeschlossenen Nachbarn bestimmt, wie schnell die Saeule auskuehlt. */
    public double passiveCooling(int neighbors) {
        double min = RBMKDials.getPassiveCoolingInner(this.level);
        double max = RBMKDials.getPassiveCooling(this.level);
        return min + (max - min) * ((4 - Mth.clamp(neighbors, 0, 4)) / 4D);
    }

    public int trackingRange() {
        return 15;
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.craneIndicator > 0) this.craneIndicator--;

        moveHeat();
        if(RBMKDials.getReasimBoilers(this.level)) boilWater();

        this.networkPackNT(trackingRange());
    }

    /** Mit dem ReaSim-Dial verhalten sich alle RBMK-Teile wie Dampferzeuger. */
    private void boilWater() {

        if(heat < 100D) return;

        double heatConsumption = RBMKDials.getBoilerHeatConsumption(this.level);
        double availableHeat = (this.heat - 100) / heatConsumption;
        double availableWater = this.reasimWater;
        double availableSpace = MAX_STEAM - this.reasimSteam;

        int processedWater = (int) Math.floor(BobMathUtil.min(availableHeat, availableWater, availableSpace)
                * Mth.clamp(RBMKDials.getReaSimBoilerSpeed(this.level), 0D, 1D));

        if(processedWater <= 0) return;

        this.reasimWater -= processedWater;
        this.reasimSteam += processedWater;
        this.heat -= processedWater * heatConsumption;
    }

    public static final Direction[] NEIGHBOR_DIRS = new Direction[] {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    protected RBMKBaseBlockEntity[] neighborCache = new RBMKBaseBlockEntity[4];

    /** Verteilt Waerme halbwegs gerecht auf die Nachbarsaeulen. */
    private void moveHeat() {

        boolean reasim = RBMKDials.getReasimBoilers(this.level);

        List<RBMKBaseBlockEntity> rec = new ArrayList<>();
        rec.add(this);
        double heatTot = this.heat;
        int waterTot = this.reasimWater;
        int steamTot = this.reasimSteam;

        for(int index = 0; index < NEIGHBOR_DIRS.length; index++) {

            if(neighborCache[index] != null && neighborCache[index].isRemoved()) neighborCache[index] = null;

            if(neighborCache[index] == null) {
                BlockEntity te = Compat.getBlockEntityStandard(this.level, this.worldPosition.relative(NEIGHBOR_DIRS[index]));
                if(te instanceof RBMKBaseBlockEntity base) neighborCache[index] = base;
            }
        }

        for(RBMKBaseBlockEntity base : neighborCache) {

            if(base != null) {
                rec.add(base);
                heatTot += base.heat;
                if(reasim) {
                    waterTot += base.reasimWater;
                    steamTot += base.reasimSteam;
                }
            }
        }

        int members = rec.size();
        double stepSize = RBMKDials.getColumnHeatFlow(this.level);

        if(members > 1) {

            double targetHeat = heatTot / (double) members;

            int tWater = waterTot / members;
            int rWater = waterTot % members;
            int tSteam = steamTot / members;
            int rSteam = steamTot % members;

            for(RBMKBaseBlockEntity rbmk : rec) {
                double delta = targetHeat - rbmk.heat;
                rbmk.heat += delta * stepSize;

                if(reasim) {
                    rbmk.reasimWater = tWater;
                    rbmk.reasimSteam = tSteam;
                }
            }

            // Der Rest der Division geht an diese Saeule, damit beim Runden nichts verloren geht.
            if(reasim) {
                this.reasimWater += rWater;
                this.reasimSteam += rSteam;
            }

            this.setChanged();
        }

        coolPassively(members - 1);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // isLoaded ist false, wenn nur der Chunk entladen wurde -- dann ist das kein Abbau.
        if(this.level != null) NeutronNodeWorld.removeNode(this.level, this.worldPosition);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if(this.level != null) NeutronNodeWorld.removeNode(this.level, this.worldPosition);
    }

    protected void coolPassively(int neighbors) {
        this.heat -= this.passiveCooling(neighbors);
        if(heat < 20) heat = 20D;
    }

    public RBMKType getRBMKType() {
        return RBMKType.OTHER;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.heat = tag.getDouble("heat");
        this.reasimWater = tag.getInt("reasimWater");
        this.reasimSteam = tag.getInt("reasimSteam");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("heat", this.heat);
        tag.putInt("reasimWater", this.reasimWater);
        tag.putInt("reasimSteam", this.reasimSteam);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.heat);
        buf.writeInt(this.reasimWater);
        buf.writeInt(this.reasimSteam);
        buf.writeByte((byte) this.craneIndicator);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.heat = buf.readDouble();
        this.reasimWater = buf.readInt();
        this.reasimSteam = buf.readInt();
        this.craneIndicator = buf.readByte();
    }

    public void onOverheat() {
        for(int i = 0; i < 4; i++) {
            this.level.setBlockAndUpdate(this.worldPosition.above(i), Blocks.LAVA.defaultBlockState());
        }
    }

    public void onMelt(int reduce) {
        standardMelt(reduce);

        if(this.hasLid()) spawnDebris(DebrisType.LID);
    }

    /**
     * Wirft ein Truemmerteil aus. Die Wurfrichtung ist zufaellig, die Hoehe steigt mit der
     * Zufallszahl -- Zahlen unveraendert aus dem Original.
     */
    protected void spawnDebris(DebrisType type) {

        if(this.level == null || this.level.isClientSide) return;

        RBMKDebris debris = new RBMKDebris(this.level,
                this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 4D, this.worldPosition.getZ() + 0.5D, type);

        double motionX = this.level.random.nextGaussian() * 0.25D;
        double motionZ = this.level.random.nextGaussian() * 0.25D;
        double motionY = 0.25D + this.level.random.nextDouble() * 1.25D;

        /* Der Deckel fliegt steiler und weniger weit -- er soll die Decke treffen. */
        if(type == DebrisType.LID) {
            motionX *= 0.5D;
            motionY += 0.5D;
            motionZ *= 0.5D;
        }

        debris.setDeltaMovement(motionX, motionY, motionZ);
        this.level.addFreshEntity(debris);
    }

    /** Laesst die Saeule zu Truemmern zusammenfallen. */
    protected void standardMelt(int reduce) {

        int h = RBMKDials.getColumnHeight(this.level);
        reduce = Mth.clamp(reduce, 1, h);

        if(this.level.random.nextInt(3) == 0) reduce++;

        for(int i = h; i >= 0; i--) {

            BlockPos pos = this.worldPosition.above(i);

            if(i <= h + 1 - reduce) {
                if(reduce > 1 && i == h + 1 - reduce) {
                    this.level.setBlockAndUpdate(pos, NtmBlocks.RBMK_DEBRIS_BURNING.get().defaultBlockState());
                } else {
                    this.level.setBlockAndUpdate(pos, NtmBlocks.RBMK_DEBRIS.get().defaultBlockState());
                }
            } else {
                this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    public static HashSet<RBMKBaseBlockEntity> columns = new HashSet<>();
    /**
     * Die Rohrnetze, die beim Schmelzen an einem Dampferzeuger hingen. Sie werden dort
     * eingetragen und hier zerrissen.
     */
    public static HashSet<FluidNetMK2> pipes = new HashSet<>();

    /**
     * Ob in der gerade schmelzenden Anlage ein DRX-Stab lag. Der Brennkanal setzt das
     * Kennzeichen beim Einstuerzen, die Kernschmelze liest es und raeumt es hinterher wieder
     * ab -- wie im Original ein statisches Feld, weil beide Seiten sonst keinen Draht
     * zueinander haben.
     */
    public static boolean digamma = false;

    /**
     * Die Kernschmelze. Sucht alle zusammenhaengenden Saeulen, laesst sie von aussen nach innen
     * immer tiefer einstuerzen, verstrahlt den Schutt um ausgelaufene Brennstoffsaeulen, zerreisst
     * die angeschlossenen Dampfleitungen und setzt zum Schluss die Pilzwolke.
     */
    public void meltdown() {

        if(this.level == null || this.level.isClientSide) return;

        columns.clear();
        getFF(this.worldPosition);

        int minX = this.worldPosition.getX();
        int maxX = this.worldPosition.getX();
        int minZ = this.worldPosition.getZ();
        int maxZ = this.worldPosition.getZ();

        for(RBMKBaseBlockEntity rbmk : columns) {
            BlockPos pos = rbmk.getBlockPos();
            if(pos.getX() < minX) minX = pos.getX();
            if(pos.getX() > maxX) maxX = pos.getX();
            if(pos.getZ() < minZ) minZ = pos.getZ();
            if(pos.getZ() > maxZ) maxZ = pos.getZ();
        }

        for(RBMKBaseBlockEntity rbmk : columns) {

            BlockPos pos = rbmk.getBlockPos();

            int distFromMinX = pos.getX() - minX;
            int distFromMaxX = maxX - pos.getX();
            int distFromMinZ = pos.getZ() - minZ;
            int distFromMaxZ = maxZ - pos.getZ();

            int minDist = Math.min(distFromMinX, Math.min(distFromMaxX, Math.min(distFromMinZ, distFromMaxZ)));

            rbmk.onMelt(minDist + 1);
        }

        this.irradiateDebrisAroundCorium();
        this.overpressure();
        this.mushroomCloud(minX, maxX, minZ, maxZ);

        columns.clear();
        pipes.clear();
        digamma = false;
    }

    /**
     * Der Ueberdruck. Was am Dampfnetz der geschmolzenen Anlage hing, haelt den Schlag nicht aus:
     * ein Fuenftel der Rohre zerplatzt, und jeder Abnehmer fliegt in die Luft.
     *
     * Die Obergrenze von hundert Rohren stammt aus dem Original und ist dort wie hier eine
     * Notbremse -- ein weitverzweigtes Netz wuerde sonst den Server anhalten.
     */
    private void overpressure() {

        if(pipes.isEmpty()) return;
        if(!RBMKDials.getOverpressure(this.level)) return;

        Set<FluidNode> pipeBlocks = new LinkedHashSet<>();
        Set<IFluidReceiverMK2> pipeReceivers = new LinkedHashSet<>();

        /* Mehrere Saeulen koennen am selben Netz haengen -- erst zusammenwerfen, dann sprengen. */
        for(FluidNetMK2 net : pipes) {
            pipeBlocks.addAll(net.links);
            pipeReceivers.addAll(net.receiverEntries.keySet());
        }

        int max = Math.min(pipeBlocks.size() / 5, 100);
        int count = 0;

        for(FluidNode node : pipeBlocks) {

            if(count >= max) break;

            for(BlockPos pos : node.positions) {
                if(this.level.getBlockEntity(pos) != null) this.level.removeBlock(pos, false);
            }

            count++;
        }

        for(IFluidReceiverMK2 receiver : pipeReceivers) {

            if(!(receiver instanceof BlockEntity be)) continue;

            BlockPos pos = be.getBlockPos();

            if(receiver instanceof IOverpressurable overpressurable) {
                overpressurable.explode(this.level, pos);
            } else {
                this.level.removeBlock(pos, false);
                this.level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5F, Level.ExplosionInteraction.NONE);
            }
        }
    }

    /**
     * Die Pilzwolke ueber der Anlage. Ihre Groesse richtet sich nach der kuerzeren Kante des
     * Reaktors -- ein langgestreckter Bau qualmt nicht staerker als ein quadratischer.
     */
    private void mushroomCloud(int minX, int maxX, int minZ, int maxZ) {

        if(!(this.level instanceof ServerLevel serverLevel)) return;

        int smallDim = Math.min(maxX - minX, maxZ - minZ);
        double x = minX + (maxX - minX) / 2 + 0.5;
        double y = this.worldPosition.getY() + 1;
        double z = minZ + (maxZ - minZ) / 2 + 0.5;

        CompoundTag data = new CompoundTag();
        data.putString("type", "rbmkmush");
        data.putFloat("scale", smallDim);

        PacketDistributor.sendToPlayersNear(serverLevel, null, x, y, z, 250, new AuxParticle(data, x, y, z));

        this.level.playSound(null, x, y, z, NtmSoundEvents.RBMK_EXPLOSION.get(), SoundSource.BLOCKS, 50.0F, 1.0F);
    }

    /**
     * Der Schutt unmittelbar um eine ausgelaufene Brennstoffsaeule wird selbst zur Strahlenquelle
     * -- ein Drittel davon, im Wuerfel von einem Block Kantenlaenge um die Saeule herum.
     *
     * Lag ein DRX-Stab in der Anlage, wird daraus kein strahlender, sondern Digamma-Schutt: der
     * klingt nicht ab, sondern frisst sich weiter.
     */
    private void irradiateDebrisAroundCorium() {

        BlockState radiating = (digamma ? NtmBlocks.RBMK_DEBRIS_DIGAMMA.get() : NtmBlocks.RBMK_DEBRIS_RADIATING.get()).defaultBlockState();

        for(RBMKBaseBlockEntity rbmk : columns) {

            if(!(rbmk instanceof RBMKRodBlockEntity)) continue;

            BlockPos center = rbmk.getBlockPos();
            if(!this.level.getBlockState(center).is(NtmBlocks.CORIUM.get())) continue;

            for(BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {

                if(this.level.random.nextInt(3) != 0) continue;

                BlockState state = this.level.getBlockState(pos);
                if(state.is(NtmBlocks.RBMK_DEBRIS.get()) || state.is(NtmBlocks.RBMK_DEBRIS_BURNING.get())) {
                    this.level.setBlock(pos, radiating, 3);
                }
            }
        }
    }

    private void getFF(BlockPos pos) {

        BlockEntity te = Compat.getBlockEntityStandard(this.level, pos);

        if(te instanceof RBMKBaseBlockEntity rbmk && !columns.contains(rbmk)) {
            columns.add(rbmk);
            getFF(pos.east());
            getFF(pos.west());
            getFF(pos.south());
            getFF(pos.north());
        }
    }

    /** Wie das Reaktorpult diese Saeule im Raster zeichnet. */
    public RBMKColumnType getConsoleType() {
        return RBMKColumnType.BLANK;
    }

    /** Die Werte, die das Reaktorpult von dieser Saeule anzeigt. */
    public CompoundTag getNBTForConsole() {
        return new CompoundTag();
    }

    /**
     * Zusaetzliche Zeilen fuer die Einblendung beim Hinsehen. Die Saeulentemperatur steht schon
     * darueber; hier kommt dazu, was die jeweilige Bauform sonst noch zu melden hat.
     */
    public void getLookInfo(List<Component> text) { }

    public boolean isModerated() {
        return false;
    }

    /**
     * Die Saeule wird vom Kernblock aus gezeichnet und ragt bis zum Deckel hinauf, das
     * Zeichenfenster muss also die ganze Hoehe umfassen.
     *
     * Kein @Override: getRenderBoundingBox stammt aus der NeoForge-Erweiterung der Block-Entitaet
     * und wird vom Compiler nicht als ueberschriebene Methode gefuehrt -- die uebrigen
     * Block-Entitaeten im Port schreiben sie aus demselben Grund ohne Annotation.
     */
    public AABB getRenderBoundingBox() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        return new AABB(x, y, z, x + 1, y + 17, z + 1);
    }

    /**
     * Wohin eine fluidfuehrende Saeule ausgibt.
     *
     * Der Deckel ganz oben ist immer dabei. Steht ausserdem ein Ladeblock einen oder zwei
     * Bloecke unter dem Saeulenfuss, kommen seine vier Seiten und der Platz darunter hinzu --
     * damit laesst sich die Rohrfuehrung unter den Reaktorboden legen statt ueber den Deckel.
     *
     * Zwei Hoehen, weil der Saeulenfuss je nach Bauart auf verschiedenen Ebenen sitzt; das
     * Original prueft dieselben beiden.
     */
    protected DirPos[] getOutputPos() {

        BlockPos top = this.worldPosition.above(RBMKDials.getColumnHeight(this.level) + 1);

        for(int depth = 1; depth <= 2; depth++) {

            BlockPos loader = this.worldPosition.below(depth);
            if(this.level == null || !this.level.getBlockState(loader).is(NtmBlocks.RBMK_LOADER.get())) continue;

            return new DirPos[] {
                    new DirPos(top, Direction.UP),
                    new DirPos(loader.east(), Direction.EAST),
                    new DirPos(loader.west(), Direction.WEST),
                    new DirPos(loader.south(), Direction.SOUTH),
                    new DirPos(loader.north(), Direction.NORTH),
                    new DirPos(loader.below(), Direction.DOWN)
            };
        }

        return new DirPos[] { new DirPos(top, Direction.UP) };
    }
}
