package com.hbm.explosion;

import api.hbm.energymk2.IEnergyHandlerMK2;
import com.hbm.blocks.NtmBlocks;
import com.hbm.config.VersatileConfig;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.inventory.NtmTags;
import com.hbm.items.weapon.sedna.factory.ConfettiUtil;
import com.hbm.lib.Library;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.util.DamageResistanceHandler.DamageClass;
import com.hbm.util.EntityDamageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ExplosionNukeGeneric {

    public static void incrementRad(Level level, double posX, double posY, double posZ, float mult) {
        for(int i = -2; i <= 2; i++) {
            for(int j = -2; j <= 2; j++) {
                if(Math.abs(i) + Math.abs(j) < 4) {
                    ChunkRadiationManager.proxy.incrementRad(level, new BlockPos((int) Math.floor(posX + i * 16), (int) Math.floor(posY), (int) Math.floor(posZ + j * 16)), 50F / (Math.abs(i) + Math.abs(j) + 1) * mult);
                }
            }
        }
    }

    /**
     * Portiert aus 1.7.10: ExplosionNukeGeneric.empBlast und emp.
     *
     * Der elektromagnetische Schlag. Er laesst das Gelaende stehen und trifft nur, was Strom
     * haelt: jede Blockentitaet im Umkreis verliert ihre Ladung, und jede fuenfte wird dabei
     * zu Elektroschrott.
     *
     * Die Kugel ist die des Originals, einschliesslich ihrer Eigenheit: geprueft wird gegen
     * r*r/2, nicht gegen r*r -- der Schlag reicht also nur etwa sieben Zehntel so weit, wie der
     * uebergebene Wert vermuten laesst. Uebernommen, weil die Staerken darauf abgestimmt sind.
     */
    public static void empBlast(Level level, BlockPos mitte, int staerke) {

        int r = staerke;
        int rQuadratHalb = r * r / 2;

        for(int dx = -r; dx < r; dx++)
        for(int dy = -r; dy < r; dy++)
        for(int dz = -r; dz < r; dz++) {
            if(dx * dx + dy * dy + dz * dz < rQuadratHalb) emp(level, mitte.offset(dx, dy, dz));
        }
    }

    /**
     * ABWEICHUNG: das Original entlaedt zusaetzlich Maschinen fremder Mods ueber die
     * RF-Schnittstelle (IEnergyProvider). Der Port kennt nur seine eigene.
     */
    public static void emp(Level level, BlockPos pos) {

        if(level.isClientSide) return;

        if(!(level.getBlockEntity(pos) instanceof IEnergyHandlerMK2 speicher)) return;

        speicher.setPower(0);
        if(level.random.nextInt(5) < 1) level.setBlockAndUpdate(pos, NtmBlocks.BLOCK_ELECTRICAL_SCRAP.get().defaultBlockState());
    }

    public static void dealDamage(Level level, double x, double y, double z, double radius) {
        dealDamage(level, x, y, z, radius, 250F);
    }

    private static void dealDamage(Level level, double x, double y, double z, double radius, float maxDamage) {
        List<Entity> entities = level.getEntities(null, new AABB(x, y, z, x, y, z).inflate(radius));

        for(Entity entity : entities) {
            double dist = Math.sqrt(entity.distanceToSqr(x, y, z));
            if(dist <= radius) {

                double entX = entity.getX();
                double entY = entity.getY() + entity.getEyeHeight();
                double entZ = entity.getZ();

                if(!isExplosionExempt(entity) && !Library.isObstructed(level, x, y, z, entX, entY, entZ)) {

                    boolean doKnockback = true;
                    double damage = maxDamage * (radius - dist) / radius;

                    DamageSource source = level.damageSources().source(NtmDamageTypes.NUCLEAR_BLAST);
                    if(entity instanceof LivingEntity living && living.isAlive()) {
                        doKnockback = EntityDamageUtil.hurtNT(living, source, (float) damage, true, true, 0F, 100F, 0F);
                        if(!living.isAlive()) ConfettiUtil.createConfetti(living, DamageClass.EXPLOSION);
                    } else {
                        entity.hurt(source, (float) damage);
                    }

                    entity.igniteForSeconds(5);

                    if(doKnockback) {
                        double knockX = entX - x;
                        double knockY = (entity.getY() + entity.getEyeHeight()) - y;
                        double knockZ = entZ - z;

                        Vec3 knock = new Vec3(knockX, knockY, knockZ).normalize().scale(0.2D);
                        entity.setDeltaMovement(entity.getDeltaMovement().add(knock));
                    }
                }
            }
        }
    }

    private static boolean isExplosionExempt(Entity entity) {
        if(entity instanceof Ocelot) return true;

        if(
                entity instanceof Player && ((Player) entity).isCreative()
        ) return true;

        return false;
    }

    public static void solinium(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            BlockState state = level.getBlockState(pos);
            Block b = state.getBlock();

            if (b == Blocks.GRASS_BLOCK || b == Blocks.MYCELIUM || b == NtmBlocks.WASTE_EARTH.get() || b == NtmBlocks.WASTE_MYCELIUM.get()) {
                level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                return;
            }

            if (state.is(NtmTags.Blocks.PLANTS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.PLANKS) || state.is(BlockTags.LOGS)) {
                level.removeBlock(pos, false);
            }
        }
    }

    /**
     * Portiert aus 1.7.10: ExplosionNukeGeneric.waste.
     *
     * Legt ein Verstrahlungsfeld um den Einschlagpunkt: eine ausgefranste Kugel, in der jeder
     * Block durch seine verstrahlte Entsprechung ersetzt wird. Der Rand franst aus, weil der
     * Schwellwert je Block um bis zu ein Fuenftel zufaellig schwankt -- so steht es im Original.
     */
    public static void waste(Level level, BlockPos center, int radius) {
        wasteInternal(level, center, radius, true);
    }

    /** Wie {@link #waste}, aber ohne die Umwandlung von Uranerz in Schrabidium. */
    public static void wasteNoSchrab(Level level, BlockPos center, int radius) {
        wasteInternal(level, center, radius, false);
    }

    private static void wasteInternal(Level level, BlockPos center, int radius, boolean schrabidium) {

        int r2 = radius * radius;
        int r22 = r2 / 2;
        if(r22 < 5) return;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for(int xx = -radius; xx < radius; xx++) {
            int sx = xx * xx;
            for(int yy = -radius; yy < radius; yy++) {
                int sy = sx + yy * yy;
                for(int zz = -radius; zz < radius; zz++) {
                    int sz = sy + zz * zz;
                    if(sz >= r22 + level.random.nextInt(r22 / 5)) continue;

                    pos.set(center.getX() + xx, center.getY() + yy, center.getZ() + zz);
                    if(level.getBlockState(pos).isAir()) continue;

                    wasteDest(level, pos.immutable(), schrabidium);
                }
            }
        }
    }

    /** Ersetzt einen einzelnen Block durch seine verstrahlte Entsprechung. */
    public static void wasteDest(Level level, BlockPos pos) {
        wasteDest(level, pos, true);
    }

    public static void wasteDest(Level level, BlockPos pos, boolean schrabidium) {

        if(level.isClientSide) return;

        BlockState state = level.getBlockState(pos);
        Block b = state.getBlock();

        if(state.is(BlockTags.DOORS)) {
            level.removeBlock(pos, false);

        } else if(b == Blocks.GRASS_BLOCK) {
            level.setBlockAndUpdate(pos, NtmBlocks.WASTE_EARTH.get().defaultBlockState());

        } else if(b == Blocks.MYCELIUM) {
            level.setBlockAndUpdate(pos, NtmBlocks.WASTE_MYCELIUM.get().defaultBlockState());

        } else if(b == Blocks.SAND) {
            // Nur jedes zwanzigste Sandfeld wird zu Trinitit.
            if(level.random.nextInt(20) == 1) level.setBlockAndUpdate(pos, NtmBlocks.WASTE_TRINITITE.get().defaultBlockState());

        } else if(b == Blocks.RED_SAND) {
            if(level.random.nextInt(20) == 1) level.setBlockAndUpdate(pos, NtmBlocks.WASTE_TRINITITE_RED.get().defaultBlockState());

        } else if(b == Blocks.CLAY) {
            level.setBlockAndUpdate(pos, Blocks.TERRACOTTA.defaultBlockState());

        } else if(b == Blocks.MOSSY_COBBLESTONE) {
            level.setBlockAndUpdate(pos, Blocks.COAL_ORE.defaultBlockState());

        } else if(b == Blocks.COAL_ORE) {
            int rand = level.random.nextInt(10);
            if(rand <= 3) level.setBlockAndUpdate(pos, Blocks.DIAMOND_ORE.defaultBlockState());
            else if(rand == 9) level.setBlockAndUpdate(pos, Blocks.EMERALD_ORE.defaultBlockState());

        } else if(state.is(BlockTags.LOGS) || b == Blocks.MUSHROOM_STEM) {
            level.setBlockAndUpdate(pos, NtmBlocks.WASTE_LOG.get().defaultBlockState());

        } else if(b == Blocks.BROWN_MUSHROOM_BLOCK || b == Blocks.RED_MUSHROOM_BLOCK) {
            /*
             * Im Original wird der Pilzblock mit Metadaten 10 zu verstrahltem Holz, jeder andere
             * verschwindet. Metadaten 10 war die Variante, die auf allen Seiten die Stielhaut
             * traegt -- in 1.21 ist das ein eigener Block, mushroom_stem, und der steht eine
             * Zeile hoeher. Hier bleibt darum nur der Fall "verschwindet".
             */
            level.removeBlock(pos, false);

        } else if(state.is(BlockTags.PLANKS) && b != NtmBlocks.WASTE_LOG.get()) {
            level.setBlockAndUpdate(pos, NtmBlocks.WASTE_PLANKS.get().defaultBlockState());

        } else if(b == NtmBlocks.ORE_URANIUM.get()) {
            level.setBlockAndUpdate(pos, schrabidium && level.random.nextInt(VersatileConfig.getSchrabOreChance()) == 1
                    ? NtmBlocks.ORE_SCHRABIDIUM.get().defaultBlockState()
                    : NtmBlocks.ORE_URANIUM_SCORCHED.get().defaultBlockState());

        } else if(b == NtmBlocks.ORE_NETHER_URANIUM.get()) {
            level.setBlockAndUpdate(pos, schrabidium && level.random.nextInt(VersatileConfig.getSchrabOreChance()) == 1
                    ? NtmBlocks.ORE_NETHER_SCHRABIDIUM.get().defaultBlockState()
                    : NtmBlocks.ORE_NETHER_URANIUM_SCORCHED.get().defaultBlockState());

        } else if(b == NtmBlocks.ORE_GNEISS_URANIUM.get()) {
            level.setBlockAndUpdate(pos, schrabidium && level.random.nextInt(VersatileConfig.getSchrabOreChance()) == 1
                    ? NtmBlocks.ORE_GNEISS_SCHRABIDIUM.get().defaultBlockState()
                    : NtmBlocks.ORE_GNEISS_URANIUM_SCORCHED.get().defaultBlockState());
        }
    }
}
