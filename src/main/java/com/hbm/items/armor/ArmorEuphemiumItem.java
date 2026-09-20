package com.hbm.items.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorEuphemium.
 *
 * Der Euphemium-Satz. Wer alle vier Teile traegt, bekommt dauerhaft Regeneration,
 * Widerstandskraft, Feuerschutz und Saettigung in der hoechsten Stufe -- und faellt nicht
 * mehr schnell als ein Blatt.
 *
 * WARUM ER AUF ArmorFSBItem SITZT, obwohl das Original ein schlichtes ItemArmor ist: die
 * Satzpruefung ist dieselbe Sache. Das Original zaehlt in ArmorUtil.checkArmor die vier
 * Gegenstaende einzeln auf, ArmorFSBItem vergleicht das Material der vier getragenen Teile.
 * Fuer diesen Satz ist das dasselbe -- NtmArmorMaterials.EUPHEMIUM traegt genau diese vier
 * Teile und sonst nichts. Zwei Satzpruefungen nebeneinander waeren eine zu viel.
 *
 * ER GEHT NICHT KAPUTT. Das Original erreicht das, indem es setDamage leer laesst: die
 * Haltbarkeitsleiste steht da und bewegt sich nie. Der Port gibt dem Gegenstand stattdessen
 * gar keine Haltbarkeit. Das Ergebnis am Spieler ist dasselbe, nur ohne die Leiste, die
 * ohnehin nie sinkt.
 */
public class ArmorEuphemiumItem extends ArmorFSBItem {

    /**
     * Schneller als das faellt niemand mit diesem Satz. Der Wert steht so im Original; ein
     * Sturz aus beliebiger Hoehe endet damit harmlos, weil die Fallhoehe gleich mit
     * zurueckgesetzt wird.
     */
    private static final double MAX_FALLGESCHWINDIGKEIT = -0.25D;

    public ArmorEuphemiumItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {

        super.inventoryTick(stack, level, entity, slotId, isSelected);

        /* Dieselben Waechter wie in der Oberklasse: nur die Brustplatte handelt, und nur bei
         * vollstaendigem Satz. Sonst bremste jedes der vier Teile denselben Sturz.
         *
         * ABSICHTLICH OHNE isClientSide-Waechter, anders als die Trankwirkungen darueber. Eine
         * Bewegungsaenderung muss auf beiden Seiten geschehen, sonst ruckelt der Fall; das
         * Original steht mit onArmorTick ebenfalls auf beiden Seiten. */
        if(!(entity instanceof Player player)) return;
        if(player.getItemBySlot(EquipmentSlot.CHEST) != stack) return;
        if(!hasFSBArmor(player)) return;

        Vec3 bewegung = player.getDeltaMovement();

        if(bewegung.y < MAX_FALLGESCHWINDIGKEIT) {
            player.setDeltaMovement(bewegung.x, MAX_FALLGESCHWINDIGKEIT, bewegung.z);
            player.fallDistance = 0F;
        }
    }
}
