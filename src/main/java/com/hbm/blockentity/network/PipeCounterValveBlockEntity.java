package com.hbm.blockentity.network;

import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.FluidValveBlock;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.uninos.UniNodespace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityFluidCounterValve.
 *
 * Ein Ventil, das zusaetzlich mitzaehlt, wie viel durch es hindurchgegangen ist. Der
 * Zaehler laeuft weiter, solange das Ventil offen ist, und bleibt beim Zudrehen stehen --
 * das Original zaehlt dabei den angefangenen Tick noch zu Ende, bevor es den Knoten
 * abbaut, und das bleibt hier so.
 *
 * NICHT UEBERNOMMEN: die OpenComputers-Anbindung (getComponentName und die fuenf
 * Callbacks). Die Mod ist im Port nicht angebunden; die gleichen Werte stehen ueber
 * Redstone-ueber-Funk bereit, und das ist portiert.
 */
public class PipeCounterValveBlockEntity extends PipeValveBlockEntity implements IRORValueProvider, IRORInteractive {

    private long counter;

    /** Der Tick, in dem zuletzt gezaehlt wurde -- damit derselbe Tick nicht doppelt zaehlt. */
    private long lastCounterUpdate = -1;

    public PipeCounterValveBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FLUID_COUNTER_VALVE.get(), pos, state);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if(this.level != null && !this.level.isClientSide) {
            this.zaehle();
            this.networkPackNT(25);
        }
    }

    private void zaehle() {
        if(this.zaehlungOffen()) {
            this.counter += this.node.net.fluidTracker;
            this.lastCounterUpdate = this.level.getGameTime();
        }
    }

    private boolean zaehlungOffen() {
        return this.level != null && this.node != null && this.node.net != null
                && this.type != Fluids.NONE && this.lastCounterUpdate != this.level.getGameTime();
    }

    /** Der Stand einschliesslich des laufenden Ticks -- fuer die Anzeige, nicht zum Speichern. */
    private long aktuellerStand() {
        return this.zaehlungOffen() ? this.counter + this.node.net.fluidTracker : this.counter;
    }

    public long getCounter() {
        return this.counter;
    }

    @Override
    public void updateState() {
        if(!this.istOffen() && this.node != null) {
            this.zaehle();
            UniNodespace.destroyNode(this.level, this.worldPosition, this.type.getNetworkProvider());
            this.node = null;
        }
    }

    /** Umlegen von aussen, also ueber Redstone-ueber-Funk. */
    private void setzeZustand(boolean offen) {
        if(this.level == null) return;
        this.level.setBlock(this.worldPosition, this.getBlockState().setValue(FluidValveBlock.OPEN, offen),
                Block.UPDATE_CLIENTS);
        this.level.playSound(null, this.worldPosition, NtmSoundEvents.LEVER.get(),
                SoundSource.BLOCKS, 1.0F, 1.0F);
        this.updateState();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.counter = tag.getLong("counter");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("counter", this.counter);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.counter);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.counter = Math.max(buf.readLong(), 0);
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                PREFIX_VALUE + "value",
                PREFIX_VALUE + "state",
                PREFIX_FUNCTION + "reset",
                PREFIX_FUNCTION + "setstate" + NAME_SEPARATOR + "state",
        };
    }

    @Override
    public String provideRORValue(String name) {
        if((PREFIX_VALUE + "value").equals(name)) return String.valueOf(this.aktuellerStand());
        if((PREFIX_VALUE + "state").equals(name)) return String.valueOf(this.istOffen() ? 1 : 0);
        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {

        if(name.equals(PREFIX_FUNCTION + "reset")) {
            this.zaehle();
            this.counter = 0;
            this.setChanged();

        } else if(name.equals(PREFIX_FUNCTION + "setstate") && params.length > 0) {
            this.setzeZustand(IRORInteractive.parseInt(params[0], 0, 1) == 1);
        }

        return null;
    }
}
