package com.hbm.datagen;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.main.NuclearTechMod;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Runde 228. Das Gegenstueck zu getCreatureAttribute() aus 1.7.10.
 *
 * Im Original sagt eine Kreatur selbst, ob sie untot ist, und EnumCreatureAttribute.UNDEAD
 * entscheidet dann ueber Bann (Smite), ueber Heil- und Schadenstraenke und ueber die Wirkung
 * des Heiligenscheins. In 1.21 sagt das niemand mehr selbst: es steht im Tag minecraft:undead,
 * und sensitive_to_smite wie inverted_healing_and_harm zeigen beide dorthin. Ein Eintrag
 * genuegt also fuer alle drei Wirkungen.
 */
public class NtmEntityTypeTagsProvider extends EntityTypeTagsProvider {

    public NtmEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, @Nullable ExistingFileHelper helper) {
        super(output, lookup, NuclearTechMod.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(EntityTypeTags.UNDEAD).add(NtmEntityTypes.UNDEAD_SOLDIER.get());
        /* Made und Glyphid sind Gliederfuesser. Das Original sagt das ueber
         * getCreatureAttribute, das es auf 1.21 nicht mehr gibt -- hier traegt es der Tag.
         * Die Made kam in Runde 297 dazu, die Glyphiden in Runde 299 nachgereicht: in
         * Runde 298 war das uebersehen worden, das Schwert der Gliederfuesser tat ihnen
         * nichts. */
        this.tag(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS).add(
                NtmEntityTypes.PARASITE_MAGGOT.get(),
                NtmEntityTypes.GLYPHID.get(),
                NtmEntityTypes.GLYPHID_BRAWLER.get(),
                NtmEntityTypes.GLYPHID_BOMBARDIER.get(),
                NtmEntityTypes.GLYPHID_BLASTER.get(),
                NtmEntityTypes.GLYPHID_DIGGER.get());
    }
}
