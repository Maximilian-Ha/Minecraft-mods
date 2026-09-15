package com.hbm.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.TileEntityTickingBase.
 *
 * Eine Block-Entitaet, die tickt. Der einzige Zweck ist, updateEntity abstrakt zu halten: wer
 * davon ableitet, muss sie schreiben und kann sie nicht versehentlich falsch benennen.
 *
 * Der Port hat dafuer bereits die Schnittstelle ITickable; diese Klasse bindet sie ein, damit
 * abgeleitete Block-Entitaeten sie nicht einzeln mitfuehren muessen.
 */
public abstract class TickingBaseBlockEntity extends LoadedBaseBlockEntity implements ITickable {

    public TickingBaseBlockEntity(BlockEntityType<? extends TickingBaseBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public abstract void updateEntity();
}
