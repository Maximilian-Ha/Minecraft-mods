package com.hbm.blockentity.machine;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.BedrockOreBlockEntity;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.DepthRockBlock;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.item.MovingItem;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineExcavatorMenu;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.DrillbitItem;
import com.hbm.items.machine.DrillbitItem.EnumDrillType;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.items.special.BedrockOreBaseItem;
import com.hbm.lib.Library;
import com.hbm.util.InventoryUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineExcavator.
 *
 * Der Bagger. Er frisst sich Lage fuer Lage nach unten, raeumt jede Lage von innen nach aussen
 * ab und sammelt ein, was dabei anfaellt. Ganz unten wartet das Grundgesteinserz.
 *
 * ER ARBEITET IN RINGEN. Der innerste Ring ist drei mal drei, jeder weitere legt einen Rahmen
 * darum. Erst wenn alle Ringe einer Lage geraeumt sind, faehrt der Bohrer eine Lage tiefer. Wie
 * lange ein Ring dauert, haengt an der Summe der Blockhaerten -- Stein geht schnell, Obsidian
 * nicht.
 *
 * VIER SCHALTER am Bedienfeld: der Bohrer selbst, der Brecher (zermahlt das Bruchgut gleich),
 * das Zumauern (ersetzt Fluessigkeiten und Luft am Rand durch Absperrung) und Adernbau plus
 * Seidenberuehrung, die beide zusaetzlich den richtigen Bohrkopf verlangen.
 *
 * DAS GRUNDGESTEINSERZ IST DER EIGENTLICHE ZWECK. Trifft der Bohrer darauf, braucht er fuenf
 * Minuten, den passenden Bohrkopf und -- je nach Ergiebigkeit -- Saeure im Tank. Heraus kommt
 * eine Rohprobe, die weiss, wie reich die Stelle war.
 *
 * TIEFENGESTEIN SCHALTET IHN AB. Es steht um jedes Erz herum und ist fuer den Bagger
 * undurchdringlich; wer weiterkommen will, muss es von Hand wegraeumen.
 *
 * ABWEICHUNG: das Original erkennt Erze am Erzwoerterbuch (ein Name, der mit "ore" beginnt). Der
 * Port hat kein Erzwoerterbuch und nimmt den Sammelbegriff c:ores.
 *
 * ABWEICHUNG: fuer die Seidenberuehrung ruft das Original ueber Reflexion createStackedBlock auf.
 * Auf 1.21 fuehrt der Weg ueber das Werkzeug -- eine Spitzhacke mit der Verzauberung, genau wie
 * beim Bergbaulaser aus Runde 118.
 */
public class MachineExcavatorBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardReceiverMK2, IControlReceiver, IUpgradeInfoProvider {

    public static final long MAX_POWER = 1_000_000;
    public long power;
    public boolean operational = false;

    public boolean enableDrill = false;
    public boolean enableCrusher = false;
    public boolean enableWalling = false;
    public boolean enableVeinMiner = false;
    public boolean enableSilkTouch = false;

    protected int ticksWorked = 0;
    /** Null ist die erste Lage unter der Ruhestellung. */
    protected int targetDepth = 0;
    protected boolean bedrockDrilling = false;

    public float drillRotation, prevDrillRotation;
    public float drillExtension, prevDrillExtension;
    public float crusherRotation, prevCrusherRotation;
    public int chuteTimer = 0;

    public double speed = 1.0D;
    public static final long BASE_CONSUMPTION = 10_000L;
    public long consumption = BASE_CONSUMPTION;

    public FluidTank tank;

    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public MachineExcavatorBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_EXCAVATOR.get(), pos, state, 14);
        this.tank = new FluidTank(Fluids.NONE, 16_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineExcavator");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        /* Die Aufwertungen muss auch der Client kennen -- die Oberflaeche zeigt den Verbrauch. */
        this.upgradeManager.checkSlots(slots, 2, 3);
        int speedLevel = this.upgradeManager.getLevel(UpgradeType.SPEED);
        int powerLevel = this.upgradeManager.getLevel(UpgradeType.POWER);

        this.consumption = BASE_CONSUMPTION * (1 + speedLevel) / (1 + powerLevel);

        if(!this.level.isClientSide) {

            if(!(this.level instanceof ServerLevel serverLevel)) return;

            this.tank.setType(1, slots);

            if(this.level.getGameTime() % 20 == 0) this.tryEjectBuffer();

            for(DirPos pos : getConPos()) {
                this.trySubscribe(level, pos);
                if(this.tank.getTankType() != Fluids.NONE) this.trySubscribe(this.tank.getTankType(), level, pos);
            }

            if(this.chuteTimer > 0) this.chuteTimer--;

            this.power = Library.chargeTEFromItems(slots, 0, this.getPower(), this.getMaxPower());
            this.operational = false;
            int radiusLevel = this.upgradeManager.getLevel(UpgradeType.EFFECT);

            EnumDrillType type = this.getInstalledDrill();

            if(this.enableDrill && type != null && this.power >= this.getPowerConsumption()) {

                this.operational = true;
                this.power -= this.getPowerConsumption();

                this.speed = type.speed * (1 + speedLevel / 2D);

                int maxDepth = this.worldPosition.getY() - 4;

                if((this.bedrockDrilling || this.targetDepth <= maxDepth) && this.tryDrill(serverLevel, 1 + radiusLevel * 2)) {
                    this.targetDepth++;
                    if(this.targetDepth > maxDepth) this.enableDrill = false;
                }

            } else {
                this.targetDepth = 0;
            }

            this.networkPackNT(150);

        } else {
            this.animate();
        }
    }

    /** Bohrer faehrt aus und ein, Bohrkopf und Brecherwalzen drehen. Reine Anzeige. */
    private void animate() {

        this.prevDrillExtension = this.drillExtension;

        if(this.drillExtension != this.targetDepth) {
            float diff = Math.abs(this.drillExtension - this.targetDepth);
            float move = Math.max(0.15F, diff / 10F);

            if(diff <= move) {
                this.drillExtension = this.targetDepth;
            } else {
                this.drillExtension -= Math.signum(this.drillExtension - this.targetDepth) * move;
            }
        }

        this.prevDrillRotation = this.drillRotation;
        this.prevCrusherRotation = this.crusherRotation;

        if(this.operational) {
            this.drillRotation += 15F;
            if(this.enableCrusher) this.crusherRotation += 15F;
        }

        if(this.drillRotation >= 360F) { this.drillRotation -= 360F; this.prevDrillRotation -= 360F; }
        if(this.crusherRotation >= 360F) { this.crusherRotation -= 360F; this.prevCrusherRotation -= 360F; }
    }

    /** Die Lage, an der gerade gearbeitet wird. */
    protected int getY() {
        return this.worldPosition.getY() - this.targetDepth - 4;
    }

    /**
     * Raeumt von innen nach aussen einen Ring nach dem anderen. Gibt true zurueck, wenn die ganze
     * Lage durch ist und der Bohrer eine tiefer darf.
     */
    protected boolean tryDrill(ServerLevel level, int radius) {

        int y = this.getY();

        /* Die erste Lage und die unterste bleiben eng -- sonst risse der Bagger sich den Boden weg. */
        if(this.targetDepth == 0 || y == level.getMinBuildHeight()) radius = 1;

        for(int ring = 1; ring <= radius; ring++) {

            boolean ignoreAll = true;
            float combinedHardness = 0F;
            BlockPos bedrockOre = null;
            this.bedrockDrilling = false;

            outer:
            for(int x = this.worldPosition.getX() - ring; x <= this.worldPosition.getX() + ring; x++) {
                for(int z = this.worldPosition.getZ() - ring; z <= this.worldPosition.getZ() + ring; z++) {

                    if(!isInRing(x, z, ring)) continue;

                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if(state.is(NtmBlocks.ORE_BEDROCK.get())) {
                        /* Fuenf Minuten je Erz -- das Original rechnet in Ticks. */
                        combinedHardness = 5 * 60 * 20;
                        bedrockOre = pos;
                        this.bedrockDrilling = true;
                        this.enableCrusher = false;
                        ignoreAll = false;
                        break outer;
                    }

                    if(state.getBlock() instanceof DepthRockBlock) this.enableDrill = false;

                    if(this.shouldIgnoreBlock(level, state, pos)) continue;

                    ignoreAll = false;
                    combinedHardness += state.getDestroySpeed(level, pos);
                }
            }

            if(ignoreAll) continue;

            this.ticksWorked++;

            int ticksToWork = (int) Math.ceil(combinedHardness / this.speed);
            if(this.ticksWorked < ticksToWork) return false;

            if(bedrockOre == null) {
                this.breakBlocks(level, ring);
                this.buildWall(level, ring + 1, ring == radius && this.enableWalling);
                if(ring == radius) this.mineOuterOres(level, ring + 1);
                this.tryCollect(level, radius + 1);
            } else {
                this.collectBedrock(level, bedrockOre);
            }

            this.ticksWorked = 0;
            return false;
        }

        this.buildWall(level, radius + 1, this.enableWalling);
        this.ticksWorked = 0;
        return true;
    }

    /** Der innerste Ring ist voll, alle weiteren nur ihr Rand. */
    private boolean isInRing(int x, int z, int ring) {
        if(ring == 1) return true;
        return x == this.worldPosition.getX() - ring || x == this.worldPosition.getX() + ring
                || z == this.worldPosition.getZ() - ring || z == this.worldPosition.getZ() + ring;
    }

    /** Das Grundgesteinserz: Bohrkopfstufe pruefen, Saeure abziehen, Rohprobe fuellen. */
    protected void collectBedrock(ServerLevel level, BlockPos pos) {

        if(!(level.getBlockEntity(pos) instanceof BedrockOreBlockEntity ore)) return;
        if(ore.resource.isEmpty()) return;

        EnumDrillType drill = this.getInstalledDrill();
        if(drill == null || ore.tier > drill.tier) return;

        if(ore.acidRequirement != null) {
            if(ore.acidRequirement.type != this.tank.getTankType()) return;
            if(ore.acidRequirement.fill > this.tank.getFill()) return;
            this.tank.setFill(this.tank.getFill() - ore.acidRequirement.fill);
        }

        ItemStack stack = ore.resource.copy();

        /* Die Probe merkt sich die Stelle -- und der Glueckszuschlag des Bohrkopfs erhoeht, was
         * sie hergibt. */
        if(stack.getItem() == NtmItems.BEDROCK_ORE_BASE.get()) {
            BedrockOreBaseItem.setOreAmount(stack, pos, 1D + drill.fortune * 0.1D);
        }

        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(stack);

        this.output(level, stacks);

        for(ItemStack rest : stacks) this.bufferOrDrop(level, rest, pos);
    }

    /** Bricht alles in einem Ring. */
    protected void breakBlocks(ServerLevel level, int ring) {

        int y = this.getY();

        for(int x = this.worldPosition.getX() - ring; x <= this.worldPosition.getX() + ring; x++) {
            for(int z = this.worldPosition.getZ() - ring; z <= this.worldPosition.getZ() + ring; z++) {

                if(!isInRing(x, z, ring)) continue;

                BlockPos pos = new BlockPos(x, y, z);
                if(!this.shouldIgnoreBlock(level, level.getBlockState(pos), pos)) this.tryMineAtLocation(level, pos);
            }
        }
    }

    public void tryMineAtLocation(ServerLevel level, BlockPos pos) {

        BlockState state = level.getBlockState(pos);

        if(this.canVeinMine() && state.is(Tags.Blocks.ORES)) {

            this.recursionBrake.clear();
            AABB[] bounds = new AABB[] { new AABB(pos) };
            this.breakRecursively(level, pos, state.getBlock(), 10, bounds);
            this.recursionBrake.clear();

            /* Alles, was dabei in der Ader angefallen ist, an die Bohrstelle holen -- sonst
             * laege es ausserhalb der Sammelreichweite. */
            for(ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, bounds[0].inflate(0.5D))) {
                item.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            }

            return;
        }

        this.breakSingleBlock(level, state, pos);
    }

    private final Set<BlockPos> recursionBrake = new HashSet<>();

    protected void breakRecursively(ServerLevel level, BlockPos pos, Block block, int depth, AABB[] bounds) {

        if(depth < 0) return;
        if(!this.recursionBrake.add(pos)) return;

        for(Direction dir : Direction.values()) {
            BlockPos next = pos.relative(dir);
            if(level.getBlockState(next).is(block)) this.breakRecursively(level, next, block, depth - 1, bounds);
        }

        this.breakSingleBlock(level, level.getBlockState(pos), pos);

        bounds[0] = bounds[0].minmax(new AABB(pos));

        if(this.enableWalling) level.setBlockAndUpdate(pos, NtmBlocks.BARRICADE.get().defaultBlockState());
    }

    protected void breakSingleBlock(ServerLevel level, BlockState state, BlockPos pos) {

        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);

        if(this.canSilkTouch()) {
            tool.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SILK_TOUCH), 1);
        } else {
            int fortune = this.getFortuneLevel();
            if(fortune > 0) tool.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.FORTUNE), fortune);
        }

        List<ItemStack> items = new ArrayList<>(Block.getDrops(state, level, pos, level.getBlockEntity(pos), null, tool));

        if(this.enableCrusher) {
            List<ItemStack> crushedList = new ArrayList<>();
            for(ItemStack stack : items) {
                ItemStack crushed = ShredderRecipes.getShredderResult(stack).copy();
                /* Schrott und Staub sind das "kein Rezept"-Ergebnis des Ports -- dann bleibt das
                 * Bruchgut, wie es ist. */
                if(crushed.isEmpty() || crushed.getItem() == NtmItems.SCRAPS.get() || crushed.getItem() == NtmItems.DUST.get()) {
                    crushedList.add(stack);
                } else {
                    crushed.setCount(crushed.getCount() * stack.getCount());
                    crushedList.add(crushed);
                }
            }
            items = crushedList;
        }

        /* Die eigene Absperrung gibt nichts her -- sonst liesse sich Baumaterial vermehren. */
        if(state.is(NtmBlocks.BARRICADE.get())) items.clear();

        for(ItemStack item : items) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item));
        }

        level.destroyBlock(pos, false);
    }

    /**
     * Zieht eine Wand am Rand hoch. Fluessigkeiten werden immer ersetzt; ist wallEverything
     * gesetzt, auch Luft und Gras. Innerhalb des Rings verschwinden Fluessigkeiten ersatzlos.
     */
    protected void buildWall(ServerLevel level, int ring, boolean wallEverything) {

        int y = this.getY();

        for(int x = this.worldPosition.getX() - ring; x <= this.worldPosition.getX() + ring; x++) {
            for(int z = this.worldPosition.getZ() - ring; z <= this.worldPosition.getZ() + ring; z++) {

                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(pos);

                boolean edge = x == this.worldPosition.getX() - ring || x == this.worldPosition.getX() + ring
                        || z == this.worldPosition.getZ() - ring || z == this.worldPosition.getZ() + ring;

                if(edge) {
                    if(state.canBeReplaced() && (wallEverything || !state.getFluidState().isEmpty())) {
                        level.setBlockAndUpdate(pos, NtmBlocks.BARRICADE.get().defaultBlockState());
                    }
                } else if(!state.getFluidState().isEmpty()) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    /** Erze am aeusseren Rand nimmt er mit, auch wenn der Ring sonst stehen bleibt. */
    protected void mineOuterOres(ServerLevel level, int ring) {

        int y = this.getY();

        for(int x = this.worldPosition.getX() - ring; x <= this.worldPosition.getX() + ring; x++) {
            for(int z = this.worldPosition.getZ() - ring; z <= this.worldPosition.getZ() + ring; z++) {

                if(!isInRing(x, z, ring)) continue;

                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(pos);

                if(!this.shouldIgnoreBlock(level, state, pos) && state.is(Tags.Blocks.ORES)) this.tryMineAtLocation(level, pos);
            }
        }
    }

    /** Wohin der Bagger ablaedt: vier Bloecke vor ihm, drei tiefer. */
    protected BlockPos getOutputPos() {
        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        return this.worldPosition.relative(dir, 4).below(3);
    }

    /** Raeumt den eigenen Zwischenspeicher in Kiste oder auf das Band. */
    protected void tryEjectBuffer() {

        if(!(this.level instanceof ServerLevel serverLevel)) return;

        List<ItemStack> items = new ArrayList<>();
        for(int i = 5; i < 14; i++) if(!slots.get(i).isEmpty()) items.add(slots.get(i).copy());
        if(items.isEmpty()) return;

        this.output(serverLevel, items);

        items.removeIf(ItemStack::isEmpty);

        for(int i = 5; i < 14; i++) {
            int index = i - 5;
            slots.set(i, items.size() > index ? items.get(index).copy() : ItemStack.EMPTY);
        }
    }

    /** Sammelt alles ein, was um den Bohrer herumliegt. */
    protected void tryCollect(ServerLevel level, int radius) {

        int y = this.getY();

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(
                this.worldPosition.getX() - radius, y - 1, this.worldPosition.getZ() - radius,
                this.worldPosition.getX() + radius + 1, y + 2, this.worldPosition.getZ() + radius + 1));

        List<ItemStack> stacks = new ArrayList<>();
        for(ItemEntity item : items) if(item.isAlive()) stacks.add(item.getItem());

        this.output(level, stacks);

        for(ItemEntity item : items) {
            if(!item.isAlive()) continue;
            ItemStack stack = item.getItem();
            if(stack.isEmpty()) { item.discard(); continue; }

            this.bufferOrDrop(level, stack, item.blockPosition());

            if(stack.isEmpty()) {
                item.discard();
                /* Sonst laesst sich der Gegenstand im selben Tick noch aufheben und verdoppeln. */
                item.setPickUpDelay(60);
            }
        }
    }

    /** Legt ab, was geht: erst Kiste, dann Band. Was bleibt, bleibt in der Liste stehen. */
    protected void output(ServerLevel level, List<ItemStack> items) {

        BlockPos out = this.getOutputPos();

        if(level.getBlockEntity(out) instanceof Container container) {
            for(ItemStack item : items) {
                if(item.isEmpty()) continue;
                this.insertInto(container, item);
            }
        }

        if(level.getBlockState(out).getBlock() instanceof IConveyorBelt belt) this.supplyConveyor(level, belt, items, out);
    }

    /**
     * Schiebt einen Stapel in einen beliebigen Behaelter -- erst auf gleiche Stapel, dann in
     * leere Faecher. Der uebergebene Stapel schrumpft um das, was untergekommen ist.
     *
     * NICHT ueber InventoryUtil: der Helfer des Ports arbeitet auf einer NonNullList und damit
     * nur auf Maschinen dieses Mods. Hier kann auch eine Vanillakiste stehen.
     */
    protected void insertInto(Container container, ItemStack stack) {

        for(int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
            ItemStack slot = container.getItem(i);
            if(slot.isEmpty()) continue;
            if(!ItemStack.isSameItemSameComponents(slot, stack)) continue;

            int transfer = Math.min(stack.getCount(), Math.min(slot.getMaxStackSize(), container.getMaxStackSize()) - slot.getCount());
            if(transfer <= 0) continue;

            slot.grow(transfer);
            stack.shrink(transfer);
            container.setChanged();
            this.chuteTimer = 40;
        }

        for(int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
            if(!container.getItem(i).isEmpty()) continue;
            if(!container.canPlaceItem(i, stack)) continue;

            int transfer = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), container.getMaxStackSize()));
            ItemStack put = stack.copy();
            put.setCount(transfer);
            container.setItem(i, put);
            stack.shrink(transfer);
            container.setChanged();
            this.chuteTimer = 40;
        }
    }

    /** Setzt die Gegenstaende auf ein Foerderband. */
    protected void supplyConveyor(ServerLevel level, IConveyorBelt belt, List<ItemStack> items, BlockPos pos) {

        for(ItemStack item : items) {

            if(item.isEmpty()) continue;

            Vec3 base = new Vec3(pos.getX() + level.random.nextDouble(), pos.getY() + 0.5, pos.getZ() + level.random.nextDouble());
            Vec3 snap = belt.getClosestSnappingPosition(level, pos, base);

            MovingItem moving = new MovingItem(NtmEntityTypes.MOVING_ITEM.get(), level);
            moving.setPos(base.x, snap.y, base.z);
            moving.setItemStack(item.copy());
            level.addFreshEntity(moving);

            item.setCount(0);
            this.chuteTimer = 40;
        }
    }

    /** Was nirgends hinpasst, kommt in den Zwischenspeicher -- und wenn der voll ist, auf den Boden. */
    protected void bufferOrDrop(ServerLevel level, ItemStack stack, BlockPos pos) {

        if(stack.isEmpty()) return;

        ItemStack rest = InventoryUtil.tryAddItemToInventory(slots, 5, 13, stack);
        if(rest.getCount() != stack.getCount()) this.chuteTimer = 40;
        stack.setCount(rest.getCount());
    }

    public long getPowerConsumption() { return this.consumption; }

    public int getFortuneLevel() {
        EnumDrillType type = this.getInstalledDrill();
        return type != null ? type.fortune : 0;
    }

    /** Luft, Gas, unzerstoerbares und Fluessiges laesst er stehen. */
    public boolean shouldIgnoreBlock(ServerLevel level, BlockState state, BlockPos pos) {
        return state.isAir()
                || state.getDestroySpeed(level, pos) < 0
                || !state.getFluidState().isEmpty()
                || state.is(Blocks.BEDROCK);
    }

    @Override
    public void receiveControl(CompoundTag data) {
        if(data.contains("drill")) this.enableDrill = !this.enableDrill;
        if(data.contains("crusher")) this.enableCrusher = !this.enableCrusher;
        if(data.contains("walling")) this.enableWalling = !this.enableWalling;
        if(data.contains("veinminer")) this.enableVeinMiner = !this.enableVeinMiner;
        if(data.contains("silktouch")) this.enableSilkTouch = !this.enableSilkTouch;

        this.setChanged();
    }

    public EnumDrillType getInstalledDrill() {
        ItemStack stack = slots.get(4);
        return stack.getItem() instanceof DrillbitItem ? DrillbitItem.getType(stack) : null;
    }

    public boolean canVeinMine() {
        EnumDrillType type = this.getInstalledDrill();
        return this.enableVeinMiner && type != null && type.vein;
    }

    public boolean canSilkTouch() {
        EnumDrillType type = this.getInstalledDrill();
        return this.enableSilkTouch && type != null && type.silk;
    }

    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise();

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY() + 1;
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + dir.getStepX() * 4 + rot.getStepX(), y, z + dir.getStepZ() * 4 + rot.getStepZ(), dir),
                new DirPos(x + dir.getStepX() * 4 - rot.getStepX(), y, z + dir.getStepZ() * 4 - rot.getStepZ(), dir),
                new DirPos(x + rot.getStepX() * 4, y, z + rot.getStepZ() * 4, rot),
                new DirPos(x - rot.getStepX() * 4, y, z - rot.getStepZ() * 4, rot.getOpposite())
        };
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(enableDrill);
        buf.writeBoolean(enableCrusher);
        buf.writeBoolean(enableWalling);
        buf.writeBoolean(enableVeinMiner);
        buf.writeBoolean(enableSilkTouch);
        buf.writeBoolean(operational);
        buf.writeInt(targetDepth);
        buf.writeInt(chuteTimer);
        buf.writeLong(power);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.enableDrill = buf.readBoolean();
        this.enableCrusher = buf.readBoolean();
        this.enableWalling = buf.readBoolean();
        this.enableVeinMiner = buf.readBoolean();
        this.enableSilkTouch = buf.readBoolean();
        this.operational = buf.readBoolean();
        this.targetDepth = buf.readInt();
        this.chuteTimer = buf.readInt();
        this.power = buf.readLong();
        this.tank.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.enableDrill = tag.getBoolean("d");
        this.enableCrusher = tag.getBoolean("c");
        this.enableWalling = tag.getBoolean("w");
        this.enableVeinMiner = tag.getBoolean("v");
        this.enableSilkTouch = tag.getBoolean("s");
        this.targetDepth = tag.getInt("t");
        this.power = tag.getLong("p");
        this.tank.readFromNBT(tag, "tank");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("d", this.enableDrill);
        tag.putBoolean("c", this.enableCrusher);
        tag.putBoolean("w", this.enableWalling);
        tag.putBoolean("v", this.enableVeinMiner);
        tag.putBoolean("s", this.enableSilkTouch);
        tag.putInt("t", this.targetDepth);
        tag.putLong("p", this.power);
        this.tank.writeToNBT(tag, "tank");
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == 0) return true;                                      // Batterie
        if(slot == 1) return true;                                      // Fluidkennung
        if(slot >= 2 && slot <= 3) return true;                         // Aufwertungen
        if(slot == 4) return stack.getItem() instanceof DrillbitItem;   // Bohrkopf
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= 5;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { 5, 6, 7, 8, 9, 10, 11, 12, 13 };
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tank }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { tank }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineExcavatorMenu(id, inventory, this);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.EFFECT;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {
        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_EXCAVATOR.get()));
        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_SPEED, "+" + (lvl * 50) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 100) + "%").withStyle(ChatFormatting.RED));
        }
        if(type == UpgradeType.POWER) {
            components.add(Component.translatable(KEY_CONSUMPTION, "-" + (lvl * 100 / (1 + lvl)) + "%").withStyle(ChatFormatting.GREEN));
        }
        if(type == UpgradeType.EFFECT) {
            components.add(Component.translatable(KEY_RANGE, "+" + (lvl * 2)).withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.POWER, 3);
        upgrades.put(UpgradeType.EFFECT, 3);
        return upgrades;
    }
}
