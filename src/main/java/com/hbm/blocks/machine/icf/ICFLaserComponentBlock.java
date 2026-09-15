package com.hbm.blocks.machine.icf;

import com.hbm.blocks.EnumMultiBlock;
import com.hbm.inventory.MetaHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.BlockICFLaserComponent.
 *
 * Die sechs Bauteile des ICF-Lasers. Sie tun einzeln nichts -- erst die Steuerung liest sie ein
 * und macht daraus eine Anlage:
 *
 * - CASING und PORT bilden die Huelle. Der Port ist die Stelle, an der Strom hereinkommt.
 * - CELL ist das Laserrohr selbst. Nur eine ununterbrochene Reihe vor der Steuerung zaehlt.
 * - EMITTER muss an eine Zelle grenzen, CAPACITOR an einen Emitter, TURBO an einen Kondensator.
 *
 * Aus dieser Kette folgt die Leistung: Wurzel aus der Zahl der Kondensatoren mal deren Leistung,
 * plus Wurzel aus der Zahl der Turbolader (hoechstens so viele wie Kondensatoren) mal deren
 * Leistung. Die Wurzel sorgt dafuer, dass sich die Anlage nicht beliebig hochbauen laesst.
 */
public class ICFLaserComponentBlock extends EnumMultiBlock {

    public enum EnumICFPart {
        CASING,
        PORT,
        CELL,
        EMITTER,
        CAPACITOR,
        TURBO
    }

    public static final IntegerProperty SUBTYPE = IntegerProperty.create("subtype", 0, EnumICFPart.values().length - 1);

    public ICFLaserComponentBlock(Properties properties) {
        super(properties, EnumICFPart.class, true, true);
        this.registerDefaultState(this.stateDefinition.any().setValue(SUBTYPE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SUBTYPE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(SUBTYPE, this.rectify(MetaHelper.getMeta(context.getItemInHand())));
    }

    @Override public int getMeta(BlockState state) { return state.getValue(SUBTYPE); }

    public static EnumICFPart getPart(BlockState state) {
        return EnumICFPart.values()[state.getValue(SUBTYPE)];
    }

    public static final MapCodec<ICFLaserComponentBlock> CODEC = simpleCodec(ICFLaserComponentBlock::new);
    @Override protected MapCodec<ICFLaserComponentBlock> codec() { return CODEC; }
}
