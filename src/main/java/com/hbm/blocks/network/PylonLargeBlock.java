package com.hbm.blocks.network;

import com.hbm.blockentity.network.PylonLargeBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Grosser Mast (red_pylon_large) */
public class PylonLargeBlock extends PylonBaseBlock {

    public PylonLargeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TYPE) == DummyBlockType.CORE ? new PylonLargeBlockEntity(pos, state) : null;
    }

    public static final MapCodec<PylonLargeBlock> CODEC = simpleCodec(PylonLargeBlock::new);
    @Override protected MapCodec<PylonLargeBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {13, 0, 1, 1, 1, 1}; }
    @Override public int getOffset() { return 0; }

    /**
     * Original getMetaForCore: der Blickwinkel wird in 45-Grad-Schritten auf vier Richtungen
     * abgebildet, daher die Diagonalstellungen des grossen Mastes.
     */
    @Override
    protected BlockState getStateForCore(Level level, BlockPos pos, Player player, Direction dir) {

        int i = Mth.floor(player.getYRot() * 4.0F / 180.0F + 0.5D) & 3;

        Direction facing = switch(i) {
            case 0 -> Direction.NORTH;
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            default -> Direction.WEST;
        };

        return this.createCoreState(facing);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("Connection Type: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Quadruple").withStyle(ChatFormatting.YELLOW)));
        components.add(Component.literal("Connection Range: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("100m").withStyle(ChatFormatting.YELLOW)));
        components.add(Component.literal("This pylon requires a substation!").withStyle(ChatFormatting.GOLD));
    }
}
