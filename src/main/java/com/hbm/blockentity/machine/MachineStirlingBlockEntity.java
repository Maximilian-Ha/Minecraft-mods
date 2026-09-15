package com.hbm.blockentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.IConfigurableMachine;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.tile.IHeatSource;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.entity.projectile.Cog;
import com.hbm.items.machine.GearItem.GearType;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.sounds.SoundSource;
import java.io.IOException;

public class MachineStirlingBlockEntity extends LoadedBaseBlockEntity implements IEnergyProviderMK2, ITickable {

    public long powerBuffer;
    public int heat;
    private int warnCooldown = 0;
    private int overspeed = 0;
    public boolean hasCog = true;

    public float spin;
    public float lastSpin;

    /* CONFIGURABLE CONSTANTS */
    // todo config
    public static double diffusion = 0.1D;
    public static double efficiency = 0.5D;
    public static int maxHeatNormal = 300;
    public static int overspeedLimit = 300;

    private AABB bb;

    public MachineStirlingBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_STIRLING.get(), pos, state);
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {

            if(this.hasCog) {
                this.powerBuffer = 0;
                this.tryPullHeat();

                this.powerBuffer = (long) (this.heat * efficiency);

                if(this.warnCooldown > 0) this.warnCooldown--;

                if(this.heat > this.maxHeat()) {

                    this.overspeed++;

                    if(this.overspeed > 60 && this.warnCooldown == 0) {
                        this.warnCooldown = 100;
                        // Warnton einen Block ueber dem Kern, wie im Original (Z. 64).
                        this.level.playSound(null, this.getBlockPos().above(), NtmSoundEvents.WARN_OVERSPEED.get(),
                                SoundSource.BLOCKS, 2.0F, 1.0F);
                    }

                    if(this.overspeed > overspeedLimit) {
                        BlockPos bp = this.getBlockPos();

                        this.hasCog = false;
                        this.level.explode(null,
                                bp.getX() + 0.5,
                                bp.getY() + 1,
                                bp.getZ() + 0.5,
                                5F, false, Level.ExplosionInteraction.NONE);

                        // Auswurf des Zahnrads, TileEntityStirling.java:70-79. Startpunkt ist
                        // einen Block ueber dem Kern und einen Block in Blickrichtung versetzt.
                        Direction dir = this.getDir();
                        Cog cog = new Cog(this.level, bp.getX() + 0.5 + dir.getStepX(), bp.getY() + 1, bp.getZ() + 0.5 + dir.getStepZ());
                        cog.setOrientation(dir.get3DDataValue());
                        cog.setMeta(this.getGearMeta());

                        // ForgeDirection.getRotation(DOWN) entspricht getCounterClockWise(Axis.Y)
                        Direction rot = dir.getCounterClockWise(Axis.Y);

                        // Je weiter ueber der Hitzegrenze, desto hoeher fliegt es
                        cog.setDeltaMovement(rot.getStepX(), 1 + (this.heat - this.maxHeat()) * 0.0001D, rot.getStepZ());
                        this.level.addFreshEntity(cog);

                        this.setChanged();
                    }

                } else {
                    this.overspeed = 0;
                }
            } else {
                this.overspeed = 0;
                this.warnCooldown = 0;
            }

            this.networkPackNT(150);

            if(this.hasCog) {
                for(DirPos pos : this.getConPos()) {
                    this.tryProvide(this.level, pos.makeCompat(), pos.getDir());
                }
            } else {
                if(this.powerBuffer > 0) this.powerBuffer--;
            }

            this.heat = 0;
        } else {

            float momentum = this.powerBuffer * 50F / (float) this.maxHeat();

            this.lastSpin = this.spin;
            this.spin += momentum;

            if(this.spin >= 360F) {
                this.spin -= 360F;
                this.lastSpin -= 360F;
            }
        }
    }

    public int maxHeat() {
        return maxHeatNormal;
    }

    private Direction getDir() {
        return this.getBlockState().getValue(DummyableBlock.FACING);
    }

    /**
     * Entspricht TileEntityStirling.getGeatMeta(): 0 fuer machine_stirling,
     * 2 fuer machine_stirling_creative, sonst 1 (machine_stirling_steel).
     * Im Port gibt es nur machine_stirling, also bleibt es beim Eisenzahnrad.
     */
    public int getGearMeta() {
        return GearType.IRON.ordinal();
    }

    protected DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ(), Direction.EAST),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ(), Direction.WEST),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 2, Direction.SOUTH),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 2, Direction.NORTH)
        };
    }

    protected void tryPullHeat() {
        if(this.level == null) return;

        BlockEntity con = this.level.getBlockEntity(this.getBlockPos().below());

        if(con instanceof IHeatSource source) {
            int heatSrc = (int) (source.getHeatStored() * diffusion);

            if(heatSrc > 0) {
                source.useUpHeat(heatSrc);
                this.heat += heatSrc;
                return;
            }
        }

        this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.powerBuffer);
        buf.writeInt(this.heat);
        buf.writeBoolean(this.hasCog);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.powerBuffer = buf.readLong();
        this.heat = buf.readInt();
        this.hasCog = buf.readBoolean();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.powerBuffer = tag.getLong("powerBuffer");
        this.overspeed = tag.getInt("overspeed");
        // Neue Maschinen haben kein "hasCog"-Tag -- dann mit Zahnrad starten
        this.hasCog = !tag.contains("hasCog") || tag.getBoolean("hasCog");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putLong("powerBuffer", this.powerBuffer);
        tag.putBoolean("hasCog", this.hasCog);
        tag.putInt("overspeed", this.overspeed);
    }

    @Override
    public void setPower(long power) {
        this.powerBuffer = power;
    }

    @Override
    public long getPower() {
        return this.powerBuffer;
    }

    @Override
    public long getMaxPower() {
        return this.powerBuffer;
    }

    public AABB getRenderBoundingBox() {
        if(this.bb == null) {
            BlockPos pos = this.getBlockPos();
            this.bb = new AABB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 2,
                    pos.getZ() + 2
            );
        }

        return this.bb;
    }

    /** Konfiguration, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static void readConfig(JsonObject obj) {
        diffusion = IConfigurableMachine.grab(obj, "D:diffusion", diffusion);
        efficiency = IConfigurableMachine.grab(obj, "D:efficiency", efficiency);
        maxHeatNormal = IConfigurableMachine.grab(obj, "I:maxHeatNormal", maxHeatNormal);
        overspeedLimit = IConfigurableMachine.grab(obj, "I:overspeedLimit", overspeedLimit);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("D:diffusion").value(diffusion);
        writer.name("D:efficiency").value(efficiency);
        writer.name("I:maxHeatNormal").value(maxHeatNormal);
        writer.name("I:overspeedLimit").value(overspeedLimit);
    }
}
