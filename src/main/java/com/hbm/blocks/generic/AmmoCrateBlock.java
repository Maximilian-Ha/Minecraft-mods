package com.hbm.blocks.generic;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.factory.GunFactory.Ammo;
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
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockAmmoCrate.
 *
 * Die Munitionskiste. Anders als die Beutekisten faellt sie nicht und zieht auch nicht aus
 * einer gewichteten Liste: ihr Inhalt steht fest und wird nur ausgewuerfelt. Kronkorken und
 * Stimpaks liegen immer drin, die zwoelf Munitionssorten je mit halber Wahrscheinlichkeit,
 * und mit einem Zehntel noch zwei Superstimpaks.
 *
 * Geoeffnet wird sie wie jede Kiste des Mods: nur mit der Brechstange.
 */
public class AmmoCrateBlock extends Block {

    public static final MapCodec<AmmoCrateBlock> CODEC = simpleCodec(AmmoCrateBlock::new);

    /** Die zwoelf Sorten Handfeuermunition, je 16 bis 32 Stueck. Reihenfolge wie im Original. */
    private static final Ammo[] HANDFEUER = {
            Ammo.P9_SP, Ammo.P9_FMJ,
            Ammo.M357_SP, Ammo.M357_FMJ,
            Ammo.M44_SP, Ammo.M44_FMJ,
            Ammo.R556_SP, Ammo.R556_FMJ,
            Ammo.R762_SP, Ammo.R762_FMJ,
            Ammo.G12, Ammo.G12_SLUG };

    /** Die beiden schweren Sorten, je 2 bis 4 Stueck. */
    private static final Ammo[] SCHWER = { Ammo.G40_HE, Ammo.ROCKET_HE };

    public AmmoCrateBlock(Properties properties) {
        super(properties);
    }

    @Override public MapCodec<AmmoCrateBlock> codec() { return CODEC; }

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

        List<ItemStack> beute = new ArrayList<>();

        beute.add(new ItemStack(NtmItems.CAP_NUKA.get(), 12 + zufall.nextInt(21)));
        beute.add(new ItemStack(NtmItems.SYRINGE_METAL_STIMPAK.get(), 1 + zufall.nextInt(3)));

        for(Ammo sorte : HANDFEUER) {
            if(zufall.nextBoolean()) beute.add(munition(sorte, 16 + zufall.nextInt(17)));
        }

        for(Ammo sorte : SCHWER) {
            if(zufall.nextBoolean()) beute.add(munition(sorte, 2 + zufall.nextInt(3)));
        }

        if(zufall.nextInt(10) == 0) beute.add(new ItemStack(NtmItems.SYRINGE_METAL_SUPER.get(), 2));

        return beute;
    }

    private static ItemStack munition(Ammo sorte, int anzahl) {
        return MetaHelper.newStack(NtmItems.AMMO_STANDARD.get(), anzahl, sorte.ordinal());
    }
}
