package com.hbm.blocks.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.FoundryCastingBaseBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.MoldItem;
import com.hbm.items.machine.MoldItem.Mold;
import com.hbm.items.machine.ScrapsItem;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.FoundryCastingBase.
 *
 * Gemeinsames Verhalten von Giessform und Giessbecken. Der Block selbst ist der Abnehmer des
 * Giessstrahls -- CrucibleUtil fragt den Block, nicht sein BlockEntity --, und reicht alles an
 * das BlockEntity weiter.
 *
 * Bedienung, in dieser Reihenfolge: liegt ein fertiges Gussstueck darin, nimmt der Klick es
 * heraus. Sonst legt ein Klick mit einer Form die Form ein. Mit einer Schaufel in der Hand wird
 * erstarrtes Metall als Schrott herausgeschoept, und der Schraubenzieher holt die Form zurueck.
 */
public abstract class FoundryCastingBaseBlock extends BaseEntityBlock implements ICrucibleAcceptor, IToolable, ILookOverlay {

    protected FoundryCastingBaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    /** Die Hoehe, auf der Herausgenommenes erscheint. */
    protected abstract double getDropHeight();

    private static ICrucibleAcceptor acceptor(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acc ? acc : null;
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc != null && acc.canAcceptPartialPour(level, pos, dX, dY, dZ, side, stack);
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc == null ? stack : acc.pour(level, pos, dX, dY, dZ, side, stack);
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc != null && acc.canAcceptPartialFlow(level, pos, side, stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acc = acceptor(level, pos);
        return acc == null ? stack : acc.flow(level, pos, side, stack);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(!(level.getBlockEntity(pos) instanceof FoundryCastingBaseBlockEntity cast)) return InteractionResult.FAIL;

        return this.takeOutput(level, pos, player, cast) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(!(level.getBlockEntity(pos) instanceof FoundryCastingBaseBlockEntity cast)) return ItemInteractionResult.FAIL;

        // Das fertige Gussstueck hat immer Vorrang
        if(this.takeOutput(level, pos, player, cast)) return ItemInteractionResult.CONSUME;

        // Form einlegen
        if(stack.getItem() instanceof MoldItem && cast.getItem(0).isEmpty()) {

            Mold mold = MoldItem.getMold(stack);

            if(mold != null && mold.size == cast.getMoldSize()) {
                ItemStack single = stack.copy();
                single.setCount(1);
                cast.setItem(0, single);
                stack.shrink(1);
                level.playSound(null, pos, NtmSoundEvents.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                return ItemInteractionResult.CONSUME;
            }
        }

        // Erstarrtes herausschoepfen
        if(stack.is(ItemTags.SHOVELS)) {

            if(cast.amount > 0) {
                this.give(level, pos, player, ScrapsItem.create(new MaterialStack(cast.type, cast.amount)));
                cast.amount = 0;
                cast.type = null;
                cast.setChanged();
            }

            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private boolean takeOutput(Level level, BlockPos pos, Player player, FoundryCastingBaseBlockEntity cast) {

        if(cast.getItem(1).isEmpty()) return false;

        this.give(level, pos, player, cast.getItem(1).copy());
        cast.setItem(1, ItemStack.EMPTY);
        cast.setChanged();

        return true;
    }

    private void give(Level level, BlockPos pos, Player player, ItemStack stack) {
        if(player.getInventory().add(stack)) {
            player.inventoryMenu.broadcastChanges();
        } else {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + this.getDropHeight(), pos.getZ() + 0.5, stack);
        }
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER) return false;
        if(!(level.getBlockEntity(pos) instanceof FoundryCastingBaseBlockEntity cast)) return false;
        if(cast.getItem(0).isEmpty()) return false;
        if(cast.amount > 0) return false;

        this.give(level, pos, player, cast.getItem(0).copy());
        cast.setItem(0, ItemStack.EMPTY);
        cast.setChanged();

        return true;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FoundryCastingBaseBlockEntity cast) {

            if(cast.amount > 0 && cast.type != null) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + this.getDropHeight(), pos.getZ() + 0.5,
                        ScrapsItem.create(new MaterialStack(cast.type, cast.amount)));
                cast.amount = 0;
            }

            Containers.dropContents(level, pos, cast);
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {

        if(!(level.getBlockEntity(pos) instanceof FoundryCastingBaseBlockEntity cast)) return;
        if(cast.amount <= 0 || cast.amount < cast.getCapacity()) return;

        level.addParticle(ParticleTypes.SMOKE,
                pos.getX() + 0.25 + random.nextDouble() * 0.5,
                pos.getY() + this.getDropHeight(),
                pos.getZ() + 0.25 + random.nextDouble() * 0.5,
                0.0, 0.0, 0.0);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof FoundryCastingBaseBlockEntity cast)) return;

        List<Component> text = new ArrayList<>();
        Mold mold = cast.getInstalledMold();

        if(mold == null) {
            text.add(Component.translatable("foundry.noCast").withStyle(ChatFormatting.RED));
        } else {
            text.add(mold.getTitle().copy().withStyle(ChatFormatting.BLUE));
        }

        if(cast.type != null && cast.amount > 0) {
            text.add(cast.type.getName().append(": " + cast.amount + " / " + cast.getCapacity()).withStyle(ChatFormatting.YELLOW));
        }

        ILookOverlay.printGeneric(event, this.getName(), 0xFF4000, 0x401000, text);
    }
}
