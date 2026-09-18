package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.config.NtmConfig;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityTowerLarge.
 *
 * Der grosse Kuehlturm: zwoelf Bloecke hoch, dafuer neun mal neun im Grundriss. Er tut dasselbe
 * wie der Kondensator -- Abdampf zurueck zu Wasser --, nur mit groesseren Taenken und ohne
 * Strom. Die gesamte Rechnung steckt in CondenserBaseBlockEntity.
 *
 * Seine Anschluesse sitzen nicht am Kern, sondern an der Aussenwand: je Himmelsrichtung drei
 * Stellen fuenf Bloecke vom Kern entfernt, mittig und je drei Bloecke nach beiden Seiten --
 * zwoelf Anschluesse rundum.
 */
public class TowerLargeBlockEntity extends CondenserBaseBlockEntity {

    public static final int TANK_SIZE = 10000;

    private AABB renderBox;

    public TowerLargeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_TOWER_LARGE.get(), pos, state);

        this.tanks = new FluidTank[2];
        this.tanks[0] = new FluidTank(Fluids.SPENTSTEAM, TANK_SIZE);
        this.tanks[1] = new FluidTank(Fluids.WATER, TANK_SIZE);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if(this.level != null && this.level.isClientSide
                && NtmConfig.CLIENT.COOLING_TOWER_PARTICLES.get()
                && this.waterTimer > 0 && this.level.getGameTime() % 4 == 0) {
            this.spawnPlume();
        }
    }

    /**
     * Der grosse Turm ist oben offen: der Dampf steigt vom Boden des Beckens auf und faechert
     * sich auf dem Weg nach oben auf. Die Quelle streut daher ueber drei Bloecke Breite.
     */
    private void spawnPlume() {

        CompoundTag fx = new CompoundTag();
        fx.putFloat("lift", 0.5F);
        fx.putFloat("base", 1F);
        fx.putFloat("max", 10F);
        fx.putInt("life", 750 + this.level.random.nextInt(250));

        ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.COOLING_TOWER.get(), fx),
                this.worldPosition.getX() + 0.5D + this.level.random.nextDouble() * 3D - 1.5D,
                this.worldPosition.getY() + 1D,
                this.worldPosition.getZ() + 0.5D + this.level.random.nextDouble() * 3D - 1.5D);
    }

    @Override
    protected void subscribeToAllAround() {
        for(DirPos pos : this.getConPos()) this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
    }

    @Override
    protected void sendFluidToAll() {
        for(DirPos pos : this.getConPos()) this.tryProvide(this.tanks[1].getTankType(), this.level, pos);
    }

    /** Zwoelf Stellen an der Aussenwand: je Richtung die Mitte und beide Ecken daneben. */
    private DirPos[] getConPos() {
        BlockPos core = this.getBlockPos();
        DirPos[] positions = new DirPos[12];
        int i = 0;
        for(Direction dir : Direction.Plane.HORIZONTAL) {
            Direction rot = dir.getClockWise();
            BlockPos wall = core.relative(dir, 5);
            positions[i++] = new DirPos(wall, dir);
            positions[i++] = new DirPos(wall.relative(rot, 3), dir);
            positions[i++] = new DirPos(wall.relative(rot, -3), dir);
        }
        return positions;
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 4, p.getY(), p.getZ() - 4, p.getX() + 5, p.getY() + 13, p.getZ() + 5);
        }
        return this.renderBox;
    }
}
