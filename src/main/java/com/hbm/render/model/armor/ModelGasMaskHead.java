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
 * Portiert aus 1.7.10: com.hbm.render.model.ModelGasMask.
 *
 * Die gewoehnliche Gasmaske, sechs Kaesten auf dem Kopf: die Sichtscheibe, zwei Nieten, das
 * Knie des Schlauchs, die Filterdose und der Nackenschutz.
 *
 * UEBERSETZUNG AUS TECHNE, nach demselben Verfahren wie beim Satellitenempfaenger (Runde 105):
 * new ModelRenderer(this, u, v) wird texOffs(u, v), addBox bleibt addBox, setRotationPoint
 * samt setRotation wird PartPose.offsetAndRotation. Die Drehreihenfolge ist in beiden Fassungen
 * Z, dann Y, dann X.
 *
 * DIE FALLE: das Original setzt auf jedem Kasten mirror = true -- aber NACH addBox. In 1.7.10
 * liest addBox das Feld, die Zeile kommt also zu spaet und tut nichts. Ein mechanisches
 * .mirror() wuerde das Modell spiegeln; hier steht deshalb keines.
 *
 * Die 0.075F/2 in jedem Y-Wert sind der Versatz aus dem Original, wortgetreu uebernommen.
 * convertToChild ist nicht mitgekommen: der Elternkasten "mask" sitzt im Original auf (0|0|0)
 * ohne Drehung, die Umrechnung zieht also ueberall null ab.
 */
public class ModelGasMaskHead extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(NuclearTechMod.withDefaultNamespace("gas_mask"), "main");

    /** Der Kopf des Originals steht 1,15fach vergroessert um den Spielerkopf. */
    private static final float SCALE = 1.15F;

    private final ModelPart mask;

    public ModelGasMaskHead(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.mask = root.getChild("mask");
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition mask = mesh.getRoot().addOrReplaceChild("mask", CubeListBuilder.create(), PartPose.ZERO);

        float y = 0.075F / 2F;

        mask.addOrReplaceChild("visor", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, 0F, 0F, 8F, 8F, 3F),
                PartPose.offset(-4F, -8F + y, -4F));
        mask.addOrReplaceChild("rivet_left", CubeListBuilder.create()
                .texOffs(22, 0).addBox(0F, 0F, 0F, 2F, 2F, 1F),
                PartPose.offset(1F - 4F, 3F - 8F + y, -0.5333334F - 4F));
        mask.addOrReplaceChild("rivet_right", CubeListBuilder.create()
                .texOffs(22, 0).addBox(0F, 0F, 0F, 2F, 2F, 1F),
                PartPose.offset(5F - 4F, 3F - 8F + y, -0.5F - 4F));
        mask.addOrReplaceChild("elbow", CubeListBuilder.create()
                .texOffs(0, 11).addBox(0F, 0F, 0F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(3F - 4F, 5F - 8F + y, -4F, -0.7853982F, 0F, 0F));
        mask.addOrReplaceChild("filter", CubeListBuilder.create()
                .texOffs(0, 15).addBox(0F, 2F, -0.5F, 3F, 4F, 3F),
                PartPose.offsetAndRotation(2.5F - 4F, 5F - 8F + y, -4F, -0.7853982F, 0F, 0F));
        mask.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(0, 22).addBox(0F, 0F, 0F, 8F, 1F, 5F),
                PartPose.offset(-4F, 3F - 8F + y, 3F - 4F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    /** Setzt die Maske auf den Kopf des Traegers -- sie dreht sich mit ihm. */
    public void copyHeadFrom(HumanoidModel<? extends LivingEntity> original) {
        this.mask.x = original.head.x;
        this.mask.y = original.head.y;
        this.mask.z = original.head.z;
        this.mask.xRot = original.head.xRot;
        this.mask.yRot = original.head.yRot;
        this.mask.zRot = original.head.zRot;
        this.mask.visible = original.head.visible;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        this.mask.render(poseStack, consumer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }
}
