package com.zuxelus.energycontrol.items;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.InventoryCardHolder und
 * InventoryPortablePanel.
 *
 * Ein Inventar, das nicht an einem Block haengt, sondern an einem Gegenstand in der Hand --
 * Kartenhalter und tragbare Tafel fuehren ihre Karten so.
 *
 * Gespeichert wird im Datenbestandteil {@code minecraft:custom_data}, genau wie beim
 * Kartenleser. Der Weg ueber {@code minecraft:container} waere der vorgesehene, haette aber
 * einen Haken: dieser Bestandteil fuehrt hoechstens 256 Faecher und zeigt seinen Inhalt in
 * der Kurzinfo an, und beides passt zu einem Kartenhalter schlechter als der eigene Beutel,
 * den der Mod ohnehin schon fuer jede Karte benutzt.
 *
 * Der Gegenstand wird bei jedem Zugriff frisch aus der Hand geholt: der Client tauscht
 * seinen ItemStack aus, sobald der Server ihn neu schickt, und eine gemerkte Kennung waere
 * danach die falsche.
 */
public class ItemInventory implements Container {

    private static final String KEY = "Items";

    private final Player player;
    private final InteractionHand hand;
    private final Item item;
    private final NonNullList<ItemStack> slots;

    public ItemInventory(Player player, InteractionHand hand, int size) {
        this.player = player;
        this.hand = hand;
        this.item = player.getItemInHand(hand).getItem();
        this.slots = NonNullList.withSize(size, ItemStack.EMPTY);

        CustomData data = player.getItemInHand(hand).get(DataComponents.CUSTOM_DATA);
        if(data != null) {
            CompoundTag tag = data.copyTag();
            if(tag.contains(KEY)) ContainerHelper.loadAllItems(tag, slots, player.level().registryAccess());
        }
    }

    public ItemStack getHolder() {
        return player.getItemInHand(hand);
    }

    @Override
    public int getContainerSize() {
        return slots.size();
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack stack : slots) {
            if(!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slots.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(slots, slot, amount);
        if(!stack.isEmpty()) setChanged();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = ContainerHelper.takeItem(slots, slot);
        setChanged();
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        slots.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    /** Schreibt den Inhalt in den Gegenstand zurueck. */
    @Override
    public void setChanged() {
        ItemStack holder = getHolder();
        if(holder.isEmpty()) return;

        CustomData data = holder.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data != null ? data.copyTag() : new CompoundTag();
        // Erst die alte Liste heraus, sonst bleiben Faecher stehen, die geleert wurden.
        tag.remove(KEY);
        ContainerHelper.saveAllItems(tag, slots, true, player.level().registryAccess());
        holder.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** Solange derselbe Gegenstand in derselben Hand liegt, gilt die Oberflaeche. */
    @Override
    public boolean stillValid(Player who) {
        return who == player && player.getItemInHand(hand).getItem() == item;
    }

    @Override
    public void clearContent() {
        slots.clear();
        setChanged();
    }

    /** Das Fach im Spielerinventar, in dem der Gegenstand liegt -- oder -1 in der Nebenhand. */
    public int getLockedSlot() {
        return hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : -1;
    }
}
