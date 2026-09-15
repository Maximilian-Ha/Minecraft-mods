package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.menus.MachineStrandCasterMenu;
import com.hbm.items.machine.MoldItem;
import com.hbm.items.machine.MoldItem.Mold;
import com.hbm.items.machine.ScrapsItem;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineStrandCaster.
 *
 * Der Strangguss ist das Fliessband der Giesserei. Giessform und Giessbecken fassen EIN
 * Gussstueck und muessen von Hand geleert werden; dieser hier nimmt bis zu neun Portionen auf
 * einmal an, giesst sie in einem Zug und legt sie in sechs Ausgabefaecher.
 *
 * ER BRAUCHT WASSER UND GIBT DAMPF AB, und das ist mehr als Beiwerk. Ein Strang wird nicht durch
 * Abwarten fest, sondern durch Abschrecken; wo das Becken hundert Ticks abkuehlt, kuehlt dieser
 * mit Wasser, und das Wasser kommt als Abdampf wieder heraus. Fuenf Millibar je Millibar Metall.
 *
 * ER WARTET AUF DIE VOLLE LADUNG. Gegossen wird erst, wenn neun Stueck beisammen sind -- oder
 * wenn zehn Sekunden lang nichts mehr nachgekommen ist. So laeuft er im Dauerbetrieb voll aus
 * und bleibt trotzdem nicht auf einem halben Rest sitzen.
 *
 * VIER GIESSPUNKTE OBEN: er ist zwei Bloecke breit und nimmt an allen vier Feldern seiner
 * Oberseite an. Wer mit einem Tiegel giesst, trifft ihn also leicht.
 *
 * ABWEICHUNG: das Original prueft beim Giessen mit einer eigenen standardCheck-Fassung, ob
 * hoechstens das Neunfache einer Form darin steht, waehrend getCapacity das Zehnfache meldet.
 * Der Port uebernimmt beide Zahlen unveraendert -- die Luecke ist Absicht: sie laesst den
 * letzten Guss ueberlaufen, statt ihn abzuschneiden.
 *
 * NICHT UEBERNOMMEN: das Modell strand_caster.obj. Der Port zeichnet Feuerfestziegel-Kaesten.
 */
public class MachineStrandCasterBlockEntity extends FoundryCastingBaseBlockEntity implements IFluidStandardTransceiverMK2, MenuProvider {

    public static final int SLOT_MOLD = 0;
    public static final int SLOT_OUTPUT_START = 1;
    public static final int SLOT_OUTPUT_END = 7;
    public static final int SLOTS = 7;

    /** Wie viele Stueck er hoechstens in einem Zug giesst. */
    public static final int BATCH = 9;

    /** Wie lange er ohne Nachschub wartet, bevor er den Rest doch giesst. */
    public static final int FLUSH_DELAY = 200;

    public final FluidTank water;
    public final FluidTank steam;

    private long lastProgressTick = 0;

    public MachineStrandCasterBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_STRAND_CASTER.get(), pos, state, SLOTS);

        this.water = new FluidTank(Fluids.WATER, 64_000);
        this.steam = new FluidTank(Fluids.SPENTSTEAM, 64_000);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.machineStrandCaster");
    }

    /**
     * Die Abkuehlschleife der Giessbasis wird hier NICHT gefahren -- der Strangguss giesst in
     * Losen statt Stueck fuer Stueck. Das Original macht es genauso.
     */
    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        /* Bei einem Ueberlauf faellt der Ueberschuss als Schrott heraus. */
        if(this.type != null && this.amount > this.getCapacity()) {
            Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 2, this.worldPosition.getZ() + 0.5,
                    ScrapsItem.create(new MaterialStack(this.type, this.amount - this.getCapacity())));
            this.amount = this.getCapacity();
        }

        if(this.amount == 0) this.type = null;

        for(DirPos pos : this.getFluidConPos()) {
            this.trySubscribe(this.water.getTankType(), this.level, pos);
            this.tryProvide(this.steam, this.level, pos);
        }

        int moldsToCast = this.maxProcessable();

        if(moldsToCast > 0 && (moldsToCast >= BATCH || this.level.getGameTime() >= this.lastProgressTick + FLUSH_DELAY)) {
            this.cast(moldsToCast);
        }

        this.networkPackNT(150);
    }

    private void cast(int moldsToCast) {

        Mold mold = this.getInstalledMold();
        if(mold == null) return;

        this.amount -= moldsToCast * mold.getCost();

        ItemStack out = mold.getOutput(this.level, this.type);
        int remaining = out.getCount() * moldsToCast;
        int maxStackSize = out.getMaxStackSize();

        for(int i = SLOT_OUTPUT_START; i < SLOT_OUTPUT_END && remaining > 0; i++) {

            ItemStack slot = this.slots.get(i);
            boolean free = slot.isEmpty();

            if(!free && !ItemStack.isSameItemSameComponents(slot, out)) continue;

            int toDeposit = Math.min(remaining, maxStackSize - (free ? 0 : slot.getCount()));
            if(toDeposit <= 0) continue;

            if(free) this.slots.set(i, out.copyWithCount(toDeposit));
            else slot.grow(toDeposit);

            remaining -= toDeposit;
        }

        int water = this.getWaterRequired() * moldsToCast;
        this.water.setFill(this.water.getFill() - water);
        this.steam.setFill(this.steam.getFill() + water);

        this.lastProgressTick = this.level.getGameTime();
        this.setChanged();
    }

    /**
     * Wie viele Stueck jetzt gegossen werden koennten: begrenzt durch das Metall im Becken, den
     * Platz in den Ausgabefaechern, das Wasser und den Platz fuer den Dampf.
     */
    private int maxProcessable() {

        Mold mold = this.getInstalledMold();
        if(this.type == null || mold == null) return 0;

        ItemStack out = mold.getOutput(this.level, this.type);
        if(out.isEmpty()) return 0;

        int freeSlots = 0;
        int stackLimit = out.getMaxStackSize();

        for(int i = SLOT_OUTPUT_START; i < SLOT_OUTPUT_END; i++) {
            ItemStack slot = this.slots.get(i);
            if(slot.isEmpty()) freeSlots += stackLimit;
            else if(ItemStack.isSameItemSameComponents(slot, out)) freeSlots += stackLimit - slot.getCount();
        }

        int required = this.getWaterRequired();

        int molds = this.amount / mold.getCost();
        molds = Math.min(molds, freeSlots / out.getCount());
        molds = Math.min(molds, this.water.getFill() / required);
        molds = Math.min(molds, (this.steam.getMaxFill() - this.steam.getFill()) / required);

        return molds;
    }

    /** Fuenf Millibar Kuehlwasser je Millibar Metall. */
    private int getWaterRequired() {
        Mold mold = this.getInstalledMold();
        return mold != null ? 5 * mold.getCost() : 50;
    }

    /** Er nimmt JEDE Formgroesse an -- anders als Giessform und Becken, die je eine fordern. */
    @Override
    public @Nullable Mold getInstalledMold() {
        ItemStack stack = this.slots.get(SLOT_MOLD);
        return stack.getItem() instanceof MoldItem ? MoldItem.getMold(stack) : null;
    }

    @Override
    public int getMoldSize() {
        Mold mold = this.getInstalledMold();
        return mold == null ? 0 : mold.size;
    }

    @Override
    public int getCapacity() {
        Mold mold = this.getInstalledMold();
        return mold == null ? 50_000 : mold.getCost() * 10;
    }

    /** Beim Giessen gilt das Neunfache, nicht das Zehnfache -- siehe der Hinweis oben. */
    private int getPourLimit() {
        Mold mold = this.getInstalledMold();
        return mold != null ? mold.getCost() * BATCH : this.getCapacity();
    }

    @Override
    public boolean standardCheck(Level level, BlockPos pos, @Nullable Direction side, MaterialStack stack) {

        if(this.type != null && this.type != stack.material) return false;
        if(this.getInstalledMold() == null) return false;

        return this.amount < this.getPourLimit();
    }

    @Override
    public MaterialStack standardAdd(Level level, BlockPos pos, @Nullable Direction side, MaterialStack stack) {

        this.type = stack.material;
        int limit = this.getPourLimit();

        this.lastProgressTick = level.getGameTime();

        if(stack.amount + this.amount <= limit) {
            this.amount += stack.amount;
            this.setChanged();
            return null;
        }

        int required = limit - this.amount;
        this.amount = limit;
        stack.amount -= required;
        this.setChanged();

        return stack;
    }

    /** Gegossen wird nur von oben und nur auf eines der vier Felder der Oberseite. */
    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {

        if(side != Direction.UP) return false;

        for(BlockPos pour : this.getMetalPourPos()) {
            if(pour.equals(pos)) return this.standardCheck(level, pos, side, stack);
        }

        return false;
    }

    /** Die vier Felder der Oberseite, auf die gegossen werden darf. */
    public BlockPos[] getMetalPourPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos pos = this.worldPosition;

        return new BlockPos[] {
                pos.offset(rot.getStepX() - dir.getStepX(), 2, rot.getStepZ() - dir.getStepZ()),
                pos.offset(-dir.getStepX(), 2, -dir.getStepZ()),
                pos.offset(rot.getStepX(), 2, rot.getStepZ()),
                pos.offset(0, 2, 0)
        };
    }

    /** Vier Rohranschluesse: zwei am vorderen Ende, zwei am hinteren. */
    public DirPos[] getFluidConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos pos = this.worldPosition;

        return new DirPos[] {
                new DirPos(pos.offset(rot.getStepX() * 2 - dir.getStepX(), 0, rot.getStepZ() * 2 - dir.getStepZ()), rot),
                new DirPos(pos.offset(-rot.getStepX() - dir.getStepX(), 0, -rot.getStepZ() - dir.getStepZ()), rot.getOpposite()),
                new DirPos(pos.offset(rot.getStepX() * 2 - dir.getStepX() * 5, 0, rot.getStepZ() * 2 - dir.getStepZ() * 5), rot),
                new DirPos(pos.offset(-rot.getStepX() - dir.getStepX() * 5, 0, -rot.getStepZ() - dir.getStepZ() * 5), rot.getOpposite())
        };
    }

    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.steam }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.water }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.water, this.steam }; }

    /* Das Abschreibwerkzeug fasst ihn gar nicht an: beide Tanks stehen fest auf Wasser und
     * Abdampf, und IFluidCopiable gibt der Strangguss deshalb nicht an -- so haelt es auch das
     * Original. */

    /* --- Container --- */

    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == SLOT_MOLD && stack.getItem() instanceof MoldItem; }

    @Override public int[] getSlotsForFace(Direction direction) { return OUTPUTS; }

    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) { return false; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return slot >= SLOT_OUTPUT_START; }

    private static final int[] OUTPUTS = { 1, 2, 3, 4, 5, 6 };

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineStrandCasterMenu(id, inventory, this);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.water.serialize(buf);
        this.steam.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.water.deserialize(buf);
        this.steam.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.water.readFromNBT(tag, "w");
        this.steam.readFromNBT(tag, "s");
        this.lastProgressTick = tag.getLong("t");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.water.writeToNBT(tag, "w");
        this.steam.writeToNBT(tag, "s");
        tag.putLong("t", this.lastProgressTick);
    }
}
