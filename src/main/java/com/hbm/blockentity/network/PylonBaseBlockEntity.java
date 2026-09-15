package com.hbm.blockentity.network;

import api.hbm.energymk2.IEnergyConductorMK2;
import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.util.ColorUtil;
import com.hbm.util.Vec3NT;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Basis aller Strommasten. Masten werden paarweise per Draht verbunden; die Verbindungen sind
 * richtungslos (im Original ForgeDirection.UNKNOWN, hier dir == null im DirPos).
 */
public abstract class PylonBaseBlockEntity extends LoadedBaseBlockEntity implements IEnergyConductorMK2, ITickable {

    protected List<BlockPos> connected = new ArrayList<>();
    public int color;

    public PowerNode node;

    public PylonBaseBlockEntity(BlockEntityType<? extends PylonBaseBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void updateEntity() {
        if(level == null || level.isClientSide) return;

        if(this.node == null || this.node.expired) {
            this.node = Nodespace.getNode(level, this.getBlockPos());

            if(this.node == null || this.node.expired) {
                this.node = this.createNode();
                Nodespace.createNode(level, this.node);
            }
        }
    }

    /** 0 = ok, 1 = andere Bauart, 2 = derselbe Mast, 3 = zu weit weg */
    public static int canConnect(PylonBaseBlockEntity first, PylonBaseBlockEntity second) {

        if(first.getConnectionType() != second.getConnectionType())
            return 1;

        if(first == second)
            return 2;

        double len = Math.min(first.getMaxWireLength(), second.getMaxWireLength());

        Vec3NT firstPos = first.getConnectionPoint();
        Vec3NT secondPos = second.getConnectionPoint();

        Vec3NT delta = new Vec3NT(
                (secondPos.xCoord) - (firstPos.xCoord),
                (secondPos.yCoord) - (firstPos.yCoord),
                (secondPos.zCoord) - (firstPos.zCoord)
                );

        return len >= delta.length() ? 0 : 3;
    }

    public boolean setColor(ItemStack stack) {
        if(stack == null || stack.isEmpty()) return false;
        int color = ColorUtil.getColorFromDye(stack);
        if(color == 0 || color == this.color) return false;
        stack.shrink(1);
        this.color = color;

        this.setChanged();
        this.markForUpdate();

        return true;
    }

    @Override
    public PowerNode createNode() {
        PowerNode node = new PowerNode(this.getBlockPos()).setConnections(new DirPos(this.getBlockPos(), null));
        for(BlockPos pos : this.connected) node.addConnection(new DirPos(pos, null));
        return node;
    }

    public void addConnection(BlockPos pos) {

        connected.add(pos);

        PowerNode node = Nodespace.getNode(level, this.getBlockPos());

        if(node != null) {
            node.recentlyChanged = true;
            node.addConnection(new DirPos(pos, null));
        }

        this.setChanged();
        this.markForUpdate();
    }

    public void disconnectAll() {
        if(level == null) return;

        for(BlockPos pos : connected) {

            BlockEntity be = level.getBlockEntity(pos);

            if(be == this)
                continue;

            if(be instanceof PylonBaseBlockEntity pylon) {
                Nodespace.destroyNode(level, pos);
                pylon.node = null;

                for(int i = 0; i < pylon.connected.size(); i++) {
                    BlockPos conPos = pylon.connected.get(i);

                    if(conPos.equals(this.getBlockPos())) {
                        pylon.connected.remove(i);
                        i--;
                    }
                }

                pylon.setChanged();
                pylon.markForUpdate();
            }
        }

        Nodespace.destroyNode(level, this.getBlockPos());
        this.node = null;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if(this.level != null && !this.level.isClientSide) {
            if(this.node != null) {
                Nodespace.destroyNode(level, this.getBlockPos());
            }
        }
    }

    /** Ersatz fuer world.getPlayerManager().markBlockForUpdate() */
    protected void markForUpdate() {
        if(level != null && !level.isClientSide) {
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public abstract ConnectionType getConnectionType();
    public abstract Vec3NT[] getMountPos();
    public abstract double getMaxWireLength();

    public Vec3NT getConnectionPoint() {
        Vec3NT[] mounts = this.getMountPos();
        BlockPos pos = this.getBlockPos();

        if(mounts == null || mounts.length == 0)
            return new Vec3NT(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        return mounts[0].add(pos.getX(), pos.getY(), pos.getZ());
    }

    public List<BlockPos> getConnected() {
        return connected;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("conCount", connected.size());
        tag.putInt("color", color);

        for(int i = 0; i < connected.size(); i++) {
            BlockPos pos = connected.get(i);
            tag.putIntArray("con" + i, new int[] {pos.getX(), pos.getY(), pos.getZ()});
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        int count = tag.getInt("conCount");
        this.color = tag.getInt("color");

        this.connected.clear();

        for(int i = 0; i < count; i++) {
            int[] pos = tag.getIntArray("con" + i);
            if(pos.length == 3) this.connected.add(new BlockPos(pos[0], pos[1], pos[2]));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        this.loadAdditional(tag, registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public enum ConnectionType {
        SINGLE,
        TRIPLE,
        QUAD
        //more to follow
    }

    /**
     * Im Original INFINITE_EXTENT_AABB. Hier wird der Kasten aus dem Mast selbst und allen
     * Drahtenden gebildet, damit die Leitungen nicht weggecullt werden.
     */
    public AABB getRenderBoundingBox() {
        BlockPos pos = this.getBlockPos();

        double minX = pos.getX();
        double minY = pos.getY();
        double minZ = pos.getZ();
        double maxX = pos.getX() + 1;
        double maxY = pos.getY() + this.getRenderHeight();
        double maxZ = pos.getZ() + 1;

        for(BlockPos con : this.connected) {
            minX = Math.min(minX, con.getX());
            minY = Math.min(minY, con.getY());
            minZ = Math.min(minZ, con.getZ());
            maxX = Math.max(maxX, con.getX() + 1);
            maxY = Math.max(maxY, con.getY() + this.getRenderHeight());
            maxZ = Math.max(maxZ, con.getZ() + 1);
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /** Hoehe des Mastes in Bloecken, fuer den Renderkasten */
    public double getRenderHeight() {
        return 8D;
    }
}
