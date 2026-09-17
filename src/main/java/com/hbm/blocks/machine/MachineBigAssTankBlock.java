package com.hbm.blocks.machine;

import com.hbm.blockentity.IPersistentNBT;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.storage.MachineBigAssTankBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.util.InventoryUtil;
import com.hbm.util.TagsUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineBigAssTank.
 *
 * Ein Mehrblockbau aus sieben Teilkoerpern: der 9x9-Rumpf um den Kern, vier Ausbuchtungen
 * an den Seiten und zwei Stutzen auf der Blickachse. Der Kern liegt sechs Bloecke vor der
 * angeklickten Stelle. Der Bau misst dreizehn Bloecke auf der Blickachse (die beiden
 * Stutzen bei +-6), elf quer dazu (die Ausbuchtungen bei +-5) und sechs in der Hoehe.
 *
 * getAllDimensions des Originals faellt weg: es dient dort allein der gruen/roten
 * Bauvorschau (BlockDummyable.drawPlacementHighlight), die der Port nicht kennt. Die
 * Zusatzgrundrisse stehen deshalb als Konstanten da, wie beim Reformer und beim Bagger.
 */
public class MachineBigAssTankBlock extends DummyableBlock {

    /*
     * Reihenfolge immer [oben, unten, Nord, Sued, West, Ost], von MultiblockHandlerXR.rotate
     * um die Blickrichtung gedreht. Negative Werte sind kein Fehler: {..., 5, -4, ...} heisst
     * "von fuenf Bloecken vor dem Kern bis vier davor", also eine zwei Bloecke dicke Scheibe.
     */
    private static final int[] DIM_WING_N   = { 4, 0,  5, -4,  2,  2 };
    private static final int[] DIM_WING_S   = { 4, 0, -4,  5,  2,  2 };
    private static final int[] DIM_WING_W   = { 4, 0,  2,  2,  5, -4 };
    private static final int[] DIM_WING_E   = { 4, 0,  2,  2, -4,  5 };
    private static final int[] DIM_NOZZLE_N = { 3, 0,  6, -5,  0,  0 };
    private static final int[] DIM_NOZZLE_S = { 3, 0, -5,  6,  0,  0 };

    private static final int[][] EXTRA_DIMS = { DIM_WING_N, DIM_WING_S, DIM_WING_W, DIM_WING_E, DIM_NOZZLE_N, DIM_NOZZLE_S };

    public MachineBigAssTankBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<MachineBigAssTankBlock> CODEC = simpleCodec(MachineBigAssTankBlock::new);
    @Override public MapCodec<MachineBigAssTankBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DummyBlockType type = state.getValue(TYPE);
        return switch(type) {
            case CORE -> new MachineBigAssTankBlockEntity(pos, state);
            case EXTRA -> new ProxyComboBlockEntity(pos, state).fluid();
            default -> null;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 5, 0, 4, 4, 4, 4 }; }
    @Override public int getOffset() { return 6; }

    @Override
    protected boolean checkRequirement(Level level, BlockPos pos, Direction dir, int offset) {

        BlockPos core = pos.relative(dir, offset);

        if(!MultiblockHandlerXR.checkSpace(level, core, this.getDimensions(), pos, dir)) return false;

        for(int[] dim : EXTRA_DIMS) {
            if(!MultiblockHandlerXR.checkSpace(level, core, dim, pos, dir)) return false;
        }

        return true;
    }

    @Override
    protected void fillSpace(Level level, BlockPos pos, Direction dir, int offset) {
        super.fillSpace(level, pos, dir, offset);

        BlockPos core = pos.relative(dir, offset);

        for(int[] dim : EXTRA_DIMS) {
            MultiblockHandlerXR.fillSpace(level, core, dim, this, dir);
        }

        /*
         * Die beiden Anschlussstutzen, sechs Bloecke vor und hinter dem Kern. Das Original
         * schreibt in der Z-Komponente versehentlich o statt 6; nachgerechnet entsteht
         * dasselbe Paar, weil bei Ost/West offsetZ und bei Nord/Sued offsetX null ist.
         */
        this.makeExtra(level, core.relative(dir, 6));
        this.makeExtra(level, core.relative(dir.getOpposite(), 6));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return InteractionResult.FAIL;

        BlockEntity blockEntity = level.getBlockEntity(corePos);

        if(!player.isShiftKeyDown()) {
            if(blockEntity instanceof MenuProvider be) player.openMenu(new SimpleMenuProvider(be, be.getDisplayName()), corePos);
            return InteractionResult.CONSUME;
        }

        if(blockEntity instanceof MachineBigAssTankBlockEntity be) {
            for(ItemStack stack : InventoryUtil.getItemsFromBothHands(player)) {
                if(stack.getItem() instanceof IItemFluidIdentifier identifier) {
                    FluidType type = identifier.getType(level, corePos, stack);

                    be.tank.setTankType(type);
                    be.setChanged();
                    /* Der Schluessel gehoert dem gewoehnlichen Tank; lang-check.sh laesst keinen
                     * zweiten mit demselben Wortlaut zu, und der Satz ist derselbe. */
                    player.displayClientMessage(Component.translatable("block.hbmsntm.machine_fluid_tank.changed_type_to", type.getName()).withStyle(ChatFormatting.YELLOW), false);
                }
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockPos corePos = this.findCore(level, pos);
        if(corePos == null) return 0;

        if(level.getBlockEntity(corePos) instanceof MachineBigAssTankBlockEntity be) {
            return be.getComparatorPower();
        }

        return 0;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return IPersistentNBT.getDropsFromLootParams(state, params);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        CompoundTag persistent = TagsUtil.getCustomData(stack).getCompound(IPersistentNBT.NBT_PERSISTENT_KEY);
        if(!persistent.contains("tank")) return;

        FluidTank tank = new FluidTank(Fluids.NONE, 0);
        tank.readFromNBT(persistent, "tank");
        components.add(Component.literal(tank.getFill() + "/" + tank.getMaxFill() + "mB ").append(tank.getTankType().getName()).withStyle(ChatFormatting.YELLOW));
    }
}
