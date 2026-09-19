package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.TeslaBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.BeamType;
import com.hbm.render.util.BeamPronter.WaveType;
import com.hbm.render.util.RenderContext;
import com.hbm.util.Vec3NT;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderTesla.
 *
 * Erst die Spule selbst, dann je ein Blitz zur Kugel hin zu jedem Ziel, das der Server
 * gemeldet hat. Die Zahl der Abschnitte waechst mit der Entfernung, damit ein langer
 * Blitz nicht grober aussieht als ein kurzer.
 */
public class RenderTesla extends BlockEntityRendererNT<TeslaBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<TeslaBlockEntity> create(Context context) {
        return new RenderTesla();
    }

    @Override
    public void render(TeslaBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0.0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(180F));

        bindTexture(ResourceManager.TESLA_TEX);
        ResourceManager.tesla.renderAll();

        if(be.getLevel() == null || be.targets.isEmpty()) return;

        double qx = be.getBlockPos().getX() + 0.5D;
        double qy = be.getBlockPos().getY() + TeslaBlockEntity.HOEHE;
        double qz = be.getBlockPos().getZ() + 0.5D;

        RenderContext.translate(0F, (float) TeslaBlockEntity.HOEHE, 0F);

        int takt = (int) (be.getLevel().getGameTime() % 1000) + 1;

        for(double[] ziel : be.targets) {

            double dx = ziel[0] - qx;
            double dy = ziel[1] - qy;
            double dz = ziel[2] - qz;
            double laenge = Math.sqrt(dx * dx + dy * dy + dz * dz);

            /* Das Vorzeichen von X und Z dreht sich, weil oben schon um 180 Grad um Y
             * gedreht wurde -- sonst zeigte jeder Blitz genau in die Gegenrichtung. */
            BeamPronter.prontBeam(new Vec3NT(-dx, dy, -dz), WaveType.RANDOM, BeamType.SOLID,
                    0x404040, 0x404040, takt, (int) (laenge * 5), 0.125F, 2, 0.03125F);
        }
    }

    @Override
    public AABB getRenderBoundingBox(TeslaBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    /* Das Original nimmt fuer die Spule INFINITE_EXTENT_AABB: die Blitze reichen weit
     * ueber den eigenen Block hinaus, und sie sollen nicht verschwinden, sobald die Spule
     * selbst aus dem Blickfeld geraet. In 1.21 ist das dieser Schalter; die Box oben
     * begrenzt danach nur noch, wie weit wirklich gezeichnet wird. */
    @Override
    public boolean shouldRenderOffScreen(TeslaBlockEntity be) { return true; }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.TESLA.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3F, 0F);
                RenderContext.scale(6F, 6F, 6F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.TESLA_TEX);
                ResourceManager.tesla.renderAll();
            }
        };
    }
}
