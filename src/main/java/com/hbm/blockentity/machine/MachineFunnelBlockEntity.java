package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.menus.MachineFunnelMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineFunnel.
 *
 * Der Trichter packt zusammen, was zu ihm kommt: neun Barren werden ein Block, vier Bretter
 * werden ein Werktisch. Er sucht sich das Rezept selbst -- man stellt ihm nichts ein, man
 * schuettet nur hinein.
 *
 * ER IST DER DRITTE ABNEHMER DER REZEPTSUCHE nach dem Selbstbauer. Dort sucht ein Muster seine
 * Rezepte; hier sucht der Trichter, ob ein Gut ueberhaupt eines hat, das nur aus ihm selbst
 * besteht. Was kein solches Rezept hat, laesst er gar nicht erst herein.
 *
 * NEUN EINGAENGE OBEN, NEUN AUSGAENGE UNTEN, und die stehen fest zueinander: was in Fach drei
 * hineingeht, kommt in Fach zwoelf heraus. So bleiben verschiedene Gueter getrennt, statt sich
 * in einem gemeinsamen Ausgang zu stapeln.
 *
 * DREI BETRIEBSARTEN: erst neun, dann vier -- oder nur neun -- oder nur vier. Die mittlere
 * braucht man fuer Gueter, die beides koennen: Lehmklumpen etwa werden zu viert ein Lehmblock,
 * zu neunt aber nichts. Ein Klick auf den Schalter blaettert durch.
 *
 * DIE SUCHE WIRD GEMERKT, und das ist kein Luxus: ohne den Merkzettel liefe bei jedem Tick fuer
 * jedes der neun Faecher die ganze Rezeptliste durch. Das Original haelt denselben Merkzettel,
 * mit derselben Begruendung.
 */
public class MachineFunnelBlockEntity extends MachineBaseBlockEntity implements IControlReceiver {

    /** Betriebsarten: beides, nur das Dreiergitter, nur das Zweiergitter. */
    public static final int MODE_ALL = 0;
    public static final int MODE_3x3 = 1;
    public static final int MODE_2x2 = 2;
    public static final int MODES = 3;

    public static final int SLOTS_PER_ROW = 9;
    public static final int SLOTS = SLOTS_PER_ROW * 2;

    public int mode = MODE_ALL;

    /* Der Merkzettel. Ein leerer Stapel darin steht fuer "es gibt kein solches Rezept".
     *
     * ER GEHOERT DER EINZELNEN MASCHINE, nicht der ganzen Welt. Das Original haelt ihn statisch,
     * und das ginge hier auch -- Rezepte sind ueberall dieselben. Aber im Einzelspieler laufen
     * Server und Abbild im selben Prozess, und beide fragen hier an; eine gemeinsame HashMap
     * haetten dann zwei Faeden zugleich in der Hand. Je Maschine stehen ohnehin nur eine
     * Handvoll Gueter darin. */
    private final Map<CacheKey, ItemStack> from4 = new HashMap<>();
    private final Map<CacheKey, ItemStack> from9 = new HashMap<>();

    /* Wie viele Rezepte es zuletzt gab. Aendert sich die Zahl, hat jemand die Datenpakete neu
     * geladen, und der Merkzettel gilt nicht mehr. Das Original kennt diese Pruefung nicht -- auf
     * 1.7.10 stehen die Rezepte fest, sobald das Spiel laeuft. */
    private int knownRecipeCount = -1;

    /** Schluessel des Merkzettels: der Gegenstand ohne Anzahl, aber mit seinen Bestandteilen. */
    private record CacheKey(Item item, DataComponentPatch components) { }

    public MachineFunnelBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_FUNNEL.get(), pos, state, SLOTS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineFunnel");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        for(int i = 0; i < SLOTS_PER_ROW; i++) {

            ItemStack in = this.slots.get(i);
            if(in.isEmpty()) continue;

            /* Erst das grosse Gitter versuchen, dann das kleine -- neun Barren sollen ein Block
             * werden und nicht vier Mal nichts. */
            int count = 9;
            ItemStack compressed = (this.mode == MODE_2x2 || in.getCount() < 9) ? ItemStack.EMPTY : this.getFrom9(in);

            if(compressed.isEmpty()) {
                compressed = (this.mode == MODE_3x3 || in.getCount() < 4) ? ItemStack.EMPTY : this.getFrom4(in);
                count = 4;
            }

            if(compressed.isEmpty() || in.getCount() < count) continue;

            ItemStack out = this.slots.get(i + SLOTS_PER_ROW);

            if(out.isEmpty()) {
                this.slots.set(i + SLOTS_PER_ROW, compressed.copy());
                this.removeItem(i, count);
                this.setChanged();
            } else if(ItemStack.isSameItemSameComponents(out, compressed)
                    && out.getCount() + compressed.getCount() <= out.getMaxStackSize()) {
                out.grow(compressed.getCount());
                this.removeItem(i, count);
                this.setChanged();
            }
        }

        this.networkPackNT(15);
    }

    /** Das Ergebnis eines Rezepts aus vier gleichen Guetern, oder ein leerer Stapel. */
    public ItemStack getFrom4(ItemStack ingredient) {
        return this.lookUp(this.from4, ingredient, 2);
    }

    /** Das Ergebnis eines Rezepts aus neun gleichen Guetern, oder ein leerer Stapel. */
    public ItemStack getFrom9(ItemStack ingredient) {
        return this.lookUp(this.from9, ingredient, 3);
    }

    private ItemStack lookUp(Map<CacheKey, ItemStack> cache, ItemStack ingredient, int size) {

        int count = this.level.getRecipeManager().getRecipes().size();

        if(count != this.knownRecipeCount) {
            this.knownRecipeCount = count;
            this.from4.clear();
            this.from9.clear();
        }

        CacheKey key = new CacheKey(ingredient.getItem(), ingredient.getComponentsPatch());

        ItemStack known = cache.get(key);
        if(known != null) return known.copy();

        List<ItemStack> grid = new ArrayList<>(size * size);
        for(int i = 0; i < size * size; i++) grid.add(ingredient.copyWithCount(1));

        CraftingInput input = CraftingInput.of(size, size, grid);
        List<RecipeHolder<CraftingRecipe>> found = this.level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, input, this.level);

        ItemStack match = found.isEmpty() ? ItemStack.EMPTY : found.get(0).value().assemble(input, this.level.registryAccess());

        cache.put(key, match.copy());
        return match;
    }

    /** Oben herein, unten heraus. */
    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction == Direction.DOWN ? OUTPUTS : INPUTS;
    }

    private static final int[] INPUTS = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
    private static final int[] OUTPUTS = { 9, 10, 11, 12, 13, 14, 15, 16, 17 };

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN ? index >= SLOTS_PER_ROW : index < SLOTS_PER_ROW;
    }

    /**
     * Herein darf nur, was sich auch zusammenpacken laesst. Steht im Fach schon etwas, wird nicht
     * noch einmal gefragt -- dann ist die Frage bereits beantwortet.
     */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {

        if(slot >= SLOTS_PER_ROW) return false;
        if(!this.slots.get(slot).isEmpty()) return true;
        if(this.level == null) return false;

        return !this.getFrom9(stack).isEmpty() || !this.getFrom4(stack).isEmpty();
    }

    @Override
    public boolean hasPermission(Player player) {
        return this.stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag data) {
        this.mode = (this.mode + 1) % MODES;
        this.setChanged();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineFunnelMenu(id, inventory, this);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.mode);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.mode = buf.readInt();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mode", this.mode);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.mode = tag.getInt("mode");
    }
}
