package com.hbm.items.special;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.IMetaItem;
import com.hbm.items.component.NtmDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.special.ItemNuclearWaste, ItemWasteShort und ItemWasteLong
 * in einer Klasse.
 *
 * Abfall aus der Wiederaufbereitung. Er traegt eine Abfallklasse -- welches Nuklid ihn ausmacht --,
 * und die entscheidet spaeter, was bei der Trennung herauskommt. Im Radiothermalgenerator zaehlt
 * sie nicht: dort ist jeder Abfall gleich viel wert.
 *
 * ER VERSCHWINDET NICHT. Liegt er auf dem Boden, bleibt er dort -- fuenf Minuten reichen nicht,
 * um ein Fass Atommuell loszuwerden. Das ist Absicht des Originals und hier uebernommen.
 *
 * ABWEICHUNG: das Original ersetzt den fallengelassenen Gegenstand durch eine eigene Entitaet,
 * die leuchtet und ihre Umgebung verstrahlt. Die gibt es im Port nicht; geblieben ist, dass der
 * Abfall nicht verfaellt.
 *
 * ABWEICHUNG: eine Textur je Gegenstand, nicht je Klasse -- so steht es auch im Original. Die
 * Klasse steht nur im Hinweistext.
 */
public class NuclearWasteItem extends Item implements IMetaItem {

    /** Wieviele Klassen dieser Gegenstand kennt; alles darueber wird umgebrochen. */
    private final WasteClass[] classes;

    public NuclearWasteItem(Properties properties, WasteClass[] classes) {
        super(properties.component(NtmDataComponents.META.get(), 0));
        this.classes = classes;
    }

    public WasteClass[] getClasses() {
        return this.classes;
    }

    public WasteClass getWasteClass(ItemStack stack) {
        return this.classes[rectify(MetaHelper.getMeta(stack), this.classes.length)];
    }

    public static int rectify(int meta, int length) {
        return Math.abs(meta) % length;
    }

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {
        for(int i = 0; i < this.classes.length; i++) stacks.add(MetaHelper.newStack(item, 1, i));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal(this.getWasteClass(stack).label).withStyle(ChatFormatting.ITALIC));
    }

    /** Atommuell verfaellt nicht, egal wie lange er herumliegt. */
    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return Integer.MAX_VALUE;
    }

    /**
     * Die Abfallklassen. Der Port fuehrt beide Listen des Originals in EINER Aufzaehlung, weil
     * sie sich ueberschneiden -- kurzlebig kennt acht davon, langlebig fuenf, und welche das
     * sind, steht in SHORT und LONG.
     *
     * Die beiden Zahlen sind die Anteile, die bei der Trennung als Fluessigkeit und als Gas
     * anfallen. Der Port fuehrt sie mit, obwohl er die Trennung noch nicht hat -- sie sind Teil
     * der Klasse, nicht der Maschine.
     */
    public enum WasteClass {

        URANIUM235("Uranium-235", 0, 100, 0, 0),
        URANIUM233("Uranium-233", 50, 100, 0, 50),
        NEPTUNIUM("Neptunium-237", 150, 500, 0, 100),
        PLUTONIUM239("Plutonium-239", 250, 1000, 0, 0),
        PLUTONIUM240("Plutonium-240", 350, 1000, 0, 0),
        PLUTONIUM241("Plutonium-241", 500, 1000, 0, 0),
        AMERICIUM242("Americium-242", 750, 1000, 0, 0),
        THORIUM("Thorium-232", 0, 0, 0, 0),
        SCHRABIDIUM("Schrabidium-326", 1000, 1000, 0, 250);

        public final String label;
        /** Anteile der kurzlebigen Trennung. */
        public final int shortLiquid;
        public final int shortGas;
        /** Anteile der langlebigen Trennung. */
        public final int longLiquid;
        public final int longGas;

        WasteClass(String label, int shortLiquid, int shortGas, int longLiquid, int longGas) {
            this.label = label;
            this.shortLiquid = shortLiquid;
            this.shortGas = shortGas;
            this.longLiquid = longLiquid;
            this.longGas = longGas;
        }

        /** Die acht Klassen des kurzlebigen Abfalls, in der Reihenfolge des Originals. */
        public static final WasteClass[] SHORT = {
                URANIUM235, URANIUM233, NEPTUNIUM, PLUTONIUM239,
                PLUTONIUM240, PLUTONIUM241, AMERICIUM242, SCHRABIDIUM
        };

        /** Die fuenf Klassen des langlebigen Abfalls, in der Reihenfolge des Originals. */
        public static final WasteClass[] LONG = {
                URANIUM235, URANIUM233, NEPTUNIUM, THORIUM, SCHRABIDIUM
        };
    }
}
