package com.hbm.blocks.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.ProxyComboBlockEntity;
import com.hbm.blockentity.machine.MachinePumpElectricBlockEntity;
import com.hbm.blockentity.machine.MachinePumpSteamBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.hbm.blocks.DummyableBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.MachinePump.
 *
 * Die Wasserpumpe ist drei mal drei Bloecke gross und vier hoch. Angeschlossen wird an den vier
 * Mittelbloecken der Aussenkanten; sie tragen deshalb einen eigenen Anschlussblock statt eines
 * blossen Beiblocks.
 *
 * DERSELBE BLOCK TRAEGT BEIDE PUMPEN. Welche daraus wird, sagt der uebergebene Bauplan -- so
 * haelt es auch das Original, das die Entscheidung am Blocknamen festmacht.
 */
public class MachinePumpBlock extends DummyableBlock {

    /* Wie bei den uebrigen Bloecken des Ports mit Bauplan-Argument: der Codec nennt eine feste
     * Spielart. Er wird nur gebraucht, wenn ein Block AUS Daten entsteht, und das tut hier
     * keiner -- beide sind fest registriert. */
    public static final MapCodec<MachinePumpBlock> CODEC = simpleCodec(properties -> new MachinePumpBlock(properties, false));

    /** Wahr: Dampfpumpe. Falsch: elektrische Pumpe. */
    public final boolean steam;

    public MachinePumpBlock(Properties properties, boolean steam) {
        super(properties);
        this.steam = steam;
    }

    @Override public MapCodec<MachinePumpBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {

        if(state.getValue(TYPE) == DummyBlockType.CORE) {
            return this.steam ? new MachinePumpSteamBlockEntity(pos, state) : new MachinePumpElectricBlockEntity(pos, state);
        }

        ProxyComboBlockEntity proxy = new ProxyComboBlockEntity(pos, state).fluid();
        return this.steam ? proxy : proxy.power();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return (lvl, pos, st, be) -> { if(be instanceof ITickable tickable) tickable.updateEntity(); };
    }

    @Override public int[] getDimensions() { return new int[] { 3, 0, 1, 1, 1, 1 }; }
    @Override public int getOffset() { return 1; }
}
