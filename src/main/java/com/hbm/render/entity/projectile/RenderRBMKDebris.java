package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.RBMKDebris;
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
import com.hbm.render.loader.IModelCustom;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.projectile.RenderRBMKDebris.
 *
 * Jedes Teil bekommt aus seiner Entitaetsnummer eine eigene feste Ausrichtung -- so sieht ein
 * Haufen Schutt nicht aus wie eine Reihe Klone.
 */
public class RenderRBMKDebris extends EntityRenderer<RBMKDebris> {

    public static final ResourceLocation TEX_BASE = NuclearTechMod.withDefaultNamespace("textures/block/rbmk_side.png");
    public static final ResourceLocation TEX_ELEMENT = NuclearTechMod.withDefaultNamespace("textures/block/rbmk_fuel.png");
    public static final ResourceLocation TEX_CONTROL = NuclearTechMod.withDefaultNamespace("textures/block/rbmk_control.png");
    public static final ResourceLocation TEX_BLANK = NuclearTechMod.withDefaultNamespace("textures/block/rbmk_blank_side.png");
    public static final ResourceLocation TEX_LID = NuclearTechMod.withDefaultNamespace("textures/block/rbmk_blank_cover_top.png");
    public static final ResourceLocation TEX_GRAPHITE = NuclearTechMod.withDefaultNamespace("textures/block/block_graphite.png");

    public RenderRBMKDebris(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(RBMKDebris debris, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        RenderContext.translate(0F, 0.125F, 0F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(debris.getId() % 360));

        float rot = debris.lastRot + (debris.rot - debris.lastRot) * partialTicks;
        RenderContext.mulPose(Axis.XP.rotationDegrees(rot));
        RenderContext.mulPose(Axis.YP.rotationDegrees(rot));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(rot));

        ResourceLocation texture;
        IModelCustom model;

        switch(debris.getDebrisType()) {
            case ELEMENT -> { texture = TEX_BASE; model = ResourceManager.deb_element; }
            case FUEL -> { texture = TEX_ELEMENT; model = ResourceManager.deb_fuel; }
            case GRAPHITE -> { texture = TEX_GRAPHITE; model = ResourceManager.deb_graphite; }
            case LID -> { texture = TEX_LID; model = ResourceManager.deb_lid; }
            case ROD -> { texture = TEX_CONTROL; model = ResourceManager.deb_rod; }
            default -> { texture = TEX_BLANK; model = ResourceManager.deb_blank; }
        }

        RenderSystem.setShaderTexture(0, texture);
        model.renderAll();

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(RBMKDebris debris) {
        return TEX_BASE;
    }
}
