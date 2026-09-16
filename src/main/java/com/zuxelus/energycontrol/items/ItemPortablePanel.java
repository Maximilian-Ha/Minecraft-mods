package com.zuxelus.energycontrol.items;

import com.zuxelus.energycontrol.menus.PortablePanelMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.ItemPortablePanel.
 *
 * Eine Informationstafel in der Hand: ein Kartenfach, ein Fach fuer Reichweitenaufwertungen,
 * und die Zeilen der Karte darueber. Gemessen wird von dort, wo der Spieler steht.
 *
 * Unterschied zur Tafel an der Wand: welche Zeilen erscheinen, laesst sich hier nicht
 * ankreuzen -- die Karte zeigt, was sie von Haus aus zeigt. Dafuer braucht die tragbare
 * Tafel keinen Strom, wie im Original.
 */
public class ItemPortablePanel extends Item {

    public static final int SLOT_CARD = 0;
    public static final int SLOT_UPGRADE_RANGE = 1;
    public static final int SIZE = 2;

    public ItemPortablePanel(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if(!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                            (id, inventory, who) -> new PortablePanelMenu(id, inventory, hand),
                            Component.translatable("container.energycontrol.portable_panel")),
                    buffer -> buffer.writeEnum(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
