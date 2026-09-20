package com.hbm.items.armor;

import com.hbm.registry.NtmDamageTypes;
import com.hbm.render.model.armor.ModelArmorBJ;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorBJ.
 *
 * Der Blackjack-Anzug -- kybernetische Gliedmassen und eine Augenklappe mit Waermesensor.
 *
 * WER IHN TRAEGT UND IHM GEHT DER STROM AUS, STIRBT. Genauer: traegt der Spieler einen
 * vollstaendigen Satz, ist aber nicht jedes Teil geladen, dann wird ihm der Helm
 * abgenommen und ins Gepaeck gelegt (oder fallengelassen, wenn dort kein Platz ist) -- und
 * danach bekommt er tausend Punkte Mondschaden, die weder Panzerung noch Widerstandskraft
 * aufhalten. Die Kybernetik im Schaedel hoert auf zu arbeiten, und das ueberlebt niemand.
 *
 * Gefragt wird das nur am HELM; so steht es im Original.
 *
 * NICHT UEBERNOMMEN: enableVATS, enableThermalSight, setHasHardLanding und
 * setStep/setJump/setFall -- dieselben fehlenden Teilsysteme wie ueberall.
 */
public class ArmorBJItem extends ArmorFSBPoweredItem {

    /** Mit oder ohne Rueckentriebwerk. Mehr unterscheidet die beiden Brustplatten nicht. */
    public enum Variante { NORMAL, JETPACK }

    private final Variante variante;

    public ArmorBJItem(Holder<ArmorMaterial> material, Type type, Properties properties, long maxPower, long chargeRate, long consumption, long drain) {
        this(material, type, properties, maxPower, chargeRate, consumption, drain, Variante.NORMAL);
    }

    public ArmorBJItem(Holder<ArmorMaterial> material, Type type, Properties properties, long maxPower, long chargeRate, long consumption, long drain, Variante variante) {
        super(material, type, properties, maxPower, chargeRate, consumption, drain);
        this.variante = variante;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {

        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(level.isClientSide) return;
        if(!(entity instanceof Player player)) return;
        if(player.getItemBySlot(EquipmentSlot.HEAD) != stack) return;

        /* Voller Satz, aber nicht voll geladen: genau diese Luecke ist toedlich. */
        if(!hasFSBArmorIgnoreCharge(player) || hasFSBArmor(player)) return;

        ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);

        if(!player.getInventory().add(helm.copy())) player.drop(helm.copy(), false);
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);

        player.hurt(level.damageSources().source(NtmDamageTypes.LUNAR), 1000F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorBJ ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorBJ(original, slot, variante);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
