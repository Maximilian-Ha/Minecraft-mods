package com.zuxelus.energycontrol.blockentity;

import com.zuxelus.energycontrol.ECConfig;
import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.IItemCard;
import com.zuxelus.energycontrol.blocks.RangeTriggerBlock;
import com.zuxelus.energycontrol.init.ECBlockEntityTypes;
import com.zuxelus.energycontrol.items.ItemUpgrade;
import com.zuxelus.energycontrol.items.ItemUpgrade.UpgradeType;
import com.zuxelus.energycontrol.items.cards.ItemCardBase;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import com.zuxelus.energycontrol.menus.RangeTriggerMenu;
import com.zuxelus.energycontrol.utils.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.tileentities.TileEntityRangeTrigger.
 *
 * Der Bereichsmelder liest wie eine Tafel eine Karte aus, zeigt aber nichts an, sondern
 * gibt ein Redstone-Signal, sobald der Messwert den eingestellten Bereich verlaesst.
 * Verglichen wird das Feld "energy", ersatzweise "amount" -- also Stromspeicher und
 * Tankfuellstand, so wie im Original.
 */
public class RangeTriggerBlockEntity extends ECContainerBlockEntity {

    public static final int SLOT_CARD = 0;
    public static final int SLOT_UPGRADE_RANGE = 1;

    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_INSIDE = 1;
    public static final int STATUS_OUTSIDE = 2;

    /** Schrittweiten der Knoepfe in der Oberflaeche. */
    public static final long[] STEPS = { 1L, 10L, 100L, 1000L, 10000L, 100000L };

    private long levelStart = 0L;
    private long levelEnd = 40000L;
    private boolean invertRedstone;
    private int status = STATUS_UNKNOWN;

    public RangeTriggerBlockEntity(BlockPos pos, BlockState state) {
        super(ECBlockEntityTypes.RANGE_TRIGGER.get(), pos, state, 2);
    }

    public void tick() {
        if(level == null || level.isClientSide) return;
        if(level.getGameTime() % 20L != 0L) return;
        updateCard();
    }

    private void updateCard() {
        int newStatus = STATUS_UNKNOWN;

        ItemStack stack = getItem(SLOT_CARD);
        if(ItemCardBase.isCard(stack)) {
            ItemCardReader reader = new ItemCardReader(stack);
            IItemCard card = (IItemCard) stack.getItem();
            int range = card.isRemoteCard(stack) ? getRange() : -1;

            CardState state;
            try {
                state = card.update(level, reader, range, worldPosition);
            } catch(Exception e) {
                state = CardState.CUSTOM_ERROR;
            }
            reader.setState(state);

            if(state == CardState.OK) {
                double current = reader.hasField(DataHelper.ENERGY) ? reader.getDouble(DataHelper.ENERGY)
                        : reader.getDouble(DataHelper.AMOUNT);
                double min = Math.min(levelStart, levelEnd);
                double max = Math.max(levelStart, levelEnd);
                newStatus = current < min || current > max ? STATUS_OUTSIDE : STATUS_INSIDE;
            }
        }

        boolean signalChanged = getSignalFor(newStatus) != getSignal();
        status = newStatus;
        updateBlockState();
        if(signalChanged) notifyNeighbours();
        sync();
    }

    private void updateBlockState() {
        if(level == null) return;
        BlockState state = getBlockState();
        if(!(state.getBlock() instanceof RangeTriggerBlock)) return;
        if(state.getValue(RangeTriggerBlock.STATUS) == status) return;
        level.setBlock(worldPosition, state.setValue(RangeTriggerBlock.STATUS, status), Block.UPDATE_ALL);
    }

    public int getRange() {
        ItemStack stack = getItem(SLOT_UPGRADE_RANGE);
        if(!ItemUpgrade.is(stack, UpgradeType.RANGE)) return InfoPanelBlockEntity.BASE_RANGE;
        return InfoPanelBlockEntity.BASE_RANGE + stack.getCount() * ECConfig.rangeUpgradeRange();
    }

    private int getSignalFor(int status) {
        // Ohne brauchbare Karte gibt der Melder nichts -- auch nicht umgekehrt. Sonst
        // stuende ein leerer Melder mit gesetzter Umkehr dauerhaft auf 15.
        if(status == STATUS_UNKNOWN) return 0;
        boolean active = status == STATUS_OUTSIDE;
        return active != invertRedstone ? 15 : 0;
    }

    public int getSignal() {
        return getSignalFor(status);
    }

    public int getStatus() {
        return status;
    }

    public long getLevelStart() {
        return levelStart;
    }

    public long getLevelEnd() {
        return levelEnd;
    }

    public boolean isInverted() {
        return invertRedstone;
    }

    public void toggleInverted() {
        invertRedstone = !invertRedstone;
        notifyNeighbours();
        sync();
    }

    /** Verschiebt eine der beiden Grenzen um einen Schritt. */
    public void adjust(boolean end, int stepIndex, boolean negative) {
        if(stepIndex < 0 || stepIndex >= STEPS.length) return;
        long delta = STEPS[stepIndex] * (negative ? -1L : 1L);
        if(end) levelEnd = Math.max(0L, levelEnd + delta);
        else levelStart = Math.max(0L, levelStart + delta);
        updateCard();
    }

    private void notifyNeighbours() {
        if(level != null && !level.isClientSide) level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    @Override
    protected void readProperties(CompoundTag tag, HolderLookup.Provider registries) {
        levelStart = tag.getLong("levelStart");
        levelEnd = tag.contains("levelEnd") ? tag.getLong("levelEnd") : 40000L;
        invertRedstone = tag.getBoolean("invert");
        status = tag.getInt("status");
    }

    @Override
    protected void writeProperties(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("levelStart", levelStart);
        tag.putLong("levelEnd", levelEnd);
        tag.putBoolean("invert", invertRedstone);
        tag.putInt("status", status);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch(slot) {
            case SLOT_CARD -> ItemCardBase.isCard(stack);
            case SLOT_UPGRADE_RANGE -> ItemUpgrade.is(stack, UpgradeType.RANGE);
            default -> false;
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.energycontrol.range_trigger");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RangeTriggerMenu(id, inventory, this);
    }
}
