package com.hbm.render.entity.mob;

import com.hbm.entity.mob.glyphid.Glyphid;
import com.hbm.main.NuclearTechMod;
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
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderGlyphid samt der inneren Klasse
 * ModelGlyphid.
 *
 * Ein OBJ-Modell mit neunzehn Teilen, und fast jedes davon bewegt sich. Der Gang steht auf
 * VIER verschobenen Wellen desselben Schrittzaehlers -- null, ein Viertel, ein halbes und
 * drei Achtel Umlauf --, und aus ihnen ergibt sich das Wechselspiel von sechs Beinen und zwei
 * Greifarmen.
 *
 * DIE PANZERUNG WIRD GEZEICHNET, WAS DA IST: fuenf Teile, fuenf Bits. Bit null ist die Front,
 * eins und zwei sind die Flanken, drei und vier die Armschienen. Wer ihm ein Stueck
 * abgeschlagen hat, sieht das sofort.
 *
 * DER BISS kommt aus dem Schlagfortschritt: der Oberkiefer hebt sich, die beiden Seitenkiefer
 * klappen auseinander, und der ganze Kopf legt sich dabei zur Seite.
 *
 * DAS MODELL STEHT IM OBJ AUF DEM KOPF -- das Original dreht es um hundertachtzig Grad um die
 * Tiefenachse und schiebt es anderthalb Bloecke nach unten. Uebernommen, wie es dasteht.
 *
 * NICHT UEBERNOMMEN: der zweite Zeichendurchgang fuer die verseuchte Haut. Das Original legt
 * sie als shouldRenderPass mit abgeschaltetem Alphatest darueber; auf 1.21 waere das eine
 * eigene Lage (RenderLayer), und die braucht einen Modelltyp, den dieser OBJ-Zeichner nicht
 * hat. Der Port zeichnet die verseuchte Haut stattdessen ALS Haut -- dieselbe Textur, nur
 * ohne Schichtung.
 */
@OnlyIn(Dist.CLIENT)
public class GlyphidRenderer extends EntityRenderer<Glyphid> {

    public static final ResourceLocation INFESTED_TEX = NuclearTechMod.withDefaultNamespace("textures/entity/glyphid_infestation.png");

    public GlyphidRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.0F;
        this.shadowStrength = 0.0F;
    }

    @Override
    public void render(Glyphid glyphid, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        double biss = glyphid.getAttackAnim(partialTick);
        byte panzer = glyphid.getArmorBits();
        double groesse = glyphid.getGlyphidScale();

        float schritt = glyphid.walkAnimation.position(partialTick);

        double cy0 = Math.sin(schritt % (Math.PI * 2));
        double cy1 = Math.sin(schritt % (Math.PI * 2) - Math.PI * 0.5);
        double cy2 = Math.sin(schritt % (Math.PI * 2) - Math.PI);
        double cy3 = Math.sin(schritt % (Math.PI * 2) - Math.PI * 0.75);

        double beissen = Mth.clamp(Math.sin(biss * Math.PI * 2 - Math.PI * 0.5), 0, 1) * 20;
        double kopfNeigung = Math.sin(biss * Math.PI) * 30;

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        /* Der Koerper richtet sich nach dem Rumpf, nicht nach dem Kopf. */
        float rumpf = Mth.rotLerp(partialTick, glyphid.yBodyRotO, glyphid.yBodyRot);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F - rumpf));

        /* Das Modell liegt im OBJ auf dem Kopf. */
        RenderContext.mulPose(Axis.XP.rotationDegrees(180F));
        RenderContext.translate(0F, -1.5F, 0F);
        RenderContext.scale((float) groesse, (float) groesse, (float) groesse);

        RenderSystem.setShaderTexture(0, this.getTextureLocation(glyphid));

        ResourceManager.glyphid.renderPart("Body");
        if((panzer & (1 << 0)) > 0) ResourceManager.glyphid.renderPart("ArmorFront");
        if((panzer & (1 << 1)) > 0) ResourceManager.glyphid.renderPart("ArmorLeft");
        if((panzer & (1 << 2)) > 0) ResourceManager.glyphid.renderPart("ArmorRight");

        /// DER LINKE GREIFARM ///
        RenderContext.pushPose();
        RenderContext.translate(0.25F, 0.625F, 0.0625F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(10F));
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) (35 + cy1 * 20)));
        RenderContext.translate(-0.25F, -0.625F, -0.0625F);
        ResourceManager.glyphid.renderPart("ArmLeftUpper");
        RenderContext.translate(0.25F, 0.625F, 0.4375F);
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) (-75 - cy1 * 20 + cy0 * 20)));
        RenderContext.translate(-0.25F, -0.625F, -0.4375F);
        ResourceManager.glyphid.renderPart("ArmLeftMid");
        RenderContext.translate(0.25F, 0.625F, 0.9375F);
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) (90 - cy0 * 45)));
        RenderContext.translate(-0.25F, -0.625F, -0.9375F);
        ResourceManager.glyphid.renderPart("ArmLeftLower");
        if((panzer & (1 << 3)) > 0) ResourceManager.glyphid.renderPart("ArmLeftArmor");
        RenderContext.popPose();

        /// DER RECHTE GREIFARM ///
        RenderContext.pushPose();
        RenderContext.translate(-0.25F, 0.625F, 0.0625F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(-10F));
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) (35 + cy2 * 20)));
        RenderContext.translate(0.25F, -0.625F, -0.0625F);
        ResourceManager.glyphid.renderPart("ArmRightUpper");
        RenderContext.translate(-0.25F, 0.625F, 0.4375F);
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) (-75 - cy2 * 20 + cy3 * 20)));
        RenderContext.translate(0.25F, -0.625F, -0.4375F);
        ResourceManager.glyphid.renderPart("ArmRightMid");
        RenderContext.translate(-0.25F, 0.625F, 0.9375F);
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) (90 - cy3 * 45)));
        RenderContext.translate(0.25F, -0.625F, -0.9375F);
        ResourceManager.glyphid.renderPart("ArmRightLower");
        if((panzer & (1 << 4)) > 0) ResourceManager.glyphid.renderPart("ArmRightArmor");
        RenderContext.popPose();

        /// DIE KIEFER ///
        RenderContext.pushPose();
        RenderContext.translate(0F, 0.5F, 0.25F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees((float) kopfNeigung));
        RenderContext.translate(0F, -0.5F, -0.25F);

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.5F, 0.25F);
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) -beissen));
        RenderContext.translate(0F, -0.5F, -0.25F);
        ResourceManager.glyphid.renderPart("JawTop");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.5F, 0.25F);
        RenderContext.mulPose(Axis.YP.rotationDegrees((float) beissen));
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) beissen));
        RenderContext.translate(0F, -0.5F, -0.25F);
        ResourceManager.glyphid.renderPart("JawLeft");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.5F, 0.25F);
        RenderContext.mulPose(Axis.YP.rotationDegrees((float) -beissen));
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) beissen));
        RenderContext.translate(0F, -0.5F, -0.25F);
        ResourceManager.glyphid.renderPart("JawRight");
        RenderContext.popPose();

        RenderContext.popPose();

        /// DIE SECHS BEINE, drei Paare, das mittlere gegenlaeufig ///
        double steppy = 15;
        double bend = 60;

        for(int i = 0; i < 3; i++) {

            double c0 = cy0 * (i == 1 ? -1 : 1);
            double c1 = cy1 * (i == 1 ? -1 : 1);

            RenderContext.pushPose();
            RenderContext.translate(0F, 0.25F, 0F);
            RenderContext.mulPose(Axis.YP.rotationDegrees((float) (i * 30 - 15 + c0 * 7.5)));
            RenderContext.mulPose(Axis.ZP.rotationDegrees((float) (steppy + c1 * steppy)));
            RenderContext.translate(0F, -0.25F, 0F);
            ResourceManager.glyphid.renderPart("LegLeftUpper");
            RenderContext.translate(0.5625F, 0.25F, 0F);
            RenderContext.mulPose(Axis.ZP.rotationDegrees((float) (-bend - c1 * steppy)));
            RenderContext.translate(-0.5625F, -0.25F, 0F);
            ResourceManager.glyphid.renderPart("LegLeftLower");
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 0.25F, 0F);
            RenderContext.mulPose(Axis.YP.rotationDegrees((float) (i * 30 - 45 + c0 * 7.5)));
            RenderContext.mulPose(Axis.ZP.rotationDegrees((float) (-steppy + c1 * steppy)));
            RenderContext.translate(0F, -0.25F, 0F);
            ResourceManager.glyphid.renderPart("LegRightUpper");
            RenderContext.translate(-0.5625F, 0.25F, 0F);
            RenderContext.mulPose(Axis.ZP.rotationDegrees((float) (bend - c1 * steppy)));
            RenderContext.translate(0.5625F, -0.25F, 0F);
            ResourceManager.glyphid.renderPart("LegRightLower");
            RenderContext.popPose();
        }

        RenderContext.end();

        super.render(glyphid, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Glyphid glyphid) {
        return glyphid.getSubtype() == Glyphid.TYPE_INFECTED ? INFESTED_TEX : glyphid.getSkin();
    }
}
