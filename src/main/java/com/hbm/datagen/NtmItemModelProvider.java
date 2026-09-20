package com.hbm.datagen;

import com.hbm.blocks.NtmBlocks;
import com.hbm.items.ICustomItemModelRegister;
import com.hbm.items.NtmItems;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class NtmItemModelProvider extends ItemModelProvider {

    public NtmItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, NuclearTechMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        NtmItems.ITEMS.getEntries().forEach(holder -> {
            Item item = holder.get();

            if(item instanceof ICustomItemModelRegister icimr) {
                ResourceLocation loc = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));
                icimr.registerItemModel(this, loc);
            }
        });


        /* RBMK: Deckel, leerer Stab und alle Brennstaebe. */
        this.basicItem(NtmItems.RBMK_LINK.get());
        this.basicItem(NtmItems.TELE_LINK.get());
        this.basicItem(NtmItems.DEBRIS_METAL.get());
        this.basicItem(NtmItems.DEBRIS_FUEL.get());
        this.basicItem(NtmItems.DEBRIS_GRAPHITE.get());
        this.basicItem(NtmItems.DEBRIS_ELEMENT.get());
        this.basicItem(NtmItems.DEBRIS_SHRAPNEL.get());
        this.basicItem(NtmItems.DEBRIS_CONCRETE.get());
        this.basicItem(NtmItems.DEBRIS_EXCHANGER.get());
        this.basicItem(NtmItems.RBMK_LID.get());
        this.basicItem(NtmItems.RBMK_LID_GLASS.get());
        this.basicItem(NtmItems.RBMK_FUEL_EMPTY.get());
        this.basicItem(NtmItems.CIRCUIT_NUMITRON.get());
        this.basicItem(NtmItems.RBMK_FUEL_UEU.get());
        this.basicItem(NtmItems.RBMK_FUEL_MEU.get());
        this.basicItem(NtmItems.RBMK_FUEL_HEU233.get());
        this.basicItem(NtmItems.RBMK_FUEL_HEU235.get());
        this.basicItem(NtmItems.RBMK_FUEL_UZH.get());
        this.basicItem(NtmItems.RBMK_FUEL_THMEU.get());
        this.basicItem(NtmItems.RBMK_FUEL_LEP.get());
        this.basicItem(NtmItems.RBMK_FUEL_MEP.get());
        this.basicItem(NtmItems.RBMK_FUEL_HEP.get());
        this.basicItem(NtmItems.RBMK_FUEL_HEP241.get());
        this.basicItem(NtmItems.RBMK_FUEL_LEA.get());
        this.basicItem(NtmItems.RBMK_FUEL_MEA.get());
        this.basicItem(NtmItems.RBMK_FUEL_HEA241.get());
        this.basicItem(NtmItems.RBMK_FUEL_HEA242.get());
        this.basicItem(NtmItems.RBMK_FUEL_MEN.get());
        this.basicItem(NtmItems.RBMK_FUEL_HEN.get());
        this.basicItem(NtmItems.RBMK_FUEL_MOX.get());
        this.basicItem(NtmItems.RBMK_FUEL_LES.get());
        this.basicItem(NtmItems.RBMK_FUEL_MES.get());
        this.basicItem(NtmItems.RBMK_FUEL_HES.get());
        this.basicItem(NtmItems.RBMK_FUEL_LEAUS.get());
        this.basicItem(NtmItems.RBMK_FUEL_HEAUS.get());
        this.basicItem(NtmItems.RBMK_FUEL_PO210BE.get());
        this.basicItem(NtmItems.RBMK_FUEL_RA226BE.get());
        this.basicItem(NtmItems.RBMK_FUEL_PU238BE.get());
        this.basicItem(NtmItems.RBMK_FUEL_BALEFIRE_GOLD.get());
        this.basicItem(NtmItems.RBMK_FUEL_FLASHLEAD.get());
        this.basicItem(NtmItems.RBMK_FUEL_BALEFIRE.get());
        this.basicItem(NtmItems.RBMK_FUEL_ZFB_BISMUTH.get());
        this.basicItem(NtmItems.RBMK_FUEL_ZFB_PU241.get());
        this.basicItem(NtmItems.RBMK_FUEL_ZFB_AM_MIX.get());
        this.basicItem(NtmItems.RBMK_FUEL_DRX.get());
        this.basicItem(NtmItems.RBMK_FUEL_TEST.get());
        this.basicItem(NtmItems.INGOT_URANIUM.get());
        this.basicItem(NtmItems.INGOT_U233.get());
        this.basicItem(NtmItems.INGOT_U235.get());
        this.basicItem(NtmItems.INGOT_U238.get());
        this.basicItem(NtmItems.INGOT_PLUTONIUM.get());
        this.basicItem(NtmItems.INGOT_PU238.get());
        this.basicItem(NtmItems.INGOT_PU239.get());
        this.basicItem(NtmItems.INGOT_PU240.get());
        this.basicItem(NtmItems.INGOT_PU241.get());
        this.basicItem(NtmItems.INGOT_PU_MIX.get());
        this.basicItem(NtmItems.INGOT_AM241.get());
        this.basicItem(NtmItems.INGOT_AM242.get());
        this.basicItem(NtmItems.INGOT_AM_MIX.get());
        this.basicItem(NtmItems.INGOT_NEPTUNIUM.get());
        this.basicItem(NtmItems.INGOT_POLONIUM.get());
        this.basicItem(NtmItems.INGOT_TECHNETIUM.get());
        this.basicItem(NtmItems.INGOT_CO60.get());
        this.basicItem(NtmItems.INGOT_SR90.get());
        this.basicItem(NtmItems.INGOT_AU198.get());
        this.basicItem(NtmItems.INGOT_PB209.get());
        this.basicItem(NtmItems.INGOT_RA226.get());
        this.basicItem(NtmItems.INGOT_TITANIUM.get());
        this.basicItem(NtmItems.INGOT_TUNGSTEN.get());
        this.basicItem(NtmItems.INGOT_ALUMINIUM.get());
        this.basicItem(NtmItems.INGOT_STEEL.get());
        this.basicItem(NtmItems.INGOT_TCALLOY.get());
        this.basicItem(NtmItems.INGOT_CDALLOY.get());
        this.basicItem(NtmItems.INGOT_BISMUTH_BRONZE.get());
        this.basicItem(NtmItems.INGOT_ARSENIC_BRONZE.get());
        this.basicItem(NtmItems.INGOT_BSCCO.get());
        this.basicItem(NtmItems.INGOT_LEAD.get());
        this.basicItem(NtmItems.INGOT_BISMUTH.get());
        this.basicItem(NtmItems.INGOT_ARSENIC.get());
        this.basicItem(NtmItems.INGOT_CALCIUM.get());
        this.basicItem(NtmItems.INGOT_CADMIUM.get());
        this.basicItem(NtmItems.INGOT_TANTALIUM.get());
        this.basicItem(NtmItems.INGOT_SILICON.get());
        this.basicItem(NtmItems.INGOT_NIOBIUM.get());
        this.basicItem(NtmItems.INGOT_BERYLLIUM.get());
        this.basicItem(NtmItems.INGOT_COBALT.get());
        this.basicItem(NtmItems.INGOT_ASBESTOS.get());
        this.basicItem(NtmItems.INGOT_BORON.get());
        this.basicItem(NtmItems.INGOT_GRAPHITE.get());
        this.basicItem(NtmItems.INGOT_FIREBRICK.get());
        this.basicItem(NtmItems.INGOT_DURA_STEEL.get());
        this.basicItem(NtmItems.INGOT_POLYMER.get());
        this.basicItem(NtmItems.INGOT_BAKELITE.get());
        this.basicItem(NtmItems.INGOT_BIORUBBER.get());
        this.basicItem(NtmItems.INGOT_RUBBER.get());
        this.basicItem(NtmItems.INGOT_PC.get());
        this.basicItem(NtmItems.INGOT_PVC.get());
        this.basicItem(NtmItems.INGOT_MUD.get());
        this.basicItem(NtmItems.INGOT_CTF.get());
        this.basicItem(NtmItems.INGOT_SCHRARANIUM.get());
        this.basicItem(NtmItems.INGOT_SCHRABIDIUM.get());
        this.basicItem(NtmItems.INGOT_SCHRABIDATE.get());
        this.basicItem(NtmItems.INGOT_MAGNETIZED_TUNGSTEN.get());
        this.basicItem(NtmItems.INGOT_COMBINE_STEEL.get());
        this.basicItem(NtmItems.INGOT_WEAPON_STEEL.get());
        this.basicItem(NtmItems.INGOT_SOLINIUM.get());
        this.basicItem(NtmItems.INGOT_GH336.get());
        this.basicItem(NtmItems.INGOT_URANIUM_FUEL.get());
        this.basicItem(NtmItems.INGOT_THORIUM_FUEL.get());
        this.basicItem(NtmItems.INGOT_PLUTONIUM_FUEL.get());
        this.basicItem(NtmItems.INGOT_NEPTUNIUM_FUEL.get());
        this.basicItem(NtmItems.INGOT_MOX_FUEL.get());
        this.basicItem(NtmItems.INGOT_AMERICIUM_FUEL.get());
        this.basicItem(NtmItems.INGOT_SCHRABIDIUM_FUEL.get());
        this.basicItem(NtmItems.INGOT_HES.get());
        this.basicItem(NtmItems.INGOT_LES.get());
        this.basicItem(NtmItems.INGOT_AUSTRALIUM.get());
        this.basicItem(NtmItems.INGOT_LANTHANIUM.get());
        this.basicItem(NtmItems.INGOT_ACTINIUM.get());
        this.basicItem(NtmItems.INGOT_DESH.get());
        this.basicItem(NtmItems.INGOT_FERROURANIUM.get());
        this.basicItem(NtmItems.INGOT_STARMETAL.get());
        this.basicItem(NtmItems.INGOT_GUNMETAL.get());
        this.basicItem(NtmItems.INGOT_SATURNITE.get());
        this.basicItem(NtmItems.INGOT_EUPHEMIUM.get());
        this.basicItem(NtmItems.INGOT_DINEUTRONIUM.get());
        this.basicItem(NtmItems.INGOT_ELECTRONIUM.get());
        this.basicItem(NtmItems.INGOT_SMORE.get());
        this.basicItem(NtmItems.INGOT_OSMIRIDIUM.get());
        this.basicItem(NtmItems.INGOT_ZIRCONIUM.get());
        this.basicItem(NtmItems.LITHIUM.get());
        this.basicItem(NtmItems.NUGGET_URANIUM.get());
        this.basicItem(NtmItems.NUGGET_U233.get());
        this.basicItem(NtmItems.NUGGET_U235.get());
        this.basicItem(NtmItems.NUGGET_U238.get());
        this.basicItem(NtmItems.NUGGET_U238M2.get());
        this.basicItem(NtmItems.NUGGET_PLUTONIUM.get());
        this.basicItem(NtmItems.NUGGET_PU238.get());
        this.basicItem(NtmItems.NUGGET_PU239.get());
        this.basicItem(NtmItems.NUGGET_PU240.get());
        this.basicItem(NtmItems.NUGGET_PU241.get());
        this.basicItem(NtmItems.NUGGET_PU_MIX.get());
        this.basicItem(NtmItems.NUGGET_AM241.get());
        this.basicItem(NtmItems.NUGGET_AM242.get());
        this.basicItem(NtmItems.NUGGET_AM_MIX.get());
        this.basicItem(NtmItems.NUGGET_TECHNETIUM.get());
        this.basicItem(NtmItems.NUGGET_NEPTUNIUM.get());
        this.basicItem(NtmItems.NUGGET_POLONIUM.get());
        this.basicItem(NtmItems.NUGGET_THORIUM_FUEL.get());
        this.basicItem(NtmItems.NUGGET_URANIUM_FUEL.get());
        this.basicItem(NtmItems.NUGGET_MOX_FUEL.get());
        this.basicItem(NtmItems.NUGGET_PLUTONIUM_FUEL.get());
        this.basicItem(NtmItems.NUGGET_NEPTUNIUM_FUEL.get());
        this.basicItem(NtmItems.NUGGET_AMERICIUM_FUEL.get());
        this.basicItem(NtmItems.NUGGET_SCHRABIDIUM_FUEL.get());
        this.basicItem(NtmItems.NUGGET_HES.get());
        this.basicItem(NtmItems.NUGGET_LES.get());
        this.basicItem(NtmItems.NUGGET_LEAD.get());
        this.basicItem(NtmItems.NUGGET_BERYLLIUM.get());
        this.basicItem(NtmItems.NUGGET_CADMIUM.get());
        this.basicItem(NtmItems.NUGGET_BISMUTH.get());
        this.basicItem(NtmItems.NUGGET_ARSENIC.get());
        this.basicItem(NtmItems.NUGGET_ZIRCONIUM.get());
        this.basicItem(NtmItems.NUGGET_TANTALIUM.get());
        this.basicItem(NtmItems.NUGGET_DESH.get());
        this.basicItem(NtmItems.NUGGET_OSMIRIDIUM.get());
        this.basicItem(NtmItems.NUGGET_SCHRABIDIUM.get());
        this.basicItem(NtmItems.NUGGET_SOLINIUM.get());
        this.basicItem(NtmItems.NUGGET_EUPHEMIUM.get());
        this.basicItem(NtmItems.NUGGET_DINEUTRONIUM.get());
        this.basicItem(NtmItems.NUGGET_NIOBIUM.get());
        this.basicItem(NtmItems.NUGGET_SILICON.get());
        this.basicItem(NtmItems.NUGGET_ACTINIUM.get());
        this.basicItem(NtmItems.NUGGET_COBALT.get());
        this.basicItem(NtmItems.NUGGET_CO60.get());
        this.basicItem(NtmItems.NUGGET_SR90.get());
        this.basicItem(NtmItems.NUGGET_PB209.get());
        this.basicItem(NtmItems.NUGGET_GH336.get());
        this.basicItem(NtmItems.NUGGET_AU198.get());
        this.basicItem(NtmItems.NUGGET_RA226.get());
        this.basicItem(NtmItems.POWDER_IRON.get());
        this.basicItem(NtmItems.POWDER_GOLD.get());
        this.basicItem(NtmItems.POWDER_DIAMOND.get());
        this.basicItem(NtmItems.POWDER_EMERALD.get());
        this.basicItem(NtmItems.POWDER_LAPIS.get());
        this.basicItem(NtmItems.POWDER_TITANIUM.get());
        this.basicItem(NtmItems.POWDER_TUNGSTEN.get());
        this.basicItem(NtmItems.POWDER_COPPER.get());
        this.basicItem(NtmItems.POWDER_BERYLLIUM.get());
        this.basicItem(NtmItems.POWDER_ALUMINIUM.get());
        this.basicItem(NtmItems.POWDER_LEAD.get());
        this.basicItem(NtmItems.POWDER_STEEL.get());
        this.basicItem(NtmItems.POWDER_COMBINE_STEEL.get());
        this.basicItem(NtmItems.POWDER_QUARTZ.get());
        this.basicItem(NtmItems.POWDER_SCHRABIDIUM.get());
        this.basicItem(NtmItems.POWDER_ASBESTOS.get());
        this.basicItem(NtmItems.POWDER_DURA_STEEL.get());
        this.basicItem(NtmItems.POWDER_POLYMER.get());
        this.basicItem(NtmItems.POWDER_BAKELITE.get());
        this.basicItem(NtmItems.POWDER_DESH.get());
        this.basicItem(NtmItems.POWDER_LITHIUM.get());
        this.basicItem(NtmItems.POWDER_COBALT.get());
        this.basicItem(NtmItems.POWDER_DINEUTRONIUM.get());
        this.basicItem(NtmItems.POWDER_COAL.get());
        this.basicItem(NtmItems.POWDER_BISMUTH.get());
        this.basicItem(NtmItems.POWDER_LIGNITE.get());
        this.basicItem(NtmItems.POWDER_ZIRCONIUM.get());
        this.basicItem(NtmItems.POWDER_URANIUM.get());
        this.basicItem(NtmItems.POWDER_NIOBIUM.get());
        this.basicItem(NtmItems.POWDER_METEORITE.get());
        this.basicItem(NtmItems.POWDER_METEORITE_TINY.get());
        this.basicItem(NtmItems.POWDER_ASH_WOOD.get());
        this.basicItem(NtmItems.POWDER_ASH_COAL.get());
        this.basicItem(NtmItems.POWDER_ASH_MISC.get());
        this.basicItem(NtmItems.POWDER_ASH_FLY.get());
        this.basicItem(NtmItems.POWDER_ASH_SOOT.get());
        this.basicItem(NtmItems.FILTER_COAL.get());

        this.basicItem(NtmItems.FRAGMENT_NIOBIUM.get());
        this.basicItem(NtmItems.FRAGMENT_COBALT.get());
        this.basicItem(NtmItems.FRAGMENT_NEODYMIUM.get());
        this.basicItem(NtmItems.FRAGMENT_CERIUM.get());
        this.basicItem(NtmItems.FRAGMENT_BORON.get());
        this.basicItem(NtmItems.FRAGMENT_LANTHANIUM.get());
        this.basicItem(NtmItems.FRAGMENT_ACTINIUM.get());
        this.basicItem(NtmItems.FRAGMENT_METEORITE.get());
        this.basicItem(NtmItems.COKE_COAL.get());
        this.basicItem(NtmItems.COKE_LIGNITE.get());
        this.basicItem(NtmItems.COKE_PETROLEUM.get());
        this.basicItem(NtmItems.BRIQUETTE_COAL.get());
        this.basicItem(NtmItems.BRIQUETTE_LIGNITE.get());
        this.basicItem(NtmItems.BRIQUETTE_WOOD.get());

        this.basicItem(NtmItems.OIL_TAR_CRUDE.get());
        this.basicItem(NtmItems.OIL_TAR_CRACK.get());
        this.basicItem(NtmItems.OIL_TAR_COAL.get());
        this.basicItem(NtmItems.OIL_TAR_PARAFFIN.get());
        this.basicItem(NtmItems.OIL_TAR_WOOD.get());
        this.basicItem(NtmItems.OIL_TAR_WAX.get());
        this.basicItem(NtmItems.SULFUR.get());
        this.basicItem(NtmItems.CINNABAR.get());
        this.basicItem(NtmItems.FRAGMENT_COBALT.get());
        this.basicItem(NtmItems.FLUORITE.get());
        this.basicItem(NtmItems.LIGNITE.get());
        this.basicItem(NtmItems.NITER.get());
        this.basicItem(NtmItems.RARE_EARTH_ORE_CHUNK.get());
        this.basicItem(NtmItems.DUST.get());
        this.basicItem(NtmItems.CHUNK_MALACHITE.get());
        this.basicItem(NtmItems.CHUNK_CRYOLITE.get());
        this.basicItem(NtmItems.NUCLEAR_WASTE.get());
        this.basicItem(NtmItems.NUCLEAR_WASTE_TINY.get());
        this.basicItem(NtmItems.NUCLEAR_WASTE_VITRIFIED.get());
        this.basicItem(NtmItems.NUCLEAR_WASTE_SHORT.get());
        this.basicItem(NtmItems.NUCLEAR_WASTE_SHORT_TINY.get());
        this.basicItem(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED.get());
        this.basicItem(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED_TINY.get());
        this.basicItem(NtmItems.NUCLEAR_WASTE_LONG.get());
        this.basicItem(NtmItems.NUCLEAR_WASTE_LONG_TINY.get());
        this.basicItem(NtmItems.NUCLEAR_WASTE_LONG_DEPLETED.get());
        this.basicItem(NtmItems.NUCLEAR_WASTE_LONG_DEPLETED_TINY.get());
        this.basicItem(NtmItems.SCRAP_NUCLEAR.get());
        this.basicItem(NtmItems.REACTOR_CORE.get());
        this.basicItem(NtmItems.PLATE_IRON.get());
        this.basicItem(NtmItems.PLATE_GOLD.get());
        this.basicItem(NtmItems.PLATE_TITANIUM.get());
        this.basicItem(NtmItems.PLATE_ALUMINIUM.get());
        this.basicItem(NtmItems.PLATE_STEEL.get());
        this.basicItem(NtmItems.PLATE_LEAD.get());
        this.basicItem(NtmItems.PLATE_COPPER.get());
        this.basicItem(NtmItems.PLATE_GUNMETAL.get());
        this.basicItem(NtmItems.PLATE_WEAPON_STEEL.get());
        this.basicItem(NtmItems.PLATE_SATURNITE.get());
        this.basicItem(NtmItems.PLATE_DURA_STEEL.get());
        this.basicItem(NtmItems.PLATE_SCHRABIDIUM.get());
        this.basicItem(NtmItems.PLATE_COMBINE_STEEL.get());
        this.basicItem(NtmItems.PLATE_BISMUTH.get());
        this.basicItem(NtmItems.WIRE_GOLD.get());
        this.basicItem(NtmItems.WIRE_COPPER.get());
        this.basicItem(NtmItems.WIRE_ALUMINIUM.get());
        this.basicItem(NtmItems.WIRE_ZIRCONIUM.get());
        this.basicItem(NtmItems.WIRE_LEAD.get());
        this.basicItem(NtmItems.WIRE_TUNGSTEN.get());
        this.basicItem(NtmItems.WIRE_SCHRABIDIUM.get());
        this.basicItem(NtmItems.WIRE_STEEL.get());
        this.basicItem(NtmItems.WIRE_MAGNETIZED_TUNGSTEN.get());
        this.basicItem(NtmItems.WIRE_CARBON.get());
        this.basicItem(NtmItems.WIRE_RED_COPPER.get());
        this.basicItem(NtmItems.SHELL_TITANIUM.get());
        this.basicItem(NtmItems.SHELL_ALUMINIUM.get());
        this.basicItem(NtmItems.SHELL_COPPER.get());
        this.basicItem(NtmItems.SHELL_STEEL.get());
        this.basicItem(NtmItems.SHELL_WEAPON_STEEL.get());
        this.basicItem(NtmItems.SHELL_SATURNITE.get());
        this.basicItem(NtmItems.PIPE_IRON.get());
        this.basicItem(NtmItems.PIPE_COPPER.get());
        this.basicItem(NtmItems.PIPE_ALUMINIUM.get());
        this.basicItem(NtmItems.PIPE_LEAD.get());
        this.basicItem(NtmItems.PIPE_STEEL.get());
        this.basicItem(NtmItems.PIPE_DURA_STEEL.get());
        this.basicItem(NtmItems.PIPE_RUBBER.get());
        this.basicItem(NtmItems.WIRE_DENSE.get());
        this.layeredItem(NtmItems.BOLT.get(), "bolt_dark", "bolt_light");

        this.basicItem(NtmItems.CIRCUIT_PRINTED_BOARD.get());
        this.basicItem(NtmItems.CIRCUIT_ANALOG_BOARD.get());
        this.basicItem(NtmItems.CIRCUIT_INTEGRATED_BOARD.get());
        this.basicItem(NtmItems.CIRCUIT_MILITARY_GRADE_BOARD.get());
        this.basicItem(NtmItems.CIRCUIT_VERSATILE_INTEGRATED.get());
        this.basicItem(NtmItems.CIRCUIT_VERSATILE_BOARD.get());
        this.basicItem(NtmItems.CIRCUIT_CAPACITOR.get());
        this.basicItem(NtmItems.CIRCUIT_TANTALIUM_CAPACITOR.get());
        this.basicItem(NtmItems.CIRCUIT_CAPACITOR_BOARD.get());
        this.basicItem(NtmItems.CIRCUIT_VACUUM_TUBE.get());
        this.basicItem(NtmItems.CIRCUIT_PRINTED_SILICON_WAFER.get());
        this.basicItem(NtmItems.CIRCUIT_MICROCHIP.get());
        this.basicItem(NtmItems.CIRCUIT_CONTROL_UNIT_CASING.get());
        this.basicItem(NtmItems.CIRCUIT_CONTROL_UNIT.get());
        this.basicItem(NtmItems.CIRCUIT_ADVANCED_CONTROL_UNIT.get());
        this.basicItem(NtmItems.CIRCUIT_SOLID_STATE_QUANTUM_PROCESSOR.get());
        this.basicItem(NtmItems.CIRCUIT_QUANTUM_PROCESSING_UNIT.get());
        this.basicItem(NtmItems.CIRCUIT_QUANTUM_COMPUTER.get());
        this.basicItem(NtmItems.CIRCUIT_ATOMIC_CLOCK.get());
        this.basicItem(NtmItems.COIL_COPPER.get());
        this.basicItem(NtmItems.COIL_COPPER_RING.get());
        this.basicItem(NtmItems.COIL_GOLD.get());
        this.basicItem(NtmItems.COIL_GOLD_RING.get());
        this.basicItem(NtmItems.COIL_MAGNETIZED_TUNGSTEN.get());
        this.basicItem(NtmItems.COIL_TUNGSTEN.get());
        this.basicItem(NtmItems.TANK_STEEL.get());
        this.basicItem(NtmItems.CATALYST_CLAY.get());
        this.basicItem(NtmItems.DEUTERIUM_FILTER.get());
        this.basicItem(NtmItems.FINS_FLAT.get());
        this.basicItem(NtmItems.FINS_SMALL_STEEL.get());
        this.basicItem(NtmItems.FINS_BIG_STEEL.get());
        this.basicItem(NtmItems.FINS_TRI_STEEL.get());
        this.basicItem(NtmItems.FINS_QUAD_TITANIUM.get());
        this.basicItem(NtmItems.SPHERE_STEEL.get());
        this.basicItem(NtmItems.PEDESTAL_STEEL.get());
        this.basicItem(NtmItems.BLADE_TITANIUM.get());
        this.basicItem(NtmItems.BLADE_TUNGSTEN.get());
        this.basicItem(NtmItems.TURBINE_TITANIUM.get());
        this.basicItem(NtmItems.TURBINE_TUNGSTEN.get());
        this.basicItem(NtmItems.FLYWHEEL_BERYLLIUM.get());
        this.basicItem(NtmItems.TOOTHPICKS.get());
        this.basicItem(NtmItems.DUCTTAPE.get());
        this.basicItem(NtmItems.PLANT_ITEM.get());
        this.basicItem(NtmItems.CANISTER_EMPTY.get());
        this.basicItem(NtmItems.CANISTER_NAPALM.get());
        this.basicItem(NtmItems.BIOMASS.get());
        this.basicItem(NtmItems.PELLET_CHARGED.get());
        this.basicItem(NtmItems.GLYPHID_MEAT.get());
        this.basicItem(NtmItems.SOLID_FUEL.get());
        this.basicItem(NtmItems.ROCKET_FUEL.get());
        this.basicItem(NtmItems.CORDITE.get());
        this.basicItem(NtmItems.CHOCOLATE.get());
        this.basicItem(NtmItems.BALL_DYNAMITE.get());
        this.basicItem(NtmItems.BALL_TNT.get());
        this.basicItem(NtmItems.BALL_TATB.get());
        this.basicItem(NtmItems.BALL_TATB.get());
        this.basicItem(NtmItems.PELLET_CLUSTER.get());
        this.basicItem(NtmItems.MAGNETRON.get());
        this.basicItem(NtmItems.PHOTO_PANEL.get());
        this.basicItem(NtmItems.MISSILE_ASSEMBLY.get());
        this.basicItem(NtmItems.THRUSTER_SMALL.get());
        this.basicItem(NtmItems.THRUSTER_MEDIUM.get());
        this.basicItem(NtmItems.THRUSTER_LARGE.get());
        this.basicItem(NtmItems.FUEL_TANK_SMALL.get());
        this.basicItem(NtmItems.FUEL_TANK_MEDIUM.get());
        this.basicItem(NtmItems.FUEL_TANK_LARGE.get());
        this.basicItem(NtmItems.WARHEAD_GENERIC_SMALL.get());
        this.basicItem(NtmItems.WARHEAD_GENERIC_MEDIUM.get());
        this.basicItem(NtmItems.WARHEAD_GENERIC_LARGE.get());
        this.basicItem(NtmItems.WARHEAD_INCENDIARY_SMALL.get());
        this.basicItem(NtmItems.WARHEAD_INCENDIARY_MEDIUM.get());
        this.basicItem(NtmItems.WARHEAD_INCENDIARY_LARGE.get());
        this.basicItem(NtmItems.WARHEAD_CLUSTER_SMALL.get());
        this.basicItem(NtmItems.WARHEAD_CLUSTER_MEDIUM.get());
        this.basicItem(NtmItems.WARHEAD_CLUSTER_LARGE.get());
        this.basicItem(NtmItems.WARHEAD_BUSTER_SMALL.get());
        this.basicItem(NtmItems.WARHEAD_BUSTER_MEDIUM.get());
        this.basicItem(NtmItems.WARHEAD_BUSTER_LARGE.get());
        this.basicItem(NtmItems.WARHEAD_NUCLEAR.get());
        this.basicItem(NtmItems.WARHEAD_MIRV.get());
        this.basicItem(NtmItems.WARHEAD_VOLCANO.get());
        this.basicItem(NtmItems.NEUTRON_REFLECTOR.get());
        this.basicItem(NtmItems.ARC_ELECTRODE_GRAPHITE.get());
        this.basicItem(NtmItems.ARC_ELECTRODE_LANTHANIUM.get());
        this.basicItem(NtmItems.ARC_ELECTRODE_DESH.get());
        this.basicItem(NtmItems.ARC_ELECTRODE_SATURNITE.get());
        this.basicItem(NtmItems.ARC_ELECTRODE_GRAPHITE_BURNT.get());
        this.basicItem(NtmItems.ARC_ELECTRODE_LANTHANIUM_BURNT.get());
        this.basicItem(NtmItems.ARC_ELECTRODE_DESH_BURNT.get());
        this.basicItem(NtmItems.ARC_ELECTRODE_SATURNITE_BURNT.get());

        this.basicItem(NtmItems.MOTOR.get());
        this.basicItem(NtmItems.MOTOR_DESH.get());
        this.basicItem(NtmItems.CENTRIFUGE_ELEMENT.get());
        this.basicItem(NtmItems.CAP_NUKA.get());
        this.basicItem(NtmItems.SYRINGE_EMPTY.get());
        this.basicItem(NtmItems.SYRINGE_METAL_EMPTY.get());
        this.basicItem(NtmItems.SYRINGE_METAL_STIMPAK.get());
        this.basicItem(NtmItems.SYRINGE_METAL_MEDX.get());
        this.basicItem(NtmItems.SYRINGE_METAL_PSYCHO.get());
        this.basicItem(NtmItems.SYRINGE_METAL_SUPER.get());
        this.basicItem(NtmItems.PISTON_SELENIUM.get());
        this.basicItem(NtmItems.CAST_PLATE_WELDED.get());
        this.basicItem(NtmItems.CAST_PLATE.get());
        this.basicItem(NtmItems.PIPES_STEEL.get());
        this.basicItem(NtmItems.CRT_DISPLAY.get());
        this.basicItem(NtmItems.RING_STARMETAL.get());

        this.basicItem(NtmItems.CRYSTAL_IRON.get());
        this.basicItem(NtmItems.CRYSTAL_GOLD.get());
        this.basicItem(NtmItems.CRYSTAL_REDSTONE.get());
        this.basicItem(NtmItems.CRYSTAL_LAPIS.get());
        this.basicItem(NtmItems.CRYSTAL_DIAMOND.get());
        this.basicItem(NtmItems.CRYSTAL_URANIUM.get());
        this.basicItem(NtmItems.CRYSTAL_THORIUM.get());
        this.basicItem(NtmItems.CRYSTAL_PLUTONIUM.get());
        this.basicItem(NtmItems.CRYSTAL_TITANIUM.get());
        this.basicItem(NtmItems.CRYSTAL_SULFUR.get());
        this.basicItem(NtmItems.CRYSTAL_NITER.get());
        this.basicItem(NtmItems.CRYSTAL_COPPER.get());
        this.basicItem(NtmItems.CRYSTAL_TUNGSTEN.get());
        this.basicItem(NtmItems.CRYSTAL_ALUMINIUM.get());
        this.basicItem(NtmItems.CRYSTAL_FLUORITE.get());
        this.basicItem(NtmItems.CRYSTAL_BERYLLIUM.get());
        this.basicItem(NtmItems.CRYSTAL_LEAD.get());
        this.basicItem(NtmItems.CRYSTAL_ASBESTOS.get());
        this.basicItem(NtmItems.CRYSTAL_SCHRARANIUM.get());
        this.basicItem(NtmItems.CRYSTAL_SCHRABIDIUM.get());
        this.basicItem(NtmItems.CRYSTAL_RARE.get());
        this.basicItem(NtmItems.CRYSTAL_PHOSPHORUS.get());
        this.basicItem(NtmItems.CRYSTAL_LITHIUM.get());
        this.basicItem(NtmItems.CRYSTAL_CINNABAR.get());
        this.basicItem(NtmItems.CRYSTAL_COBALT.get());
        this.basicItem(NtmItems.CRYSTAL_STARMETAL.get());
        this.basicItem(NtmItems.CRYSTAL_TRIXITE.get());
        this.basicItem(NtmItems.CRYSTAL_OSMIRIDIUM.get());
        this.basicItem(NtmItems.BILLET_COBALT.get());
        this.basicItem(NtmItems.BILLET_TH232.get());
        this.basicItem(NtmItems.BILLET_URANIUM.get());
        this.basicItem(NtmItems.BILLET_U233.get());
        this.basicItem(NtmItems.BILLET_U235.get());
        this.basicItem(NtmItems.BILLET_U238.get());
        this.basicItem(NtmItems.BILLET_UZH.get());
        this.basicItem(NtmItems.BILLET_PLUTONIUM.get());
        this.basicItem(NtmItems.BILLET_PU238.get());
        this.basicItem(NtmItems.BILLET_PU239.get());
        this.basicItem(NtmItems.BILLET_PU240.get());
        this.basicItem(NtmItems.BILLET_PU241.get());
        this.basicItem(NtmItems.BILLET_PU_MIX.get());
        this.basicItem(NtmItems.BILLET_AM241.get());
        this.basicItem(NtmItems.BILLET_AM242.get());
        this.basicItem(NtmItems.BILLET_AM_MIX.get());
        this.basicItem(NtmItems.BILLET_NEPTUNIUM.get());
        this.basicItem(NtmItems.BILLET_POLONIUM.get());
        this.basicItem(NtmItems.BILLET_TECHNETIUM.get());
        this.basicItem(NtmItems.BILLET_CO60.get());
        this.basicItem(NtmItems.BILLET_SR90.get());
        this.basicItem(NtmItems.BILLET_AU198.get());
        this.basicItem(NtmItems.BILLET_PB209.get());
        this.basicItem(NtmItems.BILLET_RA226.get());
        this.basicItem(NtmItems.BILLET_ACTINIUM.get());
        this.basicItem(NtmItems.BILLET_GH336.get());
        this.basicItem(NtmItems.BILLET_BERYLLIUM.get());
        this.basicItem(NtmItems.BILLET_BISMUTH.get());
        this.basicItem(NtmItems.BILLET_ZIRCONIUM.get());
        this.basicItem(NtmItems.BILLET_ZFB_BISMUTH.get());
        this.basicItem(NtmItems.BILLET_ZFB_PU241.get());
        this.basicItem(NtmItems.BILLET_ZFB_AM_MIX.get());
        this.basicItem(NtmItems.BILLET_SCHRABIDIUM.get());
        this.basicItem(NtmItems.BILLET_SOLINIUM.get());
        this.basicItem(NtmItems.BILLET_THORIUM_FUEL.get());
        this.basicItem(NtmItems.BILLET_URANIUM_FUEL.get());
        this.basicItem(NtmItems.BILLET_MOX_FUEL.get());
        this.basicItem(NtmItems.BILLET_PLUTONIUM_FUEL.get());
        this.basicItem(NtmItems.BILLET_NEPTUNIUM_FUEL.get());
        this.basicItem(NtmItems.BILLET_AMERICIUM_FUEL.get());
        this.basicItem(NtmItems.BILLET_LES.get());
        this.basicItem(NtmItems.BILLET_SCHRABIDIUM_FUEL.get());
        this.basicItem(NtmItems.BILLET_HES.get());
        this.basicItem(NtmItems.BILLET_PO210BE.get());
        this.basicItem(NtmItems.BILLET_RA226BE.get());
        this.basicItem(NtmItems.BILLET_PU238BE.get());
        this.basicItem(NtmItems.BILLET_AUSTRALIUM.get());
        this.basicItem(NtmItems.BILLET_AUSTRALIUM_LESSER.get());
        this.basicItem(NtmItems.BILLET_AUSTRALIUM_GREATER.get());
        this.basicItem(NtmItems.BILLET_UNOBTAINIUM.get());
        this.basicItem(NtmItems.BILLET_YHARONITE.get());
        this.basicItem(NtmItems.BILLET_BALEFIRE_GOLD.get());
        this.basicItem(NtmItems.BILLET_FLASHLEAD.get());
        this.basicItem(NtmItems.BILLET_NUCLEAR_WASTE.get());
        this.basicItem(NtmItems.PLATE_POLYMER.get());
        this.basicItem(NtmItems.INGOT_COPPER.get());
        this.basicItem(NtmItems.INGOT_ASTATINE.get());
        this.basicItem(NtmItems.INGOT_BROMINE.get());
        this.basicItem(NtmItems.INGOT_C4.get());
        this.basicItem(NtmItems.INGOT_CAESIUM.get());
        this.basicItem(NtmItems.INGOT_CERIUM.get());
        this.basicItem(NtmItems.INGOT_CHAINSTEEL.get());
        this.basicItem(NtmItems.INGOT_DAFFERGON.get());
        this.basicItem(NtmItems.INGOT_FIBERGLASS.get());
        this.basicItem(NtmItems.INGOT_I131.get());
        this.basicItem(NtmItems.INGOT_IODINE.get());
        this.basicItem(NtmItems.INGOT_METEORITE.get());
        this.basicItem(NtmItems.INGOT_METEORITE_FORGED.get());
        this.basicItem(NtmItems.INGOT_PHOSPHORUS.get());
        this.basicItem(NtmItems.INGOT_RAW.get());
        this.basicItem(NtmItems.INGOT_RED_COPPER.get());
        this.basicItem(NtmItems.INGOT_REIIUM.get());
        this.basicItem(NtmItems.INGOT_SEMTEX.get());
        this.basicItem(NtmItems.INGOT_STEEL_DUSTED.get());
        this.basicItem(NtmItems.INGOT_TENNESSINE.get());
        this.basicItem(NtmItems.INGOT_TH232.get());
        this.basicItem(NtmItems.INGOT_TUNGSTEN_CARBIDE.get());
        this.basicItem(NtmItems.INGOT_UNOBTAINIUM.get());
        this.basicItem(NtmItems.INGOT_VERTICIUM.get());
        this.basicItem(NtmItems.INGOT_WEIDANIUM.get());
        this.basicItem(NtmItems.NUGGET_AUSTRALIUM.get());
        this.basicItem(NtmItems.NUGGET_AUSTRALIUM_GREATER.get());
        this.basicItem(NtmItems.NUGGET_AUSTRALIUM_LESSER.get());
        this.basicItem(NtmItems.NUGGET_DAFFERGON.get());
        this.basicItem(NtmItems.NUGGET_MERCURY.get());
        this.basicItem(NtmItems.NUGGET_REIIUM.get());
        this.basicItem(NtmItems.NUGGET_STRONTIUM.get());
        this.basicItem(NtmItems.NUGGET_TH232.get());
        this.basicItem(NtmItems.NUGGET_UNOBTAINIUM.get());
        this.basicItem(NtmItems.NUGGET_UNOBTAINIUM_GREATER.get());
        this.basicItem(NtmItems.NUGGET_UNOBTAINIUM_LESSER.get());
        this.basicItem(NtmItems.NUGGET_VERTICIUM.get());
        this.basicItem(NtmItems.NUGGET_WEIDANIUM.get());
        this.basicItem(NtmItems.PLATE_ARMOR_AJR.get());
        this.basicItem(NtmItems.PLATE_ARMOR_DNT.get());
        this.basicItem(NtmItems.PLATE_ARMOR_FAU.get());
        this.basicItem(NtmItems.PLATE_ARMOR_HEV.get());
        this.basicItem(NtmItems.PLATE_ARMOR_LUNAR.get());
        this.basicItem(NtmItems.PLATE_ARMOR_TITANIUM.get());
        this.basicItem(NtmItems.PLATE_DALEKANIUM.get());
        this.basicItem(NtmItems.PLATE_DESH.get());
        this.basicItem(NtmItems.PLATE_DINEUTRONIUM.get());
        this.basicItem(NtmItems.PLATE_EUPHEMIUM.get());
        this.basicItem(NtmItems.PLATE_FUEL_MOX.get());
        this.basicItem(NtmItems.PLATE_FUEL_PU238BE.get());
        this.basicItem(NtmItems.PLATE_FUEL_PU239.get());
        this.basicItem(NtmItems.PLATE_FUEL_RA226BE.get());
        this.basicItem(NtmItems.PLATE_FUEL_SA326.get());
        this.basicItem(NtmItems.PLATE_FUEL_U233.get());
        this.basicItem(NtmItems.PLATE_FUEL_U235.get());
        this.basicItem(NtmItems.REACTOR_SENSOR.get());
        this.basicItem(NtmItems.TURRET_CHIP.get());
        this.basicItem(NtmItems.AMMO_DGK.get());
        /* Beide sind EnumMultiItem ohne eigene Textur je Wert -- sie tragen viele Namen, aber
         * ein Bild. EnumMultiItem.registerItemModel legt fuer sie nichts an, und hier wurden
         * sie vergessen; im Spielprotokoll vom 16.09. zeigten sie den fehlenden-Modell-Wuerfel. */
        this.basicItem(NtmItems.PWR_FUEL_HOT.get());
        this.basicItem(NtmItems.PWR_FUEL_DEPLETED.get());
        /* Der Debugmunition fehlt eine Textur -- auch das Original hat keine. Damit sie nicht
         * als fehlendes Modell erscheint, laeuft sie auf den Platzhalter. */
        this.withExistingParent("ammo_debug", mcLoc("item/generated")).texture("layer0", modLoc("item/nothing"));
        this.basicItem(NtmItems.PLATE_KEVLAR.get());
        this.basicItem(NtmItems.PLATE_MIXED.get());
        this.basicItem(NtmItems.PLATE_PAA.get());
        this.basicItem(NtmItems.POWDER_ACTINIUM.get());
        this.basicItem(NtmItems.POWDER_ACTINIUM_TINY.get());
        this.basicItem(NtmItems.POWDER_ASTATINE.get());
        this.basicItem(NtmItems.POWDER_AT209.get());
        this.basicItem(NtmItems.POWDER_AT209_TINY.get());
        this.basicItem(NtmItems.POWDER_AU198.get());
        this.basicItem(NtmItems.POWDER_AU198_TINY.get());
        this.basicItem(NtmItems.POWDER_AUSTRALIUM.get());
        this.basicItem(NtmItems.POWDER_BALEFIRE.get());
        this.basicItem(NtmItems.POWDER_BORAX.get());
        this.basicItem(NtmItems.POWDER_BORON.get());
        this.basicItem(NtmItems.POWDER_BORON_TINY.get());
        this.basicItem(NtmItems.POWDER_BROMINE.get());
        this.basicItem(NtmItems.POWDER_CADMIUM.get());
        this.basicItem(NtmItems.POWDER_CAESIUM.get());
        this.basicItem(NtmItems.POWDER_CALCIUM.get());
        this.basicItem(NtmItems.POWDER_CDALLOY.get());
        this.basicItem(NtmItems.POWDER_CEMENT.get());
        this.basicItem(NtmItems.POWDER_CERIUM.get());
        this.basicItem(NtmItems.POWDER_CERIUM_TINY.get());
        this.basicItem(NtmItems.POWDER_CHLOROCALCITE.get());
        this.basicItem(NtmItems.POWDER_CHLOROPHYTE.get());
        this.basicItem(NtmItems.POWDER_CLOUD.get());
        this.basicItem(NtmItems.POWDER_CO60.get());
        this.basicItem(NtmItems.POWDER_CO60_TINY.get());
        this.basicItem(NtmItems.POWDER_COAL_TINY.get());
        this.basicItem(NtmItems.POWDER_COBALT_TINY.get());
        this.basicItem(NtmItems.POWDER_COLTAN.get());
        this.basicItem(NtmItems.POWDER_COLTAN_ORE.get());
        this.basicItem(NtmItems.POWDER_CS137.get());
        this.basicItem(NtmItems.POWDER_CS137_TINY.get());
        this.basicItem(NtmItems.POWDER_DAFFERGON.get());
        this.basicItem(NtmItems.POWDER_DESH_MIX.get());
        this.basicItem(NtmItems.POWDER_DESH_READY.get());
        this.basicItem(NtmItems.POWDER_EUPHEMIUM.get());
        this.basicItem(NtmItems.POWDER_FERTILIZER.get());
        this.basicItem(NtmItems.POWDER_FIRE.get());
        this.basicItem(NtmItems.POWDER_FLUX.get());
        this.basicItem(NtmItems.POWDER_I131.get());
        this.basicItem(NtmItems.POWDER_I131_TINY.get());
        this.basicItem(NtmItems.POWDER_ICE.get());
        this.basicItem(NtmItems.POWDER_IMPURE_OSMIRIDIUM.get());
        this.basicItem(NtmItems.POWDER_IODINE.get());
        this.basicItem(NtmItems.POWDER_IODINE_TINY.get());
        this.basicItem(NtmItems.POWDER_LANTHANIUM.get());
        this.basicItem(NtmItems.POWDER_LANTHANIUM_TINY.get());
        this.basicItem(NtmItems.POWDER_LIMESTONE.get());
        this.basicItem(NtmItems.POWDER_LITHIUM_TINY.get());
        this.basicItem(NtmItems.POWDER_MAGIC.get());
        this.basicItem(NtmItems.POWDER_MAGNETIZED_TUNGSTEN.get());
        this.basicItem(NtmItems.POWDER_MOLYSITE.get());
        this.basicItem(NtmItems.POWDER_NEODYMIUM.get());
        this.basicItem(NtmItems.POWDER_NEODYMIUM_TINY.get());
        this.basicItem(NtmItems.POWDER_NEPTUNIUM.get());
        this.basicItem(NtmItems.POWDER_NIOBIUM_TINY.get());
        this.basicItem(NtmItems.POWDER_NITAN_MIX.get());
        this.basicItem(NtmItems.POWDER_OSMIRIDIUM.get());
        this.basicItem(NtmItems.POWDER_PALEOGENITE.get());
        this.basicItem(NtmItems.POWDER_PALEOGENITE_TINY.get());
        this.basicItem(NtmItems.POWDER_PB209.get());
        this.basicItem(NtmItems.POWDER_PB209_TINY.get());
        this.basicItem(NtmItems.POWDER_PLUTONIUM.get());
        this.basicItem(NtmItems.POWDER_POISON.get());
        this.basicItem(NtmItems.POWDER_POLONIUM.get());
        this.basicItem(NtmItems.POWDER_POWER.get());
        this.basicItem(NtmItems.POWDER_RA226.get());
        this.basicItem(NtmItems.POWDER_RED_COPPER.get());
        this.basicItem(NtmItems.POWDER_REIIUM.get());
        this.basicItem(NtmItems.POWDER_SAWDUST.get());
        this.basicItem(NtmItems.POWDER_SCHRABIDATE.get());
        this.basicItem(NtmItems.POWDER_SEMTEX_MIX.get());
        this.basicItem(NtmItems.POWDER_SODIUM.get());
        this.basicItem(NtmItems.POWDER_SPARK_MIX.get());
        this.basicItem(NtmItems.POWDER_SR90.get());
        this.basicItem(NtmItems.POWDER_SR90_TINY.get());
        this.basicItem(NtmItems.POWDER_STEEL_TINY.get());
        this.basicItem(NtmItems.POWDER_STRONTIUM.get());
        this.basicItem(NtmItems.POWDER_TANTALIUM.get());
        this.basicItem(NtmItems.POWDER_TCALLOY.get());
        this.basicItem(NtmItems.POWDER_TEKTITE.get());
        this.basicItem(NtmItems.POWDER_TENNESSINE.get());
        this.basicItem(NtmItems.POWDER_THERMITE.get());
        this.basicItem(NtmItems.POWDER_THORIUM.get());
        this.basicItem(NtmItems.POWDER_UNOBTAINIUM.get());
        this.basicItem(NtmItems.POWDER_VERTICIUM.get());
        this.basicItem(NtmItems.POWDER_WEIDANIUM.get());
        this.basicItem(NtmItems.POWDER_XE135.get());
        this.basicItem(NtmItems.POWDER_XE135_TINY.get());
        this.basicItem(NtmItems.POWDER_YELLOWCAKE.get());
        this.basicItem(NtmItems.GEM_ALEXANDRITE.get());
        this.basicItem(NtmItems.GEM_RAD.get());
        this.basicItem(NtmItems.RADAR_LINKER.get());
        /* Die Kassette: unten das Gehaeuse, oben der Aufkleber -- den faerbt der Farbgeber
         * nach der Spur ein (siehe NuclearTechModClient.registerItemColors). */
        this.layeredItem(NtmItems.CASSETTE.get(), "cassette", "cassette_overlay");
        this.basicItem(NtmItems.GEM_SODALITE.get());
        this.basicItem(NtmItems.GEM_TANTALIUM.get());
        this.basicItem(NtmItems.GEM_VOLCANIC.get());
        this.basicItem(NtmItems.CRYSTAL_CHARRED.get());
        this.basicItem(NtmItems.CRYSTAL_COAL.get());
        this.basicItem(NtmItems.CRYSTAL_ENERGY.get());
        this.basicItem(NtmItems.CRYSTAL_HORN.get());
        this.basicItem(NtmItems.CRYSTAL_XEN.get());

        /*
         * Beide Watz-Pellets teilen sich eine Graustufenvorlage; die Farbe der Sorte kommt zur
         * Laufzeit aus dem Farbgeber (siehe NuclearTechModClient.registerItemColors).
         */
        /*
         * Das ICF-Kuegelchen: untere Lage der eingefaerbte Hintergrund, obere das Symbol.
         * Im Original sind das zwei Renderdurchgaenge.
         */
        this.layeredItem(NtmItems.ICF_PELLET.get(), "icf_pellet_bg", "icf_pellet");
        this.basicItem(NtmItems.ICF_PELLET_DEPLETED.get());
        this.basicItem(NtmItems.ICF_PELLET_EMPTY.get());
        this.basicItem(NtmItems.PARTICLE_EMPTY.get());
        this.basicItem(NtmItems.PARTICLE_MUON.get());

        this.basicItem(NtmItems.WATZ_PELLET.get());
        this.basicCustomLayerItem(NtmItems.WATZ_PELLET_DEPLETED.get(), "watz_pellet");

        // PELLET_RTG und PELLET_RTG_DEPLETED nutzen ICustomItemModelRegister (EnumMultiItem)
        this.basicItem(NtmItems.THERMO_ELEMENT.get());
        this.basicItem(NtmItems.RTG_UNIT.get());
        this.basicItem(NtmItems.SAWBLADE.get());
        this.basicItem(NtmItems.GEAR_LARGE.get());

        this.basicItem(NtmItems.CELL_EMPTY.get());
        this.basicItem(NtmItems.CELL_UF6.get());
        this.basicItem(NtmItems.CELL_PUF6.get());
        this.basicItem(NtmItems.CELL_ANTIMATTER.get());
        this.basicItem(NtmItems.CELL_DEUTERIUM.get());
        this.basicItem(NtmItems.CELL_TRITIUM.get());
        this.basicItem(NtmItems.CELL_SAS3.get());
        this.basicItem(NtmItems.CELL_ANTI_SCHARBIDIUM.get());
        this.basicItem(NtmItems.CELL_BALEFIRE.get());

        this.basicItem(NtmItems.PARTICLE_DIGAMMA.get());
        this.basicItem(NtmItems.PARTICLE_LUTECE.get());

        this.basicItem(NtmItems.SINGULARITY.get());
        this.basicItem(NtmItems.SINGULARITY_COUNTER_RESONANT.get());
        this.basicItem(NtmItems.SINGULARITY_SUPER_HEATED.get());
        this.basicItem(NtmItems.BLACK_HOLE.get());
        this.basicItem(NtmItems.SINGULARITY_SPARK.get());
        this.basicItem(NtmItems.PELLET_ANTIMATTER.get());

        this.basicItem(NtmItems.INF_WATER.get());
        this.basicItem(NtmItems.INF_WATER_MK2.get());

        this.basicCustomLayerItem(NtmItems.FLUID_TANK_EMPTY.get(), "fluid_tank");
        this.layeredItem(NtmItems.FLUID_TANK_FULL.get(), "fluid_tank", "fluid_tank_overlay");
        this.basicCustomLayerItem(NtmItems.FLUID_TANK_LEAD_EMPTY.get(), "fluid_tank_lead");
        this.layeredItem(NtmItems.FLUID_TANK_LEAD_FULL.get(), "fluid_tank_lead", "fluid_tank_lead_overlay");
        this.basicCustomLayerItem(NtmItems.FLUID_BARREL_EMPTY.get(), "fluid_barrel_empty");
        this.layeredItem(NtmItems.FLUID_BARREL_FULL.get(), "fluid_barrel", "fluid_barrel_overlay");
        this.layeredItem(NtmItems.CANISTER_FULL.get(), "canister_empty", "canister_overlay");
        this.basicItem(NtmItems.GAS_EMPTY.get());
        /* Drei Schichten: Rumpf, gefaerbter Flaschenkoerper, gefaerbtes Etikett. */
        this.layeredItem(NtmItems.GAS_FULL.get(), "gas_empty", "gas_bottle").texture("layer2", modLoc("item/gas_label"));
        this.basicItem(NtmItems.FLUID_BARREL_INFINITE.get());

        this.basicCustomLayerItem(NtmItems.FLUID_PACK_EMPTY.get(), "fluid_pack");
        this.layeredItem(NtmItems.FLUID_PACK_FULL.get(), "fluid_pack", "fluid_pack_overlay");

        this.basicItem(NtmItems.BATTERY_SPARK.get());
        this.basicItem(NtmItems.BATTERY_TRIXITE.get());

        this.entityItem(NtmItems.BATTERY_PACK.get(), false);
        // BATTERY_SC uses ICustomItemModelRegister
        this.basicItem(NtmItems.BATTERY_CREATIVE.get());

        // todo register BLUEPRINTS

        this.basicItem(NtmItems.STAMP_STONE_FLAT.get());
        this.basicItem(NtmItems.STAMP_STONE_PLATE.get());
        this.basicItem(NtmItems.STAMP_STONE_WIRE.get());
        this.basicItem(NtmItems.STAMP_STONE_CIRCUIT.get());
        this.basicItem(NtmItems.STAMP_IRON_FLAT.get());
        this.basicItem(NtmItems.STAMP_IRON_PLATE.get());
        this.basicItem(NtmItems.STAMP_IRON_WIRE.get());
        this.basicItem(NtmItems.STAMP_IRON_CIRCUIT.get());
        this.basicItem(NtmItems.STAMP_STEEL_FLAT.get());
        this.basicItem(NtmItems.STAMP_STEEL_PLATE.get());
        this.basicItem(NtmItems.STAMP_STEEL_WIRE.get());
        this.basicItem(NtmItems.STAMP_STEEL_CIRCUIT.get());
        this.basicItem(NtmItems.STAMP_TITANIUM_FLAT.get());
        this.basicItem(NtmItems.STAMP_TITANIUM_PLATE.get());
        this.basicItem(NtmItems.STAMP_TITANIUM_WIRE.get());
        this.basicItem(NtmItems.STAMP_TITANIUM_CIRCUIT.get());
        this.basicItem(NtmItems.STAMP_OBSIDIAN_FLAT.get());
        this.basicItem(NtmItems.STAMP_OBSIDIAN_PLATE.get());
        this.basicItem(NtmItems.STAMP_OBSIDIAN_WIRE.get());
        this.basicItem(NtmItems.STAMP_OBSIDIAN_CIRCUIT.get());
        this.basicItem(NtmItems.STAMP_DESH_FLAT.get());
        this.basicItem(NtmItems.STAMP_9.get());
        this.basicItem(NtmItems.STAMP_50.get());
        /* Die Bilder der Desh-Stempel heissen stamp_9_desh, nicht stamp_desh_9. */
        this.withExistingParent("stamp_desh_9", mcLoc("item/generated")).texture("layer0", modLoc("item/stamp_9_desh"));
        this.withExistingParent("stamp_desh_50", mcLoc("item/generated")).texture("layer0", modLoc("item/stamp_50_desh"));
        this.basicItem(NtmItems.STAMP_DESH_PLATE.get());
        this.basicItem(NtmItems.STAMP_DESH_WIRE.get());
        this.basicItem(NtmItems.STAMP_DESH_CIRCUIT.get());

        this.basicItem(NtmItems.FLUID_ICON.get());
        this.layeredItem(NtmItems.FLUID_IDENTIFIER_MULTI.get(), "fluid_identifier_multi", "fluid_identifier_overlay");

        /*
         * Runde 104: die vier Bandstaebe. Sie tragen das Bild des Bandes, das sie legen --
         * das Original hat fuer sie ueberhaupt kein eigenes Bild, und so sieht man wenigstens
         * auf den ersten Blick, welcher Stab welches Band legt.
         */
        /* Drei Gegenstaende standen ohne Modell da -- im Spiel waeren sie der schwarz-violette
         * Wuerfel gewesen. Ihre Bilder tragen im Original dieselben Namen. */
        this.basicItem(NtmItems.BLUEPRINTS.get());
        this.basicItem(NtmItems.NO9.get());

        /* Schutzkleidung, Gasmasken und Filter. */
        this.basicItem(NtmItems.HAZMAT_CLOTH.get());
        this.basicItem(NtmItems.HAZMAT_CLOTH_RED.get());
        this.basicItem(NtmItems.HAZMAT_CLOTH_GREY.get());
        this.basicItem(NtmItems.RAG.get());
        this.basicItem(NtmItems.RAG_DAMP.get());
        this.basicItem(NtmItems.RAG_PISS.get());
        this.basicItem(NtmItems.HAZMAT_HELMET.get());
        this.basicItem(NtmItems.HAZMAT_PLATE.get());
        this.basicItem(NtmItems.HAZMAT_LEGS.get());
        this.basicItem(NtmItems.HAZMAT_BOOTS.get());
        this.basicItem(NtmItems.HAZMAT_HELMET_RED.get());
        this.basicItem(NtmItems.HAZMAT_PLATE_RED.get());
        this.basicItem(NtmItems.HAZMAT_LEGS_RED.get());
        this.basicItem(NtmItems.HAZMAT_BOOTS_RED.get());
        this.basicItem(NtmItems.HAZMAT_HELMET_GREY.get());
        this.basicItem(NtmItems.HAZMAT_PLATE_GREY.get());
        this.basicItem(NtmItems.HAZMAT_LEGS_GREY.get());
        this.basicItem(NtmItems.HAZMAT_BOOTS_GREY.get());
        this.basicItem(NtmItems.LIQUIDATOR_HELMET.get());
        this.basicItem(NtmItems.LIQUIDATOR_PLATE.get());
        this.basicItem(NtmItems.LIQUIDATOR_LEGS.get());
        this.basicItem(NtmItems.LIQUIDATOR_BOOTS.get());
        this.basicItem(NtmItems.HAZMAT_PAA_HELMET.get());
        this.basicItem(NtmItems.HAZMAT_PAA_PLATE.get());
        this.basicItem(NtmItems.HAZMAT_PAA_LEGS.get());
        this.basicItem(NtmItems.HAZMAT_PAA_BOOTS.get());
        this.basicItem(NtmItems.RPA_HELMET.get());
        this.basicItem(NtmItems.RPA_PLATE.get());
        this.basicItem(NtmItems.RPA_LEGS.get());
        this.basicItem(NtmItems.RPA_BOOTS.get());
        /* Die NCR-Ruestung hat im Original keine eigenen Gegenstandsbilder und nimmt die der
         * Remnant -- vier Modelle, die auf fremde Texturen zeigen. */
        this.withExistingParent("ncrpa_helmet", mcLoc("item/generated")).texture("layer0", modLoc("item/rpa_helmet"));
        this.withExistingParent("ncrpa_plate", mcLoc("item/generated")).texture("layer0", modLoc("item/rpa_plate"));
        this.withExistingParent("ncrpa_legs", mcLoc("item/generated")).texture("layer0", modLoc("item/rpa_legs"));
        this.withExistingParent("ncrpa_boots", mcLoc("item/generated")).texture("layer0", modLoc("item/rpa_boots"));
        this.basicItem(NtmItems.HEV_HELMET.get());
        this.basicItem(NtmItems.HEV_PLATE.get());
        this.basicItem(NtmItems.HEV_LEGS.get());
        this.basicItem(NtmItems.HEV_BOOTS.get());
        this.basicItem(NtmItems.GAS_MASK.get());
        this.basicItem(NtmItems.GAS_MASK_M65.get());
        this.basicItem(NtmItems.GAS_MASK_MONO.get());
        this.basicItem(NtmItems.GAS_MASK_OLDE.get());
        this.basicItem(NtmItems.GOGGLES.get());
        this.basicItem(NtmItems.ASHGLASSES.get());
        this.basicItem(NtmItems.STEEL_HELMET.get());
        this.basicItem(NtmItems.STEEL_PLATE.get());
        this.basicItem(NtmItems.STEEL_LEGS.get());
        this.basicItem(NtmItems.STEEL_BOOTS.get());
        this.basicItem(NtmItems.TITANIUM_HELMET.get());
        this.basicItem(NtmItems.TITANIUM_PLATE.get());
        this.basicItem(NtmItems.TITANIUM_LEGS.get());
        this.basicItem(NtmItems.TITANIUM_BOOTS.get());
        this.basicItem(NtmItems.ALLOY_HELMET.get());
        this.basicItem(NtmItems.ALLOY_PLATE.get());
        this.basicItem(NtmItems.ALLOY_LEGS.get());
        this.basicItem(NtmItems.ALLOY_BOOTS.get());
        this.basicItem(NtmItems.COBALT_HELMET.get());
        this.basicItem(NtmItems.COBALT_PLATE.get());
        this.basicItem(NtmItems.COBALT_LEGS.get());
        this.basicItem(NtmItems.COBALT_BOOTS.get());
        this.basicItem(NtmItems.STARMETAL_HELMET.get());
        this.basicItem(NtmItems.STARMETAL_PLATE.get());
        this.basicItem(NtmItems.STARMETAL_LEGS.get());
        this.basicItem(NtmItems.STARMETAL_BOOTS.get());
        this.basicItem(NtmItems.SECURITY_HELMET.get());
        this.basicItem(NtmItems.SECURITY_PLATE.get());
        this.basicItem(NtmItems.SECURITY_LEGS.get());
        this.basicItem(NtmItems.SECURITY_BOOTS.get());
        this.basicItem(NtmItems.DNT_HELMET.get());
        this.basicItem(NtmItems.DNT_PLATE.get());
        this.basicItem(NtmItems.DNT_LEGS.get());
        this.basicItem(NtmItems.DNT_BOOTS.get());
        this.basicItem(NtmItems.ROBES_HELMET.get());
        this.basicItem(NtmItems.ROBES_PLATE.get());
        this.basicItem(NtmItems.ROBES_LEGS.get());
        this.basicItem(NtmItems.ROBES_BOOTS.get());
        this.basicItem(NtmItems.ZIRCONIUM_LEGS.get());
        this.basicItem(NtmItems.ASBESTOS_HELMET.get());
        this.basicItem(NtmItems.ASBESTOS_PLATE.get());
        this.basicItem(NtmItems.ASBESTOS_LEGS.get());
        this.basicItem(NtmItems.ASBESTOS_BOOTS.get());
        this.basicItem(NtmItems.CMB_HELMET.get());
        this.basicItem(NtmItems.CMB_PLATE.get());
        this.basicItem(NtmItems.CMB_LEGS.get());
        this.basicItem(NtmItems.CMB_BOOTS.get());
        this.basicItem(NtmItems.SCHRABIDIUM_HELMET.get());
        this.basicItem(NtmItems.SCHRABIDIUM_PLATE.get());
        this.basicItem(NtmItems.SCHRABIDIUM_LEGS.get());
        this.basicItem(NtmItems.SCHRABIDIUM_BOOTS.get());
        this.basicItem(NtmItems.PAA_PLATE.get());
        this.basicItem(NtmItems.PAA_LEGS.get());
        this.basicItem(NtmItems.PAA_BOOTS.get());
        /* Die T-51 teilt sich das allgemeine Ruestungsbild -- im Original heisst es
         * "armor" und steht bei allen Panzerruestungen im setTextureName. */
        this.basicCustomLayerItem(NtmItems.T51_HELMET.get(), "armor");
        this.basicCustomLayerItem(NtmItems.T51_PLATE.get(), "armor");
        this.basicCustomLayerItem(NtmItems.T51_LEGS.get(), "armor");
        this.basicCustomLayerItem(NtmItems.T51_BOOTS.get(), "armor");

        /* Der Euphemium-Satz hat vier eigene Bilder, anders als die Panzerruestungen. */
        this.basicItem(NtmItems.EUPHEMIUM_HELMET.get());
        this.basicItem(NtmItems.EUPHEMIUM_PLATE.get());
        this.basicItem(NtmItems.EUPHEMIUM_LEGS.get());
        this.basicItem(NtmItems.EUPHEMIUM_BOOTS.get());
        this.basicItem(NtmItems.WATCH.get());

        /* Anders als die T-51 haben beide AJR-Garnituren je vier eigene Bilder. */
        this.basicItem(NtmItems.AJR_HELMET.get());
        this.basicItem(NtmItems.AJR_PLATE.get());
        this.basicItem(NtmItems.AJR_LEGS.get());
        this.basicItem(NtmItems.AJR_BOOTS.get());
        this.basicItem(NtmItems.AJRO_HELMET.get());
        this.basicItem(NtmItems.AJRO_PLATE.get());
        this.basicItem(NtmItems.AJRO_LEGS.get());
        this.basicItem(NtmItems.AJRO_BOOTS.get());

        this.basicItem(NtmItems.TAURUN_HELMET.get());
        this.basicItem(NtmItems.TAURUN_PLATE.get());
        this.basicItem(NtmItems.TAURUN_LEGS.get());
        this.basicItem(NtmItems.TAURUN_BOOTS.get());

        this.basicItem(NtmItems.BISMUTH_HELMET.get());
        this.basicItem(NtmItems.BISMUTH_PLATE.get());
        this.basicItem(NtmItems.BISMUTH_LEGS.get());
        this.basicItem(NtmItems.BISMUTH_BOOTS.get());
        this.basicItem(NtmItems.LASER_CRYSTAL_BISMUTH.get());

        this.basicItem(NtmItems.FAU_HELMET.get());
        this.basicItem(NtmItems.FAU_PLATE.get());
        this.basicItem(NtmItems.FAU_LEGS.get());
        this.basicItem(NtmItems.FAU_BOOTS.get());

        this.basicItem(NtmItems.TRENCHMASTER_HELMET.get());
        this.basicItem(NtmItems.TRENCHMASTER_PLATE.get());
        this.basicItem(NtmItems.TRENCHMASTER_LEGS.get());
        this.basicItem(NtmItems.TRENCHMASTER_BOOTS.get());

        this.basicItem(NtmItems.ENVSUIT_HELMET.get());
        this.basicItem(NtmItems.ENVSUIT_PLATE.get());
        this.basicItem(NtmItems.ENVSUIT_LEGS.get());
        this.basicItem(NtmItems.ENVSUIT_BOOTS.get());

        this.basicItem(NtmItems.BJ_HELMET.get());
        this.basicItem(NtmItems.BJ_PLATE.get());
        this.basicItem(NtmItems.BJ_PLATE_JETPACK.get());
        this.basicItem(NtmItems.BJ_LEGS.get());
        this.basicItem(NtmItems.BJ_BOOTS.get());

        this.basicItem(NtmItems.DNS_HELMET.get());
        this.basicItem(NtmItems.DNS_PLATE.get());
        this.basicItem(NtmItems.DNS_LEGS.get());
        this.basicItem(NtmItems.DNS_BOOTS.get());
        this.basicItem(NtmItems.COIN_WORM.get());
        this.basicItem(NtmItems.ASBESTOS_CLOTH.get());
        this.basicItem(NtmItems.MASK_RAG.get());
        this.basicItem(NtmItems.MASK_PISS.get());
        this.basicItem(NtmItems.GAS_MASK_FILTER.get());
        this.basicItem(NtmItems.GAS_MASK_FILTER_MONO.get());
        this.basicItem(NtmItems.GAS_MASK_FILTER_COMBO.get());
        this.basicItem(NtmItems.GAS_MASK_FILTER_RAG.get());
        this.basicItem(NtmItems.GAS_MASK_FILTER_PISS.get());

        /* Ruestungsmodule: Auskleidungen und Einlagen. */
        this.basicItem(NtmItems.CLADDING_PAINT.get());
        this.basicItem(NtmItems.CLADDING_RUBBER.get());
        this.basicItem(NtmItems.CLADDING_LEAD.get());
        this.basicItem(NtmItems.CLADDING_DESH.get());
        this.basicItem(NtmItems.CLADDING_GHIORSIUM.get());
        this.basicItem(NtmItems.CLADDING_IRON.get());
        this.basicItem(NtmItems.CLADDING_OBSIDIAN.get());
        this.basicItem(NtmItems.INSERT_KEVLAR.get());
        this.basicItem(NtmItems.INSERT_SAPI.get());
        this.basicItem(NtmItems.INSERT_ESAPI.get());
        this.basicItem(NtmItems.INSERT_XSAPI.get());
        this.basicItem(NtmItems.INSERT_STEEL.get());
        this.basicItem(NtmItems.INSERT_DU.get());
        this.basicItem(NtmItems.INSERT_POLONIUM.get());
        this.basicItem(NtmItems.INSERT_GHIORSIUM.get());
        this.basicItem(NtmItems.INSERT_ERA.get());
        this.basicItem(NtmItems.INSERT_YHARONITE.get());
        this.basicItem(NtmItems.INSERT_DOXIUM.get());
        this.basicItem(NtmItems.CATALYTIC_CONVERTER.get());
        this.basicItem(NtmItems.SOLID_FUEL_BF.get());
        this.basicItem(NtmItems.BIOMASS_COMPRESSED.get());
        this.basicItem(NtmItems.BIO_WAFER.get());
        this.basicItem(NtmItems.PLAN_C.get());

        this.withExistingParent("conveyor_wand", mcLoc("item/generated")).texture("layer0", modLoc("block/conveyor"));
        this.withExistingParent("conveyor_wand_express", mcLoc("item/generated")).texture("layer0", modLoc("block/conveyor_express"));
        this.withExistingParent("conveyor_wand_double", mcLoc("item/generated")).texture("layer0", modLoc("block/conveyor_double"));
        this.withExistingParent("conveyor_wand_triple", mcLoc("item/generated")).texture("layer0", modLoc("block/conveyor_triple"));

        this.handheldItem(NtmItems.SCREWDRIVER.get());
        this.handheldItem(NtmItems.SCREWDRIVER_DESH.get());
        this.handheldItem(NtmItems.HAND_DRILL.get());
        this.handheldItem(NtmItems.HAND_DRILL_DESH.get());
        this.entityItem(NtmItems.BOLTGUN.get(), true);
        this.handheldItem(NtmItems.BLOWTORCH.get());
        this.handheldItem(NtmItems.ACETYLENE_TORCH.get());
        this.handheldItem(NtmItems.BLADES_STEEL.get());
        this.handheldItem(NtmItems.BLADES_TITANIUM.get());
        this.handheldItem(NtmItems.BLADES_DESH.get());

        this.basicItem(NtmItems.ROD_EMPTY.get());
        this.basicItem(NtmItems.ROD_DUAL_EMPTY.get());
        this.basicItem(NtmItems.ROD_QUAD_EMPTY.get());

        this.basicItem(NtmItems.ROD_ZIRNOX_EMPTY.get());
        this.basicItem(NtmItems.ROD_ZIRNOX_TRITIUM.get());
        // ROD_ZIRNOX uses ICustomItemModelRegister

        this.basicItem(NtmItems.ROD_ZIRNOX_NATURAL_URANIUM_FUEL_DEPLETED.get());
        this.basicItem(NtmItems.ROD_ZIRNOX_URANIUM_FUEL_DEPLETED.get());
        this.basicItem(NtmItems.ROD_ZIRNOX_THORIUM_FUEL_DEPLETED.get());
        this.basicItem(NtmItems.ROD_ZIRNOX_MOX_FUEL_DEPLETED.get());
        this.basicItem(NtmItems.ROD_ZIRNOX_PLUTONIUM_FUEL_DEPLETED.get());
        this.basicItem(NtmItems.ROD_ZIRNOX_U233_FUEL_DEPLETED.get());
        this.basicItem(NtmItems.ROD_ZIRNOX_U235_FUEL_DEPLETED.get());
        this.basicItem(NtmItems.ROD_ZIRNOX_LES_FUEL_DEPLETED.get());
        this.basicItem(NtmItems.ROD_ZIRNOX_ZFB_MOX_DEPLETED.get());

        this.basicItem(NtmItems.SPAWN_DUCK.get());

        this.basicItem(NtmItems.DESIGNATOR.get());
        this.handheldItem(NtmItems.DESIGNATOR_RANGE.get());
        this.basicItem(NtmItems.DOSIMETER.get());
        this.basicItem(NtmItems.GEIGER_COUNTER.get());
        this.basicCustomLayerItem(NtmItems.OIL_DETECTOR.get(), "oil_detector");
        this.basicItem(NtmItems.DIGAMMA_DIAGNOSTIC.get());
        this.handheldItem(NtmItems.METEOR_REMOTE.get());
        this.basicItem(NtmItems.METEOR_CHARM.get());
        this.basicItem(NtmItems.PROTECTION_CHARM.get());

        this.basicItem(NtmItems.PIN.get());
        this.basicItem(NtmItems.KEY.get());
        this.basicItem(NtmItems.KEY_RED.get());
        this.basicItem(NtmItems.KEY_RED_CRACKED.get());
        this.basicItem(NtmItems.KEY_KIT.get());
        this.basicItem(NtmItems.KEY_FAKE.get());
        this.basicItem(NtmItems.LAUNCH_CODE_PIECE.get());
        this.basicItem(NtmItems.LAUNCH_CODE.get());
        this.basicItem(NtmItems.LAUNCH_KEY.get());

        this.entityItem(NtmItems.MISSILE_TAINT.get(), true);
        this.entityItem(NtmItems.MISSILE_MICRO.get(), true);
        this.entityItem(NtmItems.MISSILE_BHOLE.get(), true);
        this.entityItem(NtmItems.MISSILE_SCHRABIDIUM.get(), true);
        this.entityItem(NtmItems.MISSILE_EMP.get(), true);
        this.entityItem(NtmItems.MISSILE_GENERIC.get(), true);
        this.entityItem(NtmItems.MISSILE_INCENDIARY.get(), true);
        this.entityItem(NtmItems.MISSILE_CLUSTER.get(), true);
        this.entityItem(NtmItems.MISSILE_BUSTER.get(), true);
        this.entityItem(NtmItems.MISSILE_DECOY.get(), true);
        this.entityItem(NtmItems.MISSILE_STRONG.get(), true);
        this.entityItem(NtmItems.MISSILE_INCENDIARY_STRONG.get(), true);
        this.entityItem(NtmItems.MISSILE_CLUSTER_STRONG.get(), true);
        this.entityItem(NtmItems.MISSILE_BUSTER_STRONG.get(), true);
        this.entityItem(NtmItems.MISSILE_EMP_STRONG.get(), true);
        this.entityItem(NtmItems.MISSILE_BURST.get(), true);
        this.entityItem(NtmItems.MISSILE_INFERNO.get(), true);
        this.entityItem(NtmItems.MISSILE_RAIN.get(), true);
        this.entityItem(NtmItems.MISSILE_DRILL.get(), true);
        this.entityItem(NtmItems.MISSILE_NUCLEAR.get(), true);
        this.entityItem(NtmItems.MISSILE_NUCLEAR_CLUSTER.get(), true);
        this.entityItem(NtmItems.MISSILE_VOLCANO.get(), true);
        this.entityItem(NtmItems.MISSILE_DOOMSDAY.get(), true);
        this.entityItem(NtmItems.MISSILE_DOOMSDAY_RUSTED.get(), true);
        this.entityItem(NtmItems.MISSILE_SHUTTLE.get(), true);
        this.entityItem(NtmItems.MISSILE_STEALTH.get(), true);
        this.entityItem(NtmItems.MISSILE_ANTI_BALLISTIC.get(), true);

        // MISSILE_SOYUZ uses ICustomItemModelRegister

        // SATELLITE uses ICustomItemModelRegister
        this.basicItem(NtmItems.SAT_GERALD.get());
        this.basicItem(NtmItems.SAT_CHIP.get());
        this.basicItem(NtmItems.SAT_COORD.get());
        this.handheldItem(NtmItems.SAT_DESIGNATOR.get());

        this.basicItem(NtmItems.BALEFIRE_AND_STEEL.get());

        // DRINK uses ICustomItemModelRegister
        this.handheldItem(NtmItems.BOTTLE_OPENER.get());
        this.handheldItem(NtmItems.CROWBAR.get());

        // Die vier Stecker des Zyklotrons (Runde 163); der Hammer haelt sich wie ein Werkzeug.
        this.basicItem(NtmItems.BOOK_OF_.get());
        this.handheldItem(NtmItems.DIAMOND_GAVEL.get());
        this.handheldItem(NtmItems.WOOD_GAVEL.get());
        this.handheldItem(NtmItems.LEAD_GAVEL.get());
        /* Das Original nimmt fuer die Schrotkugeln die Textur der Bleikugeln. */
        this.basicCustomLayerItem(NtmItems.PELLET_BUCKSHOT.get(), "pellets_lead");
        this.basicItem(NtmItems.COIN_MASKMAN.get());
        this.handheldItem(NtmItems.STEEL_PICKAXE.get());
        this.handheldItem(NtmItems.STEEL_AXE.get());
        this.handheldItem(NtmItems.STEEL_SHOVEL.get());
        this.handheldItem(NtmItems.STEEL_HOE.get());
        this.handheldItem(NtmItems.TITANIUM_PICKAXE.get());
        this.handheldItem(NtmItems.TITANIUM_AXE.get());
        this.handheldItem(NtmItems.TITANIUM_SHOVEL.get());
        this.handheldItem(NtmItems.TITANIUM_HOE.get());
        this.handheldItem(NtmItems.DESH_PICKAXE.get());
        this.handheldItem(NtmItems.DESH_AXE.get());
        this.handheldItem(NtmItems.DESH_SHOVEL.get());
        this.handheldItem(NtmItems.DESH_HOE.get());
        this.handheldItem(NtmItems.COBALT_PICKAXE.get());
        this.handheldItem(NtmItems.COBALT_AXE.get());
        this.handheldItem(NtmItems.COBALT_SHOVEL.get());
        this.handheldItem(NtmItems.COBALT_HOE.get());
        this.handheldItem(NtmItems.COBALT_DECORATED_PICKAXE.get());
        this.handheldItem(NtmItems.COBALT_DECORATED_AXE.get());
        this.handheldItem(NtmItems.COBALT_DECORATED_SHOVEL.get());
        this.handheldItem(NtmItems.COBALT_DECORATED_HOE.get());
        this.handheldItem(NtmItems.CMB_PICKAXE.get());
        this.handheldItem(NtmItems.CMB_AXE.get());
        this.handheldItem(NtmItems.CMB_SHOVEL.get());
        this.handheldItem(NtmItems.CMB_HOE.get());
        this.handheldItem(NtmItems.BISMUTH_PICKAXE.get());
        this.handheldItem(NtmItems.BISMUTH_AXE.get());
        this.handheldItem(NtmItems.STARMETAL_PICKAXE.get());
        this.handheldItem(NtmItems.STARMETAL_AXE.get());
        this.handheldItem(NtmItems.STARMETAL_SHOVEL.get());
        this.handheldItem(NtmItems.STARMETAL_HOE.get());
        this.handheldItem(NtmItems.VOLCANIC_PICKAXE.get());
        this.handheldItem(NtmItems.VOLCANIC_AXE.get());
        this.handheldItem(NtmItems.CHLOROPHYTE_PICKAXE.get());
        this.handheldItem(NtmItems.CHLOROPHYTE_AXE.get());
        this.handheldItem(NtmItems.MESE_PICKAXE.get());
        this.handheldItem(NtmItems.MESE_AXE.get());
        this.handheldItem(NtmItems.SCHRABIDIUM_PICKAXE.get());
        this.handheldItem(NtmItems.SCHRABIDIUM_AXE.get());
        this.handheldItem(NtmItems.SCHRABIDIUM_SHOVEL.get());
        this.handheldItem(NtmItems.SCHRABIDIUM_HOE.get());

        // CANNED_CONSERVE uses ICustomItemModelRegister

        // CAP uses ICustomItemModelRegister
        this.basicItem(NtmItems.RING_PULL.get());
        this.basicItem(NtmItems.CAN_KEY.get());

        this.basicItem(NtmItems.PUDDING.get());
        this.basicItem(NtmItems.GRENADE_UNIVERSAL.get());
        this.basicItem(NtmItems.SYRINGE_ANTIDOTE.get());
        this.basicItem(NtmItems.IV_EMPTY.get());
        this.basicItem(NtmItems.IV_BLOOD.get());
        this.basicItem(NtmItems.IV_XP_EMPTY.get());
        this.basicItem(NtmItems.IV_XP.get());
        this.basicItem(NtmItems.RADAWAY.get());
        this.basicItem(NtmItems.RADAWAY_STRONG.get());
        this.basicItem(NtmItems.RADAWAY_FLUSH.get());
        this.basicItem(NtmItems.MED_BAG.get());
        this.basicItem(NtmItems.PILL_IODINE.get());
        this.basicItem(NtmItems.DEFINITELYFOOD.get());
        this.basicItem(NtmItems.MYSTERYSHOVEL.get());
        this.basicItem(NtmItems.FLAME_PONY.get());
        this.basicItem(NtmItems.CHOCOLATE_MILK.get());
        this.handheldItem(NtmItems.CIGARETTE.get());
        this.handheldItem(NtmItems.CRACKPIPE.get());

        this.basicItem(NtmItems.EARLY_EXPLOSIVE_LENSES.get());
        this.basicItem(NtmItems.EXPLOSIVE_LENSES.get());

        this.basicItem(NtmItems.GADGET_WIREING.get());
        this.basicItem(NtmItems.GADGET_CORE.get());

        this.basicItem(NtmItems.LITTLE_BOY_SHIELDING.get());
        this.basicItem(NtmItems.LITTLE_BOY_TARGET.get());
        this.basicItem(NtmItems.LITTLE_BOY_BULLET.get());
        this.basicItem(NtmItems.LITTLE_BOY_PROPELLANT.get());
        this.basicItem(NtmItems.LITTLE_BOY_IGNITER.get());

        this.basicItem(NtmItems.FAT_MAN_IGNITER.get());
        this.basicItem(NtmItems.FAT_MAN_CORE.get());
        this.basicItem(NtmItems.DEMON_CORE_OPEN.get());
        this.basicItem(NtmItems.DEMON_CORE_CLOSED.get());

        this.basicItem(NtmItems.IVY_MIKE_CORE.get());
        this.basicItem(NtmItems.IVY_MIKE_DEUT.get());
        this.basicItem(NtmItems.IVY_MIKE_COOLING_UNIT.get());

        this.basicItem(NtmItems.TSAR_BOMBA_CORE.get());

        this.basicItem(NtmItems.FLEIJA_IGNITER.get());
        this.basicItem(NtmItems.FLEIJA_PROPELLANT.get());
        this.basicItem(NtmItems.FLEIJA_CORE.get());

        this.basicItem(NtmItems.SOLINIUM_IGNITER.get());
        this.basicItem(NtmItems.SOLINIUM_PROPELLANT.get());
        this.basicItem(NtmItems.SOLINIUM_CORE.get());

        this.basicItem(NtmItems.N2_CHARGE.get());

        this.basicItem(NtmItems.EGG_BALEFIRE_SHARD.get());
        this.basicItem(NtmItems.EGG_BALEFIRE.get());

        this.handheldItem(NtmItems.IGNITER.get());
        this.handheldItem(NtmItems.DETONATOR.get());
        this.handheldItem(NtmItems.DETONATOR_MULTI.get());
        this.entityItem(NtmItems.DETONATOR_LASER.get(), true);
        this.handheldItem(NtmItems.DETONATOR_DEADMAN.get());
        this.handheldItem(NtmItems.DETONATOR_DE.get());
        this.handheldItem(NtmItems.BOMB_CALLER.get());
        this.handheldItem(NtmItems.DEFUSER.get());
        this.handheldItem(NtmItems.REACHER.get());
        this.basicItem(NtmItems.MELTDOWN_TOOL.get());
        this.basicItem(NtmItems.MIRROR_TOOL.get());
        this.basicItem(NtmItems.WIRING_TOOL.get());
        // PISTON_SET nutzt ICustomItemModelRegister (EnumMultiItem), kein Eintrag noetig
        this.handheldItem(NtmItems.DRILL_TITANIUM.get());

        this.handheldItem(NtmItems.UPGRADE_TEMPLATE.get());
        this.handheldItem(NtmItems.UPGRADE_SPEED_1.get());
        this.handheldItem(NtmItems.UPGRADE_SPEED_2.get());
        this.handheldItem(NtmItems.UPGRADE_SPEED_3.get());
        this.handheldItem(NtmItems.UPGRADE_EFFECT_1.get());
        this.handheldItem(NtmItems.UPGRADE_EFFECT_2.get());
        this.handheldItem(NtmItems.UPGRADE_EFFECT_3.get());
        this.handheldItem(NtmItems.UPGRADE_POWER_1.get());
        this.handheldItem(NtmItems.UPGRADE_POWER_2.get());
        this.handheldItem(NtmItems.UPGRADE_POWER_3.get());
        this.handheldItem(NtmItems.UPGRADE_FORTUNE_1.get());
        this.handheldItem(NtmItems.UPGRADE_FORTUNE_2.get());
        this.handheldItem(NtmItems.UPGRADE_FORTUNE_3.get());
        this.handheldItem(NtmItems.UPGRADE_AFTERBURN_1.get());
        this.handheldItem(NtmItems.UPGRADE_AFTERBURN_2.get());
        this.handheldItem(NtmItems.UPGRADE_AFTERBURN_3.get());
        this.handheldItem(NtmItems.UPGRADE_RADIUS.get());
        this.handheldItem(NtmItems.UPGRADE_HEALTH.get());
        this.handheldItem(NtmItems.UPGRADE_OVERDRIVE_1.get());
        this.handheldItem(NtmItems.UPGRADE_OVERDRIVE_2.get());
        this.handheldItem(NtmItems.UPGRADE_OVERDRIVE_3.get());
        this.handheldItem(NtmItems.UPGRADE_GC_SPEED.get());
        this.handheldItem(NtmItems.UPGRADE_SMELTER.get());
        this.handheldItem(NtmItems.UPGRADE_SHREDDER.get());
        this.handheldItem(NtmItems.UPGRADE_CENTRIFUGE.get());
        this.handheldItem(NtmItems.UPGRADE_CRYSTALLIZER.get());
        this.handheldItem(NtmItems.UPGRADE_NULLIFIER.get());
        this.handheldItem(NtmItems.UPGRADE_SCREM.get());

        this.basicItem(NtmItems.PART_LITHIUM.get());
        this.basicItem(NtmItems.PART_BERYLLIUM.get());
        this.basicItem(NtmItems.PART_CARBON.get());
        this.basicItem(NtmItems.PART_COPPER.get());
        this.basicItem(NtmItems.PART_PLUTONIUM.get());

        this.registerPolaroid();
        this.basicItem(NtmItems.BURNT_BARK.get());

        // STARTER_KIT uses ICustomItemModelRegister

        this.basicItem(NtmItems.TEMPLATE_FOLDER.get());
        this.basicItem(NtmItems.BROKEN_ITEM.get());
        // BEDROCK_ORE nutzt ICustomItemModelRegister -- sechsundzwanzig Modelle, eines je Stufe
        this.basicItem(NtmItems.BEDROCK_ORE_BASE.get());
        this.basicItem(NtmItems.BEDROCK_ORE_FRAGMENT.get());
        this.basicItem(NtmItems.PART_BARREL_LIGHT.get());
        this.basicItem(NtmItems.PART_BARREL_HEAVY.get());
        this.basicItem(NtmItems.PART_RECEIVER_LIGHT.get());
        this.basicItem(NtmItems.PART_RECEIVER_HEAVY.get());
        this.basicItem(NtmItems.PART_MECHANISM.get());
        this.basicItem(NtmItems.PART_STOCK.get());
        this.basicItem(NtmItems.PART_GRIP.get());
        this.basicItem(NtmItems.MISSILE_CUSTOM.get());
        this.basicItem(NtmItems.PARTICLE_HYDROGEN.get());
        this.basicItem(NtmItems.PARTICLE_COPPER.get());
        this.basicItem(NtmItems.PARTICLE_LEAD.get());
        this.basicItem(NtmItems.PARTICLE_AMAT.get());
        this.basicItem(NtmItems.PARTICLE_ASCHRAB.get());
        this.basicItem(NtmItems.PARTICLE_HIGGS.get());
        this.basicItem(NtmItems.PARTICLE_TACHYON.get());
        this.basicItem(NtmItems.PARTICLE_STRANGE.get());
        this.basicItem(NtmItems.PARTICLE_DARK.get());
        this.basicItem(NtmItems.PARTICLE_SPARKTICLE.get());

        /* Die hundertzweiundzwanzig Raketenbauteile teilen sich neun Bilder: eines je Art und
         * fuenf fuer die Chips. Das ist im Original genauso. */
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_10_KEROSENE.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_10_SOLID.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_10_XENON.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_15_KEROSENE.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_15_KEROSENE_DUAL.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_15_KEROSENE_TRIPLE.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_15_SOLID.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_15_SOLID_HEXDECUPLE.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_15_HYDROGEN.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_15_HYDROGEN_DUAL.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_15_BALEFIRE_SHORT.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_15_BALEFIRE.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_15_BALEFIRE_LARGE.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_15_BALEFIRE_LARGE_RAD.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_20_KEROSENE.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_20_KEROSENE_DUAL.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_20_KEROSENE_TRIPLE.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_20_SOLID.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_20_SOLID_MULTI.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_THRUSTER_20_SOLID_MULTIER.get(), "mp_thruster");
        this.basicCustomLayerItem(NtmItems.MP_STABILITY_10_FLAT.get(), "mp_stability");
        this.basicCustomLayerItem(NtmItems.MP_STABILITY_10_CRUISE.get(), "mp_stability");
        this.basicCustomLayerItem(NtmItems.MP_STABILITY_10_SPACE.get(), "mp_stability");
        this.basicCustomLayerItem(NtmItems.MP_STABILITY_15_FLAT.get(), "mp_stability");
        this.basicCustomLayerItem(NtmItems.MP_STABILITY_15_THIN.get(), "mp_stability");
        this.basicCustomLayerItem(NtmItems.MP_STABILITY_15_SOYUZ.get(), "mp_stability");
        this.basicCustomLayerItem(NtmItems.MP_STABILITY_20_FLAT.get(), "mp_stability");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_KEROSENE.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_KEROSENE_CAMO.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_KEROSENE_DESERT.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_KEROSENE_SKY.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_KEROSENE_FLAMES.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_KEROSENE_INSULATION.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_KEROSENE_SLEEK.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_KEROSENE_METAL.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_KEROSENE_TAINT.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_SOLID.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_SOLID_FLAMES.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_SOLID_INSULATION.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_SOLID_SLEEK.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_SOLID_SOVIET_GLORY.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_SOLID_CATHEDRAL.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_SOLID_MOONLIT.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_SOLID_BATTERY.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_SOLID_DURACELL.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_XENON.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_XENON_BHOLE.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_KEROSENE.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_KEROSENE_CAMO.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_KEROSENE_DESERT.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_KEROSENE_SKY.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_KEROSENE_FLAMES.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_KEROSENE_INSULATION.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_KEROSENE_SLEEK.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_KEROSENE_METAL.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_KEROSENE_DASH.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_KEROSENE_TAINT.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_KEROSENE_VAP.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_SOLID.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_SOLID_FLAMES.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_SOLID_INSULATION.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_SOLID_SLEEK.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_SOLID_SOVIET_GLORY.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_SOLID_BULLET.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_LONG_SOLID_SILVERMOONLIGHT.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_15_KEROSENE.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_15_SOLID.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_15_HYDROGEN.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_10_15_BALEFIRE.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_CAMO.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_DESERT.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_SKY.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_INSULATION.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_METAL.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_DECORATED.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_STEAMPUNK.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_POLITE.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_BLACKJACK.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_LAMBDA.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_MINUTEMAN.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_PIP.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_TAINT.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_KEROSENE_YUCK.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_SOLID.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_SOLID_INSULATION.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_SOLID_DESH.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_SOLID_SOVIET_GLORY.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_SOLID_SOVIET_STANK.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_SOLID_FAUST.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_SOLID_SILVERMOONLIGHT.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_SOLID_SNOWY.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_SOLID_PANORAMA.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_SOLID_ROSES.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_SOLID_MIMI.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_HYDROGEN.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_HYDROGEN_CATHEDRAL.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_BALEFIRE.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_20_KEROSENE.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_20_KEROSENE_MAGNUSSON.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_FUSELAGE_15_20_SOLID.get(), "mp_fuselage");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_10_HE.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_10_INCENDIARY.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_10_BUSTER.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_10_NUCLEAR.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_10_NUCLEAR_LARGE.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_10_TAINT.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_10_CLOUD.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_15_HE.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_15_INCENDIARY.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_15_NUCLEAR.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_15_NUCLEAR_SHARK.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_15_NUCLEAR_MIMI.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_15_BOXCAR.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_15_N2.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_15_BALEFIRE.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_WARHEAD_15_TURBINE.get(), "mp_warhead");
        this.basicCustomLayerItem(NtmItems.MP_CHIP_1.get(), "mp_c_1");
        this.basicCustomLayerItem(NtmItems.MP_CHIP_2.get(), "mp_c_2");
        this.basicCustomLayerItem(NtmItems.MP_CHIP_3.get(), "mp_c_3");
        this.basicCustomLayerItem(NtmItems.MP_CHIP_4.get(), "mp_c_4");
        this.basicCustomLayerItem(NtmItems.MP_CHIP_5.get(), "mp_c_5");
        // DRILLBIT nutzt ICustomItemModelRegister (EnumMultiItem)
        this.basicItem(NtmItems.ORBITAL_ASSEMBLY.get());
        this.basicItem(NtmItems.NOTHING.get());

        this.entityItem(NtmItems.GUN_DEBUG.get(), true);
        this.entityItem(NtmItems.GUN_MARESLEG.get(), true);
        this.entityItem(NtmItems.GUN_SPAS12.get(), true);
        this.entityItem(NtmItems.GUN_AUTOSHOTGUN.get(), true);
        this.entityItem(NtmItems.GUN_AUTOSHOTGUN_SHREDDER.get(), true);
        this.entityItem(NtmItems.GUN_AUTOSHOTGUN_SEXY.get(), true);
        this.entityItem(NtmItems.GUN_AUTOSHOTGUN_HERETIC.get(), true);
        this.entityItem(NtmItems.GUN_HANGMAN.get(), true);
        this.entityItem(NtmItems.GUN_N_I_4_N_I.get(), true);
        this.entityItem(NtmItems.GUN_HENRY.get(), true);
        this.entityItem(NtmItems.GUN_DRILL.get(), true);
        this.entityItem(NtmItems.GUN_TESLA_CANNON.get(), true);
        this.entityItem(NtmItems.GUN_FOLLY.get(), true);
        this.entityItem(NtmItems.GUN_CHARGE_THROWER.get(), true);
        this.entityItem(NtmItems.GUN_FIREEXT.get(), true);
        this.entityItem(NtmItems.GUN_PA_MELEE.get(), true);
        /* Die Fernwaffe hat keinen eigenen Zeichner -- im Original ein schlichtes
         * Gegenstandsbild mit setFull3D. */
        this.basicItem(NtmItems.GUN_PA_RANGED.get());
        this.entityItem(NtmItems.GUN_LASER_PISTOL.get(), true);
        this.entityItem(NtmItems.GUN_LASER_PISTOL_PEW_PEW.get(), true);
        this.entityItem(NtmItems.GUN_LASER_PISTOL_MORNING_GLORY.get(), true);
        this.entityItem(NtmItems.GUN_LASRIFLE.get(), true);
        this.entityItem(NtmItems.GUN_CHEMTHROWER.get(), true);
        this.entityItem(NtmItems.GUN_FLAMER.get(), true);
        this.entityItem(NtmItems.GUN_FLAMER_TOPAZ.get(), true);
        this.entityItem(NtmItems.GUN_FLAMER_DAYBREAKER.get(), true);
        this.entityItem(NtmItems.GUN_HENRY_LINCOLN.get(), true);
        this.entityItem(NtmItems.GUN_HEAVY_REVOLVER.get(), true);
        this.entityItem(NtmItems.GUN_LIBERATOR.get(), true);
        this.entityItem(NtmItems.GUN_HEAVY_REVOLVER_LILMAC.get(), true);
        this.entityItem(NtmItems.GUN_HEAVY_REVOLVER_PROTEGE.get(), true);
        this.entityItem(NtmItems.GUN_GREASEGUN.get(), true);
        this.entityItem(NtmItems.GUN_PEPPERBOX.get(), true);
        this.entityItem(NtmItems.GUN_LIGHT_REVOLVER.get(), true);
        this.entityItem(NtmItems.GUN_LIGHT_REVOLVER_ATLAS.get(), true);
        this.entityItem(NtmItems.GUN_AM180.get(), true);
        this.entityItem(NtmItems.GUN_STAR_F.get(), true);
        this.entityItem(NtmItems.GUN_STAR_F_AKIMBO.get(), true);
        this.entityItem(NtmItems.GUN_MARESLEG_AKIMBO.get(), true);
        this.entityItem(NtmItems.GUN_MARESLEG_BROKEN.get(), true);
        this.entityItem(NtmItems.GUN_LIGHT_REVOLVER_DANI.get(), true);
        this.entityItem(NtmItems.GUN_UZI.get(), true);
        this.entityItem(NtmItems.GUN_UZI_AKIMBO.get(), true);
        this.entityItem(NtmItems.GUN_AMAT.get(), true);
        this.entityItem(NtmItems.GUN_AMAT_SUBTLETY.get(), true);
        this.entityItem(NtmItems.GUN_AMAT_PENANCE.get(), true);
        this.entityItem(NtmItems.GUN_M2.get(), true);
        this.entityItem(NtmItems.GUN_G3.get(), true);
        this.entityItem(NtmItems.GUN_G3_ZEBRA.get(), true);
        this.entityItem(NtmItems.GUN_STG77.get(), true);
        this.entityItem(NtmItems.GUN_CARBINE.get(), true);
        this.entityItem(NtmItems.GUN_MAS36.get(), true);
        this.entityItem(NtmItems.GUN_MINIGUN.get(), true);
        this.entityItem(NtmItems.GUN_MINIGUN_LACUNAE.get(), true);
        this.entityItem(NtmItems.GUN_MINIGUN_DUAL.get(), true);
        this.entityItem(NtmItems.GUN_DOUBLE_BARREL.get(), true);
        this.entityItem(NtmItems.GUN_DOUBLE_BARREL_SACRED_DRAGON.get(), true);
        this.entityItem(NtmItems.GUN_BOLTER.get(), true);
        this.entityItem(NtmItems.GUN_ABERRATOR.get(), true);
        this.entityItem(NtmItems.GUN_ABERRATOR_EOTT.get(), true);
        this.entityItem(NtmItems.GUN_FLAREGUN.get(), true);
        this.entityItem(NtmItems.GUN_CONGOLAKE.get(), true);
        this.entityItem(NtmItems.GUN_MK108.get(), true);
        this.entityItem(NtmItems.GUN_PANZERSCHRECK.get(), true);
        this.entityItem(NtmItems.GUN_QUADRO.get(), true);
        this.entityItem(NtmItems.GUN_MISSILE_LAUNCHER.get(), true);
        this.entityItem(NtmItems.GUN_STINGER.get(), true);
        this.entityItem(NtmItems.GUN_LAG.get(), true);
        this.entityItem(NtmItems.GUN_COILGUN.get(), true);
        this.entityItem(NtmItems.GUN_TAU.get(), true);
        this.entityItem(NtmItems.GUN_FATMAN.get(), true);

        // welp
        this.getBuilder(NtmBlocks.BASALT.getId().getPath()).parent(new ModelFile.UncheckedModelFile(modLoc("block/basalt")));
        this.getBuilder(NtmBlocks.LEAVES_LAYER.getId().getPath()).parent(new ModelFile.UncheckedModelFile(modLoc("block/leaves_layer_1")));
        this.getBuilder(NtmBlocks.FOAM_LAYER.getId().getPath()).parent(new ModelFile.UncheckedModelFile(modLoc("block/foam_layer_1")));
        this.getBuilder(NtmBlocks.SAND_BORON_LAYER.getId().getPath()).parent(new ModelFile.UncheckedModelFile(modLoc("block/sand_boron_layer_1")));
        this.getBuilder(NtmBlocks.OIL_SPILL.getId().getPath()).parent(new ModelFile.UncheckedModelFile(modLoc("block/oil_spill_1")));
        this.getBuilder(NtmBlocks.FALLOUT.getId().getPath()).parent(new ModelFile.UncheckedModelFile(modLoc("block/fallout")));
        this.getBuilder(NtmBlocks.WASTE_LOG.getId().getPath()).parent(new ModelFile.UncheckedModelFile(modLoc("block/waste_log")));
        this.getBuilder(NtmBlocks.FROZEN_LOG.getId().getPath()).parent(new ModelFile.UncheckedModelFile(modLoc("block/frozen_log")));
    }

    private void registerPolaroid() {
        ItemModelBuilder builder = getBuilder("polaroid");
        for (int i = 1; i <= 18; i++) {
            builder.override()
                    .predicate(NuclearTechMod.withDefaultNamespace("polaroid_id"), i)
                    .model(getBuilder("polaroid_" + i)
                            .parent(getExistingFile(mcLoc("item/generated")))
                            .texture("layer0", modLoc("item/polaroid_" + i)))
                    .end();
        }
    }

    private ItemModelBuilder layeredItem(Item item, String layer0, String layer1) {
        return this.getBuilder(BuiltInRegistries.ITEM.getKey(item).toString()).parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/" + layer0))
                .texture("layer1", modLoc("item/" + layer1));
    }

    private ItemModelBuilder basicCustomLayerItem(Item item, String layer0) {
        return this.getBuilder(BuiltInRegistries.ITEM.getKey(item).toString()).parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/" + layer0));
    }

    private void entityItem(Item item, boolean frontLight) {
        this.getBuilder(BuiltInRegistries.ITEM.getKey(item).toString()).parent(new ModelFile.UncheckedModelFile("builtin/entity")).guiLight(frontLight ? BlockModel.GuiLight.FRONT : BlockModel.GuiLight.SIDE);
    }

}
