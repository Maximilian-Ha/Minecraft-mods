package com.hbm.blocks;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * Die Klangsaetze der Tueren und Falltueren dieses Mods.
 *
 * In 1.21 haengt an einem BlockSetType nicht nur, wie eine Tuer klingt, sondern auch, OB sie
 * sich von Hand oeffnen laesst. Minecraft bringt fuer Metall nur den Eisensatz mit, und der
 * verbietet das Oeffnen von Hand -- eine Eisentuer geht nur mit Redstone auf.
 *
 * Die Tueren des Originals sind aus Metall, lassen sich aber sehr wohl von Hand oeffnen: sie
 * ueberschreiben die Pruefung, die Minecraft dafuer eingebaut hat. Genau dafuer steht hier ein
 * eigener Satz -- er klingt wie Eisen und laesst sich anfassen.
 *
 * Die Falltuer braucht ihn nicht: die des Originals uebernimmt die Pruefung unveraendert und
 * geht deshalb wirklich nur mit Redstone auf. Sie benutzt den Eisensatz.
 */
public class NtmBlockSetTypes {

    public static final BlockSetType METAL = BlockSetType.register(new BlockSetType(
            "hbmsntm_metal",
            true,   // von Hand zu oeffnen -- der Unterschied zum Eisensatz
            true,
            true,
            BlockSetType.PressurePlateSensitivity.MOBS,
            SoundType.METAL,
            SoundEvents.IRON_DOOR_CLOSE,
            SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE,
            SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF,
            SoundEvents.STONE_BUTTON_CLICK_ON));

    private NtmBlockSetTypes() { }
}
