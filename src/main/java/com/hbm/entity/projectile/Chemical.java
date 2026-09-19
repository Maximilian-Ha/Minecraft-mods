package com.hbm.entity.projectile;

import com.hbm.blocks.NtmBlocks;
import com.hbm.blockentity.IRepairable;
import com.hbm.blockentity.IRepairable.EnumExtinguishType;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.extprop.HbmLivingAttachments;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FT_Pheromone;
import com.hbm.inventory.fluid.trait.FT_Poison;
import com.hbm.inventory.fluid.trait.FT_Toxin;
import com.hbm.inventory.fluid.trait.FT_VentRadiation;
import com.hbm.particle.helper.FlameCreator;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.EntityDamageUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.projectile.EntityChemical.
 *
 * Die Chemikalienwolke des Chemiewerfers: ein Spritzer Fluessigkeit, ein Schwall Gas oder eine
 * Stichflamme -- was davon, entscheidet nicht die Waffe, sondern der Inhalt des Tanks.
 *
 * WIE DIE ART AUS DEN EIGENSCHAFTEN FOLGT (Kommentar des Originals, sinngemaess):
 * - Antimaterie: alles andere zaehlt nicht, es wird ein Gammastrahl ohne Schwerkraft
 * - heiss: zuendet an und macht zusaetzlichen Feuerschaden, mit der Temperatur steigend
 * - kalt: friert ein, die Dauer mit der Temperatur
 * - gasfoermig oder verdampfend: kurze Reichweite, dafuer breiter werdend
 * - fluessig: schmaler Strahl, weit, der Schwerkraft folgend
 * - brennbar als Gas: Stichflamme; brennbar als Fluessigkeit: traenkt, statt zu brennen
 * - aetzend: zusaetzlicher Saeureschaden, Gift und Ruestungsverschleiss
 *
 * NICHT UEBERNOMMEN, jeweils mit Grund:
 * - der Glyphid-Zweig des Pheromons. Glyphiden gibt es im Port nicht; die Wirkung auf Spieler
 *   und alle uebrigen Lebewesen bleibt.
 * - die Blockumwandlungen der Saatbruehe fuer Stufen und Platten. Sie haengen im Original an
 *   Metadaten, die es in 1.21 nicht mehr gibt; Erde, Kopfstein, Steinziegel, Brachland und
 *   Betonziegel werden umgewandelt wie dort.
 * - der Farbstaub des Originals lief ueber einen eigenen Partikelmodus ("colordust"), den der
 *   Port nicht kennt. Hier steht der gewoehnliche gefaerbte Staub, in der Farbe des Fluids.
 */
public class Chemical extends ThrowableNT {

    private static final EntityDataAccessor<Integer> FLUID = SynchedEntityData.defineId(Chemical.class, EntityDataSerializers.INT);

    public Chemical(EntityType<? extends Chemical> type, Level level) {
        super(type, level);
    }

    public Chemical(Level level) {
        this(NtmEntityTypes.CHEMICAL.get(), level);
    }

    /**
     * Der Schuss aus der Waffe. Der Versatz ist der des Laufs, in den Blickwinkel des
     * Schuetzen gedreht -- dieselbe Rechnung wie bei den Geschossen, damit die Wolke aus der
     * Duese kommt und nicht aus dem Gesicht.
     */
    public Chemical(LivingEntity werfer, double sideOffset, double heightOffset, double frontOffset, float spread) {
        this(werfer.level);

        this.setOwner(werfer);
        this.moveTo(werfer.getX(), werfer.getY() + werfer.getEyeHeight(), werfer.getZ(), werfer.yRot, werfer.xRot);

        Vec3 versatz = new Vec3(sideOffset, heightOffset, frontOffset)
                .xRot(-this.getXRot() / 180F * (float) Math.PI)
                .yRot(-this.getYRot() / 180F * (float) Math.PI);
        this.setPos(this.position().add(versatz));

        float xd = -Mth.sin(this.getYRot() / 180.0F * (float) Math.PI) * Mth.cos(this.getXRot() / 180.0F * (float) Math.PI);
        float yd = -Mth.sin(this.getXRot() / 180.0F * (float) Math.PI);
        float zd = Mth.cos(this.getYRot() / 180.0F * (float) Math.PI) * Mth.cos(this.getXRot() / 180.0F * (float) Math.PI);
        this.shoot(xd, yd, zd, 1F, spread);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLUID, 0);
    }

    public Chemical setFluid(FluidType fluid) {
        this.entityData.set(FLUID, fluid.getID());
        return this;
    }

    public FluidType getType() {
        return Fluids.fromID(this.entityData.get(FLUID));
    }

    @Override
    public void tick() {

        if(!this.level.isClientSide) {

            if(this.tickCount > this.getMaxAge()) this.discard();

            FluidType type = this.getType();

            /*
             * Gas und Dampf wirken nicht erst beim Aufschlag, sondern im Vorbeiziehen -- und
             * ihre Wirkung laesst nach, je aelter die Wolke ist.
             */
            if(type.hasTrait(Fluids.GASEOUS.getClass()) || type.hasTrait(Fluids.EVAP.getClass())) {

                double staerke = 1D - (double) this.tickCount / (double) this.getMaxAge();
                AABB bereich = this.getBoundingBox().inflate(staerke * 2.5D);

                for(Entity e : this.level.getEntities(this.getOwner(), bereich)) this.affect(e, staerke);
            }

        } else {

            FluidType type = this.getType();
            ChemicalStyle art = this.getStyle();

            if(type == Fluids.BALEFIRE) {
                FlameCreator.composeEffectClient(this.getX(), this.getY() - 0.125D, this.getZ(), FlameCreator.META_BALEFIRE);

            } else if(art == ChemicalStyle.LIQUID) {
                int farbe = type.getColor();
                this.level.addParticle(new DustParticleOptions(
                                new Vector3f(((farbe >> 16) & 0xFF) / 255F, ((farbe >> 8) & 0xFF) / 255F, (farbe & 0xFF) / 255F), 1F),
                        this.getX(), this.getY(), this.getZ(),
                        this.getDeltaMovement().x + this.random.nextGaussian() * 0.05D,
                        this.getDeltaMovement().y - 0.2D + this.random.nextGaussian() * 0.05D,
                        this.getDeltaMovement().z + this.random.nextGaussian() * 0.05D);

            } else if(art == ChemicalStyle.BURNING) {
                FlameCreator.composeEffectClient(this.getX(), this.getY() - 0.125D, this.getZ(), FlameCreator.META_FIRE);
            }
        }

        super.tick();
    }

    protected void affect(Entity e, double staerke) {

        ChemicalStyle art = this.getStyle();
        FluidType type = this.getType();
        LivingEntity lebendig = e instanceof LivingEntity living ? living : null;

        /* Fluessigkeiten treffen mit voller Wucht, egal wie weit sie geflogen sind. */
        if(art == ChemicalStyle.LIQUID || art == ChemicalStyle.BURNING) staerke = 1D;

        if(art == ChemicalStyle.AMAT) {
            EntityDamageUtil.hurtIgnoreIFrame(e, this.damageSources().source(NtmDamageTypes.RADIATION), 1F);
            if(lebendig != null) {
                ContaminationUtil.contaminate(lebendig, HazardType.RADIATION, ContaminationType.CREATIVE, 50F * (float) staerke);
                return;
            }
        }

        if(art == ChemicalStyle.LIGHTNING) {
            EntityDamageUtil.hurtIgnoreIFrame(e, this.damageSources().source(NtmDamageTypes.ELECTRICITY), 0.5F);
            if(lebendig != null) {
                lebendig.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9));
                lebendig.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 9));
                return;
            }
        }

        /* Ein Viertel Schaden bei 100 Grad, je weitere tausend Grad ein Punkt mehr. */
        if(type.temperature >= 100) {
            EntityDamageUtil.hurtIgnoreIFrame(e, this.damageSources().source(NtmDamageTypes.FIRE),
                    Math.min(0.25F + (type.temperature - 100) * 0.001F, 15F));
            if(type.temperature >= 500) e.setRemainingFireTicks(10 * 20);
        }

        if(art == ChemicalStyle.LIQUID || art == ChemicalStyle.GAS) {

            if(type.temperature < -20 && lebendig != null) {
                EntityDamageUtil.hurtIgnoreIFrame(e, this.damageSources().source(NtmDamageTypes.FIRE),
                        Math.min(0.25F + (-type.temperature) * 0.01F, 2F));
            }

            if(type.hasTrait(Fluids.DELICIOUS.getClass()) && lebendig != null && lebendig.isAlive()) {
                lebendig.heal(2F * (float) staerke);
            }
        }

        /* Brennbares in fluessiger Form zuendet nicht an, es traenkt -- fuenfzehn Sekunden. */
        if(art == ChemicalStyle.LIQUID && type.hasTrait(FT_Flammable.class) && lebendig != null) {
            HbmLivingAttachments.setOil(lebendig, 300);
        }

        if(this.isExtinguishing()) e.clearFire();

        if(art == ChemicalStyle.BURNING) {
            FT_Combustible brennbar = type.getTrait(FT_Combustible.class);
            EntityDamageUtil.hurtIgnoreIFrame(e, this.damageSources().source(NtmDamageTypes.FIRE),
                    0.2F + (brennbar != null ? Math.min(brennbar.getCombustionEnergy() / 100_000F, 15F) : 0F));
            e.setRemainingFireTicks(5 * 20);
        }

        if(art == ChemicalStyle.GASFLAME) {
            FT_Flammable entzuendlich = type.getTrait(FT_Flammable.class);
            FT_Combustible brennbar = type.getTrait(FT_Combustible.class);

            float hitze = Math.max(
                    entzuendlich != null ? entzuendlich.getHeatEnergy() / 50_000F : 0F,
                    brennbar != null ? Math.min(brennbar.getCombustionEnergy() / 100_000F, 15F) : 0F);
            hitze *= staerke;

            EntityDamageUtil.hurtIgnoreIFrame(e, this.damageSources().source(NtmDamageTypes.FIRE), (0.2F + hitze) * (float) staerke);
            e.setRemainingFireTicks((int) Math.ceil(5 * staerke) * 20);
        }

        if(type.hasTrait(FT_Corrosive.class) && lebendig != null) {
            FT_Corrosive aetzend = type.getTrait(FT_Corrosive.class);
            EntityDamageUtil.hurtIgnoreIFrame(lebendig, this.damageSources().source(NtmDamageTypes.ACID), aetzend.getRating() / 50F);
            for(EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                ArmorUtil.damageSuit(lebendig, slot, aetzend.getRating() / 40);
            }
        }

        if(type.hasTrait(FT_VentRadiation.class)) {
            FT_VentRadiation strahlend = type.getTrait(FT_VentRadiation.class);
            if(lebendig != null) ContaminationUtil.contaminate(lebendig, HazardType.RADIATION, ContaminationType.CREATIVE, strahlend.getRadPerMB() * 5);
            ChunkRadiationManager.proxy.incrementRad(this.level, e.blockPosition(), strahlend.getRadPerMB() * 5);
        }

        if(type.hasTrait(FT_Poison.class) && lebendig != null) {
            FT_Poison gift = type.getTrait(FT_Poison.class);
            lebendig.addEffect(new MobEffectInstance(gift.isWithering() ? MobEffects.WITHER : MobEffects.POISON, (int) (5 * 20 * staerke)));
        }

        if(type.hasTrait(FT_Toxin.class) && lebendig != null) {
            type.getTrait(FT_Toxin.class).affect(lebendig, staerke);
        }

        if(type.hasTrait(FT_Pheromone.class) && lebendig != null) {
            FT_Pheromone pheromon = type.getTrait(FT_Pheromone.class);
            lebendig.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2 * 60 * 20, 2));
            lebendig.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5 * 60 * 20, 1));
            lebendig.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 2 * 60 * 20, 4));

            if(lebendig instanceof Player && pheromon.getType() == 2) {
                lebendig.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2 * 60 * 20, 2));
            }
        }

        if(type == Fluids.XPJUICE && e instanceof Player spieler) {
            spieler.giveExperiencePoints(1);
            this.discard();
        }

        if(type == Fluids.ENDERJUICE) this.teleportRandomly(e);
    }

    /** Ob diese Sorte Feuer loescht: kalte, nicht brennbare Fluessigkeit. */
    protected boolean isExtinguishing() {
        return this.getStyle() == ChemicalStyle.LIQUID && this.getType().temperature < 50 && !this.getType().hasTrait(FT_Flammable.class);
    }

    /** Womit brennende Mehrblockbauten geloescht werden -- wie beim Feuerloescher. */
    protected EnumExtinguishType getExtinguishingType(FluidType type) {
        if(type == Fluids.CARBONDIOXIDE) return EnumExtinguishType.CO2;
        if(type == Fluids.WATER || type == Fluids.HEAVYWATER || type == Fluids.COOLANT) return EnumExtinguishType.WATER;
        return null;
    }

    /** Enderbruehe wirft den Getroffenen irgendwohin -- bis zu 32 Bloecke in jede Richtung. */
    public void teleportRandomly(Entity e) {
        double x = this.getX() + (this.random.nextDouble() - 0.5D) * 64.0D;
        double y = this.getY() + (this.random.nextInt(64) - 32);
        double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 64.0D;
        e.teleportTo(x, y, z);
    }

    @Override
    protected void onImpact(HitResult treffer) {

        if(this.level.isClientSide) return;

        if(treffer instanceof EntityHitResult wesenTreffer) {
            this.affect(wesenTreffer.getEntity(), 1D - (double) this.tickCount / (double) this.getMaxAge());
            return;
        }

        if(!(treffer instanceof BlockHitResult blockTreffer)) return;

        FluidType type = this.getType();
        BlockPos stelle = blockTreffer.getBlockPos();
        ChemicalStyle art = this.getStyle();

        if(type.hasTrait(FT_VentRadiation.class)) {
            ChunkRadiationManager.proxy.incrementRad(this.level, stelle, type.getTrait(FT_VentRadiation.class).getRadPerMB() * 5);
        }

        if(art == ChemicalStyle.BURNING || art == ChemicalStyle.GASFLAME) {
            BlockState feuer = (type == Fluids.BALEFIRE ? NtmBlocks.BALEFIRE.get() : Blocks.FIRE).defaultBlockState();
            for(Direction seite : Direction.values()) {
                BlockPos nachbar = stelle.relative(seite);
                if(this.level.getBlockState(nachbar).isAir()) this.level.setBlockAndUpdate(nachbar, feuer);
            }
        }

        if(this.isExtinguishing()) {
            for(Direction seite : Direction.values()) {
                BlockPos nachbar = stelle.relative(seite);
                if(this.level.getBlockState(nachbar).is(Blocks.FIRE)) this.level.removeBlock(nachbar, false);
            }
        }

        EnumExtinguishType loeschart = this.getExtinguishingType(type);
        if(loeschart != null) {

            BlockEntity kern = this.level.getBlockEntity(stelle);
            if(kern instanceof IRepairable reparierbar) reparierbar.tryExtinguish(this.level, stelle, loeschart);

            /* Wasser waescht den Fallout ab -- fuenf mal zwei mal fuenf Bloecke weit. */
            if(loeschart == EnumExtinguishType.WATER && art == ChemicalStyle.LIQUID) {
                for(int i = -2; i <= 2; i++) for(int j = 0; j <= 1; j++) for(int k = -2; k <= 2; k++) {
                    BlockPos nahe = stelle.offset(i, j, k);
                    if(this.level.getBlockState(nahe).is(NtmBlocks.FALLOUT.get())) this.level.removeBlock(nahe, false);
                }
            }
        }

        if(type == Fluids.SEEDSLURRY) this.begruene(stelle);

        this.discard();
    }

    /** Saatbruehe macht aus totem Boden wieder Gras und setzt Moos an Stein. */
    private void begruene(BlockPos mitte) {

        for(int i = -1; i <= 1; i++) for(int j = -1; j <= 1; j++) for(int k = -1; k <= 1; k++) {

            BlockPos stelle = mitte.offset(i, j, k);
            BlockState zustand = this.level.getBlockState(stelle);

            if(zustand.is(Blocks.DIRT) || zustand.is(NtmBlocks.DIRT_DEAD.get()) || zustand.is(NtmBlocks.DIRT_OILY.get())) {
                if(this.level.getMaxLocalRawBrightness(stelle.above()) >= 9) {
                    this.level.setBlockAndUpdate(stelle, Blocks.GRASS_BLOCK.defaultBlockState());
                }
            }

            if(zustand.is(Blocks.COBBLESTONE)) this.level.setBlockAndUpdate(stelle, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            if(zustand.is(Blocks.STONE_BRICKS)) this.level.setBlockAndUpdate(stelle, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            if(zustand.is(NtmBlocks.WASTE_EARTH.get())) this.level.setBlockAndUpdate(stelle, Blocks.GRASS_BLOCK.defaultBlockState());
            if(zustand.is(NtmBlocks.BRICK_CONCRETE.get())) this.level.setBlockAndUpdate(stelle, NtmBlocks.BRICK_CONCRETE_MOSSY.get().defaultBlockState());
        }
    }

    @Override
    protected float getAirDrag() {
        return switch(this.getStyle()) {
            case AMAT, LIGHTNING -> 1F;
            case GAS -> 0.95F;
            default -> 0.99F;
        };
    }

    @Override
    protected float getWaterDrag() {
        return switch(this.getStyle()) {
            case AMAT, LIGHTNING, GAS -> 1F;
            default -> 0.8F;
        };
    }

    public int getMaxAge() {
        return switch(this.getStyle()) {
            case AMAT -> 100;
            case LIGHTNING -> 5;
            case BURNING -> 600;
            case GAS -> 60;
            case GASFLAME -> 20;
            case LIQUID -> 600;
            default -> 100;
        };
    }

    @Override
    protected double getGravityVelocity() {
        return switch(this.getStyle()) {
            case AMAT, LIGHTNING, GAS -> 0D;
            case GASFLAME -> -0.01D;
            default -> 0.03D;
        };
    }

    @Override public boolean fireImmune() { return true; }

    public ChemicalStyle getStyle() {
        return getStyleFromType(this.getType());
    }

    public static ChemicalStyle getStyleFromType(FluidType type) {

        if(type == Fluids.IONGEL) return ChemicalStyle.LIGHTNING;
        if(type.isAntimatter()) return ChemicalStyle.AMAT;

        if(type.hasTrait(Fluids.GASEOUS.getClass()) || type.hasTrait(Fluids.EVAP.getClass())) {
            return type.hasTrait(FT_Flammable.class) || type.hasTrait(FT_Combustible.class)
                    ? ChemicalStyle.GASFLAME : ChemicalStyle.GAS;
        }

        if(type.hasTrait(Fluids.LIQUID.getClass())) {
            return type.hasTrait(FT_Combustible.class) ? ChemicalStyle.BURNING : ChemicalStyle.LIQUID;
        }

        return ChemicalStyle.NULL;
    }

    /** Die grobe Art der Chemikalie. Sie entscheidet ueber Bild und Flugbahn. */
    public enum ChemicalStyle {
        AMAT,
        LIGHTNING,
        LIQUID,
        GAS,
        GASFLAME,
        BURNING,
        NULL
    }
}
