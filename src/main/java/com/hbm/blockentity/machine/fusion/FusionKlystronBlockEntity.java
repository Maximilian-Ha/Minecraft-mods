package com.hbm.blockentity.machine.fusion;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.FusionKlystronMenu;
import com.hbm.lib.Library;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.KlystronNetworkProvider;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.fusion.TileEntityFusionKlystron.
 *
 * Das Klystron heizt das Plasma des Torus auf. Es wandelt Strom und Pressluft in Zuendenergie --
 * bis zu einer Million Einheiten je Tick, und wie viel davon, stellt man selbst ein.
 *
 * Die eingestellte Leistung bestimmt auch die Groesse des noetigen Stromspeichers: das Hundertfache
 * der Zielleistung, mindestens aber eine Million. Wer viel will, muss also erst puffern.
 *
 * Wie ueberall in der Fusion ist die Fahrt stufenlos: Strom- und Luftvorrat zaehlen anteilig, und
 * der kleinere gibt das Tempo vor. Faellt die Leistung unter ein Fuenfzigstel des Ziels, schaltet
 * das Klystron ganz ab -- ein Klystron, das nur tropft, zuendet ohnehin nichts.
 *
 * ABWEICHUNG: NICHT UEBERNOMMEN ist die OpenComputers-Anbindung (ENTSCHEIDUNGEN.md).
 */
public class FusionKlystronBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardReceiverMK2, IControlReceiver {

    public static final int SLOT_BATTERY = 0;

    public static final long MAX_OUTPUT = 1_000_000;
    public static final int AIR_CONSUMPTION = 2_500;

    protected GenNode<?> klystronNode;

    public long outputTarget;
    public long output;
    public long power;
    public long maxPower;

    /** Nur Client: der Winkel des Luefters. */
    public float fan;
    public float prevFan;
    public float fanSpeed;
    public static final float FAN_ACCELERATION = 0.125F;

    public final FluidTank compair;

    private AudioWrapper audio;
    private AABB renderBox;

    public FusionKlystronBlockEntity(BlockPos pos, BlockState state) {
        this(NtmBlockEntityTypes.FUSION_KLYSTRON.get(), pos, state);
    }

    protected FusionKlystronBlockEntity(BlockEntityType<? extends FusionKlystronBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1);

        this.compair = new FluidTank(Fluids.AIR, AIR_CONSUMPTION * 60);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.fusionKlystron");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {
            this.serverUpdate();
        } else {
            this.clientUpdate();
        }
    }

    protected void serverUpdate() {

        this.maxPower = Math.max(1_000_000L, this.outputTarget * 100L);
        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, this.maxPower);

        for(DirPos pos : this.getConPos()) {
            this.trySubscribe(this.level, pos);
            this.trySubscribe(this.compair.getTankType(), this.level, pos);
        }

        this.output = 0;

        double powerFactor = FusionTorusBlockEntity.getSpeedScaled(this.maxPower, this.power);
        double airFactor = FusionTorusBlockEntity.getSpeedScaled(this.compair.getMaxFill(), this.compair.getFill());
        double factor = Math.min(powerFactor, airFactor);

        long powerReq = (long) Math.ceil(this.outputTarget * factor);
        int airReq = (int) Math.ceil(AIR_CONSUMPTION * factor);

        if(this.outputTarget > 0 && this.power >= powerReq && this.compair.getFill() >= airReq) {
            this.output = powerReq;
            this.power -= powerReq;
            this.compair.setFill(this.compair.getFill() - airReq);
        }

        if(this.output < this.outputTarget / 50) this.output = 0;

        this.klystronNode = handleKNode(this.klystronNode, this);
        provideKyU(this.klystronNode, this.output);

        this.networkPackNT(100);
    }

    protected void clientUpdate() {

        double mult = FusionTorusBlockEntity.getSpeedScaled(this.outputTarget, this.output);

        if(this.output > 0) this.fanSpeed += FAN_ACCELERATION * mult;
        else this.fanSpeed -= FAN_ACCELERATION;

        this.fanSpeed = Mth.clamp(this.fanSpeed, 0F, 5F * (float) mult);

        this.prevFan = this.fan;
        this.fan += this.fanSpeed;

        if(this.fan >= 360F) {
            this.fan -= 360F;
            this.prevFan -= 360F;
        }

        Player me = Minecraft.getInstance().player;
        boolean near = me != null && me.distanceToSqr(
                this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 2.5, this.worldPosition.getZ() + 0.5) < 30 * 30;

        if(this.fanSpeed > 0 && near) {

            float speed = this.fanSpeed / 5F;

            if(this.audio == null) {
                this.audio = AudioWrapper.getLoopedSound(NtmSoundEvents.FEL_LOOP.get(), SoundSource.BLOCKS,
                        this.worldPosition.getX() + 0.5F, this.worldPosition.getY() + 2.5F, this.worldPosition.getZ() + 0.5F,
                        this.getVolume(speed), 15F, speed, 20);
                this.audio.startSound();
            } else {
                this.audio.updateVolume(this.getVolume(speed));
                this.audio.updatePitch(speed);
                this.audio.keepAlive();
            }

        } else if(this.audio != null) {
            if(this.audio.isPlaying()) this.audio.stopSound();
            this.audio = null;
        }
    }

    /**
     * Sorgt dafuer, dass der Knoten im Klystronnetz da ist und das Klystron darin als Lieferant
     * steht. Der Knoten liegt vier Bloecke vor der Muendung -- dort, wo der Torusarm endet.
     */
    public static GenNode<?> handleKNode(GenNode<?> klystronNode, BlockEntity that) {

        Level level = that.getLevel();
        BlockPos pos = that.getBlockPos();

        if(klystronNode == null || klystronNode.expired) {

            Direction dir = that.getBlockState().getValue(DummyableBlock.FACING).getOpposite();
            BlockPos nodePos = pos.offset(dir.getStepX() * 4, 2, dir.getStepZ() * 4);

            klystronNode = UniNodespace.getNode(level, nodePos, KlystronNetworkProvider.THE_PROVIDER);

            if(klystronNode == null) {
                klystronNode = new GenNode<>(KlystronNetworkProvider.THE_PROVIDER, nodePos)
                        .setConnections(new DirPos(pos.offset(dir.getStepX() * 5, 2, dir.getStepZ() * 5), dir));

                UniNodespace.createNode(level, klystronNode);
            }
        }

        if(klystronNode.net != null) klystronNode.net.addProvider(that);

        return klystronNode;
    }

    /** Schiebt die Leistung in den Torus am anderen Ende des Netzes. */
    public static boolean provideKyU(GenNode<?> klystronNode, long output) {

        if(klystronNode == null || klystronNode.net == null) return false;

        for(Object key : klystronNode.net.receiverEntries.keySet()) {

            /* Nur der Torus nimmt Klystronenergie ab; gaebe es je einen zweiten, kaeme hier eine
             * eigene Schnittstelle hin -- so steht es auch im Original. */
            if(key instanceof FusionTorusBlockEntity torus && torus.isLoaded() && !torus.isRemoved()) {
                torus.klystronEnergy += output;
                return true;
            }
        }

        return false;
    }

    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise();

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + dir.getStepX() * 4, y + 2, z + dir.getStepZ() * 4, dir),
                new DirPos(x + rot.getStepX() * 3, y, z + rot.getStepZ() * 3, rot),
                new DirPos(x - rot.getStepX() * 3, y, z - rot.getStepZ() * 3, rot.getOpposite())
        };
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

        if(this.level != null && !this.level.isClientSide && this.klystronNode != null) {
            UniNodespace.destroyNode(this.level, this.klystronNode);
        }
    }

    private void stopAudio() {
        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeLong(this.maxPower);
        buf.writeLong(this.outputTarget);
        buf.writeLong(this.output);
        this.compair.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.outputTarget = buf.readLong();
        this.output = buf.readLong();
        this.compair.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.maxPower = tag.getLong("maxPower");
        this.outputTarget = tag.getLong("outputTarget");
        this.compair.readFromNBT(tag, "t");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putLong("maxPower", this.maxPower);
        tag.putLong("outputTarget", this.outputTarget);
        this.compair.writeToNBT(tag, "t");
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_BATTERY;
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return this.maxPower; }

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.compair }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.compair }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FusionKlystronMenu(id, inventory, this);
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 2.5, this.worldPosition.getZ() + 0.5) < 20 * 20;
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("amount")) {
            this.outputTarget = Mth.clamp(tag.getLong("amount"), 0L, MAX_OUTPUT);
            this.setChanged();
        }
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 4, y, z - 4, x + 5, y + 5, z + 5);
        }
        return this.renderBox;
    }
}
