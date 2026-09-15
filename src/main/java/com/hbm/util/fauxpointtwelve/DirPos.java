package com.hbm.util.fauxpointtwelve;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DirPos extends BlockPosNT {

    protected Direction dir;

    public DirPos(int x, int y, int z, Direction dir) {
        super(x, y, z);
        this.dir = dir;
    }

    public DirPos(BlockEntity be, Direction dir) {
        super(be);
        this.dir = dir;
    }

    public DirPos(BlockPos pos, Direction dir) {
        super(pos);
        this.dir = dir;
    }

    public DirPos(double x, double y, double z, Direction dir) {
        super(x, y, z);
        this.dir = dir;
    }

    @Override
    public DirPos rotate(Rotation rotationIn) {
        // Eine richtungslose Verbindung bleibt beim Drehen richtungslos.
        if(this.dir == null) {
            return switch (rotationIn) {
                case CLOCKWISE_90 -> new DirPos(-this.getZ(), this.getY(), this.getX(), null);
                case CLOCKWISE_180 -> new DirPos(-this.getX(), this.getY(), -this.getZ(), null);
                case COUNTERCLOCKWISE_90 -> new DirPos(this.getZ(), this.getY(), -this.getX(), null);
                default -> this;
            };
        }

        return switch (rotationIn) {
            case CLOCKWISE_90 -> new DirPos(-this.getZ(), this.getY(), this.getX(), rotationIn.rotate(this.getDir()));
            case CLOCKWISE_180 -> new DirPos(-this.getX(), this.getY(), -this.getZ(), this.getDir().getOpposite());
            case COUNTERCLOCKWISE_90 -> new DirPos(this.getZ(), this.getY(), -this.getX(), rotationIn.rotate(this.getDir()));
            default -> this;
        };
    }


    public Direction getDir() {
        return this.dir;
    }

    /**
     * Richtungslose Verbindung: dir ist null. Das ersetzt ForgeDirection.UNKNOWN aus 1.7.10,
     * das dort die Fernverbindungen von Masten, Umspannwerk und Anschlusskaesten markierte --
     * net.minecraft.core.Direction kennt nur die sechs echten Seiten.
     */
    public boolean isDirectionless() {
        return this.dir == null;
    }

    /** Seitenversatz der Verbindung; 0 wenn richtungslos, wie bei ForgeDirection.UNKNOWN. */
    public int getStepX() { return this.dir == null ? 0 : this.dir.getStepX(); }
    public int getStepY() { return this.dir == null ? 0 : this.dir.getStepY(); }
    public int getStepZ() { return this.dir == null ? 0 : this.dir.getStepZ(); }

    /** Gegenrichtung; null bleibt null, wie UNKNOWN.getOpposite() == UNKNOWN im Original. */
    public Direction getOppositeDir() {
        return this.dir == null ? null : this.dir.getOpposite();
    }
}
