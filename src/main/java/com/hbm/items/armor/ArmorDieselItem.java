package com.hbm.items.armor;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.render.model.armor.ModelArmorDiesel;

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
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorDiesel.
 *
 * Der Dieselanzug. Wie der Dampfanzug ein Satz mit Tank statt Akku, aber mit einer
 * Besonderheit:
 *
 * ER NIMMT ZWEI SORTEN. Das Original ueberschreibt acceptsFluid und laesst neben Diesel auch
 * gekrackten Diesel zu -- die Zapfsaeule fuellt beide. Der Tankname im Tooltip nennt nur den
 * ersten, so wie dort auch.
 *
 * ER SCHIRMT GEGEN NICHTS AB, und das ist kein vergessener Eintrag: im Original traegt er
 * weder setHazardClass noch setRadResist -- als einziger der Tankanzuege. Er steht deshalb
 * weder in ArmorUtil noch in HazmatRegistry, genau wie die Wismut-Garnitur aus Runde 218.
 *
 * NICHT UEBERNOMMEN: enableThermalSight und enableVATS, wie ueberall. Ebenso der
 * Partikeleffekt "bnuuy", den das Beinzeug im Original alle drei Ticks verschickt -- eine
 * Entsprechung dafuer hat der Port nicht.
 */
public class ArmorDieselItem extends ArmorFSBFueledItem {

    public ArmorDieselItem(Holder<ArmorMaterial> material, Type type, Properties properties,
            Supplier<FluidType> fuelType, int maxFuel, int fillRate, int consumption, int drain) {

        super(material, type, properties, fuelType, maxFuel, fillRate, consumption, drain);
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        return type == Fluids.DIESEL || type == Fluids.DIESEL_CRACK;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorDiesel ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorDiesel(original, slot);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
