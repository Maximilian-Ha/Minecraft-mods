package com.hbm.hazard;

import com.hbm.blocks.NtmBlocks;
import com.hbm.hazard.modifier.HazardModifierFuelRadiation;
import com.hbm.hazard.modifier.HazardModifierRBMKHot;
import com.hbm.hazard.modifier.HazardModifierRBMKRadiation;
import com.hbm.hazard.modifier.HazardModifierRTGRadiation;
import com.hbm.hazard.type.*;
import com.hbm.items.machine.WatzPelletItem.EnumWatzType;
import com.hbm.items.machine.PWRFuelItem.EnumPWRFuel;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.BreedingRodItem.BreedingRodType;
import com.hbm.items.machine.DepletedFuelItem;
import com.hbm.items.machine.RTGPelletDepletedItem.DepletedRTGMaterial;
import com.hbm.items.machine.RTGPelletItem.RTGPelletType;
import com.hbm.items.machine.ZirnoxRodItem.ZirnoxType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import static com.hbm.blocks.NtmBlocks.BLOCK_CORIUM;
import static com.hbm.blocks.NtmBlocks.ANCIENT_SCRAP;
import static com.hbm.blocks.NtmBlocks.BLOCK_CORIUM_COBBLE;
import static com.hbm.blocks.NtmBlocks.BLOCK_WASTE;
import static com.hbm.blocks.NtmBlocks.BLOCK_WASTE_PAINTED;
import static com.hbm.blocks.NtmBlocks.BLOCK_WASTE_VITRIFIED;
import static com.hbm.blocks.NtmBlocks.BRICK_ASBESTOS;
import static com.hbm.blocks.NtmBlocks.NUKE_FSTBMB;
import static com.hbm.items.NtmItems.*;

@SuppressWarnings("unused") //shut the fuck up
public class HazardRegistry {

    // CO60		                 5a		β−	030.00Rad/s	Spicy
    // SR90		                29a		β−	015.00Rad/s Spicy
    // TC99		           211,000a		β−	002.75Rad/s	Spicy
    // I181		                92h		β−	150.00Rad/s	2 much spice :(
    // XE135		             9h		β−	aaaaaaaaaaaaaaaa
    // CS137		            30a		β−	020.00Rad/s	Spicy
    // AU198		            64h		β−	500.00Rad/s	2 much spice :(
    // PB209		             3h		β−	10,000.00Rad/s mama mia my face is melting off
    // AT209		             5h		β+	like 7.5k or sth idk bruv
    // PO210		           138d		α	075.00Rad/s	Spicy
    // RA226		         1,600a		α	007.50Rad/s
    // AC227		            22a		β−	030.00Rad/s Spicy
    // TH232		14,000,000,000a		α	000.10Rad/s
    // U233		           160,000a		α	005.00Rad/s
    // U235		       700,000,000a		α	001.00Rad/s
    // U238		     4,500,000,000a		α	000.25Rad/s
    // NP237		     2,100,000a		α	002.50Rad/s
    // PU238		            88a		α	010.00Rad/s	Spicy
    // PU239		        24,000a		α	005.00Rad/s
    // PU240		         6,600a		α	007.50Rad/s
    // PU241		            14a		β−	025.00Rad/s	Spicy
    // AM241		           432a		α	008.50Rad/s
    // AM242		           141a		β−	009.50Rad/s

    //simplified groups for ReC compat
    public static final float gen_S = 10_000F;
    public static final float gen_H = 2_000F;
    public static final float gen_10D = 100F;
    public static final float gen_100D = 80F;
    public static final float gen_1Y = 50F;
    public static final float gen_10Y = 30F;
    public static final float gen_100Y = 10F;
    public static final float gen_1K = 7.5F;
    public static final float gen_10K = 6.25F;
    public static final float gen_100K = 5F;
    public static final float gen_1M = 2.5F;
    public static final float gen_10M = 1.5F;
    public static final float gen_100M = 1F;
    public static final float gen_1B = 0.5F;
    public static final float gen_10B = 0.1F;

    public static final float co60 = 30.0F;
    public static final float sr90 = 15.0F;
    public static final float tc99 = 2.75F;
    public static final float i131 = 150.0F;
    public static final float xe135 = 1250.0F;
    public static final float cs137 = 20.0F;
    public static final float au198 = 500.0F;
    public static final float pb209 = 10000.0F;
    public static final float at209 = 7500.0F;
    public static final float po210 = 75.0F;
    public static final float ra226 = 7.5F;
    public static final float ac227 = 30.0F;
    public static final float th232 = 0.1F;
    public static final float thf = 1.75F;
    public static final float u = 0.35F;
    public static final float uzh = 0.125F;
    public static final float u233 = 5.0F;
    public static final float u235 = 1.0F;
    public static final float u238 = 0.25F;
    public static final float uf = 0.5F;
    public static final float np237 = 2.5F;
    public static final float npf = 1.5F;
    public static final float pu = 7.5F;
    public static final float purg = 6.25F;
    public static final float pu238 = 10.0F;
    public static final float pu239 = 5.0F;
    public static final float pu240 = 7.5F;
    public static final float pu241 = 25.0F;
    public static final float puf = 4.25F;
    public static final float am241 = 8.5F;
    public static final float am242 = 9.5F;
    public static final float amrg = 9.0F;
    public static final float amf = 4.75F;
    public static final float mox = 2.5F;
    public static final float sa326 = 15.0F;
    public static final float sa327 = 17.5F;
    public static final float saf = 5.85F;
    public static final float sas3 = 5F;
    public static final float gh336 = 5.0F;
    public static final float mud = 1.0F;
    public static final float radsource_mult = 3.0F;
    public static final float pobe = po210 * radsource_mult;
    public static final float rabe = ra226 * radsource_mult;
    public static final float pube = pu238 * radsource_mult;
    public static final float zfb_bi = u235 * 0.35F;
    public static final float zfb_pu241 = pu241 * 0.5F;
    public static final float zfb_am_mix = amrg * 0.5F;
    public static final float bf = 300_000.0F;
    public static final float bfb = 500_000.0F;

    public static final float sr = sa326 * 0.1F;
    public static final float sb = sa326 * 0.1F;
    public static final float trx = 25.0F;
    public static final float trn = 0.1F;
    public static final float wst = 15.0F;
    public static final float wstv = 7.5F;
    public static final float yc = u;
    public static final float fo = 10F;

    public static final float nugget = 0.1F;
    public static final float ingot = 1.0F;
    public static final float gem = 1.0F;
    public static final float plate = ingot;
    public static final float plateCast = plate * 3;
    public static final float powder_mult = 3.0F;
    public static final float powder = ingot * powder_mult;
    public static final float powder_tiny = nugget * powder_mult;
    public static final float ore = ingot;
    public static final float block = 10.0F;
    public static final float crystal = block;
    public static final float billet = 0.5F;
    public static final float rtg = billet * 3;
    public static final float rod = 0.5F;
    public static final float rod_dual = rod * 2;
    public static final float rod_quad = rod * 4;
    public static final float rod_rbmk = rod * 8;

    public static final HazardTypeBase RADIATION = new HazardTypeRadiation();
    public static final HazardTypeBase DIGAMMA = new HazardTypeDigamma();
    public static final HazardTypeBase HOT = new HazardTypeHot();
    public static final HazardTypeBase BLINDING = new HazardTypeBlinding();
    public static final HazardTypeBase ASBESTOS = new HazardTypeAsbestos();
    public static final HazardTypeBase COAL = new HazardTypeCoal();
    public static final HazardTypeBase HYDROACTIVE = new HazardTypeHydroactive();
    public static final HazardTypeBase EXPLOSIVE = new HazardTypeExplosive();

    public static void registerItems() {
        HazardSystem.register(Items.GUNPOWDER, makeData(EXPLOSIVE, 1F));
        HazardSystem.register(Items.PUMPKIN_PIE, makeData(EXPLOSIVE, 1F));
        HazardSystem.register(Blocks.TNT, makeData(EXPLOSIVE, 4F));

        /*
         * Die Sprengstoffe, Runde 239. Das Original zaehlt sie im Original an derselben
         * Stelle auf (HazardRegistry, Z. 166-174). Ballistit fehlt im Port -- deshalb steht
         * hier nur das Cordit.
         */
        HazardSystem.register(item(BALL_DYNAMITE), makeData(EXPLOSIVE, 2F));
        HazardSystem.register(item(STICK_DYNAMITE), makeData(EXPLOSIVE, 1F));
        HazardSystem.register(item(STICK_TNT), makeData(EXPLOSIVE, 1.5F));
        HazardSystem.register(item(STICK_SEMTEX), makeData(EXPLOSIVE, 2.5F));
        /* Ballistit ist halb so heftig wie Cordit -- 1F gegen 2F, beides aus dem Original. */
        HazardSystem.register(item(BALLISTITE), makeData(EXPLOSIVE, 1F));
        HazardSystem.register(item(CORDITE), makeData(EXPLOSIVE, 2F));

        HazardSystem.register(item(CELL_TRITIUM), makeData(RADIATION, 0.001F));
        HazardSystem.register(item(CELL_SAS3), makeData().addEntry(RADIATION, sas3).addEntry(BLINDING, 60F));
        HazardSystem.register(item(CELL_BALEFIRE), makeData(RADIATION, 50F));
        HazardSystem.register(item(DEMON_CORE_OPEN), makeData(RADIATION, 5F));
        HazardSystem.register(item(EGG_BALEFIRE_SHARD), makeData(RADIATION, bf * nugget));
        HazardSystem.register(item(EGG_BALEFIRE), makeData(RADIATION, bf * ingot));

        HazardSystem.register(Items.GOLD_INGOT, makeData(BLINDING, 4F));

        HazardSystem.register(Items.BLAZE_POWDER, makeData(HOT, 4F));
        HazardSystem.register(Blocks.GREEN_WOOL, makeData(RADIATION, 150F));
        HazardSystem.register(Blocks.WHITE_WOOL, makeData(ASBESTOS, 5F));
        HazardSystem.register(Items.COAL, makeData(COAL, 1F));

        HazardSystem.register(NtmBlocks.FALLOUT.get(), makeData(RADIATION, 60F));

        HazardSystem.register(NtmItems.PARTICLE_DIGAMMA.get(), makeData(DIGAMMA, 0.3333F));

        registerBreedingRodRadiation(BreedingRodType.TRITIUM, 0.001F);
        registerBreedingRodRadiation(BreedingRodType.CO60, co60);
        registerBreedingRodRadiation(BreedingRodType.RA226, ra226);
        registerBreedingRodRadiation(BreedingRodType.AC227, ac227);
        registerBreedingRodRadiation(BreedingRodType.TH232, th232);
        registerBreedingRodRadiation(BreedingRodType.THF, thf);
        registerBreedingRodRadiation(BreedingRodType.U235, u235);
        registerBreedingRodRadiation(BreedingRodType.NP237, np237);
        registerBreedingRodRadiation(BreedingRodType.U238, u238);
        registerBreedingRodRadiation(BreedingRodType.PU238, pu238); //it's in a container :)
        registerBreedingRodRadiation(BreedingRodType.PU239, pu239);
        registerBreedingRodRadiation(BreedingRodType.RGP, purg);
        registerBreedingRodRadiation(BreedingRodType.WASTE, wst);
        registerBreedingRodRadiation(BreedingRodType.URANIUM, u);

        /*
         * Der Reaktorzweig. Alle Werte unveraendert aus dem Original; die Kurven stecken in den
         * vier Modifikatoren. rbmk_fuel_test hat wie im Original keinen Eintrag.
         */

        registerRBMKRod(NtmItems.RBMK_FUEL_UEU.get(), u * rod_rbmk, wst * rod_rbmk * 20F);
        registerRBMKRod(NtmItems.RBMK_FUEL_MEU.get(), uf * rod_rbmk, wst * rod_rbmk * 21.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_HEU233.get(), u233 * rod_rbmk, wst * rod_rbmk * 31F);
        registerRBMKRod(NtmItems.RBMK_FUEL_HEU235.get(), u235 * rod_rbmk, wst * rod_rbmk * 30F);
        registerRBMKRod(NtmItems.RBMK_FUEL_UZH.get(), uzh * rod_rbmk, wst * rod_rbmk * 20F);
        registerRBMKRod(NtmItems.RBMK_FUEL_THMEU.get(), thf * rod_rbmk, wst * rod_rbmk * 17.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_LEP.get(), puf * rod_rbmk, wst * rod_rbmk * 25F);
        registerRBMKRod(NtmItems.RBMK_FUEL_MEP.get(), purg * rod_rbmk, wst * rod_rbmk * 30F);
        registerRBMKRod(NtmItems.RBMK_FUEL_HEP.get(), pu239 * rod_rbmk, wst * rod_rbmk * 32.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_HEP241.get(), pu241 * rod_rbmk, wst * rod_rbmk * 35F);
        registerRBMKRod(NtmItems.RBMK_FUEL_LEA.get(), amf * rod_rbmk, wst * rod_rbmk * 26F);
        registerRBMKRod(NtmItems.RBMK_FUEL_MEA.get(), amrg * rod_rbmk, wst * rod_rbmk * 30.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_HEA241.get(), am241 * rod_rbmk, wst * rod_rbmk * 33.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_HEA242.get(), am242 * rod_rbmk, wst * rod_rbmk * 34F);
        registerRBMKRod(NtmItems.RBMK_FUEL_MEN.get(), npf * rod_rbmk, wst * rod_rbmk * 22.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_HEN.get(), np237 * rod_rbmk, wst * rod_rbmk * 30F);
        registerRBMKRod(NtmItems.RBMK_FUEL_MOX.get(), mox * rod_rbmk, wst * rod_rbmk * 25.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_LES.get(), saf * rod_rbmk, wst * rod_rbmk * 24.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_MES.get(), saf * rod_rbmk, wst * rod_rbmk * 30F);
        registerRBMKRod(NtmItems.RBMK_FUEL_HES.get(), saf * rod_rbmk, wst * rod_rbmk * 50F);
        registerRBMKRod(NtmItems.RBMK_FUEL_LEAUS.get(), 0F, wst * rod_rbmk * 37.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_HEAUS.get(), 0F, wst * rod_rbmk * 32.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_PO210BE.get(), pobe * rod_rbmk, pobe * rod_rbmk * 0.1F, true);
        registerRBMKRod(NtmItems.RBMK_FUEL_RA226BE.get(), rabe * rod_rbmk, rabe * rod_rbmk * 0.4F, true);
        registerRBMKRod(NtmItems.RBMK_FUEL_PU238BE.get(), pube * rod_rbmk, wst * rod_rbmk * 2.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_BALEFIRE_GOLD.get(), au198 * rod_rbmk, bf * rod_rbmk * 0.5F, true);
        registerRBMKRod(NtmItems.RBMK_FUEL_FLASHLEAD.get(), pb209 * 1.25F * rod_rbmk, pb209 * nugget * 0.05F * rod_rbmk, true);
        registerRBMKRod(NtmItems.RBMK_FUEL_BALEFIRE.get(), bf * rod_rbmk, bf * rod_rbmk * 100F, true);
        registerRBMKRod(NtmItems.RBMK_FUEL_ZFB_BISMUTH.get(), pu241 * rod_rbmk * 0.1F, wst * rod_rbmk * 5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_ZFB_PU241.get(), pu239 * rod_rbmk * 0.1F, wst * rod_rbmk * 7.5F);
        registerRBMKRod(NtmItems.RBMK_FUEL_ZFB_AM_MIX.get(), pu241 * rod_rbmk * 0.1F, wst * rod_rbmk * 10F);
        registerRBMK(NtmItems.RBMK_FUEL_DRX.get(), bf * rod_rbmk, bf * rod_rbmk * 100F, true, true, 0, 1F/3F);

        registerRBMKPellet(NtmItems.RBMK_PELLET_UEU.get(), u * billet, wst * billet * 20F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_MEU.get(), uf * billet, wst * billet * 21.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_HEU233.get(), u233 * billet, wst * billet * 31F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_HEU235.get(), u235 * billet, wst * billet * 30F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_UZH.get(), uzh * billet, wst * billet * 20F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_THMEU.get(), thf * billet, wst * billet * 17.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_LEP.get(), puf * billet, wst * billet * 25F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_MEP.get(), purg * billet, wst * billet * 30F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_HEP.get(), pu239 * billet, wst * billet * 32.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_HEP241.get(), pu241 * billet, wst * billet * 35F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_LEA.get(), amf * billet, wst * billet * 26F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_MEA.get(), amrg * billet, wst * billet * 30.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_HEA241.get(), am241 * billet, wst * billet * 33.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_HEA242.get(), am242 * billet, wst * billet * 34F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_MEN.get(), npf * billet, wst * billet * 22.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_HEN.get(), np237 * billet, wst * billet * 30F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_MOX.get(), mox * billet, wst * billet * 25.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_LES.get(), saf * billet, wst * billet * 24.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_MES.get(), saf * billet, wst * billet * 30F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_HES.get(), saf * billet, wst * billet * 50F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_LEAUS.get(), 0F, wst * billet * 37.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_HEAUS.get(), 0F, wst * billet * 32.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_PO210BE.get(), pobe * billet, pobe * billet * 0.1F, true);
        registerRBMKPellet(NtmItems.RBMK_PELLET_RA226BE.get(), rabe * billet, rabe * billet * 0.4F, true);
        registerRBMKPellet(NtmItems.RBMK_PELLET_PU238BE.get(), pube * billet, wst * 1.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_BALEFIRE_GOLD.get(), au198 * billet, bf * billet * 0.5F, true);
        registerRBMKPellet(NtmItems.RBMK_PELLET_FLASHLEAD.get(), pb209 * 1.25F * billet, pb209 * nugget * 0.05F, true);
        registerRBMKPellet(NtmItems.RBMK_PELLET_BALEFIRE.get(), bf * billet, bf * billet * 100F, true);
        registerRBMKPellet(NtmItems.RBMK_PELLET_ZFB_BISMUTH.get(), pu241 * billet * 0.1F, wst * billet * 5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_ZFB_PU241.get(), pu239 * billet * 0.1F, wst * billet * 7.5F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_ZFB_AM_MIX.get(), pu241 * billet * 0.1F, wst * billet * 10F);
        registerRBMKPellet(NtmItems.RBMK_PELLET_DRX.get(), bf * billet, bf * billet * 100F, true, 0F, 1F/24F);

        registerOtherWaste(NtmItems.WASTE_NATURAL_URANIUM.get(), wst * billet * 11.5F);
        registerOtherWaste(NtmItems.WASTE_URANIUM.get(), wst * billet * 10F);
        registerOtherWaste(NtmItems.WASTE_THORIUM.get(), wst * billet * 7.5F);
        registerOtherWaste(NtmItems.WASTE_MOX.get(), wst * billet * 10F);
        registerOtherWaste(NtmItems.WASTE_PLUTONIUM.get(), wst * billet * 12.5F);
        registerOtherWaste(NtmItems.WASTE_U233.get(), wst * billet * 10F);
        registerOtherWaste(NtmItems.WASTE_U235.get(), wst * billet * 11F);
        registerOtherWaste(NtmItems.WASTE_SCHRABIDIUM.get(), wst * billet * 15F);
        registerOtherWaste(NtmItems.WASTE_ZFB_MOX.get(), wst * billet * 5F);
        registerOtherWaste(NtmItems.WASTE_PLATE_U233.get(), wst * ingot * 13F);
        registerOtherWaste(NtmItems.WASTE_PLATE_U235.get(), wst * ingot * 10F);
        registerOtherWaste(NtmItems.WASTE_PLATE_MOX.get(), wst * ingot * 16F);
        registerOtherWaste(NtmItems.WASTE_PLATE_PU239.get(), wst * ingot * 13.5F);
        registerOtherWaste(NtmItems.WASTE_PLATE_SA326.get(), wst * ingot * 10F);
        registerRadSourceWaste(NtmItems.WASTE_PLATE_RA226BE.get(), pobe * nugget * 3);
        registerRadSourceWaste(NtmItems.WASTE_PLATE_PU238BE.get(), pube * nugget * 1);

        /*
         * ZIRNOX-Brennstaebe. Die Untertypen liegen als Metadaten auf einem Item; ihr abgebranntes
         * Gegenstueck ist jeweils ein eigenes Item, genau wie im Original.
         */
        registerOtherFuel(NtmItems.ROD_ZIRNOX.get(), ZirnoxType.NATURAL_URANIUM_FUEL.ordinal(), u * rod_dual, wst * rod_dual * 11.5F, false);
        registerOtherFuel(NtmItems.ROD_ZIRNOX.get(), ZirnoxType.URANIUM_FUEL.ordinal(), uf * rod_dual, wst * rod_dual * 10F, false);
        registerOtherFuel(NtmItems.ROD_ZIRNOX.get(), ZirnoxType.TH232.ordinal(), th232 * rod_dual, thf * rod_dual, false);
        registerOtherFuel(NtmItems.ROD_ZIRNOX.get(), ZirnoxType.THORIUM_FUEL.ordinal(), thf * rod_dual, wst * rod_dual * 7.5F, false);
        registerOtherFuel(NtmItems.ROD_ZIRNOX.get(), ZirnoxType.MOX_FUEL.ordinal(), mox * rod_dual, wst * rod_dual * 10F, false);
        registerOtherFuel(NtmItems.ROD_ZIRNOX.get(), ZirnoxType.PLUTONIUM_FUEL.ordinal(), puf * rod_dual, wst * rod_dual * 12.5F, false);
        registerOtherFuel(NtmItems.ROD_ZIRNOX.get(), ZirnoxType.U233_FUEL.ordinal(), u233 * rod_dual, wst * rod_dual * 10F, false);
        registerOtherFuel(NtmItems.ROD_ZIRNOX.get(), ZirnoxType.U235_FUEL.ordinal(), u235 * rod_dual, wst * rod_dual * 11F, false);
        registerOtherFuel(NtmItems.ROD_ZIRNOX.get(), ZirnoxType.LES_FUEL.ordinal(), saf * rod_dual, wst * rod_dual * 15F, false);
        registerOtherFuel(NtmItems.ROD_ZIRNOX.get(), ZirnoxType.LITHIUM.ordinal(), 0, 0.001F * rod_dual, false);
        registerOtherFuel(NtmItems.ROD_ZIRNOX.get(), ZirnoxType.ZFB_MOX.ordinal(), mox * rod_dual, wst * rod_dual * 5F, false);

        HazardSystem.register(item(NtmItems.ROD_ZIRNOX_NATURAL_URANIUM_FUEL_DEPLETED), makeData(RADIATION, wst * rod_dual * 11.5F));
        HazardSystem.register(item(NtmItems.ROD_ZIRNOX_URANIUM_FUEL_DEPLETED), makeData(RADIATION, wst * rod_dual * 10F));
        HazardSystem.register(item(NtmItems.ROD_ZIRNOX_THORIUM_FUEL_DEPLETED), makeData(RADIATION, wst * rod_dual * 7.5F));
        HazardSystem.register(item(NtmItems.ROD_ZIRNOX_MOX_FUEL_DEPLETED), makeData(RADIATION, wst * rod_dual * 10F));
        HazardSystem.register(item(NtmItems.ROD_ZIRNOX_PLUTONIUM_FUEL_DEPLETED), makeData(RADIATION, wst * rod_dual * 12.5F));
        HazardSystem.register(item(NtmItems.ROD_ZIRNOX_U233_FUEL_DEPLETED), makeData(RADIATION, wst * rod_dual * 10F));
        HazardSystem.register(item(NtmItems.ROD_ZIRNOX_U235_FUEL_DEPLETED), makeData(RADIATION, wst * rod_dual * 11F));
        HazardSystem.register(item(NtmItems.ROD_ZIRNOX_LES_FUEL_DEPLETED), makeData().addEntry(RADIATION, wst * rod_dual * 15F).addEntry(BLINDING, 20F));
        HazardSystem.register(item(NtmItems.ROD_ZIRNOX_TRITIUM), makeData(RADIATION, 0.001F * rod_dual));
        HazardSystem.register(item(NtmItems.ROD_ZIRNOX_ZFB_MOX_DEPLETED), makeData(RADIATION, wst * rod_dual * 5F));

        /*
         * Watz-Pellets. Die Werte stehen unveraendert im Original -- das Vierfache eines Barrens
         * des jeweiligen Stoffs. Blei und Bor haben keinen Eintrag: sie strahlen nicht, sie
         * schlucken.
         *
         * Nur der frische Brennstoff traegt Strahlung. Das abgebrannte Pellet hat im Original
         * ebenfalls keinen Eintrag; das ist auffaellig, aber unveraendert uebernommen.
         */
        registerWatzPellet(EnumWatzType.SCHRABIDIUM, sa326);
        registerWatzPellet(EnumWatzType.HES, saf);
        registerWatzPellet(EnumWatzType.MES, saf);
        registerWatzPellet(EnumWatzType.LES, saf);
        registerWatzPellet(EnumWatzType.HEN, np237);
        registerWatzPellet(EnumWatzType.MEU, uf);
        registerWatzPellet(EnumWatzType.MEP, purg);
        registerWatzPellet(EnumWatzType.DU, u238);
        registerWatzPellet(EnumWatzType.NQD, u235);
        registerWatzPellet(EnumWatzType.NQR, pu239);

        /*
         * PWR-Brennstoff. Seit Runde 54 im Spiel, aber ohne Gefahrendaten -- ein Stab
         * hochangereicherten Schrabidiums war bis hierher so harmlos wie ein Stein.
         *
         * NICHT UEBERNOMMEN: die Eintraege fuer pwr_fuel_hot und pwr_fuel_depleted. Beide
         * Gegenstaende fehlen im Port noch; sie gehoeren zum Brennstoffkreislauf des
         * Druckwasserreaktors.
         */
        registerPWRFuel(EnumPWRFuel.MEU, uf * billet * 2);
        registerPWRFuel(EnumPWRFuel.HEU233, u233 * billet * 2);
        registerPWRFuel(EnumPWRFuel.HEU235, u235 * billet * 2);
        registerPWRFuel(EnumPWRFuel.MEN, npf * billet * 2);
        registerPWRFuel(EnumPWRFuel.HEN237, np237 * billet * 2);
        registerPWRFuel(EnumPWRFuel.MOX, mox * billet * 2);
        registerPWRFuel(EnumPWRFuel.MEP, purg * billet * 2);
        registerPWRFuel(EnumPWRFuel.HEP239, pu239 * billet * 2);
        registerPWRFuel(EnumPWRFuel.HEP241, pu241 * billet * 2);
        registerPWRFuel(EnumPWRFuel.MEA, amrg * billet * 2);
        registerPWRFuel(EnumPWRFuel.HEA242, am242 * billet * 2);
        registerPWRFuel(EnumPWRFuel.HES326, sa326 * billet * 2);
        registerPWRFuel(EnumPWRFuel.HES327, sa327 * billet * 2);
        registerPWRFuel(EnumPWRFuel.BFB_AM_MIX, amrg * billet);
        registerPWRFuel(EnumPWRFuel.BFB_PU241, pu241 * billet);

        /*
         * Brennstoffplatten des Forschungsreaktors. Die Items sind bis zum Port von ItemPlateFuel
         * noch Platzhalter ohne Laufzeit; der Grundwert stimmt damit schon, die Abbrandkurve
         * greift erst, sobald die Platten ihre Restlaufzeit mitfuehren.
         */
        registerOtherFuel(NtmItems.PLATE_FUEL_U233.get(), u233 * ingot, wst * ingot * 13F, false);
        registerOtherFuel(NtmItems.PLATE_FUEL_U235.get(), u235 * ingot, wst * ingot * 10F, false);
        registerOtherFuel(NtmItems.PLATE_FUEL_MOX.get(), mox * ingot, wst * ingot * 16F, false);
        registerOtherFuel(NtmItems.PLATE_FUEL_PU239.get(), pu239 * ingot, wst * ingot * 13.5F, false);
        registerOtherFuel(NtmItems.PLATE_FUEL_SA326.get(), sa326 * ingot, wst * ingot * 10F, true);
        registerOtherFuel(NtmItems.PLATE_FUEL_RA226BE.get(), rabe * billet, pobe * nugget * 3, false);
        registerOtherFuel(NtmItems.PLATE_FUEL_PU238BE.get(), pube * billet, pube * nugget * 1, false);

        /*
         * RTG-Pellets. Im Original je ein eigenes Item, im Port Metadaten auf PELLET_RTG -- die
         * Werte selbst sind unveraendert.
         */
        registerRTGPellet(RTGPelletType.PU238, pu238 * rtg, 0, 3F);
        registerRTGPellet(RTGPelletType.RADIUM, ra226 * rtg, 0);
        registerRTGPellet(RTGPelletType.WEAK, (pu238 + (u238 * 2)) * billet, 0);
        registerRTGPellet(RTGPelletType.STRONTIUM, sr90 * rtg, 0);
        registerRTGPellet(RTGPelletType.COBALT, co60 * rtg, 0);
        registerRTGPellet(RTGPelletType.ACTINIUM, ac227 * rtg, 0);
        registerRTGPellet(RTGPelletType.POLONIUM, po210 * rtg, 0, 3F);
        registerRTGPellet(RTGPelletType.LEAD, pb209 * rtg, 0, 7F, 50F);
        registerRTGPellet(RTGPelletType.GOLD, au198 * rtg, 0, 5F);
        registerRTGPellet(RTGPelletType.AMERICIUM, am241 * rtg, 0);

        // Nur Neptunium strahlt noch; die uebrigen Zerfallsprodukte sind gewoehnliche Metalle.
        HazardSystem.register(MetaHelper.newStack(NtmItems.PELLET_RTG_DEPLETED, 1, DepletedRTGMaterial.NEPTUNIUM.ordinal()), makeData(RADIATION, np237 * rtg));

        /*
         * Runde 41: der Abfall selbst war bisher harmlos -- ein Loch, das erst auffiel, als die
         * PUREX-Wiederaufbereitung anfing, Abfallkrumen stapelweise auszuwerfen. Werte
         * unveraendert aus dem Original.
         */
        HazardSystem.register(item(NUCLEAR_WASTE), makeData(RADIATION, wst * ingot));
        HazardSystem.register(item(NUCLEAR_WASTE_TINY), makeData(RADIATION, wst * nugget));
        HazardSystem.register(item(BILLET_NUCLEAR_WASTE), makeData(RADIATION, wst * billet));
        HazardSystem.register(item(NUCLEAR_WASTE_VITRIFIED), makeData(RADIATION, wstv * ingot));
        HazardSystem.register(block(BLOCK_WASTE), makeData(RADIATION, wst * block));
        /* Corium, fluessig wie erstarrt: der heisseste Feststoff im ganzen Mod. */
        HazardSystem.register(block(BLOCK_CORIUM), makeData(RADIATION, 150F));
        HazardSystem.register(block(BLOCK_CORIUM_COBBLE), makeData(RADIATION, 150F));
        /* Der Uraltschrott strahlt genauso stark wie Corium -- 150, im Original Z. 218. */
        HazardSystem.register(block(ANCIENT_SCRAP), makeData(RADIATION, 150F));
        HazardSystem.register(block(BLOCK_WASTE_PAINTED), makeData(RADIATION, wst * block));
        HazardSystem.register(block(BLOCK_WASTE_VITRIFIED), makeData(RADIATION, wstv * block));

        HazardSystem.register(block(BRICK_ASBESTOS), makeData(ASBESTOS, 1F));

        //nuke parts
        HazardSystem.register(item(LITTLE_BOY_PROPELLANT), makeData(EXPLOSIVE, 2F));
        HazardSystem.register(item(GADGET_CORE), makeData(RADIATION, pu239 * nugget * 10));
        HazardSystem.register(item(LITTLE_BOY_TARGET), makeData(RADIATION, u235 * ingot * 2));
        HazardSystem.register(item(LITTLE_BOY_BULLET), makeData(RADIATION, u235 * ingot));
        HazardSystem.register(item(IVY_MIKE_CORE), makeData(RADIATION, u238 * nugget * 10));
        HazardSystem.register(item(FAT_MAN_CORE), makeData(RADIATION, pu239 * nugget * 10));

        HazardSystem.register(item(FLEIJA_PROPELLANT), makeData().addEntry(RADIATION, 15F).addEntry(EXPLOSIVE, 8F).addEntry(BLINDING, 50F));
        HazardSystem.register(item(FLEIJA_CORE), makeData(RADIATION, 10F));

        HazardSystem.register(item(SOLINIUM_PROPELLANT), makeData(EXPLOSIVE, 10F));
        HazardSystem.register(item(SOLINIUM_CORE), makeData().addEntry(RADIATION, sa327 * nugget * 8).addEntry(BLINDING, 45F));

        HazardSystem.register(block(NUKE_FSTBMB), makeData(DIGAMMA, 0.01F));
    }


    /*
     * Die Registrierhilfen des Reaktorzweigs, wortgetreu aus dem Original uebernommen. Die
     * Ueberladungen ohne Zusatzargumente setzen die Vorgabewerte des Originals ein.
     */

    private static void registerRBMKPellet(Item pellet, float base, float dep) { registerRBMKPellet(pellet, base, dep, false, 0F, 0F); }
    private static void registerRBMKPellet(Item pellet, float base, float dep, boolean linear) { registerRBMKPellet(pellet, base, dep, linear, 0F, 0F); }

    private static void registerRBMKPellet(Item pellet, float base, float dep, boolean linear, float blinding, float digamma) {
        HazardData data = new HazardData();
        data.addEntry(new HazardEntry(RADIATION, base).addMod(new HazardModifierRBMKRadiation(dep, linear)));
        if(blinding > 0) data.addEntry(new HazardEntry(BLINDING, blinding));
        if(digamma > 0) data.addEntry(new HazardEntry(DIGAMMA, digamma));
        HazardSystem.register(pellet, data);
    }

    private static void registerRBMKRod(Item rod, float base, float dep) { registerRBMK(rod, base, dep, true, false, 0F, 0F); }
    private static void registerRBMKRod(Item rod, float base, float dep, boolean linear) { registerRBMK(rod, base, dep, true, linear, 0F, 0F); }

    private static void registerRBMK(Item rod, float base, float dep, boolean hot, boolean linear, float blinding, float digamma) {
        HazardData data = new HazardData();
        data.addEntry(new HazardEntry(RADIATION, base).addMod(new HazardModifierRBMKRadiation(dep, linear)));
        if(hot) data.addEntry(new HazardEntry(HOT, 0).addMod(new HazardModifierRBMKHot()));
        if(blinding > 0) data.addEntry(new HazardEntry(BLINDING, blinding));
        if(digamma > 0) data.addEntry(new HazardEntry(DIGAMMA, digamma));
        HazardSystem.register(rod, data);
    }

    private static void registerOtherFuel(Item fuel, float base, float target, boolean blinding) {
        HazardSystem.register(fuel, makeOtherFuel(base, target, blinding));
    }

    private static void registerOtherFuel(Item fuel, int meta, float base, float target, boolean blinding) {
        HazardSystem.register(MetaHelper.newStack(fuel, 1, meta), makeOtherFuel(base, target, blinding));
    }

    private static HazardData makeOtherFuel(float base, float target, boolean blinding) {
        HazardData data = new HazardData();
        data.addEntry(new HazardEntry(RADIATION, base).addMod(new HazardModifierFuelRadiation(target)));
        if(blinding) data.addEntry(BLINDING, 20F);
        return data;
    }

    private static void registerRTGPellet(RTGPelletType type, float base, float target) { registerRTGPellet(type, base, target, 0, 0); }
    private static void registerRTGPellet(RTGPelletType type, float base, float target, float hot) { registerRTGPellet(type, base, target, hot, 0); }

    private static void registerRTGPellet(RTGPelletType type, float base, float target, float hot, float blinding) {
        HazardData data = new HazardData();
        data.addEntry(new HazardEntry(RADIATION, base).addMod(new HazardModifierRTGRadiation(target)));
        if(hot > 0) data.addEntry(new HazardEntry(HOT, hot));
        if(blinding > 0) data.addEntry(new HazardEntry(BLINDING, blinding));
        HazardSystem.register(MetaHelper.newStack(NtmItems.PELLET_RTG, 1, type.ordinal()), data);
    }

    /* Frisch abgebrannter Abfall ist heiss; abgekuehlt bleibt ein Bruchteil der Strahlung. */
    private static void registerOtherWaste(Item waste, float base) {
        registerWaste(waste, base * 0.075F, base);
    }

    /* Neutronenquellen strahlen auch kalt in voller Hoehe weiter. */
    private static void registerRadSourceWaste(Item waste, float base) {
        registerWaste(waste, base, base);
    }

    private static void registerWaste(Item waste, float cool, float hot) {
        HazardSystem.register(MetaHelper.newStack(waste, 1, DepletedFuelItem.COOL), makeData(RADIATION, cool));

        HazardData data = new HazardData();
        data.addEntry(new HazardEntry(RADIATION, hot));
        data.addEntry(new HazardEntry(HOT, 5F));
        HazardSystem.register(MetaHelper.newStack(waste, 1, DepletedFuelItem.HOT), data);
    }

    private static void registerBreedingRodRadiation(BreedingRodType type, float base) {
        HazardSystem.register(MetaHelper.newStack(ROD, 1, type.ordinal()), makeData(RADIATION, base));
        HazardSystem.register(MetaHelper.newStack(ROD_DUAL, 1, type.ordinal()), makeData(RADIATION, base * rod_dual));
        HazardSystem.register(MetaHelper.newStack(ROD_QUAD, 1, type.ordinal()), makeData(RADIATION, base * rod_quad));
    }

    private static void registerWatzPellet(EnumWatzType type, float base) {
        HazardSystem.register(MetaHelper.newStack(NtmItems.WATZ_PELLET.get(), 1, type.ordinal()), makeData(RADIATION, base * ingot * 4));
    }

    private static void registerPWRFuel(EnumPWRFuel fuel, float baseRad) {
        HazardSystem.register(MetaHelper.newStack(NtmItems.PWR_FUEL.get(), 1, fuel.ordinal()), makeData(RADIATION, baseRad));
    }

    private static Item item(DeferredItem<Item> item) { return item.get(); }
    private static Block block(DeferredBlock<Block> block) { return block.get(); }

    private static HazardData makeData() { return new HazardData(); }
    private static HazardData makeData(HazardTypeBase hazard) { return new HazardData().addEntry(hazard); }
    private static HazardData makeData(HazardTypeBase hazard, float level) { return new HazardData().addEntry(hazard, level); }
    private static HazardData makeData(HazardTypeBase hazard, float level, boolean override) { return new HazardData().addEntry(hazard, level, override); }
}
