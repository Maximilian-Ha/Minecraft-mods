package com.hbm.entity.projectile;

import com.hbm.blockentity.machine.rbmk.RBMKDials;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.items.NtmItems;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
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

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.projectile.EntityZirnoxDebris.
 *
 * Was beim Zerlegen eines ZIRNOX davonfliegt. Betonbrocken und Waermetauscher reissen beim
 * Aufstieg ein Loch in alles ueber ihnen; Brennelement und Graphit strahlen.
 *
 * ABWEICHUNG: wie beim RBMK-Schutt wirkt die Strahlung unmittelbar statt ueber den
 * Strahlungstrank, den es im Port nicht gibt. Staerke je Tick unveraendert.
 */
public class ZirnoxDebris extends DebrisBase {

    private DebrisType cachedType = DebrisType.BLANK;

    public ZirnoxDebris(EntityType<? extends ZirnoxDebris> type, Level level) {
        super(type, level);
    }

    public ZirnoxDebris(Level level, double x, double y, double z, DebrisType type) {
        this(NtmEntityTypes.ZIRNOX_DEBRIS.get(), level);
        this.setPos(x, y, z);
        this.setType(type);
    }

    @Override
    public boolean interactFirst(Player player) {

        if(!this.level().isClientSide) {

            ItemStack drop = switch(this.getDebrisType()) {
                case ELEMENT -> new ItemStack(NtmItems.DEBRIS_ELEMENT.get());
                case SHRAPNEL -> new ItemStack(NtmItems.DEBRIS_SHRAPNEL.get());
                case GRAPHITE -> new ItemStack(NtmItems.DEBRIS_GRAPHITE.get());
                case CONCRETE -> new ItemStack(NtmItems.DEBRIS_CONCRETE.get());
                case EXCHANGER -> new ItemStack(NtmItems.DEBRIS_EXCHANGER.get());
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

        if(this.horizontalCollision) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x * -0.75D, motion.y, motion.z * -0.75D);
        }

        if(!this.level().isClientSide) {

            if((this.getDebrisType() == DebrisType.CONCRETE || this.getDebrisType() == DebrisType.EXCHANGER) && this.getDeltaMovement().y > 0) {
                this.smashUpwards();
            }

            if(this.getDebrisType() == DebrisType.ELEMENT || this.getDebrisType() == DebrisType.GRAPHITE) {
                this.irradiate(this.getDebrisType() == DebrisType.ELEMENT ? 7 : 4);
            }

            if(!RBMKDials.getPermaScrap(this.level()) && this.tickCount > this.getLifetime() + this.getId() % 50) {
                this.discard();
            }
        }
    }

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
            case BLANK -> 3 * 60 * 20;
            case ELEMENT -> 10 * 60 * 20;
            case SHRAPNEL, GRAPHITE -> 15 * 60 * 20;
            case CONCRETE, EXCHANGER -> 60 * 20;
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
            case BLANK, SHRAPNEL -> EntityDimensions.scalable(0.5F, 0.5F);
            case ELEMENT, CONCRETE -> EntityDimensions.scalable(0.75F, 0.5F);
            case GRAPHITE -> EntityDimensions.scalable(0.25F, 0.25F);
            case EXCHANGER -> EntityDimensions.scalable(1F, 0.5F);
        };
    }

    public enum DebrisType {
        BLANK,      // nur ein Stahltraeger
        ELEMENT,    // Brennelement
        SHRAPNEL,   // Splitter aus Rohren und Laufstegen
        GRAPHITE,   // scharfer Stein
        CONCRETE,   // der alles vernichtende Vorbote der Vernichtung
        EXCHANGER   // derselbe, nur quer
    }
}
