package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IExplosionSFX;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.explosion.vanillant.standard.ExplosionEffectTiny.
 *
 * Die kleinste Sprengwirkung des Spiels: ein einzelner Explosionsball und ein kurzer Knall. Sie
 * gehoert den Sprenggeschossen der Handwaffen, die nur einen halben Block weit reissen.
 *
 * ABWEICHUNG: das Original schickt den Partikel als eigenen Paketentwurf an alle in hundert Block
 * Umkreis. Hier tut es sendParticles, das denselben Umkreis von sich aus bedient.
 */
public class ExplosionEffectTiny implements IExplosionSFX {

    @Override
    public void doEffect(ExplosionVNT explosion, Level level, double x, double y, double z, float size) {

        if(!(level instanceof ServerLevel serverLevel)) return;

        level.playSound(null, x, y, z, NtmSoundEvents.EXPLOSION_TINY.get(), SoundSource.BLOCKS, 15.0F, 1.0F);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0D, 0D, 0D, 0D);
    }
}
