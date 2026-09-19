package com.hbm.items.weapon.grenade;

import com.hbm.items.EnumMultiItem;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.grenade.ItemGrenadeShell.
 *
 * Der Koerper der Granate. Er entscheidet, welche Fuellung hineinpasst, wie lange das Ziehen
 * dauert, wie hoch sie abspringt und wie weit sie fliegt -- und wie viele davon auf einen
 * Stapel gehen.
 *
 * Der Stiel (STICK) fliegt anderthalbmal so weit wie die uebrigen, springt dafuer aber nur
 * halb so hoch ab: ein Griff am Ende macht den Wurf weiter und den Aufprall stumpfer.
 */
public class GrenadeShellItem extends EnumMultiItem {

    public GrenadeShellItem(Properties properties) {
        super(properties, GrenadeShell.class, true, true);
    }

    public enum GrenadeShell {
        /** Die Splitterhandgranate. Zerlegt sich beim Explodieren zusaetzlich selbst. */
        FRAG(4, 30, 0.5D, 1D),
        /** Die Stielhandgranate. Fliegt weiter, springt weniger. */
        STICK(4, 43, 0.25D, 1.5D),
        /** Der Elektronikkoerper fuer EMP-, Plasma- und Laserfuellung. */
        TECH(2, 30, 0.5D, 1D),
        /** Der Nuka-Koerper fuer die schweren Fuellungen. Nur einer je Stapel. */
        NUKE(1, 43, 0.25D, 1.5D);

        public final int stackLimit;
        /** Wie viele Ticks das Ziehen dauert, bevor geworfen werden kann. */
        public final int drawDuration;
        public final double bounce;
        public final double yeetForce;

        GrenadeShell(int stackLimit, int drawDuration, double bounce, double yeetForce) {
            this.stackLimit = stackLimit;
            this.drawDuration = drawDuration;
            this.bounce = bounce;
            this.yeetForce = yeetForce;
        }
    }
}
