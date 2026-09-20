package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.factory.XFactoryCatapult;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.hbm.render.util.RenderMiscEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderFatMan.
 *
 * Der Fatman. Sechs Modellteile, von denen sich fuenf einzeln bewegen:
 *   Launcher  das Rohr, unbeweglich
 *   Handle    der Griff, den der Schuetze beim Nachladen zurueckzieht (HANDLE)
 *   Gauge     der Zeiger, der beim Schuss hochschnellt (GAUGE) -- er sitzt am Griff und
 *             bewegt sich deshalb mit ihm mit, weshalb er in derselben Klammer steht
 *   Lid       die Klappe, die zum Laden aufgeht (LID)
 *   Piston    der Stempel, der den Kopf ins Rohr schiebt (PISTON)
 *   MiniNuke  der Sprengkopf selbst (NUKE)
 *
 * DER STEMPEL STEHT VORN, WENN DAS ROHR LEER IST. Das ist keine Bewegung, sondern ein
 * Zustand: ohne Kopf im Rohr steht er um drei Einheiten versetzt, sobald die Bewegung ihn
 * nicht gerade selbst schiebt (piston[2] == 0). So sieht man der Waffe an, ob sie geladen ist.
 *
 * DER BLAUFLAMMEN-KOPF GLITZERT. Das Original malt ihn dreimal mit verschobener Texturmatrix
 * uebereinander; der Port hat dafuer RenderMiscEffects.renderClassicGlint, das dieselbe
 * Wirkung mit zwei Lagen erzielt und schon am Blauflammen-Sprengsatz haengt.
 *
 * ABWEICHUNG: setupModTable ist nicht uebernommen -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild. Das ist im Port ueberall so.
 */
public class ItemRenderFatman extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aiming = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1D - aiming * 0.33D);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.5F * offset, -1.25F * offset, 0.5F * offset,
                -1F * offset, -1.25F * offset, 0F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();

        float scale = 0.5F;
        RenderContext.scale(scale, scale, scale);

        boolean geladen = istGeladen(gun, stack);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] lid = HbmAnimations.getRelevantTransformation("LID");
        float[] nuke = HbmAnimations.getRelevantTransformation("NUKE");
        float[] piston = HbmAnimations.getRelevantTransformation("PISTON");
        float[] handle = HbmAnimations.getRelevantTransformation("HANDLE");
        float[] gauge = HbmAnimations.getRelevantTransformation("GAUGE");

        RenderContext.translate(0F, 1F, -2F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, -1F, 2F);

        RenderSystem.setShaderTexture(0, ResourceManager.FATMAN_TEX);
        ResourceManager.fatman.renderPart("Launcher");

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, handle[2]);
        ResourceManager.fatman.renderPart("Handle");

        RenderContext.translate(0.4375F, -0.875F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(gauge[2]));
        RenderContext.translate(-0.4375F, 0.875F, 0F);
        ResourceManager.fatman.renderPart("Gauge");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0.25F, 0.125F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(lid[2]));
        RenderContext.translate(-0.25F, -0.125F, 0F);
        ResourceManager.fatman.renderPart("Lid");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, piston[2]);
        if(!geladen && piston[2] == 0F) RenderContext.translate(0F, 0F, 3F);
        ResourceManager.fatman.renderPart("Piston");
        RenderContext.popPose();

        /* Der Kopf wird auch dann gezeigt, wenn das Rohr schon leer ist, solange die
         * Nachladebewegung ihn noch bewegt -- sonst verschwindet er mitten im Einlegen. */
        if(geladen || nuke[0] != 0F || nuke[1] != 0F || nuke[2] != 0F) {
            RenderContext.pushPose();
            RenderContext.translate(nuke[0], nuke[1], nuke[2]);
            renderNuke(gun, stack);
            RenderContext.popPose();
        }
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 2.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(-0.5F, 0.5F, -3F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.375F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0F, -0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        boolean geladen = istGeladen(gun, stack);

        RenderSystem.setShaderTexture(0, ResourceManager.FATMAN_TEX);
        ResourceManager.fatman.renderPart("Launcher");
        ResourceManager.fatman.renderPart("Handle");
        ResourceManager.fatman.renderPart("Gauge");
        ResourceManager.fatman.renderPart("Lid");

        /* Ohne pushPose: das Original verschiebt hier ebenfalls dauerhaft, damit der Kopf,
         * der danach kommt, mit dem Stempel mitgeht. */
        if(!geladen) RenderContext.translate(0F, 0F, 3F);
        ResourceManager.fatman.renderPart("Piston");
        if(geladen) renderNuke(gun, stack);
    }

    private boolean istGeladen(GunBaseNTItem gun, ItemStack stack) {
        return gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, null) > 0;
    }

    /** Der Sprengkopf. Nur der Blauflammen-Kopf hat eine eigene Textur und ein Glitzern. */
    private void renderNuke(GunBaseNTItem gun, ItemStack stack) {

        Object type = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getType(stack, null);

        if(type == XFactoryCatapult.nuke_balefire) {
            RenderSystem.setShaderTexture(0, ResourceManager.FATMAN_BALEFIRE_TEX);
            ResourceManager.fatman.renderPart("MiniNuke");
            RenderSystem.setShaderTexture(0, RenderMiscEffects.GLINT_BF);
            RenderMiscEffects.renderClassicGlint(partialTick, ResourceManager.fatman, "MiniNuke", 0.0F, 0.8F, 0.15F, -6F, 2F);
        } else {
            RenderSystem.setShaderTexture(0, ResourceManager.FATMAN_MININUKE_TEX);
            ResourceManager.fatman.renderPart("MiniNuke");
        }
    }
}
