package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineReactorBreedingBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.hbm.render.util.RenderSparks;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderBreeder.
 *
 * Der Kasten steht still; laeuft eine Umwandlung, springen drei Funken darin herum.
 */
public class RenderBreeder extends BlockEntityRendererNT<MachineReactorBreedingBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineReactorBreedingBlockEntity> create(Context context) { return new RenderBreeder(); }

    @Override
    public void render(MachineReactorBreedingBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        RenderContext.mulPose(Axis.YP.rotationDegrees(switch(facing) {
            case NORTH -> 0F;
            case WEST -> 90F;
            case SOUTH -> 180F;
            default -> 270F;
        }));

        if(be.progress > 0.0F) {
            for(int i = 0; i < 3; i++) {
                RenderContext.pushPose();
                RenderContext.mulPose(Axis.YP.rotationDegrees((float) (Math.PI * i)));
                RenderSparks.renderSpark(buffer, (int) ((System.currentTimeMillis() % 10000) / 100 + i),
                        0, 1.5625, 0, 0.15F, 3, 4, 0x00ff00, 0xffffff);
                RenderContext.popPose();
            }
        }

        bindTexture(ResourceManager.BREEDER_TEX);
        ResourceManager.breeder.renderAll();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_REACTOR_BREEDING.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -4.5F, 0F);
                RenderContext.scale(4.5F, 4.5F, 4.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                bindTexture(ResourceManager.BREEDER_TEX);
                ResourceManager.breeder.renderAll();
            }
        };
    }
}
