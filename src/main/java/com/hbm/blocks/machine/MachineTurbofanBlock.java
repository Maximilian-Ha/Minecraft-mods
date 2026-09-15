package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineTurbofanBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineTurbofan.
 *
 * Mehrblock-Huelle des Turbofans. Abmessungen, Versatz und die Lage der
 * Anschlussbloecke (makeExtra) sind unveraendert aus dem Original uebernommen.
 */
public class MachineTurbofanBlock extends DummyableBlock implements ITooltipProvider {

    public MachineTurbofanBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineTurbofanBlock> CODEC = simpleCodec(MachineTurbofanBlock::new);
    @Override public MapCodec<MachineTurbofanBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineTurbofanBlockEntity(pos, state);
            // Original: new TileEntityProxyCombo().fluid().power()
            case EXTRA -> new ProxyComboBlockEntity(pos, state).fluid().power();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 1, 1, 3, 3 }; }
    @Override public int getOffset() { return 1; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    /**
     * Anders als bei der Gasturbine geht das Original hier NICHT vom Kern aus,
     * sondern von der uebergebenen Position -- das bleibt hier genauso.
     */
    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        Direction rot = dir.getClockWise(Axis.Y);

        this.makeExtra(level, pos);
        this.makeExtra(level, pos.offset(-rot.getStepX(), 0, -rot.getStepZ()));
        this.makeExtra(level, pos.offset(-dir.getStepX() * 2, 0, -dir.getStepZ() * 2));
        this.makeExtra(level, pos.offset(-dir.getStepX() * 2 - rot.getStepX(), 0, -dir.getStepZ() * 2 - rot.getStepZ()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("Fuel efficiency:").withStyle(ChatFormatting.YELLOW));
        components.add(Component.literal("-").withStyle(ChatFormatting.YELLOW)
                .append(FuelGrade.AERO.getLocalizedName().withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(": ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("100%").withStyle(ChatFormatting.RED)));
    }
}
