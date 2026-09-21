package com.hbm.items.weapon;

import com.hbm.entity.grenade.DisperserCanister;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.machine.FluidTankItem;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.items.weapon.ItemDisperser.
 *
 * Ein Behaelter, den man wirft. Er traegt seine Fluessigkeit wie der Kanister im Metawert,
 * und beim Rechtsklick fliegt er als DisperserCanister davon und laesst eine Wolke zurueck.
 *
 * ZWEI BAUFORMEN teilen sich diese Klasse, genau wie im Original: die Verteilerkanne, die
 * jede verspruehbare Fluessigkeit nimmt, und die Glyphidendruese, die nur Schwefelsaeure
 * oder Pheromon enthaelt und nicht hergestellt, sondern erbeutet wird. Der Schalter
 * entscheidet nur ueber die Beschriftung und darueber, was im Flug gezeichnet wird.
 *
 * DER NAME STEHT ANDERSHERUM: das Original setzt bei der Druese den Fluessigkeitsnamen VOR
 * den Gegenstandsnamen ("Sulfuric Acid Gland") und bei der Kanne dahinter ("Disperser
 * Canister: Sulfuric Acid"). Uebernommen ueber zwei Uebersetzungsschluessel.
 */
public class DisperserItem extends FluidTankItem {

    /** true fuer die Glyphidendruese, false fuer die Verteilerkanne. */
    private final boolean druese;

    public DisperserItem(Properties properties, boolean druese) {
        super(properties);
        this.druese = druese;
    }

    public boolean isGland() {
        return this.druese;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if(!level.isClientSide) {
            DisperserCanister kanne = new DisperserCanister(level);
            kanne.setFluid(Fluids.fromID(MetaHelper.getMeta(stack)));
            kanne.setGland(this.druese);
            kanne.werfen(player);
            level.addFreshEntity(kanne);
        }

        if(!player.isCreative()) stack.shrink(1);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
