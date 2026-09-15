package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.menus.ReactorResearchMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.FuelRodItem;
import com.hbm.items.machine.PlateFuelItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityReactorResearch.
 *
 * Der Forschungsreaktor: zwoelf Brennstoffplatten in einem Becken, das man selbst fluten muss.
 *
 * Jede Platte bekommt den Fluss ihrer Nachbarn, reagiert danach nach ihrer eigenen Kennlinie und
 * gibt das Ergebnis weiter -- multipliziert mit der Stellung der Steuerstaebe. Bei null steht der
 * Reaktor, bei eins laeuft er durch. Die Staebe fahren mit 0,04 je Tick, eine Sollwertaenderung
 * greift also nicht sofort.
 *
 * Gekuehlt wird mit Wasser, und zwar mit dem, das um den Reaktor herum steht: bis zu zwoelf
 * Bloecke zaehlen, und jeder nimmt anteilig Waerme. Ohne Wasser kuehlt er um eine Einheit je Tick
 * -- das reicht nirgends hin. Bei 50.000 Waerme fliegt er auseinander und hinterlaesst Korium.
 *
 * Wer den Reaktor nicht abschirmt, verstrahlt die Umgebung: fehlt an einer der vier Seiten
 * Wasser, Blei, Desh oder etwas mit mindestens 100 Sprengwiderstand, geht die Strahlung durch.
 *
 * ABWEICHUNGEN:
 * - Das Original ist @Deprecated und steht trotzdem im Kreativreiter; der Port uebernimmt das.
 * - NICHT UEBERNOMMEN sind die OpenComputers-Anbindung (ENTSCHEIDUNGEN.md) und der FBI-Merker,
 *   den der Block beim Oeffnen setzt -- der gehoert zu einem Ereignis, das der Port nicht hat.
 * - Der Sonderfall, der ein gezuechtetes Meteoritenschwert bestrahlt, entfaellt: beide
 *   Gegenstaende gibt es im Port nicht.
 * - Der Nachbarschaftsgraph des Originals ist an zwei Stellen unsymmetrisch (Fach 0 zaehlt 1 und
 *   5 als Nachbarn, Fach 5 aber 0, 6 und 10). Das ist unveraendert uebernommen -- es aendert das
 *   Gleichgewicht der Anlage, und ein "Ausbessern" waere eine andere Maschine.
 */
public class ReactorResearchBlockEntity extends MachineBaseBlockEntity implements IControlReceiver {

    public static final int SLOT_COUNT = 12;
    public static final int MAX_HEAT = 50_000;
    /** Wie schnell die Steuerstaebe fahren. */
    public static final double ROD_SPEED = 0.04D;

    /** Nur Client: die Stellung im letzten Tick, fuer die Anzeige. */
    public double lastLevel;
    /**
     * Stellung der Steuerstaebe, 0 bis 1. Im Original heisst das Feld level; hier nicht, weil
     * level in 1.21 die Welt ist und das Feld der Oberklasse sonst verdeckt wuerde.
     */
    public double rodLevel;
    public double targetLevel;

    public int heat;
    public byte water;
    public int[] slotFlux = new int[SLOT_COUNT];
    public int totalFlux = 0;

    private AABB renderBox;

    /** Was aus einer abgebrannten Platte wird. */
    private static final Map<Item, ItemStack> WASTE_MAP = new HashMap<>();

    public static void initWasteMap() {
        if(!WASTE_MAP.isEmpty()) return;
        WASTE_MAP.put(NtmItems.PLATE_FUEL_U233.get(), new ItemStack(NtmItems.WASTE_PLATE_U233.get()));
        WASTE_MAP.put(NtmItems.PLATE_FUEL_U235.get(), new ItemStack(NtmItems.WASTE_PLATE_U235.get()));
        WASTE_MAP.put(NtmItems.PLATE_FUEL_MOX.get(), new ItemStack(NtmItems.WASTE_PLATE_MOX.get()));
        WASTE_MAP.put(NtmItems.PLATE_FUEL_PU239.get(), new ItemStack(NtmItems.WASTE_PLATE_PU239.get()));
        WASTE_MAP.put(NtmItems.PLATE_FUEL_SA326.get(), new ItemStack(NtmItems.WASTE_PLATE_SA326.get()));
        WASTE_MAP.put(NtmItems.PLATE_FUEL_RA226BE.get(), new ItemStack(NtmItems.WASTE_PLATE_RA226BE.get()));
        WASTE_MAP.put(NtmItems.PLATE_FUEL_PU238BE.get(), new ItemStack(NtmItems.WASTE_PLATE_PU238BE.get()));
    }

    public ReactorResearchBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.REACTOR_RESEARCH.get(), pos, state, SLOT_COUNT);
        initWasteMap();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.reactorResearch");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        this.rodControl();

        if(this.level.isClientSide) return;

        this.totalFlux = 0;

        if(this.rodLevel > 0) this.reaction();

        if(this.heat > 0) {
            this.water = this.getWater();

            if(this.water > 0) {
                this.heat -= (int) (this.heat * 0.07F * this.water / 12F);
            } else {
                this.heat -= 1;
            }

            if(this.heat < 0) this.heat = 0;
        }

        if(this.heat > MAX_HEAT) {
            this.explode();
            return;
        }

        if(this.rodLevel > 0 && this.heat > 0 && !this.isShielded()) {
            float rad = (float) this.heat / (float) MAX_HEAT * 50F;
            ChunkRadiationManager.proxy.incrementRad(this.level, this.worldPosition, rad);
        }

        this.networkPackNT(150);
    }

    /* ----- Reaktion ----- */

    /**
     * Der Nachbarschaftsgraph der zwoelf Faecher, unveraendert aus dem Original -- samt seiner
     * beiden unsymmetrischen Stellen.
     */
    private static final int[][] NEIGHBOURS = new int[][] {
            {1, 5},
            {0, 6},
            {3, 7},
            {2, 4, 8},
            {3, 9},
            {0, 6, 10},
            {1, 5, 11},
            {2, 8},
            {3, 7, 9},
            {4, 8},
            {5, 11},
            {6, 10}
    };

    private void reaction() {

        for(int i = 0; i < SLOT_COUNT; i++) {

            ItemStack stack = this.slots.get(i);

            if(stack.isEmpty() || !(stack.getItem() instanceof PlateFuelItem plate)) {
                this.slotFlux[i] = 0;
                continue;
            }

            int outFlux = plate.react(stack, this.slotFlux[i]);
            this.heat += outFlux * 2;
            this.slotFlux[i] = 0;
            this.totalFlux += outFlux;

            /* Abgebrannt: die Platte wird zur Abfallplatte. */
            if(FuelRodItem.getLifeTime(stack) > plate.lifeTime) {
                ItemStack waste = WASTE_MAP.get(stack.getItem());
                if(waste != null) this.slots.set(i, waste.copy());
            }

            for(int neighbour : NEIGHBOURS[i]) {
                this.slotFlux[neighbour] += (int) (outFlux * this.rodLevel);
            }
        }
    }

    /* ----- Kuehlung und Abschirmung ----- */

    /** Zaehlt die Wasserbloecke rund um den Reaktor; hoechstens zwoelf zaehlen. */
    public byte getWater() {

        byte count = 0;

        /* Unten und oben je einen Block, zwei Stufen entfernt. */
        if(this.isWater(this.worldPosition.offset(0, -1, 0))) count++;
        if(this.isWater(this.worldPosition.offset(0, 3, 0))) count++;

        /* An den vier Seiten je die drei Bloecke der Saeule. */
        for(Direction dir : Direction.Plane.HORIZONTAL) {
            for(int i = 0; i < 3; i++) {
                if(this.isWater(this.worldPosition.offset(dir.getStepX(), i, dir.getStepZ()))) count++;
            }
        }

        return count;
    }

    private boolean isWater(BlockPos pos) {
        return this.level.getFluidState(pos).getType() == Fluids.WATER
                || this.level.getFluidState(pos).getType() == Fluids.FLOWING_WATER;
    }

    /** Wahr, wenn alle vier Seiten auf mittlerer Hoehe die Strahlung aufhalten. */
    private boolean isShielded() {
        for(Direction dir : Direction.Plane.HORIZONTAL) {
            if(!this.blocksRad(this.worldPosition.offset(dir.getStepX(), 1, dir.getStepZ()))) return false;
        }
        return true;
    }

    private boolean blocksRad(BlockPos pos) {

        BlockState state = this.level.getBlockState(pos);

        if(state.getFluidState().getType() == Fluids.WATER) return true;

        if(state.is(NtmBlocks.BLOCK_LEAD.get())
                || state.is(NtmBlocks.BLOCK_DESH.get())
                || state.is(NtmBlocks.REACTOR_RESEARCH.get())
                || state.is(NtmBlocks.MACHINE_REACTOR_BREEDING.get())) return true;

        return state.getBlock().getExplosionResistance() >= 100F;
    }

    /* ----- Kernschmelze ----- */

    private void explode() {

        for(int i = 0; i < this.slots.size(); i++) this.slots.set(i, ItemStack.EMPTY);

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        this.level.setBlock(this.worldPosition, Blocks.AIR.defaultBlockState(), 3);

        /* Das Kuehlwasser verdampft. */
        if(this.isWater(this.worldPosition.offset(0, -1, 0))) this.level.setBlock(this.worldPosition.offset(0, -1, 0), Blocks.AIR.defaultBlockState(), 3);
        if(this.isWater(this.worldPosition.offset(0, 3, 0))) this.level.setBlock(this.worldPosition.offset(0, 3, 0), Blocks.AIR.defaultBlockState(), 3);

        for(Direction dir : Direction.Plane.HORIZONTAL) {
            for(int i = 0; i < 3; i++) {
                BlockPos pos = this.worldPosition.offset(dir.getStepX(), i, dir.getStepZ());
                if(this.isWater(pos)) this.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        this.level.explode(null, x + 0.5, y + 0.5, z + 0.5, 18.0F, ExplosionInteraction.BLOCK);

        this.level.setBlock(this.worldPosition, NtmBlocks.DECO_STEEL.get().defaultBlockState(), 3);
        this.level.setBlock(this.worldPosition.above(), NtmBlocks.BLOCK_CORIUM.get().defaultBlockState(), 3);
        this.level.setBlock(this.worldPosition.above(2), NtmBlocks.DECO_STEEL.get().defaultBlockState(), 3);

        ChunkRadiationManager.proxy.incrementRad(this.level, this.worldPosition, 50F);
    }

    /* ----- Steuerstaebe ----- */

    public void rodControl() {

        if(this.level != null && this.level.isClientSide) {
            this.lastLevel = this.rodLevel;
            return;
        }

        if(this.rodLevel < this.targetLevel) {
            this.rodLevel = Math.min(this.rodLevel + ROD_SPEED, this.targetLevel);
        } else if(this.rodLevel > this.targetLevel) {
            this.rodLevel = Math.max(this.rodLevel - ROD_SPEED, this.targetLevel);
        }
    }

    public void setTarget(double target) {
        this.targetLevel = Math.max(0D, Math.min(1D, target));
    }

    /** Die Temperatur in Kelvin, wie sie die Oberflaeche zeigt. */
    public int getDisplayHeat() {
        return (int) Math.round(this.heat * 0.00002D * 980D + 20D);
    }

    /* ----- Rahmenwerk ----- */

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot >= 0 && slot < SLOT_COUNT && stack.getItem() instanceof PlateFuelItem;
    }

    /**
     * Herausgezogen wird nur, was keine Brennstoffplatte mehr ist -- also die Abfallplatten.
     *
     * ABWEICHUNG: das Original prueft mit containsValue gegen seine Abfalltabelle. Das kann nie
     * zutreffen, weil ItemStack kein sinnvolles equals hat; der Auswurf lief also faktisch ueber
     * die zweite Bedingung. Hier steht nur noch die Bedingung, die auch greift.
     */
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return !stack.isEmpty() && !(stack.getItem() instanceof PlateFuelItem);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.heat);
        buf.writeByte(this.water);
        buf.writeDouble(this.rodLevel);
        buf.writeDouble(this.targetLevel);
        for(int flux : this.slotFlux) buf.writeInt(flux);
        buf.writeInt(this.totalFlux);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.heat = buf.readInt();
        this.water = buf.readByte();
        this.rodLevel = buf.readDouble();
        this.targetLevel = buf.readDouble();
        for(int i = 0; i < SLOT_COUNT; i++) this.slotFlux[i] = buf.readInt();
        this.totalFlux = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.heat = tag.getInt("heat");
        this.water = tag.getByte("water");
        this.rodLevel = tag.getDouble("level");
        this.targetLevel = tag.getDouble("targetLevel");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("heat", this.heat);
        tag.putByte("water", this.water);
        tag.putDouble("level", this.rodLevel);
        tag.putDouble("targetLevel", this.targetLevel);
    }

    /** Das Original laesst den Regler nur von nahebei bedienen -- zwanzig Bloecke. */
    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 20 * 20;
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("level")) {
            this.setTarget(tag.getDouble("level"));
            this.setChanged();
        }
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) this.renderBox = new AABB(this.worldPosition).inflate(1D, 2D, 1D);
        return this.renderBox;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ReactorResearchMenu(id, inventory, this);
    }
}
