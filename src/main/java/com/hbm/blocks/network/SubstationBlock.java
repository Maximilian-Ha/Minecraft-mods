package com.hbm.blocks.network;

import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.network.SubstationBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Umspannwerk (substation) */
public class SubstationBlock extends PylonBaseBlock {

    public SubstationBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new SubstationBlockEntity(pos, state);
            // im Original TileEntityProxyConductor an den vier Eckbloecken
            case EXTRA -> new ProxyComboBlockEntity(pos, state).conductor();
            default -> null;
        };
    }

    public static final MapCodec<SubstationBlock> CODEC = simpleCodec(SubstationBlock::new);
    @Override protected MapCodec<SubstationBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] {4, 0, 1, 1, 2, 2}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        int x = pos.getX() + dir.getStepX() * offset;
        int y = pos.getY();
        int z = pos.getZ() + dir.getStepZ() * offset;

        this.makeExtra(level, new BlockPos(x + 1, y, z + 1));
        this.makeExtra(level, new BlockPos(x + 1, y, z - 1));
        this.makeExtra(level, new BlockPos(x - 1, y, z + 1));
        this.makeExtra(level, new BlockPos(x - 1, y, z - 1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("Connection Type: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Quadruple").withStyle(ChatFormatting.YELLOW)));
        components.add(Component.literal("Connection Range: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("20m").withStyle(ChatFormatting.YELLOW)));
    }
}
