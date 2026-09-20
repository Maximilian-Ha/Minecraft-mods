package com.hbm.items.armor;

import com.hbm.render.model.armor.ModelArmorRPA;

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
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorRPA.
 *
 * Die Remnant-Panzerruestung. Ein bestromter Vollsatz wie der HEV-Anzug, aber mit einer
 * Zutat, die er nicht hat: WER SIE VOLLSTAENDIG TRAEGT, BEKOMMT WAFFEN DAZU. Nicht in die
 * Hand -- an die Ruestung. gun_pa_melee fragt bei jedem Klick hier nach, was zu tun ist.
 *
 * DAS BAUTEIL IST STATISCH UND WIRD GETEILT. Es haelt keinen Zustand; alles, was zwischen
 * zwei Schlaegen zu merken ist, steht im Gegenstand selbst. Vier Ruestungsteile und beliebig
 * viele Spieler kommen darum mit einem einzigen aus.
 *
 * ABWEICHUNG: das Original haengt an dieser Ruestung ausserdem VATS, Strahlungsklasse,
 * Strahlenschutz, harte Landung und eigene Schritt- und Sprungklaenge. Diese Baukastenteile
 * gibt es im Port noch nicht -- ArmorFSBItem kennt bisher Effekte, Geigerton und die Frage,
 * ob ein Helm dazugehoert. Was da ist, ist uebernommen; was fehlt, fehlt sichtbar und nicht
 * still.
 */
public class ArmorRPAItem extends ArmorFSBPoweredItem implements IPAWeaponsProvider {

    public static final ArmorRPAMelee NAHKAMPF = new ArmorRPAMelee();

    public ArmorRPAItem(Holder<ArmorMaterial> material, Type type, Properties properties, long maxPower, long chargeRate, long consumption, long drain) {
        super(material, type, properties, maxPower, chargeRate, consumption, drain);
    }

    @Override
    public @Nullable IPAMelee getMeleeComponent(Player player) {
        return hasFSBArmorIgnoreCharge(player) ? NAHKAMPF : null;
    }

    /** Die Remnant hat keine Fernwaffe -- das ist der NCR-Ruestung vorbehalten. */
    @Override
    public @Nullable IPARanged getRangedComponent(Player player) {
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorRPA replacement;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(replacement == null) replacement = new ModelArmorRPA(original, slot);
                replacement.getPropertiesFrom(original);
                replacement.living = living;
                return replacement;
            }
        });
    }
}
