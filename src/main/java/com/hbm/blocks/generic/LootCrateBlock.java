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

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.generic.BlockCrate.
 *
 * Eine Beutekiste faellt wie Kies und laesst sich nur mit der Brechstange oeffnen; mit
 * jedem anderen Werkzeug schlaegt man sie ab und bekommt die Kiste selbst zurueck.
 * Beim Oeffnen fallen drei bis fuenf Gegenstaende aus der jeweiligen Liste.
 *
 * ALLE FUENF FASSUNGEN stehen: Blei, Metall, Nachschub, Waffen und die rote Kiste.
 *
 * DIE WAFFENKISTE ZAEHLT ANDERS als die uebrigen: aus ihr fallen nur ein bis zwei Stueck, denn
 * eine Waffe ist mehr wert als eine Handvoll Erz. In einem von hundert Faellen kippt sie
 * stattdessen fuenfundzwanzig aus -- ein Scherz des Originals, der hier unveraendert bleibt.
 *
 * DIE ROTE KISTE ZIEHT GAR NICHT. Sie kippt jeden ihrer vierzehn Eintraege genau einmal aus --
 * das Original wuerfelt zwar erst eine Anzahl und zieht, wirft das Ergebnis danach aber weg
 * (BlockCrate.java:168 ff.). Hier steht gleich das Ergebnis. Im Original hat sie keinen
 * Kreativ-Reiter (setCreativeTab(null)); das bleibt so.
 */
public class LootCrateBlock extends FallingBlock {

    public static final MapCodec<LootCrateBlock> CODEC = simpleCodec(properties -> new LootCrateBlock(properties, Art.BLEI));

    /** Woraus diese Kiste zieht. */
    public enum Art { BLEI, METALL, NACHSCHUB, WAFFEN, ROT }

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
            for(ItemStack stueck : this.inhalt(level.getRandom())) Block.popResource(level, pos, stueck);

            level.removeBlock(pos, false);
            level.playSound(null, pos, NtmSoundEvents.CRATE_BREAK.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        }

        return ItemInteractionResult.SUCCESS;
    }

    /**
     * Was beim Oeffnen herausfaellt. Vier der fuenf Kisten wuerfeln eine Anzahl und ziehen so
     * oft aus ihrer Liste; die rote kippt ihre ganze Liste aus.
     */
    private List<ItemStack> inhalt(RandomSource zufall) {
        if(this.art == Art.ROT) return CrateLoot.alleRot();

        List<ItemStack> inhalt = new ArrayList<>();
        int anzahl = this.anzahl(zufall);
        for(int i = 0; i < anzahl; i++) inhalt.add(this.zieh(zufall));
        return inhalt;
    }

    /** Wie viele Stuecke eine ziehende Kiste hergibt. */
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
            /* Die rote Kiste zieht nicht; inhalt() geht fuer sie den anderen Weg. Der Zweig
             * steht hier, damit eine kuenftige sechste Art nicht stillschweigend leer zieht. */
            case ROT -> throw new IllegalStateException("Aus der roten Kiste wird nicht gezogen");
        };
    }
}
