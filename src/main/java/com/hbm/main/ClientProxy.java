package com.hbm.main;

import com.hbm.blockentity.IScreenProvider;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.factory.LegoClient;
import com.hbm.render.entity.projectile.TauShotRenderer;
import com.hbm.render.blockentity.*;
import com.hbm.render.entity.EmptyEntityRenderer;
import com.hbm.render.entity.effect.*;
import com.hbm.render.entity.item.RenderFallingBlockEntityNT;
import com.hbm.render.entity.item.RenderMovingItem;
import com.hbm.render.entity.item.RenderParachuteCrate;
import com.hbm.render.entity.item.RenderMovingPackage;
import com.hbm.render.entity.item.RenderTNTPrimedBase;
import com.hbm.render.entity.mob.CreeperNuclearRenderer;
import com.hbm.render.entity.mob.CreeperGoldRenderer;
import com.hbm.render.entity.mob.CreeperPhosgeneRenderer;
import com.hbm.render.entity.mob.CreeperTaintedRenderer;
import com.hbm.render.entity.mob.MaskManRenderer;
import com.hbm.render.entity.mob.CreeperVolatileRenderer;
import com.hbm.render.entity.mob.DuckRenderer;
import com.hbm.render.entity.mob.CyberCrabRenderer;
import com.hbm.render.entity.mob.TaintCrabRenderer;
import com.hbm.render.entity.mob.TeslaCrabRenderer;
import com.hbm.render.entity.mob.UndeadSoldierRenderer;
import com.hbm.render.entity.projectile.*;
import com.hbm.render.entity.rocket.*;
import com.hbm.render.item.*;
import com.hbm.render.item.ItemRenderMissileGeneric.RenderMissileType;
import com.hbm.render.item.weapon.sedna.*;
import com.hbm.render.util.RenderInfoSystem;
import com.hbm.render.util.RenderInfoSystem.InfoEntry;
import com.hbm.util.InventoryUtil;
import com.hbm.util.i18n.I18nClient;
import com.hbm.util.i18n.ITranslate;
import com.hbm.util.particle.IParticleCreator;
import com.hbm.util.particle.ParticleCreatorClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.inventory.screens.NoContainerScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class ClientProxy extends ServerProxy {

    private static final I18nClient I18N = new I18nClient();

    private static final IParticleCreator PARTICLE_CREATOR = new ParticleCreatorClient();

    public ITranslate getI18n() { return I18N; }

    public IParticleCreator getParticleCreator() { return PARTICLE_CREATOR; }

    @Override
    public void registerClientExtensions(RegisterClientExtensionsEvent event) {

        /*
         * Hier bekommt jeder Blockentitaeten-Darsteller, der IBEWLRProvider ist, auch einen
         * Darsteller fuer seinen Gegenstand.
         *
         * WARUM HIER GESIEBT WIRD
         * Sieben Darsteller haengen an ZWEI Blockentitaetsarten (Turm und beschaedigter Turm,
         * RBMK-Saeule und ihre Zwillingsform, Giesserei und Becken, ...). Der Lauf ueber
         * PROVIDERS trifft sie darum zweimal, und ihr getItemsForRenderer nennt beide Male
         * beide Gegenstaende. Ohne das Sieb meldet NeoForge beim zweiten Mal
         * "Duplicate client extensions registration" und der Mod bricht beim Laden ab --
         * genau so ist es im Spielprotokoll vom 16.09. passiert.
         */
        Set<Item> alreadyRegistered = new HashSet<>();

        for(Entry<BlockEntityType<?>, BlockEntityRendererProvider<?>> entry : BlockEntityRenderers.PROVIDERS.entrySet()) {

            if(!(entry.getValue() instanceof IBEWLRProvider provider)) continue;

            Item[] fresh = Arrays.stream(provider.getItemsForRenderer())
                    .filter(alreadyRegistered::add)
                    .toArray(Item[]::new);

            if(fresh.length > 0) registerItemRenderer(event, provider.getRenderer(), fresh);
        }

        registerItemRenderer(event, new RenderLaserDetonator(), NtmItems.DETONATOR_LASER.get());
        registerItemRenderer(event, new RenderBoltgunItem(), NtmItems.BOLTGUN.get());

        registerItemRenderer(event, new RenderCableItem(), NtmBlocks.RED_CABLE.asItem());
        registerItemRenderer(event, new RenderDetCordItem(), NtmBlocks.DET_CORD.asItem());

        registerItemRenderer(event, new RenderPipeItem(), NtmBlocks.FLUID_DUCT_NEO.asItem());

        registerItemRenderer(event, new RenderBarrelItem(),
                NtmBlocks.BARREL_RED.asItem(),
                NtmBlocks.BARREL_PINK.asItem(),
                NtmBlocks.BARREL_LOX.asItem(),
                NtmBlocks.BARREL_TAINT.asItem(),
                NtmBlocks.BARREL_PLASTIC.asItem(),
                NtmBlocks.BARREL_STEEL.asItem(),
                NtmBlocks.BARREL_CORRODED.asItem(),
                NtmBlocks.BARREL_TCALLOY.asItem()
        );

        registerItemRenderer(event, new RenderBarbedWireItem(),
                NtmBlocks.BARBED_WIRE.asItem()
        );
        registerItemRenderer(event, new RenderSpikesItem(),
                NtmBlocks.SPIKES.asItem()
        );
        registerItemRenderer(event, new RenderAnvilItem(), NtmBlocks.ANVIL.asItem());
        registerItemRenderer(event, new RenderBatteryPackItem(), NtmItems.BATTERY_PACK.get());

        ItemRenderMissileGeneric.init();
        registerItemRenderer(event, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0),
                NtmItems.MISSILE_TAINT.get(),
                NtmItems.MISSILE_MICRO.get(),
                NtmItems.MISSILE_BHOLE.get(),
                NtmItems.MISSILE_SCHRABIDIUM.get(),
                NtmItems.MISSILE_EMP.get()
        );
        registerItemRenderer(event, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1),
                NtmItems.MISSILE_GENERIC.get(),
                NtmItems.MISSILE_DECOY.get(),
                NtmItems.MISSILE_INCENDIARY.get(),
                NtmItems.MISSILE_CLUSTER.get(),
                NtmItems.MISSILE_BUSTER.get()
        );
        registerItemRenderer(event, new ItemRenderMissileGeneric(RenderMissileType.TYPE_ABM),
                NtmItems.MISSILE_ANTI_BALLISTIC.get()
        );
        registerItemRenderer(event, new ItemRenderMissileGeneric(RenderMissileType.TYPE_STEALTH),
                NtmItems.MISSILE_STEALTH.get()
        );
        registerItemRenderer(event, new ItemRenderMissileGeneric(RenderMissileType.TYPE_ROBIN),
                NtmItems.MISSILE_SHUTTLE.get()
        );
        registerItemRenderer(event, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2),
                NtmItems.MISSILE_STRONG.get(),
                NtmItems.MISSILE_INCENDIARY_STRONG.get(),
                NtmItems.MISSILE_CLUSTER_STRONG.get(),
                NtmItems.MISSILE_BUSTER_STRONG.get(),
                NtmItems.MISSILE_EMP_STRONG.get()
        );
        registerItemRenderer(event, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3),
                NtmItems.MISSILE_BURST.get(),
                NtmItems.MISSILE_INFERNO.get(),
                NtmItems.MISSILE_RAIN.get(),
                NtmItems.MISSILE_DRILL.get()
        );
        registerItemRenderer(event, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR),
                NtmItems.MISSILE_NUCLEAR.get(),
                NtmItems.MISSILE_NUCLEAR_CLUSTER.get(),
                NtmItems.MISSILE_VOLCANO.get(),
                NtmItems.MISSILE_DOOMSDAY.get(),
                NtmItems.MISSILE_DOOMSDAY_RUSTED.get()
        );
    }

    public static void registerItemRenderer(RegisterClientExtensionsEvent event, BlockEntityWithoutLevelRenderer bewlr, Item... items) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if(renderer == null) this.renderer = bewlr;

                return renderer;
            }
        }, items);
    }

    @Override
    public void registerBlockEntityRenderers() {
        //deco
        BlockEntityRenderers.register(NtmBlockEntityTypes.BOBBLEHEAD.get(), new RenderBobble());
        BlockEntityRenderers.register(NtmBlockEntityTypes.PLUSHIE.get(), new RenderPlushie());
        BlockEntityRenderers.register(NtmBlockEntityTypes.LOOT_DECO.get(), new RenderLootDeco());
        //bombs
        BlockEntityRenderers.register(NtmBlockEntityTypes.NUKE_GADGET.get(), new RenderNukeGadget());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NUKE_LITTLE_BOY.get(), new RenderNukeLittleBoy());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NUKE_FAT_MAN.get(), new RenderNukeFatMan());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NUKE_IVY_MIKE.get(), new RenderNukeIvyMike());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NUKE_TSAR_BOMBA.get(), new RenderNukeTsarBomba());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NUKE_PROTOTYPE.get(), new RenderNukePrototype());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NUKE_FLEIJA.get(), new RenderNukeFleija());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NUKE_SOLINUIM.get(), new RenderNukeSolinium());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NUKE_N2.get(), new RenderNukeN2());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NUKE_FSTBMB.get(), new RenderNukeFstbmb());
        BlockEntityRenderers.register(NtmBlockEntityTypes.CRASHED_BOMB.get(), new RenderCrashedBomb());
        //mines
        BlockEntityRenderers.register(NtmBlockEntityTypes.LANDMINE.get(), new RenderLandmine());
        BlockEntityRenderers.register(NtmBlockEntityTypes.CHARGE.get(), new RenderExplosiveCharge());
        BlockEntityRenderers.register(NtmBlockEntityTypes.SOYUZ_CAPSULE.get(), new RenderCapsule());
        //machines
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_SOLDERING_STATION.get(), new RenderSolderingStation());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_ARC_WELDER.get(), new RenderArcWelder());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_DIESEL.get(), new RenderDieselGenerator());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_COMPRESSOR.get(), new RenderCompressor());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_COMPRESSOR_COMPACT.get(), new RenderCompressorCompact());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_MIXER.get(), new RenderMixer());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_SOLAR_BOILER.get(), new RenderSolarBoiler());
        BlockEntityRenderers.register(NtmBlockEntityTypes.SOLAR_MIRROR.get(), new RenderSolarMirror());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FURNACE_IRON.get(), new RenderFurnaceIron());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FURNACE_STEEL.get(), new RenderFurnaceSteel());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_ROCK_MILL.get(), new RenderRockMill());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_STEAM_ENGINE.get(), new RenderSteamEngine());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_STIRLING.get(), new RenderStirling());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_DIFURNACE_EXTENSION.get(), new RenderDiFurnaceExtension());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_COMBUSTION_ENGINE.get(), new RenderCombustionEngine());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_TURBINEGAS.get(), new RenderTurbineGas());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_TURBOFAN.get(), new RenderTurbofan());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_CRYSTALLIZER.get(), new RenderCrystallizer());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_THRESHER.get(), new RenderThresher());
        BlockEntityRenderers.register(NtmBlockEntityTypes.SAWMILL.get(), new RenderSawmill());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_RTG.get(), new RenderRTG());
        BlockEntityRenderers.register(NtmBlockEntityTypes.CONDENSER_POWERED.get(), new RenderCondenser());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NETWORK_CONNECTOR.get(), new RenderConnector());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NETWORK_CONNECTOR_SUPER.get(), new RenderConnectorSuper());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NETWORK_PYLON.get(), new RenderPylon());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NETWORK_PYLON_MEDIUM.get(), new RenderPylonMedium());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NETWORK_PYLON_LARGE.get(), new RenderPylonLarge());
        BlockEntityRenderers.register(NtmBlockEntityTypes.NETWORK_SUBSTATION.get(), new RenderSubstation());
        BlockEntityRenderers.register(NtmBlockEntityTypes.HEAT_BOILER.get(), new RenderHeatBoiler());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_INDUSTRIAL_BOILER.get(), new RenderIndustrialBoiler());
        BlockEntityRenderers.register(NtmBlockEntityTypes.HEATER_FIREBOX.get(), new RenderHeaterFirebox());
        BlockEntityRenderers.register(NtmBlockEntityTypes.HEATER_OVEN.get(), new RenderHeatingOven());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_ASHPIT.get(), new RenderAshpit());
        BlockEntityRenderers.register(NtmBlockEntityTypes.CHIMNEY_BRICK.get(), new RenderChimneyBrick());
        BlockEntityRenderers.register(NtmBlockEntityTypes.CHIMNEY_INDUSTRIAL.get(), new RenderChimneyIndustrial());
        BlockEntityRenderers.register(NtmBlockEntityTypes.CRANE_CONSOLE.get(), new RenderCraneConsole());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_AUTOLOADER.get(), new RenderRBMKAutoloader());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_ROD.get(), new RenderRBMKFuelChannel());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_ROD_REASIM.get(), new RenderRBMKFuelChannel());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_CONTROL.get(), new RenderRBMKControlRod());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_CONTROL_AUTO.get(), new RenderRBMKControlRod());
        BlockEntityRenderers.register(NtmBlockEntityTypes.HEATER_OILBURNER.get(), new RenderOilburner());
        BlockEntityRenderers.register(NtmBlockEntityTypes.HEATER_ELECTRIC.get(), new RenderHeaterElectric());
        BlockEntityRenderers.register(NtmBlockEntityTypes.HEATER_HEATEX.get(), new RenderHeaterHeatex());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_OIL_WELL.get(), new RenderDerrick());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_PUMPJACK.get(), new RenderPumpjack());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_FRACKING_TOWER.get(), new RenderFrackingTower());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_REFINERY.get(), new RenderRefinery());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_FRACTION_TOWER.get(), new RenderFractionTower());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FRACTION_SPACER.get(), new RenderFractionSpacer());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_CATALYTIC_REFORMER.get(), new RenderCatalyticReformer());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_HYDROTREATER.get(), new RenderHydrotreater());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_DEUTERIUM_TOWER.get(), new RenderDeuteriumTower());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_REFUELER.get(), new RenderRefueler());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_VACUUM_DISTILL.get(), new RenderVacuumDistill());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_SOLIDIFIER.get(), new RenderSolidifier());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_PYRO_OVEN.get(), new RenderPyroOven());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_LIQUEFACTOR.get(), new RenderLiquefactor());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_GAS_FLARE.get(), new RenderGasFlare());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_COKER.get(), new RenderCoker());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_CATALYTIC_CRACKER.get(), new RenderCatalyticCracker());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FURNACE_COMBINATION.get(), new RenderFurnaceCombination());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_BLAST_FURNACE.get(), new RenderBlastFurnace());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_WOOD_BURNER.get(), new RenderWoodBurner());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_CENTRIFUGE.get(), new RenderCentrifuge());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_GAUGE.get(), new RenderRBMKGauge());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_INDICATOR.get(), new RenderRBMKIndicator());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_NUMITRON.get(), new RenderRBMKNumitron());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_LEVER.get(), new RenderRBMKLever());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_KEYPAD.get(), new RenderRBMKKeyPad());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_GRAPH.get(), new RenderRBMKGraph());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_DISPLAY.get(), new RenderRBMKDisplay());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_TERMINAL.get(), new RenderRBMKTerminal());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RBMK_CONSOLE.get(), new RenderRBMKConsole());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_PUREX.get(), new RenderPUREX());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_RADIOLYSIS.get(), new RenderRadiolysis());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_CHEMICAL_PLANT.get(), new RenderChemicalPlant());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_ARC_FURNACE.get(), new RenderArcFurnace());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_CRUCIBLE.get(), new RenderCrucible());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_ROTARY_FURNACE.get(), new RenderRotaryFurnace());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FOUNDRY_CHANNEL.get(), new RenderFoundryChannel());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FOUNDRY_MOLD.get(), new RenderFoundry());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FOUNDRY_BASIN.get(), new RenderFoundry());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FOUNDRY_TANK.get(), new RenderFoundryTank());
        BlockEntityRenderers.register(NtmBlockEntityTypes.ASSEMBLY_MACHINE.get(), new RenderAssemblyMachine());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_PRECASS.get(), new RenderPrecAss());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_ORE_SLOPPER.get(), new RenderOreSlopper());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_EXCAVATOR.get(), new RenderExcavator());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FLUID_TANK.get(), new RenderFluidTank());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_BIGASSTANK.get(), new RenderBigAssTank());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_CHUNGUS.get(), new RenderChungus());
        BlockEntityRenderers.register(NtmBlockEntityTypes.PRESS.get(), new RenderPress());
        BlockEntityRenderers.register(NtmBlockEntityTypes.GEIGER_COUNTER.get(), new RenderGeigerBlock());
        BlockEntityRenderers.register(NtmBlockEntityTypes.BATTERY_SOCKET.get(), new RenderBatterySocket());
        BlockEntityRenderers.register(NtmBlockEntityTypes.BATTERY_REDD.get(), new RenderBatteryREDD());
        //ZIRNOX
        BlockEntityRenderers.register(NtmBlockEntityTypes.REACTOR_ZIRNOX.get(), new RenderZirnox());
        BlockEntityRenderers.register(NtmBlockEntityTypes.ZIRNOX_DESTROYED.get(), new RenderZirnoxDestroyed());
        BlockEntityRenderers.register(NtmBlockEntityTypes.WATZ.get(), new RenderWatz());
        BlockEntityRenderers.register(NtmBlockEntityTypes.WATZ_PUMP.get(), new RenderWatzPump());
        BlockEntityRenderers.register(NtmBlockEntityTypes.PILE_LOADER.get(), new RenderPileLoader());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FILE_CABINET.get(), new RenderFileCabinet());
        BlockEntityRenderers.register(NtmBlockEntityTypes.PILE_VENT.get(), new RenderPileVent());
        BlockEntityRenderers.register(NtmBlockEntityTypes.PILE_CONTROL.get(), new RenderPileControl());
        BlockEntityRenderers.register(NtmBlockEntityTypes.ICF.get(), new RenderICF());
        BlockEntityRenderers.register(NtmBlockEntityTypes.ICF_CONTROLLER.get(), new RenderICFController());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FUSION_TORUS.get(), new RenderFusionTorus());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FUSION_KLYSTRON.get(), new RenderFusionKlystron());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FUSION_COLLECTOR.get(), new RenderFusionCollector());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FUSION_BREEDER.get(), new RenderFusionBreeder());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FUSION_COUPLER.get(), new RenderFusionCoupler());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FUSION_BOILER.get(), new RenderFusionBoiler());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FUSION_MHDT.get(), new RenderFusionMHDT());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FUSION_PLASMA_FORGE.get(), new RenderFusionPlasmaForge());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_REACTOR_BREEDING.get(), new RenderBreeder());
        BlockEntityRenderers.register(NtmBlockEntityTypes.TURRET_SENTRY.get(), new RenderTurretSentry());
        BlockEntityRenderers.register(NtmBlockEntityTypes.TURRET_SENTRY_DAMAGED.get(), new RenderTurretSentry());
        BlockEntityRenderers.register(NtmBlockEntityTypes.TURRET_JEREMY.get(), new RenderTurretJeremy());
        BlockEntityRenderers.register(NtmBlockEntityTypes.TURRET_HOWARD.get(), new RenderTurretHoward());
        BlockEntityRenderers.register(NtmBlockEntityTypes.TURRET_HOWARD_DAMAGED.get(), new RenderTurretHoward());
        BlockEntityRenderers.register(NtmBlockEntityTypes.TURRET_CHEKHOV.get(), new RenderTurretChekhov());
        BlockEntityRenderers.register(NtmBlockEntityTypes.TURRET_FRIENDLY.get(), new RenderTurretChekhov());
        BlockEntityRenderers.register(NtmBlockEntityTypes.REACTOR_RESEARCH.get(), new RenderSmallReactor());
        //missile blocks
        BlockEntityRenderers.register(NtmBlockEntityTypes.LAUNCH_PAD.get(), new RenderLaunchPad());
        BlockEntityRenderers.register(NtmBlockEntityTypes.LAUNCH_PAD_LARGE.get(), new RenderLaunchPadLarge());
        BlockEntityRenderers.register(NtmBlockEntityTypes.SOYUZ_LAUNCHER.get(), new RenderSoyuzLauncher());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_RADAR.get(), new RenderRadar());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RADAR_SCREEN.get(), new RenderRadarScreen());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_RADAR_LARGE.get(), new RenderRadar());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_SAT_LINK.get(), new RenderSatLink());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_TAPE_DRIVE.get(), new RenderTapeDrive());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_SUPER_COMPUTER.get(), new RenderSuperComputer());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_AMMO_PRESS.get(), new RenderAmmoPress());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_AUTOSAW.get(), new RenderAutosaw());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_GAS_CENT.get(), new RenderGasCent());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_ANNIHILATOR.get(), new RenderAnnihilator());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_EXPOSURE_CHAMBER.get(), new RenderExposureChamber());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_MINING_LASER.get(), new RenderLaserMiner());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_RAD_GEN.get(), new RenderRadGen());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_CYCLOTRON.get(), new RenderCyclotron());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_PA_SOURCE.get(), new RenderPASource());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_PA_BEAMLINE.get(), new RenderPABeamline());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_PA_RFC.get(), new RenderPARFC());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_PA_QUADRUPOLE.get(), new RenderPAQuadrupole());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_PA_DIPOLE.get(), new RenderPADipole());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_PA_DETECTOR.get(), new RenderPADetector());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_TOWER_SMALL.get(), new RenderTowerSmall());
        BlockEntityRenderers.register(NtmBlockEntityTypes.FLOODLIGHT.get(), new RenderFloodlight());
        BlockEntityRenderers.register(NtmBlockEntityTypes.POLE_SATELLITE_RECEIVER.get(), new RenderPoleSatelliteReceiver());
        BlockEntityRenderers.register(NtmBlockEntityTypes.CHARGER.get(), new RenderCharger());
        BlockEntityRenderers.register(NtmBlockEntityTypes.RADIO_TELEX.get(), new RenderTelex());
        BlockEntityRenderers.register(NtmBlockEntityTypes.TESLA.get(), new RenderTesla());
        BlockEntityRenderers.register(NtmBlockEntityTypes.PEDESTAL.get(), new RenderPedestal());
        BlockEntityRenderers.register(NtmBlockEntityTypes.SKELETON_HOLDER.get(), new RenderSkeletonHolder());
        BlockEntityRenderers.register(NtmBlockEntityTypes.LAMP_DEMON.get(), new RenderDemonLamp());
        BlockEntityRenderers.register(NtmBlockEntityTypes.MACHINE_TOWER_LARGE.get(), new RenderTowerLarge());
    }

    @Override
    public void registerEntityRenderers() {
        //projectiles
        EntityRenderers.register(NtmEntityTypes.BULLET_MK4.get(), RenderBulletMK4::new);
        EntityRenderers.register(NtmEntityTypes.BULLET_BEAM.get(), RenderBeam::new);
        EntityRenderers.register(NtmEntityTypes.COIN.get(), RenderCoin::new);
        EntityRenderers.register(NtmEntityTypes.CHEMICAL.get(), RenderChemical::new);
        EntityRenderers.register(NtmEntityTypes.BOMBLET_ZETA.get(), RenderBombletZeta::new);
        EntityRenderers.register(NtmEntityTypes.METEOR.get(), RenderMeteor::new);
        EntityRenderers.register(NtmEntityTypes.BOMBER.get(), RenderBomber::new);
        EntityRenderers.register(NtmEntityTypes.TOM.get(), RenderTom::new);
        EntityRenderers.register(NtmEntityTypes.RUBBLE.get(), RenderRubble::new);
        EntityRenderers.register(NtmEntityTypes.DIGAMMA_SPEAR.get(), RenderDigammaSpear::new);
        EntityRenderers.register(NtmEntityTypes.SOYUZ_CAPSULE.get(), RenderSoyuzCapsule::new);
        EntityRenderers.register(NtmEntityTypes.SHRAPNEL.get(), RenderShrapnel::new);
        EntityRenderers.register(NtmEntityTypes.BOXCAR.get(), RenderBoxcar::new);
        EntityRenderers.register(NtmEntityTypes.DUCHESS_GAMBIT.get(), RenderDuchessGambit::new);
        EntityRenderers.register(NtmEntityTypes.TORPEDO.get(), RenderTorpedo::new);
        EntityRenderers.register(NtmEntityTypes.C130.get(), RenderC130::new);
        EntityRenderers.register(NtmEntityTypes.PARACHUTE_CRATE.get(), RenderParachuteCrate::new);
        /* Die Granate zeigt ihr Gegenstandsbild -- siehe GrenadeUniversal.getItem(). */
        EntityRenderers.register(NtmEntityTypes.GRENADE_UNIVERSAL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(NtmEntityTypes.DYNAMITE.get(), ThrownItemRenderer::new);
        EntityRenderers.register(NtmEntityTypes.SAWBLADE.get(), RenderSawblade::new);
        EntityRenderers.register(NtmEntityTypes.COG.get(), RenderCog::new);
        EntityRenderers.register(NtmEntityTypes.RBMK_DEBRIS.get(), RenderRBMKDebris::new);
        EntityRenderers.register(NtmEntityTypes.ZIRNOX_DEBRIS.get(), RenderZirnoxDebris::new);
        EntityRenderers.register(NtmEntityTypes.ROCKET.get(), ThrownItemRenderer::new);
        EntityRenderers.register(NtmEntityTypes.EMP.get(), EmptyEntityRenderer::new);
        EntityRenderers.register(NtmEntityTypes.NUKE_MK5.get(), EmptyEntityRenderer::new);
        EntityRenderers.register(NtmEntityTypes.NUKE_MK3.get(), EmptyEntityRenderer::new);
        EntityRenderers.register(NtmEntityTypes.NUKE_BALEFIRE.get(), EmptyEntityRenderer::new);
        //missiles
        EntityRenderers.register(NtmEntityTypes.MISSILE_MICRO.get(), RenderMissileMicro::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_SCHRABIDIUM.get(), RenderMissileMicro::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_BHOLE.get(), RenderMissileMicro::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_TAINT.get(), RenderMissileMicro::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_EMP.get(), RenderMissileMicro::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_GENERIC.get(), RenderMissileGeneric::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_INCENDIARY.get(), RenderMissileGeneric::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_CLUSTER.get(), RenderMissileGeneric::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_BUSTER.get(), RenderMissileGeneric::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_DECOY.get(), RenderMissileGeneric::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_STRONG.get(), RenderMissileStrong::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_INCENDIARY_STRONG.get(), RenderMissileStrong::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_CLUSTER_STRONG.get(), RenderMissileStrong::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_BUSTER_STRONG.get(), RenderMissileStrong::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_EMP_STRONG.get(), RenderMissileStrong::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_STEALTH.get(), RenderMissileStealth::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_ANTI_BALLISTIC.get(), RenderMissileAntiBallistic::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_BURST.get(), RenderMissileHuge::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_INFERNO.get(), RenderMissileHuge::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_RAIN.get(), RenderMissileHuge::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_DRILL.get(), RenderMissileHuge::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_SHUTTLE.get(), RenderMissileShuttle::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_NUCLEAR.get(), RenderMissileNuclear::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_NUCLEAR_CLUSTER.get(), RenderMissileNuclear::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_VOLCANO.get(), RenderMissileNuclear::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_DOOMSDAY.get(), RenderMissileNuclear::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_DOOMSDAY_RUSTED.get(), RenderMissileNuclear::new);
        EntityRenderers.register(NtmEntityTypes.SOYUZ_MISSILE.get(), RenderSoyuz::new);
        EntityRenderers.register(NtmEntityTypes.SATELLITE_POD.get(), RenderSatellitePod::new);
        EntityRenderers.register(NtmEntityTypes.MISSILE_CUSTOM.get(), RenderMissileCustom::new);
        //effects
        EntityRenderers.register(NtmEntityTypes.FALLOUT_RAIN.get(), RenderFallout::new);
        EntityRenderers.register(NtmEntityTypes.MIST.get(), RenderMist::new);
        EntityRenderers.register(NtmEntityTypes.FIRE_LINGERING.get(), RenderFireLingering::new);
        EntityRenderers.register(NtmEntityTypes.BLACK_HOLE.get(), RenderBlackHole::new);
        EntityRenderers.register(NtmEntityTypes.VORTEX.get(), RenderBlackHole::new);
        EntityRenderers.register(NtmEntityTypes.RAGING_VORTEX.get(), RenderBlackHole::new);
        EntityRenderers.register(NtmEntityTypes.DIGAMMA_QUASAR.get(), RenderQuasar::new);
        EntityRenderers.register(NtmEntityTypes.DEATH_BLAST.get(), RenderDeathBlast::new);
        EntityRenderers.register(NtmEntityTypes.ORBITAL_LASER.get(), RenderOrbitalLaser::new);
        //items
        EntityRenderers.register(NtmEntityTypes.TNT_PRIMED_BASE.get(), RenderTNTPrimedBase::new);
        EntityRenderers.register(NtmEntityTypes.FALLING_BLOCK.get(), RenderFallingBlockEntityNT::new);
        EntityRenderers.register(NtmEntityTypes.MOVING_ITEM.get(), RenderMovingItem::new);
        EntityRenderers.register(NtmEntityTypes.MOVING_PACKAGE.get(), RenderMovingPackage::new);
        //mobs
        EntityRenderers.register(NtmEntityTypes.CREEPER_NUCLEAR.get(), CreeperNuclearRenderer::new);
        EntityRenderers.register(NtmEntityTypes.DUCK.get(), DuckRenderer::new);
        EntityRenderers.register(NtmEntityTypes.UNDEAD_SOLDIER.get(), UndeadSoldierRenderer::new);
        EntityRenderers.register(NtmEntityTypes.CYBER_CRAB.get(), CyberCrabRenderer::new);
        EntityRenderers.register(NtmEntityTypes.TESLA_CRAB.get(), TeslaCrabRenderer::new);
        EntityRenderers.register(NtmEntityTypes.TAINT_CRAB.get(), TaintCrabRenderer::new);
        EntityRenderers.register(NtmEntityTypes.CREEPER_TAINTED.get(), CreeperTaintedRenderer::new);
        EntityRenderers.register(NtmEntityTypes.MASKMAN.get(), MaskManRenderer::new);
        EntityRenderers.register(NtmEntityTypes.CREEPER_GOLD.get(), CreeperGoldRenderer::new);
        EntityRenderers.register(NtmEntityTypes.CREEPER_VOLATILE.get(), CreeperVolatileRenderer::new);
        EntityRenderers.register(NtmEntityTypes.CREEPER_PHOSGENE.get(), CreeperPhosgeneRenderer::new);
        /* Der Tau-Bolzen hat kein Modell: das Original zeichnet ihn ueber die alte
         * Geschossklasse, deren OBJ-Datei der Port nicht hat. Zu sehen ist seine Staubspur,
         * und die zeichnet die Entitaet selbst. */
        EntityRenderers.register(NtmEntityTypes.TAU_SHOT.get(), TauShotRenderer::new);
    }

    private static final HashMap<Integer, Long> vanished = new HashMap<>();
    @Override public void vanish(int entityId) { vanished.put(entityId, System.currentTimeMillis() + 2000); }
    @Override public void vanish(int entityId, int duration) { vanished.put(entityId, System.currentTimeMillis() + duration); }

    @Override
    public boolean isVanished(Entity e) {
        if(!vanished.containsKey(e.getId())) return false;
        return vanished.get(e.getId()) > System.currentTimeMillis();
    }

    public void playLocalSound(Vec3 vec, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        this.playLocalSound(vec.x, vec.y, vec.z, soundEvent, source, volume, pitch);
    }

    public void playLocalSound(double x, double y, double z, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
        Minecraft minecraft = Minecraft.getInstance();

        double distSqr = minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(x, y, z);
        SimpleSoundInstance instance = new SimpleSoundInstance(soundEvent, source, volume, pitch, RandomSource.create(minecraft.level.random.nextLong()), x, y, z);
        if(distSqr > 100.0) {
            double dist = Math.sqrt(distSqr) / 40.0;
            minecraft.getSoundManager().playDelayed(instance, (int)(dist * 20.0));
        } else {
            minecraft.getSoundManager().play(instance);
        }
    }

    @Override
    public void openScreen(Player player, BlockPos pos) {
        if(player != this.me()) return;

        Minecraft minecraft = Minecraft.getInstance();

        Block block = player.level.getBlockState(pos).getBlock();
        if(block instanceof IScreenProvider igp) {
            Screen screen = (Screen) igp.provideScreen(player, pos);
            if(screen != null) minecraft.setScreen(screen);
        }

        List<ItemStack> stacks = InventoryUtil.getItemsFromBothHands(player);
        for(ItemStack stack : stacks) {
            if(stack.getItem() instanceof IScreenProvider igp) {
                Screen screen = (Screen) igp.provideScreen(player, pos);
                if(screen != null) minecraft.setScreen(screen);
                break;
            }
        }
    }

    @Override
    public void openNoContainerScreen(ResourceLocation screen, BlockPos pos) {

        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.level == null) return;

        Screen gui = NoContainerScreens.create(screen, minecraft.level.getBlockEntity(pos));
        if(gui != null) minecraft.setScreen(gui);
    }

    @Override
    public void displayTooltip(Component message, int time, int id) {

        InfoEntry entry = new InfoEntry(message, time);
        if(id != 0) {
            RenderInfoSystem.push(entry, id);
        } else {
            RenderInfoSystem.push(entry);
        }
    }

    @Override
    public Player me() {
        return Minecraft.getInstance().player;
    }
}
