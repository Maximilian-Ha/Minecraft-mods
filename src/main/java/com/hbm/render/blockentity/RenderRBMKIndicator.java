package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKIndicatorBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKIndicatorBlockEntity.IndicatorUnit;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.hbm.util.ColorUtil;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRBMKIndicator.
 *
 * Sechs Leuchten in zwei Spalten. Eine erloschene Lampe wird auf ein gutes Drittel
 * heruntergedimmt statt ganz schwarz -- so bleibt ihre Farbe erkennbar.
 *
 * ABWEICHUNG wie bei der Zeigertafel: das Original schaltet fuer die leuchtende Lampe die volle
 * Helligkeit ein. Hier uebernimmt das die Farbe des RenderContext.
 */
/* Kein IBEWLRProvider: die Tafel traegt wie im Original ein flaches Sinnbild
 * (rbmk/rbmk_display) als Gegenstandsmodell, keinen eigenen Darsteller. */
public class RenderRBMKIndicator extends BlockEntityRendererNT<RBMKIndicatorBlockEntity> {

    public static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/models/network/indicator.png");

    @Override
    public BlockEntityRenderer<RBMKIndicatorBlockEntity> create(Context context) {
        return new RenderRBMKIndicator();
    }

    @Override
    public void render(RBMKIndicatorBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        for(int i = 0; i < RBMKIndicatorBlockEntity.INDICATORS; i++) {

            IndicatorUnit unit = be.indicators[i];
            if(!unit.active) continue;

            RenderContext.pushPose();
            RenderContext.translate(0.25F, (i / 2) * -0.3125F + 0.3125F, (i % 2) * -0.5F + 0.25F);

            bindTexture(TEXTURE);
            ResourceManager.rbmk_indicator.renderPart("Base");

            float mult = unit.light ? 1F : 0.35F;
            RenderContext.setColor(ColorUtil.fr(unit.color) * mult, ColorUtil.fg(unit.color) * mult, ColorUtil.fb(unit.color) * mult, 1F);
            ResourceManager.rbmk_indicator.renderPart("Light");
            RenderContext.setColor(1F, 1F, 1F, 1F);

            this.drawLabel(unit, buffer);

            RenderContext.popPose();
        }
    }

    /** Die Beschriftung unter der Lampe, auf die Breite der Tafel gestaucht. */
    private void drawLabel(IndicatorUnit unit, MultiBufferSource buffer) {

        if(unit.label == null || unit.label.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        int width = font.width(unit.label);

        RenderContext.translate(0.0725F, 0.5F, 0F);

        /* Lange Beschriftungen werden kleiner, damit sie nicht ueber die Lampe hinauslaufen. */
        float scale = Math.min(0.0125F, 0.3F / Math.max(width, 1));
        RenderContext.scale(scale, -scale, scale);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        font.drawInBatch(unit.label, -width / 2F, -font.lineHeight / 2F, 0x000000, false,
                RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
    }
}
