package com.zuxelus.energycontrol.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.api.IItemKit.
 *
 * Ein Bausatz wird auf einen Block angewandt und wird dabei zur passenden, auf diesen
 * Block eingemessenen Karte. Passt der Block nicht, gibt die Umsetzung
 * {@link ItemStack#EMPTY} zurueck und der Bausatz bleibt, was er war.
 */
public interface IItemKit {

    ItemStack getSensorCard(ItemStack stack, Player player, Level level, BlockPos pos, Direction side);
}
