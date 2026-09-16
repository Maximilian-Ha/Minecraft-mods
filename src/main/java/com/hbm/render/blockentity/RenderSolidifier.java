package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.oil.MachineSolidifierBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.awt.Color;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderSolidifier.
 *
 * Drei Teile: das Gehaeuse, die Saeule Fluessigkeit darin -- in der Farbe des Tanks und
 * auf den Fuellstand gestaucht -- und darueber die durchscheinende Glashaube.
 */
public class RenderSolidifier extends BlockEntityRendererNT<MachineSolidifierBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineSolidifierBlockEntity> create(Context context) {
        return new RenderSolidifier();
    }

    @Override
    public void render(MachineSolidifierBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);

        RenderContext.translate(0.5F, 0F, 0.5F);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            default -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        RenderSystem.disableCull();

        bindTexture(ResourceManager.SOLIDIFIER_TEX);
        ResourceManager.solidifier.renderPart("Main");

        if(be.tank.getFill() > 0) {

            RenderContext.setLightning(false);

            Color color = new Color(be.tank.getTankType().getColor());
            RenderContext.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);

            /* Die Saeule haengt oben an 1,25 und wird von dort nach unten gestaucht. */
            float height = (float) be.tank.getFill() / (float) be.tank.getMaxFill();
            RenderContext.pushPose();
            RenderContext.translate(0F, 1.25F, 0F);
            RenderContext.scale(1F, height, 1F);
            RenderContext.translate(0F, -1.25F, 0F);
            ResourceManager.solidifier.renderPart("Fluid");
            RenderContext.popPose();

            RenderContext.setColor(1F, 1F, 1F, 1F);
            RenderContext.setLightning(true);
        }

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderContext.setLightning(false);
        RenderContext.setColor(0.75F, 1F, 1F, 0.15F);

        ResourceManager.solidifier.renderPart("Glass");

        RenderContext.setColor(1F, 1F, 1F, 1F);
        RenderContext.setLightning(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);

        RenderSystem.enableCull();
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles oberhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block hoch.
     */
    @Override
    public AABB getRenderBoundingBox(MachineSolidifierBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_SOLIDIFIER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -4F, 0F);
                RenderContext.scale(4F, 4F, 4F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
                RenderSystem.disableCull();

                bindTexture(ResourceManager.SOLIDIFIER_TEX);
                ResourceManager.solidifier.renderPart("Main");

                RenderSystem.enableCull();
            }
        };
    }
}
