package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.TowerSmallBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ILookOverlay;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineTowerSmall.
 *
 * Der kleine Kuehlturm. Er wandelt Abdampf zurueck in Wasser, ohne Strom zu brauchen -- die Rechnung
 * steht in TowerSmallBlockEntity, die Form hier.
 *
 * In der Aussenwand, zwei Bloecke vom Kern in jede Himmelsrichtung, macht fillSpace aus dem
 * Hilfsblock einen EXTRA-Block, der Fluessigkeit durchreicht; das Rohr kommt dahinter an.
 */
public class MachineTowerSmallBlock extends DummyableBlock implements ILookOverlay {

    public static final MapCodec<MachineTowerSmallBlock> CODEC = simpleCodec(MachineTowerSmallBlock::new);

    public MachineTowerSmallBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineTowerSmallBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new TowerSmallBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 18, 0, 2, 2, 2, 2 }; }
    @Override public int getOffset() { return 2; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        /* Je ein Anschluss zwei Bloecke vom Kern in jede Himmelsrichtung. */
        for(Direction side : Direction.Plane.HORIZONTAL) {
            this.makeExtra(level, core.relative(side, 2));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;
        if(!(level.getBlockEntity(corePos) instanceof TowerSmallBlockEntity tower)) return;

        List<Component> text = new ArrayList<>();
        for(int i = 0; i < tower.tanks.length; i++) {
            text.add(Component.literal(i < 1 ? "-> " : "<- ").withStyle(i < 1 ? ChatFormatting.GREEN : ChatFormatting.RED)
                    .append(tower.tanks[i].getTankType().getName().copy().withStyle(ChatFormatting.RESET))
                    .append(Component.literal(": " + tower.tanks[i].getFill() + "/" + tower.tanks[i].getMaxFill() + "mB")));
        }

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }
}
