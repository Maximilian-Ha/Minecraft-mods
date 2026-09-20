package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.mob.CyberCrab;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityCyberCrab.
 *
 * Das Nest. Alle zweihundert Ticks sieht es nach: ist der Platz darueber frei, steht ein
 * Spieler im Umkreis von fuenfundzwanzig Bloecken, und sind weniger als fuenf Krabben in
 * der Nachbarschaft? Dann kommt eine dazu -- jede fuenfte als Teslakrabbe.
 *
 * DER ZAEHLER WIRD NUR BEIM AUFSTELLEN ZURUECKGESETZT, nicht bei jedem Versuch: solange
 * eine der drei Bedingungen nicht stimmt, laeuft er weiter, und die naechste Krabbe kommt,
 * sobald sie wieder stimmen. Das Original macht es genauso.
 *
 * ER WIRD NICHT GESPEICHERT. Nach dem Laden faengt das Nest wieder bei null an -- im
 * Original steht der Zaehler ebenfalls nur im Arbeitsspeicher.
 */
public class MeteorSpawnerBlockEntity extends BlockEntity {

    /** Wartezeit zwischen zwei Krabben, in Ticks. */
    public static final int WARTEZEIT = 200;

    /** So weit darf der naechste Spieler hoechstens weg sein. */
    public static final double SPIELERNAEHE = 25D;

    /** Ab so vielen Krabben in der Nachbarschaft ruht das Nest. */
    public static final int HOECHSTZAHL = 5;

    private int alter = 0;

    public MeteorSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.METEOR_SPAWNER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MeteorSpawnerBlockEntity nest) {

        nest.alter++;

        if(nest.alter <= WARTEZEIT) return;
        if(!level.getBlockState(pos.above()).isAir()) return;
        /* true heisst: auch ein Spieler im Schoepfermodus zaehlt. Das Original prueft mit
         * getClosestPlayer, und der schliesst nur Zuschauer aus. */
        if(level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, SPIELERNAEHE, true) == null) return;

        List<CyberCrab> krabben = level.getEntitiesOfClass(CyberCrab.class,
                new AABB(pos.getX() - 5, pos.getY() - 2, pos.getZ() - 5, pos.getX() + 6, pos.getY() + 4, pos.getZ() + 6));

        if(krabben.size() < HOECHSTZAHL) {

            CyberCrab krabbe = level.random.nextInt(5) == 0
                    ? NtmEntityTypes.TESLA_CRAB.get().create(level)
                    : NtmEntityTypes.CYBER_CRAB.get().create(level);

            /* Kein finalizeSpawn: das Original ruft nur setPosition und spawnEntityInWorld. */
            if(krabbe != null) {
                krabbe.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, krabbe.getYRot(), krabbe.getXRot());
                level.addFreshEntity(krabbe);
            }
        }

        nest.alter = 0;
    }
}
