package com.hbm.blocks.machine;

import com.hbm.blockentity.IPersistentNBT;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.machine.storage.MachineBatteryBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.util.BobMathUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachineBattery.
 *
 * Ein Block fuer alle fuenf Stufen; die Kapazitaet steckt im Feld maxPower und
 * wird bei der Registrierung mitgegeben. Die Ausrichtung lag im Original im
 * Metadatenwert (2-5), hier uebernimmt das die Blockstate-Eigenschaft FACING.
 * Die Vorderseite liegt auf der Blickrichtung, Ober- und Unterseite teilen sich
 * eine Textur -- wie im Original getIcon().
 */
public class MachineBatteryBlock extends BaseEntityBlock implements ILookOverlay, IPersistentInfoProvider {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** Der Codec braucht einen festen Wert, die echten Kapazitaeten kommen aus NtmBlocks. */
    public static final MapCodec<MachineBatteryBlock> CODEC = simpleCodec(properties -> new MachineBatteryBlock(properties, 1_000_000L));

    public final long maxPower;

    public MachineBatteryBlock(Properties properties, long maxPower) {
        super(properties);
        this.maxPower = maxPower;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<MachineBatteryBlock> codec() {
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
        return new MachineBatteryBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if(be instanceof ITickable tickable) tickable.updateEntity();
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if(!level.isClientSide) {
            IPersistentNBT.restoreData(level, pos, stack);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        if(player.isShiftKeyDown()) return InteractionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof MenuProvider menu) {
            player.openMenu(new SimpleMenuProvider(menu, menu.getDisplayName()), pos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(!level.isClientSide && !state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof MachineBatteryBlockEntity battery) {
                Containers.dropContents(level, pos, battery);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Der Block faellt samt gespeicherter Energie. Das muss geschehen, solange
     * die BlockEntity noch existiert -- deshalb hier und nicht in playerDestroy.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if(!player.hasInfiniteMaterials()) {
            dropResources(state, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity be, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.025F);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return IPersistentNBT.getDropsFromLootParams(state, params);
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if(level.getBlockEntity(pos) instanceof MachineBatteryBlockEntity battery) {
            return battery.getComparatorPower();
        }

        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("Stores up to " + BobMathUtil.getShortNumber(this.maxPower) + "HE").withStyle(ChatFormatting.GOLD));
        components.add(Component.literal("Charge speed: " + BobMathUtil.getShortNumber(this.maxPower / 200) + "HE").withStyle(ChatFormatting.GOLD));
        components.add(Component.literal("Discharge speed: " + BobMathUtil.getShortNumber(this.maxPower / 600) + "HE").withStyle(ChatFormatting.GOLD));
    }

    /** Die vierte Zeile des Originals: der tatsaechlich gespeicherte Inhalt. */
    @Override
    public void appendHoverText(ItemStack stack, CompoundTag persistentTag, List<Component> components, Item.TooltipContext context, TooltipFlag flag) {
        components.add(Component.literal(BobMathUtil.getShortNumber(persistentTag.getLong("Power")) + "/"
                + BobMathUtil.getShortNumber(this.maxPower) + "HE").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void printHook(RenderGuiEvent.Pre event, Level level, BlockPos pos) {
        if(!(level.getBlockEntity(pos) instanceof MachineBatteryBlockEntity battery)) return;

        List<Component> text = new ArrayList<>();
        text.add(Component.literal(BobMathUtil.getShortNumber(battery.getPower()) + " / " + BobMathUtil.getShortNumber(battery.getMaxPower()) + "HE"));

        double percent = (double) battery.getPower() / (double) battery.getMaxPower();
        int charge = (int) Math.floor(percent * 10_000D);
        int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int) (0xFF * percent) << 8);

        text.add(Component.literal((charge / 100D) + "%").withColor(color));

        ILookOverlay.printGeneric(event, Component.translatable(this.getDescriptionId()), 0xffff00, 0x404000, text);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
