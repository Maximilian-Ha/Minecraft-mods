package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyProviderMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.menus.MachineRadGenMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.special.NuclearWasteItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineRadGen.
 *
 * Der Radiothermalgenerator macht aus Zerfallswaerme Strom -- ohne Kuehlung, ohne Aufwertungen,
 * ohne Schalter. Man legt Abfall hinein und holt Jahre spaeter den Rest wieder heraus.
 *
 * ZWOELF BAHNEN, DIE UNABHAENGIG VONEINANDER LAUFEN. Jede hat ein Eingabe- und ein
 * Ausgabefach, eigenen Fortschritt und eigene Leistung; die Anlage zaehlt am Ende nur zusammen.
 * Das ist der eigentliche Reiz: zwoelf verschiedene Brennstoffe gleichzeitig, jeder mit eigener
 * Laufzeit.
 *
 * DIE EINGABE VERTEILT SICH VON SELBST. Die Fachpruefung laesst nur in das Fach einlegen, das
 * nicht mehr hat als jedes andere -- wer einen Stapel hineinschiebt, fuellt damit alle zwoelf
 * Bahnen gleichmaessig, statt eine zu verstopfen.
 *
 * DIE LEISTUNG IST DAS UMGEKEHRTE DER LAUFZEIT. Der Edelstein bringt 25.000 je Tick und ist nach
 * einer halben Stunde durch; langlebiger Abfall bringt 500 und laeuft zwei Stunden. Schrott ist
 * mit 50 der Bodensatz -- und der einzige Brennstoff, der im Port schon eine Quelle hat.
 *
 * NICHT UEBERNOMMEN: die Anbindung an Energy Control und das Modell des Originals. Die
 * Abmessungen stimmen.
 */
public class MachineRadGenBlockEntity extends MachineBaseBlockEntity implements IEnergyProviderMK2 {

    /** Zwoelf Bahnen: Fach 0-11 nehmen an, 12-23 geben aus. */
    public static final int LANES = 12;
    public static final int SLOTS = 24;

    public static final long maxPower = 1_000_000;

    public int[] progress = new int[LANES];
    public int[] maxProgress = new int[LANES];
    public int[] production = new int[LANES];
    public ItemStack[] processing = new ItemStack[LANES];

    /** Was diesen Tick erzeugt wurde -- nur fuer die Anzeige. */
    protected int output;

    public long power;
    public boolean isOn = false;

    private static final int[] ACCESS = new int[SLOTS];
    static { for(int i = 0; i < SLOTS; i++) ACCESS[i] = i; }

    private AABB renderBox;

    public MachineRadGenBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_RAD_GEN.get(), pos, state, SLOTS);
        for(int i = 0; i < LANES; i++) this.processing[i] = ItemStack.EMPTY;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.radGen");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.output = 0;

        Direction dir = this.getDir();
        this.tryProvide(this.level, this.worldPosition.relative(dir, -4), dir.getOpposite());

        /* Nachladen: eine Bahn zieht nur nach, wenn ihr Ausgabefach das Ergebnis auch aufnimmt. */
        for(int i = 0; i < LANES; i++) {

            if(!this.processing[i].isEmpty()) continue;

            ItemStack in = this.slots.get(i);
            if(in.isEmpty() || getDurationFromItem(in) <= 0) continue;
            if(!this.outputFits(getOutputFromItem(in), this.slots.get(i + LANES))) continue;

            this.progress[i] = 0;
            this.maxProgress[i] = getDurationFromItem(in);
            this.production[i] = getPowerFromItem(in);
            this.processing[i] = in.copyWithCount(1);
            this.removeItem(i, 1);
            this.setChanged();
        }

        this.isOn = false;

        for(int i = 0; i < LANES; i++) {

            if(this.processing[i].isEmpty()) continue;

            this.isOn = true;
            this.power += this.production[i];
            this.output += this.production[i];
            this.progress[i]++;

            if(this.progress[i] >= this.maxProgress[i]) {

                this.progress[i] = 0;
                ItemStack out = getOutputFromItem(this.processing[i]);

                if(!out.isEmpty()) {
                    ItemStack held = this.slots.get(i + LANES);
                    if(held.isEmpty()) this.slots.set(i + LANES, out);
                    else held.grow(out.getCount());
                }

                this.processing[i] = ItemStack.EMPTY;
                this.setChanged();
            }
        }

        if(this.power > maxPower) this.power = maxPower;

        this.networkPackNT(50);
    }

    private boolean outputFits(ItemStack out, ItemStack held) {
        if(out.isEmpty() || held.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(held, out) && held.getCount() + out.getCount() <= held.getMaxStackSize();
    }

    public Direction getDir() {
        BlockState state = this.getBlockState();
        return state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(int i = 0; i < LANES; i++) buf.writeInt(this.progress[i]);
        for(int i = 0; i < LANES; i++) buf.writeInt(this.maxProgress[i]);
        for(int i = 0; i < LANES; i++) buf.writeInt(this.production[i]);
        buf.writeLong(this.power);
        buf.writeBoolean(this.isOn);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(int i = 0; i < LANES; i++) this.progress[i] = buf.readInt();
        for(int i = 0; i < LANES; i++) this.maxProgress[i] = buf.readInt();
        for(int i = 0; i < LANES; i++) this.production[i] = buf.readInt();
        this.power = buf.readLong();
        this.isOn = buf.readBoolean();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        super.loadAdditional(tag, registries);

        int[] progress = tag.getIntArray("progress");

        /* Ein Spielstand aus einer Zeit mit anderer Bahnzahl waere sonst ein Absturz. */
        if(progress.length != LANES) {
            this.progress = new int[LANES];
            this.maxProgress = new int[LANES];
            this.production = new int[LANES];
            return;
        }

        this.progress = progress;
        this.maxProgress = padded(tag.getIntArray("maxProgress"));
        this.production = padded(tag.getIntArray("production"));
        this.power = tag.getLong("power");
        this.isOn = tag.getBoolean("isOn");

        for(int i = 0; i < LANES; i++) this.processing[i] = ItemStack.EMPTY;

        ListTag list = tag.getList("progressing", Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("slot") & 255;
            if(slot < LANES) this.processing[slot] = ItemStack.parse(registries, entry).orElse(ItemStack.EMPTY);
        }
    }

    private static int[] padded(int[] array) {
        if(array.length == LANES) return array;
        int[] out = new int[LANES];
        System.arraycopy(array, 0, out, 0, Math.min(array.length, LANES));
        return out;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        super.saveAdditional(tag, registries);

        tag.putIntArray("progress", this.progress);
        tag.putIntArray("maxProgress", this.maxProgress);
        tag.putIntArray("production", this.production);
        tag.putLong("power", this.power);
        tag.putBoolean("isOn", this.isOn);

        ListTag list = new ListTag();
        for(int i = 0; i < LANES; i++) {
            if(this.processing[i].isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putByte("slot", (byte) i);
            list.add(this.processing[i].save(registries, entry));
        }
        tag.put("progressing", list);
    }

    /**
     * Hinein darf nur, was die Bahnen gleichmaessig fuellt: ein Fach nimmt nichts an, solange
     * eine andere Bahn mit demselben Brennstoff weniger hat. So verteilt sich ein eingeschobener
     * Stapel von selbst auf alle zwoelf.
     */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {

        if(slot >= LANES || getDurationFromItem(stack) <= 0) return false;
        if(this.slots.get(slot).isEmpty()) return true;

        int size = this.slots.get(slot).getCount();

        for(int i = 0; i < LANES; i++) {
            ItemStack other = this.slots.get(i);
            if(other.isEmpty()) return false;
            if(ItemStack.isSameItemSameComponents(other, stack) && other.getCount() < size) return false;
        }

        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot >= LANES;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return ACCESS;
    }

    /* --- Die Brennstofftafel --- */

    /** Was ein Brennstoff bringt: Leistung je Tick, Laufzeit in Ticks, und was uebrig bleibt. */
    public record Fuel(int power, int duration, ItemStack output) { }

    public static final Map<ComparableStack, Fuel> fuels = new HashMap<>();

    public static void registerFuels() {

        fuels.clear();

        /* Kurzlebiger Abfall: viel Leistung, halbe Stunde. Das Zehntel laeuft ein Zehntel lang. */
        for(int i = 0; i < NuclearWasteItem.WasteClass.SHORT.length; i++) {
            fuels.put(new ComparableStack(NtmItems.NUCLEAR_WASTE_SHORT.get(), 1, i),
                    new Fuel(1_500, 30 * 60 * 20, meta(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED.get(), i)));
            fuels.put(new ComparableStack(NtmItems.NUCLEAR_WASTE_SHORT_TINY.get(), 1, i),
                    new Fuel(150, 3 * 60 * 20, meta(NtmItems.NUCLEAR_WASTE_SHORT_DEPLETED_TINY.get(), i)));
        }

        /* Langlebiger Abfall: ein Drittel der Leistung, dafuer vier Mal so lange. */
        for(int i = 0; i < NuclearWasteItem.WasteClass.LONG.length; i++) {
            fuels.put(new ComparableStack(NtmItems.NUCLEAR_WASTE_LONG.get(), 1, i),
                    new Fuel(500, 2 * 60 * 60 * 20, meta(NtmItems.NUCLEAR_WASTE_LONG_DEPLETED.get(), i)));
            fuels.put(new ComparableStack(NtmItems.NUCLEAR_WASTE_LONG_TINY.get(), 1, i),
                    new Fuel(50, 12 * 60 * 20, meta(NtmItems.NUCLEAR_WASTE_LONG_DEPLETED_TINY.get(), i)));
        }

        /* Schrott verbrennt restlos -- er laesst nichts zurueck. */
        fuels.put(new ComparableStack(NtmItems.SCRAP_NUCLEAR.get()),
                new Fuel(50, 5 * 60 * 20, ItemStack.EMPTY));

        /* Der Edelstein ist der Ausreisser: fuenfhundertmal die Leistung von Schrott, und am
         * Ende liegt ein Diamant darin. */
        fuels.put(new ComparableStack(NtmItems.GEM_RAD.get()),
                new Fuel(25_000, 30 * 60 * 20, new ItemStack(Items.DIAMOND)));
    }

    private static ItemStack meta(net.minecraft.world.item.Item item, int meta) {
        return com.hbm.inventory.MetaHelper.newStack(item, 1, meta);
    }

    private static Fuel grabResult(ItemStack stack) {
        if(stack.isEmpty()) return null;
        return fuels.get(new ComparableStack(stack).makeSingular());
    }

    public static int getPowerFromItem(ItemStack stack) {
        Fuel fuel = grabResult(stack);
        return fuel == null ? 0 : fuel.power();
    }

    public static int getDurationFromItem(ItemStack stack) {
        Fuel fuel = grabResult(stack);
        return fuel == null ? 0 : fuel.duration();
    }

    public static ItemStack getOutputFromItem(ItemStack stack) {
        Fuel fuel = grabResult(stack);
        return fuel == null ? ItemStack.EMPTY : fuel.output().copy();
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 4, p.getY(), p.getZ() - 4, p.getX() + 5, p.getY() + 4, p.getZ() + 5);
        }
        return this.renderBox;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineRadGenMenu(id, inventory, this);
    }
}
