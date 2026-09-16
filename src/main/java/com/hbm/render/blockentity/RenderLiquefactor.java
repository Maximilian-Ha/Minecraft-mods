package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.oil.MachineLiquefactorBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.awt.Color;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderLiquefactor.
 *
 * Wie beim Verfestiger: Gehaeuse, eingefaerbte Fluessigkeitssaeule, Glashaube. Das Original
 * dreht den Verfluessiger nicht nach der Blickrichtung -- hier ebenso wenig.
 */
public class RenderLiquefactor extends BlockEntityRendererNT<MachineLiquefactorBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineLiquefactorBlockEntity> create(Context context) {
        return new RenderLiquefactor();
    }

    @Override
    public void render(MachineLiquefactorBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderSystem.disableCull();

        bindTexture(ResourceManager.LIQUEFACTOR_TEX);
        ResourceManager.liquefactor.renderPart("Main");

        if(be.tank.getFill() > 0) {

            RenderContext.setLightning(false);

            Color color = new Color(be.tank.getTankType().getColor());
            RenderContext.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);

            float height = (float) be.tank.getFill() / (float) be.tank.getMaxFill();
            RenderContext.pushPose();
            RenderContext.translate(0F, 1F, 0F);
            RenderContext.scale(1F, height, 1F);
            RenderContext.translate(0F, -1F, 0F);
            ResourceManager.liquefactor.renderPart("Fluid");
            RenderContext.popPose();

            RenderContext.setColor(1F, 1F, 1F, 1F);
            RenderContext.setLightning(true);
        }

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderContext.setLightning(false);
        RenderContext.setColor(0.75F, 1F, 1F, 0.15F);

        ResourceManager.liquefactor.renderPart("Glass");

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
    public AABB getRenderBoundingBox(MachineLiquefactorBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_LIQUEFACTOR.asItem();
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

                bindTexture(ResourceManager.LIQUEFACTOR_TEX);
                ResourceManager.liquefactor.renderPart("Main");

                RenderSystem.enableCull();
            }
        };
    }
}
