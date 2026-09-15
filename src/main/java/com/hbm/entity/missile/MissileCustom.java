package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.entity.logic.NukeExplosionBalefire;
import com.hbm.entity.logic.NukeExplosionMK5;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.blocks.NtmBlocks;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.CustomMissilePartItem;
import com.hbm.items.weapon.CustomMissilePartItem.FuelType;
import com.hbm.items.weapon.CustomMissilePartItem.PartSize;
import com.hbm.items.weapon.CustomMissilePartItem.WarheadType;
import com.hbm.handler.MissileStruct;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.particle.helper.NukeTorexCreator;
import com.hbm.world.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.missile.EntityMissileCustom.
 *
 * Die Eigenbau-Rakete. Sie traegt ihre vier Bauteile mit sich und liest daraus alles ab, was sie
 * tut: der Rumpf sagt, wie viel Treibstoff sie hat und welche Rauchfahne sie zieht, das Triebwerk,
 * wie schnell er verbraucht wird, der Sprengkopf, was beim Einschlag passiert.
 *
 * SIE FLIEGT NUR, SOLANGE DER TANK REICHT. Ist er leer, faellt sie -- das unterscheidet sie von
 * allen anderen Raketen des Mods, die immer ankommen.
 *
 * DAS RADAR SIEHT SIE NACH IHRER GROESSE: eine 1,0-m-Rakete gibt einen kleineren Ausschlag als
 * eine 2,0-m-Rakete. Die Zuordnung ist die des Originals.
 *
 * ABWEICHUNG: das Original uebertraegt die Bauteile als Zahlenkennung des Gegenstands. Auf 1.21
 * sind diese Kennungen nicht mehr stabil; hier stehen die Registriernamen.
 */
public class MissileCustom extends MissileBase {

    private static final EntityDataAccessor<String> WARHEAD = SynchedEntityData.defineId(MissileCustom.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> FUSELAGE = SynchedEntityData.defineId(MissileCustom.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> FINS = SynchedEntityData.defineId(MissileCustom.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> THRUSTER = SynchedEntityData.defineId(MissileCustom.class, EntityDataSerializers.STRING);

    public float fuel;
    public float consumption;

    public MissileCustom(EntityType<? extends MissileCustom> entityType, Level level) {
        super(entityType, level);
    }

    /** Setzt Ziel und Bauplan. Muss vor dem Einsetzen in die Welt aufgerufen werden. */
    public MissileCustom setup(Vec3 position, int targetX, int targetZ, MissileStruct template) {

        this.setPosAndTarget(position, targetX, targetZ);

        this.setPart(WARHEAD, template.warhead);
        this.setPart(FUSELAGE, template.fuselage);
        this.setPart(FINS, template.fins);
        this.setPart(THRUSTER, template.thruster);

        if(template.fuselage != null) this.fuel = (Float) template.fuselage.attributes[1];
        if(template.thruster != null) this.consumption = (Float) template.thruster.attributes[1];

        return this;
    }

    private void setPart(EntityDataAccessor<String> key, CustomMissilePartItem part) {
        this.entityData.set(key, part != null ? BuiltInRegistries.ITEM.getKey(part).toString() : "");
    }

    /** Das Bauteil hinter einem der vier Felder -- null, wenn keines gesetzt ist. */
    public CustomMissilePartItem getPart(EntityDataAccessor<String> key) {

        String name = this.entityData.get(key);
        if(name.isEmpty()) return null;

        ResourceLocation id = ResourceLocation.tryParse(name);
        if(id == null || !BuiltInRegistries.ITEM.containsKey(id)) return null;

        return BuiltInRegistries.ITEM.get(id) instanceof CustomMissilePartItem part ? part : null;
    }

    public CustomMissilePartItem getWarhead() { return this.getPart(WARHEAD); }
    public CustomMissilePartItem getFuselage() { return this.getPart(FUSELAGE); }
    public CustomMissilePartItem getFins() { return this.getPart(FINS); }
    public CustomMissilePartItem getThruster() { return this.getPart(THRUSTER); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WARHEAD, "");
        builder.define(FUSELAGE, "");
        builder.define(FINS, "");
        builder.define(THRUSTER, "");
    }

    @Override
    public void tick() {

        /* Ein eigener Sprengkopf darf in jedem Tick mitreden -- auf beiden Seiten. */
        CustomMissilePartItem warhead = this.getWarhead();
        if(warhead != null && warhead.attributes != null && warhead.attributes[0] instanceof WarheadType type && type.updateCustom != null) {
            type.updateCustom.accept(this);
        }

        if(!this.level.isClientSide && this.hasPropulsion()) this.fuel -= this.consumption;

        super.tick();
    }

    /** Ohne Treibstoff kein Schub. */
    @Override
    public boolean hasPropulsion() {
        return this.fuel > 0;
    }

    @Override
    protected void killMissile() {
        if(!this.isRemoved() && this.level instanceof ServerLevel serverLevel) {
            this.discard();
            ExplosionLarge.explode(serverLevel, this.getX(), this.getY(), this.getZ(), 5, true, false, true);
        }
    }

    @Override
    public void onMissileImpact(BlockHitResult bhr) {

        if(!(this.level instanceof ServerLevel serverLevel)) return;

        CustomMissilePartItem part = this.getWarhead();
        if(part == null || part.attributes == null) return;

        WarheadType type = (WarheadType) part.attributes[0];
        float strength = (Float) part.attributes[1];

        /* Ein eigener Sprengkopf ersetzt die Wirkung vollstaendig. */
        if(type.impactCustom != null) {
            type.impactCustom.accept(this);
            return;
        }

        double x = this.position.x;
        double y = this.position.y;
        double z = this.position.z;

        switch(type) {
            case HE -> {
                ExplosionCreator.composeEffectSmall(this.level, x, y, z);
                this.explodeStandard(strength, (int) (strength * 1.6F), false);
            }
            case INC -> {
                ExplosionCreator.composeEffectSmall(this.level, x, y, z);
                this.explodeStandard(strength, (int) (strength * 1.6F), true);
            }
            case BUSTER -> ExplosionLarge.explode(serverLevel, x, y, z, (int) strength, true, true, true);
            case NUCLEAR, TX -> {
                WorldUtil.loadAndAddFreshEntity(NukeExplosionMK5.statFacNoSpawn(this.level, (int) strength, x, y, z));
                NukeTorexCreator.statFacStandard(this.level, x, y, z, strength);
            }
            case N2 -> {
                /* Der N2-Sprengkopf schlaegt wie eine Kernwaffe zu, laesst aber keinen Niederschlag. */
                WorldUtil.loadAndAddFreshEntity(NukeExplosionMK5.statFacNoSpawn(this.level, (int) strength, x, y, z).setMoreFallout(0));
                NukeTorexCreator.statFacStandard(this.level, x, y, z, strength);
            }
            case BALEFIRE -> {
                NukeExplosionBalefire bf = new NukeExplosionBalefire(this.level);
                bf.setPos(x, y, z);
                bf.destructionRange = (int) strength;
                WorldUtil.loadAndAddFreshEntity(bf);
                NukeTorexCreator.statFacBale(this.level, x, y, z, strength);
            }
            case TAINT -> {
                int r = (int) strength;
                for(int i = 0; i < r * 10; i++) {
                    BlockPos pos = BlockPos.containing(
                            this.random.nextInt(r) + x - (r / 2D - 1),
                            this.random.nextInt(r) + y - (r / 2D - 1),
                            this.random.nextInt(r) + z - (r / 2D - 1));
                    if(this.level.getBlockState(pos).isSolidRender(this.level, pos)) {
                        this.level.setBlock(pos, NtmBlocks.TAINT.get().defaultBlockState(), 2);
                    }
                }
            }
            /* NICHT UEBERNOMMEN, weil der Port die Wirkung nicht hat: CLUSTER (das Original laesst
             * sie ohnehin leer), CLOUD (die Giftwolke aus ExplosionChaos.spawnPoisonCloud),
             * TURBINE (der Schaufelregen aus EntityBulletBaseNT) und SCHRAB. Sie schlagen bis auf
             * Weiteres ohne Wirkung ein; die Bauteile dazu gibt es trotzdem, damit die Liste
             * vollstaendig bleibt. */
            default -> { }
        }
    }

    /** Die Rauchfahne richtet sich nach dem Treibstoff im Rumpf. */
    @Override
    protected void spawnContrail() {
        CustomMissilePartItem fuselage = this.getFuselage();
        if(fuselage == null || fuselage.attributes == null) return;
        if(fuselage.attributes[0] == FuelType.XENON) return;   // Ionenantrieb raucht nicht
        super.spawnContrail();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.fuel = tag.getFloat("fuel");
        this.consumption = tag.getFloat("consumption");
        this.entityData.set(WARHEAD, tag.getString("warhead"));
        this.entityData.set(FUSELAGE, tag.getString("fuselage"));
        this.entityData.set(FINS, tag.getString("fins"));
        this.entityData.set(THRUSTER, tag.getString("thruster"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("fuel", this.fuel);
        tag.putFloat("consumption", this.consumption);
        tag.putString("warhead", this.entityData.get(WARHEAD));
        tag.putString("fuselage", this.entityData.get(FUSELAGE));
        tag.putString("fins", this.entityData.get(FINS));
        tag.putString("thruster", this.entityData.get(THRUSTER));
    }

    /* ---- was das Radar sieht ---- */

    @Override
    public String getUnlocalizedName() {

        CustomMissilePartItem part = this.getFuselage();
        if(part == null) return "radar.target.custom";

        if(part.top == PartSize.SIZE_10 && part.bottom == PartSize.SIZE_10) return "radar.target.custom10";
        if(part.top == PartSize.SIZE_10 && part.bottom == PartSize.SIZE_15) return "radar.target.custom1015";
        if(part.top == PartSize.SIZE_15 && part.bottom == PartSize.SIZE_15) return "radar.target.custom15";
        if(part.top == PartSize.SIZE_15 && part.bottom == PartSize.SIZE_20) return "radar.target.custom1520";
        if(part.top == PartSize.SIZE_20 && part.bottom == PartSize.SIZE_20) return "radar.target.custom20";

        return "radar.target.custom";
    }

    @Override
    public int getBlipLevel() {

        CustomMissilePartItem part = this.getFuselage();
        if(part == null) return IRadarDetectableNT.TIER1;

        if(part.top == PartSize.SIZE_10 && part.bottom == PartSize.SIZE_10) return IRadarDetectableNT.TIER10;
        if(part.top == PartSize.SIZE_10 && part.bottom == PartSize.SIZE_15) return IRadarDetectableNT.TIER10_15;
        if(part.top == PartSize.SIZE_15 && part.bottom == PartSize.SIZE_15) return IRadarDetectableNT.TIER15;
        if(part.top == PartSize.SIZE_15 && part.bottom == PartSize.SIZE_20) return IRadarDetectableNT.TIER15_20;
        if(part.top == PartSize.SIZE_20 && part.bottom == PartSize.SIZE_20) return IRadarDetectableNT.TIER20;

        return IRadarDetectableNT.TIER1;
    }

    @Override public List<ItemStack> getDebris() { return new ArrayList<>(); }
    @Override public ItemStack getDebrisRareDrop() { return ItemStack.EMPTY; }
    @Override public ItemStack getMissileItemForInfo() { return new ItemStack(NtmItems.MISSILE_CUSTOM.get()); }
}
