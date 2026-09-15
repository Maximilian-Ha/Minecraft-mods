package com.hbm.module;

import com.hbm.util.ItemStackUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.module.ModulePatternMatcher.
 *
 * Der Mustervergleicher haelt zu jedem Filterfach fest, WIE genau verglichen werden soll. Das
 * Muster selbst ist ein Gegenstand im Fach; der Vergleicher sagt, was daran zaehlt.
 *
 * Drei Arten von Vergleich:
 *   "exact"    -- Gegenstand und Daten muessen uebereinstimmen,
 *   "wildcard" -- nur der Gegenstand muss uebereinstimmen,
 *   alles uebrige ist der Name eines Tags: dann passt jeder Gegenstand, der in diesem Tag steht.
 *
 * DAS LETZTE IST DER KERN DER SACHE. Im Original stehen dort die Namen des OreDictionary --
 * "ingotSteel", "plateIron". Auf 1.21 gibt es den OreDictionary nicht mehr, an seine Stelle sind
 * Tags getreten: aus "ingotSteel" ist "c:ingots/steel" geworden. Der Vergleicher fuehrt deshalb
 * Tag-Namen statt OreDictionary-Namen; sonst ist er unveraendert.
 *
 * NICHT UEBERNOMMEN: die Art "bedrock", die zwei Grundgesteinserze nach ihrer Aufbereitungsstufe
 * vergleicht. Der Port hat ItemBedrockOreNew nicht; eine Art, die kein Gegenstand je erreichen
 * kann, waere toter Code. Sie kommt mit dem Grundgesteinserz.
 *
 * ABWEICHUNG: die Tags eines Gegenstandes kommen in beliebiger Reihenfolge. Damit das
 * Weiterschalten immer denselben Ring durchlaeuft, werden sie hier sortiert -- im Original war
 * die Reihenfolge die des OreDictionary und damit ebenfalls willkuerlich, aber innerhalb einer
 * Sitzung fest.
 */
public class ModulePatternMatcher {

    public static final String MODE_EXACT = "exact";
    public static final String MODE_WILDCARD = "wildcard";

    public String[] modes;

    public ModulePatternMatcher() {
        this(1);
    }

    public ModulePatternMatcher(int count) {
        this.modes = new String[count];
    }

    /**
     * Setzt die Art fuer ein frisch belegtes Fach. Ein Gegenstand mit eigenen Daten wird genau
     * verglichen, alles uebrige nur nach dem Gegenstand.
     *
     * ABWEICHUNG: das Original fragt getHasSubtypes() -- ob der Gegenstand Metadaten fuehrt.
     * Metadaten gibt es auf 1.21 nicht mehr; an ihre Stelle sind die Datenkomponenten getreten.
     * Gefragt wird deshalb, ob der Stapel von den Vorgaben seines Gegenstandes abweicht.
     */
    public void initPatternStandard(Level level, ItemStack stack, int i) {

        if(level == null || level.isClientSide) return;

        if(stack == null || stack.isEmpty()) {
            this.modes[i] = null;
            return;
        }

        this.modes[i] = stack.getComponentsPatch().isEmpty() ? MODE_WILDCARD : MODE_EXACT;
    }

    /**
     * Die Reihenfolge, in der bei der klugen Voreinstellung nach einem Tag gesucht wird. Das
     * Original sucht nach den OreDictionary-Praefixen "ingot", "block", "dust", "nugget",
     * "plate"; im Port heissen dieselben Formen "ingots/", "blocks/", "dusts/", "nuggets/",
     * "plates/" -- so benennt sie das Materialsystem.
     */
    private static final String[] SMART_PREFIXES = { "ingots/", "blocks/", "dusts/", "nuggets/", "plates/" };

    /**
     * Die KLUGE Voreinstellung fuer ein frisch belegtes Fach: gehoert das Muster zu einer der
     * gelaeufigen Werkstoffformen, wird gleich deren Tag eingestellt statt des einen
     * Gegenstandes. Legt man also einen Stahlbarren, steht die Art auf "c:ingots/steel" und der
     * Filter greift bei Barren jeder Herkunft.
     *
     * Das ist genau dort das Gemeinte, wo es benutzt wird -- an einer Sortieranlage. Wer nur
     * diesen einen Gegenstand meint, schaltet mit Rechtsklick zurueck.
     */
    public void initPatternSmart(Level level, ItemStack stack, int i) {

        if(level == null || level.isClientSide) return;

        if(stack == null || stack.isEmpty()) {
            this.modes[i] = null;
            return;
        }

        List<String> names = tagsOf(stack);

        for(String prefix : SMART_PREFIXES) {
            for(String name : names) {
                if(name.substring(name.indexOf(':') + 1).startsWith(prefix)) {
                    this.modes[i] = name;
                    return;
                }
            }
        }

        this.initPatternStandard(level, stack, i);
    }

    /**
     * Schaltet die Art eines Faches eine Stelle weiter: genau, nur der Gegenstand, dann der
     * Reihe nach alle Tags des Musters, dann wieder von vorn.
     */
    public void nextMode(Level level, ItemStack pattern, int i) {

        if(level == null || level.isClientSide) return;

        if(pattern == null || pattern.isEmpty()) {
            this.modes[i] = null;
            return;
        }

        if(this.modes[i] == null) {
            this.modes[i] = MODE_EXACT;
            return;
        }

        if(MODE_EXACT.equals(this.modes[i])) {
            this.modes[i] = MODE_WILDCARD;
            return;
        }

        List<String> names = tagsOf(pattern);

        if(MODE_WILDCARD.equals(this.modes[i])) {
            this.modes[i] = names.isEmpty() ? MODE_EXACT : names.get(0);
            return;
        }

        /* Der Ring schliesst sich am letzten Tag -- danach geht es wieder bei "genau" los. */
        if(names.size() < 2 || this.modes[i].equals(names.get(names.size() - 1))) {
            this.modes[i] = MODE_EXACT;
            return;
        }

        for(int j = 0; j < names.size() - 1; j++) {
            if(this.modes[i].equals(names.get(j))) {
                this.modes[i] = names.get(j + 1);
                return;
            }
        }

        /* Das Muster hat gewechselt und kennt die eingestellte Art nicht mehr. */
        this.modes[i] = MODE_EXACT;
    }

    /** Passt der hereinkommende Gegenstand auf das Muster im Fach? */
    public boolean isValidForFilter(ItemStack filter, int index, ItemStack input) {

        String mode = this.modes[index];

        if(mode == null) {
            this.modes[index] = mode = MODE_EXACT;
        }

        switch(mode) {
        case MODE_EXACT: return ItemStack.isSameItemSameComponents(input, filter);
        case MODE_WILDCARD: return input.getItem() == filter.getItem();
        default:
            ResourceLocation id = ResourceLocation.tryParse(mode);
            return id != null && input.is(ItemTags.create(id));
        }
    }

    /** Die Tags eines Gegenstandes, sortiert -- siehe die Anmerkung zur Reihenfolge oben. */
    private static List<String> tagsOf(ItemStack stack) {
        List<String> names = ItemStackUtil.getTags(stack);
        names.sort(null);
        return names;
    }

    public void load(CompoundTag tag) {

        for(int i = 0; i < this.modes.length; i++) {
            this.modes[i] = tag.contains("mode" + i) ? tag.getString("mode" + i) : null;
        }
    }

    public void save(CompoundTag tag) {

        for(int i = 0; i < this.modes.length; i++) {
            if(this.modes[i] != null) tag.putString("mode" + i, this.modes[i]);
        }
    }

    /* Ueber das Netz steht der leere Text fuer "keine Art gesetzt" -- ein Fach ohne Muster. */
    public void serialize(FriendlyByteBuf buf) {
        for(String mode : this.modes) buf.writeUtf(mode == null ? "" : mode);
    }

    public void deserialize(FriendlyByteBuf buf) {
        for(int i = 0; i < this.modes.length; i++) {
            String mode = buf.readUtf();
            this.modes[i] = mode.isEmpty() ? null : mode;
        }
    }

    public static Component getLabel(String mode) {

        return switch(mode) {
            case MODE_EXACT -> Component.translatable("patternMatcher.exact").withStyle(ChatFormatting.YELLOW);
            case MODE_WILDCARD -> Component.translatable("patternMatcher.wildcard").withStyle(ChatFormatting.YELLOW);
            default -> Component.translatable("patternMatcher.tag", mode).withStyle(ChatFormatting.YELLOW);
        };
    }
}
