package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineAnnihilatorMenu;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.saveddata.AnnihilatorSavedData;
import com.hbm.saveddata.AnnihilatorSavedData.AnnihilatorPool;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.math.BigInteger;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineAnnihilator.
 *
 * Er vernichtet, was man hineinwirft -- und merkt sich dabei jedes Stueck. Wer genug von einer
 * Sorte durchgeschickt hat, bekommt eine Blaupause. Das ist der einzige Weg an die
 * 528er-Rezepte; siehe AnnihilatorRecipes.
 *
 * ZWEI EINGAENGE: das Fach links frisst Gegenstaende, der Tank frisst Fluide. Beides wird
 * restlos verbraucht, es kommt nichts zurueck ausser den Auszahlungen.
 *
 * DAS BEOBACHTUNGSFACH (8) veraendert nichts. Wer dort etwas hineinlegt, sieht nur, wie weit der
 * Zaehler fuer diese Sorte steht -- Fluidkennungen eingeschlossen, dann zaehlt es den Fluidstand.
 *
 * DAS ANFORDERUNGSFACH (9) vernichtet ein Stueck und zahlt die zugehoerige Blaupause NOCH EINMAL
 * aus, auch wenn die Schwelle laengst erreicht war. So kommt man an eine verlorene Blaupause,
 * ohne den ganzen Weg noch einmal zu gehen.
 *
 * ABWEICHUNG: das Original zieht beim Vernichten Strahlung aus dem HazardSystem und blaest sie
 * in die Umgebung, und es spuckt eine Gasflamme aus dem Schlot. Beides braucht Teile, die der
 * Port an dieser Stelle nicht hat; die Maschine zaehlt und zahlt aus, sie qualmt nur nicht.
 */
public class MachineAnnihilatorBlockEntity extends MachineBaseBlockEntity implements IFluidStandardReceiverMK2, IControlReceiver {

    public String pool = "Recycling";
    public final FluidTank tank;

    /** Nur zur Anzeige: der Stand des Zaehlers fuer das, was im Beobachtungsfach liegt. */
    public BigInteger monitor = BigInteger.ZERO;

    private AABB renderBox;

    public MachineAnnihilatorBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_ANNIHILATOR.get(), pos, state, 11);
        this.tank = new FluidTank(Fluids.NONE, 2_500_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.annihilator");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;
        if(!(this.level instanceof ServerLevel serverLevel)) return;

        this.tank.setType(1, this.slots);

        if(this.pool != null && !this.pool.isEmpty()) {

            for(DirPos pos : this.getConPos()) {
                if(this.tank.getTankType() != Fluids.NONE) this.trySubscribe(this.tank.getTankType(), this.level, pos);
            }

            AnnihilatorSavedData data = AnnihilatorSavedData.getData(serverLevel);

            ItemStack trash = this.slots.get(0);
            if(!trash.isEmpty()) {
                this.tryAddPayout(data.pushToPool(this.pool, trash, false));
                this.slots.set(0, ItemStack.EMPTY);
                this.setChanged();
            }

            if(this.tank.getFill() > 0) {
                this.tryAddPayout(data.pushToPool(this.pool, this.tank.getTankType(), this.tank.getFill(), false));
                this.tank.setFill(0);
                this.setChanged();
            }

            this.updateMonitor(data);
            this.handlePayoutRequest(data);
        }

        this.networkPackNT(25);
    }

    private void updateMonitor(AnnihilatorSavedData data) {

        ItemStack stack = this.slots.get(8);

        if(stack.isEmpty()) {
            this.monitor = BigInteger.ZERO;
            return;
        }

        Object key = stack.getItem() instanceof IItemFluidIdentifier id
                ? id.getType(this.level, this.worldPosition, stack)
                : new ComparableStack(stack).makeSingular();

        AnnihilatorPool pool = data.pools.get(this.pool);
        BigInteger stand = pool == null ? null : pool.items.get(key);
        this.monitor = stand == null ? BigInteger.ZERO : stand;
    }

    private void handlePayoutRequest(AnnihilatorSavedData data) {

        ItemStack request = this.slots.get(9);
        if(request.isEmpty()) return;

        ItemStack single = request.copy();
        single.setCount(1);

        ItemStack payout = data.pushToPool(this.pool, single, true);
        this.removeItem(9, 1);

        if(payout.isEmpty()) return;

        ItemStack out = this.slots.get(10);

        if(out.isEmpty()) {
            this.slots.set(10, payout);
        } else if(ItemStack.isSameItemSameComponents(out, payout) && out.getCount() + payout.getCount() <= out.getMaxStackSize()) {
            out.grow(payout.getCount());
        }

        this.setChanged();
    }

    /** Auszahlungen gehen in die sechs Ausgabefaecher; passt keines, verfallen sie. */
    public void tryAddPayout(ItemStack payout) {

        if(payout.isEmpty()) return;

        for(int i = 2; i <= 7; i++) {
            ItemStack slot = this.slots.get(i);
            if(!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, payout) && slot.getCount() + payout.getCount() <= slot.getMaxStackSize()) {
                slot.grow(payout.getCount());
                this.setChanged();
                return;
            }
        }

        for(int i = 2; i <= 7; i++) {
            if(this.slots.get(i).isEmpty()) {
                this.slots.set(i, payout);
                this.setChanged();
                return;
            }
        }
    }

    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos p = this.worldPosition;

        return new DirPos[] {
                new DirPos(p.relative(dir, 5), dir),
                new DirPos(p.relative(dir, 3).relative(rot, 2), rot),
                new DirPos(p.relative(dir, 3).relative(rot, -2), rot.getOpposite())
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == 0) return true;                                                   // Muell
        if(slot == 1) return stack.getItem() instanceof IItemFluidIdentifier;        // Fluidkennung
        if(slot == 8) return true;                                                   // Beobachtung
        return slot == 9;                                                            // Anforderung
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= 2 && index <= 7;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] {0, 2, 3, 4, 5, 6, 7};
    }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.tank.serialize(buf);
        buf.writeUtf(this.pool == null ? "" : this.pool);
        buf.writeByteArray(this.monitor.toByteArray());
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.tank.deserialize(buf);
        this.pool = buf.readUtf();
        byte[] zahl = buf.readByteArray();
        this.monitor = zahl.length == 0 ? BigInteger.ZERO : new BigInteger(zahl);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tank.readFromNBT(tag, "t");
        this.pool = tag.getString("pool");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.tank.writeToNBT(tag, "t");
        tag.putString("pool", this.pool == null ? "" : this.pool);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("pool")) {
            this.pool = tag.getString("pool");
            this.setChanged();
        }
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 5, p.getY() - 2, p.getZ() - 5, p.getX() + 6, p.getY() + 10, p.getZ() + 6);
        }
        return this.renderBox;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineAnnihilatorMenu(id, inventory, this);
    }
}
