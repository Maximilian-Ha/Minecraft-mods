package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.DeuteriumTowerBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.DeuteriumTower.
 *
 * Zehn Bloecke hoch und zwei mal zwei breit. Die drei zusaetzlichen Fussbloecke setzt
 * fillSpace nach, weil sie ausserhalb des Quaders aus getDimensions liegen.
 */
public class DeuteriumTowerBlock extends DummyableBlock implements ILookOverlay {

    public static final MapCodec<DeuteriumTowerBlock> CODEC = simpleCodec(DeuteriumTowerBlock::new);

    public DeuteriumTowerBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<DeuteriumTowerBlock> codec() { return CODEC; }

    @Override public int[] getDimensions() { return new int[] { 9, 0, 1, 0, 0, 1 }; }
    @Override public int getOffset() { return 0; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new DeuteriumTowerBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).power().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        Direction seite = dir.getClockWise();
        BlockPos kern = pos.relative(dir, offset);

        this.makeExtra(level, kern.relative(dir.getOpposite()).relative(seite.getOpposite()));
        this.makeExtra(level, kern.relative(seite.getOpposite()));
        this.makeExtra(level, kern.relative(dir.getOpposite()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos kern = this.findCore(level, pos);
        if(kern == null) return;
        if(!(level.getBlockEntity(kern) instanceof DeuteriumTowerBlockEntity turm)) return;

        ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000,
                MachineDeuteriumExtractorBlock.tankZeilen(turm));
    }
}
