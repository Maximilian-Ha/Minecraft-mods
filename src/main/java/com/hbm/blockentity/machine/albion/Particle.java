package com.hbm.blockentity.machine.albion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: die eingebettete Klasse TileEntityPASource.Particle.
 *
 * Ein Teilchen auf seinem Weg durch den Ring. Es traegt drei Zahlen mit sich, und an allen dreien
 * kann die Fahrt scheitern:
 *
 * IMPULS -- was die Hochfrequenzkavitaeten hineingesteckt haben. Zu wenig, und das Rezept am Ende
 * greift nicht; zu viel, und die Spulen der Magnete halten nicht mehr mit.
 *
 * STREUUNG -- der Strahl faechert von selbst auf, die Quadrupole buendeln ihn wieder. Ueber
 * tausend ist er hin.
 *
 * ZURUECKGELEGTE STRECKE -- die Dipole verlangen einen Mindestabstand, sonst ist die Kurve zu eng.
 *
 * Es ist KEINE Entitaet und steht in keiner Welt: der Strahl ist eine Rechnung, die die Quelle
 * fuehrt, und nur die Leuchten der Bauteile verraten, wo er gerade ist.
 */
public class Particle {

    public static final int MAX_DEFOCUS = 1000;

    private final MachinePASourceBlockEntity source;

    public BlockPos pos;
    public Direction dir;

    public int momentum;
    public int defocus;
    public int distanceTraveled;

    public boolean invalid = false;

    /** Woraus es gemacht wurde -- der Detektor braucht beides, um das Rezept zu finden. */
    public ItemStack input1;
    public ItemStack input2;

    public Particle(MachinePASourceBlockEntity source, BlockPos pos, Direction dir, ItemStack input1, ItemStack input2) {
        this.source = source;
        this.pos = pos;
        this.dir = dir;
        this.input1 = input1;
        this.input2 = input2;
    }

    /** Das Ende der Fahrt. Die Quelle merkt sich, woran es lag. */
    public void crash(MachinePASourceBlockEntity.PAState state) {
        this.invalid = true;
        this.source.updateState(state);
    }

    public void move(BlockPos pos) {
        this.pos = pos;
        this.source.lastSpeed = this.momentum;
    }

    public void addDistance(int dist) { this.distanceTraveled += dist; }
    public void resetDistance() { this.distanceTraveled = 0; }

    public void defocus(int amount) {
        this.defocus += amount;
        if(this.defocus > MAX_DEFOCUS) this.crash(MachinePASourceBlockEntity.PAState.CRASH_DEFOCUS);
    }

    public void focus(int amount) {
        this.defocus -= amount;
        if(this.defocus < 0) this.defocus = 0;
    }

    public void accelerate(int amount) { this.momentum += amount; }
}
