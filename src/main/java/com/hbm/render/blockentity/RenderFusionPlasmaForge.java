package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.fusion.FusionPlasmaForgeBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Clock;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFusionPlasmaForge.
 *
 * Die Schmiede besteht aus dem feststehenden Rumpf und einem Ring, der zwei Roboterarme traegt:
 * einen mit zwei Schlaghaemmern, einen mit einem Plasmabrenner. Beide fahren Zielwinkel an, die
 * die Blockentitaet vorgibt; hier werden daraus Drehungen um die jeweiligen Gelenke.
 *
 * Das Plasma in der Kammer sind drei Lagen auf demselben Koerper, wie beim Torus. Liegt keine
 * Plasmaleistung an, wird die Kammer schwarz gezeichnet.
 *
 * ABWEICHUNG: die Flamme des Brenners (renderJet im Original) ist NICHT UEBERNOMMEN. Sie ist ein
 * handgeschriebener Farbverlauf ohne Textur, der im Original an das feste Koordinatensystem der
 * alten Pipeline gebunden ist; der Strahl zwischen Plasma und Werkstueck steht dagegen.
 */
public class RenderFusionPlasmaForge extends BlockEntityRendererNT<FusionPlasmaForgeBlockEntity> implements IBEWLRProvider {

    @Override public BlockEntityRenderer<FusionPlasmaForgeBlockEntity> create(Context context) { return new RenderFusionPlasmaForge(); }

    @Override
    public void render(FusionPlasmaForgeBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        RenderContext.mulPose(Axis.YP.rotationDegrees(switch(facing) {
            case NORTH -> 90F;
            case WEST -> 180F;
            case SOUTH -> 270F;
            default -> 0F;
        }));

        /* Die Schrauben am Anschlussstutzen nur, wenn hinter uns etwas an der Leitung haengt. */
        if(be.connected) {
            RenderContext.pushPose();
            RenderContext.translate(-2F, 0F, 0F);
            bindTexture(ResourceManager.FUSION_TORUS_TEX);
            ResourceManager.fusion_torus.renderPart("Bolts1");
            RenderContext.popPose();
        }

        bindTexture(ResourceManager.FUSION_PLASMA_FORGE_TEX);
        ResourceManager.fusion_plasma_forge.renderPart("Body");

        GenericRecipe recipe = be.plasmaModule.getRecipe();

        this.renderPlasma(be);
        this.renderWorkpiece(be, recipe, buffer, partialTicks);
        this.renderBeam(be, recipe, partialTicks);

        bindTexture(ResourceManager.FUSION_PLASMA_FORGE_TEX);
        this.renderArms(be, partialTicks);
    }

    /** Der drehende Ring mit den beiden Armen. */
    private void renderArms(FusionPlasmaForgeBlockEntity be, float partialTicks) {

        double[] striker = be.armStriker.getPositions(partialTicks);
        double[] jet = be.armJet.getPositions(partialTicks);
        double ring = Mth.lerp(partialTicks, (float) be.prevRing, (float) be.ring);

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees((float) ring));

        /* Der Schlagarm: Drehgelenk, Unterarm, Oberarm, Halterung, zwei Haemmer. */
        RenderContext.pushPose();
        ResourceManager.fusion_plasma_forge.renderPart("SliderStriker");

        pivot(-2.75F, 2.5F, 0F, Axis.ZP.rotationDegrees((float) -striker[0]));
        ResourceManager.fusion_plasma_forge.renderPart("ArmLowerStriker");

        pivot(-2.75F, 3.75F, 0F, Axis.ZP.rotationDegrees((float) -striker[1]));
        ResourceManager.fusion_plasma_forge.renderPart("ArmUpperStriker");

        pivot(-1.5F, 3.75F, 0F, Axis.ZP.rotationDegrees((float) -striker[2]));
        ResourceManager.fusion_plasma_forge.renderPart("StrikerMount");

        RenderContext.pushPose();
        pivot(0F, 3.375F, 0.5F, Axis.XP.rotationDegrees((float) striker[3]));
        ResourceManager.fusion_plasma_forge.renderPart("StrikerRight");
        RenderContext.translate(0F, (float) -striker[4], 0F);
        ResourceManager.fusion_plasma_forge.renderPart("PistonRight");
        RenderContext.popPose();

        RenderContext.pushPose();
        pivot(0F, 3.375F, -0.5F, Axis.XP.rotationDegrees((float) -striker[3]));
        ResourceManager.fusion_plasma_forge.renderPart("StrikerLeft");
        RenderContext.translate(0F, (float) -striker[5], 0F);
        ResourceManager.fusion_plasma_forge.renderPart("PistonLeft");
        RenderContext.popPose();

        RenderContext.popPose();

        /* Der Brennerarm, spiegelbildlich auf der anderen Seite des Rings. */
        RenderContext.pushPose();
        ResourceManager.fusion_plasma_forge.renderPart("SliderJet");

        pivot(2.75F, 2.5F, 0F, Axis.ZP.rotationDegrees((float) jet[0]));
        ResourceManager.fusion_plasma_forge.renderPart("ArmLowerJet");

        pivot(2.75F, 3.75F, 0F, Axis.ZP.rotationDegrees((float) jet[1]));
        ResourceManager.fusion_plasma_forge.renderPart("ArmUpperJet");

        pivot(1.5F, 3.75F, 0F, Axis.ZP.rotationDegrees((float) jet[2]));
        ResourceManager.fusion_plasma_forge.renderPart("Jet");
        RenderContext.popPose();

        RenderContext.popPose();
    }

    /** Dreht um einen Punkt statt um den Ursprung: hinfahren, drehen, zurueckfahren. */
    private static void pivot(float x, float y, float z, org.joml.Quaternionf rotation) {
        RenderContext.translate(x, y, z);
        RenderContext.mulPose(rotation);
        RenderContext.translate(-x, -y, -z);
    }

    private void renderPlasma(FusionPlasmaForgeBlockEntity be) {

        if(be.plasmaEnergySync <= 0) {
            /* Kalte Kammer: schwarz, damit man nicht durch das Gitter hindurchsieht. */
            RenderContext.setColor(0F, 0F, 0F, 1F);
            ResourceManager.fusion_plasma_forge.renderPart("Plasma");
            RenderContext.setColor(1F, 1F, 1F, 1F);
            return;
        }

        long time = Clock.get_ms() + be.timeOffset;
        float alpha = 0.5F + (float) (Math.sin(time / 500D) * 0.25F);

        double mainOsc = BobMathUtil.sps(time / 750D) % 1D;
        double glowOsc = Math.sin(time / 1000D) % 1D;
        double glowExtra = time / 10000D % 1D;

        RenderContext.setLightning(false);
        FullBright.enable();

        bindTexture(ResourceManager.FUSION_PLASMA_TEX);
        RenderSystem.setTextureMatrix(new Matrix4f().translate(0F, (float) mainOsc, 0F));
        RenderContext.setColor(be.plasmaRed * alpha, be.plasmaGreen * alpha, be.plasmaBlue * alpha, 1F);
        ResourceManager.fusion_plasma_forge.renderPart("Plasma");
        RenderSystem.resetTextureMatrix();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        RenderSystem.depthMask(false);

        RenderContext.setColor(be.plasmaRed * 2, be.plasmaGreen * 2, be.plasmaBlue * 2, 1F);

        bindTexture(ResourceManager.FUSION_PLASMA_GLOW_TEX);
        RenderSystem.setTextureMatrix(new Matrix4f().translate(0F, (float) (glowOsc + glowExtra), 0F));
        ResourceManager.fusion_plasma_forge.renderPart("Plasma");
        RenderSystem.resetTextureMatrix();

        /* Dieselbe Lage ein zweites Mal, nur anders verschoben -- das Original macht daraus die
         * unruhige Struktur im Plasma. */
        glowOsc = Math.sin(time / 600D + 2) % 1D;
        glowExtra = time / 5000D % 1D;

        RenderSystem.setTextureMatrix(new Matrix4f().translate(0F, (float) (glowOsc + glowExtra), 0F));
        ResourceManager.fusion_plasma_forge.renderPart("Plasma");
        RenderSystem.resetTextureMatrix();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        FullBright.disable();
        RenderContext.setLightning(true);
        RenderContext.setColor(1F, 1F, 1F, 1F);
    }

    /** Das Werkstueck schwebt ueber dem Amboss und wippt leicht. */
    private void renderWorkpiece(FusionPlasmaForgeBlockEntity be, GenericRecipe recipe, MultiBufferSource buffer, float partialTicks) {

        if(recipe == null) return;
        if(!this.isNear(be, 35)) return;

        ItemStack stack = recipe.getIcon().copy();
        if(stack.isEmpty()) return;
        stack.setCount(1);

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.translate(0F, 1.75F, 0F);

        float bob = (float) (Math.sin((Minecraft.getInstance().player.tickCount + partialTicks) * 0.1D) * 0.0625D);
        RenderContext.translate(0F, bob, 0F);
        RenderContext.scale(1.5F, 1.5F, 1.5F);

        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = renderer.getModel(stack, null, null, 0);
        renderer.render(stack, ItemDisplayContext.FIXED, false, RenderContext.poseStack(), buffer,
                RenderContext.light(), RenderContext.overlay(), model);

        RenderContext.popPose();
    }

    /** Der Strahl vom Plasma auf das Werkstueck. */
    private void renderBeam(FusionPlasmaForgeBlockEntity be, GenericRecipe recipe, float partialTicks) {

        if(recipe == null) return;
        if(!this.isNear(be, 50)) return;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, Fluids.STELLAR_FLUX.getTexture());
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        float offset = (float) (((Minecraft.getInstance().player.tickCount + partialTicks) / 15D) % 1D);
        float in = 0.4375F;
        float b = 1F;
        float t = 1.5F;
        float h = b + t;

        Matrix4f matrix = RenderContext.poseStack().last().pose();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        beamQuad(builder, matrix, -in, b, in, -in, h, in, -in, h, -in, -in, b, -in, offset, t);
        beamQuad(builder, matrix, in, h, in, in, b, in, in, b, -in, in, h, -in, offset, t);
        beamQuad(builder, matrix, in, b, in, in, h, in, -in, h, in, -in, b, in, offset, t);
        beamQuad(builder, matrix, in, h, -in, in, b, -in, -in, b, -in, -in, h, -in, offset, t);

        BufferUploader.drawWithShader(builder.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /**
     * Eine Seite des Strahls. Die Eckpunkte stehen paarweise: der erste und der vierte liegen
     * unten und sind undurchsichtig, der zweite und dritte oben und durchsichtig -- so laeuft der
     * Strahl nach oben aus.
     */
    private static void beamQuad(BufferBuilder builder, Matrix4f matrix,
                                 float x1, float y1, float z1, float x2, float y2, float z2,
                                 float x3, float y3, float z3, float x4, float y4, float z4,
                                 float offset, float t) {

        boolean firstSolid = y1 < y2;

        builder.addVertex(matrix, x1, y1, z1).setUv(offset + (firstSolid ? t : 0F), 0F).setColor(1F, 1F, 1F, firstSolid ? 1F : 0F);
        builder.addVertex(matrix, x2, y2, z2).setUv(offset + (firstSolid ? 0F : t), 0F).setColor(1F, 1F, 1F, firstSolid ? 0F : 1F);
        builder.addVertex(matrix, x3, y3, z3).setUv(offset + (y3 < y4 ? t : 0F), 1F).setColor(1F, 1F, 1F, y3 < y4 ? 1F : 0F);
        builder.addVertex(matrix, x4, y4, z4).setUv(offset + (y3 < y4 ? 0F : t), 1F).setColor(1F, 1F, 1F, y3 < y4 ? 0F : 1F);
    }

    private boolean isNear(FusionPlasmaForgeBlockEntity be, double range) {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.distanceToSqr(
                be.getBlockPos().getX() + 0.5, be.getBlockPos().getY() + 1, be.getBlockPos().getZ() + 0.5) <= range * range;
    }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.FUSION_PLASMA_FORGE.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1F, 0F);
                RenderContext.scale(2.75F, 2.75F, 2.75F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.scale(0.5F, 0.5F, 0.5F);
                RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

                bindTexture(ResourceManager.FUSION_PLASMA_FORGE_TEX);
                ResourceManager.fusion_plasma_forge.renderAllExcept("Plasma");

                RenderContext.setColor(0F, 0F, 0F, 1F);
                ResourceManager.fusion_plasma_forge.renderPart("Plasma");
                RenderContext.setColor(1F, 1F, 1F, 1F);
            }
        };
    }
}
