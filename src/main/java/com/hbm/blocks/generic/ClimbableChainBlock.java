package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockChain.
 *
 * Die Kette der Verliese. Anders als die Kette von Minecraft laesst sie sich erklettern -- im
 * Original ist das ihr ganzer Zweck: sie haengt in Schaechten, durch die man hinauf und hinunter
 * kommen soll.
 *
 * ABWEICHUNG: das Original zeichnet sie mit einem eigenen Renderer und laesst sie in alle sechs
 * Richtungen haengen. Hier ist es die Kette von Minecraft mit ihren drei Achsen; die
 * Umsetzungstabelle bildet die sechs Metadaten-Werte des Originals auf die drei Achsen ab.
 */
public class ClimbableChainBlock extends ChainBlock {

    public ClimbableChainBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isLadder(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos, LivingEntity entity) {
        return true;
    }
}
