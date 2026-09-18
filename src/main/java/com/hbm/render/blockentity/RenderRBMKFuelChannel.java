package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKRodBlockEntity;
import com.hbm.blocks.machine.rbmk.RBMKBaseBlock;
import com.hbm.blocks.machine.rbmk.RBMKRodBlock;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.render.NtmRenderTypes;
import com.hbm.render.util.RenderContext;
import com.hbm.util.ColorUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRBMKFuelChannel.
 *
 * Steckt ein Brennstab im Kanal, zeichnet dieser Renderer das Stabbuendel ueber die ganze
 * Saeulenhoehe -- eingefaerbt in der Farbe des Brennstoffs. Laeuft der Kanal heiss (mehr als
 * fuenf Einheiten Fluss), legt sich zusaetzlich das blaue Tscherenkow-Leuchten darueber.
 *
 * ABWEICHUNG: das Leuchten zeichnet das Original mit dem Tessellator und
 * glBlendFunc(GL_SRC_ALPHA, GL_ONE). Hier uebernimmt das NtmRenderTypes.GLOW, der genau diese
 * additive Mischung ohne Textur und ohne Ruecken-Aussortierung fuehrt.
 */
public class RenderRBMKFuelChannel extends BlockEntityRendererNT<RBMKRodBlockEntity> {

    private static final ResourceLocation RODS = NuclearTechMod.withDefaultNamespace("textures/block/rbmk_element_fuel.png");

    @Override
    public BlockEntityRenderer<RBMKRodBlockEntity> create(Context context) {
        return new RenderRBMKFuelChannel();
    }

    @Override
    public void render(RBMKRodBlockEntity rod, MultiBufferSource buffer, float partialTicks) {

        if(rod.getLevel() == null) return;

        int offset = RBMKBaseBlock.columnHeight(rod.getLevel(), rod.getBlockPos(), rod.getBlockState().getBlock());

        RenderContext.translate(0.5F, 0F, 0.5F);

        this.renderTube(rod, offset);

        if(!rod.hasRod && rod.fluxQuantity <= 5) return;

        if(rod.hasRod) {
            RenderContext.pushPose();
            bindTexture(RODS);
            RenderContext.setColor(ColorUtil.fr(rod.rodColor), ColorUtil.fg(rod.rodColor), ColorUtil.fb(rod.rodColor), 1F);

            for(int i = 0; i <= offset; i++) {
                ResourceManager.rbmk_element_rods.renderPart("Rods");
                RenderContext.translate(0F, 1F, 0F);
            }

            RenderContext.setColor(1F, 1F, 1F, 1F);
            RenderContext.popPose();
        }

        if(rod.fluxQuantity > 5) this.renderCherenkov(buffer, offset);
    }

    /**
     * Kappe und Innenrohr, ueber die ganze Saeule.
     *
     * Im Original macht das der Blockzeichner RenderRBMKRod: er zeichnet den Block selbst nur
     * an den Seiten (overrideOnlyRenderSides) und legt Deckel und Innenleben als OBJ darueber
     *
     *   ObjUtil.renderPartWithIcon(rbmk_element, "Cap",   block.getIcon(0, meta), ...);
     *   ObjUtil.renderPartWithIcon(rbmk_element, "Inner", rod.inner, ...);
     *
     * Deshalb hat das Blockmodell des Kanals hier keine Deck- und keine Bodenflaeche -- ohne
     * diese Auflage saehe man in den Block hinein. Beides gehoert zusammen.
     */
    private void renderTube(RBMKRodBlockEntity rod, int offset) {

        String basis = rod.getBlockState().getBlock() instanceof RBMKRodBlock block
                ? block.getTextureBase()
                : "rbmk_element";
        ResourceLocation cap = NuclearTechMod.withDefaultNamespace("textures/block/" + basis + "_top.png");
        ResourceLocation inner = NuclearTechMod.withDefaultNamespace("textures/block/" + basis + "_inner.png");

        RenderContext.pushPose();
        for(int i = 0; i <= offset; i++) {
            bindTexture(cap);
            ResourceManager.rbmk_element.renderPart("Cap");
            bindTexture(inner);
            ResourceManager.rbmk_element.renderPart("Inner");
            RenderContext.translate(0F, 1F, 0F);
        }
        RenderContext.popPose();
    }

    /** Waagerechte Scheiben alle Viertelbloecke, additiv gemischt -- das ergibt den Schimmer. */
    private void renderCherenkov(MultiBufferSource buffer, int offset) {

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.75F, 0F);

        VertexConsumer consumer = buffer.getBuffer(NtmRenderTypes.GLOW);
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        for(double j = 0; j <= offset; j += 0.25D) {
            float y = (float) j;
            consumer.addVertex(matrix, -0.5F, y, -0.5F).setColor(0.4F, 0.9F, 1.0F, 0.1F);
            consumer.addVertex(matrix, -0.5F, y, 0.5F).setColor(0.4F, 0.9F, 1.0F, 0.1F);
            consumer.addVertex(matrix, 0.5F, y, 0.5F).setColor(0.4F, 0.9F, 1.0F, 0.1F);
            consumer.addVertex(matrix, 0.5F, y, -0.5F).setColor(0.4F, 0.9F, 1.0F, 0.1F);
        }

        RenderContext.popPose();
    }

    /** Das Stabbuendel reicht bis zum Deckel hinauf. */
    @Override
    public AABB getRenderBoundingBox(RBMKRodBlockEntity rod) {
        int x = rod.getBlockPos().getX();
        int y = rod.getBlockPos().getY();
        int z = rod.getBlockPos().getZ();
        return new AABB(x, y, z, x + 1, y + 17, z + 1);
    }
}
