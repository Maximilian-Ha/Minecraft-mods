package com.hbm.entity.projectile;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.items.weapon.sedna.BulletConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nullable;
import java.util.Optional;

public class BulletBeamBase extends Entity implements IEntityWithComplexSpawn {

    public LivingEntity thrower;
    public BulletConfig config;
    public float damage;
    public double headingX;
    public double headingY;
    public double headingZ;
    public double beamLength;

    // private!!! use getters
    private static final EntityDataAccessor<Integer> BULLET_CONFIG = SynchedEntityData.defineId(BulletBeamBase.class, EntityDataSerializers.INT);

    public BulletBeamBase(EntityType<? extends BulletBeamBase> entityType, Level level) { super(entityType, level); }
    public BulletBeamBase(Level level) { super(NtmEntityTypes.BULLET_BEAM.get(), level); }

    public LivingEntity getThrower() { return this.thrower; }

    public BulletBeamBase(Level level, BulletConfig config, float baseDamage) {
        this(level);

        this.setBulletConfig(config);
        this.damage = baseDamage * this.config.damageMult;
    }

    public BulletBeamBase(LivingEntity entity, BulletConfig config, float baseDamage) {
        this(entity.level, config, baseDamage);
        this.thrower = entity;
    }

    /**
     * Der Schussweg-Konstruktor. Ein Strahl fliegt nicht, er trifft SOFORT: der Konstruktor
     * legt Ursprung und Richtung fest und laeuft die Strecke noch in derselben Zeile ab.
     * Was danach in der Welt steht, ist nur noch die Zeichnung.
     *
     * RUNDE 188: bis hierher hat diese Klasse gar nichts getan. Sie war angemeldet, hatte
     * einen Konfigurationszeiger und eine Laenge -- aber niemand hat je eine erzeugt, und
     * der Zweig in Lego.shoot, der das tun sollte, war auskommentiert. Damit waren ALLE drei
     * setBeam()-Konfigurationen des Ports wirkungslos: die beiden der 35800 und der
     * Schredder. Sie haben Munition verbraucht und nichts getroffen.
     */
    public BulletBeamBase(LivingEntity entity, BulletConfig config, float baseDamage, float gunSpread, double sideOffset, double heightOffset, double frontOffset) {
        this(entity.level, config, baseDamage);
        this.thrower = entity;

        this.moveTo(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(),
                entity.yRot + (float) this.random.nextGaussian() * gunSpread,
                entity.xRot + (float) this.random.nextGaussian() * gunSpread);

        Vec3 offset = new Vec3(sideOffset, heightOffset, frontOffset);
        offset = offset.xRot(-this.xRot / 180F * (float) Math.PI);
        offset = offset.yRot(-this.yRot / 180F * (float) Math.PI);
        this.setPos(this.position().add(offset));

        this.setzeRichtungAusWinkeln();
        this.schussweg(REICHWEITE);
    }

    /** Die Reichweite eines Strahls, in Bloecken. Zahl des Originals. */
    public static final double REICHWEITE = 250D;

    /** Richtung aus Gier und Neigung, wie im Original Zeile fuer Zeile. */
    private void setzeRichtungAusWinkeln() {
        this.headingX = -Mth.sin(this.yRot / 180.0F * (float) Math.PI) * Mth.cos(this.xRot / 180.0F * (float) Math.PI);
        this.headingY = -Mth.sin(this.xRot / 180.0F * (float) Math.PI);
        this.headingZ = Mth.cos(this.yRot / 180.0F * (float) Math.PI) * Mth.cos(this.xRot / 180.0F * (float) Math.PI);
    }

    /** Umgekehrt: Gier und Neigung aus einem Richtungsvektor. Die NI4NI wird das brauchen. */
    public void setRotationsFromVector(Vec3 delta) {
        this.xRot = (float) (-Math.asin(delta.y / delta.length()) * 180D / Math.PI);
        this.yRot = (float) (-Math.atan2(delta.x, delta.z) * 180D / Math.PI);
        this.setzeRichtungAusWinkeln();
    }

    /** Streckt die Richtung auf die Reichweite und laeuft sie ab. */
    public void schussweg(double reichweite) {
        this.headingX *= reichweite;
        this.headingY *= reichweite;
        this.headingZ *= reichweite;
        this.performHitscan();
    }

    /**
     * Der Schussweg selbst: erst auf Bloecke, dann auf Wesen.
     *
     * DIE BEIDEN WEGE SIND NICHT GLEICH. Ein durchschlagender Strahl (doesPenetrate) trifft
     * JEDES Wesen auf der Strecke; ein gewoehnlicher nur das naechste. Deshalb wird im
     * ersten Fall sofort abgerechnet und im zweiten erst am Ende.
     *
     * DER KNICK AN EINER MUENZE hat Vorrang vor allem anderen: trifft der Strahl eine
     * geworfene Muenze, endet er dort und ein neuer beginnt -- siehe knickAnMuenze.
     */
    protected void performHitscan() {

        Vec3 pos = this.position();
        Vec3 nextPos = pos.add(this.headingX, this.headingY, this.headingZ);

        HitResult treffer = null;

        if(!this.isSpectral()) {
            BlockHitResult block = this.level.clip(new ClipContext(pos, nextPos,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if(block.getType() == HitResult.Type.BLOCK) {
                treffer = block;
                nextPos = block.getLocation();
            }
        }

        if(!this.level.isClientSide && this.doesImpactEntities()) {

            Entity naechstes = null;
            Vec3 naechsteStelle = null;
            double naechsteEntfernung = 0D;

            CoinEntity muenze = null;
            Vec3 muenzStelle = null;
            double muenzEntfernung = 0D;

            AABB suchraum = this.getBoundingBox().expandTowards(this.headingX, this.headingY, this.headingZ).inflate(1.0D);

            for(Entity wesen : this.level.getEntities(this, suchraum)) {
                if(!wesen.isAlive() || wesen == this.thrower || !wesen.isPickable()) continue;

                Optional<Vec3> stelle = wesen.getBoundingBox().inflate(0.3D).clip(pos, nextPos);
                if(stelle.isEmpty()) continue;

                double entfernung = pos.distanceTo(stelle.get());

                /* Die Muenzen werden SEPARAT gesammelt, weil sie den Strahl umlenken statt
                 * ihn zu beenden -- und weil auch ein durchschlagender Strahl an ihnen
                 * abknickt, statt weiterzulaufen. */
                if(wesen instanceof CoinEntity getroffene) {
                    if(muenze == null || entfernung < muenzEntfernung) {
                        muenze = getroffene;
                        muenzEntfernung = entfernung;
                        muenzStelle = stelle.get();
                    }
                    continue;
                }

                if(this.doesPenetrate()) {
                    /* Hinter der Muenze wird nicht mehr abgerechnet: dort endet dieser Strahl. */
                    if(muenze == null || entfernung < muenzEntfernung) this.onImpact(new EntityHitResult(wesen, stelle.get()));
                } else if(naechstes == null || entfernung < naechsteEntfernung) {
                    naechstes = wesen;
                    naechsteEntfernung = entfernung;
                    naechsteStelle = stelle.get();
                }
            }

            if(muenze != null) {
                this.beamLength = pos.distanceTo(muenzStelle);
                this.knickAnMuenze(muenze, muenzStelle);
                return;
            }

            if(!this.doesPenetrate() && naechstes != null) treffer = new EntityHitResult(naechstes, naechsteStelle);
        }

        if(treffer != null) this.onImpact(treffer);

        /* Die Laenge geht an den Zeichner, deshalb wird sie IMMER gesetzt -- auch wenn der
         * Strahl nichts getroffen hat und bis ans Ende der Reichweite laeuft. */
        this.beamLength = pos.distanceTo(treffer != null ? treffer.getLocation() : nextPos);
    }

    /**
     * Der Knick. Die Muenze zerspringt, und von ihrer Stelle aus geht ein NEUER Strahl los --
     * mit einem Viertel mehr Schaden, damit sich das Kunststueck lohnt.
     *
     * DIE RANGFOLGE DES ZIELS IST FEST: eine andere Muenze zuerst, dann ein Spieler, dann ein
     * Monster, dann irgendetwas. Damit laesst sich eine Kette aus mehreren Muenzen bauen, und
     * genau das ist der Witz der Waffe. Findet sich in fuenfzig Bloecken gar nichts, faellt
     * der neue Strahl schraeg nach unten ins Leere.
     */
    private void knickAnMuenze(CoinEntity muenze, Vec3 stelle) {

        double reichweite = 50D;
        AABB umkreis = new AABB(stelle, stelle).inflate(reichweite);

        Entity naechsteMuenze = null, naechsterSpieler = null, naechstesMonster = null, naechstesSonst = null;
        double dMuenze = 0D, dSpieler = 0D, dMonster = 0D, dSonst = 0D;

        for(Entity wesen : this.level.getEntities((Entity) null, umkreis, e -> true)) {
            if(wesen == this.thrower || wesen == muenze || !wesen.isAlive()) continue;

            double entfernung = wesen.distanceTo(muenze);
            if(entfernung > reichweite) continue;

            if(wesen instanceof CoinEntity) {
                if(naechsteMuenze == null || entfernung < dMuenze) { dMuenze = entfernung; naechsteMuenze = wesen; }
            } else if(wesen instanceof Player) {
                if(naechsterSpieler == null || entfernung < dSpieler) { dSpieler = entfernung; naechsterSpieler = wesen; }
            } else if(wesen instanceof Monster) {
                if(naechstesMonster == null || entfernung < dMonster) { dMonster = entfernung; naechstesMonster = wesen; }
            } else {
                if(naechstesSonst == null || entfernung < dSonst) { dSonst = entfernung; naechstesSonst = wesen; }
            }
        }

        Entity ziel = naechsteMuenze != null ? naechsteMuenze
                : naechsterSpieler != null ? naechsterSpieler
                : naechstesMonster != null ? naechstesMonster
                : naechstesSonst;

        muenze.discard();

        LivingEntity urheber = muenze.getOwner() instanceof LivingEntity werfer ? werfer : this.thrower;
        BulletBeamBase neuer = new BulletBeamBase(this.level, this.config, this.damage * 1.25F);
        neuer.thrower = urheber;
        neuer.setPos(stelle);

        Vec3 richtung = ziel != null
                ? new Vec3(ziel.getX() - stelle.x, (ziel.getY() + ziel.getBbHeight() / 2D) - stelle.y, ziel.getZ() - stelle.z)
                : new Vec3(this.random.nextGaussian() * 0.5D, -1D, this.random.nextGaussian() * 0.5D);

        neuer.setRotationsFromVector(richtung);
        neuer.schussweg(REICHWEITE);
        this.level.addFreshEntity(neuer);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double perimeter = this.getBoundingBox().getSize();
        if(Double.isNaN(perimeter)) perimeter = 1.0;
        perimeter *= 64.0 * 10.0;
        return distance < perimeter * perimeter;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BULLET_CONFIG, 0);
    }

    public void setBulletConfig(BulletConfig config) {
        this.config = config;
        this.getEntityData().set(BULLET_CONFIG, config.id);
    }

    @Nullable
    public BulletConfig getBulletConfig() {
        int id = this.getEntityData().get(BULLET_CONFIG);
        if(id < 0 || id > BulletConfig.configs.size()) return null;
        return BulletConfig.configs.get(id);
    }

    @Override
    public void tick() {
        if(config == null) config = this.getBulletConfig();

        if(config == null) {
            this.discard();
            return;
        }

        if(config.onUpdate != null) config.onUpdate.accept(this);

        /* super, nicht this: das Original ruft hier super.onUpdate(). Ein this.tick() ruft sich
         * selbst und bricht beim ersten Tick mit einem Stapelueberlauf ab. */
        super.tick();

        if(!level.isClientSide && this.tickCount > config.expires) this.discard();
    }

    protected void onImpact(HitResult hr) {
        if(!level.isClientSide) {
            if(this.config.onImpactBeam != null) this.config.onImpactBeam.accept(this, hr);
        }
    }

    public boolean doesImpactEntities() { return this.config.impactsEntities; }
    public boolean doesPenetrate() { return this.config.doesPenetrate; }
    public boolean isSpectral() { return this.config.isSpectral; }

    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override public boolean save(CompoundTag tag) { return false; }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { this.discard(); }

    @Override public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        buf.writeDouble(beamLength);
        buf.writeFloat(this.yRot);
        buf.writeFloat(this.xRot);
    }

    @Override public void readSpawnData(RegistryFriendlyByteBuf buf) {
        this.beamLength = buf.readDouble();
        this.yRot = buf.readFloat();
        this.xRot = buf.readFloat();
    }
}
