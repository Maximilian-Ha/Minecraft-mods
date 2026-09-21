package com.hbm.blocks.generic;

import com.hbm.blockentity.GlyphidSpawnerBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import com.mojang.serialization.MapCodec;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockGlyphidSpawner.
 *
 * Das Gelege. Es sieht aus wie der Bau, aber es bringt alle zwei Minuten einen Schwarm
 * hervor -- und wie gross der ist, haengt vom RUSS in der Luft. Wer die Gegend zurussst,
 * bekommt groessere Schwaerme und gefaehrlichere Arten.
 *
 * DREI UNTERARTEN wie beim Baublock. Der Metawert des Originals entscheidet ueber die
 * Unterart dessen, was schluepft; hier tut das der Block selbst.
 *
 * ER FAELLT ALS EI. Das Original gibt in getItemDropped egg_glyphid zurueck und in
 * quantityDropped ein bis drei Stueck plus Gluecksstufe -- das steht hier in der Beutetafel.
 */
public class GlyphidSpawnerBlock extends BaseEntityBlock {

    public static final MapCodec<GlyphidSpawnerBlock> CODEC = simpleCodec(properties -> new GlyphidSpawnerBlock(properties, 0));

    /** Nur damit der Blockzustand nicht leer ist -- der Brutkasten merkt sich hier nichts. */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private final int unterart;

    public GlyphidSpawnerBlock(Properties properties, int unterart) {
        super(properties);
        this.unterart = unterart;
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    public int subtype() {
        return this.unterart;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GlyphidSpawnerBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide) return null;
        return createTickerHelper(type, NtmBlockEntityTypes.GLYPHID_SPAWNER.get(),
                (l, pos, st, be) -> be.serverTick(l, pos, st));
    }

    @Override
    protected net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }
}
