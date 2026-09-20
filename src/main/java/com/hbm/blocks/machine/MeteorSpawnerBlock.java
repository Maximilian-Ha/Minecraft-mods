package com.hbm.blocks.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.MeteorSpawnerBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.blocks.machine.BlockCybercrab.
 *
 * DER NAME TAEUSCHT IN BEIDE RICHTUNGEN: die Klasse heisst im Original nach der Krabbe,
 * registriert ist sie aber als meteor_spawner -- und mit einem Meteoriteneinschlag hat sie
 * nichts zu tun. Sie ist das Nest in den Sternenmetall-Ruinen, aus dem die Krabben kommen.
 *
 * ER LAESST NICHTS FALLEN: das Original gibt in getItemDropped null zurueck. Hier ist das
 * noLootTable an den Eigenschaften.
 *
 * WER IHN AUFSTELLT: im Original der CrabSpawners-Waehler des Meteoritenverlieses -- jede
 * so bezeichnete Stelle wird zu vier Fuenfteln Meteoritenziegel und zu einem Fuenftel
 * Nest. Dieses Verlies baut der Port noch nicht; bis dahin steht der Block wie der
 * dungeon_spawner aus Runde 234 nur zum Setzen bereit.
 */
public class MeteorSpawnerBlock extends BaseEntityBlock {

    public static final MapCodec<MeteorSpawnerBlock> CODEC = simpleCodec(MeteorSpawnerBlock::new);

    public MeteorSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<MeteorSpawnerBlock> codec() { return CODEC; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MeteorSpawnerBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide) return null;
        return createTickerHelper(type, NtmBlockEntityTypes.METEOR_SPAWNER.get(), MeteorSpawnerBlockEntity::serverTick);
    }
}
