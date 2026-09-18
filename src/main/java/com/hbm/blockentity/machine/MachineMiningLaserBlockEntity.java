package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineMiningLaserMenu;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes.CrystallizerRecipe;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
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
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineMiningLaser.
 *
 * Der Bergbaulaser graebt einen Schacht gerade nach unten und raeumt Lage fuer Lage ab. Er wird
 * AN DIE DECKE gesetzt und arbeitet von dort aus abwaerts, bis er am Grundgestein ansteht.
 *
 * ER SUCHT SICH SEIN ZIEL SELBST. Je Arbeitsschritt geht er die aktuelle Lage in seinem Umkreis
 * durch, nimmt den ersten Block, den er brechen darf, und bricht ihn; ist die Lage leer, faellt
 * er eine tiefer. Eingestellt wird nichts ausser dem Schalter.
 *
 * FLUESSIGKEIT SETZT ER TROCKEN. Wo Wasser oder Lava an den Schacht grenzt, setzt er einen
 * Daemmblock -- sonst liefe der Schacht voll, und der Laser stuende in einem See.
 *
 * SECHS SONDERAUFWERTUNGEN, und sie sind der eigentliche Reiz der Maschine: Schmelzer,
 * Schredder, Zentrifuge und Kristallisator verarbeiten das Bruchstueck NOCH IM SCHACHT, statt es
 * erst durch eine Maschinenkette zu schicken. Sie schliessen einander aus -- der Laser nimmt die
 * erste, die er findet. Der Nullifikator wirft Dreck, Stein und Kies gleich weg, der Schrei tut
 * nichts ausser Laerm.
 *
 * ERZOEL WIRD ZU OEL. Faellt ein Oelschieferblock an, wandert er nicht ins Fach, sondern als
 * fuenfhundert Millibar in den Tank, und der Tank gibt sie an die Rohre weiter.
 *
 * ER LAEDT KISTEN UNTER SICH AB. Was in den einundzwanzig Faechern liegt, schiebt er in jeden
 * Behaelter, der an einem seiner vier Seitenanschluesse steht.
 *
 * ABWEICHUNG: das Original zaehlt die Behaelter auf, in die es abladen darf -- Kiste, Falle,
 * drei Kistensorten, Tresor, Trichter. Hier gilt jeder Block mit einem Warenlager. Auf 1.21 gibt
 * es dafuer eine Abfrage, und eine feste Liste waere nur eine Gelegenheit, etwas zu vergessen.
 *
 * ABWEICHUNG: der Tresor der Liste fehlt dem Port ohnehin.
 *
 * NICHT UEBERNOMMEN: der Strahl und der Bruchfortschritt als Bild im Block; beides gehoert zum
 * Modell des Originals. Der Bruchfortschritt steht in der Oberflaeche.
 */
public class MachineMiningLaserBlockEntity extends MachineBaseBlockEntity
        implements IEnergyReceiverMK2, IFluidStandardSenderMK2, IUpgradeInfoProvider, IControlReceiver {

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_UPGRADE_START = 1;
    public static final int SLOT_UPGRADE_END = 8;
    public static final int SLOT_OUTPUT_START = 9;
    public static final int SLOT_OUTPUT_END = 29;
    public static final int SLOTS = 30;

    public static final long maxPower = 100_000_000L;
    public static final int consumption = 10_000;

    public long power;
    public boolean isOn;
    private boolean redstonePowered;

    public int targetX;
    public int targetY;
    public int targetZ;
    public int lastTargetX;
    public int lastTargetY;
    public int lastTargetZ;

    public boolean beam;
    private double breakProgress;
    /** Nur zur Anzeige; auf dem Server gerechnet, an den Client geschickt. */
    public double clientBreakProgress;

    public final FluidTank tank = new FluidTank(Fluids.OIL, 64_000);

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> quickCheck =
            RecipeManager.createCheck(RecipeType.SMELTING);

    public MachineMiningLaserBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_MINING_LASER.get(), pos, state, SLOTS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.miningLaser");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        /* Strom kommt von oben, Fluid geht zu allen vier Seiten. */
        this.trySubscribe(this.level, new DirPos(this.worldPosition.above(2), Direction.UP));

        for(DirPos pos : this.getConPos()) this.tryProvide(this.tank, this.level, pos);

        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, maxPower);

        /* Wandert das Ziel, faengt der Bruch von vorn an. */
        if(this.lastTargetX != this.targetX || this.lastTargetY != this.targetY || this.lastTargetZ != this.targetZ) {
            this.breakProgress = 0;
        }

        this.lastTargetX = this.targetX;
        this.lastTargetY = this.targetY;
        this.lastTargetZ = this.targetZ;

        boolean previous = this.redstonePowered;
        this.redstonePowered = this.isMultiblockRedstonePowered();
        if(previous != this.redstonePowered) this.setChanged();

        if(this.isOn && !this.redstonePowered) {
            this.work();
        } else {
            this.targetY = this.worldPosition.getY() - 2;
            this.beam = false;
        }

        for(DirPos pos : this.getConPos()) this.tryFillContainer(pos.getX(), pos.getY(), pos.getZ());

        this.networkPackNT(250);
    }

    private void work() {

        this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE_START, SLOT_UPGRADE_END);

        int cycles = 1 + this.upgradeManager.getLevel(UpgradeType.OVERDRIVE);
        int speed = 1 + this.upgradeManager.getLevel(UpgradeType.SPEED);
        int range = 1 + this.upgradeManager.getLevel(UpgradeType.EFFECT) * 2;
        int fortune = this.upgradeManager.getLevel(UpgradeType.FORTUNE);

        int draw = consumption
                - (consumption * this.upgradeManager.getLevel(UpgradeType.POWER) / 16)
                + (consumption * this.upgradeManager.getLevel(UpgradeType.SPEED) / 16);

        for(int i = 0; i < cycles; i++) {

            if(this.power < draw) {
                this.beam = false;
                break;
            }

            this.power -= draw;

            if(this.targetY <= this.level.getMinBuildHeight()) this.targetY = this.worldPosition.getY() - 2;

            this.scan(range);

            BlockPos target = new BlockPos(this.targetX, this.targetY, this.targetZ);
            BlockState state = this.level.getBlockState(target);

            /* Fluessigkeit wird weggeraeumt und abgedaemmt, nicht gebrochen. */
            if(isLiquid(state)) {
                this.level.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
                this.buildDam(target);
                continue;
            }

            if(this.beam && this.canBreak(state, target)) {

                this.breakProgress += this.getBreakSpeed(target, speed);
                this.clientBreakProgress = Math.min(this.breakProgress, 1);

                if(this.breakProgress >= 1) {
                    this.breakBlock(target, fortune);
                    this.buildDam(target);
                }
            }
        }
    }

    /**
     * Die aktuelle Lage im Umkreis durchgehen und den ersten brechbaren Block nehmen. Ist nichts
     * da, faellt der Laser eine Lage tiefer -- so frisst er sich nach unten durch.
     */
    private void scan(int range) {

        for(int x = -range; x <= range; x++) {
            for(int z = -range; z <= range; z++) {

                BlockPos pos = new BlockPos(this.worldPosition.getX() + x, this.targetY, this.worldPosition.getZ() + z);
                BlockState state = this.level.getBlockState(pos);

                if(isLiquid(state)) continue;

                if(this.canBreak(state, pos)) {
                    this.targetX = pos.getX();
                    this.targetZ = pos.getZ();
                    this.beam = true;
                    return;
                }
            }
        }

        this.beam = false;
        this.targetY--;
    }

    /**
     * Ob das ein echter Fluessigkeitsblock ist.
     *
     * NICHT ueber den Fluidzustand gefragt: ein bewaesserter Zaun hat auch einen, ist aber ein
     * Zaun und soll gebrochen werden. Das Original prueft das Material; auf 1.21 ist die
     * Entsprechung der Blocktyp.
     */
    private static boolean isLiquid(BlockState state) {
        return state.getBlock() instanceof LiquidBlock;
    }

    private boolean canBreak(BlockState state, BlockPos pos) {
        if(state.isAir()) return false;
        if(isLiquid(state)) return false;
        if(state.is(Blocks.BEDROCK)) return false;
        return state.getDestroySpeed(this.level, pos) >= 0;
    }

    private double getBreakSpeed(BlockPos pos, int speed) {
        float hardness = this.level.getBlockState(pos).getDestroySpeed(this.level, pos) * 15 / speed;
        return hardness == 0 ? 1 : 1 / hardness;
    }

    /** Wo Fluessigkeit an das frische Loch grenzt, kommt ein Daemmblock hin. */
    private void buildDam(BlockPos pos) {
        for(Direction dir : Direction.values()) {
            BlockPos side = pos.relative(dir);
            if(isLiquid(this.level.getBlockState(side))) {
                this.level.setBlockAndUpdate(side, NtmBlocks.BARRICADE.get().defaultBlockState());
            }
        }
    }

    private void breakBlock(BlockPos pos, int fortune) {

        BlockState state = this.level.getBlockState(pos);
        ItemStack stack = new ItemStack(state.getBlock());

        boolean handled = false;

        if(!stack.isEmpty()) {

            if(this.hasUpgrade(NtmItems.UPGRADE_CRYSTALLIZER.get())) {

                CrystallizerRecipe recipe = CrystallizerRecipes.getOutput(stack, Fluids.PEROXIDE);
                if(recipe == null) recipe = CrystallizerRecipes.getOutput(stack, Fluids.SULFURIC_ACID);

                if(recipe != null) {
                    this.drop(pos, recipe.output.copy());
                    handled = true;
                }

            } else if(this.hasUpgrade(NtmItems.UPGRADE_CENTRIFUGE.get())) {

                for(ItemStack out : CentrifugeRecipes.getOutput(stack)) {
                    if(!out.isEmpty()) {
                        this.drop(pos, out.copy());
                        handled = true;
                    }
                }

            } else if(this.hasUpgrade(NtmItems.UPGRADE_SHREDDER.get())) {

                /* Der Port gibt bei fehlendem Rezept Staub zurueck statt null; beides ist hier
                 * "nichts Brauchbares" und laesst den Block normal zerfallen. */
                ItemStack out = ShredderRecipes.getShredderResult(stack);
                if(!out.isEmpty() && out.getItem() != NtmItems.SCRAPS.get() && out.getItem() != NtmItems.DUST.get()) {
                    this.drop(pos, out.copy());
                    handled = true;
                }

            } else if(this.hasUpgrade(NtmItems.UPGRADE_SMELTER.get())) {

                ItemStack out = this.getSmeltingResult(stack);
                if(!out.isEmpty()) {
                    this.drop(pos, out.copy());
                    handled = true;
                }
            }
        }

        if(handled) {
            /* Die Aufwertung hat das Erzeugnis schon abgelegt; der Block verschwindet ohne
             * eigenes Bruchgut. */
            this.level.removeBlock(pos, false);
        } else {
            /* Mit Glueck: das Bruchgut faellt so, als haette eine Spitzhacke mit Gluecksverzauberung
             * zugeschlagen. Das Original reicht die Stufe direkt an dropBlockAsItem weiter; auf
             * 1.21 fuehrt der Weg ueber das Werkzeug. */
            ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);

            if(fortune > 0) {
                tool.enchant(this.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.FORTUNE), fortune);
            }

            Block.dropResources(state, this.level, pos, this.level.getBlockEntity(pos), null, tool);
            this.level.removeBlock(pos, false);
        }

        this.suckDrops(pos);

        if(this.hasUpgrade(NtmItems.UPGRADE_SCREM.get())) {
            this.level.playSound(null, pos, net.minecraft.sounds.SoundEvents.GHAST_SCREAM,
                    net.minecraft.sounds.SoundSource.BLOCKS, 8F, 1F);
        }

        this.breakProgress = 0;
    }

    private ItemStack getSmeltingResult(ItemStack input) {
        if(input.isEmpty()) return ItemStack.EMPTY;
        return this.quickCheck.getRecipeFor(new SingleRecipeInput(input.copy()), this.level)
                .map(holder -> holder.value().getResultItem(this.level.registryAccess()).copy())
                .orElse(ItemStack.EMPTY);
    }

    private void drop(BlockPos pos, ItemStack stack) {
        this.level.addFreshEntity(new ItemEntity(this.level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
    }

    /** Was der Nullifikator wegwirft: Dreck, Stein, Kies und was sonst nur Platz kostet. */
    private static final Set<Item> WORTHLESS = Set.of(
            Blocks.DIRT.asItem(), Blocks.STONE.asItem(), Blocks.COBBLESTONE.asItem(),
            Blocks.SAND.asItem(), Blocks.SANDSTONE.asItem(), Blocks.GRAVEL.asItem(),
            Blocks.DEEPSLATE.asItem(), Blocks.COBBLED_DEEPSLATE.asItem(), Blocks.TUFF.asItem(),
            Items.FLINT, Items.SNOWBALL, Items.WHEAT_SEEDS);

    private void suckDrops(BlockPos pos) {

        boolean nullifier = this.hasUpgrade(NtmItems.UPGRADE_NULLIFIER.get());

        AABB area = new AABB(pos.getX() + 0.5 - 3, pos.getY() + 0.5 - 1, pos.getZ() + 0.5 - 3,
                pos.getX() + 0.5 + 3, pos.getY() + 0.5 + 1, pos.getZ() + 0.5 + 3);

        for(ItemEntity item : this.level.getEntitiesOfClass(ItemEntity.class, area)) {

            if(item.isRemoved()) continue;

            ItemStack stack = item.getItem();

            if(nullifier && WORTHLESS.contains(stack.getItem())) {
                item.discard();
                continue;
            }

            /* Oelschiefer wird nicht eingelagert, sondern verfluessigt. */
            if(stack.getItem() == NtmBlocks.ORE_OIL.get().asItem()) {
                this.tank.setTankType(Fluids.OIL);
                this.tank.setFill(Math.min(this.tank.getFill() + 500, this.tank.getMaxFill()));
                item.discard();
                continue;
            }

            ItemStack rest = InventoryUtil.tryAddItemToInventory(this.slots, SLOT_OUTPUT_START, SLOT_OUTPUT_END, stack.copy());

            if(rest.isEmpty()) item.discard();
            else item.setItem(rest);
        }

        AABB burn = new AABB(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5,
                pos.getX() + 1.5, pos.getY() + 1.5, pos.getZ() + 1.5);

        for(LivingEntity mob : this.level.getEntitiesOfClass(LivingEntity.class, burn)) mob.igniteForSeconds(5);
    }

    /** Alles, was in den Ausgabefaechern liegt, in einen Behaelter nebenan schieben. */
    private void tryFillContainer(int x, int y, int z) {

        BlockEntity be = this.level.getBlockEntity(new BlockPos(x, y, z));
        if(!(be instanceof Container container)) return;

        for(int i = SLOT_OUTPUT_START; i <= SLOT_OUTPUT_END; i++) {

            ItemStack stack = this.slots.get(i);
            if(stack.isEmpty()) continue;

            int before = stack.getCount();
            ItemStack rest = this.pushInto(container, stack.copy());
            this.slots.set(i, rest);

            if(rest.isEmpty() || rest.getCount() < before) return;
        }
    }

    /** Einen Stapel in einen fremden Behaelter legen; zurueck kommt, was nicht hineinpasste. */
    private ItemStack pushInto(Container container, ItemStack stack) {

        boolean moved = false;

        for(int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {

            ItemStack slot = container.getItem(i);

            if(slot.isEmpty()) {
                if(!container.canPlaceItem(i, stack)) continue;
                int transfer = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), container.getMaxStackSize()));
                container.setItem(i, stack.copyWithCount(transfer));
                stack.shrink(transfer);
                moved = true;

            } else if(ItemStack.isSameItemSameComponents(slot, stack)) {
                int transfer = Math.min(stack.getCount(), Math.min(slot.getMaxStackSize(), container.getMaxStackSize()) - slot.getCount());
                if(transfer <= 0) continue;
                slot.grow(transfer);
                stack.shrink(transfer);
                moved = true;
            }
        }

        if(moved) container.setChanged();
        return stack;
    }

    private boolean hasUpgrade(Item item) {
        for(int i = SLOT_UPGRADE_START; i <= SLOT_UPGRADE_END; i++) {
            if(this.slots.get(i).getItem() == item) return true;
        }
        return false;
    }

    /** Der Laser haelt an, wenn irgendeiner seiner fuenf Anschlussbloecke Rotstein bekommt. */
    private boolean isMultiblockRedstonePowered() {
        for(DirPos pos : this.getConPos()) {
            BlockPos inner = new BlockPos(pos.getX(), pos.getY(), pos.getZ()).relative(pos.getDir().getOpposite());
            if(this.level.hasNeighborSignal(inner)) return true;
        }
        return false;
    }

    private DirPos[] getConPos() {
        BlockPos p = this.worldPosition;
        return new DirPos[] {
                new DirPos(p.offset(2, 0, 0), Direction.EAST),
                new DirPos(p.offset(-2, 0, 0), Direction.WEST),
                new DirPos(p.offset(0, 0, 2), Direction.SOUTH),
                new DirPos(p.offset(0, 0, -2), Direction.NORTH)
        };
    }

    /** Die Kantenlaenge des abgeraeumten Schachts; die Oberflaeche zeigt sie an. */
    public int getWidth() {
        return 1 + this.getRange() * 2;
    }

    public int getRange() {
        int range = 1;
        for(int i = SLOT_UPGRADE_START; i <= SLOT_UPGRADE_END; i++) {
            Item item = this.slots.get(i).getItem();
            if(item == NtmItems.UPGRADE_EFFECT_1.get()) range += 2;
            else if(item == NtmItems.UPGRADE_EFFECT_2.get()) range += 4;
            else if(item == NtmItems.UPGRADE_EFFECT_3.get()) range += 6;
        }
        return Math.min(range, 25);
    }

    /* --- Bedienung --- */

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag data) {
        this.isOn = !this.isOn;
        this.setChanged();
    }

    /* --- Faecher --- */

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot <= SLOT_UPGRADE_END) return stack.getItem() instanceof MachineUpgradeItem;
        return false;
    }

    @Override public int[] getSlotsForFace(Direction direction) { return ACCESS; }

    private static final int[] ACCESS = access();

    private static int[] access() {
        int[] slots = new int[SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1];
        for(int i = 0; i < slots.length; i++) slots[i] = SLOT_OUTPUT_START + i;
        return slots;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= SLOT_OUTPUT_START && index <= SLOT_OUTPUT_END;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineMiningLaserMenu(id, inventory, this);
    }

    /* --- Strom und Fluid --- */

    @Override public void setPower(long power) { this.power = power; }
    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }

    /* --- Aufwertungen --- */

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.EFFECT || type == UpgradeType.POWER
                || type == UpgradeType.OVERDRIVE || type == UpgradeType.FORTUNE;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {

        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_MINING_LASER.get()));

        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_SPEED, "+" + (lvl * 100) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 100 / 16) + "%").withStyle(ChatFormatting.RED));
        }
        if(type == UpgradeType.EFFECT) {
            components.add(Component.translatable(KEY_RANGE, "+" + (lvl * 2) + "").withStyle(ChatFormatting.GREEN));
        }
        if(type == UpgradeType.POWER) {
            components.add(Component.translatable(KEY_CONSUMPTION, "-" + (lvl * 100 / 16) + "%").withStyle(ChatFormatting.GREEN));
        }
        if(type == UpgradeType.OVERDRIVE) {
            components.add(Component.translatable(KEY_SPEED, "+" + (lvl * 100) + "%").withStyle(ChatFormatting.GREEN));
        }
        if(type == UpgradeType.FORTUNE) {
            components.add(Component.translatable(KEY_FORTUNE, "+" + lvl).withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 12);
        upgrades.put(UpgradeType.EFFECT, 12);
        upgrades.put(UpgradeType.POWER, 12);
        upgrades.put(UpgradeType.OVERDRIVE, 3);
        upgrades.put(UpgradeType.FORTUNE, 3);
        return upgrades;
    }

    /* --- Speichern --- */

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.targetX);
        buf.writeInt(this.targetY);
        buf.writeInt(this.targetZ);
        buf.writeBoolean(this.beam);
        buf.writeBoolean(this.isOn);
        buf.writeBoolean(this.redstonePowered);
        buf.writeDouble(this.clientBreakProgress);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.targetX = buf.readInt();
        this.targetY = buf.readInt();
        this.targetZ = buf.readInt();
        this.beam = buf.readBoolean();
        this.isOn = buf.readBoolean();
        this.redstonePowered = buf.readBoolean();
        this.clientBreakProgress = buf.readDouble();
        this.tank.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.isOn = tag.getBoolean("isOn");
        this.targetX = tag.getInt("targetX");
        this.targetY = tag.getInt("targetY");
        this.targetZ = tag.getInt("targetZ");
        this.tank.readFromNBT(tag, "tank");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putBoolean("isOn", this.isOn);
        tag.putInt("targetX", this.targetX);
        tag.putInt("targetY", this.targetY);
        tag.putInt("targetZ", this.targetZ);
        this.tank.writeToNBT(tag, "tank");
    }

    /** Der Schacht kann tief sein; der Anzeigekasten reicht von der Maschine bis zum Weltgrund. */
    public AABB getRenderBoundingBox() {
        BlockPos p = this.worldPosition;
        int range = 25;
        Level lvl = this.level;
        int bottom = lvl == null ? p.getY() - 256 : lvl.getMinBuildHeight();
        return new AABB(p.getX() - range, bottom, p.getZ() - range, p.getX() + range + 1, p.getY() + 2, p.getZ() + range + 1);
    }
}
