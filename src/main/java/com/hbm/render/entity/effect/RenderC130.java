package com.hbm.render.entity.effect;

import com.hbm.entity.logic.C130;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.item.RenderC130.
 *
 * Rumpf und vier Propeller. Die Propeller drehen sich nach der Uhr, nicht nach der Spielzeit --
 * fuenfzehn Grad je Millisekunde, so wie im Original; sie laufen also auch weiter, wenn das
 * Spiel steht.
 *
 * DIE VIER DREHPUNKTE sind die Nabenmitten, Zahl fuer Zahl aus dem Original uebernommen: zehn
 * Bloecke aussen, vier Komma zwei hoch, und in der Laengsachse bei plusminus 20,5 (die
 * aeusseren) und plusminus 11,16 (die inneren). Jeder Propeller wird dorthin geschoben,
 * gedreht und zurueckgeschoben.
 *
 * ABWEICHUNG: das Original schaltet hier die Rueckseitenaussonderung EIN; im Port ist sie das
 * ohnehin, anders als beim Bomber, der sie eigens ausschaltet.
 */
@OnlyIn(Dist.CLIENT)
public class RenderC130 extends EntityRenderer<C130> {

    /** Nabenhoehe und Abstand von der Mittellinie. */
    private static final float NABE_X = 10F;
    private static final float NABE_Y = 4.2F;

    /** Die Laengspositionen der vier Naben, in der Reihenfolge Prop1 bis Prop4. */
    private static final float[] NABE_Z = { -20.5F, -11.16F, 11.16F, 20.5F };

    public RenderC130(EntityRendererProvider.Context context) { super(context); }

    @Override
    public void render(C130 flugzeug, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        RenderContext.setup(poseStack, packedLight, OverlayTexture.NO_OVERLAY);

        RenderContext.mulPose(Axis.YP.rotationDegrees(flugzeug.yRotO + (flugzeug.getYRot() - flugzeug.yRotO) * partialTicks - 90.0F));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(flugzeug.xRotO + (flugzeug.getXRot() - flugzeug.xRotO) * partialTicks));

        RenderSystem.setShaderTexture(0, ResourceManager.C130_0_TEX);
        ResourceManager.c130.renderPart("Plane");

        float drehung = (float) (System.currentTimeMillis() * 15D % 360D);

        for(int i = 0; i < NABE_Z.length; i++) {
            RenderContext.pushPose();
            RenderContext.translate(NABE_X, NABE_Y, NABE_Z[i]);
            RenderContext.mulPose(Axis.XP.rotationDegrees(drehung));
            RenderContext.translate(-NABE_X, -NABE_Y, -NABE_Z[i]);
            ResourceManager.c130.renderPart("Prop" + (i + 1));
            RenderContext.popPose();
        }

        RenderContext.end();
    }

    @Override
    public ResourceLocation getTextureLocation(C130 flugzeug) {
        return ResourceManager.C130_0_TEX;
    }
}
