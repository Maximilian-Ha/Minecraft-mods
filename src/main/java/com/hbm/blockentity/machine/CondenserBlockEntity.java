package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityCondenser.
 *
 * Die gesamte Logik steckt bereits in CondenserBaseBlockEntity; hier stehen nur die
 * beiden Tankgroessen des einfachen Kondensators (je 100 mB, 1:1 aus dem Original).
 */
public class CondenserBlockEntity extends CondenserBaseBlockEntity {

    public CondenserBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CONDENSER.get(), pos, state);

        this.tanks = new FluidTank[2];
        this.tanks[0] = new FluidTank(Fluids.SPENTSTEAM, inputTankSize);
        this.tanks[1] = new FluidTank(Fluids.WATER, outputTankSize);
    }
}
