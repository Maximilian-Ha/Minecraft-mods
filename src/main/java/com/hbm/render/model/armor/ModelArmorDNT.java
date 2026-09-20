package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorDNT.
 *
 * Der DNT-Nanoanzug. Acht Wellenfrontteile, vier Texturen -- der schlichteste Aufbau aller
 * Panzerruestungen, obwohl er der staerkste Anzug des Mods ist.
 *
 * DIE TEXTUREN HEISSEN dnt_*, DIE GEGENSTAENDE dns_*. Das ist kein Vertipper, sondern steht
 * so im Original: der Werkstoff heisst Dineutronium (DNT), die Garnitur "DNT Nano Suit"
 * (DNS). Wer nach "dns" sucht, findet die Texturen nicht.
 *
 * ABWEICHUNG wie bei ModelArmorT51: die Drehpunkte kommen aus dem Spielermodell.
 */
public class ModelArmorDNT extends ModelArmorBase {

    public ModelArmorDNT(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_dnt, "Head").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_dnt, "Body").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_dnt, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_dnt, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_dnt, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_dnt, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_dnt, "LeftBoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_dnt, "RightBoot").copyRotationFrom(model.rightLeg);
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) {
                bindTexture(ResourceManager.DNT_HELMET);
                this.head.render(0.0625F);
            }
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.DNT_CHEST);
                this.body.render(0.0625F);
                bindTexture(ResourceManager.DNT_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(ResourceManager.DNT_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.DNT_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
