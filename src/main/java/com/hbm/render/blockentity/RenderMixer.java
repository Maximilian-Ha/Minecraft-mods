package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineMixerBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderMixer.
 * Der Fuellstandskoerper wird mit der Farbe des Ausgangstanks eingefaerbt und
 * ueber alle belegten Tanks skaliert -- wie im Original.
 */
public class RenderMixer extends BlockEntityRendererNT<MachineMixerBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineMixerBlockEntity> create(Context context) {
        return new RenderMixer();
    }

    @Override
    public void render(MachineMixerBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderSystem.disableCull();

        bindTexture(ResourceManager.MIXER_TEX);
        ResourceManager.mixer.renderPart("Main");

        float rotation = be.prevRotation + (be.rotation - be.prevRotation) * partialTicks;

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YN.rotationDegrees(rotation));
        ResourceManager.mixer.renderPart("Mixer");
        RenderContext.popPose();

        int totalFill = 0;
        int totalMax = 0;

        for(FluidTank tank : be.tanks) {
            if(tank.getTankType() != Fluids.NONE) {
                totalFill += tank.getFill();
                totalMax += tank.getMaxFill();
            }
        }

        if(totalFill > 0 && totalMax > 0) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderContext.setLightning(false);

            Color color = new Color(be.tanks[2].getTankType().getColor());
            RenderContext.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 0.75F);

            RenderContext.translate(0F, 1F, 0F);
            RenderContext.scale(1F, (float) totalFill / (float) totalMax * 0.99F, 1F);
            RenderContext.translate(0F, -1F, 0F);
            ResourceManager.mixer.renderPart("Fluid");

            RenderContext.setColor(1F, 1F, 1F, 1F);
            RenderContext.setLightning(true);
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }

        RenderSystem.enableCull();
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles oberhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block hoch. Die Maschine liefert
     * ihre tatsaechliche Ausdehnung selbst.
     */
    @Override
    public AABB getRenderBoundingBox(MachineMixerBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_MIXER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -5F, 0F);
                RenderContext.scale(5F, 5F, 5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
                RenderSystem.disableCull();

                bindTexture(ResourceManager.MIXER_TEX);
                ResourceManager.mixer.renderPart("Main");
                ResourceManager.mixer.renderPart("Mixer");

                RenderSystem.enableCull();
            }
        };
    }
}
