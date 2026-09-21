package com.hbm.render.entity.mob;

import com.hbm.entity.mob.botprime.WormBase;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
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

import java.util.function.Supplier;

/**
 * Portiert aus 1.7.10: RenderWormHead und RenderWormBody samt ihren Modellen.
 *
 * Beide Darsteller sind bis auf Modell und Textur gleich, deshalb steht hier einer fuer beide.
 * Die Drehung ist die des Originals: erst die Gierung minus neunzig Grad, dann die Neigung --
 * ebenfalls minus neunzig -- um die Z-Achse. Das Modell liegt naemlich laengs, nicht aufrecht.
 *
 * KEIN RUECKSEITENSCHNITT: das Original schaltet ihn fuer den Wurm ab, weil seine Panzerung
 * aus einseitigen Flaechen besteht und sonst durchsichtig waere.
 */
@OnlyIn(Dist.CLIENT)
public class WormRenderer<T extends WormBase> extends EntityRenderer<T> {

    private final Supplier<IModelCustom> modell;
    private final ResourceLocation haut;

    private WormRenderer(EntityRendererProvider.Context context, Supplier<IModelCustom> modell, ResourceLocation haut) {
        super(context);
        this.modell = modell;
        this.haut = haut;
        this.shadowRadius = 0F;
        this.shadowStrength = 0F;
    }

    /** Der Kopf. */
    public static <T extends WormBase> WormRenderer<T> kopf(EntityRendererProvider.Context context) {
        return new WormRenderer<>(context, () -> ResourceManager.bot_prime_head, ResourceManager.WORM_HEAD_TEX);
    }

    /** Ein Glied. */
    public static <T extends WormBase> WormRenderer<T> glied(EntityRendererProvider.Context context) {
        return new WormRenderer<>(context, () -> ResourceManager.bot_prime_body, ResourceManager.WORM_BODY_TEX);
    }

    @Override
    public void render(T teil, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        float gier = Mth.rotLerp(partialTick, teil.yRotO, teil.getYRot());
        float neigung = Mth.lerp(partialTick, teil.xRotO, teil.getXRot());

        RenderContext.mulPose(Axis.YP.rotationDegrees(gier - 90F));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(neigung - 90F));

        RenderSystem.setShaderTexture(0, this.haut);
        RenderSystem.disableCull();
        this.modell.get().renderAll();
        RenderSystem.enableCull();

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(T teil) {
        return this.haut;
    }
}
