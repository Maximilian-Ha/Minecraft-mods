package com.hbm.blockentity.network;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.CraneBaseBlock;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.menus.CraneInserterMenu;
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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityCraneInserter.
 *
 * Der Einleger nimmt an, was vom Band in ihn hineinfaehrt, und schiebt es in die Maschine an
 * seiner Ausgangsseite. Was dort gerade keinen Platz findet, bleibt in seinen einundzwanzig
 * eigenen Faechern liegen und wird bei jedem Takt erneut angeboten.
 *
 * Bleibt auch dafuer kein Platz, entscheidet der SCHALTER "destroyer": ist er an -- so steht
 * der Einleger im Original voreingestellt da --, verschwindet der Gegenstand; ist er aus, faellt
 * er als Beute zu Boden. Das klingt hart, ist aber Absicht: eine Strecke, die schneller liefert,
 * als die Maschine verbraucht, wuerde sonst den Boden mit Beute zupflastern.
 *
 * Liegt Redstone an, ruht der Einleger.
 *
 * ABWEICHUNG: das Original spricht die Nachbarmaschine ueber IInventory und ISidedInventory an
 * und baut sich die erlaubten Faecher selbst zusammen. Auf 1.21 laeuft das ueber die
 * Item-Capability -- damit erreicht der Einleger auch Kisten und Maschinen fremder Mods, was
 * dort nicht ging.
 */
public class CraneInserterBlockEntity extends MachineBaseBlockEntity implements IControlReceiver {

    public static final int SLOTS = 21;

    /** Ist er an, wird vernichtet, was nirgends mehr hinpasst; ist er aus, faellt es zu Boden. */
    public boolean destroyer = true;

    public CraneInserterBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CRANE_INSERTER.get(), pos, state, SLOTS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.craneInserter");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(!this.level.hasNeighborSignal(this.worldPosition)) this.pushOne();

        this.networkPackNT(15);
    }

    /**
     * Ein Stapel je Takt. Geht der ganze nicht, wird es mit einem einzelnen Stueck versucht --
     * das Original tut dasselbe, weil manche Maschinen nur einzeln annehmen.
     */
    private void pushOne() {

        IItemHandler target = this.getTarget();
        if(target == null) return;

        for(int i = 0; i < this.slots.size(); i++) {

            ItemStack stack = this.slots.get(i);
            if(stack.isEmpty()) continue;

            ItemStack rest = ItemHandlerHelper.insertItemStacked(target, stack.copy(), false);

            if(rest.getCount() != stack.getCount()) {
                this.slots.set(i, rest);
                this.setChanged();
                return;
            }
        }

        for(int i = 0; i < this.slots.size(); i++) {

            ItemStack stack = this.slots.get(i);
            if(stack.isEmpty()) continue;

            ItemStack single = stack.copyWithCount(1);

            if(ItemHandlerHelper.insertItemStacked(target, single, false).isEmpty()) {
                this.removeItem(i, 1);
                this.setChanged();
                return;
            }
        }
    }

    /** Das Inventar an der Ausgangsseite, von der Seite her gesehen, die dem Einleger zugewandt ist. */
    @Nullable
    public IItemHandler getTarget() {

        if(this.level == null) return null;

        Direction output = CraneBaseBlock.getOutput(this.getBlockState());
        return this.level.getCapability(Capabilities.ItemHandler.BLOCK, this.worldPosition.relative(output), output.getOpposite());
    }

    /**
     * Was vom Band kommt, legt der Block selbst hier ab. Passt nichts mehr hinein, bleibt ein
     * Rest uebrig, den der Aufrufer entsorgt.
     */
    public ItemStack storeOverflow(ItemStack stack) {

        for(int pass = 0; pass < 2; pass++) {
            for(int i = 0; i < this.slots.size() && !stack.isEmpty(); i++) {

                ItemStack slot = this.slots.get(i);

                if(pass == 0 && !slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack)) {
                    int room = Math.min(slot.getMaxStackSize(), this.getMaxStackSize(slot)) - slot.getCount();
                    int moved = Math.min(room, stack.getCount());
                    if(moved <= 0) continue;
                    slot.grow(moved);
                    stack.shrink(moved);
                } else if(pass == 1 && slot.isEmpty()) {
                    int moved = Math.min(this.getMaxStackSize(stack), stack.getCount());
                    this.slots.set(i, stack.copyWithCount(moved));
                    stack.shrink(moved);
                }
            }
        }

        this.setChanged();
        return stack;
    }

    @Override public int[] getSlotsForFace(Direction direction) { return ALL_SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) { return true; }

    private static final int[] ALL_SLOTS = allSlots();

    private static int[] allSlots() {
        int[] slots = new int[SLOTS];
        for(int i = 0; i < SLOTS; i++) slots[i] = i;
        return slots;
    }

    @Override
    public boolean hasPermission(Player player) {
        return this.stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag data) {
        if(data.contains("destroyer")) this.destroyer = !this.destroyer;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CraneInserterMenu(id, inventory, this);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.destroyer);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.destroyer = buf.readBoolean();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.destroyer = tag.getBoolean("destroyer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("destroyer", this.destroyer);
    }
}
