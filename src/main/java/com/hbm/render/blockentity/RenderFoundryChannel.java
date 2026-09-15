package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.FoundryChannelBlockEntity;
import com.hbm.blocks.machine.FoundryChannelBlock;
import com.hbm.main.NuclearTechMod;
import com.hbm.render.NtmRenderTypes;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * Zeichnet die Schmelze in der Rinne.
 *
 * ABWEICHUNG: das Original zeichnet die Rinne selbst im Renderer, samt aller Verbindungsstuecke.
 * Im Port ist sie ein gewoehnliches Blockmodell, das die vier Verbindungen aus dem Blockzustand
 * kennt -- hier bleibt nur die Oberflaeche der Schmelze, und die waechst in die angeschlossenen
 * Arme hinein, damit der Strang durchgehend aussieht.
 */
public class RenderFoundryChannel extends BlockEntityRendererNT<FoundryChannelBlockEntity> {

    public static final ResourceLocation LAVA = NuclearTechMod.withDefaultNamespace("textures/particle/lava_gray.png");

    @Override
    public BlockEntityRenderer<FoundryChannelBlockEntity> create(Context context) {
        return new RenderFoundryChannel();
    }

    @Override
    public void render(FoundryChannelBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        if(be.type == null || be.amount <= 0) return;

        BlockState state = be.getBlockState();
        if(!(state.getBlock() instanceof FoundryChannelBlock)) return;

        double min = 0.3125D;
        double max = 0.6875D;

        float minX = (float) (state.getValue(FoundryChannelBlock.WEST) ? 0D : min);
        float maxX = (float) (state.getValue(FoundryChannelBlock.EAST) ? 1D : max);
        float minZ = (float) (state.getValue(FoundryChannelBlock.NORTH) ? 0D : min);
        float maxZ = (float) (state.getValue(FoundryChannelBlock.SOUTH) ? 1D : max);

        // 0.125 ist der Boden der Rinne, 0.375 ihr Rand
        float fill = 0.125F + be.amount * 0.25F / be.getCapacity();

        Color color = new Color(be.type.moltenColor);
        float r = color.getRed() / 255F;
        float g = color.getGreen() / 255F;
        float b = color.getBlue() / 255F;

        FullBright.enable();
        RenderContext.setLightning(false);

        VertexConsumer consumer = buffer.getBuffer(NtmRenderTypes.entitySmoth(LAVA));
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        vertex(consumer, matrix, minX, fill, minZ, minZ, maxX, r, g, b);
        vertex(consumer, matrix, minX, fill, maxZ, maxZ, maxX, r, g, b);
        vertex(consumer, matrix, maxX, fill, maxZ, maxZ, minX, r, g, b);
        vertex(consumer, matrix, maxX, fill, minZ, minZ, minX, r, g, b);

        RenderContext.setLightning(true);
        FullBright.disable();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float u, float v, float r, float g, float b) {
        consumer.addVertex(matrix, x, y, z)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setColor(r, g, b, 1F)
                .setNormal(0F, 1F, 0F)
                .setLight(240);
    }
}
