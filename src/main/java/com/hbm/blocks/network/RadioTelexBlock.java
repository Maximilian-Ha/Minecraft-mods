package com.hbm.blocks.network;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.network.RadioTelexBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blockentity.IScreenProvider;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.screens.RadioTelexScreen;
import com.hbm.main.NuclearTechMod;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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
 * Portiert aus 1.7.10: com.hbm.blocks.network.RadioTelex.
 *
 * Zwei Bloecke breit, Abmessungen und Versatz unveraendert aus dem Original. Er hat keinen
 * Behaelter, sondern nur eine Oberflaeche -- deshalb nicht standardOpenBehavior, sondern der
 * Weg ueber den Proxy wie beim Funkturm.
 */
public class RadioTelexBlock extends DummyableBlock implements IScreenProvider, ITooltipProvider {

    public static final MapCodec<RadioTelexBlock> CODEC = simpleCodec(RadioTelexBlock::new);

    public RadioTelexBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<RadioTelexBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] { 0, 0, 0, 0, 1, 0 }; }
    @Override public int getOffset() { return 0; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new RadioTelexBlockEntity(pos, state);
            default -> new ProxyComboBlockEntity(pos, state);
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        BlockPos core = this.findCore(level, pos);
        if(core == null) return InteractionResult.FAIL;

        NuclearTechMod.proxy.openScreen(player, core);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public Object provideScreen(Player player, BlockPos pos) {
        BlockEntity be = player.level.getBlockEntity(pos);
        if(be instanceof RadioTelexBlockEntity telex) return new RadioTelexScreen(telex);
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
