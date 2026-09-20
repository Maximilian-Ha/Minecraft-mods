package com.hbm.render.entity.mob;

import com.hbm.entity.mob.TeslaCrab;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.BeamType;
import com.hbm.render.util.BeamPronter.WaveType;
import com.hbm.render.util.RenderContext;
import com.hbm.util.Vec3NT;
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
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderTeslaCrab samt ModelTeslaCrab.
 *
 * Der Koerper ist keine Kastensammlung, sondern ein OBJ-Modell mit drei Teilen: Body,
 * Front, Back. Vorder- und Hinterbeine schwenken gegenlaeufig mit dem Schritt, der Koerper
 * steht still. Danach zeichnet der Renderer zu jedem Ziel, das die Krabbe gerade schlaegt,
 * einen Blitz -- dieselben Werte wie bei der Teslaspule.
 *
 * WEIL DAS MODELL AUF DEM KOPF STEHT, dreht das Original es um hundertachtzig Grad um Z
 * und schiebt es um anderthalb Bloecke nach unten. Beides bleibt.
 */
@OnlyIn(Dist.CLIENT)
public class TeslaCrabRenderer extends EntityRenderer<TeslaCrab> {

    public TeslaCrabRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.0F;
        this.shadowStrength = 0.0F;
    }

    @Override
    public void render(TeslaCrab krabbe, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        /* Der Korpus richtet sich nach dem Rumpf, nicht nach dem Kopf. */
        float rumpf = Mth.rotLerp(partialTick, krabbe.yBodyRotO, krabbe.yBodyRot);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F - rumpf));

        RenderSystem.setShaderTexture(0, ResourceManager.TESLACRAB_TEX);

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.ZP.rotationDegrees(180F));
        RenderContext.translate(0F, -1.5F, 0F);

        float schwung = -(Mth.cos(krabbe.walkAnimation.position(partialTick) * 0.6662F * 2.0F) * 0.4F)
                * krabbe.walkAnimation.speed(partialTick) * 57.3F;

        ResourceManager.teslacrab.renderPart("Body");

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees(schwung));
        ResourceManager.teslacrab.renderPart("Front");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YN.rotationDegrees(schwung));
        ResourceManager.teslacrab.renderPart("Back");
        RenderContext.popPose();

        RenderContext.popPose();

        if(!krabbe.targets.isEmpty()) {

            RenderContext.translate(0F, 1F, 0F);

            int takt = (int) (krabbe.level().getGameTime() % 1000) + 1;

            for(double[] ziel : krabbe.targets) {

                double dx = ziel[0] - krabbe.getX();
                double dy = ziel[1] - (krabbe.getY() + 1);
                double dz = ziel[2] - krabbe.getZ();
                double laenge = Math.sqrt(dx * dx + dy * dy + dz * dz);

                /* Vorzeichen wie beim Blitz der Spule: oben wurde schon um Y gedreht. */
                BeamPronter.prontBeam(new Vec3NT(-dx, dy, -dz), WaveType.RANDOM, BeamType.SOLID,
                        0x404040, 0x404040, takt, (int) (laenge * 5), 0.125F, 2, 0.03125F);
            }
        }

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(TeslaCrab krabbe) {
        return ResourceManager.TESLACRAB_TEX;
    }
}
