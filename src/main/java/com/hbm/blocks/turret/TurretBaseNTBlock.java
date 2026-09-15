package com.hbm.blocks.turret;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.turret.TurretBaseNT.
 *
 * Der Fuss der grossen Geschuetztuerme: zwei mal zwei Bloecke, nur halb so hoch wie ein Block --
 * was darueber steht, ist Modell. Der Kern sitzt in einer Ecke, die drei anderen Bloecke sind
 * Stellvertreter, die Waren und Strom an ihn weiterreichen.
 */
public abstract class TurretBaseNTBlock extends DummyableBlock {

    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 8, 16);

    public TurretBaseNTBlock(Properties properties) {
        super(properties);
    }

    @Override public int[] getDimensions() { return new int[] { 0, 0, 1, 0, 1, 0 }; }
    @Override public int getOffset() { return 0; }

    /** Der Kern traegt den Turm, die drei uebrigen Bloecke reichen Waren und Strom durch. */
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> this.newTurret(pos, state);
            case EXTRA, DUMMY -> new ProxyComboBlockEntity(pos, state).inventory().power();
        };
    }

    protected abstract BlockEntity newTurret(BlockPos pos, BlockState state);

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) { return 1F; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
}
