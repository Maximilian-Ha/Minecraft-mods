package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineExcavatorBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderExcavator.
 *
 * Der Bohrer faehrt aus, und der SCHAFT WAECHST MIT: das Modell hat nur ein Schaftstueck von
 * zwei Bloecken Laenge, das so oft hintereinander gezeichnet wird, bis es bis nach unten reicht.
 * Darueber drehen zwei Brecherwalzen gegenlaeufig.
 *
 * NICHT UEBERNOMMEN: der Schuettstrahl aus der Rutsche, den das Original zeichnet, solange
 * chuteTimer laeuft. Er braucht einen eigenen Zeichenpfad mit Rollkoordinaten; der Zaehler wird
 * aber schon uebertragen, damit er spaeter vorgefunden wird.
 */
public class RenderExcavator extends BlockEntityRendererNT<MachineExcavatorBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineExcavatorBlockEntity> create(Context context) { return new RenderExcavator(); }

    @Override
    public void render(MachineExcavatorBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        }

        /* Der Kern liegt drei Bloecke hoch; das Modell rechnet ab dem Boden. */
        RenderContext.translate(0F, -((DummyableBlock) NtmBlocks.MACHINE_EXCAVATOR.get()).getHeightOffset(), 0F);

        bindTexture(ResourceManager.MINING_DRILL_TEX);
        ResourceManager.mining_drill.renderPart("Main");

        float crusher = BobMathUtil.interp(be.prevCrusherRotation, be.crusherRotation, partialTicks);

        RenderContext.pushPose();
        RenderContext.translate(0F, 2F, 2.8125F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-crusher));
        RenderContext.translate(0F, -2F, -2.8125F);
        ResourceManager.mining_drill.renderPart("Crusher1");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 2F, 2.1875F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(crusher));
        RenderContext.translate(0F, -2F, -2.1875F);
        ResourceManager.mining_drill.renderPart("Crusher2");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YN.rotationDegrees(BobMathUtil.interp(be.prevDrillRotation, be.drillRotation, partialTicks)));

        float ext = BobMathUtil.interp(be.prevDrillExtension, be.drillExtension, partialTicks);
        RenderContext.translate(0F, -ext, 0F);
        ResourceManager.mining_drill.renderPart("Drillbit");

        /* Der Schaft wird so oft wiederholt, bis er oben ankommt. */
        while(ext >= -1.5F) {
            ResourceManager.mining_drill.renderPart("Shaft");
            RenderContext.translate(0F, 2F, 0F);
            ext -= 2F;
        }

        RenderContext.popPose();
    }

    @Override
    public int getPacketLight(int packedLight, MachineExcavatorBlockEntity be) {
        if(be.getLevel() != null && be.getBlockState().getBlock() instanceof DummyableBlock dummy) {
            return LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above(dummy.getDimensions()[0]));
        }
        return packedLight;
    }

    // Kein Zwischenspeichern in einem Feld: alle Maschinen dieses Typs teilen sich einen Renderer.
    @Override
    public AABB getRenderBoundingBox(MachineExcavatorBlockEntity be) {

        int x = be.getBlockPos().getX();
        int y = be.getBlockPos().getY();
        int z = be.getBlockPos().getZ();

        /* Der Bohrer reicht bis zum Grundgestein hinunter. */
        return new AABB(x - 4, be.getLevel() != null ? be.getLevel().getMinBuildHeight() : y - 64, z - 4, x + 5, y + 4, z + 5);
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_EXCAVATOR.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -2F, 0F);
                RenderContext.scale(3F, 3F, 3F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                bindTexture(ResourceManager.MINING_DRILL_TEX);
                ResourceManager.mining_drill.renderPart("Main");
            }
        };
    }

    /*
     * Der Bohrschaft wird bis zur Bohrtiefe gezeichnet, im Grenzfall bis zum Weltboden.
     *
     * Runde 160: ein grosser getRenderBoundingBox reicht dafuer NICHT. Minecraft sammelt die
     * Blockentitaeten aus den SICHTBAREN Chunk-Abschnitten ein; faellt der Abschnitt des Kerns
     * aus dem Sichtstumpf, wird die Blockentitaet gar nicht erst angefasst, und ein noch so
     * grosser Kasten kann daran nichts aendern -- er kann nur zusaetzlich wegschneiden.
     * shouldRenderOffScreen haengt sie stattdessen in die Liste der immer gezeichneten.
     * Das ist die Entsprechung zu INFINITE_EXTENT_AABB aus 1.7.10; die Entfernung bleibt
     * ueber getViewDistance() auf 256 Bloecke begrenzt.
     */
    @Override
    public boolean shouldRenderOffScreen(MachineExcavatorBlockEntity be) {
        return true;
    }

}
