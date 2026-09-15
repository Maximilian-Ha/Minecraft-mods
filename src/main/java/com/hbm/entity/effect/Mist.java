package com.hbm.entity.effect;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FT_Poison;
import com.hbm.inventory.fluid.trait.FT_Toxin;
import com.hbm.inventory.fluid.trait.FT_VentRadiation;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous_ART;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Liquid;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Viscous;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.particle.ParticleUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.effect.EntityMist.
 *
 * Eine stehende Wolke aus einem Fluid. Was sie anrichtet, steht nicht in ihr, sondern in den
 * Eigenschaften des Fluids: aetzend, radioaktiv, giftig. Sie ist der Abnehmer, ohne den
 * FT_Toxin toter Code waere.
 *
 * Nicht portiert ist der Pheromon-Zweig: er wirkt nur auf Glyphiden, die es im Port nicht gibt.
 */
public class Mist extends Entity {

    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(Mist.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> AREA_WIDTH = SynchedEntityData.defineId(Mist.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AREA_HEIGHT = SynchedEntityData.defineId(Mist.class, EntityDataSerializers.FLOAT);

    public int maxAge = 150;

    public Mist(EntityType<? extends Mist> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public Mist(Level level) {
        this(NtmEntityTypes.MIST.get(), level);
    }

    public Mist setArea(float width, float height) {
        this.entityData.set(AREA_WIDTH, width);
        this.entityData.set(AREA_HEIGHT, height);
        this.refreshDimensions();
        return this;
    }

    public Mist setDuration(int duration) {
        this.maxAge = duration;
        return this;
    }

    /**
     * Heisst im Original schlicht setType/getType. Auf 1.21 geht das nicht: Entity.getType()
     * liefert dort den EntityType und laesst sich nicht mit einem anderen Rueckgabetyp
     * ueberschreiben.
     */
    public Mist setFluidType(FluidType fluid) {
        this.entityData.set(TYPE, fluid.getID());
        return this;
    }

    public FluidType getFluidType() {
        return Fluids.fromID(this.entityData.get(TYPE));
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TYPE, 0);
        builder.define(AREA_WIDTH, 0F);
        builder.define(AREA_HEIGHT, 0F);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(Math.max(this.entityData.get(AREA_WIDTH), 0.1F), Math.max(this.entityData.get(AREA_HEIGHT), 0.1F));
    }

    @Override
    public void tick() {

        float height = this.entityData.get(AREA_HEIGHT);

        if(!this.level().isClientSide) {

            if(this.tickCount >= this.getMaxAge()) {
                this.discard();
                return;
            }

            FluidType type = this.getFluidType();

            if(type.hasTrait(FT_VentRadiation.class)) {
                FT_VentRadiation trait = type.getTrait(FT_VentRadiation.class);
                ChunkRadiationManager.proxy.incrementRad(this.level(), this.blockPosition(), trait.getRadPerMB() * 2);
            }

            double intensity = 1D - (double) this.tickCount / (double) this.getMaxAge();

            if(type.hasTrait(FT_Flammable.class) && this.isOnFire()) {
                this.level().explode(this, this.getX(), this.getY() + height / 2, this.getZ(), (float) intensity * 15F, ExplosionInteraction.TNT);
                this.discard();
                return;
            }

            for(Entity e : this.level().getEntities(this, this.getBoundingBox())) {
                this.affect(e, intensity);
            }

        } else {

            AABB box = this.getBoundingBox();

            for(int i = 0; i < 2; i++) {
                double x = box.minX + (this.random.nextDouble() - 0.5) * (box.maxX - box.minX);
                double y = box.minY + this.random.nextDouble() * (box.maxY - box.minY);
                double z = box.minZ + (this.random.nextDouble() - 0.5) * (box.maxZ - box.minZ);

                CompoundTag fx = new CompoundTag();
                fx.putFloat("lift", 0.5F);
                fx.putFloat("base", 0.75F);
                fx.putFloat("max", 2F);
                fx.putInt("life", 50 + this.random.nextInt(10));
                fx.putInt("color", this.getFluidType().getColor());

                ParticleUtil.addParticle(this.level(), new NbtParticleOptions(NtmParticleTypes.COOLING_TOWER.get(), fx), x, y, z);
            }
        }
    }

    protected void affect(Entity e, double intensity) {

        FluidType type = this.getFluidType();
        LivingEntity living = e instanceof LivingEntity ? (LivingEntity) e : null;

        if(this.isExtinguishing(type)) {
            e.clearFire();
        }

        if(type.hasTrait(FT_Corrosive.class) && living != null) {
            FT_Corrosive trait = type.getTrait(FT_Corrosive.class);
            EntityDamageUtil.hurtIgnoreIFrame(living, living.damageSources().source(NtmDamageTypes.ACID), trait.getRating() / 60F);
            for(EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                ArmorUtil.damageSuit(living, slot, trait.getRating() / 50);
            }
        }

        if(type.hasTrait(FT_VentRadiation.class) && living != null) {
            FT_VentRadiation trait = type.getTrait(FT_VentRadiation.class);
            ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, trait.getRadPerMB() * 5);
        }

        if(type.hasTrait(FT_Poison.class) && living != null) {
            FT_Poison trait = type.getTrait(FT_Poison.class);
            living.addEffect(new MobEffectInstance(trait.isWithering() ? MobEffects.WITHER : MobEffects.POISON, (int) (5 * 20 * intensity)));
        }

        if(type.hasTrait(FT_Toxin.class) && living != null) {
            type.getTrait(FT_Toxin.class).affect(living, intensity);
        }

        if(type == Fluids.ENDERJUICE && living != null) {
            this.teleportRandomly(living);
        }
    }

    protected boolean isExtinguishing(FluidType type) {
        return this.getFluidType().temperature < 50 && !type.hasTrait(FT_Flammable.class);
    }

    /** Original: EntityMist.teleportRandomly, dort selbst aus EntityEnderman uebernommen. */
    public void teleportRandomly(Entity e) {
        double x = this.getX() + (this.random.nextDouble() - 0.5D) * 64.0D;
        double y = this.getY() + (this.random.nextInt(64) - 32);
        double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 64.0D;
        e.teleportTo(x, y, z);
    }

    /** Die Wolke steht, wo sie entstanden ist -- Stroemungen und Stoesse bewegen sie nicht. */
    @Override public void push(double x, double y, double z) { }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override protected boolean canRide(Entity entity) { return false; }
    @Override public boolean displayFireAnimation() { return false; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setFluidType(Fluids.fromID(tag.getInt("type")));
        this.setArea(tag.getFloat("width"), tag.getFloat("height"));
        this.maxAge = tag.contains("maxAge") ? tag.getInt("maxAge") : this.maxAge;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("type", this.getFluidType().getID());
        tag.putFloat("width", this.entityData.get(AREA_WIDTH));
        tag.putFloat("height", this.entityData.get(AREA_HEIGHT));
        tag.putInt("maxAge", this.maxAge);
    }

    public static SprayStyle getStyleFromType(FluidType type) {

        if(type.hasTrait(FT_Viscous.class)) return SprayStyle.NULL;
        if(type.hasTrait(FT_Gaseous.class) || type.hasTrait(FT_Gaseous_ART.class)) return SprayStyle.GAS;
        if(type.hasTrait(FT_Liquid.class)) return SprayStyle.MIST;

        return SprayStyle.NULL;
    }

    public enum SprayStyle {
        MIST,   //liquids that have been sprayed into a mist
        GAS,    //things that were already gaseous
        NULL
    }
}
