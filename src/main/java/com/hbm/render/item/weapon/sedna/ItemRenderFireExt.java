package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.factory.XFactoryTool;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.ItemRenderFireExt.
 *
 * EIN MODELLTEIL, DREI ANSTRICHE. Welcher Tank eingesetzt ist, sieht man am Lack: Wasser rot,
 * Schaum cremefarben, Sand gelb. Das Modell selbst ist fuer alle drei dasselbe.
 *
 * Der Loescher hat keinen Bewegungssatz -- das Original gibt ihm keinen, und er braucht
 * keinen. Deshalb steht hier kein einziger Aufruf von HbmAnimations: es gaebe nichts
 * abzufragen.
 *
 * ABWEICHUNG: im Original dreht sich der Loescher im Inventarbild (System.currentTimeMillis).
 * Der Port zeichnet Gegenstaende im Inventar ueber setupInv still, wie bei allen uebrigen
 * Waffen auch.
 */
public class ItemRenderFireExt extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return -0.5F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0.5F, -0.5F, -0.5F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(80F));
        float scale = 0.35F;
        RenderContext.scale(scale, scale, scale);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {
        RenderSystem.setShaderTexture(0, this.getTexture(stack));
        ResourceManager.fireext.renderAll();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 0.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(20F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(10F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(15F));
        RenderContext.translate(0.75F, -2.75F, 0.5F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 0.3F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {
        RenderSystem.setShaderTexture(0, this.getTexture(stack));
        ResourceManager.fireext.renderAll();
    }

    /** Der Lack richtet sich nach dem Tank, der im Loescher steckt. */
    private ResourceLocation getTexture(ItemStack stack) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        MagazineFullReload mag = (MagazineFullReload) gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
        Object geladen = mag.getType(stack, null);

        if(geladen == XFactoryTool.fext_foam) return ResourceManager.FIREEXT_FOAM_TEX;
        if(geladen == XFactoryTool.fext_sand) return ResourceManager.FIREEXT_SAND_TEX;
        return ResourceManager.FIREEXT_TEX;
    }
}
