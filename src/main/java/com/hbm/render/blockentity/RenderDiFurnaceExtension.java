package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.DiFurnaceExtensionBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.block.RenderDiFurnaceExtension.
 *
 * Das Original zeichnete die drei Teile des Modells mit je einem Blocksymbol; hier wird
 * fuer jedes Teil die entsprechende Blocktextur gebunden. Das Modell ist um den Ursprung
 * zentriert, daher der Versatz auf die Blockmitte.
 *
 * Das Modell ist genau einen Block gross, darum braucht es keine eigene Renderbox.
 */
public class RenderDiFurnaceExtension extends BlockEntityRendererNT<DiFurnaceExtensionBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<DiFurnaceExtensionBlockEntity> create(Context context) {
        return new RenderDiFurnaceExtension();
    }

    @Override
    public void render(DiFurnaceExtensionBlockEntity be, MultiBufferSource buffer, float partialTicks) {
        RenderContext.translate(0.5F, 0F, 0.5F);
        this.renderModel();
    }

    private void renderModel() {
        this.bindTexture(ResourceManager.DIFURNACE_EXTENSION_TOP_TEX);
        ResourceManager.difurnace_extension.renderPart("Top");

        this.bindTexture(ResourceManager.DIFURNACE_EXTENSION_BOTTOM_TEX);
        ResourceManager.difurnace_extension.renderPart("Bottom");

        this.bindTexture(ResourceManager.DIFURNACE_EXTENSION_SIDE_TEX);
        ResourceManager.difurnace_extension.renderPart("Side");
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_DIFURNACE_EXTENSION.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -4.5F, 0F);
                RenderContext.scale(9F, 9F, 9F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderDiFurnaceExtension.this.renderModel();
            }
        };
    }
}
