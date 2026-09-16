package com.hbm.items.armor;

import api.hbm.item.IGasMask;
import com.hbm.items.IHelmetOverlayItem;
import com.hbm.main.NuclearTechMod;
import com.hbm.render.util.RenderScreenOverlay;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ArmorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: ArmorGasMask, ArmorHazmat und ArmorHazmatMask in einer Klasse.
 *
 * Alle drei unterscheiden sich nur in zwei Dingen: was die Maske trotz eingesetztem
 * Filter NICHT abhaelt, und welches Bild sie ueber den Schirm legt. Beides kommt hier
 * aus dem Bauaufruf, deshalb braucht es dafuer keine eigenen Unterklassen.
 */
public class GasMaskItem extends ArmorItem implements IGasMask, IHelmetOverlayItem {

    /** Der Vorsatz der vollen Schutzhaube: eine gleichbleibende, leichte Truebung. */
    public static final ResourceLocation[] OVERLAY_HAZMAT = {
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_hazmat.png")
    };

    /*
     * Die beiden Leitern. Je abgenutzter die Maske, desto weiter hinten wird
     * gegriffen und desto schlechter sieht der Traeger.
     */
    public static final ResourceLocation[] OVERLAY_GASMASK = {
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_gasmask_0.png"),
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_gasmask_1.png"),
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_gasmask_2.png"),
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_gasmask_3.png"),
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_gasmask_4.png"),
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_gasmask_5.png")
    };

    public static final ResourceLocation[] OVERLAY_GOGGLES = {
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_goggles_0.png"),
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_goggles_1.png"),
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_goggles_2.png"),
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_goggles_3.png"),
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_goggles_4.png"),
            NuclearTechMod.withDefaultNamespace("textures/misc/overlay_goggles_5.png")
    };

    /** Was der Filter zwar koennte, die Maske aber nicht durchlaesst. */
    private final List<HazardClass> blacklist;
    /**
     * Der Vorsatz. Ein einzelnes Bild bleibt immer gleich; eine Leiter aus mehreren
     * Bildern wird mit zunehmendem Verschleiss der Maske durchgeschaltet -- so wird
     * die Sicht aus einer abgenutzten Maske immer truebender. Leer heisst kein Vorsatz.
     */
    private final ResourceLocation[] overlay;

    public GasMaskItem(Holder<ArmorMaterial> material, Properties properties, List<HazardClass> blacklist, ResourceLocation... overlay) {
        super(material, Type.HELMET, properties.stacksTo(1));
        this.blacklist = List.copyOf(blacklist);
        this.overlay = overlay;
    }

    @Override
    public ArrayList<HazardClass> getBlacklist(ItemStack stack, LivingEntity entity) {
        return new ArrayList<>(this.blacklist);
    }

    @Override
    public ItemStack getFilter(ItemStack stack, LivingEntity entity) {
        return ArmorUtil.getGasMaskFilter(entity.level(), stack);
    }

    /** Nur echte Filtereinsaetze passen ins Gewinde -- sonst liesse sich alles hineinschrauben. */
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

    /** Geduckter Rechtsklick wirft den Filter wieder aus. */
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

        if(!this.blacklist.isEmpty()) {
            components.add(Component.translatable("hazard.neverProtects").withStyle(ChatFormatting.RED));
            for(HazardClass clazz : this.blacklist) {
                components.add(Component.literal(" -").append(Component.translatable(clazz.unlocalizedMessage)).withStyle(ChatFormatting.DARK_RED));
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderHelmetOverlay(GuiGraphics guiGraphics, ItemStack stack) {

        if(this.overlay.length == 0) return;

        int index = 0;

        if(this.overlay.length > 1 && stack.getMaxDamage() > 0) {
            index = (int) ((double) stack.getDamageValue() / (double) stack.getMaxDamage() * this.overlay.length);
            index = Math.min(index, this.overlay.length - 1);
        }

        RenderScreenOverlay.renderHelmetOverlay(guiGraphics, this.overlay[index]);
    }

    /** Was eine gewoehnliche Maske trotz Filter nicht abhaelt: aetzende Gase greifen die Haut an. */
    public static List<HazardClass> standardBlacklist() {
        return List.of(HazardClass.GAS_BLISTERING);
    }

    /** Die Monoxidmaske ist nur ein Kohlenfilter und laesst alles andere durch. */
    public static List<HazardClass> monoxideBlacklist() {
        return List.of(HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING, HazardClass.BACTERIA);
    }
}
