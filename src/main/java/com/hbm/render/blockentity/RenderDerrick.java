package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.oil.MachineOilWellBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class RenderDerrick extends BlockEntityRendererNT<MachineOilWellBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineOilWellBlockEntity> create(Context context) {
        return new RenderDerrick();
    }

    @Override
    public void render(MachineOilWellBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        }

        bindTexture(ResourceManager.DERRICK_TEX);
        ResourceManager.oil_derrick.renderAll();
    }

    // Kein Zwischenspeichern in einem Feld: BlockEntityRenderers legt pro BlockEntityType
    // genau EINEN Renderer an, den sich alle Maschinen dieses Typs teilen. Ein gecachter
    // Kasten in Weltkoordinaten wuerde fuer jede weitere Maschine falsch gecullt.
    @Override
    public AABB getRenderBoundingBox(MachineOilWellBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        return new AABB(
                x - 1,
                y - 0,
                z - 1,
                x + 2,
                y + 10,
                z + 2
        );
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_WELL.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -4F, 0F);
                RenderContext.scale(4F, 4F, 4F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                bindTexture(ResourceManager.DERRICK_TEX);
                ResourceManager.oil_derrick.renderAll();
            }
        };
    }

    /*
     * Der Bohrturm ragt weit ueber den Kern. Vorlage: INFINITE_EXTENT_AABB ueber TileEntityOilDrillBase.
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
    public boolean shouldRenderOffScreen(MachineOilWellBlockEntity be) {
        return true;
    }

}
