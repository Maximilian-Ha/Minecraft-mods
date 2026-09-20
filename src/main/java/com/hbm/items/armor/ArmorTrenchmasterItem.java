package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.NtmItems;
import com.hbm.render.model.armor.ModelArmorTrenchmaster;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorTrenchmaster.
 *
 * Der Grabenmeister. Wie die Taurun-Ruestung Beute statt Bauwerk und ohne Haltbarkeit
 * (setMaxDamage(0) im Original), aber mit zwei Kampfeigenschaften, die keine andere Ruestung
 * des Ports hat:
 *
 *   EIGENE SPRENGUNGEN TUN NICHT WEH. Wer den ganzen Satz traegt und sich selbst in die Luft
 *   sprengt, nimmt daraus null Schaden. Fremde Sprengungen treffen ihn normal.
 *
 *   JEDER DRITTE TREFFER PRALLT AB. Mit vollem Satz wird ein Angriff mit einem Drittel
 *   Wahrscheinlichkeit ganz abgesagt, mit einem Klirren.
 *
 * NICHT UEBERNOMMEN: enableVATS, setStepSize(1) und hides(EnumPlayerPart.HAT) -- dieselben
 * fehlenden Teilsysteme wie ueberall. Die beiden statischen Abfragen isTrenchMaster und
 * hasAoS stehen beide unten; hasAoS kam in Runde 232 dazu, als es die AoS-Karte gab.
 */
public class ArmorTrenchmasterItem extends ArmorFSBItem {

    public ArmorTrenchmasterItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    /**
     * Traegt der Spieler einen vollstaendigen Grabenmeister-Satz? Die Waffen fragen so, um
     * schneller nachzuladen und mehr Munition aus einem Griff zu holen.
     */
    public static boolean isTrenchMaster(Player player) {
        if(player == null) return false;
        return player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorTrenchmasterItem
                && hasFSBArmor(player);
    }

    /**
     * Steckt die AoS-Karte im HELM des Spielers? Sie sitzt dort im Helmplatz des
     * Aufsatzsystems -- das Original liest armorInventory[3], also den Helm, und daraus den
     * Platz helmet_only. Wer sie traegt, spart bei jedem dritten Schuss die Munition.
     */
    public static boolean hasAoS(Player player) {
        if(player == null) return false;
        ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
        if(helm.isEmpty() || !ArmorModHandler.hasMods(helm)) return false;
        ItemStack karte = ArmorModHandler.pryMods(player.level(), helm)[ArmorModHandler.HELMET_ONLY];
        return !karte.isEmpty() && karte.is(NtmItems.CARD_AOS.get());
    }

    @Override
    public void handleHurt(LivingDamageEvent.Pre event) {

        if(!(event.getEntity() instanceof Player player)) return;
        if(!hasFSBArmor(player)) return;

        /* Nur die eigene Sprengung: der Vergleich steht so im Original. */
        if(event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)
                && event.getSource().getDirectEntity() == player) {
            event.setNewDamage(0F);
        }
    }

    @Override
    public void handleAttack(LivingIncomingDamageEvent event) {

        if(!(event.getEntity() instanceof Player player)) return;
        if(!hasFSBArmor(player)) return;

        if(player.getRandom().nextInt(3) == 0) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS,
                    0.5F, 1.0F + player.getRandom().nextFloat() * 0.5F);
            event.setCanceled(true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("  ").append(Component.translatable("armor.moreAmmo")).withStyle(ChatFormatting.RED));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorTrenchmaster ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorTrenchmaster(original, slot);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
