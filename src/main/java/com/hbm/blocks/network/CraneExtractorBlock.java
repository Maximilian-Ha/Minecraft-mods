package com.hbm.blocks.network;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.CraneExtractorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CraneExtractor.
 *
 * Der Auszieher holt aus der Maschine an seiner Ausgangsseite und setzt das Geholte auf das Band
 * an seiner Eingangsseite. Die Vertauschung ist die des Originals -- die beiden Seiten heissen
 * dort nach der Sicht des Bandes, nicht nach der der Maschine.
 */
public class CraneExtractorBlock extends CraneBaseBlock {

    public static final MapCodec<CraneExtractorBlock> CODEC = simpleCodec(CraneExtractorBlock::new);

    public CraneExtractorBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends CraneBaseBlock> codec() { return CODEC; }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraneExtractorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, NtmBlockEntityTypes.CRANE_EXTRACTOR.get(), (l, p, s, be) -> be.updateEntity());
    }

    /**
     * Beim Abbau faellt NUR das Zwischenlager heraus. Die neun Musterfaecher halten Abbilder,
     * keine Gegenstaende -- sie fallen zu lassen hiesse, sie aus dem Nichts zu erzeugen. Das
     * Original laesst aus demselben Grund nur die Faecher neun bis siebzehn fallen.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CraneExtractorBlockEntity be) {

            for(int i = CraneExtractorBlockEntity.PATTERNS; i < CraneExtractorBlockEntity.SLOTS; i++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.getItem(i));
                be.setItem(i, ItemStack.EMPTY);
            }
        }

        super.onRemove(state, level, pos, newState, moved);
    }
}
