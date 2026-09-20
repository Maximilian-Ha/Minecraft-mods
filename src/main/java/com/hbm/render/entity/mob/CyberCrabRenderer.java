package com.hbm.render.entity.mob;

import com.hbm.entity.mob.CyberCrab;
import com.hbm.main.NuclearTechMod;
import com.hbm.render.model.ModelCrab;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderCyberCrab.
 *
 * KEIN SCHATTEN: das Original setzt shadowOpaque auf null, bei Schattengroesse eins. In
 * 1.21 heisst das Feld shadowStrength; die Groesse steht im Konstruktor.
 */
@OnlyIn(Dist.CLIENT)
public class CyberCrabRenderer extends MobRenderer<CyberCrab, ModelCrab> {

    private static final ResourceLocation TEXTUR = NuclearTechMod.withDefaultNamespace("textures/entity/crab.png");

    public CyberCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelCrab(context.bakeLayer(ModelCrab.LAYER)), 1.0F);
        this.shadowStrength = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(CyberCrab krabbe) {
        return TEXTUR;
    }
}
