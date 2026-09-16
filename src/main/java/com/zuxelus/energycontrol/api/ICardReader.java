package com.zuxelus.energycontrol.api;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.api.ICardReader.
 *
 * Schreib- und Lesezugriff auf den Datenspeicher einer Sensorkarte. Im Original lag der
 * als NBT direkt am Gegenstand; auf 1.21.1 liegt er im Datenbestandteil
 * {@code minecraft:custom_data}. Die Karten merken davon nichts -- sie sehen weiter nur
 * benannte Felder.
 */
public interface ICardReader {

    /** Die Koordinaten, auf die die Karte eingemessen ist, oder null. */
    BlockPos getTarget();

    void setInt(String name, int value);

    int getInt(String name);

    void setLong(String name, long value);

    long getLong(String name);

    void setDouble(String name, double value);

    double getDouble(String name);

    void setString(String name, String value);

    String getString(String name);

    void setBoolean(String name, boolean value);

    boolean getBoolean(String name);

    void setTag(String name, CompoundTag value);

    CompoundTag getTag(String name);

    void setList(String name, ListTag value);

    ListTag getList(String name, int type);

    /** Die Ueberschrift, die der Spieler der Karte in der Tafel gegeben hat. */
    void setTitle(String title);

    String getTitle();

    CardState getState();

    void setState(CardState state);

    boolean hasField(String name);

    void removeField(String name);

    /** Wieviele Ziele eine Sammelkarte fuehrt. */
    int getCardCount();

    /** Leert den Datenspeicher, behaelt aber Ziel und Ueberschrift. */
    void reset();

    /** Uebernimmt alle Felder aus dem uebergebenen Beutel. */
    void copyFrom(CompoundTag tag);

    /** Die Ueberschriftzeile, mit der jede Kartenausgabe beginnt -- leer ohne Ueberschrift. */
    List<PanelString> getTitleList();
}
