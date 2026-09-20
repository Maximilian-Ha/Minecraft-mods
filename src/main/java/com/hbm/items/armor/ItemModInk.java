package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

/**
 * Portiert aus 1.7.10: ItemModInk.
 *
 * Die Tinte. Jeder zehnte Treffer prallt ab -- und laesst Blumen fallen.
 *
 * DIE BLUMEN: das Original wirft red_flower mit einer zufaelligen Metadatenzahl von null
 * bis acht ab, und in einem von zehn Faellen zusaetzlich eine yellow_flower. In 1.21 sind
 * das neun eigene Bloecke; sie stehen hier in derselben Reihenfolge wie die Metadaten des
 * Originals, und die gelbe ist der Loewenzahn.
 */
public class ItemModInk extends ItemArmorMod {

    /** Die neun Blumen der Metadaten null bis acht, in der Reihenfolge des Originals. */
    private static final net.minecraft.world.level.block.Block[] BLUMEN = {
            Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET,
            Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP,
            Blocks.OXEYE_DAISY };

    public ItemModInk(Properties properties) {
        super(properties, ArmorModHandler.EXTRA, true, true, true, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("armorMod.ink").withStyle(ChatFormatting.LIGHT_PURPLE));
        components.add(Component.translatable("armorMod.ink.flowers").withStyle(ChatFormatting.LIGHT_PURPLE));
        components.add(Component.empty());
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public void addDesc(List<Component> list, ItemStack stack, ItemStack armor) {
        list.add(Component.literal("  ").append(stack.getHoverName())
                .append(Component.literal(" (")).append(Component.translatable("armorMod.ink")).append(Component.literal(")"))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public void modDamage(LivingDamageEvent.Pre event, ItemStack armor) {

        LivingEntity traeger = event.getEntity();
        if(traeger.getRandom().nextInt(10) != 0) return;

        event.setNewDamage(0F);

        if(traeger.level().isClientSide) return;

        if(traeger.getRandom().nextInt(10) == 0) traeger.spawnAtLocation(Blocks.DANDELION);
        traeger.spawnAtLocation(BLUMEN[traeger.getRandom().nextInt(BLUMEN.length)]);
    }
}
