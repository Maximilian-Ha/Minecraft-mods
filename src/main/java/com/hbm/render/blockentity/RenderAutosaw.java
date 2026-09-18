package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineAutosawBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderAutosaw.
 *
 * Runde 161 nachgereicht: die Autosaege steht seit Runde 119, hatte aber keinen Darsteller
 * und war daher in der Welt wie im Inventar unsichtbar.
 *
 * Der Arm besteht aus drei Gliedern, die sich gegenlaeufig um denselben Winkel beugen: der
 * obere um +a, der untere um -2a, die Spitze wieder um +a. Dadurch bleibt das Saegeblatt
 * waagerecht, egal wie weit der Arm ausgefahren ist.
 */
public class RenderAutosaw extends BlockEntityRendererNT<MachineAutosawBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineAutosawBlockEntity> create(Context context) { return new RenderAutosaw(); }

    @Override
    public void render(MachineAutosawBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        float turn = Mth.lerp(partialTicks, be.prevRotationYaw, be.rotationYaw);
        float angle = 80F - Mth.lerp(partialTicks, be.prevRotationPitch, be.rotationPitch);
        float spin = Mth.lerp(partialTicks, be.lastSpin, be.spin);
        // Leichtes Zittern des Motors, solange die Saege laeuft.
        double engine = be.isOn && be.getLevel() != null
                ? Math.sin(be.getLevel().getGameTime() * 2 % (Math.PI * 2) + partialTicks)
                : 0D;

        this.renderSaw(turn, angle, spin, (float) engine);
    }

    private void renderSaw(float turn, float angle, float spin, float engine) {

        bindTexture(ResourceManager.AUTOSAW_TEX);
        ResourceManager.autosaw.renderPart("Base");

        RenderContext.mulPose(Axis.YN.rotationDegrees(turn));
        ResourceManager.autosaw.renderPart("Main");

        RenderContext.pushPose();
        RenderContext.translate(0F, engine * 0.01F, 0F);
        ResourceManager.autosaw.renderPart("Engine");
        RenderContext.popPose();

        RenderContext.translate(0F, 1.75F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(angle));
        RenderContext.translate(0F, -1.75F, 0F);
        ResourceManager.autosaw.renderPart("ArmUpper");

        RenderContext.translate(0F, 1.75F, -4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(angle * -2F));
        RenderContext.translate(0F, -1.75F, 4F);
        // Ein Hauch Versatz, damit das untere Glied nicht mit dem oberen um dieselbe Flaeche streitet.
        RenderContext.translate(-0.01F, 0F, 0F);
        ResourceManager.autosaw.renderPart("ArmLower");
        RenderContext.translate(0.01F, 0F, 0F);

        RenderContext.translate(0F, 1.75F, -8F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(angle));
        RenderContext.translate(0F, -1.75F, 8F);
        ResourceManager.autosaw.renderPart("ArmTip");

        RenderContext.translate(0F, 1.75F, -10F);
        RenderContext.mulPose(Axis.YN.rotationDegrees(spin));
        RenderContext.translate(0F, -1.75F, 10F);
        ResourceManager.autosaw.renderPart("Sawblade");
    }

    @Override
    public AABB getRenderBoundingBox(MachineAutosawBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_AUTOSAW.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -3.5F, -3F);
                RenderContext.scale(5F, 5F, 5F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.mulPose(Axis.YN.rotationDegrees(90F));
                RenderAutosaw.this.renderSaw(0F, 80F, System.currentTimeMillis() % 3600 * 0.1F, 0F);
            }
        };
    }
}
