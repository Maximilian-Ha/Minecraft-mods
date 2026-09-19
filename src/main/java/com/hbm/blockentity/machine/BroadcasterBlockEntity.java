package com.hbm.blockentity.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityBroadcaster.
 *
 * Ein Sender, der nichts Gutes sendet. In fuenfundzwanzig Bloecken Umkreis wird jedem
 * Lebewesen uebel, in fuenfzehn Bloecken tut es zusaetzlich weh -- und zwar umso mehr, je
 * naeher man steht: aus voller Entfernung nichts, direkt davor zehn Schaden je Tick.
 *
 * Dazu laeuft eine Klangschleife, die sich der Sender einmal aussucht und behaelt: drei
 * Aufnahmen stehen zur Wahl, und welche es wird, haengt am Ort. Zwei Sender nebeneinander
 * klingen darum verschieden, derselbe Sender aber immer gleich.
 *
 * ABWEICHUNG: das Original laesst die Schleife ueber Lebewesen auf BEIDEN Seiten laufen.
 * Auf dem Client bewirkt weder der Schaden noch der Effekt etwas -- beides gehoert dem
 * Server. Hier steht es deshalb serverseitig, der Klang clientseitig. Dasselbe Ergebnis,
 * nur ohne die halbe Arbeit doppelt.
 */
public class BroadcasterBlockEntity extends LoadedBaseBlockEntity implements ITickable {

    /** So weit wirkt die Uebelkeit. */
    public static final double REICHWEITE = 25D;

    /** So weit tut es weh. */
    public static final double SCHADENSREICHWEITE = 15D;

    /** Schaden direkt davor; er faellt bis zur Schadensreichweite linear auf null. */
    private static final float SCHADEN = 10F;

    private AudioWrapper audio;

    public BroadcasterBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.BROADCASTER.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {
            this.sende();
            return;
        }

        if(this.audio == null) {
            this.audio = this.createAudioLoop();
            this.audio.startSound();
        } else if(!this.audio.isPlaying()) {
            this.audio = this.rebootAudio(this.audio);
        }

        this.audio.keepAlive();
    }

    /**
     * Eine der drei Aufnahmen, fest am Ort haengend -- wie im Original. Zwei Sender
     * nebeneinander klingen darum verschieden, derselbe Sender aber immer gleich.
     */
    @Override
    public AudioWrapper createAudioLoop() {
        Random zufall = new Random(this.worldPosition.getX() + this.worldPosition.getY() + this.worldPosition.getZ());
        SoundEvent klang = switch(zufall.nextInt(3)) {
            case 0 -> NtmSoundEvents.BLOCK_BROADCAST_1.get();
            case 1 -> NtmSoundEvents.BLOCK_BROADCAST_2.get();
            default -> NtmSoundEvents.BLOCK_BROADCAST_3.get();
        };
        return AudioWrapper.getLoopedSound(klang, SoundSource.BLOCKS, this, 1.0F, 25F, 1.0F, 20);
    }

    private void sende() {

        Vec3 mitte = this.worldPosition.getCenter();
        AABB kasten = new AABB(mitte.x - REICHWEITE, mitte.y - REICHWEITE, mitte.z - REICHWEITE,
                mitte.x + REICHWEITE, mitte.y + REICHWEITE, mitte.z + REICHWEITE);

        List<LivingEntity> ziele = this.level.getEntitiesOfClass(LivingEntity.class, kasten);

        for(LivingEntity e : ziele) {

            double d = e.position().distanceTo(mitte);

            if(d <= REICHWEITE) {
                MobEffectInstance vorhanden = e.getEffect(MobEffects.CONFUSION);
                if(vorhanden == null || vorhanden.getDuration() < 100) {
                    e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
                }
            }

            if(d <= SCHADENSREICHWEITE) {
                float staerke = (float) ((SCHADENSREICHWEITE - d) / SCHADENSREICHWEITE) * SCHADEN;
                e.hurt(this.level.damageSources().source(NtmDamageTypes.BROADCAST), staerke);
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.stelleAb();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.stelleAb();
    }

    private void stelleAb() {
        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    /* Ohne @Override: getRenderBoundingBox kommt aus der NeoForge-Erweiterung von
     * BlockEntity und gilt dem Uebersetzer nicht als ueberschrieben. Das Original gibt
     * zwei Bloecke Hoehe an -- die Antenne ragt ueber den eigenen Block hinaus. */
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).expandTowards(0, 1, 0);
    }
}
