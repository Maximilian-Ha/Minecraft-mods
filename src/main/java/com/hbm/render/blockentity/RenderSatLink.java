package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineSatLinkBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderSatLink.
 *
 * Runde 161 nachgereicht: Block, Blockentitaet und Oberflaeche stehen seit Runde 122, der
 * Darsteller fehlte. Ohne ihn war die Schuessel unsichtbar -- in der Welt wie im Inventar.
 *
 * Die Schuessel richtet sich in zwei Achsen aus: rot dreht den Sockel, lift kippt den Spiegel.
 * Beide Werte stehen in der Blockentitaet und werden hier zwischen den Bildern geglaettet.
 */
public class RenderSatLink extends BlockEntityRendererNT<MachineSatLinkBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineSatLinkBlockEntity> create(Context context) { return new RenderSatLink(); }

    @Override
    public void render(MachineSatLinkBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

        /*
         * Woertlich aus dem Original: erst die halbe Drehung, dann die Verschiebung um eine
         * halbe Blockdiagonale. 1.7.10 dir.getRotation(DOWN) entspricht getCounterClockWise().
         */
        Direction dir = be.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getCounterClockWise();
        RenderContext.translate((dir.getStepX() + rot.getStepX()) * 0.5F, 0F, (dir.getStepZ() + rot.getStepZ()) * 0.5F);

        float r = Mth.lerp(partialTicks, be.prevRot, be.rot);
        float l = Mth.lerp(partialTicks, be.prevLift, be.lift);

        bindTexture(ResourceManager.SATLINK_TEX);
        ResourceManager.satlink.renderPart("Base");

        RenderContext.mulPose(Axis.YP.rotationDegrees(r));
        ResourceManager.satlink.renderPart("Rotor");

        RenderContext.translate(0F, 7.375F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(l));
        RenderContext.translate(0F, -7.375F, 0F);
        ResourceManager.satlink.renderPart("Dish");
    }

    // Kein Zwischenspeichern in einem Feld: BlockEntityRenderers legt pro BlockEntityType
    // genau EINEN Darsteller an, den sich alle Schuesseln teilen.
    @Override
    public AABB getRenderBoundingBox(MachineSatLinkBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_SAT_LINK.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -5F, 0F);
                RenderContext.scale(3.5F, 3.5F, 3.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                bindTexture(ResourceManager.SATLINK_TEX);
                ResourceManager.satlink.renderPart("Base");
                RenderContext.mulPose(Axis.YP.rotationDegrees(15F));
                ResourceManager.satlink.renderPart("Rotor");
                RenderContext.translate(0F, 7.375F, 0F);
                RenderContext.mulPose(Axis.ZP.rotationDegrees(-45F));
                RenderContext.translate(0F, -7.375F, 0F);
                ResourceManager.satlink.renderPart("Dish");
            }
        };
    }
}
