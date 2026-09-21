package com.hbm.explosion;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockMutatorFire;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.items.special.PolaroidItem;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.explosion.ExplosionNukeSmall.
 *
 * Die kleine Atomexplosion -- was ein Fatman, eine Spaltbombe oder ein abstuerzendes UFO
 * hinterlaesst. Sie ist keine Entitaet wie die grosse, sondern eine Folge von fuenf Schritten,
 * die alle schon im Port stehen: Klang, Pilz, Splitter, Druckwelle mit Feuer, Schaden und
 * Strahlung.
 *
 * VIER GROESSEN kennt das Original (PARAMS_TOTS, _LOW, _MEDIUM, _HIGH). Hier stehen die, die
 * der Port braucht; kommt eine weitere dazu, gehoert sie hierher und nicht noch einmal
 * ausgeschrieben an ihre Aufrufstelle.
 *
 * ENTDOPPELT IN RUNDE 282: die Spaltbombe hatte PARAMS_MEDIUM Zeile fuer Zeile in ihrer
 * explodeEntity stehen. Sie ruft jetzt dieselbe Stelle wie das UFO.
 */
public class ExplosionNukeSmall {

    /** PARAMS_MEDIUM des Originals: Druckwelle 20, Todeskreis 55, Strahlungsstufe 3. */
    public static void mittel(Level level, double x, double y, double z) {
        explode(level, x, y, z, 20F, 55F, 3F);
    }

    /**
     * Eine kleine Atomexplosion nach Mass.
     *
     * @param druckwelle wie weit die Bloecke fliegen
     * @param todeskreis wie weit hinaus sie toetet
     * @param strahlung  die Strahlungsstufe; das Original teilt sie durch drei
     */
    public static void explode(Level level, double x, double y, double z, float druckwelle, float todeskreis, float strahlung) {

        level.playSound(null, x, y, z, NtmSoundEvents.MUKE_EXPLOSION.get(), SoundSource.BLOCKS, 25.0F, 1F);

        if(level instanceof ServerLevel serverLevel) {

            CompoundTag tag = new CompoundTag();
            tag.putString("type", "muke");
            tag.putBoolean("balefire", PolaroidItem.polaroidID == 11 || level.random.nextInt(100) == 0);
            PacketDistributor.sendToPlayersNear(serverLevel, null, x, y, z, 250, new AuxParticle(tag, x, y, z));

            ExplosionLarge.spawnShrapnels(serverLevel, x, y, z, 25);
        }

        ExplosionVNT vnt = new ExplosionVNT(level, x, y, z, druckwelle)
                .setBlockAllocator(new BlockAllocatorStandard(64))
                .setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorFire()));
        vnt.explode();

        ExplosionNukeGeneric.dealDamage(level, x, y, z, todeskreis);
        ExplosionNukeGeneric.incrementRad(level, x, y, z, strahlung / 3F);
    }
}
