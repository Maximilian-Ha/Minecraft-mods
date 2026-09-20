package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorEnvsuit.
 *
 * Der M1TTY-Umgebungsanzug. Zum Helm gehoeren LAMPEN, die immer voll ausgeleuchtet und
 * leicht gelblich gezeichnet werden; sie uebernehmen den Drehpunkt des Helms.
 *
 * ABWEICHUNG, sichtbar: das Original zeichnet die Lampen GANZ OHNE TEXTUR -- es schaltet
 * GL_TEXTURE_2D ab und setzt eine reine Farbe. In 1.21 tastet die Zeichenart immer eine
 * Textur ab; der Port faerbt deshalb die Helmtextur ein, statt sie abzuschalten. Die Lampen
 * leuchten also gelblich wie dort, zeigen aber die Maserung des Helms.
 *
 * DAS MODELL HAT EIN TEIL ZU VIEL, und die Datei eine Textur: envsuit.obj enthaelt einen
 * SCHWANZ (Tail), und ResourceManager.envsuit_tail zeigt auf ein Bild dafuer. Gezeichnet wird
 * er nirgends -- weder hier noch im Original, das die Textur zwar anmeldet, aber nie bindet.
 * Der Port uebernimmt beides nicht: kein Zeichnen, keine Textur.
 *
 * DIE FUSSTEILE HEISSEN LeftFoot UND RightFoot, wie bei der Wismut-Garnitur und anders als
 * bei T-51, AJR, Taurun und Fau.
 *
 * ABWEICHUNG wie bei ModelArmorT51: die Drehpunkte kommen aus dem Spielermodell.
 */
public class ModelArmorEnvsuit extends ModelArmorBase {

    private final ModelRendererObj lamps;

    public ModelArmorEnvsuit(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_envsuit, "Helmet").copyRotationFrom(model.head);
        this.lamps = new ModelRendererObj(ResourceManager.armor_envsuit, "Lamps");
        this.body = new ModelRendererObj(ResourceManager.armor_envsuit, "Chest").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_envsuit, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_envsuit, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_envsuit, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_envsuit, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_envsuit, "LeftFoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_envsuit, "RightFoot").copyRotationFrom(model.rightLeg);
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot != EquipmentSlot.HEAD) return;

            bindTexture(ResourceManager.ENVSUIT_HELMET);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            this.head.render(0.0625F);
            RenderSystem.disableBlend();

            this.head.copyTo(this.lamps);

            int vorher = RenderContext.light();
            RenderContext.setLight(LightTexture.FULL_BRIGHT);
            RenderContext.setColor(1.0F, 1.0F, 0.8F, 1.0F);
            this.lamps.render(0.0625F);
            RenderContext.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderContext.setLight(vorher);
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.ENVSUIT_CHEST);
                this.body.render(0.0625F);
                bindTexture(ResourceManager.ENVSUIT_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(ResourceManager.ENVSUIT_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.ENVSUIT_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
