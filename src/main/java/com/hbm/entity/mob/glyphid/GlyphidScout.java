package com.hbm.entity.mob.glyphid;

import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.GlyphidBaseBlock;
import com.hbm.entity.logic.Waypoint;
import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.main.ResourceManager;
import com.hbm.util.Vec3NT;
import com.hbm.world.feature.GlyphidHive;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.glyphid.EntityGlyphidScout.
 *
 * Der Spaeher kaempft nicht, er sucht Bauplaetze. Er ist der Grund, warum ein Glyphidenbefall
 * sich ausbreitet statt an einer Stelle zu bleiben -- und er ist der kleinste der Familie.
 *
 * WIE EIN NEUER BAU ENTSTEHT, in fuenf Schritten:
 * 1. Er wuerfelt eine Stelle im Umkreis von fuenfundvierzig Bloecken um sein Zuhause,
 *    prueft, dass sie weit genug von zu Hause weg ist und dass dort fester Boden liegt, der
 *    noch kein Baufleisch ist.
 * 2. Ist die Stelle brauchbar, setzt er dort einen Merkpunkt und ruft seine Artgenossen.
 * 3. Am Ziel angekommen prueft er acht Richtungen ab, ob in acht Bloecken Umkreis schon ein
 *    Bau steht -- wenn ja, gibt er auf und geht in den Leerlauf.
 * 4. Beim ersten Zaehlschritt schickt er alle nach Hause, um Verstaerkung zu holen, und
 *    legt sich selbst Langsamkeit XI auf: er wartet.
 * 5. Beim fuenften sprengt er sich selbst und HINTERLAESST DEN BAU. Er ueberlebt das nicht.
 *
 * AUF BASALT BAUT ER GROSS: er merkt sich das, wird schneller, und der Merkpunkt bekommt
 * Vorrang. (Der grosse Bau selbst fehlt dem Original als Bauweg -- generateSmall ist alles,
 * was es gibt; useLargeHive aendert nur den Sicherheitsabstand und den Vorrang.)
 *
 * STEHT EIN BIG MAN JOHNSON NEBEN IHM, schaltet er auf Umformen um: er sucht dann weiter weg
 * (sechzig statt fuenfundvierzig) und haelt mehr Abstand (zwanzig statt acht).
 *
 * SEIN BISS VERGIFTET, und das ist Absicht -- das Original nennt es "extreme measures for
 * anti-scout bullying". Wer den Spaeher jagt, soll es merken.
 *
 * NICHT UEBERNOMMEN: der Rampant-Zweig, der den Spaeher gezielt auf den Spielerstuetzpunkt
 * zulaufen laesst. Er haengt an PollutionHandler.targetCoords, das der Port nicht hat; ohne
 * diese Koordinate faellt das Original selbst in den einfachen Zweig zurueck, und genau der
 * steht hier.
 */
public class GlyphidScout extends Glyphid {

    private boolean hatZiel = false;
    private int zaehler;
    private int suchweite = 45;
    private int mindestAbstandZumBau = 8;
    private boolean grosserBau = false;

    public GlyphidScout(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        StatBundle stats = GlyphidStats.getStats().scout;
        return Glyphid.createAttributes()
                .add(Attributes.MAX_HEALTH, stats.health())
                .add(Attributes.MOVEMENT_SPEED, stats.speed())
                .add(Attributes.ATTACK_DAMAGE, stats.damage());
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.GLYPHID_SCOUT_TEX;
    }

    @Override
    public double getGlyphidScale() {
        return 0.75D;
    }

    @Override
    public StatBundle getStats() {
        return GlyphidStats.getStats().scout;
    }

    @Override
    public boolean istSpaeher() {
        return true;
    }

    /** "extreme measures for anti-scout bullying" -- wer ihn jagt, wird vergiftet. */
    @Override
    public boolean doHurtTarget(Entity opfer) {
        if(!super.doHurtTarget(opfer)) return false;
        if(opfer instanceof LivingEntity lebendig) {
            lebendig.addEffect(new MobEffectInstance(MobEffects.POISON, 10 * 20, 3));
        }
        return true;
    }

    @Override
    public boolean isArmorBroken(float schaden) {
        return this.random.nextInt(100) <= Math.min(Math.pow(schaden, 2), 100);
    }

    /** Er sieht nicht ueber die ganze Karte -- er soll bauen, nicht jagen. */
    @Override
    public boolean useExtendedTargeting() {
        return false;
    }

    /** Nur wenn er baut, zaehlt das Ankommen. */
    @Override
    public boolean isAtDestination() {
        return this.getCurrentTask() == TASK_BUILD_HIVE && super.isAtDestination();
    }

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide) return;

        if(this.getCurrentTask() != TASK_BUILD_HIVE && this.getCurrentTask() != TASK_TERRAFORM) {
            if(this.getWaypoint() == null) this.setCurrentTask(TASK_BUILD_HIVE, null);
            return;
        }

        if(!this.hatZiel) {

            /* Steht ein Big Man Johnson daneben, formt er stattdessen die Landschaft um. */
            if(this.suchweite != 60 && this.johnsonInDerNaehe()) {
                this.setCurrentTask(TASK_TERRAFORM, null);
            }

            if(this.expandHive()) {
                this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 180 * 20, 1));
                this.hatZiel = true;
            }
        }

        /* Randfall des Originals: kein Merkpunkt, aber hatZiel steht noch. */
        if(this.getWaypoint() == null && this.hatZiel) this.hatZiel = false;

        if(this.getCurrentTask() == TASK_TERRAFORM && super.isAtDestination() && this.kannHierBauen()) {
            this.communicate(TASK_TERRAFORM, this.getWaypoint());
        }

        if(this.tickCount % 10 != 0 || !this.isAtDestination()) return;

        this.zaehler++;

        if(!this.kannHierBauen()) return;

        if(this.zaehler == 1) {
            this.verstaerkungHolen();
        } else if(this.zaehler >= 5) {
            this.bauHinterlassen();
        } else {
            this.communicate(TASK_FOLLOW, this.getWaypoint());
        }
    }

    /** Schritt 4: alle nach Hause schicken und selbst warten. */
    private void verstaerkungHolen() {

        Waypoint zurueck = new Waypoint(this.level());
        zurueck.moveTo(this.getX(), this.getY(), this.getZ(), 0, 0);
        zurueck.setWaypointType(TASK_IDLE);

        Waypoint daheim = new Waypoint(this.level());
        daheim.setWaypointType(TASK_RETREAT_FOR_REINFORCEMENTS);
        daheim.setAdditionalWaypoint(zurueck);
        daheim.moveTo(this.home.getX(), this.home.getY(), this.home.getZ(), 0, 0);
        daheim.maxAge = 1200;
        daheim.radius = 6;
        this.level().addFreshEntity(daheim);

        this.setCurrentTask(TASK_RETREAT_FOR_REINFORCEMENTS, daheim);
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40 * 20, 10));
        this.communicate(TASK_RETREAT_FOR_REINFORCEMENTS, daheim);
    }

    /** Schritt 5: er sprengt sich und laesst den Bau zurueck. */
    private void bauHinterlassen() {

        this.level().explode(this, this.getX(), this.getY(), this.getZ(), 5F, Level.ExplosionInteraction.NONE);

        GlyphidHive.generateSmall(this.level(), this.blockPosition(), this.random,
                this.getSubtype() != TYPE_NORMAL, false);

        this.discard();
    }

    /**
     * Acht Richtungen absuchen: steht in Reichweite schon Baufleisch, ist hier kein Platz.
     * Der Abstand ist beim grossen Bau doppelt so weit.
     *
     * DIE ACHT RICHTUNGEN SIND NICHT GLEICHMAESSIG VERTEILT: das Original dreht um
     * 360/16 Grad je Schritt, laeuft also nur einen HALBEN Kreis ab. Uebernommen, wie es
     * dasteht -- wer auf 360/8 geht, prueft andere Richtungen als das Original.
     */
    public boolean kannHierBauen() {

        int weite = this.grosserBau ? 16 : 8;

        for(int i = 0; i < 8; i++) {

            double winkel = Math.toRadians(360D / 16 * i);
            Vec3NT richtung = new Vec3NT(0, 0, weite).rotateAroundYRad(winkel);

            Vec3 von = new Vec3(this.getX(), this.getY() + 1, this.getZ());
            Vec3 nach = new Vec3(this.getX() + richtung.xCoord, this.getY() + 1, this.getZ() + richtung.zCoord);

            BlockHitResult treffer = this.level().clip(new ClipContext(von, nach,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));

            if(treffer.getType() != HitResult.Type.BLOCK) continue;

            if(this.level().getBlockState(treffer.getBlockPos()).getBlock() instanceof GlyphidBaseBlock) {
                this.setCurrentTask(TASK_IDLE, null);
                this.hatZiel = false;
                return false;
            }
        }

        return true;
    }

    /** Ob ein Big Man Johnson im Umkreis von acht Bloecken steht. */
    public boolean johnsonInDerNaehe() {

        AABB kasten = new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ())
                .inflate(8, 8, 8);

        for(Entity e : this.level().getEntities(this, kasten)) {
            if(e instanceof GlyphidNuclear) return true;
        }

        return false;
    }

    /** Schritt 1 bis 2: eine Stelle wuerfeln, pruefen, und bei Erfolg den Merkpunkt setzen. */
    @Override
    public boolean expandHive() {

        int nestX = this.home.getX() - this.suchweite + this.random.nextInt(this.suchweite * 2);
        int nestZ = this.home.getZ() - this.suchweite + this.random.nextInt(this.suchweite * 2);
        int nestY = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, nestX, nestZ);

        BlockPos boden = new BlockPos(nestX, nestY - 1, nestZ);
        var zustand = this.level().getBlockState(boden);

        double abstand = new Vec3NT(nestX - this.home.getX(), nestY - this.home.getY(), nestZ - this.home.getZ()).length();
        if(abstand <= this.mindestAbstandZumBau) return false;

        if(zustand.isAir()) return false;
        if(!zustand.isSolidRender(this.level(), boden)) return false;
        if(zustand.getBlock() instanceof GlyphidBaseBlock) return false;

        /* Auf Basalt baut er gross -- und wird schneller, um schnell hinzukommen. */
        if(zustand.is(NtmBlocks.BASALT.get())) {
            this.grosserBau = true;
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 * 20, 3));
        }

        Waypoint nest = new Waypoint(this.level());
        nest.setWaypointType(this.getCurrentTask());
        nest.radius = 5;
        if(this.grosserBau) nest.setHighPriority();
        nest.moveTo(nestX, nestY, nestZ, 0, 0);
        this.level().addFreshEntity(nest);

        this.setCurrentTask(this.getCurrentTask(), nest);
        this.communicate(TASK_BUILD_HIVE, nest);

        return true;
    }

    @Override
    public void carryOutTask() {

        if(!this.level().isClientSide && this.getWaypoint() == null) {

            switch(this.getCurrentTask()) {

                case TASK_INITIATE_RETREAT -> {
                    this.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 20, 4));

                    Waypoint zurueck = new Waypoint(this.level());
                    zurueck.moveTo(this.getX(), this.getY(), this.getZ(), 0, 0);
                    zurueck.setWaypointType(TASK_IDLE);

                    Waypoint daheim = new Waypoint(this.level());
                    daheim.setWaypointType(TASK_BUILD_HIVE);
                    daheim.setAdditionalWaypoint(zurueck);
                    daheim.setHighPriority();
                    daheim.radius = 6;
                    daheim.moveTo(this.home.getX(), this.home.getY(), this.home.getZ(), 0, 0);
                    this.level().addFreshEntity(daheim);

                    this.communicate(TASK_FOLLOW, daheim);
                }

                /* Umformen: er sucht weiter weg und haelt mehr Abstand. */
                case TASK_TERRAFORM -> {
                    this.suchweite = 60;
                    this.mindestAbstandZumBau = 20;
                }

                default -> { }
            }
        }

        super.carryOutTask();
    }
}
