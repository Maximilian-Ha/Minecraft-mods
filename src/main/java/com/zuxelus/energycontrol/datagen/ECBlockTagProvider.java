package com.zuxelus.energycontrol.datagen;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.init.ECBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Ohne Eintrag in mineable/pickaxe laesst sich ein Block mit
 * requiresCorrectToolForDrops nicht abbauen -- er gibt dann gar nichts.
 */
public class ECBlockTagProvider extends BlockTagsProvider {

    public ECBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        super(output, registries, EnergyControl.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        ECBlocks.BLOCKS.getEntries().forEach(holder -> pickaxe.add((Block) holder.get()));
        ECBlocks.BLOCKS.getEntries().forEach(holder -> tag(BlockTags.NEEDS_STONE_TOOL).add((Block) holder.get()));
    }
}
