package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderStinger.
 *
 * Der Stinger. Er ist die einzige Waffe, die beim Zielen GANZ VERSCHWINDET: wer voll angelegt
 * hat, sieht nur noch das Fadenkreuz und den Aufschaltbalken. Deshalb steht am Anfang der
 * ersten Person eine Rueckkehr, keine Rechnung.
 *
 * DIE RAKETE IST NICHT SEIN EIGENES MODELL. Sie kommt aus dem Panzerschreck -- dasselbe Teil,
 * dieselbe Textur --, und deshalb wird mittendrin die Textur gewechselt. Das steht so im
 * Original.
 *
 * DIE SCHRIFT AM ROHR ist ein Scherz des Originals und bleibt stehen: "Not accurate", rot,
 * schraeg am Rohr. Sie leuchtet aus sich selbst.
 *
 * ABWEICHUNG: setupModTable ist nicht uebernommen -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild. Das ist im Port ueberall so.
 */
public class ItemRenderStinger extends ItemRenderWeaponBase {

    private static final String LABEL = "Not accurate";

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aiming = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1D - aiming * 0.5D);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -3.75F * offset, -9F * offset, -3.5F * offset,
                -2.625F * offset, -6.5F, -8.5F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        /* Voll angelegt sieht man die Waffe gar nicht mehr -- nur noch Fadenkreuz und Balken. */
        if(GunBaseNTItem.prevAimingProgress >= 1F && GunBaseNTItem.aimingProgress >= 1F) return;

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();

        RenderSystem.setShaderTexture(0, ResourceManager.STINGER_TEX);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] reload = HbmAnimations.getRelevantTransformation("RELOAD");
        float[] rocket = HbmAnimations.getRelevantTransformation("ROCKET");

        RenderContext.translate(0F, -1F, -1F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 1F, 1F);

        /* Wie beim Panzerschreck dreht das Rohr um die Schulter des Schuetzen. */
        RenderContext.translate(0F, -4F, -3F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(reload[0]));
        RenderContext.translate(0F, 4F, 3F);

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
        ResourceManager.stinger.renderAll();
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderSystem.setShaderTexture(0, ResourceManager.PANZERSCHRECK_TEX);
        RenderContext.translate(rocket[0], rocket[1] + 3.5F, rocket[2] - 3F);
        ResourceManager.panzerschreck.renderPart("Rocket");
        RenderContext.popPose();

        this.renderLabel(buffer);

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, 6.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 150, 7.5F);
        RenderContext.popPose();
    }

    /** Der Scherz am Rohr. */
    private void renderLabel(MultiBufferSource buffer) {

        Font font = Minecraft.getInstance().font;
        float textScale = 0.04F;

        FullBright.enable();
        RenderContext.pushPose();
        RenderContext.translate(0.025F, -0.5F, (font.width(LABEL) / 2F) * textScale - 3F);
        RenderContext.scale(textScale, -textScale, textScale);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XN.rotationDegrees(45F));
        font.drawInBatch(LABEL, 0F, 0F, 0xff0000, false,
                RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
        RenderContext.popPose();
        FullBright.disable();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, -2.5F, -3.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.0625F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(225F));
        RenderContext.translate(0.25F, -2.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.STINGER_TEX);
        ResourceManager.stinger.renderAll();

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
        RenderContext.translate(0F, 0F, 2F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(buffer, shot, 150, 7.5F);
        RenderContext.popPose();
    }
}
