package com.hbm.render.item.weapon.sedna;

import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderFlamer.
 *
 * Der Flammenwerfer. Drei Teile bewegen sich einzeln: das Rohr, der Tank auf dem Ruecken und
 * die Anzeige am Tank.
 *
 * DIE ANZEIGE ZEIGT WIRKLICH DEN FUELLSTAND. Ihr Zeiger dreht sich ueber 270 Grad, von -135
 * bei leerem Tank bis +135 bei vollem; die Zahl dafuer kommt unmittelbar aus dem Magazin.
 *
 * DIESELBE KLASSE TRAEGT ALLE DREI WAFFEN -- Flammenwerfer, Topaz und Daybreaker. Sie
 * unterscheiden sich in der Textur, die der Konstruktor bekommt, und darin, dass nur der
 * Daybreaker den Hitzeschild traegt.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderFlamer extends ItemRenderWeaponBase {

    public final ResourceLocation texture;

    public ItemRenderFlamer(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.33);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.5F * offset, -1.5F * offset, 2.75F * offset,
                0F, -4.625F / 8F, 0.25F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, this.texture);

        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] rotate = HbmAnimations.getRelevantTransformation("ROTATE");

        RenderContext.translate(0F, 2F, -6F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(equip[0]));
        RenderContext.translate(0F, -2F, 6F);

        RenderContext.translate(0F, 1F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(rotate[2]));
        RenderContext.translate(0F, -1F, 0F);

        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("Gun");
        ResourceManager.flamethrower.renderPart("Gun");
        if(hasShield(stack)) ResourceManager.flamethrower.renderPart("HeatShield");
        RenderContext.popPose();

        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("Tank");
        ResourceManager.flamethrower.renderPart("Tank");
        RenderContext.popPose();

        /* Der Zeiger der Fuellstandsanzeige: 270 Grad ueber den ganzen Tank. */
        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("Gauge");
        IMagazine mag = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
        float fuellung = (float) mag.getAmount(stack, Minecraft.getInstance().player.getInventory()) / (float) mag.getCapacity(stack);
        RenderContext.translate(1.25F, 1.25F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-135F + fuellung * 270F));
        RenderContext.translate(-1.25F, -1.25F, 0F);
        ResourceManager.flamethrower.renderPart("Gauge");
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, -3F, 4F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.25F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-1F, 1F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {
        RenderSystem.setShaderTexture(0, this.texture);
        ResourceManager.flamethrower.renderPart("Gun");
        ResourceManager.flamethrower.renderPart("Tank");
        ResourceManager.flamethrower.renderPart("Gauge");
        if(hasShield(stack)) ResourceManager.flamethrower.renderPart("HeatShield");
    }

    /** Nur der Daybreaker traegt den Hitzeschild ueber dem Rohr. */
    public boolean hasShield(ItemStack stack) {
        return stack.getItem() == NtmItems.GUN_FLAMER_DAYBREAKER.get();
    }
}
