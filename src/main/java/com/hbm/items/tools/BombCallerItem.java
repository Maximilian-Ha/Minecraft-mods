package com.hbm.items.tools;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.entity.logic.Bomber;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.ICustomItemModelRegister;
import com.hbm.items.IMetaItem;
import com.hbm.lib.Library;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.world.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;

import java.util.List;

public class BombCallerItem extends Item implements IMetaItem, ICustomItemModelRegister {

    public BombCallerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if(!level.isClientSide) {
            BlockHitResult bhr = Library.rayTrace(player, 500, 1);

            int x = bhr.getBlockPos().getX();
            int y = bhr.getBlockPos().getY();
            int z = bhr.getBlockPos().getZ();

            // Nummerierung wie im Original (ItemBombCaller.java:54-62). Der Port hatte die
            // Atombombe auf 2 gelegt -- dort liegt dort aber das Chlor, die Atombombe auf 4.
            // Die Luecke bei 3 und alles ab 5 sind die Varianten, deren Bomber-Voreinstellungen
            // im Port noch fehlen (Agent Orange, Stinger, Boxcar, PC).
            Bomber bomber = switch(MetaHelper.getMeta(stack)) {
                case 1 -> Bomber.statFacNapalm(level, x, y, z);
                case 2 -> Bomber.statFacChlorine(level, x, y, z);
                case 4 -> Bomber.statFacABomb(level, x, y, z);
                default -> Bomber.statFacCarpet(level, x, y, z);
            };

            WorldUtil.loadAndAddFreshEntity(bomber);

            player.sendSystemMessage(Component.translatable("item.hbmsntm.obj_bomb_caller.call"));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), NtmSoundEvents.TECH_BLEEP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

            stack.consume(1, player);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        for(String s : ITooltipProvider.getDescription(stack)) components.add(Component.translatable(s).withStyle(ChatFormatting.GRAY));
        switch(MetaHelper.getMeta(stack)) {
            case 0 -> components.add(Component.translatable("item.hbmsntm.obj_bomb_caller.desc0").withStyle(ChatFormatting.GRAY));
            case 1 -> components.add(Component.translatable("item.hbmsntm.obj_bomb_caller.desc1").withStyle(ChatFormatting.GRAY));
            case 2 -> components.add(Component.translatable("item.hbmsntm.obj_bomb_caller.desc2").withStyle(ChatFormatting.GRAY));
        };
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return MetaHelper.getMeta(stack) >= 2;
    }

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {
        for(int i : new int[] { 0, 1, 2, 4 }) {
            stacks.add(MetaHelper.newStack(item, 1, i));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerItemModel(ItemModelProvider provider, ResourceLocation modelLocation) {
        provider.basicItem(this);
    }
}
