package com.hbm.entity.missile;

import com.hbm.blockentity.machine.storage.SoyuzCapsuleBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.SoyuzCapsuleBlock;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.entity.missile.EntitySoyuzCapsule.
 *
 * Die Rueckkehr. Die Sojus setzt sie im Sinkflugmodus in sechshundert Bloecken Hoehe ueber
 * dem Zielpunkt ab; von dort faellt sie mit hoechstens 0,2 je Tick am Fallschirm herunter.
 * Trifft sie auf einen Block, setzt sie sich als soyuz_capsule EINEN Block darueber und
 * schuettet ihre Nutzlast hinein -- samt der Rakete, mit der sie gekommen ist.
 *
 * SECHSHUNDERT IST AUCH IHRE DECKE: das Original schneidet posY bei 600 ab, damit eine
 * Kapsel, die zu hoch abgesetzt wird, nicht ewig faellt.
 */
public class SoyuzCapsule extends Entity {

    /** Welche Sojus sie abgesetzt hat -- das bestimmt die Rakete im neunzehnten Fach. */
    public int soyuz;

    private final NonNullList<ItemStack> payload = NonNullList.withSize(18, ItemStack.EMPTY);

    public SoyuzCapsule(EntityType<? extends SoyuzCapsule> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public SoyuzCapsule(Level level) {
        this(NtmEntityTypes.SOYUZ_CAPSULE.get(), level);
    }

    @Override
    public void tick() {

        if(this.getDeltaMovement().y > -0.2) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.02, 0));
        }

        if(this.getY() > 600) this.setPos(this.getX(), 600, this.getZ());

        this.moveTo(this.position().add(this.getDeltaMovement()), 0F, 0F);

        BlockPos pos = this.blockPosition();

        if(!this.level().getBlockState(pos).isAir()) {

            this.discard();

            if(this.level().isClientSide) return;

            BlockPos ziel = pos.above();
            BlockState zustand = NtmBlocks.SOYUZ_CAPSULE.get().defaultBlockState().setValue(SoyuzCapsuleBlock.RUSTY, false);
            this.level().setBlock(ziel, zustand, 3);

            if(this.level().getBlockEntity(ziel) instanceof SoyuzCapsuleBlockEntity kapsel) {
                for(int i = 0; i < this.payload.size(); i++) kapsel.setItem(i, this.payload.get(i));
                kapsel.setItem(SoyuzCapsuleBlockEntity.FACH_RAKETE, MetaHelper.newStack(NtmItems.MISSILE_SOYUZ.get(), 1, this.soyuz));
            }
        }
    }

    public void setPayload(NonNullList<ItemStack> payload) {
        for(int i = 0; i < Math.min(payload.size(), this.payload.size()); i++) {
            this.payload.set(i, payload.get(i));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) { }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.soyuz = tag.getInt("Soyuz");
        ContainerHelper.loadAllItems(tag, this.payload, this.registryAccess());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Soyuz", this.soyuz);
        ContainerHelper.saveAllItems(tag, this.payload, this.registryAccess());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 500000;
    }
}
