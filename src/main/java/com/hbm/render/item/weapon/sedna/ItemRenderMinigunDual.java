package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderMinigunDual.
 *
 * Zwei Miniguns nebeneinander. Die linke Haelfte zeichnet das Teil GunDual, die rechte Gun --
 * sie sind spiegelbildlich, nicht dasselbe Teil zweimal. Die Laufkraenze drehen sich
 * gegenlaeufig, jeder auf seinen eigenen Animationsschienen (Index 0 und 1).
 *
 * ABWEICHUNG: in dritter Person zeichnet der Port nur die Waffe in der Haupthand. Die zweite
 * haengt im Original an RenderPlayerEvent.Specials, einer Ereignisreihe, die es in 1.21 nicht
 * mehr gibt; der Port hat die zugehoerige Schicht noch gar nicht. Aus demselben Grund fehlt das
 * Gegenstandsbild des Originals, das beide Waffen ueber Kreuz zeigt -- hier steht die eine.
 *
 * ABWEICHUNG: setupModTable und renderModTable sind NICHT UEBERNOMMEN -- der Waffentisch des
 * Ports zeigt statt der Waffe ihr Gegenstandsbild.
 */
public class ItemRenderMinigunDual extends ItemRenderWeaponBase {

    @Override public boolean isAkimbo(LivingEntity entity) { return true; }

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.33);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        float offset = 0.8F;

        for(int side = -1; side <= 1; side += 2) {

            int index = side == -1 ? 0 : 1;

            RenderContext.pushPose();
            RenderSystem.setShaderTexture(0, ResourceManager.MINIGUN_DUAL_TEX);

            standardAimingTransform(stack, -2.75F * offset * side, -1.75F * offset, 2.5F * offset, 0F, 0F, 0F);

            float scale = 0.375F;
            RenderContext.scale(scale, scale, scale);

            float[] equip = HbmAnimations.getRelevantTransformation("EQUIP", index);
            float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL", index);
            float[] rotate = HbmAnimations.getRelevantTransformation("ROTATE", index);

            RenderContext.translate(0F, 3F, -6F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
            RenderContext.translate(0F, -3F, 6F);

            RenderContext.translate(0F, 0F, recoil[2]);

            ResourceManager.minigun.renderPart(index == 0 ? "GunDual" : "Gun");

            RenderContext.pushPose();
            RenderContext.mulPose(Axis.ZP.rotationDegrees(rotate[2] * side));
            ResourceManager.minigun.renderPart("Barrels");
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, 12F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(gun.shotRand * 90F));
            RenderContext.scale(1.5F, 1.5F, 1.5F);
            renderMuzzleFlash(buffer, gun.lastShot[index], 50, 7.5F);
            RenderContext.popPose();

            RenderContext.popPose();
        }
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(-1F, -3.5F, 8F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 0.875F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.25F, 0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.MINIGUN_DUAL_TEX);
        ResourceManager.minigun.renderPart("Gun");
        ResourceManager.minigun.renderPart("Barrels");

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

            long shot;
            float shotRand = 0F;

            if(living == Minecraft.getInstance().player) {
                GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
                shot = gun.lastShot[1];
                shotRand = gun.shotRand;
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
                if(shot < 0) return;
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, 12.25F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.translate(0F, 0.5F, 0F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(shotRand * 90F));
            RenderContext.scale(1.5F, 1.5F, 1.5F);
            renderMuzzleFlash(buffer, shot, 50, 7.5F);
            RenderContext.popPose();
        }
    }
}
