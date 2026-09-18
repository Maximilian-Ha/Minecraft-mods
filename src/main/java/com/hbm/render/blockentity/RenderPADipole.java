package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.albion.MachinePADipoleBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderPADipole.
 *
 * Runde 168 nachgereicht: der Dipolmagnet steht seit Runde 133, hatte aber keinen Darsteller und war
 * daher in der Welt wie im Inventar unsichtbar. Damit ist die Schuldenliste aus Runde 160 leer.
 */
public class RenderPADipole extends BlockEntityRendererNT<MachinePADipoleBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachinePADipoleBlockEntity> create(Context context) { return new RenderPADipole(); }

    @Override
    public void render(MachinePADipoleBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, -1F, 0.5F);

        /* Der Dipol steht rund; das Original dreht ihn nicht nach der Aufstellrichtung. */

        RenderSystem.enableCull();
        bindTexture(ResourceManager.PA_DIPOLE_TEX);
        ResourceManager.pa_dipole.renderAll();
    }

    @Override
    public AABB getRenderBoundingBox(MachinePADipoleBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_PA_DIPOLE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 0F);
                RenderContext.scale(3.5F, 3.5F, 3.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.PA_DIPOLE_TEX);
                ResourceManager.pa_dipole.renderAll();
            }
        };
    }
}
