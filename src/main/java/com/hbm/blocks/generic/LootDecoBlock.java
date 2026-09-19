package com.hbm.blocks.generic;

import com.hbm.blockentity.LootDecoBlockEntity;
import com.hbm.util.Tuple.Quartet;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockLoot.
 *
 * Der Beutesockel. Ein Blech von einem Sechzehntel Blockhoehe, das selbst nichts zeigt -- was
 * man sieht, sind die Gegenstaende, die auf ihm liegen. Die Weltgenerierung streut damit
 * Gerumpel in ihre Bauwerke: ein Gewehr auf einem Tisch, Patronen daneben, eine Dose auf dem
 * Boden.
 *
 * ER GIBT SICH SELBST NICHT HER. Was beim Zerschlagen herausfaellt, sind die Stapel, die auf
 * ihm liegen; den Sockel selbst bekommt man nicht. Deshalb auch kein Kreativreiter -- im
 * Original setCreativeTab(null).
 *
 * RECHTSKLICK OHNE SCHLEICHEN RAEUMT IHN WEG. Weil das Wegraeumen durch dieselbe Stelle
 * laeuft wie das Zerschlagen, fallen die Stapel dabei ebenso heraus -- im Original geht der
 * Rechtsklick ueber setBlockToAir, und das ruft breakBlock. Wer schleicht, geht durch; dann
 * greift, was unter dem Sockel steht.
 */
public class LootDecoBlock extends BaseEntityBlock {

    public static final MapCodec<LootDecoBlock> CODEC = simpleCodec(LootDecoBlock::new);

    /** Ein Sechzehntel hoch, wie im Original (setBlockBounds mit 0.0625F). */
    private static final VoxelShape FORM = Block.box(0, 0, 0, 16, 1, 16);

    public LootDecoBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<LootDecoBlock> codec() { return CODEC; }

    /* Er hat kein Blockmodell -- nur die Blockentitaet zeichnet. */
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LootDecoBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FORM;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        level.removeBlock(pos, false);
        return InteractionResult.SUCCESS;
    }

    /**
     * Beim Zerschlagen fallen die Stapel heraus -- alle an derselben Stelle, in der Mitte des
     * Blocks, so wie im Original. Ihr Versatz gilt nur fuer das Bild.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {

        if(!state.is(newState.getBlock()) && !level.isClientSide) {
            if(level.getBlockEntity(pos) instanceof LootDecoBlockEntity sockel) {
                for(Quartet<ItemStack, Double, Double, Double> eintrag : sockel.items) {
                    Block.popResource(level, pos, eintrag.getW());
                }
                sockel.items.clear();
            }
        }

        super.onRemove(state, level, pos, newState, moving);
    }
}
