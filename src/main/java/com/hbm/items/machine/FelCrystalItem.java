package com.hbm.items.machine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemFELCrystal.
 *
 * Ein Laserkristall. Er tut von sich aus nichts -- er sagt dem FEL nur, welche Wellenlaenge
 * dessen Strahl hat, und daran haengt, welche Rezepte die SILEX darunter fahren kann.
 *
 * FUENF KRISTALLE, FUENF WELLENLAENGEN: von Kohlendioxid in Desh (infrarot) bis zum
 * Digamma-Kristall, dessen Beschreibung das Original mit dem Verschleierungsformat schreibt --
 * man liest sie nie, sie flackert.
 */
public class FelCrystalItem extends Item {

    private final Wellenlaenge wellenlaenge;
    private final boolean verschleiert;

    public FelCrystalItem(Properties properties, Wellenlaenge wellenlaenge) {
        this(properties, wellenlaenge, false);
    }

    public FelCrystalItem(Properties properties, Wellenlaenge wellenlaenge, boolean verschleiert) {
        super(properties.stacksTo(1));
        this.wellenlaenge = wellenlaenge;
        this.verschleiert = verschleiert;
    }

    public Wellenlaenge getWellenlaenge() {
        return this.wellenlaenge;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        if(this.verschleiert) {
            components.add(Component.literal("THERADIANCEOFATHOUSANDSUNS").withStyle(ChatFormatting.OBFUSCATED));
        } else {
            components.add(Component.translatable(stack.getDescriptionId() + ".desc"));
        }

        components.add(Component.translatable(this.wellenlaenge.name).withStyle(this.wellenlaenge.textfarbe)
                .append(Component.literal(" - "))
                .append(Component.translatable(this.wellenlaenge.bereich).withStyle(this.wellenlaenge.textfarbe)));
    }

    /**
     * Die fuenf Wellenlaengen des Originals, in ihrer Reihenfolge -- und die zaehlt: die SILEX
     * vergleicht die Ordnungszahl ihres Modus mit der des Rezepts, und je weiter der Modus
     * darueber liegt, desto schneller laeuft sie.
     *
     * NULL IST KEINE WELLENLAENGE, sondern der Zustand ohne Kristall. Das Original nennt sie
     * "la creatura" und fragt im Kommentar daneben, warum es sie gibt; die Antwort ist, dass
     * jeder FEL ohne Kristall sie fuehrt.
     */
    public enum Wellenlaenge {

        NULL("wavelengths.name.null", "wavelengths.waveRange.null", 0x010101, 0x010101, ChatFormatting.WHITE),
        IR("wavelengths.name.ir", "wavelengths.waveRange.ir", 0xBB1010, 0xCC4040, ChatFormatting.RED),
        VISIBLE("wavelengths.name.visible", "wavelengths.waveRange.visible", 0, 0, ChatFormatting.GREEN),
        UV("wavelengths.name.uv", "wavelengths.waveRange.uv", 0x0A1FC4, 0x00EFFF, ChatFormatting.AQUA),
        GAMMA("wavelengths.name.gamma", "wavelengths.waveRange.gamma", 0x150560, 0xEF00FF, ChatFormatting.LIGHT_PURPLE),
        DRX("wavelengths.name.drx", "wavelengths.waveRange.drx", 0xFF0000, 0xFF0000, ChatFormatting.DARK_RED);

        public final String name;
        public final String bereich;
        /** Die Farbe des gezeichneten Strahls. */
        public final int strahlfarbe;
        /** Die Farbe im Fenster. Sichtbares Licht traegt hier null -- es schillert. */
        public final int fensterfarbe;
        public final ChatFormatting textfarbe;

        Wellenlaenge(String name, String bereich, int strahlfarbe, int fensterfarbe, ChatFormatting textfarbe) {
            this.name = name;
            this.bereich = bereich;
            this.strahlfarbe = strahlfarbe;
            this.fensterfarbe = fensterfarbe;
            this.textfarbe = textfarbe;
        }
    }
}
