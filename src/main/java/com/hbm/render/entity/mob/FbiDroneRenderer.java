package com.hbm.render.entity.mob;

import com.hbm.entity.mob.FbiDrone;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Random;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderDrone.
 *
 * Ein OBJ-Modell, ein Viertelblock angehoben, und eine feste Drehung um die Hochachse.
 *
 * DIE DREHUNG IST KEIN ZUFALL, SONDERN EINE KENNUNG: das Original baut sich einen
 * Zufallsgenerator aus der Nummer der Entitaet und zieht daraus genau eine Zahl. Dieselbe
 * Drohne steht damit in jedem Bild gleich, zwei Drohnen aber verschieden -- ein Schwarm sieht
 * nicht aus wie eine Reihe Klone. Ein gewoehnlicher Zufall wuerde sie flackern lassen.
 *
 * Der Kopter dreht sich NICHT in seine Blickrichtung. Das Original zeichnet ihn als
 * schlichten Render ohne die Drehung, die ein MobRenderer vornaehme -- er haengt in der Luft,
 * wie er gerade steht.
 */
@OnlyIn(Dist.CLIENT)
public class FbiDroneRenderer extends EntityRenderer<FbiDrone> {

    public FbiDroneRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(FbiDrone kopter, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);
        RenderContext.translate(0F, 0.25F, 0F);
        RenderContext.mulPose(Axis.YP.rotationDegrees((float) (new Random(kopter.getId()).nextDouble() * 360D)));

        RenderSystem.setShaderTexture(0, ResourceManager.QUADCOPTER_TEX);
        ResourceManager.quadcopter.renderAll();

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(FbiDrone kopter) {
        return ResourceManager.QUADCOPTER_TEX;
    }
}
