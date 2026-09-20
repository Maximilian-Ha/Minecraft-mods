package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ItemModCard.
 *
 * Zwei Spielkarten, ein Aufsatz. Beide sitzen im HELMPLATZ und passen nur auf Helm und
 * Brustplatte; beide sind reine Beute, in keinem Reiter und ohne Rezept.
 *
 *   QUEEN OF SPADES  -- jeder dritte Treffer wird ganz abgesagt, ohne Obergrenze. Das ist
 *                       maechtiger als jede Ruestung: es zaehlt nicht, wie hart es trifft.
 *   ACE OF SPADES    -- jeder dritte Schuss verbraucht keine Munition. Diese Wirkung steht
 *                       nicht hier, sondern in IMagazine.shouldUseUpTrenchie; bis zu dieser
 *                       Runde stand dort ein festes falsch mit genau dieser Begruendung.
 */
public class ModCardItem extends ItemArmorMod {

    /** Welche der beiden Karten -- die Herzdame tankt, das Pik-Ass spart Munition. */
    private final boolean queenOfSpades;

    public ModCardItem(Properties properties, boolean queenOfSpades) {
        super(properties.stacksTo(1), ArmorModHandler.HELMET_ONLY, true, true, false, false);
        this.queenOfSpades = queenOfSpades;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if(this.queenOfSpades) {
            components.add(Component.literal("Power!").withStyle(ChatFormatting.RED));
            components.add(Component.literal("Adds a 33% chance to tank damage with no cap.").withStyle(ChatFormatting.RED));
        } else {
            components.add(Component.literal("Top of the line!").withStyle(ChatFormatting.RED));
            components.add(Component.literal("Guns now have a 33% chance to not consume ammo.").withStyle(ChatFormatting.RED));
        }
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, tooltipFlag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(stack.getDisplayName().copy().withStyle(ChatFormatting.RED));
    }

    @Override
    public void modDamage(LivingDamageEvent.Pre event, ItemStack armor) {

        if(!this.queenOfSpades) return;
        LivingEntity traeger = event.getEntity();
        if(!(traeger instanceof Player spieler)) return;
        if(traeger.getRandom().nextInt(3) != 0) return;

        /* Das Klirren des Originals ist random.break; in 1.21 heisst es ITEM_BREAK. */
        spieler.level().playSound(null, spieler.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS,
                0.5F, 1.0F + traeger.getRandom().nextFloat() * 0.5F);
        event.setNewDamage(0F);
    }
}
