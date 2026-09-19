package com.hbm.items.tools;

import com.hbm.blocks.generic.HEVBatteryBlock;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.BobMathUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemFusionCore, in der Auspraegung ModItems.hev_battery.
 *
 * Ein Akku zum Mitnehmen: in die Luft geklickt laedt er die ganze getragene Ruestung auf und
 * wird dabei verbraucht.
 *
 * ABWEICHUNG, und zwar eine erzwungene: im Original sind das ZWEI Dinge mit demselben Namen --
 * ein Block (ModBlocks.hev_battery) und ein Gegenstand (ModItems.hev_battery), die sich per
 * formlosem Rezept ineinander umwandeln lassen. Auf 1.7.10 geht das, weil Bloecke und
 * Gegenstaende getrennte Namensraeume haben ("tile." und "item."); auf 1.21 waeren es zwei
 * Eintraege auf demselben Schluessel hbmsntm:hev_battery. Der Port legt deshalb beides in
 * einen Gegenstand: als Blockgegenstand gesetzt ist er der Wandakku, in die Luft geklickt der
 * tragbare. Die beiden Umwandlungsrezepte entfallen damit -- sie waeren Rezepte von einem
 * Gegenstand auf sich selbst.
 */
public class HEVBatteryItem extends BlockItem {

    public HEVBatteryItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if(level.isClientSide) return InteractionResultHolder.pass(stack);
        if(!HEVBatteryBlock.ladeRuestung(player)) return InteractionResultHolder.pass(stack);

        stack.shrink(1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), NtmSoundEvents.SUIT_BATTERY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("desc.item.hevBattery.charge", BobMathUtil.getShortNumber(HEVBatteryBlock.LADUNG)).withStyle(ChatFormatting.YELLOW));
        components.add(Component.translatable("desc.item.hevBattery.requires"));
    }
}
