package com.hbm.items.weapon;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.grenade.Dynamite;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.ItemGrenadeDynamite.
 *
 * Die Dynamitstange -- werfen, drei Sekunden warten, Knall. Sie ist die letzte Zutat, die
 * dem Sockelsystem gefehlt hat: ohne sie kein Rezept fuer den Daybreaker.
 *
 * Was sie tut, steht in der Entitaet; hier steht nur, dass sie fliegt.
 */
public class DynamiteItem extends Item {

    public DynamiteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if(!level.isClientSide) {
            Dynamite stange = new Dynamite(NtmEntityTypes.DYNAMITE.get(), level);
            stange.werfen(player);
            level.addFreshEntity(stange);
        }

        if(!player.isCreative()) stack.shrink(1);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
