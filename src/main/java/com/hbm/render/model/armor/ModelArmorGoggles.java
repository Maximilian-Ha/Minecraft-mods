package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelGlasses.
 *
 * Die Aschebrille. Ein einziges Wellenfrontmodell am Kopf, sonst nichts.
 *
 * DAS ORIGINAL SCHLEPPT MEHR MIT, ALS ES ZEICHNET: sein ModelGlasses legt neben dem Kopf auch
 * Rumpf, Arme, Beine und Fuesse aus dem BJ-Ruestungsmodell an -- und zeichnet davon in render()
 * nie eines, weil die Brille nur den Kopfschlitz belegt. Diese sieben Zuweisungen sind nicht
 * mitgekommen; sie waeren toter Zustand. Deshalb braucht der Port auch die BJ-Modelldatei
 * dafuer nicht, sondern nur goggles.obj.
 *
 * NICHT ZU VERWECHSELN mit ModelGogglesHead aus Runde 209: das ist die gewoehnliche
 * Schutzbrille (goggles), ein Kastenmodell. Diese hier ist die Aschebrille (ashglasses) und
 * benutzt verwirrenderweise die Dateien, die im Original "goggles" heissen.
 */
public class ModelArmorGoggles extends ModelArmorBase {

    public ModelArmorGoggles(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_goggles).copyRotationFrom(model.head);
    }

    @Override
    public void render(boolean head) {

        if(!head) return;
        if(this.slot != EquipmentSlot.HEAD) return;

        bindTexture(ResourceManager.ARMOR_GOGGLES);
        this.head.render(0.0625F);
    }
}
