package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.MachineThresherBlock;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toclient.ParticleBurst;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineThresher.
 *
 * Der Drescher verbrennt langsam Treibstoff aus einem 100mB-Tank und faehrt in
 * unregelmaessigen Abstaenden seinen Maehbalken aus. Alles, was dabei im Streifen
 * von sieben Bloecken Breite vor der Maschine steht, wird geerntet; Getreide wird
 * dabei gleich wieder gesetzt. Lebewesen im Wirkungsbereich werden zerlegt.
 *
 * Alle Zahlenwerte sind unveraendert uebernommen.
 */
public class MachineThresherBlockEntity extends LoadedBaseBlockEntity implements ITickable, IFluidStandardReceiverMK2, IFluidCopiable {

    /**
     * Die Liste steht seit Runde 119 dort, wo sie im Original steht: bei der Autosaege. Der
     * Drescher hatte sie nur geliehen, weil die Autosaege im Port noch fehlte. Der Verweis
     * bleibt, damit hier nichts umgeschrieben werden muss -- beide nehmen dieselben Stoffe an,
     * und zwei Listen waeren eine Gelegenheit, sie auseinanderlaufen zu lassen.
     */
    public static final HashSet<FluidType> acceptedFuels = MachineAutosawBlockEntity.acceptedFuels;

    public FluidTank tank;

    public boolean isOn;
    public boolean isSuspended;
    public int delay;

    private int turnProgress;
    public float syncAngle;
    public float angle;
    public float prevAngle;

    /** 0: wartet, 1: faehrt aus, 2: faehrt ein */
    private int state = 0;

    public float spin;
    public float lastSpin;
    private AudioWrapper audio;

    private AABB renderBox = null;

    public MachineThresherBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_THRESHER.get(), pos, state);
        this.tank = new FluidTank(Fluids.WOODOIL, 100);
    }

    /** Entspricht ForgeDirection.getOrientation(meta).getOpposite() des Originals. */
    private Direction getDir() {
        return this.getBlockState().getValue(MachineThresherBlock.FACING).getOpposite();
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        BlockPos bp = this.getBlockPos();
        int xCoord = bp.getX();
        int yCoord = bp.getY();
        int zCoord = bp.getZ();

        if(!this.level.isClientSide) {

            Direction dir = this.getDir();
            // ForgeDirection.getRotation(UP) entspricht getClockWise(Axis.Y)
            Direction rot = dir.getClockWise(Axis.Y);

            if(!this.isSuspended && this.level.getGameTime() % 20 == 0) {
                if(this.tank.getFill() > 0) {
                    this.tank.setFill(this.tank.getFill() - 1);
                    this.isOn = true;
                } else {
                    this.isOn = false;
                }

                this.trySubscribe(this.tank.getTankType(), this.level, new BlockPos(xCoord + rot.getStepX(), yCoord, zCoord + rot.getStepZ()), rot);
                this.trySubscribe(this.tank.getTankType(), this.level, new BlockPos(xCoord - rot.getStepX(), yCoord, zCoord - rot.getStepZ()), rot.getOpposite());
                this.trySubscribe(this.tank.getTankType(), this.level, bp.below(), Direction.DOWN);
            }

            if(this.isOn && !this.isSuspended) {

                if(this.state == 0) {
                    this.delay--;
                    if(this.delay <= 0) this.state = 1;
                }

                if(this.state == 1) {
                    this.angle += 82.5F / 60F;

                    if(this.angle >= 82.5F) {
                        this.angle = 82.5F;
                        this.state = 2;
                    }
                } else if(this.state == 2) {
                    this.angle -= 82.5F / 60F;

                    if(this.angle <= 0F) {
                        this.angle = 0F;
                        this.state = 0;
                        this.delay = 200 + this.level.random.nextInt(100);
                    }
                }

                if(this.angle != 0) {

                    /*
                     * Das Original setzt den Armpunkt aus Drehpunkt, zwei je vier Bloecke langen
                     * Armsegmenten und einer zwei Bloecke langen Spitze zusammen. Die beiden
                     * Segmente werden um denselben Betrag gegensinnig gedreht; in der Waagerechten
                     * liefert das wegen cos(-a) == cos(a) denselben Beitrag, deshalb steht hier
                     * nur ein Kosinusfaktor statt zweier Vektordrehungen.
                     */
                    double pivotX = xCoord + 0.5 - dir.getStepX();
                    double pivotZ = zCoord + 0.5 - dir.getStepZ();
                    double cos = Math.cos(Math.toRadians(82.5 - this.angle));

                    double endX = pivotX + 2 * (-dir.getStepX() * 4 * cos) + (-dir.getStepX() * 2);
                    double endZ = pivotZ + 2 * (-dir.getStepZ() * 4 * cos) + (-dir.getStepZ() * 2);

                    for(int i = -3; i <= 3; i++) {
                        int hitX = (int) Math.floor(endX + rot.getStepX() * i);
                        int hitZ = (int) Math.floor(endZ + rot.getStepZ() * i);
                        BlockPos hit = new BlockPos(hitX, yCoord, hitZ);

                        BlockState hitState = this.level.getBlockState(hit);

                        if(hitState.isCollisionShapeFullBlock(this.level, hit) && !canCut(hitState)) {
                            this.state = 2;
                            break;
                        }

                        if(hitState.getBlock() instanceof DoublePlantBlock) {
                            // Sonnenblume
                            if(hitState.is(Blocks.SUNFLOWER) && this.level.random.nextInt(250) == 0) {
                                this.breakParticles(hit, hitState);
                                this.dropItem(new ItemStack(Blocks.SUNFLOWER));
                            }
                            // hohes Gras
                            if(hitState.is(Blocks.TALL_GRASS) && this.level.random.nextInt(100) == 0) {
                                this.breakParticles(hit, hitState);
                                this.dropItem(new ItemStack(Items.WHEAT_SEEDS));
                            }
                            continue;
                        }

                        if(hitState.is(Blocks.SUGAR_CANE) || hitState.is(Blocks.CACTUS)) {
                            this.cutCane(hitState.getBlock(), hit);
                            continue;
                        }

                        // BonemealableBlock erfasst alles, was Knochenmehl annimmt, deshalb
                        // kommen die eigentlichen Feldfruechte zuletzt
                        if(canCut(hitState) && !shouldIgnore(this.level, hit, hitState)) this.cutCrop(hitState, hit);
                    }

                    AABB area = new AABB(endX, yCoord + 0.5, endZ, endX, yCoord + 0.5, endZ).inflate(
                            Math.abs(dir.getStepX() * 0.5) + Math.abs(rot.getStepX() * 4.5),
                            0.5,
                            Math.abs(dir.getStepZ() * 0.5) + Math.abs(rot.getStepZ() * 4.5));

                    List<LivingEntity> affected = this.level.getEntitiesOfClass(LivingEntity.class, area);

                    for(LivingEntity e : affected) {
                        if(e.isAlive() && e.hurt(this.level.damageSources().source(NtmDamageTypes.TURBOFAN), 100)) {
                            // Das Original wirft hier nitra_small aus toten Monstern aus; den
                            // Gegenstand gibt es im Port nicht, siehe omitted.
                            SoundUtils.playAtVec3(this.level, new Vec3(e.getX(), e.getY(), e.getZ()),
                                    SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS,
                                    2.0F, 0.95F + this.level.random.nextFloat() * 0.2F);
                            this.spawnBloodBurst(e);
                        }
                    }
                }
            }

            this.networkPackNT(100);

        } else {

            this.lastSpin = this.spin;

            if(this.isOn && !this.isSuspended) {
                if(this.angle > 0) this.spin += 15F;

                Direction dir = this.getDir();
                Direction rot = dir.getClockWise(Axis.Y);

                this.level.addParticle(ParticleTypes.SMOKE,
                        xCoord + 0.5 + dir.getStepX() * 0.8125 + rot.getStepX() * 0.375,
                        yCoord + 1.5625,
                        zCoord + 0.5 + dir.getStepZ() * 0.8125 + rot.getStepZ() * 0.375,
                        0, 0, 0);
            }

            Player me = NuclearTechMod.proxy.me();

            if(this.isOn && !this.isSuspended && me != null && me.distanceToSqr(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 15 * 15) {
                if(this.audio == null) {
                    this.audio = this.createAudioLoop();
                    this.audio.startSound();
                } else if(!this.audio.isPlaying()) {
                    this.audio = this.rebootAudio(this.audio);
                }

                this.audio.keepAlive();
                this.audio.updateVolume(this.getVolume(1F));

            } else {
                if(this.audio != null) {
                    this.audio.stopSound();
                    this.audio = null;
                }
            }

            if(this.spin >= 360F) {
                this.spin -= 360F;
                this.lastSpin -= 360F;
            }

            this.prevAngle = this.angle;

            if(this.turnProgress > 0) {
                double d0 = Mth.wrapDegrees(this.syncAngle - (double) this.angle);
                this.angle = (float) ((double) this.angle + d0 / (double) this.turnProgress);
                --this.turnProgress;
            } else {
                this.angle = this.syncAngle;
            }
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        if(this.level == null) return null;
        return AudioWrapper.getLoopedSound(NtmSoundEvents.ENGINE_LOOP.get(), SoundSource.BLOCKS, this,
                1.0F, 10F, 1.0F + this.level.random.nextFloat() * 0.1F, 10);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    public static boolean canCut(BlockState state) {
        if(state.getBlock() instanceof BonemealableBlock) return true;
        if(state.is(Blocks.NETHER_WART)) return true;
        if(state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN)) return true;
        return false;
    }

    /** true, solange die Pflanze noch wachsen kann -- dann wird sie in Ruhe gelassen. */
    public static boolean shouldIgnore(Level level, BlockPos pos, BlockState state) {

        if(state.getBlock() instanceof StemBlock) return true;
        if(state.is(Blocks.NETHER_WART)) return state.getValue(NetherWartBlock.AGE) < 3;

        if(state.getBlock() instanceof BonemealableBlock growable) {
            return growable.isValidBonemealTarget(level, pos, state);
        }

        return false;
    }

    /** Entfernt die oberen beiden Bloecke einer dreiteiligen Pflanze wie Zuckerrohr oder Kaktus. */
    protected void cutCane(Block target, BlockPos pos) {
        if(!(this.level instanceof ServerLevel serverLevel)) return;

        // Manche stellen das Geraet einen Block zu hoch auf, das wird hier ausgeglichen
        int offset = serverLevel.getBlockState(pos.below()).is(target) ? -1 : 0;

        // von oben nach unten
        for(int i = 2 + offset; i > 0 + offset; i--) {
            BlockPos cut = pos.above(i);
            BlockState state = serverLevel.getBlockState(cut);

            for(ItemStack drop : Block.getDrops(state, serverLevel, cut, serverLevel.getBlockEntity(cut), null, ItemStack.EMPTY)) {
                this.dropItem(drop);
            }

            // destroyBlock ersetzt playAuxSFX(2001) + setBlock(air) des Originals
            serverLevel.destroyBlock(cut, false);
        }
    }

    /** Erntet Feldfruechte wie Weizen und pflanzt sie gleich wieder. */
    protected void cutCrop(BlockState state, BlockPos pos) {
        if(!(this.level instanceof ServerLevel serverLevel)) return;

        BlockState replacement = Blocks.AIR.defaultBlockState();

        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, serverLevel.getBlockEntity(pos), null, ItemStack.EMPTY);
        boolean replanted = false;

        for(ItemStack drop : drops) {
            // Das Original prueft drop.getItem() instanceof IPlantable. Das Interface gibt es in
            // 1.21 nicht mehr; BushBlock ist das naechste Gegenstueck und deckt Weizen, Karotten,
            // Kartoffeln, Rote Bete und Netherwarzen ab (alle ueber CropBlock bzw. NetherWartBlock).
            // Ein blosses "ist ein BlockItem" waere zu weit: Kuerbis und Melone sind keine
            // IPlantable, wuerden so aber zurueckgepflanzt -- der Drescher haette sie dauerhaft
            // ohne jeden Ertrag abgeerntet.
            if(!replanted && drop.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof BushBlock) {
                BlockState seedState = blockItem.getBlock().defaultBlockState();

                // Abweichung: das Original fragt nur den Boden (soil.canSustainPlant). canSurvive
                // prueft bei CropBlock zusaetzlich die Helligkeit, unter einem Dach schlaegt das
                // Wiederanpflanzen daher fehl und die Saat faellt stattdessen als Drop.
                if(seedState.canSurvive(serverLevel, pos)) {
                    replacement = seedState;
                    replanted = true;
                    drop.shrink(1);
                }
            }

            this.dropItem(drop);
        }

        // Bis 1.14 konnte ausgewachsener Weizen gelegentlich gar keine Samen fallen lassen.
        // Notbehelf des Originals, unveraendert uebernommen.
        if(state.is(Blocks.WHEAT) && !replanted) {
            replacement = Blocks.WHEAT.defaultBlockState();
        }

        serverLevel.destroyBlock(pos, false);
        serverLevel.setBlock(pos, replacement, 3);
    }

    protected void dropItem(ItemStack drop) {
        if(this.level == null || drop.isEmpty()) return;

        BlockPos bp = this.getBlockPos();
        Direction dir = this.getBlockState().getValue(MachineThresherBlock.FACING);
        double spawnX = bp.getX() + 0.5 - dir.getStepX() * 0.75;
        double spawnZ = bp.getZ() + 0.5 - dir.getStepZ() * 0.75;

        ItemEntity entityItem = new ItemEntity(this.level, spawnX, bp.getY(), spawnZ, drop);
        entityItem.setPickUpDelay(10);
        this.level.addFreshEntity(entityItem);

        // Der Summand 0.2 steht im Original bedingungslos hinter dem X-Anteil; das ist
        // dort so und bleibt so.
        entityItem.setDeltaMovement(dir.getStepX() * -0.2 + 0.2, 0, dir.getStepZ() * -0.2);
    }

    /** Ersatz fuer playAuxSFX(2001), wenn der Block stehen bleiben soll. */
    private void breakParticles(BlockPos pos, BlockState state) {
        if(!(this.level instanceof ServerLevel serverLevel)) return;
        PacketDistributor.sendToPlayersNear(serverLevel, null, pos.getX(), pos.getY(), pos.getZ(), 50,
                new ParticleBurst(pos, state.getBlock()));
    }

    /**
     * Im Original ein AuxParticlePacketNT mit "vanillaburst"/"blockdust" aus
     * Redstoneblock-Bruchstuecken. Der Port kennt diesen Partikeltyp nicht, daher
     * wird der vorhandene ParticleBurst mit demselben Block benutzt.
     */
    private void spawnBloodBurst(LivingEntity e) {
        if(!(this.level instanceof ServerLevel serverLevel)) return;

        double px = e.getX();
        double py = e.getY() + e.getBbHeight() * 0.5;
        double pz = e.getZ();

        PacketDistributor.sendToPlayersNear(serverLevel, null, px, py, pz, 50,
                new ParticleBurst(BlockPos.containing(px, py, pz), Blocks.REDSTONE_BLOCK));
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.isOn);
        buf.writeBoolean(this.isSuspended);
        buf.writeFloat(this.angle);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.isOn = buf.readBoolean();
        this.isSuspended = buf.readBoolean();
        this.syncAngle = buf.readFloat();
        this.turnProgress = 3; // dreifache Zwischenschritte fuer weichere Bewegung
        this.tank.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.isOn = tag.getBoolean("isOn");
        this.isSuspended = tag.getBoolean("isSuspended");
        this.angle = tag.getFloat("angle");
        this.state = tag.getInt("state");
        this.tank.readFromNBT(tag, "t");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("isOn", this.isOn);
        tag.putBoolean("isSuspended", this.isSuspended);
        tag.putFloat("angle", this.angle);
        tag.putInt("state", this.state);
        this.tank.writeToNBT(tag, "t");
    }

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank getTankToPaste() { return this.tank; }

    /** Der Maehbalken reicht weit ueber den Block hinaus. */
    public AABB getRenderBoundingBox() {

        if(this.renderBox == null) {
            BlockPos pos = this.worldPosition;
            this.renderBox = new AABB(
                    pos.getX() - 10,
                    pos.getY(),
                    pos.getZ() - 10,
                    pos.getX() + 11,
                    pos.getY() + 7,
                    pos.getZ() + 11);
        }

        return this.renderBox;
    }
}
