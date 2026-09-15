package com.hbm.blocks.machine;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.MachineAutosawBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineAutosaw.
 *
 * Ein einzelner Block ohne Bauwerk und ohne Oberflaeche. Bedient wird er mit zwei Griffen: die
 * Fluidkennung in der Hand setzt den Treibstoff, der Schraubendreher haelt ihn an.
 *
 * SIE HAT KEINE ANZEIGE UND BRAUCHT KEINE. Beim Hinsehen nennt sie ihren Tank; mehr gibt es
 * nicht einzustellen.
 */
public class MachineAutosawBlock extends BaseEntityBlock implements ILookOverlay, ITooltipProvider, IToolable {

    public static final MapCodec<MachineAutosawBlock> CODEC = simpleCodec(MachineAutosawBlock::new);

    public MachineAutosawBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineAutosawBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineAutosawBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    /** Das Original zeichnet den Block ausschliesslich ueber den TESR (getRenderType() == -1). */
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(!(stack.getItem() instanceof IItemFluidIdentifier identifier)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if(!(level.getBlockEntity(pos) instanceof MachineAutosawBlockEntity saw)) return ItemInteractionResult.FAIL;

        FluidType type = identifier.getType(level, pos, stack);

        if(MachineAutosawBlockEntity.acceptedFuels.contains(type)) {
            saw.tank.setTankType(type);
            saw.setChanged();
            player.displayClientMessage(
                    Component.literal("Changed type to ").append(type.getName()).append(Component.literal("!")).withStyle(ChatFormatting.YELLOW),
                    false);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER) return false;
        if(!(level.getBlockEntity(pos) instanceof MachineAutosawBlockEntity saw)) return false;

        saw.isSuspended = !saw.isSuspended;
        saw.setChanged();

        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof MachineAutosawBlockEntity saw)) return;

        List<Component> text = new ArrayList<>();
        text.add(Component.empty().append(saw.tank.getTankType().getName())
                .append(Component.literal(": " + saw.tank.getFill() + "/" + saw.tank.getMaxFill() + "mB")));

        if(saw.isSuspended) {
            text.add(Component.literal("! ").append(Component.translatable(this.getDescriptionId() + ".suspended")).append(Component.literal(" !")).withStyle(ChatFormatting.RED));
        }

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
