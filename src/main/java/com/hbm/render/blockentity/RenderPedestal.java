package com.hbm.render.blockentity;

import com.hbm.blockentity.PedestalBlockEntity;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderPedestalTile.
 *
 * Der Sockel selbst kommt aus seinem Blockmodell; hier wird nur gezeichnet, was darauf
 * liegt. Es schwebt einen Block ueber dem Sockel, anderthalbfach vergroessert, und wippt
 * langsam auf und ab.
 *
 * ZWEI FAELLE, wie im Original: ein flaches Sinnbild dreht sich zum Zuschauer, ein Block
 * bleibt stehen, wie er ist, und liegt dafuer ein Achtel hoeher. Das Original unterscheidet
 * das ueber renderItemIn3d, der Port ueber isGui3d -- dieselbe Frage.
 *
 * DIE WIPPE rechnet mit ticksExisted des Spielers, nicht mit der Weltzeit: so steht es dort,
 * und so wippen alle Sockel im Gleichtakt statt jeder fuer sich.
 */
public class RenderPedestal extends BlockEntityRendererNT<PedestalBlockEntity> {

    @Override
    public BlockEntityRenderer<PedestalBlockEntity> create(Context context) {
        return new RenderPedestal();
    }

    @Override
    public void render(PedestalBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        if(be.item.isEmpty()) return;

        Player spieler = Minecraft.getInstance().player;
        if(spieler == null) return;

        ItemStack stack = be.item.copy();
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = renderer.getModel(stack, be.getLevel(), null, 0);

        double wippe = Math.sin((spieler.tickCount + partialTicks) * 0.1D) * 0.0625D;

        RenderContext.pushPose(); {
            RenderContext.translate(0.5F, 1.0F, 0.5F);
            RenderContext.scale(1.5F, 1.5F, 1.5F);

            if(!model.isGui3d()) {
                RenderContext.translate(0F, 0.125F, 0F);
                float blick = Mth.lerp(partialTicks, spieler.yRotO, spieler.getYRot()) + 180F;
                RenderContext.mulPose(Axis.YN.rotationDegrees(blick));
                RenderContext.translate(0F, (float) wippe, 0F);
            } else {
                RenderContext.translate(0F, (float) wippe + 0.0625F, 0F);
            }

            renderer.render(stack, ItemDisplayContext.FIXED, false, RenderContext.poseStack(), buffer,
                    RenderContext.light(), RenderContext.overlay(), model);
        }
        RenderContext.popPose();
    }
}
