package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderCoilgun.
 *
 * Die Spulenkanone. Ein einziges Modellteil, das sich als Ganzes bewegt: beim Schuss kippt es
 * nach hinten (RECOIL), beim Nachladen zur anderen Seite (RELOAD). Beide Busse liefern nur
 * EINEN Wert, den der Renderer zugleich als Verschiebung und als Drehung auswertet -- deshalb
 * sieht die Bewegung nach Ausklinken aus, obwohl nichts einzeln beweglich ist.
 *
 * KEIN MUENDUNGSFEUER: sie schiesst mit Magnetfeldern, da brennt nichts. Das Orchester, das sie
 * sich mit der NI4NI teilt, prueft das ausdruecklich.
 *
 * ABWEICHUNG: das Original bindet am Anfang die Textur der Leuchtpistole und ueberschreibt sie
 * drei Zeilen spaeter mit der eigenen -- ein Rest, der nichts tut. Er ist nicht uebernommen.
 * setupModTable fehlt wie bei allen Waffen des Ports.
 */
public class ItemRenderCoilgun extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aiming = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1D - aiming * 0.33D);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.25F * offset, -1.5F * offset, 2.5F * offset,
                0F, -7.5F / 8F, 1F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        float scale = 0.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.YP.rotationDegrees(-90F));

        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        RenderContext.translate(-1.5F - recoil[0] * 0.5F, 0F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(recoil[0] * 45F));
        RenderContext.translate(1.5F, 0F, 0F);

        float[] reload = HbmAnimations.getRelevantTransformation("RELOAD");
        RenderContext.translate(-2.5F, 0F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(reload[0] * -45F));
        RenderContext.translate(2.5F, 0F, 0F);

        RenderSystem.setShaderTexture(0, ResourceManager.COILGUN_TEX);
        ResourceManager.coilgun.renderAll();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 3F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 0.25F, 1.25F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 4F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.25F, -0.25F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderContext.mulPose(Axis.YP.rotationDegrees(-90F));
        RenderSystem.setShaderTexture(0, ResourceManager.COILGUN_TEX);
        ResourceManager.coilgun.renderAll();
    }
}
