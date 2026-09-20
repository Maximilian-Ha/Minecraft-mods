package com.hbm.render.model;

import com.hbm.main.NuclearTechMod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelBullet.
 *
 * Ein einziger Kasten, zwei mal eins mal eins, auf einer Bildflaeche von acht mal vier.
 * Mehr ist das Geschoss nicht -- und mehr war es im Original auch nie.
 *
 * DAS MIRROR AM ENDE DES KONSTRUKTORS IST WIRKUNGSLOS, wie schon bei der Krabbe in
 * Runde 236 nachgemessen: 1.7.10s addBox liest this.mirror zum Zeitpunkt des Aufrufs, und
 * das Original setzt es erst danach. Der Kasten ist also nicht gespiegelt, und hier steht
 * kein .mirror().
 */
@OnlyIn(Dist.CLIENT)
public class ModelBullet {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            NuclearTechMod.withDefaultNamespace("bullet"), "main");

    private final ModelPart root;

    public ModelBullet(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition teile = mesh.getRoot();

        /* addBox(0, 0, 0, 2, 1, 1) mit setRotationPoint(1, -0.5, -0.5): in 1.21 sind
         * Kastenversatz und Drehpunkt getrennt, also bleibt der Kasten bei null und der
         * Drehpunkt traegt die Verschiebung -- genau wie im Original. */
        teile.addOrReplaceChild("bullet",
                CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 2, 1, 1),
                PartPose.offset(1F, -0.5F, -0.5F));

        return LayerDefinition.create(mesh, 8, 4);
    }

    public void render(PoseStack pose, VertexConsumer buffer, int light, int overlay) {
        this.root.render(pose, buffer, light, overlay);
    }
}
