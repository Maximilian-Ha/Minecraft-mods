package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity.RBMKColumn;
import com.hbm.blockentity.machine.rbmk.RBMKConsoleBlockEntity.RBMKScreen;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRBMKConsole.
 *
 * Das Reaktorpult. Auf der Rueckwand steht das 15x15-Raster aller Saeulen -- dasselbe, das die
 * Rasteranzeige im Kleinen zeigt, darum kommt es aus RBMKGridPainter. Davor haengen sechs
 * Anzeigen mit je einem gemittelten Wert.
 *
 * ABWEICHUNGEN:
 * - Das Original setzt die Beschriftung aus einem Uebersetzungsschluessel und dem Wert zusammen
 *   ("rbmk.screen.temp=123.4 Grad"). Der Port haelt in screen.display nur die Zahl und holt die
 *   Einheit aus ScreenType -- so macht es auch schon die Oberflaeche des Pults. Die Beschriftung
 *   davor entfaellt damit; am Pult steht neben jeder Anzeige ohnehin, was sie zeigt.
 * - Das Original schaltet fuer die Schrift das Tiefenschreiben ab. Das gibt es beim
 *   Stapelzeichnen nicht mehr; die Schrift liegt weit genug vor dem Pult, um nicht zu streiten.
 */
public class RenderRBMKConsole extends BlockEntityRendererNT<RBMKConsoleBlockEntity> implements IBEWLRProvider {

    public static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/models/machines/rbmk_control.png");

    /** Wie weit das Raster vor der Rueckwand schwebt. Zahlen aus dem Original. */
    private static final float PLATE_DEPTH = -0.3725F;
    private static final float CELL = 0.125F;

    @Override
    public BlockEntityRenderer<RBMKConsoleBlockEntity> create(Context context) {
        return new RenderRBMKConsole();
    }

    @Override
    public void render(RBMKConsoleBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        RenderContext.translate(0.5F, 0F, 0F);

        bindTexture(TEXTURE);
        ResourceManager.rbmk_console.renderAll();

        this.drawGrid(be, buffer);
        this.drawScreens(be, buffer);
    }

    /** Das 15x15-Raster auf der Rueckwand. */
    private void drawGrid(RBMKConsoleBlockEntity be, MultiBufferSource buffer) {

        VertexConsumer consumer = RBMKGridPainter.consumer(buffer);
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        for(int i = 0; i < be.columns.length; i++) {

            RBMKColumn col = be.columns[i];
            if(col == null) continue;

            float y = -(i / RBMKConsoleBlockEntity.GRID) * CELL + 3.625F;
            float z = -(i % RBMKConsoleBlockEntity.GRID) * CELL + CELL * 7F;

            RBMKGridPainter.column(consumer, matrix, PLATE_DEPTH, y, z, i, col);
        }
    }

    /** Die sechs Anzeigen, zwei nebeneinander in drei Reihen. */
    private void drawScreens(RBMKConsoleBlockEntity be, MultiBufferSource buffer) {

        Font font = Minecraft.getInstance().font;

        RenderContext.translate(-0.42F, 3.5F, 1.75F);

        FullBright.enable();

        for(int i = 0; i < be.screens.length; i++) {

            RBMKScreen screen = be.screens[i];
            if(screen.display == null || screen.display.isEmpty()) continue;

            String text = screen.display + screen.type.unit();

            RenderContext.pushPose();

            /* Die rechte Spalte sitzt auf der anderen Seite des Pults. */
            if(i % 2 == 1) RenderContext.translate(0F, 0F, 1.75F * -2F);
            RenderContext.translate(0F, -0.75F * (i / 2), 0F);

            int width = font.width(text);

            float scale = Math.min(0.03F, 0.8F / Math.max(width, 1));
            RenderContext.scale(scale, -scale, scale);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

            font.drawInBatch(text, -width / 2F, -font.lineHeight / 2F, 0x00ff00, false,
                    RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());

            RenderContext.popPose();
        }

        FullBright.disable();
    }

    @Override
    public AABB getRenderBoundingBox(RBMKConsoleBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.RBMK_CONSOLE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -2F, 0F);
                RenderContext.scale(0.35F, 0.35F, 0.35F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(TEXTURE);
                ResourceManager.rbmk_console.renderAll();
            }
        };
    }
}
