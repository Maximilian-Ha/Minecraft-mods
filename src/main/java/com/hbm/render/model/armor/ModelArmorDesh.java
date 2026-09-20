package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorDesh.
 *
 * Der Dampfanzug. Acht Wellenfrontteile, vier Texturen.
 *
 * DIE KLASSE HEISST NACH DEM WERKSTOFF, NICHT NACH DER GARNITUR: Desh ist das Metall, der
 * Anzug heisst "Steam Suit". Wer nach "steamsuit" sucht, findet diese Klasse nicht -- genau
 * die Namensverschiebung, die in Runde 215 zu einer Fehlmessung gefuehrt hat. Die Texturen
 * heissen dagegen steamsuit_*.
 *
 * ABWEICHUNG wie bei ModelArmorT51: die Drehpunkte kommen aus dem Spielermodell.
 */
public class ModelArmorDesh extends ModelArmorBase {

    public ModelArmorDesh(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_steamsuit, "Head").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_steamsuit, "Body").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_steamsuit, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_steamsuit, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_steamsuit, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_steamsuit, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_steamsuit, "LeftBoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_steamsuit, "RightBoot").copyRotationFrom(model.rightLeg);
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) {
                bindTexture(ResourceManager.STEAMSUIT_HELMET);
                this.head.render(0.0625F);
            }
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.STEAMSUIT_CHEST);
                this.body.render(0.0625F);
                bindTexture(ResourceManager.STEAMSUIT_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(ResourceManager.STEAMSUIT_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.STEAMSUIT_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
