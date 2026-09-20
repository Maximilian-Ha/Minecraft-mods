package com.hbm.render.model.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Portiert aus 1.7.10: com.hbm.render.model.ModelHat.
 *
 * Der Hut. Das schlichteste Ruestungsmodell des Ports: EIN Wellenfrontteil, eine Textur, und
 * nur am Kopf. Das Original legt die sieben anderen Glieder als leere ModelRendererObj(null)
 * an; der Port laesst sie weg, weil ModelArmorBase sie ohnehin leer vorbelegt.
 *
 * DAS MODELL HAT KEINE BENANNTEN TEILE, nur ein einziges Netz. Deshalb wird es ohne
 * Teilenamen geladen -- ModelRendererObj zeichnet dann alles auf einmal.
 *
 * MODELL UND TEXTUR LAGEN SCHON IM PORT: der Wackelkopf (RenderBobble) traegt denselben Hut
 * und hat armor_hat und HAT_TEX mitgebracht. Es fehlte nur der Ruestungsgegenstand dazu.
 */
public class ModelHat extends ModelArmorBase {

    public ModelHat(HumanoidModel<? extends LivingEntity> model, EquipmentSlot slot) {
        super(model, slot);

        this.head = new ModelRendererObj(ResourceManager.armor_hat).copyRotationFrom(model.head);
    }

    @Override
    public void render(boolean head) {

        if(!head || this.slot != EquipmentSlot.HEAD) return;

        bindTexture(ResourceManager.HAT_TEX);
        this.head.render(0.0625F);
    }
}
