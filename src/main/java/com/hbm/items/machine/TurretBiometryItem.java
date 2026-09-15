package com.hbm.items.machine;

import com.hbm.items.component.NtmDataComponents;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemTurretBiometry.
 *
 * Der Zielchip. Wer ihn in der Hand haelt und rechtsklickt, traegt sich selbst ein; ein Turm mit
 * diesem Chip im Fach laesst alle Eingetragenen in Ruhe. Die Liste steht in einer Datenkomponente
 * statt in losem NBT -- sonst dasselbe wie im Original.
 */
public class TurretBiometryItem extends Item {

    public TurretBiometryItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        for(String name : getNames(stack)) tooltip.add(Component.literal(name).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        addName(stack, player.getName().getString());

        if(level.isClientSide) player.displayClientMessage(Component.translatable("item.hbmsntm.turret_chip.added"), true);
        else SoundUtils.playAtVec3(level, player.position(), NtmSoundEvents.TECH_BLEEP.get(), SoundSource.PLAYERS);

        player.swing(hand);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static List<String> getNames(ItemStack stack) {
        return stack.getOrDefault(NtmDataComponents.TURRET_WHITELIST.get(), List.of());
    }

    public static void addName(ItemStack stack, String name) {

        List<String> names = new ArrayList<>(getNames(stack));
        if(names.contains(name)) return;

        names.add(name);
        setNames(stack, names);
    }

    public static void setNames(ItemStack stack, List<String> names) {
        stack.set(NtmDataComponents.TURRET_WHITELIST.get(), List.copyOf(names));
    }
}
