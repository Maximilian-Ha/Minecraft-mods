package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.RBMKColor;
import com.hbm.blockentity.machine.rbmk.RBMKControlAutoBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKControlBlockEntity;
import com.hbm.blocks.machine.rbmk.RBMKBaseBlock;
import com.hbm.main.NuclearTechMod;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderRBMKControlRod.
 *
 * Zeichnet den Deckel des Steuerstabs an seiner Fahrhoehe. Der Deckel sitzt oben auf der Saeule;
 * je weiter der Stab eingefahren ist, desto tiefer sitzt er. Damit ist von aussen ablesbar, wie
 * weit ein Stab steht -- die Zahl steht sonst nur in der Oberflaeche.
 *
 * Das Licht liest der Renderer wie das Original ueber dem Saeulenkopf (getPacketLight). Das
 * Licht, das 1.21 mitgibt, gehoert zum Kernblock -- einem undurchsichtigen Block, in dem es
 * immer 0 ist. Damit gezeichnet, war der Deckel schwarz.
 */
public class RenderRBMKControlRod extends BlockEntityRendererNT<RBMKControlBlockEntity> {

    private static final ResourceLocation[] COLORED = new ResourceLocation[] {
            NuclearTechMod.withDefaultNamespace("textures/block/rbmk_control_red.png"),
            NuclearTechMod.withDefaultNamespace("textures/block/rbmk_control_yellow.png"),
            NuclearTechMod.withDefaultNamespace("textures/block/rbmk_control_green.png"),
            NuclearTechMod.withDefaultNamespace("textures/block/rbmk_control_blue.png"),
            NuclearTechMod.withDefaultNamespace("textures/block/rbmk_control_purple.png"),
    };
    private static final ResourceLocation STANDARD = NuclearTechMod.withDefaultNamespace("textures/block/rbmk_control.png");
    private static final ResourceLocation AUTO = NuclearTechMod.withDefaultNamespace("textures/block/rbmk_control_auto.png");

    @Override
    public BlockEntityRenderer<RBMKControlBlockEntity> create(Context context) {
        return new RenderRBMKControlRod();
    }

    @Override
    public void render(RBMKControlBlockEntity control, MultiBufferSource buffer, float partialTicks) {

        if(control.getLevel() == null) return;

        int offset = RBMKBaseBlock.columnHeight(control.getLevel(), control.getBlockPos(), control.getBlockState().getBlock());

        RenderContext.translate(0.5F, offset, 0.5F);

        // Der selbsttaetige Stab traegt keine Farbgruppe, er bekommt seine eigene Textur.
        ResourceLocation texture;
        if(control instanceof RBMKControlAutoBlockEntity) {
            texture = AUTO;
        } else {
            RBMKColor color = control.color;
            texture = color == null ? STANDARD : COLORED[color.ordinal()];
        }

        bindTexture(texture);

        float level = (float) (control.lastRodLevel + (control.rodLevel - control.lastRodLevel) * partialTicks);
        RenderContext.translate(0F, level, 0F);
        ResourceManager.rbmk_rods.renderPart("Lid");
    }

    @Override
    public int getPacketLight(int packedLight, RBMKControlBlockEntity control) {
        if(control.getLevel() == null) return packedLight;
        int offset = RBMKBaseBlock.columnHeight(control.getLevel(), control.getBlockPos(), control.getBlockState().getBlock());
        return LevelRenderer.getLightColor(control.getLevel(), control.getBlockPos().above(offset + 1));
    }

    /** Der Deckel sitzt bis zu sechzehn Bloecke ueber dem Kernblock. */
    /**
     * Die Blockentitaet sitzt im Kernblock ganz unten in der Saeule, sichtbar ist der Kopf bis zu
     * sechzehn Bloecke hoeher -- fast immer in einem anderen Chunk-Abschnitt. Minecraft sammelt
     * Blockentitaeten aber nur aus SICHTBAREN Abschnitten ein, und der Abschnitt des Kernblocks
     * steckt im undurchsichtigen Reaktor und faellt je nach Blickwinkel heraus. Dann verschwand
     * der ganze Kopf. shouldRenderOffScreen haengt den Renderer in die immer gezeichnete Liste;
     * der Sichtkasten unten schneidet weiter weg, was nicht im Bild ist.
     */
    @Override
    public boolean shouldRenderOffScreen(RBMKControlBlockEntity control) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(RBMKControlBlockEntity control) {
        int x = control.getBlockPos().getX();
        int y = control.getBlockPos().getY();
        int z = control.getBlockPos().getZ();
        return new AABB(x, y, z, x + 1, y + 17, z + 1);
    }
}
