package com.hbm.blockentity.machine.icf;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.inventory.menus.ICFMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.ICFPelletItem;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityICF.
 *
 * Die Brennkammer der Traegheitsfusion. Sie tut von sich aus nichts -- was hier geschieht,
 * entscheidet der Laser, der von aussen hereinschiesst.
 *
 * WIE SIE ARBEITET
 *
 * Fuenf Eingabefaecher halten frische Kuegelchen bereit, eins liegt in der Kammer, fuenf nehmen
 * die verbrauchten auf. Nachgeladen wird von selbst: was abgebrannt ist, wandert in die Ausgabe,
 * und das naechste frische rutscht nach.
 *
 * Reicht die Laserleistung ueber die Zuendschwelle des Kuegelchens, laeuft die Reaktion und
 * erzeugt Waerme. Reicht sie nicht, verpufft ein Viertel der eingestrahlten Leistung trotzdem
 * als Waerme -- der Laser heizt die Kammer auch dann, wenn nichts zuendet.
 *
 * Die Waerme geht ins Natrium: ein Viertel des Vorrats wird je Tick abgefuehrt, das Fluidsystem
 * rechnet aus, wie viel heisses Natrium dabei herauskommt. Nebenher faellt Sternenfluss an, ein
 * Zehnmillionstel je Einheit gespeicherter Waerme.
 *
 * ABWEICHUNGEN:
 * - Der Partikeleffekt "hadron" ueber der Kammer bleibt weg; die Sorte ist im Port nicht
 *   vorhanden.
 * - Die Satellitenmeldung (SatelliteRayScan) ist gestrichen, das steht so in ENTSCHEIDUNGEN.md.
 * - OpenComputers und die Energy-Control-Anzeige ebenso.
 */
public class ICFBlockEntity extends MachineBaseBlockEntity implements IFluidStandardTransceiverMK2 {

    public static final long MAX_HEAT = 1_000_000_000_000L;

    /** Fuenf Faecher fuer frische Kuegelchen. */
    public static final int SLOT_IN_FIRST = 0;
    /** Das Fach, in dem das Kuegelchen gerade liegt. */
    public static final int SLOT_CHAMBER = 5;
    /** Fuenf Faecher fuer verbrauchte. */
    public static final int SLOT_OUT_FIRST = 6;
    /** Das Fach fuer den Kuehlmittelbehaelter. */
    public static final int SLOT_FLUID_ID = 11;

    private static final int[] SLOTS_IO = new int[] {0, 1, 2, 3, 4, 6, 7, 8, 9, 10};

    /** Was der Laser in diesem Tick eingestrahlt hat. Wird am Ende jedes Ticks geleert. */
    public long laser;
    /** Was er hoechstens haette einstrahlen koennen -- nur fuer die Anzeige. */
    public long maxLaser;
    public long heat;
    /** Waerme aus der Reaktion dieses Ticks. */
    public long heatup;

    public int consumption;
    public int output;

    public final FluidTank[] tanks;

    private AABB renderBox;

    public ICFBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.ICF.get(), pos, state, 12);

        this.tanks = new FluidTank[3];
        this.tanks[0] = new FluidTank(Fluids.SODIUM, 512_000);
        this.tanks[1] = new FluidTank(Fluids.SODIUM_HOT, 512_000);
        this.tanks[2] = new FluidTank(Fluids.STELLAR_FLUX, 24_000);
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.machineICF"); }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.tanks[0].setType(SLOT_FLUID_ID, this.slots);

        for(DirPos pos : this.getConPos()) this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);

        boolean changed = this.cyclePellets();

        this.heatup = 0;
        ItemStack pellet = this.slots.get(SLOT_CHAMBER);

        if(pellet.getItem() == NtmItems.ICF_PELLET.get() && ICFPelletItem.getFusingDifficulty(pellet) <= this.laser) {

            this.heatup = ICFPelletItem.react(pellet, this.laser);
            this.heat += this.heatup;

            if(ICFPelletItem.getDepletion(pellet) >= ICFPelletItem.getMaxDepletion(pellet)) {
                this.slots.set(SLOT_CHAMBER, new ItemStack(NtmItems.ICF_PELLET_DEPLETED.get()));
                changed = true;
            }

            /* Sternenfluss faellt nebenher an, im Verhaeltnis zur gespeicherten Waerme. */
            this.tanks[2].setFill(this.tanks[2].getFill() + (int) Math.ceil(this.heat * 10D / MAX_HEAT));
            if(this.tanks[2].getFill() > this.tanks[2].getMaxFill()) this.tanks[2].setFill(this.tanks[2].getMaxFill());
        }

        /* Zuendet nichts, heizt der Laser die Kammer trotzdem -- mit einem Viertel. */
        if(this.heatup == 0) this.heat += (long) (this.laser * 0.25D);

        this.transferHeat();

        for(DirPos pos : this.getConPos()) {
            this.tryProvide(this.tanks[1], this.level, pos);
            this.tryProvide(this.tanks[2], this.level, pos);
        }

        this.heat *= 0.999D;
        if(this.heat > MAX_HEAT) this.heat = MAX_HEAT;

        if(changed) this.setChanged();

        this.networkPackNT(150);

        this.laser = 0;
        this.maxLaser = 0;
    }

    /** Abgebranntes heraus, Frisches herein. Gibt zurueck, ob sich etwas bewegt hat. */
    private boolean cyclePellets() {

        boolean changed = false;

        if(this.slots.get(SLOT_CHAMBER).getItem() == NtmItems.ICF_PELLET_DEPLETED.get()) {
            for(int i = SLOT_OUT_FIRST; i < SLOT_OUT_FIRST + 5; i++) {
                if(!this.slots.get(i).isEmpty()) continue;
                this.slots.set(i, this.slots.get(SLOT_CHAMBER).copy());
                this.slots.set(SLOT_CHAMBER, ItemStack.EMPTY);
                changed = true;
                break;
            }
        }

        if(this.slots.get(SLOT_CHAMBER).isEmpty()) {
            for(int i = SLOT_IN_FIRST; i < SLOT_IN_FIRST + 5; i++) {
                if(this.slots.get(i).getItem() != NtmItems.ICF_PELLET.get()) continue;
                this.slots.set(SLOT_CHAMBER, this.slots.get(i).copy());
                this.slots.set(i, ItemStack.EMPTY);
                changed = true;
                break;
            }
        }

        return changed;
    }

    /** Fuehrt ein Viertel der Waerme je Tick ins Kuehlmittel ab. */
    private void transferHeat() {

        this.consumption = 0;
        this.output = 0;

        if(!this.tanks[0].getTankType().hasTrait(FT_Heatable.class)) return;

        FT_Heatable trait = this.tanks[0].getTankType().getTrait(FT_Heatable.class);
        HeatingStep step = trait.getFirstStep();
        this.tanks[1].setTankType(step.typeProduced);

        int coolingCycles = this.tanks[0].getFill() / step.amountReq;
        int heatingCycles = (this.tanks[1].getMaxFill() - this.tanks[1].getFill()) / step.amountProduced;
        int heatCycles = (int) Math.min(
                this.heat / 4D / step.heatReq * trait.getEfficiency(HeatingType.ICF),
                (double) this.heat / step.heatReq);

        int cycles = Math.min(coolingCycles, Math.min(heatingCycles, heatCycles));

        this.tanks[0].setFill(this.tanks[0].getFill() - step.amountReq * cycles);
        this.tanks[1].setFill(this.tanks[1].getFill() + step.amountProduced * cycles);
        this.heat -= (long) step.heatReq * cycles;

        this.consumption = step.amountReq * cycles;
        this.output = step.amountProduced * cycles;
    }

    /** Die sechs Anschlussstellen: oben, unten und je zwei an den beiden Laengsseiten. */
    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise();

        return new DirPos[] {
                new DirPos(this.worldPosition.above(6), Direction.UP),
                new DirPos(this.worldPosition.below(), Direction.DOWN),
                new DirPos(this.worldPosition.offset(dir.getStepX() * 3 + rot.getStepX() * 6, 3, dir.getStepZ() * 3 + rot.getStepZ() * 6), dir),
                new DirPos(this.worldPosition.offset(dir.getStepX() * 3 - rot.getStepX() * 6, 3, dir.getStepZ() * 3 - rot.getStepZ() * 6), dir),
                new DirPos(this.worldPosition.offset(-dir.getStepX() * 3 + rot.getStepX() * 6, 3, -dir.getStepZ() * 3 + rot.getStepZ() * 6), dir.getOpposite()),
                new DirPos(this.worldPosition.offset(-dir.getStepX() * 3 - rot.getStepX() * 6, 3, -dir.getStepZ() * 3 - rot.getStepZ() * 6), dir.getOpposite())
        };
    }

    /* --- Faecher --- */

    @Override public int[] getSlotsForFace(Direction direction) { return SLOTS_IO; }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return index < 5 && stack.getItem() == NtmItems.ICF_PELLET.get();
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index > SLOT_CHAMBER;
    }

    /* --- Fluid --- */

    @Override public FluidTank[] getAllTanks() { return this.tanks; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] {this.tanks[1], this.tanks[2]}; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {this.tanks[0]}; }

    /* --- Speichern und Uebertragen --- */

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.laser);
        buf.writeLong(this.maxLaser);
        buf.writeLong(this.heat);
        for(FluidTank tank : this.tanks) tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.laser = buf.readLong();
        this.maxLaser = buf.readLong();
        this.heat = buf.readLong();
        for(FluidTank tank : this.tanks) tank.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].readFromNBT(tag, "t" + i);
        this.heat = tag.getLong("heat");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].writeToNBT(tag, "t" + i);
        tag.putLong("heat", this.heat);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ICFMenu(id, inventory, this);
    }

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 8, y, z - 8, x + 9, y + 6, z + 9);
        }
        return this.renderBox;
    }
}
