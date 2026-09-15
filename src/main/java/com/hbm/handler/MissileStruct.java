package com.hbm.handler;

import com.hbm.items.weapon.CustomMissilePartItem;
import com.hbm.items.weapon.CustomMissilePartItem.PartType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.handler.MissileStruct.
 *
 * Der Bauplan einer Eigenbau-Rakete: vier Teile, von oben nach unten -- Sprengkopf, Rumpf,
 * Leitwerk, Triebwerk. Mehr ist eine Rakete nicht.
 *
 * SIE PRUEFT BEIM SCHREIBEN, nicht beim Lesen: wer einen Rumpf in das Sprengkopffach legt, dessen
 * Sprengkopf wird als "keiner" uebertragen. Das ist im Original genauso und spart der Gegenseite
 * jede Pruefung.
 *
 * ABWEICHUNG: das Original schreibt die Zahlenkennung des Gegenstands in den Puffer. Auf 1.21
 * sind diese Kennungen nicht mehr stabil -- hier steht der Registriername.
 */
public class MissileStruct {

    public CustomMissilePartItem warhead;
    public CustomMissilePartItem fuselage;
    public CustomMissilePartItem fins;
    public CustomMissilePartItem thruster;

    public MissileStruct() { }

    public MissileStruct(ItemStack w, ItemStack f, ItemStack s, ItemStack t) {
        this.warhead = from(w);
        this.fuselage = from(f);
        this.fins = from(s);
        this.thruster = from(t);
    }

    public MissileStruct(Item w, Item f, Item s, Item t) {
        if(w instanceof CustomMissilePartItem part) this.warhead = part;
        if(f instanceof CustomMissilePartItem part) this.fuselage = part;
        if(s instanceof CustomMissilePartItem part) this.fins = part;
        if(t instanceof CustomMissilePartItem part) this.thruster = part;
    }

    private static CustomMissilePartItem from(ItemStack stack) {
        if(stack == null || stack.isEmpty()) return null;
        return stack.getItem() instanceof CustomMissilePartItem part ? part : null;
    }

    public void writeToByteBuffer(RegistryFriendlyByteBuf buf) {
        write(buf, this.warhead, PartType.WARHEAD);
        write(buf, this.fuselage, PartType.FUSELAGE);
        write(buf, this.fins, PartType.FINS);
        write(buf, this.thruster, PartType.THRUSTER);
    }

    /** Ein leerer Name heisst "kein Teil" -- auch dann, wenn dort etwas Falsches liegt. */
    private static void write(RegistryFriendlyByteBuf buf, CustomMissilePartItem part, PartType expected) {
        buf.writeUtf(part != null && part.type == expected ? BuiltInRegistries.ITEM.getKey(part).toString() : "");
    }

    public static MissileStruct readFromByteBuffer(RegistryFriendlyByteBuf buf) {

        MissileStruct multipart = new MissileStruct();

        multipart.warhead = read(buf);
        multipart.fuselage = read(buf);
        multipart.fins = read(buf);
        multipart.thruster = read(buf);

        return multipart;
    }

    private static CustomMissilePartItem read(RegistryFriendlyByteBuf buf) {

        String name = buf.readUtf();
        if(name.isEmpty()) return null;

        ResourceLocation id = ResourceLocation.tryParse(name);
        if(id == null || !BuiltInRegistries.ITEM.containsKey(id)) return null;

        return BuiltInRegistries.ITEM.get(id) instanceof CustomMissilePartItem part ? part : null;
    }
}
