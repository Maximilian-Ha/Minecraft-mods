package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.main.NuclearTechMod;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineAutosaw.
 *
 * Die Autosaege faellt Baeume und erntet Pflanzen in einem Ring um sich herum, und sie pflanzt
 * nach: wo ein Stamm stand, steckt danach ein Setzling.
 *
 * SIE DREHT SICH IM KREIS UND SUCHT. Der Arm faehrt langsam herum; kommt in seiner Blickrichtung
 * etwas Schneidbares in Reichweite, streckt er sich aus, schneidet und zieht sich wieder ein.
 * Eingestellt wird nichts -- sie laeuft, solange Treibstoff da ist.
 *
 * DER RING IST WICHTIG: zwischen zwei und neun Bloecken Abstand. Direkt neben sich raeumt sie
 * nichts ab, sonst stuende sie frei und saege ihren eigenen Nachwuchs nicht.
 *
 * SIE FAELLT DEN GANZEN BAUM, nicht nur den getroffenen Block. Dafuer laeuft eine Suche vom
 * Stammfuss aus durch alles, was Holz oder Laub ist -- und zwar so, dass ein Block dem NAECHSTEN
 * Stamm zugeschlagen wird: senkrechte Nachbarn kosten nichts, waagerechte kosten eins. Zwei
 * Baeume, deren Kronen sich beruehren, werden dadurch getrennt, und es faellt nur der getroffene.
 *
 * SIE BRAUCHT TREIBSTOFF, kein Strom: ein Millibar alle zwanzig Ticks aus einem Tank von
 * hundert. Welcher Stoff, stellt man mit der Fluidkennung in der Hand ein.
 *
 * DIE LISTE DER TREIBSTOFFE STAND BISHER BEIM DRESCHER. Sie gehoert hierher -- so steht es im
 * Original, und der Drescher hat sie nur geliehen, weil die Autosaege fehlte. Jetzt ist sie
 * wieder da, wo sie hingehoert, und der Drescher verweist darauf.
 *
 * ABWEICHUNG: was Holz, Laub und Pflanze ist, entscheiden auf 1.21 die Blockmarken und der
 * Blocktyp, nicht das Material des Blocks -- Materialien gibt es nicht mehr.
 *
 * ABWEICHUNG: welcher Setzling zu welchem Stamm gehoert, steht in einer Tabelle. Das Original
 * rechnet es aus den Metadaten aus, die es auf 1.21 nicht mehr gibt. Die Tabelle nennt die
 * Hoelzer, die Minecraft selbst mitbringt; an einem Stamm, der nicht darin steht, wird nicht
 * nachgepflanzt.
 *
 * NICHT UEBERNOMMEN: die nicht ausgewachsene Weide, die das Original ausspart -- den Block gibt
 * es im Port nicht. Ebenso das Modell samt Arm; die Winkel werden berechnet und uebertragen,
 * damit ein spaeterer Renderer sie hat.
 */
public class MachineAutosawBlockEntity extends LoadedBaseBlockEntity implements ITickable, IFluidStandardReceiverMK2, IFluidCopiable {

    /**
     * Was die Saege als Treibstoff annimmt. Der Drescher benutzt dieselbe Liste; im Original
     * steht sie ebenfalls hier.
     */
    public static final HashSet<FluidType> acceptedFuels = new HashSet<>();

    static {
        acceptedFuels.add(Fluids.WOODOIL);
        acceptedFuels.add(Fluids.ETHANOL);
        acceptedFuels.add(Fluids.FISHOIL);
        acceptedFuels.add(Fluids.HEAVYOIL);
        acceptedFuels.add(Fluids.COALCREOSOTE);
    }

    private static final int MIN_DIST = 2;
    private static final int MAX_DIST = 9;

    private static final int FELL_HORIZONTAL_RANGE = 10;
    private static final int FELL_BFS_RADIUS = MAX_DIST + FELL_HORIZONTAL_RANGE;
    private static final int FELL_VERTICAL_RANGE = 32;
    private static final int FELL_MAX_BASE_DEPTH = FELL_VERTICAL_RANGE / 2;

    /** 18er-Nachbarschaft: sechs Flaechen und zwoelf Kanten, aber keine Ecken. */
    private static final int[][] EIGHTEEN_DIRS = {
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
            {1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
            {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
            {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1}
    };

    /** Welcher Setzling zu welchem Stamm gehoert; siehe Klassenkommentar. */
    private static final Map<Block, Block> SAPLINGS = Map.ofEntries(
            Map.entry(Blocks.OAK_LOG, Blocks.OAK_SAPLING),
            Map.entry(Blocks.OAK_WOOD, Blocks.OAK_SAPLING),
            Map.entry(Blocks.SPRUCE_LOG, Blocks.SPRUCE_SAPLING),
            Map.entry(Blocks.SPRUCE_WOOD, Blocks.SPRUCE_SAPLING),
            Map.entry(Blocks.BIRCH_LOG, Blocks.BIRCH_SAPLING),
            Map.entry(Blocks.BIRCH_WOOD, Blocks.BIRCH_SAPLING),
            Map.entry(Blocks.JUNGLE_LOG, Blocks.JUNGLE_SAPLING),
            Map.entry(Blocks.JUNGLE_WOOD, Blocks.JUNGLE_SAPLING),
            Map.entry(Blocks.ACACIA_LOG, Blocks.ACACIA_SAPLING),
            Map.entry(Blocks.ACACIA_WOOD, Blocks.ACACIA_SAPLING),
            Map.entry(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_SAPLING),
            Map.entry(Blocks.DARK_OAK_WOOD, Blocks.DARK_OAK_SAPLING),
            Map.entry(Blocks.CHERRY_LOG, Blocks.CHERRY_SAPLING),
            Map.entry(Blocks.CHERRY_WOOD, Blocks.CHERRY_SAPLING),
            Map.entry(Blocks.MANGROVE_LOG, Blocks.MANGROVE_PROPAGULE),
            Map.entry(Blocks.MANGROVE_WOOD, Blocks.MANGROVE_PROPAGULE));

    public final FluidTank tank = new FluidTank(Fluids.WOODOIL, 100);

    public boolean isOn;
    public boolean isSuspended;

    private int forceSkip;

    public float rotationYaw;
    public float rotationPitch;
    public float prevRotationYaw;
    public float prevRotationPitch;
    public float syncYaw;
    public float syncPitch;
    private int turnProgress;

    /** 0: sucht, 1: streckt sich, 2: zieht sich ein. */
    private int state = 0;

    public float spin;
    public float lastSpin;
    private AudioWrapper audio;

    private AABB renderBox;

    public MachineAutosawBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_AUTOSAW.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) this.serverTick();
        else this.clientTick();
    }

    private void serverTick() {

        if(!this.isSuspended && this.level.getGameTime() % 20 == 0) {

            if(this.tank.getFill() > 0) {
                this.tank.setFill(this.tank.getFill() - 1);
                this.isOn = true;
            } else {
                this.isOn = false;
            }

            for(Direction dir : Direction.values()) {
                if(dir != Direction.UP) this.trySubscribe(this.tank.getTankType(), this.level, this.worldPosition.relative(dir), dir);
            }
        }

        if(this.isOn && !this.isSuspended) {

            Vec3 tip = this.armTip();

            this.hurtEntities(tip);

            if(this.state == 0) this.search();

            int hitY = Mth.floor(tip.y);
            int hitX0 = Mth.floor(tip.x - 0.5);
            int hitZ0 = Mth.floor(tip.z - 0.5);
            int hitX1 = Mth.floor(tip.x + 0.5);
            int hitZ1 = Mth.floor(tip.z + 0.5);

            this.tryInteract(new BlockPos(hitX0, hitY, hitZ0));
            this.tryInteract(new BlockPos(hitX1, hitY, hitZ0));
            this.tryInteract(new BlockPos(hitX0, hitY, hitZ1));
            this.tryInteract(new BlockPos(hitX1, hitY, hitZ1));

            if(this.state == 1) {
                this.rotationPitch += 2;
                if(this.rotationPitch > 80) {
                    this.rotationPitch = 80;
                    this.state = 2;
                }
            }

            if(this.state == 2) {
                this.rotationPitch -= 2;
                if(this.rotationPitch <= 0) {
                    this.rotationPitch = 0;
                    this.state = 0;
                }
            }
        }

        this.networkPackNT(100);
    }

    /**
     * Wo die Saegeblaetter gerade stehen. Der Arm besteht aus zwei gleich langen Gliedern und
     * einer Spitze; die Rechnung ist die des Originals, nur mit den Vektorgriffen von 1.21.
     */
    private Vec3 armTip() {

        Vec3 pivot = new Vec3(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.75, this.worldPosition.getZ() + 0.5);

        Vec3 upper = new Vec3(0, 0, -4)
                .xRot((float) Math.toRadians(80 - this.rotationPitch))
                .yRot(-(float) Math.toRadians(this.rotationYaw));

        Vec3 lower = new Vec3(0, 0, -4)
                .xRot((float) -Math.toRadians(80 - this.rotationPitch))
                .yRot(-(float) Math.toRadians(this.rotationYaw));

        Vec3 tip = new Vec3(0, 0, -2).yRot(-(float) Math.toRadians(this.rotationYaw));

        return new Vec3(pivot.x + upper.x + lower.x + tip.x, pivot.y, pivot.z + upper.z + lower.z + tip.z);
    }

    /** Was im Schnittfeld steht, wird zerlegt. Dasselbe Verfahren wie beim Drescher. */
    private void hurtEntities(Vec3 tip) {

        if(!(this.level instanceof ServerLevel serverLevel)) return;

        AABB area = new AABB(tip.x - 1, tip.y - 0.25, tip.z - 1, tip.x + 1, tip.y + 0.25, tip.z + 1);

        for(LivingEntity e : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
            if(e.isAlive() && e.hurt(serverLevel.damageSources().source(NtmDamageTypes.TURBOFAN), 100)) {
                SoundUtils.playAtVec3(serverLevel, new Vec3(e.getX(), e.getY(), e.getZ()),
                        SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS,
                        2.0F, 0.95F + serverLevel.random.nextFloat() * 0.2F);
            }
        }
    }

    /**
     * Den Ring in Blickrichtung des Arms absuchen. Gefunden wird nur, was in einem schmalen
     * Winkel von fuenf Grad vor dem Arm liegt -- er soll sich nicht quer zu seiner Drehung
     * ausstrecken.
     */
    private void search() {

        this.rotationYaw += 1;
        if(this.rotationYaw >= 360) this.rotationYaw -= 360;

        if(this.forceSkip > 0) {
            this.forceSkip--;
            return;
        }

        final double CUT_ANGLE = Math.toRadians(5);
        double yawRads = Math.toRadians((this.rotationYaw + 270) % 360);

        for(int dx = -MAX_DIST; dx <= MAX_DIST; dx++) {
            for(int dz = -MAX_DIST; dz <= MAX_DIST; dz++) {

                int sqr = dx * dx + dz * dz;
                if(sqr <= MIN_DIST * MIN_DIST || sqr > MAX_DIST * MAX_DIST) continue;

                double angle = Math.atan2(dz, dx);
                double rel = Math.abs(angle - yawRads);
                rel = Math.abs((rel + Math.PI) % (2 * Math.PI) - Math.PI);

                if(rel > CUT_ANGLE) continue;

                BlockPos pos = this.worldPosition.offset(dx, 1, dz);
                BlockState state = this.level.getBlockState(pos);

                if(!isWood(state) && !isLeaves(state) && !isPlant(state)) continue;

                this.state = 1;
                return;
            }
        }
    }

    private void tryInteract(BlockPos pos) {

        BlockState state = this.level.getBlockState(pos);

        if(isLeaves(state) || isPlant(state)) {
            this.cutCrop(pos, state);
        } else if(isWood(state)) {
            this.fellTree(pos);
            if(this.state == 1) this.state = 2;
        }

        /* An einer Wand zieht der Arm wieder ein und wartet einen Moment. */
        if(this.state == 1 && this.level.getBlockState(pos).isRedstoneConductor(this.level, pos)) {
            this.state = 2;
            this.forceSkip = 5;
        }
    }

    private void cutCrop(BlockPos pos, BlockState state) {

        if(!(this.level instanceof ServerLevel serverLevel)) return;

        this.level.levelEvent(2001, pos, Block.getId(state));

        for(ItemStack drop : Block.getDrops(state, serverLevel, pos, serverLevel.getBlockEntity(pos), null, ItemStack.EMPTY)) {

            float delta = 0.7F;
            double dx = this.level.random.nextFloat() * delta + (1F - delta) * 0.5D;
            double dy = this.level.random.nextFloat() * delta + (1F - delta) * 0.5D;
            double dz = this.level.random.nextFloat() * delta + (1F - delta) * 0.5D;

            ItemEntity item = new ItemEntity(this.level, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, drop);
            item.setPickUpDelay(10);
            this.level.addFreshEntity(item);
        }

        this.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    /**
     * Den getroffenen Baum faellen -- und NUR ihn.
     *
     * Erst werden alle Staemme im Arbeitsring gesucht, dann laeuft von allen zugleich eine Suche
     * los, bei der senkrechte Schritte nichts und waagerechte eins kosten. Jeder Block gehoert
     * damit dem Stamm, von dem aus er am wenigsten waagerechte Schritte entfernt liegt. Gefaellt
     * wird, was dem getroffenen Stamm gehoert.
     */
    private void fellTree(BlockPos hit) {

        int sawY = hit.getY();

        /* Die Spalte, nicht der Block: die Hoehe steht auf -1, damit zwei Bloecke uebereinander
         * denselben Schluessel haben. */
        BlockPos hitCol = new BlockPos(hit.getX(), -1, hit.getZ());

        Map<BlockPos, BlockPos> trunks = new HashMap<>();

        for(int dx = -MAX_DIST; dx <= MAX_DIST; dx++) {
            for(int dz = -MAX_DIST; dz <= MAX_DIST; dz++) {

                if(dx * dx + dz * dz > MAX_DIST * MAX_DIST) continue;

                BlockPos col = new BlockPos(this.worldPosition.getX() + dx, sawY, this.worldPosition.getZ() + dz);
                if(!isWood(this.level.getBlockState(col))) continue;

                int baseY = sawY;
                while(sawY - baseY < FELL_MAX_BASE_DEPTH
                        && isWood(this.level.getBlockState(new BlockPos(col.getX(), baseY - 1, col.getZ())))) {
                    baseY--;
                }

                if(!this.canSupportSapling(new BlockPos(col.getX(), baseY, col.getZ()))) continue;

                trunks.put(new BlockPos(col.getX(), -1, col.getZ()), new BlockPos(col.getX(), baseY, col.getZ()));
            }
        }

        if(!trunks.containsKey(hitCol)) {
            int baseY = hit.getY();
            while(sawY - baseY < FELL_MAX_BASE_DEPTH
                    && isWood(this.level.getBlockState(new BlockPos(hit.getX(), baseY - 1, hit.getZ())))) {
                baseY--;
            }
            trunks.put(hitCol, new BlockPos(hit.getX(), baseY, hit.getZ()));
        }

        Map<BlockPos, BlockPos> owner = new HashMap<>();
        ArrayDeque<BlockPos[]> deque = new ArrayDeque<>();
        int hitColCount = 1;

        int minY = Math.max(this.level.getMinBuildHeight(), sawY - FELL_MAX_BASE_DEPTH);
        int maxY = Math.min(this.level.getMaxBuildHeight() - 1, sawY + FELL_VERTICAL_RANGE);

        for(Map.Entry<BlockPos, BlockPos> trunk : trunks.entrySet()) {
            deque.addFirst(new BlockPos[] { trunk.getValue(), trunk.getKey() });
        }

        while(!deque.isEmpty()) {

            BlockPos[] pair = deque.pollFirst();
            BlockPos current = pair[0];
            BlockPos currentCol = pair[1];

            if(owner.containsKey(current)) {
                if(currentCol.equals(hitCol)) {
                    hitColCount--;
                    if(hitColCount == 0) break;
                }
                continue;
            }

            owner.put(current, currentCol);

            for(int[] dir : EIGHTEEN_DIRS) {

                BlockPos next = current.offset(dir[0], dir[1], dir[2]);

                int ndx = next.getX() - this.worldPosition.getX();
                int ndz = next.getZ() - this.worldPosition.getZ();

                if(ndx * ndx + ndz * ndz > FELL_BFS_RADIUS * FELL_BFS_RADIUS) continue;
                if(next.getY() < minY || next.getY() > maxY) continue;
                if(owner.containsKey(next)) continue;

                BlockState state = this.level.getBlockState(next);
                if(!isWood(state) && !isLeaves(state)) continue;

                boolean horizontal = dir[0] != 0 || dir[2] != 0;
                BlockPos[] entry = new BlockPos[] { next, currentCol };

                if(horizontal) deque.addLast(entry);
                else deque.addFirst(entry);

                if(currentCol.equals(hitCol)) hitColCount++;
            }

            if(currentCol.equals(hitCol)) {
                hitColCount--;
                if(hitColCount == 0) break;
            }
        }

        for(Map.Entry<BlockPos, BlockPos> entry : owner.entrySet()) {

            if(!entry.getValue().equals(hitCol)) continue;

            BlockPos pos = entry.getKey();
            BlockState state = this.level.getBlockState(pos);
            Block sapling = SAPLINGS.get(state.getBlock());

            if(sapling != null && this.isWithinWorkingArea(pos) && this.canSupportSapling(pos)) {
                this.level.destroyBlock(pos, true);
                this.level.setBlock(pos, sapling.defaultBlockState(), 3);
            } else {
                this.level.destroyBlock(pos, true);
            }
        }
    }

    private boolean isWithinWorkingArea(BlockPos pos) {
        int dx = pos.getX() - this.worldPosition.getX();
        int dz = pos.getZ() - this.worldPosition.getZ();
        int sqr = dx * dx + dz * dz;
        return sqr > MIN_DIST * MIN_DIST && sqr <= MAX_DIST * MAX_DIST;
    }

    /**
     * Ob an dieser Stelle ein Setzling stehen kann.
     *
     * ABWEICHUNG: das Original fragt den Boden ueber canSustainPlant. Hier wird der Setzling
     * selbst gefragt -- dasselbe Vorgehen wie beim Drescher, und auf 1.21 der vorgesehene Weg.
     */
    private boolean canSupportSapling(BlockPos pos) {
        return Blocks.OAK_SAPLING.defaultBlockState().canSurvive(this.level, pos);
    }

    private static boolean isWood(BlockState state) { return state.is(BlockTags.LOGS); }
    private static boolean isLeaves(BlockState state) { return state.is(BlockTags.LEAVES); }
    private static boolean isPlant(BlockState state) { return state.getBlock() instanceof BushBlock; }

    /* --- Client --- */

    private void clientTick() {

        this.lastSpin = this.spin;

        if(this.isOn && !this.isSuspended) {

            this.spin += 15F;

            Vec3 vec = new Vec3(0.625, 0, 1.625).yRot(-(float) Math.toRadians(this.rotationYaw));
            this.level.addParticle(ParticleTypes.SMOKE,
                    this.worldPosition.getX() + 0.5 + vec.x, this.worldPosition.getY() + 2.0625, this.worldPosition.getZ() + 0.5 + vec.z,
                    0, 0, 0);
        }

        this.updateAudio();

        if(this.spin >= 360F) {
            this.spin -= 360F;
            this.lastSpin -= 360F;
        }

        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;

        /* Die Winkel kommen nur alle hundert Ticks; dazwischen wird in drei Schritten
         * herangefuehrt, damit der Arm nicht springt. */
        if(this.turnProgress > 0) {
            this.rotationYaw += Mth.wrapDegrees(this.syncYaw - this.rotationYaw) / this.turnProgress;
            this.rotationPitch += Mth.wrapDegrees(this.syncPitch - this.rotationPitch) / this.turnProgress;
            this.turnProgress--;
        } else {
            this.rotationYaw = this.syncYaw;
            this.rotationPitch = this.syncPitch;
        }
    }

    private void updateAudio() {

        if(this.isOn && !this.isSuspended && NuclearTechMod.proxy.me() != null
                && NuclearTechMod.proxy.me().distanceToSqr(this.getBlockPos().getCenter()) < 225) {

            if(this.audio == null) {
                this.audio = this.createAudioLoop();
                if(this.audio != null) this.audio.startSound();
            } else if(!this.audio.isPlaying()) {
                this.audio = this.rebootAudio(this.audio);
            }

            if(this.audio != null) {
                this.audio.keepAlive();
                this.audio.updateVolume(this.getVolume(1F));
            }

        } else if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(NtmSoundEvents.ENGINE_LOOP.get(), SoundSource.BLOCKS, this,
                1F, 10F, 1F + this.level.random.nextFloat() * 0.1F, 10);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.stopAudio();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.stopAudio();
    }

    private void stopAudio() {
        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    /* --- Fluid --- */

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank getTankToPaste() { return this.tank; }

    /* --- Speichern --- */

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.isOn);
        buf.writeBoolean(this.isSuspended);
        buf.writeFloat(this.rotationYaw);
        buf.writeFloat(this.rotationPitch);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        this.isOn = buf.readBoolean();
        this.isSuspended = buf.readBoolean();
        this.syncYaw = buf.readFloat();
        this.syncPitch = buf.readFloat();
        this.turnProgress = 3;
        this.tank.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.isOn = tag.getBoolean("isOn");
        this.isSuspended = tag.getBoolean("isSuspended");
        this.forceSkip = tag.getInt("skip");
        this.rotationYaw = tag.getFloat("yaw");
        this.rotationPitch = tag.getFloat("pitch");
        this.state = tag.getInt("state");
        this.tank.readFromNBT(tag, "t");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("isOn", this.isOn);
        tag.putBoolean("isSuspended", this.isSuspended);
        tag.putInt("skip", this.forceSkip);
        tag.putFloat("yaw", this.rotationYaw);
        tag.putFloat("pitch", this.rotationPitch);
        tag.putInt("state", this.state);
        this.tank.writeToNBT(tag, "t");
    }

    /** Der Arm reicht neun Bloecke weit; der Anzeigekasten muss ihn fassen. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - MAX_DIST, p.getY(), p.getZ() - MAX_DIST,
                    p.getX() + MAX_DIST + 1, p.getY() + 4, p.getZ() + MAX_DIST + 1);
        }
        return this.renderBox;
    }
}
