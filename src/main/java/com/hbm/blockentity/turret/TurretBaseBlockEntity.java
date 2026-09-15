package com.hbm.blockentity.turret;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.entity.IRadarDetectableNT;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.entity.projectile.BulletBaseMK4;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.menus.TurretBaseMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.TurretBiometryItem;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.lib.Library;
import com.hbm.main.NuclearTechMod;
import com.hbm.particle.SpentCasing;
import com.hbm.particle.helper.CasingCreator;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.turret.TileEntityTurretBaseNT.
 *
 * Die Grundlage aller Geschuetztuerme. Sie sucht sich ein Ziel, dreht sich darauf und feuert,
 * sobald sie ausgerichtet ist -- was genau dabei passiert, sagt die einzelne Bauart in
 * updateFiringTick().
 *
 * DIE DREHUNG laeuft in Bogenmass, nicht in Grad. Der Turm dreht sich je Tick um einen festen
 * Betrag; kaeme er dabei ueber das Ziel hinaus, springt er stattdessen genau darauf. Um die
 * 360-Grad-Grenze wird eigens gerechnet, sonst faehrt er einmal ganz herum, weil das Ziel ein paar
 * Grad auf der anderen Seite steht.
 *
 * DAS ZIEL wird nur alle paar Ticks neu gesucht, nicht jeden -- das ist der teure Teil. Solange
 * eines steht, wird nur geprueft, ob es noch lebt und noch zu sehen ist.
 *
 * DIE ZAEHLLEISTE in der Oberflaeche ist keine Spielerei: stattrak zaehlt, was der Turm
 * erledigt hat, und wird beim Zielverlust durch Tod hochgezaehlt.
 *
 * ABWEICHUNGEN
 *
 * NICHT UEBERNOMMEN ist die OpenComputers- und RedstoneOverRadio-Anbindung (ENTSCHEIDUNGEN.md),
 * und mit ihr die Moeglichkeit, den Turm ohne Oberflaeche zu schalten.
 *
 * Die Zielklassen des Originals, die es im Port noch nicht gibt -- Raketen, Bomber, Eisenbahn --,
 * fehlen im Maschinenzweig; Loren und alles, was sich dem Radar zeigt, sind da. Sie kommen mit
 * ihren Entitaeten. Ebenso die Listen aus CompatExternal, ueber die fremde Mods eigene Ziele
 * eintragen; dafuer gibt es im Port keine Entsprechung.
 */
public abstract class TurretBaseBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IControlReceiver {

    /*
     *      X
     *
     *      YYY
     *      YYY
     *      YYY Z
     *
     *      X -> Fach fuer den Zielchip  (0)
     *      Y -> Munitionsfaecher        (1 - 9)
     *      Z -> Batteriefach            (10)
     */
    public static final int SLOT_CHIP = 0;
    public static final int SLOT_BATTERY = 10;

    /** Alles in Bogenmass. */
    public double rotationYaw;
    public double rotationPitch;
    /** Nur der Client, zum Zwischenschieben zweier Bilder. */
    public double lastRotationYaw;
    public double lastRotationPitch;
    /** Nur der Client: wohin er sich hinbewegt. */
    public double syncRotationYaw;
    public double syncRotationPitch;

    public boolean isOn = false;
    /** Ist der Turm auf dem Ziel? Nur dann wird gefeuert. */
    public boolean aligned = false;
    /** Ticks bis zur naechsten Zielsuche. */
    public int searchTimer;

    public long power;

    public boolean targetPlayers = false;
    public boolean targetAnimals = false;
    public boolean targetMobs = true;
    public boolean targetMachines = true;

    public @Nullable Entity target;
    public @Nullable Vec3 tPos;

    /** Strichliste. */
    public int stattrak;
    public int casingDelay;
    protected @Nullable SpentCasing cachedCasingConfig = null;

    public TurretBaseBlockEntity(BlockEntityType<? extends TurretBaseBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 11);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.power = tag.getLong("power");
        this.isOn = tag.getBoolean("isOn");
        this.targetPlayers = tag.getBoolean("targetPlayers");
        this.targetAnimals = tag.getBoolean("targetAnimals");
        this.targetMobs = tag.getBoolean("targetMobs");
        this.targetMachines = tag.getBoolean("targetMachines");
        this.stattrak = tag.getInt("stattrak");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putLong("power", this.power);
        tag.putBoolean("isOn", this.isOn);
        tag.putBoolean("targetPlayers", this.targetPlayers);
        tag.putBoolean("targetAnimals", this.targetAnimals);
        tag.putBoolean("targetMobs", this.targetMobs);
        tag.putBoolean("targetMachines", this.targetMachines);
        tag.putInt("stattrak", this.stattrak);
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.level.isClientSide) {

            this.lastRotationPitch = this.rotationPitch;
            this.lastRotationYaw = this.rotationYaw;
            this.rotationPitch = this.syncRotationPitch;
            this.rotationYaw = this.syncRotationYaw;

            /*
             * Ohne das faehrt der Turm im Bild einmal ganz herum, sobald er die 360-Grad-Grenze
             * kreuzt -- der Zwischenwert nimmt sonst den langen Weg.
             */
            if(Math.abs(this.lastRotationYaw - this.rotationYaw) > Math.PI) {
                if(this.lastRotationYaw < this.rotationYaw) this.lastRotationYaw += Math.PI * 2;
                else this.lastRotationYaw -= Math.PI * 2;
            }

            return;
        }

        this.aligned = false;
        this.updateConnections();

        if(this.target != null && !this.target.isAlive()) {
            this.target = null;
            this.stattrak++;
        }

        if(this.target != null && !this.entityInLOS(this.target)) this.target = null;

        this.tPos = this.target != null ? this.getEntityPos(this.target) : null;

        if(this.isOn() && this.hasPower()) {
            if(this.tPos != null) this.alignTurret();
        } else {
            this.target = null;
            this.tPos = null;
        }

        if(this.isOn() && this.hasPower()) {

            this.searchTimer--;
            this.setPower(this.getPower() - this.getConsumption());

            if(this.searchTimer <= 0) {
                this.searchTimer = this.getDetectorInterval();
                if(this.target == null) this.seekNewTarget();
            }
        } else {
            this.searchTimer = 0;
        }

        if(this.aligned) this.updateFiringTick();

        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, this.getMaxPower());

        this.networkPackNT(250);

        if(this.usesCasings() && this.casingDelay() > 0) {
            if(this.casingDelay > 0) this.casingDelay--;
            else this.spawnCasing();
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);

        buf.writeBoolean(this.tPos != null);
        if(this.tPos != null) {
            buf.writeDouble(this.tPos.x);
            buf.writeDouble(this.tPos.y);
            buf.writeDouble(this.tPos.z);
        }
        buf.writeDouble(this.rotationPitch);
        buf.writeDouble(this.rotationYaw);
        buf.writeLong(this.power);
        buf.writeBoolean(this.isOn);
        buf.writeBoolean(this.targetPlayers);
        buf.writeBoolean(this.targetAnimals);
        buf.writeBoolean(this.targetMobs);
        buf.writeBoolean(this.targetMachines);
        buf.writeInt(this.stattrak);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);

        this.tPos = buf.readBoolean() ? new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()) : null;
        this.syncRotationPitch = buf.readDouble();
        this.syncRotationYaw = buf.readDouble();
        this.power = buf.readLong();
        this.isOn = buf.readBoolean();
        this.targetPlayers = buf.readBoolean();
        this.targetAnimals = buf.readBoolean();
        this.targetMobs = buf.readBoolean();
        this.targetMachines = buf.readBoolean();
        this.stattrak = buf.readInt();
    }

    /**
     * Woher der Turm seinen Strom zieht. Die Grundlage nimmt die sechs Nachbarn; die grossen
     * Tuerme des Originals haengen an acht Punkten rings um ihren 2x2-Fuss und bringen ihre
     * eigene Fassung mit, wenn sie kommen.
     */
    protected void updateConnections() {
        if(this.level == null) return;
        BlockPos pos = this.getBlockPos();
        this.trySubscribe(this.level, new DirPos(pos.offset(+1, 0, 0), Library.POS_X));
        this.trySubscribe(this.level, new DirPos(pos.offset(-1, 0, 0), Library.NEG_X));
        this.trySubscribe(this.level, new DirPos(pos.offset(0, 0, +1), Library.POS_Z));
        this.trySubscribe(this.level, new DirPos(pos.offset(0, 0, -1), Library.NEG_Z));
        this.trySubscribe(this.level, new DirPos(pos.offset(0, +1, 0), Library.POS_Y));
        this.trySubscribe(this.level, new DirPos(pos.offset(0, -1, 0), Library.NEG_Y));
    }

    /** Was die einzelne Bauart tut, wenn sie auf dem Ziel steht. */
    public abstract void updateFiringTick();

    public boolean usesCasings() { return false; }
    public int casingDelay() { return 0; }

    /**
     * Die erste Patronenart, die in den Faechern liegt. Durchsucht die Faecher der Reihe nach --
     * nicht die Patronenarten --, damit die Faecher in der Reihenfolge leergeschossen werden, in
     * der sie dastehen.
     */
    public @Nullable BulletConfig getFirstConfigLoaded() {

        List<Integer> list = this.getAmmoList();
        if(list == null || list.isEmpty()) return null;

        for(int i = 1; i < 10; i++) {

            if(this.slots.get(i).isEmpty()) continue;

            for(Integer c : list) {
                BulletConfig conf = BulletConfig.configs.get(c);
                if(conf.getAmmo() != null && conf.getAmmo().matchesRecipe(this.slots.get(i), true)) return conf;
            }
        }

        return null;
    }

    public void spawnBullet(BulletConfig bullet, float baseDamage) {

        if(this.level == null) return;

        Vec3 pos = this.getTurretPos();
        Vec3 vec = rotate(new Vec3(this.getBarrelLength(), 0, 0), this.rotationPitch, this.rotationYaw);

        BulletBaseMK4 proj = new BulletBaseMK4(this.level, bullet, baseDamage, bullet.spread, (float) this.rotationYaw, (float) this.rotationPitch);
        proj.setPos(pos.x + vec.x, pos.y + vec.y, pos.z + vec.z);
        this.level.addFreshEntity(proj);

        if(this.usesCasings()) {
            if(this.casingDelay() == 0) this.spawnCasing();
            else this.casingDelay = this.casingDelay();
        }
    }

    /**
     * Dreht einen Vektor erst um die Z-, dann um die Y-Achse -- so kommt aus einem Vektor entlang
     * der Laufachse die Muendung im Weltkoordinatensystem.
     */
    protected static Vec3 rotate(Vec3 vec, double pitch, double yaw) {
        return vec.zRot((float) -pitch).yRot((float) -(yaw + Math.PI * 0.5));
    }

    public void consumeAmmo(ComparableStack ammo) {

        for(int i = 1; i < 10; i++) {
            if(!this.slots.get(i).isEmpty() && ammo.matchesRecipe(this.slots.get(i), true)) {
                this.removeItem(i, 1);
                return;
            }
        }

        this.setChanged();
    }

    /** Die Namensliste des Zielchips im ersten Fach, oder null, wenn keiner drinsteckt. */
    public @Nullable List<String> getWhitelist() {

        ItemStack chip = this.slots.get(SLOT_CHIP);
        if(chip.isEmpty() || chip.getItem() != NtmItems.TURRET_CHIP.get()) return null;

        List<String> names = TurretBiometryItem.getNames(chip);
        return names.isEmpty() ? null : names;
    }

    public void addName(String name) {
        ItemStack chip = this.slots.get(SLOT_CHIP);
        if(!chip.isEmpty() && chip.getItem() == NtmItems.TURRET_CHIP.get()) TurretBiometryItem.addName(chip, name);
    }

    public void removeName(int index) {

        ItemStack chip = this.slots.get(SLOT_CHIP);
        if(chip.isEmpty() || chip.getItem() != NtmItems.TURRET_CHIP.get()) return;

        List<String> names = new ArrayList<>(TurretBiometryItem.getNames(chip));
        if(index < 0 || index >= names.size()) return;

        names.remove(index);
        TurretBiometryItem.setNames(chip, names);
    }

    /** Sucht das naechste zulaessige Ziel in Reichweite und in Sicht. */
    protected void seekNewTarget() {

        if(this.level == null) return;

        Vec3 pos = this.getTurretPos();
        double range = this.getDetectorRange();
        List<Entity> entities = this.level.getEntitiesOfClass(Entity.class, new AABB(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z).inflate(range));

        Entity found = null;
        double closest = range;

        for(Entity entity : entities) {

            double dist = this.getEntityPos(entity).subtract(pos).length();

            if(dist > range) continue;
            if(!this.entityAcceptableTarget(entity)) continue;
            if(!this.entityInLOS(entity)) continue;

            if(dist < closest) {
                closest = dist;
                found = entity;
            }
        }

        this.target = found;
        if(found != null) this.tPos = this.getEntityPos(found);
    }

    protected void alignTurret() {
        if(this.tPos != null) this.turnTowards(this.tPos);
    }

    public void turnTowards(Vec3 ent) {

        Vec3 delta = ent.subtract(this.getTurretPos());

        double targetPitch = Math.asin(delta.y / delta.length());
        double targetYaw = -Math.atan2(delta.x, delta.z);

        this.turnTowardsAngle(targetPitch, targetYaw);
    }

    public void turnTowardsAngle(double targetPitch, double targetYaw) {

        double turnYaw = Math.toRadians(this.getTurretYawSpeed());
        double turnPitch = Math.toRadians(this.getTurretPitchSpeed());
        double pi2 = Math.PI * 2;

        /* Wuerde der naechste Schritt ueber das Ziel hinausfuehren, springt er stattdessen darauf. */
        if(Math.abs(this.rotationPitch - targetPitch) < turnPitch || Math.abs(this.rotationPitch - targetPitch) > pi2 - turnPitch) {
            this.rotationPitch = targetPitch;
        } else {
            if(targetPitch > this.rotationPitch) this.rotationPitch += turnPitch;
            else this.rotationPitch -= turnPitch;
        }

        double deltaYaw = (targetYaw - this.rotationYaw) % pi2;

        /*
         * In welche Richtung herum. Ohne das faehrt der Turm fast einmal ganz herum, wenn das Ziel
         * nur ein paar Grad daneben, aber auf der anderen Seite der 360-Grad-Grenze steht.
         */
        int dir = 0;
        if(deltaYaw < -Math.PI) dir = 1;
        else if(deltaYaw < 0) dir = -1;
        else if(deltaYaw > Math.PI) dir = -1;
        else if(deltaYaw > 0) dir = 1;

        if(Math.abs(this.rotationYaw - targetYaw) < turnYaw || Math.abs(this.rotationYaw - targetYaw) > pi2 - turnYaw) {
            this.rotationYaw = targetYaw;
        } else {
            this.rotationYaw += turnYaw * dir;
        }

        double deltaPitch = targetPitch - this.rotationPitch;
        deltaYaw = targetYaw - this.rotationYaw;

        double deltaAngle = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);

        this.rotationYaw = this.rotationYaw % pi2;
        this.rotationPitch = this.rotationPitch % pi2;

        if(deltaAngle <= Math.toRadians(this.getAcceptableInaccuracy())) this.aligned = true;
    }

    /** Sichtlinie und Schwenkbereich in einem. */
    public boolean entityInLOS(Entity e) {

        if(this.level == null || !e.isAlive()) return false;

        if(!this.hasThermalVision() && e instanceof LivingEntity living && living.hasEffect(MobEffects.INVISIBILITY)) return false;

        Vec3 pos = this.getTurretPos();
        Vec3 ent = this.getEntityPos(e);
        Vec3 delta = ent.subtract(pos);
        double length = delta.length();

        /* Der obere Rand ist grosszuegiger, sonst verliert der Turm ein Ziel schon am Rand wieder. */
        if(length < this.getDetectorGrace() || length > this.getDetectorRange() * 1.1) return false;

        delta = delta.normalize();
        double pitchDeg = Math.toDegrees(Math.asin(delta.y / delta.length()));

        if(pitchDeg < -this.getTurretDepression() || pitchDeg > this.getTurretElevation()) return false;

        return !Library.isObstructed(this.level, ent.x, ent.y, ent.z, pos.x, pos.y, pos.z);
    }

    /** Ob der Turm auf diese Entitaet ueberhaupt schiessen darf. */
    public boolean entityAcceptableTarget(Entity e) {

        if(!e.isAlive()) return false;

        List<String> wl = this.getWhitelist();

        if(wl != null) {
            if(e instanceof Player player) {
                if(wl.contains(player.getName().getString())) return false;
            } else if(e instanceof Mob mob) {
                if(mob.getCustomName() != null && wl.contains(mob.getCustomName().getString())) return false;
            }
        }

        if(this.targetAnimals) {
            if(e instanceof Npc) return true;
            if(e instanceof LivingEntity && !(e instanceof Enemy) && !(e instanceof Player)) return true;
        }

        if(this.targetMobs) {
            /* Auf den Drachen selbst nie -- nur auf seine Teile, sonst geht der Schuss ins Leere. */
            if(e instanceof EnderDragon) return false;
            if(e instanceof EnderDragonPart) return true;
            if(e instanceof Enemy) return true;
        }

        if(this.targetMachines) {
            if(e instanceof IRadarDetectableNT detectable && !detectable.canBeSeenBy(this)) return false;
            if(e instanceof AbstractMinecart) return true;
        }

        if(this.targetPlayers) {
            if(e instanceof Player player) return !player.isCreative() && !player.isSpectator();
        }

        return false;
    }

    /** Um wie viel Grad der Turm danebenstehen darf und trotzdem schiesst. */
    public double getAcceptableInaccuracy() { return 5; }
    /** Grad je Tick: 4,5 sind 90 Grad je Sekunde, eine halbe Drehung in zwei Sekunden. */
    public double getTurretYawSpeed() { return 4.5D; }
    public double getTurretPitchSpeed() { return 3D; }
    /** Wie weit der Turm den Lauf senken kann. */
    public double getTurretDepression() { return 30D; }
    /** Wie weit er ihn heben kann. */
    public double getTurretElevation() { return 30D; }
    /** Ticks bis zur naechsten Zielsuche. */
    public int getDetectorInterval() { return 10; }
    public double getDetectorRange() { return 32D; }
    /** Naeher als das wird nichts mehr erfasst -- direkt unter dem Turm ist tote Zone. */
    public double getDetectorGrace() { return 3D; }
    /** Der Drehpunkt ueber dem Block; grosse Tuerme sitzen hoeher. */
    public double getHeightOffset() { return 1.5D; }
    public double getBarrelLength() { return 1.0D; }
    /** Ob er Unsichtbare sieht. */
    public boolean hasThermalVision() { return true; }

    /** Der Drehpunkt. Sichtlinie, Zielsuche und Muendung rechnen alle von hier aus. */
    public Vec3 getTurretPos() {
        Vec3 offset = this.getHorizontalOffset();
        BlockPos pos = this.getBlockPos();
        return new Vec3(pos.getX() + offset.x, pos.getY() + this.getHeightOffset(), pos.getZ() + offset.z);
    }

    /** Wo der Drehpunkt waagerecht im Block sitzt. */
    public Vec3 getHorizontalOffset() { return new Vec3(0.5, 0, 0.5); }

    /** Worauf gezielt wird: die Mitte der Entitaet, nicht ihre Fusssohle. */
    public Vec3 getEntityPos(Entity e) {
        return new Vec3(e.getX(), e.getY() + e.getBbHeight() * 0.5, e.getZ());
    }

    /** Die Patronenarten, die dieser Turm verschiesst. */
    protected abstract List<Integer> getAmmoList();

    /** Das Bild der Oberflaeche. Alle Bauarten teilen sich ein Menue, aber nicht das Bild. */
    public ResourceLocation getGuiTexture() {
        return NuclearTechMod.withDefaultNamespace("textures/gui/weapon/gui_turret_base.png");
    }

    /** Nur der Client: die Patronenarten als Gegenstaende, fuer die Anzeige ueber den Faechern. */
    protected @Nullable List<ItemStack> ammoStacks;

    public List<ItemStack> getAmmoTypesForDisplay() {

        if(this.ammoStacks != null) return this.ammoStacks;

        this.ammoStacks = new ArrayList<>();

        for(Integer i : this.getAmmoList()) {
            BulletConfig config = BulletConfig.configs.get(i);
            if(config != null && config.getAmmo() != null) this.ammoStacks.add(config.getAmmo().toStack());
        }

        return this.ammoStacks;
    }

    @Override
    public int[] getSlotsForFace(net.minecraft.core.Direction direction) {
        return new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) { return true; }

    public boolean hasPower() { return this.getPower() >= this.getConsumption(); }
    public boolean isOn() { return this.isOn; }

    @Override public void setPower(long power) { this.power = power; }
    @Override public long getPower() { return this.power; }

    public int getPowerScaled(int scale) { return (int) (this.power * scale / this.getMaxPower()); }
    public long getConsumption() { return 100; }

    private @Nullable AABB renderBox;

    /**
     * Ohne @Override: getRenderBoundingBox stammt aus der NeoForge-Erweiterung.
     *
     * ABWEICHUNG: das Original nimmt hier die unendliche Box, damit der Turm nie aus dem Bild
     * faellt, egal wie weit sein Lauf ausschwenkt. Hier steht stattdessen ein Kasten, der den
     * groessten Turm mit Abstand umschliesst -- eine unendliche Box laesst den Renderer bei jedem
     * Bild laufen, auch wenn der Turm laengst hinter dem Spieler liegt.
     */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos pos = this.getBlockPos();
            this.renderBox = new AABB(pos.getX() - 4, pos.getY() - 1, pos.getZ() - 4,
                    pos.getX() + 5, pos.getY() + 8, pos.getZ() + 5);
        }
        return this.renderBox;
    }

    /** Wo die Huelse ausgeworfen wird -- vorgabeweise am Drehpunkt. */
    protected Vec3 getCasingSpawnPos() { return this.getTurretPos(); }

    /** Wie die Huelse wegfliegt: vorwaerts, nach oben, zur Seite. */
    protected Vec3 getCasingMotion() { return new Vec3(0.2, 0.2, 0); }

    protected void spawnCasing() {

        if(this.level == null || this.cachedCasingConfig == null) return;

        Vec3 spawn = this.getCasingSpawnPos();
        Vec3 motion = this.getCasingMotion();

        /*
         * ABWEICHUNG: das Original reicht die Winkel des Turms im Bogenmass an den Huelsenwerfer
         * weiter, der aber mit Grad rechnet -- dort fliegen die Huelsen deshalb in eine beliebige
         * Richtung. Hier wird umgerechnet.
         */
        CasingCreator.composeEffect(this.level, spawn.x, spawn.y, spawn.z,
                (float) Math.toDegrees(this.rotationYaw), (float) Math.toDegrees(-this.rotationPitch),
                motion.x, motion.y, motion.z, 0.01,
                1F, 1F, this.cachedCasingConfig.getName(), false, 0, 0D, 0);

        this.cachedCasingConfig = null;
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override public void receiveControl(CompoundTag tag) { }

    @Override
    public void receiveControl(Player player, CompoundTag tag) {

        if(tag.contains("toggle")) this.isOn = !this.isOn;
        if(tag.contains("players")) this.targetPlayers = !this.targetPlayers;
        if(tag.contains("animals")) this.targetAnimals = !this.targetAnimals;
        if(tag.contains("mobs")) this.targetMobs = !this.targetMobs;
        if(tag.contains("machines")) this.targetMachines = !this.targetMachines;

        if(tag.contains("del")) this.removeName(tag.getInt("del"));
        else if(tag.contains("name")) this.addName(tag.getString("name"));
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TurretBaseMenu(id, inventory, this);
    }
}
