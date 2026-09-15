package com.hbm.blocks.machine.pile;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.pile.PileBaseBlockEntity;
import com.hbm.blockentity.machine.pile.PileCoreBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.states.PileBlockType;
import com.hbm.particle.helper.MarkerCreator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.pile.BlockPile.
 *
 * Woraus der zusammengebaute Chicago Pile besteht. Jeder Block weiss ueber seine
 * Zustands-Eigenschaft, welche Rolle er spielt; zusammengehalten wird das Ganze ueber die
 * Blockentitaeten, die alle auf denselben Kern zeigen.
 *
 * Wer einen Block herausschlaegt, macht die ganze Anlage kaputt: der Kern faellt zu Graphit
 * zurueck, und die uebrigen Bloecke merken das beim naechsten Takt und tun dasselbe.
 *
 * ABWEICHUNG: die Connected Textures des Originals (IBlockCT) sind gestrichen; das steht so in
 * ENTSCHEIDUNGEN.md.
 */
public class PileBlock extends BaseEntityBlock implements IToolable, ILookOverlay {

    public static final EnumProperty<PileBlockType> TYPE = EnumProperty.create("pile_type", PileBlockType.class);

    public PileBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, PileBlockType.DUMMY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TYPE) == PileBlockType.CORE
                ? new PileCoreBlockEntity(pos, state)
                : new PileBaseBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    /**
     * Beim Abbauen faellt der Block zu Graphit zurueck und reisst die Anlage mit. Waehrend einer
     * Kernschmelze gilt das nicht -- da fliegt ohnehin gerade alles auseinander.
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {

        if(!PileCoreBlockEntity.meltingDown && !state.is(newState.getBlock())) {

            BlockEntity be = level.getBlockEntity(pos);
            PileCoreBlockEntity core = be instanceof PileBaseBlockEntity pile ? pile.getCore() : null;
            boolean hadCore = be instanceof PileBaseBlockEntity pile && pile.hasCore;

            super.onRemove(state, level, pos, newState, isMoving);

            if(!(be instanceof PileBaseBlockEntity) || hadCore) {
                level.setBlock(pos, NtmBlocks.PILE_BRICK.get().defaultBlockState(), 3);
            }

            if(core != null && !core.isRemoved()) core.destroy();
            return;
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    /** Die Handbohrmaschine treibt Kanaele hinein -- und macht sie auf demselben Weg wieder zu. */
    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.HAND_DRILL) return false;

        BlockState state = level.getBlockState(pos);

        if(state.getValue(TYPE) == PileBlockType.CORE) {
            this.error(player, pos, "Cannot intersect core");
            return false;
        }

        if(level.getBlockEntity(pos) instanceof PileBaseBlockEntity pile) {

            if(level.isClientSide) return true;

            PileCoreBlockEntity core = pile.getCore();
            if(core != null) return core.drillChannel(pos, direction.getOpposite(), player);
        }

        this.error(player, pos, "No core found");
        return false;
    }

    private void error(Player player, BlockPos pos, String message) {
        if(player instanceof ServerPlayer serverPlayer) MarkerCreator.sendError(serverPlayer, pos, Component.literal(message));
    }

    /** Was beim Hinsehen ueber dem Block steht. */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        BlockState state = level.getBlockState(pos);
        List<Component> text = new ArrayList<>();

        switch(state.getValue(TYPE)) {
            case FUEL_IN -> text.add(Component.literal("Fuel Loading Port"));
            case FUEL_OUT -> text.add(Component.literal("Fuel Ejection Port"));
            case AIR_IN -> text.add(Component.literal("Air Inlet"));
            case AIR_OUT -> text.add(Component.literal("Air Outlet"));
            case CONTROL -> text.add(Component.literal("Control Rod Channel"));
            case CORE -> {
                if(level.getBlockEntity(pos) instanceof PileCoreBlockEntity core) {
                    text.add(Component.literal("Max Temp: " + Math.round(core.highestHeat) + " / " + PileCoreBlockEntity.MAX_HEAT + " C"));
                }
            }
            default -> { }
        }

        if(!text.isEmpty()) ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
    }

    public static final MapCodec<PileBlock> CODEC = simpleCodec(PileBlock::new);
    @Override protected MapCodec<PileBlock> codec() { return CODEC; }
}
