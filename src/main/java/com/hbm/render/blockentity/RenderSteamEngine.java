package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineSteamEngineBlockEntity;
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

public class RenderSteamEngine extends BlockEntityRendererNT<MachineSteamEngineBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<MachineSteamEngineBlockEntity> create(Context context) { return new RenderSteamEngine(); }

    @Override
    public void render(MachineSteamEngineBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            default -> { }
        }

        float angle = Mth.lerp(partialTicks, be.lastRotor, be.rotor);
        RenderContext.translate(2F, 0F, 0F);
        this.renderEngine(angle);
    }

    /** Kurbeltrieb: Schwungrad, Welle, Pleuel und Kolben haengen alle am selben Winkel */
    private void renderEngine(double rot) {

        bindTexture(ResourceManager.STEAM_ENGINE_TEX);
        ResourceManager.steam_engine.renderPart("Base");

        RenderContext.pushPose();
        RenderContext.translate(2F, 1.375F, 0F);
        RenderContext.mulPose(Axis.ZN.rotationDegrees((float) rot));
        RenderContext.translate(-2F, -1.375F, 0F);
        ResourceManager.steam_engine.renderPart("Flywheel");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.375F, -0.5F);
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) (rot * 2D)));
        RenderContext.translate(0F, -1.375F, 0.5F);
        ResourceManager.steam_engine.renderPart("Shaft");
        RenderContext.popPose();

        RenderContext.pushPose();
        double sin = Math.sin(rot * Math.PI / 180D) * 0.25D - 0.25D;
        double cos = Math.cos(rot * Math.PI / 180D) * 0.25D;
        double ang = Math.acos(cos / 1.875D);
        RenderContext.translate((float) sin, (float) cos, 0F);
        RenderContext.translate(2.25F, 1.375F, 0F);
        RenderContext.mulPose(Axis.ZN.rotationDegrees((float) (ang * 180D / Math.PI - 90D)));
        RenderContext.translate(-2.25F, -1.375F, 0F);
        ResourceManager.steam_engine.renderPart("Transmission");
        RenderContext.popPose();

        RenderContext.pushPose();
        double cath = Math.sqrt(3.515625D - (cos * cos) / 2);
        //the difference that "1.875 - cath" makes is minuscule but very much noticeable
        RenderContext.translate((float) (1.875D - cath + sin), 0F, 0F);
        ResourceManager.steam_engine.renderPart("Piston");
        RenderContext.popPose();
    }

    /**
     * Ohne diesen Ueberschreiber cullt Minecraft alles oberhalb des Kernblocks weg: die
     * Standardbox eines BlockEntityRenderer ist genau ein Block hoch. Die Maschine liefert
     * ihre tatsaechliche Ausdehnung selbst.
     */
    @Override
    public AABB getRenderBoundingBox(MachineSteamEngineBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_STEAM_ENGINE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YN.rotationDegrees(90F));
                RenderContext.translate(0F, -1.5F, 0F);
                RenderContext.scale(2F, 2F, 2F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
                // im Original schaltete der Schadenswert 1 die Animation ab -- Untertypen gibt es im Port nicht
                RenderSteamEngine.this.renderEngine(System.currentTimeMillis() % 3600 * 0.1D);
            }
        };
    }

    /*
     * Vorlage: INFINITE_EXTENT_AABB.
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
    public boolean shouldRenderOffScreen(MachineSteamEngineBlockEntity be) {
        return true;
    }

}
