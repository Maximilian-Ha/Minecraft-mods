package com.hbm.render.blockentity;

import com.hbm.blockentity.bomb.ChargeBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.level.block.Block;
import org.joml.Matrix4f;

import static com.hbm.blocks.bomb.ChargeBaseBlock.FACING;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderExplosiveCharge, zusammen mit dem
 * Blockteil aus RenderBlockRotated.
 *
 * ZWEI DARSTELLER WERDEN EINER. Das Original zeichnet den Koerper der Ladung mit einem
 * Blockdarsteller (ISBRH) und die Restzeit mit einem Blockentitaeten-Darsteller (TESR). 1.21
 * kennt kein ISBRH mehr; der Block ist hier unsichtbar und beides passiert an einer Stelle.
 * Die Drehtabelle ist die des TESR, weil sie die ausgeschriebene der beiden ist -- die des
 * Blockdarstellers sagt dasselbe, nur ueber zwei Winkelvariablen.
 *
 * ZWEI FORMEN, VIER TEXTUREN: Bergbau nimmt die Form des Dynamits, Semtex die des C4. Im
 * Original steht das als getRenderType(), der die renderID der jeweils anderen Klasse liefert.
 */
public class RenderExplosiveCharge extends BlockEntityRendererNT<ChargeBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<ChargeBlockEntity> create(Context context) { return new RenderExplosiveCharge(); }

    @Override
    public void render(ChargeBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        RenderContext.setup(poseStack, packedLight, packedOverlay);
        RenderContext.translate(0.5F, 0.5F, 0.5F);
        RenderSystem.disableCull();

        drehen(be.getBlockState().getValue(FACING));
        koerper(be.getBlockState().getBlock());

        /* Die Restzeit steht auf der Ladung, in demselben Gruen wie im Original. */
        Font font = Minecraft.getInstance().font;
        float f3 = 0.0125F;
        RenderContext.translate(-0.05F, 0.315F - 0.5F, 0.15F);
        RenderContext.scale(f3, -f3, f3);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F));
        Matrix4f matrix = RenderContext.poseStack().last().pose();
        font.drawInBatch(be.getMinutes() + ":" + be.getSeconds(), 0, 0, 0x00ff00, false, matrix, buffer,
                Font.DisplayMode.NORMAL, 0, packedLight);

        RenderSystem.enableCull();
        RenderContext.end();
    }

    /** Die Drehung nach der Flaeche, an der die Ladung klebt. Oben bleibt sie, wie sie ist. */
    private static void drehen(Direction seite) {
        switch(seite) {
            case DOWN -> RenderContext.mulPose(Axis.ZP.rotationDegrees(180F));
            case UP -> { }
            case NORTH -> { RenderContext.mulPose(Axis.YP.rotationDegrees(90F)); RenderContext.mulPose(Axis.ZN.rotationDegrees(90F)); }
            case SOUTH -> { RenderContext.mulPose(Axis.YN.rotationDegrees(90F)); RenderContext.mulPose(Axis.ZN.rotationDegrees(90F)); }
            case WEST -> { RenderContext.mulPose(Axis.YP.rotationDegrees(180F)); RenderContext.mulPose(Axis.ZN.rotationDegrees(90F)); }
            case EAST -> RenderContext.mulPose(Axis.ZN.rotationDegrees(90F));
        }
    }

    /** Form und Textur der vier Ladungen. */
    private static void koerper(Block block) {

        RenderSystem.setShaderTexture(0, textur(block));

        if(block == NtmBlocks.CHARGE_DYNAMITE.get() || block == NtmBlocks.CHARGE_MINER.get()) {
            ResourceManager.charge_dynamite.renderAll();
        } else {
            ResourceManager.charge_c4.renderAll();
        }
    }

    private static ResourceLocation textur(Block block) {
        if(block == NtmBlocks.CHARGE_DYNAMITE.get()) return ResourceManager.CHARGE_DYNAMITE_TEX;
        if(block == NtmBlocks.CHARGE_MINER.get()) return ResourceManager.CHARGE_MINER_TEX;
        if(block == NtmBlocks.CHARGE_SEMTEX.get()) return ResourceManager.CHARGE_SEMTEX_TEX;
        return ResourceManager.CHARGE_C4_TEX;
    }

    @Override
    public Item[] getItemsForRenderer() {
        return new Item[] {
                NtmBlocks.CHARGE_DYNAMITE.asItem(),
                NtmBlocks.CHARGE_MINER.asItem(),
                NtmBlocks.CHARGE_C4.asItem(),
                NtmBlocks.CHARGE_SEMTEX.asItem()
        };
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {

            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(6F, 6F, 6F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderSystem.disableCull();
                if(stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
                    koerper(blockItem.getBlock());
                }
                RenderSystem.enableCull();
            }
        };
    }
}
