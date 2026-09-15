package com.hbm.render.blockentity;

import com.hbm.blockentity.turret.TurretJeremyBlockEntity;
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
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderTurretJeremy.
 *
 * Sockel und Drehkranz teilt sich der Kanonenturm mit allen grossen Tuermen; eigen ist ihm nur
 * die Kanone. Die Gierachse steht um neunzig Grad versetzt, weil das Modell nach Osten zeigt.
 */
public class RenderTurretJeremy extends RenderTurretBase<TurretJeremyBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<TurretJeremyBlockEntity> create(Context context) { return new RenderTurretJeremy(); }

    @Override
    public void render(TurretJeremyBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        Vec3 offset = be.getHorizontalOffset();
        RenderContext.translate((float) offset.x, 0F, (float) offset.z);

        this.renderConnectors(be);

        bindTexture(ResourceManager.TURRET_BASE_TEX);
        ResourceManager.turret_chekhov.renderPart("Base");

        float yaw = (float) -Math.toDegrees(Mth.lerp(partialTicks, be.lastRotationYaw, be.rotationYaw)) - 90F;
        float pitch = (float) Math.toDegrees(Mth.lerp(partialTicks, be.lastRotationPitch, be.rotationPitch));

        RenderContext.mulPose(Axis.YP.rotationDegrees(yaw));
        bindTexture(ResourceManager.TURRET_CARRIAGE_TEX);
        ResourceManager.turret_chekhov.renderPart("Carriage");

        RenderContext.translate(0F, 1.5F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(pitch));
        RenderContext.translate(0F, -1.5F, 0F);

        bindTexture(ResourceManager.TURRET_JEREMY_TEX);
        ResourceManager.turret_jeremy.renderPart("Gun");
    }

    @Override
    public Item getItemForRenderer() { return NtmBlocks.TURRET_JEREMY.asItem(); }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {

            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -0.5F, 0F);
                RenderContext.scale(0.3125F, 0.3125F, 0.3125F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                bindTexture(ResourceManager.TURRET_BASE_TEX);
                ResourceManager.turret_chekhov.renderPart("Base");
                bindTexture(ResourceManager.TURRET_CARRIAGE_TEX);
                ResourceManager.turret_chekhov.renderPart("Carriage");
                bindTexture(ResourceManager.TURRET_JEREMY_TEX);
                ResourceManager.turret_jeremy.renderPart("Gun");
            }
        };
    }
}
