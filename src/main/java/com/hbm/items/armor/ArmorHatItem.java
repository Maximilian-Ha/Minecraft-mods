package com.hbm.items.armor;

import com.hbm.render.model.armor.ModelHat;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorHat.
 *
 * Der Hut. Kein Anzugteil, sondern ein Einzelstueck -- und das letzte Ruestungsstueck, das
 * dem Port gefehlt hat.
 *
 * ER NIMMT ZWEI PUNKTE SCHADEN WEG, von jedem Treffer, der nicht unabwendbar ist. Bleibt
 * danach weniger als null uebrig, wird auf null aufgerundet.
 *
 * KLEINE TREFFER PRALLEN GANZ AB: was hoechstens zwei Punkte gemacht haette, wird abgesagt,
 * mit einem Klirren.
 *
 * WER IHN WEGWIRFT, VERLIERT IHN. Das Original laesst den fallengelassenen Gegenstand
 * sofort verschwinden -- ein Hut, den man nicht mehr traegt, ist kein Hut mehr.
 *
 * Er haengt NICHT am Satzbonus: beide Wirkungen greifen, sobald er auf dem Kopf sitzt. Dafuer
 * gibt es IAttackHandlerItem und IDamageHandlerItem, die an allen vier Plaetzen gefragt
 * werden -- anders als die Haken an ArmorFSBItem, die nur die Brustplatte kennt.
 *
 * ZU HABEN IST ER VORERST NUR IM KREATIVREITER. Im Original traegt ihn gelegentlich ein
 * Gegner (MobUtil) und ein bestimmter Spieler bekommt ihn geschenkt; beides gibt es im Port
 * nicht, und ein Werkbankrezept hat er dort auch nicht.
 */
public class ArmorHatItem extends ArmorFSBItem implements IAttackHandlerItem, IDamageHandlerItem {

    /** Was der Hut von jedem Treffer abzieht. */
    private static final float SCHADENSMINDERUNG = 2F;

    public ArmorHatItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void handleDamage(LivingDamageEvent.Pre event, ItemStack stack) {

        if(event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        event.setNewDamage(Math.max(0F, event.getNewDamage() - SCHADENSMINDERUNG));
    }

    @Override
    public void handleAttack(LivingIncomingDamageEvent event, ItemStack armor) {

        if(event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        if(event.getAmount() <= SCHADENSMINDERUNG) {
            LivingEntity getroffen = event.getEntity();
            getroffen.level().playSound(null, getroffen.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS,
                    5F, 1.0F + getroffen.getRandom().nextFloat() * 0.5F);
            event.setCanceled(true);
        }
    }

    /** Fallengelassen verschwindet er auf der Stelle. */
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        entity.discard();
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("+2 DT").withStyle(ChatFormatting.BLUE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelHat ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelHat(original, slot);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
