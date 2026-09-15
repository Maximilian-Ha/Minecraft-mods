package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.SolarBoilerBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.NtmRenderTypes;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class RenderSolarBoiler extends BlockEntityRendererNT<SolarBoilerBlockEntity> implements IBEWLRProvider {

    /** todo config: im Original ClientConfig.RENDER_HELIOSTAT_BEAM_LIMIT */
    private static final int BEAM_LIMIT = 250;

    @Override
    public BlockEntityRenderer<SolarBoilerBlockEntity> create(Context context) {
        return new RenderSolarBoiler();
    }

    @Override
    public void render(SolarBoilerBlockEntity be, MultiBufferSource buffer, float partialTicks) {
        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);

        RenderContext.translate(0.5F, 0.0F, 0.5F);

        RenderContext.pushPose();
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            default -> { }
        }

        bindTexture(ResourceManager.SOLAR_TEX);
        ResourceManager.solar_boiler.renderPart("Base");
        RenderContext.popPose();

        if(Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST) {
            this.renderBeams(be, buffer);
        }
    }

    /** Die gebuendelten Lichtstrahlen von jedem gemeldeten Spiegel zum Kessel */
    private void renderBeams(SolarBoilerBlockEntity be, MultiBufferSource buffer) {

        if(be.secondary.isEmpty()) return;

        VertexConsumer consumer = buffer.getBuffer(NtmRenderTypes.GLOW);

        BlockPos boilerPos = be.getBlockPos();
        int beamCount = 0;

        for(BlockPos co : be.secondary) {
            beamCount++;

            if(beamCount > BEAM_LIMIT) break;

            int dx = boilerPos.getX() - co.getX();
            int dy = boilerPos.getY() - co.getY();
            int dz = boilerPos.getZ() - co.getZ();

            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            float min = 0.005F;
            float max = 0.01F;

            RenderContext.pushPose();

            RenderContext.translate(-dx, -dy, -dz);

            double pitch = Math.toDegrees(-Math.asin((dy + 0.5) / dist)) + 90;
            double yaw = Math.toDegrees(-Math.atan2(dz, dx)) + 180;

            RenderContext.translate(0F, 1F, 0F);
            RenderContext.mulPose(Axis.YP.rotationDegrees((float) yaw));
            RenderContext.mulPose(Axis.ZP.rotationDegrees((float) pitch));
            RenderContext.translate(0F, -1F, 0F);

            Matrix4f matrix = RenderContext.poseStack().last().pose();
            float far = (float) dist;

            consumer.addVertex(matrix, 0.5F, 1.0625F, 0.5F).setColor(1F, 1F, 1F, max);
            consumer.addVertex(matrix, 0.5F, 1.0625F, -0.5F).setColor(1F, 1F, 1F, max);
            consumer.addVertex(matrix, 0.5F, far, -0.5F).setColor(1F, 1F, 1F, min);
            consumer.addVertex(matrix, 0.5F, far, 0.5F).setColor(1F, 1F, 1F, min);

            consumer.addVertex(matrix, -0.5F, 1.0625F, 0.5F).setColor(1F, 1F, 1F, max);
            consumer.addVertex(matrix, -0.5F, 1.0625F, -0.5F).setColor(1F, 1F, 1F, max);
            consumer.addVertex(matrix, -0.5F, far, -0.5F).setColor(1F, 1F, 1F, min);
            consumer.addVertex(matrix, -0.5F, far, 0.5F).setColor(1F, 1F, 1F, min);

            consumer.addVertex(matrix, 0.5F, 1.0625F, 0.5F).setColor(1F, 1F, 1F, max);
            consumer.addVertex(matrix, -0.5F, 1.0625F, 0.5F).setColor(1F, 1F, 1F, max);
            consumer.addVertex(matrix, -0.5F, far, 0.5F).setColor(1F, 1F, 1F, min);
            consumer.addVertex(matrix, 0.5F, far, 0.5F).setColor(1F, 1F, 1F, min);

            consumer.addVertex(matrix, 0.5F, 1.0625F, -0.5F).setColor(1F, 1F, 1F, max);
            consumer.addVertex(matrix, -0.5F, 1.0625F, -0.5F).setColor(1F, 1F, 1F, max);
            consumer.addVertex(matrix, -0.5F, far, -0.5F).setColor(1F, 1F, 1F, min);
            consumer.addVertex(matrix, 0.5F, far, -0.5F).setColor(1F, 1F, 1F, min);

            RenderContext.popPose();
        }
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles oberhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block hoch.
     */
    @Override
    public AABB getRenderBoundingBox(SolarBoilerBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_SOLAR_BOILER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0.0F, -2.5F, 0.0F);
                RenderContext.scale(3.25F, 3.25F, 3.25F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.SOLAR_TEX);
                ResourceManager.solar_boiler.renderPart("Base");
            }
        };
    }
}
