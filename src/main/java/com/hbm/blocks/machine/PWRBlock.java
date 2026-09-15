package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.PWRBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.BlockPWR.
 *
 * Der Stellvertreter, durch den beim Einlesen jedes Bauteil des Reaktors ersetzt wird. Er sieht
 * ueberall gleich aus und ist zaeher als das, was er ersetzt.
 *
 * PORT sagt, ob hier eine Anschlussstelle stand -- im Original ist das die Metadatenziffer 1.
 * Nur solche Stellen reichen Fluid an die Steuerung durch.
 *
 * Wird er abgebaut, kommt das urspruengliche Bauteil zurueck und die Anlage faellt aus dem
 * Betrieb. Er selbst laesst sich nicht aufsammeln.
 */
public class PWRBlock extends BaseEntityBlock {

    public static final BooleanProperty PORT = BooleanProperty.create("port");

    public static final MapCodec<PWRBlock> CODEC = simpleCodec(PWRBlock::new);

    public PWRBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PORT, Boolean.FALSE));
    }

    @Override
    public MapCodec<PWRBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PORT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PWRBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {

        /*
         * Beim Abbauen kommt das urspruengliche Bauteil zurueck. Der Test auf den neuen Block
         * verhindert eine Endlosschleife: restore() setzt selbst einen Block, und das laeuft
         * wieder hier durch.
         */
        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PWRBlockEntity pwr && pwr.block != null) {

            if(newState.isAir()) {
                pwr.restore();
                return;
            }

            /* Ersetzt jemand den Stellvertreter durch etwas anderes, faellt die Anlage aus. */
            var controller = pwr.getCore();
            if(controller != null) controller.assembled = false;
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

}
