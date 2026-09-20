package com.hbm.entity.mob;

import com.hbm.entity.effect.Mist;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.inventory.fluid.Fluids;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityCreeperPhosgene.
 *
 * Der Phosgencreeper. Sein Knall ist klein und richtet keinen Blockschaden an -- was
 * toetet, ist die Wolke danach: zehn Bloecke breit, fuenf hoch, hundertfuenfzig Ticks lang.
 *
 * ER GEHT DOPPELT SO SCHNELL HOCH wie ein gewoehnlicher Creeper: zwanzig Ticks statt
 * dreissig. Im Original steht dahinter ein "ehehehehehe" als Kommentar.
 *
 * ER STECKT VIER SCHADEN WEG. Das Original prueft dafuer auf isDamageAbsolute und
 * isUnblockable -- die beiden Schalter, die in 1.7.10 Panzerung und Unverwundbarkeit
 * uebergehen. In 1.21 stehen dafuer die Marken BYPASSES_ARMOR und
 * BYPASSES_INVULNERABILITY. Bleibt nach dem Abzug nichts uebrig, spuert er gar nichts.
 */
public class CreeperPhosgene extends Creeper {

    /**
     * Seine Lunte: zwanzig Ticks statt der achtundzwanzig, die der Port sonst nimmt. Das
     * Original setzt dafuer fuseTime auf zwanzig; in 1.21 ist maxSwell privat, also liest
     * der Port den Zaehler ab und zuendet frueher (siehe CreeperFuse).
     */
    public static final int ZUENDZEIT = 20;

    public CreeperPhosgene(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {

        if(!source.is(DamageTypeTags.BYPASSES_ARMOR)
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount -= 4F;
        }

        if(amount < 0) return false;

        return super.hurt(source, amount);
    }

    /** Wie bei allen Creepern dieser Mod haengt der Knall am Zaehler -- siehe CreeperFuse. */
    @Override
    public void tick() {
        super.tick();

        if(CreeperFuse.abgebrannt(this, ZUENDZEIT)) {
            this.dead = true;
            this.verwehen();
            this.triggerOnDeathMobEffects(RemovalReason.KILLED);
            this.discard();
        }
    }

    private void verwehen() {

        ExplosionVNT.newExplosion(this.level(), this,
                this.getX(), this.getY() + this.getBbHeight() / 2, this.getZ(), 2F, false, false);

        Mist wolke = new Mist(this.level());
        wolke.setFluidType(Fluids.PHOSGENE);
        wolke.setPos(this.getX(), this.getY(), this.getZ());
        wolke.setArea(10F, 5F);
        wolke.setDuration(150);
        this.level().addFreshEntity(wolke);
    }
}
