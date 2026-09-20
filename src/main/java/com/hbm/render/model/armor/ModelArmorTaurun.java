package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorTaurun.
 *
 * Die Taurun-Ruestung. Acht Wellenfrontteile, vier Texturen: Helm, Rumpf, Arme, Beine --
 * die Stiefel teilen sich die Beintextur.
 *
 * DIE BEINE STEHEN EINEN HAUCH AUSEINANDER. Das Original schiebt vor dem linken Bein um
 * -0,01 und vor dem rechten um +0,01 Welteinheiten zur Seite, damit die beiden Haelften
 * nicht ineinander flimmern. Ein Drehpunkt zaehlt hier in Sechzehnteln einer Welteinheit,
 * also sind das +/- 0,16 -- derselbe Versatz, andere Einheit. Dasselbe gilt fuer die Stiefel.
 *
 * ABWEICHUNG wie bei ModelArmorT51: die Drehpunkte kommen aus dem Spielermodell statt von
 * Hand, damit sie auch beim Ducken und beim Kindmodell sitzen. Der Versatz oben kommt
 * danach obendrauf.
 */
public class ModelArmorTaurun extends ModelArmorBase {

    /** -0,01 Welteinheiten in Modelleinheiten: ein Drehpunkt wird durch sechzehn geteilt. */
    private static final float BEIN_VERSATZ = 0.16F;

    public ModelArmorTaurun(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_taurun, "Helmet").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_taurun, "Chest").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_taurun, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_taurun, "RightArm").copyRotationFrom(model.rightArm);

        this.leftLeg = new ModelRendererObj(ResourceManager.armor_taurun, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_taurun, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_taurun, "LeftBoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_taurun, "RightBoot").copyRotationFrom(model.rightLeg);

        this.leftLeg.x -= BEIN_VERSATZ;
        this.rightLeg.x += BEIN_VERSATZ;
        this.leftFoot.x -= BEIN_VERSATZ;
        this.rightFoot.x += BEIN_VERSATZ;
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) {
                bindTexture(ResourceManager.TAURUN_HELMET);
                this.head.render(0.0625F);
            }
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.TAURUN_CHEST);
                this.body.render(0.0625F);
                bindTexture(ResourceManager.TAURUN_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(ResourceManager.TAURUN_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.TAURUN_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
