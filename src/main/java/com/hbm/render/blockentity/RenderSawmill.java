package com.hbm.render.blockentity;

import com.hbm.blockentity.IPersistentNBT;
import com.hbm.blockentity.machine.SawmillBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.hbm.util.TagsUtil;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderSawmill.
 *
 * Drehwinkel je Blickrichtung, Blatt- und Zahnradbewegung unveraendert.
 * Das Original schaltet das Backface-Culling ausdruecklich EIN, was in 1.21 der
 * Ausgangszustand ist; deshalb steht hier kein RenderSystem-Aufruf.
 */
public class RenderSawmill extends BlockEntityRendererNT<SawmillBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<SawmillBlockEntity> create(Context context) { return new RenderSawmill(); }

    @Override
    public void render(SawmillBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            default -> { }
        }

        float rot = Mth.lerp(partialTicks, be.lastSpin, be.spin);
        this.renderCommon(rot, be.hasBlade);
    }

    private void renderCommon(float rot, boolean hasBlade) {

        this.bindTexture(ResourceManager.SAWMILL_TEX);
        ResourceManager.sawmill.renderPart("Main");

        if(hasBlade) {
            RenderContext.pushPose();
            RenderContext.translate(0F, 1.375F, 0F);
            RenderContext.mulPose(Axis.ZP.rotationDegrees(-rot * 2F));
            RenderContext.translate(0F, -1.375F, 0F);
            ResourceManager.sawmill.renderPart("Blade");
            RenderContext.popPose();
        }

        RenderContext.pushPose();
        RenderContext.translate(0.5625F, 1.375F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(rot));
        RenderContext.translate(-0.5625F, -1.375F, 0F);
        ResourceManager.sawmill.renderPart("GearLeft");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(-0.5625F, 1.375F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-rot));
        RenderContext.translate(0.5625F, -1.375F, 0F);
        ResourceManager.sawmill.renderPart("GearRight");
        RenderContext.popPose();
    }

    // Kein Zwischenspeichern in einem Feld: pro BlockEntityType gibt es nur EINEN
    // Renderer fuer alle Saegewerke.
    @Override
    public AABB getRenderBoundingBox(SawmillBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        return new AABB(
                pos.getX() - 1,
                pos.getY(),
                pos.getZ() - 1,
                pos.getX() + 2,
                pos.getY() + 2,
                pos.getZ() + 2
        );
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_SAWMILL.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1.5F, 0F);
                RenderContext.scale(3.25F, 3.25F, 3.25F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

                // Im Original entschied der Schadenswert 1 ueber das fehlende Blatt,
                // hier sind es die mitgefuehrten Zusatzdaten.
                CompoundTag persistent = TagsUtil.getCustomData(stack).getCompound(IPersistentNBT.NBT_PERSISTENT_KEY);
                boolean cog = !persistent.contains("hasBlade") || persistent.getBoolean("hasBlade");

                RenderSawmill.this.renderCommon(cog ? System.currentTimeMillis() % 3600 * 0.1F : 0, cog);
            }
        };
    }
}
