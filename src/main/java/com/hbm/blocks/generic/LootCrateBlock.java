package com.hbm.blocks.generic;

import com.hbm.inventory.CrateLoot;
import com.hbm.items.NtmItems;
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
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockCrate.
 *
 * Eine Beutekiste faellt wie Kies und laesst sich nur mit der Brechstange oeffnen; mit
 * jedem anderen Werkzeug schlaegt man sie ab und bekommt die Kiste selbst zurueck.
 * Beim Oeffnen fallen drei bis fuenf Gegenstaende aus der jeweiligen Liste.
 *
 * VIER DER FUENF FASSUNGEN stehen: Blei, Metall, Nachschub und Waffen. Es fehlt nur die rote
 * Kiste, der die meisten Sonderwaffen fehlen -- siehe CrateLoot.
 *
 * DIE WAFFENKISTE ZAEHLT ANDERS als die uebrigen: aus ihr fallen nur ein bis zwei Stueck, denn
 * eine Waffe ist mehr wert als eine Handvoll Erz. In einem von hundert Faellen kippt sie
 * stattdessen fuenfundzwanzig aus -- ein Scherz des Originals, der hier unveraendert bleibt.
 */
public class LootCrateBlock extends FallingBlock {

    public static final MapCodec<LootCrateBlock> CODEC = simpleCodec(properties -> new LootCrateBlock(properties, Art.BLEI));

    /** Woraus diese Kiste zieht. */
    public enum Art { BLEI, METALL, NACHSCHUB, WAFFEN }

    private final Art art;

    public LootCrateBlock(Properties properties, Art art) {
        super(properties);
        this.art = art;
    }

    @Override public MapCodec<LootCrateBlock> codec() { return CODEC; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {

        if(!stack.is(NtmItems.CROWBAR.get())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if(!level.isClientSide) {
            RandomSource zufall = level.getRandom();
            int anzahl = this.anzahl(zufall);

            for(int i = 0; i < anzahl; i++) {
                Block.popResource(level, pos, this.zieh(zufall));
            }

            level.removeBlock(pos, false);
            level.playSound(null, pos, NtmSoundEvents.CRATE_BREAK.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        }

        return ItemInteractionResult.SUCCESS;
    }

    /** Wie viele Stuecke diese Kiste hergibt. */
    private int anzahl(RandomSource zufall) {
        if(this.art != Art.WAFFEN) return 3 + zufall.nextInt(3);
        if(zufall.nextInt(100) == 34) return 25;
        return 1 + zufall.nextInt(2);
    }

    private ItemStack zieh(RandomSource zufall) {
        return switch(this.art) {
            case BLEI -> CrateLoot.ziehBlei(zufall);
            case METALL -> CrateLoot.ziehMetall(zufall);
            case NACHSCHUB -> CrateLoot.ziehNachschub(zufall);
            case WAFFEN -> CrateLoot.ziehWaffen(zufall);
        };
    }
}
