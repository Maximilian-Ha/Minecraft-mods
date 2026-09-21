package com.hbm.entity.mob;

import com.hbm.entity.ai.MaskmanApproachGoal;
import com.hbm.entity.ai.MaskmanLasergunGoal;
import com.hbm.entity.ai.MaskmanMinigunGoal;
import com.hbm.items.NtmItems;
import com.hbm.registry.NtmCriteria;
import com.hbm.util.ArmorUtil;

import api.hbm.entity.IRadiationImmune;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityMaskMan.
 *
 * Der Maskenmann. Tausend Lebenspunkte, fuenf Bloecke hoch, unempfindlich gegen Feuer und
 * Strahlung, und nicht zurueckzustossen. Er schiesst statt zu schlagen: im Nahbereich mit der
 * Minigun, ab zehn Bloecken mit dem Laser, und dazwischen haelt er von sich aus Abstand.
 *
 * WIE ER SCHADEN NIMMT, ist der Kern seiner Zaehigkeit:
 *   Feuer und Magie      richten gar nichts aus
 *   Geschosse und Sprengungen zaehlen halb
 *   alles ueber fuenfzig wird ueber fuenfzig hinaus halbiert
 * Ein Schlag von hundert kommt also als fuenfundsiebzig an, einer von tausend als
 * fuenfhundertfuenfundzwanzig.
 *
 * EIN EI TOETET IHN. Trifft ihn ein geworfenes Ei, stirbt er mit einem Zehntel
 * Wahrscheinlichkeit auf der Stelle -- und laesst dabei keine Erfahrung fallen. Das ist die
 * Hintertuer des Originals und steht hier, wie sie dort steht.
 *
 * BEI DER HAELFTE SPRENGT ER. Faellt seine Lebensenergie unter die Haelfte, geht ueber ihm
 * eine Sprengung der Staerke 2,5 los -- einmal, nicht jedes Mal.
 *
 * WAS ER FALLEN LAESST: seine Gasmaske samt eingesetztem Kombifilter, seine Muenze, eine
 * Flasche Wolke und einen Schaedel.
 */
public class MaskMan extends Monster implements IRadiationImmune {

    /** Ob die Sprengung bei halber Lebensenergie schon war. */
    private boolean gesprengt = false;

    public MaskMan(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 100;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MaskmanApproachGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new MaskmanMinigunGoal(this, 3));
        this.goalSelector.addGoal(3, new MaskmanLasergunGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource quelle, float schaden) {

        if(quelle.getDirectEntity() instanceof ThrownEgg && this.random.nextInt(10) == 0) {
            this.xpReward = 0;
            this.setHealth(0);
            return true;
        }

        if(quelle.is(DamageTypeTags.IS_FIRE)) schaden = 0;
        /* isMagicDamage() des Originals. In 1.21 gibt es dafuer keinen Tag, sondern nur die
         * eine Schadensart selbst -- und genau die ist im Original auch gemeint. */
        if(quelle.is(DamageTypes.MAGIC)) schaden = 0;
        if(quelle.is(DamageTypeTags.IS_PROJECTILE)) schaden *= 0.5F;
        if(quelle.is(DamageTypeTags.IS_EXPLOSION)) schaden *= 0.5F;

        if(schaden > 50) schaden = 50 + (schaden - 50) * 0.5F;

        return super.hurt(quelle, schaden);
    }

    @Override
    public void tick() {
        super.tick();

        if(!this.gesprengt && this.isAlive() && this.getHealth() < this.getMaxHealth() / 2) {
            this.gesprengt = true;

            if(!this.level().isClientSide) {
                this.level().explode(this, this.getX(), this.getY() + 4, this.getZ(), 2.5F, Level.ExplosionInteraction.MOB);
            }
        }
    }

    @Override
    public void die(DamageSource quelle) {
        super.die(quelle);

        /* Den Erfolg bekommt jeder Spieler im Umkreis von hundert Bloecken, nicht nur der,
         * der ihn erlegt hat. */
        List<Player> nahe = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(100));
        for(Player spieler : nahe) {
            if(spieler instanceof ServerPlayer server) NtmCriteria.marke(server, "boss_maskman");
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource quelle, boolean kuerzlichGetroffen) {
        super.dropCustomDeathLoot(level, quelle, kuerzlichGetroffen);

        ItemStack maske = new ItemStack(NtmItems.GAS_MASK_M65.get());
        ArmorUtil.installGasMaskFilter(level, maske, new ItemStack(NtmItems.GAS_MASK_FILTER_COMBO.get()));

        this.spawnAtLocation(maske);
        this.spawnAtLocation(NtmItems.COIN_MASKMAN.get());
        this.spawnAtLocation(NtmItems.BOTTLED_CLOUD.get());
        this.spawnAtLocation(Items.SKELETON_SKULL);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double weite) {
        return false;
    }
}
