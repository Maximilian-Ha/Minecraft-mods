package com.zuxelus.energycontrol.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.blocks.AdvancedInfoPanelExtender.
 *
 * Die Erweiterung zur fortgeschrittenen Tafel -- dieselbe Dicke, dieselbe Bedienung.
 * Gemeinsame Schirme bilden nur Teile derselben Art und derselben Dicke; eine duenne
 * Erweiterung an einer vollen Tafel waere sonst eine Stufe im Schirm.
 */
public class AdvancedInfoPanelExtenderBlock extends InfoPanelExtenderBlock {

    public static final MapCodec<AdvancedInfoPanelExtenderBlock> CODEC = simpleCodec(AdvancedInfoPanelExtenderBlock::new);

    public AdvancedInfoPanelExtenderBlock(Properties properties) {
        super(properties);
        // Der Aufruf oben hat die Vorgabe schon gesetzt; hier kommt nur die Dicke dazu.
        this.registerDefaultState(this.defaultBlockState().setValue(PanelThickness.THICKNESS, 16));
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PanelThickness.THICKNESS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(PanelThickness.THICKNESS, 16);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PanelThickness.shape(state.getValue(FACING), state.getValue(PanelThickness.THICKNESS));
    }
}
