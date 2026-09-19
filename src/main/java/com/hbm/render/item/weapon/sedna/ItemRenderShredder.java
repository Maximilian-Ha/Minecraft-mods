package com.hbm.render.item.weapon.sedna;

import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderShredder.
 *
 * Die Autoschrotflinte. Drei Teile: das Gehaeuse, das Trommelmagazin und der Gurt darin. Der
 * Bus MAG verschiebt die Trommel, SPEEN dreht sie beim Pruefen einmal um sich selbst, CYCLE
 * dreht den Gurt beim Schuss um einen Platz weiter.
 *
 * DAS SCHILD AUF DEM GEHAEUSE: ueber dem Lauf steht "[> <]". Die gewoehnliche Flinte zeigt es
 * nur ueber Kimme und Korn und in Gruen, die schoene zeigt es immer und in Rot. Es flackert --
 * die Helligkeit wuerfelt bei jedem Bild neu.
 */
public class ItemRenderShredder extends ItemRenderWeaponBase {

    /** Das Schild ueber dem Lauf. Im Original heisst das Feld ebenso und steht ebenso hier. */
    protected static final String SCHILD = "[> <]";

    protected final ResourceLocation textur;

    public ItemRenderShredder(ResourceLocation textur) {
        this.textur = textur;
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
                -1.5F * offset, -1.25F * offset, 1.5F * offset,
                0F, -6.25F / 8F, 0.5F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        float scale = 0.25F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] mag = HbmAnimations.getRelevantTransformation("MAG");
        float[] speen = HbmAnimations.getRelevantTransformation("SPEEN");
        float[] cycle = HbmAnimations.getRelevantTransformation("CYCLE");

        RenderContext.translate(0F, -2F, -6F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 2F, 6F);

        RenderContext.translate(0F, 0F, -4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 0F, 4F);

        RenderContext.translate(0F, 0F, recoil[2]);

        boolean schoen = stack.getItem() == NtmItems.GUN_AUTOSHOTGUN_SEXY.get();

        if(schoen || (GunBaseNTItem.prevAimingProgress >= 1F && GunBaseNTItem.aimingProgress >= 1F)) {

            Font font = Minecraft.getInstance().font;
            float flackern = 0.9F + Minecraft.getInstance().player.getRandom().nextFloat() * 0.1F;
            int farbe = schoen
                    ? ((int) (flackern * 255F) << 16)
                    : ((int) (flackern * 255F) << 8);

            FullBright.enable();
            RenderContext.pushPose();
            float textScale = 0.04F;
            RenderContext.translate((font.width(SCHILD) / 2F) * textScale, 3.25F, -1.75F);
            RenderContext.scale(textScale, -textScale, textScale);
            RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            font.drawInBatch(SCHILD, 0F, 0F, farbe, false,
                    RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
            RenderContext.popPose();
            FullBright.disable();
        }

        RenderSystem.setShaderTexture(0, this.textur);

        ResourceManager.shredder.renderPart("Gun");

        RenderContext.pushPose();
        RenderContext.translate(mag[0], mag[1], mag[2]);
        RenderContext.translate(0F, -1F, -0.5F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(speen[0]));
        RenderContext.translate(0F, 1F, 0.5F);
        ResourceManager.shredder.renderPart("Magazine");
        RenderContext.translate(0F, -1F, -0.5F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(cycle[2]));
        RenderContext.translate(0F, 1F, 0.5F);
        ResourceManager.shredder.renderPart("Shells");
        RenderContext.popPose();

        float rauchScale = 0.75F;
        RenderContext.pushPose();
        RenderContext.translate(0F, 1F, 7.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.scale(rauchScale, rauchScale, rauchScale);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.5F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1F, 7.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 7.5F);
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 0.5F, 4F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.25F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-1.5F, 0F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, this.textur);
        ResourceManager.shredder.renderAll();

        if(living == null) return;
        if(displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) return;

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
        RenderContext.translate(0F, 1F, 7.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(buffer, shot, 75, 7.5F);
        RenderContext.popPose();
    }
}
