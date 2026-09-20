package com.hbm.items.armor;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.render.model.armor.ModelArmorDesh;

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
import java.util.function.Supplier;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorDesh.
 *
 * Der Dampfanzug -- der erste Anzug des Ports, der keinen Akku hat, sondern einen TANK. Er
 * laeuft auf Dampf und wird an der Zapfsaeule nachgefuellt.
 *
 * ER MACHT LANGSAMER. Das Original haengt ihm ueber getItemAttributeModifiers ein Viertel
 * Zehntel weniger Tempo an (-0,025), dauerhaft und an jedem Teil. Ein Anzug voller Kessel
 * und Rohre ist eben schwer.
 *
 * NICHT UEBERNOMMEN: setHasHardLanding und hides(EnumPlayerPart.HAT), wie ueberall.
 */
public class ArmorDeshItem extends ArmorFSBFueledItem {

    public ArmorDeshItem(Holder<ArmorMaterial> material, Type type, Properties properties,
            Supplier<FluidType> fuelType, int maxFuel, int fillRate, int consumption, int drain) {

        super(material, type, properties, fuelType, maxFuel, fillRate, consumption, drain);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorDesh ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorDesh(original, slot);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
