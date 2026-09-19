package com.hbm.render.model;

import com.hbm.main.NuclearTechMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelSatelliteReceiver.
 *
 * Neun Kaesten aus einem Techne-Export von 2015: ein Sockel und die Schuessel, die um
 * -15 Grad nach oben und -25 Grad zur Seite steht. Solche Drehungen lassen sich in einem
 * Blockmodell nicht ausdruecken -- deshalb eine Modellschicht und ein eigener Darsteller.
 *
 * Das Original setzt auf jedem Kasten mirror = true, aber erst NACH addBox. In 1.7.10 liest
 * addBox das Feld, also hat die Zeile keine Wirkung. Hier steht deshalb kein mirror().
 */
public class ModelSatelliteReceiver {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NuclearTechMod.MODID, "pole_satellite_receiver"), "main");

    /** Die gemeinsame Lage der acht Schuesselteile: -15 Grad hoch, -25 Grad zur Seite. */
    private static PartPose schuessel() {
        return PartPose.offsetAndRotation(-3.0F, 6.0F, 0.0F, -0.2617994F, -0.4363323F, 0.0F);
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition wurzel = mesh.getRoot();

        wurzel.addOrReplaceChild("sockel",
                CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 12.0F, 16.0F, 12.0F),
                PartPose.offset(-6.0F, 8.0F, -6.0F));

        wurzel.addOrReplaceChild("schirm",
                CubeListBuilder.create().texOffs(10, 28).addBox(3.0F, 9.0F, -8.0F, 8.0F, 8.0F, 2.0F),
                schuessel());

        wurzel.addOrReplaceChild("rand_unten",
                CubeListBuilder.create().texOffs(0, 39).addBox(3.0F, 7.0F, -10.0F, 8.0F, 2.0F, 3.0F),
                schuessel());

        wurzel.addOrReplaceChild("rand_links",
                CubeListBuilder.create().texOffs(0, 28).addBox(1.0F, 9.0F, -10.0F, 2.0F, 8.0F, 3.0F),
                schuessel());

        wurzel.addOrReplaceChild("rand_rechts",
                CubeListBuilder.create().texOffs(0, 28).addBox(11.0F, 9.0F, -10.0F, 2.0F, 8.0F, 3.0F),
                schuessel());

        wurzel.addOrReplaceChild("rand_oben",
                CubeListBuilder.create().texOffs(0, 39).addBox(3.0F, 17.0F, -10.0F, 8.0F, 2.0F, 3.0F),
                schuessel());

        wurzel.addOrReplaceChild("hals",
                CubeListBuilder.create().texOffs(0, 44).addBox(6.0F, 12.0F, -11.0F, 2.0F, 2.0F, 3.0F),
                schuessel());

        wurzel.addOrReplaceChild("stab",
                CubeListBuilder.create().texOffs(0, 49).addBox(6.5F, 12.5F, -14.0F, 1.0F, 1.0F, 3.0F),
                schuessel());

        wurzel.addOrReplaceChild("kopf",
                CubeListBuilder.create().texOffs(0, 53).addBox(6.0F, 12.0F, -16.0F, 2.0F, 2.0F, 2.0F),
                schuessel());

        return LayerDefinition.create(mesh, 64, 64);
    }
}
