package com.hbm.blockentity.network;

import api.hbm.energymk2.IEnergyConnectorMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.CableDiodeBlock;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.util.Compat;
import com.hbm.util.EnumUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CableDiode.TileEntityDiode.
 *
 * Die Diode nimmt Strom nur von den Seiten entgegen, die nicht ihre Ausgangsseite sind,
 * und gibt ihn ausschliesslich nach vorn weiter. Pro Tick laesst sie hoechstens "limit"
 * HE durch; das Feld "power" ist dabei kein Speicher, sondern ein Zaehler fuer die im
 * laufenden Tick bereits durchgereichte Menge und wird jeden Tick auf 0 gesetzt.
 */
public class DiodeBlockEntity extends LoadedBaseBlockEntity implements IEnergyReceiverMK2, IControlReceiver, ITickable {

    /** Used as an intra-tick tracker for how much energy has been transmitted, resets to 0 each tick and maxes out based on transfer */
    private long power;
    private boolean recursionBrake = false;
    private int pulses = 0;
    public ConnectionPriority priority = ConnectionPriority.NORMAL;
    public long limit = 1_000;

    /** Obergrenze der einstellbaren Durchsatzbegrenzung, 1:1 aus dem Original */
    public static final long MAX_LIMIT = 10_000_000_000L;

    public DiodeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.NETWORK_CABLE_DIODE.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // Altbestand aus dem Original: frueher stand hier eine Zehnerpotenz-Stufe statt der Zahl
        if(tag.contains("level")) {
            this.limit = (long) Math.pow(10, tag.getInt("level"));
        } else {
            this.limit = tag.getLong("limit");
        }
        this.priority = EnumUtil.grabEnumSafely(ConnectionPriority.class, tag.getByte("p"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putLong("limit", this.limit);
        tag.putByte("p", (byte) this.priority.ordinal());
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeByte((byte) this.priority.ordinal());
        buf.writeLong(this.limit);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.priority = EnumUtil.grabEnumSafely(ConnectionPriority.class, buf.readByte());
        this.limit = buf.readLong();
    }

    /** Die Ausgangsseite: das Gegenstueck zur Blickrichtung des Blocks */
    public Direction getDir() {
        BlockState state = this.getBlockState();
        if(!state.hasProperty(CableDiodeBlock.FACING)) return Direction.NORTH;
        return state.getValue(CableDiodeBlock.FACING).getOpposite();
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {

            for(Direction dir : Direction.values()) {
                if(dir == this.getDir()) continue;
                this.trySubscribe(this.level, this.getBlockPos().relative(dir), dir);
            }

            this.pulses = 0;
            this.setPower(0); //tick is over, reset our allowed transfer
            this.networkPackNT(15);
        }
    }

    @Override
    public boolean canConnect(Direction dir) {
        return dir != this.getDir();
    }

    @Override
    public long transferPower(long power) {

        if(this.recursionBrake) return power;
        if(this.level == null) return power;

        this.pulses++;
        if(this.getPower() >= this.getMaxPower() || this.pulses > 10) return power; //if we have already maxed out transfer or max pulses, abort

        this.recursionBrake = true;

        Direction dir = this.getDir();
        BlockPos target = this.getBlockPos().relative(dir);
        PowerNode node = Nodespace.getNode(this.level, target);
        BlockEntity be = Compat.getBlockEntityStandard(this.level, target);

        if(node != null && !node.expired && node.hasValidNet() && be instanceof IEnergyConnectorMK2 con && con.canConnect(dir.getOpposite())) {
            long toTransfer = Math.min(power, this.getReceiverSpeed());
            long remainder = node.net.sendPowerDiode(toTransfer);
            long transferred = (toTransfer - remainder);
            this.power += transferred;
            power -= transferred;

        } else if(be instanceof IEnergyReceiverMK2 rec && be != this) {
            if(rec.canConnect(dir.getOpposite())) {
                long toTransfer = Math.min(power, rec.getReceiverSpeed());
                long remainder = rec.transferPower(toTransfer);
                power -= (toTransfer - remainder);
                this.recursionBrake = false;
                return power;
            }
        }

        this.recursionBrake = false;
        return power;
    }

    @Override public long getReceiverSpeed() { return this.getMaxPower() - this.getPower(); }
    @Override public long getMaxPower() { return this.limit; }
    @Override public long getPower() { return Math.min(this.power, this.getMaxPower()); }
    @Override public void setPower(long power) { this.power = power; }
    @Override public ConnectionPriority getPriority() { return this.priority; }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.getBlockPos().getX() + 0.5D, this.getBlockPos().getY() + 0.5D, this.getBlockPos().getZ() + 0.5D) <= 128;
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("limit")) this.limit = tag.getLong("limit");
        if(tag.contains("priority")) this.priority = EnumUtil.grabEnumSafely(ConnectionPriority.class, tag.getByte("priority"));
        if(this.limit < 0) this.limit = 0;
        if(this.limit > MAX_LIMIT) this.limit = MAX_LIMIT;
        this.setChanged();
    }
}
