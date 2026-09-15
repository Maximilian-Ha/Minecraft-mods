package com.hbm.blockentity.machine.albion;

import com.hbm.blockentity.CooledBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.albion.MachinePASourceBlockEntity.PAState;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.menus.MachinePADipoleMenu;
import com.hbm.items.machine.PACoilItem;
import com.hbm.items.machine.PACoilItem.EnumCoilType;
import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.DirPos;
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
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.albion.TileEntityPADipole.
 *
 * Die Weiche des Beschleunigers. Sie ist das einzige Bauteil, das den Strahl um die Ecke bringt
 * -- und das einzige, das entscheidet, wohin er ueberhaupt geht.
 *
 * DREI AUSGAENGE, EINE SCHWELLE: unter dem eingestellten Impuls nimmt der Strahl den einen Weg,
 * darueber den anderen, und liegt Redstone an, den dritten. So wird aus einem Ring eine Schleife,
 * die sich nach hunderten Runden von selbst zum Detektor oeffnet.
 *
 * SIE NIMMT DEN STRAHL AUS JEDER RICHTUNG AN -- als einziges Bauteil. Sie fragt nur, ob er auf
 * ihrer Hoehe und auf einer ihrer beiden Achsen liegt.
 *
 * GERADEAUS IST GRATIS: faehrt der Strahl in die Richtung weiter, aus der er kam, zaehlt die
 * Strecke einfach weiter und keine Strafe greift. Erst die Kurve setzt den Streckenzaehler
 * zurueck -- und erst dann prueft die Spule, ob die Kante lang genug war.
 *
 * ZWEI STRAFEN SIND EINE ZU VIEL: zu langsam zieht das Zehnfache, zu enge Kurve auch -- beides
 * zusammen das Hundertfache, und so viel liefert kein Netz. Dann reisst der Strahl ab.
 *
 * SIE HAENGT NUR OBEN UND UNTEN AN: an den Seiten ist kein Platz, dort laeuft der Strahl.
 */
public class MachinePADipoleBlockEntity extends CooledBaseBlockEntity implements IControlReceiver, IParticleUser {

    public static final long usage = 100_000;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_COIL = 1;

    /** Die drei Ausgaenge, jeweils 0 = Norden, 1 = Osten, 2 = Sueden, 3 = Westen. */
    public int dirLower;
    public int dirUpper;
    public int dirRedstone;
    public int threshold;

    public MachinePADipoleBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_PA_DIPOLE.get(), pos, state, 2);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.paDipole");
    }

    @Override
    public long getMaxPower() {
        return 2_500_000;
    }

    @Override
    public boolean canConnect(Direction dir) {
        return dir == Direction.UP || dir == Direction.DOWN;
    }

    @Override
    public boolean canConnect(FluidType type, Direction dir) {
        return dir == Direction.UP || dir == Direction.DOWN;
    }

    @Override
    public boolean canParticleEnter(Particle particle, Direction dir, BlockPos pos) {
        return this.worldPosition.getY() == pos.getY()
                && (this.worldPosition.getX() == pos.getX() || this.worldPosition.getZ() == pos.getZ());
    }

    @Override
    public void onEnter(Particle particle, Direction dir) {

        EnumCoilType type = PACoilItem.getCoil(this.slots.get(SLOT_COIL));
        boolean isInline = dir == this.getExitDir(particle);

        int mult = 1;
        if(type != null) {
            if(type.diMin > particle.momentum) mult *= 10;
            if(type.diDistMin > particle.distanceTraveled) mult *= 10;
            if(isInline) mult = 1;
        }

        if(!this.isCool()) particle.crash(PAState.CRASH_NOCOOL);
        if(this.power < usage * mult) particle.crash(PAState.CRASH_NOPOWER);
        if(type == null) particle.crash(PAState.CRASH_NOCOIL);
        if(type != null && type.diMax < particle.momentum && !isInline) particle.crash(PAState.CRASH_OVERSPEED);

        if(particle.invalid) return;

        if(isInline) {
            particle.addDistance(3);
        } else {
            particle.resetDistance();
        }

        this.power -= usage * mult;
    }

    @Override
    public BlockPos getExitPos(Particle particle) {
        particle.dir = this.getExitDir(particle);
        return this.worldPosition.relative(particle.dir, 2);
    }

    public Direction getExitDir(Particle particle) {
        int dit = particle.momentum < this.threshold
                ? this.dirLower : this.checkRedstone()
                ? this.dirRedstone : this.dirUpper;
        return ditToDir(dit);
    }

    /**
     * Prueft an den Anschlusspunkten, nicht am Kern: der Dipol ist drei mal drei Bloecke gross
     * und gaebe sonst keine Kante her, an die sich ein Hebel setzen liesse.
     */
    public boolean checkRedstone() {
        if(this.level == null) return false;
        for(DirPos pos : this.getConPos()) {
            if(this.level.hasNeighborSignal(pos.makeCompat())) return true;
        }
        return false;
    }

    @Override
    public void updateEntity() {

        if(this.level != null && !this.level.isClientSide) {
            this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, this.getMaxPower());
        }

        super.updateEntity();
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.dirLower);
        buf.writeInt(this.dirUpper);
        buf.writeInt(this.dirRedstone);
        buf.writeInt(this.threshold);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.dirLower = buf.readInt();
        this.dirUpper = buf.readInt();
        this.dirRedstone = buf.readInt();
        this.threshold = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.dirLower = tag.getInt("dirLower");
        this.dirUpper = tag.getInt("dirUpper");
        this.dirRedstone = tag.getInt("dirRedstone");
        this.threshold = tag.getInt("threshold");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("dirLower", this.dirLower);
        tag.putInt("dirUpper", this.dirUpper);
        tag.putInt("dirRedstone", this.dirRedstone);
        tag.putInt("threshold", this.threshold);
    }

    private AABB renderBox;

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 1, p.getY() - 1, p.getZ() - 1, p.getX() + 2, p.getY() + 2, p.getZ() + 2);
        }
        return this.renderBox;
    }

    @Override
    public DirPos[] getConPos() {

        BlockPos p = this.worldPosition;

        return new DirPos[] {
                new DirPos(p.above(2).east(), Direction.UP),
                new DirPos(p.above(2).west(), Direction.UP),
                new DirPos(p.above(2).south(), Direction.UP),
                new DirPos(p.above(2).north(), Direction.UP),
                new DirPos(p.below(2).east(), Direction.DOWN),
                new DirPos(p.below(2).west(), Direction.DOWN),
                new DirPos(p.below(2).south(), Direction.DOWN),
                new DirPos(p.below(2).north(), Direction.DOWN)
        };
    }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == SLOT_COIL && stack.getItem() instanceof PACoilItem; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[] { SLOT_COIL }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachinePADipoleMenu(id, inventory, this);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag data) {

        if(data.contains("lower")) this.dirLower++;
        if(data.contains("upper")) this.dirUpper++;
        if(data.contains("redstone")) this.dirRedstone++;
        if(data.contains("threshold")) this.threshold = data.getInt("threshold");

        if(this.dirLower > 3) this.dirLower -= 4;
        if(this.dirUpper > 3) this.dirUpper -= 4;
        if(this.dirRedstone > 3) this.dirRedstone -= 4;

        this.threshold = Mth.clamp(this.threshold, 0, 999_999_999);
        this.setChanged();
    }

    public static Direction ditToDir(int dir) {
        if(dir == 1) return Direction.EAST;
        if(dir == 2) return Direction.SOUTH;
        if(dir == 3) return Direction.WEST;
        return Direction.NORTH;
    }

    public static String dirToName(int dir) {
        if(dir == 1) return "east";
        if(dir == 2) return "south";
        if(dir == 3) return "west";
        return "north";
    }
}
