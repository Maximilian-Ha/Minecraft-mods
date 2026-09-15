package com.hbm.blockentity.machine.icf;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.icf.ICFBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityICFController.
 *
 * Die Steuerung des ICF-Lasers. Sie liest die Anlage vor sich ein, zaehlt die Bauteile und
 * schiesst den gesammelten Strom als Laserstrahl nach vorn. Trifft der Strahl die Brennkammer,
 * geht die Energie dort hinein; trifft er etwas anderes, verdampft es -- solange es nicht
 * sprengfester ist als Obsidian. Was im Strahl steht, brennt.
 *
 * Die Leistung folgt aus der Bauform: Wurzel aus der Zahl der Kondensatoren mal deren Leistung,
 * plus Wurzel aus der Zahl der Turbolader mal deren Leistung, wobei nie mehr Turbolader zaehlen
 * als Kondensatoren da sind. Die Wurzel ist die Bremse -- doppelte Anlage heisst nicht doppelte
 * Leistung.
 *
 * ABWEICHUNG: das Original schlaegt mit DamageSource.inFire zu. Der Port nimmt seinen eigenen
 * Feuer-Schadenstyp, der dafuer schon da ist.
 */
public class ICFControllerBlockEntity extends LoadedBaseBlockEntity implements ITickable, IEnergyReceiverMK2 {

    /** Leistung je Kondensator, vor der Wurzel. */
    public static final int CAPACITOR_POWER = 2_500_000;
    /** Leistung je Turbolader, vor der Wurzel. */
    public static final int TURBO_POWER = 5_000_000;
    /** Weiter als das schiesst der Laser nicht. */
    public static final int MAX_RANGE = 50;

    public long power;
    public int laserLength;

    public int cellCount;
    public int emitterCount;
    public int capacitorCount;
    public int turbochargerCount;

    public boolean assembled;

    /** Die Anschlussstellen, an denen die Anlage Strom aufnimmt. */
    protected final List<BlockPos> ports = new ArrayList<>();

    private AABB renderBox;

    public ICFControllerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.ICF_CONTROLLER.get(), pos, state);
    }

    /** In welche Richtung die Steuerung schaut -- dorthin geht der Strahl. */
    public Direction getFacing() {
        return this.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
    }

    /**
     * Zaehlt aus, was die eingelesene Anlage hergibt. Nur was in der Kette haengt, zaehlt:
     * Zellen in einer ununterbrochenen Reihe vor der Steuerung, Emitter an einer solchen Zelle,
     * Kondensatoren an einem solchen Emitter, Turbolader an einem solchen Kondensator.
     */
    public void setup(Set<BlockPos> ports, Set<BlockPos> cells, Set<BlockPos> emitters,
                      Set<BlockPos> capacitors, Set<BlockPos> turbochargers) {

        this.cellCount = 0;
        this.emitterCount = 0;
        this.capacitorCount = 0;
        this.turbochargerCount = 0;

        Direction dir = this.getFacing().getOpposite();

        Set<BlockPos> validCells = new java.util.HashSet<>();
        Set<BlockPos> validEmitters = new java.util.HashSet<>();
        Set<BlockPos> validCapacitors = new java.util.HashSet<>();

        /* Die Reihe bricht beim ersten Loch ab. */
        for(int i = 0; i < cells.size(); i++) {
            BlockPos pos = this.worldPosition.relative(dir, i + 1);
            if(!cells.contains(pos)) break;
            this.cellCount++;
            validCells.add(pos);
        }

        this.countAdjacent(emitters, validCells, validEmitters);
        this.countAdjacent(capacitors, validEmitters, validCapacitors);
        this.countAdjacent(turbochargers, validCapacitors, null);

        this.emitterCount = validEmitters.size();
        this.capacitorCount = validCapacitors.size();

        this.ports.clear();
        this.ports.addAll(ports);
    }

    /** Zaehlt alle Kandidaten, die an mindestens einen bereits gueltigen Block grenzen. */
    private void countAdjacent(Set<BlockPos> candidates, Set<BlockPos> valid, Set<BlockPos> out) {
        for(BlockPos candidate : candidates) {
            for(Direction offset : Direction.values()) {
                if(!valid.contains(candidate.relative(offset))) continue;
                if(out != null) out.add(candidate);
                else this.turbochargerCount++;
                break;
            }
        }
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.level.isClientSide) {

            /* Ein bisschen Funkenflug vor der Muendung, solange der Strahl steht. */
            if(this.laserLength > 0 && this.level.random.nextInt(5) == 0) {

                Direction dir = this.getFacing();
                Direction rot = dir.getClockWise();

                double offXZ = this.level.random.nextDouble() * 0.25D - 0.125D;
                double offY = this.level.random.nextDouble() * 0.25D - 0.125D;
                double dist = 0.55D;

                this.level.addParticle(new DustParticleOptions(new Vector3f(1F, 0F, 0F), 1F),
                        this.worldPosition.getX() + 0.5D + dir.getStepX() * dist + rot.getStepX() * offXZ,
                        this.worldPosition.getY() + 0.5D + offY,
                        this.worldPosition.getZ() + 0.5D + dir.getStepZ() * dist + rot.getStepZ() * offXZ,
                        0D, 0D, 0D);
            }

            return;
        }

        this.networkPackNT(50);

        if(!this.assembled) {
            this.laserLength = 0;
            return;
        }

        /* Strom holen: an jeder Anschlussstelle, in jede Richtung. */
        for(BlockPos pos : this.ports) {
            for(Direction dir : Direction.values()) {
                if(this.getMaxPower() > 0) this.trySubscribe(this.level, new DirPos(pos.relative(dir), dir));
            }
        }

        if(this.power <= 0) {
            this.laserLength = 0;
            return;
        }

        this.fireLaser();
        this.setPower(0);
    }

    /** Schiesst den Strahl nach vorn und sucht, worauf er trifft. */
    private void fireLaser() {

        Direction dir = this.getFacing();

        for(int i = 1; i < MAX_RANGE; i++) {

            this.laserLength = i;
            BlockPos hit = this.worldPosition.relative(dir, i);

            /*
             * Die Brennkammer nimmt den Strahl auf. Ihr Kern sitzt acht Bloecke weiter und drei
             * tiefer als die Stelle, an der die Huelle getroffen wird -- so steht es im Original.
             */
            if(this.level.getBlockState(hit).is(NtmBlocks.ICF.get())) {
                BlockPos corePos = this.worldPosition.relative(dir, i + 8).below(3);
                if(this.level.getBlockEntity(corePos) instanceof ICFBlockEntity icf) {
                    icf.laser += this.getPower();
                    icf.maxLaser += this.getMaxPower();
                    break;
                }
            }

            BlockState state = this.level.getBlockState(hit);

            if(!state.isAir()) {
                /* Alles unter Obsidian-Festigkeit verdampft. */
                if(state.getBlock().getExplosionResistance() < 6000F) this.level.destroyBlock(hit, false);
                break;
            }
        }

        /* Wer im Strahl steht, brennt. */
        AABB beam = new AABB(
                Math.min(this.worldPosition.getX(), this.worldPosition.getX() + dir.getStepX() * this.laserLength) + 0.2D,
                this.worldPosition.getY() + 0.2D,
                Math.min(this.worldPosition.getZ(), this.worldPosition.getZ() + dir.getStepZ() * this.laserLength) + 0.2D,
                Math.max(this.worldPosition.getX(), this.worldPosition.getX() + dir.getStepX() * this.laserLength) + 0.8D,
                this.worldPosition.getY() + 0.8D,
                Math.max(this.worldPosition.getZ(), this.worldPosition.getZ() + dir.getStepZ() * this.laserLength) + 0.8D);

        DamageSource source = new DamageSource(
                this.level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(NtmDamageTypes.FIRE));

        for(Entity entity : this.level.getEntitiesOfClass(Entity.class, beam)) {
            entity.hurt(source, 50F);
            entity.igniteForSeconds(5F);
        }
    }

    /* --- Strom --- */

    @Override public long getPower() { return Math.min(this.power, this.getMaxPower()); }
    @Override public void setPower(long power) { this.power = power; }

    @Override
    public long getMaxPower() {
        return (long) (Math.sqrt(this.capacitorCount) * CAPACITOR_POWER
                + Math.sqrt(Math.min(this.turbochargerCount, this.capacitorCount)) * TURBO_POWER);
    }

    /* --- Speichern und Uebertragen --- */

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.capacitorCount);
        buf.writeInt(this.turbochargerCount);
        buf.writeInt(this.laserLength);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.capacitorCount = buf.readInt();
        this.turbochargerCount = buf.readInt();
        this.laserLength = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);

        this.power = tag.getLong("power");
        this.assembled = tag.getBoolean("assembled");
        this.cellCount = tag.getInt("cellCount");
        this.emitterCount = tag.getInt("emitterCount");
        this.capacitorCount = tag.getInt("capacitorCount");
        this.turbochargerCount = tag.getInt("turbochargerCount");

        this.ports.clear();
        int portCount = tag.getInt("portCount");
        for(int i = 0; i < portCount; i++) this.ports.add(BlockPos.of(tag.getLong("p" + i)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putLong("power", this.power);
        tag.putBoolean("assembled", this.assembled);
        tag.putInt("cellCount", this.cellCount);
        tag.putInt("emitterCount", this.emitterCount);
        tag.putInt("capacitorCount", this.capacitorCount);
        tag.putInt("turbochargerCount", this.turbochargerCount);

        tag.putInt("portCount", this.ports.size());
        for(int i = 0; i < this.ports.size(); i++) tag.putLong("p" + i, this.ports.get(i).asLong());
    }

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - MAX_RANGE, y, z - MAX_RANGE, x + MAX_RANGE + 1, y + 1, z + MAX_RANGE + 1);
        }
        return this.renderBox;
    }
}
