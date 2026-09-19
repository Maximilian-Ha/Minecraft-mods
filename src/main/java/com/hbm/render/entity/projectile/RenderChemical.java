package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.Chemical;
import com.hbm.entity.projectile.Chemical.ChemicalStyle;
import com.hbm.main.NuclearTechMod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.projectile.RenderChemical.
 *
 * Die Wolke wird nur gezeichnet, wenn sie ein Gas ist. Fluessigkeit und brennende
 * Fluessigkeit haben KEINEN Zeichner -- was man von ihnen sieht, sind Partikel, die die
 * Entitaet selbst ausstreut.
 *
 * DAS GAS WIRD MIT DEM ALTER GROESSER UND DURCHSICHTIGER. Die Stichflamme wechselt dabei
 * zusaetzlich die Farbe, von Gelb nach Rot -- dieselbe Rechnung ueber den Farbkreis wie im
 * Original.
 *
 * NICHT UEBERNOMMEN: der Strahl fuer Antimaterie und Iongel. Im Original zeichnet ihn
 * dieselbe Klasse; hier fehlt das Gegenstueck zu BeamPronter, und beide Sorten sind ohnehin
 * nur ueber den Kreativreiter in einen Tank zu bekommen.
 */
public class RenderChemical extends EntityRenderer<Chemical> {

    private static final ResourceLocation TEX = NuclearTechMod.withDefaultNamespace("textures/particle/base_particle.png");

    public RenderChemical(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Chemical wolke, float yRot, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        ChemicalStyle art = wolke.getStyle();
        if(art != ChemicalStyle.GAS && art != ChemicalStyle.GASFLAME) return;

        float alter = (wolke.tickCount + partialTick) / (float) wolke.getMaxAge();
        float groesse = alter * 2F;

        int rgb;
        float deckkraft;

        if(art == ChemicalStyle.GASFLAME) {
            rgb = Color.getHSBColor(Math.max((60F - alter * 100F) / 360F, 0F), 1F - alter * 0.25F, 1F - alter * 0.5F).getRGB();
            deckkraft = 1F - alter;
        } else {
            rgb = wolke.getFluidType().getColor();
            deckkraft = (1F - alter) * 0.5F;
        }

        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(TEX));
        Matrix4f matrix = poseStack.last().pose();

        int farbe = ((int) (Mth.clamp(deckkraft, 0F, 1F) * 255F) << 24) | (rgb & 0xFFFFFF);

        consumer.addVertex(matrix, -groesse, -groesse, 0F).setColor(farbe).setUv(0, 1).setLight(packedLight);
        consumer.addVertex(matrix, groesse, -groesse, 0F).setColor(farbe).setUv(1, 1).setLight(packedLight);
        consumer.addVertex(matrix, groesse, groesse, 0F).setColor(farbe).setUv(1, 0).setLight(packedLight);
        consumer.addVertex(matrix, -groesse, groesse, 0F).setColor(farbe).setUv(0, 0).setLight(packedLight);

        poseStack.popPose();
    }

    @Override public ResourceLocation getTextureLocation(Chemical wolke) { return TEX; }
}
