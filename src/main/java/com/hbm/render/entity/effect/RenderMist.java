package com.hbm.render.entity.effect;

import com.hbm.entity.effect.Mist;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Die Nebelwolke hat kein Modell -- sichtbar ist sie ausschliesslich ueber die Partikel, die
 * sie selbst im Tick erzeugt. Im Original ist ihr gar kein Renderer zugeordnet; auf 1.21
 * braucht jeder EntityType einen, also steht hier ein leerer.
 */
@OnlyIn(Dist.CLIENT)
public class RenderMist extends EntityRenderer<Mist> {

    public RenderMist(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Mist entity) {
        return null;
    }

    @Override
    public boolean shouldRender(Mist entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        return false;
    }
}
