package com.hbm.entity.mob;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityGhost.
 *
 * Ein Gespenst. Es tut nichts: es steht herum, schaut sich um und geht ziellos umher. Man kann
 * ihm nichts anhaben, und es tut auch niemandem etwas.
 *
 * MAN SIEHT ES NUR VON WEITEM. Sobald irgendein Spieler naeher als fuenfzig Bloecke kommt,
 * verschwindet es -- daher auch die zehnfache Sichtweite im Original: man soll es aus der
 * Ferne stehen sehen und dann feststellen, dass da nichts mehr ist, wenn man hingeht.
 *
 * ES ERSCHEINT NUR BEI DIGAMMA. Wer Digamma im Blut hat, hat alle zwanzig Takte eine Chance
 * von eins zu fuenf, dass fuenfundsiebzig Bloecke entfernt eines auftaucht.
 */
public class Ghost extends PathfinderMob {

    public Ghost(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide) return;

        /* Wer hinsieht, sieht nichts: kommt ein Spieler auf fuenfzig Bloecke heran, ist es fort. */
        if(!this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(50D)).isEmpty()) {
            this.discard();
        }
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(this.getMaxHealth());
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }
}
