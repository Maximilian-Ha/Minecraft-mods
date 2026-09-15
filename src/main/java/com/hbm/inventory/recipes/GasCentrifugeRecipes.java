package com.hbm.inventory.recipes;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.NtmItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.recipes.GasCentrifugeRecipes.
 *
 * DIE PSEUDOFLUIDE SIND DER KERN DER MASCHINE. Uranhexafluorid ist im Rohrnetz EIN Stoff --
 * es gibt kein Rohr fuer "schwach angereichertes UF6". Die Anreicherungsstufe steht deshalb
 * nicht im Fluidsystem, sondern nur INNERHALB der Zentrifugenkette: NUF6 wird zu LEUF6, das
 * zu MEUF6, das zu HEUF6. Wer die Kette unterbricht, kommt nicht weiter.
 *
 * JEDE STUFE GIBT ETWAS AB. Aus Natururan faellt Uran-238 heraus und das Fluid wird eine Stufe
 * reiner; die letzte Stufe gibt das Uran-235 und loest sich ganz auf. So ist die Ausbeute an
 * Spaltstoff klein und der Abfall gross -- wie es sein soll.
 *
 * HEUF6 BRAUCHT DIE SCHNELLDREHENDE ZENTRIFUGE. Die letzte Stufe laeuft nur mit der Aufwertung;
 * ohne sie steht die Kette beim mittelangereicherten Stoff.
 *
 * ABWEICHUNG: das Original sammelt in gasCent zusaetzlich eine Tabelle allein fuer die
 * Rezeptanzeige von NEI. Die steht hier nicht -- sie wuerde nichts antreiben.
 *
 * ABWEICHUNG: die Erzeugnisse stehen als Lieferanten (Supplier) statt als fertige Stapel. Auf
 * 1.21 sind Gegenstaende erst nach der Registrierung da, und dieser Satz wird beim Laden der
 * Klasse gebaut.
 */
public class GasCentrifugeRecipes {

    /**
     * Eine Anreicherungsstufe. Was hineingeht, was herauskommt, was dabei abfaellt, und ob es
     * dafuer die schnelle Zentrifuge braucht.
     */
    public static class PseudoFluidType {

        public static final Map<String, PseudoFluidType> types = new HashMap<>();

        public static final PseudoFluidType NONE = new PseudoFluidType("NONE", 0, 0, null, false);

        public static final PseudoFluidType HEUF6 = new PseudoFluidType("HEUF6", 300, 0, "NONE", true,
                () -> new ItemStack(NtmItems.NUGGET_U238.get(), 2),
                () -> new ItemStack(NtmItems.NUGGET_U235.get(), 1),
                () -> new ItemStack(NtmItems.FLUORITE.get(), 1));

        public static final PseudoFluidType MEUF6 = new PseudoFluidType("MEUF6", 200, 100, "HEUF6", false,
                () -> new ItemStack(NtmItems.NUGGET_U238.get(), 1));

        public static final PseudoFluidType LEUF6 = new PseudoFluidType("LEUF6", 300, 200, "MEUF6", false,
                () -> new ItemStack(NtmItems.NUGGET_U238.get(), 1),
                () -> new ItemStack(NtmItems.FLUORITE.get(), 1));

        public static final PseudoFluidType NUF6 = new PseudoFluidType("NUF6", 400, 300, "LEUF6", false,
                () -> new ItemStack(NtmItems.NUGGET_U238.get(), 1));

        public static final PseudoFluidType PF6 = new PseudoFluidType("PF6", 300, 0, "NONE", false,
                () -> new ItemStack(NtmItems.NUGGET_PU238.get(), 1),
                () -> new ItemStack(NtmItems.NUGGET_PU_MIX.get(), 2),
                () -> new ItemStack(NtmItems.FLUORITE.get(), 1));

        public static final PseudoFluidType MUD_HEAVY = new PseudoFluidType("MUD_HEAVY", 500, 0, "NONE", false,
                () -> new ItemStack(NtmItems.POWDER_IRON.get(), 1),
                () -> new ItemStack(NtmItems.DUST.get(), 1),
                () -> new ItemStack(NtmItems.NUCLEAR_WASTE_TINY.get(), 1));

        public static final PseudoFluidType MUD = new PseudoFluidType("MUD", 1_000, 500, "MUD_HEAVY", false,
                () -> new ItemStack(NtmItems.POWDER_LEAD.get(), 1),
                () -> new ItemStack(NtmItems.DUST.get(), 1));

        public final String name;
        private final int fluidConsumed;
        private final int fluidProduced;

        /* DER NACHFOLGER STEHT ALS NAME, nicht als Verweis. NUF6 nennt LEUF6, und LEUF6 wird
         * erst danach gebaut -- ein Verweis waere zu diesem Zeitpunkt null. Das Original loest
         * das, indem es die Stufen rueckwaerts anlegt; das geht hier nicht, weil die oberste
         * Stufe auf NONE zeigt und NONE zuerst dasteht. */
        private final String outputName;
        private final boolean highSpeed;
        private final Supplier<ItemStack>[] output;

        @SafeVarargs
        private PseudoFluidType(String name, int fluidConsumed, int fluidProduced, String outputName, boolean highSpeed, Supplier<ItemStack>... output) {
            this.name = name;
            this.fluidConsumed = fluidConsumed;
            this.fluidProduced = fluidProduced;
            this.outputName = outputName;
            this.highSpeed = highSpeed;
            this.output = output;
            types.put(name, this);
        }

        public int getFluidConsumed() { return this.fluidConsumed; }
        public int getFluidProduced() { return this.fluidProduced; }
        public boolean getIfHighSpeed() { return this.highSpeed; }

        /** Die naechste Stufe, oder NONE fuer "hier ist Schluss". */
        public PseudoFluidType getOutputType() {
            if(this.outputName == null) return NONE;
            PseudoFluidType next = types.get(this.outputName);
            return next == null ? NONE : next;
        }

        /** Frische Stapel; der Aufrufer darf sie behalten. */
        public ItemStack[] getOutput() {
            ItemStack[] stacks = new ItemStack[this.output.length];
            for(int i = 0; i < this.output.length; i++) stacks[i] = this.output[i].get();
            return stacks;
        }

        public Component getName() {
            return Component.translatable("hbmpseudofluid." + this.name.toLowerCase(Locale.US));
        }

        /** Sucht eine Stufe nach Namen; unbekannte Namen ergeben NONE. */
        public static PseudoFluidType byName(String name) {
            PseudoFluidType type = types.get(name);
            return type == null ? NONE : type;
        }
    }

    /** Welches Fluid im Rohr welche Kette beginnt. */
    public static final Map<FluidType, PseudoFluidType> fluidConversions = new HashMap<>();

    public static void register() {
        fluidConversions.put(Fluids.UF6, PseudoFluidType.NUF6);
        fluidConversions.put(Fluids.PUF6, PseudoFluidType.PF6);
        fluidConversions.put(Fluids.WATZ, PseudoFluidType.MUD);
    }
}
