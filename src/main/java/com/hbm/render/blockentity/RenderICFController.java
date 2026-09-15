package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.icf.ICFControllerBlockEntity;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.BeamType;
import com.hbm.render.util.BeamPronter.WaveType;
import com.hbm.render.util.RenderContext;
import com.hbm.util.Vec3NT;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderICFController.
 *
 * Nur der Strahl, der Block selbst ist ein gewoehnlicher Wuerfel.
 *
 * ABWEICHUNG: das Original dreht den Strahl ueber den Metadatenwert der Blickrichtung und zeichnet
 * ihn dann entlang der X-Achse. Hier bekommt BeamPronter den Richtungsvektor gleich mit -- er
 * rechnet sich die Drehung ohnehin selbst aus, und der Umweg ueber vier Sonderfaelle entfaellt.
 */
public class RenderICFController extends BlockEntityRendererNT<ICFControllerBlockEntity> {

    @Override public BlockEntityRenderer<ICFControllerBlockEntity> create(Context context) { return new RenderICFController(); }

    @Override
    public void render(ICFControllerBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        if(be.laserLength <= 0) return;

        Direction dir = be.getBlockState().getValue(HorizontalDirectionalBlock.FACING);

        RenderContext.translate(0.5F, 0.5F, 0.5F);

        BeamPronter.prontBeam(
                new Vec3NT(dir.getStepX() * be.laserLength, 0, dir.getStepZ() * be.laserLength),
                WaveType.SPIRAL, BeamType.SOLID, 0x202020, 0x100000, 0, 1, 0F, 10, 0.125F);
    }
}
