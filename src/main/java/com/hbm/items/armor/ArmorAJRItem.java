package com.hbm.items.armor;

import com.hbm.render.model.armor.ModelArmorAJR;

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
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorAJR UND ArmorAJRO.
 *
 * Der AJR-Panzeranzug und seine orangene Spielart. Beide Klassen des Originals sind Zeile
 * fuer Zeile dieselbe -- gleiche Oberklasse, gleiche Werte, gleiches Wellenfrontmodell; nur
 * die vier gebundenen Texturen unterscheiden sich. Der Port nimmt deshalb eine Klasse und
 * reicht nur durch, WELCHE der beiden es ist; die Texturen dazu stehen im Modell, weil der
 * ResourceManager clientseitig ist und die Anmeldung nicht.
 *
 * NICHT UEBERNOMMEN, wie bei der T-51 und dem HEV-Anzug: enableVATS, setHasHardLanding,
 * setStep/setJump/setFall und hides(EnumPlayerPart.HAT). Die zugehoerigen Teilsysteme gibt
 * es im Port nicht; ArmorFSBItem sagt das im Kopf fuer alle FSB-Anzuege.
 */
public class ArmorAJRItem extends ArmorFSBPoweredItem {

    /** Welche der beiden Garnituren ein Stueck ist. Mehr unterscheidet sie nicht. */
    public enum Variante { NORMAL, ORANGE }

    private final Variante variante;

    public ArmorAJRItem(Holder<ArmorMaterial> material, Type type, Properties properties,
            long maxPower, long chargeRate, long consumption, long drain, Variante variante) {

        super(material, type, properties, maxPower, chargeRate, consumption, drain);

        this.variante = variante;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorAJR ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorAJR(original, slot, variante);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
