package com.hbm.blockentity.network;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.RTTYSystem.RTTYChannel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityRadioTorchLogic.
 *
 * Ein Empfaenger, der nicht einfach durchreicht: er vergleicht die empfangene Nachricht der
 * Reihe nach gegen sechzehn Bedingungen und gibt die Nummer der ersten zutreffenden als
 * Redstone-Staerke aus. Trifft keine zu, ist die Staerke null.
 *
 * Jede Zeile besteht aus einer Vergleichsart und einer Konstanten. Die Arten 0 bis 5
 * rechnen (kleiner, kleinergleich, groessergleich, groesser, gleich, ungleich) und ueberspringen
 * die Zeile, wenn sich Nachricht oder Konstante nicht als Zahl lesen lassen; die Arten 6 bis 9
 * vergleichen Zeichenketten (gleich, ungleich, enthaelt, enthaelt nicht).
 *
 * Die Reihenfolge laesst sich umdrehen: aufsteigend gewinnt die kleinste zutreffende Zeile,
 * absteigend die groesste.
 *
 * Das Original leitet diese Blockentitaet nicht von TileEntityRadioTorchBase ab, sondern
 * wiederholt deren Felder. Im Port erbt sie: bis auf customMap benutzt sie alle davon.
 */
public class RadioTorchLogicBlockEntity extends RadioTorchBaseBlockEntity {

    /** Kehrt die Auswertungsreihenfolge um -- dann gewinnt die hoechste zutreffende Zeile. */
    public boolean descending = false;

    /** Vergleichsart je Zeile, 0 bis 9. */
    public int[] conditions = new int[16];

    public RadioTorchLogicBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RADIO_TORCH_LOGIC.get(), pos, state);

        for(int i = 0; i < 16; i++) this.mapping[i] = "";
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide && (!this.channel.isEmpty() || this.polling)) {

            RTTYChannel chan = this.channel.isEmpty() ? null : RTTYSystem.listen(this.level, this.channel);

            if(chan != null && (this.polling || (chan.timeStamp > this.lastUpdate - 1 && chan.timeStamp != -1))) {

                String msg = "" + chan.signal;
                this.lastUpdate = this.level.getGameTime();
                int nextState = 0;

                /*
                 * Beim Abfragen ohne frische Nachricht ist die Eingabe undefiniert -- das
                 * Original setzt sie auf "0", damit Ungleichungen weiterrechnen statt
                 * durchzufallen. Wer Zeichenketten vergleicht, kommt damit zurecht.
                 */
                if(chan.timeStamp < this.lastUpdate - 1 && this.polling) msg = "0";

                if(this.descending) {
                    for(int i = 15; i >= 0; i--) {
                        if(!this.mapping[i].isEmpty() && this.bedingungTrifftZu(msg, i)) { nextState = i; break; }
                    }
                } else {
                    for(int i = 0; i <= 15; i++) {
                        if(!this.mapping[i].isEmpty() && this.bedingungTrifftZu(msg, i)) { nextState = i; break; }
                    }
                }

                this.setzeStaerke(nextState);

            } else if(this.polling && this.lastState != 0) {
                this.setzeStaerke(0);
            }
        }

        super.updateEntity();
    }

    private void setzeStaerke(int staerke) {

        if(this.lastState == staerke || this.level == null) return;

        this.lastState = staerke;
        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        this.level.updateNeighborsAt(this.worldPosition, state.getBlock());
        this.setChanged();
    }

    public boolean bedingungTrifftZu(String signal, int index) {

        if(this.conditions[index] <= 5) {

            long sig;
            long map;
            try {
                sig = Long.parseLong(signal);
                map = Long.parseLong(this.mapping[index]);
            } catch(NumberFormatException x) {
                return false; // keine gueltige Zahl -- Zeile ueberspringen
            }

            return switch(this.conditions[index]) {
                case 1 -> sig <= map;
                case 2 -> sig >= map;
                case 3 -> sig > map;
                case 4 -> sig == map;
                case 5 -> sig != map;
                default -> sig < map;
            };
        }

        return switch(this.conditions[index]) {
            case 7 -> !signal.equals(this.mapping[index]);
            case 8 -> signal.contains(this.mapping[index]);
            case 9 -> !signal.contains(this.mapping[index]);
            default -> signal.equals(this.mapping[index]);
        };
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(this.level != null) this.lastUpdate = this.level.getGameTime();
    }

    @Override
    public void receiveControl(CompoundTag data) {
        super.receiveControl(data);

        if(data.contains("d")) this.descending = data.getBoolean("d");
        for(int i = 0; i < 16; i++) if(data.contains("k" + i)) this.conditions[i] = data.getInt("k" + i);

        this.setChanged();
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.descending);
        for(int i = 0; i < 16; i++) buf.writeInt(this.conditions[i]);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.descending = buf.readBoolean();
        for(int i = 0; i < 16; i++) this.conditions[i] = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.descending = tag.getBoolean("d");
        for(int i = 0; i < 16; i++) this.conditions[i] = tag.getInt("k" + i);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("d", this.descending);
        for(int i = 0; i < 16; i++) tag.putInt("k" + i, this.conditions[i]);
    }
}
