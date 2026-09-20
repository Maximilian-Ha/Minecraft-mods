package com.hbm.inventory.recipes;

import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.TagStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ItemEnums.ChunkType;
import com.hbm.items.food.ConserveItem.ConserveType;
import com.hbm.items.ItemEnums.SecretType;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import com.hbm.items.weapon.sedna.factory.GunFactory.AmmoSecret;
import com.hbm.items.weapon.sedna.factory.GunFactory.ModSpecial;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.PedestalRecipes.
 *
 * Neun Sockel, einer in der Mitte, acht ringsum im Abstand drei. Wer die richtigen Sachen
 * auflegt und den mittleren mit Redstone beschickt, bekommt ein Einzelstueck -- eine Waffe,
 * die es sonst nirgends gibt. Das ist die einzige Quelle dieser Stuecke im ganzen Spiel.
 *
 * ZWEI REZEPTMENGEN. recipeSet 0 und 1; die Tontafel zeigt spaeter je nach Metadatenwert die
 * eine oder die andere. Der Unterschied ist die Stufe: Menge 1 sind die Stuecke, die
 * Geheimstuecke voraussetzen.
 *
 * DIE ZUSATZBEDINGUNGEN sind die Eigenart dieses Systems. Ein Rezept kann Vollmond,
 * Neumond, Tageslicht oder einen bestimmten Ruf des Spielers verlangen; geprueft wird das
 * im Block, nicht hier.
 *
 * DER JSON-TEIL DES ORIGINALS IST NICHT UEBERNOMMEN. Dort erbt die Klasse von
 * SerializableRecipe und schreibt sich nach hbmPedestal.json; der Port hat dieses
 * Ladesystem nicht, und alle anderen Rezeptlisten stehen hier ebenso fest im Quelltext.
 *
 * VIER REZEPTE DES ORIGINALS FEHLEN, und jedes mit benanntem Grund -- siehe fehlende().
 */
public class PedestalRecipes {

    public static final List<PedestalRecipe> recipes = new ArrayList<>();

    /** Zwei Mengen wie im Original; die Tontafel liest spaeter genau hier. */
    @SuppressWarnings("unchecked")
    public static final List<PedestalRecipe>[] recipeSets = new List[] { new ArrayList<PedestalRecipe>(), new ArrayList<PedestalRecipe>() };

    public enum PedestalExtraCondition {
        NONE, FULL_MOON, NEW_MOON, SUN, GOOD_KARMA, BAD_KARMA
    }

    public static class PedestalRecipe {

        public final ItemStack output;
        public final AStack[] input;
        public int recipeSet = 0;
        public PedestalExtraCondition extra = PedestalExtraCondition.NONE;

        public PedestalRecipe(ItemStack output, AStack... input) {
            this.output = output;
            this.input = input;
        }

        public PedestalRecipe extra(PedestalExtraCondition extra) { this.extra = extra; return this; }
        public PedestalRecipe set(int set) { this.recipeSet = set; return this; }
    }

    public static void register(PedestalRecipe recipe) {
        recipes.add(recipe);
        recipeSets[Math.abs(recipe.recipeSet) % recipeSets.length].add(recipe);
    }

    /* Kuerzel, damit die neun Plaetze eines Rezepts in drei Zeilen passen wie im Original. */
    private static ComparableStack st(Item item) { return new ComparableStack(item); }
    private static ComparableStack st(Item item, int size) { return new ComparableStack(item, size); }
    private static ComparableStack meta(Item item, int size, Enum<?> value) { return new ComparableStack(item, size, value); }
    private static TagStack shape(MaterialShapes shape, NTMMaterial mat) { return new TagStack(shape.getTag(mat)); }
    private static TagStack shape(MaterialShapes shape, NTMMaterial mat, int size) { return new TagStack(shape.getTag(mat), size); }

    public static void register() {

        recipes.clear();
        for(List<PedestalRecipe> set : recipeSets) set.clear();

        /* Dani. Blei oben und unten, Gold links und rechts, der leichte Revolver in der Mitte. */
        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_LIGHT_REVOLVER_DANI.get()),
                null,                               st(NtmItems.PLATE_LEAD.get()),                  null,
                st(NtmItems.PLATE_GOLD.get()),      st(NtmItems.GUN_LIGHT_REVOLVER.get()),          st(NtmItems.PLATE_GOLD.get()),
                null,                               st(NtmItems.PLATE_LEAD.get()),                  null));

        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_MARESLEG_BROKEN.get()),
                st(NtmBlocks.BARBED_WIRE.asItem()), st(NtmItems.PLATE_WEAPON_STEEL.get()),          st(NtmBlocks.BARBED_WIRE.asItem()),
                st(NtmItems.PLATE_WEAPON_STEEL.get()), st(NtmItems.GUN_MARESLEG.get()),             st(NtmItems.PLATE_WEAPON_STEEL.get()),
                st(NtmBlocks.BARBED_WIRE.asItem()), st(NtmItems.PLATE_WEAPON_STEEL.get()),          st(NtmBlocks.BARBED_WIRE.asItem())));

        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_HEAVY_REVOLVER_LILMAC.get()),
                null,                               meta(NtmItems.WEAPON_MOD_SPECIAL.get(), 1, ModSpecial.SCOPE), null,
                st(NtmItems.POWDER_MAGIC.get()),    st(NtmItems.GUN_HEAVY_REVOLVER.get()),          st(NtmItems.PLATE_WEAPON_STEEL.get()),
                null,                               shape(MaterialShapes.GRIP, Mats.MAT_IVORY),     st(Items.APPLE, 3)));

        /* Der Protege. ModBlocks.chain des Originals heisst dort dungeon_chain -- der Port
         * fuehrt ihn unter diesem Registriernamen, nicht unter dem Feldnamen. */
        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_HEAVY_REVOLVER_PROTEGE.get()),
                st(NtmBlocks.DUNGEON_CHAIN.asItem(), 16), st(NtmItems.CINNABAR.get()),              st(NtmBlocks.DUNGEON_CHAIN.asItem(), 16),
                st(NtmItems.SCRAP_NUCLEAR.get()),   st(NtmItems.GUN_HEAVY_REVOLVER.get()),          st(NtmItems.SCRAP_NUCLEAR.get()),
                st(NtmBlocks.DUNGEON_CHAIN.asItem(), 16), st(NtmItems.CINNABAR.get()),              st(NtmBlocks.DUNGEON_CHAIN.asItem(), 16)));

        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_AMAT_SUBTLETY.get()),
                st(NtmItems.INGOT_STARMETAL.get()), shape(MaterialShapes.CASTPLATE, Mats.MAT_ALUMINIUM), st(NtmItems.INGOT_STARMETAL.get()),
                shape(MaterialShapes.CASTPLATE, Mats.MAT_ALUMINIUM), st(NtmItems.GUN_AMAT.get()),   shape(MaterialShapes.CASTPLATE, Mats.MAT_ALUMINIUM),
                st(NtmItems.INGOT_STARMETAL.get()), shape(MaterialShapes.CASTPLATE, Mats.MAT_ALUMINIUM), st(NtmItems.INGOT_STARMETAL.get())));

        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_AMAT_PENANCE.get()),
                st(NtmItems.INGOT_STARMETAL.get()), shape(MaterialShapes.CASTPLATE, Mats.MAT_DURA), st(NtmItems.INGOT_STARMETAL.get()),
                meta(NtmItems.WEAPON_MOD_SPECIAL.get(), 1, ModSpecial.SILENCER), st(NtmItems.GUN_AMAT.get()), meta(NtmItems.WEAPON_MOD_SPECIAL.get(), 1, ModSpecial.FURNITURE_BLACK),
                st(NtmItems.INGOT_STARMETAL.get()), shape(MaterialShapes.CASTPLATE, Mats.MAT_DURA), st(NtmItems.INGOT_STARMETAL.get())));

        /* Der Daybreaker -- das siebzehnte und letzte Sockelrezept, Runde 234. Es hing bis
         * dahin allein an der Dynamitstange. */
        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_FLAMER_DAYBREAKER.get()),
                shape(MaterialShapes.CASTPLATE, Mats.MAT_GOLD), meta(NtmItems.CANNED_CONSERVE.get(), 1, ConserveType.SLIME), shape(MaterialShapes.CASTPLATE, Mats.MAT_GOLD),
                st(NtmItems.INGOT_PHOSPHORUS.get()), st(NtmItems.GUN_FLAMER.get()),                st(NtmItems.INGOT_PHOSPHORUS.get()),
                shape(MaterialShapes.CASTPLATE, Mats.MAT_GOLD), st(NtmItems.STICK_DYNAMITE.get()), shape(MaterialShapes.CASTPLATE, Mats.MAT_GOLD))
                .extra(PedestalExtraCondition.SUN));

        /* Die Sexy Shotgun. Das Rezept mit den meisten Beutestuecken: Spiessbolzen, Wild P
         * und die beiden Spielkarten -- alle vier kamen erst mit Runde 232. */
        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_AUTOSHOTGUN_SEXY.get()),
                st(NtmItems.BOLT_SPIKE.get(), 16), st(NtmItems.WILD_P.get()),                   st(NtmItems.BOLT_SPIKE.get(), 16),
                st(NtmItems.CARD_QOS.get()),       st(NtmItems.GUN_AUTOSHOTGUN.get()),           st(NtmItems.CARD_AOS.get()),
                st(NtmItems.BOLT_SPIKE.get(), 16), st(NtmItems.INGOT_STARMETAL.get(), 16),       st(NtmItems.BOLT_SPIKE.get(), 16)));

        /* Die Lacunae. Erstes Rezept mit einem Geheimstueck -- und mit dem Vollmond. */
        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_MINIGUN_LACUNAE.get()),
                null,                               st(NtmItems.POWDER_MAGIC.get(), 4),             null,
                meta(NtmItems.ITEM_SECRET.get(), 4, SecretType.SELENIUM_STEEL), st(NtmItems.GUN_MINIGUN.get()), meta(NtmItems.ITEM_SECRET.get(), 4, SecretType.SELENIUM_STEEL),
                null,                               st(NtmItems.POWDER_MAGIC.get(), 4),             null)
                .extra(PedestalExtraCondition.FULL_MOON));

        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_LASER_PISTOL_MORNING_GLORY.get()),
                null,                               st(NtmItems.MORNING_GLORY.get()),            null,
                meta(NtmItems.ITEM_SECRET.get(), 2, SecretType.SELENIUM_STEEL), st(NtmItems.GUN_LASER_PISTOL.get()), meta(NtmItems.ITEM_SECRET.get(), 2, SecretType.SELENIUM_STEEL),
                null,                               st(Items.EMERALD, 16),                       null));

        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_FOLLY.get()),
                meta(NtmItems.ITEM_SECRET.get(), 4, SecretType.FOLLY), meta(NtmItems.ITEM_SECRET.get(), 2, SecretType.CONTROLLER), meta(NtmItems.ITEM_SECRET.get(), 4, SecretType.FOLLY),
                st(NtmItems.INGOT_BSCCO.get(), 16), st(NtmBlocks.BLOCK_STARMETAL.asItem(), 64),     st(NtmItems.INGOT_BSCCO.get(), 16),
                meta(NtmItems.ITEM_SECRET.get(), 4, SecretType.FOLLY), meta(NtmItems.ITEM_SECRET.get(), 2, SecretType.CONTROLLER), meta(NtmItems.ITEM_SECRET.get(), 4, SecretType.FOLLY))
                .extra(PedestalExtraCondition.FULL_MOON).set(1));

        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_ABERRATOR.get()),
                null,                               meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR), null,
                meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR), shape(MaterialShapes.MECHANISM, Mats.MAT_SATURN, 4), meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR),
                null,                               meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR), null).set(1));

        register(new PedestalRecipe(new ItemStack(NtmItems.GUN_ABERRATOR_EOTT.get()),
                meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR), meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR), meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR),
                meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR), shape(MaterialShapes.MECHANISM, Mats.MAT_SATURN, 16), meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR),
                meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR), meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR), meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR))
                .extra(PedestalExtraCondition.GOOD_KARMA).set(1));

        register(new PedestalRecipe(MetaHelper.newStack(NtmItems.AMMO_SECRET.get(), 1, AmmoSecret.FOLLY_SM.ordinal()),
                st(NtmItems.INGOT_STARMETAL.get()), st(NtmItems.POWDER_MAGIC.get()),            st(NtmItems.INGOT_STARMETAL.get()),
                st(NtmItems.POWDER_MAGIC.get()),    meta(NtmItems.CHUNK_ORE.get(), 1, ChunkType.MOONSTONE), st(NtmItems.POWDER_MAGIC.get()),
                st(NtmItems.INGOT_STARMETAL.get()), st(NtmItems.POWDER_MAGIC.get()),            st(NtmItems.INGOT_STARMETAL.get()))
                .extra(PedestalExtraCondition.FULL_MOON).set(1));

        register(new PedestalRecipe(MetaHelper.newStack(NtmItems.AMMO_SECRET.get(), 1, AmmoSecret.FOLLY_NUKE.ordinal()),
                st(NtmItems.INGOT_STARMETAL.get()), st(NtmItems.POWDER_MAGIC.get()),                st(NtmItems.INGOT_STARMETAL.get()),
                st(NtmItems.POWDER_MAGIC.get()),    meta(NtmItems.AMMO_STANDARD.get(), 4, Ammo.NUKE_HIGH), st(NtmItems.POWDER_MAGIC.get()),
                st(NtmItems.INGOT_STARMETAL.get()), st(NtmItems.POWDER_MAGIC.get()),                st(NtmItems.INGOT_STARMETAL.get()))
                .extra(PedestalExtraCondition.FULL_MOON).set(1));

        /* Die beiden Munitionsrezepte ohne Ring: ein Geheimstueck allein in der Mitte. */
        register(new PedestalRecipe(MetaHelper.newStack(NtmItems.AMMO_SECRET.get(), 5, AmmoSecret.P35_800.ordinal()),
                null, null,                                                        null,
                null, meta(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR),   null,
                null, null,                                                        null).set(1));

        register(new PedestalRecipe(MetaHelper.newStack(NtmItems.AMMO_SECRET.get(), 10, AmmoSecret.P35_800_BL.ordinal()),
                null, null,                                                        null,
                null, meta(NtmItems.ITEM_SECRET.get(), 3, SecretType.ABERRATOR),   null,
                null, null,                                                        null).set(1));
    }

    /**
     * DIE LISTE IST VOLLSTAENDIG. Alle siebzehn Rezepte des Originals stehen oben.
     *
     * Sie war es nicht immer: Runde 231 hat dreizehn angelegt und vier mit benannter Ursache
     * offengelassen, Runde 232 hat sechs Zutaten nachgereicht und drei davon eingesetzt, und
     * Runde 234 hat mit der Dynamitstange das letzte geschlossen. Keine der vier Ursachen
     * war eine Vermutung -- jede nannte den fehlenden Gegenstand beim Registriernamen.
     */
    private PedestalRecipes() { }
}
