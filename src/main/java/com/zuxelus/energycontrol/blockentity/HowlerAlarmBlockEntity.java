package com.zuxelus.energycontrol.blockentity;

import com.zuxelus.energycontrol.ECConfig;
import com.zuxelus.energycontrol.init.ECBlockEntityTypes;
import com.zuxelus.energycontrol.init.ECSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.tileentities.TileEntityHowlerAlarm.
 *
 * Der Heuler spielt einen Alarmton, solange er ein Redstone-Signal bekommt.
 *
 * ABWEICHUNG vom Original: dort waehlte man Ton und Hoerweite in einer eigenen
 * Oberflaeche mit Listenfeld und Schieberegler. Hier geht beides am Block: Rechtsklick
 * mit leerer Hand schaltet den Ton weiter, Rechtsklick im Schleichen die Hoerweite. Was
 * eingestellt ist, sagt eine Meldung ueber der Schnellleiste.
 */
public class HowlerAlarmBlockEntity extends BlockEntity {

    /** Der Ton wird alle drei Sekunden neu angestossen, solange der Alarm laeuft. */
    private static final int REPEAT_TICKS = 60;

    private int soundIndex;
    private int range = 16;
    private int timer;

    public HowlerAlarmBlockEntity(BlockPos pos, BlockState state) {
        super(ECBlockEntityTypes.HOWLER_ALARM.get(), pos, state);
    }

    public void tick(boolean powered) {
        if(level == null || level.isClientSide) return;

        if(!powered) {
            timer = 0;
            return;
        }

        if(timer-- > 0) return;
        timer = REPEAT_TICKS;

        SoundEvent sound = ECSounds.alarm(soundIndex);
        // Die Hoerweite von Minecraft haengt an der Lautstaerke: ein Wert ueber 1 traegt
        // sechzehn Bloecke je Einheit weiter.
        level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, range / 16.0F, 1.0F);
    }

    public int getSoundIndex() {
        return soundIndex;
    }

    public int getRange() {
        return range;
    }

    public void cycleSound() {
        soundIndex = (soundIndex + 1) % ECSounds.ALARM_COUNT;
        timer = 0;
        setChanged();
    }

    public void cycleRange() {
        range += 16;
        if(range > ECConfig.maxAlarmRange()) range = 16;
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        soundIndex = tag.getInt("sound");
        range = tag.contains("range") ? tag.getInt("range") : 16;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("sound", soundIndex);
        tag.putInt("range", range);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
}
