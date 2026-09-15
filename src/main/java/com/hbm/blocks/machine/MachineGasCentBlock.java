package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.MachineGasCentBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineGasCent.
 *
 * Ein Block Grundflaeche, vier hoch. Der Kern steht unten, und dort haengen auch alle
 * Anschluesse -- die drei Bloecke darueber sind nur das Gehaeuse.
 *
 * NICHT UEBERNOMMEN: der Stellvertreterblock, den das Original fuer die oberen Bloecke
 * vorsieht. Er kaeme dort nie zum Einsatz: die Maschine ruft makeExtra an keiner Stelle, also
 * bleiben die drei oberen Bloecke gewoehnliche Fueller.
 *
 * NICHT UEBERNOMMEN: die schmalere Trefferflaeche des oberen Teils. Sie gehoert zum Modell des
 * Originals, das der Port nicht hat.
 */
public class MachineGasCentBlock extends DummyableBlock implements ITooltipProvider {

    public static final MapCodec<MachineGasCentBlock> CODEC = simpleCodec(MachineGasCentBlock::new);

    public MachineGasCentBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineGasCentBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TYPE) == DummyBlockType.CORE ? new MachineGasCentBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 3, 0, 0, 0, 0, 0 }; }
    @Override public int getOffset() { return 0; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
