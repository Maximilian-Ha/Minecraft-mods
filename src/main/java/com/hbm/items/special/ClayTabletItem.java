package com.hbm.items.special;

import com.hbm.blockentity.IScreenProvider;
import com.hbm.inventory.screens.ClayTabletScreen;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.main.NuclearTechMod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.items.special.ItemClayTablet.
 *
 * Die Tontafel. Sie zeigt EIN Sockelrezept -- aber nur zum Teil: bei der hellen Tafel bleibt
 * die Haelfte der neun Plaetze verdeckt, bei der dunklen drei Viertel. Man sieht, was dabei
 * herauskommt, und muss den Rest erraten oder eine zweite Tafel suchen.
 *
 * DER WURF STEHT IM STAPEL, nicht im Bildschirm. Beim ersten Rechtsklick wird er gewuerfelt
 * und bleibt dann; dieselbe Tafel zeigt immer dasselbe Rezept. Im Original ist das eine
 * NBT-Zahl namens tabletSeed, hier eine Datenkomponente -- und sie muss zum Client, denn
 * gezeichnet wird die Tafel dort.
 *
 * WELCHE REZEPTMENGE gezeigt wird, entscheidet der Metadatenwert: null ist die helle Tafel
 * und zeigt die erste Menge, eins die dunkle und zeigt die zweite. Genau darum fuehrt
 * PedestalRecipes zwei Mengen.
 */
public class ClayTabletItem extends Item implements IScreenProvider {

    public ClayTabletItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        /* Gewuerfelt wird auf dem Server -- sonst saehe jeder Zuschauer ein anderes Rezept,
         * und beim naechsten Oeffnen waere es wieder ein anderes. */
        if(!level.isClientSide && !stack.has(NtmDataComponents.TABLET_SEED.get())) {
            stack.set(NtmDataComponents.TABLET_SEED.get(), player.getRandom().nextLong());
        }

        /* Der Proxy entscheidet, ob es einen Bildschirm gibt: auf dem Server tut er nichts. */
        NuclearTechMod.proxy.openScreen(player, BlockPos.ZERO);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object provideScreen(Player player, BlockPos pos) {
        return new ClayTabletScreen(player.getMainHandItem());
    }
}
