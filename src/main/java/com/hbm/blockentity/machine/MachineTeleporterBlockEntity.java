package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.TickingBaseBlockEntity;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineTeleporter.
 *
 * Der Teleporter versetzt, was in ihm steht, an einen anderen Ort -- auch in eine andere Welt.
 * Eingestellt wird er nicht an ihm selbst, sondern mit dem Verbindungsstueck: erst auf das Ziel
 * klicken, dann auf den Teleporter.
 *
 * ER VERSETZT ALLES, NICHT NUR SPIELER. Was im Feld ueber ihm steht, geht mit -- Tiere,
 * fallende Gegenstaende, Loren. Das Feld ist bewusst schmal, eine halbe Blockbreite; wer
 * hindurchlaeuft, wird nicht aus Versehen mitgenommen.
 *
 * EIN SPRUNG KOSTET EINE MILLION HE, und der Speicher fasst anderthalb. Das ist teuer und soll
 * es sein: der Teleporter ist die Abkuerzung fuer den, der ein Kraftwerk hat.
 *
 * ABWEICHUNG: das Ziel wird als Name der Welt gemerkt, nicht als Zahl. Auf 1.7.10 sind
 * Dimensionen durchnummeriert, auf 1.21 heissen sie -- "minecraft:the_nether" statt "-1".
 * Damit ueberlebt eine Einstellung auch, wenn sich die Reihenfolge der Welten aendert.
 *
 * ABWEICHUNG: das Versetzen selbst laeuft ueber changeDimension, den Griff, den 1.21 dafuer
 * vorsieht. Das Original baut die Pakete an den Spieler von Hand zusammen -- auf 1.7.10 gab es
 * nichts anderes.
 */
public class MachineTeleporterBlockEntity extends TickingBaseBlockEntity implements IEnergyReceiverMK2 {

    public static final long MAX_POWER = 1_500_000;
    public static final long CONSUMPTION = 1_000_000;

    public long power;

    public int targetX = -1;
    public int targetY = -1;
    public int targetZ = -1;

    /** Die Zielwelt, oder null fuer "dieselbe wie hier". */
    public @Nullable ResourceLocation targetDim;

    public MachineTeleporterBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_TELEPORTER.get(), pos, state);
    }

    public boolean hasTarget() {
        return this.targetY != -1;
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.level.isClientSide) {

            if(this.hasTarget() && this.power >= CONSUMPTION) {
                this.level.addParticle(new DustParticleOptions(new Vector3f(0.4F, 0.8F, 1F), 1F),
                        this.worldPosition.getX() + 0.5 + this.level.random.nextGaussian() * 0.25,
                        this.worldPosition.getY() + 1 + this.level.random.nextDouble() * 2,
                        this.worldPosition.getZ() + 0.5 + this.level.random.nextGaussian() * 0.25,
                        0, 0, 0);
            }

            return;
        }

        for(Direction dir : Direction.values()) {
            this.trySubscribe(this.level, new DirPos(this.worldPosition.relative(dir), dir));
        }

        if(this.hasTarget() && this.power >= CONSUMPTION) {

            AABB field = new AABB(
                    this.worldPosition.getX() + 0.25, this.worldPosition.getY(), this.worldPosition.getZ() + 0.25,
                    this.worldPosition.getX() + 0.75, this.worldPosition.getY() + 2, this.worldPosition.getZ() + 0.75);

            List<Entity> entities = this.level.getEntities((Entity) null, field, e -> !e.isRemoved());

            for(Entity entity : entities) this.teleport(entity);
        }

        this.networkPackNT(15);
    }

    private void teleport(Entity entity) {

        if(this.power < CONSUMPTION) return;
        if(!(this.level instanceof ServerLevel here)) return;

        ServerLevel there = here;

        if(this.targetDim != null) {
            ServerLevel resolved = here.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, this.targetDim));
            if(resolved == null) return;
            there = resolved;
        }

        this.level.playSound(null, this.worldPosition, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1F, 1F);

        double x = this.targetX + 0.5;
        double y = this.targetY + 1;
        double z = this.targetZ + 0.5;

        /* EIN GRIFF FUER BEIDE FAELLE: changeDimension bedient auf 1.21 auch den Sprung
         * innerhalb derselben Welt, und fuer Spieler ist es ohnehin der vorgesehene Weg. */
        entity.changeDimension(new DimensionTransition(there, new Vec3(x, y, z), Vec3.ZERO,
                entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING));

        there.playSound(null, BlockPos.containing(x, y, z), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1F, 1F);

        this.power -= CONSUMPTION;
        this.setChanged();
    }

    /** Stellt das Ziel ein; wird vom Verbindungsstueck gerufen. */
    public void setTarget(int x, int y, int z, ResourceLocation dim) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.targetDim = dim;
        this.setChanged();
    }

    @Override public boolean canConnect(Direction dir) { return true; }

    @Override public void setPower(long power) { this.power = power; }
    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.targetX);
        buf.writeInt(this.targetY);
        buf.writeInt(this.targetZ);
        buf.writeUtf(this.targetDim == null ? "" : this.targetDim.toString());
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.targetX = buf.readInt();
        this.targetY = buf.readInt();
        this.targetZ = buf.readInt();
        String dim = buf.readUtf();
        this.targetDim = dim.isEmpty() ? null : ResourceLocation.tryParse(dim);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.targetX = tag.getInt("x1");
        this.targetY = tag.contains("y1") ? tag.getInt("y1") : -1;
        this.targetZ = tag.getInt("z1");
        String dim = tag.getString("dim");
        this.targetDim = dim.isEmpty() ? null : ResourceLocation.tryParse(dim);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putInt("x1", this.targetX);
        tag.putInt("y1", this.targetY);
        tag.putInt("z1", this.targetZ);
        tag.putString("dim", this.targetDim == null ? "" : this.targetDim.toString());
    }
}
