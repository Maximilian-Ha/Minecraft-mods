package com.hbm.items.armor;

import com.hbm.render.model.armor.ModelArmorNCRPA;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorNCRPA.
 *
 * Die NCR-Panzerruestung. Dieselbe Bauart wie die Remnant, aber SIE HAT BEIDE WAFFEN: Klingen
 * an den Armen und eine Rakete auf der Schulter. Deshalb ist sie die einzige Ruestung im
 * ganzen Port, bei der gun_pa_ranged ueberhaupt etwas tut.
 *
 * Die beiden Bauteile sind statisch und werden geteilt -- siehe ArmorRPAItem.
 *
 * ABWEICHUNG: wie bei der Remnant fehlen VATS, Strahlungsklasse, Strahlenschutz, harte
 * Landung und die eigenen Schrittklaenge; diese Baukastenteile gibt es im Port noch nicht.
 */
public class ArmorNCRPAItem extends ArmorFSBPoweredItem implements IPAWeaponsProvider {

    public static final ArmorNCRPAMelee NAHKAMPF = new ArmorNCRPAMelee();
    public static final ArmorNCRPARanged FERNKAMPF = new ArmorNCRPARanged();

    public ArmorNCRPAItem(Holder<ArmorMaterial> material, Type type, Properties properties, long maxPower, long chargeRate, long consumption, long drain) {
        super(material, type, properties, maxPower, chargeRate, consumption, drain);
    }

    @Override
    public @Nullable IPAMelee getMeleeComponent(Player player) {
        return hasFSBArmorIgnoreCharge(player) ? NAHKAMPF : null;
    }

    @Override
    public @Nullable IPARanged getRangedComponent(Player player) {
        return hasFSBArmorIgnoreCharge(player) ? FERNKAMPF : null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorNCRPA replacement;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(replacement == null) replacement = new ModelArmorNCRPA(original, slot);
                replacement.getPropertiesFrom(original);
                replacement.living = living;
                return replacement;
            }
        });
    }
}
