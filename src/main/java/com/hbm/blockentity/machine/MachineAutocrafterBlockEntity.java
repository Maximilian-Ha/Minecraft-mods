package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.IControlReceiverFilter;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.menus.MachineAutocrafterMenu;
import com.hbm.lib.Library;
import com.hbm.module.ModulePatternMatcher;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineAutocrafter.
 *
 * Der Selbstbauer stellt Werkbankrezepte von selbst her. Man legt das Muster in die oberen neun
 * Faecher -- als Abbild, nicht als Gut --, und er sucht sich alle Rezepte, auf die dieses Muster
 * passt. Mehr als eines? Dann blaettert ein Klick durch.
 *
 * DIE OBEREN NEUN FAECHER SIND ZUGLEICH DER FILTER. Was in ein Zutatenfach darunter darf,
 * entscheidet das Muster darueber: der Mustervergleicher aus Runde 99, mit derselben
 * Vergleichsart wie ueberall. Damit laesst sich ein Rezept auch mit Zutaten fuettern, die
 * gleichwertig, aber nicht gleich sind -- Holz jeder Art etwa.
 *
 * ER SORTIERT SELBST EIN. Schiebt ein Band einen Stapel herein, geht er in das Fach, das ihn am
 * dringendsten braucht: das mit dem wenigsten davon. Ohne diese Regel liefe das erste passende
 * Fach voll und der Rest bliebe leer, und das Rezept kaeme nie zustande.
 *
 * HOECHSTENS VIER STUECK je Zutatenfach. Ein Selbstbauer, der vierundsechzig Stueck je Fach
 * hortet, bindet Material, das anderswo fehlt.
 *
 * NICHT UEBERNOMMEN: Gegenstaende mit Behaelter -- der Eimer, der beim Kuchenbacken
 * zurueckbleibt. Das Original gibt ihn ins Zutatenfach zurueck, wenn es leer geworden ist. Auf
 * 1.21 laeuft das ueber getRemainingItems des Rezepts, und das gibt die Reste fuer ALLE neun
 * Felder auf einmal zurueck; die Rueckgabe ist damit uebernommen, aber ueber den Griff, den
 * 1.21 dafuer vorsieht.
 */
public class MachineAutocrafterBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IControlReceiverFilter {

    /** 0-8 Muster, 9 Vorschau, 10-18 Zutaten, 19 Ausgabe, 20 Batterie. */
    public static final int PATTERNS = 9;
    public static final int SLOT_PREVIEW = 9;
    public static final int SLOT_INGREDIENTS = 10;
    public static final int SLOT_OUTPUT = 19;
    public static final int SLOT_BATTERY = 20;
    public static final int SLOTS = 21;

    /** HE je hergestelltem Gegenstand, und der Speicher dafuer. */
    public static final long CONSUMPTION = 100;
    public static final long MAX_POWER = CONSUMPTION * 100;

    /** Hoechstmenge je Zutatenfach -- siehe oben. */
    private static final int MAX_PER_INGREDIENT = 4;

    public long power;

    public final ModulePatternMatcher matcher = new ModulePatternMatcher(PATTERNS);

    /** Nur auf dem Server: die Rezepte, auf die das Muster passt. */
    private final List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

    public int recipeIndex;
    public int recipeCount;

    /** Nach dem Laden ist die Rezeptliste leer und muss einmal neu gesucht werden. */
    private boolean templateDirty = true;

    public MachineAutocrafterBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_AUTOCRAFTER.get(), pos, state, SLOTS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.autocrafter");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        /* Nach dem Laden einer Welt steht die Rezeptliste nicht mehr; sie wird einmal neu
         * gesucht, nicht bei jedem Tick. */
        if(this.templateDirty) {
            this.templateDirty = false;
            this.updateTemplate();
        }

        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, MAX_POWER);

        for(DirPos pos : this.getConPos()) this.trySubscribe(this.level, pos);

        if(!this.recipes.isEmpty() && this.power >= CONSUMPTION) this.craft();

        this.networkPackNT(15);
    }

    private void craft() {

        RecipeHolder<CraftingRecipe> holder = this.recipes.get(Math.min(this.recipeIndex, this.recipes.size() - 1));
        CraftingInput.Positioned positioned = this.gridAt(SLOT_INGREDIENTS);
        CraftingInput input = positioned.input();

        if(!holder.value().matches(input, this.level)) return;

        ItemStack result = holder.value().assemble(input, this.level.registryAccess());
        if(result.isEmpty()) return;

        ItemStack out = this.slots.get(SLOT_OUTPUT);

        if(out.isEmpty()) {
            this.slots.set(SLOT_OUTPUT, result.copy());
        } else if(ItemStack.isSameItemSameComponents(out, result)
                && out.getCount() + result.getCount() <= out.getMaxStackSize()) {
            out.grow(result.getCount());
        } else {
            return;
        }

        for(int i = 0; i < PATTERNS; i++) this.removeItem(SLOT_INGREDIENTS + i, 1);

        /* Die Reste -- leere Eimer und dergleichen -- gehen in ihr eigenes Zutatenfach zurueck.
         *
         * SIE ZAEHLEN IM BESCHNITTENEN GITTER, und das ist die Falle. Seit 1.21 schneidet
         * CraftingInput leere Randreihen weg, bevor es ein Rezept sucht; ein Rezept in der
         * unteren rechten Ecke des Dreiergitters wird so zu einem Einerfeld. Die Liste aus
         * getRemainingItems hat dann EIN Feld, nicht neun, und wer sie Feld fuer Feld auf die
         * Faecher legt, gibt den Eimer im falschen Fach zurueck. Wohin das Gitter geschoben
         * wurde, steht in Positioned. */
        var remaining = holder.value().getRemainingItems(input);

        for(int row = 0; row < input.height(); row++) {
            for(int col = 0; col < input.width(); col++) {

                int index = row * input.width() + col;
                if(index >= remaining.size()) continue;

                ItemStack rest = remaining.get(index);
                if(rest.isEmpty()) continue;

                int slot = SLOT_INGREDIENTS + (positioned.top() + row) * 3 + positioned.left() + col;
                if(this.slots.get(slot).isEmpty()) this.slots.set(slot, rest.copy());
            }
        }

        this.power -= CONSUMPTION;
        this.setChanged();
    }

    /** Die neun Faecher ab "from" als Werkbankgitter, samt der Verschiebung aus dem Beschnitt. */
    private CraftingInput.Positioned gridAt(int from) {

        List<ItemStack> grid = new ArrayList<>(PATTERNS);
        for(int i = 0; i < PATTERNS; i++) grid.add(this.slots.get(from + i));

        return CraftingInput.ofPositioned(3, 3, grid);
    }

    /**
     * Sucht alle Rezepte, auf die das Muster passt, und zeigt das erste als Vorschau. Wird
     * gerufen, sobald sich ein Musterfach aendert.
     */
    public void updateTemplate() {

        if(this.level == null || this.level.isClientSide) return;

        this.recipes.clear();
        this.recipes.addAll(this.level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, this.gridAt(0).input(), this.level));

        this.recipeCount = this.recipes.size();
        if(this.recipeIndex >= this.recipeCount) this.recipeIndex = 0;

        this.showPreview();
    }

    /** Blaettert zum naechsten passenden Rezept. */
    public void nextTemplate() {

        if(this.level == null || this.level.isClientSide || this.recipes.isEmpty()) return;

        this.recipeIndex = (this.recipeIndex + 1) % this.recipes.size();
        this.showPreview();
    }

    private void showPreview() {

        if(this.recipes.isEmpty()) {
            this.slots.set(SLOT_PREVIEW, ItemStack.EMPTY);
        } else {
            this.slots.set(SLOT_PREVIEW,
                    this.recipes.get(this.recipeIndex).value().assemble(this.gridAt(0).input(), this.level.registryAccess()));
        }

        this.setChanged();
    }

    protected DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(this.worldPosition.east(), Direction.EAST),
                new DirPos(this.worldPosition.west(), Direction.WEST),
                new DirPos(this.worldPosition.south(), Direction.SOUTH),
                new DirPos(this.worldPosition.north(), Direction.NORTH),
                new DirPos(this.worldPosition.above(), Direction.UP),
                new DirPos(this.worldPosition.below(), Direction.DOWN)
        };
    }

    // ------------------------------------------------------------------------------------
    // Was von aussen hinein darf
    // ------------------------------------------------------------------------------------

    @Override public int[] getSlotsForFace(Direction direction) { return ACCESS; }

    /** Herausnehmen darf man die Ausgabe immer, eine Zutat nur, wenn kein Muster sie verlangt. */
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {

        if(index == SLOT_OUTPUT) return true;
        if(index < SLOT_INGREDIENTS || index >= SLOT_OUTPUT) return false;

        int pattern = index - SLOT_INGREDIENTS;
        ItemStack filter = this.slots.get(pattern);

        if(filter.isEmpty() || this.matcher.modes[pattern] == null) return true;

        return !this.matcher.isValidForFilter(filter, pattern, stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    /**
     * Die Einsortierregel. Sie entscheidet nicht nur, OB ein Gegenstand in dieses Fach darf,
     * sondern auch, ob ein anderes Fach ihn noetiger hat.
     */
    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {

        /* Ab 1.21 fragt der Slot selbst hier nach -- ohne diese Zeile liesse sich keine Batterie
         * mehr von Hand einlegen. Automatik sieht davon nichts: getSlotsForFace gibt nur die
         * Zutaten und die Ausgabe heraus. */
        if(index == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;

        if(index < SLOT_INGREDIENTS || index >= SLOT_OUTPUT) return false;

        int pattern = index - SLOT_INGREDIENTS;
        if(this.slots.get(pattern).isEmpty()) return false;

        if(stack.getCount() > MAX_PER_INGREDIENT) return false;
        if(this.slots.get(index).getCount() + stack.getCount() > MAX_PER_INGREDIENT) return false;

        /* Alle Faecher sammeln, in die dieser Gegenstand ueberhaupt passt. */
        List<Integer> valid = new ArrayList<>();

        for(int i = 0; i < PATTERNS; i++) {

            ItemStack filter = this.slots.get(i);
            if(filter.isEmpty() || this.matcher.modes[i] == null) continue;
            if(!this.matcher.isValidForFilter(filter, i, stack)) continue;

            valid.add(SLOT_INGREDIENTS + i);

            /* Passt das angefragte Fach und ist es leer, ist die Sache entschieden. */
            if(SLOT_INGREDIENTS + i == index && this.slots.get(index).isEmpty()) return true;
        }

        if(!valid.contains(index)) return false;

        int here = this.slots.get(index).getCount();

        for(int other : valid) {

            ItemStack in = this.slots.get(other);

            /* Ein leeres Fach hat immer Vorrang vor einem, in dem schon etwas liegt. */
            if(in.isEmpty()) return false;
            if(!ItemStack.isSameItemSameComponents(in, stack)) continue;
            if(in.getCount() < here) return false;
        }

        return true;
    }

    private static final int[] ACCESS = access();

    private static int[] access() {
        int[] slots = new int[10];
        for(int i = 0; i < 10; i++) slots[i] = SLOT_INGREDIENTS + i;
        return slots;
    }

    // ------------------------------------------------------------------------------------
    // Filter und Steuerung
    // ------------------------------------------------------------------------------------

    @Override public int[] getFilterSlots() { return new int[] { 0, PATTERNS }; }

    @Override
    public void nextMode(int i) {
        this.matcher.nextMode(this.level, this.slots.get(i), i);
        this.updateTemplate();
    }

    /** Wie beim Verteiler die kluge Voreinstellung: ein Barren meint die Barren, nicht diesen. */
    @Override
    public void initPattern(int i) {
        this.matcher.initPatternSmart(this.level, this.slots.get(i), i);
        this.updateTemplate();
    }

    @Override
    public boolean hasPermission(Player player) {
        return this.stillValid(player);
    }

    /**
     * Der Selbstbauer hat keine Schaltflaeche: seine beiden Eingriffe -- Vergleichsart
     * weiterschalten und Rezept blaettern -- sind Klicks auf Faecher und laufen deshalb ueber
     * das Menue, nicht ueber ein Steuerpaket. Die Methode steht hier nur, weil die Schnittstelle
     * sie verlangt.
     */
    @Override
    public void receiveControl(CompoundTag data) { }

    @Override public boolean canConnect(Direction dir) { return true; }

    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return MAX_POWER; }
    @Override public void setPower(long power) { this.power = power; }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineAutocrafterMenu(id, inventory, this);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        this.matcher.serialize(buf);
        buf.writeInt(this.recipeCount);
        buf.writeInt(this.recipeIndex);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.matcher.deserialize(buf);
        this.recipeCount = buf.readInt();
        this.recipeIndex = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.recipeIndex = tag.getInt("recipeIndex");
        this.matcher.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putInt("recipeIndex", this.recipeIndex);
        this.matcher.save(tag);
    }
}
