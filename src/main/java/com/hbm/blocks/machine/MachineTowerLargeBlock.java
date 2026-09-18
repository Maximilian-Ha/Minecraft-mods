package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.TowerLargeBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineTowerLarge.
 *
 * Der grosse Kuehlturm. Er wandelt Abdampf zurueck in Wasser, ohne Strom zu brauchen -- die Rechnung
 * steht in TowerLargeBlockEntity, die Form hier.
 *
 * Die zwoelf Anschluesse sitzen in der Aussenwand, vier Bloecke vom Kern entfernt; dort macht
 * fillSpace aus dem Hilfsblock einen EXTRA-Block, der Fluessigkeit durchreicht.
 */
public class MachineTowerLargeBlock extends DummyableBlock implements ILookOverlay {

    public static final MapCodec<MachineTowerLargeBlock> CODEC = simpleCodec(MachineTowerLargeBlock::new);

    public MachineTowerLargeBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineTowerLargeBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new TowerLargeBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 12, 0, 4, 4, 4, 4 }; }
    @Override public int getOffset() { return 4; }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        /* Zwoelf Anschluesse in der Aussenwand: je Himmelsrichtung vier Bloecke vom Kern,
         * mittig und je drei Bloecke nach beiden Seiten versetzt. */
        for(Direction side : Direction.Plane.HORIZONTAL) {
            Direction rot = side.getClockWise();
            BlockPos wall = core.relative(side, 4);
            this.makeExtra(level, wall);
            this.makeExtra(level, wall.relative(rot, 3));
            this.makeExtra(level, wall.relative(rot, -3));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return;
        if(!(level.getBlockEntity(corePos) instanceof TowerLargeBlockEntity tower)) return;

        List<Component> text = new ArrayList<>();
        for(int i = 0; i < tower.tanks.length; i++) {
            text.add(Component.literal(i < 1 ? "-> " : "<- ").withStyle(i < 1 ? ChatFormatting.GREEN : ChatFormatting.RED)
                    .append(tower.tanks[i].getTankType().getName().copy().withStyle(ChatFormatting.RESET))
                    .append(Component.literal(": " + tower.tanks[i].getFill() + "/" + tower.tanks[i].getMaxFill() + "mB")));
        }

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }
}
