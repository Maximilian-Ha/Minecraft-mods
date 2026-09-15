package com.hbm.blockentity.machine.albion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.albion.IParticleUser.
 *
 * Alles, was im Strahlweg stehen darf. Das Teilchen wandert von Bauteil zu Bauteil und fragt an
 * jedem dreierlei: darf ich hier hinein, was passiert mit mir, und wo komme ich wieder heraus.
 *
 * WER NICHT ANTWORTET, BRINGT DEN STRAHL ZUM ABSTURZ. Steht im Weg etwas, das diese Schnittstelle
 * nicht hat, entgleist das Teilchen -- und das ist Absicht: ein Beschleuniger ist ein Ring, kein
 * Vorschlag.
 */
public interface IParticleUser {

    /** Darf das Teilchen aus dieser Richtung an dieser Stelle hinein? */
    boolean canParticleEnter(Particle particle, Direction dir, BlockPos pos);

    /** Was das Bauteil mit dem Teilchen macht -- beschleunigen, buendeln, ablenken, auffangen. */
    void onEnter(Particle particle, Direction dir);

    /** Wohin es danach weiterfliegt. Null heisst: hier endet der Weg. */
    BlockPos getExitPos(Particle particle);
}
