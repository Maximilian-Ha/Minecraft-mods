package com.hbm.blocks.generic;

import com.hbm.blocks.states.BrickFace;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockRedBrick.
 *
 * Der Ziegel des Roten Zimmers. Er hat eine einzige Eigenschaft: WELCHE SEITE das Ziegelbild
 * traegt. Alle uebrigen Seiten zeigen den schlichten Untergrund, oben und unten ein eigenes
 * Deckbild. Im Original ist das der Metadatenwert und die Abfrage lautet "side == meta".
 *
 * SIEBEN WERTE, NICHT SECHS. Der Zimmererzeuger setzt fuer alle Kanten den Wert sechs, der
 * gar keiner Seite entspricht -- solche Ziegel zeigen ringsum den Untergrund. Eine blosse
 * Richtungseigenschaft koennte das nicht ausdruecken.
 *
 * ER GIBT SICH NICHT HER. Das Original ueberschreibt getItemDropped mit null; im Port ist
 * das noLootTable() an den Eigenschaften. Dazu ein Sprengwiderstand von zehntausend: das
 * Zimmer soll nicht aufgesprengt werden, sondern durch die Tuer betreten.
 */
public class RedBrickBlock extends Block {

    public static final EnumProperty<BrickFace> FACE = EnumProperty.create("face", BrickFace.class);

    public RedBrickBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACE, BrickFace.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE);
    }

    /**
     * Das Original nimmt BlockPistonBase.determineOrientation: die Seite, auf die der Spieler
     * blickt. getNearestLookingDirection liefert dieselbe Richtung, nur ohne den Umweg ueber
     * den Kolben.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACE, BrickFace.of(context.getNearestLookingDirection().getOpposite()));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction seite = state.getValue(FACE).direction();
        if(seite == null || seite.getAxis() == Direction.Axis.Y) return state;
        return state.setValue(FACE, BrickFace.of(rotation.rotate(seite)));
    }
}
