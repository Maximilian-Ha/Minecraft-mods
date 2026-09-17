package com.hbm.blockentity.machine.storage;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.fluidmk2.FluidNode;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.IPersistentNBT;
import com.hbm.blockentity.LoadedBaseBlockEntity.TiltType;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineBigAssTankMenu;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.fauxpointtwelve.DirPos;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.storage.TileEntityMachineBigAssTank
 * samt der Oberklasse TileEntityBarrel.
 *
 * Kein Nachfahre von BarrelBlockEntity, obwohl das Original genau so gebaut ist. Die
 * Fassklasse des Ports ist an vier Stellen fest an das Fass gebunden: sie meldet im
 * Konstruktor NtmBlockEntityTypes.BARREL an, holt ihr Fassungsvermoegen aus BarrelBlock,
 * traegt die Fassregeln (Kunststoff, Sprengstaerke 5) in einer privaten Methode und kippt
 * nur nach Einstellung. Der Big-Ass Tank braucht an allen vier Stellen etwas anderes und
 * steht ausserdem auf einem Mehrblockbau. MachineFluidTankBlockEntity ist im Port aus
 * demselben Grund ein Geschwister und kein Nachfahre; dieser Bau folgt dem.
 *
 * Weggelassen wie ueberall im Port: OpenComputers und Redstone-over-Radio, die das
 * Original an TileEntityBarrel haengt.
 */
public class MachineBigAssTankBlockEntity extends MachineBaseBlockEntity implements IFluidStandardTransceiverMK2, IPersistentNBT, IFluidCopiable, IControlReceiver {

    private static final int INV_SIZE = 6;
    private static final int MODES = 4;

    public static final int CAPACITY = 16_000_000;

    protected FluidNode node;
    private FluidType lastType = Fluids.NONE;

    public final FluidTank tank;
    public short mode = 0;
    private byte lastRedstone = 0;

    private AABB renderBox;

    public MachineBigAssTankBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_BIGASSTANK.get(), pos, state, INV_SIZE);

        this.tank = new FluidTank(Fluids.NONE, CAPACITY);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.bigasstank");
    }

    /*
     * Das Original rechnet die Durchsatzgrenze aus dem Fuellstand und laesst mindestens
     * 50.000 mB je Tick zu -- hundertmal so viel wie beim gewoehnlichen Tank.
     */
    @Override public long getReceiverSpeed(FluidType type, int pressure) { return Math.max(50_000, (this.tank.getMaxFill() - this.tank.getFill()) / 100); }
    @Override public long getProviderSpeed(FluidType type, int pressure) { return Math.max(50_000, this.tank.getFill() / 100); }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        /*
         * UNAVOIDABLE: der Tank kippt immer, wenn der Untergrund nicht taugt, unabhaengig
         * von der Einstellung. extraHeavy verlangt an jeder der sechzehn Proben eine feste,
         * volle Flaeche mit mindestens Sprengwiderstand von Stein.
         */
        this.checkTilt(TiltType.UNAVOIDABLE, true);

        if(this.tank.setType(0, 1, this.slots)) {
            this.setChanged();
        }
        this.tank.loadTank(this.level, 2, 3, this.slots);
        this.tank.unloadTank(this.level, 4, 5, this.slots);

        byte comp = this.getComparatorPower();
        if(comp != this.lastRedstone) {
            this.setChanged();
            for(DirPos pos : this.getConPos()) {
                this.level.updateNeighbourForOutputSignal(pos.makeCompat(), this.getBlockState().getBlock());
            }
        }
        this.lastRedstone = comp;

        if(this.mode == 1) {
            if(this.node == null || this.node.expired || this.tank.getTankType() != this.lastType) {
                if(this.node != null) {
                    this.destroyNode();
                }

                FluidNode existing = (FluidNode) UniNodespace.getNode(this.level, this.getBlockPos(), this.tank.getTankType().getNetworkProvider());
                if(existing == null || existing.expired) {
                    this.node = this.createNode(this.tank.getTankType());
                    UniNodespace.createNode(this.level, this.node);
                } else {
                    this.node = existing;
                }
                this.lastType = this.tank.getTankType();
            }

            if(this.node != null && this.node.hasValidNet()) {
                this.node.net.addProvider(this);
                this.node.net.addReceiver(this);
            }
        } else {
            this.destroyNode();

            if(!this.tilted) {
                for(DirPos pos : this.getConPos()) {
                    if(this.mode == 2) {
                        this.tryProvide(this.tank, this.level, pos);
                    } else {
                        this.trySubscribe(this.tank.getTankType(), this.level, pos);
                    }
                }
            }
        }

        if(this.tank.getFill() > 0) {
            this.checkFluidInteraction();
        }

        this.networkPackNT(50);
    }

    private void destroyNode() {
        if(this.level == null) return;
        if(this.node != null) {
            UniNodespace.destroyNode(this.level, this.node);
            this.node = null;
            this.lastType = Fluids.NONE;
        }
    }

    protected FluidNode createNode(FluidType type) {
        DirPos[] conPos = this.getConPos();

        HashSet<BlockPos> posSet = new HashSet<>();
        posSet.add(this.getBlockPos());
        for(DirPos pos : conPos) {
            Direction dir = pos.getDir();
            posSet.add(new BlockPos(pos.getX() - dir.getStepX(), pos.getY() - dir.getStepY(), pos.getZ() - dir.getStepZ()));
        }

        return new FluidNode(type.getNetworkProvider(), posSet.toArray(new BlockPos[0])).setConnections(conPos);
    }

    /*
     * Das Original laesst den Tank bei Antimaterie mit Staerke 10 hochgehen -- doppelt so
     * stark wie ein Fass. Die Kunststoffregeln des Fasses entfallen hier.
     */
    private void checkFluidInteraction() {
        if(this.level == null || this.level.isClientSide) return;

        if(this.tank.getTankType().isAntimatter()) {
            BlockPos pos = this.getBlockPos();
            this.level.destroyBlock(pos, false);
            this.level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10F, Level.ExplosionInteraction.TNT);
        }
    }

    /*
     * Die beiden Stutzen liegen auf der Blickachse, sieben Bloecke vor und hinter dem Kern.
     * Die Attrappen dort tragen nur eine Fluidweiche -- ein Kabel oder ein Gegenstandsrohr
     * findet an dieser Stelle nichts, genau wie im Original.
     */
    public DirPos[] getConPos() {
        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        BlockPos pos = this.getBlockPos();

        return new DirPos[] {
                new DirPos(pos.getX() + dir.getStepX() * 7, pos.getY(), pos.getZ() + dir.getStepZ() * 7, dir),
                new DirPos(pos.getX() - dir.getStepX() * 7, pos.getY(), pos.getZ() - dir.getStepZ() * 7, dir.getOpposite())
        };
    }

    public byte getComparatorPower() {
        if(this.tank.getFill() <= 0) return 0;
        double frac = (double) this.tank.getFill() / (double) this.tank.getMaxFill() * 15D;
        return (byte) Math.clamp((int) Math.ceil(frac), 0, 15);
    }

    @Override public int getFloorCount() { return 4 * 4; }
    @Override public BlockPos getFloorPosFromIndex(int index) { return this.standardFloor7x7(index); }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch(slot) {
            case 0, 1 -> stack.getItem() instanceof IItemFluidIdentifier;
            case 2 -> !FluidContainerRegistry.getFullContainer(stack, this.tank.getTankType()).isEmpty();
            case 4 -> FluidContainerRegistry.getFluidContent(stack, this.tank.getTankType()) > 0;
            default -> true;
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return switch(slot) {
            case 1, 3, 5 -> true;
            default -> !this.canPlaceItem(slot, stack);
        };
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return switch(direction.get3DDataValue()) {
            case 0 -> new int[] {3, 5};
            case 1 -> new int[] {2};
            default -> new int[] {4};
        };
    }

    /*
     * Grosszuegig bemessen: der Bau ist dreizehn Bloecke breit, der Spieler kann also weit
     * vom Kern entfernt an seiner Oberflaeche stehen. stillValid mit acht Bloecken waere hier
     * zu knapp.
     */
    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5) <= 256.0D;
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("Mode")) {
            this.mode = (short) ((this.mode + 1) % MODES);
            this.setChanged();
        }
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        if(this.tilted || this.mode == 2 || this.mode == 3) return 0;
        if(this.tank.getPressure() != pressure) return 0;
        return type == this.tank.getTankType() ? this.tank.getMaxFill() - this.tank.getFill() : 0;
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long fluid) {
        long toTransfer = Math.min(this.getDemand(type, pressure), fluid);
        this.tank.setFill(this.tank.getFill() + (int) toTransfer);
        this.setChanged();
        return fluid - toTransfer;
    }

    @Override
    public FluidTank[] getReceivingTanks() {
        return (this.mode == 0 || this.mode == 1) ? new FluidTank[] { this.tank } : FluidTank.EMPTY_ARRAY;
    }

    @Override
    public FluidTank[] getSendingTanks() {
        return (this.mode == 1 || this.mode == 2) ? new FluidTank[] { this.tank } : FluidTank.EMPTY_ARRAY;
    }

    @Override
    public FluidTank[] getAllTanks() {
        return new FluidTank[] { this.tank };
    }

    /*
     * Im Puffermodus haengt der Tank als Teil des Netzes darin und darf den Verbrauchern nichts
     * wegnehmen: LOW ist ein eigener, spaeter bedienter Topf. Ohne diese Zeile stuende er mit
     * seinem Bedarf von 160.000 mB je Tick im selben Topf wie eine Maschine mit 24.000 und
     * bekaeme vier Fuenftel des Durchsatzes. Das Original hat es an TileEntityBarrel.
     */
    @Override
    public ConnectionPriority getFluidPriority() {
        return this.mode == 1 ? ConnectionPriority.LOW : ConnectionPriority.NORMAL;
    }

    @Override
    public int[] getFluidIDToCopy() {
        return new int[] { this.tank.getTankType().getID() };
    }

    @Override
    public FluidTank getTankToPaste() {
        return this.tank;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineBigAssTankMenu(id, inventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.mode = tag.getShort("mode");
        this.tank.readFromNBT(tag, "tank");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("mode", this.mode);
        this.tank.writeToNBT(tag, "tank");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeShort(this.mode);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.mode = buf.readShort();
        this.tank.deserialize(buf);
    }

    @Override
    public void writeNBT(CompoundTag savedTag) {
        if(this.tank.getFill() <= 0) return;
        CompoundTag tag = new CompoundTag();
        tag.putShort("mode", this.mode);
        this.tank.writeToNBT(tag, "tank");
        savedTag.put(NBT_PERSISTENT_KEY, tag);
    }

    @Override
    public void readNBT(CompoundTag savedTag) {
        CompoundTag tag = savedTag.getCompound(NBT_PERSISTENT_KEY);
        this.mode = tag.getShort("mode");
        this.tank.readFromNBT(tag, "tank");
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.destroyNode();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.destroyNode();
    }

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            /*
             * Das Original nimmt hier x-6 bis x+7 und y+5 -- das schneidet die Kuppel ab,
             * denn bigasstank.obj reicht bis X/Z +-6,5 und Y 6,5. Einen Block grosszuegiger,
             * sonst verschwindet der Tank, sobald nur noch die Kuppel im Bild ist.
             */
            this.renderBox = new AABB(x - 7, y - 1, z - 7, x + 8, y + 8, z + 8);
        }
        return this.renderBox;
    }
}
