package com.hbm.inventory.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Die Oberflaechen ohne Container, nach Kennung.
 *
 * Siehe {@link com.hbm.network.toclient.OpenScreenPacket}: der uebliche Weg ueber einen
 * MenuProvider setzt einen Container voraus, den diese Oberflaechen nicht haben. Der Server
 * schickt stattdessen Kennung und Position, und hier steht, was daraus wird.
 */
public class NoContainerScreens {

    private static final Map<ResourceLocation, Function<BlockEntity, Screen>> SCREENS = new HashMap<>();

    public static void register(ResourceLocation key, Function<BlockEntity, Screen> factory) {
        SCREENS.put(key, factory);
    }

    /** Liefert den Bildschirm zur Kennung, oder null, wenn die Block-Entitaet nicht passt. */
    public static @Nullable Screen create(ResourceLocation key, @Nullable BlockEntity be) {

        Function<BlockEntity, Screen> factory = SCREENS.get(key);
        if(factory == null || be == null) return null;

        return factory.apply(be);
    }

    public static boolean has(ResourceLocation key) {
        return SCREENS.containsKey(key);
    }
}
