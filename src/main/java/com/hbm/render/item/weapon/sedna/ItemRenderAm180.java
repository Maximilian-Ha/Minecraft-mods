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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderAm180.
 *
 * Die AM180. Ihre Animationen kommen aus der Datei am180.json und werden ueber
 * applyRelevantTransformation auf die gleichnamigen Teile gelegt -- dieselbe Machart wie bei der
 * SPAS-12.
 *
 * Die Trommel dreht sich mit dem Fuellstand: bei 59 verbleibenden Patronen genau einmal ganz
 * herum. So sieht man von aussen, wie viel noch drin ist.
 *
 * Steckt ein Schalldaempfer, kommt das gleichnamige Modellteil dazu; Rauch und Muendungsfeuer
 * ruecken dann um vier Einheiten nach vorn und fallen kleiner aus.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- es stellt die Waffe im Waffentisch dar; der
 * Tisch des Ports zeigt statt der Waffe ihr Gegenstandsbild.
 */
public class ItemRenderAm180 extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

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
                -1F * offset, -1F * offset, 1F * offset,
                0F, -4.1875F / 8F, 0.25F);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        RenderContext.translate(0F, -0.5F, 3F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 0.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(1.5F, 0F, 0F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.AM180_TEX);

        float scale = 0.1875F;
        RenderContext.scale(scale, scale, scale);

        boolean silenced = hasSilencer(stack);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] magazine = HbmAnimations.getRelevantTransformation("MAG");
        float[] magTurn = HbmAnimations.getRelevantTransformation("MAGTURN");
        float[] magSpin = HbmAnimations.getRelevantTransformation("MAGSPIN");
        float[] bolt = HbmAnimations.getRelevantTransformation("BOLT");
        float[] turn = HbmAnimations.getRelevantTransformation("TURN");

        RenderContext.translate(0F, -2F, -6F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 2F, 6F);

        RenderContext.mulPose(Axis.ZP.rotationDegrees(turn[2]));
        RenderContext.translate(0F, 0F, recoil[2]);

        HbmAnimations.applyRelevantTransformation("Gun");
        ResourceManager.am180.renderPart("Gun");
        if(silenced) ResourceManager.am180.renderPart("Silencer");

        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("Trigger");
        ResourceManager.am180.renderPart("Trigger");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, bolt[2]);
        HbmAnimations.applyRelevantTransformation("Bolt");
        ResourceManager.am180.renderPart("Bolt");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(magazine[0], magazine[1], magazine[2]);

        RenderContext.translate(0F, 2.0625F, 3.75F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(magTurn[0]));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(magTurn[2]));
        RenderContext.translate(0F, -2.0625F, -3.75F);

        RenderContext.translate(0F, 2.3125F, 1.5F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(magSpin[0]));
        RenderContext.translate(0F, -2.3125F, -1.5F);

        HbmAnimations.applyRelevantTransformation("Mag");

        /* Die Trommel dreht sich mit dem Fuellstand. */
        RenderContext.pushPose();
        int ammo = Minecraft.getInstance().player == null ? 0
                : gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack)
                        .getAmount(stack, Minecraft.getInstance().player.getInventory());
        RenderContext.translate(0F, 0F, 1.5F);
        RenderContext.mulPose(Axis.YN.rotationDegrees(ammo / 59F * 360F));
        RenderContext.translate(0F, 0F, -1.5F);
        ResourceManager.am180.renderPart("Mag");
        RenderContext.popPose();

        ResourceManager.am180.renderPart("MagPlate");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.875F, silenced ? 17F : 13F);
        RenderContext.mulPose(Axis.ZN.rotationDegrees(turn[2]));
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.25F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.875F, silenced ? 16.75F : 12F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        float flashScale = silenced ? 0.5F : 0.75F;
        RenderContext.scale(flashScale, flashScale, flashScale);
        renderMuzzleFlash(buffer, gun.lastShot[0], silenced ? 75 : 50, silenced ? 5F : 7.5F);
        RenderContext.popPose();
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.AM180_TEX);
        ResourceManager.am180.renderPart("Gun");
        ResourceManager.am180.renderPart("Trigger");
        ResourceManager.am180.renderPart("Bolt");
        ResourceManager.am180.renderPart("Mag");
        ResourceManager.am180.renderPart("MagPlate");
        if(hasSilencer(stack)) ResourceManager.am180.renderPart("Silencer");

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

            long shot;
            float shotRand = 0;

            if(living == Minecraft.getInstance().player) {
                GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
                shot = gun.lastShot[0];
                shotRand = gun.shotRand;
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
                if(shot < 0) return;
            }

            boolean silenced = hasSilencer(stack);

            RenderContext.pushPose();
            RenderContext.translate(0F, 1.875F, silenced ? 16.75F : 12F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
            float flashScale = silenced ? 0.5F : 0.75F;
            RenderContext.scale(flashScale, flashScale, flashScale);
            renderMuzzleFlash(buffer, shot, silenced ? 75 : 50, silenced ? 5F : 7.5F);
            RenderContext.popPose();
        }
    }

    public boolean hasSilencer(ItemStack stack) {
        return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SILENCER);
    }
}
