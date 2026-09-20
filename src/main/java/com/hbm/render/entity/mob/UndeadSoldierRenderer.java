package com.hbm.render.entity.mob;

import com.hbm.entity.mob.UndeadSoldier;
import com.hbm.entity.mob.UndeadSoldier.Variante;
import com.hbm.render.model.ModelUndeadSoldier;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.entity.mob.RenderUndeadSoldier.
 *
 * Zwei Gestalten, ein Renderer. Das Original tauscht Modell und Textur in preRenderCallback;
 * der Port tut es eine Ebene frueher, in render(), denn in 1.21 liest LivingEntityRenderer
 * this.model schon VOR dem Aufruf von scale() -- der Stelle, die preRenderCallback entspricht.
 * Wer dort tauschte, haette den Schlagtakt noch am alten Modell gesetzt.
 *
 * DIE TEXTUREN SIND DIE VON MINECRAFT. Auch im Original: der Soldat traegt Zombie- und
 * Skeletthaut, die Mod bringt fuer ihn keine eigene mit.
 *
 * DIE RUESTUNGSLAGE muss hier von Hand dazu. HumanoidMobRenderer bringt nur Kopfschmuck,
 * Elytren und die Waffe in der Hand mit; ohne HumanoidArmorLayer traege der Soldat die
 * Taurun-Ruestung zwar, man saehe sie aber nicht.
 */
@OnlyIn(Dist.CLIENT)
public class UndeadSoldierRenderer extends HumanoidMobRenderer<UndeadSoldier, ModelUndeadSoldier> {

    private static final ResourceLocation TEX_ZOMBIE = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");
    private static final ResourceLocation TEX_SKELETT = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    private final ModelUndeadSoldier fleisch;
    private final ModelUndeadSoldier knochen;

    public UndeadSoldierRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelUndeadSoldier(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        this.fleisch = this.model;
        this.knochen = new ModelUndeadSoldier(context.bakeLayer(ModelLayers.SKELETON));
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<UndeadSoldier>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new HumanoidModel<UndeadSoldier>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public void render(UndeadSoldier soldat, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int light) {
        this.model = soldat.getVariante() == Variante.SKELETT ? this.knochen : this.fleisch;
        super.render(soldat, yaw, partialTick, pose, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(UndeadSoldier soldat) {
        return soldat.getVariante() == Variante.SKELETT ? TEX_SKELETT : TEX_ZOMBIE;
    }
}
