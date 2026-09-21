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
 * dafuer von Hand die Blende ein und laesst die Durchsicht allein aus dem Alphakanal der
 * Haut kommen -- gemessen hat ghost.png 88 Bildpunkte mit Alpha 112 neben 1372 undurchsichtigen.
 * Auf 1.21 leistet der durchscheinende Zeichentyp dasselbe. Er wird hier am Zeichner gesetzt
 * und nicht am Modell: Model.renderType ist endgueltig und laesst sich nicht ueberschreiben
 * (gemessen in CI-Lauf 480).
 */
@OnlyIn(Dist.CLIENT)
public class GhostRenderer extends HumanoidMobRenderer<Ghost, HumanoidModel<Ghost>> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/entity/ghost.png");

    public GhostRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Ghost ghost) {
        return TEXTURE;
    }

    @Override
    protected RenderType getRenderType(Ghost ghost, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(TEXTURE);
    }
}
