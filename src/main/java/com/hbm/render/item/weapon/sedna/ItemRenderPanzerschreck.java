package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderPanzerschreck.
 *
 * Der Panzerschreck. Drei Teile, drei Bewegungen: EQUIP kippt die ganze Waffe beim Ziehen,
 * RELOAD dreht das Rohr zur Seite, und ROCKET fuehrt die Rakete von hinten hinein.
 *
 * ABWEICHUNG, das Schutzschild: das Original laesst es per Aufsatz abnehmen und fragt dafuer
 * XWeaponModManager.hasUpgrade(stack, 0, ID_NO_SHIELD). Diesen Aufsatz gibt es im Port nicht --
 * die Abfrage haette nur einen Zweig, der nie genommen wird. Das Schild steht deshalb fest.
 * Sobald der Aufsatz nachkommt, gehoert die Abfrage hierher zurueck.
 *
 * ABWEICHUNG: setupModTable ist nicht uebernommen -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild. Das ist im Port ueberall so.
 */
public class ItemRenderPanzerschreck extends ItemRenderWeaponBase {

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
                -2.75F * offset, -2F * offset, 2.5F * offset,
                -0.9375F, -9.25F / 8F, 0.25F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.PANZERSCHRECK_TEX);

        float scale = 1.25F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] reload = HbmAnimations.getRelevantTransformation("RELOAD");
        float[] rocket = HbmAnimations.getRelevantTransformation("ROCKET");

        RenderContext.translate(0F, -1F, -1F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 1F, 1F);

        /* Das Rohr dreht sich um einen Punkt weit hinten und unten -- die Schulter des Schuetzen. */
        RenderContext.translate(0F, -4F, -3F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(reload[0]));
        RenderContext.translate(0F, 4F, 3F);

        ResourceManager.panzerschreck.renderPart("Tube");
        ResourceManager.panzerschreck.renderPart("Shield");

        RenderContext.pushPose();
        RenderContext.translate(rocket[0], rocket[1], rocket[2]);
        ResourceManager.panzerschreck.renderPart("Rocket");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, 6.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 150, 7.5F);
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 3F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 0.5F, 1F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.5F, 0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.PANZERSCHRECK_TEX);
        /*
         * Nur Rohr und Schild: die Rakete gehoert zur Bewegung, und ausserhalb der ersten Person
         * gibt es keine -- das Original laesst sie hier ebenfalls weg.
         */
        ResourceManager.panzerschreck.renderPart("Tube");
        ResourceManager.panzerschreck.renderPart("Shield");

        if(living == null) return;
        if(displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) return;

        long shot;
        float shotRand = 0F;

        if(living == Minecraft.getInstance().player) {
            GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
            shot = gun.lastShot[0];
            shotRand = gun.shotRand;
        } else {
            shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
            if(shot < 0) return;
        }

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, 6.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(buffer, shot, 150, 7.5F);
        RenderContext.popPose();
    }
}
