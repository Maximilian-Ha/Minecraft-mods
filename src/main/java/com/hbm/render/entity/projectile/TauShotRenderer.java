package com.hbm.render.entity.projectile;

import java.util.Random;

import com.hbm.entity.projectile.TauShot;
import com.hbm.main.NuclearTechMod;
import com.hbm.render.model.ModelBullet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.projectile.RenderRocket.
 *
 * Der Tau-Bolzen der Krabbe. Bis Runde 245 hat ihn EmptyEntityRenderer gezeichnet, also gar
 * nicht -- die Krabbe schoss ins Leere, sichtbar nur am Staub.
 *
 * WELCHER DARSTELLER ES IST, WAR LANGE FALSCH NOTIERT. Die Aufgabenliste hielt fest, das
 * Original zeichne den Bolzen ueber ResourceManager.projectiles, Teil "BulletRifle", und
 * diese OBJ-Datei fehle dem Port. Das gilt aber fuer RenderBullet -- das alte Geschosssystem,
 * das im Original selbst als veraltet markiert ist. EntityBullet, das die Krabbe wirklich
 * wirft, meldet ClientProxy Z. 615 bei RenderRocket an, und das zeichnet ModelBullet: einen
 * einzigen Kasten. Kein OBJ noetig.
 *
 * AUCH DIE TEXTUR IST NICHT DIE ERWARTETE. RenderRocket waehlt tau.png nur, wenn
 * getIsCritical() wahr ist -- gesetzt wird das ausschliesslich im isTau-Konstruktor
 * (EntityBullet Z. 191), und die Krabbe nimmt einen anderen (Z. 82). Ihr Bolzen traegt also
 * bullet.png.
 *
 * DIE ZUFAELLIGE DREHUNG stammt aus dem Original und haengt an der Entitaetskennung, nicht
 * an der Zeit: derselbe Bolzen dreht sich nicht, aber zwei Bolzen liegen verschieden.
 */
@OnlyIn(Dist.CLIENT)
public class TauShotRenderer extends EntityRenderer<TauShot> {

    private static final ResourceLocation TEXTUR = NuclearTechMod.withDefaultNamespace("textures/models/bullet.png");

    private final ModelBullet modell;

    public TauShotRenderer(Context context) {
        super(context);
        this.modell = new ModelBullet(context.bakeLayer(ModelBullet.LAYER));
    }

    @Override
    public void render(TauShot bolzen, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int light) {

        pose.pushPose();

        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, bolzen.yRotO, bolzen.getYRot()) - 90F));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, bolzen.xRotO, bolzen.getXRot()) + 180F));
        pose.scale(1.5F, 1.5F, 1.5F);
        pose.mulPose(Axis.XP.rotationDegrees(new Random(bolzen.getId()).nextInt(360)));

        /* Die Kastenmasse stehen in Sechzehnteln; ohne diese Stauchung waere der Bolzen
         * zwei Bloecke lang. Im Original macht das der Parameter 0.0625F von renderAll. */
        pose.scale(0.0625F, 0.0625F, 0.0625F);

        VertexConsumer strom = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTUR));
        this.modell.render(pose, strom, light, OverlayTexture.NO_OVERLAY);

        pose.popPose();

        super.render(bolzen, yaw, partialTick, pose, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(TauShot bolzen) {
        return TEXTUR;
    }
}
