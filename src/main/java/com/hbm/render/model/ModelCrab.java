package com.hbm.render.model;

import com.hbm.entity.mob.CyberCrab;
import com.hbm.main.NuclearTechMod;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelCrab.
 *
 * Die Krabbe. Zwanzig Kaesten: ein gestapelter Panzer aus acht Platten, vier Beine mit je
 * einem Fuss, und vier Zangen vorn. Die Zahlen sind die des Originals, Kasten fuer Kasten.
 *
 * DIE ACHT BEINTEILE SIND IM KREIS GESETZT, nicht einzeln gedreht: jedes Bein sitzt im
 * Mittelpunkt und ist um ein Vielfaches von fuenfundvierzig Grad um die Hochachse gedreht,
 * sein Kasten steht zwei Einheiten davor. Deshalb haben alle vier Beine dieselben
 * Kastenmasse und unterscheiden sich nur im Winkel.
 *
 * WAS SICH BEWEGT, ist allein die Gierung der acht Beinteile: sie schwenken im Gleichtakt
 * mit dem Schritt hin und her, Bein und Fuss gegenlaeufig zum Nachbarn. Das Original
 * rechnet dafuer genau einen Wert aus (f9) und verteilt ihn mit wechselndem Vorzeichen --
 * die anderen sechs Zeilen, die es dafuer einmal gab, stehen dort auskommentiert.
 *
 * DIE DREHUNG UM MINUS NEUNZIG GRAD und der Versatz um anderthalb Bloecke stehen im
 * Original in renderAll. Sie bleiben hier, denn ohne sie laeuft die Krabbe seitwaerts --
 * was fuer eine Krabbe zwar passend waere, aber nicht das ist, was das Original zeigt.
 *
 * KEIN KASTEN IST GESPIEGELT, obwohl das Original am Ende seines Konstruktors
 * mirror = true fuer alle zwanzig setzt: in 1.7.10 liest addBox den Schalter in dem
 * Augenblick, in dem der Kasten entsteht, und schreibt ihn in die ModelBox. Wer ihn
 * danach setzt, setzt ihn fuer keinen Kasten mehr -- es folgt ja kein addBox. Die Zeile
 * ist im Original wirkungslos; sie in 1.21 nachzubauen waere eine Aenderung.
 */
@OnlyIn(Dist.CLIENT)
public class ModelCrab extends EntityModel<CyberCrab> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            NuclearTechMod.withDefaultNamespace("crab"), "main");

    /** Die acht Beinteile: vier Beine, vier Fuesse. Nur sie bewegen sich. */
    private static final String[] BEINE = { "leg_ne", "leg_nw", "leg_sw", "leg_se" };
    private static final String[] FUESSE = { "foot_ne", "foot_nw", "foot_sw", "foot_se" };

    /** Die vier Grundwinkel der Beine, in der Reihenfolge des Originals. */
    private static final float[] WINKEL = { 0.78539816F, -0.78539816F, -2.35619449F, 2.35619449F };

    private final ModelPart root;

    public ModelCrab(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition teile = mesh.getRoot();

        /* Der Panzer: acht Platten uebereinander, jede mit eigener Bildstelle. */
        teile.addOrReplaceChild("shell_1", CubeListBuilder.create().texOffs(1, 1).addBox(0F, 0F, 0F, 4, 1, 4), PartPose.offset(-2F, -3F, -2F));
        teile.addOrReplaceChild("shell_2", CubeListBuilder.create().texOffs(17, 1).addBox(0F, 0F, 0F, 4, 1, 6), PartPose.offset(-2F, -4F, -3F));
        teile.addOrReplaceChild("shell_3", CubeListBuilder.create().texOffs(33, 1).addBox(0F, 0F, 0F, 3, 1, 3), PartPose.offset(-1.5F, -5F, -1.5F));
        teile.addOrReplaceChild("shell_4", CubeListBuilder.create().texOffs(49, 1).addBox(0F, 0F, 0F, 4, 1, 2), PartPose.offset(-2F, -4.5F, -1F));
        teile.addOrReplaceChild("shell_5", CubeListBuilder.create().texOffs(1, 9).addBox(0F, 0F, 0F, 6, 1, 4), PartPose.offset(-3F, -4F, -2F));
        teile.addOrReplaceChild("shell_6", CubeListBuilder.create().texOffs(1, 25).addBox(0F, 0F, 0F, 2, 1, 4), PartPose.offset(-1F, -4.5F, -2F));
        teile.addOrReplaceChild("shell_7", CubeListBuilder.create().texOffs(17, 25).addBox(0F, 0F, 0F, 5, 1, 3), PartPose.offset(-2.5F, -3.5F, -1.5F));
        teile.addOrReplaceChild("shell_8", CubeListBuilder.create().texOffs(33, 25).addBox(0F, 0F, 0F, 3, 1, 5), PartPose.offset(-1.5F, -3.5F, -2.5F));

        /* Die vier Beine und ihre Fuesse. Bildstellen und Winkel wie im Original. */
        int[][] beinBild = { {25, 9}, {41, 9}, {1, 17}, {17, 17} };
        int[][] fussBild = { {33, 17}, {57, 9}, {41, 17}, {49, 17} };

        for(int i = 0; i < 4; i++) {
            teile.addOrReplaceChild(BEINE[i],
                    CubeListBuilder.create().texOffs(beinBild[i][0], beinBild[i][1]).addBox(-0.5F, 0F, 2F, 1, 1, 3),
                    PartPose.offsetAndRotation(0F, -3F, 0F, -0.17453293F, WINKEL[i], 0F));
            teile.addOrReplaceChild(FUESSE[i],
                    CubeListBuilder.create().texOffs(fussBild[i][0], fussBild[i][1]).addBox(-0.5F, 1F, 4F, 1, 3, 1),
                    PartPose.offsetAndRotation(0F, -3F, 0F, 0.17453293F, WINKEL[i], 0F));
        }

        /* Die vier Zangen vorn. Sie ruehren sich nicht. */
        float[] zangenWinkel = { -0.6981317F, 0.87266463F, -2.26892803F, 2.44346095F };
        int[][] zangenBild = { {17, 1}, {33, 9}, {49, 9}, {9, 17} };
        for(int i = 0; i < 4; i++) {
            teile.addOrReplaceChild("fang_" + i,
                    CubeListBuilder.create().texOffs(zangenBild[i][0], zangenBild[i][1]).addBox(-0.5F, 0F, 1.5F, 1, 1, 1),
                    PartPose.offsetAndRotation(0F, -3F, 0F, -0.43633231F, zangenWinkel[i], 0F));
        }

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(CyberCrab krabbe, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

        /* Der eine Wert des Originals -- alles andere stand dort auskommentiert. */
        float schwung = -(Mth.cos(limbSwing * 0.6662F * 2.0F) * 0.4F) * limbSwingAmount * 1.5F;

        /* Vorzeichen wie im Original: erstes und viertes Bein vor, zweites und drittes zurueck. */
        float[] vorzeichen = { 1F, -1F, -1F, 1F };

        for(int i = 0; i < 4; i++) {
            float gierung = WINKEL[i] + schwung * vorzeichen[i];
            this.root.getChild(BEINE[i]).yRot = gierung;
            this.root.getChild(FUESSE[i]).yRot = gierung;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {

        pose.pushPose();
        /* Aus renderAll des Originals: anderthalb Bloecke hoch und eine Vierteldrehung. */
        pose.translate(0F, 1.5F, 0F);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90F));
        this.root.render(pose, buffer, light, overlay, color);
        pose.popPose();
    }
}
