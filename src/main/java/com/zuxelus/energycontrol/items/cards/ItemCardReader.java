package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.LinkedList;
import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardReader.
 *
 * Der Datenspeicher einer Karte. Im Original war das der NBT-Beutel am Gegenstand, den
 * jeder Setter frisch anlegte. Auf 1.21.1 gibt es kein Gegenstands-NBT mehr; der Beutel
 * liegt im Datenbestandteil {@code minecraft:custom_data}.
 *
 * Der Leser haelt eine eigene Abschrift des Beutels und schreibt sie nach jeder Aenderung
 * zurueck. Die Abschrift gehoert ihm allein -- {@link CustomData#copyTag()} gibt eine
 * Kopie --, ein spaeteres Weiterschreiben wirkt sich also auf keinen fremden Beutel aus.
 */
public class ItemCardReader implements ICardReader {

    private final ItemStack card;
    private final CompoundTag tag;

    public ItemCardReader(ItemStack card) {
        this.card = card;
        CustomData data = card.get(DataComponents.CUSTOM_DATA);
        this.tag = data != null ? data.copyTag() : new CompoundTag();
    }

    private void writeBack() {
        card.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** Der rohe Beutel, fuer Uebertragung und Vergleich. */
    public CompoundTag getTagCompound() {
        return tag;
    }

    public ItemStack getStack() {
        return card;
    }

    public void setTarget(BlockPos pos) {
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        writeBack();
    }

    @Override
    public BlockPos getTarget() {
        if(!tag.contains("x") || !tag.contains("y") || !tag.contains("z")) return null;
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    @Override public void setInt(String name, int value) { tag.putInt(name, value); writeBack(); }
    @Override public int getInt(String name) { return tag.getInt(name); }

    @Override public void setLong(String name, long value) { tag.putLong(name, value); writeBack(); }
    @Override public long getLong(String name) { return tag.getLong(name); }

    @Override public void setDouble(String name, double value) { tag.putDouble(name, value); writeBack(); }
    @Override public double getDouble(String name) { return tag.getDouble(name); }

    @Override public void setString(String name, String value) { tag.putString(name, value); writeBack(); }
    @Override public String getString(String name) { return tag.getString(name); }

    @Override public void setBoolean(String name, boolean value) { tag.putBoolean(name, value); writeBack(); }
    @Override public boolean getBoolean(String name) { return tag.getBoolean(name); }

    @Override public void setTag(String name, CompoundTag value) { tag.put(name, value); writeBack(); }
    @Override public CompoundTag getTag(String name) { return tag.getCompound(name); }

    @Override public void setList(String name, ListTag value) { tag.put(name, value); writeBack(); }
    @Override public ListTag getList(String name, int type) { return tag.getList(name, type); }

    @Override public void setTitle(String title) { setString("title", title); }
    @Override public String getTitle() { return getString("title"); }

    @Override
    public CardState getState() {
        return CardState.fromInteger(getInt("state"));
    }

    @Override
    public void setState(CardState state) {
        setInt("state", state == null ? CardState.NO_TARGET.getIndex() : state.getIndex());
    }

    @Override public boolean hasField(String name) { return tag.contains(name); }

    @Override public void removeField(String name) { tag.remove(name); writeBack(); }

    @Override public int getCardCount() { return getInt("cardCount"); }

    /**
     * Ziel und Ueberschrift ueberleben, alles andere nicht. Genau wie im Original: die
     * Messwerte der letzten Runde duerfen nicht stehenbleiben, wenn das Ziel diesmal
     * weniger liefert.
     */
    @Override
    public void reset() {
        BlockPos pos = getTarget();
        String title = getTitle();
        int state = getInt("state");
        for(String key : new java.util.ArrayList<>(tag.getAllKeys())) tag.remove(key);
        if(pos != null) {
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
        }
        if(!title.isEmpty()) tag.putString("title", title);
        tag.putInt("state", state);
        writeBack();
    }

    @Override
    public void copyFrom(CompoundTag source) {
        for(String name : source.getAllKeys()) {
            Tag value = source.get(name);
            if(value != null) tag.put(name, value.copy());
        }
        writeBack();
    }

    @Override
    public List<PanelString> getTitleList() {
        List<PanelString> result = new LinkedList<>();
        String title = getTitle();
        if(title != null && !title.isEmpty()) {
            PanelString line = new PanelString();
            line.textCenter = Component.literal(title);
            result.add(line);
        }
        return result;
    }

    /** Die Standardmeldung zu einem Kartenzustand, wenn die Karte nichts zu zeigen hat. */
    public static List<PanelString> getStateMessage(CardState state) {
        List<PanelString> result = new LinkedList<>();
        PanelString line = new PanelString();
        switch(state) {
            case OUT_OF_RANGE -> line.textCenter = Component.translatable("msg.ec.InfoPanelOutOfRange");
            case INVALID_CARD -> line.textCenter = Component.translatable("msg.ec.InfoPanelInvalidCard");
            case NO_TARGET -> line.textCenter = Component.translatable("msg.ec.InfoPanelNoTarget");
            default -> { }
        }
        result.add(line);
        return result;
    }
}
