package com.hbm.items.armor;

import com.hbm.render.model.armor.ModelArmorGoggles;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorAshGlasses.
 *
 * Die Aschebrille. Sie tut nichts ausser gut aussehen -- das Original haengt ihr weder
 * Gefahrenklassen noch Strahlenschutz noch ein Schirmbild an, sie ist ein gewoehnlicher
 * Helmgegenstand aus Eisen mit einem eigenen Kopfmodell.
 *
 * ANDERS ALS DIE MASKEN AUS RUNDE 206 UND 209 braucht sie keinen eigenen Werkstoff: ihr Modell
 * ist ein Wellenfrontmodell und bindet seine Textur selbst ueber RenderContext, statt sie von
 * der Ruestungsschicht zu beziehen. Das ist derselbe Weg, den HEV- und RPA-Ruestung gehen.
 */
public class ArmorAshGlassesItem extends ArmorItem {

    public ArmorAshGlassesItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties.stacksTo(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private ModelArmorGoggles ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorGoggles(original, slot);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
