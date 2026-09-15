package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderUziAkimbo.
 *
 * Die beidhaendige Uzi. Sie zeichnet zweimal dieselbe Waffe, einmal je Empfaenger: die linke
 * bekommt das gespiegelte Gehaeuse, damit der Auswurf nach aussen zeigt. Jede Haelfte hat ihre
 * eigenen Animationen -- wer links nachlaedt, schiesst rechts weiter.
 *
 * ABWEICHUNG: in dritter Person zeichnet der Port nur die Waffe in der Haupthand. Die zweite
 * haengt im Original an RenderPlayerEvent.Specials, einer Ereignisreihe, die es in 1.21 nicht
 * mehr gibt; der Port hat die zugehoerige Schicht (Arme ausblenden, zweite Waffe an den linken
 * Arm haengen) noch gar nicht. Das gilt fuer alle beidhaendigen Waffen gleichermassen.
 */
public class ItemRenderUziAkimbo extends ItemRenderUzi {

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        float offset = 0.8F;

        for(int side = -1; side <= 1; side += 2) {

            int index = side == -1 ? 0 : 1;

            RenderContext.pushPose();
            RenderSystem.setShaderTexture(0, isSaturnite(stack, index) ? ResourceManager.UZI_SATURNITE_TEX : ResourceManager.UZI_TEX);

            standardAimingTransform(stack,
                    -2.25F * offset * side, -1.5F * offset, 2.5F * offset,
                    0F, -4.375F / 8F, 1F);

            float scale = 0.25F;
            RenderContext.scale(scale, scale, scale);

            renderGunBody(stack, buffer, gun, index, index == 0 ? "GunMirror" : "Gun");
            RenderContext.popPose();
        }
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 0.75F;
        RenderContext.scale(scale, scale, scale);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        if(displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND) {

            /* Mit Schalldaempfer passt das Paar sonst nicht mehr ins Feld. */
            if(hasSilencer(stack, 0) || hasSilencer(stack, 1)) {
                float scale = 0.625F;
                RenderContext.scale(scale, scale, scale);
                RenderContext.translate(0F, 0F, -4F);
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, -2.5F);
            renderStandardGun(stack, 1, "Gun");
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, 2.5F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            renderStandardGun(stack, 0, "GunMirror");
            RenderContext.popPose();
            return;
        }

        renderStandardGun(stack, 1, "Gun");

        if(living != null && !hasSilencer(stack, 1)) renderThirdPersonFlash(stack, buffer, 1);
    }
}
