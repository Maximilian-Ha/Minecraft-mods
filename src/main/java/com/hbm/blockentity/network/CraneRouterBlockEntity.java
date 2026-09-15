package com.hbm.blockentity.network;

import com.hbm.blockentity.IControlReceiverFilter;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.menus.CraneRouterMenu;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityCraneRouter.
 *
 * Der Verteiler ist die Kreuzung des Foerdernetzes. Was in ihn hineinfaehrt -- ein einzelner
 * Gegenstand oder ein ganzes Paket --, wird nach Muster auf bis zu sechs Seiten aufgeteilt.
 *
 * JEDE SEITE FUEHRT IHREN EIGENEN FILTER aus fuenf Mustern und ihre eigene Betriebsart:
 *   AUS       -- diese Seite kommt nicht in Frage,
 *   WEISS     -- sie nimmt, was auf ihre Muster passt,
 *   SCHWARZ   -- sie nimmt, was NICHT darauf passt,
 *   AUFFANG   -- sie nimmt nur, was keine andere Seite haben wollte.
 *
 * DER AUFFANG IST DER ZWEITE DURCHGANG, nicht der erste. Erst werden alle Weiss- und
 * Schwarzseiten gefragt; nur wenn keine zusagt, kommen die Auffangseiten zum Zuge. Das ist der
 * Unterschied zwischen "sortiere Eisen hierhin" und "alles Uebrige dorthin".
 *
 * KOMMEN MEHRERE SEITEN IN FRAGE, entscheidet der Zufall. Das steht so im Original und ist auch
 * sinnvoll: zwei gleich eingestellte Seiten teilen sich damit die Last, statt dass die erste
 * alles bekommt.
 *
 * WER NIRGENDS HINPASST, faellt in der Mitte zu Boden. Ihn verschwinden zu lassen waere
 * bequemer, aber ein Verteiler, der bei einem vergessenen Muster still alles frisst, waere eine
 * Falle.
 *
 * ER HAT KEINEN TAKT. Verteilt wird in dem Augenblick, in dem etwas hineinfaehrt -- die Arbeit
 * steht im Block, nicht hier. Diese Blockentitaet haelt nur die Einstellungen.
 */
public class CraneRouterBlockEntity extends MachineBaseBlockEntity implements IControlReceiverFilter {

    /** Sechs Seiten zu fuenf Mustern. */
    public static final int SIDES = 6;
    public static final int PATTERNS_PER_SIDE = 5;
    public static final int SLOTS = SIDES * PATTERNS_PER_SIDE;

    public static final int MODE_NONE = 0;
    public static final int MODE_WHITELIST = 1;
    public static final int MODE_BLACKLIST = 2;
    public static final int MODE_WILDCARD = 3;
    public static final int MODES = 4;

    public final ModulePatternMatcher[] patterns = new ModulePatternMatcher[SIDES];
    public final int[] modes = new int[SIDES];

    public CraneRouterBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CRANE_ROUTER.get(), pos, state, SLOTS);

        for(int i = 0; i < SIDES; i++) this.patterns[i] = new ModulePatternMatcher(PATTERNS_PER_SIDE);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.craneRouter");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.networkPackNT(15);
    }

    /**
     * Auf welche Seite gehoert dieser Gegenstand? Gibt null zurueck, wenn keine Seite ihn will --
     * dann faellt er zu Boden.
     */
    @Nullable
    public Direction getOutputDir(ItemStack stack) {

        List<Direction> valid = new ArrayList<>();

        for(int side = 0; side < SIDES; side++) {

            int mode = this.modes[side];
            if(mode == MODE_NONE || mode == MODE_WILDCARD) continue;

            int from = side * PATTERNS_PER_SIDE;
            boolean match = IControlReceiverFilter.matches(this, this.patterns[side], from, from + PATTERNS_PER_SIDE, stack);

            if(mode == MODE_WHITELIST == match) valid.add(Direction.from3DDataValue(side));
        }

        /* Erst wenn keine der eingestellten Seiten zusagt, kommen die Auffangseiten. */
        if(valid.isEmpty()) {
            for(int side = 0; side < SIDES; side++) {
                if(this.modes[side] == MODE_WILDCARD) valid.add(Direction.from3DDataValue(side));
            }
        }

        if(valid.isEmpty()) return null;

        return valid.get(this.level.random.nextInt(valid.size()));
    }

    /* Musterfaecher halten Abbilder: von aussen ist hier nichts zu holen und nichts abzulegen. */
    @Override public int[] getSlotsForFace(Direction direction) { return NO_SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) { return false; }
    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) { return false; }
    @Override public boolean canPlaceItem(int index, ItemStack stack) { return false; }
    @Override public ItemStack removeItem(int index, int count) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItemNoUpdate(int index) { return ItemStack.EMPTY; }

    private static final int[] NO_SLOTS = new int[0];

    @Override public int[] getFilterSlots() { return new int[] { 0, SLOTS }; }

    @Override
    public void nextMode(int i) {
        this.patterns[i / PATTERNS_PER_SIDE].nextMode(this.level, this.slots.get(i), i % PATTERNS_PER_SIDE);
        this.setChanged();
    }

    /**
     * Der Verteiler benutzt als einziger die KLUGE Voreinstellung: legt man einen Stahlbarren
     * als Muster, steht die Art gleich auf "c:ingots/steel" statt auf dem einen Gegenstand.
     * Das ist bei einer Sortieranlage fast immer das Gemeinte -- man will die Barren, nicht
     * genau diesen einen.
     */
    @Override
    public void initPattern(int i) {
        this.patterns[i / PATTERNS_PER_SIDE].initPatternSmart(this.level, this.slots.get(i), i % PATTERNS_PER_SIDE);
        this.setChanged();
    }

    @Override
    public boolean hasPermission(Player player) {
        return this.stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag data) {

        if(data.contains("toggle")) {
            int side = data.getInt("toggle");
            if(side < 0 || side >= SIDES) return;
            this.modes[side] = (this.modes[side] + 1) % MODES;
            this.setChanged();
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CraneRouterMenu(id, inventory, this);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(ModulePatternMatcher pattern : this.patterns) pattern.serialize(buf);
        for(int mode : this.modes) buf.writeByte(mode);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(ModulePatternMatcher pattern : this.patterns) pattern.deserialize(buf);
        for(int i = 0; i < SIDES; i++) this.modes[i] = buf.readByte();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        super.loadAdditional(tag, registries);

        for(int i = 0; i < SIDES; i++) this.patterns[i].load(tag.getCompound("pattern" + i));

        int[] stored = tag.getIntArray("modes");
        for(int i = 0; i < SIDES; i++) this.modes[i] = i < stored.length ? stored[i] : MODE_NONE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        super.saveAdditional(tag, registries);

        for(int i = 0; i < SIDES; i++) {
            CompoundTag pattern = new CompoundTag();
            this.patterns[i].save(pattern);
            tag.put("pattern" + i, pattern);
        }

        tag.putIntArray("modes", this.modes);
    }
}
