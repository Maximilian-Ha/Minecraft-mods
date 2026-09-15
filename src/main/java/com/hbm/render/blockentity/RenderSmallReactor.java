package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.ReactorResearchBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderSmallReactor.
 *
 * Das Becken steht fest, die Steuerstaebe fahren mit ihrer Stellung auf und ab.
 *
 * ABWEICHUNG: das Tscherenkow-Leuchten -- ein Stapel blauer Quadrate im Wasser, sobald der Fluss
 * ueber zehn liegt -- ist NICHT UEBERNOMMEN. Es ist ein handgeschriebener Verlauf ohne Textur, der
 * an die feste Pipeline der alten Fassung gebunden ist.
 */
public class RenderSmallReactor extends BlockEntityRendererNT<ReactorResearchBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<ReactorResearchBlockEntity> create(Context context) { return new RenderSmallReactor(); }

    @Override
    public void render(ReactorResearchBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

        bindTexture(ResourceManager.REACTOR_SMALL_BASE_TEX);
        ResourceManager.reactor_small_base.renderAll();

        /* Die Staebe haengen an der Stellung: ganz drin bei null, ganz heraus bei eins. */
        float rods = Mth.lerp(partialTicks, (float) be.lastLevel, (float) be.rodLevel);

        RenderContext.pushPose();
        RenderContext.translate(0F, rods, 0F);
        bindTexture(ResourceManager.REACTOR_SMALL_RODS_TEX);
        ResourceManager.reactor_small_rods.renderAll();
        RenderContext.popPose();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.REACTOR_RESEARCH.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1F, 0F);
                RenderContext.scale(2F, 2F, 2F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                bindTexture(ResourceManager.REACTOR_SMALL_BASE_TEX);
                ResourceManager.reactor_small_base.renderAll();
                bindTexture(ResourceManager.REACTOR_SMALL_RODS_TEX);
                ResourceManager.reactor_small_rods.renderAll();
            }
        };
    }
}
