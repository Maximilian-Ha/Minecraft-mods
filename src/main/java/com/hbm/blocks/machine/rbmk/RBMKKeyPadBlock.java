package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.IScreenProvider;
import com.hbm.blockentity.machine.rbmk.RBMKKeyPadBlockEntity;
import com.hbm.inventory.screens.RBMKKeyPadScreen;
import com.hbm.main.NuclearTechMod;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKKeyPad.
 * Die Tafel mit den vier Drucktasten.
 *
 * ABWEICHUNG wie bei der Hebeltafel: das Schleichen oeffnet die Einstellungen, nicht der
 * Schraubenzieher.
 */
public class RBMKKeyPadBlock extends RBMKMiniPanelBlock implements EntityBlock, IScreenProvider {

    public RBMKKeyPadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RBMKKeyPadBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if(player.isShiftKeyDown()) {
            NuclearTechMod.proxy.openScreen(player, pos);
            return InteractionResult.CONSUME;
        }

        if(level.isClientSide) return InteractionResult.SUCCESS;

        /* Nur die beiden grossen Flaechen zaehlen, nicht Deckel, Boden oder die schmalen Kanten. */
        if(!hitResult.getDirection().getAxis().isHorizontal()) return InteractionResult.SUCCESS;

        Vec3 hit = hitResult.getLocation();
        double hitX = hit.x - pos.getX();
        double hitY = hit.y - pos.getY();
        double hitZ = hit.z - pos.getZ();

        if(hitX == 0D || hitX == 1D || hitZ == 0D || hitZ == 1D) return InteractionResult.SUCCESS;

        if(!(level.getBlockEntity(pos) instanceof RBMKKeyPadBlockEntity keypad)) return InteractionResult.SUCCESS;

        int index = indexHit(state.getValue(BlockStateProperties.HORIZONTAL_FACING), hitX, hitZ);
        /* Untere Reihe, also die Tasten 3 und 4. */
        if(hitY < 0.5D) index += 2;

        keypad.keys[index].click();

        return InteractionResult.SUCCESS;
    }

    /** Welche Spalte getroffen wurde -- die Tafel ist laengs und quer geteilt. */
    private static int indexHit(Direction facing, double hitX, double hitZ) {
        return switch(facing) {
            case NORTH -> hitX < 0.5D ? 1 : 0;
            case SOUTH -> hitX > 0.5D ? 1 : 0;
            case WEST -> hitZ > 0.5D ? 1 : 0;
            case EAST -> hitZ < 0.5D ? 1 : 0;
            default -> 0;
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object provideScreen(Player player, BlockPos pos) {
        BlockEntity be = player.level().getBlockEntity(pos);
        if(be instanceof RBMKKeyPadBlockEntity keypad) return new RBMKKeyPadScreen(keypad);
        return null;
    }

    public static final MapCodec<RBMKKeyPadBlock> CODEC = simpleCodec(RBMKKeyPadBlock::new);
    @Override protected MapCodec<? extends RBMKKeyPadBlock> codec() { return CODEC; }
}
