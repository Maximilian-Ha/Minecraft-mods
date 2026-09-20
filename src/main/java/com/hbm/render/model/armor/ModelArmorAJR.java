package com.hbm.render.model.armor;

import com.hbm.items.armor.ArmorAJRItem.Variante;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelArmorAJR UND ModelArmorAJRO.
 *
 * EINE KLASSE FUER BEIDE GARNITUREN. Das Original hat zwei Dateien, die sich Zeile fuer
 * Zeile gleichen -- beide laden dasselbe Wellenfrontmodell (AJR.obj) und unterscheiden sich
 * nur in den vier Texturen, die sie binden. Der Port reicht deshalb die Variante in den
 * Konstruktor hinein, statt die Klasse abzuschreiben. WELCHE Texturen dazugehoeren, steht
 * hier und nicht am Gegenstand: ResourceManager ist clientseitig, die Anmeldung in NtmItems
 * laeuft auf beiden Seiten.
 *
 * DAS MODELL HAT EIN TEIL ZU VIEL: AJR.obj enthaelt neun Teile, gezeichnet werden acht. Der
 * RocketBox ruehrt auch im Original keine der beiden Klassen an. Er bleibt ungezeichnet, hier
 * wie dort.
 *
 * ABWEICHUNG wie bei ModelArmorT51: die Drehpunkte kommen aus dem Spielermodell statt von
 * Hand, damit sie auch beim Ducken und beim Kindmodell sitzen.
 */
public class ModelArmorAJR extends ModelArmorBase {

    private final ResourceLocation texHelm;
    private final ResourceLocation texBrust;
    private final ResourceLocation texArm;
    private final ResourceLocation texBein;

    public ModelArmorAJR(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot, Variante variante) {

        super(model, slot);

        boolean orange = variante == Variante.ORANGE;
        this.texHelm = orange ? ResourceManager.AJRO_HELMET : ResourceManager.AJR_HELMET;
        this.texBrust = orange ? ResourceManager.AJRO_CHEST : ResourceManager.AJR_CHEST;
        this.texArm = orange ? ResourceManager.AJRO_ARM : ResourceManager.AJR_ARM;
        this.texBein = orange ? ResourceManager.AJRO_LEG : ResourceManager.AJR_LEG;

        this.head = new ModelRendererObj(ResourceManager.armor_ajr, "Head").copyRotationFrom(model.head);
        this.body = new ModelRendererObj(ResourceManager.armor_ajr, "Body").copyRotationFrom(model.body);
        this.leftArm = new ModelRendererObj(ResourceManager.armor_ajr, "LeftArm").copyRotationFrom(model.leftArm);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_ajr, "RightArm").copyRotationFrom(model.rightArm);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_ajr, "LeftLeg").copyRotationFrom(model.leftLeg);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_ajr, "RightLeg").copyRotationFrom(model.rightLeg);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_ajr, "LeftBoot").copyRotationFrom(model.leftLeg);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_ajr, "RightBoot").copyRotationFrom(model.rightLeg);
    }

    @Override
    public void render(boolean head) {

        if(head) {
            if(this.slot == EquipmentSlot.HEAD) {
                bindTexture(this.texHelm);
                this.head.render(0.0625F);
            }
            return;
        }

        switch(this.slot) {
            case CHEST -> {
                bindTexture(this.texBrust);
                this.body.render(0.0625F);
                bindTexture(this.texArm);
                this.leftArm.render(0.0625F);
                this.rightArm.render(0.0625F);
            }
            case LEGS -> {
                bindTexture(this.texBein);
                this.leftLeg.render(0.0625F);
                this.rightLeg.render(0.0625F);
            }
            /* Die Stiefel teilen sich die Beintextur -- so steht es im Original. */
            case FEET -> {
                bindTexture(this.texBein);
                this.leftFoot.render(0.0625F);
                this.rightFoot.render(0.0625F);
            }
            default -> { }
        }
    }
}
