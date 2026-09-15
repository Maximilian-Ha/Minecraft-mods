package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.WatzBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.BoltItem;
import com.hbm.items.NtmItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.Watz.
 *
 * Ein Segment des Watz-Reaktors. Es belegt einen Klotz von sieben mal sieben Bloecken Grundflaeche
 * und drei Bloecken Hoehe -- an den Ecken abgeschraegt, daher die vier zusaetzlichen Bereiche.
 *
 * Der Block hat keine eigene Fallform: wer ihn abbaut, bekommt seine Bauteile zurueck.
 */
public class WatzBlock extends DummyableBlock {

    /* Die vier abgeschraegten Ecken, die das Original ueber getAllDimensions mitbelegt. */
    private static final int[][] DIM_CORNERS = new int[][] {
            {2, 0, 2, 2, 2, -2},
            {2, 0, 2, 2, -2, 2},
            {2, 0, 1, 1, 3, -3},
            {2, 0, 1, 1, -3, 3}
    };

    public WatzBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(state.getValue(TYPE)) {
            case CORE -> new WatzBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).inventory().fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] {2, 0, 3, 3, 1, 1}; }
    @Override public int getOffset() { return 3; }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {

        if(!super.checkRequirement(level, pos, dir, offset)) return false;

        BlockPos corePos = pos.relative(dir, offset);
        for(int[] dim : DIM_CORNERS) {
            if(!MultiblockHandlerXR.checkSpace(level, corePos, dim, pos, dir)) return false;
        }

        return true;
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos corePos = pos.relative(dir, offset);

        for(int[] dim : DIM_CORNERS) MultiblockHandlerXR.fillSpace(level, corePos, dim, this, dir);

        /* Die Anschlussstellen: vier unten, vier oben, und die Mitte des Deckels. */
        this.makeExtra(level, corePos.offset(2, 0, 0));
        this.makeExtra(level, corePos.offset(-2, 0, 0));
        this.makeExtra(level, corePos.offset(0, 0, 2));
        this.makeExtra(level, corePos.offset(0, 0, -2));
        this.makeExtra(level, corePos.offset(2, 2, 0));
        this.makeExtra(level, corePos.offset(-2, 2, 0));
        this.makeExtra(level, corePos.offset(0, 2, 2));
        this.makeExtra(level, corePos.offset(0, 2, -2));
        this.makeExtra(level, corePos.offset(0, 2, 0));
    }

    /**
     * Baut das Segment an Ort und Stelle auf. Der Zusammenbauklotz benutzt das, nachdem er die
     * Wand geprueft hat -- er steht selbst schon an der richtigen Stelle, deshalb ohne Versatz.
     */
    public void assemble(Level level, BlockPos pos, Direction dir) {
        this.fillSpace(level, pos, dir, 0);
    }

    /**
     * Ob beim Abbauen die Bauteile zurueckfallen. Der Zusammenbauklotz und die Kernschmelze
     * setzen das ab, damit ein umgebauter Reaktor nicht doppelt ausschuettet.
     */
    public static boolean drop = true;

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {

        if(isCore(state) && drop && !safeRem && !state.is(newState.getBlock())) {

            if(level.getBlockEntity(pos) instanceof WatzBlockEntity be) Containers.dropContents(level, pos, be);

            /* Die Stueckzahlen stehen so im Original. */
            this.drop(level, pos, new ItemStack(NtmBlocks.WATZ_END.get(), 48));
            for(int i = 0; i < 3; i++) this.drop(level, pos, MetaHelper.newStack(NtmItems.BOLT, 64, BoltItem.Type.DURA_STEEL.meta));
            this.drop(level, pos, new ItemStack(NtmBlocks.WATZ_ELEMENT.get(), 36));
            this.drop(level, pos, new ItemStack(NtmBlocks.WATZ_COOLER.get(), 26));
            this.drop(level, pos, new ItemStack(NtmBlocks.STRUCT_WATZ_CORE.get(), 1));
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void drop(Level level, BlockPos pos, ItemStack stack) {
        Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }

    public static final MapCodec<WatzBlock> CODEC = simpleCodec(WatzBlock::new);
    @Override public MapCodec<WatzBlock> codec() { return CODEC; }
}
