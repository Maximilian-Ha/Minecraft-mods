package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderTau.
 *
 * Die Tau-Kanone. Zwei Teile: das Gehaeuse und der Rotor, der sich um seinen eigenen Punkt
 * dreht -- beim Aufladen immer schneller, bis er nicht mehr zu sehen ist.
 *
 * DER RUECKSTOSS WIRKT DOPPELT: einmal als Verschiebung nach hinten und einmal als Kippen um
 * einen Punkt zwei Einheiten vor der Waffe, beides aus derselben Zahl. Deshalb wirkt der
 * aufgeladene Schuss so viel schwerer als der gewoehnliche, obwohl nur eine Zahl groesser ist.
 *
 * DIE RUECKSEITENFLAECHEN WERDEN MITGEZEICHNET (disableCull): das Modell des Originals ist
 * innen offen, und ohne das saehe man beim Aufladen durch den Rotor hindurch ins Leere.
 *
 * ABWEICHUNG: setupModTable ist nicht uebernommen -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild. Das ist im Port ueberall so.
 */
public class ItemRenderTau extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.75F * offset, -1.75F * offset, 3.5F * offset,
                -1.75F * offset, -1.75F * offset, 3.5F * offset);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        RenderSystem.setShaderTexture(0, ResourceManager.TAU_TEX);
        float scale = 0.75F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] rotate = HbmAnimations.getRelevantTransformation("ROTATE");

        RenderContext.translate(0F, -1F, -4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 1F, 4F);

        RenderContext.translate(0F, 0F, recoil[2]);

        RenderContext.translate(0F, 0F, -2F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[2] * 5F));
        RenderContext.translate(0F, 0F, 2F);

        RenderSystem.disableCull();
        ResourceManager.tau.renderPart("Body");

        RenderContext.pushPose();
        RenderContext.translate(0F, -0.25F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(rotate[2]));
        RenderContext.translate(0F, 0.25F, 0F);
        ResourceManager.tau.renderPart("Rotor");
        RenderContext.popPose();
        RenderSystem.enableCull();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 2.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 1F, 2F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 2F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.25F, 0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.TAU_TEX);
        RenderSystem.disableCull();
        ResourceManager.tau.renderAll();
        RenderSystem.enableCull();
    }
}
