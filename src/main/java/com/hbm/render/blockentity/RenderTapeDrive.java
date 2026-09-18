package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineTapeDriveBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.MachineTapeDriveBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderTapeDrive.
 *
 * Runde 161 nachgereicht -- der Darsteller fehlte seit Runde 123.
 *
 * Zwoelf Schaechte in zwei Reihen. Gezeichnet wird nur, was belegt ist, und darueber je ein
 * Laempchen in der Farbe des Schachtinhalts: rot fuer beliebig, gelb fuer ein leeres Band,
 * gruen fuer ein beschriebenes.
 */
public class RenderTapeDrive extends BlockEntityRendererNT<MachineTapeDriveBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineTapeDriveBlockEntity> create(Context context) { return new RenderTapeDrive(); }

    @Override
    public void render(MachineTapeDriveBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        /* Zuordnung woertlich aus dem Original, dort ueber die rohe Metadata 2 bis 5. */
        Direction facing = be.getBlockState().getValue(MachineTapeDriveBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            default -> { }
        }

        bindTexture(ResourceManager.TAPE_DRIVE_TEX);
        ResourceManager.tape_drive.renderPart("Frame");

        for(int i = 0; i < be.tapes.length; i++) {
            if(be.tapes[i] == MachineTapeDriveBlockEntity.SLOT_EMPTY) continue;

            RenderContext.pushPose();
            RenderContext.translate(0F, 0.25F - 0.5F * (i / 6), 0.3125F - (i % 6) * 0.125F);
            ResourceManager.tape_drive.renderPart("Drive");
            RenderContext.popPose();
        }

        FullBright.enable();

        for(int i = 0; i < be.tapes.length; i++) {
            byte tape = be.tapes[i];
            if(tape == MachineTapeDriveBlockEntity.SLOT_EMPTY) continue;

            if(tape == MachineTapeDriveBlockEntity.SLOT_ANY) RenderContext.setColor(1F, 0F, 0F, 1F);
            if(tape == MachineTapeDriveBlockEntity.SLOT_EMPTY_TAPE) RenderContext.setColor(1F, 0.75F, 0F, 1F);
            if(tape == MachineTapeDriveBlockEntity.SLOT_FILLED_TAPE) RenderContext.setColor(0F, 1F, 0F, 1F);

            RenderContext.pushPose();
            RenderContext.translate(0F, 0.25F - 0.5F * (i / 6), 0.3125F - (i % 6) * 0.125F);
            ResourceManager.tape_drive.renderPart("Light");
            RenderContext.popPose();
        }

        RenderContext.setColor(1F, 1F, 1F, 1F);
        FullBright.disable();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_TAPE_DRIVE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 0F);
                RenderContext.scale(5F, 5F, 5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(2F, 2F, 2F);
                bindTexture(ResourceManager.TAPE_DRIVE_TEX);
                ResourceManager.tape_drive.renderPart("Frame");
            }
        };
    }
}
