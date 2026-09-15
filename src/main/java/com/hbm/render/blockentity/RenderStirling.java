package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineStirlingBlockEntity;
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
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class RenderStirling extends BlockEntityRendererNT<MachineStirlingBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineStirlingBlockEntity> create(Context context) {
        return new RenderStirling();
    }

    @Override
    public void render(MachineStirlingBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            default -> { }
        }

        float rot = Mth.lerp(partialTicks, be.lastSpin, be.spin);
        this.renderModel(rot, be.hasCog);
    }

    private void renderModel(float rot, boolean hasCog) {

        RenderSystem.enableCull();
        bindTexture(ResourceManager.STIRLING_TEX);

        ResourceManager.stirling.renderPart("Base");

        if(hasCog) {
            RenderContext.pushPose();
            RenderContext.translate(0F, 1.375F, 0F);
            RenderContext.mulPose(Axis.ZP.rotationDegrees(-rot));
            RenderContext.translate(0F, -1.375F, 0F);
            ResourceManager.stirling.renderPart("Cog");
            RenderContext.popPose();
        }

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.375F, 0.25F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(rot * 2F + 3F));
        RenderContext.translate(0F, -1.375F, -0.25F);
        ResourceManager.stirling.renderPart("CogSmall");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate((float) (Math.sin(rot * Math.PI / 90D) * 0.25D + 0.125D), 0F, 0F);
        ResourceManager.stirling.renderPart("Piston");
        RenderContext.popPose();
    }

    // Kein Zwischenspeichern in einem Feld: BlockEntityRenderers legt pro BlockEntityType
    // genau EINEN Renderer an, den sich alle Maschinen dieses Typs teilen. Ein gecachter
    // Kasten in Weltkoordinaten wuerde fuer jede weitere Maschine falsch gecullt.
    @Override
    public AABB getRenderBoundingBox(MachineStirlingBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        return new AABB(
                pos.getX() - 1,
                pos.getY(),
                pos.getZ() - 1,
                pos.getX() + 2,
                pos.getY() + 2,
                pos.getZ() + 2
        );
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_STIRLING.asItem();
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
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                RenderStirling.this.renderModel(System.currentTimeMillis() % 3600 * 0.1F, true);
            }
        };
    }
}
