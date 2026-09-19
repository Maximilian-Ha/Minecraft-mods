package com.hbm.render.blockentity;

import com.hbm.blockentity.SkeletonHolderBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.SkeletonHolderBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderSkeletonHolder.
 *
 * Erst der Sockel, dann der Gegenstand darauf. Das Original zeichnet ihn als schwebende
 * Wurfgabe mit renderInFrame -- das ist in 1.21 die Darstellungsart FIXED.
 *
 * Die Vergroesserung um die Haelfte gilt nur flachen Sinnbildern; ein Block, der ohnehin
 * raeumlich dasteht, bleibt so gross, wie er ist. Im Original steht dafuer die Abfrage auf
 * renderItemIn3d, hier die auf isGui3d -- dieselbe Unterscheidung.
 */
public class RenderSkeletonHolder extends BlockEntityRendererNT<SkeletonHolderBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<SkeletonHolderBlockEntity> create(Context context) {
        return new RenderSkeletonHolder();
    }

    @Override
    public void render(SkeletonHolderBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0.0F, 0.5F);

        // Die Tabelle des Originals, eins zu eins: Nord 180, Sued 0, West 270, Ost 90.
        Direction facing = be.getBlockState().getValue(SkeletonHolderBlock.FACING);
        RenderContext.mulPose(Axis.YP.rotationDegrees(switch(facing) {
            case NORTH -> 180F;
            case WEST -> 270F;
            case EAST -> 90F;
            default -> 0F;
        }));

        bindTexture(ResourceManager.SKELETON_HOLDER_TEX);
        ResourceManager.skeleton_holder.renderPart("Holder1");

        if(be.item.isEmpty()) return;

        ItemStack stack = be.item.copy();
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = renderer.getModel(stack, be.getLevel(), null, 0);

        RenderContext.pushPose(); {
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            if(!model.isGui3d()) RenderContext.scale(1.5F, 1.5F, 1.5F);
            RenderContext.translate(0F, 0.125F, 0F);

            renderer.render(stack, ItemDisplayContext.FIXED, false, RenderContext.poseStack(), buffer,
                    RenderContext.light(), RenderContext.overlay(), model);
        }
        RenderContext.popPose();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.SKELETON_HOLDER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            /* Das Original hat fuer den Sockel kein Inventarbild -- er steht dort in keinem
             * Reiter. Die Werte sind darum hergeleitet, nicht abgeschrieben: ItemRenderBase
             * verkleinert auf ein Sechzehntel, das Modell ist 1,43 hoch, und mit acht kommt
             * es auf dieselbe Bildhoehe wie die Teslaspule (1,94 bei sechs). */
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -4F, 0F);
                RenderContext.scale(8F, 8F, 8F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.SKELETON_HOLDER_TEX);
                ResourceManager.skeleton_holder.renderPart("Holder1");
            }
        };
    }
}
