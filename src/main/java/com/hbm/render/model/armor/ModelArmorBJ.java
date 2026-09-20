package com.hbm.render.model.armor;

import com.hbm.items.armor.ArmorBJItem.Variante;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorBJ.
 *
 * Der Blackjack-Anzug. Neun Wellenfrontteile; das neunte ist ein RUECKENTRIEBWERK, das nur
 * die gefluegelte Brustplatte traegt. Es hat keinen eigenen Drehpunkt, sondern uebernimmt
 * den des Rumpfes.
 *
 * EINE KLASSE FUER BEIDE BRUSTPLATTEN. Das Original loest das ueber eine fuenfte
 * Ordnungszahl: ModelArmorBJ(5) heisst "Brustplatte MIT Triebwerk", und ArmorBJJetpack
 * baut sich sein Modell damit. Der Port nimmt dafuer einen Aufzaehlungswert -- dieselbe
 * Unterscheidung, nur ohne eine Zahl, die man kennen muss.
 *
 * DER HELM IST EINE AUGENKLAPPE. Seine Textur heisst im Original darum bj_eyepatch und
 * nicht bj_helmet; der Port behaelt den Namen bei.
 *
 * ABWEICHUNG wie bei ModelArmorT51: die Drehpunkte kommen aus dem Spielermodell.
 */
public class ModelArmorBJ extends ModelArmorBase {

    private final ModelRendererObj jetpack;
    private final boolean mitTriebwerk;

    public ModelArmorBJ(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot, Variante variante) {
        super(model, slot);

        this.mitTriebwerk = variante == Variante.JETPACK;

        this.head = new ModelRendererObj(ResourceManager.armor_bj, "Head").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_bj, "Body").copyRotationFrom(model.body);
        this.jetpack = new ModelRendererObj(ResourceManager.armor_bj, "Jetpack");
        this.leftArm = new ModelRendererObj(ResourceManager.armor_bj, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_bj, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_bj, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_bj, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_bj, "LeftFoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_bj, "RightFoot").copyRotationFrom(model.rightLeg);
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) {
                bindTexture(ResourceManager.BJ_EYEPATCH);
                this.head.render(0.0625F);
            }
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.BJ_CHEST);
                this.body.render(0.0625F);

                if(this.mitTriebwerk) {
                    this.body.copyTo(this.jetpack);
                    bindTexture(ResourceManager.BJ_JETPACK);
                    this.jetpack.render(0.0625F);
                }

                bindTexture(ResourceManager.BJ_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(ResourceManager.BJ_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.BJ_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
