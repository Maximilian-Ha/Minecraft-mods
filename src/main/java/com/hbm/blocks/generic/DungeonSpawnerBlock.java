package com.hbm.blocks.generic;

import com.hbm.blockentity.DungeonSpawnerBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.DungeonSpawner.
 *
 * Der Block, der den Aufrufer traegt. Er sieht aus wie Stein und tut selbst nichts -- alles
 * steht in der Blockentitaet.
 *
 * WARUM ER SO SPAET KOMMT: Runde 227 hat ihn als einen von sechs echt fehlenden
 * Bauwerksbloecken ausgewiesen, mit der Begruendung "braucht EntityUndeadSoldier". Der Weg
 * dahin war laenger als die Begruendung ahnen liess -- Soldat (228), Geheimstuecke (230),
 * Sockelsystem (231), dessen Zutaten (232), Rotes Zimmer (233) und Tontafel (234). Erst mit
 * der Tafel hat seine Belohnung ueberhaupt etwas herzugeben.
 *
 * ER STEHT IN KEINEM REITER und hat kein Rezept: die Weltgenerierung setzt ihn, wie im
 * Original (setCreativeTab wird dort nicht gerufen).
 */
public class DungeonSpawnerBlock extends BaseEntityBlock {

    public static final MapCodec<DungeonSpawnerBlock> CODEC = simpleCodec(DungeonSpawnerBlock::new);

    public DungeonSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<DungeonSpawnerBlock> codec() { return CODEC; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonSpawnerBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide) return null;
        return createTickerHelper(type, NtmBlockEntityTypes.DUNGEON_SPAWNER.get(), DungeonSpawnerBlockEntity::serverTick);
    }
}
