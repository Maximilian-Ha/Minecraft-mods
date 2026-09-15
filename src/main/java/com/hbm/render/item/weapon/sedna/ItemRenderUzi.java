package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderUzi.
 *
 * Beweglich sind das Schloss (SLIDE), das Magazin (MAG), die Patrone im Lauf (BULLET) und die
 * beiden Haelften der Schulterstuetze, die beim Ziehen aufklappen. Mit Schalldaempfer faellt
 * Muendungsfeuer und Rauch weg -- das ist es, was ein Daempfer im Bild ausmacht --, und das
 * Saturnit-Gehaeuse tauscht nur die Textur.
 *
 * ABWEICHUNG: setupModTable und renderModTable entfallen. Sie stellen die Waffe im Waffentisch
 * dar; der Tisch des Ports zeigt statt der Waffe ihr Gegenstandsbild.
 */
public class ItemRenderUzi extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.33F);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.75F * offset, -1.5F * offset, 2.5F * offset,
                0F, -4.375F / 8F, 1F);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        RenderContext.translate(0F, 1F, 1F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0F, 1F, 0F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, isSaturnite(stack, 0) ? ResourceManager.UZI_SATURNITE_TEX : ResourceManager.UZI_TEX);

        float scale = 0.25F;
        RenderContext.scale(scale, scale, scale);

        renderGunBody(stack, buffer, gun, 0, "Gun");
    }

    /**
     * Der bewegliche Teil. Er steht hier fuer sich, weil die beidhaendige Ausfuehrung ihn zweimal
     * braucht -- einmal je Empfaenger, mit gespiegeltem Gehaeuse.
     */
    protected void renderGunBody(ItemStack stack, MultiBufferSource buffer, GunBaseNTItem gun, int index, String bodyPart) {

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP", index);
        float[] stockFront = HbmAnimations.getRelevantTransformation("STOCKFRONT", index);
        float[] stockBack = HbmAnimations.getRelevantTransformation("STOCKBACK", index);
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL", index);
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT", index);
        float[] mag = HbmAnimations.getRelevantTransformation("MAG", index);
        float[] bullet = HbmAnimations.getRelevantTransformation("BULLET", index);
        float[] slide = HbmAnimations.getRelevantTransformation("SLIDE", index);
        float[] yeet = HbmAnimations.getRelevantTransformation("YEET", index);
        float[] speen = HbmAnimations.getRelevantTransformation("SPEEN", index);

        RenderContext.translate(yeet[0], yeet[1], yeet[2]);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(speen[0]));

        RenderContext.translate(0F, -2F, -4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 2F, 4F);

        RenderContext.translate(0F, 0F, -6F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 0F, 6F);

        RenderContext.translate(0F, 0F, recoil[2]);

        ResourceManager.uzi.renderPart(bodyPart);

        boolean silenced = hasSilencer(stack, index);
        if(silenced) ResourceManager.uzi.renderPart("Silencer");

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.3125F, -5.75F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(180F - stockFront[0]));
        RenderContext.translate(0F, -0.3125F, 5.75F);
        ResourceManager.uzi.renderPart("StockFront");

        RenderContext.translate(0F, -0.3125F, -3F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-200F - stockBack[0]));
        RenderContext.translate(0F, 0.3125F, 3F);
        ResourceManager.uzi.renderPart("StockBack");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, slide[2]);
        ResourceManager.uzi.renderPart("Slide");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(mag[0], mag[1], mag[2]);
        ResourceManager.uzi.renderPart("Magazine");
        if(bullet[0] == 1) ResourceManager.uzi.renderPart("Bullet");
        RenderContext.popPose();

        if(!silenced) {
            float smokeScale = 0.5F;

            RenderContext.pushPose();
            RenderContext.translate(0F, 0.75F, 8.5F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.scale(smokeScale, smokeScale, smokeScale);
            renderSmokeNodes(buffer, gun.getConfig(stack, index).smokeNodes, 0.75F);
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 0.75F, 8.5F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
            renderMuzzleFlash(buffer, gun.lastShot[index], 75, 7.5F);
            RenderContext.popPose();
        }
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        boolean silenced = hasSilencer(stack, 0);

        if(silenced && displayContext == ItemDisplayContext.GUI) {
            float scale = 0.625F;
            RenderContext.scale(scale, scale, scale);
            RenderContext.translate(0F, 0F, -4F);
        }

        renderStandardGun(stack, 0, "Gun");

        if(living != null && !silenced && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {
            renderThirdPersonFlash(stack, buffer, 0);
        }
    }

    /**
     * Die Waffe ohne Animation -- im Inventar, am Boden und in dritter Person. Welches Gehaeuse
     * gezeichnet wird, steht ausdruecklich dabei: die beidhaendige Ausfuehrung spiegelt ihre
     * linke Haelfte, die einzelne nie.
     */
    protected void renderStandardGun(ItemStack stack, int index, String bodyPart) {

        RenderSystem.setShaderTexture(0, isSaturnite(stack, index) ? ResourceManager.UZI_SATURNITE_TEX : ResourceManager.UZI_TEX);
        ResourceManager.uzi.renderPart(bodyPart);
        ResourceManager.uzi.renderPart("StockBack");
        ResourceManager.uzi.renderPart("StockFront");
        ResourceManager.uzi.renderPart("Slide");
        ResourceManager.uzi.renderPart("Magazine");
        if(hasSilencer(stack, index)) ResourceManager.uzi.renderPart("Silencer");
    }

    /**
     * Das Muendungsfeuer in dritter Person. Bei fremden Spielern steht nur ein einziger Zeitpunkt
     * zur Verfuegung -- die Karte kennt keine Empfaenger --, also blitzen bei der beidhaendigen
     * Ausfuehrung beide Laeufe zugleich.
     */
    protected void renderThirdPersonFlash(ItemStack stack, MultiBufferSource buffer, int index) {

        long shot;
        float shotRand = 0;

        if(living == Minecraft.getInstance().player) {
            GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
            shot = gun.lastShot[index];
            shotRand = gun.shotRand;
        } else {
            shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
            if(shot < 0) return;
        }

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.75F, 8.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
        renderMuzzleFlash(buffer, shot, 75, 7.5F);
        RenderContext.popPose();
    }

    public boolean hasSilencer(ItemStack stack, int cfg) {
        return XWeaponModManager.hasUpgrade(stack, cfg, XWeaponModManager.ID_SILENCER);
    }

    public boolean isSaturnite(ItemStack stack, int cfg) {
        return XWeaponModManager.hasUpgrade(stack, cfg, XWeaponModManager.ID_UZI_SATURN);
    }
}
