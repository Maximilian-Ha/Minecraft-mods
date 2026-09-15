package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineArcFurnaceLargeBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.handler.MultiblockHandlerXR;
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
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineArcFurnaceLarge.
 *
 * Ein 5x5-Bau mit sechs Anschlussstellen am Rand. Mit einer Schaufel in der Hand schoepft man
 * den fluessigen Inhalt als Schrott heraus, statt die Oberflaeche zu oeffnen.
 */
public class MachineArcFurnaceLargeBlock extends DummyableBlock {

    /** Der zweite Umriss, den das Original zusaetzlich zu getDimensions prueft und fuellt. */
    private static final int[] EXTRA_DIMENSIONS = new int[] { 4, 0, 3, -2, 1, 1 };

    public MachineArcFurnaceLargeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineArcFurnaceLargeBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    public static final MapCodec<MachineArcFurnaceLargeBlock> CODEC = simpleCodec(MachineArcFurnaceLargeBlock::new);
    @Override public MapCodec<MachineArcFurnaceLargeBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {4, 0, 2, 2, 2, 2}; }
    @Override public int getOffset() { return 2; }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {
        if(!super.checkRequirement(level, pos, dir, offset)) return false;
        return MultiblockHandlerXR.checkSpace(level, pos.relative(dir, offset), EXTRA_DIMENSIONS, pos, dir);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        MultiblockHandlerXR.fillSpace(level, core, EXTRA_DIMENSIONS, this, dir);

        Direction rot = dir.getClockWise();

        // die sechs Anschlussstellen am Rand, an denen Kabel andocken
        this.makeExtra(level, core.offset(dir.getStepX() * 2 + rot.getStepX(), 0, dir.getStepZ() * 2 + rot.getStepZ()));
        this.makeExtra(level, core.offset(dir.getStepX() * 2 - rot.getStepX(), 0, dir.getStepZ() * 2 - rot.getStepZ()));
        this.makeExtra(level, core.offset(rot.getStepX() * 2 + dir.getStepX(), 0, rot.getStepZ() * 2 + dir.getStepZ()));
        this.makeExtra(level, core.offset(rot.getStepX() * 2 - dir.getStepX(), 0, rot.getStepZ() * 2 - dir.getStepZ()));
        this.makeExtra(level, core.offset(-rot.getStepX() * 2 + dir.getStepX(), 0, -rot.getStepZ() * 2 + dir.getStepZ()));
        this.makeExtra(level, core.offset(-rot.getStepX() * 2 - dir.getStepX(), 0, -rot.getStepZ() * 2 - dir.getStepZ()));
    }

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
        if(!(level.getBlockEntity(corePos) instanceof MachineArcFurnaceLargeBlockEntity furnace)) return ItemInteractionResult.FAIL;
        if(furnace.liquids.isEmpty()) return ItemInteractionResult.CONSUME;

        for(MaterialStack materialStack : furnace.liquids) {
            ItemStack scrap = ScrapsItem.create(new MaterialStack(materialStack.material, materialStack.amount));
            if(!player.getInventory().add(scrap)) {
                Containers.dropItemStack(level, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z, scrap);
            }
        }

        player.inventoryMenu.broadcastChanges();
        furnace.liquids.clear();
        furnace.setChanged();

        return ItemInteractionResult.CONSUME;
    }
}
