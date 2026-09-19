package com.hbm.blockentity.network;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.RTTYSystem.RTTYChannel;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.util.NoteBuilder;
import com.hbm.util.NoteBuilder.Instrument;
import com.hbm.util.NoteBuilder.Note;
import com.hbm.util.NoteBuilder.Octave;
import com.hbm.util.Tuple.Triplet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityRadioRec.
 *
 * Ein Empfaenger fuer den Fernschreibfunk: steht er auf einem Kanal und ist eingeschaltet,
 * setzt er das Gesendete in Notenblocktoene um. Die Umsetzung nimmt ihm NoteBuilder ab.
 *
 * Gesendet wird in der Vorphase des Server-Ticks, gehoert wird eine Runde spaeter --
 * daher der Vergleich des Zeitstempels mit der vorigen Spielzeit, wie im Original.
 */
public class RadioRecBlockEntity extends LoadedBaseBlockEntity implements ITickable, IControlReceiver {

    public String channel = "";
    public boolean isOn = false;

    public RadioRecBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RADIO_REC.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.isOn && !this.channel.isEmpty()) {

            RTTYChannel kanal = RTTYSystem.listen(this.level, this.channel);

            if(kanal != null && kanal.timeStamp == this.level.getGameTime() - 1) {

                List<Triplet<Instrument, Note, Octave>> noten = NoteBuilder.translate(String.valueOf(kanal.signal));

                for(Triplet<Instrument, Note, Octave> note : noten) {

                    int hoehe = note.getY().ordinal() + note.getZ().ordinal() * 12;
                    // Dieselbe Formel wie der Notenblock: eine Oktave sind zwoelf Halbtoene.
                    float tonhoehe = (float) Math.pow(2.0D, (hoehe - 12) / 12.0D);

                    this.level.playSound(null, this.worldPosition, klang(note.getX()), SoundSource.RECORDS, 3.0F, tonhoehe);
                }
            }
        }

        this.networkPackNT(15);
    }

    private static SoundEvent klang(Instrument instrument) {
        return switch(instrument) {
            case BASSDRUM -> SoundEvents.NOTE_BLOCK_BASEDRUM.value();
            case SNARE -> SoundEvents.NOTE_BLOCK_SNARE.value();
            case CLICKS -> SoundEvents.NOTE_BLOCK_HAT.value();
            case BASSGUITAR -> SoundEvents.NOTE_BLOCK_BASS.value();
            default -> SoundEvents.NOTE_BLOCK_HARP.value();
        };
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("channel")) this.channel = tag.getString("channel");
        if(tag.contains("isOn")) this.isOn = tag.getBoolean("isOn");
        this.setChanged();
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 15 * 15;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeUtf(this.channel);
        buf.writeBoolean(this.isOn);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.channel = buf.readUtf();
        this.isOn = buf.readBoolean();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.channel = tag.getString("channel");
        this.isOn = tag.getBoolean("isOn");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("channel", this.channel);
        tag.putBoolean("isOn", this.isOn);
    }
}
