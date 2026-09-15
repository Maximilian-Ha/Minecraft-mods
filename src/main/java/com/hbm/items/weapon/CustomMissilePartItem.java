package com.hbm.items.weapon;

import com.hbm.entity.missile.MissileCustom;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.ItemCustomMissilePart.
 *
 * Ein Bauteil der Eigenbau-Rakete. Fuenf Arten -- Chip, Sprengkopf, Rumpf, Leitwerk und
 * Triebwerk -- und jedes traegt seine Kennwerte als Gegenstand mit sich, nicht als Zahlenwert am
 * Stapel: JEDES BAUTEIL IST EIN EIGENER GEGENSTAND. Das Original macht es genauso, und deshalb
 * gibt es hundertzweiundzwanzig davon.
 *
 * ZUSAMMENPASSEN MUSS ES OBEN UND UNTEN. Jedes Teil hat eine Groesse oben und eine unten; ein
 * Rumpf von 1,0 m oben nimmt nur ein Leitwerk oder einen Sprengkopf derselben Groesse an. So
 * entstehen die Uebergangsstuecke, die unten breiter sind als oben.
 *
 * DIE HAUT IST NUR HAUT. Einundsechzig der hundertzweiundzwanzig Teile sind Abschriften mit
 * anderem Namen und anderer Seltenheit -- Tarnanstriche, Flammen, Blech. Ihre Kennwerte sind bis
 * auf gelegentlich mehr Haltbarkeit dieselben.
 */
public class CustomMissilePartItem extends Item {

    public PartType type;
    public PartSize top;
    public PartSize bottom;
    public Rarity rarity;
    public float health;

    private String title;
    private String author;
    private String witty;

    /**
     * Die Kennwerte, je nach Art verschieden belegt -- so wie im Original.
     *
     * Chip:       [0] Streuung
     * Sprengkopf: [0] Art, [1] Staerke/Radius/Anzahl, [2] Gewicht
     * Rumpf:      [0] Treibstoffart, [1] Tankgroesse
     * Leitwerk:   [0] Streuungsabzug
     * Triebwerk:  [0] Treibstoffart, [1] Verbrauch, [2] Schub
     */
    public Object[] attributes;

    /** Alle Bauteile, nach ihrem Namen auffindbar -- die Rakete liest sie beim Laden zurueck. */
    public static final Map<String, CustomMissilePartItem> parts = new HashMap<>();

    public CustomMissilePartItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public enum PartType {
        CHIP,
        WARHEAD,
        FUSELAGE,
        FINS,
        THRUSTER
    }

    public enum PartSize {
        /** Fuer Chips: passt ueberall. */
        ANY,
        /** Fuer Spitzen und Triebwerke: da ist nichts mehr. */
        NONE,
        SIZE_10,
        SIZE_15,
        SIZE_20
    }

    public enum WarheadType {

        HE,
        INC,
        BUSTER,
        CLUSTER,
        NUCLEAR,
        TX,
        N2,
        BALEFIRE,
        SCHRAB,
        TAINT,
        CLOUD,
        TURBINE,

        /* Leere Plaetze, an die sich eigene Sprengkoepfe haengen lassen -- eine Notloesung des
         * Originals, die aber funktioniert. */
        CUSTOM0, CUSTOM1, CUSTOM2, CUSTOM3, CUSTOM4, CUSTOM5, CUSTOM6, CUSTOM7, CUSTOM8, CUSTOM9;

        /** Ersetzt die Wirkung beim Einschlag. Laeuft nur auf der Server-Seite. */
        public Consumer<MissileCustom> impactCustom = null;
        /** Laeuft zu Beginn jedes Ticks der Rakete, auf beiden Seiten. */
        public Consumer<MissileCustom> updateCustom = null;
        /** Ersetzt den Namen des Sprengkopfs in der Beschreibung. */
        public String labelCustom = null;
    }

    public enum FuelType {
        KEROSENE,
        SOLID,
        HYDROGEN,
        XENON,
        BALEFIRE
    }

    public enum Rarity {

        COMMON("item.missile.part.rarity.common", ChatFormatting.GRAY),
        UNCOMMON("item.missile.part.rarity.uncommon", ChatFormatting.YELLOW),
        RARE("item.missile.part.rarity.rare", ChatFormatting.AQUA),
        EPIC("item.missile.part.rarity.epic", ChatFormatting.LIGHT_PURPLE),
        LEGENDARY("item.missile.part.rarity.legendary", ChatFormatting.DARK_GREEN),
        /* Der Name der letzten Stufe ist der des Originals und bleibt unuebersetzt. */
        STRANGE("item.missile.part.rarity.strange", ChatFormatting.DARK_AQUA);

        private final String key;
        private final ChatFormatting color;

        Rarity(String key, ChatFormatting color) {
            this.key = key;
            this.color = color;
        }

        public Component getDisplay() {
            return Component.translatable(this.key).withStyle(this.color);
        }
    }

    /* ---- der Erbauer ---- */

    public CustomMissilePartItem makeChip(float inaccuracy) {
        this.type = PartType.CHIP;
        this.top = PartSize.ANY;
        this.bottom = PartSize.ANY;
        this.attributes = new Object[] { inaccuracy };
        return this;
    }

    public CustomMissilePartItem makeWarhead(WarheadType type, float punch, float weight, PartSize size) {
        this.type = PartType.WARHEAD;
        this.top = PartSize.NONE;
        this.bottom = size;
        this.attributes = new Object[] { type, punch, weight };
        return this;
    }

    public CustomMissilePartItem makeFuselage(FuelType type, float fuel, PartSize top, PartSize bottom) {
        this.type = PartType.FUSELAGE;
        this.top = top;
        this.bottom = bottom;
        this.attributes = new Object[] { type, fuel };
        return this;
    }

    public CustomMissilePartItem makeStability(float inaccuracy, PartSize size) {
        this.type = PartType.FINS;
        this.top = size;
        this.bottom = size;
        this.attributes = new Object[] { inaccuracy };
        return this;
    }

    public CustomMissilePartItem makeThruster(FuelType type, float consumption, float lift, PartSize size) {
        this.type = PartType.THRUSTER;
        this.top = size;
        this.bottom = PartSize.NONE;
        this.attributes = new Object[] { type, consumption, lift };
        return this;
    }

    /**
     * Uebernimmt die Kennwerte eines anderen Teils. Das Original nennt die Methode copy und
     * fragt sich im Kommentar daneben, ob es noch ganz richtig tickt; die Antwort steht eine
     * Zeile darunter.
     */
    public CustomMissilePartItem copyFrom(CustomMissilePartItem other) {
        this.type = other.type;
        this.top = other.top;
        this.bottom = other.bottom;
        this.health = other.health;
        this.attributes = other.attributes;
        return this;
    }

    public CustomMissilePartItem setAuthor(String author) { this.author = author; return this; }
    public CustomMissilePartItem setTitle(String title) { this.title = title; return this; }
    public CustomMissilePartItem setWittyText(String witty) { this.witty = witty; return this; }
    public CustomMissilePartItem setHealth(float health) { this.health = health; return this; }
    public CustomMissilePartItem setRarity(Rarity rarity) { this.rarity = rarity; return this; }

    /* ---- die Anzeige ---- */

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        if(this.title != null) {
            components.add(Component.literal("\"" + this.title + "\"").withStyle(ChatFormatting.DARK_PURPLE));
        }

        if(this.type != null && this.attributes != null) {
            switch(this.type) {
                case CHIP -> line(components, "item.missile.part.inaccuracy", ((Float) this.attributes[0]) * 100F + "%");
                case WARHEAD -> {
                    line(components, "item.missile.part.size", getSize(this.bottom));
                    components.add(label("item.missile.part.type").append(getWarhead((WarheadType) this.attributes[0])));
                    line(components, "item.missile.part.strength", String.valueOf((Float) this.attributes[1]));
                    line(components, "item.missile.part.weight", this.attributes[2] + "t");
                }
                case FUSELAGE -> {
                    line(components, "item.missile.part.topSize", getSize(this.top));
                    line(components, "item.missile.part.bottomSize", getSize(this.bottom));
                    components.add(label("item.missile.part.fuelType").append(getFuel((FuelType) this.attributes[0])));
                    line(components, "item.missile.part.fuelAmount", this.attributes[1] + "l");
                }
                case FINS -> {
                    line(components, "item.missile.part.size", getSize(this.top));
                    line(components, "item.missile.part.inaccuracy", ((Float) this.attributes[0]) * 100F + "%");
                }
                case THRUSTER -> {
                    line(components, "item.missile.part.size", getSize(this.top));
                    components.add(label("item.missile.part.fuelType").append(getFuel((FuelType) this.attributes[0])));
                    line(components, "item.missile.part.fuelConsumption", this.attributes[1] + "l/tick");
                    line(components, "item.missile.part.maxPayload", this.attributes[2] + "t");
                }
            }
        }

        if(this.type != PartType.CHIP) line(components, "item.missile.part.health", this.health + "HP");

        if(this.rarity != null) components.add(label("item.missile.part.rarity").append(this.rarity.getDisplay()));
        if(this.author != null) components.add(Component.translatable("item.missile.part.by").append(" " + this.author).withStyle(ChatFormatting.WHITE));
        if(this.witty != null) components.add(Component.literal("   \"" + this.witty + "\"").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
    }

    /* MutableComponent, nicht Component: append() gibt es nur auf der veraenderlichen Fassung,
     * und jede Zeile haengt hier noch etwas an. */
    private static MutableComponent label(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.BOLD).append(Component.literal(": ").withStyle(ChatFormatting.GRAY));
    }

    private static void line(List<Component> components, String key, String value) {
        components.add(label(key).append(Component.literal(value).withStyle(ChatFormatting.GRAY)));
    }

    /** Dieselbe Zeile, wenn der Wert schon ein fertiger Textbaustein ist (Groesse, Treibstoff). */
    private static void line(List<Component> components, String key, Component value) {
        components.add(label(key).append(value));
    }

    public static Component getSize(PartSize size) {
        return switch(size) {
            case ANY -> Component.translatable("item.missile.part.size.any");
            case SIZE_10 -> Component.literal("1.0m");
            case SIZE_15 -> Component.literal("1.5m");
            case SIZE_20 -> Component.literal("2.0m");
            default -> Component.translatable("item.missile.part.size.none");
        };
    }

    public static Component getWarhead(WarheadType type) {

        if(type.labelCustom != null) return Component.literal(type.labelCustom);

        return switch(type) {
            case HE -> Component.translatable("item.warhead.desc.he").withStyle(ChatFormatting.YELLOW);
            case INC -> Component.translatable("item.warhead.desc.incendiary").withStyle(ChatFormatting.GOLD);
            case CLUSTER -> Component.translatable("item.warhead.desc.cluster").withStyle(ChatFormatting.GRAY);
            case BUSTER -> Component.translatable("item.warhead.desc.bunker_buster").withStyle(ChatFormatting.WHITE);
            case NUCLEAR -> Component.translatable("item.warhead.desc.nuclear").withStyle(ChatFormatting.DARK_GREEN);
            case TX -> Component.translatable("item.warhead.desc.thermonuclear").withStyle(ChatFormatting.DARK_PURPLE);
            case N2 -> Component.translatable("item.warhead.desc.n2").withStyle(ChatFormatting.RED);
            case BALEFIRE -> Component.translatable("item.warhead.desc.balefire").withStyle(ChatFormatting.GREEN);
            case SCHRAB -> Component.translatable("item.warhead.desc.schrabidium").withStyle(ChatFormatting.AQUA);
            case TAINT -> Component.translatable("item.warhead.desc.taint").withStyle(ChatFormatting.DARK_PURPLE);
            case CLOUD -> Component.translatable("item.warhead.desc.cloud").withStyle(ChatFormatting.LIGHT_PURPLE);
            /* Die Turbine blinkt -- das ist im Original genauso. */
            case TURBINE -> Component.translatable("item.warhead.desc.turbine")
                    .withStyle(System.currentTimeMillis() % 1000 < 500 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE);
            default -> Component.translatable("general.na").withStyle(ChatFormatting.BOLD);
        };
    }

    public static Component getFuel(FuelType type) {
        return switch(type) {
            case KEROSENE -> Component.translatable("item.missile.fuel.kerosene_peroxide").withStyle(ChatFormatting.LIGHT_PURPLE);
            case SOLID -> Component.translatable("item.missile.fuel.solid").withStyle(ChatFormatting.GOLD);
            case HYDROGEN -> Component.translatable("item.missile.fuel.hydrogen").withStyle(ChatFormatting.DARK_AQUA);
            case XENON -> Component.translatable("item.missile.fuel.xenon").withStyle(ChatFormatting.DARK_PURPLE);
            case BALEFIRE -> Component.translatable("item.missile.fuel.balefire").withStyle(ChatFormatting.GREEN);
        };
    }
}
