package com.hbm.items.tools;

import com.hbm.blockentity.machine.MachineTeleporterBlockEntity;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.special.ItemTeleLink.
 *
 * Das Verbindungsstueck des Teleporters. ZWEI KLICKS: der erste merkt sich einen Ort samt Welt,
 * der zweite -- auf einen Teleporter -- traegt ihn dort ein.
 *
 * DIE REIHENFOLGE ERGIBT SICH VON SELBST, und das ist der Kniff des Originals: klickt man auf
 * einen Teleporter, wird eingetragen; klickt man auf irgendetwas anderes, wird gemerkt. Man
 * muss also nichts umschalten.
 *
 * ABWEICHUNG: der gemerkte Ort steht in einem Datenbestandteil statt in freiem NBT -- so
 * verlangt es 1.21. Die Welt wird beim Namen gemerkt, nicht bei der Nummer.
 */
public class TeleLinkItem extends Item {

    private static final String KEY = "telelink";

    public TeleLinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if(player == null || player.isShiftKeyDown()) return InteractionResult.PASS;
        if(level.isClientSide) return InteractionResult.SUCCESS;

        if(level.getBlockEntity(pos) instanceof MachineTeleporterBlockEntity teleporter) {

            CompoundTag target = read(stack);

            if(target == null) {
                level.playSound(null, player.blockPosition(), NtmSoundEvents.TECH_BOOP.get(), SoundSource.PLAYERS, 1F, 1F);
                player.displayClientMessage(Component.translatable("telelink.noDestination").withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }

            ResourceLocation dim = ResourceLocation.tryParse(target.getString("dim"));
            if(dim == null) return InteractionResult.CONSUME;

            teleporter.setTarget(target.getInt("x"), target.getInt("y"), target.getInt("z"), dim);

            level.playSound(null, player.blockPosition(), NtmSoundEvents.TECH_BLEEP.get(), SoundSource.PLAYERS, 1F, 1F);
            player.displayClientMessage(Component.translatable("telelink.set").withStyle(ChatFormatting.AQUA), false);

            return InteractionResult.CONSUME;
        }

        CompoundTag target = new CompoundTag();
        target.putInt("x", pos.getX());
        target.putInt("y", pos.getY());
        target.putInt("z", pos.getZ());
        target.putString("dim", level.dimension().location().toString());

        store(stack, target);

        level.playSound(null, player.blockPosition(), NtmSoundEvents.TECH_BLEEP.get(), SoundSource.PLAYERS, 1F, 1F);
        player.displayClientMessage(Component.translatable("telelink.stored", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.AQUA), false);

        return InteractionResult.CONSUME;
    }

    private static void store(ItemStack stack, CompoundTag target) {
        CompoundTag tag = new CompoundTag();
        tag.put(KEY, target);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static @Nullable CompoundTag read(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if(data == null) return null;
        CompoundTag tag = data.copyTag();
        return tag.contains(KEY) ? tag.getCompound(KEY) : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        CompoundTag target = read(stack);

        if(target == null) {
            components.add(Component.translatable("telelink.empty").withStyle(ChatFormatting.RED));
            return;
        }

        components.add(Component.literal("X: " + target.getInt("x")));
        components.add(Component.literal("Y: " + target.getInt("y")));
        components.add(Component.literal("Z: " + target.getInt("z")));
        components.add(Component.literal("D: " + target.getString("dim")));
    }
}
