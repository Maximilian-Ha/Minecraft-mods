package com.hbm.render.entity.mob;

import com.hbm.entity.mob.MaskMan;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderMaskMan samt ModelMaskMan.
 *
 * Sechs Teile: Rumpf, zwei Beine, zwei Arme und der Kopf. Alle schwingen aus derselben Zahl --
 * dem Gangschwung, den das Original aus cos(Schritt / 2 + PI) mal 1,4 mal Schrittweite
 * berechnet und in Grad umrechnet. Rumpf und Arme nehmen davon ein Zehntel und ein Viertel,
 * die Beine das Ganze, jeweils gegenlaeufig.
 *
 * DER KOPF DREHT SICH FUER SICH. Er folgt dem Blick, nicht dem Rumpf -- deshalb steht er in
 * einer eigenen Ebene und bekommt den Kopfwinkel gegengerechnet.
 *
 * AB DER HALBEN LEBENSENERGIE IST DER KOPF WEG. Unter der Haelfte zeichnet das Original
 * statt des Kopfes einen Schaedel -- und daneben, mit eigener Textur, einen Zettel: IOU.
 *
 * DAS MODELL STEHT AUF DEM KOPF UND QUER. Das Original dreht es um 180 Grad um X, setzt es
 * anderthalb Bloecke tiefer und dreht es eine Vierteldrehung nach links; diese drei Schritte
 * stehen hier, wie sie dort stehen.
 */
@OnlyIn(Dist.CLIENT)
public class MaskManRenderer extends EntityRenderer<MaskMan> {

    public MaskManRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.0F;
        this.shadowStrength = 0.0F;
    }

    @Override
    public void render(MaskMan mann, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        float rumpfWinkel = Mth.rotLerp(partialTick, mann.yBodyRotO, mann.yBodyRot);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F - rumpfWinkel));

        RenderContext.mulPose(Axis.XP.rotationDegrees(180F));
        RenderContext.translate(0F, -1.5F, 0F);
        RenderContext.mulPose(Axis.YN.rotationDegrees(90F));

        RenderSystem.setShaderTexture(0, ResourceManager.MASKMAN_TEX);

        double schwung = Math.toDegrees(
                Mth.cos(mann.walkAnimation.position(partialTick) / 2F + (float) Math.PI) * 1.4F
                * mann.walkAnimation.speed(partialTick));

        RenderContext.mulPose(Axis.XP.rotationDegrees((float) (schwung * -0.1)));
        ResourceManager.maskman.renderPart("Torso");

        glied("LLeg", -0.5F, 1.75F, -0.5F, (float) schwung);
        glied("RLeg", -0.5F, 1.75F, 0.5F, (float) -schwung);
        glied("LArm", -0.5F, 3.75F, -1.5F, (float) (schwung * 0.25));
        glied("RArm", -0.5F, 3.75F, 1.5F, (float) (schwung * -0.25));

        /* Der Kopf: er folgt dem Blick. Das Original rechnet dazu den Kopfwinkel gegen, der
         * ihm als f3 hereingereicht wird. */
        float kopfWinkel = Mth.rotLerp(partialTick, mann.yHeadRotO, mann.yHeadRot) - rumpfWinkel;

        RenderContext.pushPose();
        RenderContext.translate(0.5F, 4F, 0F);
        RenderContext.mulPose(Axis.YN.rotationDegrees(kopfWinkel));

        if(mann.getHealth() >= mann.getMaxHealth() / 2) {
            ResourceManager.maskman.renderPart("Head");
        } else {
            ResourceManager.maskman.renderPart("Skull");
            RenderSystem.setShaderTexture(0, ResourceManager.IOU_TEX);
            ResourceManager.maskman.renderPart("IOU");
        }

        RenderContext.popPose();
        RenderContext.end();
    }

    /** Ein Glied an seinem Aufhaengepunkt, um die Z-Achse geschwenkt. */
    private static void glied(String teil, float x, float y, float z, float winkel) {
        RenderContext.pushPose();
        RenderContext.translate(x, y, z);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(winkel));
        ResourceManager.maskman.renderPart(teil);
        RenderContext.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MaskMan mann) {
        return ResourceManager.MASKMAN_TEX;
    }
}
