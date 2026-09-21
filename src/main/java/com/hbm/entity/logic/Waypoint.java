package com.hbm.entity.logic;

import com.hbm.entity.mob.glyphid.Glyphid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.logic.EntityWaypoint.
 *
 * Ein unsichtbarer Merkpunkt, den Glyphiden untereinander weiterreichen. Er traegt eine
 * Aufgabe; wer ihn erreicht, bekommt diese Aufgabe -- und der Merkpunkt verschwindet.
 *
 * ER KANN EINEN ZWEITEN NACH SICH ZIEHEN: das Original haengt an einen Merkpunkt optional
 * einen weiteren (additional). Erreicht ein Glyphid den ersten, wird der zweite in die Welt
 * gesetzt und als neue Aufgabe weitergereicht. So entsteht "geh nach Hause, hol Verstaerkung,
 * komm zurueck".
 *
 * ZWEI AUSNAHMEN, und beide stehen im Original: Spaeher und Atomglyphiden nehmen die Aufgabe
 * NICHT an -- sie haben ihre eigenen Plaene. Und beim Bau eines Baus verschwindet der
 * Merkpunkt nur, wenn wirklich ein Spaeher da war.
 *
 * NICHT UEBERNOMMEN: die Sichtbarmachung fuer die Fehlersuche (MobConfig.waypointDebug). Sie
 * zeichnet eine farbige Saeule ueber jedem Merkpunkt und haengt an einem Schalter, den das
 * Original nur fuer die Entwicklung fuehrt. Mit ihr faellt auch getColor weg -- eine Farbe,
 * die niemand liest.
 *
 * ER VERFAELLT von selbst nach zweitausendvierhundert Ticks, also zwei Minuten.
 */
public class Waypoint extends Entity {

    private static final EntityDataAccessor<Integer> AUFGABE =
            SynchedEntityData.defineId(Waypoint.class, EntityDataSerializers.INT);

    /** Nach zwei Minuten ist er weg. */
    public int maxAge = 2400;
    /** Wie nah ein Glyphid heran muss. */
    public int radius = 3;
    /** Hoch gesetzte Merkpunkte unterbrechen den Angriff. */
    public boolean highPriority = false;

    @Nullable
    protected Waypoint additional;
    private boolean hasSpawned = false;

    public Waypoint(EntityType<? extends Waypoint> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public Waypoint(Level level) {
        this(com.hbm.entity.NtmEntityTypes.WAYPOINT.get(), level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AUFGABE, Glyphid.TASK_IDLE);
    }

    public void setHighPriority() {
        this.highPriority = true;
    }

    public int getWaypointType() {
        return this.entityData.get(AUFGABE);
    }

    public void setWaypointType(int aufgabe) {
        this.entityData.set(AUFGABE, aufgabe);
    }

    public void setAdditionalWaypoint(Waypoint waypoint) {
        this.additional = waypoint;
    }

    @Override
    public void tick() {
        super.tick();

        if(this.tickCount >= this.maxAge) {
            this.discard();
            return;
        }

        if(this.level().isClientSide) return;
        if(this.tickCount % 40 != 0) return;

        AABB kasten = new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ())
                .inflate(this.radius, this.radius, this.radius);

        List<Entity> nahe = this.level().getEntities(this, kasten);

        for(Entity e : nahe) {

            if(!(e instanceof Glyphid glyphid)) continue;

            if(this.additional != null && !this.hasSpawned) {
                this.level().addFreshEntity(this.additional);
                this.hasSpawned = true;
            }

            boolean ausnahme = glyphid.getWaypoint() != this || !glyphid.nimmtWegpunkteAn();

            if(!ausnahme) glyphid.setCurrentTask(this.getWaypointType(), this.additional);

            if(this.getWaypointType() == Glyphid.TASK_BUILD_HIVE) {
                if(glyphid.istSpaeher()) this.discard();
            } else {
                this.discard();
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setWaypointType(tag.getInt("type"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("type", this.getWaypointType());
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
