package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorDiesel.
 *
 * Der Dieselanzug. Acht Wellenfrontteile, vier Texturen.
 *
 * DRITTER NAME FUER DIESELBE SACHE: die Klasse heisst Diesel, die Registriernamen heissen
 * dieselsuit_*, und das Modell samt Texturen heisst bnuuy -- auch der Werkstoff des
 * Originals traegt diesen Namen (HBM_BNUUY). Wer nach "diesel" sucht, findet weder Modell
 * noch Texturen. Nach dnt/dns (Runde 223) und Desh/steamsuit (Runde 224) ist das die dritte
 * Verschiebung dieser Art in drei Runden.
 *
 * ABWEICHUNG wie bei ModelArmorT51: die Drehpunkte kommen aus dem Spielermodell.
 */
public class ModelArmorDiesel extends ModelArmorBase {

    public ModelArmorDiesel(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_dieselsuit, "Head").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_dieselsuit, "Body").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_dieselsuit, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_dieselsuit, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_dieselsuit, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_dieselsuit, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_dieselsuit, "LeftBoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_dieselsuit, "RightBoot").copyRotationFrom(model.rightLeg);
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) {
                bindTexture(ResourceManager.DIESELSUIT_HELMET);
                this.head.render(0.0625F);
            }
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.DIESELSUIT_CHEST);
                this.body.render(0.0625F);
                bindTexture(ResourceManager.DIESELSUIT_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(ResourceManager.DIESELSUIT_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.DIESELSUIT_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
