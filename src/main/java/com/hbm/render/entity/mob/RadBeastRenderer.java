package com.hbm.render.entity.mob;

import com.hbm.entity.mob.RadBeast;
import com.hbm.main.NuclearTechMod;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.BeamType;
import com.hbm.render.util.BeamPronter.WaveType;
import com.hbm.render.util.RenderContext;
import com.hbm.util.Vec3NT;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderRADBeast.
 *
 * Das Modell ist Vanillas Lohe, nur mit anderer Haut. Dazu kommt der gruene Strahl zu dem,
 * den das Biest gerade bestrahlt -- er geht von anderthalb Bloecken Hoehe aus und endet auf
 * halber Hoehe des Opfers.
 *
 * EIN VIERTEL TIEFER BEIM SPIELER SELBST: das Original zieht dem eigenen Spieler anderthalb
 * Bloecke ab, damit der Strahl nicht durchs Bild geht, sondern von unten kommt.
 *
 * NICHT UEBERNOMMEN: die M65-Maske. Das Original legt der Lohe ein eigenes Kopfmodell auf
 * (ModelM65Blaze, ein Techne-Modell aus zehn Formen); auf 1.21 waere das eine eigene
 * LayerDefinition mit eigenem Aufbau. Das Biest steht ohne sie; die Maske ist Schmuck, kein
 * Verhalten.
 */
@OnlyIn(Dist.CLIENT)
public class RadBeastRenderer extends MobRenderer<RadBeast, BlazeModel<RadBeast>> {

    private static final ResourceLocation HAUT = NuclearTechMod.withDefaultNamespace("textures/entity/radbeast.png");

    /** Wo der Strahl ansetzt, in Bloecken ueber den Fuessen. */
    private static final double STRAHLHOEHE = 1.25D;

    public RadBeastRenderer(EntityRendererProvider.Context context) {
        super(context, new BlazeModel<>(context.bakeLayer(ModelLayers.BLAZE)), 0.5F);
    }

    @Override
    public void render(RadBeast biest, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        Entity opfer = biest.getOpfer();

        if(opfer != null && biest.getY() > 0.1) {

            RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);
            RenderContext.translate(0F, (float) STRAHLHOEHE, 0F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

            double zy = opfer.getY() + opfer.getBbHeight() / 2;
            if(opfer == Minecraft.getInstance().player) zy -= 1.5D;

            double dx = opfer.getX() - biest.getX();
            double dy = zy - (biest.getY() + STRAHLHOEHE);
            double dz = opfer.getZ() - biest.getZ();
            double laenge = Math.sqrt(dx * dx + dy * dy + dz * dz);

            int takt = (int) (biest.level().getGameTime() % 1000) + 1;

            BeamPronter.prontBeam(new Vec3NT(-dx, dy, -dz), WaveType.RANDOM, BeamType.SOLID,
                    0x004000, 0x004000, takt, (int) (laenge * 5), 0.125F, 2, 0.03125F);

            RenderContext.end();
        }

        super.render(biest, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RadBeast biest) {
        return HAUT;
    }
}
