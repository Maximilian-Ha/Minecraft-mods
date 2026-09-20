package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorBismuth.
 *
 * Die Wismut-Garnitur. Acht Wellenfrontteile, aber nur EINE Textur fuer alle -- darin
 * unterscheidet sie sich von T-51, AJR und Taurun, die vier haben. Gebunden wird sie
 * deshalb einmal am Anfang statt vor jedem Teil.
 *
 * DIE FUSSTEILE HEISSEN LeftFoot UND RightFoot, nicht LeftBoot und RightBoot wie in den
 * anderen Modellen. Das steht so im Wellenfrontmodell; wer die anderen abschreibt, bekommt
 * unsichtbare Schuhe.
 *
 * DIE ARME KOMMEN VOR DEM RUMPF. Auch das ist aus dem Original uebernommen und dort die
 * einzige Ruestung, die so zeichnet.
 *
 * ABWEICHUNG wie bei ModelArmorT51: die Drehpunkte kommen aus dem Spielermodell statt von
 * Hand, damit sie auch beim Ducken und beim Kindmodell sitzen.
 */
public class ModelArmorBismuth extends ModelArmorBase {

    public ModelArmorBismuth(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_bismuth, "Head").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_bismuth, "Body").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_bismuth, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_bismuth, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_bismuth, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_bismuth, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_bismuth, "LeftFoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_bismuth, "RightFoot").copyRotationFrom(model.rightLeg);
    }

    @Override
    public void render(boolean head) {

        bindTexture(ResourceManager.ARMOR_BISMUTH_TEX);

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) this.head.render(0.0625F);
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
                this.body.render(0.0625F);
            }
            case LEGS -> {
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
