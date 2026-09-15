package com.hbm.blocks.network;

import api.hbm.block.IToolable;
import com.hbm.items.tools.ToolingItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.BlockCraneBase.
 *
 * Die Kranmaschinen sind das Bindeglied zwischen Foerderband und Maschine. Jede von ihnen hat
 * eine Eingangs- und eine Ausgangsseite; welche das sind, stellt der Schraubenzieher ein --
 * ohne Schleichtaste die Eingangsseite, mit ihr die Ausgangsseite, jeweils die angeklickte
 * Flaeche.
 *
 * ABWEICHUNG: im Original steckt die Eingangsseite in den Blockmetadaten und die Ausgangsseite
 * in der Blockentitaet, wo sie eigens uebers Netz geschickt werden muss. Auf 1.21 stehen beide
 * im Blockstate. Das ist nicht nur weniger Code, es ist auch noetig: nach dem Blockstate wird
 * das Modell gewaehlt, und die Maschine zeigt an ihren Flaechen, wo ein und wo aus geht.
 *
 * Beide Seiten koennen nicht dieselbe sein. Wer eine Seite auf die andere legt, schiebt die
 * andere auf die Gegenseite -- so haelt es auch das Original.
 */
public abstract class CraneBaseBlock extends BaseEntityBlock implements IToolable {

    public static final DirectionProperty INPUT = DirectionProperty.create("input", Direction.values());
    public static final DirectionProperty OUTPUT = DirectionProperty.create("output", Direction.values());

    public CraneBaseBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(INPUT, Direction.NORTH)
                .setValue(OUTPUT, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INPUT, OUTPUT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Gesetzt wird so, dass der Eingang zum Setzenden zeigt: man stellt sich vor die Maschine
     * und sie nimmt von dort an, was man ihr bringt.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction input = context.getHorizontalDirection();
        return this.defaultBlockState().setValue(INPUT, input).setValue(OUTPUT, input.getOpposite());
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, @Nullable Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER || direction == null) return false;

        BlockState state = level.getBlockState(pos);

        if(player.isShiftKeyDown()) {
            level.setBlock(pos, withOutput(state, direction), 3);
        } else {
            level.setBlock(pos, withInput(state, direction), 3);
        }

        return true;
    }

    /** Legt den Eingang auf die Seite. Lag dort der Ausgang, weicht der auf die Gegenseite aus. */
    private static BlockState withInput(BlockState state, Direction input) {
        BlockState result = state.setValue(INPUT, input);
        if(result.getValue(OUTPUT) == input) result = result.setValue(OUTPUT, input.getOpposite());
        return result;
    }

    /** Und umgekehrt fuer den Ausgang. */
    private static BlockState withOutput(BlockState state, Direction output) {
        BlockState result = state.setValue(OUTPUT, output);
        if(result.getValue(INPUT) == output) result = result.setValue(INPUT, output.getOpposite());
        return result;
    }

    /**
     * Ein Klick oeffnet das Fenster -- ausser mit einem Werkzeug in der Hand. Dann tritt der
     * Block zur Seite und laesst das Werkzeug schrauben.
     *
     * DAS IST NOETIG, obwohl der Schraubenzieher sich seit Runde 99 ueber onItemUseFirst VOR
     * den Block stellt: er tut das nur, wenn onScrew auch angeschlagen hat. Ein Handbohrer,
     * mit dem an einer Kranmaschine nichts einzustellen ist, faende sonst hier ein Fenster
     * statt gar nichts. Das Original tritt aus demselben Grund fuer JEDES Werkzeug zur Seite.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {

        if(stack.getItem() instanceof ToolingItem) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;

        if(level.getBlockEntity(pos) instanceof MenuProvider provider) {
            player.openMenu(provider, pos);
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static Direction getInput(BlockState state) { return state.getValue(INPUT); }
    public static Direction getOutput(BlockState state) { return state.getValue(OUTPUT); }
}
