package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.RTTYSystem;
import com.hbm.blockentity.network.RTTYSystem.RTTYChannel;
import com.hbm.interfaces.IControlReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKGauge.
 *
 * Vier Rundinstrumente auf einer Tafel. Jedes horcht auf einen eigenen RTTY-Kanal und legt den
 * empfangenen Zahlenwert als Zeigerstellung zwischen seinem Kleinst- und Groesstwert an.
 *
 * Ein Instrument im Abfragebetrieb (polling) faellt auf null zurueck, sobald kein Signal mehr
 * kommt; ohne Abfragebetrieb behaelt es den zuletzt empfangenen Wert.
 *
 * NICHT UEBERNOMMEN: die OpenComputers-Anbindung. Das ist so entschieden und in
 * docs/ENTSCHEIDUNGEN.md festgehalten.
 */
public class RBMKGaugeBlockEntity extends LoadedBaseBlockEntity implements ITickable, IControlReceiver {

    public static final int GAUGES = 4;

    public final GaugeUnit[] gauges = new GaugeUnit[GAUGES];

    public RBMKGaugeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_GAUGE.get(), pos, state);

        for(int i = 0; i < GAUGES; i++) this.gauges[i] = new GaugeUnit(i);
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {
            for(GaugeUnit gauge : this.gauges) gauge.update();
            this.networkPackNT(50);
        } else {
            for(GaugeUnit gauge : this.gauges) gauge.updateClient();
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(GaugeUnit gauge : this.gauges) gauge.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(GaugeUnit gauge : this.gauges) gauge.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < GAUGES; i++) this.gauges[i].load(tag, i);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < GAUGES; i++) this.gauges[i].save(tag, i);
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 15 * 15;
    }

    /** Die Einstellungen kommen als Ganzes von der Oberflaeche zurueck. */
    @Override
    public void receiveControl(CompoundTag data) {

        int active = data.getByte("active");
        int polling = data.getByte("polling");

        for(int i = 0; i < GAUGES; i++) {
            GaugeUnit gauge = this.gauges[i];

            gauge.active = (active & (1 << i)) != 0;
            gauge.polling = (polling & (1 << i)) != 0;
            gauge.color = Mth.clamp(data.getInt("color" + i), 0, 0xFFFFFF);
            gauge.label = data.getString("label" + i);
            gauge.rtty = data.getString("rtty" + i);
            gauge.min = data.getInt("min" + i);
            gauge.max = data.getInt("max" + i);
        }

        this.setChanged();
    }

    public class GaugeUnit {

        /** Ob der Wert jeden Tick neu vom Kanal geholt wird -- sonst nur bei Aenderung. */
        public boolean polling;
        /** Farbe des Zeigers. */
        public int color;
        /** Beschriftung auf dem Zifferblatt. */
        public String label = "";
        /** Der Kanal, von dem gelesen wird. */
        public String rtty = "";
        public long min = 0;
        public long max = 100;
        /** Der zuletzt empfangene Wert, also die Zeigerstellung. */
        public long value;
        /** Nur zum Zeichnen: der Zeiger laeuft der Stellung weich hinterher. */
        public double renderValue;
        public double lastRenderValue;
        /** Ob dieses Instrument auf der Tafel ueberhaupt sichtbar ist. */
        public boolean active;

        public GaugeUnit(int index) {
            /* Die vier Anfangsfarben stehen so im Original. */
            if(index == 0) this.color = 0x800000;
            if(index == 1) this.color = 0x804000;
            if(index == 2) this.color = 0x808000;
            if(index == 3) this.color = 0x000080;
            this.label = "Gauge " + (index + 1);
        }

        public void updateClient() {
            this.lastRenderValue = this.renderValue;
            this.renderValue += (this.value - this.renderValue) * 0.1D;
        }

        public void update() {

            if(!this.active) return;
            if(this.rtty == null || this.rtty.isEmpty()) return;

            RTTYChannel chan = RTTYSystem.listen(RBMKGaugeBlockEntity.this.level, this.rtty);

            /* Ein Signal aelter als einen Tick zaehlt nicht mehr als neu. */
            if(chan != null && chan.timeStamp < RBMKGaugeBlockEntity.this.level.getGameTime() - 1) chan = null;

            if(chan != null && chan.signal != null) {
                try {
                    this.value = Long.parseLong(chan.signal.toString().trim());
                } catch(NumberFormatException ignored) { }
            } else if(this.polling) {
                this.value = 0;
            }
        }

        public void serialize(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(this.active);
            buf.writeBoolean(this.polling);
            buf.writeInt(this.color);
            buf.writeUtf(this.label);
            buf.writeUtf(this.rtty);
            buf.writeLong(this.min);
            buf.writeLong(this.max);
            buf.writeLong(this.value);
        }

        public void deserialize(RegistryFriendlyByteBuf buf) {
            this.active = buf.readBoolean();
            this.polling = buf.readBoolean();
            this.color = buf.readInt();
            this.label = buf.readUtf();
            this.rtty = buf.readUtf();
            this.min = buf.readLong();
            this.max = buf.readLong();
            this.value = buf.readLong();
        }

        public void load(CompoundTag tag, int index) {
            this.active = tag.getBoolean("active" + index);
            this.polling = tag.getBoolean("polling" + index);
            this.color = tag.getInt("color" + index);
            this.label = tag.getString("label" + index);
            this.rtty = tag.getString("rtty" + index);
            this.min = tag.getLong("min" + index);
            this.max = tag.getLong("max" + index);
            this.value = tag.getLong("value" + index);
        }

        public void save(CompoundTag tag, int index) {
            tag.putBoolean("active" + index, this.active);
            tag.putBoolean("polling" + index, this.polling);
            tag.putInt("color" + index, this.color);
            tag.putString("label" + index, this.label);
            tag.putString("rtty" + index, this.rtty);
            tag.putLong("min" + index, this.min);
            tag.putLong("max" + index, this.max);
            tag.putLong("value" + index, this.value);
        }
    }
}
