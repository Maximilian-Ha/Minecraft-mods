package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.RadarScreenBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineRadarScreen.
 *
 * Zwei mal zwei, zwei hoch. Er hat keine Oberflaeche -- was er zeigt, zeigt er auf sich selbst,
 * und ein Renderer dafuer fehlt dem Port noch. Bis dahin ist er ein Empfaenger ohne Bild.
 *
 * ABWEICHUNG: das Original faengt den Rechtsklick ab und oeffnet clientseitig nichts weiter. Hier
 * bleibt der Klick unbehandelt, was auf 1.21 dasselbe bedeutet und eine Zeile weniger ist.
 */
public class RadarScreenBlock extends DummyableBlock implements ITooltipProvider {

    public static final MapCodec<RadarScreenBlock> CODEC = simpleCodec(RadarScreenBlock::new);

    public RadarScreenBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<RadarScreenBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TYPE) == DummyBlockType.CORE ? new RadarScreenBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 1, 0, 0, 0, 1, 0 }; }
    @Override public int getOffset() { return 0; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
