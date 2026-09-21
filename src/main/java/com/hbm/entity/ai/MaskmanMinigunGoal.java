package com.hbm.entity.ai;

import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.items.weapon.sedna.factory.XFactory762mm;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.ai.EntityAIMaskmanMinigun.
 *
 * Der Nahbereich: zwischen fuenf und zehn Bloecken Abstand feuert der Maskenmann seine
 * Minigun, alle drei Ticks eine Patrone 7,62 mm Vollmantel.
 *
 * DIE VERSETZUNG IST DIE DES ORIGINALS: eineinhalb Bloecke nach links und eineinhalb nach
 * unten, gemessen von den Augen -- dort haelt er die Waffe.
 */
public class MaskmanMinigunGoal extends Goal {

    private final PathfinderMob wirt;
    private final int pause;
    private int zaehler;

    public MaskmanMinigunGoal(PathfinderMob wirt, int pause) {
        this.wirt = wirt;
        this.pause = pause;
        this.zaehler = pause;
    }

    @Override
    public boolean canUse() {
        LivingEntity ziel = this.wirt.getTarget();
        if(ziel == null || !ziel.isAlive()) return false;
        double weite = this.wirt.position().distanceTo(ziel.position());
        return weite > 5 && weite < 10;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.wirt.getNavigation().isDone();
    }

    @Override
    public void tick() {

        LivingEntity ziel = this.wirt.getTarget();
        if(ziel != null) this.wirt.getLookControl().setLookAt(ziel, 15F, 15F);

        if(--this.zaehler > 0) return;
        this.zaehler = this.pause;

        if(this.wirt.level().isClientSide) return;

        BulletBaseMK4 kugel = new BulletBaseMK4(this.wirt, XFactory762mm.r762_fmj, 5F, 0.075F, -1.5, -1.5, 0);
        this.wirt.level().addFreshEntity(kugel);
        this.wirt.level().playSound(null, this.wirt.getX(), this.wirt.getY(), this.wirt.getZ(),
                NtmSoundEvents.GUN_MINIGUN_FIRE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
    }
}
