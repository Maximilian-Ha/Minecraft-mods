package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineSuperComputerBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.FullBright;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderSuperComputer.
 *
 * Runde 161 nachgereicht: der Grossrechner steht seit Runde 124, hatte aber keinen Darsteller
 * und war daher in der Welt wie im Inventar unsichtbar.
 *
 * Bewusste Abweichung vom Original: dort laeuft das Leuchtband "Lights" ueber die
 * GL-Texturmatrix (glMatrixMode(GL_TEXTURE) + glTranslatef(-scroll, 0, 0)) durch das Bild.
 * 1.21 kennt keine Texturmatrix mehr; die Teilbilder muessten dafuer einzeln erzeugt werden.
 * Das Band wird darum unbewegt gezeichnet -- weiterhin voll erhellt und weiterhin schwarz,
 * solange der Rechner nichts verarbeitet, sodass der Zustand am Block ablesbar bleibt.
 */
public class RenderSuperComputer extends BlockEntityRendererNT<MachineSuperComputerBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineSuperComputerBlockEntity> create(Context context) { return new RenderSuperComputer(); }

    @Override
    public void render(MachineSuperComputerBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            default -> { }
        }

        bindTexture(ResourceManager.SUPERCOMPUTER_TEX);
        ResourceManager.supercomputer.renderPart("Computer");

        // Steht der Rechner still, bleibt das Leuchtband schwarz -- wie im Original.
        if(!be.didProcess) RenderContext.setColor(0F, 0F, 0F, 1F);

        FullBright.enable();
        bindTexture(ResourceManager.SUPERCOMPUTER_SCAN_TEX);
        ResourceManager.supercomputer.renderPart("Lights");
        FullBright.disable();

        RenderContext.setColor(1F, 1F, 1F, 1F);
    }

    @Override
    public AABB getRenderBoundingBox(MachineSuperComputerBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_SUPER_COMPUTER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -2.5F, 0F);
                RenderContext.scale(2.5F, 2.5F, 2.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.translate(-2F, 0F, 0F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                bindTexture(ResourceManager.SUPERCOMPUTER_TEX);
                ResourceManager.supercomputer.renderPart("Computer");
                bindTexture(ResourceManager.SUPERCOMPUTER_SCAN_TEX);
                ResourceManager.supercomputer.renderPart("Lights");
            }
        };
    }
}
