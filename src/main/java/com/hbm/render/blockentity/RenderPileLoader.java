package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.pile.PileLoaderBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.pile.PileDeviceBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderPileLoader.
 *
 * Der Hebel dreht sich, der Schieber faehrt vor, und der Stab wird nur gezeichnet, solange einer
 * eingelegt ist.
 */
public class RenderPileLoader extends BlockEntityRendererNT<PileLoaderBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<PileLoaderBlockEntity> create(Context context) { return new RenderPileLoader(); }

    @Override
    public void render(PileLoaderBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(PileDeviceBlock.FACING);
        RenderContext.mulPose(Axis.YP.rotationDegrees(facingAngle(facing)));

        double position = be.lastLevel + (be.progress - be.lastLevel) * partialTicks;

        bindTexture(ResourceManager.PILE_LOADER_TEX);
        ResourceManager.pile_loader.renderPart("Loader");

        RenderContext.pushPose();
        RenderContext.translate(-0.1875F, 0.5F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees((float) (position * 90D)));
        RenderContext.translate(0.1875F, -0.5F, 0F);
        ResourceManager.pile_loader.renderPart("Lever");
        RenderContext.popPose();

        RenderContext.translate((float) (position * -0.5D), 0F, 0F);
        ResourceManager.pile_loader.renderPart("Slider");
        if(!be.syncStack.isEmpty()) ResourceManager.pile_loader.renderPart("Rod");
    }

    /** Dieselbe Zuordnung wie im Original, nur ueber die Blickrichtung statt ueber den Metadatenwert. */
    static float facingAngle(Direction facing) {
        return switch(facing) {
            case NORTH -> 90F;
            case SOUTH -> 270F;
            case WEST -> 180F;
            default -> 0F;
        };
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.PILE_LOADER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3.5F, 0F);
                RenderContext.scale(5F, 5F, 5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(2F, 2F, 2F);
                bindTexture(ResourceManager.PILE_LOADER_TEX);
                ResourceManager.pile_loader.renderAll();
            }
        };
    }
}
