package com.hbm.render.item;

import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.ItemRenderBoltgun.
 *
 * Zwei Teile: das Gehaeuse und das Rohr. NUR IN DER ERSTEN PERSON faehrt das Rohr zurueck --
 * ueberall sonst steht es fest, und deshalb wird es dort zusammen mit dem Gehaeuse gezeichnet.
 * Das ist die Aufteilung des Originals: in der ersten Person zeichnet es das Rohr VOR dem
 * Gehaeuse und mit eigener Verschiebung, sonst beide hintereinander weg.
 *
 * ABWEICHUNG: das Original legt fuer jede Haltung eine fertige Matrix aus ItemRenderFrames17
 * an -- eine Nachbildung der Gegenstandshaltungen von 1.7.10, die es in 1.21 nicht braucht,
 * weil die Haltung schon im Stapel steht. Die Zahlen hier sind deshalb die des Ports, nicht
 * die des Originals; uebernommen ist, was sich uebernehmen laesst: die Reihenfolge der
 * Drehungen und das Verhaeltnis der Groessen.
 */
public class RenderBoltgunItem extends BlockEntityWithoutLevelRenderer {

    public RenderBoltgunItem() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        RenderContext.setup(poseStack, packedLight, packedOverlay);

        if(displayContext != ItemDisplayContext.GUI) RenderContext.translate(0.5F, 0F, 0.5F);

        boolean ersteHand = displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        switch(displayContext) {
            case FIRST_PERSON_RIGHT_HAND -> {
                RenderContext.translate(-0.1F, 0.55F, 0.1F);
                RenderContext.scale(0.15F, 0.15F, 0.15F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(80F));
                RenderContext.mulPose(Axis.ZP.rotationDegrees(15F));
            }
            case FIRST_PERSON_LEFT_HAND -> {
                RenderContext.translate(0.1F, 0.55F, 0.1F);
                RenderContext.scale(-0.15F, 0.15F, 0.15F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(80F));
                RenderContext.mulPose(Axis.ZP.rotationDegrees(15F));
            }
            case THIRD_PERSON_RIGHT_HAND, HEAD -> {
                RenderContext.translate(0F, 0.4F, 0F);
                RenderContext.scale(0.15F, 0.15F, 0.15F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(10F));
                RenderContext.mulPose(Axis.ZP.rotationDegrees(10F));
                RenderContext.mulPose(Axis.XP.rotationDegrees(10F));
            }
            case THIRD_PERSON_LEFT_HAND -> {
                RenderContext.translate(0F, 0.4F, 0F);
                RenderContext.scale(-0.15F, 0.15F, 0.15F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(10F));
                RenderContext.mulPose(Axis.ZP.rotationDegrees(10F));
                RenderContext.mulPose(Axis.XP.rotationDegrees(10F));
            }
            case GROUND -> {
                RenderContext.translate(0F, 0.25F, 0F);
                RenderContext.scale(0.1F, 0.1F, 0.1F);
            }
            case FIXED -> {
                RenderContext.translate(0F, 0.25F, 0F);
                RenderContext.scale(0.1F, 0.1F, 0.1F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            }
            case GUI -> {
                float s = 0.115F;
                RenderContext.scale(s, -s, -s);
                RenderContext.translate(3.5F, -6.5F, 0F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(-90F));
                RenderContext.mulPose(Axis.XP.rotationDegrees(-135F));
            }
            default -> { }
        }

        RenderSystem.setShaderTexture(0, ResourceManager.BOLTGUN_TEX);

        if(ersteHand) {
            RenderContext.pushPose();
            /* Der Rueckstoss sitzt auf der x-Achse des Busses und schiebt das Rohr nach hinten. */
            float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
            RenderContext.translate(0F, 0F, -recoil[0]);
            ResourceManager.boltgun.renderPart("Barrel");
            RenderContext.popPose();

            ResourceManager.boltgun.renderPart("Gun");
        } else {
            ResourceManager.boltgun.renderPart("Gun");
            ResourceManager.boltgun.renderPart("Barrel");
        }

        RenderContext.end();
    }
}
