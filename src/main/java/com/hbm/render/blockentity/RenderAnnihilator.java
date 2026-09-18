package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineAnnihilatorBlockEntity;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderAnnihilator.
 *
 * Runde 162 nachgereicht: der Annihilator steht seit Runde 128, hatte aber keinen Darsteller
 * und war daher in der Welt wie im Inventar unsichtbar.
 *
 * Bewusste Abweichung: das Original schiebt die Bandtextur ueber die GL-Texturmatrix am
 * Foerderband entlang (glMatrixMode(GL_TEXTURE) + glTranslated). 1.21 kennt keine
 * Texturmatrix mehr; das Band wird darum unbewegt gezeichnet. Die Rolle dreht sich weiter,
 * sodass man der Maschine ihren Lauf trotzdem ansieht.
 */
public class RenderAnnihilator extends BlockEntityRendererNT<MachineAnnihilatorBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineAnnihilatorBlockEntity> create(Context context) { return new RenderAnnihilator(); }

    @Override
    public void render(MachineAnnihilatorBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            default -> { }
        }

        bindTexture(ResourceManager.ANNIHILATOR_TEX);
        ResourceManager.annihilator.renderPart("Annihilator");

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.75F, 0F);
        RenderContext.mulPose(Axis.ZN.rotationDegrees((float) (System.currentTimeMillis() * 0.15D % 360D)));
        RenderContext.translate(0F, -1.75F, 0F);
        ResourceManager.annihilator.renderPart("Roller");
        RenderContext.popPose();

        bindTexture(ResourceManager.ANNIHILATOR_BELT_TEX);
        ResourceManager.annihilator.renderPart("Belt");
    }

    @Override
    public AABB getRenderBoundingBox(MachineAnnihilatorBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_ANNIHILATOR.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 0F);
                RenderContext.scale(2.75F, 2.75F, 2.75F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                bindTexture(ResourceManager.ANNIHILATOR_TEX);
                ResourceManager.annihilator.renderPart("Annihilator");
                ResourceManager.annihilator.renderPart("Roller");
                bindTexture(ResourceManager.ANNIHILATOR_BELT_TEX);
                ResourceManager.annihilator.renderPart("Belt");
            }
        };
    }
}
