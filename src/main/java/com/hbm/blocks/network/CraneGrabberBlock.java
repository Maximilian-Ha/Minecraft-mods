package com.hbm.blocks.network;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.CraneGrabberBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CraneGrabber.
 *
 * Der Greifer nimmt vom Band an seiner Eingangsseite und setzt auf das an seiner Ausgangsseite --
 * und waehlt dabei nach neun Mustern aus, was er ueberhaupt anfasst.
 *
 * ER LAESST BEIM ABBAU NICHTS FALLEN. Seine neun Faecher halten nur Abbilder, keine
 * Gegenstaende; sie fallen zu lassen hiesse, sie aus dem Nichts zu erzeugen. Das Original
 * laesst genau aus diesem Grund nur die Faecher neun bis elf fallen -- die Aufwertungen --, und
 * die gibt es hier nicht.
 */
public class CraneGrabberBlock extends CraneBaseBlock {

    public static final MapCodec<CraneGrabberBlock> CODEC = simpleCodec(CraneGrabberBlock::new);

    public CraneGrabberBlock(Properties properties) {
        super(properties);
    }

    @Override protected MapCodec<? extends CraneBaseBlock> codec() { return CODEC; }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraneGrabberBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, NtmBlockEntityTypes.CRANE_GRABBER.get(), (l, p, s, be) -> be.updateEntity());
    }
}
