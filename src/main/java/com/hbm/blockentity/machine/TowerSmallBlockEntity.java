package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.config.NtmConfig;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.util.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityTowerSmall.
 *
 * Der kleine Kuehlturm: achtzehn Bloecke hoch, Grundflaeche fuenf mal fuenf. Er tut dasselbe
 * wie der Kondensator -- Abdampf zurueck zu Wasser --, nur mit groesseren Taenken und ohne
 * Strom. Die gesamte Rechnung steckt in CondenserBaseBlockEntity.
 *
 * Anders als der Kondensator nimmt und gibt er NICHT an allen sechs Seiten, sondern an vier
 * Stellen drei Bloecke vom Kern entfernt -- dort sitzen die Anschluesse des Bauwerks.
 */
public class TowerSmallBlockEntity extends CondenserBaseBlockEntity {

    public static final int TANK_SIZE = 1000;

    private AABB renderBox;

    public TowerSmallBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_TOWER_SMALL.get(), pos, state);

        this.tanks = new FluidTank[2];
        this.tanks[0] = new FluidTank(Fluids.SPENTSTEAM, TANK_SIZE);
        this.tanks[1] = new FluidTank(Fluids.WATER, TANK_SIZE);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if(this.level != null && this.level.isClientSide
                && NtmConfig.CLIENT.COOLING_TOWER_PARTICLES.get()
                && this.waterTimer > 0 && this.level.getGameTime() % 2 == 0) {
            this.spawnPlume();
        }
    }

    /** Die Dampffahne verlaesst den Turm oben, achtzehn Bloecke ueber dem Kern. */
    private void spawnPlume() {

        CompoundTag fx = new CompoundTag();
        fx.putFloat("lift", 1F);
        fx.putFloat("base", 0.5F);
        fx.putFloat("max", 4F);
        fx.putInt("life", 250 + this.level.random.nextInt(250));

        ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.COOLING_TOWER.get(), fx),
                this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 18D, this.worldPosition.getZ() + 0.5D);
    }

    @Override
    protected void subscribeToAllAround() {
        for(Direction dir : Direction.Plane.HORIZONTAL) {
            this.trySubscribe(this.tanks[0].getTankType(), this.level, this.getBlockPos().relative(dir, 3), dir);
        }
    }

    @Override
    protected void sendFluidToAll() {
        for(Direction dir : Direction.Plane.HORIZONTAL) {
            this.tryProvide(this.tanks[1].getTankType(), this.level, this.getBlockPos().relative(dir, 3), dir);
        }
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 2, p.getY(), p.getZ() - 2, p.getX() + 3, p.getY() + 20, p.getZ() + 3);
        }
        return this.renderBox;
    }
}
