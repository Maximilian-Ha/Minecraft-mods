package com.hbm.blockentity.turret;

import com.hbm.blocks.DummyableBlock;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Die Grundlage der grossen Geschuetztuerme: alles, was auf einem 2x2-Fuss steht statt auf einem
 * einzelnen Block.
 *
 * Zwei Dinge unterscheiden sie vom kleinen Wachturm. Erstens sitzt ihr Drehpunkt nicht in der
 * Blockmitte, sondern in der Mitte des ganzen Fusses -- wo genau, rechnet sich aus den Massen des
 * Mehrfachblocks und seiner Richtung aus, statt wie im Original aus einer Tabelle nach Metadatum.
 *
 * Zweitens ziehen sie ihren Strom nicht von den sechs Nachbarn, sondern von acht Punkten rings um
 * den Fuss: je zwei an jeder Seite, jeweils einen Block weiter aussen.
 */
public abstract class TurretDummyableBlockEntity extends TurretBaseBlockEntity {

    public TurretDummyableBlockEntity(BlockEntityType<? extends TurretDummyableBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Die Mitte des Fusses, waagerecht, in Bloecken vom Kern aus gezaehlt. Steht der Kern in der
     * Ecke eines 2x2-Fusses, liegt die Mitte genau auf seiner Kante -- deshalb kommen hier ganze
     * Zahlen und keine halben heraus.
     */
    @Override
    public Vec3 getHorizontalOffset() {

        if(!(this.getBlockState().getBlock() instanceof DummyableBlock dummyable)) return new Vec3(0.5, 0, 0.5);

        int[] rot = MultiblockHandlerXR.rotate(dummyable.getDimensions(), this.getBlockState().getValue(DummyableBlock.FACING));
        if(rot == null) return new Vec3(0.5, 0, 0.5);

        return new Vec3((rot[5] - rot[4] + 1) / 2D, 0, (rot[3] - rot[2] + 1) / 2D);
    }

    /**
     * Die acht Anschlusspunkte: an jeder der vier Seiten des Fusses zwei, jeweils einen Block
     * ausserhalb. Wer eine Leitung an irgendeine Kante des Turms legt, trifft.
     */
    @Override
    protected void updateConnections() {

        if(this.level == null) return;

        Vec3 offset = this.getHorizontalOffset();
        BlockPos base = this.getBlockPos().offset((int) offset.x, 0, (int) offset.z);

        this.trySubscribe(this.level, new DirPos(base.offset(-2, 0, 0), Direction.WEST));
        this.trySubscribe(this.level, new DirPos(base.offset(-2, 0, -1), Direction.WEST));

        this.trySubscribe(this.level, new DirPos(base.offset(-1, 0, +1), Direction.SOUTH));
        this.trySubscribe(this.level, new DirPos(base.offset(0, 0, +1), Direction.SOUTH));

        this.trySubscribe(this.level, new DirPos(base.offset(+1, 0, 0), Direction.EAST));
        this.trySubscribe(this.level, new DirPos(base.offset(+1, 0, -1), Direction.EAST));

        this.trySubscribe(this.level, new DirPos(base.offset(0, 0, -2), Direction.NORTH));
        this.trySubscribe(this.level, new DirPos(base.offset(-1, 0, -2), Direction.NORTH));
    }
}
