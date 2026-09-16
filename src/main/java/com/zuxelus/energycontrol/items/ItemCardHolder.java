package com.zuxelus.energycontrol.items;

import com.zuxelus.energycontrol.menus.CardHolderMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.ItemCardHolder.
 *
 * Eine Mappe fuer Sensorkarten: vierundfuenfzig Faecher, in denen nur Karten liegen
 * duerfen. Mehr tut sie nicht -- sie misst nichts und braucht keinen Strom.
 */
public class ItemCardHolder extends Item {

    public static final int SIZE = 54;

    public ItemCardHolder(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if(!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                            (id, inventory, who) -> new CardHolderMenu(id, inventory, hand),
                            Component.translatable("container.energycontrol.card_holder")),
                    buffer -> buffer.writeEnum(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
