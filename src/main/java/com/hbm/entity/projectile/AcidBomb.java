package com.hbm.entity.projectile;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.mob.glyphid.Glyphid;
import com.hbm.registry.NtmDamageTypes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.entity.projectile.EntityAcidBomb.
 *
 * Der Saeureklecks, den der Bombardier und der Blaster ausspucken. Ein Wurfkoerper ohne
 * eigenes Aussehen -- das Original zeichnet ihn als Schleimball (RenderSnowball mit
 * Items.slime_ball), und genau das macht der Port mit ThrownItemRenderer auch.
 *
 * ER FLIEGT DURCH GLYPHIDEN HINDURCH: trifft er einen, geschieht nichts, und er fliegt
 * weiter. Nur ein Treffer auf etwas anderes setzt Schaden und beendet ihn. So steht es im
 * Original, und es ist nicht dasselbe wie die Regel in Glyphid.hurt -- die faengt den
 * Schaden ab, dieser Klecks aber wird gar nicht erst verbraucht.
 *
 * SCHWERKRAFT 0,04 statt der ueblichen 0,03 und LUFTWIDERSTAND 1,0 statt 0,99: er faellt
 * schneller und wird nicht gebremst. Der Bombardier rechnet seine Wurfbahn mit g = 0,04
 * aus -- die beiden Zahlen muessen zusammenpassen, sonst geht jeder Wurf daneben.
 */
public class AcidBomb extends ThrowableNT implements ItemSupplier {

    /** Wie viel er beim Treffer abzieht. Der Werfer setzt das, der Bombardier auf 5, der Blaster auf 15. */
    public float damage = 1.5F;

    public AcidBomb(EntityType<? extends AcidBomb> type, Level level) {
        super(type, level);
    }

    public AcidBomb(Level level, double x, double y, double z) {
        super(NtmEntityTypes.ACID_BOMB.get(), level);
        this.setPos(x, y, z);
    }

    /**
     * Die Richtungsgebung des Originals (EntityThrowable.setThrowableHeading), Zahl fuer
     * Zahl: Richtung normieren, Gauss mal 0,0075 mal Streuung aufschlagen, mit der
     * Geschwindigkeit strecken. Projectile.shoot koennte das fast -- aber es streut
     * dreieckig mit 0,0172275 statt gaussisch mit 0,0075, also mehr als doppelt so weit.
     * Der Blaster wirft zehn Bomben mit aufsteigender Streuung; mit der falschen Zahl
     * waere das ein anderer Faecher.
     */
    public void richten(double x, double y, double z, float geschwindigkeit, float streuung) {

        Vec3 richtung = new Vec3(x, y, z).normalize()
                .add(this.random.nextGaussian() * 0.0075D * streuung,
                        this.random.nextGaussian() * 0.0075D * streuung,
                        this.random.nextGaussian() * 0.0075D * streuung)
                .scale(geschwindigkeit);

        this.setDeltaMovement(richtung);

        double flach = richtung.horizontalDistance();
        this.setYRot((float) (Math.atan2(richtung.x, richtung.z) * 180.0D / Math.PI));
        this.setXRot((float) (Math.atan2(richtung.y, flach) * 180.0D / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    protected void onImpact(HitResult treffer) {

        if(this.level().isClientSide) return;

        if(treffer instanceof EntityHitResult wesen) {

            Entity getroffen = wesen.getEntity();

            if(!(getroffen instanceof Glyphid)) {
                getroffen.hurt(this.damageSources().source(NtmDamageTypes.ACID, this, this.getOwner()), this.damage);
                this.discard();
            }

            return;
        }

        this.discard();
    }

    @Override
    protected double getGravityVelocity() {
        return 0.04D;
    }

    @Override
    protected float getAirDrag() {
        return 1.0F;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.SLIME_BALL);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("damage");
    }
}
