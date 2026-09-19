package com.hbm.entity.grenade;

import com.hbm.items.weapon.grenade.GrenadeExtraItem.GrenadeExtra;
import com.hbm.items.weapon.grenade.GrenadeFillingItem.GrenadeFilling;
import com.hbm.items.weapon.grenade.GrenadeFuzeItem.GrenadeFuze;
import com.hbm.items.weapon.grenade.GrenadeShellItem.GrenadeShell;
import com.hbm.items.weapon.grenade.GrenadeUniversalItem;
import com.hbm.entity.projectile.ThrowableNT;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;
import com.hbm.util.Vec3NT;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.entity.grenade.EntityGrenadeUniversal.
 *
 * Die geworfene Granate. Sie traegt ihren eigenen Gegenstand mit sich -- daraus liest sie
 * Koerper, Fuellung, Zuender und Aufsatz, und daraus weiss auch der Zeichner, wie sie aussieht.
 *
 * DREI STELLEN, an denen die Bauteile zu Wort kommen: jeden Tick (Zeitzuender,
 * Naeherungszuender), beim Anstossen (Aufschlagzuender, Kleber) und beim Hochgehen (die
 * Fuellung, dazu Splittermantel und Dreifachteiler).
 *
 * SIE GEHT NICHT VOM ANSTOSSEN HOCH. Ein Treffer laesst sie abspringen: senkrecht zur
 * getroffenen Flaeche kehrt sich die Bewegung um und wird um den Absprungwert des Koerpers
 * gedaempft, laengs der Flaeche bleibt ein Fuenftel liegen. Ob sie dabei hochgeht, entscheidet
 * allein der Zuender.
 */
public class GrenadeUniversal extends ThrowableNT implements ItemSupplier {

    public static final int TRAIL_TRIPLET = 1;

    private static final EntityDataAccessor<ItemStack> GRANATE = SynchedEntityData.defineId(GrenadeUniversal.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> ABSPRUENGE = SynchedEntityData.defineId(GrenadeUniversal.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPUR = SynchedEntityData.defineId(GrenadeUniversal.class, EntityDataSerializers.INT);

    /** Die Eigendrehung im Flug. Nur der Zeichner liest sie, deshalb steht sie nicht im Netz. */
    public double prevSpin;
    public double spin;

    public GrenadeUniversal(EntityType<? extends GrenadeUniversal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GRANATE, ItemStack.EMPTY);
        builder.define(ABSPRUENGE, 0);
        builder.define(SPUR, 0);
    }

    /** Immer genau eine -- der Stapel des Werfers bleibt unberuehrt. */
    public void setGrenadeItem(ItemStack stack) {
        ItemStack einzeln = stack.copy();
        einzeln.setCount(1);
        this.entityData.set(GRANATE, einzeln);
    }

    public GrenadeUniversal setTrail(int spur) {
        this.entityData.set(SPUR, spur);
        return this;
    }

    public ItemStack getGrenadeItem() { return this.entityData.get(GRANATE); }
    public int getBounces() { return this.entityData.get(ABSPRUENGE); }
    public int getTrail() { return this.entityData.get(SPUR); }

    public GrenadeShell getShell() { return GrenadeUniversalItem.getShell(this.getGrenadeItem()); }
    public GrenadeFilling getFilling() { return GrenadeUniversalItem.getFilling(this.getGrenadeItem()); }
    public GrenadeFuze getFuze() { return GrenadeUniversalItem.getFuze(this.getGrenadeItem()); }
    public GrenadeExtra getExtra() { return GrenadeUniversalItem.getExtra(this.getGrenadeItem()); }

    /**
     * Setzt die Granate in die Hand des Werfers und wirft sie. Der Versatz von einem Viertel
     * Block nach rechts und unten ist der des Originals -- sie soll aus der Hand kommen, nicht
     * aus der Nasenspitze.
     */
    public void werfen(LivingEntity werfer) {

        this.setOwner(werfer);

        Vec3NT versatz = new Vec3NT(0.25, -0.25, 0).rotateAroundYDeg(-werfer.getYRot() + 180);
        this.setPos(werfer.getX() + versatz.xCoord,
                werfer.getEyeY() + versatz.yCoord,
                werfer.getZ() + versatz.zCoord);

        Vec3 blick = werfer.getLookAngle().normalize();
        this.shoot(blick.x, blick.y, blick.z, (float) this.getShell().yeetForce, 0F);
    }

    @Override
    public void tick() {

        super.tick();

        GrenadeFuze zuender = this.getFuze();
        GrenadeExtra aufsatz = this.getExtra();

        if(zuender.updateTick != null) zuender.updateTick.accept(this);
        if(aufsatz != null && aufsatz.updateTick != null) aufsatz.updateTick.accept(this);

        if(this.level.isClientSide) this.dreheDich();
    }

    /**
     * Im Flug dreht sie sich mit fester Geschwindigkeit; sobald sie einmal aufgesetzt hat,
     * richtet sich die Drehung nach der zurueckgelegten Strecke -- eine rollende Granate
     * kommt so mit dem Rollen zum Stehen.
     */
    private void dreheDich() {

        this.prevSpin = this.spin;

        if(this.getBounces() <= 0) {
            this.spin += 15;
        } else {
            double strecke = new Vec3NT(this.xo - this.getX(), 0, this.zo - this.getZ()).length();
            this.spin += Math.min(15, strecke * 50);
        }

        if(this.spin >= 360) {
            this.prevSpin -= 360;
            this.spin -= 360;
        }
    }

    @Override
    protected void onImpact(HitResult treffer) {

        GrenadeFuze zuender = this.getFuze();
        GrenadeExtra aufsatz = this.getExtra();

        if(zuender.onImpact != null) zuender.onImpact.accept(this, treffer);
        if(aufsatz != null && aufsatz.onImpact != null) aufsatz.onImpact.accept(this, treffer);

        /* Ist sie hier schon weg, hat der Zuender sie gezuendet -- dann gibt es nichts mehr
         * abzuspringen. */
        if(this.isRemoved()) return;
        if(!(treffer instanceof BlockHitResult bhr)) return;

        Direction seite = bhr.getDirection();
        Vec3 stelle = treffer.getLocation();

        this.setPos(stelle.x + seite.getStepX() * 0.05,
                stelle.y + seite.getStepY() * 0.05,
                stelle.z + seite.getStepZ() * 0.05);

        Vec3 bewegung = this.getDeltaMovement();

        if(bewegung.length() > 0.2) {
            SoundUtils.playAtVec3(this.level, this.position(), NtmSoundEvents.GRENADE_BOUNCE.get(), this.getSoundSource());
        }

        double absprung = this.getShell().bounce;

        this.setDeltaMovement(
                seite.getStepX() != 0 ? bewegung.x * -absprung : bewegung.x * 0.8,
                seite.getStepY() != 0 ? bewegung.y * -absprung : bewegung.y * 0.8,
                seite.getStepZ() != 0 ? bewegung.z * -absprung : bewegung.z * 0.8);

        this.hasImpulse = true;
        this.entityData.set(ABSPRUENGE, this.getBounces() + 1);
    }

    public void explode() {

        this.discard();

        GrenadeFilling fuellung = this.getFilling();
        if(fuellung.explode != null) fuellung.explode.accept(this);

        GrenadeExtra aufsatz = this.getExtra();
        if(aufsatz != null && aufsatz.onExplode != null) aufsatz.onExplode.accept(this);
    }

    /**
     * Was der Zeichner zeigt.
     *
     * ABWEICHUNG: das Original zeichnet ein Wellenfrontmodell (grenades.obj) mit vier
     * Koerperformen und faerbt Koerper, Aufkleber und Zuenderring nach den Werten der Bauteile.
     * Der Port zeigt vorerst das Gegenstandsbild -- die Granate fliegt und wirkt richtig, sie
     * sieht nur noch flach aus. Das Modell gehoert in eine eigene Runde, zusammen mit der
     * Ziehbewegung, die aus demselben Grund fehlt.
     */
    @Override public ItemStack getItem() { return this.getGrenadeItem(); }

    /** Wie lange sie schon unterwegs ist -- ob fliegend oder liegend. */
    public int getTimer() { return this.ticksInAir + this.ticksInGround; }

    /* Eine Granate verschwindet nicht von selbst: sie liegt, bis ihr Zuender sie holt. */
    @Override protected int groundDespawn() { return 0; }
    @Override public boolean fullBlockCollisions() { return true; }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("granate", this.getGrenadeItem().saveOptional(this.registryAccess()));
        tag.putInt("abspruenge", this.getBounces());
        tag.putInt("spur", this.getTrail());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(GRANATE, ItemStack.parseOptional(this.registryAccess(), tag.getCompound("granate")));
        this.entityData.set(ABSPRUENGE, tag.getInt("abspruenge"));
        this.entityData.set(SPUR, tag.getInt("spur"));
    }
}
