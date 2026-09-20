package com.hbm.render.model.armor;

import com.hbm.main.NuclearTechMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelGoggles.
 *
 * Die Schutzbrille, fuenf Kaesten auf der Stirn: die Blende, der Brillenkoerper, zwei Glaeser
 * und das Band um den Hinterkopf.
 *
 * UEBERSETZUNG AUS TECHNE wie bei den beiden Masken in Runde 206, mit derselben mirror-Falle:
 * das Original setzt mirror = true nach addBox, wo es nichts mehr tut. Hier steht kein
 * .mirror(). convertToChild ist wieder ein Nulldurchgang, weil der Elternkasten auf (0|0|0)
 * ohne Drehung sitzt.
 *
 * OHNE EIGENE VERGROESSERUNG: anders als Gasmaske und M65 malt das Original die Brille
 * unskaliert. Sie sitzt eng am Kopf, und genau so soll sie sitzen.
 *
 * DER TIPPFEHLER DES ORIGINALS ist nicht mitgekommen: dort heisst die Gruppe "google".
 */
public class ModelGogglesHead extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(NuclearTechMod.withDefaultNamespace("goggles"), "main");

    private final ModelPart goggles;

    public ModelGogglesHead(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.goggles = root.getChild("goggles");
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition goggles = mesh.getRoot().addOrReplaceChild("goggles", CubeListBuilder.create(), PartPose.ZERO);

        goggles.addOrReplaceChild("brow", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, 0F, 0F, 9F, 3F, 1F),
                PartPose.offset(-4.5F, -3F - 2F, -4.5F));
        goggles.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 4).addBox(0F, 0F, 0F, 9F, 2F, 5F),
                PartPose.offset(-4.5F, -3F - 2F, -3.5F));
        goggles.addOrReplaceChild("lens_left", CubeListBuilder.create()
                .texOffs(26, 0).addBox(0F, 0F, 0F, 2F, 2F, 1F),
                PartPose.offset(1F, -2.5F - 2F, -5F));
        goggles.addOrReplaceChild("lens_right", CubeListBuilder.create()
                .texOffs(20, 0).addBox(0F, 0F, 0F, 2F, 2F, 1F),
                PartPose.offset(-3F, -2.5F - 2F, -5F));
        goggles.addOrReplaceChild("strap", CubeListBuilder.create()
                .texOffs(0, 11).addBox(0F, 0F, 0F, 9F, 1F, 4F),
                PartPose.offset(-4.5F, -3F - 2F, 0.5F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    /** Setzt die Brille auf den Kopf des Traegers -- sie dreht sich mit ihm. */
    public void copyHeadFrom(HumanoidModel<? extends LivingEntity> original) {
        this.goggles.x = original.head.x;
        this.goggles.y = original.head.y;
        this.goggles.z = original.head.z;
        this.goggles.xRot = original.head.xRot;
        this.goggles.yRot = original.head.yRot;
        this.goggles.zRot = original.head.zRot;
        this.goggles.visible = original.head.visible;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        this.goggles.render(poseStack, consumer, packedLight, packedOverlay, color);
    }
}
