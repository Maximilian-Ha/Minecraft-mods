package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.DecoPoleSatelliteReceiverBlockEntity;
import com.hbm.blocks.generic.DecoPoleSatelliteReceiverBlock;
import com.hbm.main.NuclearTechMod;
import com.hbm.render.model.ModelSatelliteReceiver;
import com.hbm.blocks.NtmBlocks;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderPoleSatelliteReceiver.
 *
 * Das Original verschiebt um (0,5|1,5|0,5) und dreht um 180 Grad um Z -- der uebliche Griff,
 * um ein Entitaetsmodell (Y zeigt nach unten) in die Welt zu stellen. Danach dreht es nach
 * der Setzrichtung.
 */
public class RenderPoleSatelliteReceiver extends BlockEntityRendererNT<DecoPoleSatelliteReceiverBlockEntity> implements IBEWLRProvider {

    private static final ResourceLocation TEXTUR =
            NuclearTechMod.withDefaultNamespace("textures/models/pole_satellite_receiver.png");

    private final ModelPart wurzel;

    /** Nur als Anbieter: diese Fassung zeichnet nie, sie liefert nur create(). */
    public RenderPoleSatelliteReceiver() {
        this.wurzel = null;
    }

    public RenderPoleSatelliteReceiver(Context context) {
        this.wurzel = context.bakeLayer(ModelSatelliteReceiver.LAYER);
    }

    @Override
    public BlockEntityRenderer<DecoPoleSatelliteReceiverBlockEntity> create(Context context) {
        return new RenderPoleSatelliteReceiver(context);
    }

    @Override
    public void render(DecoPoleSatelliteReceiverBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 1.5F, 0.5F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(180F));

        Direction facing = be.getBlockState().getValue(DecoPoleSatelliteReceiverBlock.FACING);
        int drehung = switch(facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        RenderContext.mulPose(Axis.YP.rotationDegrees(drehung));

        this.wurzel.render(RenderContext.poseStack(), buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTUR)),
                RenderContext.light(), RenderContext.overlay());
    }

    @Override
    public AABB getRenderBoundingBox(DecoPoleSatelliteReceiverBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.POLE_SATELLITE_RECEIVER.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {

        // Zum Zeitpunkt der Anmeldung der Client-Erweiterungen stehen die Modellschichten
        // bereit; einmal backen reicht.
        ModelPart teil = Minecraft.getInstance().getEntityModels().bakeLayer(ModelSatelliteReceiver.LAYER);

        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -0.25F, 0F);
                RenderContext.scale(1.5F, 1.5F, 1.5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                // Wie der ENTITY-Fall des Originals: halbe Groesse, umgedreht, um eins
                // heruntergesetzt. Die Unterschiede zwischen Hand, Kopf und Boden nimmt
                // ItemRenderBase dem Darsteller bereits ab.
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.mulPose(Axis.XP.rotationDegrees(180F));
                RenderContext.translate(0F, -1F, 0F);
                teil.render(RenderContext.poseStack(), buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTUR)),
                        RenderContext.light(), RenderContext.overlay());
            }
        };
    }
}
