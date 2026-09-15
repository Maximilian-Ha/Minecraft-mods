package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.RTTYSystem;
import com.hbm.interfaces.IControlReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKKeyPad.
 *
 * Vier Drucktasten in zwei Reihen. Jede schickt beim Druck einen frei eingestellten Befehl ueber
 * einen RTTY-Kanal.
 *
 * Eine Taste ohne "staendig senden" sendet einmal und springt nach sieben Ticks wieder heraus.
 * Eine Taste mit "staendig senden" rastet ein und wiederholt ihren Befehl, bis man sie wieder
 * loest.
 *
 * NICHT UEBERNOMMEN: die OpenComputers-Anbindung, siehe docs/ENTSCHEIDUNGEN.md.
 */
public class RBMKKeyPadBlockEntity extends LoadedBaseBlockEntity implements ITickable, IControlReceiver {

    public static final int KEYS = 4;

    public final KeyUnit[] keys = new KeyUnit[KEYS];

    public RBMKKeyPadBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_KEYPAD.get(), pos, state);

        for(int i = 0; i < KEYS; i++) this.keys[i] = new KeyUnit(i);
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        for(KeyUnit unit : this.keys) unit.update();
        this.networkPackNT(50);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(KeyUnit unit : this.keys) unit.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(KeyUnit unit : this.keys) unit.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < KEYS; i++) this.keys[i].load(tag, i);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < KEYS; i++) this.keys[i].save(tag, i);
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 15 * 15;
    }

    @Override
    public void receiveControl(CompoundTag data) {

        int active = data.getByte("active");
        int polling = data.getByte("polling");

        for(int i = 0; i < KEYS; i++) {
            KeyUnit unit = this.keys[i];

            unit.active = (active & (1 << i)) != 0;
            unit.polling = (polling & (1 << i)) != 0;
            unit.color = Mth.clamp(data.getInt("color" + i), 0, 0xffffff);
            unit.label = data.getString("label" + i);
            unit.rtty = data.getString("rtty" + i);
            unit.command = data.getString("cmd" + i);
        }

        this.setChanged();
    }

    public class KeyUnit {

        /** Wie lange eine Taste ohne "staendig senden" gedrueckt aussieht. */
        private static final int CLICK_TIME = 7;

        /** Ob der Befehl jeden Tick wiederholt wird -- dann rastet die Taste ein. */
        public boolean polling;
        public boolean isPressed;
        public int color;
        public String label = "";
        public String rtty = "";
        public String command = "";
        public boolean active;
        public int clickTimer;

        public KeyUnit(int index) {
            /* Vier Farben von oben links nach unten rechts, wie im Original. */
            this.color = switch(index) {
                case 0 -> 0xff0000;
                case 1 -> 0xffff00;
                case 2 -> 0x0080ff;
                default -> 0x00ff00;
            };
            this.label = "Button " + (index + 1);
        }

        /** Ein Spieler hat die Taste gedrueckt. */
        public void click() {

            if(!this.active) return;

            if(!this.polling) {
                if(this.canSend()) RTTYSystem.broadcast(RBMKKeyPadBlockEntity.this.level, this.rtty, this.command);
                this.isPressed = true;
                this.clickTimer = CLICK_TIME;
            } else {
                this.isPressed = !this.isPressed;
                RBMKKeyPadBlockEntity.this.setChanged();
            }

            RBMKKeyPadBlockEntity.this.level.playSound(null, RBMKKeyPadBlockEntity.this.worldPosition,
                    SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 1F, this.isPressed ? 1F : 0.75F);
        }

        public void update() {

            if(!this.active) return;

            if(this.polling && this.isPressed) {
                if(this.canSend()) RTTYSystem.broadcast(RBMKKeyPadBlockEntity.this.level, this.rtty, this.command);
            }

            if(!this.polling && this.isPressed) {
                if(this.clickTimer-- <= 0) this.isPressed = false;
            }
        }

        public boolean canSend() {
            return this.rtty != null && !this.rtty.isEmpty() && this.command != null && !this.command.isEmpty();
        }

        public void serialize(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(this.active);
            buf.writeBoolean(this.polling);
            buf.writeBoolean(this.isPressed);
            buf.writeInt(this.color);
            buf.writeUtf(this.label);
            buf.writeUtf(this.rtty);
            buf.writeUtf(this.command);
        }

        public void deserialize(RegistryFriendlyByteBuf buf) {
            this.active = buf.readBoolean();
            this.polling = buf.readBoolean();
            this.isPressed = buf.readBoolean();
            this.color = buf.readInt();
            this.label = buf.readUtf();
            this.rtty = buf.readUtf();
            this.command = buf.readUtf();
        }

        public void load(CompoundTag tag, int index) {
            this.active = tag.getBoolean("active" + index);
            this.polling = tag.getBoolean("polling" + index);
            this.isPressed = tag.getBoolean("isPressed" + index);
            this.color = tag.getInt("color" + index);
            this.label = tag.getString("label" + index);
            this.rtty = tag.getString("rtty" + index);
            this.command = tag.getString("command" + index);
        }

        public void save(CompoundTag tag, int index) {
            tag.putBoolean("active" + index, this.active);
            tag.putBoolean("polling" + index, this.polling);
            tag.putBoolean("isPressed" + index, this.isPressed);
            tag.putInt("color" + index, this.color);
            tag.putString("label" + index, this.label);
            tag.putString("rtty" + index, this.rtty);
            tag.putString("command" + index, this.command);
        }
    }
}
