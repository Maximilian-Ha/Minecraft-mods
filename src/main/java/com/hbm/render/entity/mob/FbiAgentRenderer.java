package com.hbm.render.entity.mob;

import com.hbm.entity.mob.FbiAgent;
import com.hbm.main.NuclearTechMod;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderFBI.
 *
 * Eine Menschengestalt in Anzug und Sonnenbrille. Das Original nimmt ModelFBI, und das ist
 * ein ModelBiped ohne eine einzige Aenderung -- die Klasse ueberschreibt render und ruft nur
 * super auf. Der Port nimmt darum gleich das Menschenmodell.
 *
 * DIE ARME BLEIBEN VORN. Das Original setzt vor und nach dem Zeichnen aimedBow auf true, also
 * die Haltung des angelegten Bogens: der Beamte haelt seine Waffe stets im Anschlag, auch
 * wenn er nie schiesst. Auf 1.21 heisst diese Haltung ArmPose.BOW_AND_ARROW, und sie wird
 * hier vor dem Zeichnen gesetzt, weil HumanoidMobRenderer sie sonst aus dem Gegenstand in der
 * Hand ableitet.
 *
 * DIE RUESTUNGSLAGE muss von Hand dazu, sonst saehe man die Sicherheitsruestung nicht --
 * dieselbe Stelle wie beim Untoten Soldaten.
 */
@OnlyIn(Dist.CLIENT)
public class FbiAgentRenderer extends HumanoidMobRenderer<FbiAgent, HumanoidModel<FbiAgent>> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/entity/fbi.png");

    public FbiAgentRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<FbiAgent>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new HumanoidModel<FbiAgent>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public void render(FbiAgent beamter, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int light) {
        this.model.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
        this.model.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
        super.render(beamter, yaw, partialTick, pose, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(FbiAgent beamter) {
        return TEXTURE;
    }
}
