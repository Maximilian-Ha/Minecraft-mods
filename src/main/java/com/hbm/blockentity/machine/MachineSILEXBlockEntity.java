package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineSILEXMenu;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.inventory.recipes.SILEXRecipes.Output;
import com.hbm.inventory.recipes.SILEXRecipes.SILEXRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.FelCrystalItem.Wellenlaenge;
import com.hbm.util.ItemStackUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntitySILEX.
 *
 * Die Maschine zur Isotopentrennung durch Laseranregung. Sie loest ihren Eingang in Peroxid auf
 * und schiesst dann Licht hinein; was sich dabei abscheidet, steht in SILEXRecipes.
 *
 * SIE BRAUCHT EINEN FEL. Von sich aus hat sie keinen Strahl -- ihr Feld mode steht jeden Tick
 * wieder auf NULL, und nur ein FEL ueber ihr setzt es neu. Ohne ihn laufen nur Rezepte, die
 * ueberhaupt keine Wellenlaenge verlangen, und das sind keine.
 *
 * DIE AUSGABE WIRD NICHT GEWUERFELT. Ein Zaehler springt nach jedem Ausgang um 137 weiter und
 * wird auf die Summe der Gewichte umgebrochen; ueber viele Durchgaenge trifft er damit genau
 * die Anteile des Rezepts. Das ist der Grund, warum die Zahlen in den Rezepten Zusagen sind.
 *
 * ABWEICHUNG: das Original haelt den aktuellen Eingang als ComparableStack und schickt dessen
 * Zahlen-ID ueber das Netz. Auf 1.21 gibt es keine stabilen Zahlen-IDs mehr; der Port schickt
 * den Anmeldenamen.
 */
public class MachineSILEXBlockEntity extends MachineBaseBlockEntity implements IFluidStandardReceiverMK2 {

    /** 0 Eingang, 1 Fluidkennung, 2/3 Behaelterwechsel, 4 frische Ausgabe, 5 bis 10 Warteschlange. */
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FLUID_ID = 1;
    public static final int SLOT_CONTAINER_IN = 2;
    public static final int SLOT_CONTAINER_OUT = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int SLOT_QUEUE = 5;
    public static final int QUEUE_SIZE = 6;
    public static final int SLOTS = 11;

    /** Der Eingang und die sechs Warteschlangenfaecher sind von aussen erreichbar. */
    private static final int[] SLOT_IO = new int[] { 0, 5, 6, 7, 8, 9, 10 };

    public static final int MAX_FILL = 16_000;
    public static final int PROCESS_TIME = 100;
    /** Der Schritt des Ausgabezaehlers. Eine Primzahl, damit er jede Stelle der Leiter trifft. */
    public static final int PRIME = 137;

    /** Wird jeden Tick auf NULL zurueckgesetzt und vom FEL neu gesetzt. */
    public Wellenlaenge mode = Wellenlaenge.NULL;
    public final FluidTank tank;

    /** Was gerade geloest in der Maschine steht, und wieviel davon. */
    public ComparableStack current;
    public int currentFill;
    public int progress;
    public int recipeIndex;

    private int loadDelay;

    public MachineSILEXBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_SILEX.get(), pos, state, SLOTS);
        this.tank = new FluidTank(Fluids.PEROXIDE, MAX_FILL);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineSILEX");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.tank.setType(SLOT_FLUID_ID, this.slots);
        this.tank.loadTank(this.level, SLOT_CONTAINER_IN, SLOT_CONTAINER_OUT, this.slots);

        for(DirPos pos : this.getConPos()) this.trySubscribe(this.tank.getTankType(), this.level, pos);

        this.loadFluid();

        if(!this.process()) this.progress = 0;

        this.dequeue();

        if(this.currentFill <= 0) this.current = null;

        this.networkPackNT(50);

        /* Zuletzt: der Modus gilt immer nur fuer den Tick, in dem der FEL ihn gesetzt hat. */
        this.mode = Wellenlaenge.NULL;
    }

    /**
     * Die beiden Anschluesse liegen quer zur Blickrichtung, zwei Bloecke zur Seite und einen
     * ueber dem Kern -- dort, wo im Modell die Rohre sitzen.
     */
    public DirPos[] getConPos() {

        BlockState state = this.getBlockState();
        if(!state.hasProperty(DummyableBlock.FACING)) return new DirPos[0];

        Direction rot = state.getValue(DummyableBlock.FACING).getClockWise();
        BlockPos pos = this.worldPosition.above();

        return new DirPos[] {
                new DirPos(pos.relative(rot, 2), rot),
                new DirPos(pos.relative(rot.getOpposite(), 2), rot.getOpposite())
        };
    }

    /**
     * Sechs Fluessigkeiten gehen unmittelbar in die Maschine, ohne den Umweg ueber einen
     * Gegenstand: drei ueber die Zuordnung unten (sie fahren das Rezept eines Barrens), drei
     * ueber ein eigenes Rezept. Alles andere muss als Gegenstand ins Fach 0 und loest sich
     * dort in Peroxid.
     */
    private void loadFluid() {

        ComparableStack conv = acceptsFluid(this.tank.getTankType()) ? fluidStack(this.tank.getTankType()) : null;

        if(conv != null) {

            if(this.currentFill == 0) this.current = conv.copy();

            if(this.current != null && this.current.equals(conv)) {
                int toFill = Math.min(50, Math.min(MAX_FILL - this.currentFill, this.tank.getFill()));
                this.currentFill += toFill;
                this.tank.setFill(this.tank.getFill() - toFill);
            }
        }

        this.loadDelay++;
        if(this.loadDelay > 20) this.loadDelay = 0;

        if(this.loadDelay != 0) return;

        ItemStack input = this.slots.get(SLOT_INPUT);
        if(input.isEmpty() || this.tank.getTankType() != Fluids.PEROXIDE) return;
        if(this.current != null && !this.current.equals(new ComparableStack(input).makeSingular())) return;

        SILEXRecipe recipe = SILEXRecipes.getOutput(input);
        if(recipe == null) return;

        int load = recipe.fluidProduced;

        if(load <= MAX_FILL - this.currentFill && load <= this.tank.getFill()) {
            this.currentFill += load;
            this.current = new ComparableStack(input).makeSingular();
            this.tank.setFill(this.tank.getFill() - load);
            this.removeItem(SLOT_INPUT, 1);
        }
    }

    /**
     * Ein Durchgang. Das Tempo haengt am Abstand zwischen Strahl und Rezept: gleiche
     * Wellenlaenge ist ein Schritt je Tick, jede Stufe darueber verdoppelt ihn.
     */
    private boolean process() {

        if(this.current == null || this.currentFill <= 0) return false;

        SILEXRecipe recipe = SILEXRecipes.getOutput(this.current.toStack());

        if(recipe == null) return false;
        if(recipe.laserStrength.ordinal() > this.mode.ordinal()) return false;
        if(this.currentFill < recipe.fluidConsumed) return false;
        if(!this.slots.get(SLOT_OUTPUT).isEmpty()) return false;

        this.progress += (int) Math.pow(2, this.mode.ordinal() - recipe.laserStrength.ordinal() + 1) / 2;

        if(this.progress < PROCESS_TIME) return true;

        this.currentFill -= recipe.fluidConsumed;

        int totalWeight = 0;
        for(Output output : recipe.outputs) totalWeight += output.weight();
        this.recipeIndex %= Math.max(totalWeight, 1);

        int weight = 0;
        for(Output output : recipe.outputs) {
            weight += output.weight();

            if(this.recipeIndex < weight) {
                this.slots.set(SLOT_OUTPUT, output.stack().copy());
                break;
            }
        }

        this.progress = 0;
        this.recipeIndex += PRIME;
        this.setChanged();

        return true;
    }

    /**
     * Die frische Ausgabe wandert in die Warteschlange: erst auf einen passenden Stapel, sonst
     * in das erste leere Fach. Passt nichts, bleibt sie liegen und die Maschine steht.
     */
    private void dequeue() {

        ItemStack output = this.slots.get(SLOT_OUTPUT);
        if(output.isEmpty()) return;

        for(int i = SLOT_QUEUE; i < SLOT_QUEUE + QUEUE_SIZE; i++) {
            ItemStack slot = this.slots.get(i);

            if(!slot.isEmpty() && slot.getCount() < slot.getMaxStackSize() && ItemStackUtil.areStacksCompatible(output, slot)) {
                slot.grow(1);
                this.removeItem(SLOT_OUTPUT, 1);
                return;
            }
        }

        for(int i = SLOT_QUEUE; i < SLOT_QUEUE + QUEUE_SIZE; i++) {
            if(this.slots.get(i).isEmpty()) {
                this.slots.set(i, output.copy());
                this.slots.set(SLOT_OUTPUT, ItemStack.EMPTY);
                return;
            }
        }
    }

    /** Der Knopf im Fenster: was geloest ist, wird weggekippt. */
    public void voidContents() {
        this.currentFill = 0;
        this.current = null;
    }

    public int getProgressScaled(int i) { return this.progress * i / PROCESS_TIME; }
    public int getFluidScaled(int i) { return this.tank.getFill() * i / this.tank.getMaxFill(); }
    public int getFillScaled(int i) { return this.currentFill * i / MAX_FILL; }

    /**
     * Ob diese Fluessigkeit sich unmittelbar einleiten laesst. Das Fenster faerbt danach den
     * Balken, und loadFluid entscheidet danach, ob es fuellt.
     *
     * GEMESSEN: das Original fuehrt hier zusaetzlich eine Tabelle mit UF6, PUF6 und Todesloesung
     * und fragt erst DIESE, ehe es nach einem Rezept sucht. Die Tabelle bildet jede der drei
     * Fluessigkeiten auf ihr eigenes Sinnbild ab -- also auf genau das, was der zweite Zweig
     * ohnehin bildet. Und ein Rezept findet sich fuer alle drei: fuer die beiden Hexafluoride
     * ueber die Uebersetzung auf ihren Barren, fuer die Todesloesung unmittelbar. Die Tabelle
     * aendert damit nichts und steht hier nicht.
     */
    public static boolean acceptsFluid(FluidType type) {
        return SILEXRecipes.getOutput(fluidStack(type).toStack()) != null;
    }

    /** Das Sinnbild einer Fluessigkeit -- SILEXRecipes schlaegt genau danach nach. */
    public static ComparableStack fluidStack(FluidType type) {
        return new ComparableStack(NtmItems.FLUID_ICON.get(), 1, type.getID());
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_INPUT) return SILEXRecipes.getOutput(stack) != null;
        return slot == SLOT_FLUID_ID || slot == SLOT_CONTAINER_IN;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= SLOT_QUEUE;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return SLOT_IO;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tank.readFromNBT(tag, "tank");
        this.currentFill = tag.getInt("fill");
        this.progress = tag.getInt("progress");
        this.recipeIndex = tag.getInt("recipeIndex");
        this.mode = readMode(tag.getString("mode"));
        this.current = this.currentFill > 0 ? readStack(tag.getString("item"), tag.getInt("meta")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.tank.writeToNBT(tag, "tank");
        tag.putInt("fill", this.currentFill);
        tag.putInt("progress", this.progress);
        tag.putInt("recipeIndex", this.recipeIndex);
        tag.putString("mode", this.mode.name());

        if(this.current != null) {
            tag.putString("item", BuiltInRegistries.ITEM.getKey(this.current.item).toString());
            tag.putInt("meta", this.current.meta);
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.currentFill);
        buf.writeInt(this.progress);
        buf.writeUtf(this.mode.name());
        this.tank.serialize(buf);

        buf.writeBoolean(this.current != null);
        if(this.current != null) {
            buf.writeUtf(BuiltInRegistries.ITEM.getKey(this.current.item).toString());
            buf.writeInt(this.current.meta);
        }
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.currentFill = buf.readInt();
        this.progress = buf.readInt();
        this.mode = readMode(buf.readUtf());
        this.tank.deserialize(buf);

        this.current = buf.readBoolean() ? readStack(buf.readUtf(), buf.readInt()) : null;
    }

    /** Ein unbekannter Name darf die Maschine nicht zerlegen -- dann steht sie eben still. */
    private static Wellenlaenge readMode(String name) {
        for(Wellenlaenge wellenlaenge : Wellenlaenge.values()) {
            if(wellenlaenge.name().equals(name)) return wellenlaenge;
        }
        return Wellenlaenge.NULL;
    }

    private static ComparableStack readStack(String name, int meta) {
        ResourceLocation location = ResourceLocation.tryParse(name);
        if(location == null) return null;
        Item item = BuiltInRegistries.ITEM.get(location);
        return new ComparableStack(MetaHelper.newStack(item, 1, meta));
    }

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        return new AABB(x - 1, y, z - 1, x + 2, y + 3, z + 2);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineSILEXMenu(id, inventory, this);
    }
}
