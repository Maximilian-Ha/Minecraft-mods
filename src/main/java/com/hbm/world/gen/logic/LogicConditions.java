package com.hbm.world.gen.logic;

import com.hbm.blockentity.WandLogicBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Portiert aus 1.7.10: com.hbm.world.gen.util.LogicBlockConditions.
 *
 * Die Bedingung eines Logikstabs wird jeden Tick gefragt; trifft sie zu, geht die Phase eine
 * weiter. Was der Port braucht, sind vier -- nachgezaehlt ueber die drei Bauwerke, die
 * Logikstaebe benutzen (Kran, Fabrik, Turmsockel): EMPTY und die drei Spielerwuerfel.
 *
 * DER WUERFEL IST SCHIEF, und das ist kein Versehen: das Original spannt ihn von (x, y, z) bis
 * (x+1, y-2, z+1) auf und weitet ihn dann um r in jede Richtung. Die Untergrenze liegt also
 * zwei Bloecke UNTER dem Stab -- wer davorsteht, wird gesehen, wer darueber laeuft, erst ab
 * halber Reichweite. Uebernommen, wie es dasteht.
 */
public class LogicConditions {

    /** Fuer Staebe, die allein ueber ihre Wechselwirkung weiterkommen: trifft nie zu. */
    public static final Predicate<WandLogicBlockEntity> EMPTY = be -> false;

    private static final Map<String, Predicate<WandLogicBlockEntity>> BEDINGUNGEN = Map.of(
            "EMPTY", EMPTY,
            "PLAYER_CUBE_3", spielerImWuerfel(3),
            "PLAYER_CUBE_5", spielerImWuerfel(5),
            "PLAYER_CUBE_25", spielerImWuerfel(25));

    /** Null, wenn der Name unbekannt ist -- dann loescht sich der Stab, wie im Original. */
    public static Predicate<WandLogicBlockEntity> finde(String name) {
        return BEDINGUNGEN.get(name);
    }

    private static Predicate<WandLogicBlockEntity> spielerImWuerfel(double reichweite) {
        return be -> {
            BlockPos pos = be.getBlockPos();
            AABB wuerfel = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() - 2, pos.getZ() + 1).inflate(reichweite);
            return !be.getLevel().getEntitiesOfClass(Player.class, wuerfel).isEmpty();
        };
    }
}
