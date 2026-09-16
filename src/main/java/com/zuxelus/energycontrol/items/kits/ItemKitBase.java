package com.zuxelus.energycontrol.items.kits;

import com.zuxelus.energycontrol.api.IItemKit;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.kits.ItemKitBase.
 *
 * Ein Bausatz wird auf einen Block angewandt und wird dabei zur passenden Sensorkarte,
 * eingemessen auf diesen Block. Im Original hatte jeder Bausatz eine eigene Klasse, die
 * sich nur in zwei Zeilen unterschied; hier bekommt der Bausatz seine Karte und seine
 * Eignungspruefung bei der Anmeldung mit.
 */
public class ItemKitBase extends Item implements IItemKit {

    private final Supplier<? extends Item> card;
    private final BiPredicate<Level, BlockPos> suitable;

    public ItemKitBase(Properties properties, Supplier<? extends Item> card, BiPredicate<Level, BlockPos> suitable) {
        super(properties);
        this.card = card;
        this.suitable = suitable;
    }

    @Override
    public ItemStack getSensorCard(ItemStack stack, Player player, Level level, BlockPos pos, Direction side) {
        if(!suitable.test(level, pos)) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(card.get());
        new ItemCardReader(result).setTarget(pos);
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if(player == null) return InteractionResult.PASS;
        // Auf dem Client wird nur bestaetigt; eingemessen wird auf dem Server.
        if(level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack stack = context.getItemInHand();
        ItemStack result = getSensorCard(stack, player, level, context.getClickedPos(), context.getClickedFace());
        if(result.isEmpty()) return InteractionResult.PASS;

        stack.shrink(1);
        if(!player.getInventory().add(result)) player.drop(result, false);

        level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.4F, 1.0F);
        return InteractionResult.CONSUME;
    }
}
