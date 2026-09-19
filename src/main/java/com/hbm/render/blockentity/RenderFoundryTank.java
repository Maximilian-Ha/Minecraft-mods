package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.FoundryTankBlockEntity;
import com.hbm.blocks.machine.FoundryTankBlock;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * Zeichnet die Schmelze im Giessereitank.
 *
 * ABWEICHUNG: das Original zeichnet den Tank selbst im Renderer, samt aller Waende. Im Port ist
 * er ein gewoehnliches Blockmodell, das seine Nachbarschaft aus dem Blockzustand kennt -- hier
 * bleibt nur die Oberflaeche der Schmelze.
 *
 * Die Seitenflaechen entstehen nur dort, wo ein weiterer Tank steht. Das klingt verkehrt herum,
 * ist aber richtig: an einer geschlossenen Wand sieht sie ohnehin niemand, und zwischen zwei
 * Tanks mit ungleichem Stand waere sonst eine Luecke zu sehen. Ebenso das Original.
 */
public class RenderFoundryTank extends BlockEntityRendererNT<FoundryTankBlockEntity> {

    public static final ResourceLocation LAVA = NuclearTechMod.withDefaultNamespace("textures/particle/lava_gray.png");

    @Override
    public BlockEntityRenderer<FoundryTankBlockEntity> create(Context context) {
        return new RenderFoundryTank();
    }

    @Override
    public void render(FoundryTankBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        if(be.type == null || be.amount <= 0) return;

        BlockState state = be.getBlockState();
        if(!(state.getBlock() instanceof FoundryTankBlock)) return;

        float oben = (float) be.hoeheDerOberflaeche();
        float unten = state.getValue(FoundryTankBlock.DOWN) ? 0F : 0.125F;

        /*
         * Zweimal aufgehellt, wie im Original: erst ueber Color.brighter, dann noch einmal um
         * drei Zehntel in Richtung Weiss. Die Schmelze soll deutlich heller leuchten als das
         * Material, aus dem sie kommt.
         */
        Color color = new Color(be.type.moltenColor).brighter();
        float r = (255F - (255F - color.getRed()) * 0.7F) / 255F;
        float g = (255F - (255F - color.getGreen()) * 0.7F) / 255F;
        float b = (255F - (255F - color.getBlue()) * 0.7F) / 255F;

        FullBright.enable();
        RenderContext.setLightning(false);

        VertexConsumer consumer = buffer.getBuffer(NtmRenderTypes.entitySmoth(LAVA));
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        // Die Oberflaeche.
        vertex(consumer, matrix, 0F, oben, 0F, 0F, 1F, r, g, b);
        vertex(consumer, matrix, 0F, oben, 1F, 1F, 1F, r, g, b);
        vertex(consumer, matrix, 1F, oben, 1F, 1F, 0F, r, g, b);
        vertex(consumer, matrix, 1F, oben, 0F, 0F, 0F, r, g, b);

        this.seite(consumer, matrix, state, FoundryTankBlock.EAST, unten, oben, r, g, b);
        this.seite(consumer, matrix, state, FoundryTankBlock.WEST, unten, oben, r, g, b);
        this.seite(consumer, matrix, state, FoundryTankBlock.SOUTH, unten, oben, r, g, b);
        this.seite(consumer, matrix, state, FoundryTankBlock.NORTH, unten, oben, r, g, b);

        RenderContext.setLightning(true);
        FullBright.disable();
    }

    private void seite(VertexConsumer consumer, Matrix4f matrix, BlockState state, BooleanProperty seite,
                       float unten, float oben, float r, float g, float b) {

        if(!state.getValue(seite)) return;

        float x = seite == FoundryTankBlock.EAST ? 1F : 0F;
        float z = seite == FoundryTankBlock.SOUTH ? 1F : 0F;
        boolean entlangX = seite == FoundryTankBlock.NORTH || seite == FoundryTankBlock.SOUTH;

        if(entlangX) {
            vertex(consumer, matrix, 0F, unten, z, 0F, unten, r, g, b);
            vertex(consumer, matrix, 1F, unten, z, 1F, unten, r, g, b);
            vertex(consumer, matrix, 1F, oben, z, 1F, oben, r, g, b);
            vertex(consumer, matrix, 0F, oben, z, 0F, oben, r, g, b);
        } else {
            vertex(consumer, matrix, x, unten, 0F, 0F, unten, r, g, b);
            vertex(consumer, matrix, x, unten, 1F, 1F, unten, r, g, b);
            vertex(consumer, matrix, x, oben, 1F, 1F, oben, r, g, b);
            vertex(consumer, matrix, x, oben, 0F, 0F, oben, r, g, b);
        }
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
