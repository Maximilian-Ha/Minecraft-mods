package com.hbm.blockentity.machine;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.FoundryNetwork;
import com.hbm.uninos.networkproviders.FoundryNetworkProvider;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFoundryChannel.
 *
 * Der Giesskanal. Er schiebt seinen Inhalt waagerecht weiter: zuerst an alles, was ihn annimmt
 * (Form, Becken), sonst an den naechsten Kanal, wobei sich Nachbarn angleichen. Ein Fuenftel der
 * Versuche tauscht die Fuellstaende ganz, statt sie auszugleichen -- das haelt die Schmelze in
 * Bewegung und verhindert, dass ein Strang auf halber Strecke stehenbleibt.
 *
 * Das Netz dient nur der Sortenreinheit: alle Kanaele eines Strangs fuehren dasselbe Material.
 */
public class FoundryChannelBlockEntity extends FoundryBaseBlockEntity {

    public int nextUpdate;
    public int lastFlow = 0;

    protected FoundryNode node;

    public FoundryChannelBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FOUNDRY_CHANNEL.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.initNode();

        if(this.node.type != null && this.amount == 0) this.node.type = null;
        if(this.type == null && this.amount != 0) this.amount = 0;

        this.nextUpdate--;

        if(this.nextUpdate <= 0 && this.amount > 0 && this.type != null) {

            boolean hasOp = false;
            this.nextUpdate = 5;

            List<Direction> dirs = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
            Collections.shuffle(dirs);

            // die Richtung, aus der es kam, kommt ans Ende -- sonst schwappt es nur hin und her
            if(this.lastFlow > 0) {
                Direction last = Direction.from3DDataValue(this.lastFlow);
                if(dirs.remove(last)) dirs.add(last);
            }

            // erst an alles abgeben, was den Guss annimmt
            for(Direction dir : dirs) {

                BlockPos neighbour = this.worldPosition.relative(dir);
                var block = this.level.getBlockState(neighbour).getBlock();

                if(!(block instanceof ICrucibleAcceptor acc)) continue;
                if(this.level.getBlockEntity(neighbour) instanceof FoundryChannelBlockEntity) continue;

                MaterialStack offer = new MaterialStack(this.type, this.amount);

                if(acc.canAcceptPartialFlow(this.level, neighbour, dir.getOpposite(), offer)) {

                    MaterialStack left = acc.flow(this.level, neighbour, dir.getOpposite(), offer);

                    if(left == null) {
                        this.type = null;
                        this.amount = 0;
                        this.node.type = null;
                    } else {
                        this.amount = left.amount;
                    }

                    hasOp = true;
                    break;
                }
            }

            // sonst mit den Nachbarkanaelen ausgleichen
            if(!hasOp) {
                for(Direction dir : dirs) {

                    BlockPos neighbour = this.worldPosition.relative(dir);

                    if(!(this.level.getBlockEntity(neighbour) instanceof FoundryChannelBlockEntity acc)) continue;
                    if(acc.type != null && acc.type != this.type && acc.amount != 0) continue;

                    acc.type = this.type;
                    acc.initNode();
                    acc.node.type = this.type;
                    acc.lastFlow = dir.getOpposite().get3DDataValue();

                    if(this.level.random.nextInt(5) == 0 || this.amount == 1) {
                        // ein Fuenftel der Faelle tauscht die Fuellstaende ganz; einzelne Quanten
                        // immer, sonst blieben sie liegen
                        int buf = this.amount;
                        this.amount = acc.amount;
                        acc.amount = buf;
                    } else {
                        int diff = this.amount - acc.amount;

                        if(diff > 0) {
                            diff /= 2;
                            this.amount -= diff;
                            acc.amount += diff;
                        }
                    }

                    acc.setChanged();
                }
            }

            this.setChanged();
        }

        if(this.amount == 0) {
            this.lastFlow = 0;
            this.nextUpdate = 5;
        }

        super.updateEntity();
    }

    protected void initNode() {

        if(this.node != null && !this.node.expired) return;

        this.node = (FoundryNode) UniNodespace.getNode(this.level, this.worldPosition, FoundryNetworkProvider.THE_PROVIDER);

        if(this.node == null || this.node.expired) {
            this.node = this.createNode();
            this.node.type = this.type;
            UniNodespace.createNode(this.level, this.node);
        }
    }

    public FoundryNode createNode() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return (FoundryNode) new FoundryNode(FoundryNetworkProvider.THE_PROVIDER, this.worldPosition).setConnections(
                new DirPos(x + 1, y, z, Direction.EAST),
                new DirPos(x - 1, y, z, Direction.WEST),
                new DirPos(x, y, z + 1, Direction.SOUTH),
                new DirPos(x, y, z - 1, Direction.NORTH));
    }

    @Override
    public int getCapacity() {
        return MaterialShapes.INGOT.q(2);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if(this.level != null && !this.level.isClientSide && this.node != null) {
            UniNodespace.destroyNode(this.level, this.worldPosition, FoundryNetworkProvider.THE_PROVIDER);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.lastFlow = tag.getByte("flow");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("flow", (byte) this.lastFlow);
    }

    /** Gegossen wird nur hinein, wenn der ganze Strang dasselbe Material fuehrt. */
    @Override
    public boolean canAcceptPartialPour(net.minecraft.world.level.Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {

        if(this.node == null || !this.node.hasValidNet()) return false;

        for(FoundryNode other : this.node.net.links) {
            if(other.type != null && other.type != stack.material) return false;
        }

        return super.canAcceptPartialPour(level, pos, dX, dY, dZ, side, stack);
    }

    @Override
    public MaterialStack flow(net.minecraft.world.level.Level level, BlockPos pos, Direction side, MaterialStack stack) {
        if(this.node != null) this.node.type = stack.material;
        return super.flow(level, pos, side, stack);
    }

    @Override
    public MaterialStack pour(net.minecraft.world.level.Level level, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
        if(this.node != null) this.node.type = stack.material;
        return super.pour(level, pos, dX, dY, dZ, side, stack);
    }

    /** Der Knoten im Giessereinetz. Er merkt sich, welches Material der Strang gerade fuehrt. */
    public static class FoundryNode extends GenNode<FoundryNetwork> {

        public NTMMaterial type;

        public FoundryNode(INetworkProvider<FoundryNetwork> provider, BlockPos... positions) {
            super(provider, positions);
        }
    }
}
