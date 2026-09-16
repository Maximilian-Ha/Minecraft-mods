package com.hbm.items.armor;

import api.hbm.item.IGasMask;
import com.hbm.handler.ArmorModHandler;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.ArmorUtil;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: ItemFilter.
 *
 * Ein Filtereinsatz. Rechtsklick schraubt ihn in das getragene Kopfteil -- entweder
 * direkt, oder in eine Maske, die als Aufsatz in einem Helm steckt. Ein schon
 * eingesetzter Filter kommt dabei zurueck in die Hand.
 */
public class FilterItem extends Item {

    public FilterItem(Properties properties) {
        super(properties.stacksTo(1).durability(20000));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack filter = player.getItemInHand(hand);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        if(helmet.isEmpty()) return InteractionResultHolder.pass(filter);

        /* Steckt im Helm kein Filtergewinde, kann immer noch eine Maske als Aufsatz
         * darin sitzen. Die wird dann bestueckt und danach wieder eingesetzt. */
        if(!(helmet.getItem() instanceof IGasMask) && ArmorModHandler.hasMods(helmet)) {

            ItemStack mask = ArmorModHandler.pryMods(level, helmet)[ArmorModHandler.HELMET_ONLY];

            if(!mask.isEmpty()) {
                InteractionResultHolder<ItemStack> result = installOn(mask, filter, level, player);
                ArmorModHandler.applyMod(level, helmet, mask);
                return result;
            }
        }

        return installOn(helmet, filter, level, player);
    }

    private InteractionResultHolder<ItemStack> installOn(ItemStack helmet, ItemStack filter, Level level, Player player) {

        if(!(helmet.getItem() instanceof IGasMask mask)) return InteractionResultHolder.pass(filter);
        if(!mask.isFilterApplicable(helmet, player, filter)) return InteractionResultHolder.pass(filter);

        if(!level.isClientSide) {

            ItemStack current = ArmorUtil.getGasMaskFilter(level, helmet);

            ArmorUtil.installGasMaskFilter(level, helmet, filter.copy());

            /* Der neue Filter ist eingebaut, in der Hand bleibt der alte -- oder nichts. */
            filter.shrink(1);
            if(!current.isEmpty() && !player.getInventory().add(current)) player.drop(current, false);

            level.playSound(null, player, NtmSoundEvents.FILTER_SCREW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResultHolder.sidedSuccess(filter, level.isClientSide);
    }
}
