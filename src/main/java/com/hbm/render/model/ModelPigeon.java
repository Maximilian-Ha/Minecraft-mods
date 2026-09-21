package com.hbm.render.model;

import com.hbm.entity.mob.Pigeon;
import com.hbm.main.NuclearTechMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelPigeon.
 *
 * Zehn Kaesten. Bemerkenswert ist, dass es den Rumpf ZWEIMAL gibt: einmal schlank und einmal
 * um einen Bildpunkt aufgeblasen (CubeDeformation 1). Welcher gezeichnet wird, entscheidet,
 * ob die Taube gefressen hat.
 *
 * DIE FLUEGEL HAENGEN IM ORIGINAL AN BEIDEN RUEMPFEN -- dieselben zwei ModelRenderer werden
 * zweimal als Kind eingehaengt. Auf 1.21 gehoert ein gebackenes Teil zu genau einem Elternteil,
 * darum gibt es hier zwei Paare, und gezeichnet wird das Paar des Rumpfes, der gerade dran ist.
 * Das Ergebnis ist dasselbe, weil das Original ohnehin immer nur einen der beiden Ruempfe
 * zeichnet.
 *
 * DER FLUEGELSCHLAG IST KEIN SCHLAG, SONDERN EINE DREHUNG: das Original setzt
 * rotateAngleZ = ageInTicks, also den fortlaufenden Zaehler selbst -- die Fluegel wirbeln
 * durch, statt zu schwingen. So steht es dort, und so steht es hier.
 */
public class ModelPigeon extends EntityModel<Pigeon> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NuclearTechMod.MODID, "pigeon"), "main");

    private final ModelPart kopf;
    private final ModelPart schnabel;
    private final ModelPart rumpf;
    private final ModelPart rumpfFett;
    private final ModelPart beinLinks;
    private final ModelPart beinRechts;
    private final ModelPart fluegelLinks;
    private final ModelPart fluegelRechts;
    private final ModelPart fluegelLinksFett;
    private final ModelPart fluegelRechtsFett;
    private final ModelPart buerzel;
    private final ModelPart federn;

    private boolean fett;

    public ModelPigeon(ModelPart wurzel) {
        this.kopf = wurzel.getChild("kopf");
        this.schnabel = wurzel.getChild("schnabel");
        this.rumpf = wurzel.getChild("rumpf");
        this.rumpfFett = wurzel.getChild("rumpf_fett");
        this.beinLinks = wurzel.getChild("bein_links");
        this.beinRechts = wurzel.getChild("bein_rechts");
        this.fluegelLinks = this.rumpf.getChild("fluegel_links");
        this.fluegelRechts = this.rumpf.getChild("fluegel_rechts");
        this.fluegelLinksFett = this.rumpfFett.getChild("fluegel_links");
        this.fluegelRechtsFett = this.rumpfFett.getChild("fluegel_rechts");
        this.buerzel = wurzel.getChild("buerzel");
        this.federn = wurzel.getChild("federn");
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition wurzel = mesh.getRoot();

        wurzel.addOrReplaceChild("kopf",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2F, -6F, -2F, 4F, 6F, 4F),
                PartPose.offset(0F, 16F, -2F));

        wurzel.addOrReplaceChild("schnabel",
                CubeListBuilder.create().texOffs(14, 0).addBox(-1F, -4F, -4F, 2F, 2F, 2F),
                PartPose.offset(0F, 16F, -2F));

        PartDefinition rumpf = wurzel.addOrReplaceChild("rumpf",
                CubeListBuilder.create().texOffs(0, 10).addBox(-3F, -3F, -4F, 6F, 6F, 8F),
                PartPose.offset(0F, 17F, 0F));

        PartDefinition rumpfFett = wurzel.addOrReplaceChild("rumpf_fett",
                CubeListBuilder.create().texOffs(0, 10).addBox(-3F, -3F, -4F, 6F, 6F, 8F, new CubeDeformation(1F)),
                PartPose.offset(0F, 17F, 0F));

        for(PartDefinition koerper : new PartDefinition[] { rumpf, rumpfFett }) {

            koerper.addOrReplaceChild("fluegel_links",
                    CubeListBuilder.create().texOffs(28, 0).addBox(0F, 0F, -3F, 1F, 4F, 6F),
                    PartPose.offset(3F, -2F, 0F));

            koerper.addOrReplaceChild("fluegel_rechts",
                    CubeListBuilder.create().texOffs(28, 10).addBox(-1F, 0F, -3F, 1F, 4F, 6F),
                    PartPose.offset(-3F, -2F, 0F));
        }

        wurzel.addOrReplaceChild("buerzel",
                CubeListBuilder.create().texOffs(0, 24).addBox(-2F, -2F, -2F, 4F, 4F, 4F),
                PartPose.offset(0F, 20F, 4F));

        wurzel.addOrReplaceChild("federn",
                CubeListBuilder.create().texOffs(16, 24).addBox(-1F, -0.5F, -2F, 2F, 1F, 4F),
                PartPose.offset(0F, 21.5F, 7.5F));

        wurzel.addOrReplaceChild("bein_links",
                CubeListBuilder.create().texOffs(20, 0).addBox(-1F, 0F, 0F, 2F, 4F, 2F),
                PartPose.offset(1F, 20F, -1F));

        wurzel.addOrReplaceChild("bein_rechts",
                CubeListBuilder.create().texOffs(20, 0).addBox(-1F, 0F, 0F, 2F, 4F, 2F),
                PartPose.offset(-1F, 20F, -1F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Pigeon taube, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

        this.fett = taube.isFat();

        this.kopf.xRot = this.schnabel.xRot = headPitch * ((float) Math.PI / 180F);
        this.kopf.yRot = this.schnabel.yRot = netHeadYaw * ((float) Math.PI / 180F);

        this.rumpf.xRot = this.rumpfFett.xRot = this.buerzel.xRot = -((float) Math.PI / 4F);
        this.federn.xRot = -((float) Math.PI / 8F);

        this.beinRechts.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.beinLinks.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;

        this.fluegelRechts.zRot = this.fluegelRechtsFett.zRot = ageInTicks;
        this.fluegelLinks.zRot = this.fluegelLinksFett.zRot = -ageInTicks;

        /* Fett sitzt alles einen Bildpunkt weiter aussen. */
        this.kopf.z = this.schnabel.z = this.fett ? -4F : -2F;
        this.buerzel.z = this.fett ? 5F : 4F;
        this.federn.z = this.fett ? 8.5F : 7.5F;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int farbe) {

        this.kopf.render(pose, buffer, light, overlay, farbe);
        this.schnabel.render(pose, buffer, light, overlay, farbe);

        if(this.fett) {
            this.rumpfFett.render(pose, buffer, light, overlay, farbe);
        } else {
            this.rumpf.render(pose, buffer, light, overlay, farbe);
        }

        this.beinRechts.render(pose, buffer, light, overlay, farbe);
        this.beinLinks.render(pose, buffer, light, overlay, farbe);
        this.buerzel.render(pose, buffer, light, overlay, farbe);
        this.federn.render(pose, buffer, light, overlay, farbe);
    }
}
