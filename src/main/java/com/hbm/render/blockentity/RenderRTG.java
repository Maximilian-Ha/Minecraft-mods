package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineRTGBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.lib.Library;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRTG, das Inventarbild aus
 * com.hbm.render.block.RenderRTGBlock.
 *
 * Das Original schaltet vor dem Zeichnen GL_CULL_FACE ab, hier uebernimmt das
 * RenderSystem.disableCull()/enableCull(). An den vier waagerechten Seiten kommt ein
 * Anschlussstutzen dazu, sobald dort etwas Strom annimmt.
 */
public class RenderRTG extends BlockEntityRendererNT<MachineRTGBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineRTGBlockEntity> create(Context context) {
        return new RenderRTG();
    }

    @Override
    public void render(MachineRTGBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0.0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

        RenderSystem.disableCull();
        bindTexture(ResourceManager.RTG_TEX);

        ResourceManager.rtg.renderPart("Gen");

        Level level = be.getLevel();
        if(level != null) {
            BlockPos pos = be.getBlockPos();

            if(Library.canConnect(level, pos.east(), Direction.EAST)) {
                ResourceManager.rtg.renderPart("Connector");
            }

            if(Library.canConnect(level, pos.west(), Direction.WEST)) {
                this.renderConnector(180F);
            }

            if(Library.canConnect(level, pos.north(), Direction.NORTH)) {
                this.renderConnector(90F);
            }

            if(Library.canConnect(level, pos.south(), Direction.SOUTH)) {
                this.renderConnector(-90F);
            }
        }

        RenderSystem.enableCull();
    }

    /**
     * Im Original wurde nach dem Zeichnen jeweils um den gleichen Winkel
     * zurueckgedreht; push/pop ist dasselbe, nur ohne Rundungsreste.
     */
    private void renderConnector(float degrees) {
        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees(degrees));
        ResourceManager.rtg.renderPart("Connector");
        RenderContext.popPose();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_RTG.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                // Wie bei den anderen Ein-Block-Maschinen des Ports; die alte
                // ISBRH-Projektion aus 1.7.10 hat kein direktes Gegenstueck.
                RenderContext.translate(0.0F, -1.0F, 0.0F);
                RenderContext.scale(2.75F, 2.75F, 2.75F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderSystem.disableCull();
                bindTexture(ResourceManager.RTG_TEX);
                ResourceManager.rtg.renderPart("Gen");
                RenderSystem.enableCull();
            }
        };
    }
}
