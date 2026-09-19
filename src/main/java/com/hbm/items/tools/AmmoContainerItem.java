package com.hbm.items.tools;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.EnumMultiItem;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.InventoryUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemAmmoContainer.
 *
 * Der Munitionsbehaelter. Er sieht nach, welche Waffen der Spieler bei sich traegt, sucht sich
 * bis zu drei davon aus und legt zu jeder deren Standardmunition ins Inventar -- was nicht
 * hineinpasst, faellt vor die Fuesse.
 *
 * ZWEI SORTEN: der gewoehnliche Behaelter bedient jede Waffe; der behelfsmaessige gibt nur die
 * Haelfte her und laesst die Waffen aus, deren Munition als teuer gilt.
 *
 * WELCHE DREI es werden, entscheidet der Zufall: die Liste wird gemischt und vorne
 * abgeschnitten. Wer nur drei Waffen dabei hat, bekommt also fuer alle drei etwas.
 */
public class AmmoContainerItem extends EnumMultiItem {

    /** Wie viele Waffen ein Behaelter hoechstens versorgt. */
    private static final int WAFFEN = 3;

    public AmmoContainerItem(Properties properties) {
        super(properties.stacksTo(1), AmmoContainerType.class, true, true);
    }

    public enum AmmoContainerType {
        STANDARD,
        /** Behelfsmaessig: halbe Menge, und nichts Teures. */
        MAKESHIFT
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        boolean behelf = MetaHelper.getMeta(stack) == AmmoContainerType.MAKESHIFT.ordinal();

        Inventory inventar = player.getInventory();
        List<GunBaseNTItem> waffen = new ArrayList<>();

        for(ItemStack imGepaeck : inventar.items) {
            if(!(imGepaeck.getItem() instanceof GunBaseNTItem waffe)) continue;
            if(waffe.defaultAmmo == null) continue;
            if(behelf && waffe.isDefaultExpensive) continue;
            waffen.add(waffe);
        }

        if(waffen.isEmpty()) return InteractionResultHolder.pass(stack);
        if(level.isClientSide) return InteractionResultHolder.success(stack);

        Collections.shuffle(waffen);

        for(int i = 0; i < WAFFEN && i < waffen.size(); i++) {

            ItemStack munition = waffen.get(i).getDefaultAmmo();
            if(munition.isEmpty()) continue;

            if(behelf) munition.setCount((int) Math.ceil(munition.getCount() / 2D));

            /* Nur die Haupttaschen, nicht Ruestung oder Zweithand -- deshalb bis items.size()-1. */
            ItemStack rest = InventoryUtil.tryAddItemToInventory(inventar.items, 0, inventar.items.size() - 1, munition);
            if(!rest.isEmpty()) player.drop(rest, false);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), NtmSoundEvents.UNPACK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        stack.shrink(1);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        String art = (MetaHelper.getMeta(stack) == AmmoContainerType.MAKESHIFT.ordinal()
                ? AmmoContainerType.MAKESHIFT : AmmoContainerType.STANDARD).name().toLowerCase(Locale.US);
        components.add(Component.translatable("desc.item.ammo_container." + art).withStyle(ChatFormatting.YELLOW));
    }
}
