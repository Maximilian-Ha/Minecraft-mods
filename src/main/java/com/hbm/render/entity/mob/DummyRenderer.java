package com.hbm.render.entity.mob;

import com.hbm.entity.mob.Dummy;
import com.hbm.main.NuclearTechMod;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderDummy.
 *
 * Eine Menschengestalt mit eigener Haut. Die Ruestungslage muss von Hand dazu -- ohne sie
 * saehe man nicht, was man der Puppe angezogen hat, und genau darum geht es bei ihr.
 */
@OnlyIn(Dist.CLIENT)
public class DummyRenderer extends HumanoidMobRenderer<Dummy, HumanoidModel<Dummy>> {

    private static final ResourceLocation TEXTURE = NuclearTechMod.withDefaultNamespace("textures/entity/dummy.png");

    public DummyRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<Dummy>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<Dummy>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(Dummy puppe) {
        return TEXTURE;
    }
}
