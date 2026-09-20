package com.hbm.items.machine;

import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.fluid.Fluids;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemGasTank.
 *
 * Die volle Gasflasche. Wie der volle Kanister ein Meta-Gegenstand: welches Gas drin ist,
 * steht in den Metadaten, und der Name setzt sich daraus zusammen.
 *
 * DREI SCHICHTEN statt der zwei des Kanisters: der Flaschenkoerper und das Etikett werden
 * getrennt eingefaerbt, weil CD_Gastank zwei Farben mitbringt (bottleColor, labelColor). Das
 * Original loest das mit drei Darstellungsdurchgaengen; in 1.21 sind es drei Texturschichten
 * mit je einem eigenen Farbton, angemeldet in NuclearTechModClient.
 */
public class GasTankItem extends Item {

    public GasTankItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(), Fluids.fromID(MetaHelper.getMeta(stack)).getName());
    }
}
