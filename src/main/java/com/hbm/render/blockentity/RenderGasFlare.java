package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.oil.MachineGasFlareBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderGasFlare.
 *
 * Ein Teil, keine Bewegung -- nur die Schraeglage, wenn der Turm kein Fundament unter sich hat.
 */
public class RenderGasFlare extends BlockEntityRendererNT<MachineGasFlareBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineGasFlareBlockEntity> create(Context context) {
        return new RenderGasFlare();
    }

    @Override
    public void render(MachineGasFlareBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

        if(be.tilted) {
            RenderContext.translate(0F, -0.25F, 0F);
            RenderContext.mulPose(Axis.ZP.rotationDegrees(10F));
            RenderContext.mulPose(Axis.YP.rotationDegrees(5F));
        }

        RenderSystem.disableCull();
        bindTexture(ResourceManager.FLARE_STACK_TEX);
        ResourceManager.flareStack.renderAll();
        RenderSystem.enableCull();
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles oberhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block hoch, der Turm zwoelf.
     */
    @Override
    public AABB getRenderBoundingBox(MachineGasFlareBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_GAS_FLARE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -6F, 0F);
                RenderContext.scale(11F, 11F, 11F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.12F, 0.12F, 0.12F);
                RenderSystem.disableCull();
                bindTexture(ResourceManager.FLARE_STACK_TEX);
                ResourceManager.flareStack.renderAll();
                RenderSystem.enableCull();
            }
        };
    }
}
