package com.hbm.blockentity.machine.albion;

import com.hbm.blockentity.CooledBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.albion.MachinePASourceBlockEntity.PAState;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.menus.MachinePAQuadrupoleMenu;
import com.hbm.items.machine.PACoilItem;
import com.hbm.items.machine.PACoilItem.EnumCoilType;
import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.albion.TileEntityPAQuadrupole.
 *
 * Der Quadrupol buendelt, was die Kavitaeten aufgefaechert haben -- hundert Streuung je
 * Durchgang, genau so viel, wie eine Kavitaet erzeugt. Eins zu eins: wer beschleunigen will,
 * muss gleich viele Buendler aufstellen.
 *
 * ER BRAUCHT EINE SPULE. Ohne stuerzt der Strahl ab, nicht etwa "laeuft ungebuendelt weiter" --
 * ein leerer Quadrupol im Ring ist ein Abbruch.
 *
 * UNTER DEM FENSTER DER SPULE ZIEHT ER DAS ZEHNFACHE. Eine Chlorophyt-Spule bei niedrigem Impuls
 * kostet also eine Million je Durchgang statt hunderttausend -- die teure Spule ist am Anfang
 * des Laufs die schlechtere. Ueber dem Fenster stuerzt er ab.
 */
public class MachinePAQuadrupoleBlockEntity extends CooledBaseBlockEntity implements IParticleUser {

    public static final long usage = 100_000;
    public static final int focusGain = 100;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_COIL = 1;

    public MachinePAQuadrupoleBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_PA_QUADRUPOLE.get(), pos, state, 2);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.paQuadrupole");
    }

    @Override
    public long getMaxPower() {
        return 2_500_000;
    }

    public Direction getDir() {
        BlockState state = this.getBlockState();
        return state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
    }

    public Direction getBeamlineDir() {
        return this.getDir().getCounterClockWise(Axis.Y);
    }

    @Override
    public boolean canParticleEnter(Particle particle, Direction dir, BlockPos pos) {
        Direction beamlineDir = this.getBeamlineDir();
        return this.worldPosition.relative(beamlineDir, -1).equals(pos) && beamlineDir == dir;
    }

    @Override
    public void onEnter(Particle particle, Direction dir) {

        EnumCoilType type = PACoilItem.getCoil(this.slots.get(SLOT_COIL));

        int mult = 1;
        if(type != null) mult = type.quadMin > particle.momentum ? 10 : 1;

        if(!this.isCool()) particle.crash(PAState.CRASH_NOCOOL);
        if(this.power < usage * mult) particle.crash(PAState.CRASH_NOPOWER);
        if(type == null) particle.crash(PAState.CRASH_NOCOIL);
        if(type != null && type.quadMax < particle.momentum) particle.crash(PAState.CRASH_OVERSPEED);

        if(particle.invalid) return;

        particle.addDistance(3);
        particle.focus(focusGain);
        this.power -= usage * mult;
    }

    @Override
    public BlockPos getExitPos(Particle particle) {
        return this.worldPosition.relative(this.getBeamlineDir(), 2);
    }

    @Override
    public void updateEntity() {

        if(this.level != null && !this.level.isClientSide) {
            this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, this.getMaxPower());
        }

        super.updateEntity();
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

        Direction dir = this.getDir();
        BlockPos p = this.worldPosition;

        return new DirPos[] {
                new DirPos(p.above(2), Direction.UP),
                new DirPos(p.below(2), Direction.DOWN),
                new DirPos(p.relative(dir, 2), dir),
                new DirPos(p.relative(dir, -2), dir.getOpposite())
        };
    }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == SLOT_COIL && stack.getItem() instanceof PACoilItem; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[] { SLOT_COIL }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachinePAQuadrupoleMenu(id, inventory, this);
    }
}
