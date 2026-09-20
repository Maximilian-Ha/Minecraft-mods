package com.hbm.items.food;

import com.hbm.extprop.HbmPlayerAttachments;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.EnumMultiItem;
import com.hbm.lib.ModAttachments;
import com.hbm.util.EnumUtil;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.items.food.ItemFlask.
 *
 * Die Infusionsflasche. Getrunken hebt sie die Obergrenze des Schilds um fuenf und fuellt
 * es gleich mit auf -- bis zur harten Grenze von hundert (HbmPlayerAttachments.shieldCap).
 *
 * NUR EINE SORTE, und das ist das Original: EnumInfusion kennt genau SHIELD. Die
 * CE-Abspaltung liefert drei Texturen (flask_infusion.empty, .shield, .radpot); die beiden
 * anderen gehoeren zu Sorten, die der 1.7.10-Stand nicht hat. Der Port meldet nur die eine
 * an, die es gibt, statt zwei leere Plaetze dazuzuerfinden.
 *
 * WER SCHON VOLL IST, TRINKT NICHT. Das Original prueft das in onItemRightClick und gibt
 * den Stapel unveraendert zurueck -- sonst verlaengert eine volle Flasche nichts und ist
 * doch weg.
 */
public class FlaskItem extends EnumMultiItem {

    /** Um wie viel eine Flasche Obergrenze und Fuellstand anhebt. */
    public static final float INFUSION = 5F;

    public enum InfusionType {
        SHIELD
    }

    public FlaskItem(Properties properties) {
        super(properties, InfusionType.class, true, true);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity trinker) {

        if(!(trinker instanceof Player player)) return stack;

        if(!player.hasInfiniteMaterials()) stack.shrink(1);

        if(level.isClientSide) return stack;

        if(EnumUtil.grabEnumSafely(InfusionType.class, MetaHelper.getMeta(stack)) == InfusionType.SHIELD) {

            HbmPlayerAttachments props = HbmPlayerAttachments.getData(player);

            props.maxShield = Math.min(HbmPlayerAttachments.shieldCap, props.maxShield + INFUSION);
            props.shield = Math.min(props.shield + INFUSION, props.getEffectiveMaxShield(player));

            player.setData(ModAttachments.PLAYER_ATTACHMENT.get(), props);
        }

        return stack;
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack inHand = player.getItemInHand(hand);

        if(EnumUtil.grabEnumSafely(InfusionType.class, MetaHelper.getMeta(inHand)) == InfusionType.SHIELD
                && HbmPlayerAttachments.getData(player).maxShield >= HbmPlayerAttachments.shieldCap) {
            return InteractionResultHolder.pass(inHand);
        }

        return ItemUtils.startUsingInstantly(level, player, hand);
    }
}
