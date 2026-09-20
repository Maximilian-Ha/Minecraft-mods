package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.storage.FileCabinetBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.FileCabinetBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFileCabinet.
 *
 * Drei Teile, ein Modell: der Korpus steht, die beiden Schubladen fahren nach vorne. Die
 * Strecke -- 0,6875 Bloecke bei voll ausgefahrener Schublade -- ist die des Originals.
 *
 * ZWISCHENWERTE STATT SPRUENGE: gezeichnet wird nicht der Stand dieses Ticks, sondern der
 * Zwischenwert zum vorigen. Ohne ihn ruckte die Schublade zwanzigmal je Sekunde statt zu
 * gleiten -- deshalb fuehrt die Blockentitaet prevLowerExtent und prevUpperExtent mit.
 *
 * EINE BLOCKENTITAETSART, ZWEI TEXTUREN: gruen und stahlgrau sind im Original zwei
 * Metadatensorten desselben Blocks, im Port zwei Bloecke -- aber dieselbe Blockentitaet.
 * Welche Textur gilt, entscheidet darum der Block unter der Entitaet, nicht der Darsteller.
 * Das muss so sein: die Schleife in ClientProxy legt je Blockentitaetsart genau einen
 * Darsteller an.
 */
public class RenderFileCabinet extends BlockEntityRendererNT<FileCabinetBlockEntity> implements IBEWLRProvider {

    /** Wie weit die Schublade bei vollem Ausfahren nach vorne steht. */
    private static final float AUSZUG = 0.6875F;

    @Override public BlockEntityRenderer<FileCabinetBlockEntity> create(Context context) { return new RenderFileCabinet(); }

    @Override
    public void render(FileCabinetBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        /* Die Zuordnung des Originals, ausgerechnet: BlockDecoModel legt die Drehung als
         * Metadatenwert 0=Nord, 1=Sued, 2=West, 3=Ost ab (die Bitschieberei in
         * onBlockPlacedBy sagt es in ihren eigenen Kommentaren), und RenderFileCabinet
         * dreht dann 180, 0, 270, 90 Grad. */
        Direction facing = be.getBlockState().getValue(FileCabinetBlock.FACING);
        RenderContext.mulPose(Axis.YP.rotationDegrees(switch(facing) {
            case SOUTH -> 0F;
            case EAST -> 90F;
            case WEST -> 270F;
            default -> 180F;
        }));

        bindTexture(be.getBlockState().is(NtmBlocks.FILING_CABINET_STEEL.get())
                ? ResourceManager.FILE_CABINET_STEEL_TEX : ResourceManager.FILE_CABINET_TEX);
        ResourceManager.file_cabinet.renderPart("Cabinet");

        RenderContext.pushPose();
        float unten = be.prevLowerExtent + (be.lowerExtent - be.prevLowerExtent) * partialTicks;
        RenderContext.translate(0F, 0F, AUSZUG * unten);
        ResourceManager.file_cabinet.renderPart("LowerDrawer");
        RenderContext.popPose();

        RenderContext.pushPose();
        float oben = be.prevUpperExtent + (be.upperExtent - be.prevUpperExtent) * partialTicks;
        RenderContext.translate(0F, 0F, AUSZUG * oben);
        ResourceManager.file_cabinet.renderPart("UpperDrawer");
        RenderContext.popPose();
    }

    @Override
    public Item[] getItemsForRenderer() {
        return new Item[] { NtmBlocks.FILING_CABINET.asItem(), NtmBlocks.FILING_CABINET_STEEL.asItem() };
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(-1F, 0.5F, -1F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
                RenderContext.scale(4F, 4F, 4F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1.25F, 0F);
                RenderContext.scale(2.75F, 2.75F, 2.75F);
                ResourceLocation textur = stack.is(NtmBlocks.FILING_CABINET_STEEL.asItem())
                        ? ResourceManager.FILE_CABINET_STEEL_TEX : ResourceManager.FILE_CABINET_TEX;
                bindTexture(textur);
                ResourceManager.file_cabinet.renderAll();
            }
        };
    }
}
