package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.mojang.math.Axis;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorRPA.
 *
 * Die Remnant-Panzerruestung. Wie beim HEV-Anzug ein eigenes Wellenfrontmodell an den
 * Gliedern des Spielers, aber mit zwei Besonderheiten an der Weste:
 *
 *   * DER GLUEHSTREIFEN (Glow) wird mit voller Helligkeit gezeichnet, unabhaengig davon, wie
 *     dunkel es um den Traeger ist. Er sitzt auf demselben Drehpunkt wie der Rumpf.
 *   * DER LUEFTER (Fan) dreht sich, und zwar nach der Uhr, nicht nach einer Tickzahl: eine
 *     halbe Umdrehung je Sekunde. Er sitzt viereinhalb Achtel ueber dem Drehpunkt des Rumpfes
 *     und dreht sich um die Z-Achse.
 *
 * ABWEICHUNG: das Original setzt die Drehpunkte von Armen und Beinen von Hand. Im Port
 * uebernimmt ModelArmorBase sie aus dem Spielermodell -- damit sitzen sie auch dann richtig,
 * wenn der Spieler kniet oder ein Kindmodell traegt. Das ist dieselbe Entscheidung wie beim
 * HEV-Anzug.
 */
public class ModelArmorRPA extends ModelArmorBase {

    private final ModelRendererObj fan;
    private final ModelRendererObj glow;

    public ModelArmorRPA(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_remnant, "Head").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_remnant, "Body").copyRotationFrom(model.body);
        this.fan = new ModelRendererObj(ResourceManager.armor_remnant, "Fan").copyRotationFrom(model.body);
        this.glow = new ModelRendererObj(ResourceManager.armor_remnant, "Glow").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_remnant, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_remnant, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_remnant, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_remnant, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_remnant, "LeftBoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_remnant, "RightBoot").copyRotationFrom(model.rightLeg);
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) {
                bindTexture(ResourceManager.RPA_HELMET);
                this.head.render(0.0625F);
            }
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(ResourceManager.RPA_ARM);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);

                bindTexture(ResourceManager.RPA_CHEST);
                this.body.render(0.0625F);

                /* Der Gluehstreifen sitzt genau auf dem Rumpf und leuchtet aus sich selbst. */
                this.body.copyTo(this.glow);
                FullBright.enable();
                this.glow.render(0.0625F);
                FullBright.disable();

                this.renderFan();
            }
            case LEGS -> {
                bindTexture(ResourceManager.RPA_LEG);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            case FEET -> {
                bindTexture(ResourceManager.RPA_LEG);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }

    /**
     * Der Luefter dreht sich um seine eigene Mitte, nicht um den Drehpunkt des Rumpfes.
     * Deshalb wird er erst dorthin geschoben, gedreht und wieder zurueckgeschoben -- genau
     * die Reihenfolge des Originals.
     */
    private void renderFan() {

        float px = 0.0625F;

        RenderContext.pushPose();
        RenderContext.translate(this.body.x * px, this.body.y * px, this.body.z * px);

        if(this.body.zRot != 0F) RenderContext.mulPose(Axis.ZP.rotation(this.body.zRot));
        if(this.body.yRot != 0F) RenderContext.mulPose(Axis.YP.rotation(this.body.yRot));
        if(this.body.xRot != 0F) RenderContext.mulPose(Axis.XP.rotation(this.body.xRot));

        RenderContext.translate(0F, 4.875F * px, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-(System.currentTimeMillis() / 2D % 360D)));
        RenderContext.translate(0F, -4.875F * px, 0F);

        /* Der Luefter ist schon an seinem Platz -- sein eigener Drehpunkt bleibt bei null. */
        this.fan.setPos(0F, 0F, 0F);
        this.fan.setRotation(0F, 0F, 0F);
        bindTexture(ResourceManager.RPA_CHEST);
        this.fan.render(0.0625F);

        RenderContext.popPose();
    }
}
