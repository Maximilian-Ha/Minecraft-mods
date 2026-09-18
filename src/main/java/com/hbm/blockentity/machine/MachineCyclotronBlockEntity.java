package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IConditionalInvAccess;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineCyclotronMenu;
import com.hbm.inventory.recipes.CyclotronRecipes;
import com.hbm.inventory.recipes.CyclotronRecipes.Result;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineCyclotron.
 *
 * Das Zyklotron beschiesst ein Pulver mit einem Geschoss und macht daraus ein anderes Element.
 * Es ist der Weg zu allem, was man nicht aus dem Boden holt: Kobalt, Niob, Astat, Tennessin,
 * Australium -- und am Ende Schrabidium.
 *
 * DREI BAHNEN NEBENEINANDER. Fach null bis zwei nehmen die Geschosse, drei bis fuenf die Ziele,
 * sechs bis acht geben aus; jede Bahn arbeitet fuer sich. Ein Durchgang bedient alle drei auf
 * einmal, kostet aber nur einmal Strom -- wer nur eine Bahn belegt, verschenkt zwei Drittel.
 *
 * JEDER SCHUSS ERZEUGT ANTIMATERIE, und das ist der eigentliche Ertrag. Sie sammelt sich im
 * dritten Tank und wird ueber ein Rohr abgeholt.
 *
 * SIE BRAUCHT KUEHLWASSER und gibt Abdampf ab. Wieviel, haengt an den Aufwertungen: Tempo
 * erhoeht den Verbrauch, Wirkung senkt ihn.
 *
 * DIE ANSCHLUESSE LIEGEN AN DEN VIER SEITEN, je zwei, und die Seite entscheidet, welche Bahn ein
 * Trichter erreicht -- links die erste, mittig die zweite, rechts die dritte. Ausgeben duerfen
 * alle drei ueberall.
 *
 * BEIM PORTIEREN GEFUNDEN: das Original will beim Einsetzen einer Aufwertung ein Geraeusch
 * abspielen und prueft dafuer die Faecher vierzehn und fuenfzehn. Die Maschine hat aber nur
 * zwoelf -- die Aufwertungen liegen in zehn und elf. Das Geraeusch erklingt im Original also
 * nie. Hier stehen die richtigen Nummern.
 *
 * NICHT UEBERNOMMEN: die vier Stecker, die das Original als Ostereier vorsieht (Hoellenfeuer,
 * Buch, Hammer, Muenze), die Anbindung an Energy Control und das Modell. Die Abmessungen
 * stimmen: fuenf mal fuenf Bloecke, drei hoch. Der Anzeigekasten reicht eine Lage hoeher, weil
 * das Modell des Originals dort hinausragt.
 */
public class MachineCyclotronBlockEntity extends MachineBaseBlockEntity
        implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IConditionalInvAccess {

    /** Drei Bahnen: Geschoss, Ziel, Ausgabe. */
    public static final int LANES = 3;
    public static final int SLOT_PARTICLE = 0;
    public static final int SLOT_INGREDIENT = 3;
    public static final int SLOT_OUTPUT = 6;
    public static final int SLOT_BATTERY = 9;
    public static final int SLOT_UPGRADE_START = 10;
    public static final int SLOT_UPGRADE_END = 11;
    public static final int SLOTS = 12;

    public static final long maxPower = 100_000_000L;
    public static final int consumption = 1_000_000;
    public static final int duration = 690;

    public long power;
    public int progress;

    /* Die vier Sockel als Bitmuster. Einmal gesteckt, bleibt ein Stecker drin -- das Original
     * kennt kein Herausnehmen. Der Darsteller liest das ueber getPlug(). */
    private byte plugs;

    public final FluidTank[] tanks = new FluidTank[3];

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    private AABB renderBox;

    public MachineCyclotronBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_CYCLOTRON.get(), pos, state, SLOTS);

        this.tanks[0] = new FluidTank(Fluids.WATER, 32_000);
        this.tanks[1] = new FluidTank(Fluids.SPENTSTEAM, 32_000);
        this.tanks[2] = new FluidTank(Fluids.AMAT, 8_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.cyclotron");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, maxPower);

        /* Ersatz fuer autoPort() aus 1.7.10: Strom und Wasser abonnieren, Abdampf und
         * Antimaterie anbieten. */
        for(DirPos pos : this.getConPos()) {
            this.trySubscribe(this.level, pos);
            this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
            if(this.tanks[1].getFill() > 0) this.tryProvide(this.tanks[1], this.level, pos);
            if(this.tanks[2].getFill() > 0) this.tryProvide(this.tanks[2], this.level, pos);
        }

        this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE_START, SLOT_UPGRADE_END);

        if(this.canProcess()) {

            this.progress += this.getSpeed();
            this.power -= this.getConsumption();

            int convert = this.getCoolantConsumption();
            this.tanks[0].setFill(this.tanks[0].getFill() - convert);
            this.tanks[1].setFill(this.tanks[1].getFill() + convert);

            if(this.progress >= duration) {
                this.process();
                this.progress = 0;
                this.setChanged();
            }

        } else {
            this.progress = 0;
        }

        this.networkPackNT(25);
    }

    public boolean canProcess() {

        if(this.power < this.getConsumption()) return false;

        int convert = this.getCoolantConsumption();

        if(this.tanks[0].getFill() < convert) return false;
        if(this.tanks[1].getFill() + convert > this.tanks[1].getMaxFill()) return false;

        for(int i = 0; i < LANES; i++) if(this.laneResult(i) != null) return true;

        return false;
    }

    /** Das Ergebnis einer Bahn, sofern es eines gibt UND es ins Ausgabefach passt. */
    private Result laneResult(int lane) {

        Result result = CyclotronRecipes.getOutput(this.slots.get(SLOT_INGREDIENT + lane), this.slots.get(SLOT_PARTICLE + lane));
        if(result == null || result.output().isEmpty()) return null;

        ItemStack out = this.slots.get(SLOT_OUTPUT + lane);
        if(out.isEmpty()) return result;

        if(ItemStack.isSameItemSameComponents(out, result.output()) && out.getCount() < out.getMaxStackSize()) return result;

        return null;
    }

    public void process() {

        for(int i = 0; i < LANES; i++) {

            Result result = this.laneResult(i);
            if(result == null) continue;

            this.removeItem(SLOT_PARTICLE + i, 1);
            this.removeItem(SLOT_INGREDIENT + i, 1);

            ItemStack out = this.slots.get(SLOT_OUTPUT + i);
            if(out.isEmpty()) this.slots.set(SLOT_OUTPUT + i, result.output().copy());
            else out.grow(1);

            this.tanks[2].setFill(this.tanks[2].getFill() + result.antimatter());
        }

        if(this.tanks[2].getFill() > this.tanks[2].getMaxFill()) this.tanks[2].setFill(this.tanks[2].getMaxFill());
    }

    public int getSpeed() {
        return this.upgradeManager.getLevel(UpgradeType.SPEED) + 1;
    }

    public int getConsumption() {
        return consumption - 100_000 * this.upgradeManager.getLevel(UpgradeType.POWER);
    }

    /** Ein halber kleiner Kuehlturm; Tempo treibt ihn hoch, Wirkung drueckt ihn. */
    public int getCoolantConsumption() {
        return 500 / (this.upgradeManager.getLevel(UpgradeType.EFFECT) + 1) * this.getSpeed();
    }

    public DirPos[] getConPos() {

        BlockPos p = this.worldPosition;

        return new DirPos[] {
                new DirPos(p.offset(3, 0, 1), Direction.EAST),
                new DirPos(p.offset(3, 0, -1), Direction.EAST),
                new DirPos(p.offset(-3, 0, 1), Direction.WEST),
                new DirPos(p.offset(-3, 0, -1), Direction.WEST),
                new DirPos(p.offset(1, 0, 3), Direction.SOUTH),
                new DirPos(p.offset(-1, 0, 3), Direction.SOUTH),
                new DirPos(p.offset(1, 0, -3), Direction.NORTH),
                new DirPos(p.offset(-1, 0, -3), Direction.NORTH)
        };
    }

    /* --- Faecher --- */

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) return stack.getItem() instanceof MachineUpgradeItem;
        return this.isItemValidForSlot(this.worldPosition, slot, stack);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {

        super.setItem(slot, stack);

        /* Das Geraeusch beim Einsetzen einer Aufwertung. Das Original prueft hier die Faecher
         * vierzehn und fuenfzehn, die es gar nicht gibt -- siehe Klassenkommentar. */
        if(this.level != null && !this.level.isClientSide && !stack.isEmpty()
                && slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END
                && stack.getItem() instanceof MachineUpgradeItem) {

            this.level.playSound(null, this.worldPosition.above(), NtmSoundEvents.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.5F, 1F);
        }
    }

    @Override
    public boolean isItemValidForSlot(BlockPos pos, int slot, ItemStack stack) {
        if(slot < SLOT_INGREDIENT) return CyclotronRecipes.isParticle(stack);
        if(slot < SLOT_OUTPUT) return CyclotronRecipes.isIngredient(stack);
        return false;
    }

    @Override
    public boolean canExtractItem(BlockPos pos, int slot, ItemStack stack, Direction side) {
        return slot >= SLOT_OUTPUT && slot < SLOT_BATTERY;
    }

    /**
     * Welche Bahn ein Anschluss bedient, haengt daran, wo er steht: aussen links die erste,
     * mittig die zweite, aussen rechts die dritte. Ausgeben duerfen alle drei ueberall.
     */
    @Override
    public int[] getAccessibleSlotsFromSide(BlockPos pos, Direction side) {

        BlockPos core = this.worldPosition;

        for(Direction dir : Direction.Plane.HORIZONTAL) {

            Direction rot = dir.getClockWise();
            BlockPos middle = core.relative(dir, 2);

            if(pos.equals(middle.relative(rot))) return LANE_0;
            if(pos.equals(middle)) return LANE_1;
            if(pos.equals(middle.relative(rot.getOpposite()))) return LANE_2;
        }

        return OUTPUTS_ONLY;
    }

    private static final int[] LANE_0 = { 0, 3, 6, 7, 8 };
    private static final int[] LANE_1 = { 1, 4, 6, 7, 8 };
    private static final int[] LANE_2 = { 2, 5, 6, 7, 8 };
    private static final int[] OUTPUTS_ONLY = { 6, 7, 8 };

    @Override public int[] getSlotsForFace(Direction direction) { return OUTPUTS_ONLY; }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= SLOT_OUTPUT && index < SLOT_BATTERY;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineCyclotronMenu(id, inventory, this);
    }

    /* --- Strom und Fluid --- */

    @Override public void setPower(long power) { this.power = power; }
    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tanks[0] }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.tanks[1], this.tanks[2] }; }
    @Override public FluidTank[] getAllTanks() { return this.tanks; }

    /* --- Aufwertungen --- */

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.EFFECT;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {

        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_CYCLOTRON.get()));

        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_DELAY, "-" + (100 - 100 / (lvl + 1)) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_COOLANT_CONSUMPTION, "+" + (lvl * 100) + "%").withStyle(ChatFormatting.RED));
        }
        if(type == UpgradeType.POWER) {
            components.add(Component.translatable(KEY_CONSUMPTION, "-" + (lvl * 10) + "%").withStyle(ChatFormatting.GREEN));
        }
        if(type == UpgradeType.EFFECT) {
            components.add(Component.translatable(KEY_COOLANT_CONSUMPTION, "-" + (100 - 100 / (lvl + 1)) + "%").withStyle(ChatFormatting.GREEN));
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

    /* --- Speichern --- */

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.progress);
        buf.writeByte(this.plugs);
        for(FluidTank tank : this.tanks) tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.progress = buf.readInt();
        this.plugs = buf.readByte();
        for(FluidTank tank : this.tanks) tank.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].readFromNBT(tag, "t" + i);
        this.progress = tag.getInt("progress");
        this.power = tag.getLong("power");
        this.plugs = tag.getByte("plugs");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].writeToNBT(tag, "t" + i);
        tag.putInt("progress", this.progress);
        tag.putLong("power", this.power);
        tag.putByte("plugs", this.plugs);
    }

    /* --- Die vier Sockel --- */

    public void setPlug(int index) {
        this.plugs |= (byte) (1 << index);
        this.setChanged();
    }

    public boolean getPlug(int index) {
        return (this.plugs & (1 << index)) > 0;
    }

    /** Was in welchen Sockel gehoert. Reihenfolge wie im Original. */
    public static Item getItemForPlug(int index) {
        return switch(index) {
            case 0 -> NtmItems.POWDER_BALEFIRE.get();
            case 1 -> NtmItems.BOOK_OF_.get();
            case 2 -> NtmItems.DIAMOND_GAVEL.get();
            case 3 -> NtmItems.COIN_MASKMAN.get();
            default -> null;
        };
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 2, p.getY(), p.getZ() - 2, p.getX() + 3, p.getY() + 4, p.getZ() + 3);
        }
        return this.renderBox;
    }
}
