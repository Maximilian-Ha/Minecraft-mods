package com.hbm.world.gen;

import com.hbm.main.NuclearTechMod;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Die Vorlagenpools des Meteoritenverlieses -- die Uebersetzung der JigsawPools aus
 * NTMWorldGenerator.java:278 ff.
 *
 * WAS EIN POOL IST: eine gewichtete Liste von Bauwerksstuecken. Ein Jigsaw-Block nennt einen
 * Pool; an seiner Anschlussstelle wird ein Stueck daraus gezogen, gedreht und angesetzt. Das
 * ist in 1.21 dasselbe Verfahren wie im Original -- HBM hat es von Vanilla nachgebaut, und
 * hier geht es wieder zurueck.
 *
 * DER AUSWEICHPOOL (fallback) hat in beiden dieselbe Bedeutung: er kommt zum Zug, wenn das
 * Verlies seine Grenze erreicht hat und nicht weiterwachsen darf. Dann werden Gaenge mit einer
 * Wand verschlossen statt offen zu enden. Der Pool "3x3loot" hat sich selbst als Ausweich --
 * das ist kein Versehen, sondern steht so im Original, mit dem Kommentar "generate loot even
 * if we're at the size limit". Die Beutestuecke haben keine eigenen Ausgaenge, also endet es
 * dort.
 */
public class NtmTemplatePools {

    public static final ResourceKey<StructureTemplatePool> START = registerKey("meteor/start");
    public static final ResourceKey<StructureTemplatePool> SPIKE = registerKey("meteor/spike");
    public static final ResourceKey<StructureTemplatePool> DEFAULT = registerKey("meteor/default");
    public static final ResourceKey<StructureTemplatePool> ROOM10 = registerKey("meteor/10room");
    public static final ResourceKey<StructureTemplatePool> LOOT3X3 = registerKey("meteor/3x3loot");
    public static final ResourceKey<StructureTemplatePool> HEADLOOT = registerKey("meteor/headloot");
    public static final ResourceKey<StructureTemplatePool> FALLBACK = registerKey("meteor/fallback");
    public static final ResourceKey<StructureTemplatePool> ROOMBACK = registerKey("meteor/roomback");
    public static final ResourceKey<StructureTemplatePool> HEADBACK = registerKey("meteor/headback");

    /* Die Einzelbauwerke der Wueste. Jedes ist im Original EIN JigsawPiece ohne Pools -- in
     * 1.21 braucht auch das einen Pool, denn ein Jigsaw-Bauwerk faengt immer bei einem an.
     * Ein Stueck, Gewicht eins, kein Ausweich. */
    public static final ResourceKey<StructureTemplatePool> VERTIBIRD = registerKey("desert/vertibird");
    public static final ResourceKey<StructureTemplatePool> CRASHED_VERTIBIRD = registerKey("desert/crashed_vertibird");
    public static final ResourceKey<StructureTemplatePool> DESERT_SHACK_1 = registerKey("desert/desert_shack_1");
    public static final ResourceKey<StructureTemplatePool> DESERT_SHACK_2 = registerKey("desert/desert_shack_2");
    public static final ResourceKey<StructureTemplatePool> DESERT_SHACK_3 = registerKey("desert/desert_shack_3");
    public static final ResourceKey<StructureTemplatePool> DEAD_DISH_SMALL = registerKey("desert/dead_dish_small");

    /* Die zehn Ruinen. Jede ein Stueck, jede mit dem Schwerkraftprozessor. */
    public static final ResourceKey<StructureTemplatePool> RUIN_A = registerKey("ruins/ruin_a");
    public static final ResourceKey<StructureTemplatePool> RUIN_B = registerKey("ruins/ruin_b");
    public static final ResourceKey<StructureTemplatePool> RUIN_C = registerKey("ruins/ruin_c");
    public static final ResourceKey<StructureTemplatePool> RUIN_D = registerKey("ruins/ruin_d");
    public static final ResourceKey<StructureTemplatePool> RUIN_E = registerKey("ruins/ruin_e");
    public static final ResourceKey<StructureTemplatePool> RUIN_F = registerKey("ruins/ruin_f");
    public static final ResourceKey<StructureTemplatePool> RUIN_G = registerKey("ruins/ruin_g");
    public static final ResourceKey<StructureTemplatePool> RUIN_H = registerKey("ruins/ruin_h");
    public static final ResourceKey<StructureTemplatePool> RUIN_I = registerKey("ruins/ruin_i");
    public static final ResourceKey<StructureTemplatePool> RUIN_J = registerKey("ruins/ruin_j");

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {

        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        HolderGetter<StructureProcessorList> prozessoren = context.lookup(Registries.PROCESSOR_LIST);

        Holder<StructureTemplatePool> leer = pools.getOrThrow(Pools.EMPTY);
        Holder<StructureProcessorList> ziegel = prozessoren.getOrThrow(NtmProcessorLists.METEOR_BRICKS);
        Holder<StructureProcessorList> kisten = prozessoren.getOrThrow(NtmProcessorLists.METEOR_CRATES);
        Holder<StructureProcessorList> schleim = prozessoren.getOrThrow(NtmProcessorLists.METEOR_OOZE);
        Holder<StructureProcessorList> spitze = prozessoren.getOrThrow(NtmProcessorLists.METEOR_SPIKE);

        Eintraege start = new Eintraege();
        start.add("meteor/meteor-core", ziegel, 1);
        context.register(START, new StructureTemplatePool(leer, start.liste(), StructureTemplatePool.Projection.RIGID));

        Eintraege dorn = new Eintraege();
        dorn.add("meteor/meteor-spike", spitze, 1);
        context.register(SPIKE, new StructureTemplatePool(leer, dorn.liste(), StructureTemplatePool.Projection.RIGID));

        Eintraege gaenge = new Eintraege();
        gaenge.add("meteor/meteor-corner", ziegel, 2);
        gaenge.add("meteor/meteor-t", ziegel, 3);
        gaenge.add("meteor/meteor-stairs", ziegel, 1);
        gaenge.add("meteor/room10/room-base-thru", ziegel, 3);
        gaenge.add("meteor/room10/room-base-end", ziegel, 4);
        context.register(DEFAULT, new StructureTemplatePool(pools.getOrThrow(FALLBACK), gaenge.liste(),
                StructureTemplatePool.Projection.RIGID));

        Eintraege zimmer = new Eintraege();
        zimmer.add("meteor/room10/room-basic", ziegel, 1);
        zimmer.add("meteor/room10/room-balcony", ziegel, 1);
        zimmer.add("meteor/room10/room-dragon", ziegel, 1);
        zimmer.add("meteor/room10/room-ladder", ziegel, 1);
        zimmer.add("meteor/room10/room-ooze", schleim, 1);
        zimmer.add("meteor/room10/room-split", ziegel, 1);
        zimmer.add("meteor/room10/room-stairs", ziegel, 1);
        zimmer.add("meteor/room10/room-triple", ziegel, 1);
        context.register(ROOM10, new StructureTemplatePool(pools.getOrThrow(ROOMBACK), zimmer.liste(),
                StructureTemplatePool.Projection.RIGID));

        /* Die sechzehn Beutestuecke tragen keine Blocktabelle -- im Original steht hinter
         * keinem von ihnen ein blockTable. */
        Eintraege beute = new Eintraege();
        for(String name : new String[] {
                "bale", "blank", "block", "crab", "crab-tesla", "crate", "dirt", "lead",
                "ooze", "pillar", "star", "tesla", "book", "mku", "statue", "glow" }) {
            beute.add("meteor/loot3x3/meteor-3-" + name, 1);
        }
        context.register(LOOT3X3, new StructureTemplatePool(pools.getOrThrow(LOOT3X3), beute.liste(),
                StructureTemplatePool.Projection.RIGID));

        Eintraege drachen = new Eintraege();
        drachen.add("meteor/room10/headloot/loot-chest", kisten, 1);
        drachen.add("meteor/room10/headloot/loot-tesla", kisten, 1);
        drachen.add("meteor/room10/headloot/loot-trap", kisten, 1);
        drachen.add("meteor/room10/headloot/loot-crate-crab", kisten, 1);
        context.register(HEADLOOT, new StructureTemplatePool(pools.getOrThrow(HEADBACK), drachen.liste(),
                StructureTemplatePool.Projection.RIGID));

        Eintraege wand = new Eintraege();
        wand.add("meteor/meteor-fallback", ziegel, 1);
        context.register(FALLBACK, new StructureTemplatePool(leer, wand.liste(), StructureTemplatePool.Projection.RIGID));

        Eintraege zimmerwand = new Eintraege();
        zimmerwand.add("meteor/room10/room-fallback", ziegel, 1);
        context.register(ROOMBACK, new StructureTemplatePool(leer, zimmerwand.liste(), StructureTemplatePool.Projection.RIGID));

        Eintraege drachenwand = new Eintraege();
        drachenwand.add("meteor/room10/headloot/loot-fallback", kisten, 1);
        context.register(HEADBACK, new StructureTemplatePool(leer, drachenwand.liste(), StructureTemplatePool.Projection.RIGID));

        einzeln(context, leer, VERTIBIRD, "desert/vertibird");
        einzeln(context, leer, CRASHED_VERTIBIRD, "desert/crashed_vertibird");
        einzeln(context, leer, DESERT_SHACK_1, "desert/desert_shack_1");
        einzeln(context, leer, DESERT_SHACK_2, "desert/desert_shack_2");
        einzeln(context, leer, DESERT_SHACK_3, "desert/desert_shack_3");
        einzeln(context, leer, DEAD_DISH_SMALL, "desert/dead_dish_small");

        Holder<StructureProcessorList> gelaende = prozessoren.getOrThrow(NtmProcessorLists.RUINS);
        einzeln(context, leer, RUIN_A, "ruins/ruin_a", gelaende);
        einzeln(context, leer, RUIN_B, "ruins/ruin_b", gelaende);
        einzeln(context, leer, RUIN_C, "ruins/ruin_c", gelaende);
        einzeln(context, leer, RUIN_D, "ruins/ruin_d", gelaende);
        einzeln(context, leer, RUIN_E, "ruins/ruin_e", gelaende);
        einzeln(context, leer, RUIN_F, "ruins/ruin_f", gelaende);
        einzeln(context, leer, RUIN_G, "ruins/ruin_g", gelaende);
        einzeln(context, leer, RUIN_H, "ruins/ruin_h", gelaende);
        einzeln(context, leer, RUIN_I, "ruins/ruin_i", gelaende);
        einzeln(context, leer, RUIN_J, "ruins/ruin_j", gelaende);
    }

    /**
     * Ein Pool aus genau einem Stueck -- die Form, die ein Einzelbauwerk in 1.21 braucht.
     *
     * RIGID heisst: das Stueck steht, wie es gebaut wurde, und bekommt keine Gelaendeanpassung.
     * Das entspricht dem Original, das seine Einzelbauwerke ebenso unveraendert setzt; die
     * Ausnahme sind die Ruinen mit conformToTerrain, und die kommen mit einem Prozessor.
     */
    private static void einzeln(BootstrapContext<StructureTemplatePool> context, Holder<StructureTemplatePool> leer,
            ResourceKey<StructureTemplatePool> schluessel, String pfad) {

        Eintraege eintrag = new Eintraege();
        eintrag.add(pfad, 1);
        context.register(schluessel, new StructureTemplatePool(leer, eintrag.liste(), StructureTemplatePool.Projection.RIGID));
    }

    /** Dasselbe, aber mit Prozessoren -- die Ruinen folgen dem Gelaende. */
    private static void einzeln(BootstrapContext<StructureTemplatePool> context, Holder<StructureTemplatePool> leer,
            ResourceKey<StructureTemplatePool> schluessel, String pfad, Holder<StructureProcessorList> prozessoren) {

        Eintraege eintrag = new Eintraege();
        eintrag.add(pfad, prozessoren, 1);
        context.register(schluessel, new StructureTemplatePool(leer, eintrag.liste(), StructureTemplatePool.Projection.RIGID));
    }

    /** Sammelt die gewichteten Stuecke eines Pools; nur damit die Aufrufe oben lesbar bleiben. */
    private static class Eintraege {

        private final List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> liste =
                new ArrayList<>();

        void add(String pfad, int gewicht) {
            this.liste.add(Pair.of(StructurePoolElement.single(NuclearTechMod.MODID + ":" + pfad), gewicht));
        }

        void add(String pfad, Holder<StructureProcessorList> prozessoren, int gewicht) {
            this.liste.add(Pair.of(StructurePoolElement.single(NuclearTechMod.MODID + ":" + pfad, prozessoren), gewicht));
        }

        List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> liste() {
            return this.liste;
        }
    }

    private static ResourceKey<StructureTemplatePool> registerKey(String path) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, NuclearTechMod.withDefaultNamespace(path));
    }
}
