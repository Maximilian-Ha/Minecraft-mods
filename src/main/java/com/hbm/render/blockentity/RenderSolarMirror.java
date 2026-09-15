package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.SolarMirrorBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Zeichnet den Heliostatspiegel. Im Original ein statischer Blockrenderer, der die
 * Spiegelflaeche im Chunkmesh verdreht hat; hier uebernimmt das der BlockEntityRenderer.
 */
public class RenderSolarMirror extends BlockEntityRendererNT<SolarMirrorBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<SolarMirrorBlockEntity> create(Context context) {
        return new RenderSolarMirror();
    }

    @Override
    public void render(SolarMirrorBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0.0F, 0.5F);

        bindTexture(ResourceManager.SOLAR_MIRROR_TEX);
        ResourceManager.solar_mirror.renderPart("Base");

        BlockPos pos = be.getBlockPos();

        if(be.tY <= pos.getY()) {
            // Kein gueltiges Ziel: Spiegel bleibt in Ruhelage
            ResourceManager.solar_mirror.renderPart("Mirror");
            return;
        }

        int dx = be.tX - pos.getX();
        int dy = be.tY - pos.getY();
        int dz = be.tZ - pos.getZ();

        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double pitch = -Math.asin(dy / dist) + Math.PI / 2D;
        double yaw = -Math.atan2(dz, dx) - Math.PI / 2D;

        // Drehpunkt der Spiegelflaeche liegt einen Block ueber dem Blockursprung
        RenderContext.pushPose();
        RenderContext.translate(0.0F, 1.0F, 0.0F);
        RenderContext.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(yaw)));
        RenderContext.mulPose(Axis.XN.rotationDegrees((float) Math.toDegrees(pitch)));
        RenderContext.translate(0.0F, -1.0F, 0.0F);
        ResourceManager.solar_mirror.renderPart("Mirror");
        RenderContext.popPose();
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles ausserhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block gross.
     */
    @Override
    public AABB getRenderBoundingBox(SolarMirrorBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.SOLAR_MIRROR.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0.0F, -3.0F, 0.0F);
                RenderContext.scale(8.0F, 8.0F, 8.0F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.SOLAR_MIRROR_TEX);
                ResourceManager.solar_mirror.renderPart("Base");
                RenderContext.translate(0.0F, 1.0F, 0.0F);
                RenderContext.mulPose(Axis.ZN.rotationDegrees(45F));
                RenderContext.translate(0.0F, -1.0F, 0.0F);
                ResourceManager.solar_mirror.renderPart("Mirror");
            }
        };
    }
}
