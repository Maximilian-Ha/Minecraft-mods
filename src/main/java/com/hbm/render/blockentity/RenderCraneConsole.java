package com.hbm.render.blockentity;

import com.hbm.blockentity.machine.rbmk.CraneConsoleBlockEntity;
import com.hbm.blocks.DummyableBlock;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.render.tileentity.RenderCraneConsole.
 *
 * Zeichnet zwei Dinge: das Pult mit Steuerhebel, zwei Zeigern und zwei Lampen, und -- sobald ein
 * Reaktor zugewiesen ist -- den Laufkran ueber dem Reaktorsaal.
 *
 * ABWEICHUNG: das Original blendet fuer die Lampen die Textur ganz aus. Hier liegt stattdessen
 * die weisse Ersatztextur darunter, das ergibt dieselbe reine Farbflaeche.
 */
public class RenderCraneConsole extends BlockEntityRendererNT<CraneConsoleBlockEntity> {

    @Override
    public BlockEntityRenderer<CraneConsoleBlockEntity> create(Context context) {
        return new RenderCraneConsole();
    }

    @Override
    public void render(CraneConsoleBlockEntity console, MultiBufferSource buffer, float partialTicks) {

        float facing = getFacingAngle(console);

        RenderContext.pushPose();
        RenderContext.translate(0.5F, 0F, 0.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(facing));
        RenderContext.translate(0.5F, 0F, 0F);

        bindTexture(ResourceManager.RBMK_CRANE_CONSOLE_TEX);
        ResourceManager.rbmk_crane_console.renderPart("Console_Coonsole");

        RenderContext.pushPose();
        RenderContext.translate(0.75F, 1F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees((float) lerp(console.lastTiltFront, console.tiltFront, partialTicks)));
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) lerp(console.lastTiltLeft, console.tiltLeft, partialTicks)));
        RenderContext.translate(-0.75F, -1.015F, 0F);
        ResourceManager.rbmk_crane_console.renderPart("JoyStick");
        RenderContext.popPose();

        renderMeter(console, "Meter1", 0.75F, console.loadedHeat);
        renderMeter(console, "Meter2", 0.25F, console.loadedEnrichment);

        renderLamps(console);

        RenderContext.popPose();

        if(console.setUpCrane) renderCrane(console, facing, partialTicks);
    }

    /** Ein Zeiger, der bei null nach links und bei eins nach rechts ausschlaegt und dabei zittert. */
    private void renderMeter(CraneConsoleBlockEntity console, String part, float offset, double value) {

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.25F, offset);
        RenderContext.mulPose(Axis.XP.rotationDegrees((float) (Math.sin(System.currentTimeMillis() * 0.01 % 360) * 180 / Math.PI * 0.05 + 135 - 270 * value)));
        RenderContext.translate(0F, -1.25F, -offset);
        ResourceManager.rbmk_crane_console.renderPart(part);
        RenderContext.popPose();
    }

    /** Lampe eins meldet den Ladezustand, Lampe zwei, ob unter dem Haken eine gueltige Saeule steht. */
    private void renderLamps(CraneConsoleBlockEntity console) {

        int light = RenderContext.light();
        RenderContext.setLight(LightTexture.FULL_BRIGHT);
        bindTexture(ResourceManager.WHITE_TEX);

        if(console.isCraneLoading()) RenderContext.setColor(0.8F, 0.8F, 0F, 1F);
        else if(console.hasItemLoaded()) RenderContext.setColor(0F, 1F, 0F, 1F);
        else RenderContext.setColor(0F, 0.1F, 0F, 1F);
        ResourceManager.rbmk_crane_console.renderPart("Lamp1");

        if(console.isAboveValidTarget()) RenderContext.setColor(0F, 1F, 0F, 1F);
        else RenderContext.setColor(1F, 0F, 0F, 1F);
        ResourceManager.rbmk_crane_console.renderPart("Lamp2");

        RenderContext.setColor(1F, 1F, 1F, 1F);
        RenderContext.setLight(light);
    }

    /**
     * Der Kran steht nicht ueber dem Pult, sondern ueber der Reaktormitte. Der Laufbalken laeuft
     * in der vom Schraubenzieher gewaehlten Richtung ueber den ganzen Saal, der Wagen faehrt
     * darauf, und das Hubwerk haengt so weit herunter, wie der Ladevorgang fortgeschritten ist.
     */
    private void renderCrane(CraneConsoleBlockEntity console, float facing, float partialTicks) {

        RenderContext.pushPose();
        RenderContext.translate(0.5F, -1F, 0.5F);

        bindTexture(ResourceManager.RBMK_CRANE_TEX);

        RenderContext.translate(
                console.center.getX() - console.getBlockPos().getX(),
                console.center.getY() - console.getBlockPos().getY() + 1,
                console.center.getZ() - console.getBlockPos().getZ());
        RenderContext.mulPose(Axis.YP.rotationDegrees(facing));

        float posX = (float) lerp(console.lastPosFront, console.posFront, partialTicks);
        float posZ = (float) lerp(console.lastPosLeft, console.posLeft, partialTicks);
        RenderContext.translate(-posX, 0F, posZ);

        int rot = console.craneRotationOffset;
        RenderContext.mulPose(Axis.YP.rotationDegrees(rot));

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.YP.rotationDegrees(-rot));

        int girderSpan;
        switch(rot) {
            case 90 -> {
                girderSpan = console.spanL + console.spanR + 1;
                RenderContext.translate(0F, 0F, -posZ - console.spanR);
            }
            case 180 -> {
                girderSpan = console.spanF + console.spanB + 1;
                RenderContext.translate(posX - console.spanF, 0F, 0F);
            }
            case 270 -> {
                girderSpan = console.spanL + console.spanR + 1;
                RenderContext.translate(0F, 0F, -posZ + console.spanL);
            }
            default -> {
                girderSpan = console.spanF + console.spanB + 1;
                RenderContext.translate(posX + console.spanB, 0F, 0F);
            }
        }

        RenderContext.mulPose(Axis.YP.rotationDegrees(rot));

        for(int i = 0; i < girderSpan; i++) {
            ResourceManager.rbmk_crane.renderPart("Girder");
            RenderContext.translate(-1F, 0F, 0F);
        }
        RenderContext.popPose();

        ResourceManager.rbmk_crane.renderPart("Main");

        RenderContext.pushPose();
        for(int i = 0; i < console.height - 6; i++) {
            ResourceManager.rbmk_crane.renderPart("Tube");
            RenderContext.translate(0F, 1F, 0F);
        }
        RenderContext.translate(0F, -1F, 0F);
        ResourceManager.rbmk_crane.renderPart("Carriage");
        RenderContext.popPose();

        RenderContext.translate(0F, (float) (-3.25 * (1 - lerp(console.lastProgress, console.progress, partialTicks))), 0F);
        ResourceManager.rbmk_crane.renderPart("Lift");

        RenderContext.popPose();
    }

    private static float getFacingAngle(CraneConsoleBlockEntity console) {

        Direction dir = DummyableBlock.getPointingDirection(console.getBlockState());

        return switch(dir) {
            case NORTH -> 90F;
            case SOUTH -> 270F;
            case WEST -> 180F;
            default -> 0F;
        };
    }

    private static double lerp(double last, double now, float partialTicks) {
        return last + (now - last) * partialTicks;
    }

    @Override
    public AABB getRenderBoundingBox(CraneConsoleBlockEntity console) {
        return console.getRenderBoundingBox();
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
    public boolean shouldRenderOffScreen(CraneConsoleBlockEntity be) {
        return true;
    }

}
