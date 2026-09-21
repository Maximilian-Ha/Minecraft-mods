package com.hbm.handler;

import com.hbm.items.armor.ItemArmorMod;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.TagsUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArmorModHandler {

    public static final int HELMET_ONLY = 0;
    public static final int PLATE_ONLY = 1;
    public static final int LEGS_ONLY = 2;
    public static final int BOOTS_ONLY = 3;
    public static final int SERVOS = 4;
    public static final int CLADDING = 5;
    public static final int KEVLAR = 6;
    public static final int EXTRA = 7;
    public static final int BATTERY = 8;

    public static final int MOD_SLOTS = 9;

    //The key for the NBTTagCompound that holds the armor mods
    public static final String MOD_COMPOUND_KEY = "ntm_armor_mods";
    //The key for the specific slot inside the armor mod NBT Tag
    public static final String MOD_SLOT_KEY = "mod_slot_";

    /**
     * Checks if a mod can be applied to an armor piece
     * Needs to be used to prevent people from inserting invalid items into the armor table
     */
    public static boolean isApplicable(ItemStack armor, ItemStack mod) {

        if(armor.isEmpty() || mod.isEmpty()) return false;
        if(!(armor.getItem() instanceof ArmorItem aItem)) return false;
        if(!(mod.getItem() instanceof ItemArmorMod aMod)) return false;

        ArmorItem.Type type = aItem.getType();

        return (type == ArmorItem.Type.HELMET && aMod.helmet) ||
                (type == ArmorItem.Type.CHESTPLATE && aMod.chestplate) ||
                (type == ArmorItem.Type.LEGGINGS && aMod.leggings) ||
                (type == ArmorItem.Type.BOOTS && aMod.boots);
    }

    /**
     * Applies a mod to the given armor piece
     * Make sure to check for applicability first
     * Will override present mods so make sure to only use unmodded armor pieces
     */
    public static void applyMod(Level level, ItemStack armor, ItemStack mod) {
        CompoundTag tag = TagsUtil.getCustomData(armor);

        if(!tag.contains(MOD_COMPOUND_KEY)) tag.put(MOD_COMPOUND_KEY, new CompoundTag());

        CompoundTag mods = tag.getCompound(MOD_COMPOUND_KEY);

        ItemArmorMod aMod = (ItemArmorMod) mod.getItem();
        int slot = aMod.type;

        CompoundTag cmp = new CompoundTag();
        mod.save(level.registryAccess(), cmp);

        mods.put(MOD_SLOT_KEY + slot, cmp);

        TagsUtil.putCustomData(armor, tag);
    }

    /**
     * Removes the mod from the given slot
     */
    public static void removeMod(ItemStack armor, int slot) {
        if(armor.isEmpty()) return;

        CompoundTag tag = TagsUtil.getCustomData(armor);

        if(!tag.contains(MOD_COMPOUND_KEY)) tag.put(MOD_COMPOUND_KEY, new CompoundTag());

        CompoundTag mods = tag.getCompound(MOD_COMPOUND_KEY);
        mods.remove(MOD_SLOT_KEY + slot);

        /* getCustomData liefert eine Kopie, also muss die AEUSSERE Tafel zurueckgeschrieben
         * werden. Wurde hier frueher die innere geschrieben, landeten die Modulplaetze auf
         * oberster Ebene und hasMods sah danach gar keine Module mehr. */
        if(mods.isEmpty()) {
            clearMods(armor);
            return;
        }

        tag.put(MOD_COMPOUND_KEY, mods);
        TagsUtil.putCustomData(armor, tag);
    }

    /**
     * Removes ALL mods
     * Should be used when the armor piece is put in the armor table slot AFTER the armor pieces have been separated
     */
    public static void clearMods(ItemStack armor) {

        if(!TagsUtil.hasCustomData(armor)) return;

        CompoundTag tag = TagsUtil.getCustomData(armor);
        tag.remove(MOD_COMPOUND_KEY);
        TagsUtil.putCustomData(armor, tag);
    }

    /**
     * Does what the name implies. Returns true if the stack has NBT and that NBT has the MOD_COMPOUND_KEY tag.
     */
    public static boolean hasMods(ItemStack armor) {

        if(!TagsUtil.hasCustomData(armor)) return false;

        CompoundTag tag = TagsUtil.getCustomData(armor);
        return tag.contains(MOD_COMPOUND_KEY);
    }

    /**
     * Gets all the modifications in the provided armor
     */
    public static ItemStack[] pryMods(Level level, ItemStack armor) {

        /* Nie null: die Aufrufer fragen reihum mod.isEmpty(), ein Loch waere ein Absturz. */
        ItemStack[] slots = new ItemStack[MOD_SLOTS];
        Arrays.fill(slots, ItemStack.EMPTY);

        if(!hasMods(armor)) return slots;

        CompoundTag tag = TagsUtil.getCustomData(armor);
        CompoundTag mods = tag.getCompound(MOD_COMPOUND_KEY);

        for(int i = 0; i < MOD_SLOTS; i++) {

            CompoundTag cmp = mods.getCompound(MOD_SLOT_KEY + i);

            Optional<ItemStack> stack = ItemStack.parse(level.registryAccess(), cmp);

            if(stack.isPresent() && !stack.get().isEmpty()) {
                slots[i] = stack.get();
            } else {
                // Any non-existing armor mods will be sorted out automatically
                removeMod(armor, i);
            }
        }

        return slots;
    }

    public static ItemStack pryMod(Level level, ItemStack armor, int slot) {

        if(!hasMods(armor)) return ItemStack.EMPTY;

        CompoundTag tag = TagsUtil.getCustomData(armor);
        CompoundTag mods = tag.getCompound(MOD_COMPOUND_KEY);
        CompoundTag cmp = mods.getCompound(MOD_SLOT_KEY + slot);
        Optional<ItemStack> stack = ItemStack.parse(level.registryAccess(), cmp);

        if(stack.isPresent() && !stack.get().isEmpty()) return stack.get();

        removeMod(armor, slot);

        return ItemStack.EMPTY;
    }

    /**
     * Der Name des Eigenschaftswerts, den die Module dem Traeger geben. Ein fester Name je
     * Eigenschaft reicht: die Werte aller vier Ruestungsteile werden vorher aufsummiert.
     */
    private static ResourceLocation attributeId(Holder<Attribute> attribute) {
        ResourceLocation key = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
        return NuclearTechMod.withDefaultNamespace("armor_mod/" + (key != null ? key.getPath() : "unknown"));
    }

    /**
     * Ein Durchgang ueber alle vier Ruestungsteile: jedes Modul darf ticken, und die
     * Eigenschaftswerte werden eingesammelt und am Traeger nachgefuehrt.
     *
     * Im Original haengen die Werte als Multimap am Ruestungsteil. In 1.21 ist das nicht mehr
     * moeglich, weil die Eigenschaften eines Gegenstands in einer Datenkomponente stehen und
     * nicht vom NBT des Stapels abhaengen duerfen. Stattdessen wird hier jeden Tick die Summe
     * gebildet und als voruebergehender Wert gesetzt oder wieder entfernt.
     */
    public static void updateMods(LivingEntity entity) {

        Map<Holder<Attribute>, Double> attributes = new HashMap<>();

        for(EquipmentSlot slot : ARMOR_SLOTS) {

            ItemStack armor = entity.getItemBySlot(slot);

            if(armor.isEmpty() || !hasMods(armor)) continue;

            for(ItemStack mod : pryMods(entity.level(), armor)) {
                if(mod.isEmpty() || !(mod.getItem() instanceof ItemArmorMod armorMod)) continue;

                armorMod.modUpdate(entity, armor);
                armorMod.addAttributes(armor, attributes);
            }
        }

        for(TrackedAttribute tracked : TRACKED_ATTRIBUTES) {

            AttributeInstance instance = entity.getAttribute(tracked.attribute());
            if(instance == null) continue;

            ResourceLocation id = attributeId(tracked.attribute());
            Double value = attributes.get(tracked.attribute());

            if(value == null || value == 0D) {
                instance.removeModifier(id);
            } else {
                instance.addOrUpdateTransientModifier(new AttributeModifier(id, value, tracked.operation()));
            }
        }
    }

    /** Jedes Modul darf den Schaden verrechnen, den der Traeger gerade abbekommt. */
    public static void handleDamage(LivingDamageEvent.Pre event) {

        LivingEntity entity = event.getEntity();

        for(EquipmentSlot slot : ARMOR_SLOTS) {

            ItemStack armor = entity.getItemBySlot(slot);

            if(armor.isEmpty() || !hasMods(armor)) continue;

            for(ItemStack mod : pryMods(entity.level(), armor)) {
                if(mod.isEmpty() || !(mod.getItem() instanceof ItemArmorMod armorMod)) continue;

                armorMod.modDamage(event, armor);
            }
        }
    }

    public static final EquipmentSlot[] ARMOR_SLOTS = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    /**
     * Eine Eigenschaft, die von Modulen kommen kann, samt der Rechenart -- die entscheidet,
     * wie der von addAttributes gelieferte Summand zu lesen ist. Beides ist unveraendert aus
     * dem Original: das Tempo wird anteilig gerechnet, der Rueckstoss absolut.
     */
    private record TrackedAttribute(Holder<Attribute> attribute, AttributeModifier.Operation operation) { }

    /**
     * Nur ueber diese Eigenschaften wird jeden Tick nachgesehen -- so wird ein abgenommenes
     * Modul auch wieder abgeraeumt.
     */
    private static final List<TrackedAttribute> TRACKED_ATTRIBUTES = List.of(
            new TrackedAttribute(Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
            new TrackedAttribute(Attributes.KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_VALUE),
            /* HOECHSTE LEBENSENERGIE, NACHGETRAGEN IN RUNDE 272. Sie fehlte hier, obwohl
             * ItemModHealth sie seit seiner Portierung in die Karte legt -- der Herzcontainer
             * und der schwarze Diamant gaben also GAR NICHTS. Ein Summand, den niemand liest,
             * ist dasselbe wie keiner. Im Original haengt der Wert als Attributaenderung am
             * Ruestungsteil (ItemModHealth.getModifiers), also absolut, nicht anteilig. */
            new TrackedAttribute(Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE));
}
