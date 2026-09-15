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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKIndicator.
 *
 * Sechs Leuchten auf einer Tafel. Jede horcht auf einen RTTY-Kanal und leuchtet, solange der
 * empfangene Wert zwischen ihrem Kleinst- und Groesstwert liegt.
 *
 * Steht der Groesstwert UNTER dem Kleinstwert, dreht sich die Bedingung um: dann leuchtet die
 * Lampe ausserhalb der Grenzen. So laesst sich mit derselben Lampe entweder ein guter Bereich
 * oder ein Alarmbereich anzeigen -- das ist im Original ausdruecklich so gedacht.
 *
 * NICHT UEBERNOMMEN: die OpenComputers-Anbindung, siehe docs/ENTSCHEIDUNGEN.md.
 */
public class RBMKIndicatorBlockEntity extends LoadedBaseBlockEntity implements ITickable, IControlReceiver {

    public static final int INDICATORS = 6;

    public final IndicatorUnit[] indicators = new IndicatorUnit[INDICATORS];

    public RBMKIndicatorBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_INDICATOR.get(), pos, state);

        for(int i = 0; i < INDICATORS; i++) this.indicators[i] = new IndicatorUnit(i);
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        for(IndicatorUnit unit : this.indicators) unit.update();
        this.networkPackNT(50);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(IndicatorUnit unit : this.indicators) unit.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(IndicatorUnit unit : this.indicators) unit.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < INDICATORS; i++) this.indicators[i].load(tag, i);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < INDICATORS; i++) this.indicators[i].save(tag, i);
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 15 * 15;
    }

    @Override
    public void receiveControl(CompoundTag data) {

        int active = data.getByte("active");
        int polling = data.getByte("polling");

        for(int i = 0; i < INDICATORS; i++) {
            IndicatorUnit unit = this.indicators[i];

            unit.active = (active & (1 << i)) != 0;
            unit.polling = (polling & (1 << i)) != 0;
            unit.color = Mth.clamp(data.getInt("color" + i), 0, 0xFFFFFF);
            unit.label = data.getString("label" + i);
            unit.rtty = data.getString("rtty" + i);
            unit.min = data.getInt("min" + i);
            unit.max = data.getInt("max" + i);
        }

        this.setChanged();
    }

    public class IndicatorUnit {

        public boolean polling;
        public int color;
        public String label = "";
        public String rtty = "";
        public long min = 0;
        public long max = 100;
        /** Ob die Lampe gerade leuchtet. */
        public boolean light;
        public boolean active;

        public IndicatorUnit(int index) {
            /* Abwechselnd rot und gelb, wie im Original. */
            this.color = index % 2 == 0 ? 0xFF0000 : 0xFFFF00;
            this.label = "Indicator " + (index + 1);
        }

        public void update() {

            if(!this.active) return;
            if(this.rtty == null || this.rtty.isEmpty()) return;

            RTTYChannel chan = RTTYSystem.listen(RBMKIndicatorBlockEntity.this.level, this.rtty);

            if(chan != null && chan.timeStamp < RBMKIndicatorBlockEntity.this.level.getGameTime() - 1) chan = null;

            if(chan != null && chan.signal != null) {
                long value = 0;
                try { value = Long.parseLong(chan.signal.toString().trim()); } catch(NumberFormatException ignored) { }
                this.decideLight(value);
            } else if(this.polling) {
                this.decideLight(0);
            }
        }

        /** Steht der Groesstwert unter dem Kleinstwert, leuchtet die Lampe ausserhalb statt innerhalb. */
        private void decideLight(long value) {
            if(this.min <= this.max) {
                this.light = value >= this.min && value <= this.max;
            } else {
                this.light = value < this.max || value > this.min;
            }
        }

        public void serialize(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(this.active);
            buf.writeBoolean(this.polling);
            buf.writeBoolean(this.light);
            buf.writeInt(this.color);
            buf.writeUtf(this.label);
            buf.writeUtf(this.rtty);
            buf.writeLong(this.min);
            buf.writeLong(this.max);
        }

        public void deserialize(RegistryFriendlyByteBuf buf) {
            this.active = buf.readBoolean();
            this.polling = buf.readBoolean();
            this.light = buf.readBoolean();
            this.color = buf.readInt();
            this.label = buf.readUtf();
            this.rtty = buf.readUtf();
            this.min = buf.readLong();
            this.max = buf.readLong();
        }

        public void load(CompoundTag tag, int index) {
            this.active = tag.getBoolean("active" + index);
            this.polling = tag.getBoolean("polling" + index);
            this.color = tag.getInt("color" + index);
            this.label = tag.getString("label" + index);
            this.rtty = tag.getString("rtty" + index);
            this.min = tag.getLong("min" + index);
            this.max = tag.getLong("max" + index);
            this.light = tag.getBoolean("light" + index);
        }

        public void save(CompoundTag tag, int index) {
            tag.putBoolean("active" + index, this.active);
            tag.putBoolean("polling" + index, this.polling);
            tag.putInt("color" + index, this.color);
            tag.putString("label" + index, this.label);
            tag.putString("rtty" + index, this.rtty);
            tag.putLong("min" + index, this.min);
            tag.putLong("max" + index, this.max);
            tag.putBoolean("light" + index, this.light);
        }
    }
}
