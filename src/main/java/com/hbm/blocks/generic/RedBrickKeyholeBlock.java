package com.hbm.blocks.generic;

import com.hbm.blocks.NtmBlocks;
import com.hbm.world.RedRoomGenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import com.hbm.registry.NtmCriteria;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockRedBrickKeyhole.
 *
 * DAS ZWEITE SCHLUESSELLOCH, und es sitzt in der Wand des ersten Zimmers. Es sieht aus wie
 * ein roter Ziegel -- darum erbt es von diesem, samt seiner Seiten-Eigenschaft -- und traegt
 * auf der gemusterten Seite statt des Ziegelbilds den Schlitz.
 *
 * DAHINTER LIEGT DAS SCHWARZE ZIMMER: schmucklos, ringsum schlichter Untergrund, ohne
 * Fackeln und ohne Lava, dafuer mit bis zu fuenf Sockeln -- in der Mitte die Tontafel, die
 * ein Sockelrezept zeigt, und vier ringsum mit je halber Wahrscheinlichkeit, auf denen die
 * Geheimstuecke liegen.
 *
 * UND DIE WAND, DURCH DIE MAN EINTRITT, BLEIBT OFFEN. Das ist der sichtbarste Unterschied
 * zum ersten Zimmer und im Original eine eigene Abfrage je Wand.
 *
 * ER GIBT SICH ALS ROTER ZIEGEL AUS, bis hin zum Mittelklick.
 */
public class RedBrickKeyholeBlock extends RedBrickBlock {

    public RedBrickKeyholeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(NtmBlocks.BRICK_RED.get());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {

        Direction seite = hit.getDirection();
        if(!KeyholeRitual.passt(stack, seite)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;

        BlockPos mitte = KeyholeRitual.vollziehe(level, pos, player, stack, seite);
        /* Offen bleibt die Wand, durch die man kommt -- von der Zimmermitte aus gesehen
         * liegt sie in der Richtung, aus welcher der Spieler geklickt hat. */
        RedRoomGenerator.generateBrickRoom(level, mitte, seite);

        /* Derselbe Erfolg wie am steinernen Schluesselloch -- BlockRedBrickKeyhole Z. 91. */
        if(player instanceof ServerPlayer spieler) NtmCriteria.marke(spieler, "red_room");

        return ItemInteractionResult.CONSUME;
    }
}
