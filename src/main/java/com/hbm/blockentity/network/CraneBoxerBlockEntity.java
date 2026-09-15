package com.hbm.blockentity.network;

import api.hbm.conveyor.IConveyorBelt;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.CraneBaseBlock;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.item.MovingPackage;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.menus.CraneBoxerMenu;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityCraneBoxer.
 *
 * Der Packer sammelt, was ihm das Band bringt, und schnuert daraus ein Paket, sobald genug
 * beisammen ist. Damit faehrt eine Kistenladung als EINE Entitaet ueber die Strecke statt als
 * einundzwanzig -- das ist sein ganzer Sinn.
 *
 * VIER BETRIEBSARTEN, die ein Klick auf den Schalter durchschaltet:
 *   vier, acht und sechzehn VOLLE Stapel -- gezaehlt werden nur ganze, halbe bleiben liegen --,
 *   und "auf Redstone", wo bei jeder steigenden Flanke alles verpackt wird, was gerade da ist,
 *   ob voll oder nicht.
 *
 * Er verpackt nur, wenn an seiner Ausgangsseite wirklich ein Band liegt. Sonst bleibt alles in
 * den Faechern -- ein Paket ins Leere zu setzen hiesse, es fallen zu lassen.
 */
public class CraneBoxerBlockEntity extends MachineBaseBlockEntity implements IControlReceiver {

    /** Drei Reihen zu sieben, wie im Original. */
    public static final int SLOTS = 7 * 3;

    public static final byte MODE_4 = 0;
    public static final byte MODE_8 = 1;
    public static final byte MODE_16 = 2;
    public static final byte MODE_REDSTONE = 3;
    public static final byte MODES = 4;

    public byte mode = MODE_4;

    private boolean lastRedstone = false;

    public CraneBoxerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CRANE_BOXER.get(), pos, state, SLOTS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.craneBoxer");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        boolean redstone = this.level.hasNeighborSignal(this.worldPosition);

        if(this.mode == MODE_REDSTONE) {
            /* Nur die STEIGENDE Flanke zaehlt -- sonst schnuerte er bei liegendem Signal
             * jeden Tick ein neues Paket. */
            if(redstone && !this.lastRedstone) this.pack(0);
        } else if(this.level.getGameTime() % 2 == 0) {
            this.pack(this.requiredStacks());
        }

        this.lastRedstone = redstone;

        this.networkPackNT(15);
    }

    private int requiredStacks() {
        return switch(this.mode) {
            case MODE_8 -> 8;
            case MODE_16 -> 16;
            default -> 4;
        };
    }

    /**
     * Schnuert ein Paket. Ist required groesser als null, kommen nur VOLLE Stapel hinein und es
     * muessen mindestens so viele sein; bei null wandert alles hinein, was da ist.
     */
    private void pack(int required) {

        Direction output = CraneBaseBlock.getOutput(this.getBlockState());
        BlockPos beltPos = this.worldPosition.relative(output);

        if(!(this.level.getBlockState(beltPos).getBlock() instanceof IConveyorBelt belt)) return;

        /* Gesammelt werden die FACHNUMMERN, nicht die Stapel: zwei Faecher koennen denselben
         * Inhalt haben, und ueber den Inhalt zurueckzufinden waere nicht eindeutig. */
        List<Integer> picked = new ArrayList<>();

        for(int i = 0; i < this.slots.size(); i++) {

            ItemStack stack = this.slots.get(i);
            if(stack.isEmpty()) continue;
            if(required > 0 && stack.getCount() < stack.getMaxStackSize()) continue;

            picked.add(i);
            if(required > 0 && picked.size() == required) break;
        }

        if(picked.isEmpty()) return;
        if(required > 0 && picked.size() < required) return;

        /* Erst jetzt wird geleert -- vorher stand nicht fest, ob ueberhaupt gepackt wird. */
        ItemStack[] box = new ItemStack[picked.size()];

        for(int i = 0; i < picked.size(); i++) {
            int slot = picked.get(i);
            box[i] = this.slots.get(slot).copy();
            this.slots.set(slot, ItemStack.EMPTY);
        }

        this.setChanged();

        Vec3 mouth = new Vec3(
                this.worldPosition.getX() + 0.5 + output.getStepX() * 0.55,
                this.worldPosition.getY() + 0.5 + output.getStepY() * 0.55,
                this.worldPosition.getZ() + 0.5 + output.getStepZ() * 0.55);

        Vec3 snap = belt.getClosestSnappingPosition(this.level, beltPos, mouth);

        MovingPackage moving = new MovingPackage(NtmEntityTypes.MOVING_PACKAGE.get(), this.level);
        moving.setItemStacks(box);
        moving.moveTo(snap.x, snap.y, snap.z, 0F, 0F);

        this.level.addFreshEntity(moving);
    }

    /**
     * Was vom Band kommt, legt der Block hier ab. Was nicht mehr hineinpasst, bleibt uebrig und
     * wird vom Aufrufer entsorgt.
     */
    public ItemStack store(ItemStack stack) {

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
        if(data.contains("toggle")) {
            this.mode = (byte) ((this.mode + 1) % MODES);
            this.setChanged();
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CraneBoxerMenu(id, inventory, this);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeByte(this.mode);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.mode = buf.readByte();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.mode = tag.getByte("mode");
        this.lastRedstone = tag.getBoolean("lastRedstone");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("mode", this.mode);
        tag.putBoolean("lastRedstone", this.lastRedstone);
    }
}
