package com.hbm.blocks.machine.icf;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.icf.ICFControllerBlockEntity;
import com.hbm.blockentity.machine.icf.ICFWrapperBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.BlockICF.
 *
 * Der Stellvertreter fuer ein eingelesenes Laserbauteil. Jedes Bauteil der Anlage wird beim
 * Einlesen durch ihn ersetzt; er merkt sich, was er war, und gibt es beim Abbauen zurueck.
 *
 * Die Anschlussstellen sehen anders aus als der Rest -- das ist der einzige Unterschied.
 *
 * ABWEICHUNG: die Connected Textures des Originals sind gestrichen (ENTSCHEIDUNGEN.md).
 */
public class ICFWrapperBlock extends BaseEntityBlock {

    /** Ob dieser Stellvertreter eine Anschlussstelle war. */
    public static final BooleanProperty PORT = BooleanProperty.create("port");

    public ICFWrapperBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PORT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PORT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ICFWrapperBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    /** Beim Abbauen kommt das Laserbauteil zurueck und die Anlage faellt auseinander. */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ICFWrapperBlockEntity wrapper) {

            ICFControllerBlockEntity controller = wrapper.getCore();
            int part = wrapper.part;

            super.onRemove(state, level, pos, newState, isMoving);

            if(part >= 0) {
                level.setBlock(pos, com.hbm.blocks.NtmBlocks.ICF_LASER_COMPONENT.get().defaultBlockState()
                        .setValue(ICFLaserComponentBlock.SUBTYPE, part), 3);
                if(controller != null) controller.assembled = false;
            }

            return;
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static final MapCodec<ICFWrapperBlock> CODEC = simpleCodec(ICFWrapperBlock::new);
    @Override protected MapCodec<ICFWrapperBlock> codec() { return CODEC; }
}
