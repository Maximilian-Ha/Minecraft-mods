package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.IItemCard;
import com.zuxelus.energycontrol.api.PanelString;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardBase.
 *
 * Gemeinsames aller Sensorkarten. Im Original war jede Kartenart ein Schadenswert
 * desselben Gegenstands; auf 1.21.1 gibt es keine Schadenswerte mehr als Unterscheidung,
 * jede Kartenart ist deshalb ein eigener Gegenstand.
 */
public abstract class ItemCardBase extends Item implements IItemCard {

    protected ItemCardBase(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean isCard(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IItemCard;
    }

    /** Die eingemessenen Koordinaten stehen in der Kurzinfo -- sonst sieht man der Karte nichts an. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ItemCardReader reader = new ItemCardReader(stack);
        BlockPos target = reader.getTarget();
        if(target != null) {
            tooltip.add(Component.translatable("item.ec.card.target", target.getX(), target.getY(), target.getZ())
                    .withStyle(ChatFormatting.GRAY));
        }
        String title = reader.getTitle();
        if(title != null && !title.isEmpty()) {
            tooltip.add(Component.literal(title).withStyle(ChatFormatting.AQUA));
        }
    }

    /** Die Koordinaten des n-ten Ziels einer Sammelkarte. */
    protected BlockPos getCoordinates(ICardReader reader, int cardNumber) {
        if(cardNumber >= reader.getCardCount()) return null;
        return new BlockPos(reader.getInt(String.format("_%dx", cardNumber)),
                reader.getInt(String.format("_%dy", cardNumber)),
                reader.getInt(String.format("_%dz", cardNumber)));
    }

    /**
     * Haengt "An"/"Aus" rechts an eine der ersten beiden Zeilen. Genau wie im Original:
     * der Zustand soll oben stehen, aber keine eigene Zeile kosten.
     */
    protected void addOnOff(List<PanelString> result, boolean value) {
        Component text = Component.translatable(value ? "msg.ec.InfoPanelOn" : "msg.ec.InfoPanelOff");
        int color = value ? 0x00FF00 : 0xFF0000;

        for(int i = 0; i < Math.min(2, result.size()); i++) {
            PanelString line = result.get(i);
            if(line.textCenter == null && line.textRight == null) {
                line.textRight = text;
                line.colorRight = color;
                return;
            }
        }

        PanelString line = new PanelString();
        line.textLeft = text;
        line.colorLeft = color;
        result.add(line);
    }

    /** Temperaturzeile, gruen bis rot je nach Abstand zur Schmelzgrenze. */
    protected void addHeat(List<PanelString> result, String key, double heat, double maxHeat, boolean showLabels) {
        PanelString line = PanelString.of(key, heat, showLabels);
        int rate = maxHeat <= 0 ? 0 : (int) (10 * heat / maxHeat);
        line.colorLeft = rate < 4 ? 0x00FF00 : rate < 8 ? 0xFFFF00 : 0xFF0000;
        result.add(line);
    }

    /** Pruefung der Reichweite, gemeinsam fuer alle Karten mit festem Ziel. */
    protected boolean inRange(BlockPos target, BlockPos panel, int range) {
        if(range < 0) return true;
        return target.distSqr(panel) <= (double) range * range;
    }
}
