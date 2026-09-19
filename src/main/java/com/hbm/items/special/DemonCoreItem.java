package com.hbm.items.special;

import com.hbm.items.NtmItems;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.special.ItemDemonCore.
 *
 * Der offene Daemonenkern. Offen gehalten wird er von einem Schraubenzieher -- am Amboss
 * wird er so zusammengesetzt. Faellt er zu Boden, rutscht der Schraubenzieher heraus: der
 * Kern schliesst sich, und das Werkzeug liegt daneben.
 *
 * Das ist kein Scherz des Mods, sondern der Unfall von 1946 nachgebaut: Louis Slotin hielt
 * die beiden Berylliumhalbschalen mit einem Schraubenzieher auseinander, der abrutschte.
 */
public class DemonCoreItem extends Item {

    public DemonCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {

        Level level = entity.level();

        if(level.isClientSide || !entity.onGround()) return false;

        entity.setItem(new ItemStack(NtmItems.DEMON_CORE_CLOSED.get(), stack.getCount()));
        level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(),
                new ItemStack(NtmItems.SCREWDRIVER.get())));

        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, components, flag);
        components.add(Component.literal("[" + I18nUtil.resolveKey("trait.drop") + "]").withStyle(ChatFormatting.RED));
    }
}
