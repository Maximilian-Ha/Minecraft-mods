package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.fusion.FusionKlystronBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFusionKlystron.
 *
 * Der Koerper steht still, der Rotor dreht sich um die Laengsachse -- je schneller, desto mehr
 * Leistung geht hinaus.
 */
public class RenderFusionKlystron extends BlockEntityRendererNT<FusionKlystronBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<FusionKlystronBlockEntity> create(Context context) { return new RenderFusionKlystron(); }

    @Override
    public void render(FusionKlystronBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        RenderContext.mulPose(Axis.YP.rotationDegrees(switch(facing) {
            case NORTH -> 90F;
            case WEST -> 180F;
            case SOUTH -> 270F;
            default -> 0F;
        }));

        RenderContext.translate(-1F, 0F, 0F);

        bindTexture(ResourceManager.FUSION_KLYSTRON_TEX);
        ResourceManager.fusion_klystron.renderPart("Klystron");

        RenderContext.pushPose();
        float rot = Mth.lerp(partialTicks, be.prevFan, be.fan);
        RenderContext.translate(0F, 2.5F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(rot));
        RenderContext.translate(0F, -2.5F, 0F);
        ResourceManager.fusion_klystron.renderPart("Rotor");
        RenderContext.popPose();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.FUSION_KLYSTRON.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 1F);
                RenderContext.scale(3.5F, 3.5F, 3.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                bindTexture(ResourceManager.FUSION_KLYSTRON_TEX);
                ResourceManager.fusion_klystron.renderPart("Klystron");
                float rot = (System.currentTimeMillis() / 10) % 360F;
                RenderContext.translate(0F, 2.5F, 0F);
                RenderContext.mulPose(Axis.XP.rotationDegrees(rot));
                RenderContext.translate(0F, -2.5F, 0F);
                ResourceManager.fusion_klystron.renderPart("Rotor");
            }
        };
    }
}
