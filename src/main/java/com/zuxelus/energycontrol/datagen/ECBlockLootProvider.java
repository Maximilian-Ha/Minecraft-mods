package com.zuxelus.energycontrol.datagen;

import com.zuxelus.energycontrol.init.ECBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.stream.Collectors;

/** Jeder Block dieses Mods laesst sich selbst fallen. */
public class ECBlockLootProvider extends BlockLootSubProvider {

    protected ECBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        ECBlocks.BLOCKS.getEntries().forEach(holder -> dropSelf(holder.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ECBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get()).collect(Collectors.toList());
    }
}
