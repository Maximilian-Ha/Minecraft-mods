package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineFELBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.BeamType;
import com.hbm.render.util.BeamPronter.WaveType;
import com.hbm.render.util.RenderContext;
import com.hbm.util.Vec3NT;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.awt.Color;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderFEL.
 *
 * Zwei Strahlen uebereinander: einer als Spirale, einer als Zufallsflirren, beide in der Farbe
 * der Wellenlaenge. Sichtbares Licht hat keine eigene Farbe und schillert stattdessen.
 *
 * ABWEICHUNG: das Original zeichnet mit prontBeamwithDepth, also mit gesetzter Tiefenmaske.
 * Der Port hat diese Unterscheidung nicht mehr -- auf 1.21 entscheidet der Zeichentyp ueber
 * die Tiefe, und BeamPronter kennt nur noch den einen Weg.
 */
public class RenderFEL extends BlockEntityRendererNT<MachineFELBlockEntity> implements IBEWLRProvider {

    @Override
    public BlockEntityRenderer<MachineFELBlockEntity> create(Context context) {
        return new RenderFEL();
    }

    @Override
    public void render(MachineFELBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderSystem.enableCull();

        Direction facing = be.getBlockState().getValue(DummyableBlock.FACING);
        switch(facing) {
            case WEST -> RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            case SOUTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            case EAST -> RenderContext.mulPose(Axis.YP.rotationDegrees(270F));
            case NORTH -> RenderContext.mulPose(Axis.YP.rotationDegrees(0F));
            default -> { }
        }

        bindTexture(ResourceManager.FEL_TEX);
        ResourceManager.fel.renderAll();

        if(be.getLevel() == null) return;

        int length = be.distance - 3;
        if(!be.isBeamActive() || length <= 0) return;

        int color = be.mode.strahlfarbe == 0
                ? Color.HSBtoRGB(be.getLevel().getGameTime() / 50.0F, 0.5F, 0.1F) & 0xFFFFFF
                : be.mode.strahlfarbe;

        RenderContext.translate(0F, 1.5F, -1.5F);

        Vec3NT skeleton = new Vec3NT(0, 0, -length - 1);
        BeamPronter.prontBeam(skeleton, WaveType.SPIRAL, BeamType.SOLID, color, color, 0, 1, 0F, 2, 0.0625F);
        BeamPronter.prontBeam(skeleton, WaveType.RANDOM, BeamType.SOLID, color, color,
                (int) (be.getLevel().getGameTime() % 1000 / 2), (length / 2) + 1, 0.0625F, 2, 0.0625F);
    }

    @Override
    public AABB getRenderBoundingBox(MachineFELBlockEntity be) {
        return be.getRenderBoundingBox();
    }

    /**
     * Der Strahl reicht 24 Bloecke weit, der Kernblock steht am Anfang. Das Original nimmt
     * dafuer INFINITE_EXTENT_AABB; auf 1.21 ist das dieser Haken. Ohne ihn verschwindet der
     * ganze Strahl, sobald die Maschine selbst aus dem Bild faellt.
     */
    @Override
    public boolean shouldRenderOffScreen(MachineFELBlockEntity be) { return true; }

    @Override
    public Item getItemForRenderer() {
        return NtmBlocks.MACHINE_FEL.asItem();
    }

    @Override
    public BlockEntityWithoutLevelRenderer getRenderer() {
        return new ItemRenderBase() {
            @Override
            public void renderInventory(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(0F, -1F, 0F);
                RenderContext.scale(2F, 2F, 2F);
            }

            @Override
            public void renderCommon(ItemStack stack, MultiBufferSource buffer) {
                RenderContext.translate(1F, 0F, 0F);
                RenderContext.mulPose(Axis.YN.rotationDegrees(90F));
                bindTexture(ResourceManager.FEL_TEX);
                ResourceManager.fel.renderAll();
            }
        };
    }
}
