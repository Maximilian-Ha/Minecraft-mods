package com.hbm.render.entity.mob;

import com.hbm.entity.mob.TaintCrab;
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
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderTaintCrab samt ModelTaintCrab.
 *
 * Wie die Teslakrabbe, mit drei Unterschieden: das Modell heisst taintcrab.obj und seine
 * Beinteile Legs1 und Legs2, es steht eine Vierteldrehung quer, und die Blitze gehen von
 * anderthalb statt von einem Block Hoehe aus.
 */
@OnlyIn(Dist.CLIENT)
public class TaintCrabRenderer extends EntityRenderer<TaintCrab> {

    /** Hoehe, aus der die Blitze kommen -- im Original posY + 1.25. */
    private static final float BLITZHOEHE = 1.25F;

    public TaintCrabRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.0F;
        this.shadowStrength = 0.0F;
    }

    @Override
    public void render(TaintCrab krabbe, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        /* Erst die Blitze in Weltrichtung, wie im Original vor super.doRender. */
        if(!krabbe.targets.isEmpty()) {

            RenderContext.pushPose();
            RenderContext.translate(0F, BLITZHOEHE, 0F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

            int takt = (int) (krabbe.level().getGameTime() % 1000) + 1;

            for(double[] ziel : krabbe.targets) {

                double dx = ziel[0] - krabbe.getX();
                double dy = ziel[1] - (krabbe.getY() + BLITZHOEHE);
                double dz = ziel[2] - krabbe.getZ();
                double laenge = Math.sqrt(dx * dx + dy * dy + dz * dz);

                BeamPronter.prontBeam(new Vec3NT(-dx, dy, -dz), WaveType.RANDOM, BeamType.SOLID,
                        0x404040, 0x404040, takt, (int) (laenge * 5), 0.125F, 2, 0.03125F);
            }

            RenderContext.popPose();
        }

        float rumpf = Mth.rotLerp(partialTick, krabbe.yBodyRotO, krabbe.yBodyRot);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F - rumpf));

        RenderSystem.setShaderTexture(0, ResourceManager.TAINTCRAB_TEX);

        /* Die Vierteldrehung, die nur dieses Modell braucht, und die Wende auf die Fuesse. */
        RenderContext.mulPose(Axis.YN.rotationDegrees(90F));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(180F));
        RenderContext.translate(0F, -1.5F, 0F);

        float schwung = -(Mth.cos(krabbe.walkAnimation.position(partialTick) * 0.6662F * 2.0F) * 0.4F)
                * krabbe.walkAnimation.speed(partialTick) * 57.3F;

        ResourceManager.taintcrab.renderPart("Body");

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees(schwung));
        ResourceManager.taintcrab.renderPart("Legs1");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YN.rotationDegrees(schwung));
        ResourceManager.taintcrab.renderPart("Legs2");
        RenderContext.popPose();

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(TaintCrab krabbe) {
        return ResourceManager.TAINTCRAB_TEX;
    }
}
