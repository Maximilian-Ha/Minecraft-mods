package com.zuxelus.energycontrol.client.screens;

import com.zuxelus.energycontrol.network.PanelControl;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.network.PacketDistributor;

/** Kurzer Weg von einer Oberflaeche zum Block, zu dem sie gehoert. */
public final class ControlSender {

    private ControlSender() { }

    public static void send(BlockPos pos, CompoundTag tag) {
        PacketDistributor.sendToServer(new PanelControl(pos, tag));
    }

    public static void sendInt(BlockPos pos, String action, int value) {
        CompoundTag tag = new CompoundTag();
        tag.putString("action", action);
        tag.putInt("value", value);
        send(pos, tag);
    }

    public static void sendLong(BlockPos pos, String action, long value) {
        CompoundTag tag = new CompoundTag();
        tag.putString("action", action);
        tag.putLong("value", value);
        send(pos, tag);
    }
}
