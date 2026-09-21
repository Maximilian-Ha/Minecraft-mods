package com.hbm.entity.grenade;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.effect.Mist;
import com.hbm.entity.projectile.ThrowableNT;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.NtmItems;
import com.hbm.util.Vec3NT;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.entity.grenade.EntityDisperserCanister.
 *
 * Die geworfene Kanne. Beim ersten Anstossen platzt sie und laesst eine Wolke ihres Inhalts
 * zurueck -- zehn Bloecke breit, fuenf hoch, vier Sekunden lang. Was die Wolke tut, steht
 * nicht hier, sondern in den Merkmalen der Fluessigkeit: Saeure aetzt, Gift vergiftet,
 * Pheromon ruft Glyphiden.
 *
 * SIE SPRINGT NICHT AB. Anders als die Universalgranate geht sie beim ersten Treffer auf --
 * im Original erbt sie von EntityGrenadeBase, und dessen onImpact ist genau das.
 *
 * ZWEI SYNCHRONE FELDER wie im Original: die Fluessigkeit und die Bauform. Die Bauform
 * entscheidet nur, welcher Gegenstand im Flug gezeichnet wird -- die Kanne oder die
 * Glyphidendruese. Das Original merkt sich dafuer die Gegenstandskennung; hier genuegt ein
 * Schalter, weil es nur diese beiden gibt.
 */
public class DisperserCanister extends ThrowableNT implements ItemSupplier {

    private static final EntityDataAccessor<Integer> FLUID = SynchedEntityData.defineId(DisperserCanister.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DRUESE = SynchedEntityData.defineId(DisperserCanister.class, EntityDataSerializers.BOOLEAN);

    public DisperserCanister(EntityType<? extends DisperserCanister> type, Level level) {
        super(type, level);
    }

    public DisperserCanister(Level level) {
        super(NtmEntityTypes.DISPERSER_CANISTER.get(), level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLUID, 0);
        builder.define(DRUESE, false);
    }

    public DisperserCanister setFluid(FluidType fluid) {
        this.entityData.set(FLUID, fluid.getID());
        return this;
    }

    public FluidType getFluid() {
        return Fluids.fromID(this.entityData.get(FLUID));
    }

    /** true zeichnet die Glyphidendruese, false die Verteilerkanne. */
    public DisperserCanister setGland(boolean druese) {
        this.entityData.set(DRUESE, druese);
        return this;
    }

    public boolean isGland() {
        return this.entityData.get(DRUESE);
    }

    /** Wie die Universalgranate: aus der Hand, nicht aus der Nasenspitze. */
    public void werfen(LivingEntity werfer) {

        this.setOwner(werfer);

        Vec3NT versatz = new Vec3NT(0.25, -0.25, 0).rotateAroundYDeg(-werfer.getYRot() + 180);
        this.setPos(werfer.getX() + versatz.xCoord,
                werfer.getEyeY() + versatz.yCoord,
                werfer.getZ() + versatz.zCoord);

        Vec3 blick = werfer.getLookAngle().normalize();
        this.shoot(blick.x, blick.y, blick.z, 1.0F, 0F);
    }

    @Override
    protected void onImpact(HitResult treffer) {

        if(this.level().isClientSide) return;

        Mist wolke = new Mist(this.level());
        wolke.setFluidType(this.getFluid());
        wolke.setPos(this.getX(), this.getY(), this.getZ());
        wolke.setArea(10F, 5F);
        wolke.setDuration(80);
        this.level().addFreshEntity(wolke);

        this.discard();
    }

    @Override
    public ItemStack getItem() {
        return MetaHelper.newStack(
                this.isGland() ? NtmItems.GLYPHID_GLAND.get() : NtmItems.DISPERSER_CANISTER.get(),
                1, this.entityData.get(FLUID));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("fluid", this.entityData.get(FLUID));
        tag.putBoolean("druese", this.isGland());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(FLUID, tag.getInt("fluid"));
        this.entityData.set(DRUESE, tag.getBoolean("druese"));
    }
}
