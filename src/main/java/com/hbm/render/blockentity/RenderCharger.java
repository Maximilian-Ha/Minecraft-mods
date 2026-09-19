package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.ChargerBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.ChargerBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderCharger.
 *
 * Der Arm faehrt in der ersten Haelfte der Bewegung heraus und schwenkt in der zweiten auf.
 * Das Laempchen leuchtet in Orange -- im Original mit abgeschalteter Beleuchtung, hier ueber
 * die Farbe des RenderContext.
 */
public class RenderCharger extends BlockEntityRendererNT<ChargerBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<ChargerBlockEntity> create(Context context) { return new RenderCharger(); }

    @Override
    public void render(ChargerBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        Direction facing = be.getBlockState().getValue(ChargerBlock.FACING);
        int drehung = switch(facing) {
            case WEST -> 90;
            case SOUTH -> 180;
            case EAST -> 270;
            default -> 0;
        };
        RenderContext.mulPose(Axis.YP.rotationDegrees(drehung));

        bindTexture(ResourceManager.CHARGER_TEX);
        ResourceManager.charger.renderPart("Base");

        float zeit = Mth.lerp(partialTicks, be.letzteAusfahrt, be.ausfahrt) / (float) ChargerBlockEntity.DAUER;
        float ausfahrt = Math.min(1F, zeit * 2F);
        float schwenk = Math.max(0F, (zeit - 0.5F) * 2F);

        RenderContext.pushPose();
        neige();
        RenderContext.translate(0F, -0.25F * ausfahrt, 0F);

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.28F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(30F * schwenk));
        RenderContext.translate(0F, -0.28F, 0F);
        ResourceManager.charger.renderPart("Left");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.28F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-30F * schwenk));
        RenderContext.translate(0F, -0.28F, 0F);
        ResourceManager.charger.renderPart("Right");
        RenderContext.popPose();

        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.setColor(1F, 0.75F, 0F, 1F);
        ResourceManager.charger.renderPart("Light");
        RenderContext.setColor(1F, 1F, 1F, 1F);

        neige();
        RenderContext.translate(0F, -0.25F * ausfahrt, 0F);
        ResourceManager.charger.renderPart("Slide");
        RenderContext.popPose();
    }

    /** Die feste Schraeglage des Arms: zehn Grad um die Aufhaengung bei (-0,34375|0,25). */
    private static void neige() {
        RenderContext.translate(-0.34375F, 0.25F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(10F));
        RenderContext.translate(0.34375F, -0.25F, 0F);
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.CHARGER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -0.35F, 0F);
                RenderContext.scale(1.8F, 1.8F, 1.8F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.CHARGER_TEX);
                ResourceManager.charger.renderPart("Base");
                ResourceManager.charger.renderPart("Slide");
                ResourceManager.charger.renderPart("Left");
                ResourceManager.charger.renderPart("Right");
            }
        };
    }
}
