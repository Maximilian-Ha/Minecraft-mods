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
 * weiter. Was der Port braucht, sind FUENF -- nachgemessen ueber die 31 Staebe der drei
 * Bauwerke, die Logikstaebe benutzen (Kran, Fabrik, Turmsockel): EMPTY, die drei
 * Spielerwuerfel und BOMB_CRANE.
 *
 * DER WUERFEL IST SCHIEF, und das ist kein Versehen: das Original spannt ihn von (x, y, z) bis
 * (x+1, y-2, z+1) auf und weitet ihn dann um r in jede Richtung. Die Untergrenze liegt also
 * zwei Bloecke UNTER dem Stab -- wer davorsteht, wird gesehen, wer darueber laeuft, erst ab
 * halber Reichweite. Uebernommen, wie es dasteht.
 */
public class LogicConditions {

    /** Fuer Staebe, die allein ueber ihre Wechselwirkung weiterkommen: trifft nie zu. */
    public static final Predicate<WandLogicBlockEntity> EMPTY = be -> false;

    /** Der Wuerfel der Bombenbedingung -- einmal gebaut, nicht bei jedem Tick neu. */
    private static final Predicate<WandLogicBlockEntity> WUERFEL_10 = spielerImWuerfel(10);

    /**
     * BOMB_CRANE ist beides: im Original steht derselbe Name in der Aktions- UND in der
     * Bedingungsliste, und der Kran benutzt beide an einem Stab. Als Bedingung setzt sie in
     * Phase 0 dieselbe C4-Haftladung wie die Aktion, nur auf zehn Sekunden statt auf eine
     * Minute; danach wartet sie auf einen Spieler im Umkreis von zehn Bloecken.
     *
     * DIE BEDINGUNG GEWINNT. LogicBlock ruft in jedem Tick erst die Aktion, dann die Bedingung
     * (LogicBlock.java:117-118, im Port WandLogicBlockEntity.serverTick). In Phase 0 stellt die
     * Aktion die Uhr also auf 1200 und die Bedingung unmittelbar danach auf 200 -- am Ende des
     * Ticks stehen zehn Sekunden, nicht eine Minute. Die Minute der Aktion ist nie zu sehen.
     * Uebernommen, wie es dasteht.
     */
    public static final Predicate<WandLogicBlockEntity> BOMB_CRANE = be -> {

        if(be.phase == 0 && be.getLevel() != null && !be.getLevel().isClientSide) {
            LogicActions.ladungSetzen(be.getLevel(), be.getBlockPos().above(), 200);
        }

        return WUERFEL_10.test(be);
    };

    private static final Map<String, Predicate<WandLogicBlockEntity>> BEDINGUNGEN = Map.of(
            "EMPTY", EMPTY,
            "PLAYER_CUBE_3", spielerImWuerfel(3),
            "PLAYER_CUBE_5", spielerImWuerfel(5),
            "PLAYER_CUBE_25", spielerImWuerfel(25),
            "BOMB_CRANE", BOMB_CRANE);

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
