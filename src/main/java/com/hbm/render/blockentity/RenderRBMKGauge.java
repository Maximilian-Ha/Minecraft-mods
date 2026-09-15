package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKGaugeBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKGaugeBlockEntity.GaugeUnit;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ColorUtil;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRBMKGauge.
 *
 * Vier Rundinstrumente im Quadrat. Der Zeiger schwenkt ueber achtzig Grad; die beiden
 * Skalenendwerte stehen mitgedreht auf dem Zifferblatt.
 *
 * ABWEICHUNG: das Original zeichnet den Zeiger mit abgeschalteter Textur und voller Helligkeit.
 * Hier uebernimmt das die Farbe des RenderContext -- ein eigener Vollhelligkeitsschalter wie
 * RenderArcFurnace.fullbright ist im Port nicht noetig, weil die Beleuchtung ohnehin am
 * Lichtwert des Blocks haengt.
 */
public class RenderRBMKGauge extends BlockEntityRendererNT<RBMKGaugeBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<RBMKGaugeBlockEntity> create(Context context) {
        return new RenderRBMKGauge();
    }

    @Override
    public void render(RBMKGaugeBlockEntity gauge, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = gauge.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        for(int i = 0; i < RBMKGaugeBlockEntity.GAUGES; i++) {

            GaugeUnit unit = gauge.gauges[i];
            if(!unit.active) continue;

            RenderContext.pushPose();
            RenderContext.translate(0.25F, (i / 2) * -0.5F + 0.25F, (i % 2) * -0.5F + 0.25F);

            bindTexture(ResourceManager.RBMK_GAUGE_TEX);
            ResourceManager.rbmk_gauge.renderPart("Gauge");

            RenderContext.pushPose();
            RenderContext.setColor(ColorUtil.fr(unit.color), ColorUtil.fg(unit.color), ColorUtil.fb(unit.color), 1F);

            RenderContext.translate(0F, 0.4375F, -0.125F);
            RenderContext.mulPose(Axis.XN.rotationDegrees((float) needleAngle(unit, partialTicks)));
            RenderContext.translate(0F, -0.4375F, 0.125F);

            ResourceManager.rbmk_gauge.renderPart("Needle");

            RenderContext.setColor(1F, 1F, 1F, 1F);
            RenderContext.popPose();

            this.drawScale(unit, buffer);

            RenderContext.popPose();
        }
    }

    /** Die beiden Skalenendwerte, mitgedreht auf das Zifferblatt geschrieben. */
    private void drawScale(GaugeUnit unit, MultiBufferSource buffer) {

        Font font = Minecraft.getInstance().font;
        float scale = 0.0025F;

        String lower = Math.abs(unit.min) <= 10_000 ? String.valueOf(unit.min) : BobMathUtil.getShortNumber(unit.min);
        String upper = Math.abs(unit.max) <= 10_000 ? String.valueOf(unit.max) : BobMathUtil.getShortNumber(unit.max);

        for(int j = 0; j < 2; j++) {

            RenderContext.pushPose();
            RenderContext.translate(0F, 0.4375F, -0.125F);
            RenderContext.mulPose(Axis.XN.rotationDegrees(10F + j * 50F));
            RenderContext.translate(0F, -0.4375F, 0.125F);

            RenderContext.translate(0.032F, 0.4375F, 0.125F);
            RenderContext.scale(scale, -scale, scale);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

            font.drawInBatch(j == 0 ? lower : upper, 0F, -font.lineHeight / 2F, 0x000000, false,
                    RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());

            RenderContext.popPose();
        }
    }

    /**
     * Wo der Zeiger steht. Der Ausschlag betraegt fuenfzig Grad; steht der Kleinstwert ueber dem
     * Groesstwert, laeuft die Skala andersherum. Abgeschnitten wird bei achtzig Grad -- so sitzt
     * der Zeiger im Anschlag statt durch das Gehaeuse zu wandern.
     */
    private static double needleAngle(GaugeUnit unit, float partialTicks) {

        double value = BobMathUtil.interp((float) unit.lastRenderValue, (float) unit.renderValue, partialTicks);

        long lower = Math.min(unit.min, unit.max);
        long upper = Math.max(unit.min, unit.max);
        if(lower == upper) upper += 1;

        double angle = (value - lower) / (double) (upper - lower) * 50D;
        if(unit.min > unit.max) angle = 50D - angle;

        return Mth.clamp(angle, 0D, 80D) - 85D;
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.RBMK_GAUGE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.RBMK_GAUGE_TEX);
                ResourceManager.rbmk_gauge.renderPart("Gauge");
            }
        };
    }
}
