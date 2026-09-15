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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKNumitron.
 *
 * Zwei Ziffernanzeigen auf einer Tafel, jede mit sieben Stellen. Was angezeigt wird, kommt ueber
 * einen RTTY-Kanal.
 *
 * Je Anzeige laesst sich einstellen, ob fuehrende Nullen mitgeschrieben werden und ob grosse
 * Zahlen abgekuerzt werden sollen.
 *
 * activeDigits, die Bitmaske der leuchtenden Stellen, bleibt auf ihrem Vorgabewert: im Original
 * setzt sie ausschliesslich die OpenComputers-Anbindung, und die ist nicht uebernommen. Der
 * Wert wird trotzdem gespeichert und mitgeschickt, damit eine spaetere Anbindung ihn vorfindet.
 *
 * NICHT UEBERNOMMEN: die OpenComputers-Anbindung, siehe docs/ENTSCHEIDUNGEN.md.
 */
public class RBMKNumitronBlockEntity extends LoadedBaseBlockEntity implements ITickable, IControlReceiver {

    public static final int DISPLAYS = 2;
    /** Sieben Stellen je Anzeige; das oberste Bit bleibt ungenutzt, so steht es im Original. */
    public static final int DIGITS = 7;

    public final DisplayUnit[] displays = new DisplayUnit[DISPLAYS];

    public RBMKNumitronBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_NUMITRON.get(), pos, state);

        for(int i = 0; i < DISPLAYS; i++) this.displays[i] = new DisplayUnit(i);
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        for(DisplayUnit unit : this.displays) unit.update();
        this.networkPackNT(50);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(DisplayUnit unit : this.displays) unit.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(DisplayUnit unit : this.displays) unit.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < DISPLAYS; i++) this.displays[i].load(tag, i);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < DISPLAYS; i++) this.displays[i].save(tag, i);
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 15 * 15;
    }

    @Override
    public void receiveControl(CompoundTag data) {

        int active = data.getByte("active");
        int polling = data.getByte("polling");
        int shorten = data.getByte("shorten_number");
        int leading = data.getByte("leading_zeroes");

        for(int i = 0; i < DISPLAYS; i++) {
            DisplayUnit unit = this.displays[i];

            unit.active = (active & (1 << i)) != 0;
            unit.polling = (polling & (1 << i)) != 0;
            unit.shortenNumber = (shorten & (1 << i)) != 0;
            unit.leadingZeroes = (leading & (1 << i)) != 0;
            unit.label = data.getString("label" + i);
            unit.rtty = data.getString("rtty" + i);
        }

        this.setChanged();
    }

    public class DisplayUnit {

        public boolean polling;
        public String label = "";
        public String rtty = "";
        public long value;
        public boolean active;
        /** Ob fuehrende Nullen weggelassen werden. */
        public boolean leadingZeroes;
        /** Welche Stellen leuchten, als Bitmaske. */
        public long activeDigits;
        /** Ob grosse Zahlen abgekuerzt werden. */
        public boolean shortenNumber;

        public DisplayUnit(int index) {
            this.label = "Display " + (index + 1);
            this.activeDigits = 0b01111111;
            this.shortenNumber = true;
            this.leadingZeroes = true;
        }

        public void update() {

            if(!this.active) return;
            if(this.rtty == null || this.rtty.isEmpty()) return;

            RTTYChannel chan = RTTYSystem.listen(RBMKNumitronBlockEntity.this.level, this.rtty);

            if(chan != null && chan.timeStamp < RBMKNumitronBlockEntity.this.level.getGameTime() - 1) chan = null;

            if(chan != null && chan.signal != null) {
                /* Wie im Original: ein unleserliches Signal setzt die Anzeige auf null zurueck. */
                long parsed = 0;
                try { parsed = Long.parseLong(chan.signal.toString().trim()); } catch(NumberFormatException ignored) { }
                this.value = parsed;
            } else if(this.polling) {
                /* Kein frisches Signal, und die Anzeige fragt staendig ab -- also nichts anzeigen. */
                this.value = 0;
            }
        }

        public void serialize(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(this.shortenNumber);
            buf.writeLong(this.activeDigits);
            buf.writeBoolean(this.leadingZeroes);
            buf.writeBoolean(this.active);
            buf.writeBoolean(this.polling);
            buf.writeUtf(this.label);
            buf.writeUtf(this.rtty);
            buf.writeLong(this.value);
        }

        public void deserialize(RegistryFriendlyByteBuf buf) {
            this.shortenNumber = buf.readBoolean();
            this.activeDigits = buf.readLong();
            this.leadingZeroes = buf.readBoolean();
            this.active = buf.readBoolean();
            this.polling = buf.readBoolean();
            this.label = buf.readUtf();
            this.rtty = buf.readUtf();
            this.value = buf.readLong();
        }

        public void load(CompoundTag tag, int index) {
            this.shortenNumber = tag.getBoolean("shorten_number" + index);
            this.activeDigits = tag.getLong("active_digits" + index);
            this.leadingZeroes = tag.getBoolean("leading_zeroes" + index);
            this.active = tag.getBoolean("active" + index);
            this.polling = tag.getBoolean("polling" + index);
            this.label = tag.getString("label" + index);
            this.rtty = tag.getString("rtty" + index);
            this.value = tag.getLong("value" + index);
        }

        public void save(CompoundTag tag, int index) {
            tag.putBoolean("shorten_number" + index, this.shortenNumber);
            tag.putLong("active_digits" + index, this.activeDigits);
            tag.putBoolean("leading_zeroes" + index, this.leadingZeroes);
            tag.putBoolean("active" + index, this.active);
            tag.putBoolean("polling" + index, this.polling);
            tag.putString("label" + index, this.label);
            tag.putString("rtty" + index, this.rtty);
            tag.putLong("value" + index, this.value);
        }
    }
}
