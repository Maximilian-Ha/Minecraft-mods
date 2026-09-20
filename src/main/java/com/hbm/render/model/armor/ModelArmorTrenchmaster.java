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
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorTrenchmaster.
 *
 * Der Grabenmeister. Neun Wellenfrontteile: zum Helm gehoert ein LAMPENTEIL, das immer voll
 * ausgeleuchtet gezeichnet wird -- die Lampe leuchtet auch im Dunkeln, sonst waere sie keine.
 * Es hat keinen eigenen Drehpunkt, sondern uebernimmt den des Helms.
 *
 * DER HELM IST DURCHSCHEINEND, die Lampe nicht. Das Original schaltet die Alphamischung um
 * den Helm herum ein und danach wieder aus -- anders als beim Fau-Anzug, wo es das Ausschalten
 * vergisst. Hier ist also nichts zu berichtigen, nur zu uebernehmen.
 *
 * DIE BEINE STEHEN EINEN HAUCH AUSEINANDER, wie bei der Taurun-Ruestung: das Original
 * verschiebt um -0,01 und +0,01 Welteinheiten, im Port sind das +/- 0,16 Modelleinheiten.
 *
 * ABWEICHUNG wie bei ModelArmorT51: die Drehpunkte kommen aus dem Spielermodell statt von
 * Hand, damit sie auch beim Ducken und beim Kindmodell sitzen.
 */
public class ModelArmorTrenchmaster extends ModelArmorBase {

    /** -0,01 Welteinheiten in Modelleinheiten: ein Drehpunkt wird durch sechzehn geteilt. */
    private static final float BEIN_VERSATZ = 0.16F;

    private final ModelRendererObj light;

    public ModelArmorTrenchmaster(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_trenchmaster, "Helmet").copyRotationFrom(model.head);
        this.light = new ModelRendererObj(ResourceManager.armor_trenchmaster, "Light");
        this.body = new ModelRendererObj(ResourceManager.armor_trenchmaster, "Chest").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_trenchmaster, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_trenchmaster, "RightArm").copyRotationFrom(model.rightArm);

        this.leftLeg = new ModelRendererObj(ResourceManager.armor_trenchmaster, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_trenchmaster, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_trenchmaster, "LeftBoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_trenchmaster, "RightBoot").copyRotationFrom(model.rightLeg);

        this.leftLeg.x -= BEIN_VERSATZ;
        this.rightLeg.x += BEIN_VERSATZ;
        this.leftFoot.x -= BEIN_VERSATZ;
        this.rightFoot.x += BEIN_VERSATZ;
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot != EquipmentSlot.HEAD) return;

            bindTexture(ResourceManager.TRENCHMASTER_HELMET);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            this.head.render(0.0625F);
            RenderSystem.disableBlend();

            /* Die Lampe geht mit dem Helm mit und leuchtet aus sich selbst. Der vorige
             * Lichtwert wird gemerkt und danach zurueckgelegt, damit der Rest der Figur
             * nicht mitleuchtet. */
            this.head.copyTo(this.light);

            int vorher = RenderContext.light();
            RenderContext.setLight(LightTexture.FULL_BRIGHT);
            this.light.render(0.0625F);
            RenderContext.setLight(vorher);
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.TRENCHMASTER_CHEST);
                this.body.render(0.0625F);
                bindTexture(ResourceManager.TRENCHMASTER_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(ResourceManager.TRENCHMASTER_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.TRENCHMASTER_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
