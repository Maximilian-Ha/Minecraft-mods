package com.hbm.render.blockentity;

import com.hbm.blockentity.turret.TurretChekhovBlockEntity;
import com.hbm.blockentity.turret.TurretFriendlyBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderTurretChekhov und RenderTurretFriendly.
 *
 * Die beiden unterscheiden sich nur im Anstrich: der Begleitturm traegt Sockel und Drehkranz in
 * seinen eigenen Farben, alles uebrige ist dasselbe Modell.
 */
public class RenderTurretChekhov extends RenderTurretBase<TurretChekhovBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<TurretChekhovBlockEntity> create(Context context) { return new RenderTurretChekhov(); }

    private static boolean isFriendly(TurretChekhovBlockEntity be) { return be instanceof TurretFriendlyBlockEntity; }

    private static ResourceLocation baseTex(boolean friendly) { return friendly ? ResourceManager.TURRET_BASE_FRIENDLY_TEX : ResourceManager.TURRET_BASE_TEX; }
    private static ResourceLocation carriageTex(boolean friendly) { return friendly ? ResourceManager.TURRET_CARRIAGE_FRIENDLY_TEX : ResourceManager.TURRET_CARRIAGE_TEX; }

    @Override
    public void render(TurretChekhovBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        boolean friendly = isFriendly(be);

        Vec3 offset = be.getHorizontalOffset();
        RenderContext.translate((float) offset.x, 0F, (float) offset.z);

        this.renderConnectors(be);

        bindTexture(baseTex(friendly));
        ResourceManager.turret_chekhov.renderPart("Base");

        float yaw = (float) -Math.toDegrees(Mth.lerp(partialTicks, be.lastRotationYaw, be.rotationYaw)) - 90F;
        float pitch = (float) Math.toDegrees(Mth.lerp(partialTicks, be.lastRotationPitch, be.rotationPitch));

        RenderContext.mulPose(Axis.YP.rotationDegrees(yaw));
        bindTexture(carriageTex(friendly));
        ResourceManager.turret_chekhov.renderPart("Carriage");

        RenderContext.translate(0F, 1.5F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(pitch));
        RenderContext.translate(0F, -1.5F, 0F);

        bindTexture(ResourceManager.TURRET_CHEKHOV_TEX);
        ResourceManager.turret_chekhov.renderPart("Body");

        float rot = Mth.lerp(partialTicks, be.lastSpin, be.spin);

        RenderContext.translate(0F, 1.5F, 0F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(rot));
        RenderContext.translate(0F, -1.5F, 0F);

        bindTexture(ResourceManager.TURRET_CHEKHOV_BARRELS_TEX);
        ResourceManager.turret_chekhov.renderPart("Barrels");
    }

    @Override
    public Item[] getItemsForRenderer() {
        return new Item[] { NtmBlocks.TURRET_CHEKHOV.asItem(), NtmBlocks.TURRET_FRIENDLY.asItem() };
    }

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

                boolean friendly = stack.getItem() == NtmBlocks.TURRET_FRIENDLY.asItem();

                bindTexture(baseTex(friendly));
                ResourceManager.turret_chekhov.renderPart("Base");
                bindTexture(carriageTex(friendly));
                ResourceManager.turret_chekhov.renderPart("Carriage");
                bindTexture(ResourceManager.TURRET_CHEKHOV_TEX);
                ResourceManager.turret_chekhov.renderPart("Body");
                bindTexture(ResourceManager.TURRET_CHEKHOV_BARRELS_TEX);
                ResourceManager.turret_chekhov.renderPart("Barrels");
            }
        };
    }
}
