package com.zuxelus.energycontrol.blocks;

import com.mojang.serialization.MapCodec;
import com.zuxelus.energycontrol.blockentity.AdvancedInfoPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.blocks.AdvancedInfoPanel.
 *
 * Die fortgeschrittene Tafel ist eine Informationstafel mit einstellbarer Dicke. Alles
 * andere erbt sie -- damit gilt fuer sie ohne weiteres Zutun, was fuer die gewoehnliche
 * Tafel gilt: Karten, Aufwertungen, Beruehrung, grosse Schirme.
 */
public class AdvancedInfoPanelBlock extends InfoPanelBlock {

    public static final MapCodec<AdvancedInfoPanelBlock> CODEC = simpleCodec(AdvancedInfoPanelBlock::new);

    public AdvancedInfoPanelBlock(Properties properties) {
        super(properties);
        // Der Aufruf oben hat die Vorgabe schon gesetzt; hier kommt nur die Dicke dazu.
        this.registerDefaultState(this.defaultBlockState().setValue(PanelThickness.THICKNESS, 16));
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedInfoPanelBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PanelThickness.shape(state.getValue(FACING), state.getValue(PanelThickness.THICKNESS));
    }
}
