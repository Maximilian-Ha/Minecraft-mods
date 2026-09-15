package com.hbm.blockentity.machine.albion;

import com.hbm.blockentity.CooledBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.albion.MachinePASourceBlockEntity.PAState;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.menus.MachinePARFCMenu;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.albion.TileEntityPARFC.
 *
 * Die Hochfrequenzkavitaet -- das einzige Bauteil, das dem Teilchen Impuls gibt. Hundert je
 * Durchgang, und weil ein Lauf zehntausende braucht, faehrt der Strahl hunderte Runden.
 *
 * SIE STREUT DEN STRAHL GENAUSO STARK, WIE SIE IHN BESCHLEUNIGT: hundert Impuls fuer hundert
 * Streuung. Bei tausend Streuung ist er hin -- ohne Quadrupole dazwischen kommt man also keine
 * zehn Kavitaeten weit. Das ist der Grund, warum ein Beschleuniger ein Ring ist und keine Gerade.
 *
 * NEUN BLOECKE LANG, und sie zaehlt diese neun auch als Strecke.
 *
 * VIERTELMILLION JE DURCHGANG, und ihr Speicher fasst nur eine Million -- sie will an eine
 * Leitung, nicht an eine Batterie.
 */
public class MachinePARFCBlockEntity extends CooledBaseBlockEntity implements IParticleUser {

    public static final long usage = 250_000;
    public static final int momentumGain = 100;
    public static final int defocusGain = 100;

    public static final int SLOT_BATTERY = 0;

    public MachinePARFCBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_PA_RFC.get(), pos, state, 1);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.paRFC");
    }

    @Override
    public long getMaxPower() {
        return 1_000_000;
    }

    /** Die Richtung, in die der Strahl durch die Kavitaet laeuft. */
    public Direction getBeamlineDir() {
        return this.getDir().getCounterClockWise(Axis.Y);
    }

    public Direction getDir() {
        BlockState state = this.getBlockState();
        return state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
    }

    @Override
    public boolean canParticleEnter(Particle particle, Direction dir, BlockPos pos) {
        Direction rfcDir = this.getBeamlineDir();
        return this.worldPosition.relative(rfcDir, -4).equals(pos) && rfcDir == dir;
    }

    @Override
    public void onEnter(Particle particle, Direction dir) {

        if(!this.isCool()) particle.crash(PAState.CRASH_NOCOOL);
        if(this.power < usage) particle.crash(PAState.CRASH_NOPOWER);

        if(particle.invalid) return;

        particle.addDistance(9);
        particle.accelerate(momentumGain);
        particle.defocus(defocusGain);
        this.power -= usage;
    }

    @Override
    public BlockPos getExitPos(Particle particle) {
        return this.worldPosition.relative(this.getBeamlineDir(), 5);
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
            this.renderBox = new AABB(p.getX() - 4, p.getY() - 1, p.getZ() - 4, p.getX() + 5, p.getY() + 2, p.getZ() + 5);
        }
        return this.renderBox;
    }

    @Override
    public DirPos[] getConPos() {

        Direction dir = this.getDir().getClockWise(Axis.Y);
        BlockPos p = this.worldPosition;

        return new DirPos[] {
                new DirPos(p.relative(dir, 3).above(2), Direction.UP),
                new DirPos(p.relative(dir, -3).above(2), Direction.UP),
                new DirPos(p.above(2), Direction.UP),
                new DirPos(p.relative(dir, 3).below(2), Direction.DOWN),
                new DirPos(p.relative(dir, -3).below(2), Direction.DOWN),
                new DirPos(p.below(2), Direction.DOWN)
        };
    }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return false; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[0]; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachinePARFCMenu(id, inventory, this);
    }
}
