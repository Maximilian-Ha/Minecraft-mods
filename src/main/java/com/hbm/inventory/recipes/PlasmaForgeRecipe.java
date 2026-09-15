package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.PlasmaForgeRecipe.
 *
 * Das Rezept der Plasmaschmiede hat eine Zutat mehr als alle anderen: die Zuendtemperatur. Sie
 * ist kein Verbrauch, sondern eine Schwelle -- der Fusionsreaktor muss dem Ofen mindestens so
 * viel Plasmaleistung je Tick liefern, sonst laeuft er nicht an. Verbraucht wird die Leistung
 * nicht: sie wird nur durchgereicht.
 */
public class PlasmaForgeRecipe extends GenericRecipe {

    /** Mindestens so viel Plasmaleistung je Tick, sonst zuendet der Ofen nicht. */
    public long ignitionTemp;

    public PlasmaForgeRecipe(String name) {
        super(name);
    }

    public PlasmaForgeRecipe setInputEnergy(long ignitionTemp) {
        this.ignitionTemp = ignitionTemp;
        return this;
    }

    @Override
    public List<Component> print() {
        List<Component> list = new java.util.ArrayList<>();
        list.add(this.getName().withStyle(ChatFormatting.YELLOW));

        this.autoSwitch(list);
        this.duration(list);
        this.power(list);
        list.add(Component.literal(I18nUtil.resolveKey("container.recipe.plasma_in") + ": "
                + BobMathUtil.getShortNumber(this.ignitionTemp) + "TU/t").withStyle(ChatFormatting.LIGHT_PURPLE));
        this.input(list);
        this.output(list);

        return list;
    }
}
