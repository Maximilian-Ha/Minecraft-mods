package com.hbm.entity.grenade;

import com.hbm.entity.projectile.ThrowableNT;
import com.hbm.items.NtmItems;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.util.Vec3NT;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: die Wurfseite von com.hbm.items.weapon.ItemGrenadeDynamite.
 *
 * Die Dynamitstange. Sie ist KEINE Universalgranate: im Original haengt sie an
 * ItemGenericGrenade, der alten, einfachen Familie -- ein Zuender, ein Knall, keine
 * Bauteile. Der Port hat diese Familie nicht, denn seine Granaten setzen sich aus Koerper,
 * Fuellung, Zuender und Aufsatz zusammen. Eine Dynamitstange als Universalgranate
 * auszugeben, hiesse ihr eine Zusammenstellung anzudichten, die es im Original nicht gibt.
 * Darum diese eigene, kurze Klasse.
 *
 * DREI SEKUNDEN ZUENDER, wie im Original (ItemGrenadeDynamite(3)) -- und zwar unabhaengig
 * vom Aufschlag: sie springt ab und rollt, bis die Zeit um ist.
 *
 * DIE SPRENGWERTE sind die des Originals, Zahl fuer Zahl: Radius fuenf,
 * EntityProcessorCrossSmooth(1, 15), PlayerProcessorStandard und als Effekt
 * ExplosionEffectWeapon(10, 2.5F, 1F).
 */
public class Dynamite extends ThrowableNT implements ItemSupplier {

    /** Sechzig Takte, also drei Sekunden. */
    private static final int ZUENDER = 60;

    public Dynamite(EntityType<? extends Dynamite> type, Level level) {
        super(type, level);
    }

    /** Wie die Universalgranate: aus der Hand, nicht aus der Nasenspitze. */
    public void werfen(LivingEntity werfer) {

        this.setOwner(werfer);

        Vec3NT versatz = new Vec3NT(0.25, -0.25, 0).rotateAroundYDeg(-werfer.getYRot() + 180);
        this.setPos(werfer.getX() + versatz.xCoord,
                werfer.getEyeY() + versatz.yCoord,
                werfer.getZ() + versatz.zCoord);

        Vec3 blick = werfer.getLookAngle().normalize();
        this.shoot(blick.x, blick.y, blick.z, 1.0F, 0F);
    }

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide) return;
        if(this.tickCount < ZUENDER) return;

        this.zuende();
    }

    /** Der Aufschlag zuendet sie NICHT -- sie springt ab und rollt weiter. */
    @Override
    protected void onImpact(HitResult treffer) { }

    /** Sie fliegt als das, was sie ist -- der Darsteller zeichnet schlicht den Gegenstand. */
    @Override
    public ItemStack getItem() {
        return new ItemStack(NtmItems.STICK_DYNAMITE.get());
    }

    /** Geschuetzt, damit der Fischerdynamit sie ersetzen kann -- er sprengt anders. */
    protected void zuende() {

        ExplosionVNT knall = new ExplosionVNT(this.level(), this.getX(), this.getY(), this.getZ(), 5F,
                this.getOwner() instanceof LivingEntity werfer ? werfer : null);
        knall.setEntityProcessor(new EntityProcessorCrossSmooth(1, 15));
        knall.setPlayerProcessor(new PlayerProcessorStandard());
        knall.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
        knall.explode();

        this.discard();
    }
}
