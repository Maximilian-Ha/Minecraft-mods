package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.MachinePWRControllerBlockEntity;
import com.hbm.blockentity.machine.PWRBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.particle.helper.MarkerCreator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachinePWRController.
 *
 * Die Steuerung des Druckwasserreaktors und zugleich das Werkzeug, mit dem er gebaut wird.
 *
 * Der erste Rechtsklick liest die Anlage ein: von dem Block hinter der Steuerung aus laeuft eine
 * Flutfuellung durch den Kern, bis sie ueberall auf Gehaeuse stoesst. Was sie unterwegs findet,
 * wird gezaehlt; was nicht dazugehoert, bricht den Vorgang ab und wird dem Spieler rot markiert.
 *
 * Erst wenn es gelingt, werden alle gefundenen Bloecke durch Stellvertreter ersetzt und die
 * Anlage laeuft. Ab dann oeffnet derselbe Rechtsklick die Oberflaeche.
 *
 * ABWEICHUNG: das Original ruft die Flutfuellung rekursiv auf -- bei viertausend Bloecken ist
 * das ein tiefer Stapel. Hier laeuft sie ueber eine eigene Liste; das Ergebnis ist dasselbe.
 */
public class MachinePWRControllerBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Mehr Bloecke liest die Steuerung nicht ein. */
    private static final int MAX_SIZE = 4096;

    public static final MapCodec<MachinePWRControllerBlock> CODEC = simpleCodec(MachinePWRControllerBlock::new);

    public MachinePWRControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<MachinePWRControllerBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachinePWRControllerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        if(!(level.getBlockEntity(pos) instanceof MachinePWRControllerBlockEntity controller)) return InteractionResult.PASS;

        if(!controller.assembled) {
            assemble(level, pos, state, controller, player);
            return InteractionResult.CONSUME;
        }

        if(controller instanceof MenuProvider menu) {
            player.openMenu(new SimpleMenuProvider(menu, menu.getDisplayName()), pos);
        }

        return InteractionResult.CONSUME;
    }

    /** Liest die Anlage ein und ersetzt sie bei Erfolg durch Stellvertreter. */
    public static void assemble(Level level, BlockPos pos, BlockState state, MachinePWRControllerBlockEntity controller, Player player) {

        Map<BlockPos, Block> assembly = new LinkedHashMap<>();
        Map<BlockPos, Block> fuelRods = new HashMap<>();
        int sources = 0;

        assembly.put(pos, state.getBlock());

        /* Eingelesen wird nach hinten -- die Steuerung schaut aus dem Reaktor heraus. */
        Direction into = state.getValue(FACING).getOpposite();

        boolean errored = !floodFill(level, pos.relative(into), assembly, fuelRods, player);

        for(Block block : assembly.values()) {
            if(block == NtmBlocks.PWR_NEUTRON_SOURCE.get()) sources++;
        }

        if(fuelRods.isEmpty()) {
            sendError(player, pos, "Fuel rods required");
            errored = true;
        }

        if(sources == 0) {
            sendError(player, pos, "Neutron sources required");
            errored = true;
        }

        if(!errored) {

            for(Map.Entry<BlockPos, Block> entry : assembly.entrySet()) {

                BlockPos partPos = entry.getKey();
                Block block = entry.getValue();

                if(block == NtmBlocks.PWR_CONTROLLER.get()) continue;

                boolean port = block == NtmBlocks.PWR_PORT.get();
                level.setBlock(partPos, NtmBlocks.PWR_BLOCK.get().defaultBlockState().setValue(PWRBlock.PORT, port), 3);

                if(level.getBlockEntity(partPos) instanceof PWRBlockEntity pwr) {
                    pwr.block = block;
                    pwr.core = pos.immutable();
                    pwr.setChanged();
                }
            }

            controller.setup(assembly, fuelRods);
        }

        controller.assembled = !errored;
        controller.setChanged();
    }

    /**
     * Laeuft vom Startblock aus durch den Kern. Gehaeuse, Reflektor und Anschlussstelle beenden
     * einen Zweig, alles andere Reaktorbauteil setzt ihn fort. Ein fremder Block ist ein Fehler.
     *
     * @return ob alles zusammenpasst
     */
    private static boolean floodFill(Level level, BlockPos start, Map<BlockPos, Block> assembly, Map<BlockPos, Block> fuelRods, Player player) {

        Deque<BlockPos> open = new ArrayDeque<>();
        open.add(start);

        boolean ok = true;

        while(!open.isEmpty()) {

            BlockPos pos = open.poll();

            if(assembly.containsKey(pos)) continue;

            if(assembly.size() >= MAX_SIZE) {
                sendError(player, pos, "Max size exceeded");
                return false;
            }

            Block block = level.getBlockState(pos).getBlock();

            if(isValidCasing(block)) {
                assembly.put(pos, block);
                continue;
            }

            if(isValidCore(block)) {

                assembly.put(pos, block);
                if(block == NtmBlocks.PWR_FUEL_CHANNEL.get()) fuelRods.put(pos, block);

                for(Direction dir : Direction.values()) open.add(pos.relative(dir));
                continue;
            }

            sendError(player, pos, "Non-reactor block");
            ok = false;
        }

        return ok;
    }

    private static void sendError(Player player, BlockPos pos, String message) {
        if(player instanceof ServerPlayer serverPlayer) {
            MarkerCreator.sendError(serverPlayer, pos, Component.literal(message));
        }
    }

    private static boolean isValidCore(Block block) {
        return block == NtmBlocks.PWR_FUEL_CHANNEL.get()
                || block == NtmBlocks.PWR_CONTROL.get()
                || block == NtmBlocks.PWR_CHANNEL.get()
                || block == NtmBlocks.PWR_HEATEX.get()
                || block == NtmBlocks.PWR_HEATSINK.get()
                || block == NtmBlocks.PWR_NEUTRON_SOURCE.get();
    }

    private static boolean isValidCasing(Block block) {
        return block == NtmBlocks.PWR_CASING.get()
                || block == NtmBlocks.PWR_REFLECTOR.get()
                || block == NtmBlocks.PWR_PORT.get();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {

        if(!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MachinePWRControllerBlockEntity controller) {
            Containers.dropContents(level, pos, controller);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
