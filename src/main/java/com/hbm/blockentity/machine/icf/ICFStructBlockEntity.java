package com.hbm.blockentity.machine.icf;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.ToolConversionBlock;
import com.hbm.blocks.machine.icf.MachineICFBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityICFStruct.
 *
 * Der Zusammenbauklotz der Brennkammer. Er sieht jede Sekunde nach, ob die Kammer um ihn herum
 * fertig gemauert ist -- siebzehn Scheiben quer zur Blickrichtung, jede mit demselben Muster --
 * und ersetzt sich dann durch die Anlage.
 *
 * Die Baustufen sind verschieden: die Mittelachse aus rohen Bauteilen, die inneren Scheiben aus
 * verschweissten Gefaessen, die aeusseren aus verschraubter Struktur. Das Original prueft dafuer
 * die Metadatenwerte 0, 2 und 4; im Port sind das die entsprechenden Baustufen.
 */
public class ICFStructBlockEntity extends BlockEntity implements ITickable {

    private static final int CHECK_INTERVAL = 20;

    /** Rohe Bauteile: die Achse, an der der Laser entlanglaeuft. */
    private static final int STAGE_RAW = 0;
    /** Verschweisstes Gefaess: die inneren Scheiben. */
    private static final int STAGE_VESSEL_WELDED = 2;
    /** Verschraubte Struktur: die aeusseren Scheiben. */
    private static final int STAGE_STRUCTURE_BOLTED = 4;

    private AABB renderBox;

    public ICFStructBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.ICF_STRUCT.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;
        if(this.level.getGameTime() % CHECK_INTERVAL != 0) return;

        Direction dir = this.getBlockState().getValue(HorizontalDirectionalBlock.FACING);

        for(int i = -8; i <= 8; i++) {

            /* Die Achse und der Boden darunter. */
            if(!this.check(STAGE_RAW, 1, 0, i, dir)) return;
            if(i != 0 && !this.check(STAGE_RAW, 0, 0, i, dir)) return;
            if(!this.check(STAGE_RAW, -1, 0, i, dir)) return;
            if(!this.check(STAGE_RAW, 0, 3, i, dir)) return;

            /* Die Scheibe: innen verschweisst, ab drei Bloecken Abstand verschraubt. */
            int stage = Math.abs(i) <= 2 ? STAGE_VESSEL_WELDED : STAGE_STRUCTURE_BOLTED;

            for(int j = -1; j <= 1; j++) if(!this.check(stage, j, 1, i, dir)) return;
            for(int j = -2; j <= 2; j++) if(!this.check(stage, j, 2, i, dir)) return;
            for(int j = -2; j <= 2; j++) if(j != 0 && !this.check(stage, j, 3, i, dir)) return;
            for(int j = -2; j <= 2; j++) if(!this.check(stage, j, 4, i, dir)) return;
            for(int j = -1; j <= 1; j++) if(!this.check(stage, j, 5, i, dir)) return;
        }

        this.assemble(dir);
    }

    private void assemble(Direction dir) {

        MachineICFBlock icf = (MachineICFBlock) NtmBlocks.ICF.get();

        DummyableBlock.safeRem = true;
        this.level.setBlock(this.worldPosition, icf.createCoreState(dir), 3);
        icf.assemble(this.level, this.worldPosition.relative(dir), dir);
        DummyableBlock.safeRem = false;
    }

    /**
     * Prueft ein Bauteil an einer Stelle, die relativ zur Blickrichtung angegeben ist:
     * widthwise laeuft nach vorn, lengthwise zur Seite.
     */
    private boolean check(int stage, int widthwiseOffset, int y, int lengthwiseOffset, Direction dir) {

        Direction rot = dir.getClockWise();

        BlockPos pos = this.worldPosition.offset(
                rot.getStepX() * lengthwiseOffset + dir.getStepX() * widthwiseOffset,
                y,
                rot.getStepZ() * lengthwiseOffset + dir.getStepZ() * widthwiseOffset);

        BlockState state = this.level.getBlockState(pos);

        return state.is(NtmBlocks.ICF_COMPONENT.get()) && state.getValue(ToolConversionBlock.STAGE) == stage;
    }

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 9, y, z - 9, x + 10, y + 7, z + 10);
        }
        return this.renderBox;
    }
}
