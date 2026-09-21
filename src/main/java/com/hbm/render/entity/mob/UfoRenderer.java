package com.hbm.render.entity.mob;

import com.hbm.entity.mob.Ufo;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.BeamType;
import com.hbm.render.util.BeamPronter.WaveType;
import com.hbm.render.util.RenderContext;
import com.hbm.util.Vec3NT;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderUFO.
 *
 * Die Scheibe dreht sich jede Sekunde um hundert Grad (fuenf je Tick) und steht doppelt so
 * gross da, wie ihr Modell sie zeichnet.
 *
 * IM STURZ KIPPT SIE. Nach dem Tod dreht das Original sie um eine Achse, die zwischen X und Z
 * liegt, und zwar um so viel Grad, wie die Sterbezeit fortgeschritten ist -- weil die bei
 * minus dreissig beginnt, sieht man erst nach dreissig Ticks etwas davon.
 *
 * DER FANGSTRAHL IST DREIFACH: ein breiter, gewundener Kern und zwei duenne, zufaellig
 * zuckende Straenge darum. Er reicht vom UFO bis zum ersten festen Block darunter.
 */
@OnlyIn(Dist.CLIENT)
public class UfoRenderer extends EntityRenderer<Ufo> {

    private static final double MASSSTAB = 2D;

    public UfoRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0F;
        this.shadowStrength = 0F;
    }

    @Override
    public void render(Ufo ufo, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);
        RenderContext.translate(0F, 1F, 0F);

        if(!ufo.isAlive()) {
            float kippung = ufo.deathTime + partialTick;
            RenderContext.mulPose(Axis.of(new org.joml.Vector3f(1, 0, 1)).rotationDegrees(kippung));
        }

        RenderContext.pushPose();
        float drehung = (float) ((ufo.tickCount + partialTick) * 5 % 360D);
        RenderContext.mulPose(Axis.YP.rotationDegrees(drehung));
        RenderContext.scale((float) MASSSTAB, (float) MASSSTAB, (float) MASSSTAB);

        RenderSystem.setShaderTexture(0, ResourceManager.UFO_TEX);
        ResourceManager.ufo.renderAll();

        RenderContext.popPose();

        if(ufo.hatStrahl()) this.strahl(ufo);

        RenderContext.end();
    }

    /** Der Strahl vom UFO bis zum ersten festen Block darunter. */
    private void strahl(Ufo ufo) {

        int ix = (int) Math.floor(ufo.getX());
        int iz = (int) Math.floor(ufo.getZ());
        int boden = ufo.level().getMinBuildHeight();

        for(int y = (int) Math.ceil(ufo.getY()); y >= ufo.level().getMinBuildHeight(); y--) {
            if(!ufo.level().getBlockState(new BlockPos(ix, y, iz)).isAir()) { boden = y; break; }
        }

        double laenge = ufo.getY() - boden;
        if(laenge <= 0) return;

        Vec3NT nachUnten = new Vec3NT(0, -laenge, 0);

        BeamPronter.prontBeam(nachUnten, WaveType.SPIRAL, BeamType.SOLID, 0x101020, 0x101020,
                0, (int) (laenge + 1), 0F, 6, (float) MASSSTAB * 0.75F);
        BeamPronter.prontBeam(nachUnten, WaveType.RANDOM, BeamType.SOLID, 0x202060, 0x202060,
                ufo.tickCount / 2, (int) (laenge / 2 + 1), (float) MASSSTAB * 1.5F, 2, 0.0625F);
        BeamPronter.prontBeam(nachUnten, WaveType.RANDOM, BeamType.SOLID, 0x202060, 0x202060,
                ufo.tickCount / 4, (int) (laenge / 2 + 1), (float) MASSSTAB * 1.5F, 2, 0.0625F);
    }

    @Override
    public ResourceLocation getTextureLocation(Ufo ufo) {
        return ResourceManager.UFO_TEX;
    }
}
