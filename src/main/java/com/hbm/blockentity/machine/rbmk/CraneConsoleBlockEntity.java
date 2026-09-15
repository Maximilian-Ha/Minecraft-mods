package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.machine.rbmk.RBMKBaseBlock;
import com.hbm.extprop.HbmPlayerAttachments;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.items.machine.RBMKRodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityCraneConsole.
 *
 * Das Kranpult. Wer davorsteht, faehrt mit den vier Richtungstasten den Laufkran ueber dem
 * Reaktor und laesst ihn mit der Ladetaste herunter. Steht der Kran ueber einem Brennkanal oder
 * einer Lagersaeule, tauscht er dort einen Stab ein oder aus.
 *
 * Der Zielpunkt kommt vom Verbindungsstab: das Pult merkt sich die Mitte des Reaktors, misst
 * von dort aus die vier Reichweiten bis zur naechsten Wand und weiss damit, wie weit der Kran
 * fahren darf.
 *
 * ABWEICHUNG: das Original bietet die Kransteuerung zusaetzlich als OpenComputers-Bauteil an.
 * OpenComputers gibt es fuer 1.21 nicht, die Methoden entfallen ersatzlos.
 */
public class CraneConsoleBlockEntity extends LoadedBaseBlockEntity implements ITickable {

    /** Wie weit der Kran je Tick faehrt, solange eine Richtungstaste gehalten wird. */
    private static final double SPEED = 0.05D;
    /** Wie weit der Kran je Tick sinkt beziehungsweise steigt. */
    private static final double LIFT_SPEED = 0.04D;
    /** Wie weit das Pult hoechstens nach einer Wand sucht. */
    private static final int MAX_SPAN = 16;

    /** Mitte des Reaktors, eine Ebene ueber den Saeulendeckeln. */
    public BlockPos center = BlockPos.ZERO;

    public int spanF;
    public int spanB;
    public int spanL;
    public int spanR;
    public int height;

    public boolean setUpCrane = false;
    public int craneRotationOffset = 0;

    public double lastTiltFront = 0;
    public double lastTiltLeft = 0;
    public double tiltFront = 0;
    public double tiltLeft = 0;

    public double lastPosFront = 0;
    public double lastPosLeft = 0;
    public double posFront = 0;
    public double posLeft = 0;
    public double syncFront = 0;
    public double syncLeft = 0;

    private boolean goesDown = false;
    public double lastProgress = 1D;
    public double progress = 1D;
    public double syncProgress = 1D;

    private ItemStack loadedItem = ItemStack.EMPTY;
    private boolean hasLoaded = false;
    public double loadedHeat;
    public double loadedEnrichment;

    public CraneConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CRANE_CONSOLE.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.level.isClientSide) {
            this.lastTiltFront = this.tiltFront;
            this.lastTiltLeft = this.tiltLeft;
            this.lastPosFront = this.posFront;
            this.lastPosLeft = this.posLeft;
            this.lastProgress = this.progress;

            // Das Original fuehrt hier einen Zaehler turnProgress mit, der nie gesetzt wird --
            // der Zweig laeuft also nie. Weil ohnehin jeden Tick ein Paket kommt, uebernimmt der
            // Port gleich den Sollwert; die Zwischenbilder macht der Renderer aus lastPos.
            this.posFront = this.syncFront;
            this.posLeft = this.syncLeft;
            this.progress = this.syncProgress;
        }

        if(!this.level.isClientSide) {

            RBMKBaseBlockEntity aboveColumn = this.getColumnAtPos();
            if(aboveColumn != null) aboveColumn.craneIndicator = 10;

            if(this.goesDown) {

                if(this.progress > 0) {
                    this.progress -= LIFT_SPEED;
                } else {
                    this.progress = 0;
                    this.goesDown = false;

                    if(aboveColumn instanceof IRBMKLoadable column && this.canTargetInteract(column)) {
                        if(!this.loadedItem.isEmpty()) {
                            column.load(this.loadedItem);
                            this.loadedItem = ItemStack.EMPTY;
                        } else {
                            this.loadedItem = column.provideNext().copy();
                            column.unload();
                        }
                        this.setChanged();
                    }
                }
            } else if(this.progress != 1) {
                this.progress = Math.min(this.progress + LIFT_SPEED, 1D);
            }
        }

        this.handleControls();

        this.posFront = Mth.clamp(this.posFront, -this.spanB, this.spanF);
        this.posLeft = Mth.clamp(this.posLeft, -this.spanR, this.spanL);

        if(!this.level.isClientSide) {

            if(this.loadedItem.getItem() instanceof RBMKRodItem) {
                this.loadedHeat = RBMKRodItem.getHullHeat(this.loadedItem);
                this.loadedEnrichment = RBMKRodItem.getEnrichment(this.loadedItem);
            } else {
                this.loadedHeat = 0;
                this.loadedEnrichment = 0;
            }

            this.networkPackNT(250);
        }
    }

    /**
     * Sucht den Spieler vor dem Pult und liest dessen Tastenstand. Laeuft auf beiden Seiten: der
     * Server faehrt den Kran, der Client neigt nur den Steuerhebel.
     */
    private void handleControls() {

        Direction dir = DummyableBlock.getPointingDirection(this.getBlockState());
        Direction side = dir.getClockWise();

        double minX = this.worldPosition.getX() + 0.5 - side.getStepX() * 1.5;
        double maxX = this.worldPosition.getX() + 0.5 + side.getStepX() * 1.5 + dir.getStepX() * 2;
        double minZ = this.worldPosition.getZ() + 0.5 - side.getStepZ() * 1.5;
        double maxZ = this.worldPosition.getZ() + 0.5 + side.getStepZ() * 1.5 + dir.getStepZ() * 2;

        List<Player> players = this.level.getEntitiesOfClass(Player.class, new AABB(
                Math.min(minX, maxX), this.worldPosition.getY(), Math.min(minZ, maxZ),
                Math.max(minX, maxX), this.worldPosition.getY() + 2, Math.max(minZ, maxZ)));

        this.tiltFront = 0;
        this.tiltLeft = 0;

        if(players.isEmpty() || this.isCraneLoading()) return;

        HbmPlayerAttachments props = HbmPlayerAttachments.getData(players.get(0));
        boolean up = props.getKeyPressed(EnumKeybind.CRANE_UP);
        boolean down = props.getKeyPressed(EnumKeybind.CRANE_DOWN);
        boolean left = props.getKeyPressed(EnumKeybind.CRANE_LEFT);
        boolean right = props.getKeyPressed(EnumKeybind.CRANE_RIGHT);

        if(up && !down) {
            this.tiltFront = 30;
            if(!this.level.isClientSide) this.posFront += SPEED;
        }
        if(!up && down) {
            this.tiltFront = -30;
            if(!this.level.isClientSide) this.posFront -= SPEED;
        }
        if(left && !right) {
            this.tiltLeft = 30;
            if(!this.level.isClientSide) this.posLeft += SPEED;
        }
        if(!left && right) {
            this.tiltLeft = -30;
            if(!this.level.isClientSide) this.posLeft -= SPEED;
        }

        if(props.getKeyPressed(EnumKeybind.CRANE_LOAD)) this.goesDown = true;
    }

    public boolean hasItemLoaded() {
        if(this.level != null && this.level.isClientSide) return this.hasLoaded;
        return !this.loadedItem.isEmpty();
    }

    public boolean isCraneLoading() {
        return this.progress != 1D;
    }

    public boolean isAboveValidTarget() {
        return this.getLoadableAtPos() != null;
    }

    public boolean canTargetInteract(IRBMKLoadable column) {
        if(column == null) return false;
        return this.hasItemLoaded() ? column.canLoad(this.loadedItem) : column.canUnload();
    }

    /** Die Saeule unter dem Kranhaken, oder null. */
    public RBMKBaseBlockEntity getColumnAtPos() {

        if(this.level == null) return null;

        Direction dir = DummyableBlock.getPointingDirection(this.getBlockState());
        Direction left = dir.getCounterClockWise();

        int x = Mth.floor(this.center.getX() - dir.getStepX() * this.posFront - left.getStepX() * this.posLeft + 0.5D);
        int y = this.center.getY() - 1;
        int z = Mth.floor(this.center.getZ() - dir.getStepZ() * this.posFront - left.getStepZ() * this.posLeft + 0.5D);

        BlockPos pos = new BlockPos(x, y, z);
        if(!(this.level.getBlockState(pos).getBlock() instanceof RBMKBaseBlock column)) return null;

        BlockPos corePos = column.findCore(this.level, pos);
        if(corePos == null) return null;

        return this.level.getBlockEntity(corePos) instanceof RBMKBaseBlockEntity be ? be : null;
    }

    public IRBMKLoadable getLoadableAtPos() {
        return this.getColumnAtPos() instanceof IRBMKLoadable loadable ? loadable : null;
    }

    /** Setzt die Mitte des Reaktors und misst von dort aus die vier Reichweiten. */
    public void setTarget(BlockPos pos) {

        if(this.level == null) return;

        this.center = new BlockPos(pos.getX(), pos.getY() + RBMKDials.getColumnHeight(this.level) + 1, pos.getZ());

        int girderY = this.center.getY() + 6;

        Direction dir = DummyableBlock.getPointingDirection(this.getBlockState()).getOpposite();
        this.spanF = this.findRoomExtent(pos.getX(), girderY, pos.getZ(), dir);
        dir = dir.getClockWise();
        this.spanR = this.findRoomExtent(pos.getX(), girderY, pos.getZ(), dir);
        dir = dir.getClockWise();
        this.spanB = this.findRoomExtent(pos.getX(), girderY, pos.getZ(), dir);
        dir = dir.getClockWise();
        this.spanL = this.findRoomExtent(pos.getX(), girderY, pos.getZ(), dir);

        this.height = 7;
        this.setUpCrane = true;

        this.setChanged();
    }

    /** Wie viele Bloecke es in dieser Richtung frei bleibt, hoechstens {@link #MAX_SPAN}. */
    private int findRoomExtent(int x, int y, int z, Direction dir) {

        for(int i = 1; i < MAX_SPAN; i++) {
            if(!this.level.getBlockState(new BlockPos(x + dir.getStepX() * i, y, z + dir.getStepZ() * i)).isAir()) return i - 1;
        }

        return MAX_SPAN;
    }

    public void cycleCraneRotation() {
        this.craneRotationOffset = (this.craneRotationOffset + 90) % 360;
        this.setChanged();
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);

        buf.writeBoolean(this.setUpCrane);
        if(!this.setUpCrane) return;

        buf.writeInt(this.craneRotationOffset);
        buf.writeBlockPos(this.center);
        buf.writeInt(this.spanF);
        buf.writeInt(this.spanB);
        buf.writeInt(this.spanL);
        buf.writeInt(this.spanR);
        buf.writeInt(this.height);
        buf.writeDouble(this.posFront);
        buf.writeDouble(this.posLeft);
        buf.writeDouble(this.progress);
        buf.writeBoolean(this.hasItemLoaded());
        buf.writeDouble(this.loadedHeat);
        buf.writeDouble(this.loadedEnrichment);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);

        this.setUpCrane = buf.readBoolean();
        if(!this.setUpCrane) return;

        this.craneRotationOffset = buf.readInt();
        this.center = buf.readBlockPos();
        this.spanF = buf.readInt();
        this.spanB = buf.readInt();
        this.spanL = buf.readInt();
        this.spanR = buf.readInt();
        this.height = buf.readInt();
        this.syncFront = buf.readDouble();
        this.syncLeft = buf.readDouble();
        this.syncProgress = buf.readDouble();
        this.hasLoaded = buf.readBoolean();
        this.loadedHeat = buf.readDouble();
        this.loadedEnrichment = buf.readDouble();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.setUpCrane = tag.getBoolean("crane");
        this.craneRotationOffset = tag.getInt("craneRotationOffset");
        this.center = new BlockPos(tag.getInt("centerX"), tag.getInt("centerY"), tag.getInt("centerZ"));
        this.spanF = tag.getInt("spanF");
        this.spanB = tag.getInt("spanB");
        this.spanL = tag.getInt("spanL");
        this.spanR = tag.getInt("spanR");
        this.height = tag.getInt("height");
        this.posFront = tag.getDouble("posFront");
        this.posLeft = tag.getDouble("posLeft");

        this.loadedItem = tag.contains("held")
                ? ItemStack.parse(registries, tag.getCompound("held")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("crane", this.setUpCrane);
        tag.putInt("craneRotationOffset", this.craneRotationOffset);
        tag.putInt("centerX", this.center.getX());
        tag.putInt("centerY", this.center.getY());
        tag.putInt("centerZ", this.center.getZ());
        tag.putInt("spanF", this.spanF);
        tag.putInt("spanB", this.spanB);
        tag.putInt("spanL", this.spanL);
        tag.putInt("spanR", this.spanR);
        tag.putInt("height", this.height);
        tag.putDouble("posFront", this.posFront);
        tag.putDouble("posLeft", this.posLeft);

        if(!this.loadedItem.isEmpty()) tag.put("held", this.loadedItem.save(registries));
    }

    /**
     * Der Kran haengt hoch ueber dem Reaktor und ragt weit zur Seite, das Zeichenfenster muss
     * also den ganzen Reaktorsaal umfassen.
     *
     * Kein @Override: getRenderBoundingBox stammt aus der NeoForge-Erweiterung der
     * Block-Entitaet und wird vom Compiler nicht als ueberschriebene Methode gefuehrt.
     */
    public AABB getRenderBoundingBox() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        return new AABB(x - MAX_SPAN - 2, y, z - MAX_SPAN - 2, x + MAX_SPAN + 3, y + 32, z + MAX_SPAN + 3);
    }
}
