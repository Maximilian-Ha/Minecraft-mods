package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachinePollutingBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.menus.MachineTurbofanMenu;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.SoundUtils;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.particle.ParticleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineTurbofan.
 *
 * Verbrennt Flugturbinenkraftstoff (FuelGrade.AERO) zu Strom. Nachbrenner-Upgrades
 * erhoehen sowohl Durchsatz als auch Wirkungsgrad. Vor dem Einlass wird alles
 * angesaugt und im Einlass selbst geschreddert; das Blut sammelt sich in einem
 * eigenen Tank, der abgepumpt werden kann.
 *
 * Nicht uebernommen: IInfoProviderEC (CompatEnergyControl) gibt es im Port nicht,
 * ebenso wenig den Gegenstand flame_pony (Nachbrennerstufe 100) und die
 * Metadaten-Migration alter Mehrblock-Anordnungen aus 1.7.10.
 */
public class MachineTurbofanBlockEntity extends MachinePollutingBlockEntity
        implements IEnergyProviderMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IFluidCopiable {

    public static final int SLOT_FLUID_IN = 0;
    public static final int SLOT_FLUID_OUT = 1;
    public static final int SLOT_UPGRADE = 2;
    public static final int SLOT_BATTERY = 3;
    public static final int SLOT_IDENTIFIER = 4;

    private static final int SMOKE_BUFFER = 150;

    public long power;
    public static final long maxPower = 1_000_000L;
    public FluidTank tank;
    public FluidTank blood;

    public int afterburner;
    public boolean wasOn;
    public boolean showBlood = false;
    protected int output;
    protected int consumption;

    public float spin;
    public float lastSpin;
    public int momentum = 0;

    private AudioWrapper audio;

    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public MachineTurbofanBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_TURBOFAN.get(), pos, state, 5, SMOKE_BUFFER);
        this.tank = new FluidTank(Fluids.KEROSENE, 24000);
        this.blood = new FluidTank(Fluids.BLOOD, 24000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machine_turbofan");
    }

    public long getPowerScaled(long i) {
        return (this.power * i) / maxPower;
    }

    /**
     * Vier Anschluesse seitlich am Kern. Im Original ist dir die um 90 Grad
     * gedrehte Blickrichtung und rot die Gegendrehung davon -- also wieder die
     * Blickrichtung selbst. Beides bleibt hier unveraendert stehen.
     */
    protected DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING).getClockWise(Axis.Y);
        Direction rot = dir.getCounterClockWise(Axis.Y);

        BlockPos pos = this.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        return new DirPos[] {
                new DirPos(x + rot.getStepX() * 2, y, z + rot.getStepZ() * 2, rot),
                new DirPos(x + rot.getStepX() * 2 - dir.getStepX(), y, z + rot.getStepZ() * 2 - dir.getStepZ(), rot),
                new DirPos(x - rot.getStepX() * 2, y, z - rot.getStepZ() * 2, rot.getOpposite()),
                new DirPos(x - rot.getStepX() * 2 - dir.getStepX(), y, z - rot.getStepZ() * 2 - dir.getStepZ(), rot.getOpposite())
        };
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.output = 0;
            this.consumption = 0;

            this.tank.setType(SLOT_IDENTIFIER, this.slots);
            this.tank.loadTank(this.level, SLOT_FLUID_IN, SLOT_FLUID_OUT, this.slots);
            this.blood.setTankType(Fluids.BLOOD);

            this.wasOn = false;

            this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE, SLOT_UPGRADE);
            this.afterburner = this.upgradeManager.getLevel(UpgradeType.AFTERBURN);

            long burnValue = 0;
            int amount = 1 + this.afterburner;
            int amountToBurn = Math.min(amount, this.tank.getFill());

            boolean redstone = false;

            DirPos[] connections = this.getConPos();

            for(DirPos pos : connections) {
                // Ersatz fuer Compat.isPositionLoaded: keine Chunks nachladen (siehe com.hbm.util.Compat)
                if(!this.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;
                if(this.level.hasNeighborSignal(pos.makeCompat())) {
                    redstone = true;
                    break;
                }
            }

            if(!redstone) {

                if(this.tank.getTankType().hasTrait(FT_Combustible.class) && this.tank.getTankType().getTrait(FT_Combustible.class).getGrade() == FuelGrade.AERO) {
                    burnValue = this.tank.getTankType().getTrait(FT_Combustible.class).getCombustionEnergy() / 1_000L;
                }

                if(amountToBurn > 0) {
                    this.wasOn = true;
                    this.tank.setFill(this.tank.getFill() - amountToBurn);
                    this.output = (int) (burnValue * amountToBurn * (1 + Math.min(this.afterburner / 3D, 4)));
                    this.power += this.output;
                    this.consumption = amountToBurn;

                    if(this.level.getGameTime() % 20 == 0) this.pollute(this.tank.getTankType(), FluidReleaseType.BURN, amountToBurn * 5);
                }
            }

            this.power = Library.chargeItemsFromTE(this.slots, SLOT_BATTERY, this.power, this.power);

            // Ersatz fuer autoPort() aus 1.7.10: Strom anbieten, Treibstoff abonnieren, Blut abgeben
            for(DirPos pos : connections) {
                this.tryProvide(this.level, pos.makeCompat(), pos.getDir());
                this.trySubscribe(this.tank.getTankType(), this.level, pos);
                if(this.blood.getFill() > 0) this.tryProvide(this.blood, this.level, pos);
            }

            if(burnValue > 0 && amountToBurn > 0) {
                this.doBurnEffects();
            }

            if(this.power > maxPower) {
                this.power = maxPower;
            }

            this.networkPackNT(150);

        } else {

            this.lastSpin = this.spin;

            if(this.wasOn) {
                if(this.momentum < 100F)
                    this.momentum++;
            } else {
                if(this.momentum > 0)
                    this.momentum--;
            }

            this.spin += this.momentum / 2;

            if(this.spin >= 360) {
                this.spin -= 360F;
                this.lastSpin -= 360F;
            }

            if(this.momentum > 0) {

                if(this.audio == null) {
                    this.audio = this.createAudioLoop();
                    this.audio.startSound();
                } else if(!this.audio.isPlaying()) {
                    this.audio = this.rebootAudio(this.audio);
                }

                this.audio.keepAlive();
                this.audio.updateVolume(this.getVolume(this.momentum / 50F));
                this.audio.updatePitch(this.momentum / 200F + 0.5F + this.afterburner * 0.16F);

            } else {

                if(this.audio != null) {
                    this.audio.stopSound();
                    this.audio = null;
                }
            }

            this.doClientMotion();
        }
    }

    /** Partikel, Sog und der Haecksler im Einlass -- nur auf der Serverseite. */
    private void doBurnEffects() {
        if(!(this.level instanceof ServerLevel serverLevel)) return;

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING).getClockWise(Axis.Y);
        Direction rot = dir.getClockWise(Axis.Y);

        BlockPos bp = this.getBlockPos();
        int xCoord = bp.getX();
        int yCoord = bp.getY();
        int zCoord = bp.getZ();

        if(this.afterburner > 0) {

            for(int i = 0; i < 2; i++) {
                double speed = 2 + serverLevel.random.nextDouble() * 3;
                double deviation = serverLevel.random.nextGaussian() * 0.2;
                CompoundTag data = new CompoundTag();
                data.putFloat("scale", 8F);
                ParticleUtil.addParticle(serverLevel, new NbtParticleOptions(NtmParticleTypes.GAS_FLAME.get(), data),
                        xCoord + 0.5F - dir.getStepX() * (3 - i),
                        yCoord + 1.5F,
                        zCoord + 0.5F - dir.getStepZ() * (3 - i),
                        (float) (-dir.getStepX() * speed + deviation),
                        0F,
                        (float) (-dir.getStepZ() * speed + deviation),
                        150D);
            }

            if(this.afterburner > 90 && serverLevel.random.nextInt(30) == 0) {
                SoundUtils.playAtVec3(serverLevel, new Vec3(xCoord + 0.5, yCoord + 1.5, zCoord + 0.5),
                        NtmSoundEvents.TURBOFAN_DAMAGE.get(), SoundSource.BLOCKS, 3.0F, 0.95F + serverLevel.random.nextFloat() * 0.2F);
            }

            if(this.afterburner > 90) {
                CompoundTag data = new CompoundTag();
                data.putFloat("scale", 4F);
                ParticleUtil.addParticle(serverLevel, new NbtParticleOptions(NtmParticleTypes.GAS_FLAME.get(), data),
                        xCoord + 0.5F + dir.getStepX() * (serverLevel.random.nextDouble() * 4 - 2) + rot.getStepX() * (serverLevel.random.nextDouble() * 2 - 1),
                        yCoord + 1F + serverLevel.random.nextDouble() * 2,
                        // Vorzeichenfehler aus dem Original (dort "- dir.offsetZ"), bewusst uebernommen
                        zCoord + 0.5F - dir.getStepZ() * (serverLevel.random.nextDouble() * 4 - 2) + rot.getStepZ() * (serverLevel.random.nextDouble() * 2 - 1),
                        0F,
                        (float) (0.1 * serverLevel.random.nextDouble()),
                        0F,
                        150D);
            }
        }

        // Abgasstrahl hinter dem Triebwerk: schiebt und verbrennt
        for(Entity e : serverLevel.getEntitiesOfClass(Entity.class, this.makeBox(dir, rot, -3.5, -19.5))) {

            if(this.afterburner > 0) {
                e.igniteForSeconds(5);
                e.hurt(serverLevel.damageSources().onFire(), 5F);
            }
            this.push(e, dir);
        }

        // Sogbereich vor dem Einlass
        for(Entity e : serverLevel.getEntitiesOfClass(Entity.class, this.makeBox(dir, rot, 3.5, 8.5))) {
            this.push(e, dir);
        }

        // Der Einlass selbst
        for(Entity e : serverLevel.getEntitiesOfClass(Entity.class, this.makeBox(dir, rot, 3.5, 3.75))) {

            e.hurt(serverLevel.damageSources().source(NtmDamageTypes.TURBOFAN), 1000);
            this.makeStuck(e);

            if(!e.isAlive() && e instanceof LivingEntity) {
                CompoundTag vdat = new CompoundTag();
                vdat.putString("type", "giblets");
                vdat.putInt("ent", e.getId());
                vdat.putInt("cDiv", 5);
                double px = e.getX();
                double py = e.getY() + e.getBbHeight() * 0.5;
                double pz = e.getZ();
                PacketDistributor.sendToPlayersNear(serverLevel, null, px, py, pz, 150, new AuxParticle(vdat, px, py, pz));

                SoundUtils.playAtVec3(serverLevel, new Vec3(e.getX(), e.getY(), e.getZ()),
                        SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 2.0F, 0.95F + serverLevel.random.nextFloat() * 0.2F);

                this.blood.setFill(this.blood.getFill() + 50);
                if(this.blood.getFill() > this.blood.getMaxFill()) {
                    this.blood.setFill(this.blood.getMaxFill());
                }
                this.showBlood = true;
            }
        }
    }

    /**
     * Die drei Wirkungsbereiche des Originals, jeweils 3 Bloecke hoch und 3 breit.
     * near und far sind die Abstaende entlang der Triebwerksachse.
     */
    private AABB makeBox(Direction dir, Direction rot, double near, double far) {

        BlockPos pos = this.getBlockPos();

        double minX = pos.getX() + 0.5 + dir.getStepX() * near - rot.getStepX() * 1.5;
        double maxX = pos.getX() + 0.5 + dir.getStepX() * far + rot.getStepX() * 1.5;
        double minZ = pos.getZ() + 0.5 + dir.getStepZ() * near - rot.getStepZ() * 1.5;
        double maxZ = pos.getZ() + 0.5 + dir.getStepZ() * far + rot.getStepZ() * 1.5;

        return new AABB(Math.min(minX, maxX), pos.getY(), Math.min(minZ, maxZ), Math.max(minX, maxX), pos.getY() + 3, Math.max(minZ, maxZ));
    }

    /** Entspricht "motionX -= dir.offsetX * 0.2" aus dem Original. */
    private void push(Entity e, Direction dir) {
        Vec3 motion = e.getDeltaMovement();
        e.setDeltaMovement(motion.x - dir.getStepX() * 0.2, motion.y, motion.z - dir.getStepZ() * 0.2);
    }

    /**
     * Ersatz fuer Entity.setInWeb() aus 1.7.10. Die Faktoren sind die des
     * Spinnennetzes (0.25 / 0.05 / 0.25); das Original erreicht denselben Effekt
     * ueber das isInWeb-Flag, das es in 1.21 so nicht mehr gibt.
     */
    private void makeStuck(Entity e) {
        Vec3 motion = e.getDeltaMovement();
        e.setDeltaMovement(motion.x * 0.25, motion.y * 0.05, motion.z * 0.25);
    }

    /**
     * Sog und Haecksler muessen auf dem Client wiederholt werden, aber nur fuer
     * den Spieler an der Tastatur -- die Bewegung wird nie vom Server geschickt,
     * sonst gaebe es Desync. Wie im Original.
     */
    private void doClientMotion() {
        if(this.level == null) return;
        if(!this.wasOn) return;

        Player me = NuclearTechMod.proxy.me();
        if(me == null || me.getAbilities().instabuild) return;

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING).getClockWise(Axis.Y);
        Direction rot = dir.getClockWise(Axis.Y);

        for(Entity e : this.level.getEntitiesOfClass(Entity.class, this.makeBox(dir, rot, -3.5, -19.5))) {
            if(e == me) this.push(e, dir);
        }

        for(Entity e : this.level.getEntitiesOfClass(Entity.class, this.makeBox(dir, rot, 3.5, 8.5))) {
            if(e == me) this.push(e, dir);
        }

        for(Entity e : this.level.getEntitiesOfClass(Entity.class, this.makeBox(dir, rot, 3.5, 3.75))) {
            if(e == me) this.makeStuck(e);
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(NtmSoundEvents.TURBOFAN_LOOP.get(), SoundSource.BLOCKS, this, 1.0F, 50F, 1.0F, 20);
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.power = tag.getLong("powerTime");
        this.tank.readFromNBT(tag, "fuel");
        this.blood.readFromNBT(tag, "blood");
        this.showBlood = tag.getBoolean("showBlood");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putLong("powerTime", this.power);
        this.tank.writeToNBT(tag, "fuel");
        this.blood.writeToNBT(tag, "blood");
        tag.putBoolean("showBlood", this.showBlood);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeByte((byte) this.afterburner);
        buf.writeBoolean(this.wasOn);
        buf.writeBoolean(this.showBlood);
        this.tank.serialize(buf);
        this.blood.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.afterburner = buf.readByte();
        this.wasOn = buf.readBoolean();
        this.showBlood = buf.readBoolean();
        this.tank.deserialize(buf);
        this.blood.deserialize(buf);
    }

    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return maxPower; }
    @Override public void setPower(long i) { this.power = i; }

    private AABB renderBox = null;

    /** Ersetzt INFINITE_EXTENT_AABB aus 1.7.10 -- deckt den gesamten Mehrblock ab */
    public AABB getRenderBoundingBox() {

        if(this.renderBox == null) {
            BlockPos pos = this.worldPosition;
            this.renderBox = new AABB(pos.getX() - 5, pos.getY(), pos.getZ() - 5, pos.getX() + 6, pos.getY() + 4, pos.getZ() + 6);
        }

        return this.renderBox;
    }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }
    /** Wie im Original: nur Blut geht raus, der Rauch bleibt im Puffer. */
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.blood }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank, this.blood, this.smoke, this.smokeLeaded, this.smokePoison }; }

    @Override
    public FluidTank getTankToPaste() {
        return this.tank;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_FLUID_IN) return true;
        if(slot == SLOT_UPGRADE) return stack.getItem() instanceof MachineUpgradeItem item && this.getValidUpgrades().containsKey(item.type);
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_IDENTIFIER) return stack.getItem() instanceof IItemFluidIdentifier;
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        // Wie im Original: TileEntityMachineBase.getAccessibleSlotsFromSide gibt ein leeres
        // Feld zurueck und der Turbofan ueberschreibt das nicht. Trichter und Rohre kommen
        // also an keinen Slot heran.
        return new int[0];
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.AFTERBURN;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {
        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_TURBOFAN.get()));
        if(type == UpgradeType.AFTERBURN) {
            components.add(Component.translatable(KEY_EFFICIENCY, "+" + (int) (lvl * 100 * (1 + Math.min(lvl / 3D, 4D))) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 100) + "%").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.AFTERBURN, 3);
        return upgrades;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineTurbofanMenu(id, inventory, this);
    }
}
