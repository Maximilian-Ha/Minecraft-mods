package com.hbm.render.blockentity;

import api.hbm.entity.RadarEntry;
import com.hbm.blockentity.machine.RadarScreenBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.screens.MachineRadarScreen;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRadarScreen.
 *
 * Runde 161: der Block war seit Runde 126 vollstaendig da -- Modell, Haut und Blockentitaet
 * eingeschlossen -- nur dieser Darsteller fehlte. Ohne ihn blieb der Schirm unsichtbar, in der
 * Welt wie im Inventar, denn sein Blockmodell traegt nur eine Partikeltextur.
 *
 * Die Scheibe wird nicht aus dem Modell gezeichnet, sondern als rohe Vierecke darueber gelegt:
 * ein gruener Ablaufstreifen, die Punkte der erfassten Ziele, und wenn kein Radar angeschlossen
 * ist, ein Rauschbild aus derselben Oberflaechentextur, die auch die Radarkonsole benutzt.
 */
public class RenderRadarScreen extends BlockEntityRendererNT<RadarScreenBlockEntity> implements IBEWLRProvider {

    /** Die Punkte liegen im Atlas nebeneinander, eine Zeile je Gefahrenstufe. */
    private static final float BLIP_U0 = 216F / 256F;
    private static final float BLIP_U1 = 224F / 256F;

    @Override public BlockEntityRenderer<RadarScreenBlockEntity> create(Context context) { return new RenderRadarScreen(); }

    @Override
    public void render(RadarScreenBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        if(be.getLevel() == null) return;

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderSystem.disableCull();

        /* Zuordnung woertlich aus dem Original, dort ueber getBlockMetadata() - offset. */
        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            default -> { }
        }

        bindTexture(ResourceManager.RADAR_SCREEN_TEX);
        ResourceManager.radar_screen.renderAll();

        if(be.linked) {
            this.renderSweep(be, partialTicks);
            this.renderBlips(be);
        } else {
            this.renderStatic(be);
        }

        RenderSystem.enableCull();
    }

    /** Der gruene Streifen, der langsam ueber die Scheibe nach unten wandert. */
    private void renderSweep(RadarScreenBlockEntity be, float partialTicks) {

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        float offset = (be.getLevel().getGameTime() % 56L + partialTicks) / 30F;
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        /* Oben durchsichtig, unten leicht deckend -- so laeuft der Streifen aus. */
        builder.addVertex(matrix, 0.38F, 2F - offset, 1.375F).setColor(0F, 1F, 0F, 0F);
        builder.addVertex(matrix, 0.38F, 2F - offset, -0.375F).setColor(0F, 1F, 0F, 0F);
        builder.addVertex(matrix, 0.38F, 2F - offset - 0.125F, -0.375F).setColor(0F, 1F, 0F, 50F / 255F);
        builder.addVertex(matrix, 0.38F, 2F - offset - 0.125F, 1.375F).setColor(0F, 1F, 0F, 50F / 255F);

        BufferUploader.drawWithShader(builder.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    /** Ein Punkt je erfasstem Ziel, die Zeile im Atlas richtet sich nach der Gefahrenstufe. */
    private void renderBlips(RadarScreenBlockEntity be) {

        if(be.entries.isEmpty()) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MachineRadarScreen.TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        Matrix4f matrix = RenderContext.poseStack().last().pose();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        for(RadarEntry entry : be.entries) {

            float sX = (float) (entry.pos.getX() - be.refX) / (be.range + 1F) * 0.875F;
            float sZ = (float) (entry.pos.getZ() - be.refZ) / (be.range + 1F) * 0.875F;
            float size = 0.0625F;

            float v0 = entry.blipLevel * 8F / 256F;
            float v1 = (entry.blipLevel * 8F + 8F) / 256F;

            builder.addVertex(matrix, 0.38F, 1F - sZ + size, 0.5F - sX + size).setUv(BLIP_U0, v1);
            builder.addVertex(matrix, 0.38F, 1F - sZ + size, 0.5F - sX - size).setUv(BLIP_U1, v1);
            builder.addVertex(matrix, 0.38F, 1F - sZ - size, 0.5F - sX - size).setUv(BLIP_U1, v0);
            builder.addVertex(matrix, 0.38F, 1F - sZ - size, 0.5F - sX + size).setUv(BLIP_U0, v0);
        }

        BufferUploader.drawWithShader(builder.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    /** Ohne angeschlossenes Radar zeigt der Schirm Rauschen -- dieselben Kacheln wie die Konsole. */
    private void renderStatic(RadarScreenBlockEntity be) {

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MachineRadarScreen.TEXTURE);

        int offset = 118 + be.getLevel().random.nextInt(81);
        float v0 = offset / 256F;
        float v1 = (offset + 40F) / 256F;

        Matrix4f matrix = RenderContext.poseStack().last().pose();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        builder.addVertex(matrix, 0.38F, 1.875F, 1.375F).setUv(BLIP_U0, v1);
        builder.addVertex(matrix, 0.38F, 1.875F, -0.375F).setUv(1F, v1);
        builder.addVertex(matrix, 0.38F, 0.125F, -0.375F).setUv(1F, v0);
        builder.addVertex(matrix, 0.38F, 0.125F, 1.375F).setUv(BLIP_U0, v0);

        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    // Kein Zwischenspeichern in einem Feld: BlockEntityRenderers legt pro BlockEntityType
    // genau EINEN Darsteller an, den sich alle Schirme teilen.
    @Override
    public AABB getRenderBoundingBox(RadarScreenBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.RADAR_SCREEN.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 0F);
                RenderContext.scale(5.5F, 5.5F, 5.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, 0F, -0.5F);
                bindTexture(ResourceManager.RADAR_SCREEN_TEX);
                ResourceManager.radar_screen.renderAll();
            }
        };
    }
}
