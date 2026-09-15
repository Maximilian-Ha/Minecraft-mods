package com.hbm.render.entity.effect;

import com.hbm.entity.effect.FireLingering;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Das stehende Feuer hat kein Modell -- sichtbar ist es ausschliesslich ueber die Flammenpartikel,
 * die es selbst im Tick erzeugt. Im Original ist ihm gar kein Renderer zugeordnet; auf 1.21
 * braucht jeder EntityType einen, also steht hier ein leerer.
 */
@OnlyIn(Dist.CLIENT)
public class RenderFireLingering extends EntityRenderer<FireLingering> {

    public RenderFireLingering(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(FireLingering entity) {
        return null;
    }

    @Override
    public boolean shouldRender(FireLingering entity, Frustum frustum, double x, double y, double z) {
        return false;
    }
}
