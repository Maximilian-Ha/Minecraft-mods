package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorHEV.
 *
 * Der HEV-Anzug wird nicht als Ruestungsschicht auf den Spieler gemalt, sondern als eigenes
 * Wellenfrontmodell an dessen Gliedern gezeichnet. Jedes Teil bringt nur die Stuecke mit, die
 * zu seinem Platz gehoeren: der Helm den Kopf, die Weste Rumpf und Arme, die Hose die Beine,
 * die Stiefel die Fuesse.
 *
 * ABWEICHUNG: das Original setzt die Drehpunkte von Armen und Beinen von Hand
 * (setRotationPoint(5, 2, 0) und so weiter). Im Port uebernimmt ModelArmorBase sie aus dem
 * Spielermodell -- damit sitzen sie auch dann richtig, wenn der Spieler kniet oder ein
 * Kindmodell traegt, was die festen Werte des Originals nicht leisten.
 */
public class ModelArmorHEV extends ModelArmorBase {

    public ModelArmorHEV(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_hev, "Head").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_hev, "Body").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_hev, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_hev, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_hev, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_hev, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_hev, "LeftFoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_hev, "RightFoot").copyRotationFrom(model.rightLeg);
    }

    /*
     * ModelArmorBase ruft render zweimal auf: einmal fuer den Kopf, einmal fuer den Rest. Das
     * kommt daher, dass beim Kindmodell beide Haelften unterschiedlich skaliert werden.
     */
    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) {
                bindTexture(ResourceManager.HEV_HELMET);
                this.head.render(0.0625F);
            }
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.HEV_CHEST);
                this.body.render(0.0625F);
                bindTexture(ResourceManager.HEV_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(ResourceManager.HEV_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.HEV_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
