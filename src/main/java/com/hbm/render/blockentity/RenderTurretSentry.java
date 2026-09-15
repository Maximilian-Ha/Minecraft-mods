package com.hbm.render.blockentity;

import com.hbm.blockentity.turret.TurretSentryBlockEntity;
import com.hbm.blockentity.turret.TurretSentryDamagedBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderTurretSentry.
 *
 * Der Wachturm besteht aus fuenf Teilen: der Sockel steht fest, der Drehkranz folgt der Gierachse,
 * und Koerper, Trommel und die beiden Laeufe hebt und senkt die Nickachse. Die Laeufe fahren beim
 * Schuss einen halben Block zurueck.
 *
 * Beim zerschossenen Turm steht der rechte Lauf fest um 25 Grad verkantet -- er ist verbogen und
 * bewegt sich nicht mehr.
 */
public class RenderTurretSentry extends BlockEntityRendererNT<TurretSentryBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<TurretSentryBlockEntity> create(Context context) { return new RenderTurretSentry(); }

    @Override
    public void render(TurretSentryBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        Vec3 offset = be.getHorizontalOffset();
        RenderContext.translate((float) offset.x, 0F, (float) offset.z);

        boolean damaged = be instanceof TurretSentryDamagedBlockEntity;
        bindTexture(damaged ? ResourceManager.TURRET_SENTRY_DAMAGED_TEX : ResourceManager.TURRET_SENTRY_TEX);

        ResourceManager.turret_sentry.renderPart("Base");

        float yaw = (float) -Math.toDegrees(Mth.lerp(partialTicks, be.lastRotationYaw, be.rotationYaw));
        float pitch = (float) Math.toDegrees(Mth.lerp(partialTicks, be.lastRotationPitch, be.rotationPitch));

        RenderContext.mulPose(Axis.YP.rotationDegrees(yaw));
        ResourceManager.turret_sentry.renderPart("Pivot");

        RenderContext.translate(0F, 1.25F, 0F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(pitch));
        RenderContext.translate(0F, -1.25F, 0F);

        ResourceManager.turret_sentry.renderPart("Body");
        ResourceManager.turret_sentry.renderPart("Drum");

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, (float) Mth.lerp(partialTicks, be.lastBarrelLeftPos, be.barrelLeftPos) * -0.5F);
        ResourceManager.turret_sentry.renderPart("BarrelL");
        RenderContext.popPose();

        RenderContext.pushPose();
        if(damaged) {
            RenderContext.translate(0F, 1.5F, 0.5F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
            RenderContext.translate(0F, -1.5F, -0.5F);
        } else {
            RenderContext.translate(0F, 0F, (float) Mth.lerp(partialTicks, be.lastBarrelRightPos, be.barrelRightPos) * -0.5F);
        }
        ResourceManager.turret_sentry.renderPart("BarrelR");
        RenderContext.popPose();
    }

    @Override
    public Item[] getItemsForRenderer() {
        return new Item[] { NtmBlocks.TURRET_SENTRY.asItem(), NtmBlocks.TURRET_SENTRY_DAMAGED.asItem() };
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {

            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -0.25F, 0F);
                RenderContext.scale(0.4375F, 0.4375F, 0.4375F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                bindTexture(stack.getItem() == NtmBlocks.TURRET_SENTRY_DAMAGED.asItem()
                        ? ResourceManager.TURRET_SENTRY_DAMAGED_TEX : ResourceManager.TURRET_SENTRY_TEX);
                ResourceManager.turret_sentry.renderAll();
            }
        };
    }
}
