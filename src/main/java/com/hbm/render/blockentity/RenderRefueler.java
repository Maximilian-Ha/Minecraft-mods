package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.RefuelerBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.MachineRefuelerBlock;
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

import java.awt.Color;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRefueler.
 *
 * Das Gehaeuse und darin die Fluidsaeule, deren Hoehe dem Fuellstand folgt.
 *
 * ABWEICHUNG: das Original schiebt die Fluidsaeule nach unten aus dem Gehaeuse heraus und
 * schneidet sie mit einer Klipp-Ebene (GL_CLIP_PLANE0) bei y = 0.125 ab. Eine Klipp-Ebene gibt
 * es auf 1.21 nicht mehr; stattdessen wird die Saeule um dieselbe Hoehe GESTAUCHT. Fuer einen
 * Quader waere das genau dasselbe Bild -- das Teil ist keiner, deshalb bleibt beim Stand null
 * hier nichts stehen, wo das Original noch einen schmalen Rest zeigt.
 *
 * ZWEITE ABWEICHUNG: das Original zeichnet die Saeule ohne Textur und additiv verrechnet. Hier
 * bleibt die Modelltextur stehen und wird eingefaerbt, wie beim Fluidtank auch.
 */
public class RenderRefueler extends BlockEntityRendererNT<RefuelerBlockEntity> implements IBEWLRProvider {

    /** Hoehe, auf der die Fluidsaeule abgeschnitten wird. Wert aus dem Original. */
    private static final float SCHNITT = 0.125F;

    @Override
    public BlockEntityRenderer<RefuelerBlockEntity> create(Context context) {
        return new RenderRefueler();
    }

    @Override
    public void render(RefuelerBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        Direction facing = be.getBlockState().getValue(MachineRefuelerBlock.FACING);

        RenderContext.translate(0.5F, 0F, 0.5F);

        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            default -> { }
        }

        bindTexture(ResourceManager.REFUELER_TEX);
        ResourceManager.refueler.renderPart("Fueler");

        float fuellstand = be.prevFillLevel + (be.fillLevel - be.prevFillLevel) * partialTicks;

        RenderContext.pushPose();
        RenderContext.translate(0F, SCHNITT, 0F);
        RenderContext.scale(1F, fuellstand, 1F);
        RenderContext.translate(0F, -SCHNITT, 0F);

        Color farbe = new Color(be.tank.getTankType().getColor());
        RenderContext.setColor(farbe.getRed() / 255F, farbe.getGreen() / 255F, farbe.getBlue() / 255F, 0.75F);
        ResourceManager.refueler.renderPart("Fluid");
        RenderContext.setColor(1F, 1F, 1F, 1F);

        RenderContext.popPose();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_REFUELER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 0F);
                RenderContext.scale(6F, 6F, 6F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(2F, 2F, 2F);
                RenderContext.translate(0.5F, 0F, 0F);
                bindTexture(ResourceManager.REFUELER_TEX);
                ResourceManager.refueler.renderPart("Fueler");
            }
        };
    }
}
