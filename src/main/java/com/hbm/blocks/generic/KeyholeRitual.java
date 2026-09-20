package com.hbm.blocks.generic;

import com.hbm.blocks.NtmBlocks;
import com.hbm.items.NtmItems;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Was beide Schluessellochbloecke gemeinsam haben: die Pruefung des Schluessels, die Tuer
 * und der Klang. Im Original steht das in beiden Klassen abgeschrieben.
 *
 * DER ROTE SCHLUESSEL BLEIBT, DER RISSIGE NICHT. Das Original zieht nur vom rissigen einen
 * ab -- der heile laesst sich beliebig oft benutzen. Das ist kein Versehen: der heile ist
 * das seltene Stueck, der rissige die Wegwerffassung.
 *
 * NUR AN DEN SEITEN. Ein Klick auf Ober- oder Unterseite tut nichts; das Zimmer wird stets
 * waagerecht ausgehoben, vier Bloecke hinter dem Schluesselloch.
 *
 * NICHT UEBERNOMMEN: die Errungenschaft achRedRoom. Der Port hat kein
 * Errungenschaftssystem.
 */
public final class KeyholeRitual {

    private KeyholeRitual() { }

    /** Hat der Spieler einen passenden Schluessel und die richtige Seite getroffen? */
    public static boolean passt(ItemStack stack, Direction seite) {
        if(seite.getAxis() == Direction.Axis.Y) return false;
        return stack.is(NtmItems.KEY_RED.get()) || stack.is(NtmItems.KEY_RED_CRACKED.get());
    }

    /**
     * Schluessel abnutzen, Tuer setzen, Klang spielen. Die Mitte des Zimmers liegt vier
     * Bloecke hinter dem Schluesselloch und zwei tiefer -- so steht es im Original.
     */
    public static BlockPos vollziehe(Level level, BlockPos pos, Player player, ItemStack stack, Direction seite) {

        if(stack.is(NtmItems.KEY_RED_CRACKED.get())) stack.shrink(1);

        /* Die Tuer steht, wo das Schluesselloch war, und zwar eine Stufe tiefer: das
         * Schluesselloch sitzt auf Augenhoehe, die Tuer auf dem Boden. Sie blickt den
         * Spieler an, also in die Richtung der angeklickten Seite. */
        setzeTuer(level, pos.below(), seite);

        level.playSound(null, pos, NtmSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

        return pos.relative(seite.getOpposite(), 4).below(2);
    }

    private static void setzeTuer(Level level, BlockPos unten, Direction blick) {
        DoorBlock tuer = (DoorBlock) NtmBlocks.DOOR_RED.get();
        BlockState untere = tuer.defaultBlockState()
                .setValue(DoorBlock.FACING, blick)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        level.setBlock(unten, untere, 3);
        level.setBlock(unten.above(), untere.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 3);
    }
}
