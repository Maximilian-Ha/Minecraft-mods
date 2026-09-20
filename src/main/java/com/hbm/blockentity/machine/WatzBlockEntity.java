package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.ToolConversionBlock;
import com.hbm.entity.projectile.Shrapnel;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.menus.WatzMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.WatzPelletItem;
import com.hbm.items.machine.WatzPelletItem.EnumWatzType;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.Compat;
import com.hbm.util.EnumUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.function.Function;
import com.hbm.registry.NtmCriteria;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityWatz.
 *
 * Der Watz-Reaktor. Er besteht aus uebereinandergestapelten Segmenten von je drei Blockstufen
 * Hoehe; jedes fuehrt vierundzwanzig Pellets. Die Segmente rechnen NICHT jedes fuer sich: das
 * unterste Segment mit einem Segment darueber haelt still, und das oberste zieht die ganze
 * Saeule zusammen und rechnet sie in einem Stueck durch. Nur so ist die Reihenfolge festgelegt
 * -- sonst haengt das Ergebnis davon ab, in welcher Reihenfolge die Welt ihre Blockentitaeten
 * abarbeitet.
 *
 * WIE ER RECHNET
 *
 * Jede Sorte gibt einen Grundfluss ab. Der Grundfluss aller Pellets plus der Fluss der letzten
 * Runde ist der Eingangsfluss. Aus ihm macht jedes brennende Pellet ueber seine Brennfunktion
 * neue Reaktivitaet, geteilt durch den Temperaturkoeffizienten -- bei den meisten Sorten bremst
 * die Hitze, bei den beiden Neptunium-Sorten treibt sie an. Was die Absorber schlucken, wird
 * unmittelbar zu Waerme.
 *
 * Gekuehlt wird von unten nach oben, gerechnet von oben nach unten, und die Tanks aller Segmente
 * gelten als ein Tank. Ein Prozent der Waerme geht je Tick von selbst verloren, zwanzig Prozent
 * nimmt das Kuehlmittel auf.
 *
 * Laeuft der Schlammtank ueber, ist Schluss: die Anlage zerlegt sich, verstrahlt die Gegend und
 * hinterlaesst einen Haufen Schlamm und abgebrochener Saeulen.
 *
 * ABWEICHUNGEN:
 * - Die OpenComputers-Anbindung und die Radio-Werte (IRORValueProvider) sind gestrichen; beides
 *   steht so in ENTSCHEIDUNGEN.md.
 * - Der Erfolg "achWatzBoom" entfaellt -- der Port hat das Erfolgssystem des Originals nicht.
 */
public class WatzBlockEntity extends MachineBaseBlockEntity implements IControlReceiver, IFluidStandardTransceiverMK2 {

    /** Fassungsvermoegen je Tank und Segment. */
    public static final int TANK_SIZE = 64_000;
    /** Anteil der Waerme, den das Kuehlmittel je Tick aufnimmt. */
    private static final double COOLING_FACTOR = 0.2D;
    /** Wie viele Pellets ein Segment fuehrt. */
    public static final int PELLETS = 24;
    /** Hoehe eines Segments in Bloecken. */
    public static final int SEGMENT_HEIGHT = 3;

    private static final int[] SLOTS_IO = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};

    public FluidTank[] tanks;
    /** Die zusammengefassten Tanks der ganzen Saeule -- nur zur Anzeige und zum Rechnen. */
    public FluidTank[] sharedTanks;

    public int heat;
    /** Fluss aus dem Grundzerfall der letzten Runde, nur zur Anzeige. */
    public double fluxLastBase;
    /** Fluss aus der Reaktion der letzten Runde; er geht in die naechste ein. */
    public double fluxLastReaction;
    /** Was die Oberflaeche anzeigt: beides zusammen. */
    public double fluxDisplay;
    public boolean isOn;

    /** Ob die Faecher auf ihre jetzige Bestueckung festgelegt sind. */
    public boolean isLocked = false;
    /** Womit jedes Fach belegt werden darf, solange die Sperre steht. */
    public NonNullList<ItemStack> locks;

    private AABB renderBox;

    public WatzBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.WATZ.get(), pos, state, PELLETS);

        this.locks = NonNullList.withSize(PELLETS, ItemStack.EMPTY);

        this.tanks = new FluidTank[3];
        this.tanks[0] = new FluidTank(Fluids.COOLANT, TANK_SIZE);
        this.tanks[1] = new FluidTank(Fluids.COOLANT_HOT, TANK_SIZE);
        this.tanks[2] = new FluidTank(Fluids.WATZ, TANK_SIZE);

        this.resetSharedTanks();
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.watz"); }

    /** Setzt die Anzeigetanks auf den eigenen Stand zurueck, damit nie Unsinn darin steht. */
    protected void resetSharedTanks() {
        this.sharedTanks = new FluidTank[3];
        this.sharedTanks[0] = new FluidTank(Fluids.COOLANT, TANK_SIZE);
        this.sharedTanks[1] = new FluidTank(Fluids.COOLANT_HOT, TANK_SIZE);
        this.sharedTanks[2] = new FluidTank(Fluids.WATZ, TANK_SIZE);
        for(int i = 0; i < 3; i++) this.sharedTanks[i].setFill(this.tanks[i].getFill());
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;
        if(!this.level.isClientSide) this.resetSharedTanks();
        if(this.level.isClientSide || this.updateLock()) return;

        /* Angeschaltet ist die Anlage, wenn die Pumpe darueber steht und Strom anliegt. */
        boolean turnedOn = this.level.getBlockState(this.worldPosition.above(SEGMENT_HEIGHT)).is(NtmBlocks.WATZ_PUMP.get())
                && this.level.getSignal(this.worldPosition.above(5), Direction.DOWN) > 0;

        List<WatzBlockEntity> segments = new ArrayList<>();
        segments.add(this);
        this.subscribeToTop();

        /* Die Saeule von oben nach unten einsammeln. */
        for(int y = this.worldPosition.getY() - SEGMENT_HEIGHT; y >= this.level.getMinBuildHeight(); y -= SEGMENT_HEIGHT) {
            BlockEntity be = Compat.getBlockEntityStandard(this.level, new BlockPos(this.worldPosition.getX(), y, this.worldPosition.getZ()));
            if(be instanceof WatzBlockEntity segment) segments.add(segment);
            else break;
        }

        /* Alle Tanks der Saeule als einen behandeln. */
        FluidTank[] shared = new FluidTank[3];
        for(int i = 0; i < 3; i++) shared[i] = new FluidTank(this.tanks[i].getTankType(), 0);

        for(WatzBlockEntity segment : segments) {
            segment.setupCoolant();
            for(int i = 0; i < 3; i++) {
                shared[i].changeTankSize(shared[i].getMaxFill() + segment.tanks[i].getMaxFill());
                shared[i].setFill(shared[i].getFill() + segment.tanks[i].getFill());
            }
        }

        /* Gekuehlt wird von unten nach oben. */
        for(int i = segments.size() - 1; i >= 0; i--) segments.get(i).updateCoolant(shared);

        /* Gerechnet wird von oben nach unten. */
        this.updateReaction(null, shared, turnedOn);
        for(int i = 1; i < segments.size(); i++) segments.get(i).updateReaction(segments.get(i - 1), shared, turnedOn);

        for(WatzBlockEntity segment : segments) {
            segment.sharedTanks[0] = shared[0];
            segment.sharedTanks[1] = shared[1];
            segment.sharedTanks[2] = shared[2];
            segment.isOn = turnedOn;
            segment.networkPackNT(25);
            /* Ein Prozent Waerme geht je Tick von selbst verloren. */
            segment.heat *= 0.99;
        }

        /* Den gemeinsamen Bestand wieder auf die einzelnen Tanks verteilen, von unten nach oben. */
        for(int i = segments.size() - 1; i >= 0; i--) {
            WatzBlockEntity segment = segments.get(i);
            for(int j = 0; j < 3; j++) {
                int min = Math.min(segment.tanks[j].getMaxFill(), shared[j].getFill());
                shared[j].setFill(shared[j].getFill() - min);
                segment.tanks[j].setFill(min);
            }
        }

        segments.get(segments.size() - 1).sendOutBottom();

        /* Was jetzt noch an Schlamm uebrig ist, hat nirgends Platz. */
        if(shared[2].getFill() > 0) this.blowUp();
    }

    /**
     * Sicherung gegen kaputte Speicherstaende: die beiden Kuehlmitteltanks muessen zueinander
     * passen, sonst rechnet updateCoolant mit dem falschen Ziel.
     */
    public void setupCoolant() {
        this.tanks[0].setTankType(Fluids.COOLANT);
        this.tanks[1].setTankType(this.tanks[0].getTankType().getTrait(FT_Heatable.class).getFirstStep().typeProduced);
    }

    /** Wandelt Waerme in heisses Kuehlmittel, solange kaltes da ist und Platz fuer heisses. */
    public void updateCoolant(FluidTank[] shared) {

        double heatToUse = this.heat * COOLING_FACTOR;

        FT_Heatable trait = shared[0].getTankType().getTrait(FT_Heatable.class);
        HeatingStep step = trait.getFirstStep();

        int heatCycles = (int) (heatToUse / step.heatReq);
        int coolCycles = shared[0].getFill() / step.amountReq;
        int hotCycles = (shared[1].getMaxFill() - shared[1].getFill()) / step.amountProduced;

        int cycles = Math.min(heatCycles, Math.min(hotCycles, coolCycles));

        this.heat -= cycles * step.heatReq;
        shared[0].setFill(shared[0].getFill() - cycles * step.amountReq);
        shared[1].setFill(shared[1].getFill() + cycles * step.amountProduced);
    }

    /**
     * Eine Runde Reaktion fuer dieses Segment. Das Segment darueber wird mitgegeben, damit die
     * Pellets nachrutschen koennen.
     */
    public void updateReaction(@Nullable WatzBlockEntity above, FluidTank[] shared, boolean turnedOn) {

        if(turnedOn) {

            List<ItemStack> pellets = new ArrayList<>();
            for(int i = 0; i < PELLETS; i++) {
                ItemStack stack = this.slots.get(i);
                if(stack.getItem() == NtmItems.WATZ_PELLET.get()) pellets.add(stack);
            }

            double baseFlux = 0D;
            for(ItemStack stack : pellets) baseFlux += type(stack).passive;

            double inputFlux = baseFlux + this.fluxLastReaction;
            double addedFlux = 0D;
            double addedHeat = 0D;

            /* Was brennt, erzeugt Fluss und Waerme. */
            for(ItemStack stack : pellets) {

                EnumWatzType type = type(stack);
                Function burnFunc = type.burnFunc;
                if(burnFunc == null) continue;

                double div = type.heatDiv != null ? type.heatDiv.effonix(this.heat) : 1D;
                double burn = burnFunc.effonix(inputFlux) / div;

                WatzPelletItem.setYield(stack, WatzPelletItem.getYield(stack) - burn);
                addedFlux += burn;
                addedHeat += type.heatEmission * burn;
                shared[2].setFill(shared[2].getFill() + (int) Math.round(type.mudContent * burn));
            }

            /* Was schluckt, macht daraus nur Waerme -- und nutzt sich dabei ebenso ab. */
            for(ItemStack stack : pellets) {

                EnumWatzType type = type(stack);
                Function absorbFunc = type.absorbFunc;
                if(absorbFunc == null) continue;

                double absorb = absorbFunc.effonix(baseFlux + this.fluxLastReaction);

                addedHeat += absorb;
                WatzPelletItem.setYield(stack, WatzPelletItem.getYield(stack) - absorb);
                shared[2].setFill(shared[2].getFill() + (int) Math.round(type.mudContent * absorb));
            }

            this.heat += (int) addedHeat;
            this.fluxLastBase = baseFlux;
            this.fluxLastReaction = addedFlux;

        } else {
            this.fluxLastBase = 0;
            this.fluxLastReaction = 0;
        }

        /* Aufgebrauchte Pellets werden zu Abfall. */
        for(int i = 0; i < PELLETS; i++) {
            ItemStack stack = this.slots.get(i);
            if(stack.getItem() == NtmItems.WATZ_PELLET.get() && WatzPelletItem.getEnrichment(stack) <= 0) {
                this.slots.set(i, MetaHelper.newStack(NtmItems.WATZ_PELLET_DEPLETED, 1, type(stack)));
            }
        }

        if(above == null) return;

        for(int i = 0; i < PELLETS; i++) {

            ItemStack bottom = this.slots.get(i);
            ItemStack top = above.slots.get(i);

            /* Ist unten Platz, faellt das Pellet von oben nach. */
            if(bottom.isEmpty() && !top.isEmpty()) {
                this.slots.set(i, top.copy());
                above.slots.set(i, ItemStack.EMPTY);
                continue;
            }

            /* Liegt oben Abfall und unten Brennstoff, tauschen die beiden die Plaetze. */
            if(bottom.getItem() == NtmItems.WATZ_PELLET.get() && top.getItem() == NtmItems.WATZ_PELLET_DEPLETED.get()) {
                ItemStack buf = top.copy();
                above.slots.set(i, bottom.copy());
                this.slots.set(i, buf);
            }
        }
    }

    private static EnumWatzType type(ItemStack stack) {
        return EnumUtil.grabEnumSafely(EnumWatzType.class, MetaHelper.getMeta(stack));
    }

    /** Ob ueber diesem Segment noch eines steht -- dann rechnet dieses hier nicht selbst. */
    public boolean updateLock() {
        return Compat.getBlockEntityStandard(this.level, this.worldPosition.above(SEGMENT_HEIGHT)) instanceof WatzBlockEntity;
    }

    /** Kuehlmittel kommt von oben, durch die Pumpe. */
    protected void subscribeToTop() {
        for(DirPos pos : this.getReceivingPos()) this.trySubscribe(this.tanks[0].getTankType(), this.level, pos);
    }

    /** Heisses Kuehlmittel und Schlamm gehen unten wieder heraus. */
    protected void sendOutBottom() {
        for(DirPos pos : this.getSendingPos()) {
            if(this.tanks[1].getFill() > 0) this.tryProvide(this.tanks[1], this.level, pos);
            if(this.tanks[2].getFill() > 0) this.tryProvide(this.tanks[2], this.level, pos);
        }
    }

    protected DirPos[] getReceivingPos() {
        BlockPos top = this.worldPosition.above(SEGMENT_HEIGHT);
        return new DirPos[] {
                new DirPos(top, Direction.UP),
                new DirPos(top.offset(2, 0, 0), Direction.UP),
                new DirPos(top.offset(-2, 0, 0), Direction.UP),
                new DirPos(top.offset(0, 0, 2), Direction.UP),
                new DirPos(top.offset(0, 0, -2), Direction.UP)
        };
    }

    protected DirPos[] getSendingPos() {
        BlockPos bottom = this.worldPosition.below();
        return new DirPos[] {
                new DirPos(bottom, Direction.DOWN),
                new DirPos(bottom.offset(2, 0, 0), Direction.DOWN),
                new DirPos(bottom.offset(-2, 0, 0), Direction.DOWN),
                new DirPos(bottom.offset(0, 0, 2), Direction.DOWN),
                new DirPos(bottom.offset(0, 0, -2), Direction.DOWN)
        };
    }

    /* --- Ende mit Schrecken --- */

    /** Der Schlammtank laeuft ueber: Deckel weg, Anlage auseinander, Gegend verstrahlt. */
    private void blowUp() {

        for(int x = -3; x <= 3; x++)
            for(int y = 3; y < 6; y++)
                for(int z = -3; z <= 3; z++)
                    this.level.setBlock(this.worldPosition.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);

        this.disassemble();

        ChunkRadiationManager.proxy.incrementRad(this.level, this.worldPosition.above(), 1_000F);

        double x = this.worldPosition.getX() + 0.5D;
        double y = this.worldPosition.getY() + 2D;
        double z = this.worldPosition.getZ() + 0.5D;

        this.level.playSound(null, x, y, z, NtmSoundEvents.RBMK_EXPLOSION.get(), SoundSource.BLOCKS, 50.0F, 1.0F);

        if(this.level instanceof ServerLevel serverLevel) {
            CompoundTag data = new CompoundTag();
            data.putString("type", "rbmkmush");
            data.putFloat("scale", 5);
            PacketDistributor.sendToPlayersNear(serverLevel, null, x, y, z, 250, new AuxParticle(data, x, y, z));
        }
    }

    /** Was von der Anlage uebrig bleibt: Schlamm in der Mitte, Stuempfe aussen, Splitter in der Luft. */
    private void disassemble() {

        int count = 20;

        for(int i = 0; i < count * 5; i++) {

            Shrapnel shrapnel = new Shrapnel(this.level);
            shrapnel.setPos(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 3D, this.worldPosition.getZ() + 0.5D);

            double motionY = ((this.level.random.nextFloat() * 0.5D) + 0.5D) * (1 + (count / (15 + this.level.random.nextInt(21)))) + (this.level.random.nextFloat() / 50D * count);
            double motionX = this.level.random.nextGaussian() * (1 + (count / 100));
            double motionZ = this.level.random.nextGaussian() * (1 + (count / 100));

            shrapnel.setDeltaMovement(motionX, motionY, motionZ);
            shrapnel.setWatz(true);
            this.level.addFreshEntity(shrapnel);
        }

        BlockState mud = NtmBlocks.MUD.get().defaultBlockState();
        for(int i = 0; i < SEGMENT_HEIGHT; i++) this.level.setBlock(this.worldPosition.above(i), mud, 3);

        Block element = NtmBlocks.WATZ_ELEMENT.get();
        Block cooler = NtmBlocks.WATZ_COOLER.get();
        Block end = NtmBlocks.WATZ_END.get();

        this.setBrokenColumn(0, element, 1, 0);
        this.setBrokenColumn(0, element, 2, 0);
        this.setBrokenColumn(0, element, 0, 1);
        this.setBrokenColumn(0, element, 0, 2);
        this.setBrokenColumn(0, element, -1, 0);
        this.setBrokenColumn(0, element, -2, 0);
        this.setBrokenColumn(0, element, 0, -1);
        this.setBrokenColumn(0, element, 0, -2);
        this.setBrokenColumn(0, element, 1, 1);
        this.setBrokenColumn(0, element, 1, -1);
        this.setBrokenColumn(0, element, -1, 1);
        this.setBrokenColumn(0, element, -1, -1);
        this.setBrokenColumn(0, cooler, 2, 1);
        this.setBrokenColumn(0, cooler, 2, -1);
        this.setBrokenColumn(0, cooler, 1, 2);
        this.setBrokenColumn(0, cooler, -1, 2);
        this.setBrokenColumn(0, cooler, -2, 1);
        this.setBrokenColumn(0, cooler, -2, -1);
        this.setBrokenColumn(0, cooler, 1, -2);
        this.setBrokenColumn(0, cooler, -1, -2);

        /* Der Erfolg des Originals, Runde 249 -- TileEntityWatz Z. 532. */
        NtmCriteria.markeImUmkreis(this.level, this.worldPosition, 50D, "watz_boom");

        for(int j = -1; j < 2; j++) {
            this.setBrokenColumn(1, end, 3, j);
            this.setBrokenColumn(1, end, j, 3);
            this.setBrokenColumn(1, end, -3, j);
            this.setBrokenColumn(1, end, j, -3);
        }
        this.setBrokenColumn(1, end, 2, 2);
        this.setBrokenColumn(1, end, 2, -2);
        this.setBrokenColumn(1, end, -2, 2);
        this.setBrokenColumn(1, end, -2, -2);
    }

    /**
     * Eine Saeule der Aussenwand auf zufaellige Hoehe abbrechen; was darueber lag, wird Schlamm.
     * Die Wandbloecke stehen dabei in der verschraubten Baustufe, so wie sie im Original mit
     * Metadatenwert 1 gesetzt werden.
     */
    private void setBrokenColumn(int minHeight, Block block, int x, int z) {

        int height = minHeight + this.level.random.nextInt(SEGMENT_HEIGHT - minHeight);
        BlockState state = block == NtmBlocks.WATZ_END.get()
                ? block.defaultBlockState().setValue(ToolConversionBlock.STAGE, 1)
                : block.defaultBlockState();

        for(int i = 0; i < SEGMENT_HEIGHT; i++) {
            BlockPos pos = this.worldPosition.offset(x, i, z);
            this.level.setBlock(pos, i <= height ? state : NtmBlocks.MUD.get().defaultBlockState(), 3);
        }
    }

    /* --- Faecher --- */

    @Override public int[] getSlotsForFace(Direction direction) { return SLOTS_IO; }
    @Override public int getMaxStackSize() { return 1; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(stack.getItem() != NtmItems.WATZ_PELLET.get()) return false;
        if(!this.isLocked) return true;
        ItemStack lock = this.locks.get(slot);
        return !lock.isEmpty() && lock.getItem() == stack.getItem() && MetaHelper.getMeta(lock) == MetaHelper.getMeta(stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        /* Frischer Brennstoff bleibt drin -- herausnehmen darf man nur den Abfall. */
        return stack.getItem() != NtmItems.WATZ_PELLET.get();
    }

    /* --- Steuerung --- */

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag tag) {

        if(!tag.contains("lock")) return;

        if(this.isLocked) {
            this.locks = NonNullList.withSize(PELLETS, ItemStack.EMPTY);
        } else {
            for(int i = 0; i < PELLETS; i++) this.locks.set(i, this.slots.get(i).copy());
        }

        this.isLocked = !this.isLocked;
        this.setChanged();
    }

    /* --- Speichern und Uebertragen --- */

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);

        this.locks = NonNullList.withSize(PELLETS, ItemStack.EMPTY);
        ListTag list = tag.getList("locks", 10);

        for(int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            byte slot = entry.getByte("slot");
            if(slot >= 0 && slot < PELLETS) {
                ItemStack.parse(registries, entry.getCompound("stack")).ifPresent(stack -> this.locks.set(slot, stack));
            }
        }

        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].readFromNBT(tag, "t" + i);

        this.heat = tag.getInt("heat");
        this.fluxLastBase = tag.getDouble("lastFluxB");
        this.fluxLastReaction = tag.getDouble("lastFluxR");
        this.isLocked = tag.getBoolean("isLocked");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);

        ListTag list = new ListTag();

        for(int i = 0; i < this.locks.size(); i++) {
            ItemStack lock = this.locks.get(i);
            if(lock.isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putByte("slot", (byte) i);
            entry.put("stack", lock.save(registries));
            list.add(entry);
        }
        tag.put("locks", list);

        for(int i = 0; i < this.tanks.length; i++) this.tanks[i].writeToNBT(tag, "t" + i);

        tag.putInt("heat", this.heat);
        tag.putDouble("lastFluxB", this.fluxLastBase);
        tag.putDouble("lastFluxR", this.fluxLastReaction);
        tag.putBoolean("isLocked", this.isLocked);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.heat);
        buf.writeBoolean(this.isOn);
        buf.writeBoolean(this.isLocked);
        buf.writeDouble(this.fluxLastReaction + this.fluxLastBase);
        for(FluidTank tank : this.sharedTanks) tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.heat = buf.readInt();
        this.isOn = buf.readBoolean();
        this.isLocked = buf.readBoolean();
        this.fluxDisplay = buf.readDouble();
        /*
         * Eigenheit des Originals, uebernommen: gesendet werden die zusammengefassten Tanks der
         * ganzen Saeule, gelesen wird in die eigenen. Der Client sieht also in jedem Segment den
         * Gesamtbestand -- genau das zeigt die Oberflaeche auch an.
         */
        for(FluidTank tank : this.tanks) tank.deserialize(buf);
    }

    /* --- Fluid --- */

    @Override public FluidTank[] getAllTanks() { return this.tanks; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] {this.tanks[1], this.tanks[2]}; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {this.tanks[0]}; }

    /* --- Sonstiges --- */

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new WatzMenu(id, inventory, this);
    }

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 3, y, z - 3, x + 4, y + SEGMENT_HEIGHT, z + 4);
        }
        return this.renderBox;
    }
}
