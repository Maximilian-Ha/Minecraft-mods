package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.FloodlightBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.FloodlightBlock;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFloodlight.
 *
 * Drei Teile: "Base" steht fest an der Wand, "Lights" schwenkt mit der Neigung, "Lamps"
 * leuchtet -- oder bleibt dunkelgrau, wenn kein Strom da ist.
 */
public class RenderFloodlight extends BlockEntityRendererNT<FloodlightBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<FloodlightBlockEntity> create(Context context) { return new RenderFloodlight(); }

    @Override
    public void render(FloodlightBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        BlockState zustand = be.getBlockState();
        Direction seite = zustand.getValue(FloodlightBlock.FACING);
        boolean gedreht = zustand.getValue(FloodlightBlock.FLIPPED);

        RenderContext.translate(0.5F, 0.5F, 0.5F);

        switch(seite) {
            case DOWN -> RenderContext.mulPose(Axis.XP.rotationDegrees(180F));
            case UP -> { }
            case NORTH -> { RenderContext.mulPose(Axis.XP.rotationDegrees(90F)); RenderContext.mulPose(Axis.ZP.rotationDegrees(180F)); }
            case SOUTH -> RenderContext.mulPose(Axis.XP.rotationDegrees(90F));
            case WEST -> { RenderContext.mulPose(Axis.XP.rotationDegrees(90F)); RenderContext.mulPose(Axis.ZP.rotationDegrees(90F)); }
            case EAST -> { RenderContext.mulPose(Axis.XP.rotationDegrees(90F)); RenderContext.mulPose(Axis.ZP.rotationDegrees(270F)); }
        }

        RenderContext.translate(0F, -0.5F, 0F);

        // Steht das Flutlicht an Decke oder Boden, liegt es quer -- im Original die Metadaten
        // 6 und 7, hier FLIPPED. Alle Wandfassungen stehen ohnehin quer.
        boolean quer = (seite != Direction.DOWN && seite != Direction.UP) || gedreht;
        if(quer) RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        bindTexture(ResourceManager.FLOODLIGHT_TEX);
        ResourceManager.floodlight.renderPart("Base");

        float neigung = be.neigung;
        if(seite == Direction.DOWN) neigung -= 90F;
        if(seite == Direction.UP) neigung += 90F;

        RenderContext.translate(0F, 0.5F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(neigung));
        RenderContext.translate(0F, -0.5F, 0F);

        ResourceManager.floodlight.renderPart("Lights");

        if(!be.brennt()) RenderContext.setColor(0.25F, 0.25F, 0.25F, 1F);
        ResourceManager.floodlight.renderPart("Lamps");
        if(!be.brennt()) RenderContext.setColor(1F, 1F, 1F, 1F);
    }

    @Override
    public AABB getRenderBoundingBox(FloodlightBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.FLOODLIGHT.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1.5F, 0F);
                RenderContext.scale(6.5F, 6.5F, 6.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.FLOODLIGHT_TEX);
                ResourceManager.floodlight.renderPart("Base");
                RenderContext.translate(0F, 0.5F, 0F);
                RenderContext.mulPose(Axis.ZP.rotationDegrees(-30F));
                RenderContext.translate(0F, -0.5F, 0F);
                ResourceManager.floodlight.renderPart("Lights");
                ResourceManager.floodlight.renderPart("Lamps");
            }
        };
    }
}
