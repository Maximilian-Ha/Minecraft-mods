package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineGasCentBlockEntity;
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
 * Portiert aus 1.7.10: der Gaszentrifugen-Zweig von com.hbm.render.tileentity.RenderCentrifuge.
 *
 * Runde 162 nachgereicht: die Gaszentrifuge steht seit Runde 115, hatte aber keinen Darsteller
 * und war daher in der Welt wie im Inventar unsichtbar.
 *
 * Das Original bindet EINEN Darsteller an beide Zentrifugen und verzweigt drinnen nach der
 * Klasse der Blockentitaet -- in 1.7.10 ist der Darsteller untypisiert. In 1.21 ist er ueber
 * den BlockEntityType typisiert, darum steht der Gaszentrifugen-Zweig hier als eigene Klasse.
 * Die zusaetzliche halbe Drehung stammt woertlich aus diesem Zweig.
 *
 * Der Ausrichtungsschalter ist bewusst derselbe wie in RenderCentrifuge, damit die beiden
 * Schwestern gleich stehen. Er weicht um 90 Grad vom Original ab; das ist ein bekannter,
 * getrennt verfolgter Befund und faellt hier kaum auf, weil beide Zentrifugen einen
 * quadratischen Grundriss von einem Block haben.
 */
public class RenderGasCent extends BlockEntityRendererNT<MachineGasCentBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineGasCentBlockEntity> create(Context context) { return new RenderGasCent(); }

    @Override
    public void render(MachineGasCentBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            default -> { }
        }

        // Die Gaszentrifuge steht im Original um eine halbe Drehung versetzt zur normalen.
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

        RenderSystem.disableCull();
        bindTexture(ResourceManager.GASCENT_TEX);
        ResourceManager.gascent.renderPart("Centrifuge");
        ResourceManager.gascent.renderPart("Flag");
        RenderSystem.enableCull();
    }

    @Override
    public AABB getRenderBoundingBox(MachineGasCentBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_GAS_CENT.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -4F, 0F);
                RenderContext.scale(3.5F, 3.5F, 3.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                // Im Inventar zeigt das Original nur den Koerper, ohne Fahne.
                bindTexture(ResourceManager.GASCENT_TEX);
                ResourceManager.gascent.renderPart("Centrifuge");
            }
        };
    }
}
