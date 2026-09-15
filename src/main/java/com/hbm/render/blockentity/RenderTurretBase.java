package com.hbm.render.blockentity;

import com.hbm.blockentity.turret.TurretBaseBlockEntity;
import com.hbm.lib.Library;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderTurretBase.
 *
 * Die Anschlussstutzen der grossen Tuerme. An jeder der acht Stellen, an denen der Turm Strom
 * zieht, wird ein Stutzen gezeichnet -- aber nur, wenn dort auch wirklich eine Leitung liegt.
 *
 * ABWEICHUNG: das Original zeichnet daneben auch Fluessigkeitsstutzen; die brauchen erst die
 * Tuerme, die Treibstoff verbrennen.
 */
public abstract class RenderTurretBase<T extends TurretBaseBlockEntity> extends BlockEntityRendererNT<T> {

    protected void renderConnectors(T turret) {

        if(turret.getLevel() == null) return;

        bindTexture(ResourceManager.TURRET_CONNECTOR_TEX);

        Vec3 offset = turret.getHorizontalOffset();
        BlockPos base = turret.getBlockPos().offset((int) offset.x, 0, (int) offset.z);
        Level level = turret.getLevel();

        checkPlug(level, base.offset(-2, 0, 0), 0, 0, 0, Direction.WEST);
        checkPlug(level, base.offset(-2, 0, -1), 0, -1, 0, Direction.WEST);

        checkPlug(level, base.offset(-1, 0, +1), 0, -1, 90, Direction.SOUTH);
        checkPlug(level, base.offset(0, 0, +1), 0, 0, 90, Direction.SOUTH);

        checkPlug(level, base.offset(+1, 0, 0), 0, -1, 180, Direction.EAST);
        checkPlug(level, base.offset(+1, 0, -1), 0, 0, 180, Direction.EAST);

        checkPlug(level, base.offset(0, 0, -2), 0, -1, 270, Direction.NORTH);
        checkPlug(level, base.offset(-1, 0, -2), 0, 0, 270, Direction.NORTH);
    }

    private void checkPlug(Level level, BlockPos pos, int ox, int oz, int rot, Direction dir) {

        if(!Library.canConnect(level, pos, dir)) return;

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees(rot));
        RenderContext.translate(ox, 0F, oz);
        ResourceManager.turret_chekhov.renderPart("Connectors");
        RenderContext.popPose();
    }
}
