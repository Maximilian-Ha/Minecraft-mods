package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.TeslaBlockEntity;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineTesla.
 *
 * Der Block selbst ist leer -- er zeigt nichts an, dreht sich nicht und hat keine
 * Oberflaeche. Alles steckt in der Blockentitaet und im Darsteller.
 *
 * Wie im Original bleibt der Umriss ein voller Wuerfel, obwohl das Modell schlanker ist:
 * so steht man nicht in der Spule, waehrend sie schlaegt.
 */
public class TeslaBlock extends BaseEntityBlock implements ITooltipProvider {

    public static final MapCodec<TeslaBlock> CODEC = simpleCodec(TeslaBlock::new);

    public TeslaBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<TeslaBlock> codec() { return CODEC; }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TeslaBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, p, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }
}
