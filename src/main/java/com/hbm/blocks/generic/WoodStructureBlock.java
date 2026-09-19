package com.hbm.blocks.generic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockWoodStructure.
 *
 * Drei Holzteile fuer Bauwerke: ein Dachbrett unten im Block, ein Geruestpfosten und eine
 * Deckenplatte oben. Das Original haelt sie als drei Metadaten eines Blocks; in 1.21 sind es
 * drei Bloecke, wie bei allen Metadatenfamilien des Ports.
 *
 * Nur das Geruest traegt oben auf -- im Original ueber isSideSolid nur fuer SCAFFOLD und UP.
 */
public class WoodStructureBlock extends Block {

    public static final MapCodec<WoodStructureBlock> CODEC = simpleCodec(properties -> new WoodStructureBlock(properties, Type.ROOF));

    public enum Type {
        /** Das Dachbrett liegt am Boden des Blocks, drei Pixel hoch. */
        ROOF(Block.box(0, 0, 0, 16, 3, 16)),
        /** Der Geruestpfosten steht ueber die ganze Hoehe, einen Pixel von jeder Seite weg. */
        SCAFFOLD(Block.box(1, 0, 1, 15, 16, 15)),
        /** Die Deckenplatte haengt oben, zwei Pixel dick. */
        CEILING(Block.box(0, 14, 0, 16, 16, 16));

        public final VoxelShape shape;

        Type(VoxelShape shape) {
            this.shape = shape;
        }
    }

    private final Type type;

    public WoodStructureBlock(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    @Override public MapCodec<WoodStructureBlock> codec() { return CODEC; }

    public Type getType() { return this.type; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.type.shape;
    }
}
