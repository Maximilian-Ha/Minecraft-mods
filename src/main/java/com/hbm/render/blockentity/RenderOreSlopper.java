package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineOreSlopperBlockEntity;
import com.hbm.blockentity.machine.MachineOreSlopperBlockEntity.SlopperAnimation;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.items.NtmItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderOreSlopper.
 *
 * Vier bewegte Teile: der Schlitten faehrt vor und zurueck, die Schaufel faehrt hinunter und
 * wieder herauf, zwei Messerwalzen drehen gegenlaeufig, und das Geblaese laeuft.
 *
 * WAEHREND DIE SCHAUFEL HOCHFAEHRT, HAENGT EIN ERZ DARIN -- das Original zeichnet dafuer ein
 * Grundgesteinserz an die Schaufel; das bleibt so.
 */
public class RenderOreSlopper extends BlockEntityRendererNT<MachineOreSlopperBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineOreSlopperBlockEntity> create(Context context) { return new RenderOreSlopper(); }

    @Override
    public void render(MachineOreSlopperBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
        }

        bindTexture(ResourceManager.ORE_SLOPPER_TEX);
        ResourceManager.ore_slopper.renderPart("Base");

        RenderContext.pushPose();

        float slide = BobMathUtil.interp(be.prevSlider, be.slider, partialTicks);
        RenderContext.translate(0F, 0F, slide * -3F);
        ResourceManager.ore_slopper.renderPart("Slider");

        RenderContext.pushPose();
        float extend = BobMathUtil.interp(be.prevBucket, be.bucket, partialTicks) * 1.5F;
        RenderContext.translate(0F, -Mth.clamp(extend - 0.25F, 0F, 1.25F), 0F);
        ResourceManager.ore_slopper.renderPart("Hydraulics");
        RenderContext.translate(0F, -Mth.clamp(extend, 0F, 1.25F), 0F);
        ResourceManager.ore_slopper.renderPart("Bucket");

        /* Beim Hochfahren haengt ein Erz in der Schaufel. */
        if(be.animation == SlopperAnimation.LIFTING) {

            RenderContext.pushPose();
            RenderContext.translate(0.0625F, 4.3125F, 2F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(-90F));
            RenderContext.scale(1.75F, 1.75F, 1.75F);

            ItemStack stack = new ItemStack(NtmItems.BEDROCK_ORE_BASE.get());
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel model = renderer.getModel(stack, null, null, 0);
            renderer.render(stack, ItemDisplayContext.FIXED, false, RenderContext.poseStack(), buffer, RenderContext.light(), RenderContext.overlay(), model);

            RenderContext.popPose();
            bindTexture(ResourceManager.ORE_SLOPPER_TEX);
        }

        RenderContext.popPose();
        RenderContext.popPose();

        float blades = BobMathUtil.interp(be.prevBlades, be.blades, partialTicks);

        RenderContext.pushPose();
        RenderContext.translate(0.375F, 2.75F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(blades));
        RenderContext.translate(-0.375F, -2.75F, 0F);
        ResourceManager.ore_slopper.renderPart("BladesLeft");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(-0.375F, 2.75F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-blades));
        RenderContext.translate(0.375F, -2.75F, 0F);
        ResourceManager.ore_slopper.renderPart("BladesRight");
        RenderContext.popPose();

        float fan = BobMathUtil.interp(be.prevFan, be.fan, partialTicks);

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.875F, -1F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-fan));
        RenderContext.translate(0F, -1.875F, 1F);
        ResourceManager.ore_slopper.renderPart("Fan");
        RenderContext.popPose();
    }

    @Override
    public int getPacketLight(int packedLight, MachineOreSlopperBlockEntity be) {
        if(be.getLevel() != null && be.getBlockState().getBlock() instanceof DummyableBlock dummy) {
            return LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above(dummy.getDimensions()[0]));
        }
        return packedLight;
    }

    // Kein Zwischenspeichern in einem Feld: alle Maschinen dieses Typs teilen sich einen Renderer.
    @Override
    public AABB getRenderBoundingBox(MachineOreSlopperBlockEntity be) {

        int x = be.getBlockPos().getX();
        int y = be.getBlockPos().getY();
        int z = be.getBlockPos().getZ();

        return new AABB(x - 3, y, z - 3, x + 4, y + 7, z + 4);
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_ORE_SLOPPER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 0F);
                RenderContext.scale(3.75F, 3.75F, 3.75F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(-90F));
                bindTexture(ResourceManager.ORE_SLOPPER_TEX);
                ResourceManager.ore_slopper.renderAll();
            }
        };
    }
}
