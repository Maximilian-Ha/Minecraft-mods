package com.hbm.blocks.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.blockentity.IPersistentNBT;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.SawmillBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.NtmItems;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.BobMathUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineSawmill.
 *
 * Mehrblockmaschine mit 3x3-Grundflaeche. Die Bedienung laeuft vollstaendig ueber
 * Rechtsklick: Saegeblatt einsetzen, Holz einlegen, Ergebnis herausnehmen.
 */
public class MachineSawmillBlock extends DummyableBlock implements ILookOverlay, ITooltipProvider {

    public MachineSawmillBlock(Properties properties) {
        super(properties);

        this.bounding.add(new AABB(-1.5D, 0D, -1.5D, 1.5D, 1D, 1.5D));
        this.bounding.add(new AABB(-1.25D, 1D, -0.5D, -0.625D, 1.875D, 0.5D));
        this.bounding.add(new AABB(-0.625D, 1D, -1D, 1.375D, 2D, 1D));
    }

    public static final MapCodec<MachineSawmillBlock> CODEC = simpleCodec(MachineSawmillBlock::new);
    @Override public MapCodec<MachineSawmillBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new SawmillBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] {1, 0, 1, 1, 1, 1}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        this.makeExtra(level, core.east());
        this.makeExtra(level, core.west());
        this.makeExtra(level, core.south());
        this.makeExtra(level, core.north());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        return this.handle(level, pos, player, ItemStack.EMPTY) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        return this.handle(level, pos, player, stack)
                ? ItemInteractionResult.CONSUME
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Gemeinsamer Rumpf von onBlockActivated aus dem Original. */
    private boolean handle(Level level, BlockPos pos, Player player, ItemStack held) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return false;

        if(!(level.getBlockEntity(corePos) instanceof SawmillBlockEntity sawmill)) return false;

        if(!sawmill.hasBlade && !held.isEmpty() && held.is(NtmItems.SAWBLADE.get())) {
            held.shrink(1);
            sawmill.hasBlade = true;
            sawmill.setChanged();
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    NtmSoundEvents.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.5F, 0.75F);
            return true;
        }

        if(!sawmill.getItem(1).isEmpty() || !sawmill.getItem(2).isEmpty()) {
            for(int i = 1; i < 3; i++) {
                ItemStack slot = sawmill.getItem(i);
                if(!slot.isEmpty()) {
                    if(!player.getInventory().add(slot.copy())) {
                        player.drop(slot.copy(), false);
                    }
                    sawmill.setItem(i, ItemStack.EMPTY);
                }
            }
            player.containerMenu.broadcastChanges();
            sawmill.setChanged();
            return true;

        } else {
            if(sawmill.getItem(0).isEmpty() && !held.isEmpty() && !sawmill.getOutput(held).isEmpty()) {
                ItemStack input = held.copy();
                input.setCount(1);
                sawmill.setItem(0, input);
                held.shrink(1);
                sawmill.setChanged();
                player.containerMenu.broadcastChanges();
                return true;
            }
        }

        return false;
    }

    /** Das Saegeblatt faellt mit dem Block -- oder eben nicht, wenn es weggeflogen ist. */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return IPersistentNBT.getDropsFromLootParams(state, params);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;

        BlockEntity te = level.getBlockEntity(corePos);
        if(!(te instanceof SawmillBlockEntity sawmill)) return;

        List<Component> text = new ArrayList<>();
        text.add(Component.literal(sawmill.heat + "TU/t"));

        double percent = (double) sawmill.heat / (double) 300;
        int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int) (0xFF * percent) << 8);

        if(percent > 1D) color = 0xff0000;

        text.add(Component.literal(((sawmill.heat * 1000 / 300) / 10D) + "%").withColor(color));

        // Fortschrittsbalken aus 25 Strichen, der gefuellte Teil ist gruen
        int limiter = Math.min(25, sawmill.progress * 26 / SawmillBlockEntity.processingTime);
        text.add(Component.literal("[ ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("\u258F".repeat(limiter)).withStyle(ChatFormatting.GREEN))
                .append(Component.literal("\u258F".repeat(25 - limiter)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" ]").withStyle(ChatFormatting.GREEN)));

        for(int i = 0; i < 3; i++) {
            ItemStack slot = sawmill.getItem(i);
            if(!slot.isEmpty()) {
                text.add(Component.literal(i == 0 ? "-> " : "<- ").withStyle(i == 0 ? ChatFormatting.GREEN : ChatFormatting.RED)
                        .append(slot.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(slot.getCount() > 1 ? " x" + slot.getCount() : "").withStyle(ChatFormatting.WHITE)));
            }
        }

        if(sawmill.heat > 300) {
            text.add(Component.literal("! ! ! OVERSPEED ! ! !").withColor(BobMathUtil.getBlink() ? 0xff0000 : 0xffff00));
        }

        if(!sawmill.hasBlade) {
            text.add(Component.literal("Blade missing!").withColor(0xff0000));
        }

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }
}
