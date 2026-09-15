package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.util.Compat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.
 *
 * Das Reaktorpult. Es tastet alle zehn Ticks ein 15x15-Feld um seinen Zielpunkt ab und haelt
 * fuer jede gefundene Saeule deren Werte bereit. Dazu kommen sechs frei belegbare Anzeigen, die
 * je eine Auswahl von Saeulen mitteln, und ein Verlauf des Gesamtflusses ueber die letzte Minute.
 *
 * Der Zielpunkt wird mit dem Verbindungsstab gesetzt, die Ausrichtung des Rasters mit dem
 * Schraubenzieher gedreht.
 */
public class RBMKConsoleBlockEntity extends LoadedBaseBlockEntity implements ITickable, IControlReceiver {

    public static final int GRID = 15;
    public static final int FLUX_BUFFER = 60;

    private BlockPos target = BlockPos.ZERO;
    private byte rotation;

    public int[] fluxBuffer = new int[FLUX_BUFFER];

    /** Eindimensional, weil sich das viel einfacher verschicken laesst. */
    public RBMKColumn[] columns = new RBMKColumn[GRID * GRID];
    public RBMKScreen[] screens = new RBMKScreen[6];

    public RBMKConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_CONSOLE.get(), pos, state);
        for(int i = 0; i < this.screens.length; i++) this.screens[i] = new RBMKScreen();
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.level.getGameTime() % 10 == 0) {
            this.rescan();
            this.prepareScreenInfo();
        }

        this.networkPackNT(50);
    }

    /** Liest alle Saeulen im Raster neu ein und schreibt den Gesamtfluss in den Verlauf. */
    private void rescan() {

        double flux = 0;

        for(int index = 0; index < this.columns.length; index++) {

            BlockPos pos = this.target.offset(this.getXFromIndex(index), 0, this.getZFromIndex(index));
            BlockEntity te = Compat.getBlockEntityStandard(this.level, pos);

            if(te instanceof RBMKBaseBlockEntity rbmk) {

                RBMKColumn column = new RBMKColumn(rbmk.getConsoleType(), rbmk.getNBTForConsole());
                column.data.putDouble("heat", rbmk.heat);
                column.data.putDouble("maxHeat", rbmk.maxHeat());
                column.data.putByte("indicator", (byte) rbmk.craneIndicator);
                // false ist ohnehin der Standard, das Feld bleibt sonst weg
                if(rbmk.isModerated()) column.data.putBoolean("moderated", true);

                this.columns[index] = column;

                if(te instanceof RBMKRodBlockEntity fuel) flux += fuel.lastFluxQuantity;

            } else {
                this.columns[index] = null;
            }
        }

        System.arraycopy(this.fluxBuffer, 1, this.fluxBuffer, 0, this.fluxBuffer.length - 1);
        this.fluxBuffer[this.fluxBuffer.length - 1] = (int) flux;
    }

    /** Mittelt fuer jede der sechs Anzeigen den gewaehlten Wert ueber die ihr zugeteilten Saeulen. */
    private void prepareScreenInfo() {

        for(RBMKScreen screen : this.screens) {

            if(screen.type == ScreenType.NONE) {
                screen.display = null;
                continue;
            }

            double value = 0;
            int count = 0;

            for(int i : screen.columns) {

                if(i < 0 || i >= this.columns.length) continue;
                RBMKColumn col = this.columns[i];
                if(col == null) continue;

                switch(screen.type) {
                    case COL_TEMP -> { count++; value += col.data.getDouble("heat"); }
                    case FUEL_DEPLETION -> { if(col.data.contains("enrichment")) { count++; value += 100D - col.data.getDouble("enrichment") * 100D; } }
                    case FUEL_POISON -> { if(col.data.contains("xenon")) { count++; value += col.data.getDouble("xenon"); } }
                    case FUEL_TEMP -> { if(col.data.contains("c_heat")) { count++; value += col.data.getDouble("c_heat"); } }
                    case ROD_EXTRACTION -> { if(col.data.contains("level")) { count++; value += col.data.getDouble("level") * 100; } }
                    default -> { }
                }
            }

            // Ohne passende Saeule bleibt die Anzeige leer, statt NaN zu zeigen.
            if(count == 0) {
                screen.display = null;
                continue;
            }

            screen.display = String.valueOf(((int) (value / count * 10)) / 10D);
        }
    }

    public void setTarget(BlockPos pos) {
        this.target = pos.immutable();
        this.setChanged();
    }

    public BlockPos getTarget() {
        return this.target;
    }

    public void rotate() {
        this.rotation = (byte) ((this.rotation + 1) % 4);
        this.setChanged();
    }

    public int getXFromIndex(int col) {
        int i = col % GRID - 7;
        int j = col / GRID - 7;
        return switch(this.rotation) {
            case 1 -> -j;
            case 2 -> -i;
            case 3 -> j;
            default -> i;
        };
    }

    public int getZFromIndex(int col) {
        int i = col % GRID - 7;
        int j = col / GRID - 7;
        return switch(this.rotation) {
            case 1 -> i;
            case 2 -> -j;
            case 3 -> -i;
            default -> j;
        };
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) < 400;
    }

    @Override
    public void receiveControl(CompoundTag data) {

        if(this.level == null) return;

        if(data.contains("level")) {
            for(String key : data.getAllKeys()) {
                if(!key.startsWith("sel_")) continue;
                this.withControlRod(data.getInt(key), rod -> rod.setTarget(Mth.clamp(data.getDouble("level"), 0, 1)));
            }
        }

        if(data.contains("toggle")) {
            int slot = Math.abs(data.getByte("toggle")) % this.screens.length;
            int next = (this.screens[slot].type.ordinal() + 1) % ScreenType.values().length;
            this.screens[slot].type = ScreenType.values()[next];
            this.setChanged();
        }

        if(data.contains("id")) {

            int slot = Math.abs(data.getByte("id")) % this.screens.length;
            List<Integer> list = new ArrayList<>();

            for(int i = 0; i < GRID * GRID; i++) {
                if(data.getBoolean("s" + i)) list.add(i);
            }

            this.screens[slot].columns = list.stream().mapToInt(Integer::intValue).toArray();
            this.setChanged();
        }

        if(data.contains("assignColor")) {
            RBMKColor color = RBMKColor.byIndex(data.getByte("assignColor"));
            for(int i : data.getIntArray("cols")) {
                this.withControlRod(i, rod -> rod.color = color);
            }
        }

        if(data.contains("compressor")) {
            for(int i : data.getIntArray("cols")) {
                BlockEntity te = Compat.getBlockEntityStandard(this.level, this.posFromIndex(i));
                if(te instanceof RBMKBoilerBlockEntity boiler) boiler.cycleCompressor();
            }
        }
    }

    private BlockPos posFromIndex(int index) {
        return this.target.offset(this.getXFromIndex(index), 0, this.getZFromIndex(index));
    }

    private void withControlRod(int index, java.util.function.Consumer<RBMKControlBlockEntity> action) {
        if(index < 0 || index >= this.columns.length) return;
        BlockEntity te = Compat.getBlockEntityStandard(this.level, this.posFromIndex(index));
        if(te instanceof RBMKControlBlockEntity rod) {
            action.accept(rod);
            rod.setChanged();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.target = new BlockPos(tag.getInt("tX"), tag.getInt("tY"), tag.getInt("tZ"));
        this.rotation = tag.getByte("rotation");

        for(int i = 0; i < this.screens.length; i++) {
            this.screens[i].type = ScreenType.values()[Math.abs(tag.getByte("t" + i)) % ScreenType.values().length];
            this.screens[i].columns = tag.getIntArray("s" + i);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("tX", this.target.getX());
        tag.putInt("tY", this.target.getY());
        tag.putInt("tZ", this.target.getZ());
        tag.putByte("rotation", this.rotation);

        for(int i = 0; i < this.screens.length; i++) {
            tag.putByte("t" + i, (byte) this.screens[i].type.ordinal());
            tag.putIntArray("s" + i, this.screens[i].columns);
        }
    }

    /**
     * Abweichung vom Original: dort wird das Raster nur jeden zehnten Tick verschickt und der
     * Rest der Zeit ein Kennbit gesetzt. Der Port braucht das nicht -- networkPackNT verschickt
     * ohnehin nur, was sich geaendert hat, und das Raster aendert sich nur beim Abtasten.
     */
    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);

        for(RBMKColumn column : this.columns) {
            if(column == null || column.type == null) {
                buf.writeByte(-1);
            } else {
                buf.writeByte(column.type.ordinal());
                buf.writeNbt(column.data);
            }
        }

        for(RBMKScreen screen : this.screens) {
            buf.writeByte(screen.type.ordinal());
            buf.writeVarIntArray(screen.columns);
            buf.writeBoolean(screen.display != null);
            if(screen.display != null) buf.writeUtf(screen.display);
        }

        for(int flux : this.fluxBuffer) buf.writeInt(flux);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);

        for(int i = 0; i < this.columns.length; i++) {
            int ordinal = buf.readByte();
            if(ordinal < 0 || ordinal >= RBMKColumnType.values().length) {
                this.columns[i] = null;
            } else {
                this.columns[i] = new RBMKColumn(RBMKColumnType.values()[ordinal], buf.readNbt());
            }
        }

        for(RBMKScreen screen : this.screens) {
            screen.type = ScreenType.values()[Math.abs(buf.readByte()) % ScreenType.values().length];
            screen.columns = buf.readVarIntArray();
            screen.display = buf.readBoolean() ? buf.readUtf() : null;
        }

        for(int i = 0; i < this.fluxBuffer.length; i++) this.fluxBuffer[i] = buf.readInt();
    }

    /* Kein @Override: getRenderBoundingBox stammt aus der NeoForge-Erweiterung, und die
     * Oberklasse LoadedBaseBlockEntity erklaert sie nicht selbst. */
    public AABB getRenderBoundingBox() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        return new AABB(x - 2, y, z - 2, x + 3, y + 4, z + 3);
    }

    /** Eine Saeule im Raster, so wie das Pult sie kennt. */
    public static class RBMKColumn {

        public final RBMKColumnType type;
        public final CompoundTag data;

        public RBMKColumn(RBMKColumnType type, @Nullable CompoundTag data) {
            this.type = type;
            this.data = data != null ? data : new CompoundTag();
        }
    }

    /** Eine der sechs Anzeigen: was sie zeigt, welche Saeulen sie mittelt, und der letzte Wert. */
    public static class RBMKScreen {

        public ScreenType type = ScreenType.NONE;
        public int[] columns = new int[0];
        public String display = null;
    }

    public enum ScreenType {

        NONE(0),
        COL_TEMP(18),
        ROD_EXTRACTION(36),
        FUEL_DEPLETION(54),
        FUEL_POISON(72),
        FUEL_TEMP(90);

        public final int offset;

        ScreenType(int offset) {
            this.offset = offset;
        }

        /** Die Einheit, die hinter dem gemittelten Wert steht. */
        public String unit() {
            return switch(this) {
                case COL_TEMP, FUEL_TEMP -> "C";
                case ROD_EXTRACTION, FUEL_DEPLETION, FUEL_POISON -> "%";
                default -> "";
            };
        }
    }
}
