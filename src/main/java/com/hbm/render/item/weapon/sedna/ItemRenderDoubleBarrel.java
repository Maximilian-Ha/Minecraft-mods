package com.hbm.render.item.weapon.sedna;

import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderDoubleBarrel.
 *
 * Die Doppelflinte. Zum Nachladen klappen die Laeufe nach oben (BARREL), nachdem der Hebel
 * (LEVER) umgelegt wurde; die Huelsen fliegen heraus (SHELLS) und drehen sich dabei einmal um
 * sich selbst (SHELL_FLIP). BUCKLE ist der Ruck des Kolbens beim Schuss.
 *
 * ABGESAEGT faellt der lange Lauf weg und es bleibt nur der kurze -- der Heilige Drache ist von
 * Haus aus abgesaegt, an der gewoehnlichen Flinte ist es Aufsatz.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderDoubleBarrel extends ItemRenderWeaponBase {

    protected final ResourceLocation texture;

    public ItemRenderDoubleBarrel(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.33);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.25F * offset, -1F * offset, 2F * offset,
                0F, -2F / 8F, 1F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, this.texture);

        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] turn = HbmAnimations.getRelevantTransformation("TURN");
        float[] barrel = HbmAnimations.getRelevantTransformation("BARREL");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] shells = HbmAnimations.getRelevantTransformation("SHELLS");
        float[] shellFlip = HbmAnimations.getRelevantTransformation("SHELL_FLIP");
        float[] lever = HbmAnimations.getRelevantTransformation("LEVER");
        float[] buckle = HbmAnimations.getRelevantTransformation("BUCKLE");
        float[] noAmmo = HbmAnimations.getRelevantTransformation("NO_AMMO");

        /* Der Ruecklauf verschiebt die Waffe UND kippt sie -- daher derselbe Wert zweimal. */
        RenderContext.translate(recoil[0] * 3F, recoil[1], recoil[2]);
        RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[2] * 10F));

        RenderContext.translate(0F, 0F, -4F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 0F, 4F);

        RenderContext.translate(0F, 0F, -4F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(turn[1]));
        RenderContext.translate(0F, 0F, 4F);

        RenderContext.translate(0F, 0F, -4F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 0F, 4F);

        ResourceManager.double_barrel.renderPart("Stock");

        RenderContext.pushPose();

        RenderContext.translate(0F, -0.4375F, -0.875F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(barrel[0]));
        RenderContext.translate(0F, 0.4375F, 0.875F);

        ResourceManager.double_barrel.renderPart("BarrelShort");
        if(!isSawedOff(stack)) ResourceManager.double_barrel.renderPart("Barrel");

        RenderContext.pushPose();
        RenderContext.translate(0.75F, 0F, -0.6875F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(buckle[1]));
        RenderContext.translate(-0.75F, 0F, 0.6875F);
        ResourceManager.double_barrel.renderPart("Buckle");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(-0.3125F, 0.3125F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(lever[2]));
        RenderContext.translate(0.3125F, -0.3125F, 0F);
        ResourceManager.double_barrel.renderPart("Lever");
        RenderContext.popPose();

        if(noAmmo[0] == 0) {
            RenderContext.pushPose();
            RenderContext.translate(shells[0], shells[1], shells[2]);
            RenderContext.translate(0F, 0F, -1F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(shellFlip[0]));
            RenderContext.translate(0F, 0F, 1F);
            ResourceManager.double_barrel.renderPart("Shells");
            RenderContext.popPose();
        }

        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, 8F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(2F, 2F, 2F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 5F);
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 1F, 3F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);

        /* Abgesaegt ist die Waffe kuerzer und darf deshalb groesser gezeichnet werden. */
        boolean sawed = isSawedOff(stack);
        float scale = sawed ? 2F : 1.375F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(sawed ? -2F : 0F, 0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, this.texture);
        ResourceManager.double_barrel.renderPart("Stock");
        ResourceManager.double_barrel.renderPart("BarrelShort");
        if(!isSawedOff(stack)) ResourceManager.double_barrel.renderPart("Barrel");
        ResourceManager.double_barrel.renderPart("Buckle");
        ResourceManager.double_barrel.renderPart("Lever");
        ResourceManager.double_barrel.renderPart("Shells");

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

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
            RenderContext.translate(0F, 0F, 8F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
            RenderContext.scale(2F, 2F, 2F);
            renderMuzzleFlash(buffer, shot, 75, 5F);
            RenderContext.popPose();
        }
    }

    /** Der Heilige Drache ist von Haus aus abgesaegt; an der gewoehnlichen Flinte ist es Aufsatz. */
    public boolean isSawedOff(ItemStack stack) {
        return stack.getItem() == NtmItems.GUN_DOUBLE_BARREL_SACRED_DRAGON.get()
                || XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SAWED_OFF);
    }
}
