package com.hbm.items.armor;

import api.hbm.item.IGasMask;
import com.hbm.render.model.armor.ModelM65Head;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ArmorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorLiquidatorMask.
 *
 * Die Haube des Bleianzugs. Sie ist zugleich Helm und Gasmaske, und als VOLLE HAUBE haelt sie
 * mit Filter alles ab, was der Filter kann -- es gibt keine Gefahrenklasse, die sie trotz
 * Filter durchliesse. Das Original sagt das mit einem Einzeiler: "full hood has no
 * restrictions".
 *
 * AM KOERPER traegt sie das M65-Kopfmodell, dasselbe wie die M65-Gasmaske. Gezeichnet wird es
 * mit der Schichttextur ihres Werkstoffs -- darum hat der Helm einen eigenen Werkstoff, dessen
 * Schicht auf liquidator_helmet.png zeigt, waehrend die drei uebrigen Teile sich den Werkstoff
 * mit den gewoehnlichen Ruestungsschichten teilen. Der Grund steht in Runde 206.
 */
public class ArmorLiquidatorMaskItem extends ArmorLiquidatorItem implements IGasMask {

    public ArmorLiquidatorMaskItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties);
    }

    /** Die volle Haube kennt keine Ausnahme -- was der Filter kann, haelt sie ab. */
    @Override
    public ArrayList<HazardClass> getBlacklist(ItemStack stack, LivingEntity entity) {
        return new ArrayList<>();
    }

    @Override
    public ItemStack getFilter(ItemStack stack, LivingEntity entity) {
        return ArmorUtil.getGasMaskFilter(entity.level(), stack);
    }

    @Override
    public boolean isFilterApplicable(ItemStack stack, LivingEntity entity, ItemStack filter) {
        return filter.getItem() instanceof FilterItem;
    }

    @Override
    public void installFilter(ItemStack stack, LivingEntity entity, ItemStack filter) {
        ArmorUtil.installGasMaskFilter(entity.level(), stack, filter);
    }

    @Override
    public void damageFilter(ItemStack stack, LivingEntity entity, int damage) {
        ArmorUtil.damageGasMaskFilter(entity.level(), stack, damage);
    }

    /** Geduckter Rechtsklick wirft den Filter wieder aus -- wie bei jeder Maske des Ports. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if(player.isShiftKeyDown()) {

            ItemStack filter = this.getFilter(stack, player);

            if(!filter.isEmpty()) {
                if(!level.isClientSide) {
                    ArmorUtil.removeFilter(stack);
                    if(!player.getInventory().add(filter)) player.drop(filter, false);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        ArmorUtil.addGasMaskTooltip(context.level(), stack, components);
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private ModelM65Head m65;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {

                if(slot != EquipmentSlot.HEAD) return original;

                EntityModelSet modelle = Minecraft.getInstance().getEntityModels();
                if(m65 == null) m65 = new ModelM65Head(modelle.bakeLayer(ModelM65Head.LAYER));
                m65.copyHeadFrom(original);
                m65.living = living;
                return m65;
            }
        });
    }
}
