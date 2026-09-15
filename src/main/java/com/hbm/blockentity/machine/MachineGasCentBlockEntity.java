package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineGasCentMenu;
import com.hbm.inventory.recipes.GasCentrifugeRecipes;
import com.hbm.inventory.recipes.GasCentrifugeRecipes.PseudoFluidType;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.main.NuclearTechMod;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.InventoryUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineGasCent.
 *
 * Die Gaszentrifuge trennt Uranhexafluorid nach Gewicht. Sie ist der Abnehmer, der dem Port
 * bisher fehlte: die Chemieanlage stellt UF6 und PuF6 her, und bis jetzt konnte man damit
 * nichts anfangen.
 *
 * SIE ARBEITET NUR IN DER KETTE. Eine einzelne Zentrifuge macht aus Natururan schwach
 * angereichertes und bleibt dann stehen; das Ergebnis ist ein PSEUDOFLUID, das kein Rohr
 * befoerdert. Weiter kommt es nur, indem die Zentrifuge es der naechsten HINTER SICH
 * hinueberreicht -- "hinter sich" heisst entgegen der Blickrichtung. Vier hintereinander
 * gestellte Zentrifugen ergeben die volle Kaskade bis zum hochangereicherten Stoff.
 *
 * DIE LETZTE STUFE BRAUCHT DIE SCHNELLE ZENTRIFUGE. Ohne die Aufwertung im siebten Fach bleibt
 * die Kette beim mittelangereicherten Stoff stehen -- das ist die Sperre, die im Original vor
 * dem Waffenuran steht.
 *
 * WER NICHT WEITERREICHEN KANN, BAUT BRENNSTOFF. Steht hinter einer Zentrifuge, die schwach
 * angereicherten Stoff verarbeitet, keine weitere, so macht sie aus sechshundert Millibar ihres
 * Erzeugnisses -- des mittelangereicherten -- sechs Brennstoffnuggets und Fluorit. So ist eine
 * kurze Kette nicht nutzlos, sondern der normale Weg zum Reaktorbrennstoff; genau das meint der
 * Hinweistext "zwei Zentrifugen ergeben Brennstoff, vier die volle Trennung".
 *
 * ABWEICHUNG: das Weiterreichen sieht auf den Block GENAU HINTER dem Kern, nicht auf einen
 * Bereich. So steht es im Original, und es ist die einzige Stelle, an der die Ausrichtung der
 * Maschine ueberhaupt zaehlt.
 *
 * NICHT UEBERNOMMEN: die Anbindung an Energy Control, die es im Port nicht gibt, und das
 * Modell des Originals. Die Abmessungen stimmen: ein Block Grundflaeche, vier hoch.
 */
public class MachineGasCentBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardReceiverMK2 {

    public static final int SLOT_OUTPUT_START = 0;
    public static final int SLOT_OUTPUT_END = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_IDENTIFIER = 5;
    public static final int SLOT_UPGRADE = 6;

    public static final long maxPower = 100_000;
    public static final int processingSpeed = 150;

    /** Was die schnelle Zentrifuge an Dauer spart, und was sie an Strom mehr zieht. */
    public static final int fastBonus = 70;
    public static final int drawSlow = 200;
    public static final int drawFast = 300;

    /** Ohne Nachfolger: soviel schwach angereicherter Stoff wird zu Brennstoff. */
    public static final int fuelConversion = 600;

    public long power;
    public int progress;
    public boolean isProgressing;

    /** Das Rohrfluid; nur hierueber kommt etwas herein. */
    public final FluidTank tank = new FluidTank(Fluids.UF6, 2_000);

    public final PseudoFluidTank inputTank = new PseudoFluidTank(PseudoFluidType.NUF6, 8_000);
    public final PseudoFluidTank outputTank = new PseudoFluidTank(PseudoFluidType.LEUF6, 8_000);

    /** Der Ton laeuft an und aus, statt hart zu schalten -- eine Zentrifuge braucht ihre Zeit. */
    private int audioDuration;
    private AudioWrapper audio;

    private AABB renderBox;

    private static final int[] ACCESS = { 0, 1, 2, 3 };

    public MachineGasCentBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_GAS_CENT.get(), pos, state, 7);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.gasCentrifuge");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.updateConnections();

            this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, maxPower);
            this.setTankType();

            /* Nachfuellen aus dem Rohr geht nur, solange die erste Stufe ansteht; danach ist der
             * Inhalt ein Pseudofluid, und Rohrfluid wuerde ihn verduennen. */
            if(GasCentrifugeRecipes.fluidConversions.containsValue(this.inputTank.getTankType())) {
                this.attemptConversion();
            }

            if(this.canEnrich()) {

                this.isProgressing = true;
                this.progress++;
                this.power -= this.isFast() ? drawFast : drawSlow;

                if(this.power < 0) {
                    this.power = 0;
                    this.progress = 0;
                }

                if(this.progress >= this.getProcessingSpeed()) this.enrich();

            } else {
                this.isProgressing = false;
                this.progress = 0;
            }

            if(this.level.getGameTime() % 10 == 0) {
                this.handOver();
            }

            this.networkPackNT(50);

        } else {
            this.updateAudio();
        }
    }

    /* --- Anreicherung --- */

    public boolean isFast() {
        return this.slots.get(SLOT_UPGRADE).getItem() == NtmItems.UPGRADE_GC_SPEED.get();
    }

    public int getProcessingSpeed() {
        return this.isFast() ? processingSpeed - fastBonus : processingSpeed;
    }

    private boolean canEnrich() {

        PseudoFluidType type = this.inputTank.getTankType();

        if(this.power <= 0) return false;
        if(this.inputTank.getFill() < type.getFluidConsumed()) return false;
        if(this.outputTank.getFill() + type.getFluidProduced() > this.outputTank.getMaxFill()) return false;

        if(type.getIfHighSpeed() && !this.isFast()) return false;

        ItemStack[] output = type.getOutput();
        if(output.length < 1) return false;

        return InventoryUtil.doesArrayHaveSpace(this.slots, SLOT_OUTPUT_START, SLOT_OUTPUT_END, output);
    }

    private void enrich() {

        PseudoFluidType type = this.inputTank.getTankType();

        this.progress = 0;
        this.inputTank.setFill(this.inputTank.getFill() - type.getFluidConsumed());
        this.outputTank.setFill(this.outputTank.getFill() + type.getFluidProduced());

        for(ItemStack stack : type.getOutput()) {
            InventoryUtil.tryAddItemToInventory(this.slots, SLOT_OUTPUT_START, SLOT_OUTPUT_END, stack);
        }

        this.setChanged();
    }

    /** Rohrfluid wird zur ersten Stufe: eins zu eins, nur der Name aendert sich. */
    private void attemptConversion() {

        if(this.inputTank.getFill() >= this.inputTank.getMaxFill()) return;
        if(this.tank.getFill() <= 0) return;

        int fill = Math.min(this.inputTank.getMaxFill() - this.inputTank.getFill(), this.tank.getFill());

        this.tank.setFill(this.tank.getFill() - fill);
        this.inputTank.setFill(this.inputTank.getFill() + fill);
    }

    /**
     * Das Erzeugnis an die Zentrifuge hinter dieser weiterreichen. Geht das nicht, und steht
     * schwach angereicherter Stoff an, wird stattdessen Brennstoff daraus.
     */
    private void handOver() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        BlockEntity behind = this.level.getBlockEntity(this.worldPosition.relative(dir.getOpposite()));

        if(this.attemptTransfer(behind)) return;

        /* Die Bedingung steht auf dem EINGANG, verbraucht wird aber der AUSGANG: wer schwach
         * angereichertes verarbeitet, hat mittelangereichertes stehen, und daraus wird der
         * Brennstoff. So im Original. */
        if(this.inputTank.getTankType() != PseudoFluidType.LEUF6) return;
        if(this.outputTank.getFill() < fuelConversion) return;

        ItemStack[] converted = {
                new ItemStack(NtmItems.NUGGET_URANIUM_FUEL.get(), 6),
                new ItemStack(NtmItems.FLUORITE.get(), 1)
        };

        if(!InventoryUtil.doesArrayHaveSpace(this.slots, SLOT_OUTPUT_START, SLOT_OUTPUT_END, converted)) return;

        this.outputTank.setFill(this.outputTank.getFill() - fuelConversion);
        for(ItemStack stack : converted) InventoryUtil.tryAddItemToInventory(this.slots, SLOT_OUTPUT_START, SLOT_OUTPUT_END, stack);
        this.setChanged();
    }

    /**
     * Uebergabe an die naechste Zentrifuge. Sie muss dasselbe Rohrfluid eingestellt haben --
     * sonst liefe Uran in eine Plutoniumkette. Ist ihre Stufe noch nicht gesetzt, setzt diese
     * hier sie mit.
     */
    private boolean attemptTransfer(BlockEntity be) {

        if(!(be instanceof MachineGasCentBlockEntity cent)) return false;
        if(cent.tank.getTankType() != this.tank.getTankType()) return false;

        PseudoFluidType out = this.outputTank.getTankType();

        if(out == PseudoFluidType.NONE) return true;

        if(cent.inputTank.getTankType() != out) {
            cent.inputTank.setTankType(out);
            cent.outputTank.setTankType(out.getOutputType());
        }

        int fill = Math.min(cent.inputTank.getMaxFill() - cent.inputTank.getFill(), this.outputTank.getFill());

        if(fill > 0) {
            this.outputTank.setFill(this.outputTank.getFill() - fill);
            cent.inputTank.setFill(cent.inputTank.getFill() + fill);
            cent.setChanged();
            this.setChanged();
        }

        return true;
    }

    /**
     * Die Fluidkennung im sechsten Fach waehlt die Kette. Nur Fluide mit einer Kette werden
     * angenommen -- eine Kennung fuer Wasser laesst die Maschine unveraendert.
     */
    private void setTankType() {

        ItemStack stack = this.slots.get(SLOT_IDENTIFIER);
        if(!(stack.getItem() instanceof IItemFluidIdentifier id)) return;

        FluidType newType = id.getType(this.level, this.worldPosition, stack);
        if(this.tank.getTankType() == newType) return;

        PseudoFluidType pseudo = GasCentrifugeRecipes.fluidConversions.get(newType);
        if(pseudo == null) return;

        this.inputTank.setTankType(pseudo);
        this.outputTank.setTankType(pseudo.getOutputType());
        this.tank.setTankType(newType);
        this.setChanged();
    }

    private void updateConnections() {

        for(DirPos pos : this.getConPos()) {
            this.trySubscribe(this.level, pos);

            /* Nur solange die erste Stufe ansteht, wird Rohrfluid abgerufen. */
            if(GasCentrifugeRecipes.fluidConversions.containsValue(this.inputTank.getTankType())) {
                this.trySubscribe(this.tank.getTankType(), this.level, pos);
            }
        }
    }

    private DirPos[] getConPos() {
        BlockPos p = this.worldPosition;
        return new DirPos[] {
                new DirPos(p.below(), Direction.DOWN),
                new DirPos(p.east(), Direction.EAST),
                new DirPos(p.west(), Direction.WEST),
                new DirPos(p.south(), Direction.SOUTH),
                new DirPos(p.north(), Direction.NORTH)
        };
    }

    /* --- Ton --- */

    private void updateAudio() {

        this.audioDuration = Mth.clamp(this.audioDuration + (this.isProgressing ? 2 : -3), 0, 60);

        if(this.audioDuration > 10 && NuclearTechMod.proxy.me() != null
                && NuclearTechMod.proxy.me().distanceToSqr(this.getBlockPos().getCenter()) < 625) {

            if(this.audio == null) {
                this.audio = this.createAudioLoop();
                if(this.audio != null) this.audio.startSound();
            } else if(!this.audio.isPlaying()) {
                this.audio = this.rebootAudio(this.audio);
            }

            if(this.audio != null) {
                this.audio.updateVolume(this.getVolume(1F));
                this.audio.updatePitch((this.audioDuration - 10) / 100F + 0.5F);
                this.audio.keepAlive();
            }

        } else if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(NtmSoundEvents.CENTRIFUGE_OPERATE.get(), SoundSource.BLOCKS, this, 1F, 10F, 1F, 20);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.stopAudio();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.stopAudio();
    }

    private void stopAudio() {
        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    /* --- Container --- */

    @Override public int[] getSlotsForFace(Direction direction) { return ACCESS; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch(slot) {
            case SLOT_BATTERY -> stack.getItem() instanceof IBatteryItem;
            case SLOT_IDENTIFIER -> stack.getItem() instanceof IItemFluidIdentifier;
            case SLOT_UPGRADE -> stack.getItem() == NtmItems.UPGRADE_GC_SPEED.get();
            default -> false;
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index <= SLOT_OUTPUT_END;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineGasCentMenu(id, inventory, this);
    }

    /* --- Strom und Fluid --- */

    @Override public void setPower(long power) { this.power = power; }
    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }

    /* --- Speichern --- */

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.progress);
        buf.writeBoolean(this.isProgressing);
        buf.writeInt(this.inputTank.getFill());
        buf.writeInt(this.outputTank.getFill());
        buf.writeUtf(this.inputTank.getTankType().name);
        buf.writeUtf(this.outputTank.getTankType().name);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.progress = buf.readInt();
        this.isProgressing = buf.readBoolean();
        int in = buf.readInt();
        int out = buf.readInt();
        this.inputTank.setTankType(PseudoFluidType.byName(buf.readUtf()));
        this.outputTank.setTankType(PseudoFluidType.byName(buf.readUtf()));
        this.inputTank.setFill(in);
        this.outputTank.setFill(out);
        this.tank.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.progress = tag.getShort("progress");
        this.tank.readFromNBT(tag, "tank");
        this.inputTank.readFromNBT(tag, "inputTank");
        this.outputTank.readFromNBT(tag, "outputTank");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putShort("progress", (short) this.progress);
        this.tank.writeToNBT(tag, "tank");
        this.inputTank.writeToNBT(tag, "inputTank");
        this.outputTank.writeToNBT(tag, "outputTank");
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 5, p.getZ() + 1);
        }
        return this.renderBox;
    }

    /**
     * Ein Tank fuer ein Pseudofluid. Er sieht aus wie ein FluidTank, ist aber keiner: er haengt
     * an keinem Rohrnetz und kennt keinen Druck. Genau darum geht es -- was hier drinsteht, kann
     * das Bauwerk nicht verlassen.
     *
     * DER WECHSEL DER STUFE LEERT IHN. Anders ginge es nicht: die Fuellung einer Stufe ist in
     * einer anderen kein sinnvoller Wert.
     */
    public static class PseudoFluidTank {

        private PseudoFluidType type;
        private int fluid;
        private int maxFluid;

        public PseudoFluidTank(PseudoFluidType type, int maxFluid) {
            this.type = type;
            this.maxFluid = maxFluid;
        }

        public void setFill(int i) { this.fluid = i; }
        public int getFill() { return this.fluid; }
        public int getMaxFill() { return this.maxFluid; }
        public PseudoFluidType getTankType() { return this.type; }

        public void setTankType(PseudoFluidType type) {
            if(this.type == type) return;
            this.type = type == null ? PseudoFluidType.NONE : type;
            this.setFill(0);
        }

        public void writeToNBT(CompoundTag tag, String key) {
            tag.putInt(key, this.fluid);
            tag.putInt(key + "_max", this.maxFluid);
            tag.putString(key + "_type", this.type.name);
        }

        public void readFromNBT(CompoundTag tag, String key) {
            this.fluid = tag.getInt(key);
            int max = tag.getInt(key + "_max");
            if(max > 0) this.maxFluid = max;
            this.type = PseudoFluidType.byName(tag.getString(key + "_type"));
        }
    }
}
