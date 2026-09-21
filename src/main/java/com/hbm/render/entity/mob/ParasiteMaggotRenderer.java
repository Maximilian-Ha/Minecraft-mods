package com.hbm.render.entity.mob;

import com.hbm.entity.mob.ParasiteMaggot;
import com.hbm.main.NuclearTechMod;

import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderMaggot.
 *
 * Das Modell des Silberfischchens mit eigener Haut -- so macht es auch das Original.
 *
 * SIE KIPPT BEIM STERBEN GANZ UM: das Original gibt aus getDeathMaxRotation hundertachtzig
 * Grad statt der ueblichen neunzig. Auf 1.21 heisst die Stelle getFlipDegrees.
 */
@OnlyIn(Dist.CLIENT)
public class ParasiteMaggotRenderer extends MobRenderer<ParasiteMaggot, SilverfishModel<ParasiteMaggot>> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/entity/parasite_maggot.png");

    public ParasiteMaggotRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverfishModel<>(context.bakeLayer(ModelLayers.SILVERFISH)), 0.3F);
    }

    @Override
    protected float getFlipDegrees(ParasiteMaggot made) {
        return 180.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(ParasiteMaggot made) {
        return TEXTURE;
    }
}
