package com.hbm.items.weapon.grenade;

import com.hbm.entity.grenade.GrenadeUniversal;
import com.hbm.items.EnumMultiItem;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.grenade.ItemGrenadeFuze.
 *
 * Der Zuender. Er entscheidet allein, wann die Granate hochgeht -- die Fuellung sagt nur, was
 * dann passiert. Drei Zeitzuender, ein Aufschlagzuender und ein Hoehenzuender.
 *
 * Die Farbe ist der Ring am Koerper: je spaeter der Zeitzuender, desto heller, der
 * Aufschlagzuender orange, der Hoehenzuender gruen.
 */
public class GrenadeFuzeItem extends EnumMultiItem {

    public GrenadeFuzeItem(Properties properties) {
        super(properties, GrenadeFuze.class, true, true);
    }

    public enum GrenadeFuze {
        S3(FUZE_3S, 0x000000),
        S7(FUZE_7S, 0x404040),
        S15(FUZE_15S, 0x808080),
        /** Geht beim ersten Anstossen hoch -- aber erst nach einer halben Sekunde Sicherheit. */
        IMPACT(FUZE_IMPACT, 0xE36C17),
        /** Geht zehn Bloecke ueber dem Boden hoch, fruehestens nach anderthalb Sekunden. */
        AIRBURST(FUZE_AIRBURST, 0x56A137);

        public final Consumer<GrenadeUniversal> updateTick;
        public final BiConsumer<GrenadeUniversal, HitResult> onImpact;
        public final int bandColor;

        GrenadeFuze(Consumer<GrenadeUniversal> updateTick, int color) { this(updateTick, null, color); }
        GrenadeFuze(BiConsumer<GrenadeUniversal, HitResult> onImpact, int color) { this(null, onImpact, color); }
        GrenadeFuze(Consumer<GrenadeUniversal> updateTick, BiConsumer<GrenadeUniversal, HitResult> onImpact, int color) {
            this.updateTick = updateTick;
            this.onImpact = onImpact;
            this.bandColor = color;
        }
    }

    public static final Consumer<GrenadeUniversal> FUZE_3S = (granate) -> { if(granate.getTimer() >= 60) granate.explode(); };
    public static final Consumer<GrenadeUniversal> FUZE_7S = (granate) -> { if(granate.getTimer() >= 140) granate.explode(); };
    public static final Consumer<GrenadeUniversal> FUZE_15S = (granate) -> { if(granate.getTimer() >= 300) granate.explode(); };

    public static final BiConsumer<GrenadeUniversal, HitResult> FUZE_IMPACT = (granate, treffer) -> {
        if(granate.getTimer() < 10) return;
        Vec3 stelle = treffer.getLocation();
        granate.setPos(stelle.x, stelle.y, stelle.z);
        granate.explode();
    };

    /**
     * Der Hoehenzuender. Er schaut jeden Tick zehn Bloecke nach unten und zuendet, sobald dort
     * fester Grund ist -- die Granate geht also ueber dem Ziel hoch, nicht darin.
     */
    public static final Consumer<GrenadeUniversal> FUZE_AIRBURST = (granate) -> {

        if(granate.getTimer() < 30) return;

        Vec3 von = granate.position();
        Vec3 bis = von.add(0, -10, 0);

        HitResult treffer = granate.level.clip(new ClipContext(von, bis, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, granate));
        if(treffer.getType() == HitResult.Type.BLOCK) granate.explode();
    };
}
