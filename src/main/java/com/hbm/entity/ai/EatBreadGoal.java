package com.hbm.entity.ai;

import com.hbm.entity.mob.IFlyingCreature;
import com.hbm.entity.mob.Pigeon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.ai.EntityAIEatBread.
 *
 * Die Taube sucht sich BROT, das am Boden liegt -- zehn Bloecke weit --, laeuft hin und
 * frisst es. Danach ist sie fett, und eine fette Taube sucht kein Brot mehr.
 *
 * WIE SIE FRISST: naeher als einen Block wuerfelt sie eins zu drei. Trifft der Wurf, ist der
 * Gegenstand weg -- der Rest des Stapels bleibt als neuer Gegenstand liegen. Fett wird sie
 * bei jedem Versuch, auch wenn der Wurf danebengeht; so steht es im Original.
 *
 * DER TON IST DER DES ESSENS, und seine Lautstaerke ist eine Merkwuerdigkeit des Originals:
 * 0,5 + 0,5 * rand.nextInt(2) ergibt entweder ein halb oder ganz -- eine ganzzahlige Wahl,
 * wo man eine gleitende erwarten wuerde. Uebernommen, wie sie dasteht.
 */
public class EatBreadGoal extends Goal {

    /** Wie weit sie nach Brot sucht. */
    private static final double SUCHWEITE = 10D;

    private final Pigeon taube;
    private final double tempo;
    private ItemEntity brot;

    public EatBreadGoal(Pigeon taube, double tempo) {
        this.taube = taube;
        this.tempo = tempo;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {

        if(this.taube.isFat()) return false;
        if(this.taube.getFlyingState() != IFlyingCreature.STATE_WALKING) return false;

        AABB kasten = this.taube.getBoundingBox().inflate(SUCHWEITE, SUCHWEITE, SUCHWEITE);
        List<ItemEntity> liegendes = this.taube.level().getEntitiesOfClass(ItemEntity.class, kasten);

        for(ItemEntity gegenstand : liegendes) {
            if(gegenstand.getItem().is(Items.BREAD)) {
                this.brot = gegenstand;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.brot != null && this.brot.isAlive() && this.canUse();
    }

    @Override
    public void tick() {

        if(this.brot == null) return;

        this.taube.getLookControl().setLookAt(this.brot, 30.0F, this.taube.getMaxHeadXRot());

        if(this.taube.distanceTo(this.brot) > 1) {
            this.taube.getNavigation().moveTo(this.brot, this.tempo);
            return;
        }

        if(this.taube.getRandom().nextInt(3) == 0) {

            ItemStack stapel = this.brot.getItem();

            if(stapel.getCount() > 1) {
                ItemStack rest = stapel.copy();
                rest.shrink(1);

                ItemEntity liegenbleibt = new ItemEntity(this.taube.level(),
                        this.brot.getX(), this.brot.getY(), this.brot.getZ(), rest);
                this.taube.level().addFreshEntity(liegenbleibt);
            }

            this.brot.discard();
        }

        this.taube.setFat(true);
        this.taube.level().playSound(null, this.taube.getX(), this.taube.getY(), this.taube.getZ(),
                SoundEvents.GENERIC_EAT.value(), SoundSource.NEUTRAL,
                0.5F + 0.5F * this.taube.getRandom().nextInt(2),
                (this.taube.getRandom().nextFloat() - this.taube.getRandom().nextFloat()) * 0.2F + 1.0F);
    }
}
