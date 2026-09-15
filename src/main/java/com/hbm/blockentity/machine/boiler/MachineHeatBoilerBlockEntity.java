package com.hbm.blockentity.machine.boiler;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.IConfigurableMachine;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import java.io.IOException;

public class MachineHeatBoilerBlockEntity extends AbstractBoilerBlockEntity {

    public static int maxHeat = 3_200_000;
    public static double diffusion = 0.1D;
    public static boolean canExplode = true;

    public MachineHeatBoilerBlockEntity(BlockPos pos, BlockState state) {
        super((net.minecraft.world.level.block.entity.BlockEntityType<? extends AbstractBoilerBlockEntity>) NtmBlockEntityTypes.HEAT_BOILER.get(), pos, state, 16_000, 1_600_000);
    }

    @Override
    protected int getInputCapacity() {
        return 16_000;
    }

    @Override
    protected int getOutputCapacity() {
        return 1_600_000;
    }

    @Override
    protected DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        Direction facing = this.getBlockState().getValue(DummyableBlock.FACING);
        return new DirPos[] {
                new DirPos(pos.getX() + facing.getStepX() * 2, pos.getY(), pos.getZ() + facing.getStepZ() * 2, facing),
                new DirPos(pos.getX() - facing.getStepX() * 2, pos.getY(), pos.getZ() - facing.getStepZ() * 2, facing.getOpposite()),
                new DirPos(pos.getX(), pos.getY() + 4, pos.getZ(), Direction.UP)
        };
    }

    @Override
    protected int getRenderHeight() {
        return 4;
    }

    @Override protected int getMaxHeat() { return maxHeat; }
    @Override protected double getDiffusion() { return diffusion; }

    @Override
    protected boolean canExplode() {
        return canExplode;
    }

    /** Konfiguration, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static void readConfig(JsonObject obj) {
        maxHeat = IConfigurableMachine.grab(obj, "I:maxHeat", maxHeat);
        diffusion = IConfigurableMachine.grab(obj, "D:diffusion", diffusion);
        canExplode = IConfigurableMachine.grab(obj, "B:canExplode", canExplode);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:maxHeat").value(maxHeat);
        writer.name("D:diffusion").value(diffusion);
        writer.name("B:canExplode").value(canExplode);
    }

}
