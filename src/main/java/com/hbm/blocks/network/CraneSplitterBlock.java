package com.hbm.blocks.network;

import api.hbm.block.IToolable;
import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.CraneSplitterBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.item.MovingItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CraneSplitter.
 *
 * Die Weiche ist ZWEI BLOECKE BREIT und teilt einen ankommenden Strom auf zwei
 * nebeneinanderliegende Baender auf. Sie ist damit das Gegenstueck zum Verteiler: der schickt
 * nach Muster, sie nach Menge.
 *
 * DAS VERHAELTNIS STELLT DER SCHRAUBENZIEHER EIN, und zwar an jedem der beiden Bloecke
 * getrennt: der linke traegt das linke Verhaeltnis, der rechte das rechte. Anklicken erhoeht,
 * mit Schleichtaste verringert, zwischen eins und sechzehn. Drei zu eins heisst also: drei
 * Stueck nach links, eines nach rechts, und wieder von vorn.
 *
 * DER ZWEITE BLOCK WIRD GESUCHT, NICHT AUSGERECHNET. Wo er liegt, ergibt sich aus der
 * Drehtabelle des Mehrblock-Systems; sie nachzurechnen waere eine Fehlerquelle ohne Not. Die
 * Weiche schaut stattdessen ihre vier waagerechten Nachbarn an und nimmt den, der zu ihr
 * gehoert. Das stimmt in jeder Drehung und bleibt auch dann richtig, wenn sich an der Tabelle
 * einmal etwas aendert.
 *
 * ABWEICHUNG: Pakete laesst sie nicht herein. Ein Paket aufzuteilen hiesse, es aufzuschnueren,
 * und dafuer gibt es den Entpacker. Das Original haelt es ebenso.
 *
 * ABWEICHUNG: das Original zeichnet sie mit einem eigenen Renderer aus elf Bildern und zeigt
 * das eingestellte Verhaeltnis als Einblendung an, wenn man sie ansieht. Hier stehen zwei
 * schlichte Kaesten; das Verhaeltnis sieht man beim Einstellen an der Wirkung.
 */
public class CraneSplitterBlock extends DummyableBlock implements IConveyorBelt, IEnterableBlock, IToolable {

    public static final MapCodec<CraneSplitterBlock> CODEC = simpleCodec(CraneSplitterBlock::new);

    public CraneSplitterBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends DummyableBlock> codec() { return CODEC; }

    /** Ein zusaetzlicher Block nach rechts -- die Angabe des Originals, unveraendert. */
    @Override public int[] getDimensions() { return new int[] { 0, 0, 0, 0, 0, 1 }; }
    @Override public int getOffset() { return 0; }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isCore(state) ? new CraneSplitterBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, NtmBlockEntityTypes.CRANE_SPLITTER.get(), (l, p, s, be) -> be.updateEntity());
    }

    // ------------------------------------------------------------------------------------
    // Die beiden Spuren
    // ------------------------------------------------------------------------------------

    /**
     * Der Block, der zum selben Bauwerk gehoert und neben diesem liegt. Fuer den Kern ist das
     * der Beiblock, fuer den Beiblock der Kern.
     */
    @Nullable
    private BlockPos findPartner(Level level, BlockPos pos) {

        BlockPos core = this.findCore(level, pos);
        if(core == null) return null;

        if(!core.equals(pos)) return core;

        for(Direction dir : Direction.Plane.HORIZONTAL) {

            BlockPos side = pos.relative(dir);
            BlockState state = level.getBlockState(side);

            if(state.getBlock() != this || isCore(state)) continue;
            if(pos.equals(this.findCore(level, side))) return side;
        }

        return null;
    }

    /** Wohin die Ware faehrt: dem FACING entgegen, wie bei jedem Band dieses Mods. */
    private Direction travelDirection(Level level, BlockPos pos) {

        BlockPos core = this.findCore(level, pos);
        BlockState state = level.getBlockState(core == null ? pos : core);

        return state.getValue(FACING);
    }

    // ------------------------------------------------------------------------------------
    // Als Band
    // ------------------------------------------------------------------------------------

    @Override public boolean canItemStay(Level level, BlockPos pos, Vec3 itemPos) { return true; }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {

        Direction dir = this.travelDirection(level, pos);
        Vec3 snap = this.getClosestSnappingPosition(level, pos, itemPos);
        Vec3 dest = new Vec3(snap.x - dir.getStepX() * speed, snap.y - dir.getStepY() * speed, snap.z - dir.getStepZ() * speed);
        Vec3 motion = new Vec3(dest.x - itemPos.x, dest.y - itemPos.y, dest.z - itemPos.z);
        double len = motion.length();

        if(len == 0D) return itemPos;

        return new Vec3(itemPos.x + motion.x / len * speed, itemPos.y + motion.y / len * speed, itemPos.z + motion.z / len * speed);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {

        Direction dir = this.travelDirection(level, pos);

        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;

        if(dir.getStepX() != 0) x = Mth.clamp(itemPos.x, pos.getX(), pos.getX() + 1);
        if(dir.getStepZ() != 0) z = Mth.clamp(itemPos.z, pos.getZ(), pos.getZ() + 1);

        return new Vec3(x, pos.getY() + 0.25, z);
    }

    // ------------------------------------------------------------------------------------
    // Das Aufteilen
    // ------------------------------------------------------------------------------------

    @Override
    public boolean canItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) {
        return this.travelDirection(level, pos) == dir;
    }

    @Override
    public void onItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) {

        if(entity == null || entity.getItemStack().isEmpty()) return;

        BlockPos core = this.findCore(level, pos);
        if(core == null) return;
        if(!(level.getBlockEntity(core) instanceof CraneSplitterBlockEntity be)) return;

        BlockPos partner = this.findPartner(level, core);
        if(partner == null) return;

        ItemStack[] split = be.splitStack(entity.getItemStack());

        this.spawn(level, core, split[0]);
        this.spawn(level, partner, split[1]);
    }

    private void spawn(Level level, BlockPos pos, ItemStack stack) {

        if(stack.isEmpty()) return;

        Vec3 middle = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vec3 snap = this.getClosestSnappingPosition(level, pos, middle);

        MovingItem moving = new MovingItem(NtmEntityTypes.MOVING_ITEM.get(), level);
        moving.setItemStack(stack);
        moving.moveTo(snap.x, snap.y, snap.z, 0F, 0F);

        level.addFreshEntity(moving);
    }

    @Override public boolean canPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) { return false; }
    @Override public void onPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) { }

    // ------------------------------------------------------------------------------------
    // Das Verhaeltnis einstellen
    // ------------------------------------------------------------------------------------

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, @Nullable Direction direction, ToolType tool) {

        if(tool != ToolType.SCREWDRIVER) return false;
        if(level.isClientSide) return true;

        BlockPos core = this.findCore(level, pos);
        if(core == null) return false;
        if(!(level.getBlockEntity(core) instanceof CraneSplitterBlockEntity be)) return false;

        /* Der Kern traegt das linke Verhaeltnis, der Beiblock das rechte. */
        be.adjustRatio(core.equals(pos), player.isShiftKeyDown() ? -1 : 1);

        return true;
    }
}
