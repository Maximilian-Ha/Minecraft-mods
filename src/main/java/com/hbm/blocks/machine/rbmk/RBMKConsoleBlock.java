package com.hbm.blocks.machine.rbmk;

import api.hbm.block.IToolable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.main.NuclearTechMod;
import com.hbm.network.toclient.OpenScreenPacket;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKConsole.
 *
 * Das Reaktorpult, ein 5x1x3-Bau. Seine Oberflaeche hat kein Inventar und wird deshalb ueber
 * {@link OpenScreenPacket} geoeffnet statt ueber einen MenuProvider.
 */
public class RBMKConsoleBlock extends DummyableBlock implements IToolable {

    /** Kennung der Oberflaeche, siehe com.hbm.inventory.screens.NoContainerScreens. */
    public static final ResourceLocation SCREEN = NuclearTechMod.withDefaultNamespace("rbmk_console");

    public RBMKConsoleBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<RBMKConsoleBlock> CODEC = simpleCodec(RBMKConsoleBlock::new);

    @Override
    protected MapCodec<? extends RBMKConsoleBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return new RBMKConsoleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override public int[] getDimensions() { return new int[] {0, 0, 0, 1, 2, 2}; }
    @Override public int getOffset() { return 1; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(player.isShiftKeyDown()) return InteractionResult.PASS;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return InteractionResult.PASS;
        if(!(level.getBlockEntity(corePos) instanceof RBMKConsoleBlockEntity)) return InteractionResult.PASS;

        if(level.isClientSide) return InteractionResult.SUCCESS;

        if(player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new OpenScreenPacket(SCREEN, corePos));
        }

        return InteractionResult.CONSUME;
    }

    /** Der Schraubenzieher dreht das Raster um neunzig Grad. */
    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER) return false;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return false;
        if(!(level.getBlockEntity(corePos) instanceof RBMKConsoleBlockEntity console)) return false;

        if(!level.isClientSide) console.rotate();

        return true;
    }
}
