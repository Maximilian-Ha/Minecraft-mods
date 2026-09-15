package com.hbm.items.machine;

import com.hbm.blocks.machine.ReactorResearchBlock;
import com.hbm.items.component.NtmDataComponents;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.items.machine.ItemReactorSensor.
 *
 * Der Fuehler merkt sich einen Forschungsreaktor; im Reaktorpult eingelegt, sagt er diesem, welchen
 * Reaktor es regeln soll.
 *
 * ABWEICHUNG: die gemerkte Position liegt in einer Datenkomponente statt in losem NBT, und es wird
 * gleich der Kern gespeichert statt des angeklickten Blocks -- das Original sucht den Kern erst im
 * Pult, was dasselbe Ergebnis hat, aber jeden Tick erneut.
 */
public class ReactorSensorItem extends Item {

    public ReactorSensorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if(player == null) return InteractionResult.PASS;
        if(!(level.getBlockState(pos).getBlock() instanceof ReactorResearchBlock reactor)) return InteractionResult.PASS;

        BlockPos corePos = reactor.findCore(level, pos);
        if(corePos == null) return InteractionResult.PASS;

        if(!level.isClientSide) {
            stack.set(NtmDataComponents.REACTOR_LINK.get(), corePos);
            player.displayClientMessage(Component.translatable("item.hbmsntm.reactor_sensor.linked",
                    corePos.getX(), corePos.getY(), corePos.getZ()).withStyle(ChatFormatting.GREEN), true);
            level.playSound(null, pos, NtmSoundEvents.TECH_BOOP.get(), SoundSource.PLAYERS, 1F, 1F);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {

        BlockPos target = stack.get(NtmDataComponents.REACTOR_LINK.get());

        if(target == null) {
            components.add(Component.translatable("item.hbmsntm.reactor_sensor.empty").withStyle(ChatFormatting.GRAY));
        } else {
            components.add(Component.literal("x: " + target.getX()).withStyle(ChatFormatting.GRAY));
            components.add(Component.literal("y: " + target.getY()).withStyle(ChatFormatting.GRAY));
            components.add(Component.literal("z: " + target.getZ()).withStyle(ChatFormatting.GRAY));
        }
    }
}
