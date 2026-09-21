package com.hbm.render.entity.mob;

import com.hbm.entity.mob.Pigeon;
import com.hbm.main.NuclearTechMod;
import com.hbm.render.model.ModelPigeon;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderPigeon.
 *
 * DER FLUEGELSCHLAG STECKT IM ALTERSWERT: das Original ueberschreibt handleRotationFloat und
 * gibt dort NICHT die Lebenszeit zurueck, sondern den Zaehler fallTime, den die Taube selbst
 * fuehrt -- er steigt, solange sie in der Luft ist, und steht still, wenn sie sitzt. Das
 * Modell dreht die Fluegel mit genau diesem Wert. Auf 1.21 heisst die Stelle getBob.
 */
@OnlyIn(Dist.CLIENT)
public class PigeonRenderer extends MobRenderer<Pigeon, ModelPigeon> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/entity/pigeon.png");

    public PigeonRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelPigeon(context.bakeLayer(ModelPigeon.LAYER)), 0.3F);
    }

    @Override
    protected float getBob(Pigeon taube, float partialTick) {
        return Mth.lerp(partialTick, taube.prevFallTime, taube.fallTime);
    }

    @Override
    public ResourceLocation getTextureLocation(Pigeon taube) {
        return TEXTURE;
    }
}
