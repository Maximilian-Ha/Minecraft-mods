package com.hbm.explosion.vanillant.standard;

import com.hbm.blocks.generic.GlyphidSpawnerBlock;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockAllocator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;

/**
 * Portiert aus 1.7.10: com.hbm.explosion.vanillant.standard.BlockAllocatorGlyphidDig.
 *
 * Der Graber. Er sieht aus wie die gewoehnliche Zuteilung, rechnet aber anders herum: die
 * gewoehnliche verbraucht Kraft an jedem Block und bleibt stehen, wenn sie alle ist. Dieser
 * hier laeuft eine FESTE STRECKE und bricht nur ab, wenn ein Block HAERTER ist als die
 * Obergrenze -- der Glyphid graebt sich durch alles Weiche und prallt an allem Harten ab.
 *
 * DAS GELEGE IST TABU: davor bricht der Strahl immer ab, wie hart es auch sei. Sonst wuerde
 * ein grabender Glyphid seine eigene Brut wegsprengen. Im Original ist das eine
 * Sonderabfrage auf ModBlocks.glyphid_spawner; seit Runde 303 gibt es den Block im Port,
 * und mit ihm kam die Abfrage -- vorher stand hier, dass sie fehlt.
 */
public class BlockAllocatorGlyphidDig implements IBlockAllocator {

    protected double maximum;
    protected int resolution;

    public BlockAllocatorGlyphidDig(double maximum) {
        this(maximum, 16);
    }

    public BlockAllocatorGlyphidDig(double maximum, int resolution) {
        this.resolution = resolution;
        this.maximum = maximum;
    }

    @Override
    public HashSet<BlockPos> allocate(ExplosionVNT explosion, Level level, double x, double y, double z, float size) {

        HashSet<BlockPos> affectedBlocks = new HashSet<>();

        for(int i = 0; i < this.resolution; ++i) {
            for(int j = 0; j < this.resolution; ++j) {
                for(int k = 0; k < this.resolution; ++k) {

                    if(i == 0 || i == this.resolution - 1 || j == 0 || j == this.resolution - 1 || k == 0 || k == this.resolution - 1) {

                        double d0 = ((float) i / ((float) this.resolution - 1.0F) * 2.0F - 1.0F);
                        double d1 = ((float) j / ((float) this.resolution - 1.0F) * 2.0F - 1.0F);
                        double d2 = ((float) k / ((float) this.resolution - 1.0F) * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;

                        double currentX = x;
                        double currentY = y;
                        double currentZ = z;

                        double dist = 0;

                        for(float stepSize = 0.3F; dist <= explosion.size;) {

                            double deltaX = currentX - x;
                            double deltaY = currentY - y;
                            double deltaZ = currentZ - z;
                            dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                            BlockPos pos = BlockPos.containing(currentX, currentY, currentZ);
                            BlockState state = level.getBlockState(pos);

                            if(!state.isAir()) {
                                if(this.maximum < state.getExplosionResistance(level, pos, explosion.compat)) break;
                                if(state.getBlock() instanceof GlyphidSpawnerBlock) break;
                            }

                            affectedBlocks.add(pos);

                            currentX += d0 * (double) stepSize;
                            currentY += d1 * (double) stepSize;
                            currentZ += d2 * (double) stepSize;
                        }
                    }
                }
            }
        }

        return affectedBlocks;
    }
}
