package com.zuxelus.energycontrol.datagen;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.init.ECItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Gegenstandsmodelle. Die Modelle der Blockgegenstaende entstehen beim Blockmodell;
 * hier bleiben die flachen Bilder aller uebrigen Gegenstaende.
 */
public class ECItemModelProvider extends ItemModelProvider {

    public ECItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, EnergyControl.MODID, helper);
    }

    @Override
    protected void registerModels() {
        ECItems.ITEMS.getEntries().forEach(holder -> {
            if(holder.get() instanceof BlockItem) return;
            basicItem(holder.get());
        });
    }
}
