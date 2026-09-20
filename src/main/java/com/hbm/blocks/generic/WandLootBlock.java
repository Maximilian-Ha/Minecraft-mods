package com.hbm.blocks.generic;

import com.hbm.blockentity.WandLootBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockWandLoot.
 *
 * EIN ZAUBERSTAB IST KEIN BLOCK DER WELT, sondern eine Marke in einer Bauwerksdatei. Wer ein
 * Bauwerk baut, setzt an die Stelle, an der spaeter Beute liegen soll, einen Beutestab und
 * traegt in ihm ein, WAS dort entstehen soll: welcher Block, aus welchem Vorrat, wie viele
 * Zuege. Beim Erzeugen des Bauwerks ersetzt sich der Stab durch genau das.
 *
 * DER TAUSCH GESCHIEHT BEIM ERSTEN SERVERTICK, wie im Original
 * (TileEntityWandLoot.updateEntity ruft replace, sobald triggerReplace steht). In 1.21 heisst
 * das: die Blockentitaet bekommt einen Ticker, und der erste Durchlauf ersetzt den Block. Der
 * Umweg ueber den Tick ist kein Notbehelf -- die Strukturvorlage setzt erst alle Bloecke und
 * danach ihre Blockentitaeten; wer schon beim Setzen ersetzen wollte, haette die Eintraege
 * noch nicht.
 *
 * DIE DREI ANDEREN ZAUBERSTAEBE des Originals -- wand_jigsaw, wand_logic, wand_tandem -- sind
 * im Port keine Bloecke: der Jigsaw-Stab ist in 1.21 der Jigsaw-Block von Vanilla, die beiden
 * anderen gehoeren zu Bauwerken, die der Port noch nicht baut.
 */
public class WandLootBlock extends BaseEntityBlock {

    public static final MapCodec<WandLootBlock> CODEC = simpleCodec(WandLootBlock::new);

    public WandLootBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<WandLootBlock> codec() { return CODEC; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WandLootBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide) return null;
        return createTickerHelper(type, NtmBlockEntityTypes.WAND_LOOT.get(), WandLootBlockEntity::serverTick);
    }
}
