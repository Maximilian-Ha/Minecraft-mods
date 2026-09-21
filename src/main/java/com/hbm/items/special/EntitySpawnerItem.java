package com.hbm.items.special;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.mob.Duck;
import com.hbm.entity.mob.Ufo;
import com.hbm.items.NtmItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class EntitySpawnerItem extends Item {

    public EntitySpawnerItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());

        if (!level.isClientSide) {
            Entity entity = spawnCreature(level, ctx.getItemInHand(), pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);

            if (entity != null) {
                if (entity instanceof LivingEntity living) {

                    Component name = ctx.getItemInHand().getHoverName();
                    living.setCustomName(name);
                }

                if (player != null && !player.getAbilities().instabuild) {
                    ctx.getItemInHand().shrink(1);
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            BlockHitResult hit = getPlayerPOVHitResult(level, player, net.minecraft.world.level.ClipContext.Fluid.ANY);

            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = hit.getBlockPos();

                if (level.getFluidState(pos).isEmpty()) {
                    return InteractionResultHolder.pass(stack);
                }

                Entity entity = spawnCreature(level, stack, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);

                if (entity != null) {
                    if (entity instanceof LivingEntity living) {

                        Component name = stack.getHoverName();
                        living.setCustomName(name);
                    }

                    if (player != null && !player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private Entity spawnCreature(Level level, ItemStack stack, double x, double y, double z) {
        Entity entity = null;

        /* Der Kopterruf fehlt weiter: den Jagdkopter gibt es im Port nicht. Die beiden
         * anderen stehen seit den Runden 282 und 283 und werden hier nachgezogen. */
        if (stack.is(NtmItems.SPAWN_WORM.get())) {
            entity = NtmEntityTypes.BOT_PRIME_HEAD.get().create(level);
        }
        if (stack.is(NtmItems.SPAWN_UFO.get())) {
            Ufo ufo = NtmEntityTypes.UFO.get().create(level);
            if (ufo != null) {
                /* Das UFO kommt von oben herein und braucht seine hundert Ticks Anlauf,
                 * ehe es das erste Mal nach einem Ziel sucht -- so steht es im Original. */
                ufo.anlaufZeit(100);
                y += 35;
                entity = ufo;
            }
        }
        if (stack.is(NtmItems.SPAWN_DUCK.get())) {
            Duck duck = NtmEntityTypes.DUCK.get().create(level);
            if (duck != null) {
                duck.moveTo(x, y, z, level.random.nextFloat() * 360F, 0.0F);
                level.addFreshEntity(duck);
            }
        }

        if (entity != null) {
            if (entity instanceof LivingEntity living) {
                entity.moveTo(x, y, z, Mth.wrapDegrees(level.random.nextFloat() * 360.0F), 0.0F);
                living.setYHeadRot(living.getYRot());
                living.setYBodyRot(living.getYRot());
                level.addFreshEntity(entity);
            }
        }

        return entity;
    }

    /**
     * Der Hinweistext des Wurmrufs. Er stand hier seit Runde 282 auskommentiert, weil es den
     * Gegenstand noch nicht gab; seit Runde 288 gibt es ihn.
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        if(stack.is(NtmItems.SPAWN_WORM.get())) {
            tooltip.add(Component.translatable("item.hbmsntm.spawn_worm.desc0"));
            tooltip.add(Component.translatable("item.hbmsntm.spawn_worm.desc1"));
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("item.hbmsntm.spawn_worm.desc2"));
            tooltip.add(Component.translatable("item.hbmsntm.spawn_worm.desc3"));
        }
    }
}
