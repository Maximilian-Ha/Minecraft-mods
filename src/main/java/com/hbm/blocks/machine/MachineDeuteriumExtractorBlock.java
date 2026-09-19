package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.DeuteriumExtractorBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.BobMathUtil;
import com.mojang.serialization.MapCodec;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineDeuteriumExtractor.
 *
 * Ein gewoehnlicher Wuerfel ohne Oberflaeche -- was drin ist, sagt der Blick auf den Block.
 */
public class MachineDeuteriumExtractorBlock extends BaseEntityBlock implements ILookOverlay {

    public static final MapCodec<MachineDeuteriumExtractorBlock> CODEC = simpleCodec(MachineDeuteriumExtractorBlock::new);

    public MachineDeuteriumExtractorBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineDeuteriumExtractorBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeuteriumExtractorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof DeuteriumExtractorBlockEntity extractor)) return;

        ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, tankZeilen(extractor));
    }

    /** Strom und beide Tanks, in derselben Reihenfolge wie im Original. */
    static List<Component> tankZeilen(DeuteriumExtractorBlockEntity maschine) {

        List<Component> text = new ArrayList<>();
        text.add(Component.literal("Power: " + BobMathUtil.getShortNumber(maschine.power) + "HE")
                .withStyle(maschine.hasPower() ? ChatFormatting.GREEN : ChatFormatting.RED));

        for(int i = 0; i < maschine.tanks.length; i++) {
            FluidTank tank = maschine.tanks[i];
            text.add(Component.literal(i < 1 ? "-> " : "<- ").withStyle(i < 1 ? ChatFormatting.GREEN : ChatFormatting.RED)
                    .append(Component.translatable(tank.getTankType().getUnlocalizedName()).withStyle(ChatFormatting.RESET))
                    .append(Component.literal(": " + tank.getFill() + "/" + tank.getMaxFill() + "mB").withStyle(ChatFormatting.RESET)));
        }

        return text;
    }
}
