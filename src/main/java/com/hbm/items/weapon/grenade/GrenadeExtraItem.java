package com.hbm.items.weapon.grenade;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.grenade.GrenadeUniversal;
import com.hbm.items.EnumMultiItem;
import com.hbm.items.weapon.grenade.GrenadeFuzeItem.GrenadeFuze;
import com.hbm.util.Vec3NT;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.grenade.ItemGrenadeExtra.
 *
 * Der Aufsatz -- das einzige Bauteil, das eine Granate auch ohne haben kann. Vier Stueck:
 * Kleber, Naeherungszuender, Splittermantel und der Dreifachteiler.
 *
 * Jeder haengt an einer anderen Stelle: der Kleber am Aufschlag, der Naeherungszuender am
 * Tick, Splittermantel und Dreifachteiler an der Explosion.
 */
public class GrenadeExtraItem extends EnumMultiItem {

    public GrenadeExtraItem(Properties properties) {
        super(properties, GrenadeExtra.class, true, true);
    }

    public enum GrenadeExtra {
        /** Bleibt kleben, wo sie auftrifft. */
        GLUE(null, EXTRA_GLUE, null),
        /** Ein zweiter Zuender: geht hoch, sobald ein Wesen auf zehn Bloecke herankommt. */
        PROXY_FUZE(EXTRA_PROXY, null, null),
        /** Fuenfundzwanzig Splitter obendrauf. */
        FRAG_SLEEVE(null, null, EXTRA_FRAG),
        /** Teilt sich beim Hochgehen in drei. */
        TRIPLEX(null, null, EXTRA_TRIPLEX);

        public final Consumer<GrenadeUniversal> updateTick;
        public final BiConsumer<GrenadeUniversal, HitResult> onImpact;
        public final Consumer<GrenadeUniversal> onExplode;

        GrenadeExtra(Consumer<GrenadeUniversal> updateTick,
                     BiConsumer<GrenadeUniversal, HitResult> onImpact,
                     Consumer<GrenadeUniversal> onExplode) {
            this.updateTick = updateTick;
            this.onImpact = onImpact;
            this.onExplode = onExplode;
        }
    }

    public static final BiConsumer<GrenadeUniversal, HitResult> EXTRA_GLUE = (granate, treffer) -> {
        if(!(treffer instanceof BlockHitResult bhr)) return;
        Vec3 stelle = treffer.getLocation();
        granate.setPos(stelle.x, stelle.y, stelle.z);
        granate.getStuck(bhr.getBlockPos(), bhr.getDirection());
    };

    /**
     * Der Naeherungszuender. Er sieht alle drei Ticks nach, ob ein Wesen auf zehn Bloecke
     * herangekommen ist -- der Werfer selbst zaehlt nicht.
     */
    public static final Consumer<GrenadeUniversal> EXTRA_PROXY = (granate) -> {

        if(granate.getTimer() < 10 || granate.getTimer() % 3 != 0) return;

        AABB bereich = new AABB(granate.position(), granate.position()).inflate(10);

        for(LivingEntity wesen : granate.level.getEntitiesOfClass(LivingEntity.class, bereich)) {
            if(wesen == granate.getOwner()) continue;
            if(wesen.distanceTo(granate) <= 10) {
                granate.explode();
                return;
            }
        }
    };

    public static final Consumer<GrenadeUniversal> EXTRA_FRAG = (granate) -> {
        GrenadeFillingItem.standardFragmentation(granate, 25);
    };

    /**
     * Der Dreifachteiler. Er wirft drei neue Granaten mit demselben Koerper und derselben
     * Fuellung in einem Dreieck nach oben weg, jede mit Dreisekundenzuender.
     */
    public static final Consumer<GrenadeUniversal> EXTRA_TRIPLEX = (granate) -> {

        ItemStack teil = GrenadeUniversalItem.make(granate.getShell(), granate.getFilling(), GrenadeFuze.S3);
        Vec3NT richtung = new Vec3NT(0.25, 0, 0).rotateAroundYDeg(granate.level.random.nextDouble() * 360);

        for(int i = 0; i < 3; i++) {

            GrenadeUniversal drilling = new GrenadeUniversal(NtmEntityTypes.GRENADE_UNIVERSAL.get(), granate.level);
            drilling.setGrenadeItem(teil);
            drilling.setTrail(GrenadeUniversal.TRAIL_TRIPLET);
            drilling.setPos(granate.getX(), granate.getY(), granate.getZ());
            drilling.setOwner(granate.getOwner());
            drilling.setDeltaMovement(richtung.xCoord, 0.75D, richtung.zCoord);
            granate.level.addFreshEntity(drilling);

            richtung.rotateAroundYDeg(120);
        }
    };
}
