package com.hbm.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: api.hbm.item.IDepthRockTool.
 *
 * Werkzeug, das Tiefengestein brechen kann. Das Original fragt zusaetzlich nach der Welt und der
 * Stelle, damit ein Werkzeug je nach Lage entscheiden kann -- das bleibt hier so.
 */
public interface IDepthRockTool {

    boolean canBreakRock(BlockGetter level, Player player, ItemStack stack, BlockState state, BlockPos pos);
}
