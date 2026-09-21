package com.hbm.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.ai.EntityAIBreaking.
 *
 * Wer nicht durch die Tuer kommt, macht sich eine. Steht dem Traeger etwas im Weg und findet
 * sein Wegfinder keinen Pfad mehr, sucht diese Aufgabe den Block vor seiner Nase und gräbt ihn
 * weg -- langsam, mit Klopfgeraeusch und Sprungbild, so wie ein Spieler es taete.
 *
 * DIE RECHNUNG DES ORIGINALS, unveraendert uebernommen:
 *   * haerte = Abbaufestigkeit geteilt durch drei, als ganze Zahl;
 *   * jeder Takt zaehlt digTick hoch, der Fortschritt ist digTick * 0,05 / haerte;
 *   * bei eins ist der Block weg, alle fuenf Takte gibt es Ton und Sprungbild.
 * Eine Haerte unter null (unzerstoerbar) bricht ab.
 *
 * ABWEICHUNG: das Original tastet die Umrisse des Traegers Punkt fuer Punkt ab, um bei grossen
 * Wesen alle Ecken zu erwischen -- ein Zaehler wandert dabei ueber breite mal breite mal hoch
 * Punkte. Fuer zweibeinige Wesen sind das nach seinem eigenen Kommentar zwei Punkte, und der
 * FBI-Agent ist eines. Der Port tastet die Augenhoehe und die Fusshoehe ab, also dieselben
 * zwei, und spart sich den Zaehler.
 */
public class BreakingGoal extends Goal {

    /** Wie weit vor die Nase geschaut wird. Das Original nimmt zwei Bloecke. */
    private static final double REICHWEITE = 2D;

    private final Mob graeber;
    private BlockPos marke;
    private int digTick;

    public BreakingGoal(Mob graeber) {
        this.graeber = graeber;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {

        LivingEntity ziel = this.graeber.getTarget();
        if(ziel == null) return false;
        if(!this.graeber.getNavigation().isDone()) return false;
        if(this.graeber.distanceTo(ziel) <= 1D) return false;
        if(!ziel.onGround() && this.graeber.hasLineOfSight(ziel)) return false;

        BlockPos treffer = this.naechstesHindernis();
        if(treffer == null) return false;

        this.marke = treffer;
        return true;
    }

    @Override
    public boolean canContinueToUse() {

        if(this.marke == null) return false;
        if(!this.graeber.isAlive()) return false;

        Vec3 abstand = new Vec3(
                this.marke.getX() - this.graeber.getX(),
                this.marke.getY() - (this.graeber.getY() + this.graeber.getEyeHeight()),
                this.marke.getZ() - this.graeber.getZ());

        return abstand.length() <= 4D;
    }

    @Override
    public void stop() {
        this.abbrechen();
    }

    @Override
    public void tick() {

        /* Alle zehn Takte neu schauen, ob inzwischen etwas Naeheres im Weg steht. */
        if(this.graeber.tickCount % 10 == 0) {
            BlockPos treffer = this.naechstesHindernis();
            if(treffer != null) this.marke = treffer;
        }

        Level level = this.graeber.level();

        if(this.marke == null || level.getBlockState(this.marke).isAir()) {
            this.digTick = 0;
            return;
        }

        BlockState state = level.getBlockState(this.marke);
        this.digTick++;

        int haerte = (int) state.getDestroySpeed(level, this.marke) / 3;

        /* Nur noch ein Fangnetz: unzerstoerbare Bloecke haelt schon strahl() heraus. Hier
         * greift es, wenn sich der Block zwischen Markieren und Graben geaendert hat. */
        if(haerte < 0) {
            this.abbrechen();
            return;
        }

        /* Haerte null faellt beim ersten Schlag. Im Original kommt das von einer Division
         * durch null, die Unendlich ergibt; hier steht es ausgeschrieben. Das betrifft mehr
         * als Laub und Blumen: die Ganzzahldivision macht aus Stein (1,5) ebenfalls null. */
        float fortschritt = haerte == 0 ? 1F : (this.digTick * 0.05F) / haerte;

        if(fortschritt >= 1F) {

            this.digTick = 0;
            level.destroyBlock(this.marke, false);
            this.marke = null;

            LivingEntity ziel = this.graeber.getTarget();
            if(ziel != null) this.graeber.getNavigation().moveTo(ziel, 1D);

        } else if(this.digTick % 5 == 0) {

            SoundType klang = state.getSoundType();
            level.playSound(null, this.marke, klang.getHitSound(), SoundSource.BLOCKS,
                    klang.getVolume() + 1F, klang.getPitch());
            this.graeber.swing(InteractionHand.MAIN_HAND);
            level.destroyBlockProgress(this.graeber.getId(), this.marke, (int) (fortschritt * 10F));
        }
    }

    /** Nimmt die Marke zurueck und loescht das Sprungbild, damit kein Rest stehen bleibt. */
    private void abbrechen() {
        if(this.marke != null) this.graeber.level().destroyBlockProgress(this.graeber.getId(), this.marke, -1);
        this.marke = null;
        this.digTick = 0;
    }

    /**
     * Der Block vor der Nase, oder null. Geschaut wird aus der Augenhoehe und aus der
     * Fusshoehe -- die zwei Punkte, die ein zweibeiniges Wesen braucht.
     */
    private BlockPos naechstesHindernis() {

        Vec3 blick = this.graeber.getLookAngle();

        BlockPos ausAugenhoehe = this.strahl(this.graeber.getEyePosition(), blick);
        if(ausAugenhoehe != null) return ausAugenhoehe;

        return this.strahl(this.graeber.position(), blick);
    }

    /** Ein Strahl von start in Blickrichtung; gibt den getroffenen Block zurueck, wenn er sich abbauen laesst. */
    private BlockPos strahl(Vec3 start, Vec3 blick) {

        Vec3 ende = start.add(blick.x * REICHWEITE, blick.y * REICHWEITE, blick.z * REICHWEITE);

        BlockHitResult treffer = this.graeber.level().clip(new ClipContext(
                start, ende, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.graeber));

        if(treffer.getType() != HitResult.Type.BLOCK) return null;

        BlockPos pos = treffer.getBlockPos();
        Level level = this.graeber.level();

        if(level.getBlockState(pos).isAir()) return null;
        if(level.getBlockState(pos).getDestroySpeed(level, pos) < 0) return null;

        return pos;
    }
}
