package com.hbm.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.animal.Chicken;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Die Quackos ist eine Ente, nur fuenfundzwanzigmal so gross -- Modell und Textur sind
 * dieselben, im Original wie hier. Der Schattenradius ist der des Originals (7,5).
 */
@OnlyIn(Dist.CLIENT)
public class QuackosRenderer extends DuckRenderer {

    public QuackosRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 7.5F;
    }

    @Override
    protected void scale(Chicken chicken, PoseStack poseStack, float partialTick) {
        super.scale(chicken, poseStack, partialTick);
        poseStack.scale(25F, 25F, 25F);
    }
}
