package com.hbm.render.blockentity;

import com.hbm.blockentity.network.RadioTelexBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderTelex.
 *
 * Ein einziges Modellteil, nach der Setzrichtung gedreht. Das Original rechnet dafuer in
 * Metadaten; im Port steht die Richtung im Blockzustand.
 */
public class RenderTelex extends BlockEntityRendererNT<RadioTelexBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<RadioTelexBlockEntity> create(Context context) { return new RenderTelex(); }

    @Override
    public void render(RadioTelexBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        int drehung = switch(facing) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
        RenderContext.mulPose(Axis.YP.rotationDegrees(drehung));

        RenderSystem.disableCull();
        bindTexture(ResourceManager.TELEX_TEX);
        ResourceManager.telex.renderAll();
        RenderSystem.enableCull();
    }

    @Override
    public AABB getRenderBoundingBox(RadioTelexBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.RADIO_TELEX.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -2F, 0F);
                RenderContext.scale(6F, 6F, 6F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, 0F, -0.5F);
                bindTexture(ResourceManager.TELEX_TEX);
                ResourceManager.telex.renderAll();
            }
        };
    }
}
