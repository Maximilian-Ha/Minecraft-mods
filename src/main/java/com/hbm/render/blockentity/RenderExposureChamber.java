package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineExposureChamberBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.BeamType;
import com.hbm.render.util.BeamPronter.WaveType;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.hbm.util.Vec3NT;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderExposureChamber.
 *
 * Runde 162 nachgereicht: die Bestrahlungskammer steht seit Runde 134, hatte aber keinen
 * Darsteller und war daher in der Welt wie im Inventar unsichtbar.
 *
 * Laeuft die Kammer, dreht sich der Magnetring, der Kern schwebt auf und ab, und es zucken
 * Blitze: drei waagerechte in den Ring hinein, einer senkrecht durch die Mitte und zwei
 * gewundene um die Achse. Welche der drei waagerechten erscheinen, entscheidet ein Zufall,
 * der an der Weltzeit haengt -- so blinken sie bei allen Spielern gleich.
 */
public class RenderExposureChamber extends BlockEntityRendererNT<MachineExposureChamberBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineExposureChamberBlockEntity> create(Context context) { return new RenderExposureChamber(); }

    @Override
    public void render(MachineExposureChamberBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            default -> { }
        }

        RenderSystem.disableCull();

        float rotation = Mth.lerp(partialTicks, be.prevRotation, be.rotation);

        bindTexture(ResourceManager.EXPOSURE_CHAMBER_TEX);
        ResourceManager.exposure_chamber.renderPart("Chamber");

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees(rotation));
        ResourceManager.exposure_chamber.renderPart("Magnets");
        RenderContext.popPose();

        if(be.isOn && be.getLevel() != null) {

            long zeit = be.getLevel().getGameTime();

            FullBright.enable();
            RenderContext.pushPose();
            RenderContext.mulPose(Axis.YP.rotationDegrees(rotation / 2F));
            RenderContext.translate(0F, (float) (Math.sin((zeit % (Math.PI * 16D) + partialTicks) * 0.125D) * 0.0625D), 0F);
            ResourceManager.exposure_chamber.renderPart("Core");
            RenderContext.popPose();
            FullBright.disable();

            RenderSystem.enableCull();

            int dauer = 8;
            RandomSource rand = RandomSource.create(zeit / dauer);
            int chance = 2;
            int farbe = zeit % dauer >= dauer / 2 ? 0x80d0ff : 0xffffff;
            rand.nextInt(chance); // der erste Wurf verhaelt sich merkwuerdig -- so steht es im Original
            int takt = (int) (System.currentTimeMillis() % 1000L) / 50;

            if(rand.nextInt(chance) == 0) this.blitzWaagerecht(0F, 3.675F, farbe, takt);
            if(rand.nextInt(chance) == 0) this.blitzWaagerecht(1.1875F, 2.5F, farbe, takt);
            if(rand.nextInt(chance) == 0) this.blitzWaagerecht(-1.1875F, 2.5F, farbe, takt);

            RenderContext.pushPose();
            RenderContext.translate(0F, 1.75F, 0F);
            BeamPronter.prontBeam(new Vec3NT(0, 1.5, 0), WaveType.RANDOM, BeamType.LINE, 0x80d0ff, 0xffffff, takt, 10, 0.125F, 1, 0F);
            BeamPronter.prontBeam(new Vec3NT(0, 1.5, 0), WaveType.RANDOM, BeamType.LINE, 0x8080ff, 0xffffff,
                    (int) ((System.currentTimeMillis() + 5L % 1000L) / 50L), 10, 0.125F, 1, 0F);
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 2.5F, 0F);
            int wirbel = (int) (System.currentTimeMillis() % 360L);
            BeamPronter.prontBeam(new Vec3NT(0, 0, -1), WaveType.SPIRAL, BeamType.LINE, 0xffff80, 0xffffff, wirbel, 15, 0.125F, 1, 0F);
            BeamPronter.prontBeam(new Vec3NT(0, 0, -1), WaveType.SPIRAL, BeamType.LINE, 0xff8080, 0xffffff, wirbel + 180, 15, 0.125F, 1, 0F);
            RenderContext.popPose();

        } else {
            RenderSystem.enableCull();
        }
    }

    private void blitzWaagerecht(float x, float y, int farbe, int takt) {
        RenderContext.pushPose();
        RenderContext.translate(x, y, -7.5F);
        BeamPronter.prontBeam(new Vec3NT(0, 0, 5), WaveType.RANDOM, BeamType.LINE, farbe, 0xffffff, takt, 15, 0.125F, 1, 0F);
        RenderContext.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(MachineExposureChamberBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_EXPOSURE_CHAMBER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1.5F, 0F);
                RenderContext.scale(3F, 3F, 3F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(1.5F, 0F, 0F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderSystem.disableCull();
                bindTexture(ResourceManager.EXPOSURE_CHAMBER_TEX);
                ResourceManager.exposure_chamber.renderAll();
                RenderSystem.enableCull();
            }
        };
    }
}
