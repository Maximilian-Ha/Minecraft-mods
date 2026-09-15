package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineArcFurnaceLargeBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderArcFurnace.
 *
 * Der Deckel hebt sich, die Elektroden haengen an schwingenden Kabeln, und was drin ist, sieht
 * man am Fuellstand: kalt, wenn nur Gut im Ofen liegt, gluehend, wenn es geschmolzen ist.
 */
public class RenderArcFurnace extends BlockEntityRendererNT<MachineArcFurnaceLargeBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineArcFurnaceLargeBlockEntity> create(Context context) {
        return new RenderArcFurnace();
    }

    @Override
    public void render(MachineArcFurnaceLargeBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
        }

        float lift = BobMathUtil.interp(be.prevLid, be.lid, partialTicks);
        double time = (be.getLevel() != null ? be.getLevel().getGameTime() : 0) + partialTicks;

        RenderSystem.enableCull();
        bindTexture(ResourceManager.ARC_FURNACE_TEX);
        ResourceManager.arc_furnace.renderPart("Furnace");

        if(!be.liquids.isEmpty()) {
            FullBright.enable();
            RenderContext.setLightning(false);
            RenderContext.translate(0F, (float) (-1.75D + MachineArcFurnaceLargeBlockEntity.getStackAmount(be.liquids) * 1.75D / MachineArcFurnaceLargeBlockEntity.maxLiquid), 0F);
            ResourceManager.arc_furnace.renderPart("ContentsHot");
            RenderContext.setLightning(true);
            FullBright.disable();
        } else if(be.hasMaterial) {
            ResourceManager.arc_furnace.renderPart("ContentsCold");
        }

        RenderContext.translate(0F, 2F * lift, 0F);
        if(be.isProgressing) RenderContext.translate(0F, 0F, (float) (Math.sin(time) * 0.005D));

        ResourceManager.arc_furnace.renderPart("Lid");

        for(int i = 0; i < 3; i++) {
            if(be.electrodes[i] != MachineArcFurnaceLargeBlockEntity.ELECTRODE_NONE) ResourceManager.arc_furnace.renderPart("Ring" + (i + 1));
        }
        for(int i = 0; i < 3; i++) {
            if(be.electrodes[i] == MachineArcFurnaceLargeBlockEntity.ELECTRODE_FRESH) ResourceManager.arc_furnace.renderPart("Electrode" + (i + 1));
        }

        FullBright.enable();
        RenderContext.setLightning(false);
        for(int i = 0; i < 3; i++) {
            if(be.electrodes[i] == MachineArcFurnaceLargeBlockEntity.ELECTRODE_USED) ResourceManager.arc_furnace.renderPart("Electrode" + (i + 1) + "Hot");
            if(be.electrodes[i] == MachineArcFurnaceLargeBlockEntity.ELECTRODE_DEPLETED) ResourceManager.arc_furnace.renderPart("Electrode" + (i + 1) + "Short");
        }
        RenderContext.setLightning(true);
        FullBright.disable();

        // die drei Kabel schwingen mit, solange der Ofen laeuft
        float[] cableOffsets = { 0.5F, 0F, -0.5F };

        for(int i = 0; i < 3; i++) {

            if(be.electrodes[i] == MachineArcFurnaceLargeBlockEntity.ELECTRODE_NONE) continue;

            RenderContext.pushPose();
            RenderContext.translate(0F, 5.5F, cableOffsets[i]);
            if(be.isProgressing) RenderContext.mulPose(Axis.XP.rotationDegrees((float) (Math.sin(time / 2D) * 30D)));
            RenderContext.translate(0F, -5.5F, -cableOffsets[i]);
            ResourceManager.arc_furnace.renderPart("Cable" + (i + 1));
            RenderContext.popPose();
        }
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles oberhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block hoch.
     */
    @Override
    public AABB getRenderBoundingBox(MachineArcFurnaceLargeBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_ARC_FURNACE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 0F);
                RenderContext.scale(3.5F, 3.5F, 3.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);

                bindTexture(ResourceManager.ARC_FURNACE_TEX);
                ResourceManager.arc_furnace.renderPart("Furnace");
                ResourceManager.arc_furnace.renderPart("Lid");
                for(int i = 1; i <= 3; i++) {
                    ResourceManager.arc_furnace.renderPart("Ring" + i);
                    ResourceManager.arc_furnace.renderPart("Electrode" + i);
                    ResourceManager.arc_furnace.renderPart("Cable" + i);
                }
            }
        };
    }
}
