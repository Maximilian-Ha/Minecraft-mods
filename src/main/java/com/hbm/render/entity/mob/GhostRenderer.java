package com.hbm.render.entity.mob;

import com.hbm.entity.mob.Ghost;
import com.hbm.main.NuclearTechMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderGhost.
 *
 * Eine gewoehnliche Menschengestalt mit einer durchscheinenden Haut. Das Original schaltet
 * dafuer von Hand die Blende ein; auf 1.21 genuegt der durchscheinende Zeichentyp.
 */
@OnlyIn(Dist.CLIENT)
public class GhostRenderer extends HumanoidMobRenderer<Ghost, HumanoidModel<Ghost>> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/entity/ghost.png");

    public GhostRenderer(EntityRendererProvider.Context context) {
        super(context, new DurchscheinendesModell(context), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Ghost ghost) {
        return TEXTURE;
    }

    /** Dasselbe Menschenmodell, nur durchscheinend gezeichnet. */
    private static class DurchscheinendesModell extends HumanoidModel<Ghost> {

        DurchscheinendesModell(EntityRendererProvider.Context context) {
            super(context.bakeLayer(ModelLayers.ZOMBIE));
        }

        @Override
        public RenderType renderType(ResourceLocation texture) {
            return RenderType.entityTranslucent(texture);
        }
    }
}
