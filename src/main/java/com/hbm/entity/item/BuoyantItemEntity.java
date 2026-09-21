package com.hbm.entity.item;

import com.hbm.entity.NtmEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Portiert aus 1.7.10: com.hbm.entity.item.EntityItemBuoyant.
 *
 * Ein Gegenstand, der schwimmt. Steht unter ihm Wasser, bekommt er in jedem Takt einen
 * Schubs von 0,045 nach oben -- genug, um an der Oberflaeche zu treiben statt auf den Grund
 * zu sinken.
 *
 * NACHGEMESSEN: das Original schaut einen Sechzehntelblock unter sich und verlangt Wasser mit
 * einem Metadatenwert UNTER ACHT. Acht und darueber heisst auf 1.7.10 "fallendes Wasser",
 * also der senkrechte Strahl unter einer Quelle. Auf 1.21 traegt dieselbe Unterscheidung die
 * Eigenschaft FALLING des Fluessigkeitszustands.
 */
public class BuoyantItemEntity extends ItemEntity {

    /** Der Schubs nach oben, Zahl des Originals. */
    private static final double AUFTRIEB = 0.045D;

    public BuoyantItemEntity(EntityType<? extends BuoyantItemEntity> type, Level level) {
        super(type, level);
    }

    public BuoyantItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(NtmEntityTypes.BUOYANT_ITEM.get(), level);
        this.setPos(x, y, z);
        this.setYRot(this.random.nextFloat() * 360F);
        this.setItem(stack);
    }

    @Override
    public void tick() {

        BlockPos unter = BlockPos.containing(this.getX(), this.getY() - 0.0625D, this.getZ());
        FluidState fluss = this.level().getFluidState(unter);

        if(fluss.is(FluidTags.WATER) && !fluss.getValue(FlowingFluid.FALLING)) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, AUFTRIEB, 0));
        }

        super.tick();
    }
}
