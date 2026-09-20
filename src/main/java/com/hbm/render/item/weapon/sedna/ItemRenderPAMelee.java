package com.hbm.render.item.weapon.sedna;

import com.hbm.items.armor.IPAMelee;
import com.hbm.items.armor.IPAWeaponsProvider;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderPAMelee.
 *
 * DIESER ZEICHNER ZEICHNET NICHTS. Er fragt die getragene Brustplatte, was in der ersten
 * Person zu sehen ist -- Faeuste bei der Remnant, Klingen bei der NCR -- und ueberlaesst ihr
 * die ganze Arbeit. Wer keine Panzerruestung traegt, sieht gar nichts, und das ist richtig so.
 *
 * BEIDHAENDIG: isAkimbo sagt dem Spielermodell, dass beide Haende gebraucht werden. Ohne das
 * haenge die zweite Hand leer herunter, waehrend ihr Arm im Bild zuschlaegt.
 *
 * Ausserhalb der ersten Person sind die Arme nicht mehr die des Traegers, sondern zwei
 * Modellteile im Gegenstandsbild. Dort steht der Bezug zur Ruestung nicht zur Verfuegung --
 * ein Gegenstand am Boden weiss nicht, wer ihn einmal getragen hat --, und wie im Original
 * werden schlicht die NCR-Arme gezeigt.
 *
 * ABWEICHUNG: das Original schreibt an dieser Stelle zweimal renderPart("Leftarm") mit
 * kleinem a. Der Lader des Ports vergleicht Namen ohne Ruecksicht auf Gross- und
 * Kleinschreibung, hier steht trotzdem die richtige Schreibweise.
 */
public class ItemRenderPAMelee extends ItemRenderWeaponBase {

    @Override public boolean isAkimbo(LivingEntity entity) { return true; }

    @Override protected float getSwayMagnitude(ItemStack stack) { return 2F; }
    @Override protected float getSwayPeriod(ItemStack stack) { return 0.5F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        IPAMelee bauteil = IPAWeaponsProvider.getMeleeComponentClient();
        if(bauteil != null) bauteil.setupFirstPerson(stack);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {
        IPAMelee bauteil = IPAWeaponsProvider.getMeleeComponentClient();
        if(bauteil != null) bauteil.renderFirstPerson(stack, buffer);
    }

    /* In der dritten Person haelt der Traeger nichts in der Hand -- er schlaegt mit der Ruestung. */
    @Override public void setupThirdPerson(ItemStack stack) { }

    @Override
    public void setupInv(ItemStack stack) {
        RenderContext.scale(1F, 1F, -1F);
        RenderContext.translate(8F, 8F, 0F);
        float scale = 2.5F;
        RenderContext.scale(scale, scale, scale);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.NCRPA_ARM);

        float scale = 0.3125F;
        RenderContext.pushPose();
        RenderContext.scale(scale, scale, scale);

        if(displayContext == ItemDisplayContext.GUI) {
            RenderContext.mulPose(Axis.ZP.rotationDegrees(135F));
            RenderContext.mulPose(Axis.YP.rotationDegrees(135F));
            RenderContext.translate(0F, -5.5F, 0F);
            RenderContext.translate(-3.5F, 0F, 0F);
            ResourceManager.armor_ncrpa.renderPart("LeftArm");
            RenderContext.translate(7F, 1F, -1F);
            ResourceManager.armor_ncrpa.renderPart("RightArm");
        } else {
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F));
            RenderContext.translate(0F, -5.5F, 0F);
            RenderContext.translate(-2F, 0F, 0F);
            ResourceManager.armor_ncrpa.renderPart("LeftArm");
            RenderContext.translate(4F, 0F, 0F);
            ResourceManager.armor_ncrpa.renderPart("RightArm");
        }

        RenderContext.popPose();
    }
}
