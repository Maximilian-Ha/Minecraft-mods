package com.hbm.blockentity.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Heliostatspiegel: richtet sich auf den Solarkessel aus und leitet Sonnenlicht
 * als Waerme an ihn weiter. Das Ziel wird mit dem Spiegelwerkzeug gesetzt.
 */
public class SolarMirrorBlockEntity extends LoadedBaseBlockEntity implements ITickable {

    public int tX;
    public int tY;
    public int tZ;
    public boolean isOn;

    private AABB renderBox;

    public SolarMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.SOLAR_MIRROR.get(), pos, state);
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        BlockPos pos = this.getBlockPos();

        if(!this.level.isClientSide) {

            if(this.level.getGameTime() % 20 == 0) this.networkPackNT(200);

            if(this.tY < pos.getY()) {
                this.isOn = false;
                return;
            }

            int sun = this.level.getBrightness(LightLayer.SKY, pos) - this.level.getSkyDarken() - 11;

            if(sun <= 0 || !this.level.canSeeSky(pos.above())) {
                this.isOn = false;
                return;
            }

            this.isOn = true;

            BlockEntity be = this.level.getBlockEntity(new BlockPos(this.tX, this.tY - 1, this.tZ));

            if(be instanceof SolarBoilerBlockEntity boiler) {
                boiler.heat += sun;
            }
        } else {

            BlockEntity be = this.level.getBlockEntity(new BlockPos(this.tX, this.tY - 1, this.tZ));

            if(this.isOn && be instanceof SolarBoilerBlockEntity boiler) {
                boiler.primary.add(pos);
            }

            // Das markBlockForUpdate des Originals entfaellt: der Spiegel wird hier
            // ueber einen BlockEntityRenderer gezeichnet, nicht im Chunkmesh gebacken.
        }
    }

    public void setTarget(int x, int y, int z) {
        this.tX = x;
        this.tY = y;
        this.tZ = z;
        this.setChanged();
        this.networkPackNT(200);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.tX);
        buf.writeInt(this.tY);
        buf.writeInt(this.tZ);
        buf.writeBoolean(this.isOn);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.tX = buf.readInt();
        this.tY = buf.readInt();
        this.tZ = buf.readInt();
        this.isOn = buf.readBoolean();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tX = tag.getInt("targetX");
        this.tY = tag.getInt("targetY");
        this.tZ = tag.getInt("targetZ");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("targetX", this.tX);
        tag.putInt("targetY", this.tY);
        tag.putInt("targetZ", this.tZ);
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos pos = this.getBlockPos();
            this.renderBox = new AABB(
                    pos.getX() - 25,
                    pos.getY() - 25,
                    pos.getZ() - 25,
                    pos.getX() + 25,
                    pos.getY() + 25,
                    pos.getZ() + 25
            );
        }
        return this.renderBox;
    }
}
