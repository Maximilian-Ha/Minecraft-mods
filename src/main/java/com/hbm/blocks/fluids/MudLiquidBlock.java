package com.hbm.blocks.fluids;

import com.hbm.handler.HazmatRegistry;
import com.hbm.registry.NtmDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.fluid.MudBlock.
 *
 * Rotschlamm -- der Rueckstand der Watz-Reaktion. Er haelt fest wie ein Spinnennetz, vergiftet,
 * wer darin steht, frisst weiche Bloecke weg und laesst Stein zu Geroell zerfallen. Andere
 * Fluessigkeiten verdraengt er ersatzlos: wo Schlamm ankommt, ist kein Wasser mehr.
 *
 * ABWEICHUNGEN:
 * - Das Original ist ein BlockFluidClassic mit vier Stufen und eigener Verdraengungslogik. Auf
 *   1.21 uebernimmt das Fluidsystem das Fliessen; die vier Stufen entsprechen dem Stufenabfall
 *   von zwei je Block.
 * - Das Original listet die zu zerstoerenden Bloecke ueber "Material" auf -- Holz, Wolle, Glas,
 *   Laub, Eis, Pflanzen, Netz und ein Dutzend weiterer. Material gibt es auf 1.21 nicht mehr.
 *   Die Aufzaehlung ist aber ohnehin fast ganz von der letzten Regel des Originals gedeckt: was
 *   eine Sprengfestigkeit unter 1,2 hat, verschwindet. Nur Holz (3,0) und Spinnennetz (4,0)
 *   liegen darueber; die beiden stehen deshalb ausdruecklich hier.
 */
public class MudLiquidBlock extends LiquidBlock {

    /** Sprengfestigkeit, unterhalb derer ein Block dem Schlamm nicht standhaelt. */
    private static final float RESISTANCE_THRESHOLD = 1.2F;

    public MudLiquidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if(level.isClientSide) return;

        /* Angrenzende Fluessigkeiten verschwinden -- Schlamm mischt sich mit nichts. */
        for(Direction dir : Direction.values()) {
            BlockPos target = pos.relative(dir);
            BlockState neighbor = level.getBlockState(target);

            if(neighbor.getBlock() instanceof MudLiquidBlock) continue;
            if(neighbor.getFluidState().isEmpty()) continue;

            level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {

        /* Zaeh wie ein Spinnennetz -- das Original ruft hier setInWeb. */
        entity.makeStuckInBlock(state, new Vec3(0.25D, 0.05D, 0.25D));

        if(level.isClientSide) return;

        /* Der Schutzanzug haelt den Schlamm ab. */
        if(entity instanceof Player player && HazmatRegistry.getResistance(player) > 0F) return;

        DamageSource source = new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(NtmDamageTypes.MUD_POISONING));
        entity.hurt(source, 8F);
    }

    /** Frisst sich in die Nachbarschaft. Wird vom Fluid bei jedem Takt aufgerufen. */
    public static void baseTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        if(!(state.getBlock() instanceof MudLiquidBlock)) return;

        for(Direction dir : Direction.values()) {
            corrode(level, pos.relative(dir), random);
        }
    }

    private static void corrode(ServerLevel level, BlockPos pos, RandomSource random) {

        BlockState state = level.getBlockState(pos);

        if(state.isAir()) return;
        if(state.getBlock() instanceof MudLiquidBlock) return;

        /* Stein zerfaellt stufenweise: Stein zu Geroell, Geroell zu Kies. */
        if(state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.STONE_BRICKS) || state.is(Blocks.STONE_BRICK_STAIRS) || state.is(Blocks.STONE_SLAB)) {
            if(random.nextInt(20) == 0) level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 3);
            return;
        }

        if(state.is(Blocks.COBBLESTONE)) {
            if(random.nextInt(15) == 0) level.setBlock(pos, Blocks.GRAVEL.defaultBlockState(), 3);
            return;
        }

        if(state.is(Blocks.SANDSTONE)) {
            if(random.nextInt(5) == 0) level.setBlock(pos, Blocks.SAND.defaultBlockState(), 3);
            return;
        }

        if(state.is(BlockTags.TERRACOTTA)) {
            if(random.nextInt(10) == 0) level.setBlock(pos, Blocks.CLAY.defaultBlockState(), 3);
            return;
        }

        /* Holz und Spinnennetz sind zu fest fuer die Festigkeitsregel, aber nicht fuer Schlamm. */
        boolean weak = state.is(BlockTags.MINEABLE_WITH_AXE)
                || state.is(Blocks.COBWEB)
                || state.getBlock().getExplosionResistance() < RESISTANCE_THRESHOLD;

        if(weak) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
}
