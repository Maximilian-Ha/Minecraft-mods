package com.hbm.render.model.armor;

import com.hbm.main.NuclearTechMod;
import com.hbm.util.ArmorUtil;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelM65.
 *
 * Die M65-Vollhaube. Sie steckt in zwei Gruppen, und das ist keine Ordnungsfrage:
 *   "mask"    die Haube selbst -- Kopfstueck, Nase, Ausatemventil, Nasenschraege, zwei
 *             Sichtscheiben und ein Kasten, den der Erbauer "iForgot" genannt hat
 *   "filter"  der Filteranschluss und die Dose
 *
 * DIE DOSE WIRD NUR GEZEICHNET, WENN AUCH EINE DRINSTECKT. Ohne Filter bleibt am Gesicht nur
 * der nackte Stutzen -- so sieht man von aussen, ob jemand ungefiltert atmet. Das Original
 * prueft dafuer ArmorUtil.getGasMaskFilterRecursively, und zwar rekursiv, weil der Filter auch
 * im Helmaufsatz stecken kann.
 *
 * UEBERSETZUNG AUS TECHNE wie bei ModelGasMaskHead; dieselbe mirror-Falle, dieselbe
 * Drehreihenfolge. Der Y-Versatz heisst im Original yOffset und ist 0,5.
 *
 * ZWEI FLACHE KAESTEN: die Sichtscheiben haben Tiefe null. Das ist kein Fehler im Original,
 * sondern Absicht -- sie liegen als Flaeche auf dem Kopfstueck und sind deshalb wortgetreu
 * mit Tiefe null uebernommen.
 */
public class ModelM65Head extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(NuclearTechMod.withDefaultNamespace("gas_mask_m65"), "main");

    /**
     * Das Original malt die Haube mit 18/16 und legt noch ein Prozent drauf, damit sie nicht
     * mit dem Kopf darunter um dieselben Bildpunkte streitet.
     */
    private static final float SCALE = (18F / 16F) * 1.01F;

    private final ModelPart mask;
    private final ModelPart filter;

    /** Der Traeger, an dem nachgesehen wird, ob ein Filter steckt. */
    public @Nullable LivingEntity living;

    public ModelM65Head(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.mask = root.getChild("mask");
        this.filter = root.getChild("filter");
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition mask = root.addOrReplaceChild("mask", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition filter = root.addOrReplaceChild("filter", CubeListBuilder.create(), PartPose.ZERO);

        float y = 0.5F;

        mask.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, 0F, 0F, 8F, 8F, 8F),
                PartPose.offset(-4F, -8F + y, -4F));
        mask.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0F, 0F, 0F, 3F, 3F, 1F),
                PartPose.offset(-1.5F, -3.5F + y, -5F));
        mask.addOrReplaceChild("outlet", CubeListBuilder.create()
                .texOffs(0, 20).addBox(0F, -2F, 0F, 2F, 2F, 1F),
                PartPose.offsetAndRotation(-1F, -3.5F + y, -5F, -0.4799655F, 0F, 0F));
        mask.addOrReplaceChild("nose_slope", CubeListBuilder.create()
                .texOffs(8, 16).addBox(0F, 0F, -2F, 3F, 2F, 2F),
                PartPose.offsetAndRotation(-1.5F, -2F + y, -4F, 0.6108652F, 0F, 0F));
        mask.addOrReplaceChild("eye_left", CubeListBuilder.create()
                .texOffs(0, 23).addBox(0F, 0F, 0F, 3F, 3F, 0F),
                PartPose.offset(-3.5F, -6F + y, -4.2F));
        mask.addOrReplaceChild("eye_right", CubeListBuilder.create()
                .texOffs(0, 26).addBox(0F, 0F, 0F, 3F, 3F, 0F),
                PartPose.offset(0.5F, -6F + y, -4.2F));
        mask.addOrReplaceChild("i_forgot", CubeListBuilder.create()
                .texOffs(6, 20).addBox(0F, 0F, 0F, 2F, 2F, 1F),
                PartPose.offset(-1F, -3.2F + y, -6F));

        filter.addOrReplaceChild("connector", CubeListBuilder.create()
                .texOffs(6, 23).addBox(0F, 0F, -3F, 2F, 2F, 1F),
                PartPose.offsetAndRotation(-1F, -2F + y, -4F, 0.6108652F, 0F, 0F));
        filter.addOrReplaceChild("canister_inner", CubeListBuilder.create()
                .texOffs(18, 21).addBox(0F, -1F, -5F, 3F, 4F, 2F),
                PartPose.offsetAndRotation(-1.5F, -2F + y, -4F, 0.6108652F, 0F, 0F));
        filter.addOrReplaceChild("canister_outer", CubeListBuilder.create()
                .texOffs(18, 16).addBox(0F, -0.5F, -5F, 4F, 3F, 2F),
                PartPose.offsetAndRotation(-2F, -2F + y, -4F, 0.6108652F, 0F, 0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    /** Setzt Haube und Filter auf den Kopf des Traegers -- beide drehen sich mit ihm. */
    public void copyHeadFrom(HumanoidModel<? extends LivingEntity> original) {
        kopfUebernehmen(this.mask, original);
        kopfUebernehmen(this.filter, original);
    }

    private static void kopfUebernehmen(ModelPart teil, HumanoidModel<? extends LivingEntity> original) {
        teil.x = original.head.x;
        teil.y = original.head.y;
        teil.z = original.head.z;
        teil.xRot = original.head.xRot;
        teil.yRot = original.head.yRot;
        teil.zRot = original.head.zRot;
        teil.visible = original.head.visible;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {

        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);

        this.mask.render(poseStack, consumer, packedLight, packedOverlay, color);

        if(this.living == null || !ArmorUtil.getGasMaskFilterRecursively(
                this.living.getItemBySlot(EquipmentSlot.HEAD), this.living).isEmpty()) {
            this.filter.render(poseStack, consumer, packedLight, packedOverlay, color);
        }

        poseStack.popPose();
    }
}
