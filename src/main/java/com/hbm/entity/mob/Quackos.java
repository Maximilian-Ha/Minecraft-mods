package com.hbm.entity.mob;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.items.NtmItems;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityQuackos.
 *
 * Eine Ente, die zu lange in der Strahlung stand. Ab 200 Rad wird aus einer gewoehnlichen Ente
 * DIESE hier: fuenfundzwanzigmal so gross, unverwundbar, nicht zu toeten und selbst gegen
 * Strahlung immun. Man kann sie reiten.
 *
 * SIE LAESST SICH NICHT ABSCHUETTELN. Ihre drei Methoden setDead, setHealth und onDeath sind
 * im Original mit "prank'd" kommentiert: der Tod wird auf dem Server verweigert, die
 * Gesundheit springt bei jedem Setzen auf das Maximum zurueck, und fiele sie doch einmal unter
 * die Welt, setzt sie sich selbst wieder auf Hoehe 256.
 *
 * DER EINZIGE AUSWEG SIND ERBSEN. Der Erbsengegenstand laesst jede Quackos im Umkreis von
 * fuenfzig Bloecken verschwinden -- in einer Wolke aus hundertfuenfzig Teilchen, und sie
 * hinterlaesst drei Enten-Rufgegenstaende.
 *
 * ABWEICHUNG: das Original schreibt ueber jede einzelne Methode dieser Klasse den Kommentar
 * "BOW". Das ist keine Doku, das ist eine Verbeugung; sie steht hier einmal statt sechzehnmal.
 */
public class Quackos extends Duck {

    public Quackos(EntityType<? extends Chicken> type, Level level) {
        super(type, level);
    }

    /* KEINE eigenen Attribute: das Original gibt der Quackos keine. Sie erbt die der Ente --
     * vier Lebenspunkte, die nie sinken, weil setHealth sie jedes Mal zurueckdreht. */
    @Override protected SoundEvent getAmbientSound() { return NtmSoundEvents.MEGAQUACC.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return NtmSoundEvents.MEGAQUACC.get(); }
    @Override protected SoundEvent getDeathSound() { return NtmSoundEvents.MEGAQUACC.get(); }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    /** Auf dem Server verweigert sie das Sterben; der Client darf sie entfernen. */
    @Override
    public void remove(RemovalReason reason) {
        if(this.level().isClientSide) super.remove(reason);
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(this.getMaxHealth());
    }

    @Override
    public void die(DamageSource source) { }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {

        InteractionResult result = super.mobInteract(player, hand);
        if(result.consumesAction()) return result;

        if(!this.level().isClientSide && this.getFirstPassenger() == null) {
            player.startRiding(this);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        /* Faellt sie aus der Welt, kommt sie oben wieder herein -- irgendwo in der Naehe. */
        if(!this.level().isClientSide && this.getY() < -30) {
            this.setPos(this.getX() + this.random.nextGaussian() * 30, 256, this.getZ() + this.random.nextGaussian() * 30);
        }
    }

    /** Was die Erbsen ausloesen: hundertfuenfzig Teilchen, drei Rufgegenstaende, weg. */
    public void despawn() {

        if(this.level() instanceof ServerLevel serverLevel) {

            for(int i = 0; i < 150; i++) {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        this.getX() + this.random.nextDouble() * 20 - 10,
                        this.getY() + this.random.nextDouble() * 25,
                        this.getZ() + this.random.nextDouble() * 20 - 10,
                        1, 0D, 0D, 0D, 0D);
            }

            this.spawnAtLocation(new ItemStack(NtmItems.SPAWN_DUCK.get(), 3));
        }

        super.remove(RemovalReason.KILLED);
    }

    @Nullable
    @Override
    public Quackos getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new Quackos(NtmEntityTypes.QUACKOS.get(), level);
    }

    /**
     * Der Reiter sitzt vorn auf dem Hals, nicht in der Mitte -- die Masse sind die des
     * Originals, nur auf die Groesse dieser Ente gerechnet.
     */
    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {

        float sin = (float) Math.sin(this.yBodyRot * Math.PI / 180D);
        float cos = (float) Math.cos(this.yBodyRot * Math.PI / 180D);

        callback.accept(passenger, this.getX() + 0.1F * sin, this.getY() + this.getBbHeight() - 0.125F, this.getZ() - 0.1F * cos);

        if(passenger instanceof net.minecraft.world.entity.LivingEntity living) living.yBodyRot = this.yBodyRot;
    }
}
