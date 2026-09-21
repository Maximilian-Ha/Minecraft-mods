package com.hbm.items.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemFertilizer.
 *
 * Knochenmehl, aber grosszuegig: EIN Klick duengt den ganzen Wuerfel von drei mal drei mal
 * drei Bloecken um die angeklickte Stelle. Der angeklickte Block wird dabei ERZWUNGEN -- bei
 * ihm entfaellt der Wurf, der sonst entscheidet, ob das Knochenmehl anschlaegt. Die
 * sechsundzwanzig Nachbarn muessen wuerfeln wie sonst auch.
 *
 * VERBRAUCHT WIRD HOECHSTENS EINER, egal wie viele Pflanzen angeschlagen haben.
 *
 * EINE KLEINE ABWEICHUNG IM RUECKGABEWERT: das Original gibt aus onItemUse IMMER false
 * zurueck, auch wenn etwas gewachsen ist -- auf 1.7.10 hat das nur zur Folge, dass die Hand
 * nicht ausschlaegt. Auf 1.21 entscheidet der Rueckgabewert ueber Handschlag UND darueber, ob
 * der Klick als erledigt gilt; der Port gibt darum sidedSuccess zurueck, wenn etwas
 * angeschlagen hat, und sonst PASS.
 *
 * NICHT UEBERNOMMEN: useFertillizer, die zweite oeffentliche Methode des Originals. Sie
 * bedient den Werfer (Dispenser), und der Port hat kein Werferverhalten -- keine einzige
 * Stelle registriert eines. Eine Methode ohne Aufrufer waere toter Code; kommt das
 * Werferverhalten, kommt sie mit.
 *
 * NICHT UEBERNOMMEN: das BonemealEvent. Das Original fragt damit andere Mods, ob sie das
 * Duengen uebernehmen wollen. Auf 1.21 traegt NeoForge dieses Ereignis an anderer Stelle,
 * und der Port wuerde hier entweder doppelt fragen oder eine fremde Rechnung nachbauen. Was
 * wachsen darf, entscheidet hier die Pflanze selbst.
 */
public class FertilizerItem extends Item {

    public FertilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Level level = context.getLevel();
        BlockPos mitte = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if(context.getPlayer() != null && !context.getPlayer().mayUseItemAt(mitte, context.getClickedFace(), stack)) {
            return InteractionResult.PASS;
        }

        boolean etwasGewachsen = false;

        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                for(int z = -1; z <= 1; z++) {

                    BlockPos pos = mitte.offset(x, y, z);
                    boolean erzwungen = x == 0 && y == 0 && z == 0;

                    if(duengen(level, pos, erzwungen)) {
                        etwasGewachsen = true;
                        if(!level.isClientSide) level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
                    }
                }
            }
        }

        if(etwasGewachsen && context.getPlayer() != null && !context.getPlayer().isCreative()) {
            stack.shrink(1);
        }

        return etwasGewachsen ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    /**
     * Duengt eine einzelne Stelle. erzwungen ueberspringt den Wurf, der sonst entscheidet, ob
     * das Knochenmehl anschlaegt -- das Original tut das beim angeklickten Block.
     */
    public static boolean duengen(Level level, BlockPos pos, boolean erzwungen) {

        BlockState state = level.getBlockState(pos);

        if(!(state.getBlock() instanceof BonemealableBlock pflanze)) return false;
        if(!pflanze.isValidBonemealTarget(level, pos, state)) return false;

        if(level instanceof ServerLevel server) {
            if(erzwungen || pflanze.isBonemealSuccess(level, level.random, pos, state)) {
                pflanze.performBonemeal(server, level.random, pos, state);
            }
        }

        return true;
    }
}
