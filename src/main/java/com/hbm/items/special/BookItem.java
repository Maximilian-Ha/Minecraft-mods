package com.hbm.items.special;

import com.hbm.inventory.menus.BookMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.items.special.ItemBook.
 *
 * Das Buch oeffnet beim Rechtsklick seine eigene Werkbank mit vier Plaetzen. Was darauf geht,
 * steht in MagicRecipes.
 */
public class BookItem extends Item {

    public BookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if(!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new BookMenu(id, inventory),
                    Component.translatable("container.book_of_")));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
