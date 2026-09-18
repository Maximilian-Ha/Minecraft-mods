package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineAmmoPressBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderAmmoPress.
 *
 * Runde 161 nachgereicht: die Munitionspresse steht seit Runde 125, hatte aber keinen
 * Darsteller und war daher in der Welt wie im Inventar unsichtbar.
 *
 * Zwei bewegte Teile: "Press" faehrt herunter, "Shells" hebt den Huelsenteller an. Die
 * fertigen Geschosse ("Bullets") liegen nur waehrend der beiden ruecklaufenden Abschnitte auf.
 */
public class RenderAmmoPress extends BlockEntityRendererNT<MachineAmmoPressBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineAmmoPressBlockEntity> create(Context context) { return new RenderAmmoPress(); }

    @Override
    public void render(MachineAmmoPressBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            default -> { }
        }

        float press = Mth.lerp(partialTicks, be.prevPress, be.press);
        float lift = Mth.lerp(partialTicks, be.prevLift, be.lift);

        bindTexture(ResourceManager.AMMO_PRESS_TEX);
        ResourceManager.ammo_press.renderPart("Frame");

        RenderContext.pushPose();
        RenderContext.translate(0F, -press * 0.25F, 0F);
        ResourceManager.ammo_press.renderPart("Press");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, lift * 0.5F - 0.5F, 0F);
        ResourceManager.ammo_press.renderPart("Shells");
        if(be.animState == MachineAmmoPressBlockEntity.AnimationState.RETRACTING
                || be.animState == MachineAmmoPressBlockEntity.AnimationState.LOWERING) {
            ResourceManager.ammo_press.renderPart("Bullets");
        }
        RenderContext.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(MachineAmmoPressBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_AMMO_PRESS.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -2.5F, 0F);
                RenderContext.scale(5F, 5F, 5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                bindTexture(ResourceManager.AMMO_PRESS_TEX);
                ResourceManager.ammo_press.renderAll();
            }
        };
    }
}
