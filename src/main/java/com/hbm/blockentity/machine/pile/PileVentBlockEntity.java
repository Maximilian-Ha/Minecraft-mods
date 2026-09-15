package com.hbm.blockentity.machine.pile;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.pile.PileCoreBlockEntity.PileChannel;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.pile.PileBlock;
import com.hbm.blocks.states.PileBlockType;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.pile.TileEntityPileVent.
 *
 * Der Luefter. Er nimmt Pressluft aus dem Rohrnetz auf der einen Seite und drueckt sie in den
 * Lueftungskanal auf der anderen. Was im Kanal ankommt, zieht Waerme aus den Brennstoffkanaelen
 * auf gleicher Hoehe.
 */
public class PileVentBlockEntity extends PileDeviceBaseBlockEntity implements IFluidStandardReceiverMK2 {

    public final FluidTank compair;
    public boolean isActive = false;

    /* Nur zum Zeichnen: der Winkel des Luefterrads. */
    public float fan;
    public float lastFan;

    public PileVentBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.PILE_VENT.get(), pos, state);
        this.compair = new FluidTank(Fluids.AIR, 4_000).withPressure(1);
    }

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] {this.compair}; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {this.compair}; }

    @Override
    public boolean canConnect(FluidType type, Direction dir) {
        if(type != this.compair.getTankType()) return false;
        return dir == this.getOrientation();
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.level.isClientSide) {

            this.lastFan = this.fan;

            if(this.isActive) {
                this.fan += 45F;
                if(this.level.random.nextInt(20) == 0) {
                    this.level.addParticle(ParticleTypes.CLOUD,
                            this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 1D, this.worldPosition.getZ() + 0.5D,
                            0D, 0.05D, 0D);
                }
            }

            if(this.fan >= 360F) {
                this.lastFan -= 360F;
                this.fan -= 360F;
            }

            return;
        }

        Direction dir = this.getOrientation();
        this.trySubscribe(this.compair.getTankType(), this.level, new DirPos(this.worldPosition.relative(dir), dir));

        this.isActive = false;

        BlockPos pilePos = this.worldPosition.relative(dir.getOpposite());
        BlockState state = this.level.getBlockState(pilePos);

        if(state.is(NtmBlocks.PILE_BLOCK.get()) && state.getValue(PileBlock.TYPE) == PileBlockType.AIR_IN) {

            PileCoreBlockEntity core = this.getCore(pilePos);

            if(core != null) {
                PileChannel chan = core.getChannel(pilePos, core.ventilationChannels);

                if(chan != null) {
                    this.chanNum = core.ventilationChannels.indexOf(chan);
                    int toFill = Math.min(this.compair.getFill(), PileChannel.MAX_AIR - chan.air);
                    chan.air += toFill;
                    this.compair.setFill(this.compair.getFill() - toFill);
                    this.isActive = toFill > 0;
                }
            }
        }

        this.networkPackNT(35);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.isActive);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.isActive = buf.readBoolean();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        this.compair.readFromNBT(tag, "t");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);
        this.compair.writeToNBT(tag, "t");
    }
}
