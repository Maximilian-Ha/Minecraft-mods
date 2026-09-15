package com.hbm.items.weapon.sedna.mods;

import com.google.common.collect.HashBiMap;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.factory.GunFactory.ModGeneric;
import com.hbm.items.weapon.sedna.factory.Orchestras;
import com.hbm.items.weapon.sedna.factory.XFactory762mm;
import com.hbm.items.weapon.sedna.factory.GunFactory.ModSpecial;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.inventory.MetaHelper;
import com.hbm.util.TagsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class XWeaponModManager {

    public static final String KEY_MOD_LIST = "KEY_MOD_LIST_";

    /** Mapping of mods to IDs, keep the register order consistent! */
    public static HashBiMap<Integer, IWeaponMod> idToMod = HashBiMap.create();
    /** Mapping of mod items to mod definitions */
    public static HashMap<ComparableStack, WeaponModDefinition> stackToMod = new HashMap<>();
    /** Map for turning individual mods back into their item form, used when uninstaling mods */
    public static HashMap<IWeaponMod, ItemStack> modToStack = new HashMap<>();

    /**
     * Legt die Aufsaetze an und sagt, welcher an welche Waffe passt.
     *
     * DIE REIHENFOLGE IST BINDEND: die Nummer im Konstruktor steht spaeter in der Waffe. Wer hier
     * etwas dazwischenschiebt, macht aus dem Schalldaempfer aller gespeicherten Waffen etwas
     * anderes. Die Nummern sind deshalb dieselben wie im Original, auch dort, wo Luecken bleiben,
     * weil die zugehoerige Waffe im Port noch fehlt.
     *
     * ABWEICHUNG: das Original traegt hier rund vierzig Waffen ein. Im Port stehen bisher sieben;
     * die uebrigen Eintraege kommen mit ihren Waffen nach. Die Testaufsaetze des Originals
     * (weapon_mod_test) sind NICHT UEBERNOMMEN -- sie sind Werkzeug des Autors, kein Spielinhalt.
     */
    public static void init() {

        /* Die beiden Allgemeinaufsaetze aus Eisen. Sie passen an die Pfefferbuechse. */
        new WeaponModDefinition(ModGeneric.IRON_DAMAGE)
                .addMod(NtmItems.GUN_PEPPERBOX.get(), new WeaponModGenericDamage(100));
        new WeaponModDefinition(ModGeneric.IRON_DURA)
                .addMod(NtmItems.GUN_PEPPERBOX.get(), new WeaponModGenericDurability(101));

        /* Stahl passt an die beiden Revolver, die M3 und die Mare's Legs. */
        Item[] steelGuns = new Item[] {
                NtmItems.GUN_LIGHT_REVOLVER.get(), NtmItems.GUN_LIGHT_REVOLVER_ATLAS.get(),
                NtmItems.GUN_GREASEGUN.get(),
                NtmItems.GUN_MARESLEG.get(), NtmItems.GUN_MARESLEG_AKIMBO.get() };
        new WeaponModDefinition(ModGeneric.STEEL_DAMAGE).addMod(steelGuns, new WeaponModGenericDamage(102));
        new WeaponModDefinition(ModGeneric.STEEL_DURA).addMod(steelGuns, new WeaponModGenericDurability(103));

        /* Schnellarbeitsstahl passt an die AM180. */
        Item[] duraGuns = new Item[] { NtmItems.GUN_AM180.get() };
        new WeaponModDefinition(ModGeneric.DURA_DAMAGE).addMod(duraGuns, new WeaponModGenericDamage(104));
        new WeaponModDefinition(ModGeneric.DURA_DURA).addMod(duraGuns, new WeaponModGenericDurability(105));

        /* Desh passt an die Uzi, die SPAS-12 und den Karabiner. */
        Item[] deshGuns = new Item[] {
                NtmItems.GUN_UZI.get(), NtmItems.GUN_UZI_AKIMBO.get(),
                NtmItems.GUN_SPAS12.get(),
                NtmItems.GUN_CARBINE.get() };
        new WeaponModDefinition(ModGeneric.DESH_DAMAGE).addMod(deshGuns, new WeaponModGenericDamage(106));
        new WeaponModDefinition(ModGeneric.DESH_DURA).addMod(deshGuns, new WeaponModGenericDurability(107));

        /* Waffenstahl passt an die beiden Star-F und an die beiden G3. */
        Item[] wsteelGuns = new Item[] {
                NtmItems.GUN_STAR_F.get(), NtmItems.GUN_STAR_F_AKIMBO.get(),
                NtmItems.GUN_G3.get(), NtmItems.GUN_G3_ZEBRA.get() };
        new WeaponModDefinition(ModGeneric.WSTEEL_DAMAGE).addMod(wsteelGuns, new WeaponModGenericDamage(108));
        new WeaponModDefinition(ModGeneric.WSTEEL_DURA).addMod(wsteelGuns, new WeaponModGenericDurability(109));

        /* Ferrouranium passt an das Antimateriegewehr und die M2. */
        Item[] ferroGuns = new Item[] {
                NtmItems.GUN_AMAT.get(), NtmItems.GUN_M2.get() };
        new WeaponModDefinition(ModGeneric.FERRO_DAMAGE).addMod(ferroGuns, new WeaponModGenericDamage(110));
        new WeaponModDefinition(ModGeneric.FERRO_DURA).addMod(ferroGuns, new WeaponModGenericDurability(111));

        /* Technetiumstahl passt an die Minigun. */
        Item[] tcalloyGuns = new Item[] { NtmItems.GUN_MINIGUN.get() };
        new WeaponModDefinition(ModGeneric.TCALLOY_DAMAGE).addMod(tcalloyGuns, new WeaponModGenericDamage(112));
        new WeaponModDefinition(ModGeneric.TCALLOY_DURA).addMod(tcalloyGuns, new WeaponModGenericDurability(113));

        /* Die Legierung des grossen Berges passt an die StG 77. */
        Item[] bigmtGuns = new Item[] { NtmItems.GUN_STG77.get() };
        new WeaponModDefinition(ModGeneric.BIGMT_DAMAGE).addMod(bigmtGuns, new WeaponModGenericDamage(114));
        new WeaponModDefinition(ModGeneric.BIGMT_DURA).addMod(bigmtGuns, new WeaponModGenericDurability(115));

        /* Der Schalldaempfer: er macht die Waffe leiser und benennt sie um. */
        new WeaponModDefinition(ModSpecial.SILENCER)
                .addMod(new Item[] {
                        NtmItems.GUN_AM180.get(),
                        NtmItems.GUN_UZI.get(), NtmItems.GUN_UZI_AKIMBO.get(),
                        NtmItems.GUN_STAR_F.get(), NtmItems.GUN_STAR_F_AKIMBO.get(),
                        NtmItems.GUN_AMAT.get(), NtmItems.GUN_AMAT_SUBTLETY.get(),
                        NtmItems.GUN_G3.get() }, new WeaponModSilencer(ID_SILENCER));

        /* Das Zielfernrohr: Bild statt Fadenkreuz. */
        new WeaponModDefinition(ModSpecial.SCOPE)
                .addMod(new Item[] {
                        NtmItems.GUN_G3.get(),
                        NtmItems.GUN_CARBINE.get(),
                        NtmItems.GUN_MAS36.get() }, new WeaponModScope(ID_SCOPE));

        /* Die Saege: kuerzerer Lauf, mehr Streuung, mehr Schaden -- an der G3 der Schaft. */
        new WeaponModDefinition(ModSpecial.SAW)
                .addMod(new Item[] {
                        NtmItems.GUN_MARESLEG.get(),
                        NtmItems.GUN_DOUBLE_BARREL.get() }, new WeaponModSawedOff(ID_SAWED_OFF))
                .addMod(new Item[] { NtmItems.GUN_G3.get(), NtmItems.GUN_G3_ZEBRA.get() }, new WeaponModG3SawedOff(ID_NO_STOCK));

        /* Der aufgearbeitete Schaft der M3: dreifache Haltbarkeit, mehr Schaden, keine Streuung. */
        new WeaponModDefinition(ModSpecial.GREASEGUN)
                .addMod(NtmItems.GUN_GREASEGUN.get(), new WeaponModGreasegun(ID_GREASEGUN_CLEAN));

        /* Die beiden Kunststoffschaefte der G3 -- gleiches Verhalten, verschiedene Texturen. */
        new WeaponModDefinition(ModSpecial.FURNITURE_GREEN)
                .addMod(NtmItems.GUN_G3.get(), new WeaponModPolymerFurniture(ID_FURNITURE_GREEN));
        new WeaponModDefinition(ModSpecial.FURNITURE_BLACK)
                .addMod(NtmItems.GUN_G3.get(), new WeaponModPolymerFurniture(ID_FURNITURE_BLACK));

        /* Die Drossel: halbe Schussfolge, dafuer keine Streuung mehr. */
        new WeaponModDefinition(ModSpecial.SLOWDOWN)
                .addMod(new Item[] { NtmItems.GUN_MINIGUN.get(), NtmItems.GUN_MINIGUN_DUAL.get() }, new WeaponModSlowdown(207));

        /* Der Schnellauf: dreifache Schusszahl, dafuer anderthalbfache Streuung. */
        new WeaponModDefinition(ModSpecial.SPEEDUP)
                .addMod(new Item[] { NtmItems.GUN_MINIGUN.get(), NtmItems.GUN_MINIGUN_DUAL.get() }, new WeaponModMinigunSpeedup(ID_MINIGUN_SPEED));

        /*
         * Das Bajonett. Es macht aus dem Betrachten einen Stoss. Karabiner und MAS-36 bekommen
         * eigene Nummern, weil sie verschiedene Animationssaetze durchreichen.
         */
        new WeaponModDefinition(ModSpecial.BAYONET)
                .addMod(NtmItems.GUN_MAS36.get(), new WeaponModBayonet(ID_MAS_BAYONET,
                        XFactory762mm.LAMBDA_MAS36_ANIMS, Orchestras.ORCHESTRA_MAS36))
                .addMod(NtmItems.GUN_CARBINE.get(), new WeaponModBayonet(ID_CARBINE_BAYONET,
                        XFactory762mm.LAMBDA_CARBINE_ANIMS, Orchestras.ORCHESTRA_CARBINE));

        /* Das Saturnit-Gehaeuse der Uzi. */
        new WeaponModDefinition(ModSpecial.SKIN_SATURNITE)
                .addMod(new Item[] { NtmItems.GUN_UZI.get(), NtmItems.GUN_UZI_AKIMBO.get() }, new WeaponModUziSaturnite(ID_UZI_SATURN));
    }

    public static final int ID_SILENCER = 201;
    public static final int ID_SCOPE = 202;
    public static final int ID_SAWED_OFF = 203;
    public static final int ID_NO_SHIELD = 204;
    public static final int ID_NO_STOCK = 205;
    public static final int ID_GREASEGUN_CLEAN = 206;
    public static final int ID_MINIGUN_SPEED = 208;
    public static final int ID_FURNITURE_GREEN = 211;
    public static final int ID_FURNITURE_BLACK = 212;
    public static final int ID_MAS_BAYONET = 213;
    public static final int ID_UZI_SATURN = 215;
    public static final int ID_CARBINE_BAYONET = 219;

    /** Die Aufsaetze einer Waffe, als Gegenstaende -- so zeigt der Waffentisch sie an. */
    public static ItemStack[] getUpgradeItems(ItemStack stack, int cfg) {

        if(!TagsUtil.hasCustomData(stack)) return new ItemStack[0];

        int[] modIds = TagsUtil.getCustomData(stack).getIntArray(KEY_MOD_LIST + cfg);
        if(modIds.length == 0) return new ItemStack[0];

        ItemStack[] mods = new ItemStack[modIds.length];

        for(int i = 0; i < mods.length; i++) {
            IWeaponMod mod = idToMod.get(modIds[i]);
            ItemStack modStack = mod == null ? null : modToStack.get(mod);
            mods[i] = modStack == null ? ItemStack.EMPTY : modStack.copy();
        }

        return mods;
    }

    public static boolean hasUpgrade(ItemStack stack, int cfg, int id) {

        if(!TagsUtil.hasCustomData(stack)) return false;

        for(int modId : TagsUtil.getCustomData(stack).getIntArray(KEY_MOD_LIST + cfg)) {
            if(modId == id) return true;
        }

        return false;
    }

    /* Der Magazinzustand wird ueber den Umbau hinweg gerettet, soweit er noch passt. */
    private static Object prevMagType;
    private static int prevMagCount;
    private static boolean changedMagState = false;

    public static void changedMagState() { changedMagState = true; }

    private static void saveMagState(ItemStack stack, int cfg) {
        IMagazine<?> mag = ((GunBaseNTItem) stack.getItem()).getConfig(stack, cfg).getReceivers(stack)[0].getMagazine(stack);
        if(mag == null) return;
        prevMagType = mag.getType(stack, null);
        prevMagCount = mag.getAmount(stack, null);
    }

    private static void restoreMagState(ItemStack stack, int cfg) {

        if(!changedMagState) return;
        changedMagState = false;

        IMagazine<?> mag = ((GunBaseNTItem) stack.getItem()).getConfig(stack, cfg).getReceivers(stack)[0].getMagazine(stack);
        if(mag == null) return;

        if(mag.getType(stack, null) == prevMagType) {
            mag.setAmount(stack, Mth.clamp(prevMagCount, 0, mag.getCapacity(stack)));
        } else {
            mag.setAmount(stack, 0);
        }
    }

    /**
     * Setzt die Aufsaetze neu. Erst wird alles abgenommen -- sonst wuerde ein schon steckender
     * Aufsatz ein zweites Mal angebracht --, dann kommt alles in der Reihenfolge der Prioritaet
     * wieder dran.
     */
    public static void install(ItemStack stack, int cfg, ItemStack... mods) {

        saveMagState(stack, cfg);
        uninstall(stack, cfg);

        List<IWeaponMod> toInstall = new ArrayList<>();
        ComparableStack gun = new ComparableStack(stack).makeSingular();

        for(ItemStack mod : mods) {

            if(mod == null || mod.isEmpty()) continue;

            WeaponModDefinition def = stackToMod.get(new ComparableStack(mod).makeSingular());
            if(def == null) continue;

            IWeaponMod forGun = def.modByGun.get(gun);
            if(forGun == null) forGun = def.modByGun.get(null);
            if(forGun != null) toInstall.add(forGun);
        }

        if(toInstall.isEmpty()) return;
        toInstall.sort(MOD_SORTER);

        int[] modIds = new int[toInstall.size()];

        for(int i = 0; i < modIds.length; i++) {
            IWeaponMod mod = toInstall.get(i);
            modIds[i] = idToMod.inverse().get(mod);
            onInstallStack(stack, modToStack.get(mod), cfg);
        }

        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.putIntArray(KEY_MOD_LIST + cfg, modIds);
        TagsUtil.putCustomData(stack, tag);

        restoreMagState(stack, cfg);
    }

    /** Nimmt alle Aufsaetze ab. */
    public static void uninstall(ItemStack stack, int cfg) {

        if(stack == null || stack.isEmpty() || !TagsUtil.hasCustomData(stack)) return;

        for(ItemStack mod : getUpgradeItems(stack, cfg)) onUninstallStack(stack, mod, cfg);

        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.remove(KEY_MOD_LIST + cfg);
        TagsUtil.putCustomData(stack, tag);
    }

    public static void onInstallStack(ItemStack gun, ItemStack mod, int cfg) {
        IWeaponMod newMod = modFromStack(gun, mod, cfg);
        if(newMod != null) newMod.onInstall(gun, mod, cfg);
    }

    public static void onUninstallStack(ItemStack gun, ItemStack mod, int cfg) {
        IWeaponMod newMod = modFromStack(gun, mod, cfg);
        if(newMod != null) newMod.onUninstall(gun, mod, cfg);
    }

    public static IWeaponMod modFromStack(ItemStack gun, ItemStack mod, int cfg) {

        if(gun == null || gun.isEmpty() || mod == null || mod.isEmpty()) return null;

        WeaponModDefinition def = stackToMod.get(new ComparableStack(mod).makeSingular());
        if(def == null) return null;

        /* Beim Verschieben mit Umschalt hat die Waffe Stapelgroesse 0 -- daher makeSingular. */
        IWeaponMod newMod = def.modByGun.get(new ComparableStack(gun).makeSingular());
        if(newMod == null) newMod = def.modByGun.get(null);

        return newMod;
    }

    /**
     * Passt der Aufsatz an diese Waffe? Mit checkMutex wird zusaetzlich geprueft, ob der Platz
     * schon belegt ist -- zwei Schalldaempfer gehen nicht.
     */
    public static boolean isApplicable(ItemStack gun, ItemStack mod, int cfg, boolean checkMutex) {

        IWeaponMod newMod = modFromStack(gun, mod, cfg);
        if(newMod == null) return false;

        if(checkMutex && TagsUtil.hasCustomData(gun)) {
            for(int i : TagsUtil.getCustomData(gun).getIntArray(KEY_MOD_LIST + cfg)) {
                IWeaponMod installed = idToMod.get(i);
                if(installed == null) continue;
                for(String slot0 : newMod.getSlots()) {
                    for(String slot1 : installed.getSlots()) {
                        if(slot0.equals(slot1)) return false;
                    }
                }
            }
        }

        return true;
    }

    /** Hoehere Prioritaet zuerst: was multipliziert, greift vor dem, was addiert. */
    public static final Comparator<IWeaponMod> MOD_SORTER = (a, b) -> b.getModPriority() - a.getModPriority();

    /** Scrapes all upgrades, iterates over them and evaluates the given value. The parent (i.e. holder of the base value)
     * is passed for context (so upgrades can differentiate primary and secondary receivers for example). Passing a null
     * stack causes the base value to be returned. */
    public static <T> T eval(T base, ItemStack stack, String key, Object parent, int cfg) {
        if(stack == null) return base;
        if(!TagsUtil.hasCustomData(stack)) return base;
        CompoundTag tag = TagsUtil.getCustomData(stack);

        for(int i : tag.getIntArray(KEY_MOD_LIST + cfg)) {
            IWeaponMod mod = idToMod.get(i);
            if(mod != null) base = mod.eval(base, stack, key, parent);
        }

        return base;
    }

    public static class WeaponModDefinition {

        /** Holds the weapon mod handlers for each given gun. Key null refers to mods that apply to ALL guns that are otherwise not included. */
        public HashMap<ComparableStack, IWeaponMod> modByGun = new HashMap<>();
        public ItemStack stack;

        public WeaponModDefinition(ItemStack stack) {
            this.stack = stack;
            stackToMod.put(new ComparableStack(stack).makeSingular(), this);
        }

        public WeaponModDefinition(ModGeneric num) {
            this(MetaHelper.newStack(NtmItems.WEAPON_MOD_GENERIC.get(), 1, num.ordinal()));
        }

        public WeaponModDefinition(ModSpecial num) {
            this(MetaHelper.newStack(NtmItems.WEAPON_MOD_SPECIAL.get(), 1, num.ordinal()));
        }

        public WeaponModDefinition(com.hbm.items.weapon.sedna.factory.GunFactory.ModCaliber num) {
            this(MetaHelper.newStack(NtmItems.WEAPON_MOD_CALIBER.get(), 1, num.ordinal()));
        }

        public WeaponModDefinition addMod(ItemStack gun, IWeaponMod mod) { return addMod(new ComparableStack(gun), mod); }
        public WeaponModDefinition addMod(Item gun, IWeaponMod mod) { return addMod(new ComparableStack(gun), mod); }
        public WeaponModDefinition addMod(Item[] gun, IWeaponMod mod) { for(Item item : gun) addMod(new ComparableStack(item), mod); return this; }
        public WeaponModDefinition addMod(ComparableStack gun, IWeaponMod mod) {
            modByGun.put(gun, mod);
            modToStack.put(mod, stack);
            if(gun != null) {
                GunBaseNTItem nt = (GunBaseNTItem) gun.item;
                ComparableStack comp = new ComparableStack(stack);
                if(!nt.recognizedMods.contains(comp)) nt.recognizedMods.add(comp);
            }
            return this;
        }

        public WeaponModDefinition addDefault(IWeaponMod mod) {
            return addMod((ComparableStack) null, mod);
        }
    }
}
