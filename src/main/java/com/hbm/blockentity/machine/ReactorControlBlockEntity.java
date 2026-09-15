package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.menus.ReactorControlMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.component.NtmDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityReactorControl.
 *
 * Das Reaktorpult regelt einen Forschungsreaktor auf Temperatur. Man gibt ein Temperaturfenster
 * und die zugehoerigen Stabstellungen vor; dazwischen interpoliert es nach einer von drei
 * Kennlinien:
 *
 *   LINEAR  gerade Linie zwischen den beiden Eckpunkten
 *   QUAD    steigt langsam an und zieht spaet durch
 *   LOG     zieht frueh durch und flacht dann ab
 *
 * Unterhalb des Fensters gilt die untere Stellung, oberhalb die obere. Wer die obere Stellung
 * kleiner waehlt als die untere, bekommt einen Regler, der bei Hitze herunterfaehrt -- genau so
 * ist er gedacht.
 *
 * Welchen Reaktor es regelt, sagt ihm der Reaktorfuehler im einzigen Fach.
 *
 * ABWEICHUNGEN:
 * - NICHT UEBERNOMMEN ist die OpenComputers-Anbindung (ENTSCHEIDUNGEN.md); sie war hier die
 *   einzige Moeglichkeit, die Kennlinie ohne Oberflaeche zu setzen.
 * - Das Original sucht den Kern des Reaktors jeden Tick neu. Hier steht der Kern schon im
 *   Fuehler, weil der ihn beim Einmessen speichert.
 */
public class ReactorControlBlockEntity extends MachineBaseBlockEntity implements IControlReceiver {

    public static final int SLOT_SENSOR = 0;

    /** Nur Server: der Reaktor, den wir regeln. */
    private ReactorResearchBlockEntity reactor;

    public boolean isLinked;

    public int flux;
    public double rodLevel;
    public int heat;

    public double levelLower;
    public double levelUpper;
    public double heatLower;
    public double heatUpper;
    public RodFunction function = RodFunction.LINEAR;

    public enum RodFunction {
        LINEAR,
        QUAD,
        LOG
    }

    public ReactorControlBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.REACTOR_CONTROL.get(), pos, state, 1);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.reactorControl");
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        this.isLinked = this.establishLink();

        if(this.isLinked) {

            double lowerBound = Math.min(this.heatLower, this.heatUpper);
            double upperBound = Math.max(this.heatLower, this.heatUpper);

            double fauxLevel;

            if(this.heat < lowerBound) {
                fauxLevel = this.levelLower;
            } else if(this.heat > upperBound) {
                fauxLevel = this.levelUpper;
            } else {
                fauxLevel = this.getTargetLevel(this.function, this.heat);
            }

            double target = Mth.clamp(fauxLevel * 0.01D, 0D, 1D);

            if(target != this.rodLevel) this.reactor.setTarget(target);
        }

        this.networkPackNT(150);
    }

    /** Sucht den eingemessenen Reaktor und liest seine Werte aus. */
    private boolean establishLink() {

        ItemStack sensor = this.slots.get(SLOT_SENSOR);
        if(sensor.isEmpty() || sensor.getItem() != NtmItems.REACTOR_SENSOR.get()) return false;

        BlockPos target = sensor.get(NtmDataComponents.REACTOR_LINK.get());
        if(target == null) return false;

        if(!(this.level.getBlockEntity(target) instanceof ReactorResearchBlockEntity core)) return false;

        this.reactor = core;
        this.flux = core.totalFlux;
        this.rodLevel = core.rodLevel;
        this.heat = core.heat;

        return true;
    }

    /**
     * Die Stellung, die bei dieser Temperatur gelten soll. Die Formeln stehen unveraendert aus
     * dem Original; LOG spiegelt die Quadratische an der oberen Ecke.
     */
    public double getTargetLevel(RodFunction function, int heat) {

        double heatSpan = this.heatUpper - this.heatLower;
        if(heatSpan == 0D) return this.levelLower;

        return switch(function) {
            case LINEAR -> (heat - this.heatLower) * ((this.levelUpper - this.levelLower) / heatSpan) + this.levelLower;
            case LOG -> Math.pow((heat - this.heatUpper) / (this.heatLower - this.heatUpper), 2)
                    * (this.levelLower - this.levelUpper) + this.levelUpper;
            case QUAD -> Math.pow((heat - this.heatLower) / heatSpan, 2)
                    * (this.levelUpper - this.levelLower) + this.levelLower;
        };
    }

    /** Stellung in Prozent, Fluss, Temperatur in Kelvin -- was die Oberflaeche zeigt. */
    public int[] getDisplayData() {
        if(!this.isLinked) return new int[] {0, 0, 0};
        return new int[] {
                (int) (this.rodLevel * 100),
                this.flux,
                (int) Math.round(this.heat * 0.00002D * 980D + 20D)
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_SENSOR && stack.getItem() == NtmItems.REACTOR_SENSOR.get();
    }

    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) { return false; }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[0]; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.heat);
        buf.writeDouble(this.rodLevel);
        buf.writeInt(this.flux);
        buf.writeBoolean(this.isLinked);
        buf.writeDouble(this.levelLower);
        buf.writeDouble(this.levelUpper);
        buf.writeDouble(this.heatLower);
        buf.writeDouble(this.heatUpper);
        buf.writeByte(this.function.ordinal());
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.heat = buf.readInt();
        this.rodLevel = buf.readDouble();
        this.flux = buf.readInt();
        this.isLinked = buf.readBoolean();
        this.levelLower = buf.readDouble();
        this.levelUpper = buf.readDouble();
        this.heatLower = buf.readDouble();
        this.heatUpper = buf.readDouble();
        this.function = RodFunction.values()[Mth.clamp(buf.readByte(), 0, RodFunction.values().length - 1)];
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.isLinked = tag.getBoolean("isLinked");
        this.levelLower = tag.getDouble("levelLower");
        this.levelUpper = tag.getDouble("levelUpper");
        this.heatLower = tag.getDouble("heatLower");
        this.heatUpper = tag.getDouble("heatUpper");
        this.function = RodFunction.values()[Mth.clamp(tag.getInt("function"), 0, RodFunction.values().length - 1)];
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("isLinked", this.isLinked);
        tag.putDouble("levelLower", this.levelLower);
        tag.putDouble("levelUpper", this.levelUpper);
        tag.putDouble("heatLower", this.heatLower);
        tag.putDouble("heatUpper", this.heatUpper);
        tag.putInt("function", this.function.ordinal());
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 20 * 20;
    }

    @Override
    public void receiveControl(CompoundTag tag) {

        if(tag.contains("function")) {
            this.function = RodFunction.values()[Mth.clamp(tag.getInt("function"), 0, RodFunction.values().length - 1)];
        } else {
            this.levelLower = Mth.clamp(tag.getDouble("levelLower"), 0D, 100D);
            this.levelUpper = Mth.clamp(tag.getDouble("levelUpper"), 0D, 100D);
            /* Die Oberflaeche rechnet ihre 0..1000 mit 50 hoch, also gilt hier die
             * Reaktorobergrenze. Das Original klammert an dieser Stelle auf 9999 -- das ist ein
             * Widerspruch zu seiner eigenen Oberflaeche und hier ausgebessert. */
            this.heatLower = Mth.clamp(tag.getDouble("heatLower"), 0D, ReactorResearchBlockEntity.MAX_HEAT);
            this.heatUpper = Mth.clamp(tag.getDouble("heatUpper"), 0D, ReactorResearchBlockEntity.MAX_HEAT);
        }

        this.setChanged();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ReactorControlMenu(id, inventory, this);
    }
}
