package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.FoundryCastingBaseBlockEntity;
import com.hbm.blockentity.machine.IRenderFoundry;
import com.hbm.main.NuclearTechMod;
import com.hbm.render.NtmRenderTypes;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFoundry.
 *
 * Zeichnet, was in einem Giessereiblock liegt: die eingelegte Form flach am Boden, darueber das
 * fertige Gussstueck und, solange noch Metall darin steht, dessen gluehende Oberflaeche.
 *
 * ABWEICHUNG: das Original zeichnet ein Gussstueck, das ein Block ist, als eingefaerbte Flaeche
 * aus der Blocktextur statt als Gegenstand. Der Port zeichnet beides als Gegenstand -- auf 1.21
 * liefert der Gegenstandsrenderer fuer Blockgegenstaende ohnehin das Blockmodell.
 */
public class RenderFoundry extends BlockEntityRendererNT<FoundryCastingBaseBlockEntity> {

    public static final ResourceLocation LAVA = NuclearTechMod.withDefaultNamespace("textures/particle/lava_gray.png");

    @Override
    public BlockEntityRenderer<FoundryCastingBaseBlockEntity> create(Context context) {
        return new RenderFoundry();
    }

    @Override
    public void render(FoundryCastingBaseBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        if(!(be instanceof IRenderFoundry foundry)) return;

        ItemStack mold = be.getItem(0);
        if(!mold.isEmpty()) this.drawItem(mold, foundry.moldHeight(), buffer, be);

        ItemStack out = be.getItem(1);
        if(!out.isEmpty()) this.drawItem(out, foundry.outHeight(), buffer, be);

        if(!foundry.shouldRender() || foundry.getMat() == null) return;

        Color color = new Color(foundry.getMat().moltenColor);
        float r = color.getRed() / 255F;
        float g = color.getGreen() / 255F;
        float b = color.getBlue() / 255F;

        FullBright.enable();
        RenderContext.setLightning(false);

        VertexConsumer consumer = buffer.getBuffer(NtmRenderTypes.entitySmoth(LAVA));
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        double fill = foundry.getFillLevel();

        // Die Flaeche selbst dient zugleich als Bildausschnitt, wie im Original
        vertex(consumer, matrix, foundry.minX(), fill, foundry.minZ(), foundry.minZ(), foundry.maxX(), r, g, b);
        vertex(consumer, matrix, foundry.minX(), fill, foundry.maxZ(), foundry.maxZ(), foundry.maxX(), r, g, b);
        vertex(consumer, matrix, foundry.maxX(), fill, foundry.maxZ(), foundry.maxZ(), foundry.minX(), r, g, b);
        vertex(consumer, matrix, foundry.maxX(), fill, foundry.minZ(), foundry.minZ(), foundry.minX(), r, g, b);

        RenderContext.setLightning(true);
        FullBright.disable();
    }

    /** Legt einen Gegenstand flach auf die angegebene Hoehe, wie im Original. */
    private void drawItem(ItemStack stack, double height, MultiBufferSource buffer, FoundryCastingBaseBlockEntity be) {

        RenderContext.pushPose();
        RenderContext.translate(0.5F, (float) height, 0.5F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F));
        RenderContext.scale(0.75F, 0.75F, 0.75F);

        ItemStack single = stack.copy();
        single.setCount(1);

        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = renderer.getModel(single, be.getLevel(), null, 0);
        renderer.render(single, ItemDisplayContext.FIXED, false, RenderContext.poseStack(), buffer,
                this.getPacketLight(RenderContext.light(), be), RenderContext.overlay(), model);

        RenderContext.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, double x, double y, double z, double u, double v, float r, float g, float b) {
        consumer.addVertex(matrix, (float) x, (float) y, (float) z)
                .setUv((float) u, (float) v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setColor(r, g, b, 1F)
                .setNormal(0F, 1F, 0F)
                .setLight(240);
    }
}
