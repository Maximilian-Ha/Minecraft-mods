package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.ResourceManager;
import com.hbm.particle.SpentCasing;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderCongoLake.
 *
 * Der Congo Lake. Fast alles kommt aus der Animationsdatei -- Gehaeuse, Pumpe, Visier, Buegel und
 * Abzugsbuegel haengen unmittelbar an ihren Kanaelen. Von Hand kommt nur das Visier, das sich
 * beim Anlegen aus dem Weg klappt, und die Granate im Rohr.
 *
 * DIE GRANATE IM ROHR wird in der Farbe der geladenen Munition gezeichnet: die Huelsenfarbe
 * faerbt den Koerper, die zweite Farbe die Spitze. So sieht der Schuetze, was im Rohr steckt.
 * Beim Betrachten mit leerem Rohr faellt sie weg.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderCongoLake extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

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
                -1.5F * offset, -2F * offset, 1.25F * offset,
                0F, -10F / 8F, 0.25F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.CONGOLAKE_TEX);

        float scale = 0.5F;
        RenderContext.scale(scale, scale, scale);

        HbmAnimations.applyRelevantTransformation("Gun");
        ResourceManager.congolake.renderPart("Gun");

        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("Pump");
        ResourceManager.congolake.renderPart("Pump");
        RenderContext.popPose();

        /* Das Visier klappt beim Anlegen nach unten weg -- es sitzt sonst im Blickfeld. */
        RenderContext.pushPose();
        float aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        HbmAnimations.applyRelevantTransformation("Sight");
        RenderContext.translate(0F, 2.125F, 3F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(aimingProgress * -90F));
        RenderContext.translate(0F, -2.125F, -3F);
        ResourceManager.congolake.renderPart("Sight");
        RenderContext.popPose();

        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("Loop");
        ResourceManager.congolake.renderPart("Loop");
        RenderContext.popPose();

        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("GuardOuter");
        ResourceManager.congolake.renderPart("GuardOuter");
        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("GuardInner");
        ResourceManager.congolake.renderPart("GuardInner");
        RenderContext.popPose();
        RenderContext.popPose();

        RenderContext.pushPose();
        IMagazine mag = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);

        if(GunBaseNTItem.getLastAnim(stack, 0) != GunAnimation.INSPECT
                || mag.getAmount(stack, Minecraft.getInstance().player.getInventory()) > 0) {

            RenderSystem.setShaderTexture(0, ResourceManager.CASINGS_TEX);
            HbmAnimations.applyRelevantTransformation("Shell");

            SpentCasing casing = mag.getCasing(stack, Minecraft.getInstance().player.getInventory());
            int[] colors = casing != null ? casing.getColors() : new int[] { SpentCasing.COLOR_CASE_40MM };

            setColorFrom(colors[0]);
            ResourceManager.congolake.renderPart("Shell");

            setColorFrom(colors.length > 1 ? colors[1] : colors[0]);
            ResourceManager.congolake.renderPart("ShellFore");

            RenderContext.setColor(1F, 1F, 1F, 1F);
        }
        RenderContext.popPose();

        float smokeScale = 0.25F;

        /* Der Rauch sitzt an der Muendung, folgt aber der Gehaeusedrehung zurueck. */
        RenderContext.pushPose();
        RenderContext.translate(0F, 1.75F, 4.25F);
        float[] transform = HbmAnimations.getRelevantTransformation("Gun");
        RenderContext.mulPose(Axis.ZN.rotationDegrees(transform[5]));
        RenderContext.mulPose(Axis.YN.rotationDegrees(transform[4]));
        RenderContext.mulPose(Axis.XN.rotationDegrees(transform[3]));
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 1F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.75F, 4.25F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(0.5F, 0.5F, 0.5F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 150, 7.5F);
        RenderContext.popPose();
    }

    private static void setColorFrom(int rgb) {
        RenderContext.setColor(((rgb >> 16) & 0xFF) / 255F, ((rgb >> 8) & 0xFF) / 255F, (rgb & 0xFF) / 255F, 1F);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        RenderContext.translate(0F, -2.5F, 4F);
        float scale = 2.5F;
        RenderContext.scale(scale, scale, scale);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 2.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0F, -1.25F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {
        RenderSystem.setShaderTexture(0, ResourceManager.CONGOLAKE_TEX);
        ResourceManager.congolake.renderPart("Gun");
        ResourceManager.congolake.renderPart("Pump");
        ResourceManager.congolake.renderPart("Sight");
        ResourceManager.congolake.renderPart("Loop");
        ResourceManager.congolake.renderPart("GuardOuter");
        ResourceManager.congolake.renderPart("GuardInner");
    }
}
