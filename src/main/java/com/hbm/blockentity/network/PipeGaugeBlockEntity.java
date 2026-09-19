package com.hbm.blockentity.network;

import api.hbm.redstoneoverradio.IRORValueProvider;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.FluidDuctGauge.TileEntityPipeGauge.
 *
 * Ein ganz normales Rohrstueck, das nebenbei mitschreibt, wie viel Fluid im Netz
 * bewegt wurde: deltaTick ist der Zaehler des laufenden Ticks, deltaLastSecond die
 * Summe der letzten vollen Sekunde.
 */
public class PipeGaugeBlockEntity extends PipeBaseBlockEntity implements IRORValueProvider {

    public long deltaTick = 0;
    private long deltaSecond = 0;
    public long deltaLastSecond = 0;

    public PipeGaugeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FLUID_DUCT_GAUGE.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level != null && !this.level.isClientSide) {

            if(this.node != null && this.node.net != null && this.type != Fluids.NONE) {

                this.deltaTick = this.node.net.fluidTracker;
                if(this.level.getGameTime() % 20 == 0) {
                    this.deltaLastSecond = this.deltaSecond;
                    this.deltaSecond = 0;
                }
                this.deltaSecond += this.deltaTick;
            }
        }

        // Die Basis verschickt am Ende ihren Netzwerkpuffer. Die Zaehler stehen deshalb
        // vorher schon fest und reisen im selben Paket mit, statt ein zweites zu kosten.
        super.updateEntity();
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.deltaTick);
        buf.writeLong(this.deltaLastSecond);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.deltaTick = Math.max(buf.readLong(), 0);
        this.deltaLastSecond = Math.max(buf.readLong(), 0);
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                PREFIX_VALUE + "deltatick",
                PREFIX_VALUE + "deltasecond",
        };
    }

    @Override
    public String provideRORValue(String name) {
        if((PREFIX_VALUE + "deltatick").equals(name))   return "" + this.deltaTick;
        if((PREFIX_VALUE + "deltasecond").equals(name)) return "" + this.deltaLastSecond;
        return null;
    }
}
