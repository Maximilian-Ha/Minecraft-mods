package com.hbm.items.tools;

import api.hbm.block.IToolable.ToolType;

import com.hbm.entity.mob.CreeperDefuser;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemDefuser.
 *
 * Der Seitenschneider. Als Werkzeug stellt er Maschinen ein wie jedes andere -- das erledigt
 * ToolingItem --, aber auf einen Creeper angewandt schneidet er dessen Zuendschnur durch.
 *
 * BIS RUNDE 242 KONNTE ER DAS NICHT. Der Port hat ihn in Runde 195 als schlichtes
 * ToolingItem angemeldet; der Griff fuer Entitaeten fehlte, weil es CreeperDefuser noch
 * nicht gab. Ein Entschaerfer, der nichts entschaerft, hiess also nur so.
 *
 * NICHT UEBERNOMMEN: der zweite Zweig des Originals, der einen sterbenden EntityGlyphidNuclear
 * an Ort und Stelle sprengt und eine NUKE_DEMO-Patrone fallen laesst. Die Glyphiden gibt es
 * im Port nicht -- kein einziger der rund zwei Dutzend EntityGlyphid* ist portiert. Der Zweig
 * kommt mit ihnen, nicht vorher.
 */
public class DefuserItem extends ToolingItem {

    public DefuserItem(ToolType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {

        if(!(entity instanceof Creeper creeper)) return InteractionResult.PASS;

        if(player.level().isClientSide) return InteractionResult.SUCCESS;

        /* KEINE ABNUTZUNG, und das ist das Original. ItemTooling nutzt beim Einstellen
         * eines Blocks einen Punkt Haltbarkeit ab; itemInteractionForEntity tut es nicht.
         * Der Seitenschneider entschaerft also beliebig viele Creeper, ohne stumpf zu
         * werden. Wahrscheinlich ein Versehen dort -- aber eines nachzubessern hiesse,
         * eine Wirkung zu erfinden, die das Original nicht hat. */
        CreeperDefuser.entschaerfe(creeper, player, true);

        return InteractionResult.SUCCESS;
    }
}
