package com.hbm.render.item.weapon.sedna;

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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderQuadro.
 *
 * Der Quadro. Zwei Modellteile: das Gehaeuse und der Raketenblock. Beim Nachladen kippt das
 * ganze Geraet zur Seite (RELOAD_ROTATE), und der Block wird dabei herausgezogen und wieder
 * hineingeschoben (RELOAD_PUSH) -- deshalb bekommt er eine eigene Verschiebung.
 *
 * DIE SCHRIFT AM VISIER dreht sich, solange der Schuetze voll gezielt hat: ">> <<" in Cyan,
 * eine halbe Umdrehung je Sekunde. Sie leuchtet aus sich selbst, sonst waere sie im Dunkeln
 * nicht zu lesen. Im Original steht dafuer ein Block aus Lichtkarten-Rechnerei; der Port hat
 * dafuer FullBright.
 *
 * ABWEICHUNG: setupModTable ist nicht uebernommen -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild. Das ist im Port ueberall so.
 */
public class ItemRenderQuadro extends ItemRenderWeaponBase {

    /** Die Schrift am Visier. Im Original heisst dieses Feld ebenso. */
    private static final String LABEL = ">> <<";

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -2.5F * offset, -3.5F * offset, 2.5F * offset,
                -1.5F * offset, -3F * offset, 2.5F * offset);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();

        float scale = 1.75F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] reloadPush = HbmAnimations.getRelevantTransformation("RELOAD_PUSH");
        float[] reloadRotate = HbmAnimations.getRelevantTransformation("RELOAD_ROTATE");

        RenderContext.translate(0F, -1F, -1F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 1F, 1F);

        RenderContext.translate(0F, 0F, recoil[2]);

        RenderContext.translate(0F, -1F, -1F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(reloadRotate[2]));
        RenderContext.translate(0F, 1F, 1F);

        RenderSystem.setShaderTexture(0, ResourceManager.QUADRO_TEX);
        ResourceManager.quadro.renderPart("Launcher");

        RenderContext.pushPose();
        RenderContext.translate(0F, 2F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(reloadPush[1] * 30F));
        RenderContext.translate(0F, -3F, reloadPush[0] * 3F);
        RenderSystem.setShaderTexture(0, ResourceManager.QUADRO_ROCKET_TEX);
        ResourceManager.quadro.renderPart("Rockets");
        RenderContext.popPose();

        if(GunBaseNTItem.prevAimingProgress >= 1F && GunBaseNTItem.aimingProgress >= 1F) {
            this.renderLabel(buffer);
        }

        RenderContext.pushPose();
        RenderContext.translate(-1F, 0.75F, 6.5F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 150, 7.5F);
        RenderContext.popPose();
    }

    /** Die drehende Schrift am Visier. */
    private void renderLabel(MultiBufferSource buffer) {

        Font font = Minecraft.getInstance().font;
        float textScale = 0.04F;

        FullBright.enable();
        RenderContext.pushPose();
        RenderContext.translate(-0.375F, 2.25F, 0.875F);
        RenderContext.mulPose(Axis.YN.rotationDegrees(180F + (System.currentTimeMillis() / 2L) % 360L));
        RenderContext.translate(-(font.width(LABEL) / 2F) * textScale, 0F, 0F);
        RenderContext.scale(textScale, -textScale, textScale);
        font.drawInBatch(LABEL, 0F, 0F, 0x00ffff, false,
                RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
        RenderContext.popPose();
        FullBright.disable();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 7.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, -0.5F, -0.25F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 4.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0F, -1F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.QUADRO_TEX);
        /* Nur das Gehaeuse: der Raketenblock gehoert zur Nachladebewegung, und die gibt es
         * ausserhalb der ersten Person nicht -- das Original laesst ihn hier ebenfalls weg. */
        ResourceManager.quadro.renderPart("Launcher");

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
        RenderContext.translate(0F, 0.75F, 2F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(buffer, shot, 150, 7.5F);
        RenderContext.popPose();
    }
}
