package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineRadGenBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRadGen.
 *
 * Runde 162 nachgereicht: der Radiothermalgenerator steht seit Runde 135, hatte aber keinen
 * Darsteller und war daher in der Welt unsichtbar.
 *
 * Kein Gegenstandsdarsteller: das Original hat fuer diese Maschine keinen, sie traegt ein
 * flaches Sinnbild (machine_radgen).
 *
 * Die Glashaube wird zweimal gezeichnet -- erst blaeulich und durchscheinend ohne
 * Tiefenschreiben, dann noch einmal mit ihrer Textur. So steht sie vor dem Rotor, ohne ihn
 * zu verdecken.
 */
public class RenderRadGen extends BlockEntityRendererNT<MachineRadGenBlockEntity> {

    @Override public BlockEntityRenderer<MachineRadGenBlockEntity> create(Context context) { return new RenderRadGen(); }

    @Override
    public void render(MachineRadGenBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case WEST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case EAST ->  RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            default -> { }
        }

        RenderSystem.disableCull();

        bindTexture(ResourceManager.RADGEN_TEX);
        ResourceManager.radgen.renderPart("Base");

        RenderContext.pushPose();
        if(be.isOn) {
            RenderContext.translate(0F, 1.5F, 0F);
            RenderContext.mulPose(Axis.XP.rotationDegrees((System.currentTimeMillis() % 3600L) * -0.1F));
            RenderContext.translate(0F, -1.5F, 0F);
        }
        ResourceManager.radgen.renderPart("Rotor");
        RenderContext.popPose();

        // Das Laempchen traegt keine Textur, nur eine Farbe -- an ist gruen, aus ist fast schwarz.
        FullBright.enable();
        bindTexture(ResourceManager.WHITE_TEX);
        if(be.isOn) RenderContext.setColor(0F, 1F, 0F, 1F);
        else RenderContext.setColor(0F, 0.1F, 0F, 1F);
        ResourceManager.radgen.renderPart("Light");
        RenderContext.setColor(1F, 1F, 1F, 1F);
        FullBright.disable();

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderContext.setColor(0.5F, 0.75F, 1F, 0.3F);
        ResourceManager.radgen.renderPart("Glass");
        RenderContext.setColor(1F, 1F, 1F, 1F);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        bindTexture(ResourceManager.RADGEN_TEX);
        ResourceManager.radgen.renderPart("Glass");

        RenderSystem.enableCull();
    }

    /*
     * Das Original nimmt hier INFINITE_EXTENT_AABB. Die Entsprechung in 1.21 ist nicht ein
     * grosser Kasten, sondern shouldRenderOffScreen: Minecraft sammelt die Blockentitaeten aus
     * den SICHTBAREN Chunk-Abschnitten ein und legt die so gekennzeichneten zusaetzlich in die
     * Liste der immer gezeichneten. Die Entfernung bleibt ueber getViewDistance() auf 256
     * Bloecke begrenzt.
     */
    @Override
    public boolean shouldRenderOffScreen(MachineRadGenBlockEntity be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(MachineRadGenBlockEntity be) {
        return be.getRenderBoundingBox();
    }
}
