package com.hbm.fluids;

import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.fluids.VolcanicLiquidBlock;
import com.hbm.blocks.fluids.CoriumLiquidBlock;
import com.hbm.blocks.fluids.MudLiquidBlock;
import com.hbm.main.NuclearTechMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NtmFluids {
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, NuclearTechMod.MODID);

    public static final DeferredHolder<Fluid, VolcanicLavaBaseSource> VOLCANIC_LAVA = FLUIDS.register("volcanic_lava", () -> new VolcanicLavaBaseSource(volcanicLavaProps()));
    public static final DeferredHolder<Fluid, VolcanicLavaBaseFlowing> VOLCANIC_LAVA_FLOWING = FLUIDS.register("volcanic_lava_flowing", () -> new VolcanicLavaBaseFlowing(volcanicLavaProps()));
    public static final DeferredHolder<Fluid, VolcanicLavaBaseSource> RAD_LAVA = FLUIDS.register("rad_lava", () -> new VolcanicLavaBaseSource(radLavaProps()));
    public static final DeferredHolder<Fluid, VolcanicLavaBaseFlowing> RAD_LAVA_FLOWING = FLUIDS.register("rad_lava_flowing", () -> new VolcanicLavaBaseFlowing(radLavaProps()));
    public static final DeferredHolder<Fluid, CoriumBaseSource> CORIUM = FLUIDS.register("corium", () -> new CoriumBaseSource(coriumProps()));
    public static final DeferredHolder<Fluid, CoriumBaseFlowing> CORIUM_FLOWING = FLUIDS.register("corium_flowing", () -> new CoriumBaseFlowing(coriumProps()));
    public static final DeferredHolder<Fluid, MudBaseSource> MUD = FLUIDS.register("mud", () -> new MudBaseSource(mudProps()));
    public static final DeferredHolder<Fluid, MudBaseFlowing> MUD_FLOWING = FLUIDS.register("mud_flowing", () -> new MudBaseFlowing(mudProps()));

    private static BaseFlowingFluid.Properties volcanicLavaProps() {
        return new BaseFlowingFluid.Properties(NtmFluidTypes.VOLCANIC_LAVA_TYPE, NtmFluids.VOLCANIC_LAVA, NtmFluids.VOLCANIC_LAVA_FLOWING)
                .bucket(() -> Items.LAVA_BUCKET)
                .block(NtmBlocks.VOLCANIC_LAVA)
                .levelDecreasePerBlock(2)
                .explosionResistance(500F)
                .tickRate(15);
    }

    private static BaseFlowingFluid.Properties radLavaProps() {
        return new BaseFlowingFluid.Properties(NtmFluidTypes.RAD_LAVA_TYPE, NtmFluids.RAD_LAVA, NtmFluids.RAD_LAVA_FLOWING)
                .bucket(() -> Items.LAVA_BUCKET)
                .block(NtmBlocks.RAD_LAVA)
                .levelDecreasePerBlock(2)
                .explosionResistance(500F)
                .tickRate(15);
    }

    private static BaseFlowingFluid.Properties coriumProps() {
        return new BaseFlowingFluid.Properties(NtmFluidTypes.CORIUM_TYPE, NtmFluids.CORIUM, NtmFluids.CORIUM_FLOWING)
                .bucket(() -> Items.LAVA_BUCKET)
                .block(NtmBlocks.CORIUM)
                /*
                 * Zwei Stufen je Block und ein langsamer Takt: Corium laeuft traege und kommt
                 * nicht weit, bevor es erstarrt. Die Zahlen entsprechen den fuenf Stufen und dem
                 * Takt von 30 aus dem Original, so gut das neue Fluidsystem sie hergibt.
                 */
                .levelDecreasePerBlock(2)
                .explosionResistance(500F)
                .tickRate(30);
    }

    private static BaseFlowingFluid.Properties mudProps() {
        return new BaseFlowingFluid.Properties(NtmFluidTypes.MUD_TYPE, NtmFluids.MUD, NtmFluids.MUD_FLOWING)
                .bucket(() -> Items.LAVA_BUCKET)
                .block(NtmBlocks.MUD)
                /* Vier Stufen wie im Original (setQuantaPerBlock(4)), Takt 15. */
                .levelDecreasePerBlock(2)
                .explosionResistance(500F)
                .tickRate(15);
    }

    public static void register(IEventBus eventBus) { FLUIDS.register(eventBus); }

    public static class VolcanicLavaBaseSource extends BaseFlowingFluid.Source {

        public VolcanicLavaBaseSource(Properties properties) {
            super(properties);
        }

        @Override
        public void tick(Level level, BlockPos pos, FluidState state) {
            super.tick(level, pos, state);

            if(level instanceof ServerLevel serverLevel) {
                VolcanicLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, level.random);
            }
        }

        @Override
        protected void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            super.randomTick(level, pos, state, random);

            if(level instanceof ServerLevel serverLevel) {
                VolcanicLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, random);
            }
        }
    }

    public static class VolcanicLavaBaseFlowing extends BaseFlowingFluid.Flowing {

        public VolcanicLavaBaseFlowing(Properties properties) {
            super(properties);
        }

        @Override
        public void tick(Level level, BlockPos pos, FluidState state) {
            super.tick(level, pos, state);

            if(level instanceof ServerLevel serverLevel) {
                VolcanicLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, level.random);
            }
        }

        @Override
        protected void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            super.randomTick(level, pos, state, random);

            if(level instanceof ServerLevel serverLevel) {
                VolcanicLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, random);
            }
        }
    }

    public static class CoriumBaseSource extends BaseFlowingFluid.Source {

        public CoriumBaseSource(Properties properties) {
            super(properties);
        }

        @Override
        public void tick(Level level, BlockPos pos, FluidState state) {
            super.tick(level, pos, state);
            if(level instanceof ServerLevel serverLevel) CoriumLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, level.random);
        }

        @Override
        protected void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            super.randomTick(level, pos, state, random);
            if(level instanceof ServerLevel serverLevel) CoriumLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, random);
        }
    }

    public static class CoriumBaseFlowing extends BaseFlowingFluid.Flowing {

        public CoriumBaseFlowing(Properties properties) {
            super(properties);
        }

        @Override
        public void tick(Level level, BlockPos pos, FluidState state) {
            super.tick(level, pos, state);
            if(level instanceof ServerLevel serverLevel) CoriumLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, level.random);
        }

        @Override
        protected void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            super.randomTick(level, pos, state, random);
            if(level instanceof ServerLevel serverLevel) CoriumLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, random);
        }
    }

    public static class MudBaseSource extends BaseFlowingFluid.Source {

        public MudBaseSource(Properties properties) {
            super(properties);
        }

        @Override
        public void tick(Level level, BlockPos pos, FluidState state) {
            super.tick(level, pos, state);
            if(level instanceof ServerLevel serverLevel) MudLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, level.random);
        }

        @Override
        protected void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            super.randomTick(level, pos, state, random);
            if(level instanceof ServerLevel serverLevel) MudLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, random);
        }
    }

    public static class MudBaseFlowing extends BaseFlowingFluid.Flowing {

        public MudBaseFlowing(Properties properties) {
            super(properties);
        }

        @Override
        public void tick(Level level, BlockPos pos, FluidState state) {
            super.tick(level, pos, state);
            if(level instanceof ServerLevel serverLevel) MudLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, level.random);
        }

        @Override
        protected void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            super.randomTick(level, pos, state, random);
            if(level instanceof ServerLevel serverLevel) MudLiquidBlock.baseTick(level.getBlockState(pos), serverLevel, pos, random);
        }
    }
}
