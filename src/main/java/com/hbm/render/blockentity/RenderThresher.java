package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineThresherBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.MachineThresherBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderThresher.
 *
 * Drehwinkel je Blickrichtung, Armkinematik und Radlauf unveraendert.
 * Das Original schaltet das Backface-Culling ausdruecklich EIN; das ist in 1.21
 * ohnehin der Ausgangszustand, deshalb steht hier kein RenderSystem-Aufruf.
 * glShadeModel(GL_SMOOTH) hat in 1.21 keine Entsprechung und entfaellt.
 */
public class RenderThresher extends BlockEntityRendererNT<MachineThresherBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineThresherBlockEntity> create(Context context) { return new RenderThresher(); }

    @Override
    public void render(MachineThresherBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(MachineThresherBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            default -> { }
        }

        double angle = be.prevAngle + (be.angle - be.prevAngle) * partialTicks;
        double spin = be.lastSpin + (be.spin - be.lastSpin) * partialTicks;
        double engine = be.isOn && be.getLevel() != null
                ? Math.sin(be.getLevel().getGameTime() * 2 % (Math.PI * 2) + partialTicks)
                : 0;

        this.renderCommon(82.5 - angle, spin, engine);
    }

    private void renderCommon(double angle, double spin, double engine) {

        this.bindTexture(ResourceManager.THRESHER_TEX);
        ResourceManager.thresher.renderPart("Base");

        RenderContext.pushPose();
        RenderContext.translate(0F, (float) (engine * 0.01), 0F);
        ResourceManager.thresher.renderPart("Engine");
        RenderContext.popPose();

        RenderContext.translate(0F, 0.5F, -1F);
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) angle));
        RenderContext.translate(0F, -0.5F, 1F);
        ResourceManager.thresher.renderPart("ArmUpper");

        RenderContext.translate(0F, 0.5F, -5F);
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) (angle * -2)));
        RenderContext.translate(0F, -0.5F, 5F);
        RenderContext.translate(-0.01F, 0F, 0F);
        ResourceManager.thresher.renderPart("ArmLower");
        RenderContext.translate(0.01F, 0F, 0F);

        RenderContext.translate(0F, 0.5F, -9F);
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) angle));
        RenderContext.translate(0F, -0.5F, 9F);
        RenderContext.translate(0.01F, 0F, 0F);
        ResourceManager.thresher.renderPart("Front");

        RenderContext.translate(0F, 0.5F, -11F);
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) -spin));
        RenderContext.translate(0F, -0.5F, 11F);
        ResourceManager.thresher.renderPart("Wheel");
    }

    @Override
    public AABB getRenderBoundingBox(MachineThresherBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_THRESHER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, 4F, -8F);
                RenderContext.scale(4.5F, 4.5F, 4.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.mulPose(Axis.YN.rotationDegrees(90F));
                RenderThresher.this.renderCommon(80D, System.currentTimeMillis() % 3600 * 0.25D, 0);
            }
        };
    }
}
