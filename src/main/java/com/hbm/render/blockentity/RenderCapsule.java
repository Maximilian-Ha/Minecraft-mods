package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.storage.SoyuzCapsuleBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.SoyuzCapsuleBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderCapsule.
 *
 * Die gelandete Kapsel steht schief -- das Original dreht sie um fuenfundzwanzig Grad um die
 * senkrechte und um fuenfzehn um die Laengsachse und setzt sie ein Viertel tiefer. Sie ist
 * nicht aufgesetzt, sie ist eingeschlagen.
 *
 * ROSTIG ODER NICHT: das Original liest die Blockmetadate und nimmt bei 3 die verrostete
 * Textur. Im Port steht dafuer die Zustandseigenschaft rusty.
 */
public class RenderCapsule extends BlockEntityRendererNT<SoyuzCapsuleBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<SoyuzCapsuleBlockEntity> create(Context context) { return new RenderCapsule(); }

    @Override
    public void render(SoyuzCapsuleBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        RenderContext.setup(poseStack, packedLight, packedOverlay);
        RenderContext.translate(0.5F, 0F, 0.5F);

        koerper(be.getBlockState().getValue(SoyuzCapsuleBlock.RUSTY));

        RenderContext.end();
    }

    /** Lage und Textur. Beides teilt sich der Block mit dem Gegenstand im Inventar. */
    private static void koerper(boolean rostig) {

        RenderContext.translate(0F, -0.25F, 0F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(-25F));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(15F));

        RenderSystem.setShaderTexture(0, rostig ? ResourceManager.SOYUZ_LANDER_RUST_TEX : ResourceManager.SOYUZ_LANDER_TEX);
        ResourceManager.soyuz_lander.renderPart("Capsule");
    }

    @Override
    public Item[] getItemsForRenderer() {
        return new Item[] { NtmBlocks.SOYUZ_CAPSULE.asItem() };
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {

            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -4F, 0F);
                RenderContext.scale(5F, 5F, 5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                koerper(false);
            }
        };
    }
}
