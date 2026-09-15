package com.hbm.entity.projectile;

import com.hbm.blockentity.machine.rbmk.RBMKDials;
import com.hbm.items.NtmItems;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.entity.NtmEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.minecraft.network.syncher.EntityDataAccessor;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.projectile.EntityRBMKDebris.
 *
 * Was beim Einsturz einer RBMK-Saeule davonfliegt. Der Deckel ist der gefaehrlichste Teil: er
 * schlaegt beim Aufstieg ein Loch in alles, was ueber ihm steht.
 *
 * ABWEICHUNG: das Original legt Spielern in der Naehe von Brennstoff- und Graphittruemmern den
 * Strahlungstrank auf, der eine Minute nachwirkt. Ein Strahlungs-Statuseffekt ist im Port nicht
 * vorhanden -- die Strahlung wirkt hier unmittelbar, solange man in Reichweite steht, mit
 * derselben Staerke je Tick, die der Trank gehabt haette ((Stufe + 1) * 0,05).
 */
public class RBMKDebris extends DebrisBase {

    private DebrisType cachedType = DebrisType.BLANK;

    public RBMKDebris(EntityType<? extends RBMKDebris> type, Level level) {
        super(type, level);
    }

    public RBMKDebris(Level level, double x, double y, double z, DebrisType type) {
        this(NtmEntityTypes.RBMK_DEBRIS.get(), level);
        this.setPos(x, y, z);
        this.setType(type);
    }

    @Override
    public boolean interactFirst(Player player) {

        if(!this.level().isClientSide) {

            ItemStack drop = switch(this.getDebrisType()) {
                case FUEL -> new ItemStack(NtmItems.DEBRIS_FUEL.get());
                case GRAPHITE -> new ItemStack(NtmItems.DEBRIS_GRAPHITE.get());
                case LID -> new ItemStack(NtmItems.RBMK_LID.get());
                default -> new ItemStack(NtmItems.DEBRIS_METAL.get());
            };

            if(player.getInventory().add(drop)) this.discard();
        }

        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        this.interactFirst(player);
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void tick() {

        if(!this.hasSizeSet) {
            this.refreshDimensions();
            this.hasSizeSet = true;
        }

        if(!this.level().isClientSide) {

            if(this.getDebrisType() == DebrisType.LID && this.getDeltaMovement().y > 0) this.smashUpwards();

            if(this.getDebrisType() == DebrisType.FUEL || this.getDebrisType() == DebrisType.GRAPHITE) {
                this.irradiate(this.getDebrisType() == DebrisType.FUEL ? 9 : 4);
            }

            if(!RBMKDials.getPermaScrap(this.level()) && this.tickCount > this.getLifetime() + this.getId() % 50) {
                this.discard();
            }
        }

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        this.setDeltaMovement(this.getDeltaMovement().subtract(0D, 0.04D, 0D));
        this.move(MoverType.SELF, this.getDeltaMovement());

        this.lastRot = this.rot;

        if(this.onGround()) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x * 0.85D, motion.y * -0.5D, motion.z * 0.85D);
        } else {
            this.rot += 10F;
            if(this.rot >= 360F) {
                this.rot -= 360F;
                this.lastRot -= 360F;
            }
        }

        /*
         * Das Original prallt beim seitlichen Anstossen ab, statt stehenzubleiben -- in 1.7.10
         * steht das in einer eigenen Kopie von moveEntity. Hier reicht die Kollisionsmeldung, die
         * move() ohnehin setzt.
         */
        if(this.horizontalCollision) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x * -0.75D, motion.y, motion.z * -0.75D);
        }
    }

    /** Der Deckel reisst beim Aufstieg ein Loch von drei mal drei Bloecken in die Decke. */
    private void smashUpwards() {

        Vec3 from = this.position();
        Vec3 to = from.add(this.getDeltaMovement().scale(2D));

        BlockHitResult hit = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if(hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos center = hit.getBlockPos();

        for(int i = -1; i <= 1; i++) for(int j = -1; j <= 1; j++) for(int k = -1; k <= 1; k++) {

            int rn = Math.abs(i) + Math.abs(j) + Math.abs(k);

            if(rn <= 1 || this.random.nextInt(rn) == 0) {
                this.level().setBlockAndUpdate(center.offset(i, j, k), Blocks.AIR.defaultBlockState());
            }
        }

        this.discard();
    }

    private void irradiate(int level) {

        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5D));
        float rad = (level + 1F) * 0.05F;

        for(LivingEntity entity : entities) {
            ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, rad);
        }
    }

    @Override
    protected int getLifetime() {
        return switch(this.getDebrisType()) {
            case BLANK, ELEMENT -> 3 * 60 * 20;
            case FUEL -> 10 * 60 * 20;
            case GRAPHITE -> 15 * 60 * 20;
            case LID -> 30 * 20;
            case ROD -> 60 * 20;
        };
    }

    public void setType(DebrisType type) {
        this.cachedType = type;
        this.entityData.set(DEB_TYPE, type.ordinal());
        this.refreshDimensions();
    }

    public DebrisType getDebrisType() {
        return this.cachedType;
    }

    /*
     * Der Typ liegt zusaetzlich als Feld vor: die Masse werden schon waehrend des Anlegens der
     * Entitaet abgefragt, und zu dem Zeitpunkt steht der synchronisierte Wert noch nicht.
     */
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if(DEB_TYPE.equals(key)) {
            this.cachedType = DebrisType.values()[Math.abs(this.entityData.get(DEB_TYPE)) % DebrisType.values().length];
            this.refreshDimensions();
        }
    }

    /*
     * getDimensions, nicht getDefaultDimensions: in dieser Fassung gibt es nur den ersten Haken.
     * Die Mist-Entitaet macht es an derselben Stelle genauso.
     */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return switch(this.cachedType) {
            case BLANK -> EntityDimensions.scalable(0.5F, 0.5F);
            case ELEMENT -> EntityDimensions.scalable(1F, 1F);
            case FUEL, GRAPHITE -> EntityDimensions.scalable(0.25F, 0.25F);
            case LID -> EntityDimensions.scalable(1F, 0.5F);
            case ROD -> EntityDimensions.scalable(0.75F, 0.5F);
        };
    }

    public enum DebrisType {
        BLANK,      // nur ein Stahltraeger
        ELEMENT,    // das ganze Gehaeuse eines Brennelements
        FUEL,       // scharf
        ROD,        // massiver Borstab
        GRAPHITE,   // scharfer Stein
        LID         // der alles vernichtende Vorbote der Vernichtung
    }
}
