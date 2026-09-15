package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachineExposureChamberBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineExposureChamber.
 *
 * Ein langgestreckter Bau: fuenf mal fuenf um den Kern, dazu ein Rohr von acht Bloecken zur
 * Seite, zwei Fluegel zwei Lagen darueber und ein Kopfstueck am Ende. Das Original fuehrt die
 * vier Zusatzgrundrisse in getAllDimensions; der Port schreibt sie aus und legt sie ueber
 * dieselbe Pruefung wie die Hauptflaeche -- das Verfahren des Baggers aus Runde 131.
 *
 * DIE ANSCHLUESSE LIEGEN AM FERNEN ENDE, sieben bis neun Bloecke seitlich des Kerns: dort steht
 * im Original der Strahleingang.
 */
public class MachineExposureChamberBlock extends DummyableBlock implements ITooltipProvider {

    /** Das Strahlrohr zur Seite und die beiden Fluegel darueber. */
    private static final int[] DIM_TUBE = new int[] { 3, 0, 0, 0, -3, 8 };
    private static final int[] DIM_WING_LEFT = new int[] { 0, 0, 1, -1, -3, 6 };
    private static final int[] DIM_WING_RIGHT = new int[] { 0, 0, -1, 1, -3, 6 };

    /** Das Kopfstueck am fernen Ende des Rohrs. */
    private static final int[] DIM_HEAD_LEFT = new int[] { 3, 0, 1, -1, 0, 1 };
    private static final int[] DIM_HEAD_RIGHT = new int[] { 3, 0, -1, 1, 0, 1 };

    public static final MapCodec<MachineExposureChamberBlock> CODEC = simpleCodec(MachineExposureChamberBlock::new);

    public MachineExposureChamberBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MachineExposureChamberBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new MachineExposureChamberBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().power();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 4, 0, 2, 2, 2, 2 }; }
    @Override public int getOffset() { return 2; }

    /** Das Rohr laeuft entgegen der Drehrichtung -- im Original rot = dir.getRotation(UP).getOpposite(). */
    private static Direction beamDir(Direction dir) {
        return dir.getClockWise(Axis.Y).getOpposite();
    }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {

        BlockPos core = pos.relative(dir, offset);
        BlockPos head = core.relative(beamDir(dir), 7);

        return super.checkRequirement(level, pos, dir, offset)
                && MultiblockHandlerXR.checkSpace(level, core, DIM_TUBE, pos, dir)
                && MultiblockHandlerXR.checkSpace(level, core.above(2), DIM_WING_LEFT, pos, dir)
                && MultiblockHandlerXR.checkSpace(level, core.above(2), DIM_WING_RIGHT, pos, dir)
                && MultiblockHandlerXR.checkSpace(level, head, DIM_HEAD_LEFT, pos, dir)
                && MultiblockHandlerXR.checkSpace(level, head, DIM_HEAD_RIGHT, pos, dir);
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {

        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);
        Direction rot = beamDir(dir);
        BlockPos head = core.relative(rot, 7);

        MultiblockHandlerXR.fillSpace(level, core, DIM_TUBE, this, dir);
        MultiblockHandlerXR.fillSpace(level, core.above(2), DIM_WING_LEFT, this, dir);
        MultiblockHandlerXR.fillSpace(level, core.above(2), DIM_WING_RIGHT, this, dir);
        MultiblockHandlerXR.fillSpace(level, head, DIM_HEAD_LEFT, this, dir);
        MultiblockHandlerXR.fillSpace(level, head, DIM_HEAD_RIGHT, this, dir);

        /* Die fuenf Anschlussstellen am fernen Ende. */
        this.makeExtra(level, head.relative(dir));
        this.makeExtra(level, head.relative(dir, -1));
        this.makeExtra(level, core.relative(rot, 8).relative(dir));
        this.makeExtra(level, core.relative(rot, 8).relative(dir, -1));
        this.makeExtra(level, core.relative(rot, 8));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        this.addStandardInfo(components);
    }
}
