package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKTerminalBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.screens.RBMKTerminalScreen;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRBMKTerminal.
 *
 * Achtzehn Zeilen Text auf der Tafel: oben die gerade getippte Zeile, darunter der Verlauf.
 * Waehrend fortwaehrend gesendet wird, faerbt sich die Schrift orange.
 *
 * Zeilen werden nicht umgebrochen, sondern Zeichen fuer Zeichen gesetzt und abgeschnitten, sobald
 * die Tafel voll ist -- so wie im Original.
 */
public class RenderRBMKTerminal extends BlockEntityRendererNT<RBMKTerminalBlockEntity> implements IBEWLRProvider {

    public static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/models/network/terminal.png");

    /** Achtzehn Zeilen: die getippte plus siebzehn aus dem Verlauf. */
    private static final int ROWS = RBMKTerminalBlockEntity.LINES + 1;
    /** Wie breit eine Zeile werden darf, in den Einheiten der Schrift. */
    private static final int MAX_WIDTH = 172;

    private static final String PREFIX = "> ";

    @Override
    public BlockEntityRenderer<RBMKTerminalBlockEntity> create(Context context) {
        return new RenderRBMKTerminal();
    }

    @Override
    public void render(RBMKTerminalBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        RenderContext.translate(0.25F, 0F, 0F);
        bindTexture(TEXTURE);
        ResourceManager.rbmk_terminal.renderAll();

        RenderContext.translate(0.0635F, 0.125F, 0.0625F * 5.5F);

        Font font = Minecraft.getInstance().font;
        float scale = 1F / 250F;
        int color = be.doesRepeat ? 0xff8000 : 0x00ff00;

        /* Der Schreibstrich blinkt nur an dem Terminal, vor dem gerade jemand steht. */
        String caret = RBMKTerminalScreen.lastTerminal == be && BobMathUtil.getBlink() ? "_" : "";
        int caretWidth = font.width(caret);

        FullBright.enable();

        for(int i = 0; i < ROWS; i++) {

            String label = i == 0 ? RBMKTerminalScreen.getWorkingLine(be) : be.history[i - 1];
            if(label == null) label = "";

            StringBuilder builder = new StringBuilder(40);
            if(i == 0 || !label.isEmpty()) builder.append(PREFIX);

            int width = font.width(PREFIX);
            for(int j = 0; j < label.length(); j++) {
                char c = label.charAt(j);
                width += font.width(String.valueOf(c));
                if(width > MAX_WIDTH) break;
                builder.append(c);
            }

            if(i == 0 && font.width(builder.toString()) + caretWidth <= MAX_WIDTH) builder.append(caret);

            RenderContext.translate(0F, 10F * scale, 0F);

            RenderContext.pushPose();
            RenderContext.scale(scale, -scale, scale);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

            font.drawInBatch(builder.toString(), 0F, -font.lineHeight / 2F, color, false,
                    RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());

            RenderContext.popPose();
        }

        FullBright.disable();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.RBMK_TERMINAL.asItem();
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
                RenderContext.translate(0.25F, 0F, 0F);

                ResourceManager.rbmk_terminal.renderAll();
            }
        };
    }
}
