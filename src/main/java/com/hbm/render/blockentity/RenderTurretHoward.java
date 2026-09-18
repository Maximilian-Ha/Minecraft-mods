package com.hbm.render.blockentity;

import com.hbm.blockentity.turret.TurretHowardBlockEntity;
import com.hbm.blockentity.turret.TurretHowardDamagedBlockEntity;
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
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderTurretHoward und RenderTurretHowardDamaged.
 *
 * Die beiden Laeufe drehen sich gegenlaeufig, solange ein Ziel steht -- auch wenn gerade nicht
 * gefeuert wird. Das Wrack sieht gleich aus, nur verrostet.
 */
public class RenderTurretHoward extends RenderTurretBase<TurretHowardBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<TurretHowardBlockEntity> create(Context context) { return new RenderTurretHoward(); }

    private static boolean isDamaged(TurretHowardBlockEntity be) { return be instanceof TurretHowardDamagedBlockEntity; }

    @Override
    public void render(TurretHowardBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        boolean damaged = isDamaged(be);

        Vec3 offset = be.getHorizontalOffset();
        RenderContext.translate((float) offset.x, 0F, (float) offset.z);

        this.renderConnectors(be);

        bindTexture(damaged ? ResourceManager.TURRET_BASE_RUSTED_TEX : ResourceManager.TURRET_BASE_TEX);
        ResourceManager.turret_chekhov.renderPart("Base");

        float yaw = (float) -Math.toDegrees(Mth.lerp(partialTicks, be.lastRotationYaw, be.rotationYaw)) - 90F;
        float pitch = (float) Math.toDegrees(Mth.lerp(partialTicks, be.lastRotationPitch, be.rotationPitch));

        RenderContext.mulPose(Axis.YP.rotationDegrees(yaw));
        bindTexture(damaged ? ResourceManager.TURRET_CARRIAGE_CIWS_RUSTED_TEX : ResourceManager.TURRET_CARRIAGE_CIWS_TEX);
        ResourceManager.turret_howard.renderPart("Carriage");

        RenderContext.translate(0F, 2.25F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(pitch));
        RenderContext.translate(0F, -2.25F, 0F);

        bindTexture(damaged ? ResourceManager.TURRET_HOWARD_RUSTED_TEX : ResourceManager.TURRET_HOWARD_TEX);
        ResourceManager.turret_howard.renderPart("Body");

        float rot = Mth.lerp(partialTicks, be.lastSpin, be.spin);

        bindTexture(damaged ? ResourceManager.TURRET_HOWARD_BARRELS_RUSTED_TEX : ResourceManager.TURRET_HOWARD_BARRELS_TEX);

        RenderContext.pushPose();
        RenderContext.translate(0F, 2.5F, 0F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(rot));
        RenderContext.translate(0F, -2.5F, 0F);
        ResourceManager.turret_howard.renderPart("BarrelsTop");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 2F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(rot));
        RenderContext.translate(0F, -2F, 0F);
        ResourceManager.turret_howard.renderPart("BarrelsBottom");
        RenderContext.popPose();
    }

    @Override
    public Item[] getItemsForRenderer() {
        return new Item[] { NtmBlocks.TURRET_HOWARD.asItem(), NtmBlocks.TURRET_HOWARD_DAMAGED.asItem() };
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {

            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -4.5F, 0F);
                RenderContext.scale(4F, 4F, 4F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {

                boolean damaged = stack.getItem() == NtmBlocks.TURRET_HOWARD_DAMAGED.asItem();

                bindTexture(damaged ? ResourceManager.TURRET_BASE_RUSTED_TEX : ResourceManager.TURRET_BASE_TEX);
                ResourceManager.turret_chekhov.renderPart("Base");
                bindTexture(damaged ? ResourceManager.TURRET_CARRIAGE_CIWS_RUSTED_TEX : ResourceManager.TURRET_CARRIAGE_CIWS_TEX);
                ResourceManager.turret_howard.renderPart("Carriage");
                bindTexture(damaged ? ResourceManager.TURRET_HOWARD_RUSTED_TEX : ResourceManager.TURRET_HOWARD_TEX);
                ResourceManager.turret_howard.renderPart("Body");
                bindTexture(damaged ? ResourceManager.TURRET_HOWARD_BARRELS_RUSTED_TEX : ResourceManager.TURRET_HOWARD_BARRELS_TEX);
                ResourceManager.turret_howard.renderPart("BarrelsTop");
                ResourceManager.turret_howard.renderPart("BarrelsBottom");
            }
        };
    }
}
