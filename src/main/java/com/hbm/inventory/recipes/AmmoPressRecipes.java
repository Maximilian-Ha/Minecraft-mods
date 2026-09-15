package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.BoltItem;
import com.hbm.items.ItemEnums.CasingType;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.AmmoPressRecipes.
 *
 * DIE ERSTEN MUNITIONSREZEPTE, DIE DER PORT UEBERHAUPT HAT. Die Waffen stehen seit den Runden 81
 * bis 87 da, ihre Munition war bis jetzt nur ueber Befehle zu bekommen. Neunundsiebzig der
 * neunundachtzig Rezepte des Originals sind hier.
 *
 * JEDES REZEPT IST EIN DREIMALDREI-MUSTER, und die Presse kennt kein Ergebnis ausser dem
 * eingestellten: wer nichts einstellt, presst nichts, egal was im Gitter liegt.
 *
 * DAS ERZWOERTERBUCH GIBT ES IM PORT NICHT. Wo das Original "irgendein Kunststoff" oder
 * "irgendein rauchloses Pulver" sagt, steht hier der Gegenstand selbst: das Polymer, das Kordit,
 * der TNT-Ball. Ein Billet Silizium sind nach MaterialShapes des Ports sechs Nuggets; Silizium
 * gibt es hier nur als Nugget, also stehen die Vielfachen davon da.
 *
 * ZEHN REZEPTE FEHLEN, weil ihnen ein Gegenstand fehlt, den der Port nicht hat:
 *
 *   - G40_INC, ROCKET_INC (zweimal), FLAME_DIESEL, FLAME_GAS, FLAME_BALEFIRE brauchen einen
 *     Kanister mit tausend Millibar Diesel, Gas oder Balefire. Der Port kennt nur den leeren
 *     Kanister und den Napalmkanister -- FLAME_NAPALM ist deshalb dabei, die anderen vier nicht.
 *   - NUKE_STANDARD, NUKE_DEMO, NUKE_HIGH und NUKE_BALEFIRE brauchen die Minibomben-Huelle
 *     (assembly_nuke, "Mini Nuke Shell"). Die gibt es im Port nicht. NUKE_TOTS und NUKE_HIVE
 *     kommen ohne sie aus und sind dabei.
 *
 * Wer eines der beiden nachreicht, traegt die Rezepte hier nach; sie stehen im Original
 * unveraendert bereit.
 */
public class AmmoPressRecipes extends SerializableRecipe {

    public static List<AmmoPressRecipe> recipes = new ArrayList<>();

    @Override
    public void registerDefaults() {

        if(!recipes.isEmpty()) return;

        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 16, Ammo.M357_BP),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 2), null,
                null, new ComparableStack(Items.GUNPOWDER, 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 8, Ammo.M357_SP),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 8, Ammo.M357_FMJ),
                null, new ComparableStack(NtmItems.INGOT_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 8, Ammo.M357_JHP),
                new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), new ComparableStack(NtmItems.INGOT_COPPER.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 8, Ammo.M357_AP),
                null, new ComparableStack(NtmItems.INGOT_WEAPON_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 8, Ammo.M357_EXPRESS),
                null, new ComparableStack(NtmItems.INGOT_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 3), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.M44_BP),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 2), null,
                null, new ComparableStack(Items.GUNPOWDER, 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.M44_SP),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.M44_FMJ),
                null, new ComparableStack(NtmItems.INGOT_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.M44_JHP),
                new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), new ComparableStack(NtmItems.INGOT_COPPER.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.M44_AP),
                null, new ComparableStack(NtmItems.INGOT_WEAPON_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.M44_EXPRESS),
                null, new ComparableStack(NtmItems.INGOT_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 3), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 24, Ammo.P22_SP),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 24, Ammo.P22_FMJ),
                null, new ComparableStack(NtmItems.INGOT_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 24, Ammo.P22_JHP),
                new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), new ComparableStack(NtmItems.INGOT_COPPER.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 24, Ammo.P22_AP),
                null, new ComparableStack(NtmItems.INGOT_WEAPON_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.P9_SP),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.P9_FMJ),
                null, new ComparableStack(NtmItems.INGOT_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.P9_JHP),
                new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), new ComparableStack(NtmItems.INGOT_COPPER.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.P9_AP),
                null, new ComparableStack(NtmItems.INGOT_WEAPON_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 8, Ammo.P45_SP),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 8, Ammo.P45_FMJ),
                null, new ComparableStack(NtmItems.INGOT_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 8, Ammo.P45_JHP),
                new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), new ComparableStack(NtmItems.INGOT_COPPER.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 8, Ammo.P45_AP),
                null, new ComparableStack(NtmItems.INGOT_WEAPON_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 8, Ammo.P45_DU),
                null, new ComparableStack(NtmItems.INGOT_U238.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SMALL_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 16, Ammo.R556_SP),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 2, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 16, Ammo.R556_FMJ),
                null, new ComparableStack(NtmItems.INGOT_STEEL.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 2, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 16, Ammo.R556_JHP),
                new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), new ComparableStack(NtmItems.INGOT_COPPER.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 2, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 16, Ammo.R556_AP),
                null, new ComparableStack(NtmItems.INGOT_WEAPON_STEEL.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 4), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 2, CasingType.SMALL_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.R762_SP),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 2, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.R762_FMJ),
                null, new ComparableStack(NtmItems.INGOT_STEEL.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 2, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.R762_JHP),
                new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), new ComparableStack(NtmItems.INGOT_COPPER.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 2, CasingType.SMALL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.R762_AP),
                null, new ComparableStack(NtmItems.INGOT_WEAPON_STEEL.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 4), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 2, CasingType.SMALL_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.R762_DU),
                null, new ComparableStack(NtmItems.INGOT_U238.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 4), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 2, CasingType.SMALL_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.R762_HE),
                new ComparableStack(NtmItems.BALL_TNT.get(), 1), new ComparableStack(NtmItems.INGOT_FERROURANIUM.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 4), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 2, CasingType.SMALL_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.BMG50_SP),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 3), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.BMG50_FMJ),
                null, new ComparableStack(NtmItems.INGOT_STEEL.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 3), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.BMG50_JHP),
                new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), new ComparableStack(NtmItems.INGOT_COPPER.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 3), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.BMG50_AP),
                null, new ComparableStack(NtmItems.INGOT_WEAPON_STEEL.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 6), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.BMG50_DU),
                null, new ComparableStack(NtmItems.INGOT_U238.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 6), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 12, Ammo.BMG50_HE),
                new ComparableStack(NtmItems.BALL_TNT.get(), 1), new ComparableStack(NtmItems.INGOT_FERROURANIUM.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 6), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE_STEEL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.G12_BP),
                null, new ComparableStack(NtmItems.NUGGET_LEAD.get(), 6), null,
                null, new ComparableStack(Items.GUNPOWDER, 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SHOTSHELL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.G12_BP_MAGNUM),
                null, new ComparableStack(NtmItems.NUGGET_LEAD.get(), 8), null,
                null, new ComparableStack(Items.GUNPOWDER, 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SHOTSHELL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.G12_BP_SLUG),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 1), null,
                null, new ComparableStack(Items.GUNPOWDER, 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.SHOTSHELL)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.G12),
                null, new ComparableStack(NtmItems.NUGGET_LEAD.get(), 6), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.BUCKSHOT)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.G12_SLUG),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.BUCKSHOT)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.G12_FLECHETTE),
                null, new ComparableStack(MetaHelper.newStack(NtmItems.BOLT.get(), 12, BoltItem.Type.LEAD.meta)), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.BUCKSHOT)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.G12_MAGNUM),
                null, new ComparableStack(NtmItems.NUGGET_LEAD.get(), 8), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.BUCKSHOT_ADVANCED)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.G12_EXPLOSIVE),
                null, new ComparableStack(NtmItems.BALL_TNT.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.BUCKSHOT_ADVANCED)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 6, Ammo.G12_PHOSPHORUS),
                null, new ComparableStack(NtmItems.INGOT_PHOSPHORUS.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.BUCKSHOT_ADVANCED)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.G10),
                null, new ComparableStack(NtmItems.NUGGET_LEAD.get(), 8), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.BUCKSHOT_ADVANCED)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.G10_SHRAPNEL),
                new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), new ComparableStack(NtmItems.NUGGET_LEAD.get(), 8), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.BUCKSHOT_ADVANCED)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.G10_DU),
                null, new ComparableStack(NtmItems.INGOT_U238.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.BUCKSHOT_ADVANCED)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.G10_SLUG),
                null, new ComparableStack(NtmItems.INGOT_LEAD.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.BUCKSHOT_ADVANCED)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.G10_EXPLOSIVE),
                new ComparableStack(NtmItems.BALL_TNT.get(), 1), new ComparableStack(NtmItems.INGOT_FERROURANIUM.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.BUCKSHOT_ADVANCED)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.G26_FLARE),
                null, new ComparableStack(NtmItems.POWDER_FIRE.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.G40_HE),
                null, new ComparableStack(NtmItems.BALL_DYNAMITE.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.G40_HEAT),
                new ComparableStack(NtmItems.PLATE_COPPER.get(), 1), new ComparableStack(NtmItems.BALL_TNT.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.G40_DEMO),
                null, new ComparableStack(NtmItems.BALL_TNT.get(), 2), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.G40_PHOSPHORUS),
                new ComparableStack(NtmItems.INGOT_PHOSPHORUS.get(), 1), new ComparableStack(NtmItems.BALL_TNT.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 2, Ammo.ROCKET_HE),
                null, new ComparableStack(NtmItems.BALL_DYNAMITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 3), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 2, Ammo.ROCKET_HE),
                null, new ComparableStack(NtmItems.BALL_DYNAMITE.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null,
                null, new ComparableStack(NtmItems.ROCKET_FUEL.get(), 1), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 2, Ammo.ROCKET_HEAT),
                new ComparableStack(NtmItems.PLATE_COPPER.get(), 1), new ComparableStack(NtmItems.BALL_TNT.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 3), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 2, Ammo.ROCKET_HEAT),
                new ComparableStack(NtmItems.PLATE_COPPER.get(), 1), new ComparableStack(NtmItems.BALL_TNT.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null,
                null, new ComparableStack(NtmItems.ROCKET_FUEL.get(), 1), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 2, Ammo.ROCKET_DEMO),
                null, new ComparableStack(NtmItems.BALL_TNT.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 3), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 2, Ammo.ROCKET_DEMO),
                null, new ComparableStack(NtmItems.BALL_TNT.get(), 2), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null,
                null, new ComparableStack(NtmItems.ROCKET_FUEL.get(), 1), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 2, Ammo.ROCKET_PHOSPHORUS),
                new ComparableStack(NtmItems.INGOT_PHOSPHORUS.get(), 1), new ComparableStack(NtmItems.BALL_TNT.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 3), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 2, Ammo.ROCKET_PHOSPHORUS),
                new ComparableStack(NtmItems.INGOT_PHOSPHORUS.get(), 1), new ComparableStack(NtmItems.BALL_TNT.get(), 1), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 1, CasingType.LARGE)), null,
                null, new ComparableStack(NtmItems.ROCKET_FUEL.get(), 1), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 1, Ammo.FLAME_NAPALM),
                null, new ComparableStack(NtmItems.PLATE_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CANISTER_NAPALM.get(), 1), null,
                null, new ComparableStack(NtmItems.PLATE_STEEL.get(), 1), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.CAPACITOR),
                null, new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), null,
                null, new ComparableStack(NtmItems.NUGGET_SILICON.get(), 24), null,
                null, new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.CAPACITOR_OVERCHARGE),
                null, new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), null,
                null, new ComparableStack(NtmItems.NUGGET_SILICON.get(), 36), null,
                null, new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.CAPACITOR_IR),
                null, new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), null,
                null, new ComparableStack(NtmItems.INGOT_NIOBIUM.get(), 1), null,
                null, new ComparableStack(NtmItems.INGOT_POLYMER.get(), 1), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 16, Ammo.TAU_URANIUM),
                null, new ComparableStack(NtmItems.PLATE_LEAD.get(), 1), null,
                null, new ComparableStack(NtmItems.INGOT_U238.get(), 1), null,
                null, new ComparableStack(NtmItems.PLATE_LEAD.get(), 1), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.COIL_TUNGSTEN),
                null, null, null,
                null, new ComparableStack(NtmItems.INGOT_TUNGSTEN.get(), 1), null,
                null, null, null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.COIL_FERROURANIUM),
                null, null, null,
                null, new ComparableStack(NtmItems.INGOT_FERROURANIUM.get(), 1), null,
                null, null, null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 1, Ammo.NUKE_TOTS),
                null, new ComparableStack(NtmItems.NUGGET_PU239.get(), 2), null,
                null, new ComparableStack(NtmItems.BALL_TATB.get(), 2), null,
                null, new ComparableStack(NtmItems.PLATE_STEEL.get(), 4), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 1, Ammo.NUKE_HIVE),
                null, new ComparableStack(NtmItems.BALL_TNT.get(), 8), null,
                null, new ComparableStack(MetaHelper.newStack(NtmItems.CASING.get(), 2, CasingType.LARGE_STEEL)), null,
                null, new ComparableStack(NtmItems.PLATE_STEEL.get(), 4), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 16, Ammo.CT_HOOK),
                null, new ComparableStack(NtmItems.INGOT_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.PIPES_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null));
        recipes.add(new AmmoPressRecipe(MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), 4, Ammo.CT_MORTAR),
                null, new ComparableStack(NtmItems.BALL_TNT.get(), 4), null,
                null, new ComparableStack(NtmItems.PIPES_STEEL.get(), 1), null,
                null, new ComparableStack(NtmItems.CORDITE.get(), 1), null));
    }

    @Override
    public String getFileName() {
        return "hbmAmmoPress.json";
    }

    @Override
    public String getComment() {
        return "Input array describes slots from left to right, top to bottom. Make sure the input array is exactly 9 elements long, empty slots are represented by null.";
    }

    @Override
    public Object getRecipeObject() {
        return recipes;
    }

    @Override
    public void deleteRecipes() {
        recipes.clear();
    }

    @Override
    public void readRecipe(JsonElement recipe) {

        JsonObject obj = (JsonObject) recipe;

        ItemStack output = readItemStack(obj.get("output").getAsJsonArray());
        JsonArray inputArray = obj.get("input").getAsJsonArray();
        AStack[] input = new AStack[9];

        for(int i = 0; i < 9; i++) {
            JsonElement element = inputArray.get(i);
            input[i] = element.isJsonNull() ? null : readAStack(element.getAsJsonArray());
        }

        recipes.add(new AmmoPressRecipe(output, input));
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {

        AmmoPressRecipe rec = (AmmoPressRecipe) recipe;

        writer.name("output");
        writeItemStack(rec.output, writer);

        writer.name("input").beginArray();
        for(AStack stack : rec.input) {
            if(stack == null) writer.nullValue();
            else writeAStack(stack, writer);
        }
        writer.endArray();
    }

    public static class AmmoPressRecipe {

        public ItemStack output;
        public AStack[] input;

        public AmmoPressRecipe(ItemStack output, AStack... input) {
            this.output = output;
            this.input = input;
        }
    }
}
