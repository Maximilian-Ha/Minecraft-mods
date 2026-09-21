package com.hbm.main;

import com.hbm.config.MachineDynConfig;
import com.hbm.blockentity.bomb.LaunchPadBaseBlockEntity;
import com.hbm.blockentity.machine.MachineRadarBlockEntity;
import com.hbm.blockentity.machine.MachineRadGenBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.commands.ChunkRadCommand;
import com.hbm.commands.LivingPropsCommand;
import com.hbm.commands.PollutionCommand;
import com.hbm.commands.RbmkDialCommand;
import com.hbm.commands.SatellitesCommand;
import com.hbm.config.FalloutConfigJSON;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.items.armor.ModReviveItem;
import com.hbm.entity.mob.CreeperNuclear;
import com.hbm.entity.mob.CreeperGold;
import com.hbm.entity.mob.CreeperPhosgene;
import com.hbm.entity.mob.CreeperTainted;
import com.hbm.entity.mob.Ghost;
import com.hbm.entity.mob.MaskMan;
import com.hbm.entity.mob.RadBeast;
import com.hbm.entity.mob.Ufo;
import com.hbm.entity.mob.botprime.BotPrimeBase;
import com.hbm.registry.NtmCriteria;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.entity.mob.CyberCrab;
import com.hbm.entity.mob.TaintCrab;
import com.hbm.entity.mob.TeslaCrab;
import com.hbm.entity.mob.UndeadSoldier;
import com.hbm.entity.mob.Duck;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ItemModIndestructible;
import com.hbm.handler.EntityEffectHandler;
import com.hbm.handler.HTTPHandler;
import com.hbm.handler.HazmatRegistry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.GasCentrifugeRecipes;
import com.hbm.inventory.recipes.MagicRecipes;
import com.hbm.inventory.recipes.PedestalRecipes;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.machine.MoldItem;
import com.hbm.blocks.generic.ToolConversionBlock;
import com.hbm.items.machine.ICFPelletItem;
import com.hbm.blockentity.turret.TurretChekhovBlockEntity;
import com.hbm.blockentity.turret.TurretFriendlyBlockEntity;
import com.hbm.blockentity.turret.TurretHowardBlockEntity;
import com.hbm.blockentity.turret.TurretJeremyBlockEntity;
import com.hbm.blockentity.turret.TurretSentryBlockEntity;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.inventory.screens.*;
import com.hbm.itempool.ItemPoolsC130;
import com.hbm.itempool.ItemPoolsMeteor;
import com.hbm.itempool.ItemPoolsPile;
import com.hbm.itempool.ItemPoolsRedRoom;
import com.hbm.itempool.ItemPoolsSatellite;
import com.hbm.items.IEquipReceiver;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.saveddata.satellite.XSatelliteRegistry;
import com.hbm.items.armor.ArmorFSBItem;
import com.hbm.items.armor.IDamageHandlerItem;
import com.hbm.items.armor.IAttackHandlerItem;
import com.hbm.items.NtmItems;
import com.hbm.util.ArmorUtil;
import com.hbm.util.DamageResistanceHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import java.util.List;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import com.hbm.extprop.HbmPlayerAttachments;
import com.hbm.lib.ModAttachments;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.minecraft.world.entity.monster.Creeper;
import com.hbm.entity.mob.CreeperDefuser;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = NuclearTechMod.MODID)
public class CommonEvents {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {

        event.enqueueWork(() -> {
            // to make sure that foreign registered fluids are accounted for,
            // even when the reload listener is registered too late due to load order
            // IMPORTANT: fluids have to load before recipes. weird shit happens if not.
            Fluids.reloadFluids();
            FluidContainerRegistry.register();

            //the good stuff
            MachineDynConfig.initialize();

            MoldItem.registerMolds();
            ToolConversionBlock.registerRecipes();
            ICFPelletItem.init();
            XWeaponModManager.init();
            TurretSentryBlockEntity.initAmmo();
            TurretJeremyBlockEntity.initAmmo();
            TurretHowardBlockEntity.initAmmo();
            TurretChekhovBlockEntity.initAmmo();
            TurretFriendlyBlockEntity.initAmmo();

            SerializableRecipe.registerAllHandlers();
            SerializableRecipe.initialize();

            /* Die Gaszentrifuge ist kein SerializableRecipe -- ihre Stufen stehen fest im
             * Quelltext, wie im Original. Nur die Zuordnung Fluid -> Kette wird hier gebaut,
             * und die braucht Fluids.init() davor. */
            GasCentrifugeRecipes.register();

            /* Die Rezepte des Buchs stehen wie im Original fest im Quelltext; sie haben keine
             * Form, sondern nur eine Reihenfolge, und passen deshalb in kein Rezeptdatenblatt. */
            MagicRecipes.register();

            /* Die Sockelrezepte, Runde 231. Auch sie stehen fest im Quelltext: sie haben
             * neun Plaetze im Abstand drei und eine Bedingung an Mondphase oder Ruf -- das
             * passt in kein Rezeptdatenblatt von 1.21. */
            PedestalRecipes.register();

            HTTPHandler.loadStats();
            FalloutConfigJSON.initialize();
            DamageResistanceHandler.init();
            HazardRegistry.registerItems();
            HazmatRegistry.registerHazmats();
            ArmorUtil.register();
            XSatelliteRegistry.register();
            ItemPoolsSatellite.init();
            ItemPoolsC130.init();
            ItemPoolsRedRoom.init();
            ItemPoolsPile.init();
            ItemPoolsMeteor.init();
            LaunchPadBaseBlockEntity.registerLaunchables();

            /* Die Brennstofftafel des Radiothermalgenerators steht fest im Quelltext, wie im
             * Original -- sie braucht nur die fertig registrierten Gegenstaende. */
            MachineRadGenBlockEntity.registerFuels();

            MachineRadarBlockEntity.registerEntityClasses();
            MachineRadarBlockEntity.registerConverters();
        });
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack itemInHand = event.getEntity().getItemInHand(InteractionHand.MAIN_HAND);
        if(itemInHand.getItem() instanceof GunBaseNTItem) event.setCanceled(true);
    }

    /**
     * DAS WILD P, Runde 232. Wer mit dem Aufsatz an der Beinschiene stirbt, steht wieder auf:
     * volle Gesundheit, drei Sekunden Resistenz, und der Aufsatz verliert ein Leben. Beim
     * letzten wird er abgenommen, statt kaputtzugehen.
     *
     * DAS ORIGINAL SUCHT IHN IN ALLEN VIER TEILEN (ModEventHandler.onEntityDeathFirst),
     * obwohl der Aufsatz nur auf die Beinschiene passt. Der Port tut dasselbe: haengt ihn
     * jemand mit einem Befehl woandershin, soll er trotzdem wirken.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {

        LivingEntity gestorbener = event.getEntity();
        if(gestorbener.level().isClientSide) return;

        /*
         * DER VERSTECKTE KATALOG. Ein verseuchter Creeper, erschlagen von einem fallenden
         * Gueterwagen -- eine Kombination, die niemand zufaellig hinbekommt, und genau darum
         * geht es dem Original (ModEventHandler.java:309). Der Umkreis von fuenfzig Bloecken
         * ist seiner.
         */
        if(gestorbener instanceof CreeperTainted && event.getSource().is(NtmDamageTypes.BOXCAR)) {
            NtmCriteria.markeImUmkreis(gestorbener, 50, "hidden");
        }

        for(EquipmentSlot platz : ArmorModHandler.ARMOR_SLOTS) {
            ItemStack teil = gestorbener.getItemBySlot(platz);
            if(teil.isEmpty() || !ArmorModHandler.hasMods(teil)) continue;

            ItemStack aufsatz = ArmorModHandler.pryMods(gestorbener.level(), teil)[ArmorModHandler.EXTRA];
            if(aufsatz.isEmpty() || !(aufsatz.getItem() instanceof ModReviveItem)) continue;

            aufsatz.setDamageValue(aufsatz.getDamageValue() + 1);

            if(aufsatz.getDamageValue() >= aufsatz.getMaxDamage()) {
                ArmorModHandler.removeMod(teil, ArmorModHandler.EXTRA);
            } else {
                ArmorModHandler.applyMod(gestorbener.level(), teil, aufsatz);
            }

            gestorbener.setHealth(gestorbener.getMaxHealth());
            gestorbener.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 99));
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(NtmEntityTypes.DUCK.get(), Duck.createAttributes().build());
        /* Die Quackos erbt die Attribute der Ente -- das Original gibt ihr keine eigenen. */
        event.put(NtmEntityTypes.QUACKOS.get(), Duck.createAttributes().build());
        event.put(NtmEntityTypes.GHOST.get(), Ghost.createAttributes().build());
        event.put(NtmEntityTypes.CREEPER_NUCLEAR.get(), CreeperNuclear.createAttributes().build());
        event.put(NtmEntityTypes.UNDEAD_SOLDIER.get(), UndeadSoldier.createAttributes().build());
        event.put(NtmEntityTypes.CYBER_CRAB.get(), CyberCrab.createAttributes().build());
        event.put(NtmEntityTypes.TESLA_CRAB.get(), TeslaCrab.createAttributes().build());
        event.put(NtmEntityTypes.TAINT_CRAB.get(), TaintCrab.createAttributes().build());
        event.put(NtmEntityTypes.CREEPER_TAINTED.get(), CreeperTainted.createAttributes().build());
        event.put(NtmEntityTypes.MASKMAN.get(), MaskMan.createAttributes().build());
        event.put(NtmEntityTypes.RAD_BEAST.get(), RadBeast.createAttributes().build());
        event.put(NtmEntityTypes.BOT_PRIME_HEAD.get(), BotPrimeBase.createAttributes().build());
        event.put(NtmEntityTypes.BOT_PRIME_BODY.get(), BotPrimeBase.createAttributes().build());
        event.put(NtmEntityTypes.UFO.get(), Ufo.createAttributes().build());
        event.put(NtmEntityTypes.CREEPER_GOLD.get(), CreeperGold.createAttributes().build());
        event.put(NtmEntityTypes.CREEPER_VOLATILE.get(), CreeperGold.createAttributes().build());
        event.put(NtmEntityTypes.CREEPER_PHOSGENE.get(), CreeperPhosgene.createAttributes().build());
    }

    /**
     * WO die drei Creeper erscheinen duerfen, Runde 240. Das Gewicht und das Biom stehen im
     * Biom-Aenderer; hier steht, worauf sie erscheinen: auf festem Boden, im Dunkeln, nach
     * denselben Regeln wie jedes andere Monster (Monster.checkMonsterSpawnRules).
     *
     * OHNE DIESEN EINTRAG WUERDEN SIE UEBERALL ERSCHEINEN -- auch in der Luft und im
     * Hellen: ohne Platzierungsregel prueft 1.21 gar nichts.
     */
    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {

        for(EntityType<? extends Monster> art : List.of(
                NtmEntityTypes.CREEPER_GOLD.get(),
                NtmEntityTypes.CREEPER_VOLATILE.get(),
                NtmEntityTypes.CREEPER_PHOSGENE.get())) {

            event.register(art, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player player) {
            HazardSystem.updatePlayerInventory(player);
        }
        if (entity instanceof ItemEntity itemEntity) {
            HazardSystem.updateDroppedItem(itemEntity);
        }
        if (entity instanceof LivingEntity livingEntity) {
            HazardSystem.updateLivingInventory(livingEntity);
            EntityEffectHandler.tick(livingEntity);
            ArmorModHandler.updateMods(livingEntity);
        }

        /* Ein entschaerfter Creeper wird hier niedergehalten -- VOR Creeper.tick(), sonst
         * ist der Zaehler schon gestiegen. Im Original steht an dieser Stelle derselbe
         * Griff, nur um nach dem Laden der Welt das entfernte SwellGoal wieder zu
         * entfernen; im Port ist er der Mechanismus selbst. CreeperDefuser erklaert, warum. */
        if (entity instanceof Creeper creeper) {
            CreeperDefuser.haltNieder(creeper);
        }
    }

    /*
     * Die Obsidianauskleidung: ein so ausgekleidetes Ruestungsteil, das am Boden liegt,
     * verbrennt und zerfaellt nicht mehr. Geprueft wird einmal beim Erscheinen des
     * Gegenstands, nicht in jedem Tick.
     */
    @SubscribeEvent
    public static void onItemDropped(EntityJoinLevelEvent event) {

        if(!(event.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if(!ArmorModHandler.hasMods(stack)) return;

        if(ArmorModHandler.pryMod(event.getLevel(), stack, ArmorModHandler.CLADDING).getItem() instanceof ItemModIndestructible) {
            itemEntity.setInvulnerable(true);
        }
    }

    /*
     * Die Ruestungsmodule duerfen den Schaden verrechnen, sobald Ruestung und Verzauberungen
     * ihn schon gemindert haben -- das ist die Stelle, an der im Original LivingHurtEvent lag.
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {

        /* DAS SCHILD ZUERST, Runde 244. Im Original steht es an derselben Stelle
         * (ModEventHandler Z. 700) -- im selben LivingHurtEvent, aber VOR den
         * Ruestungsaufsaetzen bei Z. 736. Was das Schild traegt, sehen die Aufsaetze
         * gar nicht erst. */
        schildAbzug(event);

        ArmorModHandler.handleDamage(event);

        /* Danach darf der Anzug selbst ran -- und zwar nur die Brustplatte, wie im Original
         * (ModEventHandler Z. 733). */
        if(!(event.getEntity() instanceof Player player)) return;

        if(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorFSBItem brustplatte) {
            brustplatte.handleHurt(event);
        }

        /* Danach jedes einzelne Teil, das sich dafuer anmeldet -- an allen vier Plaetzen,
         * nicht nur an der Brust (ModEventHandler Z. 736). */
        for(EquipmentSlot platz : ArmorModHandler.ARMOR_SLOTS) {
            ItemStack teil = player.getItemBySlot(platz);
            if(teil.getItem() instanceof IDamageHandlerItem handler) handler.handleDamage(event, teil);
        }
    }

    /**
     * Der Schildabzug, Runde 244. Was das Schild traegt, nimmt der Spieler nicht -- und der
     * Zeitpunkt des letzten Treffers wird festgehalten, weil die Regeneration in
     * EntityEffectHandler sechzig Takte Ruhe verlangt.
     *
     * DIE MARKE WIRD AUCH GESETZT, WENN DAS SCHILD LEER IST. Das Original macht es ebenso:
     * lastDamage steht ausserhalb der Abfrage auf shield > 0. Sonst koennte ein Spieler mit
     * leerem Schild unter Dauerbeschuss weiter aufladen.
     */
    private static void schildAbzug(LivingDamageEvent.Pre event) {

        if(!(event.getEntity() instanceof Player player)) return;

        HbmPlayerAttachments props = HbmPlayerAttachments.getData(player);

        if(props.shield > 0) {
            float traegt = Math.min(props.shield, event.getNewDamage());
            props.shield -= traegt;
            event.setNewDamage(event.getNewDamage() - traegt);
        }

        props.lastDamage = player.tickCount;
        player.setData(ModAttachments.PLAYER_ATTACHMENT.get(), props);
    }

    /**
     * Eine Stufe frueher als oben: hier laesst sich der Angriff noch ganz absagen.
     *
     * DER EUPHEMIUM-SATZ STEHT HIER UND NICHT IN SEINER KLASSE. Im Original ist das ebenso
     * (ModEventHandler Z. 674): die Abfrage haengt am Ereignis, nicht am Gegenstand, weil sie
     * den ganzen Satz auf einmal prueft. Runde 215 hat den Satz portiert, aber diese Wirkung
     * uebersehen -- sie kommt hier nach.
     */
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {

        if(!(event.getEntity() instanceof Player player)) return;

        if(ArmorUtil.checkArmor(player, NtmItems.EUPHEMIUM_HELMET.get(), NtmItems.EUPHEMIUM_PLATE.get(),
                NtmItems.EUPHEMIUM_LEGS.get(), NtmItems.EUPHEMIUM_BOOTS.get())) {

            player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS,
                    0.5F, 1.0F + player.getRandom().nextFloat() * 0.5F);
            event.setCanceled(true);
            return;
        }

        if(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorFSBItem brustplatte) {
            brustplatte.handleAttack(event);
        }

        /* Und hier ebenso an allen vier Plaetzen (ModEventHandler Z. 682). */
        for(EquipmentSlot platz : ArmorModHandler.ARMOR_SLOTS) {
            ItemStack teil = player.getItemBySlot(platz);
            if(teil.getItem() instanceof IAttackHandlerItem handler) handler.handleAttack(event, teil);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if(event.getSlot() != EquipmentSlot.MAINHAND) return;

        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        if(to.isEmpty()) return;
        if(!from.isEmpty() && from.getItem() == to.getItem()) return;

        if(to.getItem() instanceof IEquipReceiver receiver) {
            receiver.onEquip(player, to);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BreakEvent event) {
        BlockPos pos = event.getPos();
        Level level = (Level) event.getLevel();

        /*
         * DER GNEIS. Das Original gibt beim ersten Stueck fuenfhundert Erfahrung dazu und
         * prueft dafuer eigens, ob der Erfolg schon steht (ModEventHandler.java:1168) -- sonst
         * waere ein Gneisbruch eine Erfahrungsmuehle. Auf 1.21 steht dieselbe Auskunft im
         * Fortschrittsblatt des Spielers.
         *
         * DIE ERFAHRUNG WIRD GELEGT, NICHT GESETZT. BlockEvent.BreakEvent hatte in Forge ein
         * setExpToDrop; NeoForge hat es auf 1.21 herausgenommen und der Erfahrung ein eigenes
         * Ereignis gegeben (BlockDropsEvent.setDroppedExperience). Fuer eine feste Zahl
         * genuegt ExperienceOrb.award -- dasselbe Ergebnis, ein Ereignis weniger.
         */
        if(!level.isClientSide && event.getState().is(NtmBlocks.STONE_GNEISS.get())
                && event.getPlayer() instanceof ServerPlayer spieler
                && level instanceof ServerLevel serverLevel) {

            AdvancementHolder erfolg = spieler.server.getAdvancements()
                    .get(NuclearTechMod.withDefaultNamespace("stratum"));

            if(erfolg == null || !spieler.getAdvancements().getOrStartProgress(erfolg).isDone()) {
                NtmCriteria.marke(spieler, "stratum");
                ExperienceOrb.award(serverLevel, Vec3.atCenterOf(pos), 500);
            }
        }

        if (!level.isClientSide) {
            if (event.getState() == Blocks.COAL_ORE.defaultBlockState() || event.getState() == Blocks.DEEPSLATE_COAL_ORE.defaultBlockState() || event.getState() == Blocks.COAL_BLOCK.defaultBlockState()) {
                for (Direction dir : Direction.values()) {
                    BlockPos offsetPos = pos.relative(dir);

                    if (level.random.nextInt(2) == 0 && level.getBlockState(offsetPos).isAir()) {
                        level.setBlock(offsetPos, NtmBlocks.GAS_COAL.get().defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    /**
     * DER SCHLEIMBALL. "I should dip my balls in sulfuric acid" -- das Aufheben eines
     * Schleimballs genuegt, mehr will das Original nicht (ModEventHandler.java:1156).
     */
    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        if(event.getPlayer() instanceof ServerPlayer spieler && event.getOriginalStack().is(Items.SLIME_BALL)) {
            NtmCriteria.marke(spieler, "slimeball");
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        LivingPropsCommand.register(event.getDispatcher());
        SatellitesCommand.register(event.getDispatcher());
        ChunkRadCommand.register(event.getDispatcher());
        PollutionCommand.register(event.getDispatcher());
        RbmkDialCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(NtmMenuTypes.MACHINE_SOLDERING_STATION.get(), MachineSolderingStationScreen::new);
        event.register(NtmMenuTypes.MACHINE_ARC_WELDER.get(), MachineArcWelderScreen::new);
        event.register(NtmMenuTypes.MACHINE_SHREDDER.get(), MachineShredderScreen::new);
        event.register(NtmMenuTypes.MACHINE_ELECTRIC_FURNACE.get(), MachineElectricFurnaceScreen::new);
        event.register(NtmMenuTypes.MACHINE_MICROWAVE.get(), MachineMicrowaveScreen::new);
        event.register(NtmMenuTypes.MACHINE_RTG_FURNACE.get(), MachineRtgFurnaceScreen::new);
        event.register(NtmMenuTypes.MACHINE_DIFURNACE_RTG.get(), MachineDiFurnaceRtgScreen::new);
        event.register(NtmMenuTypes.MACHINE_PWR.get(), MachinePWRScreen::new);
        event.register(NtmMenuTypes.MACHINE_DIFURNACE.get(), MachineDiFurnaceScreen::new);
        event.register(NtmMenuTypes.MACHINE_COMBUSTION_ENGINE.get(), MachineCombustionEngineScreen::new);
        event.register(NtmMenuTypes.MACHINE_TURBINEGAS.get(), MachineTurbineGasScreen::new);
        event.register(NtmMenuTypes.MACHINE_TURBOFAN.get(), MachineTurbofanScreen::new);
        event.register(NtmMenuTypes.MACHINE_AUTOCRAFTER.get(), MachineAutocrafterScreen::new);
        event.register(NtmMenuTypes.MACHINE_EPRESS.get(), MachineEPressScreen::new);
        event.register(NtmMenuTypes.MACHINE_FUNNEL.get(), MachineFunnelScreen::new);
        event.register(NtmMenuTypes.MACHINE_STRAND_CASTER.get(), MachineStrandCasterScreen::new);
        event.register(NtmMenuTypes.MACHINE_KEY_FORGE.get(), MachineKeyForgeScreen::new);
        event.register(NtmMenuTypes.MACHINE_CHEMICAL_FACTORY.get(), MachineChemicalFactoryScreen::new);
        event.register(NtmMenuTypes.MACHINE_ASSEMBLY_FACTORY.get(), MachineAssemblyFactoryScreen::new);
        event.register(NtmMenuTypes.MACHINE_CRYSTALLIZER.get(), MachineCrystallizerScreen::new);
        event.register(NtmMenuTypes.MACHINE_RTG.get(), MachineRTGScreen::new);
        event.register(NtmMenuTypes.MACHINE_BATTERY.get(), MachineBatteryScreen::new);
        event.register(NtmMenuTypes.MACHINE_DIESEL.get(), MachineDieselScreen::new);
        event.register(NtmMenuTypes.MACHINE_COMPRESSOR.get(), MachineCompressorScreen::new);
        event.register(NtmMenuTypes.MACHINE_GAS_CENT.get(), MachineGasCentScreen::new);
        event.register(NtmMenuTypes.MACHINE_CYCLOTRON.get(), MachineCyclotronScreen::new);
        event.register(NtmMenuTypes.MACHINE_SILEX.get(), MachineSILEXScreen::new);
        event.register(NtmMenuTypes.MACHINE_FEL.get(), MachineFELScreen::new);
        event.register(NtmMenuTypes.MACHINE_PA_SOURCE.get(), MachinePASourceScreen::new);
        event.register(NtmMenuTypes.MACHINE_PA_RFC.get(), MachinePARFCScreen::new);
        event.register(NtmMenuTypes.MACHINE_PA_QUADRUPOLE.get(), MachinePAQuadrupoleScreen::new);
        event.register(NtmMenuTypes.MACHINE_PA_DIPOLE.get(), MachinePADipoleScreen::new);
        event.register(NtmMenuTypes.MACHINE_PA_DETECTOR.get(), MachinePADetectorScreen::new);
        event.register(NtmMenuTypes.MACHINE_EXPOSURE_CHAMBER.get(), MachineExposureChamberScreen::new);
        event.register(NtmMenuTypes.MACHINE_RAD_GEN.get(), MachineRadGenScreen::new);
        event.register(NtmMenuTypes.MACHINE_MINING_LASER.get(), MachineMiningLaserScreen::new);
        event.register(NtmMenuTypes.MACHINE_MIXER.get(), MachineMixerScreen::new);
        event.register(NtmMenuTypes.MACHINE_TURBINE.get(), MachineTurbineScreen::new);
        event.register(NtmMenuTypes.FURNACE_IRON.get(), FurnaceIronScreen::new);
        event.register(NtmMenuTypes.FURNACE_BRICK.get(), FurnaceBrickScreen::new);
        event.register(NtmMenuTypes.RADIO_TORCH_COUNTER.get(), RadioTorchCounterScreen::new);
        event.register(NtmMenuTypes.FURNACE_STEEL.get(), FurnaceSteelScreen::new);
        event.register(NtmMenuTypes.MACHINE_ROCK_MILL.get(), MachineRockMillScreen::new);
        event.register(NtmMenuTypes.MACHINE_OIL_WELL.get(), MachineOilWellScreen::new);
        event.register(NtmMenuTypes.MACHINE_REFINERY.get(), MachineRefineryScreen::new);
        event.register(NtmMenuTypes.MACHINE_CATALYTIC_REFORMER.get(), MachineCatalyticReformerScreen::new);
        event.register(NtmMenuTypes.MACHINE_HYDROTREATER.get(), MachineHydrotreaterScreen::new);
        event.register(NtmMenuTypes.MACHINE_VACUUM_DISTILL.get(), MachineVacuumDistillScreen::new);
        event.register(NtmMenuTypes.MACHINE_SOLIDIFIER.get(), MachineSolidifierScreen::new);
        event.register(NtmMenuTypes.MACHINE_PYRO_OVEN.get(), MachinePyroOvenScreen::new);
        event.register(NtmMenuTypes.MACHINE_LIQUEFACTOR.get(), MachineLiquefactorScreen::new);
        event.register(NtmMenuTypes.MACHINE_GAS_FLARE.get(), MachineGasFlareScreen::new);
        event.register(NtmMenuTypes.MACHINE_COKER.get(), MachineCokerScreen::new);
        event.register(NtmMenuTypes.FURNACE_COMBINATION.get(), MachineFurnaceCombinationScreen::new);
        event.register(NtmMenuTypes.MACHINE_BLAST_FURNACE.get(), MachineBlastFurnaceScreen::new);
        event.register(NtmMenuTypes.MACHINE_WOOD_BURNER.get(), MachineWoodBurnerScreen::new);
        event.register(NtmMenuTypes.MACHINE_ASHPIT.get(), AshpitScreen::new);
        event.register(NtmMenuTypes.MACHINE_CENTRIFUGE.get(), MachineCentrifugeScreen::new);
        event.register(NtmMenuTypes.MACHINE_PUREX.get(), MachinePUREXScreen::new);
        event.register(NtmMenuTypes.MACHINE_RADIOLYSIS.get(), MachineRadiolysisScreen::new);
        event.register(NtmMenuTypes.MACHINE_CHEMICAL_PLANT.get(), MachineChemicalPlantScreen::new);
        event.register(NtmMenuTypes.MACHINE_ARC_FURNACE.get(), MachineArcFurnaceLargeScreen::new);
        event.register(NtmMenuTypes.MACHINE_CRUCIBLE.get(), MachineCrucibleScreen::new);
        event.register(NtmMenuTypes.MACHINE_ROTARY_FURNACE.get(), MachineRotaryFurnaceScreen::new);
        event.register(NtmMenuTypes.RBMK_ROD.get(), RBMKRodScreen::new);
        event.register(NtmMenuTypes.RBMK_CONTROL.get(), RBMKControlScreen::new);
        event.register(NtmMenuTypes.RBMK_BOILER.get(), RBMKBoilerScreen::new);
        event.register(NtmMenuTypes.RBMK_HEATER.get(), RBMKHeaterScreen::new);
        event.register(NtmMenuTypes.RBMK_OUTGASSER.get(), RBMKOutgasserScreen::new);
        event.register(NtmMenuTypes.RBMK_CONTROL_AUTO.get(), RBMKControlAutoScreen::new);
        event.register(NtmMenuTypes.RBMK_STORAGE.get(), RBMKStorageScreen::new);
        event.register(NtmMenuTypes.CRANE_INSERTER.get(), CraneInserterScreen::new);
        event.register(NtmMenuTypes.CRANE_EXTRACTOR.get(), CraneExtractorScreen::new);
        event.register(NtmMenuTypes.CRANE_GRABBER.get(), CraneGrabberScreen::new);
        event.register(NtmMenuTypes.CRANE_BOXER.get(), CraneBoxerScreen::new);
        event.register(NtmMenuTypes.CRANE_UNBOXER.get(), CraneUnboxerScreen::new);
        event.register(NtmMenuTypes.CRANE_ROUTER.get(), CraneRouterScreen::new);
        event.register(NtmMenuTypes.RBMK_AUTOLOADER.get(), RBMKAutoloaderScreen::new);
        event.register(NtmMenuTypes.WASTE_DRUM.get(), WasteDrumScreen::new);
        event.register(NtmMenuTypes.SAT_LINKER.get(), MachineSatLinkerScreen::new);
        event.register(NtmMenuTypes.SAT_DOCK.get(), MachineSatDockScreen::new);
        event.register(NtmMenuTypes.TAPE_DRIVE.get(), MachineTapeDriveScreen::new);
        event.register(NtmMenuTypes.SUPER_COMPUTER.get(), MachineSuperComputerScreen::new);
        event.register(NtmMenuTypes.AMMO_PRESS.get(), MachineAmmoPressScreen::new);
        event.register(NtmMenuTypes.RADAR.get(), MachineRadarSlotsScreen::new);
        event.register(NtmMenuTypes.SIREN.get(), MachineSirenScreen::new);
        event.register(NtmMenuTypes.ANNIHILATOR.get(), MachineAnnihilatorScreen::new);
        event.register(NtmMenuTypes.BARREL.get(), BarrelScreen::new);
        event.register(NtmMenuTypes.MACHINE_BIGASSTANK.get(), MachineBigAssTankScreen::new);
        event.register(NtmMenuTypes.CRATE.get(), CrateScreen::new);
        event.register(NtmMenuTypes.ANVIL.get(), AnvilMenuScreen::new);
        event.register(NtmMenuTypes.HEATER_FIREBOX.get(), HeaterFireboxScreen::new);
        event.register(NtmMenuTypes.HEATER_OVEN.get(), HeaterOvenScreen::new);
        event.register(NtmMenuTypes.HEATER_OILBURNER.get(), HeaterOilburnerScreen::new);
        event.register(NtmMenuTypes.HEATER_HEATEX.get(), HeaterHeatexScreen::new);

        event.register(NtmMenuTypes.FLUID_TANK.get(), MachineFluidTankScreen::new);

        event.register(NtmMenuTypes.ASSEMBLY_MACHINE.get(), MachineAssemblyMachineScreen::new);
        event.register(NtmMenuTypes.PRECASS.get(), MachinePrecAssScreen::new);
        event.register(NtmMenuTypes.ORE_SLOPPER.get(), MachineOreSlopperScreen::new);
        event.register(NtmMenuTypes.EXCAVATOR.get(), MachineExcavatorScreen::new);
        event.register(NtmMenuTypes.MISSILE_ASSEMBLY.get(), MachineMissileAssemblyScreen::new);
        event.register(NtmMenuTypes.PRESS.get(), MachinePressScreen::new);

        event.register(NtmMenuTypes.REACTOR_ZIRNOX.get(), ReactorZirnoxScreen::new);
        event.register(NtmMenuTypes.WATZ.get(), WatzScreen::new);
        event.register(NtmMenuTypes.ICF.get(), ICFScreen::new);
        event.register(NtmMenuTypes.ICF_PRESS.get(), ICFPressScreen::new);
        event.register(NtmMenuTypes.FUSION_TORUS.get(), FusionTorusScreen::new);
        event.register(NtmMenuTypes.FUSION_KLYSTRON.get(), FusionKlystronScreen::new);
        event.register(NtmMenuTypes.FUSION_BREEDER.get(), FusionBreederScreen::new);
        event.register(NtmMenuTypes.FUSION_PLASMA_FORGE.get(), FusionPlasmaForgeScreen::new);
        event.register(NtmMenuTypes.REACTOR_RESEARCH.get(), ReactorResearchScreen::new);
        event.register(NtmMenuTypes.MACHINE_REACTOR_BREEDING.get(), MachineReactorBreedingScreen::new);
        event.register(NtmMenuTypes.TURRET_BASE.get(), TurretBaseScreen::new);
        event.register(NtmMenuTypes.REACTOR_CONTROL.get(), ReactorControlScreen::new);
        event.register(NtmMenuTypes.WEAPON_TABLE.get(), WeaponTableScreen::new);
        event.register(NtmMenuTypes.ARMOR_TABLE.get(), ArmorTableScreen::new);
        event.register(NtmMenuTypes.BOOK.get(), BookScreen::new);

        event.register(NtmMenuTypes.BATTERY_SOCKET.get(), BatterySocketScreen::new);
        event.register(NtmMenuTypes.BATTERY_REDD.get(), BatteryREDDScreen::new);

        event.register(NtmMenuTypes.NUKE_GADGET.get(), NukeGadgetScreen::new);
        event.register(NtmMenuTypes.NUKE_LITTLE_BOY.get(), NukeLittleBoyScreen::new);
        event.register(NtmMenuTypes.NUKE_FAT_MAN.get(), NukeFatManScreen::new);
        event.register(NtmMenuTypes.NUKE_IVY_MIKE.get(), NukeIvyMikeScreen::new);
        event.register(NtmMenuTypes.NUKE_TSAR_BOMBA.get(), NukeTsarBombaScreen::new);
        event.register(NtmMenuTypes.NUKE_PROTOTYPE.get(), NukePrototypeScreen::new);
        event.register(NtmMenuTypes.NUKE_FLEIJA.get(), NukeFleijaScreen::new);
        event.register(NtmMenuTypes.NUKE_SOLINIUM.get(), NukeSoliniumScreen::new);
        event.register(NtmMenuTypes.NUKE_N2.get(), NukeN2Screen::new);
        event.register(NtmMenuTypes.NUKE_FSTBMB.get(), NukeFstbmbScreen::new);

        event.register(NtmMenuTypes.LAUNCH_PAD_LARGE.get(), LaunchPadLargeScreen::new);
        event.register(NtmMenuTypes.SOYUZ_LAUNCHER.get(), SoyuzLauncherScreen::new);
    }
}
