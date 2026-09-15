package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.util.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityChimneyBrick. */
public class ChimneyBrickBlockEntity extends ChimneyBaseBlockEntity {

    public ChimneyBrickBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CHIMNEY_BRICK.get(), pos, state);
    }

    @Override
    public void spawnParticles() {
        if(this.level == null || this.level.getGameTime() % 2 != 0) return;

        CompoundTag tag = new CompoundTag();
        tag.putFloat("lift", 10F);
        tag.putFloat("base", 0.5F);
        tag.putFloat("max", 3F);
        tag.putInt("life", 250 + this.level.random.nextInt(50));
        tag.putInt("color", 0x404040);

        ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.COOLING_TOWER.get(), tag),
                this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 12D, this.worldPosition.getZ() + 0.5D);
    }

    /**
     * Original: MobConfig.rampantMode schaltet auf rampantSmokeStackOverride um. Der
     * Glyphiden-Ansturm gibt es im Port nicht, also bleibt es beim Normalwert.
     */
    @Override public double getPollutionMod() { return 0.25D; }

    public AABB getRenderBoundingBox() {
        return new AABB(
                this.worldPosition.getX() - 1, this.worldPosition.getY(), this.worldPosition.getZ() - 1,
                this.worldPosition.getX() + 2, this.worldPosition.getY() + 13, this.worldPosition.getZ() + 2);
    }
}
