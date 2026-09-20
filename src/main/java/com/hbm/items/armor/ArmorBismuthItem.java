package com.hbm.items.armor;

import com.hbm.render.model.armor.ModelArmorBismuth;

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
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorBismuth.
 *
 * Die Wismut-Garnitur -- Kopfschmuck, Schulterstuecke, Knieschuetzer und Sandalen. Kein
 * Panzeranzug, sondern Zierat mit Wirkung: der ganze Satz gibt Sprungkraft VII, Tempo VII,
 * Regeneration II und Nachtsicht.
 *
 * SIE SCHIRMT GEGEN NICHTS AB. Anders als jede andere Wellenfront-Ruestung des Ports traegt
 * sie im Original weder setHazardClass noch setRadResist. Deshalb steht sie weder in
 * ArmorUtil noch in HazmatRegistry -- das ist kein vergessener Eintrag, sondern ihr Zustand.
 *
 * NICHT UEBERNOMMEN: setDashCount(3). Einen Sprung nach vorn ("dash") kennt der Port nicht,
 * genau wie bei HEV, T-51 und AJR.
 */
public class ArmorBismuthItem extends ArmorFSBItem {

    public ArmorBismuthItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorBismuth ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorBismuth(original, slot);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
