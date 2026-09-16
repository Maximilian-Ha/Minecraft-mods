package com.zuxelus.energycontrol.network;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.blockentity.IControlReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Eine Eingabe aus einer Oberflaeche an den Block, zu dem sie gehoert.
 *
 * Ein Paket fuer alle Faelle statt eines je Knopf: was gemeint ist, steht im Beutel unter
 * "action". Das Original hatte dafuer drei Pakete (PacketCard, PacketKeys, PacketAlarm) und
 * fuer die Schalter noch einmal eigene Wege; hier laufen die Schalter ueber
 * {@code clickMenuButton} von Minecraft und nur die Werte ueber dieses Paket.
 */
public record PanelControl(BlockPos pos, CompoundTag data) implements CustomPacketPayload {

    public static final Type<PanelControl> TYPE = new Type<>(EnergyControl.loc("panel_control"));

    public static final StreamCodec<FriendlyByteBuf, PanelControl> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PanelControl decode(FriendlyByteBuf buf) {
            return new PanelControl(buf.readBlockPos(), buf.readNbt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, PanelControl packet) {
            buf.writeBlockPos(packet.pos);
            buf.writeNbt(packet.data);
        }
    };

    public static void handleServer(PanelControl packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if(player == null || packet.data == null) return;

            Level level = player.level();
            // Ein Paket auf einen ungeladenen Chunk wuerde ihn laden -- und das auf Zuruf
            // vom Client. Also nur, was ohnehin schon da ist.
            if(!level.isLoaded(packet.pos)) return;

            BlockEntity be = level.getBlockEntity(packet.pos);
            if(!(be instanceof IControlReceiver receiver)) return;
            if(!receiver.hasPermission(player)) return;

            receiver.receiveControl(player, packet.data);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
