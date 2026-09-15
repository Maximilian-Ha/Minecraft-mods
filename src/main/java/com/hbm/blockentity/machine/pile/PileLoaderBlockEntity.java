package com.hbm.blockentity.machine.pile;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.pile.PileCoreBlockEntity.PileChannel;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.pile.PileBlock;
import com.hbm.blocks.states.PileBlockType;
import com.hbm.items.machine.PileRodItem;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.pile.TileEntityPileLoader.
 *
 * Der Nachlader. Er haelt genau einen Stab bereit und schiebt ihn auf Knopfdruck oder
 * Redstoneflanke in den Brennstoffkanal. Was hinten herausfaellt, faellt heraus -- der Kanal
 * schiebt seinen Inhalt weiter.
 *
 * Der Vorgang laeuft in sieben Schritten hin und in sieben zurueck; erst am Umkehrpunkt wird der
 * Stab tatsaechlich uebergeben. Das ist reine Anzeige, aber es ist die Anzeige, an der man sieht,
 * ob die Anlage noch nachgeladen wird.
 *
 * FEHLER DES ORIGINALS BEHOBEN: der Redstonezustand wird unter "wasRedstone" gespeichert, aber
 * unter "redstone" wieder gelesen. Nach jedem Neuladen der Welt stand er damit auf falsch, und
 * ein anliegendes Signal loeste sofort noch einmal aus. Hier steht beide Male derselbe Schluessel.
 *
 * ABWEICHUNGEN: die OpenComputers-Anbindung und die Radio-Werte sind gestrichen
 * (ENTSCHEIDUNGEN.md). Den Klang des Verschlusses gibt es im Port nicht; hier klickt dasselbe
 * Einsteckgeraeusch wie beim Einlegen.
 */
public class PileLoaderBlockEntity extends PileDeviceBaseBlockEntity implements WorldlyContainer {

    /** Sieben Schritte hin, sieben zurueck. */
    public static final double SPEED = 1D / 7D;

    private static final int[] SLOTS = new int[] {0};

    private final NonNullList<ItemStack> slots = NonNullList.withSize(1, ItemStack.EMPTY);

    public boolean loading = false;
    public int delay = 0;
    public boolean wasRedstone;

    /** Wie weit der Schieber vorgefahren ist. */
    public double progress;

    /* Was die Anzeige ueber den Kanal dahinter weiss. */
    public ItemStack channelStack = ItemStack.EMPTY;
    public double channelDepletion;
    public double channelTemp;

    /* Nur auf der Client-Seite: geglaettete Bewegung zwischen zwei Paketen. */
    public double syncLevel;
    public double lastLevel;
    public int turnProgress;
    public ItemStack syncStack = ItemStack.EMPTY;

    public PileLoaderBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.PILE_LOADER.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.level.isClientSide) {

            this.lastLevel = this.progress;

            if(this.turnProgress > 0) {
                this.progress += (this.syncLevel - this.progress) / this.turnProgress;
                this.turnProgress--;
            } else {
                this.progress = this.syncLevel;
            }

            return;
        }

        Direction dir = this.getOrientation();
        PileChannel fuelChan = null;

        this.channelStack = ItemStack.EMPTY;
        this.channelDepletion = 0D;
        this.channelTemp = 0D;

        BlockPos pilePos = this.worldPosition.relative(dir.getOpposite());
        BlockState state = this.level.getBlockState(pilePos);

        if(state.is(NtmBlocks.PILE_BLOCK.get()) && state.getValue(PileBlock.TYPE) == PileBlockType.FUEL_IN) {

            PileCoreBlockEntity core = this.getCore(pilePos);

            if(core != null) {
                fuelChan = core.getChannel(pilePos, core.fuelChannels);

                if(fuelChan != null) {
                    this.chanNum = core.fuelChannels.indexOf(fuelChan);
                    /* Was ganz hinten liegt, ist der Stab, der als naechstes herausfaellt. */
                    this.channelStack = fuelChan.rods.get(fuelChan.rods.size() - 1);
                    this.channelDepletion = PileRodItem.getDepletionPercent(this.channelStack);
                    this.channelTemp = fuelChan.heat;
                }
            }
        }

        boolean redstone = this.level.getSignal(this.worldPosition.relative(dir), dir) > 0;
        if(redstone && !this.wasRedstone && this.delay <= 0 && this.progress <= 0D) this.loading = true;
        this.wasRedstone = redstone;

        if(this.delay > 0) {
            this.delay--;
        } else if(this.loading) {

            if(this.progress == 0D) this.click(1F);

            this.progress += SPEED;

            if(this.progress >= 1D) {
                this.progress = 1D;
                this.loading = false;
                this.delay = 5;
            }

        } else {

            /* Am Umkehrpunkt geht der Stab in den Kanal. */
            if(this.progress == 1D) {
                this.click(0.75F);
                if(fuelChan != null) {
                    fuelChan.loadItem(this.slots.get(0));
                    this.slots.set(0, ItemStack.EMPTY);
                    this.setChanged();
                }
            }

            if(this.progress > 0D) {
                this.progress -= SPEED;
                if(this.progress < 0D) this.progress = 0D;
            }
        }

        this.networkPackNT(35);
    }

    private void click(float pitch) {
        this.level.playSound(null, this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D,
                NtmSoundEvents.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1F, pitch);
    }

    public static boolean isItemLoadable(ItemStack stack) {
        return stack.getItem() instanceof PileRodItem;
    }

    /* --- Uebertragung --- */

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.progress);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.slots.get(0));
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.channelStack);
        buf.writeDouble(this.channelDepletion);
        buf.writeDouble(this.channelTemp);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);

        double lastSync = this.syncLevel;
        this.syncLevel = buf.readDouble();

        this.syncStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        this.channelStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        this.channelDepletion = buf.readDouble();
        this.channelTemp = buf.readDouble();

        if(this.syncLevel != lastSync) this.turnProgress = 2;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        this.loading = tag.getBoolean("loading");
        this.progress = tag.getDouble("level");
        this.delay = tag.getInt("delay");
        this.wasRedstone = tag.getBoolean("wasRedstone");
        this.slots.set(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.slots, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("loading", this.loading);
        tag.putDouble("level", this.progress);
        tag.putInt("delay", this.delay);
        tag.putBoolean("wasRedstone", this.wasRedstone);
        ContainerHelper.saveAllItems(tag, this.slots, registries);
    }

    /* --- Ein einziges Fach --- */

    @Override public int getContainerSize() { return 1; }
    @Override public int getMaxStackSize() { return 1; }
    @Override public boolean isEmpty() { return this.slots.get(0).isEmpty(); }
    @Override public ItemStack getItem(int slot) { return this.slots.get(0); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(this.slots, 0, amount);
        if(!removed.isEmpty()) this.setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.slots, 0);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.slots.set(0, stack);
        this.setChanged();
    }

    @Override public boolean stillValid(Player player) { return false; }
    @Override public void clearContent() { this.slots.clear(); }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return isItemLoadable(stack); }
    @Override public int[] getSlotsForFace(Direction direction) { return SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) { return isItemLoadable(stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }
}
