package com.hbm.render.model;

import com.hbm.entity.mob.UndeadSoldier;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelSkeletonNT, genauer dessen Rolle.
 *
 * Das Original hat zwei Modelle fuer den Untoten Soldaten: ModelZombie fuer die Zombiegestalt
 * und ModelSkeletonNT fuer die Skelettgestalt. ModelSkeletonNT ist nichts anderes als ein
 * ModelZombie mit duennen Armen und Beinen -- dieselbe Haltung, schmalere Glieder.
 *
 * DARUM STEHT HIER NUR EINE KLASSE. In 1.21 ist die Geometrie vom Verhalten getrennt: was ein
 * Modell FUER EIN TEIL ist, steht in der Modelllage, und die beiden, die gebraucht werden,
 * bringt Minecraft selbst mit (ModelLayers.ZOMBIE und ModelLayers.SKELETON -- letztere ist
 * Punkt fuer Punkt das, was ModelSkeletonNT von Hand aufbaut). Diese Klasse liefert nur noch,
 * was BEIDE gemeinsam haben: die Zombiehaltung.
 *
 * DIE ARME STEHEN IMMER VOR. Das ist kein Versehen, sondern ModelZombie aus 1.7.10: dessen
 * setRotationAngles setzt die Armwinkel unbedingt, nach ModelBiped und also ueber dessen
 * Haltung fuer die getragene Waffe hinweg. Deshalb steht hier true statt isAggressive().
 */
@OnlyIn(Dist.CLIENT)
public class ModelUndeadSoldier extends HumanoidModel<UndeadSoldier> {

    public ModelUndeadSoldier(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(UndeadSoldier soldat, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(soldat, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, this.attackTime, ageInTicks);
    }
}
