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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKGraph.
 *
 * Zwei Schreiber auf einer Tafel. Jeder liest einen RTTY-Kanal und haelt die letzten dreissig
 * Messwerte -- zweimal je Sekunde ueber fuenfzehn Sekunden.
 *
 * Kleinst- und Groesstwert der Skala lassen sich festnageln. Bleibt ein Feld leer, sucht sich der
 * Schreiber die Grenze aus den vorhandenen Werten -- die Kurve fuellt dann immer das ganze Feld.
 *
 * NICHT UEBERNOMMEN: die OpenComputers-Anbindung, siehe docs/ENTSCHEIDUNGEN.md.
 */
public class RBMKGraphBlockEntity extends LoadedBaseBlockEntity implements ITickable, IControlReceiver {

    public static final int GRAPHS = 2;
    /** Zweimal je Sekunde ueber fuenfzehn Sekunden. */
    public static final int SAMPLES = 30;
    /** Nur jeden zehnten Tick wird gemessen. */
    private static final int SAMPLE_INTERVAL = 10;

    public final GraphUnit[] graphs = new GraphUnit[GRAPHS];

    public RBMKGraphBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_GRAPH.get(), pos, state);

        for(int i = 0; i < GRAPHS; i++) this.graphs[i] = new GraphUnit(i);
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        if(this.level.getGameTime() % SAMPLE_INTERVAL == 0) {
            for(GraphUnit unit : this.graphs) unit.update();
        }

        this.networkPackNT(50);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(GraphUnit unit : this.graphs) unit.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(GraphUnit unit : this.graphs) unit.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < GRAPHS; i++) this.graphs[i].load(tag, i);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < GRAPHS; i++) this.graphs[i].save(tag, i);
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 15 * 15;
    }

    @Override
    public void receiveControl(CompoundTag data) {

        int active = data.getByte("active");
        int polling = data.getByte("polling");

        for(int i = 0; i < GRAPHS; i++) {
            GraphUnit unit = this.graphs[i];

            unit.active = (active & (1 << i)) != 0;
            unit.polling = (polling & (1 << i)) != 0;
            unit.label = data.getString("label" + i);
            unit.rtty = data.getString("rtty" + i);

            /* Ein leer gelassenes Feld heisst: Grenze nicht festnageln. */
            unit.minBound = data.contains("min" + i);
            if(unit.minBound) unit.min = data.getLong("min" + i);

            unit.maxBound = data.contains("max" + i);
            if(unit.maxBound) unit.max = data.getLong("max" + i);

            /* Verkehrt herum eingegebene Grenzen werden getauscht statt abgelehnt. */
            if(unit.max < unit.min) {
                long temp = unit.max;
                unit.max = unit.min;
                unit.min = temp;
            }
        }

        this.setChanged();
    }

    public class GraphUnit {

        /** Ob die Kurve bei ausbleibendem Signal auf null faellt. */
        public boolean polling;
        public String label = "";
        public String rtty = "";
        /** Die letzten Messwerte, der juengste ganz hinten. */
        public final long[] values = new long[SAMPLES];
        public boolean active;
        public long min;
        public boolean minBound;
        public long max;
        public boolean maxBound;

        public GraphUnit(int index) {
            this.label = "Graph " + (index + 1);
        }

        public void update() {

            if(!this.active) return;
            if(this.rtty == null || this.rtty.isEmpty()) return;

            RTTYChannel chan = RTTYSystem.listen(RBMKGraphBlockEntity.this.level, this.rtty);

            if(chan != null && chan.timeStamp < RBMKGraphBlockEntity.this.level.getGameTime() - 1) chan = null;

            if(chan != null && chan.signal != null) {
                /* Wie im Original: ein unleserliches Signal zaehlt als null. */
                long parsed = 0;
                try { parsed = Long.parseLong(chan.signal.toString().trim()); } catch(NumberFormatException ignored) { }
                this.pushValue(parsed);
            } else if(this.polling) {
                this.pushValue(0);
            }
        }

        /** Schiebt die Kurve um einen Messwert weiter. */
        public void pushValue(long value) {

            System.arraycopy(this.values, 1, this.values, 0, this.values.length - 1);
            this.values[this.values.length - 1] = value;
        }

        public void serialize(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(this.active);
            buf.writeBoolean(this.polling);
            buf.writeUtf(this.label);
            buf.writeUtf(this.rtty);
            buf.writeBoolean(this.minBound);
            if(this.minBound) buf.writeLong(this.min);
            buf.writeBoolean(this.maxBound);
            if(this.maxBound) buf.writeLong(this.max);
            /* Eine abgeschaltete Kurve wird nicht gezeichnet, also braucht sie auch keine Werte. */
            if(this.active) for(long value : this.values) buf.writeLong(value);
        }

        public void deserialize(RegistryFriendlyByteBuf buf) {
            this.active = buf.readBoolean();
            this.polling = buf.readBoolean();
            this.label = buf.readUtf();
            this.rtty = buf.readUtf();
            this.minBound = buf.readBoolean();
            if(this.minBound) this.min = buf.readLong();
            this.maxBound = buf.readBoolean();
            if(this.maxBound) this.max = buf.readLong();
            if(this.active) for(int i = 0; i < this.values.length; i++) this.values[i] = buf.readLong();
        }

        public void load(CompoundTag tag, int index) {
            this.active = tag.getBoolean("active" + index);
            this.polling = tag.getBoolean("polling" + index);
            this.label = tag.getString("label" + index);
            this.rtty = tag.getString("rtty" + index);
            this.minBound = tag.getBoolean("minBound" + index);
            this.min = tag.getLong("min" + index);
            this.maxBound = tag.getBoolean("maxBound" + index);
            this.max = tag.getLong("max" + index);
            for(int i = 0; i < this.values.length; i++) this.values[i] = tag.getLong("value" + index + "_" + i);
        }

        public void save(CompoundTag tag, int index) {
            tag.putBoolean("active" + index, this.active);
            tag.putBoolean("polling" + index, this.polling);
            tag.putString("label" + index, this.label);
            tag.putString("rtty" + index, this.rtty);
            tag.putBoolean("minBound" + index, this.minBound);
            tag.putLong("min" + index, this.min);
            tag.putBoolean("maxBound" + index, this.maxBound);
            tag.putLong("max" + index, this.max);
            for(int i = 0; i < this.values.length; i++) tag.putLong("value" + index + "_" + i, this.values[i]);
        }
    }
}
