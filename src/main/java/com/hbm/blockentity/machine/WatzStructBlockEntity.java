package com.hbm.blockentity.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.ToolConversionBlock;
import com.hbm.blocks.machine.WatzBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityWatzStruct.
 *
 * Der Zusammenbauklotz. Er sieht jede Sekunde nach, ob um ihn herum ein vollstaendiges
 * Watz-Segment gemauert ist -- zwei Kuehlkoerper ueber sich, zwoelf Elemente und acht
 * Kuehlkoerper im Ring, und aussen herum die verschraubte Wand. Stimmt alles, ersetzt er sich
 * selbst durch den Reaktorkern und belegt den Raum.
 *
 * Die Pruefung ist eine lange Reihe von Einzelabfragen. Das Original nennt sie selbst
 * unelegant und laesst sie stehen, weil sie kurz und lesbar ist; hier steht sie ebenso.
 */
public class WatzStructBlockEntity extends BlockEntity implements ITickable {

    /** Nur jede Sekunde nachsehen -- die Pruefung liest ueber hundert Bloecke. */
    private static final int CHECK_INTERVAL = 20;

    private AABB renderBox;

    public WatzStructBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.WATZ_STRUCT.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;
        if(this.level.getGameTime() % CHECK_INTERVAL != 0) return;

        Block element = NtmBlocks.WATZ_ELEMENT.get();
        Block cooler = NtmBlocks.WATZ_COOLER.get();

        if(!this.check(cooler, 0, 1, 0)) return;
        if(!this.check(cooler, 0, 2, 0)) return;

        for(int i = 0; i < 3; i++) {

            if(!this.check(element, 1, i, 0)) return;
            if(!this.check(element, 2, i, 0)) return;
            if(!this.check(element, 0, i, 1)) return;
            if(!this.check(element, 0, i, 2)) return;
            if(!this.check(element, -1, i, 0)) return;
            if(!this.check(element, -2, i, 0)) return;
            if(!this.check(element, 0, i, -1)) return;
            if(!this.check(element, 0, i, -2)) return;
            if(!this.check(element, 1, i, 1)) return;
            if(!this.check(element, 1, i, -1)) return;
            if(!this.check(element, -1, i, 1)) return;
            if(!this.check(element, -1, i, -1)) return;
            if(!this.check(cooler, 2, i, 1)) return;
            if(!this.check(cooler, 2, i, -1)) return;
            if(!this.check(cooler, 1, i, 2)) return;
            if(!this.check(cooler, -1, i, 2)) return;
            if(!this.check(cooler, -2, i, 1)) return;
            if(!this.check(cooler, -2, i, -1)) return;
            if(!this.check(cooler, 1, i, -2)) return;
            if(!this.check(cooler, -1, i, -2)) return;

            for(int j = -1; j < 2; j++) {
                if(!this.checkBolted(3, i, j)) return;
                if(!this.checkBolted(j, i, 3)) return;
                if(!this.checkBolted(-3, i, j)) return;
                if(!this.checkBolted(j, i, -3)) return;
            }

            if(!this.checkBolted(2, i, 2)) return;
            if(!this.checkBolted(2, i, -2)) return;
            if(!this.checkBolted(-2, i, 2)) return;
            if(!this.checkBolted(-2, i, -2)) return;
        }

        this.assemble();
    }

    /** Setzt den Kern und belegt den Raum. Die Richtung ist beliebig -- das Segment ist rund. */
    private void assemble() {

        WatzBlock watz = (WatzBlock) NtmBlocks.WATZ.get();

        DummyableBlock.safeRem = true;
        this.level.setBlock(this.worldPosition, watz.createCoreState(Direction.NORTH), 3);
        watz.assemble(this.level, this.worldPosition, Direction.NORTH);
        DummyableBlock.safeRem = false;
    }

    private boolean check(Block block, int x, int y, int z) {
        return this.level.getBlockState(this.worldPosition.offset(x, y, z)).is(block);
    }

    /** Die Aussenwand zaehlt nur in der verschraubten Baustufe -- im Original Metadatenwert 1. */
    private boolean checkBolted(int x, int y, int z) {
        BlockState state = this.level.getBlockState(this.worldPosition.offset(x, y, z));
        return state.is(NtmBlocks.WATZ_END.get()) && state.getValue(ToolConversionBlock.STAGE) == 1;
    }

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 3, y, z - 3, x + 4, y + 3, z + 4);
        }
        return this.renderBox;
    }
}
