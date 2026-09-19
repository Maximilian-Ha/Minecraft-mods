package com.hbm.blocks.generic;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import com.hbm.items.food.ConserveItem.ConserveType;
import com.hbm.items.food.DrinkItem.DrinkType;
import com.hbm.registry.NtmSoundEvents;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockCanCrate.
 *
 * Die Dosenkiste. Fuenf bis acht Stueck liegen drin, jedes einzeln und gleich wahrscheinlich
 * aus einem Topf gezogen, in dem alle Konserven, die acht Dosengetraenke und der Pudding
 * stehen. Dieselbe Sorte kann also mehrfach kommen -- das Original wuerfelt ebenso.
 *
 * Geoeffnet wird sie wie jede Kiste des Mods: nur mit der Brechstange.
 *
 * ABWEICHUNG, wie der Topf entsteht: das Original zaehlt zehn einzelne Gegenstaende auf, weil
 * jede Dose bei ihm ein eigener Gegenstand ist. Im Port sind die Getraenke Spielarten eines
 * einzigen Gegenstands, deshalb steht hier die Liste der acht Spielarten. Gezogen wird aus
 * demselben Topf mit denselben Gewichten.
 */
public class CanCrateBlock extends Block {

    public static final MapCodec<CanCrateBlock> CODEC = simpleCodec(CanCrateBlock::new);

    /** Die acht Dosengetraenke. Die uebrigen Spielarten des Gegenstands stehen in Flaschen
     *  oder Bechern und gehoeren nicht in eine Dosenkiste. */
    private static final DrinkType[] DOSEN = {
            DrinkType.SMART, DrinkType.CREATURE, DrinkType.REDBOMB, DrinkType.MRSUGAR,
            DrinkType.OVERCHARGE, DrinkType.LUNA, DrinkType.BREEN, DrinkType.BEPIS };

    public CanCrateBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<CanCrateBlock> codec() { return CODEC; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {

        if(!stack.is(NtmItems.CROWBAR.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(!level.isClientSide) {
            for(ItemStack stueck : wuerfleInhalt(level.getRandom())) Block.popResource(level, pos, stueck);

            level.removeBlock(pos, false);
            level.playSound(null, pos, NtmSoundEvents.CRATE_BREAK.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        }

        return ItemInteractionResult.SUCCESS;
    }

    public static List<ItemStack> wuerfleInhalt(RandomSource zufall) {

        List<ItemStack> topf = topf();
        List<ItemStack> beute = new ArrayList<>();

        int anzahl = 5 + zufall.nextInt(4);
        for(int i = 0; i < anzahl; i++) beute.add(topf.get(zufall.nextInt(topf.size())).copy());

        return beute;
    }

    private static List<ItemStack> topf() {

        List<ItemStack> topf = new ArrayList<>();

        for(ConserveType sorte : ConserveType.values()) topf.add(MetaHelper.newStack(NtmItems.CANNED_CONSERVE.get(), sorte));
        for(DrinkType sorte : DOSEN) topf.add(MetaHelper.newStack(NtmItems.DRINK.get(), sorte));
        topf.add(new ItemStack(NtmItems.PUDDING.get()));

        return topf;
    }
}
