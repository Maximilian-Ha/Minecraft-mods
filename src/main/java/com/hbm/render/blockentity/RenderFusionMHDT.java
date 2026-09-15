package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.fusion.FusionMHDTBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFusionMHDT.
 *
 * Die Spulen schwingen, statt sich zu drehen: der Winkel laeuft modulo 15 Grad, springt also
 * staendig zurueck. Aus der Naehe sieht das aus wie ein Ruckeln unter Last -- so im Original.
 */
public class RenderFusionMHDT extends BlockEntityRendererNT<FusionMHDTBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<FusionMHDTBlockEntity> create(Context context) { return new RenderFusionMHDT(); }

    @Override
    public void render(FusionMHDTBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        RenderContext.mulPose(Axis.YP.rotationDegrees(switch(facing) {
            case NORTH -> 90F;
            case WEST -> 180F;
            case SOUTH -> 270F;
            default -> 0F;
        }));

        bindTexture(ResourceManager.FUSION_MHDT_TEX);
        ResourceManager.fusion_mhdt.renderPart("Turbine");

        RenderContext.pushPose();
        float rot = Mth.lerp(partialTicks, be.prevRotor, be.rotor) % 15F;
        RenderContext.translate(0F, 1.5F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(rot));
        RenderContext.translate(0F, -1.5F, 0F);
        ResourceManager.fusion_mhdt.renderPart("Coils");
        RenderContext.popPose();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.FUSION_MHDT.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(2.5F, 2.5F, 2.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                bindTexture(ResourceManager.FUSION_MHDT_TEX);
                ResourceManager.fusion_mhdt.renderPart("Turbine");
                float rot = (System.currentTimeMillis() / 5) % 30F - 15F;
                RenderContext.translate(0F, 1.5F, 0F);
                RenderContext.mulPose(Axis.XP.rotationDegrees(rot));
                RenderContext.translate(0F, -1.5F, 0F);
                ResourceManager.fusion_mhdt.renderPart("Coils");
            }
        };
    }
}
