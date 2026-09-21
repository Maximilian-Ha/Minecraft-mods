package com.hbm.items.weapon;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.grenade.DynamiteFishing;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.ItemGrenadeFishing.
 *
 * Wie die Dynamitstange zu werfen, und im Original hat sie denselben Zuender von drei
 * Sekunden. Was beim Knall geschieht, steht in der Entitaet.
 */
public class DynamiteFishingItem extends Item {

    public DynamiteFishingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if(!level.isClientSide) {
            DynamiteFishing stange = new DynamiteFishing(NtmEntityTypes.DYNAMITE_FISHING.get(), level);
            stange.werfen(player);
            level.addFreshEntity(stange);
        }

        if(!player.isCreative()) stack.shrink(1);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
