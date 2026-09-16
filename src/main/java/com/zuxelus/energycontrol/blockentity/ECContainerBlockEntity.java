package com.zuxelus.energycontrol.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.Connection;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.12.2: com.zuxelus.zlib.tileentities.TileEntityInventory.
 *
 * Gemeinsames aller Bloecke dieses Mods: ein kleines Inventar und ein vollstaendiger
 * Zustandsabgleich zum Client. Der Abgleich ist hier wichtiger als bei einer Maschine --
 * die Tafel zeichnet ihre Zeilen aus der Karte, die im Fach liegt, also muss der Client
 * die Karte samt Messwerten kennen.
 */
public abstract class ECContainerBlockEntity extends BlockEntity implements Container, MenuProvider, IControlReceiver {

    protected final NonNullList<ItemStack> slots;

    protected ECContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        this.slots = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    // ------------------------------------------------------------- Inventar

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
        return ContainerHelper.takeItem(slots, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        slots.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        slots.clear();
    }

    // ------------------------------------------------------------- Eingaben

    /**
     * Wer den Block bedienen darf: wer nah genug steht. {@code stillValid} prueft genau das
     * und wird von Minecraft fuer die Faecher ohnehin benutzt -- also dieselbe Grenze fuer
     * Fach und Eingabefeld.
     */
    @Override
    public boolean hasPermission(Player player) {
        return stillValid(player);
    }

    @Override
    public void receiveControl(Player player, CompoundTag tag) { }

    // ------------------------------------------------------------- Speichern

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        slots.clear();
        ContainerHelper.loadAllItems(tag, slots, registries);
        readProperties(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, slots, registries);
        writeProperties(tag, registries);
    }

    /** Die eigenen Felder des Blocks, ohne das Inventar. */
    protected void readProperties(CompoundTag tag, HolderLookup.Provider registries) { }

    protected void writeProperties(CompoundTag tag, HolderLookup.Provider registries) { }

    // -------------------------------------------------------------- Abgleich

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        CompoundTag tag = packet.getTag();
        if(tag != null) loadAdditional(tag, registries);
    }

    /** Speichert und schickt den neuen Zustand an alle Clients in Sichtweite. */
    public void sync() {
        setChanged();
        if(level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }
}
