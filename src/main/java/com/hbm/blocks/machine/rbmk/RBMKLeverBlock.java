package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.IScreenProvider;
import com.hbm.blockentity.machine.rbmk.RBMKLeverBlockEntity;
import com.hbm.inventory.screens.RBMKLeverScreen;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKLever.
 * Die Tafel mit den beiden Kipphebeln.
 *
 * ABWEICHUNG: im Original braucht man einen Schraubenzieher, um an die Einstellungen zu kommen,
 * weil der blosse Rechtsklick den Hebel umlegt. Hier tut das Schleichen denselben Dienst -- so
 * wie bei den beiden anderen kleinen Tafeln, die den Schraubenzieher ebenfalls nicht verlangen.
 */
public class RBMKLeverBlock extends RBMKMiniPanelBlock implements EntityBlock, IScreenProvider {

    public RBMKLeverBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RBMKLeverBlockEntity(pos, state);
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
        double hitZ = hit.z - pos.getZ();

        if(hitX == 0D || hitX == 1D || hitZ == 0D || hitZ == 1D) return InteractionResult.SUCCESS;

        if(!(level.getBlockEntity(pos) instanceof RBMKLeverBlockEntity lever)) return InteractionResult.SUCCESS;

        lever.levers[indexHit(state.getValue(BlockStateProperties.HORIZONTAL_FACING), hitX, hitZ)].click();

        return InteractionResult.SUCCESS;
    }

    /** Welcher der beiden Hebel getroffen wurde -- die Tafel ist laengs geteilt. */
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
        if(be instanceof RBMKLeverBlockEntity lever) return new RBMKLeverScreen(lever);
        return null;
    }

    public static final MapCodec<RBMKLeverBlock> CODEC = simpleCodec(RBMKLeverBlock::new);
    @Override protected MapCodec<? extends RBMKLeverBlock> codec() { return CODEC; }
}
