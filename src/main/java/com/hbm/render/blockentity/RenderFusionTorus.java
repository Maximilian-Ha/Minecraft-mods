package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.fusion.FusionTorusBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.recipes.FusionRecipe;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Clock;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFusionTorus.
 *
 * Der Torus steht still, der Magnetring dreht sich. An jedem der vier Arme sitzen Schrauben, die
 * nur dann gezeichnet werden, wenn dort etwas angeschlossen ist -- so sieht man von aussen, welche
 * Arme in Betrieb sind.
 *
 * Das Plasma sind drei Lagen auf demselben Koerper: die Grundfarbe des Rezepts, ein Glimmen
 * darueber und ein Funkeln obendrauf. Die beiden oberen Lagen entfallen ab hundert Bloecken
 * Entfernung -- so im Original.
 */
public class RenderFusionTorus extends BlockEntityRendererNT<FusionTorusBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<FusionTorusBlockEntity> create(Context context) { return new RenderFusionTorus(); }

    @Override
    public void render(FusionTorusBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        /* Umgekippt liegt der Torus schief in der Grube. */
        if(be.tilted) {
            RenderContext.translate(0F, -1F, 0F);
            RenderContext.mulPose(Axis.ZP.rotationDegrees(10F));
            RenderContext.mulPose(Axis.YP.rotationDegrees(5F));
        }

        bindTexture(ResourceManager.FUSION_TORUS_TEX);
        ResourceManager.fusion_torus.renderPart("Torus");

        RenderContext.pushPose();
        float rot = Mth.lerp(partialTicks, be.prevMagnet, be.magnet);
        RenderContext.mulPose(Axis.YP.rotationDegrees(rot));
        ResourceManager.fusion_torus.renderPart("Magnet");
        RenderContext.popPose();

        if(be.connections[0]) ResourceManager.fusion_torus.renderPart("Bolts2");
        if(be.connections[1]) ResourceManager.fusion_torus.renderPart("Bolts4");
        if(be.connections[2]) ResourceManager.fusion_torus.renderPart("Bolts3");
        if(be.connections[3]) ResourceManager.fusion_torus.renderPart("Bolts1");

        FusionRecipe recipe = (FusionRecipe) be.fusionModule.getRecipe();

        if(be.plasmaEnergy > 0 && recipe != null) this.renderPlasma(be, recipe);
    }

    private void renderPlasma(FusionTorusBlockEntity be, FusionRecipe recipe) {

        long time = Clock.get_ms();

        float alpha = 0.35F + (float) (Math.sin(time / 1000D) * 0.25F);

        float r = recipe.r;
        float g = recipe.g;
        float b = recipe.b;

        double mainOsc = BobMathUtil.sps(time / 1000D) % 1D;
        double glowOsc = Math.sin(time / 2000D) % 1D;
        double glowExtra = time / 10000D % 1D;
        double sparkleSpin = time / 500D * -1 % 1D;
        double sparkleOsc = Math.sin(time / 1000D) * 0.5D % 1D;

        RenderContext.setLightning(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);

        FullBright.enable();

        bindTexture(ResourceManager.FUSION_PLASMA_TEX);
        RenderSystem.setTextureMatrix(new Matrix4f().translate(0F, (float) mainOsc, 0F));
        RenderContext.setColor(r, g, b, alpha);
        ResourceManager.fusion_torus.renderPart("Plasma");
        RenderSystem.resetTextureMatrix();

        /* Sparmassnahme des Originals: die beiden oberen Lagen nur aus der Naehe. */
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.distanceToSqr(
                be.getBlockPos().getX() + 0.5, be.getBlockPos().getY() + 2.5, be.getBlockPos().getZ() + 0.5) < 100 * 100) {

            bindTexture(ResourceManager.FUSION_PLASMA_GLOW_TEX);
            RenderSystem.setTextureMatrix(new Matrix4f().translate(0F, (float) (glowOsc + glowExtra), 0F));
            RenderContext.setColor(r * 2, g * 2, b * 2, alpha * 2);
            ResourceManager.fusion_torus.renderPart("Plasma");
            RenderSystem.resetTextureMatrix();

            bindTexture(ResourceManager.FUSION_PLASMA_SPARKLE_TEX);
            RenderSystem.setTextureMatrix(new Matrix4f().translate((float) sparkleSpin, (float) sparkleOsc, 0F));
            RenderContext.setColor(r * 2, g * 2, b * 2, 0.75F);
            ResourceManager.fusion_torus.renderPart("Plasma");
            RenderSystem.resetTextureMatrix();
        }

        FullBright.disable();

        RenderContext.setColor(1F, 1F, 1F, 1F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderContext.setLightning(true);
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.FUSION_TORUS.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1.5F, 0F);
                RenderContext.scale(0.75F, 0.75F, 0.75F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.1F, 0.1F, 0.1F);
                bindTexture(ResourceManager.FUSION_TORUS_TEX);
                ResourceManager.fusion_torus.renderPart("Torus");
                ResourceManager.fusion_torus.renderPart("Magnet");
            }
        };
    }

    /*
     * Der Ring misst sechzehn Bloecke im Durchmesser, der Kern sitzt in seiner Mitte.
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
    public boolean shouldRenderOffScreen(FusionTorusBlockEntity be) {
        return true;
    }

}
