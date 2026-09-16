package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.oil.MachineCatalyticCrackerBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineCatalyticCracker.
 *
 * Der groesste Verbund der Erdoelkette: fuenf Teilkoerper, alle an der Blickrichtung
 * ausgerichtet. Er hat keine Oberflaeche -- eingestellt wird er mit einem Fluidkennzeichner
 * in der Hand, abgelesen ueber die Einblendung beim Hinsehen.
 */
public class MachineCatalyticCrackerBlock extends DummyableBlock implements ILookOverlay {

    /** Die vier Teilkoerper neben dem Sockel, unveraendert aus dem Original. */
    private static final int[][] EXTRA_DIMENSIONS = {
            { 8, -1, 3, -1, 2, 0 },
            { 13, 0, 0, 3, 2, 1 },
            { 14, -13, -1, 2, 1, 0 },
            { 3, -1, 2, 3, -1, 3 }
    };

    public MachineCatalyticCrackerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineCatalyticCrackerBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    public static final MapCodec<MachineCatalyticCrackerBlock> CODEC = simpleCodec(MachineCatalyticCrackerBlock::new);
    @Override public MapCodec<MachineCatalyticCrackerBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {0, 0, 3, 3, 2, 3}; }
    @Override public int getOffset() { return 3; }

    /**
     * Ein Rechtsklick mit einem Fluidkennzeichner stellt den Eingabetank ein -- das ist die
     * einzige Bedienung, die dieser Verbund hat.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!(stack.getItem() instanceof IItemFluidIdentifier identifier)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!(level.getBlockEntity(corePos) instanceof MachineCatalyticCrackerBlockEntity cracker)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(!level.isClientSide) {
            FluidType type = identifier.getType(level, corePos, stack);
            cracker.tanks[0].setTankType(type);
            cracker.setChanged();
            player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                    .append(type.getName()).append(Component.literal("!")));
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {

        if(!super.checkRequirement(level, pos, dir, offset)) return false;

        BlockPos core = pos.relative(dir, offset);

        for(int[] dim : EXTRA_DIMENSIONS) {
            if(!MultiblockHandlerXR.checkSpace(level, core, dim, pos, dir)) return false;
        }

        return true;
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        for(int[] dim : EXTRA_DIMENSIONS) {
            MultiblockHandlerXR.fillSpace(level, core, dim, this, dir);
        }

        Direction rot = dir.getClockWise();

        /* Die acht Ecken des Sockels tragen die Anschluesse. */
        for(int sign : new int[] { 1, -1 }) {
            this.makeExtra(level, core.relative(dir, 3 * sign).relative(rot));
            this.makeExtra(level, core.relative(dir, 3 * sign).relative(rot.getOpposite(), 2));
            this.makeExtra(level, core.relative(dir, 2 * sign).relative(rot, 2));
            this.makeExtra(level, core.relative(dir, 2 * sign).relative(rot.getOpposite(), 3));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;
        if(!(level.getBlockEntity(corePos) instanceof MachineCatalyticCrackerBlockEntity cracker)) return;

        List<Component> text = new ArrayList<>();

        for(int i = 0; i < cracker.tanks.length; i++) {
            FluidTank tank = cracker.tanks[i];
            text.add(Component.literal(i < 2 ? "-> " : "<- ").withStyle(i < 2 ? ChatFormatting.GREEN : ChatFormatting.RED)
                    .append(tank.getTankType().getName())
                    .append(Component.literal(": " + tank.getFill() + "/" + tank.getMaxFill() + "mB"))
                    .withStyle(ChatFormatting.RESET));
        }

        ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
    }
}
