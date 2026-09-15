package com.hbm.entity.item;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.lib.Library;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class MovingConveyorObject extends Entity {

    protected int lerpSteps;
    protected double lerpX;
    protected double lerpY;
    protected double lerpZ;
    protected float lerpYRot;
    protected float lerpXRot;

    public MovingConveyorObject(EntityType<? extends MovingConveyorObject> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpXRot = xRot;
        this.lerpSteps = 10;
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        this.setDeltaMovement(x, y, z);
    }

    @Override
    public double lerpTargetX() {
        return this.lerpSteps > 0 ? this.lerpX : this.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.lerpSteps > 0 ? this.lerpY : this.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.lerpSteps > 0 ? this.lerpZ : this.getZ();
    }

    @Override
    public float lerpTargetXRot() {
        return this.lerpSteps > 0 ? this.lerpXRot : this.xRot;
    }

    @Override
    public float lerpTargetYRot() {
        return this.lerpSteps > 0 ? this.lerpYRot : this.yRot;
    }

    @Override
    public void tick() {
        if(this.level.isClientSide) {
            if(this.lerpSteps > 0) {
                this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
                this.lerpSteps--;
            } else {
                this.reapplyPosition();
            }
        } else {
            tickCount++;

            if(this.tickCount <= 5) {
                return;
            }

            BlockPos pos = this.blockPosition();
            Block b = this.level.getBlockState(pos).getBlock();
            boolean isOnConveyor = b instanceof IConveyorBelt icb && icb.canItemStay(this.level, pos, this.position());

            if(!isOnConveyor) {

                if(onLeaveConveyor()) {
                    return;
                }
            } else {

                Vec3 target = ((IConveyorBelt) b).getTravelLocation(this.level, pos, this.position(), this.getMoveSpeed());
                this.setDeltaMovement(
                        new Vec3(
                                target.x - this.position().x,
                                target.y - this.position().y,
                                target.z - this.position().z
                        )
                );
            }

            /*
             * Das Fahren steht AUSSERHALB der beiden Zweige, so wie im Original. Wer die
             * Strecke verlaesst und onLeaveConveyor ueberlebt -- ein Paket zum Beispiel --,
             * faehrt mit seinem bisherigen Schwung weiter und kann so noch in eine Maschine
             * hineinfahren. Nur fuer den einzelnen Gegenstand macht es keinen Unterschied, der
             * gibt an dieser Stelle immer auf.
             */
            BlockPos lastPos = this.blockPosition();
            this.move(MoverType.SELF, this.getDeltaMovement());
            BlockPos newPos = this.blockPosition();

            if(lastPos.equals(newPos)) return;

            BlockState newState = level.getBlockState(newPos);

            if(newState.getBlock() instanceof IEnterableBlock inb) {

                Direction dir = directionBetween(lastPos, newPos);
                if(dir != null) this.enterBlock(inb, newPos, dir);

            } else if(!newState.isSolidRender(level, newPos)) {

                if(level.getBlockState(newPos.below()).getBlock() instanceof IEnterableBlock below) {
                    this.enterBlockFalling(below, newPos);
                }
            }
        }
    }

    /**
     * Aus welcher Richtung der Gegenstand in den neuen Block gefahren ist -- also die Richtung,
     * die vom neuen Block aus auf den alten zeigt. Diagonale Wechsel gibt es nicht; kommt doch
     * einer vor, bleibt die Richtung offen und es wird nichts betreten.
     */
    private static Direction directionBetween(BlockPos from, BlockPos to) {

        if(from.getY() == to.getY() && from.getZ() == to.getZ()) {
            if(from.getX() > to.getX()) return Library.POS_X;
            if(from.getX() < to.getX()) return Library.NEG_X;
        }
        if(from.getX() == to.getX() && from.getZ() == to.getZ()) {
            if(from.getY() > to.getY()) return Library.POS_Y;
            if(from.getY() < to.getY()) return Library.NEG_Y;
        }
        if(from.getX() == to.getX() && from.getY() == to.getY()) {
            if(from.getZ() > to.getZ()) return Library.POS_Z;
            if(from.getZ() < to.getZ()) return Library.NEG_Z;
        }

        return null;
    }

    public abstract void enterBlock(IEnterableBlock enterable, BlockPos pos, Direction dir);

    public void enterBlockFalling(IEnterableBlock enterable, BlockPos pos) {
        this.enterBlock(enterable, pos.offset(0, -1, 0), Direction.UP);
    }

    /**
     * @return true if the update loop should end
     */
    public abstract boolean onLeaveConveyor();

    public double getMoveSpeed() {
        return 0.0625D;
    }
}
