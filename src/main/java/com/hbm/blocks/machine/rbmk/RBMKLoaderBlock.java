package com.hbm.blocks.machine.rbmk;

import api.hbm.fluidmk2.IFluidConnectorBlockMK2;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKLoader.
 *
 * Der Name fuehrt in die Irre: das ist kein Kran und laedt keine Brennstaebe. Es ist der
 * Rohranschluss der Saeule, und er steht UNTER ihr.
 *
 * Ohne ihn hat eine Kessel-, Heiz- oder Kuehlsaeule genau einen Anschluss: oben auf dem Deckel.
 * Alle Rohre muessen dann ueber den Reaktor gefuehrt werden. Steht dagegen ein Ladeblock einen
 * oder zwei Bloecke unter dem Saeulenfuss, bekommt die Saeule fuenf weitere Anschlusspunkte --
 * die vier Seiten des Ladeblocks und den Platz darunter. Damit laesst sich die ganze
 * Rohrfuehrung unter den Reaktorboden legen, so wie es die Anlagen des Originals tun.
 *
 * Der Block selbst bewegt nichts. Er hat keine Blockentitaet und keinen Tank; er sagt den
 * Rohren nur, dass sie sich hier anschliessen duerfen, und die Saeule ueber ihm gibt ihren
 * Dampf an das aus, was daneben steht. Nach oben nimmt er an, was sich heizen laesst
 * (Speisewasser), zur Seite, was sich kuehlen laesst (Dampf) -- dazu Perfluormethyl, das
 * Kuehlmittel der Kuehlsaeule.
 *
 * Er ist der erste Block des Ports, der IFluidConnectorBlockMK2 benutzt. Die Schnittstelle lag
 * seit dem Fluidnetz bereit, hatte aber bis jetzt keinen Abnehmer.
 */
public class RBMKLoaderBlock extends Block implements IFluidConnectorBlockMK2 {

    public RBMKLoaderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canConnect(FluidType type, BlockGetter level, BlockPos pos, Direction dir) {
        if(dir == Direction.UP) return type.hasTrait(FT_Heatable.class);
        return type.hasTrait(FT_Coolable.class) || type == Fluids.PERFLUOROMETHYL;
    }
}
