package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKLeverBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKLeverBlockEntity.LeverUnit;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRBMKLever.
 *
 * Zwei Kipphebel nebeneinander. Der Hebel dreht sich um seinen Lagerpunkt bei (0.125, 0.5625)
 * um bis zu 180 Grad, je nachdem, wie weit er umgelegt ist.
 */
/* Kein IBEWLRProvider: die Tafel traegt wie im Original ein flaches Sinnbild
 * (rbmk/rbmk_display) als Gegenstandsmodell, keinen eigenen Darsteller. */
public class RenderRBMKLever extends BlockEntityRendererNT<RBMKLeverBlockEntity> {

    public static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/models/network/lever.png");

    @Override
    public BlockEntityRenderer<RBMKLeverBlockEntity> create(Context context) {
        return new RenderRBMKLever();
    }

    @Override
    public void render(RBMKLeverBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        for(int i = 0; i < RBMKLeverBlockEntity.LEVERS; i++) {

            LeverUnit unit = be.levers[i];
            if(!unit.active) continue;

            RenderContext.pushPose();
            RenderContext.translate(0.25F, 0F, i * -0.5F + 0.25F);

            bindTexture(TEXTURE);
            ResourceManager.rbmk_lever.renderPart("Base");

            RenderContext.pushPose();
            float progress = BobMathUtil.interp(unit.prevFlipProgress, unit.flipProgress, partialTicks);
            RenderContext.translate(0.125F, 0.5625F, 0F);
            RenderContext.mulPose(Axis.ZP.rotationDegrees(-180F * progress));
            RenderContext.translate(-0.125F, -0.5625F, 0F);
            ResourceManager.rbmk_lever.renderPart("Lever");
            RenderContext.popPose();

            this.drawLabel(unit, buffer);

            RenderContext.popPose();
        }
    }

    /** Die Beschriftung unter dem Hebel. */
    private void drawLabel(LeverUnit unit, MultiBufferSource buffer) {

        if(unit.label == null || unit.label.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        int width = font.width(unit.label);

        RenderContext.translate(0.01F, 0.0625F, 0F);

        /* Lange Beschriftungen werden kleiner, damit sie nicht ueber den Hebel hinauslaufen. */
        float scale = Math.min(0.0125F, 0.4F / Math.max(width, 1));
        RenderContext.scale(scale, -scale, scale);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        FullBright.enable();
        font.drawInBatch(unit.label, -width / 2F, -font.lineHeight / 2F, 0x00ff00, false,
                RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
        FullBright.disable();
    }
}
