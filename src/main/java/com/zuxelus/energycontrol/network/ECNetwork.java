package com.zuxelus.energycontrol.network;

import com.zuxelus.energycontrol.EnergyControl;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Die Pakete des Mods -- es ist genau eines. */
@EventBusSubscriber(modid = EnergyControl.MODID)
public class ECNetwork {

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(PanelControl.TYPE, PanelControl.STREAM_CODEC, PanelControl::handleServer);
    }
}
