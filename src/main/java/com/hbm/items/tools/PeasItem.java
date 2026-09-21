package com.hbm.items.tools;

import com.hbm.entity.mob.Quackos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.tool.ItemPeas.
 *
 * Erbsen. Der einzige Weg, eine Quackos wieder loszuwerden: ein Rechtsklick laesst jede im
 * Umkreis von fuenfzig Bloecken verschwinden. Warum ausgerechnet Erbsen, sagt das Original
 * nicht.
 */
public class PeasItem extends Item {

    public PeasItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if(!player.getAbilities().instabuild) stack.shrink(1);

        if(!level.isClientSide) {
            AABB box = player.getBoundingBox().inflate(50D);
            for(Quackos duck : level.getEntitiesOfClass(Quackos.class, box)) duck.despawn();
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
