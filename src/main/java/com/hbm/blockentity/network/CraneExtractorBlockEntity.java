package com.hbm.blockentity.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blockentity.IControlReceiverFilter;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.CraneBaseBlock;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.item.MovingItem;
import com.hbm.inventory.menus.CraneExtractorMenu;
import com.hbm.module.ModulePatternMatcher;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityCraneExtractor.
 *
 * Der Auszieher ist das Gegenstueck zum Einleger: er holt aus der Maschine an seiner einen Seite
 * und setzt das Geholte auf das Band an seiner anderen. Damit schliesst sich die Strecke --
 * Maschine, Band, Maschine.
 *
 * Man beachte die VERTAUSCHUNG: gezogen wird an der AUSGANGSseite, abgelegt an der
 * EINGANGSseite. Das steht so im Original -- die beiden Seiten heissen dort nach der Sicht des
 * Bandes, nicht nach der des Ausziehers.
 *
 * NEUN MUSTER stehen als Filter davor. Sie lassen sich als Weiss- oder Schwarzliste fuehren:
 * voreingestellt ist die Schwarzliste, und weil eine leere Schwarzliste auf nichts passt, zieht
 * der frisch gesetzte Auszieher alles. Wie genau ein Muster vergleicht, haelt der
 * Mustervergleicher fest -- genau der Gegenstand, nur der Gegenstand, oder ein ganzer Tag.
 *
 * DAS ZWISCHENLAGER dahinter nimmt auf, was gerade kein Band abnimmt; sobald wieder eines steht,
 * wird es zuerst geleert. Beim Leeren zaehlt der Filter NICHT mehr: was einmal drin ist, soll
 * auch wieder heraus.
 *
 * NICHT UEBERNOMMEN: die beiden Aufwertungen upgrade_ejector und upgrade_stack, die Takt und
 * Menge erhoehen -- die Gegenstaende gibt es im Port nicht. Mit ihnen faellt auch der Schalter
 * "nur volle Stapel" weg: er vergleicht die gefundene Menge mit der Menge JE ZUG, und die ist
 * ohne die Stapelaufwertung immer eins. Ein Schalter, der nie etwas aendern kann, waere
 * schlechter als keiner.
 */
public class CraneExtractorBlockEntity extends MachineBaseBlockEntity implements IControlReceiverFilter {

    /** Neun Muster als Filter, dahinter neun Faecher als Zwischenlager. */
    public static final int PATTERNS = 9;
    public static final int BUFFER = 9;
    public static final int SLOTS = PATTERNS + BUFFER;

    /** Takt des Originals ohne Aufwertung. */
    private static final int DELAY = 20;

    /** Aus: die Muster sperren, was auf sie passt. An: sie lassen nur das durch. */
    public boolean isWhitelist = false;

    public final ModulePatternMatcher matcher = new ModulePatternMatcher(PATTERNS);

    public CraneExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CRANE_EXTRACTOR.get(), pos, state, SLOTS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.craneExtractor");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.level.getGameTime() % DELAY == 0 && !this.level.hasNeighborSignal(this.worldPosition)) {
            this.extractOne();
        }

        this.networkPackNT(15);
    }

    private void extractOne() {

        BlockState state = this.getBlockState();
        Direction source = CraneBaseBlock.getOutput(state);
        Direction target = CraneBaseBlock.getInput(state);

        BlockPos beltPos = this.worldPosition.relative(target);
        IConveyorBelt belt = this.level.getBlockState(beltPos).getBlock() instanceof IConveyorBelt icb ? icb : null;

        IItemHandler from = this.level.getCapability(Capabilities.ItemHandler.BLOCK,
                this.worldPosition.relative(source), source.getOpposite());

        if(from != null) {
            for(int slot = 0; slot < from.getSlots(); slot++) {

                /* Erst ohne Entnahme schauen, was da liegt -- sonst zoege der Filter den
                 * Gegenstand heraus, nur um ihn gleich wieder abzulehnen. */
                ItemStack found = from.getStackInSlot(slot);
                if(found.isEmpty() || !this.passesFilter(found)) continue;

                ItemStack pulled = from.extractItem(slot, 1, false);
                if(pulled.isEmpty()) continue;

                if(belt != null) this.sendItem(pulled, belt, beltPos, target);
                else this.store(pulled);

                return;
            }
        }

        /* Kam nichts aus der Maschine, wird das Zwischenlager geleert -- ohne Ansehen der Ware. */
        if(belt == null) return;

        for(int i = PATTERNS; i < this.slots.size(); i++) {

            ItemStack stack = this.slots.get(i);
            if(stack.isEmpty()) continue;

            this.sendItem(this.removeItem(i, 1), belt, beltPos, target);
            return;
        }
    }

    /**
     * Die Weiss-Schwarz-Logik des Originals: bei Weissliste muss das Muster passen, bei
     * Schwarzliste darf es das nicht.
     */
    private boolean passesFilter(ItemStack stack) {
        boolean match = this.matchesFilter(stack);
        return this.isWhitelist == match;
    }

    /**
     * Setzt den Gegenstand auf den Rastpunkt des Bandes. Ist das Band zugleich eine annehmende
     * Maschine -- ein Einleger etwa --, wird er unmittelbar hineingegeben, statt erst ein Stueck
     * zu fahren.
     */
    private void sendItem(ItemStack stack, IConveyorBelt belt, BlockPos beltPos, Direction target) {

        Vec3 mouth = new Vec3(
                this.worldPosition.getX() + 0.5 + target.getStepX() * 0.55,
                this.worldPosition.getY() + 0.5 + target.getStepY() * 0.55,
                this.worldPosition.getZ() + 0.5 + target.getStepZ() * 0.55);

        Vec3 snap = belt.getClosestSnappingPosition(this.level, beltPos, mouth);

        MovingItem moving = new MovingItem(NtmEntityTypes.MOVING_ITEM.get(), this.level);
        moving.setItemStack(stack);
        moving.moveTo(snap.x, snap.y, snap.z, 0F, 0F);

        if(belt instanceof IEnterableBlock enterable
                && enterable.canItemEnter(this.level, beltPos, target.getOpposite(), moving)) {
            enterable.onItemEnter(this.level, beltPos, target.getOpposite(), moving);
            return;
        }

        this.level.addFreshEntity(moving);
    }

    /** Ins Zwischenlager. Was nicht mehr hineinpasst, faellt zu Boden. */
    private void store(ItemStack stack) {
        ItemStack rest = ItemHandlerHelper.insertItemStacked(new net.neoforged.neoforge.items.wrapper.InvWrapper(this), stack, false);
        if(!rest.isEmpty()) this.level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, rest));
    }

    /* Nach aussen sichtbar ist nur das Zwischenlager -- die Musterfaecher halten kein Gut. */
    @Override public int[] getSlotsForFace(Direction direction) { return BUFFER_SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) { return index >= PATTERNS; }
    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) { return index >= PATTERNS; }
    @Override public boolean canPlaceItem(int index, ItemStack stack) { return index >= PATTERNS; }

    /**
     * Aus einem Musterfach laesst sich nichts entnehmen -- auch nicht ueber eine Huelle, die
     * nach der Seite gar nicht erst fragt. Ohne diese Sperre liesse sich das Abbild als
     * Gegenstand herausziehen, und damit waere aus dem Nichts etwas geworden.
     */
    @Override
    public ItemStack removeItem(int index, int count) {
        return index < PATTERNS ? ItemStack.EMPTY : super.removeItem(index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return index < PATTERNS ? ItemStack.EMPTY : super.removeItemNoUpdate(index);
    }

    private static final int[] BUFFER_SLOTS = bufferSlots();

    private static int[] bufferSlots() {
        int[] slots = new int[BUFFER];
        for(int i = 0; i < BUFFER; i++) slots[i] = PATTERNS + i;
        return slots;
    }

    /** Der gemeinsame Griff aus IControlReceiverFilter, mit den Grenzen dieser Maschine. */
    public boolean matchesFilter(ItemStack stack) {
        return IControlReceiverFilter.matches(this, this.matcher, 0, PATTERNS, stack);
    }

    @Override public int[] getFilterSlots() { return new int[] { 0, PATTERNS }; }
    @Override public void nextMode(int i) { this.matcher.nextMode(this.level, this.slots.get(i), i); this.setChanged(); }
    @Override public void initPattern(int i) { this.matcher.initPatternStandard(this.level, this.slots.get(i), i); this.setChanged(); }

    @Override
    public boolean hasPermission(Player player) {
        return this.stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag data) {
        if(data.contains("whitelist")) this.isWhitelist = !this.isWhitelist;
        this.setChanged();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CraneExtractorMenu(id, inventory, this);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.isWhitelist);
        this.matcher.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.isWhitelist = buf.readBoolean();
        this.matcher.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.isWhitelist = tag.getBoolean("isWhitelist");
        this.matcher.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("isWhitelist", this.isWhitelist);
        this.matcher.save(tag);
    }
}
