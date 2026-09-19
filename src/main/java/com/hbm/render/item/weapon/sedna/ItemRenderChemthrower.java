package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderChemthrower.
 *
 * Der Chemiewerfer. Vier Teile, von denen sich nur eines bewegt: der Zeiger der
 * Fuellstandsanzeige am Tank. Er laeuft ueber 270 Grad, aber ANDERSHERUM als beim
 * Flammenwerfer -- voll ist hier -135 Grad, nicht +135.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderChemthrower extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -2.5F * offset, -2.5F * offset, 2.5F * offset,
                0F, -4.375F / 8F, 1F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.CHEMTHROWER_TEX);

        float scale = 0.75F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");

        RenderContext.translate(0F, -2F, -4F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 2F, 4F);

        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        ResourceManager.chemthrower.renderPart("Gun");
        ResourceManager.chemthrower.renderPart("Hose");
        ResourceManager.chemthrower.renderPart("Nozzle");

        IMagazine<?> mag = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
        float fuellung = (float) mag.getAmount(stack, Minecraft.getInstance().player.getInventory()) / (float) mag.getCapacity(stack);

        RenderContext.translate(0F, 0.875F, 1.75F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(135F - fuellung * 270F));
        RenderContext.translate(0F, -0.875F, -1.75F);
        ResourceManager.chemthrower.renderPart("Gauge");
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 2F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, -2.5F, 0.5F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 2F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0.875F, 0F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {
        RenderSystem.setShaderTexture(0, ResourceManager.CHEMTHROWER_TEX);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        ResourceManager.chemthrower.renderPart("Gun");
        ResourceManager.chemthrower.renderPart("Hose");
        ResourceManager.chemthrower.renderPart("Nozzle");
        ResourceManager.chemthrower.renderPart("Gauge");
    }
}
