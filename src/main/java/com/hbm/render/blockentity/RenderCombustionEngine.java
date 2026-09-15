package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineCombustionEngineBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.fluid.Fluids.CD_Canister;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderCombustionEngine.
 *
 * Der Kanister wird in der Farbe des gerade eingefuellten Treibstoffs
 * eingefaerbt, die Wartungsklappe schwingt auf, solange jemand die
 * Oberflaeche geoeffnet hat.
 */
public class RenderCombustionEngine extends BlockEntityRendererNT<MachineCombustionEngineBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineCombustionEngineBlockEntity> create(Context context) {
        return new RenderCombustionEngine();
    }

    @Override
    public void render(MachineCombustionEngineBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            default -> { }
        }

        RenderContext.translate(-0.5F, 0F, 3F);

        bindTexture(ResourceManager.COMBUSTION_ENGINE_TEX);
        ResourceManager.combustion_engine.renderPart("Engine");

        CD_Canister canister = be.tank.getTankType().getContainer(CD_Canister.class);

        if(canister != null) {
            int color = canister.color;
            float r = ((color & 0xff0000) >> 16) / 256F;
            float g = ((color & 0x00ff00) >> 8) / 256F;
            float b = (color & 0x0000ff) / 256F;
            RenderContext.setColor(r, g, b, 1F);
        }
        ResourceManager.combustion_engine.renderPart("Canister");
        RenderContext.setColor(1F, 1F, 1F, 1F);

        float door = Mth.lerp(partialTicks, be.prevDoorAngle, be.doorAngle);
        RenderContext.translate(1F, 0F, -2.6875F);
        RenderContext.mulPose(Axis.YN.rotationDegrees(door));
        RenderContext.translate(-1F, 0F, 2.6875F);
        ResourceManager.combustion_engine.renderPart("Hatch");
    }

    private AABB bb = null;

    @Override
    public AABB getRenderBoundingBox(MachineCombustionEngineBlockEntity be) {

        if(bb == null) {
            int x = be.getBlockPos().getX();
            int y = be.getBlockPos().getY();
            int z = be.getBlockPos().getZ();

            bb = new AABB(x - 3, y, z - 3, x + 4, y + 2, z + 4);
        }

        return bb;
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_COMBUSTION_ENGINE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1F, 0F);
                RenderContext.scale(2.75F, 2.75F, 2.75F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                RenderContext.translate(0F, 0F, 2.75F);
                bindTexture(ResourceManager.COMBUSTION_ENGINE_TEX);
                ResourceManager.combustion_engine.renderAll();
            }
        };
    }
}
