package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorT51.
 *
 * Die T-51-Panzerruestung. Wie HEV und RPA kein Schichtbild auf dem Spieler, sondern ein
 * eigenes Wellenfrontmodell an seinen Gliedern. Vier Texturen teilen sich die acht Teile:
 * der Helm eine, Rumpf und Arme zwei, Beine und Stiefel je eine.
 *
 * ABWEICHUNG wie bei ModelArmorHEV: das Original setzt die Drehpunkte von Armen und Beinen
 * von Hand; im Port uebernimmt ModelArmorBase sie aus dem Spielermodell, damit sie auch beim
 * Ducken und beim Kindmodell sitzen.
 */
public class ModelArmorT51 extends ModelArmorBase {

    public ModelArmorT51(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_t51, "Helmet").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_t51, "Chest").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_t51, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_t51, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_t51, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_t51, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_t51, "LeftBoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_t51, "RightBoot").copyRotationFrom(model.rightLeg);
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) {
                bindTexture(ResourceManager.T51_HELMET);
                this.head.render(0.0625F);
            }
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.T51_CHEST);
                this.body.render(0.0625F);
                bindTexture(ResourceManager.T51_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(ResourceManager.T51_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.T51_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
