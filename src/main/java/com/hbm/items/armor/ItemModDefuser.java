package com.hbm.items.armor;

import java.util.List;

import com.hbm.entity.mob.CreeperDefuser;
import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: ItemModDefuser -- der goldene Seitenschneider.
 *
 * Ein Ruestungsaufsatz, der jede Sekunde alle Creeper im Umkreis von fuenf Bloecken
 * entschaerft. Jeder von ihnen laesst dabei seine Zuendschnur fallen.
 *
 * DIE SEKUNDE IST DIE DES ORIGINALS: worldObj.getTotalWorldTime() % 20. Sie steht hier,
 * weil modUpdate in jedem Takt laeuft und die Umkreissuche nicht zwanzigmal je Sekunde
 * gemacht werden soll.
 */
public class ItemModDefuser extends ItemArmorMod {

    /** Der Umkreis des Originals: boundingBox.expand(5, 5, 5). */
    public static final double REICHWEITE = 5D;

    public ItemModDefuser(Properties properties) {
        super(properties, ArmorModHandler.EXTRA, true, true, true, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.defuser").withStyle(ChatFormatting.YELLOW));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.defuser.short")).append(Component.literal(")"))
                .withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {

        if(entity.level().isClientSide) return;
        if(entity.level().getGameTime() % 20 != 0) return;

        AABB umkreis = entity.getBoundingBox().inflate(REICHWEITE);

        for(Creeper creeper : entity.level().getEntitiesOfClass(Creeper.class, umkreis)) {
            CreeperDefuser.entschaerfe(creeper, entity, true);
        }
    }
}
