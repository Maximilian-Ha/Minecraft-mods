package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.FloodlightBeamBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.FloodlightBeam.
 *
 * Der Lichtfleck eines Flutlichts: unsichtbar, ohne Umriss, hell.
 *
 * Anders als der Strahl des Scheinwerfers merkt er sich nicht Richtungen, sondern seine
 * Quelle und die Nummer des Strahls, der ihn gesetzt hat -- das Flutlicht wirft fuenfzehn
 * davon und frischt sie der Reihe nach auf. Deshalb braucht er eine Blockentitaet.
 */
public class FloodlightBeamBlock extends Block implements EntityBlock {

    public static final MapCodec<FloodlightBeamBlock> CODEC = simpleCodec(FloodlightBeamBlock::new);

    public FloodlightBeamBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<FloodlightBeamBlock> codec() { return CODEC; }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }

    /** Der Lichtfleck weicht allem: wer hier baut, loescht ihn. */
    @Override public boolean canBeReplaced(BlockState state, BlockPlaceContext context) { return true; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FloodlightBeamBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }
}
