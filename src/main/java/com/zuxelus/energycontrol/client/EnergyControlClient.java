package com.zuxelus.energycontrol.client;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.client.renderer.InfoPanelRenderer;
import com.zuxelus.energycontrol.client.screens.EnergyCounterScreen;
import com.zuxelus.energycontrol.client.screens.InfoPanelScreen;
import com.zuxelus.energycontrol.client.screens.KitAssemblerScreen;
import com.zuxelus.energycontrol.client.screens.RangeTriggerScreen;
import com.zuxelus.energycontrol.client.screens.ThermalMonitorScreen;
import com.zuxelus.energycontrol.init.ECBlockEntityTypes;
import com.zuxelus.energycontrol.init.ECMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Alles, was nur der Client kennt: Oberflaechen und der Renderer der Tafel. */
@EventBusSubscriber(modid = EnergyControl.MODID, value = Dist.CLIENT)
public class EnergyControlClient {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ECMenuTypes.INFO_PANEL.get(), InfoPanelScreen::new);
        event.register(ECMenuTypes.THERMAL_MONITOR.get(), ThermalMonitorScreen::new);
        event.register(ECMenuTypes.RANGE_TRIGGER.get(), RangeTriggerScreen::new);
        event.register(ECMenuTypes.ENERGY_COUNTER.get(), EnergyCounterScreen::new);
        event.register(ECMenuTypes.KIT_ASSEMBLER.get(), KitAssemblerScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ECBlockEntityTypes.INFO_PANEL.get(), InfoPanelRenderer::new);
    }
}
