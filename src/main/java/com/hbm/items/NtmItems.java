package com.hbm.items;

import api.hbm.block.IToolable.ToolType;
import com.hbm.blocks.NtmBlocks;
import com.hbm.handler.ability.IToolAreaAbility;
import com.hbm.handler.ability.IToolHarvestAbility;
import com.hbm.handler.ability.IWeaponAbility;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.NtmFoods;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.NtmTiers;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemEnums.CapType;
import com.hbm.items.ItemEnums.U238M2Type;
import com.hbm.items.ItemEnums.CasingType;
import com.hbm.items.armor.ArmorFSBItem;
import com.hbm.items.food.PillItem;
import com.hbm.items.machine.GunPartItem;
import com.hbm.items.special.SimpleConsumableItem;
import com.hbm.items.special.SyringeItem;
import com.hbm.items.armor.ArmorHEVItem;
import com.hbm.items.armor.ArmorNo9;
import com.hbm.items.armor.FilterItem;
import com.hbm.items.armor.GasMaskItem;
import com.hbm.items.armor.ItemModCladding;
import com.hbm.items.armor.ItemModIndestructible;
import com.hbm.items.armor.ItemModInsert;
import com.hbm.items.armor.ItemModKnockback;
import com.hbm.items.armor.ModCharmItem;
import com.hbm.items.armor.NtmArmorMaterials;
import com.hbm.items.food.ConserveItem;
import com.hbm.items.tools.AmmoContainerItem;
import com.hbm.items.tools.MysteryShovelItem;
import com.hbm.items.weapon.grenade.GrenadeExtraItem;
import com.hbm.items.weapon.grenade.GrenadeFillingItem;
import com.hbm.items.weapon.grenade.GrenadeFuzeItem;
import com.hbm.items.weapon.grenade.GrenadeShellItem;
import com.hbm.items.weapon.grenade.GrenadeUniversalItem;
import com.hbm.items.food.DrinkItem;
import com.hbm.items.food.EnergyItem;
import com.hbm.blockentity.machine.rbmk.IRBMKFluxReceiver.NType;
import com.hbm.items.special.DemonCoreItem;
import com.hbm.items.special.BedrockOreBaseItem;
import com.hbm.items.special.BedrockOreFragmentItem;
import com.hbm.items.special.BedrockOreItem;
import com.hbm.items.weapon.CustomMissileItem;
import com.hbm.items.weapon.CustomMissilePartItem;
import com.hbm.items.weapon.CustomMissilePartItem.FuelType;
import com.hbm.items.weapon.CustomMissilePartItem.PartSize;
import com.hbm.items.weapon.CustomMissilePartItem.WarheadType;
import com.hbm.items.machine.DrillbitItem;
import com.hbm.items.machine.DriveItem;
import com.hbm.items.machine.OrbitalAssemblyItem;
import com.hbm.items.machine.PACoilItem;
import com.hbm.items.special.NuclearWasteItem;
import com.hbm.items.special.NuclearWasteItem.WasteClass;
import com.hbm.items.machine.PlateFuelItem;
import com.hbm.items.machine.PlateFuelItem.FunctionType;
import com.hbm.items.machine.ReactorSensorItem;
import com.hbm.items.machine.TurretBiometryItem;
import com.hbm.items.machine.DepletedFuelItem;
import com.hbm.items.machine.ICFPelletItem;
import com.hbm.items.machine.PileRodItem;
import com.hbm.items.machine.PWRFuelItem;
import com.hbm.items.machine.WatzPelletItem;
import com.hbm.items.machine.PWRFuelItem.EnumPWRFuel;
import com.hbm.items.machine.RBMKPelletItem;
import com.hbm.items.machine.RBMKRodItem;
import net.minecraft.ChatFormatting;
import com.hbm.items.tools.RBMKLinkItem;
import com.hbm.items.machine.RBMKRodItem.EnumBurnFunc;
import com.hbm.items.machine.RBMKRodItem.EnumDepleteFunc;
import com.hbm.items.machine.*;
import com.hbm.items.machine.StampItem.StampType;
import com.hbm.items.special.*;
import com.hbm.items.tools.*;
import com.hbm.items.weapon.MissileItem;
import com.hbm.items.weapon.MissileItem.MissileFormFactor;
import com.hbm.items.weapon.MissileItem.MissileFuel;
import com.hbm.items.weapon.MissileItem.MissileTier;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.main.NuclearTechMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Consumer;

public class NtmItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NuclearTechMod.MODID);

    // Ingots, nuggets & fragments
    public static final DeferredItem<Item> INGOT_URANIUM = ITEMS.register("ingot_uranium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_U233 = ITEMS.register("ingot_u233", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_U235 = ITEMS.register("ingot_u235", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_U238 = ITEMS.register("ingot_u238", () -> new Item(new Item.Properties()));
    /* Vier Spielarten: der Barren und die drei Ostereier des bruechigen Spatens. Die
     * Ordnungszahlen sind die Metadaten des Originals. */
    public static final DeferredItem<Item> INGOT_U238M2 = ITEMS.register("ingot_u238m2", () -> new EnumMultiItem(new Item.Properties(), U238M2Type.class, true, true));
    public static final DeferredItem<Item> INGOT_PLUTONIUM = ITEMS.register("ingot_plutonium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_PU238 = ITEMS.register("ingot_pu238", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_PU239 = ITEMS.register("ingot_pu239", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_PU240 = ITEMS.register("ingot_pu240", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_PU241 = ITEMS.register("ingot_pu241", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_PU_MIX = ITEMS.register("ingot_pu_mix", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_AM241 = ITEMS.register("ingot_am241", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_AM242 = ITEMS.register("ingot_am242", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_AM_MIX = ITEMS.register("ingot_am_mix", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_NEPTUNIUM = ITEMS.register("ingot_neptunium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_POLONIUM = ITEMS.register("ingot_polonium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_TECHNETIUM = ITEMS.register("ingot_technetium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_CO60 = ITEMS.register("ingot_co60", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_SR90 = ITEMS.register("ingot_sr90", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_AU198 = ITEMS.register("ingot_au198", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_PB209 = ITEMS.register("ingot_pb209", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_RA226 = ITEMS.register("ingot_ra226", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_TITANIUM = ITEMS.register("ingot_titanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_TUNGSTEN = ITEMS.register("ingot_tungsten", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_ALUMINIUM = ITEMS.register("ingot_aluminium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_STEEL = ITEMS.register("ingot_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_TCALLOY = ITEMS.register("ingot_tcalloy", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_CDALLOY = ITEMS.register("ingot_cdalloy", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_BISMUTH_BRONZE = ITEMS.register("ingot_bismuth_bronze", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_ARSENIC_BRONZE = ITEMS.register("ingot_arsenic_bronze", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_BSCCO = ITEMS.register("ingot_bscco", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_LEAD = ITEMS.register("ingot_lead", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_BISMUTH = ITEMS.register("ingot_bismuth", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_ARSENIC = ITEMS.register("ingot_arsenic", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_CALCIUM = ITEMS.register("ingot_calcium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_CADMIUM = ITEMS.register("ingot_cadmium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_ASBESTOS = ITEMS.register("ingot_asbestos", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_TANTALIUM = ITEMS.register("ingot_tantalium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_SILICON = ITEMS.register("ingot_silicon", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_NIOBIUM = ITEMS.register("ingot_niobium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_BERYLLIUM = ITEMS.register("ingot_beryllium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_COBALT = ITEMS.register("ingot_cobalt", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_BORON = ITEMS.register("ingot_boron", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_GRAPHITE = ITEMS.register("ingot_graphite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_FIREBRICK = ITEMS.register("ingot_firebrick", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_DURA_STEEL = ITEMS.register("ingot_dura_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_POLYMER = ITEMS.register("ingot_polymer", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_BAKELITE = ITEMS.register("ingot_bakelite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_BIORUBBER = ITEMS.register("ingot_biorubber", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_RUBBER = ITEMS.register("ingot_rubber", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_PC = ITEMS.register("ingot_pc", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_PVC = ITEMS.register("ingot_pvc", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_MUD = ITEMS.register("ingot_mud", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_CTF = ITEMS.register("ingot_cft", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_SCHRARANIUM = ITEMS.register("ingot_schraranium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_SCHRABIDIUM = ITEMS.register("ingot_schrabidium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_SCHRABIDATE = ITEMS.register("ingot_schrabidate", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_MAGNETIZED_TUNGSTEN = ITEMS.register("ingot_magnetized_tungsten", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_COMBINE_STEEL = ITEMS.register("ingot_combine_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_SOLINIUM = ITEMS.register("ingot_solinium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_GH336 = ITEMS.register("ingot_gh336", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_URANIUM_FUEL = ITEMS.register("ingot_uranium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_THORIUM_FUEL = ITEMS.register("ingot_thorium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_PLUTONIUM_FUEL = ITEMS.register("ingot_plutonium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_NEPTUNIUM_FUEL = ITEMS.register("ingot_neptunium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_MOX_FUEL = ITEMS.register("ingot_mox_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_AMERICIUM_FUEL = ITEMS.register("ingot_americium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_SCHRABIDIUM_FUEL = ITEMS.register("ingot_schrabidium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_HES = ITEMS.register("ingot_hes", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_LES = ITEMS.register("ingot_les", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_AUSTRALIUM = ITEMS.register("ingot_australium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_LANTHANIUM = ITEMS.register("ingot_lanthanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_ACTINIUM = ITEMS.register("ingot_actinium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_DESH = ITEMS.register("ingot_desh", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_FERROURANIUM = ITEMS.register("ingot_ferrouranium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_STARMETAL = ITEMS.register("ingot_starmetal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_GUNMETAL = ITEMS.register("ingot_gunmetal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_WEAPON_STEEL = ITEMS.register("ingot_weapon_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_SATURNITE = ITEMS.register("ingot_saturnite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_EUPHEMIUM = ITEMS.register("ingot_euphemium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_DINEUTRONIUM = ITEMS.register("ingot_dineutronium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_ELECTRONIUM = ITEMS.register("ingot_electronium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_SMORE = ITEMS.register("ingot_smore", () -> new Item(new Item.Properties().food(NtmFoods.SMORE)));
    public static final DeferredItem<Item> INGOT_OSMIRIDIUM = ITEMS.register("ingot_osmiridium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_ZIRCONIUM = ITEMS.register("ingot_zirconium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LITHIUM = ITEMS.register("lithium", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> NUGGET_URANIUM = registerNugget("nugget_uranium");
    public static final DeferredItem<Item> NUGGET_U233 = registerNugget("nugget_u233");
    public static final DeferredItem<Item> NUGGET_U235 = registerNugget("nugget_u235");
    public static final DeferredItem<Item> NUGGET_U238 = registerNugget("nugget_u238");
    public static final DeferredItem<Item> NUGGET_U238M2 = registerNugget("nugget_u238m2");
    public static final DeferredItem<Item> NUGGET_PLUTONIUM = registerNugget("nugget_plutonium");
    public static final DeferredItem<Item> NUGGET_PU238 = registerNugget("nugget_pu238");
    public static final DeferredItem<Item> NUGGET_PU239 = registerNugget("nugget_pu239");
    public static final DeferredItem<Item> NUGGET_PU240 = registerNugget("nugget_pu240");
    public static final DeferredItem<Item> NUGGET_PU241 = registerNugget("nugget_pu241");
    public static final DeferredItem<Item> NUGGET_PU_MIX = registerNugget("nugget_pu_mix");
    public static final DeferredItem<Item> NUGGET_AM241 = registerNugget("nugget_am241");
    public static final DeferredItem<Item> NUGGET_AM242 = registerNugget("nugget_am242");
    public static final DeferredItem<Item> NUGGET_AM_MIX = registerNugget("nugget_am_mix");
    public static final DeferredItem<Item> NUGGET_TECHNETIUM = registerNugget("nugget_technetium");
    public static final DeferredItem<Item> NUGGET_NEPTUNIUM = registerNugget("nugget_neptunium");
    public static final DeferredItem<Item> NUGGET_POLONIUM = registerNugget("nugget_polonium");
    public static final DeferredItem<Item> NUGGET_THORIUM_FUEL = registerNugget("nugget_thorium_fuel");
    public static final DeferredItem<Item> NUGGET_URANIUM_FUEL = registerNugget("nugget_uranium_fuel");
    public static final DeferredItem<Item> NUGGET_MOX_FUEL = registerNugget("nugget_mox_fuel");
    public static final DeferredItem<Item> NUGGET_PLUTONIUM_FUEL = registerNugget("nugget_plutonium_fuel");
    public static final DeferredItem<Item> NUGGET_NEPTUNIUM_FUEL = registerNugget("nugget_neptunium_fuel");
    public static final DeferredItem<Item> NUGGET_AMERICIUM_FUEL = registerNugget("nugget_americium_fuel");
    public static final DeferredItem<Item> NUGGET_SCHRABIDIUM_FUEL = registerNugget("nugget_schrabidium_fuel");
    public static final DeferredItem<Item> NUGGET_HES = registerNugget("nugget_hes");
    public static final DeferredItem<Item> NUGGET_LES = registerNugget("nugget_les");
    public static final DeferredItem<Item> NUGGET_LEAD = registerNugget("nugget_lead");
    public static final DeferredItem<Item> NUGGET_BERYLLIUM = registerNugget("nugget_beryllium");
    public static final DeferredItem<Item> NUGGET_CADMIUM = registerNugget("nugget_cadmium");
    public static final DeferredItem<Item> NUGGET_BISMUTH = registerNugget("nugget_bismuth");
    public static final DeferredItem<Item> NUGGET_ARSENIC = registerNugget("nugget_arsenic");
    public static final DeferredItem<Item> NUGGET_ZIRCONIUM = registerNugget("nugget_zirconium");
    public static final DeferredItem<Item> NUGGET_TANTALIUM = registerNugget("nugget_tantalium");
    public static final DeferredItem<Item> NUGGET_DESH = registerNugget("nugget_desh");
    public static final DeferredItem<Item> NUGGET_OSMIRIDIUM = registerNugget("nugget_osmiridium");
    public static final DeferredItem<Item> NUGGET_SCHRABIDIUM = registerNugget("nugget_schrabidium");
    public static final DeferredItem<Item> NUGGET_SOLINIUM = registerNugget("nugget_solinium");
    public static final DeferredItem<Item> NUGGET_EUPHEMIUM = registerNugget("nugget_euphemium");
    public static final DeferredItem<Item> NUGGET_DINEUTRONIUM = registerNugget("nugget_dineutronium");
    public static final DeferredItem<Item> NUGGET_NIOBIUM = registerNugget("nugget_niobium");
    public static final DeferredItem<Item> NUGGET_SILICON = registerNugget("nugget_silicon");
    public static final DeferredItem<Item> NUGGET_ACTINIUM = registerNugget("nugget_actinium");
    public static final DeferredItem<Item> NUGGET_COBALT = registerNugget("nugget_cobalt");
    public static final DeferredItem<Item> NUGGET_CO60 = registerNugget("nugget_co60");
    public static final DeferredItem<Item> NUGGET_SR90 = registerNugget("nugget_sr90");
    public static final DeferredItem<Item> NUGGET_PB209 = registerNugget("nugget_pb209");
    public static final DeferredItem<Item> NUGGET_GH336 = registerNugget("nugget_gh336");
    public static final DeferredItem<Item> NUGGET_AU198 = registerNugget("nugget_au198");
    public static final DeferredItem<Item> NUGGET_RA226 = registerNugget("nugget_ra226");
    public static final DeferredItem<Item> FRAGMENT_NIOBIUM = ITEMS.register("fragment_niobium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FRAGMENT_NEODYMIUM = ITEMS.register("fragment_neodymium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FRAGMENT_COBALT = ITEMS.register("fragment_cobalt", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FRAGMENT_CERIUM = ITEMS.register("fragment_cerium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FRAGMENT_BORON = ITEMS.register("fragment_boron", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FRAGMENT_LANTHANIUM = ITEMS.register("fragment_lanthanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FRAGMENT_ACTINIUM = ITEMS.register("fragment_actinium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FRAGMENT_METEORITE = ITEMS.register("fragment_meteorite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COKE_COAL = ITEMS.register("coke_coal", () -> new FuelItem(new Item.Properties(), 3200));
    public static final DeferredItem<Item> COKE_LIGNITE = ITEMS.register("coke_lignite", () -> new FuelItem(new Item.Properties(), 2400));
    public static final DeferredItem<Item> COKE_PETROLEUM = ITEMS.register("coke_petroleum", () -> new FuelItem(new Item.Properties(), 6400));
    public static final DeferredItem<Item> BRIQUETTE_COAL = ITEMS.register("briquette_coal", () -> new FuelItem(new Item.Properties(), 2400));
    public static final DeferredItem<Item> BRIQUETTE_LIGNITE = ITEMS.register("briquette_lignite", () -> new FuelItem(new Item.Properties(), 1800));
    public static final DeferredItem<Item> BRIQUETTE_WOOD = ITEMS.register("briquette_wood", () -> new FuelItem(new Item.Properties(), 1600));

    // Powders
    public static final DeferredItem<Item> POWDER_IRON = ITEMS.register("powder_iron", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_GOLD = ITEMS.register("powder_gold", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_DIAMOND = ITEMS.register("powder_diamond", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_EMERALD = ITEMS.register("powder_emerald", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_LAPIS = ITEMS.register("powder_lapis", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_TITANIUM = ITEMS.register("powder_titanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_TUNGSTEN = ITEMS.register("powder_tungsten", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_COPPER = ITEMS.register("powder_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_BERYLLIUM = ITEMS.register("powder_beryllium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_ALUMINIUM = ITEMS.register("powder_aluminium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_LEAD = ITEMS.register("powder_lead", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_STEEL = ITEMS.register("powder_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_COMBINE_STEEL = ITEMS.register("powder_combine_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_QUARTZ = ITEMS.register("powder_quartz", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_SCHRABIDIUM = ITEMS.register("powder_schrabidium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_ASBESTOS = ITEMS.register("powder_asbestos", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_DURA_STEEL = ITEMS.register("powder_dura_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_POLYMER = ITEMS.register("powder_polymer", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_BAKELITE = ITEMS.register("powder_bakelite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_DESH = ITEMS.register("powder_desh", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_LITHIUM = ITEMS.register("powder_lithium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_COBALT = ITEMS.register("powder_cobalt", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_DINEUTRONIUM = ITEMS.register("powder_dineutronium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_COAL = ITEMS.register("powder_coal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_ASH_WOOD = ITEMS.register("powder_ash_wood", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_ASH_COAL = ITEMS.register("powder_ash_coal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_ASH_MISC = ITEMS.register("powder_ash_misc", () -> new Item(new Item.Properties()));
    // Runde 10: die beiden Aschesorten, die im Original nicht aus dem Brennkasten, sondern
    // aus dem Schornstein stammen. Flugasche faellt in jedem Schornstein an, Feinruss nur
    // im Industrieschornstein (TileEntityChimneyIndustrial.cpaturesSoot).
    public static final DeferredItem<Item> POWDER_ASH_FLY = ITEMS.register("powder_ash_fly", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_ASH_SOOT = ITEMS.register("powder_ash_soot", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_BISMUTH = ITEMS.register("powder_bismuth", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_LIGNITE = ITEMS.register("powder_lignite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_ZIRCONIUM = ITEMS.register("powder_zirconium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_URANIUM = ITEMS.register("powder_uranium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_NIOBIUM = ITEMS.register("powder_niobium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_METEORITE = ITEMS.register("powder_meteorite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_METEORITE_TINY = ITEMS.register("powder_meteorite_tiny", () -> new Item(new Item.Properties()));

    //Crystals
    public static final DeferredItem<Item> CRYSTAL_IRON = ITEMS.register("crystal_iron", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_GOLD = ITEMS.register("crystal_gold", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_REDSTONE = ITEMS.register("crystal_redstone", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_LAPIS = ITEMS.register("crystal_lapis", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_DIAMOND = ITEMS.register("crystal_diamond", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_URANIUM = ITEMS.register("crystal_uranium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_THORIUM = ITEMS.register("crystal_thorium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_PLUTONIUM = ITEMS.register("crystal_plutonium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_TITANIUM = ITEMS.register("crystal_titanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_SULFUR = ITEMS.register("crystal_sulfur", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_NITER = ITEMS.register("crystal_niter", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_COPPER = ITEMS.register("crystal_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_TUNGSTEN = ITEMS.register("crystal_tungsten", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_ALUMINIUM = ITEMS.register("crystal_aluminium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_FLUORITE = ITEMS.register("crystal_fluorite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_BERYLLIUM = ITEMS.register("crystal_beryllium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_LEAD = ITEMS.register("crystal_lead", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_ASBESTOS = ITEMS.register("crystal_asbestos", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_SCHRARANIUM = ITEMS.register("crystal_schraranium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_SCHRABIDIUM = ITEMS.register("crystal_schrabidium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_RARE = ITEMS.register("crystal_rare", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_PHOSPHORUS = ITEMS.register("crystal_phosphorus", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_LITHIUM = ITEMS.register("crystal_lithium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_CINNABAR = ITEMS.register("crystal_cinnabar", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_COBALT = ITEMS.register("crystal_cobalt", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_STARMETAL = ITEMS.register("crystal_starmetal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_TRIXITE = ITEMS.register("crystal_trixite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_OSMIRIDIUM = ITEMS.register("crystal_osmiridium", () -> new Item(new Item.Properties()));

    // Billets
    public static final DeferredItem<Item> BILLET_COBALT = ITEMS.register("billet_cobalt", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_TH232 = ITEMS.register("billet_th232", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_URANIUM = ITEMS.register("billet_uranium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_U233 = ITEMS.register("billet_u233", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_U235 = ITEMS.register("billet_u235", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_U238 = ITEMS.register("billet_u238", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_UZH = ITEMS.register("billet_uzh", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_PLUTONIUM = ITEMS.register("billet_plutonium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_PU238 = ITEMS.register("billet_pu238", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_PU239 = ITEMS.register("billet_pu239", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_PU240 = ITEMS.register("billet_pu240", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_PU241 = ITEMS.register("billet_pu241", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_PU_MIX = ITEMS.register("billet_pu_mix", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_AM241 = ITEMS.register("billet_am241", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_AM242 = ITEMS.register("billet_am242", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_AM_MIX = ITEMS.register("billet_am_mix", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_NEPTUNIUM = ITEMS.register("billet_neptunium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_POLONIUM = ITEMS.register("billet_polonium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_TECHNETIUM = ITEMS.register("billet_technetium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_CO60 = ITEMS.register("billet_co60", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_SR90 = ITEMS.register("billet_sr90", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_AU198 = ITEMS.register("billet_au198", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_PB209 = ITEMS.register("billet_pb209", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_RA226 = ITEMS.register("billet_ra226", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_ACTINIUM = ITEMS.register("billet_actinium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_GH336 = ITEMS.register("billet_gh336", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_BERYLLIUM = ITEMS.register("billet_beryllium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_BISMUTH = ITEMS.register("billet_bismuth", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_ZIRCONIUM = ITEMS.register("billet_zirconium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_ZFB_BISMUTH = ITEMS.register("billet_zfb_bismuth", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_ZFB_PU241 = ITEMS.register("billet_zfb_pu241", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_ZFB_AM_MIX = ITEMS.register("billet_zfb_am_mix", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_SCHRABIDIUM = ITEMS.register("billet_schrabidium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_SOLINIUM = ITEMS.register("billet_solinium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_THORIUM_FUEL = ITEMS.register("billet_thorium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_URANIUM_FUEL = ITEMS.register("billet_uranium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_MOX_FUEL = ITEMS.register("billet_mox_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_PLUTONIUM_FUEL = ITEMS.register("billet_plutonium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_NEPTUNIUM_FUEL = ITEMS.register("billet_neptunium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_AMERICIUM_FUEL = ITEMS.register("billet_americium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_LES = ITEMS.register("billet_les", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_SCHRABIDIUM_FUEL = ITEMS.register("billet_schrabidium_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_HES = ITEMS.register("billet_hes", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_PO210BE = ITEMS.register("billet_po210be", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_RA226BE = ITEMS.register("billet_ra226be", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_PU238BE = ITEMS.register("billet_pu238be", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_AUSTRALIUM = ITEMS.register("billet_australium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_AUSTRALIUM_LESSER = ITEMS.register("billet_australium_lesser", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_AUSTRALIUM_GREATER = ITEMS.register("billet_australium_greater", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_UNOBTAINIUM = ITEMS.register("billet_unobtainium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_YHARONITE = ITEMS.register("billet_yharonite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_BALEFIRE_GOLD = ITEMS.register("billet_balefire_gold", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_FLASHLEAD = ITEMS.register("billet_flashlead", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BILLET_NUCLEAR_WASTE = ITEMS.register("billet_nuclear_waste", () -> new Item(new Item.Properties()));

    // Integridients & parts
    public static final DeferredItem<Item> PLATE_IRON = ITEMS.register("plate_iron", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_GOLD = ITEMS.register("plate_gold", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_TITANIUM = ITEMS.register("plate_titanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_ALUMINIUM = ITEMS.register("plate_aluminium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_STEEL = ITEMS.register("plate_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_LEAD = ITEMS.register("plate_lead", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_COPPER = ITEMS.register("plate_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_GUNMETAL = ITEMS.register("plate_gunmetal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_WEAPON_STEEL = ITEMS.register("plate_weapon_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_SATURNITE = ITEMS.register("plate_saturnite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_DURA_STEEL = ITEMS.register("plate_dura_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_SCHRABIDIUM = ITEMS.register("plate_schrabidium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_COMBINE_STEEL = ITEMS.register("plate_combine_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_BISMUTH = ITEMS.register("plate_bismuth", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_GOLD = ITEMS.register("wire_gold", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_COPPER = ITEMS.register("wire_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_ALUMINIUM = ITEMS.register("wire_aluminium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_ZIRCONIUM = ITEMS.register("wire_zirconium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_LEAD = ITEMS.register("wire_lead", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_TUNGSTEN = ITEMS.register("wire_tungsten", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_SCHRABIDIUM = ITEMS.register("wire_schrabidium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_STEEL = ITEMS.register("wire_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_MAGNETIZED_TUNGSTEN = ITEMS.register("wire_magnetized_tungsten", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_CARBON = ITEMS.register("wire_carbon", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_RED_COPPER = ITEMS.register("wire_red_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHELL_TITANIUM = ITEMS.register("shell_titanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHELL_ALUMINIUM = ITEMS.register("shell_aluminium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHELL_COPPER = ITEMS.register("shell_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHELL_STEEL = ITEMS.register("shell_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHELL_WEAPON_STEEL = ITEMS.register("shell_weapon_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHELL_SATURNITE = ITEMS.register("shell_saturnite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PIPE_IRON = ITEMS.register("pipe_iron", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PIPE_COPPER = ITEMS.register("pipe_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PIPE_ALUMINIUM = ITEMS.register("pipe_aluminium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PIPE_LEAD = ITEMS.register("pipe_lead", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PIPE_STEEL = ITEMS.register("pipe_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PIPE_DURA_STEEL = ITEMS.register("pipe_dura_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PIPE_RUBBER = ITEMS.register("pipe_rubber", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WIRE_DENSE = ITEMS.register("wire_dense", () -> new WireDenseItem(new Item.Properties()));
    public static final DeferredItem<Item> BOLT = ITEMS.register("bolt", () -> new BoltItem(new Item.Properties()));

    public static final DeferredItem<Item> CIRCUIT_PRINTED_BOARD = ITEMS.register("circuit_printed_board", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_ANALOG_BOARD = ITEMS.register("circuit_analog_board", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_INTEGRATED_BOARD = ITEMS.register("circuit_integrated_board", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_MILITARY_GRADE_BOARD = ITEMS.register("circuit_military_grade_board", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_VERSATILE_INTEGRATED = ITEMS.register("circuit_versatile_integrated", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_VERSATILE_BOARD = ITEMS.register("circuit_versatile_board", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_CAPACITOR = ITEMS.register("circuit_capacitor", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_TANTALIUM_CAPACITOR = ITEMS.register("circuit_tantalium_capacitor", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_CAPACITOR_BOARD = ITEMS.register("circuit_capacitor_board", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_VACUUM_TUBE = ITEMS.register("circuit_vacuum_tube", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_NUMITRON = ITEMS.register("circuit_numitron", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_PRINTED_SILICON_WAFER = ITEMS.register("circuit_printed_silicon_wafer", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_MICROCHIP = ITEMS.register("circuit_microchip", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_CONTROL_UNIT_CASING = ITEMS.register("circuit_control_unit_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_CONTROL_UNIT = ITEMS.register("circuit_control_unit", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_ADVANCED_CONTROL_UNIT = ITEMS.register("circuit_advanced_control_unit", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_SOLID_STATE_QUANTUM_PROCESSOR = ITEMS.register("circuit_solid_state_quantum_processor", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_QUANTUM_PROCESSING_UNIT = ITEMS.register("circuit_quantum_processing_unit", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_QUANTUM_COMPUTER = ITEMS.register("circuit_quantum_computer", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CIRCUIT_ATOMIC_CLOCK = ITEMS.register("circuit_atomic_clock", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COIL_COPPER = ITEMS.register("coil_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COIL_COPPER_RING = ITEMS.register("coil_copper_ring", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COIL_GOLD = ITEMS.register("coil_gold", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COIL_GOLD_RING = ITEMS.register("coil_gold_ring", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COIL_MAGNETIZED_TUNGSTEN = ITEMS.register("coil_magnetized_tungsten", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COIL_TUNGSTEN = ITEMS.register("coil_tungsten", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> MOTOR = ITEMS.register("motor", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> MOTOR_DESH = ITEMS.register("motor_desh", () -> new Item(new Item.Properties()));
    /* Zwei Maschinenbauteile, die bisher fehlten: das Zentrifugenelement (Aufsatz und
     * Gaszentrifuge) und der Selenkolben (Kettensaege, Bohrer, Motoraufsaetze). Beide sind
     * im Original schlichte Items ohne eigene Klasse. */
    public static final DeferredItem<Item> CENTRIFUGE_ELEMENT = ITEMS.register("centrifuge_element", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PISTON_SELENIUM = ITEMS.register("piston_selenium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CAST_PLATE_WELDED = ITEMS.register("cast_plate_welded", () -> new CastPlateItem(new Item.Properties(), true));
    public static final DeferredItem<Item> CAST_PLATE = ITEMS.register("cast_plate", () -> new CastPlateItem(new Item.Properties(), false));
    public static final DeferredItem<Item> PIPES_STEEL = ITEMS.register("pipes_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRT_DISPLAY = ITEMS.register("crt_display", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RING_STARMETAL = ITEMS.register("ring_starmetal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TANK_STEEL = ITEMS.register("tank_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CATALYST_CLAY = ITEMS.register("catalyst_clay", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CATALYTIC_CONVERTER = ITEMS.register("catalytic_converter", () -> new Item(new Item.Properties().stacksTo(1)));

    /* ---- Runde 142: die Erzeugnisse der Erdoelkette, die noch fehlten ---------------- */

    /* Der Brennstoffwuerfel aus Balefire -- der energiedichteste der Kette. */
    public static final DeferredItem<Item> SOLID_FUEL_BF = ITEMS.register("solid_fuel_bf", () -> new Item(new Item.Properties()));
    /* Gepresste Biomasse und der Riegel, den der Verfestiger aus Nahrfluessigkeit macht. */
    public static final DeferredItem<Item> BIOMASS_COMPRESSED = ITEMS.register("biomass_compressed", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BIO_WAFER = ITEMS.register("bio_wafer", () -> new Item(new Item.Properties().food(NtmFoods.BIO_WAFER)));
    public static final DeferredItem<Item> DEUTERIUM_FILTER = ITEMS.register("deuterium_filter", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FINS_FLAT = ITEMS.register("fins_flat", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FINS_SMALL_STEEL = ITEMS.register("fins_small_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FINS_BIG_STEEL = ITEMS.register("fins_big_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FINS_TRI_STEEL = ITEMS.register("fins_tri_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FINS_QUAD_TITANIUM = ITEMS.register("fins_quad_titanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SPHERE_STEEL = ITEMS.register("sphere_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PEDESTAL_STEEL = ITEMS.register("pedestal_steel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BLADE_TITANIUM = ITEMS.register("blade_titanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BLADE_TUNGSTEN = ITEMS.register("blade_tungsten", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TURBINE_TITANIUM = ITEMS.register("turbine_titanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TURBINE_TUNGSTEN = ITEMS.register("turbine_tungsten", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLYWHEEL_BERYLLIUM = ITEMS.register("flywheel_beryllium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TOOTHPICKS = ITEMS.register("toothpicks", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DUCTTAPE = ITEMS.register("ducttape", () -> new Item(new Item.Properties()));
    // Runde 10: Aktivkohlefilter, im Original ein schlichtes Item. Wird fuer den
    // Industrieschornstein gebraucht.
    public static final DeferredItem<Item> FILTER_COAL = ITEMS.register("filter_coal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLANT_ITEM = ITEMS.register("plant_item", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PART_GENERIC = ITEMS.register("part_generic", () -> new PartGenericItem(new Item.Properties()));

    /* Die fuenf Geschosse des Zyklotrons. Sie sind keine Bauteile, sondern Munition: das
     * Zyklotron schiesst sie auf ein Pulver, und was dabei herauskommt, haengt an beidem.
     * Gebaut werden sie in der Montagemaschine, je acht aus einem Pulver. */
    public static final DeferredItem<Item> PART_LITHIUM = ITEMS.register("part_lithium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PART_BERYLLIUM = ITEMS.register("part_beryllium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PART_CARBON = ITEMS.register("part_carbon", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PART_COPPER = ITEMS.register("part_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PART_PLUTONIUM = ITEMS.register("part_plutonium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BIOMASS = ITEMS.register("biomass", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PELLET_CHARGED = ITEMS.register("pellet_charged", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GLYPHID_MEAT = ITEMS.register("glyphid_meat", () -> new Item(new Item.Properties().food(NtmFoods.GLYPHID_MEAT)));
    public static final DeferredItem<Item> SOLID_FUEL = ITEMS.register("solid_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CANISTER_EMPTY = ITEMS.register("canister_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CANISTER_NAPALM = ITEMS.register("canister_napalm", () -> new Item(new Item.Properties()));
    /* Der gefuellte Kanister. Wie das Fass ein Gegenstand je Fluessigkeit, nur kleiner: tausend
     * Millibar statt sechzehntausend. Er gilt aber NICHT fuer jede Fluessigkeit, sondern nur
     * fuer die mit einem CD_Canister -- das ist im Original dieselbe Bedingung, und sie
     * entscheidet zugleich ueber die Farbe des Aufdrucks. */
    public static final DeferredItem<Item> CANISTER_FULL = ITEMS.register("canister_full", () -> new FluidTankItem(new Item.Properties()));
    public static final DeferredItem<Item> FUEL_ADDITIVE = ITEMS.register("fuel_additive", () -> new FuelAdditiveItem(new Item.Properties()));
    public static final DeferredItem<Item> ROCKET_FUEL = ITEMS.register("rocket_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CORDITE = ITEMS.register("cordite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CHOCOLATE = ITEMS.register("chocolate", () -> new Item(new Item.Properties().food(NtmFoods.CHOCOLATE)));
    public static final DeferredItem<Item> BALL_DYNAMITE = ITEMS.register("ball_dynamite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BALL_TNT = ITEMS.register("ball_tnt", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BALL_TATB = ITEMS.register("ball_tatb", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PELLET_CLUSTER = ITEMS.register("pellet_cluster", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> MAGNETRON = ITEMS.register("magnetron", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PHOTO_PANEL = ITEMS.register("photo_panel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> MISSILE_ASSEMBLY = ITEMS.register("missile_assembly", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> THRUSTER_SMALL = ITEMS.register("thruster_small", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> THRUSTER_MEDIUM = ITEMS.register("thruster_medium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> THRUSTER_LARGE = ITEMS.register("thruster_large", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FUEL_TANK_SMALL = ITEMS.register("fuel_tank_small", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FUEL_TANK_MEDIUM = ITEMS.register("fuel_tank_medium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FUEL_TANK_LARGE = ITEMS.register("fuel_tank_large", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_GENERIC_SMALL = ITEMS.register("warhead_generic_small", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_GENERIC_MEDIUM = ITEMS.register("warhead_generic_medium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_GENERIC_LARGE = ITEMS.register("warhead_generic_large", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_INCENDIARY_SMALL = ITEMS.register("warhead_incendiary_small", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_INCENDIARY_MEDIUM = ITEMS.register("warhead_incendiary_medium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_INCENDIARY_LARGE = ITEMS.register("warhead_incendiary_large", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_CLUSTER_SMALL = ITEMS.register("warhead_cluster_small", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_CLUSTER_MEDIUM = ITEMS.register("warhead_cluster_medium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_CLUSTER_LARGE = ITEMS.register("warhead_cluster_large", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_BUSTER_SMALL = ITEMS.register("warhead_buster_small", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_BUSTER_MEDIUM = ITEMS.register("warhead_buster_medium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_BUSTER_LARGE = ITEMS.register("warhead_buster_large", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_NUCLEAR = ITEMS.register("warhead_nuclear", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_MIRV = ITEMS.register("warhead_mirv", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WARHEAD_VOLCANO = ITEMS.register("warhead_volcano", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NEUTRON_REFLECTOR = ITEMS.register("neutron_reflector", () -> new Item(new Item.Properties()));
    // Haltbarkeiten wie im Original, gezaehlt in Schmelzgaengen
    public static final DeferredItem<Item> ARC_ELECTRODE_GRAPHITE = ITEMS.register("arc_electrode_graphite", () -> new ArcElectrodeItem(10, new Item.Properties()));
    public static final DeferredItem<Item> ARC_ELECTRODE_LANTHANIUM = ITEMS.register("arc_electrode_lanthanium", () -> new ArcElectrodeItem(100, new Item.Properties()));
    public static final DeferredItem<Item> ARC_ELECTRODE_DESH = ITEMS.register("arc_electrode_desh", () -> new ArcElectrodeItem(500, new Item.Properties()));
    public static final DeferredItem<Item> ARC_ELECTRODE_SATURNITE = ITEMS.register("arc_electrode_saturnite", () -> new ArcElectrodeItem(1500, new Item.Properties()));
    public static final DeferredItem<Item> ARC_ELECTRODE_GRAPHITE_BURNT = ITEMS.register("arc_electrode_graphite_burnt", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ARC_ELECTRODE_LANTHANIUM_BURNT = ITEMS.register("arc_electrode_lanthanium_burnt", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ARC_ELECTRODE_DESH_BURNT = ITEMS.register("arc_electrode_desh_burnt", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ARC_ELECTRODE_SATURNITE_BURNT = ITEMS.register("arc_electrode_saturnite_burnt", () -> new Item(new Item.Properties()));

    /** Materialklumpen aus dem Tiegel -- Material in der Meta-Komponente, Menge in Quanten */
    public static final DeferredItem<Item> SCRAPS = ITEMS.register("scraps", () -> new ScrapsItem(new Item.Properties()));

    /** Giessform -- welche Form es ist, steht in der Meta-Komponente */
    public static final DeferredItem<Item> MOLD = ITEMS.register("mold", () -> new MoldItem(new Item.Properties()));

    //resources
    public static final DeferredItem<Item> SULFUR = ITEMS.register("sulfur", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CINNABAR = ITEMS.register("cinnabar", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLUORITE = ITEMS.register("fluorite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LIGNITE = ITEMS.register("lignite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NITER = ITEMS.register("niter", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RARE_EARTH_ORE_CHUNK = ITEMS.register("rare_earth_ore_chunk", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CHUNK_CRYOLITE = ITEMS.register("chunk_cryolite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CHUNK_MALACHITE = ITEMS.register("chunk_malachite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> OIL_TAR_CRUDE = ITEMS.register("oil_tar_crude", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> OIL_TAR_CRACK = ITEMS.register("oil_tar_crack", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> OIL_TAR_COAL = ITEMS.register("oil_tar_coal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> OIL_TAR_PARAFFIN = ITEMS.register("oil_tar_paraffin", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> OIL_TAR_WOOD = ITEMS.register("oil_tar_wood", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> OIL_TAR_WAX = ITEMS.register("oil_tar_wax", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DUST = ITEMS.register("dust", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUCLEAR_WASTE = ITEMS.register("nuclear_waste", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUCLEAR_WASTE_TINY = ITEMS.register("nuclear_waste_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUCLEAR_WASTE_VITRIFIED = ITEMS.register("nuclear_waste_vitrified", () -> new Item(new Item.Properties()));

    /* Runde 135: kurz- und langlebiger Abfall samt ihren abgeklungenen Fassungen. Die
     * Abfallklasse steckt als Metawert im Gegenstand, nicht in eigenen Registriernamen -- so
     * haelt es auch das Original. */
    public static final DeferredItem<Item> NUCLEAR_WASTE_SHORT = ITEMS.register("nuclear_waste_short", () -> new NuclearWasteItem(new Item.Properties(), WasteClass.SHORT));
    public static final DeferredItem<Item> NUCLEAR_WASTE_SHORT_TINY = ITEMS.register("nuclear_waste_short_tiny", () -> new NuclearWasteItem(new Item.Properties(), WasteClass.SHORT));
    public static final DeferredItem<Item> NUCLEAR_WASTE_SHORT_DEPLETED = ITEMS.register("nuclear_waste_short_depleted", () -> new NuclearWasteItem(new Item.Properties(), WasteClass.SHORT));
    public static final DeferredItem<Item> NUCLEAR_WASTE_SHORT_DEPLETED_TINY = ITEMS.register("nuclear_waste_short_depleted_tiny", () -> new NuclearWasteItem(new Item.Properties(), WasteClass.SHORT));

    public static final DeferredItem<Item> NUCLEAR_WASTE_LONG = ITEMS.register("nuclear_waste_long", () -> new NuclearWasteItem(new Item.Properties(), WasteClass.LONG));
    public static final DeferredItem<Item> NUCLEAR_WASTE_LONG_TINY = ITEMS.register("nuclear_waste_long_tiny", () -> new NuclearWasteItem(new Item.Properties(), WasteClass.LONG));
    public static final DeferredItem<Item> NUCLEAR_WASTE_LONG_DEPLETED = ITEMS.register("nuclear_waste_long_depleted", () -> new NuclearWasteItem(new Item.Properties(), WasteClass.LONG));
    public static final DeferredItem<Item> NUCLEAR_WASTE_LONG_DEPLETED_TINY = ITEMS.register("nuclear_waste_long_depleted_tiny", () -> new NuclearWasteItem(new Item.Properties(), WasteClass.LONG));

    /* Radioaktiver Schrott -- der Bodensatz des Radiothermalgenerators, und der einzige seiner
     * Brennstoffe, der im Port schon eine Quelle hat: der Schredder macht ihn aus Truemmern. */
    public static final DeferredItem<Item> SCRAP_NUCLEAR = ITEMS.register("scrap_nuclear", () -> new Item(new Item.Properties()));

    /* Der Reaktorkern -- ein Bauteil, das der Port bisher nicht brauchte. Der
     * Radiothermalgenerator ist sein erster Abnehmer. */
    public static final DeferredItem<Item> REACTOR_CORE = ITEMS.register("reactor_core", () -> new Item(new Item.Properties()));

    // Pellets
    public static final DeferredItem<Item> PELLET_RTG = ITEMS.register("pellet_rtg", () -> new RTGPelletItem(new Item.Properties()));
    /* Abgebrannter Brennstoff. Faellt heiss an und kuehlt im Brennstoffbecken ab. */
    public static final DeferredItem<Item> WASTE_NATURAL_URANIUM = ITEMS.register("waste_natural_uranium", () -> new DepletedFuelItem("waste_uranium", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_URANIUM = ITEMS.register("waste_uranium", () -> new DepletedFuelItem("waste_uranium", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_THORIUM = ITEMS.register("waste_thorium", () -> new DepletedFuelItem("waste_thorium", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_MOX = ITEMS.register("waste_mox", () -> new DepletedFuelItem("waste_mox", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_PLUTONIUM = ITEMS.register("waste_plutonium", () -> new DepletedFuelItem("waste_plutonium", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_U233 = ITEMS.register("waste_u233", () -> new DepletedFuelItem("waste_uranium", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_U235 = ITEMS.register("waste_u235", () -> new DepletedFuelItem("waste_uranium", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_SCHRABIDIUM = ITEMS.register("waste_schrabidium", () -> new DepletedFuelItem("waste_schrabidium", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_ZFB_MOX = ITEMS.register("waste_zfb_mox", () -> new DepletedFuelItem("waste_zfb_mox", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_PLATE_U233 = ITEMS.register("waste_plate_u233", () -> new DepletedFuelItem("waste_plate_uranium", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_PLATE_U235 = ITEMS.register("waste_plate_u235", () -> new DepletedFuelItem("waste_plate_uranium", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_PLATE_MOX = ITEMS.register("waste_plate_mox", () -> new DepletedFuelItem("waste_plate_mox", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_PLATE_PU239 = ITEMS.register("waste_plate_pu239", () -> new DepletedFuelItem("waste_plate_mox", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_PLATE_SA326 = ITEMS.register("waste_plate_sa326", () -> new DepletedFuelItem("waste_plate_sa326", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_PLATE_RA226BE = ITEMS.register("waste_plate_ra226be", () -> new DepletedFuelItem("waste_plate_ra226be", new Item.Properties()));
    public static final DeferredItem<Item> WASTE_PLATE_PU238BE = ITEMS.register("waste_plate_pu238be", () -> new DepletedFuelItem("waste_plate_pu238be", new Item.Properties()));
    public static final DeferredItem<Item> PELLET_RTG_DEPLETED = ITEMS.register("pellet_rtg_depleted", () -> new RTGPelletDepletedItem(new Item.Properties()));
    // Beide sind im Original schlichte new Item(); ohne sie haette der RTG kein Rezept.
    public static final DeferredItem<Item> THERMO_ELEMENT = ITEMS.register("thermo_element", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RTG_UNIT = ITEMS.register("rtg_unit", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SAWBLADE = ITEMS.register("sawblade", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GEAR_LARGE = ITEMS.register("gear_large", () -> new GearItem(new Item.Properties()));

    // Cells
    public static final DeferredItem<Item> CELL_EMPTY = ITEMS.register("cell_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CELL_UF6 = ITEMS.register("cell_uf6", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CELL_PUF6 = ITEMS.register("cell_puf6", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CELL_ANTIMATTER = ITEMS.register("cell_antimatter", () -> new DangerousDropItem(new Item.Properties()));
    public static final DeferredItem<Item> CELL_DEUTERIUM = ITEMS.register("cell_deuterium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CELL_TRITIUM = ITEMS.register("cell_tritium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CELL_SAS3 = ITEMS.register("cell_sas3", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> CELL_ANTI_SCHARBIDIUM = ITEMS.register("cell_anti_schrabidium", () -> new DangerousDropItem(new Item.Properties()));
    public static final DeferredItem<Item> CELL_BALEFIRE = ITEMS.register("cell_balefire", () -> new Item(new Item.Properties()));

    // Particle Containers
    /* Die Teilchenkapseln. Ihr Erzeuger, der Teilchenbeschleuniger, fehlt im Port noch; das
     * Myon steht hier, weil die ICF-Presse es verbraucht. */
    public static final DeferredItem<Item> PARTICLE_EMPTY = ITEMS.register("particle_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PARTICLE_MUON = ITEMS.register("particle_muon", () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final DeferredItem<Item> PARTICLE_DIGAMMA = ITEMS.register("particle_digamma", () -> new DangerousDropItem(new Item.Properties()));
    public static final DeferredItem<Item> PARTICLE_LUTECE = ITEMS.register("particle_lutece", () -> new Item(new Item.Properties()));

    /*
     * Runde 133, der Teilchenbeschleuniger. Die zehn Teilchen, die dem Port bisher fehlten --
     * PARTICLE_EMPTY, MUON, DIGAMMA und LUTECE standen schon da.
     *
     * JEDES TEILCHEN HINTERLAESST EINE LEERE HUELSE: der Beschleuniger gibt sie beim Verbrauch
     * zurueck, deshalb traegt jedes craftRemainder(PARTICLE_EMPTY). Das Original macht es ueber
     * setContainerItem, was auf dasselbe hinauslaeuft.
     */
    public static final DeferredItem<Item> PARTICLE_HYDROGEN = ITEMS.register("particle_hydrogen", () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final DeferredItem<Item> PARTICLE_COPPER = ITEMS.register("particle_copper", () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final DeferredItem<Item> PARTICLE_LEAD = ITEMS.register("particle_lead", () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final DeferredItem<Item> PARTICLE_AMAT = ITEMS.register("particle_amat", () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final DeferredItem<Item> PARTICLE_ASCHRAB = ITEMS.register("particle_aschrab", () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final DeferredItem<Item> PARTICLE_HIGGS = ITEMS.register("particle_higgs", () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final DeferredItem<Item> PARTICLE_TACHYON = ITEMS.register("particle_tachyon", () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final DeferredItem<Item> PARTICLE_STRANGE = ITEMS.register("particle_strange", () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final DeferredItem<Item> PARTICLE_DARK = ITEMS.register("particle_dark", () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final DeferredItem<Item> PARTICLE_SPARKTICLE = ITEMS.register("particle_sparkticle", () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));

    /* Die Spulen der Magnete. Vier Stufen, und jede verschiebt das Fenster, in dem der Ring
     * arbeitet -- von Gold bis Chlorophyt liegt der ganze Ausbau der Anlage. */
    public static final DeferredItem<Item> PA_COIL = ITEMS.register("pa_coil", () -> new PACoilItem(new Item.Properties()));

    // Singularities, black holes and other cosmic horrors
    public static final DeferredItem<Item> SINGULARITY = ITEMS.register("singularity", () -> new DangerousDropItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SINGULARITY_COUNTER_RESONANT = ITEMS.register("singularity_counter_resonant", () -> new DangerousDropItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SINGULARITY_SUPER_HEATED = ITEMS.register("singularity_super_heated", () -> new DangerousDropItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> BLACK_HOLE = ITEMS.register("black_hole", () -> new DangerousDropItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SINGULARITY_SPARK = ITEMS.register("singularity_spark", () -> new DangerousDropItem(new Item.Properties().stacksTo(1)));
    // todo crystal_xen
    public static final DeferredItem<Item> PELLET_ANTIMATTER = ITEMS.register("pellet_antimatter", () -> new DangerousDropItem(new Item.Properties()));

    // Infinite Tanks
    public static final DeferredItem<Item> INF_WATER = ITEMS.register("inf_water", () -> new InfiniteFluidItem(new Item.Properties().stacksTo(1), Fluids.WATER, 50));
    public static final DeferredItem<Item> INF_WATER_MK2 = ITEMS.register("inf_water_mk2", () -> new InfiniteFluidItem(new Item.Properties().stacksTo(1), Fluids.WATER, 500));

    // Universal Tank
    public static final DeferredItem<Item> FLUID_TANK_EMPTY = ITEMS.register("fluid_tank_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLUID_TANK_FULL = ITEMS.register("fluid_tank_full", () -> new FluidTankItem(new Item.Properties()));
    public static final DeferredItem<Item> FLUID_TANK_LEAD_EMPTY = ITEMS.register("fluid_tank_lead_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLUID_TANK_LEAD_FULL = ITEMS.register("fluid_tank_lead_full", () -> new FluidTankItem(new Item.Properties()));
    public static final DeferredItem<Item> FLUID_BARREL_EMPTY = ITEMS.register("fluid_barrel_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLUID_BARREL_FULL = ITEMS.register("fluid_barrel_full", () -> new FluidTankItem(new Item.Properties()));
    public static final DeferredItem<Item> FLUID_BARREL_INFINITE = ITEMS.register("fluid_barrel_infinite", () -> new InfiniteFluidItem(new Item.Properties().stacksTo(1), null, 1_000_000_000));

    // Packaged fluids
    public static final DeferredItem<Item> FLUID_PACK_EMPTY = ITEMS.register("fluid_pack_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLUID_PACK_FULL = ITEMS.register("fluid_pack_full", () -> new FluidTankItem(new Item.Properties()));

    // Batteries
    public static final DeferredItem<Item> BATTERY_SPARK = ITEMS.register("battery_spark", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> BATTERY_TRIXITE = ITEMS.register("battery_trixite", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BATTERY_PACK = ITEMS.register("battery_pack", () -> new BatteryPackItem(new Item.Properties()));
    public static final DeferredItem<Item> BATTERY_SC = ITEMS.register("battery_sc", () -> new BatterySCItem(new Item.Properties()));
    public static final DeferredItem<Item> BATTERY_CREATIVE = ITEMS.register("battery_creative", () -> new BatteryCreativeItem(new Item.Properties().stacksTo(1)));

    // Folders
    public static final DeferredItem<Item> BLUEPRINTS = ITEMS.register("blueprints", () -> new BlueprintsItem(new Item.Properties()));

    /* Runde 131, das Grundgesteinserz: die Rohprobe aus dem Bagger, das aufbereitete Erz und
     * die Bruchstuecke, die am Ende herausfallen. Alle drei sind Meta-Gegenstaende. */
    public static final DeferredItem<Item> BEDROCK_ORE_BASE = ITEMS.register("bedrock_ore_base", () -> new BedrockOreBaseItem(new Item.Properties()));
    public static final DeferredItem<Item> BEDROCK_ORE = ITEMS.register("bedrock_ore", () -> new BedrockOreItem(new Item.Properties()));
    public static final DeferredItem<Item> BEDROCK_ORE_FRAGMENT = ITEMS.register("bedrock_ore_fragment", () -> new BedrockOreFragmentItem(new Item.Properties()));

    /*
     * DIE SIEBEN WAFFENBAUTEILE. Lauf, Verschluss, Mechanik, Schaft, Griff -- alles, woraus
     * das Original seine Waffen zusammensetzt. Jedes ist EIN Gegenstand mit allen Materialien
     * in den Metadaten; welche das sind, steht als setAutogen(...) am Material selbst.
     *
     * Sie kommen aus der Giessform, nicht von der Werkbank: MoldItem.registerMolds haengt
     * ihnen die Formnummern 22 bis 28 des Originals an.
     */
    public static final DeferredItem<Item> PART_BARREL_LIGHT = ITEMS.register("part_barrel_light", () -> new GunPartItem(new Item.Properties(), MaterialShapes.LIGHTBARREL));
    public static final DeferredItem<Item> PART_BARREL_HEAVY = ITEMS.register("part_barrel_heavy", () -> new GunPartItem(new Item.Properties(), MaterialShapes.HEAVYBARREL));
    public static final DeferredItem<Item> PART_RECEIVER_LIGHT = ITEMS.register("part_receiver_light", () -> new GunPartItem(new Item.Properties(), MaterialShapes.LIGHTRECEIVER));
    public static final DeferredItem<Item> PART_RECEIVER_HEAVY = ITEMS.register("part_receiver_heavy", () -> new GunPartItem(new Item.Properties(), MaterialShapes.HEAVYRECEIVER));
    public static final DeferredItem<Item> PART_MECHANISM = ITEMS.register("part_mechanism", () -> new GunPartItem(new Item.Properties(), MaterialShapes.MECHANISM));
    public static final DeferredItem<Item> PART_STOCK = ITEMS.register("part_stock", () -> new GunPartItem(new Item.Properties(), MaterialShapes.STOCK));
    public static final DeferredItem<Item> PART_GRIP = ITEMS.register("part_grip", () -> new GunPartItem(new Item.Properties(), MaterialShapes.GRIP));
    public static final DeferredItem<Item> DRILLBIT = ITEMS.register("drillbit", () -> new DrillbitItem(new Item.Properties().stacksTo(1)));

    /** Runde 132: die fertig montierte Eigenbau-Rakete. Ihre Teile stehen in den Zusatzdaten. */
    public static final DeferredItem<Item> MISSILE_CUSTOM = ITEMS.register("missile_custom", () -> new CustomMissileItem(new Item.Properties()));

    /*
     * Runde 132, der Baukasten der Eigenbau-Raketen. HUNDERTZWEIUNDZWANZIG Bauteile -- das
     * Original fuehrt jedes als eigenen Gegenstand, weil die Kennwerte am Gegenstand haengen und
     * nicht am Zahlenwert des Stapels.
     *
     * Einundsechzig davon sind ABSCHRIFTEN: Tarnanstriche, Flammen, Blech. Sie uebernehmen die
     * Kennwerte des Grundteils und aendern hoechstens die Haltbarkeit. Weil copyFrom das
     * Grundteil zur Bauzeit braucht, MUSS es in dieser Liste davorstehen -- die
     * Registrierreihenfolge ist die Reihenfolge dieser Zeilen.
     */
    public static final DeferredItem<Item> MP_THRUSTER_10_KEROSENE = ITEMS.register("mp_thruster_10_kerosene", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.KEROSENE, 1F, 1.5F, PartSize.SIZE_10).setHealth(10F));
    public static final DeferredItem<Item> MP_THRUSTER_10_SOLID = ITEMS.register("mp_thruster_10_solid", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.SOLID, 1F, 1.5F, PartSize.SIZE_10).setHealth(15F));
    public static final DeferredItem<Item> MP_THRUSTER_10_XENON = ITEMS.register("mp_thruster_10_xenon", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.XENON, 1F, 1.5F, PartSize.SIZE_10).setHealth(5F));
    public static final DeferredItem<Item> MP_THRUSTER_15_KEROSENE = ITEMS.register("mp_thruster_15_kerosene", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.KEROSENE, 1F, 7.5F, PartSize.SIZE_15).setHealth(15F));
    public static final DeferredItem<Item> MP_THRUSTER_15_KEROSENE_DUAL = ITEMS.register("mp_thruster_15_kerosene_dual", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.KEROSENE, 1F, 2.5F, PartSize.SIZE_15).setHealth(15F));
    public static final DeferredItem<Item> MP_THRUSTER_15_KEROSENE_TRIPLE = ITEMS.register("mp_thruster_15_kerosene_triple", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.KEROSENE, 1F, 5F, PartSize.SIZE_15).setHealth(15F));
    public static final DeferredItem<Item> MP_THRUSTER_15_SOLID = ITEMS.register("mp_thruster_15_solid", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.SOLID, 1F, 5F, PartSize.SIZE_15).setHealth(20F));
    public static final DeferredItem<Item> MP_THRUSTER_15_SOLID_HEXDECUPLE = ITEMS.register("mp_thruster_15_solid_hexdecuple", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.SOLID, 1F, 5F, PartSize.SIZE_15).setHealth(25F).setRarity(CustomMissilePartItem.Rarity.UNCOMMON));
    public static final DeferredItem<Item> MP_THRUSTER_15_HYDROGEN = ITEMS.register("mp_thruster_15_hydrogen", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.HYDROGEN, 1F, 7.5F, PartSize.SIZE_15).setHealth(20F));
    public static final DeferredItem<Item> MP_THRUSTER_15_HYDROGEN_DUAL = ITEMS.register("mp_thruster_15_hydrogen_dual", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.HYDROGEN, 1F, 2.5F, PartSize.SIZE_15).setHealth(15F));
    public static final DeferredItem<Item> MP_THRUSTER_15_BALEFIRE_SHORT = ITEMS.register("mp_thruster_15_balefire_short", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.BALEFIRE, 1F, 5F, PartSize.SIZE_15).setHealth(25F));
    public static final DeferredItem<Item> MP_THRUSTER_15_BALEFIRE = ITEMS.register("mp_thruster_15_balefire", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.BALEFIRE, 1F, 5F, PartSize.SIZE_15).setHealth(25F));
    public static final DeferredItem<Item> MP_THRUSTER_15_BALEFIRE_LARGE = ITEMS.register("mp_thruster_15_balefire_large", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.BALEFIRE, 1F, 7.5F, PartSize.SIZE_15).setHealth(35F));
    public static final DeferredItem<Item> MP_THRUSTER_15_BALEFIRE_LARGE_RAD = ITEMS.register("mp_thruster_15_balefire_large_rad", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.BALEFIRE, 1F, 7.5F, PartSize.SIZE_15).setAuthor("The Master").setHealth(35F).setRarity(CustomMissilePartItem.Rarity.UNCOMMON));
    public static final DeferredItem<Item> MP_THRUSTER_20_KEROSENE = ITEMS.register("mp_thruster_20_kerosene", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20).setHealth(30F));
    public static final DeferredItem<Item> MP_THRUSTER_20_KEROSENE_DUAL = ITEMS.register("mp_thruster_20_kerosene_dual", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20).setHealth(30F));
    public static final DeferredItem<Item> MP_THRUSTER_20_KEROSENE_TRIPLE = ITEMS.register("mp_thruster_20_kerosene_triple", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20).setHealth(30F));
    public static final DeferredItem<Item> MP_THRUSTER_20_SOLID = ITEMS.register("mp_thruster_20_solid", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.SOLID, 1F, 100F, PartSize.SIZE_20).setHealth(35F).setWittyText("It's basically just a big hole at the end of the fuel tank."));
    public static final DeferredItem<Item> MP_THRUSTER_20_SOLID_MULTI = ITEMS.register("mp_thruster_20_solid_multi", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.SOLID, 1F, 100F, PartSize.SIZE_20).setHealth(35F));
    public static final DeferredItem<Item> MP_THRUSTER_20_SOLID_MULTIER = ITEMS.register("mp_thruster_20_solid_multier", () -> new CustomMissilePartItem(new Item.Properties()).makeThruster(FuelType.SOLID, 1F, 100F, PartSize.SIZE_20).setHealth(35F).setWittyText("Did I miscount? Hope not."));
    public static final DeferredItem<Item> MP_STABILITY_10_FLAT = ITEMS.register("mp_stability_10_flat", () -> new CustomMissilePartItem(new Item.Properties()).makeStability(0.5F, PartSize.SIZE_10).setHealth(10F));
    public static final DeferredItem<Item> MP_STABILITY_10_CRUISE = ITEMS.register("mp_stability_10_cruise", () -> new CustomMissilePartItem(new Item.Properties()).makeStability(0.25F, PartSize.SIZE_10).setHealth(5F));
    public static final DeferredItem<Item> MP_STABILITY_10_SPACE = ITEMS.register("mp_stability_10_space", () -> new CustomMissilePartItem(new Item.Properties()).makeStability(0.35F, PartSize.SIZE_10).setHealth(5F).setRarity(CustomMissilePartItem.Rarity.COMMON).setWittyText("Standing there alone, the ship is waiting / All systems are go, are you sure?"));
    public static final DeferredItem<Item> MP_STABILITY_15_FLAT = ITEMS.register("mp_stability_15_flat", () -> new CustomMissilePartItem(new Item.Properties()).makeStability(0.5F, PartSize.SIZE_15).setHealth(10F));
    public static final DeferredItem<Item> MP_STABILITY_15_THIN = ITEMS.register("mp_stability_15_thin", () -> new CustomMissilePartItem(new Item.Properties()).makeStability(0.35F, PartSize.SIZE_15).setHealth(5F));
    public static final DeferredItem<Item> MP_STABILITY_15_SOYUZ = ITEMS.register("mp_stability_15_soyuz", () -> new CustomMissilePartItem(new Item.Properties()).makeStability(0.25F, PartSize.SIZE_15).setHealth(15F).setRarity(CustomMissilePartItem.Rarity.COMMON).setWittyText("Союз!"));
    public static final DeferredItem<Item> MP_STABILITY_20_FLAT = ITEMS.register("mp_s_20", () -> new CustomMissilePartItem(new Item.Properties()).makeStability(0.5F, PartSize.SIZE_20));
    public static final DeferredItem<Item> MP_FUSELAGE_10_KEROSENE = ITEMS.register("mp_fuselage_10_kerosene", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.KEROSENE, 2500F, PartSize.SIZE_10, PartSize.SIZE_10).setAuthor("Hoboy").setHealth(20F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_KEROSENE_CAMO = ITEMS.register("mp_fuselage_10_kerosene_camo", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Camo"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_KEROSENE_DESERT = ITEMS.register("mp_fuselage_10_kerosene_desert", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Desert Camo"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_KEROSENE_SKY = ITEMS.register("mp_fuselage_10_kerosene_sky", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Sky Camo"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_KEROSENE_FLAMES = ITEMS.register("mp_fuselage_10_kerosene_flames", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setTitle("Sick Flames"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_KEROSENE_INSULATION = ITEMS.register("mp_fuselage_10_kerosene_insulation", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Orange Insulation").setHealth(25F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_KEROSENE_SLEEK = ITEMS.register("mp_fuselage_10_kerosene_sleek", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setTitle("IF-R&D").setHealth(35F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_KEROSENE_METAL = ITEMS.register("mp_fuselage_10_kerosene_metal", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setTitle("Bolted Metal").setHealth(30F).setAuthor("Hoboy"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_KEROSENE_TAINT = ITEMS.register("mp_fuselage_10_kerosene_taint", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_SOLID = ITEMS.register("mp_fuselage_10_solid", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.SOLID, 2500F, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(25F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_SOLID_FLAMES = ITEMS.register("mp_fuselage_10_solid_flames", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setTitle("Sick Flames"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_SOLID_INSULATION = ITEMS.register("mp_fuselage_10_solid_insulation", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Orange Insulation").setHealth(30F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_SOLID_SLEEK = ITEMS.register("mp_fuselage_10_solid_sleek", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setTitle("IF-R&D").setHealth(35F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_SOLID_SOVIET_GLORY = ITEMS.register("mp_fuselage_10_solid_soviet_glory", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.EPIC).setAuthor("Hoboy").setHealth(35F).setTitle("Soviet Glory"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_SOLID_CATHEDRAL = ITEMS.register("mp_fuselage_10_solid_cathedral", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setAuthor("Satan").setTitle("Unholy Cathedral").setWittyText("Quakeesque!"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_SOLID_MOONLIT = ITEMS.register("mp_fuselage_10_solid_moonlit", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("The Master & Hoboy").setTitle("Moonlit"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_SOLID_BATTERY = ITEMS.register("mp_fuselage_10_solid_battery", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("wolfmonster222").setHealth(30F).setTitle("Ecstatic").setWittyText("I got caught eating batteries again :("));
    public static final DeferredItem<Item> MP_FUSELAGE_10_SOLID_DURACELL = ITEMS.register("mp_fuselage_10_solid_duracell", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setAuthor("Hoboy").setTitle("Duracell").setHealth(30F).setWittyText("The crunchiest battery on the market!"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_XENON = ITEMS.register("mp_fuselage_10_xenon", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.XENON, 5000F, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(20F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_XENON_BHOLE = ITEMS.register("mp_fuselage_10_xenon_bhole", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_XENON.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setAuthor("Sten89").setTitle("Morceus-1457"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_KEROSENE = ITEMS.register("mp_fuselage_10_long_kerosene", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.KEROSENE, 5000F, PartSize.SIZE_10, PartSize.SIZE_10).setAuthor("Hoboy").setHealth(30F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_KEROSENE_CAMO = ITEMS.register("mp_fuselage_10_long_kerosene_camo", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Camo"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_KEROSENE_DESERT = ITEMS.register("mp_fuselage_10_long_kerosene_desert", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Desert Camo"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_KEROSENE_SKY = ITEMS.register("mp_fuselage_10_long_kerosene_sky", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Sky Camo"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_KEROSENE_FLAMES = ITEMS.register("mp_fuselage_10_long_kerosene_flames", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setTitle("Sick Flames"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_KEROSENE_INSULATION = ITEMS.register("mp_fuselage_10_long_kerosene_insulation", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Orange Insulation").setHealth(35F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_KEROSENE_SLEEK = ITEMS.register("mp_fuselage_10_long_kerosene_sleek", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setTitle("IF-R&D").setHealth(40F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_KEROSENE_METAL = ITEMS.register("mp_fuselage_10_long_kerosene_metal", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("Hoboy").setHealth(35F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_KEROSENE_DASH = ITEMS.register("mp_fuselage_10_long_kerosene_dash", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.EPIC).setAuthor("Sam").setTitle("Dash").setWittyText("I wash my hands of it."));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_KEROSENE_TAINT = ITEMS.register("mp_fuselage_10_long_kerosene_taint", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_KEROSENE_VAP = ITEMS.register("mp_fuselage_10_long_kerosene_vap", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.EPIC).setAuthor("VT-6/24").setTitle("Minty Contrail").setWittyText("Upper rivet!"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_SOLID = ITEMS.register("mp_fuselage_10_long_solid", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.SOLID, 5000F, PartSize.SIZE_10, PartSize.SIZE_10).setHealth(35F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_SOLID_FLAMES = ITEMS.register("mp_fuselage_10_long_solid_flames", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setTitle("Sick Flames"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_SOLID_INSULATION = ITEMS.register("mp_fuselage_10_long_solid_insulation", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Orange Insulation").setHealth(40F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_SOLID_SLEEK = ITEMS.register("mp_fuselage_10_long_solid_sleek", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setTitle("IF-R&D").setHealth(45F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_SOLID_SOVIET_GLORY = ITEMS.register("mp_fuselage_10_long_solid_soviet_glory", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.EPIC).setAuthor("Hoboy").setHealth(45F).setTitle("Soviet Glory").setWittyText("Fully Automated Luxury Gay Space Communism!"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_SOLID_BULLET = ITEMS.register("mp_fuselage_10_long_solid_bullet", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setAuthor("Sam").setTitle("Bullet Bill"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_LONG_SOLID_SILVERMOONLIGHT = ITEMS.register("mp_fuselage_10_long_solid_silvermoonlight", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_10_LONG_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("The Master").setTitle("Silver Moonlight"));
    public static final DeferredItem<Item> MP_FUSELAGE_10_15_KEROSENE = ITEMS.register("mp_fuselage_10_15_kerosene", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.KEROSENE, 10000F, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_15_SOLID = ITEMS.register("mp_fuselage_10_15_solid", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.SOLID, 10000F, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_15_HYDROGEN = ITEMS.register("mp_fuselage_10_15_hydrogen", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.HYDROGEN, 10000F, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F));
    public static final DeferredItem<Item> MP_FUSELAGE_10_15_BALEFIRE = ITEMS.register("mp_fuselage_10_15_balefire", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.BALEFIRE, 10000F, PartSize.SIZE_10, PartSize.SIZE_15).setHealth(40F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE = ITEMS.register("mp_fuselage_15_kerosene", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.KEROSENE, 15000F, PartSize.SIZE_15, PartSize.SIZE_15).setAuthor("Hoboy").setHealth(50F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_CAMO = ITEMS.register("mp_fuselage_15_kerosene_camo", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Camo"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_DESERT = ITEMS.register("mp_fuselage_15_kerosene_desert", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Desert Camo"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_SKY = ITEMS.register("mp_fuselage_15_kerosene_sky", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Sky Camo"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_INSULATION = ITEMS.register("mp_fuselage_15_kerosene_insulation", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Orange Insulation").setHealth(55F).setWittyText("Rest in spaghetti Columbia :("));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_METAL = ITEMS.register("mp_fuselage_15_kerosene_metal", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Bolted Metal").setHealth(60F).setWittyText("Metal frame with metal plating reinforced with bolted metal sheets and metal."));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_DECORATED = ITEMS.register("mp_fuselage_15_kerosene_decorated", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Decorated").setHealth(60F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_STEAMPUNK = ITEMS.register("mp_fuselage_15_kerosene_steampunk", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setAuthor("Hoboy").setTitle("Steampunk").setHealth(60F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_POLITE = ITEMS.register("mp_fuselage_15_kerosene_polite", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.LEGENDARY).setAuthor("Hoboy").setTitle("Polite").setHealth(60F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_BLACKJACK = ITEMS.register("mp_fuselage_15_kerosene_blackjack", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.LEGENDARY).setTitle("Queen Whiskey").setHealth(100F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_LAMBDA = ITEMS.register("mp_fuselage_15_kerosene_lambda", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setAuthor("VT-6/24").setTitle("Lambda Complex").setHealth(75F).setWittyText("MAGNIFICENT MICROWAVE CASSEROLE"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_MINUTEMAN = ITEMS.register("mp_fuselage_15_kerosene_minuteman", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("Spexta").setTitle("MX 1702"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_PIP = ITEMS.register("mp_fuselage_15_kerosene_pip", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.EPIC).setAuthor("The Doctor").setTitle("LittlePip").setWittyText("31!"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_TAINT = ITEMS.register("mp_fuselage_15_kerosene_taint", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted").setWittyText("DUN-DUN!"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_KEROSENE_YUCK = ITEMS.register("mp_fuselage_15_kerosene_yuck", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.EPIC).setAuthor("Hoboy").setTitle("Flesh").setWittyText("Note: Never clean DNA vials with your own spit.").setHealth(60F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_SOLID = ITEMS.register("mp_fuselage_15_solid", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.SOLID, 15000F, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(60F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_SOLID_INSULATION = ITEMS.register("mp_fuselage_15_solid_insulation", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.COMMON).setTitle("Orange Insulation").setHealth(65F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_SOLID_DESH = ITEMS.register("mp_fuselage_15_solid_desh", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setAuthor("Hoboy").setTitle("Desh Plating").setHealth(80F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_SOLID_SOVIET_GLORY = ITEMS.register("mp_fuselage_15_solid_soviet_glory", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setAuthor("Hoboy").setTitle("Soviet Glory").setHealth(70F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_SOLID_SOVIET_STANK = ITEMS.register("mp_fuselage_15_solid_soviet_stank", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.EPIC).setAuthor("Hoboy").setTitle("Soviet Stank").setHealth(15F).setWittyText("Aged like a fine wine! Well, almost."));
    public static final DeferredItem<Item> MP_FUSELAGE_15_SOLID_FAUST = ITEMS.register("mp_fuselage_15_solid_faust", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.LEGENDARY).setAuthor("Dr.Nostalgia").setTitle("Mighty Lauren").setHealth(250F).setWittyText("Welcome to Subway, may I take your order?"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_SOLID_SILVERMOONLIGHT = ITEMS.register("mp_fuselage_15_solid_silvermoonlight", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("The Master").setTitle("Silver Moonlight"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_SOLID_SNOWY = ITEMS.register("mp_fuselage_15_solid_snowy", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("Dr.Nostalgia").setTitle("Chilly Day"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_SOLID_PANORAMA = ITEMS.register("mp_fuselage_15_solid_panorama", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setAuthor("Hoboy").setTitle("Panorama"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_SOLID_ROSES = ITEMS.register("mp_fuselage_15_solid_roses", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Bed of roses"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_SOLID_MIMI = ITEMS.register("mp_fuselage_15_solid_mimi", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_SOLID.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setTitle("Mimi-chan"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_HYDROGEN = ITEMS.register("mp_fuselage_15_hydrogen", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.HYDROGEN, 15000F, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(50F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_HYDROGEN_CATHEDRAL = ITEMS.register("mp_fuselage_15_hydrogen_cathedral", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_HYDROGEN.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setAuthor("Satan").setTitle("Unholy Cathedral"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_BALEFIRE = ITEMS.register("mp_fuselage_15_balefire", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.BALEFIRE, 15000F, PartSize.SIZE_15, PartSize.SIZE_15).setHealth(75F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_20_KEROSENE = ITEMS.register("mp_fuselage_15_20_kerosene", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.KEROSENE, 20000, PartSize.SIZE_15, PartSize.SIZE_20).setAuthor("Hoboy").setHealth(70F));
    public static final DeferredItem<Item> MP_FUSELAGE_15_20_KEROSENE_MAGNUSSON = ITEMS.register("mp_fuselage_15_20_kerosene_magnusson", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_FUSELAGE_15_20_KEROSENE.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setAuthor("VT-6/24").setTitle("White Forest Rocket").setWittyText("And get your cranio-conjugal parasite away from my nose cone!"));
    public static final DeferredItem<Item> MP_FUSELAGE_15_20_SOLID = ITEMS.register("mp_fuselage_15_20_solid", () -> new CustomMissilePartItem(new Item.Properties()).makeFuselage(FuelType.SOLID, 20000, PartSize.SIZE_15, PartSize.SIZE_20).setHealth(70F));
    public static final DeferredItem<Item> MP_WARHEAD_10_HE = ITEMS.register("mp_warhead_10_he", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.HE, 15F, 1.5F, PartSize.SIZE_10).setHealth(5F));
    public static final DeferredItem<Item> MP_WARHEAD_10_INCENDIARY = ITEMS.register("mp_warhead_10_incendiary", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.INC, 15F, 1.5F, PartSize.SIZE_10).setHealth(5F));
    public static final DeferredItem<Item> MP_WARHEAD_10_BUSTER = ITEMS.register("mp_warhead_10_buster", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.BUSTER, 5F, 1.5F, PartSize.SIZE_10).setHealth(5F));
    public static final DeferredItem<Item> MP_WARHEAD_10_NUCLEAR = ITEMS.register("mp_warhead_10_nuclear", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.NUCLEAR, 35F, 1.5F, PartSize.SIZE_10).setTitle("Tater Tot").setHealth(10F));
    public static final DeferredItem<Item> MP_WARHEAD_10_NUCLEAR_LARGE = ITEMS.register("mp_warhead_10_nuclear_large", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.NUCLEAR, 75F, 2.5F, PartSize.SIZE_10).setTitle("Chernobyl Boris").setHealth(15F));
    public static final DeferredItem<Item> MP_WARHEAD_10_TAINT = ITEMS.register("mp_warhead_10_taint", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.TAINT, 15F, 1.5F, PartSize.SIZE_10).setHealth(20F).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setWittyText("Eat my taint! Bureaucracy is dead and we killed it!"));
    public static final DeferredItem<Item> MP_WARHEAD_10_CLOUD = ITEMS.register("mp_warhead_10_cloud", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.CLOUD, 15F, 1.5F, PartSize.SIZE_10).setHealth(20F).setRarity(CustomMissilePartItem.Rarity.RARE));
    public static final DeferredItem<Item> MP_WARHEAD_15_HE = ITEMS.register("mp_warhead_15_he", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.HE, 50F, 2.5F, PartSize.SIZE_15).setHealth(10F));
    public static final DeferredItem<Item> MP_WARHEAD_15_INCENDIARY = ITEMS.register("mp_warhead_15_incendiary", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.INC, 35F, 2.5F, PartSize.SIZE_15).setHealth(10F));
    public static final DeferredItem<Item> MP_WARHEAD_15_NUCLEAR = ITEMS.register("mp_warhead_15_nuclear", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.NUCLEAR, 125F, 5F, PartSize.SIZE_15).setTitle("Auntie Bertha").setHealth(15F));
    public static final DeferredItem<Item> MP_WARHEAD_15_NUCLEAR_SHARK = ITEMS.register("mp_warhead_15_nuclear_shark", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_WARHEAD_15_NUCLEAR.get()).setRarity(CustomMissilePartItem.Rarity.UNCOMMON).setTitle("Discount Bullet Bill").setWittyText("Nose art on a cannon bullet? Who does that?"));
    public static final DeferredItem<Item> MP_WARHEAD_15_NUCLEAR_MIMI = ITEMS.register("mp_warhead_15_nuclear_mimi", () -> new CustomMissilePartItem(new Item.Properties()).copyFrom((CustomMissilePartItem) MP_WARHEAD_15_NUCLEAR.get()).setRarity(CustomMissilePartItem.Rarity.RARE).setTitle("FASHIONABLE MISSILE"));
    public static final DeferredItem<Item> MP_WARHEAD_15_BOXCAR = ITEMS.register("mp_warhead_15_boxcar", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.TX, 250F, 7.5F, PartSize.SIZE_15).setWittyText("?!?!").setHealth(35F).setRarity(CustomMissilePartItem.Rarity.LEGENDARY));
    public static final DeferredItem<Item> MP_WARHEAD_15_N2 = ITEMS.register("mp_warhead_15_n2", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.N2, 100F, 5F, PartSize.SIZE_15).setWittyText("[screams geometrically]").setHealth(20F).setRarity(CustomMissilePartItem.Rarity.RARE));
    public static final DeferredItem<Item> MP_WARHEAD_15_BALEFIRE = ITEMS.register("mp_warhead_15_balefire", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.BALEFIRE, 100F, 7.5F, PartSize.SIZE_15).setRarity(CustomMissilePartItem.Rarity.LEGENDARY).setAuthor("VT-6/24").setHealth(15F).setWittyText("Hightower, never forgetti."));
    public static final DeferredItem<Item> MP_WARHEAD_15_TURBINE = ITEMS.register("mp_warhead_15_turbine", () -> new CustomMissilePartItem(new Item.Properties()).makeWarhead(WarheadType.TURBINE, 200F, 5F, PartSize.SIZE_15).setRarity(CustomMissilePartItem.Rarity.STRANGE).setHealth(250F));
    public static final DeferredItem<Item> MP_CHIP_1 = ITEMS.register("mp_c_1", () -> new CustomMissilePartItem(new Item.Properties()).makeChip(0.1F));
    public static final DeferredItem<Item> MP_CHIP_2 = ITEMS.register("mp_c_2", () -> new CustomMissilePartItem(new Item.Properties()).makeChip(0.05F));
    public static final DeferredItem<Item> MP_CHIP_3 = ITEMS.register("mp_c_3", () -> new CustomMissilePartItem(new Item.Properties()).makeChip(0.01F));
    public static final DeferredItem<Item> MP_CHIP_4 = ITEMS.register("mp_c_4", () -> new CustomMissilePartItem(new Item.Properties()).makeChip(0.005F));
    public static final DeferredItem<Item> MP_CHIP_5 = ITEMS.register("mp_c_5", () -> new CustomMissilePartItem(new Item.Properties()).makeChip(0.0F));

    /* Runde 130, die Praezisionsmontage: der Ausschuss und das Erzeugnis, das nur sie herstellt. */
    public static final DeferredItem<Item> BROKEN_ITEM = ITEMS.register("broken_item", () -> new BrokenItem(new Item.Properties()));
    public static final DeferredItem<Item> ORBITAL_ASSEMBLY = ITEMS.register("orbital_assembly", () -> new OrbitalAssemblyItem(new Item.Properties()));

    // Hydraulic Press Stamps
    public static final DeferredItem<Item> STAMP_STONE_FLAT = ITEMS.register("stamp_stone_flat", () -> new StampItem(32, StampType.FLAT));
    public static final DeferredItem<Item> STAMP_STONE_PLATE = ITEMS.register("stamp_stone_plate", () -> new StampItem(32, StampType.PLATE));
    public static final DeferredItem<Item> STAMP_STONE_WIRE = ITEMS.register("stamp_stone_wire", () -> new StampItem(32, StampType.WIRE));
    public static final DeferredItem<Item> STAMP_STONE_CIRCUIT = ITEMS.register("stamp_stone_circuit", () -> new StampItem(32, StampType.CIRCUIT));
    public static final DeferredItem<Item> STAMP_IRON_FLAT = ITEMS.register("stamp_iron_flat", () -> new StampItem(64, StampType.FLAT));
    public static final DeferredItem<Item> STAMP_IRON_PLATE = ITEMS.register("stamp_iron_plate", () -> new StampItem(64, StampType.PLATE));
    public static final DeferredItem<Item> STAMP_IRON_WIRE = ITEMS.register("stamp_iron_wire", () -> new StampItem(64, StampType.WIRE));
    public static final DeferredItem<Item> STAMP_IRON_CIRCUIT = ITEMS.register("stamp_iron_circuit", () -> new StampItem(64, StampType.CIRCUIT));
    public static final DeferredItem<Item> STAMP_STEEL_FLAT = ITEMS.register("stamp_steel_flat", () -> new StampItem(192, StampType.FLAT));
    public static final DeferredItem<Item> STAMP_STEEL_PLATE = ITEMS.register("stamp_steel_plate", () -> new StampItem(192, StampType.PLATE));
    public static final DeferredItem<Item> STAMP_STEEL_WIRE = ITEMS.register("stamp_steel_wire", () -> new StampItem(192, StampType.WIRE));
    public static final DeferredItem<Item> STAMP_STEEL_CIRCUIT = ITEMS.register("stamp_steel_circuit", () -> new StampItem(192, StampType.CIRCUIT));
    public static final DeferredItem<Item> STAMP_TITANIUM_FLAT = ITEMS.register("stamp_titanium_flat", () -> new StampItem(256, StampType.FLAT));
    public static final DeferredItem<Item> STAMP_TITANIUM_PLATE = ITEMS.register("stamp_titanium_plate", () -> new StampItem(256, StampType.PLATE));
    public static final DeferredItem<Item> STAMP_TITANIUM_WIRE = ITEMS.register("stamp_titanium_wire", () -> new StampItem(256, StampType.WIRE));
    public static final DeferredItem<Item> STAMP_TITANIUM_CIRCUIT = ITEMS.register("stamp_titanium_circuit", () -> new StampItem(256, StampType.CIRCUIT));
    public static final DeferredItem<Item> STAMP_OBSIDIAN_FLAT = ITEMS.register("stamp_obsidian_flat", () -> new StampItem(512, StampType.FLAT));
    public static final DeferredItem<Item> STAMP_OBSIDIAN_PLATE = ITEMS.register("stamp_obsidian_plate", () -> new StampItem(512, StampType.PLATE));
    public static final DeferredItem<Item> STAMP_OBSIDIAN_WIRE = ITEMS.register("stamp_obsidian_wire", () -> new StampItem(512, StampType.WIRE));
    public static final DeferredItem<Item> STAMP_OBSIDIAN_CIRCUIT = ITEMS.register("stamp_obsidian_circuit", () -> new StampItem(512, StampType.CIRCUIT));
    public static final DeferredItem<Item> STAMP_DESH_FLAT = ITEMS.register("stamp_desh_flat", () -> new StampItem(0, StampType.FLAT));
    public static final DeferredItem<Item> STAMP_DESH_PLATE = ITEMS.register("stamp_desh_plate", () -> new StampItem(0, StampType.PLATE));
    public static final DeferredItem<Item> STAMP_DESH_WIRE = ITEMS.register("stamp_desh_wire", () -> new StampItem(0, StampType.WIRE));
    public static final DeferredItem<Item> STAMP_DESH_CIRCUIT = ITEMS.register("stamp_desh_circuit", () -> new StampItem(0, StampType.CIRCUIT));
    /*
     * DIE KALIBERSTEMPEL. Sie praegen Huelsen aus Platten; ohne sie hat die Munitionspresse
     * keinen Rohstoff, denn Huelsen entstehen im ganzen Spiel nur hier.
     *
     * NICHT UEBERNOMMEN sind stamp_357 und stamp_44 (samt ihren Desh-Ausfuehrungen). Sie gibt
     * es im Original zwar, aber KEIN Pressrezept verlangt C357 oder C44 -- nachgemessen in
     * PressRecipes des Originals, das nur C9 und C50 kennt. Es waeren Gegenstaende ohne
     * Wirkung. Die Bilder dafuer liegen im Baum und warten.
     */
    public static final DeferredItem<Item> STAMP_9 = ITEMS.register("stamp_9", () -> new StampItem(1000, StampType.C9));
    public static final DeferredItem<Item> STAMP_50 = ITEMS.register("stamp_50", () -> new StampItem(1000, StampType.C50));
    public static final DeferredItem<Item> STAMP_DESH_9 = ITEMS.register("stamp_desh_9", () -> new StampItem(0, StampType.C9));
    public static final DeferredItem<Item> STAMP_DESH_50 = ITEMS.register("stamp_desh_50", () -> new StampItem(0, StampType.C50));

    // Machine Templates
    public static final DeferredItem<FluidIconItem> FLUID_ICON = ITEMS.register("fluid_icon", () -> new FluidIconItem(new Item.Properties()));
    public static final DeferredItem<Item> FLUID_IDENTIFIER_MULTI = ITEMS.register("fluid_identifier_multi", () -> new FluidIDMultiItem(new Item.Properties()));

    // Machine Items
    //by using these in crafting table recipes, i'm running the risk of making my recipes too greg-ian (which i don't like)
    //in the event that i forget about the meaning of the word "sparingly", please throw a brick at my head
    /*
     * Runde 99: die beiden Schraubenzieher sind jetzt WERKZEUGE. Bis hierher waren sie
     * gewoehnliche Gegenstaende -- damit hat nie etwas onScrew aufgerufen, und JEDE der
     * fuenfzehn Maschinen mit onScrew(ToolType.SCREWDRIVER) war in Wahrheit nicht einstellbar:
     * die RBMK-Saeulen, die Heizer, das Giessbecken, der Drescher und das ganze Foerdernetz
     * der Runden 94 bis 98. Aufgefallen ist es beim Durchsehen des Greifers, nicht im Bau --
     * uebersetzen laesst sich beides.
     *
     * Die Haltbarkeit ist die des Originals: hundert Anwendungen fuer den eisernen, unbegrenzt
     * fuer den aus Desh.
     */
    public static final DeferredItem<Item> SCREWDRIVER = ITEMS.register("screwdriver", () -> new ToolingItem(ToolType.SCREWDRIVER, new Item.Properties().durability(100)));
    public static final DeferredItem<Item> SCREWDRIVER_DESH = ITEMS.register("screwdriver_desh", () -> new ToolingItem(ToolType.SCREWDRIVER, new Item.Properties()));
    public static final DeferredItem<Item> BLOWTORCH = ITEMS.register("blowtorch", () -> new BlowtorchItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> ACETYLENE_TORCH = ITEMS.register("acetylene_torch", () -> new BlowtorchItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> BLADES_STEEL = ITEMS.register("blades_steel", () -> new Item(new Item.Properties().stacksTo(1).durability(256)));
    public static final DeferredItem<Item> BLADES_TITANIUM = ITEMS.register("blades_titanium", () -> new Item(new Item.Properties().stacksTo(1).durability(512)));
    public static final DeferredItem<Item> BLADES_DESH = ITEMS.register("blades_desh", () -> new Item(new Item.Properties().stacksTo(1)));

    // Upgrades
    public static final DeferredItem<Item> UPGRADE_TEMPLATE = ITEMS.register("upgrade_template", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> UPGRADE_SPEED_1 = ITEMS.register("upgrade_speed_1", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.SPEED, 1));
    public static final DeferredItem<Item> UPGRADE_SPEED_2 = ITEMS.register("upgrade_speed_2", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.SPEED, 2));
    public static final DeferredItem<Item> UPGRADE_SPEED_3 = ITEMS.register("upgrade_speed_3", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.SPEED, 3));
    public static final DeferredItem<Item> UPGRADE_EFFECT_1 = ITEMS.register("upgrade_effect_1", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.EFFECT, 1));
    public static final DeferredItem<Item> UPGRADE_EFFECT_2 = ITEMS.register("upgrade_effect_2", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.EFFECT, 2));
    public static final DeferredItem<Item> UPGRADE_EFFECT_3 = ITEMS.register("upgrade_effect_3", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.EFFECT, 3));
    public static final DeferredItem<Item> UPGRADE_POWER_1 = ITEMS.register("upgrade_power_1", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.POWER, 1));
    public static final DeferredItem<Item> UPGRADE_POWER_2 = ITEMS.register("upgrade_power_2", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.POWER, 2));
    public static final DeferredItem<Item> UPGRADE_POWER_3 = ITEMS.register("upgrade_power_3", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.POWER, 3));
    public static final DeferredItem<Item> UPGRADE_FORTUNE_1 = ITEMS.register("upgrade_fortune_1", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.FORTUNE, 1));
    public static final DeferredItem<Item> UPGRADE_FORTUNE_2 = ITEMS.register("upgrade_fortune_2", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.FORTUNE, 2));
    public static final DeferredItem<Item> UPGRADE_FORTUNE_3 = ITEMS.register("upgrade_fortune_3", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.FORTUNE, 3));
    public static final DeferredItem<Item> UPGRADE_AFTERBURN_1 = ITEMS.register("upgrade_afterburn_1", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.AFTERBURN, 1));
    public static final DeferredItem<Item> UPGRADE_AFTERBURN_2 = ITEMS.register("upgrade_afterburn_2", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.AFTERBURN, 2));
    public static final DeferredItem<Item> UPGRADE_AFTERBURN_3 = ITEMS.register("upgrade_afterburn_3", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.AFTERBURN, 3));
    public static final DeferredItem<Item> UPGRADE_RADIUS = ITEMS.register("upgrade_radius", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> UPGRADE_HEALTH = ITEMS.register("upgrade_health", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> UPGRADE_OVERDRIVE_1 = ITEMS.register("upgrade_overdrive_1", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.OVERDRIVE, 1));
    public static final DeferredItem<Item> UPGRADE_OVERDRIVE_2 = ITEMS.register("upgrade_overdrive_2", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.OVERDRIVE, 2));
    public static final DeferredItem<Item> UPGRADE_OVERDRIVE_3 = ITEMS.register("upgrade_overdrive_3", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.OVERDRIVE, 3));

    /* Die schnelle Gaszentrifuge. Sie ist keine Aufwertung im ueblichen Sinn: die Maschine
     * fragt nach dem Gegenstand selbst, nicht ueber den Aufwertungsverwalter. GS_SPEED stand
     * schon im Original in der Liste der Arten und hatte dort keinen Traeger. */
    public static final DeferredItem<Item> UPGRADE_GC_SPEED = ITEMS.register("upgrade_gc_speed", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.GS_SPEED));

    /* Die sechs Sonderaufwertungen des Bergbaulasers. Vier davon schliessen einander aus -- der
     * Laser fragt jede einzeln ab und nimmt die erste, die er findet; zwei zugleich waeren
     * sinnlos. Die Arten standen schon im Original in der Liste und hatten dort keinen Traeger. */
    public static final DeferredItem<Item> UPGRADE_SMELTER = ITEMS.register("upgrade_smelter", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.LM_SMELTER));
    public static final DeferredItem<Item> UPGRADE_SHREDDER = ITEMS.register("upgrade_shredder", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.LM_SHREDDER));
    public static final DeferredItem<Item> UPGRADE_CENTRIFUGE = ITEMS.register("upgrade_centrifuge", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.LM_CENTRIFUGE));
    public static final DeferredItem<Item> UPGRADE_CRYSTALLIZER = ITEMS.register("upgrade_crystallizer", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.LM_CRYSTALLIZER));
    public static final DeferredItem<Item> UPGRADE_NULLIFIER = ITEMS.register("upgrade_nullifier", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.LM_DESROYER));
    public static final DeferredItem<Item> UPGRADE_SCREM = ITEMS.register("upgrade_screm", () -> new MachineUpgradeItem(new Item.Properties(), MachineUpgradeItem.UpgradeType.LM_SCREM));

    // Breeding Rods
    public static final DeferredItem<Item> ROD_EMPTY = ITEMS.register("rod_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD = ITEMS.register("rod", () -> new BreedingRodItem(new Item.Properties()));
    public static final DeferredItem<Item> ROD_DUAL_EMPTY = ITEMS.register("rod_dual_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD_DUAL = ITEMS.register("rod_dual", () -> new BreedingRodItem(new Item.Properties()));
    public static final DeferredItem<Item> ROD_QUAD_EMPTY = ITEMS.register("rod_quad_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD_QUAD = ITEMS.register("rod_quad", () -> new BreedingRodItem(new Item.Properties()));

    // ZIRNOX parts
    public static final DeferredItem<Item> ROD_ZIRNOX_EMPTY = ITEMS.register("rod_zirnox_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD_ZIRNOX_TRITIUM = ITEMS.register("rod_zirnox_tritium", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> ROD_ZIRNOX = ITEMS.register("rod_zirnox", () -> new ZirnoxRodItem(new Item.Properties().stacksTo(1).setNoRepair()));

    public static final DeferredItem<Item> ROD_ZIRNOX_NATURAL_URANIUM_FUEL_DEPLETED = ITEMS.register("rod_zirnox_natural_uranium_fuel_depleted", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD_ZIRNOX_URANIUM_FUEL_DEPLETED = ITEMS.register("rod_zirnox_uranium_fuel_depleted", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD_ZIRNOX_THORIUM_FUEL_DEPLETED = ITEMS.register("rod_zirnox_thorium_fuel_depleted", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD_ZIRNOX_MOX_FUEL_DEPLETED = ITEMS.register("rod_zirnox_mox_fuel_depleted", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD_ZIRNOX_PLUTONIUM_FUEL_DEPLETED = ITEMS.register("rod_zirnox_plutonium_fuel_depleted", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD_ZIRNOX_U233_FUEL_DEPLETED = ITEMS.register("rod_zirnox_u233_fuel_depleted", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD_ZIRNOX_U235_FUEL_DEPLETED = ITEMS.register("rod_zirnox_u235_fuel_depleted", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD_ZIRNOX_LES_FUEL_DEPLETED = ITEMS.register("rod_zirnox_les_fuel_depleted", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROD_ZIRNOX_ZFB_MOX_DEPLETED = ITEMS.register("rod_zirnox_zfb_mox_depleted", () -> new Item(new Item.Properties()));

    // Spawners
    public static final DeferredItem<Item> SPAWN_DUCK = ITEMS.register("spawn_duck", () -> new EntitySpawnerItem(new Item.Properties().stacksTo(16)));

    // Computer Tools
    public static final DeferredItem<Item> DESIGNATOR = ITEMS.register("designator", () -> new DesignatorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DESIGNATOR_RANGE = ITEMS.register("designator_range", () -> new DesignatorRangeItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DOSIMETER = ITEMS.register("dosimeter", () -> new DosimeterItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> GEIGER_COUNTER = ITEMS.register("geiger_counter", () -> new GeigerCounterItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> OIL_DETECTOR = ITEMS.register("oil_detector", () -> new OilDetectorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DIGAMMA_DIAGNOSTIC = ITEMS.register("digamma_diagnostic", () -> new DigammaDiagnosticItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> METEOR_REMOTE = ITEMS.register("meteor_remote", () -> new MeteorRemoteItem(new Item.Properties()));
    public static final DeferredItem<Item> METEOR_CHARM = ITEMS.register("meteor_charm", () -> new ModCharmItem(new Item.Properties(), true));
    public static final DeferredItem<Item> PROTECTION_CHARM = ITEMS.register("protection_charm", () -> new ModCharmItem(new Item.Properties(), false));

    // Keys and Locks
    public static final DeferredItem<Item> PIN = ITEMS.register("pin", () -> new Item(new Item.Properties().stacksTo(8)));
    public static final DeferredItem<Item> KEY = ITEMS.register("key", () -> new KeyItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> KEY_RED = ITEMS.register("key_red", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> KEY_RED_CRACKED = ITEMS.register("key_red_cracked", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> KEY_KIT = ITEMS.register("key_kit", () -> new KeyItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> KEY_FAKE = ITEMS.register("key_fake", () -> new KeyItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LAUNCH_CODE_PIECE = ITEMS.register("launch_code_piece", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LAUNCH_CODE = ITEMS.register("launch_code", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LAUNCH_KEY = ITEMS.register("launch_key", () -> new Item(new Item.Properties().stacksTo(1)));

    // Missiles
    // Tier 0
    public static final DeferredItem<Item> MISSILE_TAINT =       ITEMS.register("missile_taint",       () -> new MissileItem(MissileFormFactor.MICRO, MissileTier.TIER0));
    public static final DeferredItem<Item> MISSILE_MICRO =       ITEMS.register("missile_micro",       () -> new MissileItem(MissileFormFactor.MICRO, MissileTier.TIER0));
    public static final DeferredItem<Item> MISSILE_BHOLE =       ITEMS.register("missile_bhole",       () -> new MissileItem(MissileFormFactor.MICRO, MissileTier.TIER0));
    public static final DeferredItem<Item> MISSILE_SCHRABIDIUM = ITEMS.register("missile_schrabidium", () -> new MissileItem(MissileFormFactor.MICRO, MissileTier.TIER0));
    public static final DeferredItem<Item> MISSILE_EMP =         ITEMS.register("missile_emp",         () -> new MissileItem(MissileFormFactor.MICRO, MissileTier.TIER0));
    // Tier 1
    public static final DeferredItem<Item> MISSILE_GENERIC =        ITEMS.register("missile_generic",        () -> new MissileItem(MissileFormFactor.V2, MissileTier.TIER1));
    public static final DeferredItem<Item> MISSILE_INCENDIARY =     ITEMS.register("missile_incendiary",     () -> new MissileItem(MissileFormFactor.V2, MissileTier.TIER1));
    public static final DeferredItem<Item> MISSILE_CLUSTER =        ITEMS.register("missile_cluster",        () -> new MissileItem(MissileFormFactor.V2, MissileTier.TIER1));
    public static final DeferredItem<Item> MISSILE_BUSTER =         ITEMS.register("missile_buster",         () -> new MissileItem(MissileFormFactor.V2, MissileTier.TIER1));
    public static final DeferredItem<Item> MISSILE_DECOY =          ITEMS.register("missile_decoy",          () -> new MissileItem(MissileFormFactor.V2, MissileTier.TIER1));
    public static final DeferredItem<Item> MISSILE_STEALTH =        ITEMS.register("missile_stealth",        () -> new MissileItem(MissileFormFactor.STRONG, MissileTier.TIER1));
    public static final DeferredItem<Item> MISSILE_ANTI_BALLISTIC = ITEMS.register("missile_anti_ballistic", () -> new MissileItem(MissileFormFactor.ABM, MissileTier.TIER1));
    // Tier 2
    public static final DeferredItem<Item> MISSILE_STRONG =            ITEMS.register("missile_strong",            () -> new MissileItem(MissileFormFactor.STRONG, MissileTier.TIER2));
    public static final DeferredItem<Item> MISSILE_INCENDIARY_STRONG = ITEMS.register("missile_incendiary_strong", () -> new MissileItem(MissileFormFactor.STRONG, MissileTier.TIER2));
    public static final DeferredItem<Item> MISSILE_CLUSTER_STRONG =    ITEMS.register("missile_cluster_strong",    () -> new MissileItem(MissileFormFactor.STRONG, MissileTier.TIER2));
    public static final DeferredItem<Item> MISSILE_BUSTER_STRONG =     ITEMS.register("missile_buster_strong",     () -> new MissileItem(MissileFormFactor.STRONG, MissileTier.TIER2));
    public static final DeferredItem<Item> MISSILE_EMP_STRONG =        ITEMS.register("missile_emp_strong",        () -> new MissileItem(MissileFormFactor.STRONG, MissileTier.TIER2));
    // Tier 3
    public static final DeferredItem<Item> MISSILE_BURST =   ITEMS.register("missile_burst",   () -> new MissileItem(MissileFormFactor.HUGE, MissileTier.TIER3));
    public static final DeferredItem<Item> MISSILE_INFERNO = ITEMS.register("missile_inferno", () -> new MissileItem(MissileFormFactor.HUGE, MissileTier.TIER3));
    public static final DeferredItem<Item> MISSILE_RAIN =    ITEMS.register("missile_rain",    () -> new MissileItem(MissileFormFactor.HUGE, MissileTier.TIER3));
    public static final DeferredItem<Item> MISSILE_DRILL =   ITEMS.register("missile_drill",   () -> new MissileItem(MissileFormFactor.HUGE, MissileTier.TIER3));
    public static final DeferredItem<Item> MISSILE_SHUTTLE = ITEMS.register("missile_shuttle", () -> new MissileItem(MissileFormFactor.OTHER, MissileTier.TIER3, MissileFuel.KEROSENE_PEROXIDE));
    // Tier 4
    public static final DeferredItem<Item> MISSILE_NUCLEAR =         ITEMS.register("missile_nuclear",         () -> new MissileItem(MissileFormFactor.ATLAS, MissileTier.TIER4));
    public static final DeferredItem<Item> MISSILE_NUCLEAR_CLUSTER = ITEMS.register("missile_nuclear_cluster", () -> new MissileItem(MissileFormFactor.ATLAS, MissileTier.TIER4));
    public static final DeferredItem<Item> MISSILE_VOLCANO =         ITEMS.register("missile_volcano",         () -> new MissileItem(MissileFormFactor.ATLAS, MissileTier.TIER4));
    public static final DeferredItem<Item> MISSILE_DOOMSDAY =        ITEMS.register("missile_doomsday",        () -> new MissileItem(MissileFormFactor.ATLAS, MissileTier.TIER4));
    public static final DeferredItem<Item> MISSILE_DOOMSDAY_RUSTED = ITEMS.register("missile_doomsday_rusted", () -> new MissileItem(MissileFormFactor.ATLAS, MissileTier.TIER4).notLaunchable());
    // Rockets
    public static final DeferredItem<Item> MISSILE_SOYUZ = ITEMS.register("missile_soyuz", () -> new SoyuzItem(new Item.Properties()));

    // Satellites
    public static final DeferredItem<Item> SATELLITE = ITEMS.register("satellite", () -> new SatelliteItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SAT_GERALD = ITEMS.register("sat_gerald", () -> new SatChipItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SAT_CHIP = ITEMS.register("sat_chip", () -> new SatChipItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SAT_COORD = ITEMS.register("sat_coord", () -> new SatInterfaceItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SAT_DESIGNATOR = ITEMS.register("sat_designator", () -> new SatDesignatorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> RADAR_LINKER = ITEMS.register("radar_linker", () -> new RadarLinkerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CASSETTE = ITEMS.register("cassette", () -> new CassetteItem(new Item.Properties()));

    // Drives
    public static final DeferredItem<Item> DRIVE = ITEMS.register("drive", () -> new DriveItem(new Item.Properties()));

    // Tools
    public static final DeferredItem<Item> BALEFIRE_AND_STEEL = ITEMS.register("balefire_and_steel", () -> new BalefireAndSteelItem(new Item.Properties().stacksTo(1).durability(256)));
    public static final DeferredItem<Item> STEEL_PICKAXE = registerPickaxe("steel_pickaxe", NtmTiers.TOOL_STEEL, 4, -2.8F, tool -> tool.addAbility(IToolAreaAbility.RECURSION, 0));
    public static final DeferredItem<Item> STEEL_AXE = registerAxe("steel_axe", NtmTiers.TOOL_STEEL, 5.0F, -2.8F, tool -> tool.addAbility(IToolAreaAbility.RECURSION, 0).addAbility(IWeaponAbility.BEHEADER, 0));
    public static final DeferredItem<Item> STEEL_SHOVEL = registerShovel("steel_shovel", NtmTiers.TOOL_STEEL, 3.0F, -2.8F, tool -> tool.addAbility(IToolAreaAbility.RECURSION, 0));
    public static final DeferredItem<Item> STEEL_HOE = registerHoe("steel_hoe", NtmTiers.TOOL_STEEL, 0, -2.8F);

    public static final DeferredItem<Item> TITANIUM_PICKAXE = registerPickaxe("titanium_pickaxe", NtmTiers.TOOL_TITANIUM, 4, -2.8F);
    public static final DeferredItem<Item> TITANIUM_AXE = registerAxe("titanium_axe", NtmTiers.TOOL_TITANIUM, 5.5F, -2.8F);
    public static final DeferredItem<Item> TITANIUM_SHOVEL = registerShovel("titanium_shovel", NtmTiers.TOOL_TITANIUM, 3.5F, -2.8F);
    public static final DeferredItem<Item> TITANIUM_HOE = registerHoe("titanium_hoe", NtmTiers.TOOL_TITANIUM, 0, -2.8F);

    public static final DeferredItem<Item> DESH_PICKAXE = registerPickaxe("desh_pickaxe", NtmTiers.TOOL_DESH, 5, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 0).addAbility(IToolAreaAbility.HAMMER, 0).addAbility(IToolAreaAbility.HAMMER_FLAT, 0).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 1));
    public static final DeferredItem<Item> DESH_AXE = registerAxe("desh_axe", NtmTiers.TOOL_DESH, 6.5F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 0).addAbility(IToolAreaAbility.HAMMER, 0).addAbility(IToolAreaAbility.HAMMER_FLAT, 0).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 1).addAbility(IWeaponAbility.BEHEADER, 0));
    public static final DeferredItem<Item> DESH_SHOVEL = registerShovel("desh_shovel", NtmTiers.TOOL_DESH, 4.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 0).addAbility(IToolAreaAbility.HAMMER, 0).addAbility(IToolAreaAbility.HAMMER_FLAT, 0).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 1));
    public static final DeferredItem<Item> DESH_HOE = registerHoe("desh_hoe", NtmTiers.TOOL_DESH, 0, -2.8F);

    public static final DeferredItem<Item> COBALT_PICKAXE = registerPickaxe("cobalt_pickaxe", NtmTiers.TOOL_COBALT, 4, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 0));
    public static final DeferredItem<Item> COBALT_AXE = registerAxe("cobalt_axe", NtmTiers.TOOL_COBALT, 6.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 0).addAbility(IWeaponAbility.BEHEADER, 0));
    public static final DeferredItem<Item> COBALT_SHOVEL = registerShovel("cobalt_shovel", NtmTiers.TOOL_COBALT, 3.5F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 0));
    public static final DeferredItem<Item> COBALT_HOE = registerHoe("cobalt_hoe", NtmTiers.TOOL_COBALT, 0, -2.8F);

    public static final DeferredItem<Item> COBALT_DECORATED_PICKAXE = registerPickaxe("cobalt_decorated_pickaxe", NtmTiers.TOOL_DECORATED_COBALT, 6, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolAreaAbility.HAMMER, 0).addAbility(IToolAreaAbility.HAMMER_FLAT, 0).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 2));
    public static final DeferredItem<Item> COBALT_DECORATED_AXE = registerAxe("cobalt_decorated_axe", NtmTiers.TOOL_DECORATED_COBALT, 8.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolAreaAbility.HAMMER, 0).addAbility(IToolAreaAbility.HAMMER_FLAT, 0).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 2).addAbility(IWeaponAbility.BEHEADER, 0));
    public static final DeferredItem<Item> COBALT_DECORATED_SHOVEL = registerShovel("cobalt_decorated_shovel", NtmTiers.TOOL_DECORATED_COBALT, 5.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolAreaAbility.HAMMER, 0).addAbility(IToolAreaAbility.HAMMER_FLAT, 0).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 2));
    public static final DeferredItem<Item> COBALT_DECORATED_HOE = registerHoe("cobalt_decorated_hoe", NtmTiers.TOOL_DECORATED_COBALT, 0, -2.8F);

    public static final DeferredItem<Item> CMB_PICKAXE = registerPickaxe("cmb_pickaxe", NtmTiers.TOOL_CMB, 10, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 2).addAbility(IToolHarvestAbility.SMELTER, 0).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 2));
    public static final DeferredItem<Item> CMB_AXE = registerAxe("cmb_axe", NtmTiers.TOOL_CMB, 30.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 2).addAbility(IToolHarvestAbility.SMELTER, 0).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 2).addAbility(IWeaponAbility.BEHEADER, 0));
    public static final DeferredItem<Item> CMB_SHOVEL = registerShovel("cmb_shovel", NtmTiers.TOOL_CMB, 8.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 2).addAbility(IToolHarvestAbility.SMELTER, 0).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 2));
    public static final DeferredItem<Item> CMB_HOE = registerHoe("cmb_hoe", NtmTiers.TOOL_CMB, 0, -2.8F);

    public static final DeferredItem<Item> BISMUTH_PICKAXE = registerPickaxe("bismuth_pickaxe", NtmTiers.TOOL_BISMUTH, 15, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolHarvestAbility.SHREDDER, 0).addAbility(IToolHarvestAbility.LUCK, 1).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IWeaponAbility.STUN, 2).addAbility(IWeaponAbility.VAMPIRE, 0).addAbility(IWeaponAbility.BEHEADER, 0).setDepthRockBreaker());
    public static final DeferredItem<Item> BISMUTH_AXE = registerAxe("bismuth_axe", NtmTiers.TOOL_BISMUTH, 25.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolHarvestAbility.SHREDDER, 0).addAbility(IToolHarvestAbility.LUCK, 1).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IWeaponAbility.STUN, 3).addAbility(IWeaponAbility.VAMPIRE, 1).addAbility(IWeaponAbility.BEHEADER, 0));

    public static final DeferredItem<Item> STARMETAL_PICKAXE = registerPickaxe("starmetal_pickaxe", NtmTiers.TOOL_STARMETAL, 8, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 3).addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 4).addAbility(IWeaponAbility.STUN, 1));
    public static final DeferredItem<Item> STARMETAL_AXE = registerAxe("starmetal_axe", NtmTiers.TOOL_STARMETAL, 12.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 3).addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 4).addAbility(IWeaponAbility.STUN, 1).addAbility(IWeaponAbility.BEHEADER, 0));
    public static final DeferredItem<Item> STARMETAL_SHOVEL = registerShovel("starmetal_shovel", NtmTiers.TOOL_STARMETAL, 7.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.RECURSION, 3).addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 4).addAbility(IWeaponAbility.STUN, 1));
    public static final DeferredItem<Item> STARMETAL_HOE = registerHoe("starmetal_hoe", NtmTiers.TOOL_STARMETAL, 0, -2.8F);

    public static final DeferredItem<Item> SCHRABIDIUM_PICKAXE = registerPickaxe("schrabidium_pickaxe", NtmTiers.TOOL_SCHRABIDIUM, 20, -2.8F, Rarity.RARE,
            tool -> tool.addAbility(IWeaponAbility.RADIATION, 0).addAbility(IToolAreaAbility.RECURSION, 6).addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 4).addAbility(IToolHarvestAbility.SMELTER, 0).addAbility(IToolHarvestAbility.SHREDDER, 0));
    public static final DeferredItem<Item> SCHRABIDIUM_AXE = registerAxe("schrabidium_axe", NtmTiers.TOOL_SCHRABIDIUM, 25.0F, -2.8F, Rarity.RARE,
            tool -> tool.addAbility(IWeaponAbility.RADIATION, 0).addAbility(IToolAreaAbility.RECURSION, 6).addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 4).addAbility(IToolHarvestAbility.SMELTER, 0).addAbility(IToolHarvestAbility.SHREDDER, 0).addAbility(IWeaponAbility.BEHEADER, 0));
    public static final DeferredItem<Item> SCHRABIDIUM_SHOVEL = registerShovel("schrabidium_shovel", NtmTiers.TOOL_SCHRABIDIUM, 15.0F, -2.8F, Rarity.RARE,
            tool -> tool.addAbility(IWeaponAbility.RADIATION, 0).addAbility(IToolAreaAbility.RECURSION, 6).addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 4).addAbility(IToolHarvestAbility.SMELTER, 0).addAbility(IToolHarvestAbility.SHREDDER, 0));
    public static final DeferredItem<Item> SCHRABIDIUM_HOE = registerHoe("schrabidium_hoe", NtmTiers.TOOL_SCHRABIDIUM, 0, -2.8F, Rarity.RARE);

    public static final DeferredItem<Item> MESE_PICKAXE = registerPickaxe("mese_pickaxe", NtmTiers.TOOL_ZERO_POWER, 35, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.HAMMER, 2).addAbility(IToolAreaAbility.HAMMER_FLAT, 2).addAbility(IToolAreaAbility.RECURSION, 2).addAbility(IToolHarvestAbility.CRYSTALLIZER, 0).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 5).addAbility(IToolAreaAbility.EXPLOSION, 3).addAbility(IWeaponAbility.STUN, 3).addAbility(IWeaponAbility.PHOSPHORUS, 0).addAbility(IWeaponAbility.BEHEADER, 0).setDepthRockBreaker());
    public static final DeferredItem<Item> MESE_AXE = registerAxe("mese_axe", NtmTiers.TOOL_ZERO_POWER, 75.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.HAMMER, 2).addAbility(IToolAreaAbility.HAMMER_FLAT, 2).addAbility(IToolAreaAbility.RECURSION, 2).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IToolHarvestAbility.LUCK, 5).addAbility(IToolAreaAbility.EXPLOSION, 3).addAbility(IWeaponAbility.STUN, 4).addAbility(IWeaponAbility.PHOSPHORUS, 1).addAbility(IWeaponAbility.BEHEADER, 0));

    public static final DeferredItem<Item> VOLCANIC_PICKAXE = registerPickaxe("volcanic_pickaxe", NtmTiers.TOOL_ZERO_POWER, 15, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolHarvestAbility.SMELTER, 0).addAbility(IToolHarvestAbility.LUCK, 2).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IWeaponAbility.FIRE, 0).addAbility(IWeaponAbility.VAMPIRE, 0).addAbility(IWeaponAbility.BEHEADER, 0).setDepthRockBreaker());
    public static final DeferredItem<Item> VOLCANIC_AXE = registerAxe("volcanic_axe", NtmTiers.TOOL_ZERO_POWER, 25.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolHarvestAbility.SMELTER, 0).addAbility(IToolHarvestAbility.LUCK, 2).addAbility(IToolHarvestAbility.SILK, 0).addAbility(IWeaponAbility.FIRE, 1).addAbility(IWeaponAbility.VAMPIRE, 1).addAbility(IWeaponAbility.BEHEADER, 0));

    public static final DeferredItem<Item> CHLOROPHYTE_PICKAXE = registerPickaxe("chlorophyte_pickaxe", NtmTiers.TOOL_ZERO_POWER, 20, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolHarvestAbility.LUCK, 3).addAbility(IToolHarvestAbility.CENTRIFUGE, 0).addAbility(IToolHarvestAbility.MERCURY, 0).addAbility(IWeaponAbility.STUN, 3).addAbility(IWeaponAbility.VAMPIRE, 2).addAbility(IWeaponAbility.BEHEADER, 0).setDepthRockBreaker());
    public static final DeferredItem<Item> CHLOROPHYTE_AXE = registerAxe("chlorophyte_axe", NtmTiers.TOOL_ZERO_POWER, 50.0F, -2.8F,
            tool -> tool.addAbility(IToolAreaAbility.HAMMER, 1).addAbility(IToolAreaAbility.HAMMER_FLAT, 1).addAbility(IToolAreaAbility.RECURSION, 1).addAbility(IToolHarvestAbility.LUCK, 3).addAbility(IWeaponAbility.STUN, 4).addAbility(IWeaponAbility.VAMPIRE, 3).addAbility(IWeaponAbility.BEHEADER, 0));

    // Energy Drinks
    public static final DeferredItem<Item> DRINK = ITEMS.register("drink", () -> new DrinkItem(new Item.Properties()));
    /* Die Brechstange: im Original ein Stahlschwert mit eigenem Bild (ModItems.java:4284).
     * Sie ist der einzige Weg, eine Beutekiste zu oeffnen. */
    public static final DeferredItem<Item> CROWBAR = ITEMS.register(
            "crowbar",
            () -> new SwordItem(
                    NtmTiers.STEEL,
                    new Item.Properties()
                            .stacksTo(1)
                            .attributes(SwordItem.createAttributes(NtmTiers.STEEL, 3, -2.4F))
            )
    );

    public static final DeferredItem<Item> BOTTLE_OPENER = ITEMS.register(
            "bottle_opener",
            () -> new SpecialSwordItem(
                    NtmTiers.BOTTLE_OPENER,
                    new Item.Properties()
                            .stacksTo(1)
                            .attributes(SwordItem.createAttributes(NtmTiers.BOTTLE_OPENER, 3, -2.4F))
            ).setHurtEnemy(SpecialSwordItem.LAMBDA_OPENER_HURT_ENEMY)
    );

    /* ------------------------------------------------------------------------------------
     * Die vier Stecker des Zyklotrons (Runde 163).
     *
     * Das Zyklotron hat vier Sockel; steckt in jedem der passende Gegenstand, laeuft es mit
     * vollem Zierat. Balefire-Pulver stand schon, die drei anderen kommen hier dazu.
     * ---------------------------------------------------------------------------------- */

    /* Das Buch oeffnet beim Rechtsklick seine eigene Werkbank mit vier Plaetzen, auf der die
     * Rezepte aus MagicRecipes liegen (Runde 170). */
    public static final DeferredItem<Item> BOOK_OF_ = ITEMS.register("book_of_", () -> new BlackBookItem(new Item.Properties().stacksTo(1)));

    /* "Deals as much damage as it needs to": der Hammer nimmt dem Ziel ein Drittel seiner
     * hoechsten Lebenspunkte ab, egal wie viel das ist. */
    public static final DeferredItem<Item> DIAMOND_GAVEL = ITEMS.register(
            "diamond_gavel",
            () -> new SpecialSwordItem(
                    Tiers.DIAMOND,
                    new Item.Properties()
                            .stacksTo(1)
                            .attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4F))
            ).setHurtEnemy(SpecialSwordItem.LAMBDA_GAVEL_HURT_ENEMY)
    );

    /* Die beiden Vorstufen des Diamanthammers (Runde 170). Ohne sie ist der Diamanthammer
     * im Ueberlebensmodus nicht zu bekommen: sein Rezept verlangt den Bleihammer. */
    public static final DeferredItem<Item> WOOD_GAVEL = ITEMS.register(
            "wood_gavel",
            () -> new SpecialSwordItem(
                    Tiers.WOOD,
                    new Item.Properties()
                            .stacksTo(1)
                            .attributes(SwordItem.createAttributes(Tiers.WOOD, 3, -2.4F))
            ).setHurtEnemy(SpecialSwordItem.LAMBDA_GAVEL_WOOD_HURT_ENEMY)
    );

    public static final DeferredItem<Item> LEAD_GAVEL = ITEMS.register(
            "lead_gavel",
            () -> new SpecialSwordItem(
                    NtmTiers.STEEL,
                    new Item.Properties()
                            .stacksTo(1)
                            .attributes(SwordItem.createAttributes(NtmTiers.STEEL, 3, -2.4F))
            ).setHurtEnemy(SpecialSwordItem.LAMBDA_GAVEL_LEAD_HURT_ENEMY)
    );

    /* Die Schrotkugeln aus dem Rezept des Bleihammers. Das Original nimmt fuer sie die
     * Textur pellets_lead. */
    public static final DeferredItem<Item> PELLET_BUCKSHOT = ITEMS.register("pellet_buckshot", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> COIN_MASKMAN = ITEMS.register("coin_maskman", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // Canned Food
    public static final DeferredItem<Item> CANNED_CONSERVE = ITEMS.register("canned_conserve", () -> new ConserveItem(new Item.Properties()));
    public static final DeferredItem<Item> PUDDING = ITEMS.register("pudding", () -> new Item(new Item.Properties().food(NtmFoods.PUDDING)));

    /* Die Granaten. Vier Bauteile einzeln, dazu die zusammengesetzte Granate selbst. */
    public static final DeferredItem<Item> GRENADE_SHELL = ITEMS.register("grenade_shell", () -> new GrenadeShellItem(new Item.Properties()));
    public static final DeferredItem<Item> GRENADE_FILLING = ITEMS.register("grenade_filling", () -> new GrenadeFillingItem(new Item.Properties()));
    public static final DeferredItem<Item> GRENADE_FUZE = ITEMS.register("grenade_fuze", () -> new GrenadeFuzeItem(new Item.Properties()));
    public static final DeferredItem<Item> GRENADE_EXTRA = ITEMS.register("grenade_extra", () -> new GrenadeExtraItem(new Item.Properties()));
    public static final DeferredItem<Item> GRENADE_UNIVERSAL = ITEMS.register("grenade_universal", () -> new GrenadeUniversalItem(new Item.Properties()));

    /* Der Munitionsbehaelter der Nachschubkiste. */
    public static final DeferredItem<Item> AMMO_CONTAINER = ITEMS.register("ammo_container", () -> new AmmoContainerItem(new Item.Properties()));

    /* Zwei Sonderstuecke der roten Kiste. */
    public static final DeferredItem<Item> MYSTERYSHOVEL = ITEMS.register("mysteryshovel", () -> new MysteryShovelItem(new Item.Properties()));
    public static final DeferredItem<Item> FLAME_PONY = ITEMS.register("flame_pony", () -> new LoreItem(new Item.Properties()));

    // Money
    public static final DeferredItem<Item> CAP = ITEMS.register("cap", () -> new EnumMultiItem(new Item.Properties(), CapType.class, true, true));
    public static final DeferredItem<Item> RING_PULL = ITEMS.register("ring_pull", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CAN_KEY = ITEMS.register("can_key", () -> new Item(new Item.Properties()));

    // Chaos
    public static final DeferredItem<Item> CHOCOLATE_MILK = ITEMS.register("chocolate_milk", () -> new EnergyItem(new Item.Properties()));
    public static final DeferredItem<Item> CIGARETTE = ITEMS.register("cigarette", () -> new CigaretteItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> CRACKPIPE = ITEMS.register("crackpipe", () -> new CigaretteItem(new Item.Properties().stacksTo(1)));

    // High Explosive Lenses
    public static final DeferredItem<Item> EARLY_EXPLOSIVE_LENSES = ITEMS.register("early_explosive_lenses", () -> new LoreItem(new Item.Properties()));
    public static final DeferredItem<Item> EXPLOSIVE_LENSES = ITEMS.register("explosive_lenses", () -> new LoreItem(new Item.Properties()));

    // The Gadget
    public static final DeferredItem<Item> GADGET_WIREING = ITEMS.register("gadget_wireing", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> GADGET_CORE =    ITEMS.register("gadget_core",    () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    // Little Boy
    public static final DeferredItem<Item> LITTLE_BOY_SHIELDING =  ITEMS.register("little_boy_shielding",  () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LITTLE_BOY_TARGET =     ITEMS.register("little_boy_target",     () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> LITTLE_BOY_BULLET =     ITEMS.register("little_boy_bullet",     () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> LITTLE_BOY_PROPELLANT = ITEMS.register("little_boy_propellant", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LITTLE_BOY_IGNITER =    ITEMS.register("little_boy_igniter",    () -> new Item(new Item.Properties().stacksTo(1)));

    // Fat Man
    public static final DeferredItem<Item> FAT_MAN_IGNITER = ITEMS.register("fat_man_igniter", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> FAT_MAN_CORE =    ITEMS.register("fat_man_core",    () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    /* Der Daemonenkern. Offen wird er von einem Schraubenzieher gehalten; faellt er zu
     * Boden, rutscht der heraus und der Kern schliesst sich. Werte aus ModItems.java:2692. */
    public static final DeferredItem<Item> DEMON_CORE_OPEN = ITEMS.register("demon_core_open", () -> new DemonCoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> DEMON_CORE_CLOSED = ITEMS.register("demon_core_closed", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    // Ivy Mike
    public static final DeferredItem<Item> IVY_MIKE_CORE =         ITEMS.register("ivy_mike_core",         () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> IVY_MIKE_DEUT =         ITEMS.register("ivy_mike_deut",         () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> IVY_MIKE_COOLING_UNIT = ITEMS.register("ivy_mike_cooling_unit", () -> new Item(new Item.Properties().stacksTo(1)));

    // Tsar Bomba
    public static final DeferredItem<Item> TSAR_BOMBA_CORE = ITEMS.register("tsar_bomba_core", () -> new Item(new Item.Properties().stacksTo(1)));

    // FLEIJA
    public static final DeferredItem<Item> FLEIJA_IGNITER =    ITEMS.register("fleija_igniter",    () -> new UsedInItem(new Item.Properties().stacksTo(1), List.of(NtmBlocks.NUKE_FLEIJA.get())));
    public static final DeferredItem<Item> FLEIJA_PROPELLANT = ITEMS.register("fleija_propellant", () -> new UsedInItem(new Item.Properties().stacksTo(1), List.of(NtmBlocks.NUKE_FLEIJA.get())));
    public static final DeferredItem<Item> FLEIJA_CORE =       ITEMS.register("fleija_core",       () -> new UsedInItem(new Item.Properties().stacksTo(1), List.of(NtmBlocks.NUKE_FLEIJA.get())));

    // Solinium
    public static final DeferredItem<Item> SOLINIUM_IGNITER =    ITEMS.register("solinium_igniter",    () -> new UsedInItem(new Item.Properties().stacksTo(1), List.of(NtmBlocks.NUKE_SOLINIUM.get())));
    public static final DeferredItem<Item> SOLINIUM_PROPELLANT = ITEMS.register("solinium_propellant", () -> new UsedInItem(new Item.Properties().stacksTo(1), List.of(NtmBlocks.NUKE_SOLINIUM.get())));
    public static final DeferredItem<Item> SOLINIUM_CORE =       ITEMS.register("solinium_core",       () -> new UsedInItem(new Item.Properties().stacksTo(1), List.of(NtmBlocks.NUKE_SOLINIUM.get())));

    // N2
    public static final DeferredItem<Item> N2_CHARGE = ITEMS.register("n2_charge", () -> new UsedInItem(new Item.Properties().stacksTo(1), List.of(NtmBlocks.NUKE_N2.get())));

    // FSTBMB
    public static final DeferredItem<Item> EGG_BALEFIRE_SHARD = ITEMS.register("egg_balefire_shard", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> EGG_BALEFIRE = ITEMS.register("egg_balefire", () -> new Item(new Item.Properties().stacksTo(1)));

    // Nobody will ever read this anyway, so it shouldn't matter.
    public static final DeferredItem<Item> IGNITER = ITEMS.register("igniter", () -> new LoreItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DETONATOR = ITEMS.register("detonator", () -> new DetonatorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DETONATOR_MULTI = ITEMS.register("detonator_multi", () -> new MultiDetonatorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DETONATOR_LASER = ITEMS.register("detonator_laser", () -> new LaserDetonatorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DETONATOR_DEADMAN = ITEMS.register("detonator_deadman", () -> new DangerousDropItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DETONATOR_DE = ITEMS.register("detonator_de", () -> new DangerousDropItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> BOMB_CALLER = ITEMS.register("bomb_caller", () -> new BombCallerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DEFUSER = ITEMS.register("defuser", () -> new ToolingItem(ToolType.DEFUSER, new Item.Properties().durability(100)));
    public static final DeferredItem<Item> REACHER = ITEMS.register("reacher", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MELTDOWN_TOOL = ITEMS.register("meltdown_tool", () -> new DyatlovItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MIRROR_TOOL = ITEMS.register("mirror_tool", () -> new MirrorToolItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> WIRING_TOOL = ITEMS.register("wiring_tool", () -> new WiringToolItem(new Item.Properties().stacksTo(1)));

    /*
     * Runde 104: die vier Bandstaebe. Im Original ist das EIN Gegenstand mit vier
     * Metadaten-Werten; im Port sind es vier eigene, wie schon bei den Maschinenaufwertungen.
     */
    /* Runde 111: das Verbindungsstueck des Teleporters. */
    public static final DeferredItem<Item> TELE_LINK = ITEMS.register("tele_link", () -> new TeleLinkItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> CONVEYOR_WAND = ITEMS.register("conveyor_wand", () -> new ConveyorWandItem(new Item.Properties(), ConveyorWandItem.ConveyorType.REGULAR));
    public static final DeferredItem<Item> CONVEYOR_WAND_EXPRESS = ITEMS.register("conveyor_wand_express", () -> new ConveyorWandItem(new Item.Properties(), ConveyorWandItem.ConveyorType.EXPRESS));
    public static final DeferredItem<Item> CONVEYOR_WAND_DOUBLE = ITEMS.register("conveyor_wand_double", () -> new ConveyorWandItem(new Item.Properties(), ConveyorWandItem.ConveyorType.DOUBLE));
    public static final DeferredItem<Item> CONVEYOR_WAND_TRIPLE = ITEMS.register("conveyor_wand_triple", () -> new ConveyorWandItem(new Item.Properties(), ConveyorWandItem.ConveyorType.TRIPLE));
    public static final DeferredItem<Item> PISTON_SET = ITEMS.register("piston_set", () -> new PistonsItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DRILL_TITANIUM = ITEMS.register("drill_titanium", () -> new Item(new Item.Properties()));

    // Wands, Tools, Other Crap
    public static final DeferredItem<Item> POLAROID = ITEMS.register("polaroid", () -> new PolaroidItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> BURNT_BARK = ITEMS.register("burnt_bark", () -> new LoreItem(new Item.Properties()));
    public static final DeferredItem<Item> PLAN_C = ITEMS.register("plan_c", () -> new Item(new Item.Properties()));

    // Kits
    public static final DeferredItem<Item> STARTER_KIT = ITEMS.register("starter_kit", () -> new StarterKitItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TEMPLATE_FOLDER = ITEMS.register("template_folder", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NOTHING = ITEMS.register("nothing", () -> new Item(new Item.Properties()));


    // ...
    public static final DeferredItem<Item> CASING = ITEMS.register("casing", () -> new EnumMultiItem(new Item.Properties(), CasingType.class, true, true));
    public static DeferredItem<Item> AMMO_DEBUG;
    public static DeferredItem<Item> AMMO_STANDARD;
    public static DeferredItem<Item> AMMO_SHELL;
    public static DeferredItem<Item> AMMO_DGK;
    public static DeferredItem<Item> AMMO_SECRET;
    public static DeferredItem<Item> WEAPON_MOD_GENERIC;
    public static DeferredItem<Item> WEAPON_MOD_SPECIAL;
    public static DeferredItem<Item> WEAPON_MOD_CALIBER;

    public static DeferredItem<Item> GUN_DEBUG;
    public static DeferredItem<Item> GUN_MARESLEG;
    public static DeferredItem<Item> GUN_SPAS12;
    public static DeferredItem<Item> GUN_AUTOSHOTGUN;
    public static DeferredItem<Item> GUN_AUTOSHOTGUN_SHREDDER;
    public static DeferredItem<Item> GUN_AUTOSHOTGUN_SEXY;
    public static DeferredItem<Item> GUN_LIBERATOR;
    public static DeferredItem<Item> GUN_HEAVY_REVOLVER_LILMAC;
    public static DeferredItem<Item> GUN_HEAVY_REVOLVER_PROTEGE;
    public static DeferredItem<Item> GUN_HANGMAN;
    public static DeferredItem<Item> GUN_N_I_4_N_I;
    public static DeferredItem<Item> GUN_HENRY;
    public static DeferredItem<Item> GUN_DRILL;
    public static DeferredItem<Item> GUN_TESLA_CANNON;
    public static DeferredItem<Item> GUN_FOLLY;
    public static DeferredItem<Item> GUN_LASER_PISTOL;
    public static DeferredItem<Item> GUN_LASER_PISTOL_PEW_PEW;
    public static DeferredItem<Item> GUN_LASER_PISTOL_MORNING_GLORY;
    public static DeferredItem<Item> GUN_LASRIFLE;
    public static DeferredItem<Item> GUN_CHEMTHROWER;
    public static DeferredItem<Item> GUN_FLAMER;
    public static DeferredItem<Item> GUN_FLAMER_TOPAZ;
    public static DeferredItem<Item> GUN_FLAMER_DAYBREAKER;
    public static DeferredItem<Item> GUN_HENRY_LINCOLN;
    public static DeferredItem<Item> GUN_HEAVY_REVOLVER;
    public static DeferredItem<Item> GUN_GREASEGUN;
    public static DeferredItem<Item> GUN_PEPPERBOX;
    public static DeferredItem<Item> GUN_LIGHT_REVOLVER;
    public static DeferredItem<Item> GUN_LIGHT_REVOLVER_ATLAS;
    public static DeferredItem<Item> GUN_AM180;
    public static DeferredItem<Item> GUN_STAR_F;
    public static DeferredItem<Item> GUN_STAR_F_AKIMBO;
    public static DeferredItem<Item> GUN_MARESLEG_AKIMBO;
    public static DeferredItem<Item> GUN_MARESLEG_BROKEN;
    public static DeferredItem<Item> GUN_LIGHT_REVOLVER_DANI;
    public static DeferredItem<Item> GUN_UZI;
    public static DeferredItem<Item> GUN_UZI_AKIMBO;
    public static DeferredItem<Item> GUN_AMAT;
    public static DeferredItem<Item> GUN_AMAT_SUBTLETY;
    public static DeferredItem<Item> GUN_AMAT_PENANCE;
    public static DeferredItem<Item> GUN_M2;
    public static DeferredItem<Item> GUN_G3;
    public static DeferredItem<Item> GUN_G3_ZEBRA;
    public static DeferredItem<Item> GUN_STG77;
    public static DeferredItem<Item> GUN_CARBINE;
    public static DeferredItem<Item> GUN_MAS36;
    public static DeferredItem<Item> GUN_MINIGUN;
    public static DeferredItem<Item> GUN_MINIGUN_DUAL;
    public static DeferredItem<Item> GUN_DOUBLE_BARREL;
    public static DeferredItem<Item> GUN_DOUBLE_BARREL_SACRED_DRAGON;
    public static DeferredItem<Item> GUN_BOLTER;
    public static DeferredItem<Item> GUN_ABERRATOR;
    public static DeferredItem<Item> GUN_ABERRATOR_EOTT;
    public static DeferredItem<Item> GUN_FLAREGUN;
    public static DeferredItem<Item> GUN_CONGOLAKE;
    public static DeferredItem<Item> GUN_MK108;
    public static DeferredItem<Item> GUN_PANZERSCHRECK;

    public static final DeferredItem<Item> NO9 = ITEMS.register("no9", () -> new ArmorNo9(ArmorMaterials.IRON));

    /* ---- Schutzkleidung: Schutzanzug, Gasmasken, Filter ---------------------------- */

    /* Das Tuch ist zugleich Baustoff und Reparaturmaterial des jeweiligen Anzugs. */
    public static final DeferredItem<Item> HAZMAT_CLOTH = ITEMS.register("hazmat_cloth", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> HAZMAT_CLOTH_RED = ITEMS.register("hazmat_cloth_red", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> HAZMAT_CLOTH_GREY = ITEMS.register("hazmat_cloth_grey", () -> new Item(new Item.Properties()));

    /* Der Lappen und seine beiden Zustaende. Nass wird er von selbst, wenn er im
     * Wasser liegt; der andere Weg ist ein Rechtsklick. */
    public static final DeferredItem<Item> RAG = ITEMS.register("rag", () -> new RagItem(new Item.Properties()));
    public static final DeferredItem<Item> RAG_DAMP = ITEMS.register("rag_damp", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAG_PISS = ITEMS.register("rag_piss", () -> new Item(new Item.Properties()));

    /* Die Haube des gelben Anzugs traegt ein Filtergewinde und einen Sichtvorsatz.
     * Die Haube schraenkt den Filter nicht ein -- sie sitzt ueber dem ganzen Kopf. */
    public static final DeferredItem<Item> HAZMAT_HELMET = ITEMS.register("hazmat_helmet", () -> new GasMaskItem(NtmArmorMaterials.HAZMAT, hazmatProperties(ArmorItem.Type.HELMET), List.of(), GasMaskItem.OVERLAY_HAZMAT));
    public static final DeferredItem<Item> HAZMAT_PLATE = ITEMS.register("hazmat_plate", () -> new ArmorItem(NtmArmorMaterials.HAZMAT, ArmorItem.Type.CHESTPLATE, hazmatProperties(ArmorItem.Type.CHESTPLATE)));
    public static final DeferredItem<Item> HAZMAT_LEGS = ITEMS.register("hazmat_legs", () -> new ArmorItem(NtmArmorMaterials.HAZMAT, ArmorItem.Type.LEGGINGS, hazmatProperties(ArmorItem.Type.LEGGINGS)));
    public static final DeferredItem<Item> HAZMAT_BOOTS = ITEMS.register("hazmat_boots", () -> new ArmorItem(NtmArmorMaterials.HAZMAT, ArmorItem.Type.BOOTS, hazmatProperties(ArmorItem.Type.BOOTS)));

    /* Rot und grau tragen im Original ein eigenes Kopfmodell und haben deshalb
     * keinen Sichtvorsatz. */
    public static final DeferredItem<Item> HAZMAT_HELMET_RED = ITEMS.register("hazmat_helmet_red", () -> new GasMaskItem(NtmArmorMaterials.HAZMAT_RED, hazmatProperties(ArmorItem.Type.HELMET), List.of()));
    public static final DeferredItem<Item> HAZMAT_PLATE_RED = ITEMS.register("hazmat_plate_red", () -> new ArmorItem(NtmArmorMaterials.HAZMAT_RED, ArmorItem.Type.CHESTPLATE, hazmatProperties(ArmorItem.Type.CHESTPLATE)));
    public static final DeferredItem<Item> HAZMAT_LEGS_RED = ITEMS.register("hazmat_legs_red", () -> new ArmorItem(NtmArmorMaterials.HAZMAT_RED, ArmorItem.Type.LEGGINGS, hazmatProperties(ArmorItem.Type.LEGGINGS)));
    public static final DeferredItem<Item> HAZMAT_BOOTS_RED = ITEMS.register("hazmat_boots_red", () -> new ArmorItem(NtmArmorMaterials.HAZMAT_RED, ArmorItem.Type.BOOTS, hazmatProperties(ArmorItem.Type.BOOTS)));

    public static final DeferredItem<Item> HAZMAT_HELMET_GREY = ITEMS.register("hazmat_helmet_grey", () -> new GasMaskItem(NtmArmorMaterials.HAZMAT_GREY, hazmatProperties(ArmorItem.Type.HELMET), List.of()));
    public static final DeferredItem<Item> HAZMAT_PLATE_GREY = ITEMS.register("hazmat_plate_grey", () -> new ArmorItem(NtmArmorMaterials.HAZMAT_GREY, ArmorItem.Type.CHESTPLATE, hazmatProperties(ArmorItem.Type.CHESTPLATE)));
    public static final DeferredItem<Item> HAZMAT_LEGS_GREY = ITEMS.register("hazmat_legs_grey", () -> new ArmorItem(NtmArmorMaterials.HAZMAT_GREY, ArmorItem.Type.LEGGINGS, hazmatProperties(ArmorItem.Type.LEGGINGS)));
    public static final DeferredItem<Item> HAZMAT_BOOTS_GREY = ITEMS.register("hazmat_boots_grey", () -> new ArmorItem(NtmArmorMaterials.HAZMAT_GREY, ArmorItem.Type.BOOTS, hazmatProperties(ArmorItem.Type.BOOTS)));

    /* Der PAA-Anzug: dieselbe Haube, aber gepanzert und langlebiger. */
    public static final DeferredItem<Item> HAZMAT_PAA_HELMET = ITEMS.register("hazmat_paa_helmet", () -> new GasMaskItem(NtmArmorMaterials.HAZMAT_PAA, paaProperties(ArmorItem.Type.HELMET), List.of(), GasMaskItem.OVERLAY_HAZMAT));
    public static final DeferredItem<Item> HAZMAT_PAA_PLATE = ITEMS.register("hazmat_paa_plate", () -> new ArmorItem(NtmArmorMaterials.HAZMAT_PAA, ArmorItem.Type.CHESTPLATE, paaProperties(ArmorItem.Type.CHESTPLATE)));
    public static final DeferredItem<Item> HAZMAT_PAA_LEGS = ITEMS.register("hazmat_paa_legs", () -> new ArmorItem(NtmArmorMaterials.HAZMAT_PAA, ArmorItem.Type.LEGGINGS, paaProperties(ArmorItem.Type.LEGGINGS)));
    public static final DeferredItem<Item> HAZMAT_PAA_BOOTS = ITEMS.register("hazmat_paa_boots", () -> new ArmorItem(NtmArmorMaterials.HAZMAT_PAA, ArmorItem.Type.BOOTS, paaProperties(ArmorItem.Type.BOOTS)));

    /* Die Masken. Alle bis auf die Monoxidmaske lassen aetzende Gase durch, weil die
     * auch die Haut angreifen; die Monoxidmaske ist nur ein Kohlefilter. */
    public static final DeferredItem<Item> GAS_MASK = ITEMS.register("gas_mask", () -> new GasMaskItem(NtmArmorMaterials.MASK, maskProperties(), GasMaskItem.standardBlacklist(), GasMaskItem.OVERLAY_GASMASK));
    public static final DeferredItem<Item> GAS_MASK_M65 = ITEMS.register("gas_mask_m65", () -> new GasMaskItem(NtmArmorMaterials.MASK, maskProperties(), GasMaskItem.standardBlacklist(), GasMaskItem.OVERLAY_GOGGLES));
    public static final DeferredItem<Item> GAS_MASK_MONO = ITEMS.register("gas_mask_mono", () -> new GasMaskItem(NtmArmorMaterials.MASK, maskProperties(), GasMaskItem.monoxideBlacklist()));
    public static final DeferredItem<Item> GAS_MASK_OLDE = ITEMS.register("gas_mask_olde", () -> new GasMaskItem(NtmArmorMaterials.MASK, maskProperties(), GasMaskItem.standardBlacklist()));

    /* Lappen vor dem Gesicht: kein Gewinde, kein Filter, nur der Schutz, den der
     * Lappen selbst mitbringt. */
    public static final DeferredItem<Item> MASK_RAG = ITEMS.register("mask_rag", () -> new ArmorItem(NtmArmorMaterials.RAGS, ArmorItem.Type.HELMET, ragsProperties()));
    public static final DeferredItem<Item> MASK_PISS = ITEMS.register("mask_piss", () -> new ArmorItem(NtmArmorMaterials.RAGS, ArmorItem.Type.HELMET, ragsProperties()));

    public static final DeferredItem<Item> GAS_MASK_FILTER = ITEMS.register("gas_mask_filter", () -> new FilterItem(new Item.Properties()));
    public static final DeferredItem<Item> GAS_MASK_FILTER_MONO = ITEMS.register("gas_mask_filter_mono", () -> new FilterItem(new Item.Properties()));
    public static final DeferredItem<Item> GAS_MASK_FILTER_COMBO = ITEMS.register("gas_mask_filter_combo", () -> new FilterItem(new Item.Properties()));
    public static final DeferredItem<Item> GAS_MASK_FILTER_RAG = ITEMS.register("gas_mask_filter_rag", () -> new FilterItem(new Item.Properties()));
    public static final DeferredItem<Item> GAS_MASK_FILTER_PISS = ITEMS.register("gas_mask_filter_piss", () -> new FilterItem(new Item.Properties()));

    /*
     * Der HEV-Anzug. Die vier Zahlen stehen genauso in ModItemsArmor: eine Million HE
     * Fassungsvermoegen, zehntausend Ladegeschwindigkeit, 2500 HE je Schadenspunkt und
     * null Dauerverbrauch -- der Anzug zehrt also nicht von selbst, er zahlt nur, wenn
     * er etwas abbekommt.
     *
     * ABWEICHUNG: das Original schreibt die Eigenschaften nur am Helm aus und laesst die
     * drei anderen Teile sie mit cloneStats vom fertigen Helm abschreiben. Das setzt eine
     * Reihenfolge voraus -- der Helm muss schon angemeldet sein. Der Port baut alle vier
     * aus derselben Vorschrift, damit bleibt die Anmeldung reihenfolgefrei.
     */
    public static final DeferredItem<Item> HEV_HELMET = ITEMS.register("hev_helmet", () -> hev(ArmorItem.Type.HELMET));
    public static final DeferredItem<Item> HEV_PLATE = ITEMS.register("hev_plate", () -> hev(ArmorItem.Type.CHESTPLATE));
    public static final DeferredItem<Item> HEV_LEGS = ITEMS.register("hev_legs", () -> hev(ArmorItem.Type.LEGGINGS));
    public static final DeferredItem<Item> HEV_BOOTS = ITEMS.register("hev_boots", () -> hev(ArmorItem.Type.BOOTS));

    private static ArmorFSBItem hev(ArmorItem.Type type) {
        return new ArmorHEVItem(NtmArmorMaterials.HEV, type, new Item.Properties().durability(type.getDurability(NtmArmorMaterials.DURABILITY_HEV)), 1_000_000, 10_000, 2_500, 0)
                .addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1))
                .addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 0))
                .setHasGeigerSound(true)
                .setHasCustomGeiger(true);
    }

    /*
     * Die Metallspritzen. Jede traegt ihre Wirkung selbst, statt wie im Original von einer
     * Kette aus Identitaetsabfragen bedient zu werden -- siehe SyringeItem.
     */
    public static final DeferredItem<Item> SYRINGE_EMPTY = ITEMS.register("syringe_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SYRINGE_METAL_EMPTY = ITEMS.register("syringe_metal_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SYRINGE_ANTIDOTE = ITEMS.register("syringe_antidote", () -> SyringeItem.antidote(new Item.Properties()));
    public static final DeferredItem<Item> SYRINGE_METAL_STIMPAK = ITEMS.register("syringe_metal_stimpak", () -> SyringeItem.stimpak(new Item.Properties()));
    public static final DeferredItem<Item> SYRINGE_METAL_MEDX = ITEMS.register("syringe_metal_medx", () -> SyringeItem.medx(new Item.Properties()));
    public static final DeferredItem<Item> SYRINGE_METAL_PSYCHO = ITEMS.register("syringe_metal_psycho", () -> SyringeItem.psycho(new Item.Properties()));
    public static final DeferredItem<Item> SYRINGE_METAL_SUPER = ITEMS.register("syringe_metal_super", () -> SyringeItem.superStimpak(new Item.Properties()));

    /*
     * Die Radaway-Familie und die vier Beutel. Im Original sind sie keine Spritzen, sondern
     * ItemSimpleConsumable -- sie lassen einen Behaelter zurueck, statt eine leere Huelle,
     * und kennen keine Uebelkeitssperre.
     *
     * DER RADAWAY-EFFEKT LAG SCHON IM PORT und hatte keine Quelle: nichts hat ihn je
     * ausgeloest. Diese sieben Gegenstaende sind sie.
     *
     * Die Zahlen sind die des Originals. Der Hinweis des Originals zu radaway_flush nennt
     * 1.000 RAD, die Dauer gibt aber 500 Ticks her -- der Wortlaut ist uebernommen wie er
     * dort steht, samt dieser Unstimmigkeit.
     */
    public static final DeferredItem<Item> IV_EMPTY = ITEMS.register("iv_empty", () -> SimpleConsumableItem.blutbeutelLeer(new Item.Properties()));
    public static final DeferredItem<Item> IV_BLOOD = ITEMS.register("iv_blood", () -> SimpleConsumableItem.blutbeutelVoll(new Item.Properties()));
    public static final DeferredItem<Item> IV_XP_EMPTY = ITEMS.register("iv_xp_empty", () -> SimpleConsumableItem.erfahrungsbeutelLeer(new Item.Properties()));
    public static final DeferredItem<Item> IV_XP = ITEMS.register("iv_xp", () -> SimpleConsumableItem.erfahrungsbeutelVoll(new Item.Properties()));
    public static final DeferredItem<Item> RADAWAY = ITEMS.register("radaway", () -> SimpleConsumableItem.radaway(new Item.Properties(), 140, "desc.item.radaway"));
    public static final DeferredItem<Item> RADAWAY_STRONG = ITEMS.register("radaway_strong", () -> SimpleConsumableItem.radaway(new Item.Properties(), 350, "desc.item.radaway_strong"));
    public static final DeferredItem<Item> RADAWAY_FLUSH = ITEMS.register("radaway_flush", () -> SimpleConsumableItem.radaway(new Item.Properties(), 500, "desc.item.radaway_flush"));

    /** Der Sanitaetsbeutel. Im Original eine Spritze, keine Konserve -- er laesst nichts zurueck. */
    public static final DeferredItem<Item> MED_BAG = ITEMS.register("med_bag", () -> SyringeItem.medBag(new Item.Properties()));

    /** Die Jodtablette. Dieselben neun Wirkungen wie der Sanitaetsbeutel, ohne die Heilung. */
    public static final DeferredItem<Item> PILL_IODINE = ITEMS.register("pill_iodine", () -> PillItem.jod(new Item.Properties()));
    /** Die Feldration der C-130. Im Original nur ein ItemLemon(3, 0.5F) ohne weitere Wirkung. */
    public static final DeferredItem<Item> DEFINITELYFOOD = ITEMS.register("definitelyfood", () -> new Item(new Item.Properties().food(NtmFoods.DEFINITELY_FOOD)));

    /** Der Kronkorken. Im Original das Zahlungsmittel des Oedlands, hier vorerst nur Beute. */
    public static final DeferredItem<Item> CAP_NUKA = ITEMS.register("cap_nuka", () -> new Item(new Item.Properties()));

    private static Item.Properties hazmatProperties(ArmorItem.Type type) {
        return new Item.Properties().durability(type.getDurability(NtmArmorMaterials.DURABILITY_HAZMAT));
    }

    private static Item.Properties paaProperties(ArmorItem.Type type) {
        return new Item.Properties().durability(type.getDurability(NtmArmorMaterials.DURABILITY_PAA));
    }

    private static Item.Properties maskProperties() {
        return new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(NtmArmorMaterials.DURABILITY_MASK));
    }

    private static Item.Properties ragsProperties() {
        return new Item.Properties().stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(NtmArmorMaterials.DURABILITY_RAGS));
    }

    /* ---- Ruestungsmodule: Auskleidungen und Einlagen ------------------------------- */

    /* Die Auskleidung sitzt im Verkleidungsplatz und mindert die Strahlung, die durch das
     * Teil kommt. Die Werte sind unveraendert aus dem Original. */
    public static final DeferredItem<Item> CLADDING_PAINT = ITEMS.register("cladding_paint", () -> new ItemModCladding(new Item.Properties(), 0.025D));
    public static final DeferredItem<Item> CLADDING_RUBBER = ITEMS.register("cladding_rubber", () -> new ItemModCladding(new Item.Properties(), 0.005D));
    public static final DeferredItem<Item> CLADDING_LEAD = ITEMS.register("cladding_lead", () -> new ItemModCladding(new Item.Properties(), 0.1D));
    public static final DeferredItem<Item> CLADDING_DESH = ITEMS.register("cladding_desh", () -> new ItemModCladding(new Item.Properties(), 0.2D));
    public static final DeferredItem<Item> CLADDING_GHIORSIUM = ITEMS.register("cladding_ghiorsium", () -> new ItemModCladding(new Item.Properties(), 0.5D));
    /* Die beiden Sonderfaelle: Eisen macht schwer, Obsidian macht das abgelegte Teil unzerstoerbar. */
    public static final DeferredItem<Item> CLADDING_IRON = ITEMS.register("cladding_iron", () -> new ItemModKnockback(new Item.Properties(), 0.5D));
    public static final DeferredItem<Item> CLADDING_OBSIDIAN = ITEMS.register("cladding_obsidian", () -> new ItemModIndestructible(new Item.Properties()));

    /* Die Einlagen sitzen im Kevlarplatz der Brustplatte. Die Reihenfolge der Zahlen ist
     * Haltbarkeit, Schaden, Geschoss, Sprengung, Tempo, Strahlung je Tick, Reaktivpanzerung. */
    public static final DeferredItem<Item> INSERT_KEVLAR = ITEMS.register("insert_kevlar", () -> new ItemModInsert(new Item.Properties(), 1500, 1F, 0.9F, 1F, 1F, 0F, false));
    public static final DeferredItem<Item> INSERT_SAPI = ITEMS.register("insert_sapi", () -> new ItemModInsert(new Item.Properties(), 1750, 1F, 0.85F, 1F, 1F, 0F, false));
    public static final DeferredItem<Item> INSERT_ESAPI = ITEMS.register("insert_esapi", () -> new ItemModInsert(new Item.Properties(), 2000, 0.95F, 0.8F, 1F, 1F, 0F, false));
    public static final DeferredItem<Item> INSERT_XSAPI = ITEMS.register("insert_xsapi", () -> new ItemModInsert(new Item.Properties(), 2500, 0.9F, 0.75F, 1F, 1F, 0F, false));
    public static final DeferredItem<Item> INSERT_STEEL = ITEMS.register("insert_steel", () -> new ItemModInsert(new Item.Properties(), 1000, 1F, 0.95F, 0.75F, 0.95F, 0F, false));
    public static final DeferredItem<Item> INSERT_DU = ITEMS.register("insert_du", () -> new ItemModInsert(new Item.Properties(), 1500, 0.9F, 0.85F, 0.5F, 0.9F, 0F, false));
    public static final DeferredItem<Item> INSERT_POLONIUM = ITEMS.register("insert_polonium", () -> new ItemModInsert(new Item.Properties(), 500, 0.9F, 1F, 0.95F, 0.9F, 100F, false));
    public static final DeferredItem<Item> INSERT_GHIORSIUM = ITEMS.register("insert_ghiorsium", () -> new ItemModInsert(new Item.Properties(), 2000, 0.8F, 0.75F, 0.35F, 0.9F, 0F, false));
    public static final DeferredItem<Item> INSERT_ERA = ITEMS.register("insert_era", () -> new ItemModInsert(new Item.Properties(), 25, 0.5F, 1F, 0.25F, 1F, 0F, true));
    public static final DeferredItem<Item> INSERT_YHARONITE = ITEMS.register("insert_yharonite", () -> new ItemModInsert(new Item.Properties(), 9999, 0.01F, 1F, 1F, 1F, 0F, false));
    public static final DeferredItem<Item> INSERT_DOXIUM = ITEMS.register("insert_doxium", () -> new ItemModInsert(new Item.Properties(), 9999, 5.0F, 1F, 1F, 1F, 0F, false));


    public static final DeferredItem<Item> PLATE_POLYMER = ITEMS.register("plate_polymer", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INSULATOR = PLATE_POLYMER;

    public static final DeferredItem<Item> INGOT_COPPER = ITEMS.register("ingot_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_ASTATINE = ITEMS.register("ingot_astatine", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_BROMINE = ITEMS.register("ingot_bromine", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_C4 = ITEMS.register("ingot_c4", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_CAESIUM = ITEMS.register("ingot_caesium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_CERIUM = ITEMS.register("ingot_cerium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_CHAINSTEEL = ITEMS.register("ingot_chainsteel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_DAFFERGON = ITEMS.register("ingot_daffergon", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_FIBERGLASS = ITEMS.register("ingot_fiberglass", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_I131 = ITEMS.register("ingot_i131", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_IODINE = ITEMS.register("ingot_iodine", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_METEORITE = ITEMS.register("ingot_meteorite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_METEORITE_FORGED = ITEMS.register("ingot_meteorite_forged", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_PHOSPHORUS = ITEMS.register("ingot_phosphorus", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_RAW = ITEMS.register("ingot_raw", () -> new RawIngotItem(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_RED_COPPER = ITEMS.register("ingot_red_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_REIIUM = ITEMS.register("ingot_reiium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_SEMTEX = ITEMS.register("ingot_semtex", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_STEEL_DUSTED = ITEMS.register("ingot_steel_dusted", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_TENNESSINE = ITEMS.register("ingot_tennessine", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_TH232 = ITEMS.register("ingot_th232", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_TUNGSTEN_CARBIDE = ITEMS.register("ingot_tungsten_carbide", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_UNOBTAINIUM = ITEMS.register("ingot_unobtainium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_VERTICIUM = ITEMS.register("ingot_verticium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_WEIDANIUM = ITEMS.register("ingot_weidanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_AUSTRALIUM = ITEMS.register("nugget_australium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_AUSTRALIUM_GREATER = ITEMS.register("nugget_australium_greater", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_AUSTRALIUM_LESSER = ITEMS.register("nugget_australium_lesser", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_DAFFERGON = ITEMS.register("nugget_daffergon", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_MERCURY = ITEMS.register("nugget_mercury", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_REIIUM = ITEMS.register("nugget_reiium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_STRONTIUM = ITEMS.register("nugget_strontium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_TH232 = ITEMS.register("nugget_th232", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_UNOBTAINIUM = ITEMS.register("nugget_unobtainium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_UNOBTAINIUM_GREATER = ITEMS.register("nugget_unobtainium_greater", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_UNOBTAINIUM_LESSER = ITEMS.register("nugget_unobtainium_lesser", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_VERTICIUM = ITEMS.register("nugget_verticium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NUGGET_WEIDANIUM = ITEMS.register("nugget_weidanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_ARMOR_AJR = ITEMS.register("plate_armor_ajr", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_ARMOR_DNT = ITEMS.register("plate_armor_dnt", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_ARMOR_FAU = ITEMS.register("plate_armor_fau", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_ARMOR_HEV = ITEMS.register("plate_armor_hev", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_ARMOR_LUNAR = ITEMS.register("plate_armor_lunar", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_ARMOR_TITANIUM = ITEMS.register("plate_armor_titanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_DALEKANIUM = ITEMS.register("plate_dalekanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_DESH = ITEMS.register("plate_desh", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_DINEUTRONIUM = ITEMS.register("plate_dineutronium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_EUPHEMIUM = ITEMS.register("plate_euphemium", () -> new Item(new Item.Properties()));
    /* Die sieben Brennstoffplatten des Forschungsreaktors. Lebensdauer, Kennlinie und
     * Reaktivitaet unveraendert aus dem Original. */
    public static final DeferredItem<Item> PLATE_FUEL_MOX = ITEMS.register("plate_fuel_mox", () -> new PlateFuelItem(2_400_000, FunctionType.LOGARITHM, 50, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> PLATE_FUEL_PU238BE = ITEMS.register("plate_fuel_pu238be", () -> new PlateFuelItem(1_000_000, FunctionType.PASSIVE, 50, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> PLATE_FUEL_PU239 = ITEMS.register("plate_fuel_pu239", () -> new PlateFuelItem(2_000_000, FunctionType.NEGATIVE_QUADRATIC, 50, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> PLATE_FUEL_RA226BE = ITEMS.register("plate_fuel_ra226be", () -> new PlateFuelItem(1_300_000, FunctionType.PASSIVE, 30, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> PLATE_FUEL_SA326 = ITEMS.register("plate_fuel_sa326", () -> new PlateFuelItem(2_000_000, FunctionType.LINEAR, 80, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> PLATE_FUEL_U233 = ITEMS.register("plate_fuel_u233", () -> new PlateFuelItem(2_200_000, FunctionType.SQUARE_ROOT, 50, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> PLATE_FUEL_U235 = ITEMS.register("plate_fuel_u235", () -> new PlateFuelItem(2_200_000, FunctionType.SQUARE_ROOT, 40, new Item.Properties().stacksTo(1)));

    /** Der Reaktorfuehler: einmal auf einen Forschungsreaktor geklickt, merkt er sich dessen Ort. */
    public static final DeferredItem<Item> REACTOR_SENSOR = ITEMS.register("reactor_sensor", () -> new ReactorSensorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> TURRET_CHIP = ITEMS.register("turret_chip", () -> new TurretBiometryItem(new Item.Properties().stacksTo(1)));

    /* PWR: Brennstoff frisch, heiss und abgebrannt. Die heisse und die abgebrannte Form tragen
     * dieselbe Sorte weiter, damit das Brennstoffbecken sie wieder auseinanderhalten kann. */
    public static final DeferredItem<Item> PWR_FUEL = ITEMS.register("pwr_fuel", () -> new PWRFuelItem(new Item.Properties()));
    public static final DeferredItem<Item> WATZ_PELLET = ITEMS.register("watz_pellet", () -> new WatzPelletItem(new Item.Properties().stacksTo(16), false));
    public static final DeferredItem<Item> WATZ_PELLET_DEPLETED = ITEMS.register("watz_pellet_depleted", () -> new WatzPelletItem(new Item.Properties().stacksTo(16), true));
    public static final DeferredItem<Item> PILE_ROD = ITEMS.register("pile_rod", () -> new PileRodItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> ICF_PELLET = ITEMS.register("icf_pellet", () -> new ICFPelletItem(new Item.Properties()));
    public static final DeferredItem<Item> ICF_PELLET_DEPLETED = ITEMS.register("icf_pellet_depleted", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> ICF_PELLET_EMPTY = ITEMS.register("icf_pellet_empty", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PWR_FUEL_HOT = ITEMS.register("pwr_fuel_hot", () -> new EnumMultiItem(new Item.Properties(), EnumPWRFuel.class, true, false));
    public static final DeferredItem<Item> PWR_FUEL_DEPLETED = ITEMS.register("pwr_fuel_depleted", () -> new EnumMultiItem(new Item.Properties(), EnumPWRFuel.class, true, false));
    public static final DeferredItem<Item> PLATE_KEVLAR = ITEMS.register("plate_kevlar", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_MIXED = ITEMS.register("plate_mixed", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_PAA = ITEMS.register("plate_paa", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_ACTINIUM = ITEMS.register("powder_actinium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_ACTINIUM_TINY = ITEMS.register("powder_actinium_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_ASTATINE = ITEMS.register("powder_astatine", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_AT209 = ITEMS.register("powder_at209", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_AT209_TINY = ITEMS.register("powder_at209_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_AU198 = ITEMS.register("powder_au198", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_AU198_TINY = ITEMS.register("powder_au198_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_AUSTRALIUM = ITEMS.register("powder_australium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_BALEFIRE = ITEMS.register("powder_balefire", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_BORAX = ITEMS.register("powder_borax", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_BORON = ITEMS.register("powder_boron", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_BORON_TINY = ITEMS.register("powder_boron_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_BROMINE = ITEMS.register("powder_bromine", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CADMIUM = ITEMS.register("powder_cadmium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CAESIUM = ITEMS.register("powder_caesium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CALCIUM = ITEMS.register("powder_calcium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CDALLOY = ITEMS.register("powder_cdalloy", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CEMENT = ITEMS.register("powder_cement", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CERIUM = ITEMS.register("powder_cerium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CERIUM_TINY = ITEMS.register("powder_cerium_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CHLOROCALCITE = ITEMS.register("powder_chlorocalcite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CHLOROPHYTE = ITEMS.register("powder_chlorophyte", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CLOUD = ITEMS.register("powder_cloud", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CO60 = ITEMS.register("powder_co60", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CO60_TINY = ITEMS.register("powder_co60_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_COAL_TINY = ITEMS.register("powder_coal_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_COBALT_TINY = ITEMS.register("powder_cobalt_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_COLTAN = ITEMS.register("powder_coltan", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_COLTAN_ORE = ITEMS.register("powder_coltan_ore", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CS137 = ITEMS.register("powder_cs137", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_CS137_TINY = ITEMS.register("powder_cs137_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_DAFFERGON = ITEMS.register("powder_daffergon", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_DESH_MIX = ITEMS.register("powder_desh_mix", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_DESH_READY = ITEMS.register("powder_desh_ready", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_EUPHEMIUM = ITEMS.register("powder_euphemium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_FERTILIZER = ITEMS.register("powder_fertilizer", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_FIRE = ITEMS.register("powder_fire", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_FLUX = ITEMS.register("powder_flux", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_I131 = ITEMS.register("powder_i131", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_I131_TINY = ITEMS.register("powder_i131_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_ICE = ITEMS.register("powder_ice", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_IMPURE_OSMIRIDIUM = ITEMS.register("powder_impure_osmiridium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_IODINE = ITEMS.register("powder_iodine", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_IODINE_TINY = ITEMS.register("powder_iodine_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_LANTHANIUM = ITEMS.register("powder_lanthanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_LANTHANIUM_TINY = ITEMS.register("powder_lanthanium_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_LIMESTONE = ITEMS.register("powder_limestone", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_LITHIUM_TINY = ITEMS.register("powder_lithium_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_MAGIC = ITEMS.register("powder_magic", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_MAGNETIZED_TUNGSTEN = ITEMS.register("powder_magnetized_tungsten", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_MOLYSITE = ITEMS.register("powder_molysite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_NEODYMIUM = ITEMS.register("powder_neodymium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_NEODYMIUM_TINY = ITEMS.register("powder_neodymium_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_NEPTUNIUM = ITEMS.register("powder_neptunium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_NIOBIUM_TINY = ITEMS.register("powder_niobium_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_NITAN_MIX = ITEMS.register("powder_nitan_mix", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_OSMIRIDIUM = ITEMS.register("powder_osmiridium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_PALEOGENITE = ITEMS.register("powder_paleogenite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_PALEOGENITE_TINY = ITEMS.register("powder_paleogenite_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_PB209 = ITEMS.register("powder_pb209", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_PB209_TINY = ITEMS.register("powder_pb209_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_PLUTONIUM = ITEMS.register("powder_plutonium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_POISON = ITEMS.register("powder_poison", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_POLONIUM = ITEMS.register("powder_polonium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_POWER = ITEMS.register("powder_power", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_RA226 = ITEMS.register("powder_ra226", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_RED_COPPER = ITEMS.register("powder_red_copper", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_REIIUM = ITEMS.register("powder_reiium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_SAWDUST = ITEMS.register("powder_sawdust", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_SCHRABIDATE = ITEMS.register("powder_schrabidate", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_SEMTEX_MIX = ITEMS.register("powder_semtex_mix", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_SODIUM = ITEMS.register("powder_sodium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_SPARK_MIX = ITEMS.register("powder_spark_mix", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_SR90 = ITEMS.register("powder_sr90", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_SR90_TINY = ITEMS.register("powder_sr90_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_STEEL_TINY = ITEMS.register("powder_steel_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_STRONTIUM = ITEMS.register("powder_strontium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_TANTALIUM = ITEMS.register("powder_tantalium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_TCALLOY = ITEMS.register("powder_tcalloy", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_TEKTITE = ITEMS.register("powder_tektite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_TENNESSINE = ITEMS.register("powder_tennessine", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_THERMITE = ITEMS.register("powder_thermite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_THORIUM = ITEMS.register("powder_thorium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_UNOBTAINIUM = ITEMS.register("powder_unobtainium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_VERTICIUM = ITEMS.register("powder_verticium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_WEIDANIUM = ITEMS.register("powder_weidanium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_XE135 = ITEMS.register("powder_xe135", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_XE135_TINY = ITEMS.register("powder_xe135_tiny", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_YELLOWCAKE = ITEMS.register("powder_yellowcake", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GEM_ALEXANDRITE = ITEMS.register("gem_alexandrite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GEM_RAD = ITEMS.register("gem_rad", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GEM_SODALITE = ITEMS.register("gem_sodalite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GEM_TANTALIUM = ITEMS.register("gem_tantalium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GEM_VOLCANIC = ITEMS.register("gem_volcanic", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> CRYSTAL_CHARRED = ITEMS.register("crystal_charred", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_COAL = ITEMS.register("crystal_coal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_ENERGY = ITEMS.register("crystal_energy", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CRYSTAL_HORN = ITEMS.register("crystal_horn", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRYSTAL_XEN = ITEMS.register("crystal_xen", () -> new Item(new Item.Properties().stacksTo(1)));

    private static DeferredItem<Item> registerNugget(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static DeferredItem<Item> registerPickaxe(String name, Tier tier, int damage, float speed) {
        return registerPickaxe(name, tier, damage, speed, Rarity.COMMON, tool -> {});
    }

    private static DeferredItem<Item> registerPickaxe(String name, Tier tier, int damage, float speed, Consumer<ToolAbilityItem> configure) {
        return registerPickaxe(name, tier, damage, speed, Rarity.COMMON, configure);
    }

    private static DeferredItem<Item> registerPickaxe(String name, Tier tier, int damage, float speed, Rarity rarity) {
        return registerPickaxe(name, tier, damage, speed, rarity, tool -> {});
    }

    private static DeferredItem<Item> registerPickaxe(String name, Tier tier, int damage, float speed, Rarity rarity, Consumer<ToolAbilityItem> configure) {
        return registerPickaxe(name, tier, damage, speed, ToolAbilityItem.ToolRole.PICKAXE, rarity, configure);
    }

    private static DeferredItem<Item> registerPickaxe(String name, Tier tier, int damage, float speed, ToolAbilityItem.ToolRole role, Rarity rarity, Consumer<ToolAbilityItem> configure) {
        return ITEMS.register(name, () -> {
            ToolAbilityItem item = new ToolAbilityItem(new Item.Properties().stacksTo(1).durability(tier.getUses()).rarity(rarity).attributes(PickaxeItem.createAttributes(tier, damage, speed)), tier, role);
            configure.accept(item);
            return item;
        });
    }

    private static DeferredItem<Item> registerAxe(String name, Tier tier, float damage, float speed) {
        return registerAxe(name, tier, damage, speed, Rarity.COMMON, tool -> {});
    }

    private static DeferredItem<Item> registerAxe(String name, Tier tier, float damage, float speed, Consumer<ToolAbilityItem> configure) {
        return registerAxe(name, tier, damage, speed, Rarity.COMMON, configure);
    }

    private static DeferredItem<Item> registerAxe(String name, Tier tier, float damage, float speed, Rarity rarity) {
        return registerAxe(name, tier, damage, speed, rarity, tool -> {});
    }

    private static DeferredItem<Item> registerAxe(String name, Tier tier, float damage, float speed, Rarity rarity, Consumer<ToolAbilityItem> configure) {
        return ITEMS.register(name, () -> {
            ToolAbilityItem item = new ToolAbilityItem(new Item.Properties().stacksTo(1).durability(tier.getUses()).rarity(rarity).attributes(AxeItem.createAttributes(tier, damage, speed)), tier, ToolAbilityItem.ToolRole.AXE);
            configure.accept(item);
            return item;
        });
    }

    private static DeferredItem<Item> registerShovel(String name, Tier tier, float damage, float speed) {
        return registerShovel(name, tier, damage, speed, Rarity.COMMON, tool -> {});
    }

    private static DeferredItem<Item> registerShovel(String name, Tier tier, float damage, float speed, Consumer<ToolAbilityItem> configure) {
        return registerShovel(name, tier, damage, speed, Rarity.COMMON, configure);
    }

    private static DeferredItem<Item> registerShovel(String name, Tier tier, float damage, float speed, Rarity rarity) {
        return registerShovel(name, tier, damage, speed, rarity, tool -> {});
    }

    private static DeferredItem<Item> registerShovel(String name, Tier tier, float damage, float speed, Rarity rarity, Consumer<ToolAbilityItem> configure) {
        return ITEMS.register(name, () -> {
            ToolAbilityItem item = new ToolAbilityItem(new Item.Properties().stacksTo(1).durability(tier.getUses()).rarity(rarity).attributes(ShovelItem.createAttributes(tier, damage, speed)), tier, ToolAbilityItem.ToolRole.SHOVEL);
            configure.accept(item);
            return item;
        });
    }

    private static DeferredItem<Item> registerHoe(String name, Tier tier, int damage, float speed) {
        return registerHoe(name, tier, damage, speed, Rarity.COMMON, tool -> {});
    }

    private static DeferredItem<Item> registerHoe(String name, Tier tier, int damage, float speed, Consumer<ToolAbilityItem> configure) {
        return registerHoe(name, tier, damage, speed, Rarity.COMMON, configure);
    }

    private static DeferredItem<Item> registerHoe(String name, Tier tier, int damage, float speed, Rarity rarity) {
        return registerHoe(name, tier, damage, speed, rarity, tool -> {});
    }

    private static DeferredItem<Item> registerHoe(String name, Tier tier, int damage, float speed, Rarity rarity, Consumer<ToolAbilityItem> configure) {
        return ITEMS.register(name, () -> {
            ToolAbilityItem item = new ToolAbilityItem(new Item.Properties().stacksTo(1).durability(tier.getUses()).rarity(rarity).attributes(HoeItem.createAttributes(tier, damage, speed)), tier, ToolAbilityItem.ToolRole.HOE);
            configure.accept(item);
            return item;
        });
    }

    public static ItemStack castPlate(CastPlateItem.Type type) {
        return MetaHelper.newStack(CAST_PLATE.get(), 1, type.ordinal());
    }

    public static ItemStack castPlate(CastPlateItem.Type type, int count) {
        return MetaHelper.newStack(CAST_PLATE.get(), count, type.ordinal());
    }

    public static ItemStack castPlateWelded(CastPlateItem.Type type) {
        return MetaHelper.newStack(CAST_PLATE_WELDED.get(), 1, type.ordinal());
    }

    public static ItemStack castPlateWelded(CastPlateItem.Type type, int count) {
        return MetaHelper.newStack(CAST_PLATE_WELDED.get(), count, type.ordinal());
    }


    /* RBMK: Deckel, leerer Stab und die Brennstaebe. Alle Werte unveraendert aus dem Original. */
    public static final DeferredItem<Item> RBMK_LINK = ITEMS.register("rbmk_link", () -> new RBMKLinkItem(new Item.Properties()));
    /* Runde 42: die Truemmer, die eine Kernschmelze hinterlaesst. */
    public static final DeferredItem<Item> DEBRIS_METAL = ITEMS.register("debris_metal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DEBRIS_FUEL = ITEMS.register("debris_fuel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DEBRIS_GRAPHITE = ITEMS.register("debris_graphite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DEBRIS_ELEMENT = ITEMS.register("debris_element", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DEBRIS_SHRAPNEL = ITEMS.register("debris_shrapnel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DEBRIS_CONCRETE = ITEMS.register("debris_concrete", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DEBRIS_EXCHANGER = ITEMS.register("debris_exchanger", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RBMK_LID = ITEMS.register("rbmk_lid", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RBMK_LID_GLASS = ITEMS.register("rbmk_lid_glass", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RBMK_FUEL_EMPTY = ITEMS.register("rbmk_fuel_empty", () -> new Item(new Item.Properties()));

    /* RBMK: die Pellets, die beim Zerlegen eines abgekuehlten Brennstabs anfallen. */
    public static final DeferredItem<Item> RBMK_PELLET_UEU = ITEMS.register("rbmk_pellet_ueu", () -> new RBMKPelletItem("Unenriched Uranium", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_MEU = ITEMS.register("rbmk_pellet_meu", () -> new RBMKPelletItem("Medium Enriched Uranium-235", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_HEU233 = ITEMS.register("rbmk_pellet_heu233", () -> new RBMKPelletItem("Highly Enriched Uranium-233", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_HEU235 = ITEMS.register("rbmk_pellet_heu235", () -> new RBMKPelletItem("Highly Enriched Uranium-235", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_UZH = ITEMS.register("rbmk_pellet_uzh", () -> new RBMKPelletItem("Uranium Zirconium Hydride", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_THMEU = ITEMS.register("rbmk_pellet_thmeu", () -> new RBMKPelletItem("Thorium with MEU Driver Fuel", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_LEP = ITEMS.register("rbmk_pellet_lep", () -> new RBMKPelletItem("Low Enriched Plutonium-239", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_MEP = ITEMS.register("rbmk_pellet_mep", () -> new RBMKPelletItem("Medium Enriched Plutonium-239", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_HEP = ITEMS.register("rbmk_pellet_hep", () -> new RBMKPelletItem("Highly Enriched Plutonium-239", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_HEP241 = ITEMS.register("rbmk_pellet_hep241", () -> new RBMKPelletItem("Highly Enriched Plutonium-241", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_LEA = ITEMS.register("rbmk_pellet_lea", () -> new RBMKPelletItem("Low Enriched Americium-242", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_MEA = ITEMS.register("rbmk_pellet_mea", () -> new RBMKPelletItem("Medium Enriched Americium-242", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_HEA241 = ITEMS.register("rbmk_pellet_hea241", () -> new RBMKPelletItem("Highly Enriched Americium-241", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_HEA242 = ITEMS.register("rbmk_pellet_hea242", () -> new RBMKPelletItem("Highly Enriched Americium-242", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_MEN = ITEMS.register("rbmk_pellet_men", () -> new RBMKPelletItem("Medium Enriched Neptunium-237", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_HEN = ITEMS.register("rbmk_pellet_hen", () -> new RBMKPelletItem("Highly Enriched Neptunium-237", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_MOX = ITEMS.register("rbmk_pellet_mox", () -> new RBMKPelletItem("Mixed MEU & LEP Oxide", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_LES = ITEMS.register("rbmk_pellet_les", () -> new RBMKPelletItem("Low Enriched Schrabidium-326", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_MES = ITEMS.register("rbmk_pellet_mes", () -> new RBMKPelletItem("Medium Enriched Schrabidium-326", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_HES = ITEMS.register("rbmk_pellet_hes", () -> new RBMKPelletItem("Highly Enriched Schrabidium-326", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_LEAUS = ITEMS.register("rbmk_pellet_leaus", () -> new RBMKPelletItem("Low Enriched Australium (Tasmanite)", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_HEAUS = ITEMS.register("rbmk_pellet_heaus", () -> new RBMKPelletItem("Highly Enriched Australium (Ayerite)", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_PO210BE = ITEMS.register("rbmk_pellet_po210be", () -> new RBMKPelletItem("Polonium-210 & Beryllium Neutron Source", new Item.Properties()).disableXenon());
    public static final DeferredItem<Item> RBMK_PELLET_RA226BE = ITEMS.register("rbmk_pellet_ra226be", () -> new RBMKPelletItem("Radium-226 & Beryllium Neutron Source", new Item.Properties()).disableXenon());
    public static final DeferredItem<Item> RBMK_PELLET_PU238BE = ITEMS.register("rbmk_pellet_pu238be", () -> new RBMKPelletItem("Plutonium-238 & Beryllium Neutron Source", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_BALEFIRE_GOLD = ITEMS.register("rbmk_pellet_balefire_gold", () -> new RBMKPelletItem("Antihydrogen in a Magnetized Gold-198 Lattice", new Item.Properties()).disableXenon());
    public static final DeferredItem<Item> RBMK_PELLET_FLASHLEAD = ITEMS.register("rbmk_pellet_flashlead", () -> new RBMKPelletItem("Antihydrogen confined by a Magnetized Gold-198 and Lead-209 Lattice", new Item.Properties()).disableXenon());
    public static final DeferredItem<Item> RBMK_PELLET_BALEFIRE = ITEMS.register("rbmk_pellet_balefire", () -> new RBMKPelletItem("Draconic Flames", new Item.Properties()).disableXenon());
    public static final DeferredItem<Item> RBMK_PELLET_ZFB_BISMUTH = ITEMS.register("rbmk_pellet_zfb_bismuth", () -> new RBMKPelletItem("Zirconium Fast Breeder - LEU/HEP-241#Bi", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_ZFB_PU241 = ITEMS.register("rbmk_pellet_zfb_pu241", () -> new RBMKPelletItem("Zirconium Fast Breeder - HEU-235/HEP-240#Pu-241", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_ZFB_AM_MIX = ITEMS.register("rbmk_pellet_zfb_am_mix", () -> new RBMKPelletItem("Zirconium Fast Breeder - HEP-241#MEA", new Item.Properties()));
    public static final DeferredItem<Item> RBMK_PELLET_DRX = ITEMS.register("rbmk_pellet_drx", () -> new RBMKPelletItem(ChatFormatting.OBFUSCATED + "can't you hear, can't you hear the thunder?", new Item.Properties()));

    public static final DeferredItem<Item> RBMK_FUEL_UEU = ITEMS.register("rbmk_fuel_ueu", () -> new RBMKRodItem("Unenriched Uranium", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(15).setFunction(EnumBurnFunc.LOG_TEN).setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE).setHeat(0.65).setMeltingPoint(2865).setTint(0x868D82).setPellet(RBMK_PELLET_UEU));
    public static final DeferredItem<Item> RBMK_FUEL_MEU = ITEMS.register("rbmk_fuel_meu", () -> new RBMKRodItem("Medium Enriched Uranium-235", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(20).setFunction(EnumBurnFunc.LOG_TEN).setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE).setHeat(0.65).setMeltingPoint(2865).setTint(0x868D82).setPellet(RBMK_PELLET_MEU));
    public static final DeferredItem<Item> RBMK_FUEL_HEU233 = ITEMS.register("rbmk_fuel_heu233", () -> new RBMKRodItem("Highly Enriched Uranium-233", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(27.5D).setFunction(EnumBurnFunc.LINEAR).setHeat(1.25D).setMeltingPoint(2865).setTint(0x868D82).setPellet(RBMK_PELLET_HEU233));
    public static final DeferredItem<Item> RBMK_FUEL_HEU235 = ITEMS.register("rbmk_fuel_heu235", () -> new RBMKRodItem("Highly Enriched Uranium-235", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(50).setFunction(EnumBurnFunc.SQUARE_ROOT).setMeltingPoint(2865).setTint(0x868D82).setPellet(RBMK_PELLET_HEU235));
    public static final DeferredItem<Item> RBMK_FUEL_UZH = ITEMS.register("rbmk_fuel_uzh", () -> new RBMKRodItem("Uranium Zirconium Hydride", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(50_000_000D).setStats(30).setFunction(EnumBurnFunc.LOG_TEN).setDepletionFunction(EnumDepleteFunc.GENTLE_SLOPE).setHeat(0.75).setMeltingPoint(1845).setTint(0x7077AF).setHeatCoeff(1_000D, 500D).setDiffusion(0.1D).setPellet(RBMK_PELLET_UZH));
    public static final DeferredItem<Item> RBMK_FUEL_THMEU = ITEMS.register("rbmk_fuel_thmeu", () -> new RBMKRodItem("Thorium with MEU Driver Fuel", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(20).setFunction(EnumBurnFunc.PLATEU).setDepletionFunction(EnumDepleteFunc.BOOSTED_SLOPE).setHeat(0.65D).setMeltingPoint(3350).setTint(0x665448).setPellet(RBMK_PELLET_THMEU));
    public static final DeferredItem<Item> RBMK_FUEL_LEP = ITEMS.register("rbmk_fuel_lep", () -> new RBMKRodItem("Low Enriched Plutonium-239", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(35).setFunction(EnumBurnFunc.LOG_TEN).setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE).setHeat(0.75D).setMeltingPoint(2744).setTint(0x656E6B).setPellet(RBMK_PELLET_LEP));
    public static final DeferredItem<Item> RBMK_FUEL_MEP = ITEMS.register("rbmk_fuel_mep", () -> new RBMKRodItem("Medium Enriched Plutonium-239", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(35).setFunction(EnumBurnFunc.SQUARE_ROOT).setMeltingPoint(2744).setTint(0x656E6B).setPellet(RBMK_PELLET_MEP));
    public static final DeferredItem<Item> RBMK_FUEL_HEP = ITEMS.register("rbmk_fuel_hep", () -> new RBMKRodItem("Highly Enriched Plutonium-239", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(30).setFunction(EnumBurnFunc.LINEAR).setHeat(1.25D).setMeltingPoint(2744).setTint(0x656E6B).setPellet(RBMK_PELLET_HEP));
    public static final DeferredItem<Item> RBMK_FUEL_HEP241 = ITEMS.register("rbmk_fuel_hep241", () -> new RBMKRodItem("Highly Enriched Plutonium-241", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(40).setFunction(EnumBurnFunc.LINEAR).setHeat(1.75D).setMeltingPoint(2744).setTint(0x656E6B).setPellet(RBMK_PELLET_HEP241));
    public static final DeferredItem<Item> RBMK_FUEL_LEA = ITEMS.register("rbmk_fuel_lea", () -> new RBMKRodItem("Low Enriched Americium-242", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(60, 10).setFunction(EnumBurnFunc.SQUARE_ROOT).setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE).setHeat(1.5D).setMeltingPoint(2386).setTint(0xA88A8F).setPellet(RBMK_PELLET_LEA));
    public static final DeferredItem<Item> RBMK_FUEL_MEA = ITEMS.register("rbmk_fuel_mea", () -> new RBMKRodItem("Medium Enriched Americium-242", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(35D, 20).setFunction(EnumBurnFunc.ARCH).setHeat(1.75D).setMeltingPoint(2386).setTint(0xA88A8F).setPellet(RBMK_PELLET_MEA));
    public static final DeferredItem<Item> RBMK_FUEL_HEA241 = ITEMS.register("rbmk_fuel_hea241", () -> new RBMKRodItem("Highly Enriched Americium-241", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(65, 15).setFunction(EnumBurnFunc.SQUARE_ROOT).setHeat(1.85D).setMeltingPoint(2386).setTint(0xA88A8F).setNeutronTypes(NType.FAST, NType.FAST).setPellet(RBMK_PELLET_HEA241));
    public static final DeferredItem<Item> RBMK_FUEL_HEA242 = ITEMS.register("rbmk_fuel_hea242", () -> new RBMKRodItem("Highly Enriched Americium-242", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(45).setFunction(EnumBurnFunc.LINEAR).setHeat(2D).setMeltingPoint(2386).setTint(0xA88A8F).setPellet(RBMK_PELLET_HEA242));
    public static final DeferredItem<Item> RBMK_FUEL_MEN = ITEMS.register("rbmk_fuel_men", () -> new RBMKRodItem("Medium Enriched Neptunium-237", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(30).setFunction(EnumBurnFunc.SQUARE_ROOT).setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE).setHeat(0.75).setMeltingPoint(2800).setTint(0x757E73).setNeutronTypes(NType.ANY, NType.FAST).setPellet(RBMK_PELLET_MEN));
    public static final DeferredItem<Item> RBMK_FUEL_HEN = ITEMS.register("rbmk_fuel_hen", () -> new RBMKRodItem("Highly Enriched Neptunium-237", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(40).setFunction(EnumBurnFunc.SQUARE_ROOT).setMeltingPoint(2800).setTint(0x757E73).setNeutronTypes(NType.FAST, NType.FAST).setPellet(RBMK_PELLET_HEN));
    public static final DeferredItem<Item> RBMK_FUEL_MOX = ITEMS.register("rbmk_fuel_mox", () -> new RBMKRodItem("Mixed Oxide Fuel", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(40).setFunction(EnumBurnFunc.LOG_TEN).setDepletionFunction(EnumDepleteFunc.RAISING_SLOPE).setMeltingPoint(2815).setTint(0x868D82).setPellet(RBMK_PELLET_MOX));
    public static final DeferredItem<Item> RBMK_FUEL_LES = ITEMS.register("rbmk_fuel_les", () -> new RBMKRodItem("Low Enriched Schrabidium-326", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(50).setFunction(EnumBurnFunc.SQUARE_ROOT).setHeat(1.25D).setMeltingPoint(2500).setTint(0x2D9A94).setNeutronTypes(NType.SLOW, NType.SLOW).setPellet(RBMK_PELLET_LES));
    public static final DeferredItem<Item> RBMK_FUEL_MES = ITEMS.register("rbmk_fuel_mes", () -> new RBMKRodItem("Medium Enriched Schrabidium-326", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(75D).setFunction(EnumBurnFunc.ARCH).setHeat(1.5D).setMeltingPoint(2750).setTint(0x2D9A94).setPellet(RBMK_PELLET_MES));
    public static final DeferredItem<Item> RBMK_FUEL_HES = ITEMS.register("rbmk_fuel_hes", () -> new RBMKRodItem("Highly Enriched Schrabidium-326", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(90).setFunction(EnumBurnFunc.LINEAR).setDepletionFunction(EnumDepleteFunc.LINEAR).setHeat(1.75D).setMeltingPoint(3000).setTint(0x2D9A94).setPellet(RBMK_PELLET_HES));
    public static final DeferredItem<Item> RBMK_FUEL_LEAUS = ITEMS.register("rbmk_fuel_leaus", () -> new RBMKRodItem("Low Enriched Australium", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(30).setFunction(EnumBurnFunc.SIGMOID).setDepletionFunction(EnumDepleteFunc.LINEAR).setHeat(1.5D).setMeltingPoint(7029).setTint(0xFFEE00).setXenon(0.05D, 50D).setPellet(RBMK_PELLET_LEAUS));
    public static final DeferredItem<Item> RBMK_FUEL_HEAUS = ITEMS.register("rbmk_fuel_heaus", () -> new RBMKRodItem("High Enriched Australium", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(35).setFunction(EnumBurnFunc.LINEAR).setHeat(1.5D).setMeltingPoint(5211).setTint(0xFFEE00).setXenon(0.05D, 50D).setPellet(RBMK_PELLET_HEAUS));
    public static final DeferredItem<Item> RBMK_FUEL_PO210BE = ITEMS.register("rbmk_fuel_po210be", () -> new RBMKRodItem("Polonium-210 Beryllium Neutron Source", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(25_000_000D).setStats(0D, 50).setFunction(EnumBurnFunc.PASSIVE).setDepletionFunction(EnumDepleteFunc.LINEAR).setHeat(0.1D).setMeltingPoint(1287).setTint(0x563A26).setXenon(0.0D, 50D).setDiffusion(0.05D).setNeutronTypes(NType.SLOW, NType.SLOW).setPellet(RBMK_PELLET_PO210BE));
    public static final DeferredItem<Item> RBMK_FUEL_RA226BE = ITEMS.register("rbmk_fuel_ra226be", () -> new RBMKRodItem("Radium-226 Beryllium Neutron Source", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(0D, 20).setFunction(EnumBurnFunc.PASSIVE).setDepletionFunction(EnumDepleteFunc.LINEAR).setHeat(0.035D).setMeltingPoint(700).setTint(0xB3B6AD).setXenon(0.0D, 50D).setDiffusion(0.5D).setNeutronTypes(NType.SLOW, NType.SLOW).setPellet(RBMK_PELLET_RA226BE));
    public static final DeferredItem<Item> RBMK_FUEL_PU238BE = ITEMS.register("rbmk_fuel_pu238be", () -> new RBMKRodItem("Plutonium-238 Beryllium Neutron Source", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(50_000_000D).setStats(40, 40).setFunction(EnumBurnFunc.SQUARE_ROOT).setHeat(0.1D).setMeltingPoint(1287).setTint(0x656E6B).setDiffusion(0.05D).setNeutronTypes(NType.SLOW, NType.SLOW).setPellet(RBMK_PELLET_PU238BE));
    public static final DeferredItem<Item> RBMK_FUEL_BALEFIRE_GOLD = ITEMS.register("rbmk_fuel_balefire_gold", () -> new RBMKRodItem("Gold-198 Balefire", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(50, 10).setFunction(EnumBurnFunc.ARCH).setDepletionFunction(EnumDepleteFunc.LINEAR).setMeltingPoint(2000).setTint(0xDC9613).setXenon(0.0D, 50D).setPellet(RBMK_PELLET_BALEFIRE_GOLD));
    public static final DeferredItem<Item> RBMK_FUEL_FLASHLEAD = ITEMS.register("rbmk_fuel_flashlead", () -> new RBMKRodItem("Lead-209 Balefire", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(250_000_000D).setStats(40, 50).setFunction(EnumBurnFunc.ARCH).setDepletionFunction(EnumDepleteFunc.LINEAR).setMeltingPoint(2050).setTint(0x7B7B87).setXenon(0.0D, 50D).setPellet(RBMK_PELLET_FLASHLEAD));
    public static final DeferredItem<Item> RBMK_FUEL_BALEFIRE = ITEMS.register("rbmk_fuel_balefire", () -> new RBMKRodItem("Balefire", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(100_000_000D).setStats(100, 35).setFunction(EnumBurnFunc.LINEAR).setHeat(3D).setMeltingPoint(3652).setTint(0xB2FF1B).setXenon(0.0D, 50D).setPellet(RBMK_PELLET_BALEFIRE));
    public static final DeferredItem<Item> RBMK_FUEL_ZFB_BISMUTH = ITEMS.register("rbmk_fuel_zfb_bismuth", () -> new RBMKRodItem("Zirconium Fuel Bundle: Bismuth", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(50_000_000D).setStats(20).setFunction(EnumBurnFunc.SQUARE_ROOT).setHeat(1.75D).setMeltingPoint(2744).setTint(0xAAA36A).setPellet(RBMK_PELLET_ZFB_BISMUTH));
    public static final DeferredItem<Item> RBMK_FUEL_ZFB_PU241 = ITEMS.register("rbmk_fuel_zfb_pu241", () -> new RBMKRodItem("Zirconium Fuel Bundle: Plutonium-241", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(50_000_000D).setStats(20).setFunction(EnumBurnFunc.SQUARE_ROOT).setMeltingPoint(2865).setTint(0xAAA36A).setPellet(RBMK_PELLET_ZFB_PU241));
    public static final DeferredItem<Item> RBMK_FUEL_ZFB_AM_MIX = ITEMS.register("rbmk_fuel_zfb_am_mix", () -> new RBMKRodItem("Zirconium Fuel Bundle: Americium Mix", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(50_000_000D).setStats(20).setFunction(EnumBurnFunc.LINEAR).setHeat(1.75D).setMeltingPoint(2744).setTint(0xAAA36A).setPellet(RBMK_PELLET_ZFB_AM_MIX));
    public static final DeferredItem<Item> RBMK_FUEL_DRX = ITEMS.register("rbmk_fuel_drx", () -> new RBMKRodItem("Digamma Rod Experimental", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(10_000_000D).setStats(1000, 10).setFunction(EnumBurnFunc.QUADRATIC).setHeat(0.1D).setMeltingPoint(100000).setTint(0xD77276).setPellet(RBMK_PELLET_DRX));
    public static final DeferredItem<Item> RBMK_FUEL_TEST = ITEMS.register("rbmk_fuel_test", () -> new RBMKRodItem("THE VOICES", new Item.Properties().craftRemainder(RBMK_FUEL_EMPTY.get())).setYield(1_000_000D).setStats(100).setFunction(EnumBurnFunc.EXPERIMENTAL).setHeat(1.0D).setMeltingPoint(100000));

    public static ComparableStack castPlateIngredient(CastPlateItem.Type type) {
        return new ComparableStack(CAST_PLATE.get(), 1, type.ordinal());
    }

    public static ComparableStack castPlateIngredient(CastPlateItem.Type type, int count) {
        return new ComparableStack(CAST_PLATE.get(), count, type.ordinal());
    }

    public static ComparableStack castPlateWeldedIngredient(CastPlateItem.Type type) {
        return new ComparableStack(CAST_PLATE_WELDED.get(), 1, type.ordinal());
    }

    public static ComparableStack castPlateWeldedIngredient(CastPlateItem.Type type, int count) {
        return new ComparableStack(CAST_PLATE_WELDED.get(), count, type.ordinal());
    }

    public static void registerOther(DeferredRegister.Items itemRegistry) {
        GunFactory.init(itemRegistry);
    }

    public static void register(IEventBus eventBus) {
        registerOther(ITEMS);

        ITEMS.register(eventBus);
    }
}
