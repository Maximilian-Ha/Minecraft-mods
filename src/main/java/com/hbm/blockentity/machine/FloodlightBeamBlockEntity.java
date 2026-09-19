package com.hbm.blockentity.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.NtmBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: FloodlightBeam.TileEntityFloodlightBeam.
 *
 * Der Lichtfleck merkt sich, welches Flutlicht ihn gesetzt hat und der wievielte seiner
 * fuenfzehn Strahlen er ist. Alle fuenf Ticks prueft er nach, ob das noch stimmt, und
 * loescht sich selbst, wenn nicht -- so verschwinden Lichtflecke auch dann, wenn das
 * Flutlicht ohne Vorwarnung weg ist.
 */
public class FloodlightBeamBlockEntity extends BlockEntity implements ITickable {

    private BlockPos quelle;
    private int nummer;

    /** Nur zwischengespeichert, nie gesichert: das Flutlicht selbst. */
    private FloodlightBlockEntity zwischenspeicher;

    public FloodlightBeamBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FLOODLIGHT_BEAM.get(), pos, state);
    }

    public void setzeQuelle(FloodlightBlockEntity flutlicht, int nummer) {
        this.zwischenspeicher = flutlicht;
        this.quelle = flutlicht.getBlockPos();
        this.nummer = nummer;
        this.setChanged();
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;
        if(this.level.getGameTime() % 5 != 0) return;

        if(this.quelle == null) {
            this.level.removeBlock(this.worldPosition, false);
            return;
        }

        if(this.zwischenspeicher == null) {
            // Erst nachsehen, wenn der Abschnitt der Quelle geladen ist: sonst wuerde ein
            // Lichtfleck am Rand der Sichtweite sich selbst loeschen.
            if(!this.level.isLoaded(this.quelle)) return;

            if(this.level.getBlockEntity(this.quelle) instanceof FloodlightBlockEntity flutlicht) {
                this.zwischenspeicher = flutlicht;
            } else {
                this.level.removeBlock(this.worldPosition, false);
                return;
            }
        }

        if(this.zwischenspeicher.isRemoved()
                || !this.zwischenspeicher.brennt()
                || !this.worldPosition.equals(this.zwischenspeicher.lichtOrt(this.nummer))) {
            this.level.removeBlock(this.worldPosition, false);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.quelle = tag.contains("quelleX")
                ? new BlockPos(tag.getInt("quelleX"), tag.getInt("quelleY"), tag.getInt("quelleZ"))
                : null;
        this.nummer = tag.getInt("nummer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if(this.quelle != null) {
            tag.putInt("quelleX", this.quelle.getX());
            tag.putInt("quelleY", this.quelle.getY());
            tag.putInt("quelleZ", this.quelle.getZ());
        }
        tag.putInt("nummer", this.nummer);
    }
}
