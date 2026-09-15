package com.hbm.blocks.machine;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineCrucibleBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ScrapsItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineCrucible.
 *
 * Ein 3x3-Bau: der Boden ist eine flache Platte, darauf steht ein Rand. Mit der Schaufel holt man
 * den erstarrten Inhalt beider Baender heraus.
 */
public class MachineCrucibleBlock extends DummyableBlock implements ICrucibleAcceptor {

    public MachineCrucibleBlock(Properties properties) {
        super(properties);

        // derselbe Umriss wie im Original: Bodenplatte plus vier Randstuecke
        this.bounding.add(new AABB(-1.5D, 0D, -1.5D, 1.5D, 0.5D, 1.5D));
        this.bounding.add(new AABB(-1.25D, 0.5D, -1.25D, 1.25D, 1.5D, -1D));
        this.bounding.add(new AABB(-1.25D, 0.5D, -1.25D, -1D, 1.5D, 1.25D));
        this.bounding.add(new AABB(-1.25D, 0.5D, 1D, 1.25D, 1.5D, 1.25D));
        this.bounding.add(new AABB(1D, 0.5D, -1.25D, 1.25D, 1.5D, 1.25D));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineCrucibleBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    public static final MapCodec<MachineCrucibleBlock> CODEC = simpleCodec(MachineCrucibleBlock::new);
    @Override public MapCodec<MachineCrucibleBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {1, 0, 1, 1, 1, 1}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(!stack.is(ItemTags.SHOVELS)) return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return ItemInteractionResult.SUCCESS;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return ItemInteractionResult.FAIL;
        if(!(level.getBlockEntity(corePos) instanceof MachineCrucibleBlockEntity crucible)) return ItemInteractionResult.FAIL;

        for(MaterialStack materialStack : collect(crucible)) {

            ItemStack scrap = ScrapsItem.create(new MaterialStack(materialStack.material, materialStack.amount));

            if(!player.getInventory().add(scrap)) {
                Containers.dropItemStack(level, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z, scrap);
            }
        }

        player.inventoryMenu.broadcastChanges();
        crucible.recipeStack.clear();
        crucible.wasteStack.clear();
        crucible.setChanged();

        return ItemInteractionResult.CONSUME;
    }

    private static List<MaterialStack> collect(MachineCrucibleBlockEntity crucible) {
        List<MaterialStack> stacks = new ArrayList<>();
        stacks.addAll(crucible.recipeStack);
        stacks.addAll(crucible.wasteStack);
        return stacks;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MachineCrucibleBlockEntity crucible) {

            for(MaterialStack stack : collect(crucible)) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        ScrapsItem.create(new MaterialStack(stack.material, stack.amount)));
            }

            crucible.recipeStack.clear();
            crucible.wasteStack.clear();
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    private static MachineCrucibleBlockEntity core(Level level, BlockPos pos, DummyableBlock self) {
        BlockPos corePos = self.findCore(level, pos);
        if(corePos == null) return null;
        return level.getBlockEntity(corePos) instanceof MachineCrucibleBlockEntity crucible ? crucible : null;
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        MachineCrucibleBlockEntity crucible = core(level, pos, this);
        return crucible != null && crucible.canAcceptPartialPour(level, pos, dX, dY, dZ, side, stack);
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        MachineCrucibleBlockEntity crucible = core(level, pos, this);
        return crucible == null ? stack : crucible.pour(level, pos, dX, dY, dZ, side, stack);
    }

    @Override public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) { return false; }
    @Override public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) { return stack; }
}
