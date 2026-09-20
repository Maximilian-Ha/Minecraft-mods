package com.hbm.items.armor;

import com.hbm.render.model.armor.ModelArmorT51;

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
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorT51.
 *
 * Die T-51-Panzerruestung. Ein bestrombarer Satz mit einer Million Ladung, der den Traeger
 * gegen alles abschirmt ausser Licht und ihm neunzig Prozent der Strahlung abnimmt.
 *
 * NICHT UEBERNOMMEN, und zwar aus demselben Grund wie beim HEV-Anzug: enableVATS,
 * setHasHardLanding sowie setStep, setJump und setFall. Alle vier haengen an Teilsystemen, die
 * der Port nicht hat -- ArmorFSBItem sagt das im Kopf ausdruecklich fuer alle FSB-Anzuege.
 * Ebenso hides(EnumPlayerPart.HAT): der Port kennt das Ausblenden von Spielerteilen nicht.
 */
public class ArmorT51Item extends ArmorFSBPoweredItem {

    public ArmorT51Item(Holder<ArmorMaterial> material, Type type, Properties properties, long maxPower, long chargeRate, long consumption, long drain) {
        super(material, type, properties, maxPower, chargeRate, consumption, drain);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorT51 ersatz;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(ersatz == null) ersatz = new ModelArmorT51(original, slot);
                ersatz.getPropertiesFrom(original);
                ersatz.living = living;
                return ersatz;
            }
        });
    }
}
