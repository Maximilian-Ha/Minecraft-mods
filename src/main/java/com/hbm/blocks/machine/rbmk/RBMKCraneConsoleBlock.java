package com.hbm.blocks.machine.rbmk;

import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.rbmk.CraneConsoleBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKCraneConsole.
 *
 * Das Kranpult, ein 2x1x3-Bau. Es hat keine Oberflaeche: bedient wird es, indem man sich
 * davorstellt und die Krantasten drueckt.
 */
public class RBMKCraneConsoleBlock extends DummyableBlock implements IToolable {

    /** Das Pult ist nur einen halben Block hoch, damit man darueber hinwegsieht. */
    private static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 0.5, 1);

    public RBMKCraneConsoleBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<RBMKCraneConsoleBlock> CODEC = simpleCodec(RBMKCraneConsoleBlock::new);

    @Override
    protected MapCodec<? extends RBMKCraneConsoleBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return new CraneConsoleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    /**
     * Kein protected: DummyableBlock weitet getShape bereits auf public, eine Ueberschreibung
     * darf den Zugriff nicht wieder verengen.
     */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Ein Tisch: einen Block hoch, zwei tief, drei breit.
     *
     * ABWEICHUNG: das Original setzt ueber die hintere Haelfte noch eine zweite Lage und fuellt
     * die vordere in einem zweiten Durchgang. Die Dummyable-Bloecke des Ports kennen nur ein
     * Mass je Bau; die zweite Lage war ohnehin nur Zierde, das Modell ragt so oder so darueber.
     */
    @Override public int[] getDimensions() { return new int[] {0, 0, 0, 1, 1, 1}; }
    @Override public int getOffset() { return 1; }

    /** Der Schraubenzieher dreht den Laufbalken des Krans um neunzig Grad. */
    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER) return false;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return false;
        if(!(level.getBlockEntity(corePos) instanceof CraneConsoleBlockEntity console)) return false;

        if(!level.isClientSide) console.cycleCraneRotation();

        return true;
    }
}
