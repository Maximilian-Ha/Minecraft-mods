package com.hbm.blocks.machine.icf;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.icf.ICFControllerBlockEntity;
import com.hbm.blockentity.machine.icf.ICFWrapperBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.icf.ICFLaserComponentBlock.EnumICFPart;
import com.hbm.particle.helper.MarkerCreator;
import com.hbm.util.BobMathUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineICFController.
 *
 * Die Steuerung des ICF-Lasers. Ein Rechtsklick liest die Anlage vor ihr ein: von der Rueckseite
 * aus wird geflutet, bis nur noch Huellenteile den Rand bilden. Was dabei gefunden wird, zaehlt
 * die Blockentitaet aus; alle beteiligten Bloecke werden durch Stellvertreter ersetzt.
 *
 * Findet die Flutung etwas, das kein Laserbauteil ist, wird die Stelle rot eingerahmt und nichts
 * gebaut.
 *
 * ABWEICHUNG: das Original flutet rekursiv und geht dabei bis zu tausend Ebenen tief. Der Port
 * benutzt eine Liste -- dieselbe Entscheidung wie beim Druckwasserreaktor in Runde 54.
 */
public class ICFControllerBlock extends BaseEntityBlock implements ILookOverlay {

    /** Mehr Bloecke liest die Steuerung nicht ein. */
    public static final int MAX_SIZE = 1024;

    public ICFControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ICFControllerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        if(level.getBlockEntity(pos) instanceof ICFControllerBlockEntity controller && !controller.assembled) {
            this.assemble(level, pos, player, controller);
        }

        return InteractionResult.CONSUME;
    }

    /** Liest die Anlage ein und ersetzt sie durch Stellvertreter. */
    private void assemble(Level level, BlockPos pos, Player player, ICFControllerBlockEntity controller) {

        Map<BlockPos, Integer> assembly = new HashMap<>();
        Set<BlockPos> ports = new HashSet<>();
        Set<BlockPos> cells = new HashSet<>();
        Set<BlockPos> emitters = new HashSet<>();
        Set<BlockPos> capacitors = new HashSet<>();
        Set<BlockPos> turbochargers = new HashSet<>();

        Direction dir = controller.getFacing().getOpposite();

        boolean errored = this.floodFill(level, pos.relative(dir), player, assembly, ports, cells, emitters, capacitors, turbochargers);

        if(!errored) {

            for(Map.Entry<BlockPos, Integer> entry : assembly.entrySet()) {

                BlockPos partPos = entry.getKey();

                level.setBlock(partPos, NtmBlocks.ICF_BLOCK.get().defaultBlockState()
                        .setValue(ICFWrapperBlock.PORT, ports.contains(partPos)), 3);

                if(level.getBlockEntity(partPos) instanceof ICFWrapperBlockEntity wrapper) {
                    wrapper.part = entry.getValue();
                    wrapper.core = pos;
                    wrapper.setChanged();
                }
            }

            controller.setup(ports, cells, emitters, capacitors, turbochargers);
            controller.setChanged();
        }

        controller.assembled = !errored;
    }

    /**
     * Flutet von der Rueckseite der Steuerung aus. Huellenteile begrenzen die Flut, alles
     * andere Laserbauteil laeuft weiter. Gibt zurueck, ob dabei etwas schiefging.
     */
    private boolean floodFill(Level level, BlockPos start, Player player, Map<BlockPos, Integer> assembly,
            Set<BlockPos> ports, Set<BlockPos> cells, Set<BlockPos> emitters,
            Set<BlockPos> capacitors, Set<BlockPos> turbochargers) {

        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);

        while(!queue.isEmpty()) {

            BlockPos pos = queue.poll();
            if(assembly.containsKey(pos)) continue;

            if(assembly.size() >= MAX_SIZE) {
                this.error(player, pos, "Max size exceeded");
                return true;
            }

            BlockState state = level.getBlockState(pos);

            if(!state.is(NtmBlocks.ICF_LASER_COMPONENT.get())) {
                this.error(player, pos, "Non-laser block");
                return true;
            }

            int subtype = state.getValue(ICFLaserComponentBlock.SUBTYPE);
            EnumICFPart part = EnumICFPart.values()[subtype];

            assembly.put(pos, subtype);

            switch(part) {
                /* Die Huelle begrenzt die Flut. */
                case CASING -> { continue; }
                case PORT -> { ports.add(pos); continue; }
                case CELL -> cells.add(pos);
                case EMITTER -> emitters.add(pos);
                case CAPACITOR -> capacitors.add(pos);
                case TURBO -> turbochargers.add(pos);
            }

            for(Direction offset : Direction.values()) queue.add(pos.relative(offset));
        }

        return false;
    }

    private void error(Player player, BlockPos pos, String message) {
        if(player instanceof ServerPlayer serverPlayer) MarkerCreator.sendError(serverPlayer, pos, Component.literal(message));
    }

    @Override
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof ICFControllerBlockEntity controller)) return;

        List<Component> text = new ArrayList<>();
        text.add(Component.literal(BobMathUtil.getShortNumber(controller.getPower()) + "/"
                + BobMathUtil.getShortNumber(controller.getMaxPower()) + "HE"));

        ILookOverlay.printGeneric(event, this.getName(), 0xffff00, 0x404000, text);
    }

    public static final MapCodec<ICFControllerBlock> CODEC = simpleCodec(ICFControllerBlock::new);
    @Override protected MapCodec<ICFControllerBlock> codec() { return CODEC; }
}
