package com.hbm.blocks.generic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockSlag -- der feste Schlackeblock
 * (block_slag).
 *
 * ACHTUNG, NAMENSFALLE: SlagBlock heisst im Port schon etwas anderes, naemlich die
 * Schlackepfuetze unter dem Abstich (slag, mit Blockentitaet). Diese Klasse hier ist der
 * feste Block, den man in der Hand halten kann. Im Original heissen beide verschieden
 * genug; im Port waere SlagBlock zweimal vergeben.
 *
 * ER HAT ZWEI ANSICHTEN: glatt und gesprungen. Im Original sind das die Metadaten 0 und 1
 * desselben Blocks -- und weil dort kein ItemBlockMulti angemeldet ist, setzt der
 * Gegenstand immer die glatte. Die gesprungene entsteht nur, wo etwas gross genug
 * einschlaegt: der Moerserwerfer der Waffenfabrik und der Knall des fluechtigen Creepers.
 *
 * DARUM EIN ZUSTAND UND KEIN ZWEITER BLOCK: ein zweiter Block haette einen zweiten
 * Registriernamen, einen zweiten Gegenstand und einen Platz im Kreativreiter -- alles drei
 * hat das Original nicht.
 */
public class SolidSlagBlock extends Block {

    public static final BooleanProperty BROKEN = BooleanProperty.create("broken");

    public SolidSlagBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BROKEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BROKEN);
    }
}
