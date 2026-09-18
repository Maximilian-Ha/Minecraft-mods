package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineCyclotronBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderCyclotron.
 *
 * Runde 163 nachgereicht. Runde 162 musste das Zyklotron zurueckstellen: sein Darsteller
 * liest vier Sockel ueber getPlug(), und die gab es im Port noch gar nicht. Das Steckersystem
 * ist jetzt nachgezogen, damit auch dieses Bild.
 *
 * Der Gegenstandsdarsteller zeigt die Maschine mit LEEREN Sockeln -- so steht es im Original
 * (ItemRenderLibrary), und ein Stecker im Inventarbild waere ohnehin irrefuehrend.
 *
 * Jeder der vier Sockel zeigt zwei Zustaende -- leer oder gefuellt -- ueber eine eigene
 * Textur. Stecken alle vier, dreht sich ein Ring aus Standard-Galactic-Schrift um die
 * Maschine: "plures necat crapula quam gladius", mehr toetet der Rausch als das Schwert.
 */
public class RenderCyclotron extends BlockEntityRendererNT<MachineCyclotronBlockEntity> implements IBEWLRProvider {

    /** Die Schrift der Verzauberungstafel; im Original der standardGalacticFontRenderer. */
    private static final Style GALAKTISCH = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt"));
    private static final String SPRUCH = "plures necat crapula quam gladius";

    @Override public BlockEntityRenderer<MachineCyclotronBlockEntity> create(Context context) { return new RenderCyclotron(); }

    @Override
    public void render(MachineCyclotronBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderSystem.disableCull();

        bindTexture(ResourceManager.CYCLOTRON_TEX);
        ResourceManager.cyclotron.renderPart("Body");

        boolean alleGesteckt = true;
        for(int i = 0; i < 4; i++) {
            boolean gesteckt = be.getPlug(i);
            if(!gesteckt) alleGesteckt = false;
            bindTexture(sockelBild(i, gesteckt));
            ResourceManager.cyclotron.renderPart("B" + (i + 1));
        }

        if(alleGesteckt) this.zeichneSpruch(buffer);

        RenderSystem.enableCull();
    }

    /** Sockelbild je Sockel und Zustand -- Reihenfolge wie getItemForPlug. */
    private static ResourceLocation sockelBild(int index, boolean gesteckt) {
        return switch(index) {
            case 0 -> gesteckt ? ResourceManager.CYCLOTRON_ASHES_FILLED_TEX : ResourceManager.CYCLOTRON_ASHES_TEX;
            case 1 -> gesteckt ? ResourceManager.CYCLOTRON_BOOK_FILLED_TEX : ResourceManager.CYCLOTRON_BOOK_TEX;
            case 2 -> gesteckt ? ResourceManager.CYCLOTRON_GAVEL_FILLED_TEX : ResourceManager.CYCLOTRON_GAVEL_TEX;
            default -> gesteckt ? ResourceManager.CYCLOTRON_COIN_FILLED_TEX : ResourceManager.CYCLOTRON_COIN_TEX;
        };
    }

    private void zeichneSpruch(MultiBufferSource buffer) {

        Font font = Minecraft.getInstance().font;

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees((float) (System.currentTimeMillis() * 0.025D % 360D)));

        FullBright.enable();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);

        RenderContext.translate(0F, 2F, 0F);
        // Der Ring steht auf dem Kopf: erst danach zeigt die Schrift richtig herum nach aussen.
        RenderContext.mulPose(Axis.XP.rotationDegrees(180F));

        float winkel = 0F;
        for(char c : SPRUCH.toCharArray()) {
            Component zeichen = Component.literal(String.valueOf(c)).withStyle(GALAKTISCH);

            RenderContext.pushPose();
            RenderContext.mulPose(Axis.YP.rotationDegrees(winkel));
            winkel -= font.width(zeichen) * 2F;

            RenderContext.translate(2.75F, 0F, 0F);
            RenderContext.mulPose(Axis.YN.rotationDegrees(90F));
            RenderContext.scale(0.1F, 0.1F, 0.1F);

            font.drawInBatch(zeichen, 0F, 0F, 0x600060, false,
                    RenderContext.poseStack().last().pose(), buffer, DisplayMode.NORMAL, 0, RenderContext.light());
            RenderContext.popPose();
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        FullBright.disable();
        RenderContext.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(MachineCyclotronBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_CYCLOTRON.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1F, 0F);
                RenderContext.scale(2.75F, 2.75F, 2.75F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.CYCLOTRON_TEX);
                ResourceManager.cyclotron.renderPart("Body");
                bindTexture(ResourceManager.CYCLOTRON_ASHES_TEX);
                ResourceManager.cyclotron.renderPart("B1");
                bindTexture(ResourceManager.CYCLOTRON_BOOK_TEX);
                ResourceManager.cyclotron.renderPart("B2");
                bindTexture(ResourceManager.CYCLOTRON_GAVEL_TEX);
                ResourceManager.cyclotron.renderPart("B3");
                bindTexture(ResourceManager.CYCLOTRON_COIN_TEX);
                ResourceManager.cyclotron.renderPart("B4");
            }
        };
    }
}
