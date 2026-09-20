package com.hbm.render.entity.mob;

import com.hbm.main.NuclearTechMod;

import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Der verseuchte Creeper, Runde 238. Wie der nukleare: dasselbe Creeper-Modell, nur eine
 * andere Haut. Das Original zeichnet ihn mit RenderCreeperUniversal, einem Darsteller
 * fuer alle seine Creeper-Arten; der Port hat fuer jede Art eine eigene Klasse, weil in
 * 1.21 ohnehin jede Entitaetsart ihre eigene Anmeldung braucht.
 */
@OnlyIn(Dist.CLIENT)
public class CreeperTaintedRenderer extends CreeperRenderer {

    private static final ResourceLocation TEXTUR = NuclearTechMod.withDefaultNamespace("textures/entity/creeper_tainted.png");

    public CreeperTaintedRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override
    public ResourceLocation getTextureLocation(Creeper creeper) {
        return TEXTUR;
    }
}
