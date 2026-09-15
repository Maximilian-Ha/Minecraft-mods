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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderMAS36.
 *
 * Die MAS-36. Der Verschluss dreht sich auf (BOLT_TURN) und wird zurueckgezogen (BOLT_PULL); der
 * Schaft klappt beim Ziehen aus (STOCK). Nachgeladen wird mit einem Ladestreifen, der ueber
 * SHOW_CLIP eingeblendet und mit CLIP von oben eingeschoben wird; BULLETS sind die Patronen darin.
 *
 * ABWEICHUNG: das Original schneidet die Patronen des Ladestreifens beim Herunterdruecken mit
 * einer Schnittebene (glClipPlane) ab, damit sie im Gehaeuse verschwinden statt hindurchzustossen.
 * Schnittebenen gibt es im festen Renderweg von 1.21 nicht mehr. Hier bleiben die Patronen ganz;
 * der Tiefenpuffer verdeckt sie ueber den groessten Teil der Bewegung ohnehin.
 *
 * ABWEICHUNG: setupModTable und renderModTable sind NICHT UEBERNOMMEN -- der Waffentisch des
 * Ports zeigt statt der Waffe ihr Gegenstandsbild.
 */
public class ItemRenderMAS36 extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * (isScoped(stack) ? 0.66 : 0.33));
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;

        if(isScoped(stack)) {
            standardAimingTransform(stack,
                    -1.5F * offset, -1.25F * offset, 1.75F * offset,
                    -0.2F, -5.875F / 8F, 1.125F);
        } else {
            standardAimingTransform(stack,
                    -1.5F * offset, -1.25F * offset, 1.75F * offset,
                    0F, -4.6825F / 8F, 0.75F);
        }
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        boolean isScoped = isScoped(stack);
        if(isScoped && GunBaseNTItem.prevAimingProgress == 1 && GunBaseNTItem.aimingProgress == 1) return;

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.MAS36_TEX);

        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] stock = HbmAnimations.getRelevantTransformation("STOCK");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] boltTurn = HbmAnimations.getRelevantTransformation("BOLT_TURN");
        float[] boltPull = HbmAnimations.getRelevantTransformation("BOLT_PULL");
        float[] bullet = HbmAnimations.getRelevantTransformation("BULLET");
        float[] showClip = HbmAnimations.getRelevantTransformation("SHOW_CLIP");
        float[] clip = HbmAnimations.getRelevantTransformation("CLIP");
        float[] bullets = HbmAnimations.getRelevantTransformation("BULLETS");
        float[] stab = HbmAnimations.getRelevantTransformation("STAB");

        RenderContext.translate(0F, -3F, -3F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 3F, 3F);

        RenderContext.translate(stab[0], stab[1], stab[2]);
        RenderContext.translate(0F, 0F, recoil[2]);

        ResourceManager.mas36.renderPart("Gun");
        if(hasBayonet(stack)) ResourceManager.mas36.renderPart("Bayonet");

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.3125F, -2.125F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(stock[0]));
        RenderContext.translate(0F, -0.3125F, 2.125F);
        ResourceManager.mas36.renderPart("Stock");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.0625F * 18.5F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(boltTurn[2]));
        RenderContext.translate(0F, 0.0625F * -18.5F, 0F);
        RenderContext.translate(0F, 0F, boltPull[2]);
        ResourceManager.mas36.renderPart("Bolt");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(bullet[0], bullet[1], bullet[2]);
        ResourceManager.mas36.renderPart("Bullet");
        RenderContext.popPose();

        if(isScoped) ResourceManager.mas36.renderPart("Scope");

        if(showClip[0] != 0) {

            RenderContext.pushPose();
            RenderContext.translate(clip[0], clip[1], clip[2]);
            ResourceManager.mas36.renderPart("Clip");
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(bullets[0], bullets[1], bullets[2]);
            ResourceManager.mas36.renderPart("Bullets");
            RenderContext.popPose();
        }

        float smokeScale = 0.25F;

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.125F, 8F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 1F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1F, 8F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(0.5F, 0.5F, 0.5F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 7.5F);
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 0.5F, 3F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.5F, 0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        boolean inHand = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        RenderSystem.setShaderTexture(0, ResourceManager.MAS36_TEX);
        ResourceManager.mas36.renderPart("Gun");
        ResourceManager.mas36.renderPart("Stock");
        ResourceManager.mas36.renderPart("Bolt");
        if(isScoped(stack)) ResourceManager.mas36.renderPart("Scope");

        /* Ausserhalb der Hand steht das Bajonett versetzt -- sonst ragt es aus dem Bild. */
        if(!inHand) RenderContext.translate(0F, -1F, -6F);
        if(hasBayonet(stack)) ResourceManager.mas36.renderPart("Bayonet");

        if(living != null && inHand) {

            long shot;
            float shotRand = 0F;

            if(living == Minecraft.getInstance().player) {
                GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
                shot = gun.lastShot[0];
                shotRand = gun.shotRand;
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
                if(shot < 0) return;
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 1F, 8F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
            RenderContext.scale(0.5F, 0.5F, 0.5F);
            renderMuzzleFlash(buffer, shot, 75, 10F);
            RenderContext.popPose();
        }
    }

    public boolean isScoped(ItemStack stack) {
        return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SCOPE);
    }

    public boolean hasBayonet(ItemStack stack) {
        return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_MAS_BAYONET);
    }
}
