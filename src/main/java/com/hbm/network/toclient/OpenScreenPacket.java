package com.hbm.network.toclient;

import com.hbm.main.NuclearTechMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Oeffnet beim Spieler eine Oberflaeche ohne Container.
 *
 * Hintergrund: in 1.7.10 konnte jede Oberflaeche ueber den GUI-Handler geoeffnet werden, auch
 * eine ohne Inventar -- das Reaktorpult, das Kranpult und die sieben Anzeigetafeln des RBMK
 * sind allesamt solche. In 1.21 fuehrt der uebliche Weg ueber einen MenuProvider, und der
 * setzt einen Container voraus. Fuer die containerlosen Oberflaechen geht es deshalb ueber
 * dieses Paket: der Server nennt Position und Kennung, der Client schlaegt die Kennung in
 * {@link com.hbm.inventory.screens.NoContainerScreens} nach und oeffnet den Bildschirm.
 */
public record OpenScreenPacket(ResourceLocation screen, BlockPos pos) implements CustomPacketPayload {

    public static final Type<OpenScreenPacket> TYPE = new Type<>(NuclearTechMod.withDefaultNamespace("open_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreenPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OpenScreenPacket decode(RegistryFriendlyByteBuf buf) {
            return new OpenScreenPacket(buf.readResourceLocation(), buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, OpenScreenPacket packet) {
            buf.writeResourceLocation(packet.screen);
            buf.writeBlockPos(packet.pos);
        }
    };

    public static void handleClient(OpenScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> NuclearTechMod.proxy.openNoContainerScreen(packet.screen, packet.pos));
    }

    @Override public Type<OpenScreenPacket> type() { return TYPE; }
}
