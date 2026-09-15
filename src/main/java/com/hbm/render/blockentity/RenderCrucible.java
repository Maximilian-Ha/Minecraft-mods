package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineCrucibleBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.render.NtmRenderTypes;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderCrucible.
 *
 * Der Tiegel selbst und, wenn etwas darin steht, seine Oberflaeche. Die Hoehe richtet sich nach
 * dem Gesamtinhalt beider Baender.
 */
public class RenderCrucible extends BlockEntityRendererNT<MachineCrucibleBlockEntity> implements IBEWLRProvider {

    public static final ResourceLocation LAVA = NuclearTechMod.withDefaultNamespace("textures/models/machines/lava.png");

    @Override
    public BlockEntityRenderer<MachineCrucibleBlockEntity> create(Context context) {
        return new RenderCrucible();
    }

    @Override
    public void render(MachineCrucibleBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        RenderSystem.enableCull();
        bindTexture(ResourceManager.CRUCIBLE_TEX);
        ResourceManager.crucible.renderPart("Main");

        if(be.recipeStack.isEmpty() && be.wasteStack.isEmpty()) return;

        int totalCap = MachineCrucibleBlockEntity.recipeCapacity + MachineCrucibleBlockEntity.wasteCapacity;
        int totalMass = 0;
        for(MaterialStack stack : be.recipeStack) totalMass += stack.amount;
        for(MaterialStack stack : be.wasteStack) totalMass += stack.amount;

        float fill = (float) (((double) totalMass / (double) totalCap) * 0.875D);

        FullBright.enable();
        RenderContext.setLightning(false);

        VertexConsumer consumer = buffer.getBuffer(NtmRenderTypes.entitySmoth(LAVA));
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        vertex(consumer, matrix, -1F, 0.5F + fill, -1F, 0F, 0F);
        vertex(consumer, matrix, -1F, 0.5F + fill, 1F, 0F, 1F);
        vertex(consumer, matrix, 1F, 0.5F + fill, 1F, 1F, 1F);
        vertex(consumer, matrix, 1F, 0.5F + fill, -1F, 1F, 0F);

        RenderContext.setLightning(true);
        FullBright.disable();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float u, float v) {
        consumer.addVertex(matrix, x, y, z)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setColor(1F, 1F, 1F, 1F)
                .setNormal(0F, 1F, 0F)
                .setLight(240);
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles ausserhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block gross.
     */
    @Override
    public AABB getRenderBoundingBox(MachineCrucibleBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_CRUCIBLE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1.5F, 0F);
                RenderContext.scale(3.25F, 3.25F, 3.25F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.CRUCIBLE_TEX);
                ResourceManager.crucible.renderPart("Main");
            }
        };
    }
}
