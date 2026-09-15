package com.hbm.blocks.generic;

import com.hbm.blocks.NtmBlocks;
import com.hbm.entity.item.FallingBlockEntityNT;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockUberConcrete.
 *
 * Hochleistungsbeton, der von selbst zerfaellt. Im Original steckt der Grad des Zerfalls in den
 * Blockmetadaten; auf 1.21 ist daraus die Blockstate-Eigenschaft "decay" geworden, 0 bis 15.
 *
 * Der Zerfall wird langsamer, je weiter er fortgeschritten ist: bei jedem Zufallstakt wird nur
 * mit Wahrscheinlichkeit 1/(decay+1) ueberhaupt etwas getan. Frischer Beton (0) altert also bei
 * jedem Takt, fast zerfallener (15) nur noch bei jedem sechzehnten.
 *
 * Am Ende bricht er zusammen. Ist unter ihm Luft, bleibt an seiner Stelle Bruchbeton stehen und
 * faellt herunter. Steht er dagegen auf festem Grund, sucht er sich eine der vier Seiten, wo
 * sowohl der Nachbar als auch dessen Untergrund frei sind, und wirft den Bruch dorthin -- so
 * rutscht eine zerfallende Wand seitlich weg statt in sich zusammen. Findet er keine solche
 * Seite, bleibt der Bruch an Ort und Stelle.
 */
public class UberConcreteBlock extends Block {

    /** 0 = frisch, 15 = kurz vor dem Zusammenbruch. Wie die Metadaten des Originals. */
    public static final IntegerProperty DECAY = IntegerProperty.create("decay", 0, 15);

    public static final MapCodec<UberConcreteBlock> CODEC = simpleCodec(UberConcreteBlock::new);

    public UberConcreteBlock(Properties properties) {
        super(properties.randomTicks());
        this.registerDefaultState(this.stateDefinition.any().setValue(DECAY, 0));
    }

    @Override public MapCodec<UberConcreteBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DECAY);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {

        int decay = state.getValue(DECAY);

        if(rand.nextInt(decay + 1) > 0) return;

        if(decay < 15) {
            level.setBlock(pos, state.setValue(DECAY, decay + 1), 3);
            return;
        }

        BlockState broken = NtmBlocks.CONCRETE_SUPER_BROKEN.get().defaultBlockState();

        if(level.getBlockState(pos.below()).isAir()) {
            level.setBlock(pos, broken, 3);
            return;
        }

        List<Direction> sides = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
        Collections.shuffle(sides);

        for(Direction dir : sides) {
            BlockPos side = pos.relative(dir);

            if(level.getBlockState(side).isAir() && level.getBlockState(side.below()).isAir()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                FallingBlockEntityNT debris = FallingBlockEntityNT.fall(level, side, broken);
                debris.time = 2;
                debris.dropItem = false;
                debris.setHurtsEntities(2.0F, 40);
                return;
            }
        }

        level.setBlock(pos, broken, 3);
    }
}
