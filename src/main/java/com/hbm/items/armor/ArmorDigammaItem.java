package com.hbm.items.armor;

import com.hbm.render.model.armor.ModelArmorDigamma;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorDigamma.
 *
 * Der Fau-Anzug. Zehn Millionen Ladung und 99,99 Prozent Strahlenschutz -- der zweitbeste
 * Wert des ganzen Mods, nur vom DNT-Anzug uebertroffen.
 *
 * NICHT UEBERNOMMEN: enableThermalSight, setHasHardLanding, setStep/setJump/setFall sowie
 * hides(...) und setFullSetForHide. Waermesicht, harte Landung, Schrittgeraeusche und das
 * Ausblenden von Spielerteilen gibt es im Port nicht -- dieselbe Liste wie bei HEV, T-51 und
 * AJR, nur um die Waermesicht laenger.
 */
public class ArmorDigammaItem extends ArmorFSBPoweredItem {

    public ArmorDigammaItem(Holder<ArmorMaterial> material, Type type, Properties properties, long maxPower, long chargeRate, long consumption, long drain) {
        super(material, type, properties, maxPower, chargeRate, consumption, drain);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorDigamma ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorDigamma(original, slot);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
