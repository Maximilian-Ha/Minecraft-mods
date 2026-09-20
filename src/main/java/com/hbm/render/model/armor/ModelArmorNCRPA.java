package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import com.hbm.render.util.FullBright;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorNCRPA.
 *
 * Die NCR-Panzerruestung. Dieselbe Bauart wie die Remnant, nur sitzt das leuchtende Stueck
 * hier am Helm statt an der Weste: die Augen. Einen Luefter hat sie nicht.
 *
 * Die Teile des Modells heissen anders als bei der Remnant -- Helmet und Chest statt Head und
 * Body. Das ist keine Nachlaessigkeit, sondern steht so in beiden Wellenfrontdateien.
 */
public class ModelArmorNCRPA extends ModelArmorBase {

    private final ModelRendererObj eyes;

    public ModelArmorNCRPA(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_ncrpa, "Helmet").copyRotationFrom(model.head);
        this.eyes = new ModelRendererObj(ResourceManager.armor_ncrpa, "Eyes").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_ncrpa, "Chest").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_ncrpa, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_ncrpa, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_ncrpa, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_ncrpa, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_ncrpa, "LeftBoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_ncrpa, "RightBoot").copyRotationFrom(model.rightLeg);
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) {
                bindTexture(ResourceManager.NCRPA_HELMET);
                this.head.render(0.0625F);

                /* Die Augen sitzen genau auf dem Helm und leuchten aus sich selbst. */
                this.head.copyTo(this.eyes);
                FullBright.enable();
                this.eyes.render(0.0625F);
                FullBright.disable();
            }
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.NCRPA_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);

                bindTexture(ResourceManager.NCRPA_CHEST);
                this.body.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(ResourceManager.NCRPA_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.NCRPA_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
