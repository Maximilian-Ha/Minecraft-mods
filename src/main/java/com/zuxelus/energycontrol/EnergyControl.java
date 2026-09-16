package com.zuxelus.energycontrol;

import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.init.ECBlockEntityTypes;
import com.zuxelus.energycontrol.init.ECBlocks;
import com.zuxelus.energycontrol.init.ECCapabilities;
import com.zuxelus.energycontrol.init.ECCreativeTabs;
import com.zuxelus.energycontrol.init.ECItems;
import com.zuxelus.energycontrol.init.ECMenuTypes;
import com.zuxelus.energycontrol.init.ECSounds;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Energy Control fuer Minecraft 1.21.1 / NeoForge.
 *
 * Portiert aus 1.12.2 (Forge): com.zuxelus.energycontrol.EnergyControl von Zuxelus,
 * seinerseits die Fortfuehrung von Nuclear Control (Shedar). Der Mod zeigt Messwerte
 * anderer Maschinen auf Informationstafeln an; die Werte holen Sensorkarten, die auf
 * einen Block eingemessen sind.
 */
@Mod(EnergyControl.MODID)
public class EnergyControl {

    public static final String MODID = "energycontrol";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public EnergyControl(IEventBus eventBus, ModContainer container) {
        ECItems.register(eventBus);
        ECBlocks.register(eventBus);
        ECBlockEntityTypes.register(eventBus);
        ECMenuTypes.register(eventBus);
        ECSounds.register(eventBus);
        ECCreativeTabs.register(eventBus);

        eventBus.addListener(this::commonSetup);
        eventBus.addListener(ECCapabilities::register);

        ECConfig.register(container);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Erst hier steht fest, welche Mods geladen sind.
        event.enqueueWork(CrossModLoader::init);
    }
}
