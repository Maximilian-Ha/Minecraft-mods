package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.MachineMiningLaserBlockEntity;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.BeamType;
import com.hbm.render.util.BeamPronter.WaveType;
import com.hbm.render.util.RenderContext;
import com.hbm.util.Vec3NT;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderLaserMiner.
 *
 * Runde 162 nachgereicht: der Bergbaulaser steht seit Runde 118, hatte aber keinen Darsteller
 * und war daher in der Welt unsichtbar.
 *
 * Kein Gegenstandsdarsteller: das Original hat fuer diese Maschine keinen, sie traegt ein
 * flaches Sinnbild (machine_mining_laser) -- genau wie beim Radiothermalgenerator.
 *
 * Der Kopf zielt nicht nach der Blockausrichtung, sondern auf den Zielblock: aus dem Vektor
 * zum Ziel fallen Gier- und Nickwinkel. Der Nickwinkel kommt ueber atan2(y, Grundlaenge),
 * nicht ueber die Gesamtlaenge -- sonst zeigt der Kopf beim Blick nach unten am Strahl vorbei.
 */
public class RenderLaserMiner extends BlockEntityRendererNT<MachineMiningLaserBlockEntity> {

    @Override public BlockEntityRenderer<MachineMiningLaserBlockEntity> create(Context context) { return new RenderLaserMiner(); }

    @Override
    public void render(MachineMiningLaserBlockEntity be, MultiBufferSource buffer, float partialTicks) {

        RenderContext.translate(0.5F, -1F, 0.5F);

        BlockPos pos = be.getBlockPos();
        double tx = Mth.lerp(partialTicks, be.lastTargetX, be.targetX);
        double ty = Mth.lerp(partialTicks, be.lastTargetY, be.targetY);
        double tz = Mth.lerp(partialTicks, be.lastTargetZ, be.targetZ);
        double vx = tx - pos.getX();
        double vy = ty - pos.getY() + 3;
        double vz = tz - pos.getZ();

        // Der Strahl beginnt nicht im Mittelpunkt, sondern anderthalb Bloecke weiter aussen.
        double laenge = Math.sqrt(vx * vx + vy * vy + vz * vz);
        double nx = 0, ny = 0, nz = 0;
        if(laenge > 0) {
            double d = 1.5D / laenge;
            nx = vx * d;
            ny = vy * d;
            nz = vz * d;
        }
        double sx = vx - nx;
        double sy = vy - ny;
        double sz = vz - nz;

        double gier = Math.toDegrees(Math.atan2(sx, sz));
        double grund = Math.sqrt(sx * sx + sz * sz);
        double nick = Math.toDegrees(Math.atan2(sy, grund));

        bindTexture(ResourceManager.MINING_LASER_BASE_TEX);
        ResourceManager.mining_laser.renderPart("Base");

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees((float) gier));
        bindTexture(ResourceManager.MINING_LASER_PIVOT_TEX);
        ResourceManager.mining_laser.renderPart("Pivot");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees((float) gier));
        RenderContext.translate(0F, -1F, 0F);
        RenderContext.mulPose(Axis.XN.rotationDegrees((float) nick + 90F));
        RenderContext.translate(0F, 1F, 0F);
        bindTexture(ResourceManager.MINING_LASER_LASER_TEX);
        ResourceManager.mining_laser.renderPart("Laser");
        RenderContext.popPose();

        if(be.beam && be.getLevel() != null) {
            RenderContext.translate((float) nx, (float) ny - 1F, (float) nz);
            double strahl = Math.sqrt(sx * sx + sy * sy + sz * sz);
            int reichweite = (int) Math.ceil(strahl * 0.5D);
            int takt = (int) (be.getLevel().getGameTime() * -25L % 360L);
            Vec3NT vec = new Vec3NT(sx, sy, sz);
            BeamPronter.prontBeam(vec, WaveType.SPIRAL, BeamType.SOLID, 0xa00000, 0xa00000, takt, reichweite * 2, 0.075F, 3, 0.025F);
            BeamPronter.prontBeam(vec, WaveType.SPIRAL, BeamType.SOLID, 0xa00000, 0xa00000, takt + 120, reichweite * 2, 0.075F, 3, 0.025F);
            BeamPronter.prontBeam(vec, WaveType.SPIRAL, BeamType.SOLID, 0xa00000, 0xa00000, takt + 240, reichweite * 2, 0.075F, 3, 0.025F);
        }
    }

    /*
     * Das Original nimmt hier INFINITE_EXTENT_AABB. Die Entsprechung in 1.21 ist nicht ein
     * grosser Kasten, sondern shouldRenderOffScreen: Minecraft sammelt die Blockentitaeten aus
     * den SICHTBAREN Chunk-Abschnitten ein und legt die so gekennzeichneten zusaetzlich in die
     * Liste der immer gezeichneten. Die Entfernung bleibt ueber getViewDistance() auf 256
     * Bloecke begrenzt.
     */
    @Override
    public boolean shouldRenderOffScreen(MachineMiningLaserBlockEntity be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(MachineMiningLaserBlockEntity be) {
        return be.getRenderBoundingBox();
    }
}
