package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineCrystallizerBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderCrystallizer.
 * Drehwinkel des Rezipienten und die Zuordnung Blickrichtung -> Drehung sind 1:1 uebernommen.
 */
public class RenderCrystallizer extends BlockEntityRendererNT<MachineCrystallizerBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineCrystallizerBlockEntity> create(Context context) {
        return new RenderCrystallizer();
    }

    @Override
    public void render(MachineCrystallizerBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            default -> { }
        }

        bindTexture(ResourceManager.CRYSTALLIZER_TEX);
        ResourceManager.crystallizer.renderPart("Body");

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees(be.prevAngle + (be.angle - be.prevAngle) * partialTicks));
        ResourceManager.crystallizer.renderPart("Spinner");
        RenderContext.popPose();

        if(be.prevAngle != be.angle) {
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            bindTexture(be.tank.getTankType().getTexture());
            ResourceManager.crystallizer.renderPart("Fluid");
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    /**
     * Ohne eigene Box cullt Minecraft alles oberhalb des Kernblocks weg. Das Ergebnis darf
     * nicht im Renderer zwischengespeichert werden -- es gibt pro BlockEntityType nur einen
     * Renderer fuer alle Maschinen dieses Typs.
     */
    @Override
    public AABB getRenderBoundingBox(MachineCrystallizerBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_CRYSTALLIZER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderNonInv(ItemStack stack, MultiBufferSource buffer, boolean rightHand) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
            }

            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -4F, 0F);
                RenderContext.scale(2F, 2F, 2F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.CRYSTALLIZER_TEX);
                ResourceManager.crystallizer.renderPart("Body");
                ResourceManager.crystallizer.renderPart("Spinner");
            }
        };
    }
}
