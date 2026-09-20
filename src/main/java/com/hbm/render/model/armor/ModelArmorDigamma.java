package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorDigamma.
 *
 * Der Fau-Anzug. Neun Wellenfrontteile statt der ueblichen acht: ueber der Brustplatte sitzt
 * eine KASSETTE mit eigener Textur. Sie hat keinen eigenen Drehpunkt, sondern uebernimmt den
 * des Rumpfes -- deshalb wird er vor dem Zeichnen auf sie kopiert.
 *
 * DIE KASSETTE IST DURCHSCHEINEND. Sie ist der einzige Ruestungsteil des Ports, der mit
 * Alphamischung gezeichnet wird.
 *
 * ABWEICHUNG, die absichtlich ist: das Original schaltet die Mischung vor der Kassette EIN
 * und nie wieder aus -- was danach gezeichnet wird, erbt sie. Der Port schaltet sie hinterher
 * wieder ab. Einen durchgereichten Zeichenzustand nachzubauen waere kein treuer Port, sondern
 * ein abgeschriebener Fehler.
 *
 * ABWEICHUNG wie bei ModelArmorT51: die Drehpunkte kommen aus dem Spielermodell statt von
 * Hand, damit sie auch beim Ducken und beim Kindmodell sitzen.
 */
public class ModelArmorDigamma extends ModelArmorBase {

    private final ModelRendererObj cassette;

    public ModelArmorDigamma(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_fau, "Head").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_fau, "Body").copyRotationFrom(model.body);
        this.cassette = new ModelRendererObj(ResourceManager.armor_fau, "Cassette");
        this.leftArm = new ModelRendererObj(ResourceManager.armor_fau, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_fau, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_fau, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_fau, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_fau, "LeftBoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_fau, "RightBoot").copyRotationFrom(model.rightLeg);
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) {
                bindTexture(ResourceManager.FAU_HELMET);
                this.head.render(0.0625F);
            }
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.FAU_CHEST);
                this.body.render(0.0625F);

                /* Die Kassette sitzt am Rumpf und geht mit ihm mit. */
                this.body.copyTo(this.cassette);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                bindTexture(ResourceManager.FAU_CASSETTE);
                this.cassette.render(0.0625F);
                RenderSystem.disableBlend();

                bindTexture(ResourceManager.FAU_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(ResourceManager.FAU_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.FAU_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
