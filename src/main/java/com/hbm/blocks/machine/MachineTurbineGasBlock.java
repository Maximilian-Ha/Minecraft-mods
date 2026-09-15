package com.hbm.blocks.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineTurbineGasBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Pre;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineTurbineGas.
 *
 * Mehrblock-Huelle der Gasturbine. Die Abmessungen, der Versatz und die Lage der
 * Anschlussbloecke (makeExtra) sind unveraendert aus dem Original uebernommen.
 */
public class MachineTurbineGasBlock extends DummyableBlock implements ILookOverlay {

    public MachineTurbineGasBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineTurbineGasBlock> CODEC = simpleCodec(MachineTurbineGasBlock::new);
    @Override public MapCodec<MachineTurbineGasBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineTurbineGasBlockEntity(pos, state);
            // Original: new TileEntityProxyCombo(false, true, true) -- also Strom und Fluid, kein Inventar
            case EXTRA -> new ProxyComboBlockEntity(pos, state).power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 1, 1, 4, 5 }; }
    @Override public int getOffset() { return 1; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = dir.getClockWise(Axis.Y);

        // Treibstoff- und Schmiermittelanschluss links und rechts vorne
        this.makeExtra(level, core.offset(-dir.getStepX() + rot.getStepX(), 0, -dir.getStepZ() + rot.getStepZ()));
        this.makeExtra(level, core.offset(dir.getStepX() + rot.getStepX(), 0, dir.getStepZ() + rot.getStepZ()));
        // Wasseranschluss links und rechts hinten
        this.makeExtra(level, core.offset(-dir.getStepX() + rot.getStepX() * -4, 0, -dir.getStepZ() + rot.getStepZ() * -4));
        this.makeExtra(level, core.offset(dir.getStepX() + rot.getStepX() * -4, 0, dir.getStepZ() + rot.getStepZ() * -4));
        // Stromausgang und Dampfausgang
        this.makeExtra(level, core.offset(rot.getStepX() * 4, 1, rot.getStepZ() * 4));
        this.makeExtra(level, core.offset(rot.getStepX() * -5, 1, rot.getStepZ() * -5));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;

        BlockEntity be = level.getBlockEntity(corePos);
        if(!(be instanceof MachineTurbineGasBlockEntity turbine)) return;

        Direction dir = level.getBlockState(corePos).getValue(FACING);

        List<Component> text = new ArrayList<>();

        if(hitCheck(dir, corePos, -1, -1, 0, pos) || hitCheck(dir, corePos, 1, -1, 0, pos)) {
            text.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN).append(turbine.tanks[0].getTankType().getName()).withStyle(ChatFormatting.RESET));
            text.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN).append(turbine.tanks[1].getTankType().getName()).withStyle(ChatFormatting.RESET));
        }

        if(hitCheck(dir, corePos, -1, 4, 0, pos) || hitCheck(dir, corePos, 1, 4, 0, pos)) {
            text.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN).append(turbine.tanks[2].getTankType().getName()).withStyle(ChatFormatting.RESET));
        }

        if(hitCheck(dir, corePos, 0, 5, 1, pos)) {
            text.add(Component.literal("<- ").withStyle(ChatFormatting.RED).append(turbine.tanks[3].getTankType().getName()).withStyle(ChatFormatting.RESET));
        }

        if(hitCheck(dir, corePos, 0, -4, 1, pos)) {
            text.add(Component.literal("<- ").withStyle(ChatFormatting.RED).append("Power").withStyle(ChatFormatting.RESET));
        }

        if(!text.isEmpty()) {
            ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
        }
    }

    /** exDir laeuft entlang der Blickrichtung, exRot entlang der Gegenuhrzeigerdrehung, exY nach oben */
    protected boolean hitCheck(Direction dir, BlockPos corePos, int exDir, int exRot, int exY, BlockPos hitPos) {

        Direction turn = dir.getCounterClockWise(Axis.Y);

        int iX = corePos.getX() + dir.getStepX() * exDir + turn.getStepX() * exRot;
        int iY = corePos.getY() + exY;
        int iZ = corePos.getZ() + dir.getStepZ() * exDir + turn.getStepZ() * exRot;

        return iX == hitPos.getX() && iZ == hitPos.getZ() && iY == hitPos.getY();
    }
}
