package com.hbm.items.armor;

import com.hbm.render.model.armor.ModelArmorTaurun;

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
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorTaurun.
 *
 * Die Taurun-Ruestung. Ein Satz ohne Strom: Staerke I fuer den ganzen Satz, voller
 * Gefahrenschutz, ein Viertel der Strahlung abgehalten.
 *
 * SIE GEHT NICHT KAPUTT. Das Original ruft im Konstruktor setMaxDamage(0); der Port gibt dem
 * Gegenstand schlicht keine Haltbarkeit. Das ist dasselbe und passt zu ihrer Herkunft -- sie
 * ist keine Bauruestung, sondern Beute: der Untote Soldat traegt sie, und sie liegt in den
 * Beutetoepfen der Halde. Im Original gibt es fuer sie kein einziges Werkbankrezept.
 *
 * NICHT UEBERNOMMEN: setStepSize(1) und hides(EnumPlayerPart.HAT). Eine Schritthoehe kennt
 * der Port an Ruestung nirgends, und das Ausblenden von Spielerteilen ebenso wenig -- beides
 * waere ein Schalter, den niemand liest.
 */
public class ArmorTaurunItem extends ArmorFSBItem {

    public ArmorTaurunItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorTaurun ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorTaurun(original, slot);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
