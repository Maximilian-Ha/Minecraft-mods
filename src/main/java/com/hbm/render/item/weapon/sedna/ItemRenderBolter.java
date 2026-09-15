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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderBolter.
 *
 * Der Bolter. Er ist die einzige Waffe des Ports mit einem Zaehlwerk: die Zahl der verbleibenden
 * Bolzen steht in roten Ziffern auf dem Gehaeuse, in voller Helligkeit, damit sie auch im Dunkeln
 * lesbar bleibt.
 *
 * Beweglich sind das Magazin (MAG) und die ganze Waffe, die zum Nachladen nach hinten kippt
 * (TILT). Die dritte Achse von MAG dient als Schalter: steht sie auf 1, ist das Magazin leer.
 *
 * ABWEICHUNG: das Zaehlwerk liest den Magazinstand im Original mit einem null-Behaelter aus, was
 * fuer alle Magazine ausser dem Gurt genuegt. Hier steht der Rucksack des Spielers -- das ist
 * dasselbe Ergebnis und haelte auch, wenn der Bolter je einen Gurt bekaeme.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderBolter extends ItemRenderWeaponBase {

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
                -1.5F * offset, -2F * offset, 2.5F * offset,
                0F, -10.5F / 8F, 1.25F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        RenderSystem.setShaderTexture(0, ResourceManager.BOLTER_TEX);

        float scale = 0.5F;
        RenderContext.scale(scale, scale, scale);

        /* Das Modell zeigt nach hinten; es muss erst umgedreht werden. */
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[0] * 5F));
        RenderContext.translate(0F, 0F, recoil[0]);

        float[] tilt = HbmAnimations.getRelevantTransformation("TILT");
        RenderContext.translate(0F, tilt[0], 3F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(tilt[0] * 35F));
        RenderContext.translate(0F, 0F, -3F);

        ResourceManager.bolter.renderPart("Body");

        float[] mag = HbmAnimations.getRelevantTransformation("MAG");
        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, 5F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(mag[0] * 60F * (mag[2] == 1 ? 2.5F : 1F)));
        RenderContext.translate(0F, 0F, -5F);
        ResourceManager.bolter.renderPart("Mag");
        if(mag[2] != 1) ResourceManager.bolter.renderPart("Bullet");
        RenderContext.popPose();

        /* Das Zaehlwerk. Es sitzt schraeg auf dem Gehaeuse und leuchtet aus sich selbst. */
        Font font = Minecraft.getInstance().font;
        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        String count = String.valueOf(gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack)
                .getAmount(stack, Minecraft.getInstance().player.getInventory()));

        FullBright.enable();
        RenderContext.pushPose();
        float textScale = 0.04F;
        RenderContext.translate(0.025F - (font.width(count) / 2F) * textScale, 2.11F, 2.91F);
        RenderContext.scale(textScale, -textScale, textScale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(45F));
        font.drawInBatch(count, 0F, 0F, 0xff0000, false,
                RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
        RenderContext.popPose();
        FullBright.disable();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 2.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, -0.75F, 1.25F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 2.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.25F, -0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
        RenderSystem.setShaderTexture(0, ResourceManager.BOLTER_TEX);
        ResourceManager.bolter.renderAll();
    }
}
