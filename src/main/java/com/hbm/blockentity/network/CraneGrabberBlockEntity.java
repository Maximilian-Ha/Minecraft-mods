package com.hbm.blockentity.network;

import api.hbm.conveyor.IConveyorBelt;
import com.hbm.blockentity.IControlReceiverFilter;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.ConveyorDoubleBlock;
import com.hbm.blocks.network.ConveyorTripleBlock;
import com.hbm.blocks.network.CraneBaseBlock;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.item.MovingItem;
import com.hbm.inventory.menus.CraneGrabberMenu;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityCraneGrabber.
 *
 * Der Greifer nimmt einen fahrenden Gegenstand von dem Band an seiner Eingangsseite und setzt
 * ihn auf das an seiner Ausgangsseite -- oder, wenn dort statt eines Bandes eine Maschine steht,
 * in diese hinein.
 *
 * ER TUT DAS NUR, UM DABEI ZU FILTERN. Zwei Baender aneinanderzulegen braucht keinen Greifer;
 * was ihn ausmacht, sind die neun Muster, nach denen er auswaehlt, was er ueberhaupt anfasst.
 * Genau deshalb hat Runde 97 ihn zurueckgestellt: ohne den Mustervergleicher waere er ein Block
 * ohne Zweck gewesen. Mit ihm ist er der Punkt, an dem sich eine Strecke gabelt.
 *
 * ER HAELT NICHTS FEST. Anders als Einleger und Auszieher hat er kein Zwischenlager: passt nichts
 * oder ist das Ziel voll, bleibt der Gegenstand liegen, wo er ist, und faehrt auf seinem Band
 * weiter. Seine neun Faecher sind ausschliesslich Musterfaecher.
 *
 * DER GREIFBEREICH schrumpft mit der Spurzahl des Bandes: auf einem Doppelband greift er nur
 * die halbe, auf einem Dreifachband nur ein Drittel der Blocktiefe ab. So trifft er die eine
 * Spur, die vor ihm liegt, und nicht die daneben. Das steht so im Original -- mitsamt der dort
 * angemerkten Ungenauigkeit: der Bereich wird verschoben, nicht verkleinert, weshalb sich ein
 * Greifer vor einem Dreifachband auch von der Seite beschicken laesst. Das ist eine Sonderlage
 * ohne praktischen Wert, und sie zu beheben waere mehr Aufwand als Nutzen.
 *
 * NICHT UEBERNOMMEN: die beiden Aufwertungen upgrade_ejector und upgrade_stack -- die
 * Gegenstaende gibt es im Port nicht. Ohne sie greift er ein Stueck alle zwanzig Ticks, genau
 * wie der frisch gesetzte des Originals.
 */
public class CraneGrabberBlockEntity extends MachineBaseBlockEntity implements IControlReceiverFilter {

    public static final int SLOTS = 9;

    /** Takt des Originals ohne Aufwertung. */
    private static final int DELAY = 20;

    /** Aus: die Muster sperren, was auf sie passt. An: sie lassen nur das durch. */
    public boolean isWhitelist = false;

    public final ModulePatternMatcher matcher = new ModulePatternMatcher(SLOTS);

    private long lastGrabbedTick = 0;

    public CraneGrabberBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CRANE_GRABBER.get(), pos, state, SLOTS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.craneGrabber");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.level.getGameTime() >= this.lastGrabbedTick + DELAY && !this.level.hasNeighborSignal(this.worldPosition)) {
            this.grabOne();
        }

        this.networkPackNT(15);
    }

    private void grabOne() {

        BlockState state = this.getBlockState();
        Direction input = CraneBaseBlock.getInput(state);
        Direction output = CraneBaseBlock.getOutput(state);

        List<MovingItem> items = this.level.getEntitiesOfClass(MovingItem.class, this.grabArea(input));
        if(items.isEmpty()) return;

        BlockPos targetPos = this.worldPosition.relative(output);
        Block targetBlock = this.level.getBlockState(targetPos).getBlock();

        for(MovingItem item : items) {

            if(item.isRemoved()) continue;

            ItemStack stack = item.getItemStack();
            if(stack.isEmpty()) continue;
            if(this.isWhitelist != this.matchesFilter(stack)) continue;

            if(targetBlock instanceof IConveyorBelt belt) {
                this.moveToBelt(item, belt, targetPos, output);
                this.lastGrabbedTick = this.level.getGameTime();
                return;
            }

            IItemHandler target = this.level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, output.getOpposite());
            if(target == null) return;

            /* Ein Stueck je Takt -- die Menge des Originals ohne Stapelaufwertung. */
            ItemStack single = stack.copyWithCount(1);
            if(!ItemHandlerHelper.insertItemStacked(target, single, false).isEmpty()) continue;

            /* ABGESCHRIEBEN, NICHT VERKLEINERT: getItemStack liefert den Stapel, wie er in den
             * synchronisierten Daten der Entitaet steht. Wer ihn an Ort und Stelle verkleinert,
             * aendert ihn zwar, meldet die Aenderung aber nicht an -- der Spieler saehe
             * weiterhin die alte Anzahl. */
            ItemStack reduced = stack.copy();
            reduced.shrink(1);

            if(reduced.isEmpty()) item.discard();
            else item.setItemStack(reduced);

            this.lastGrabbedTick = this.level.getGameTime();
            return;
        }
    }

    /**
     * Der Wuerfel, in dem gegriffen wird: er liegt vor der Eingangsseite und ist zwoelf Pixel
     * breit. Bei mehrspurigen Baendern rueckt er naeher an den Greifer heran, damit er die eine
     * Spur trifft, die vor ihm liegt.
     */
    private AABB grabArea(Direction input) {

        double reach = 1D;

        /* Zeigt der Eingang nach oben oder unten, gibt es keine Spuren -- dann bleibt es bei eins. */
        if(input.getAxis() != Direction.Axis.Y) {

            Block ahead = this.level.getBlockState(this.worldPosition.relative(input)).getBlock();
            if(ahead instanceof ConveyorDoubleBlock) reach = 0.5D;
            if(ahead instanceof ConveyorTripleBlock) reach = 1D / 3D;
        }

        double x = this.worldPosition.getX() + input.getStepX() * reach;
        double y = this.worldPosition.getY() + input.getStepY() * reach;
        double z = this.worldPosition.getZ() + input.getStepZ() * reach;

        return new AABB(x + 0.1875D, y + 0.1875D, z + 0.1875D, x + 0.8125D, y + 0.8125D, z + 0.8125D);
    }

    /** Setzt den Gegenstand auf den Rastpunkt des Zielbandes und loescht den alten. */
    private void moveToBelt(MovingItem item, IConveyorBelt belt, BlockPos beltPos, Direction output) {

        Vec3 mouth = new Vec3(
                this.worldPosition.getX() + 0.5 + output.getStepX() * 0.55,
                this.worldPosition.getY() + 0.5 + output.getStepY() * 0.55,
                this.worldPosition.getZ() + 0.5 + output.getStepZ() * 0.55);

        Vec3 snap = belt.getClosestSnappingPosition(this.level, beltPos, mouth);

        MovingItem moved = new MovingItem(NtmEntityTypes.MOVING_ITEM.get(), this.level);
        moved.setItemStack(item.getItemStack().copy());
        moved.moveTo(snap.x, snap.y, snap.z, 0F, 0F);

        item.discard();
        this.level.addFreshEntity(moved);
    }

    /* Musterfaecher halten kein Gut: von aussen ist hier nichts zu holen und nichts abzulegen. */
    @Override public int[] getSlotsForFace(Direction direction) { return NO_SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) { return false; }
    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) { return false; }
    @Override public boolean canPlaceItem(int index, ItemStack stack) { return false; }

    /**
     * Aus einem Musterfach laesst sich nichts entnehmen -- auch nicht ueber eine Huelle, die
     * nach der Seite gar nicht erst fragt. Beim Greifer sind das alle neun.
     */
    @Override public ItemStack removeItem(int index, int count) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItemNoUpdate(int index) { return ItemStack.EMPTY; }

    private static final int[] NO_SLOTS = new int[0];

    /** Der gemeinsame Griff aus IControlReceiverFilter, mit den Grenzen dieser Maschine. */
    public boolean matchesFilter(ItemStack stack) {
        return IControlReceiverFilter.matches(this, this.matcher, 0, SLOTS, stack);
    }

    @Override public int[] getFilterSlots() { return new int[] { 0, SLOTS }; }
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
        return new CraneGrabberMenu(id, inventory, this);
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
        this.lastGrabbedTick = tag.getLong("lastGrabbedTick");
        this.matcher.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("isWhitelist", this.isWhitelist);
        tag.putLong("lastGrabbedTick", this.lastGrabbedTick);
        this.matcher.save(tag);
    }
}
