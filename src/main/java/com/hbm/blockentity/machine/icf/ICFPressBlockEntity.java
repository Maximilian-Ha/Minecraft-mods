package com.hbm.blockentity.machine.icf;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.menus.ICFPressMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.ICFPelletItem;
import com.hbm.items.machine.ICFPelletItem.EnumICFFuel;
import com.hbm.items.machine.IItemFluidIdentifier;
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
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityICFPress.
 *
 * Die Presse formt aus einer leeren Huelle und ZWEI Stoffen ein Brennstoffkuegelchen der
 * Traegheitsfusion. Jeder der beiden Stoffe kommt entweder aus einem Tank oder aus einem
 * Barren; der Tank hat Vorrang. Beide muessen da sein und sie duerfen nicht derselbe sein.
 *
 * Ein Myon im Fach haelt sechzehn Pressungen lang vor und kennzeichnet die Kuegelchen als
 * myonenkatalysiert -- das viertelt spaeter die noetige Laserstaerke.
 *
 * ABWEICHUNG: das Original fuehrt das Merkzeichen, welcher der beiden Stoffe aus dem Tank kam,
 * in einem STATISCHEN Feld -- alle Pressen der Welt teilen es sich. Hier ist es ein gewoehnliches
 * Feld der Blockentitaet.
 *
 * ABWEICHUNG: isItemValidForSlot des Originals laesst Fluidkennungen nur in die Barrenfaecher.
 * Das faellt dort nicht auf, weil die Oberflaeche auf 1.7.10 gar nicht nachfragt und nur Trichter
 * die Pruefung sehen. Auf 1.21 fragt das Fach nach, also brauchen die Kennungsfaecher hier einen
 * eigenen Zweig -- sonst liessen sie sich von Hand nicht befuellen.
 */
public class ICFPressBlockEntity extends MachineBaseBlockEntity implements IFluidStandardReceiverMK2, IFluidCopiable {

    public static final int SLOT_PELLET_IN = 0;
    public static final int SLOT_PELLET_OUT = 1;
    public static final int SLOT_MUON_IN = 2;
    public static final int SLOT_MUON_OUT = 3;
    public static final int SLOT_MATERIAL_FIRST = 4;
    public static final int SLOT_MATERIAL_SECOND = 5;
    public static final int SLOT_FLUID_ID_FIRST = 6;
    public static final int SLOT_FLUID_ID_SECOND = 7;

    /** Wie viele Pressungen ein einzelnes Myon traegt. */
    public static final int MAX_MUON = 16;

    public final FluidTank[] tanks = new FluidTank[2];
    public int muon;

    /** Je Stoff: kam er aus dem Tank oder aus dem Barrenfach? */
    private final boolean[] usedFluid = new boolean[2];

    public ICFPressBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.ICF_PRESS.get(), pos, state, 8);

        this.tanks[0] = new FluidTank(Fluids.DEUTERIUM, 16_000);
        this.tanks[1] = new FluidTank(Fluids.TRITIUM, 16_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineICFPress");
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        this.tanks[0].setType(SLOT_FLUID_ID_FIRST, this.slots);
        this.tanks[1].setType(SLOT_FLUID_ID_SECOND, this.slots);

        if(this.level.getGameTime() % 20 == 0) {
            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
                this.trySubscribe(this.tanks[1].getTankType(), this.level, pos);
            }
        }

        this.absorbMuon();
        this.press();

        this.networkPackNT(15);
    }

    /** Ein Myon wird verbraucht, sobald das letzte aufgezehrt ist -- und seine Huelle faellt an. */
    private void absorbMuon() {

        if(this.muon > 0) return;

        ItemStack stack = this.slots.get(SLOT_MUON_IN);
        if(stack.isEmpty() || stack.getItem() != NtmItems.PARTICLE_MUON.get()) return;

        ItemStack container = stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem().copy() : ItemStack.EMPTY;
        ItemStack out = this.slots.get(SLOT_MUON_OUT);
        boolean canStore = false;

        if(container.isEmpty()) {
            canStore = true;
        } else if(out.isEmpty()) {
            this.slots.set(SLOT_MUON_OUT, container);
            canStore = true;
        } else if(ItemStack.isSameItemSameComponents(out, container) && out.getCount() < out.getMaxStackSize()) {
            out.grow(1);
            canStore = true;
        }

        if(canStore) {
            this.muon = MAX_MUON;
            this.removeItem(SLOT_MUON_IN, 1);
            this.setChanged();
        }
    }

    public void press() {

        if(this.slots.get(SLOT_PELLET_IN).getItem() != NtmItems.ICF_PELLET_EMPTY.get()) return;
        if(!this.slots.get(SLOT_PELLET_OUT).isEmpty()) return;

        ICFPelletItem.init();

        EnumICFFuel fuel1 = this.getFuel(this.tanks[0], this.slots.get(SLOT_MATERIAL_FIRST), 0);
        EnumICFFuel fuel2 = this.getFuel(this.tanks[1], this.slots.get(SLOT_MATERIAL_SECOND), 1);

        if(fuel1 == null || fuel2 == null || fuel1 == fuel2) return;

        this.slots.set(SLOT_PELLET_OUT, ICFPelletItem.setup(new ItemStack(NtmItems.ICF_PELLET.get()), fuel1, fuel2, this.muon > 0));

        if(this.muon > 0) this.muon--;

        this.removeItem(SLOT_PELLET_IN, 1);

        if(this.usedFluid[0]) this.tanks[0].setFill(this.tanks[0].getFill() - 1_000);
        else this.removeItem(SLOT_MATERIAL_FIRST, 1);

        if(this.usedFluid[1]) this.tanks[1].setFill(this.tanks[1].getFill() - 1_000);
        else this.removeItem(SLOT_MATERIAL_SECOND, 1);

        this.setChanged();
    }

    /**
     * Woraus der Stoff kommt: zuerst der Tank, dann das Barrenfach. Der Barren muss genau ein
     * Werkstoff in genau Barrenmenge sein -- Bloecke und Nuggets zaehlen nicht.
     */
    public EnumICFFuel getFuel(FluidTank tank, ItemStack slot, int index) {

        this.usedFluid[index] = false;

        if(tank.getFill() >= 1_000 && ICFPelletItem.FLUID_MAP.containsKey(tank.getTankType())) {
            this.usedFluid[index] = true;
            return ICFPelletItem.FLUID_MAP.get(tank.getTankType());
        }

        if(slot.isEmpty()) return null;

        List<MaterialStack> mats = Mats.getMaterialsFromItem(slot);
        if(mats == null || mats.size() != 1) return null;

        MaterialStack mat = mats.get(0);
        if(mat.amount != MaterialShapes.INGOT.q(1)) return null;

        return ICFPelletItem.MATERIAL_MAP.get(mat.material);
    }

    public DirPos[] getConPos() {

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        DirPos[] pos = new DirPos[6];
        int i = 0;

        for(Direction dir : Direction.values()) {
            pos[i++] = new DirPos(x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ(), dir);
        }

        return pos;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeByte((byte) this.muon);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.muon = buf.readByte();
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tanks[0].readFromNBT(tag, "t0");
        this.tanks[1].readFromNBT(tag, "t1");
        this.muon = tag.getByte("muon");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.tanks[0].writeToNBT(tag, "t0");
        this.tanks[1].writeToNBT(tag, "t1");
        tag.putByte("muon", (byte) this.muon);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(stack.getItem() == NtmItems.ICF_PELLET_EMPTY.get()) return slot == SLOT_PELLET_IN;
        if(stack.getItem() == NtmItems.PARTICLE_MUON.get()) return slot == SLOT_MUON_IN;
        if(stack.getItem() instanceof IItemFluidIdentifier) return slot == SLOT_FLUID_ID_FIRST || slot == SLOT_FLUID_ID_SECOND;
        return slot == SLOT_MATERIAL_FIRST || slot == SLOT_MATERIAL_SECOND;
    }

    /** Barren gehen oben und unten ins erste, an den Seiten ins zweite Fach -- so im Original. */
    public static final int[] TOP_BOTTOM = new int[] {0, 1, 2, 3, 4};
    public static final int[] SIDES = new int[] {0, 1, 2, 3, 5};

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction.getAxis() == Direction.Axis.Y ? TOP_BOTTOM : SIDES;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_PELLET_OUT || index == SLOT_MUON_OUT;
    }

    @Override public FluidTank[] getAllTanks() { return this.tanks; }
    @Override public FluidTank[] getReceivingTanks() { return this.tanks; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ICFPressMenu(id, inventory, this);
    }
}
