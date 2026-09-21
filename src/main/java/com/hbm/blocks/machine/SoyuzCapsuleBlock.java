package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.storage.CrateBaseBlockEntity;
import com.hbm.blockentity.machine.storage.SoyuzCapsuleBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.SoyuzCapsule.
 *
 * Die gelandete Landekapsel als Block. Sie hat kein Blockmodell -- ihr Aussehen kommt aus
 * soyuz_lander.obj, das der Blockentitaetsdarsteller zeichnet.
 *
 * ROSTIG ODER NICHT: im Original entscheidet das die Blockmetadate. Eine Kapsel, die gerade
 * heruntergekommen ist, traegt 0 und ist blank; die Weltgeneration setzt 3 und laesst sie
 * verrostet am Strand liegen. Im Port ist daraus die Zustandseigenschaft rusty geworden.
 */
public class SoyuzCapsuleBlock extends BaseEntityBlock {

    public static final MapCodec<SoyuzCapsuleBlock> CODEC = simpleCodec(SoyuzCapsuleBlock::new);

    public static final BooleanProperty RUSTY = BooleanProperty.create("rusty");

    /** Das Modell steht schraeg und ragt ueber den Block hinaus; der Umriss bleibt trotzdem
     *  der eine Block, auf dem sie sitzt. Ohne eigenen Umriss ist er einen Block hoch, und
     *  darauf laesst sie sich anklicken. */
    private static final VoxelShape UMRISS = Block.box(1, 0, 1, 15, 14, 15);

    public SoyuzCapsuleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(RUSTY, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RUSTY);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoyuzCapsuleBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return UMRISS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof CrateBaseBlockEntity kapsel && kapsel.canAccess(player) && blockEntity instanceof MenuProvider menu) {
            player.openMenu(new SimpleMenuProvider(menu, menu.getDisplayName()), pos);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(!level.isClientSide && !state.is(newState.getBlock())) {
            if(level.getBlockEntity(pos) instanceof CrateBaseBlockEntity kapsel) Containers.dropContents(level, pos, kapsel);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
