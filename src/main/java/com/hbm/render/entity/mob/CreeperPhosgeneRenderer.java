package com.hbm.render.entity.mob;

import com.hbm.main.NuclearTechMod;

import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Runde 240. Dasselbe Creeper-Modell, andere Haut -- wie beim nuklearen und beim verseuchten. */
@OnlyIn(Dist.CLIENT)
public class CreeperPhosgeneRenderer extends CreeperRenderer {

    private static final ResourceLocation TEXTUR = NuclearTechMod.withDefaultNamespace("textures/entity/creeper_phosgene.png");

    public CreeperPhosgeneRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override
    public ResourceLocation getTextureLocation(Creeper creeper) {
        return TEXTUR;
    }
}
