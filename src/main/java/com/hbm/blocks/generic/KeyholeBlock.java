package com.hbm.blocks.generic;

import com.hbm.world.RedRoomGenerator;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockKeyhole.
 *
 * DAS STEINERNE SCHLUESSELLOCH. Es sieht aus wie Stein -- oben und unten ganz, an den Seiten
 * mit einem Schlitz -- und steht in den Bauwerken des Originals herum, ohne dass ihm
 * jemand ansieht, was es ist. Wer den roten Schluessel daran haelt, hebt dahinter ein
 * Zimmer aus und bekommt eine Tuer.
 *
 * ER GIBT SICH ALS STEIN AUS, bis hin zum Mittelklick: getCloneItemStack liefert Stein,
 * nicht ihn selbst. Das ist Absicht des Originals und bleibt so.
 *
 * WAS IM ZIMMER LIEGT, entscheidet RedRoomGenerator -- mit einem Zwanzigstel eine
 * Beutekiste mit einem ganzen Panzerruestungssatz, sonst ein Sockel mit einem Beutestueck.
 */
public class KeyholeBlock extends Block {

    public static final MapCodec<KeyholeBlock> CODEC = simpleCodec(KeyholeBlock::new);

    public KeyholeBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<KeyholeBlock> codec() { return CODEC; }

    /** Er gibt sich als Stein aus -- so steht es im Original. */
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(Items.STONE);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {

        Direction seite = hit.getDirection();
        if(!KeyholeRitual.passt(stack, seite)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(level.isClientSide) return ItemInteractionResult.SUCCESS;

        BlockPos mitte = KeyholeRitual.vollziehe(level, pos, player, stack, seite);
        RedRoomGenerator.generateStoneRoom(level, mitte);

        return ItemInteractionResult.CONSUME;
    }
}
