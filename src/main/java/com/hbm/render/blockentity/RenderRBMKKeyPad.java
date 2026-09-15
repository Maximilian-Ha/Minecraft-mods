package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKKeyPadBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKKeyPadBlockEntity.KeyUnit;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.hbm.util.ColorUtil;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRBMKKeyPad.
 *
 * Vier Tasten in zwei Reihen. Eine gedrueckte Taste sitzt einen halben Zentimeter tiefer in der
 * Fassung und leuchtet in voller Farbe; eine gefallene wird auf knapp zwei Drittel gedimmt.
 */
public class RenderRBMKKeyPad extends BlockEntityRendererNT<RBMKKeyPadBlockEntity> implements IBEWLRProvider {

    public static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/models/network/keypad.png");

    @Override
    public BlockEntityRenderer<RBMKKeyPadBlockEntity> create(Context context) {
        return new RenderRBMKKeyPad();
    }

    @Override
    public void render(RBMKKeyPadBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        for(int i = 0; i < RBMKKeyPadBlockEntity.KEYS; i++) {

            KeyUnit unit = be.keys[i];
            if(!unit.active) continue;

            RenderContext.pushPose();
            RenderContext.translate(0.25F, (i / 2) * -0.5F + 0.25F, (i % 2) * -0.5F + 0.25F);

            bindTexture(TEXTURE);
            ResourceManager.rbmk_button.renderPart("Socket");

            RenderContext.pushPose();
            /* Die gedrueckte Taste verschwindet ein Stueck in ihrer Fassung. */
            if(unit.isPressed) RenderContext.translate(-0.03125F, 0F, 0F);

            float mult = unit.isPressed ? 1F : 0.65F;
            RenderContext.setColor(ColorUtil.fr(unit.color) * mult, ColorUtil.fg(unit.color) * mult, ColorUtil.fb(unit.color) * mult, 1F);

            if(unit.isPressed) FullBright.enable();
            ResourceManager.rbmk_button.renderPart("Button");
            if(unit.isPressed) FullBright.disable();

            RenderContext.setColor(1F, 1F, 1F, 1F);
            RenderContext.popPose();

            this.drawLabel(unit, buffer);

            RenderContext.popPose();
        }
    }

    /** Die Beschriftung unter der Taste. */
    private void drawLabel(KeyUnit unit, MultiBufferSource buffer) {

        if(unit.label == null || unit.label.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        int width = font.width(unit.label);

        RenderContext.translate(0.01F, 0.3125F, 0F);

        /* Lange Beschriftungen werden kleiner, damit sie nicht ueber die Taste hinauslaufen. */
        float scale = Math.min(0.0125F, 0.4F / Math.max(width, 1));
        RenderContext.scale(scale, -scale, scale);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        FullBright.enable();
        font.drawInBatch(unit.label, -width / 2F, -font.lineHeight / 2F, 0x00ff00, false,
                RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
        FullBright.disable();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.RBMK_KEYPAD.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(TEXTURE);

                /* Ausrichtung wie im Original, damit die Tafel im Inventar nicht schief liegt. */
                RenderContext.translate(0F, -0.5F, 0F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(-90F));

                for(int i = 0; i < RBMKKeyPadBlockEntity.KEYS; i++) {
                    RenderContext.pushPose();
                    RenderContext.translate(0.25F, (i / 2) * -0.5F + 0.25F, (i % 2) * -0.5F + 0.25F);
                    ResourceManager.rbmk_button.renderPart("Socket");
                    RenderContext.setColor(0.65F, 0F, 0F, 1F);
                    ResourceManager.rbmk_button.renderPart("Button");
                    RenderContext.setColor(1F, 1F, 1F, 1F);
                    RenderContext.popPose();
                }
            }
        };
    }
}
