package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.CraneRouterBlockEntity;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.item.MovingItem;
import com.hbm.entity.item.MovingPackage;
import com.hbm.items.tools.ToolingItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CraneRouter.
 *
 * Der Verteiler nimmt von JEDER Seite an -- er ist eine Kreuzung, keine Maschine mit Vorder-
 * und Rueckseite -- und schickt jeden Gegenstand dorthin, wo sein Muster passt. Die Entscheidung
 * trifft die Blockentitaet; hier steht, was danach mit dem Gegenstand geschieht.
 *
 * EIN PAKET WIRD NEU GESCHNUERT, nicht aufgeloest. Was in dieselbe Richtung geht, faehrt auch
 * gemeinsam weiter -- sonst wuerde ein Verteiler mitten in der Strecke die Ersparnis wieder
 * zunichte machen, um derentwillen es das Paket gibt. Nur wo ein einzelner Stapel uebrig
 * bleibt, faehrt er auch einzeln.
 *
 * LIEGT AUF DER GEWAEHLTEN SEITE KEIN BAND, faellt die Ware dort zu Boden. Sie ist dann
 * immerhin sichtbar, statt im Verteiler zu verschwinden.
 *
 * ABWEICHUNG: das Original zeichnet den Block in sieben Durchgaengen, um jede seiner sechs
 * Flaechen in einer eigenen Farbe einzufaerben -- rot, orange, gelb, gruen, blau, violett --,
 * damit man von aussen sieht, welche Seite welche ist. Der Port zeichnet ihn einfarbig; die
 * Zuordnung steht im Fenster, wo die sechs Filter ohnehin nebeneinander liegen.
 */
public class CraneRouterBlock extends BaseEntityBlock implements IEnterableBlock {

    public static final MapCodec<CraneRouterBlock> CODEC = simpleCodec(CraneRouterBlock::new);

    public CraneRouterBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraneRouterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, NtmBlockEntityTypes.CRANE_ROUTER.get(), (l, p, s, be) -> be.updateEntity());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {

        if(stack.getItem() instanceof ToolingItem) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;

        if(level.getBlockEntity(pos) instanceof CraneRouterBlockEntity be) {
            player.openMenu(be, pos);
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override public boolean canItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) { return true; }
    @Override public boolean canPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) { return true; }

    @Override
    public void onItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) {

        if(entity == null || entity.getItemStack().isEmpty()) return;

        this.route(level, pos, new ItemStack[] { entity.getItemStack().copy() });
    }

    @Override
    public void onPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) {

        if(entity == null || entity.getItemStacks() == null) return;

        this.route(level, pos, entity.getItemStacks());
    }

    /** Sortiert die Ware auf die Seiten und schickt jede Gruppe geschlossen weiter. */
    private void route(Level level, BlockPos pos, ItemStack[] stacks) {

        if(!(level.getBlockEntity(pos) instanceof CraneRouterBlockEntity be)) return;

        Map<Direction, List<ItemStack>> sorted = new EnumMap<>(Direction.class);
        List<ItemStack> homeless = new ArrayList<>();

        for(ItemStack stack : stacks) {

            if(stack == null || stack.isEmpty()) continue;

            Direction dir = be.getOutputDir(stack);

            if(dir == null) homeless.add(stack.copy());
            else sorted.computeIfAbsent(dir, d -> new ArrayList<>()).add(stack.copy());
        }

        for(ItemStack stack : homeless) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        }

        for(Map.Entry<Direction, List<ItemStack>> entry : sorted.entrySet()) {
            this.send(level, pos, entry.getKey(), entry.getValue());
        }
    }

    private void send(Level level, BlockPos pos, Direction dir, List<ItemStack> stacks) {

        BlockPos target = pos.relative(dir);

        if(!(level.getBlockState(target).getBlock() instanceof IConveyorBelt belt)) {
            for(ItemStack stack : stacks) {
                Containers.dropItemStack(level,
                        pos.getX() + 0.5 + dir.getStepX() * 0.55,
                        pos.getY() + 0.5 + dir.getStepY() * 0.55,
                        pos.getZ() + 0.5 + dir.getStepZ() * 0.55, stack);
            }
            return;
        }

        Vec3 mouth = new Vec3(
                pos.getX() + 0.5 + dir.getStepX() * 0.55,
                pos.getY() + 0.5 + dir.getStepY() * 0.55,
                pos.getZ() + 0.5 + dir.getStepZ() * 0.55);

        Vec3 snap = belt.getClosestSnappingPosition(level, target, mouth);

        if(stacks.size() == 1) {
            MovingItem moving = new MovingItem(NtmEntityTypes.MOVING_ITEM.get(), level);
            moving.setItemStack(stacks.get(0));
            moving.moveTo(snap.x, snap.y, snap.z, 0F, 0F);
            level.addFreshEntity(moving);
            return;
        }

        MovingPackage moving = new MovingPackage(NtmEntityTypes.MOVING_PACKAGE.get(), level);
        moving.setItemStacks(stacks.toArray(new ItemStack[0]));
        moving.moveTo(snap.x, snap.y, snap.z, 0F, 0F);
        level.addFreshEntity(moving);
    }
}
