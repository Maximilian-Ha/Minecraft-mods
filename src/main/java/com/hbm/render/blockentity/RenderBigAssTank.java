package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.storage.MachineBigAssTankBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.DiamondPronter;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderBigAssTank.
 */
public class RenderBigAssTank extends BlockEntityRendererNT<MachineBigAssTankBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineBigAssTankBlockEntity> create(Context context) { return new RenderBigAssTank(); }

    @Override
    public void render(MachineBigAssTankBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        /*
         * Das Kippen kommt VOR der Blickrichtung, so wie im Original. Andersherum kippte der
         * Tank je nach Aufstellrichtung in eine andere Ecke.
         */
        if(be.tilted) {
            RenderContext.translate(0F, -1F, 0F);
            RenderContext.mulPose(Axis.ZP.rotationDegrees(10F));
            RenderContext.mulPose(Axis.YP.rotationDegrees(5F));
        }

        /*
         * Die Zuordnung stammt woertlich aus dem Original und ist NICHT die von RenderFluidTank.
         * Die lange Achse des Modells mit den beiden Stutzen liegt auf X, deshalb ist WEST die
         * Nullstellung.
         */
        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            default -> { }
        }

        bindTexture(ResourceManager.BIGASSTANK_TEX);
        ResourceManager.bigasstank.renderAll();

        FluidType type = be.tank.getTankType();
        if(type == Fluids.NONE) return;

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees(22.5F));
        for(int i = 0; i < 2; i++) {
            RenderContext.pushPose();
            RenderContext.translate(5.5F, 2F, 0F);
            DiamondPronter.pront(buffer, type.poison, type.flammability, type.reactivity, type.symbol);
            RenderContext.popPose();
            RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
        }
        RenderContext.popPose();

        this.renderLevel(be, type, partialTicks);
    }

    /**
     * Die beiden Fuellstandsfenster an den Stirnseiten. Das Original zeichnet sie als rohe
     * Vierecke mit der Fluidtextur und einer langsam wandernden Textur -- ohne Beleuchtung,
     * damit sie auch nachts zu sehen sind. Der Positionsschattierer bringt genau das mit:
     * er kennt keine Beleuchtung.
     */
    private void renderLevel(MachineBigAssTankBlockEntity be, FluidType type, float partialTicks) {

        if(be.getLevel() == null) return;
        if(be.tank.getFill() <= 0) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, type.getTexture());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        float height = (float) be.tank.getFill() * 1.5F / (float) be.tank.getMaxFill();
        float off = 5.9375F;
        float scale = 0.5F;

        float minU = -((be.getLevel().getGameTime() % 250L + partialTicks) / 250F) % 1F;
        float maxU = minU + scale;
        float v = -height * 2F * scale;

        Matrix4f matrix = RenderContext.poseStack().last().pose();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        builder.addVertex(matrix, -off, 1.75F, -0.25F).setUv(minU, 0F);
        builder.addVertex(matrix, -off, 1.75F + height, -0.25F).setUv(minU, v);
        builder.addVertex(matrix, -off, 1.75F + height, 0.25F).setUv(maxU, v);
        builder.addVertex(matrix, -off, 1.75F, 0.25F).setUv(maxU, 0F);

        builder.addVertex(matrix, off, 1.75F, -0.25F).setUv(maxU, 0F);
        builder.addVertex(matrix, off, 1.75F + height, -0.25F).setUv(maxU, v);
        builder.addVertex(matrix, off, 1.75F + height, 0.25F).setUv(minU, v);
        builder.addVertex(matrix, off, 1.75F, 0.25F).setUv(minU, 0F);

        BufferUploader.drawWithShader(builder.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // Kein Zwischenspeichern in einem Feld: BlockEntityRenderers legt pro BlockEntityType
    // genau EINEN Darsteller an, den sich alle Tanks teilen.
    @Override
    public AABB getRenderBoundingBox(MachineBigAssTankBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_BIGASSTANK.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1F, 0F);
                RenderContext.scale(2.5F, 2.5F, 2.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                bindTexture(ResourceManager.BIGASSTANK_TEX);
                ResourceManager.bigasstank.renderAll();
            }
        };
    }
}
