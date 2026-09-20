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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderMissileLauncher.
 *
 * Der Raketenwerfer. Vier Modellteile, und drei davon bewegen sich beim Nachladen: der
 * Vorderteil klappt auf (OPEN), das Rohr faehrt heraus (BARREL), und die Rakete wird
 * eingeschoben (MISSILE). Weil Rohr und Rakete IM Vorderteil sitzen, haengen ihre
 * Verschiebungen an dessen Klappbewegung -- deshalb steht das alles in einer gemeinsamen
 * Klammer.
 *
 * DIE SCHRIFT AM VISIER flackert, solange der Schuetze voll gezielt hat: "AUTO" in Rot, dessen
 * Helligkeit jeden Bild neu ausgewuerfelt wird. Sie leuchtet aus sich selbst, sonst waere sie
 * im Dunkeln nicht zu lesen; im Original steht dafuer ein Block aus Lichtkarten-Rechnerei, der
 * Port hat FullBright.
 *
 * ABWEICHUNG: setupModTable ist nicht uebernommen -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild. Das ist im Port ueberall so.
 */
public class ItemRenderMissileLauncher extends ItemRenderWeaponBase {

    /** Die Schrift am Visier. Im Original heisst dieses Feld ebenso. */
    private static final String LABEL = "AUTO";

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.5F * offset, -1.25F * offset, 0.5F * offset,
                -1F * offset, -1.25F * offset, 0F * offset);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();

        RenderSystem.setShaderTexture(0, ResourceManager.MISSILE_LAUNCHER_TEX);
        float scale = 0.5F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] barrel = HbmAnimations.getRelevantTransformation("BARREL");
        float[] open = HbmAnimations.getRelevantTransformation("OPEN");
        float[] missile = HbmAnimations.getRelevantTransformation("MISSILE");

        RenderContext.translate(0F, -2F, -2F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 2F, 2F);

        ResourceManager.missile_launcher.renderPart("Launcher");

        RenderContext.pushPose();

        RenderContext.translate(0F, 0.25F, 1.6875F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(open[0]));
        RenderContext.translate(0F, -0.25F, -1.6875F);

        ResourceManager.missile_launcher.renderPart("Front");

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, barrel[2]);
        ResourceManager.missile_launcher.renderPart("Barrel");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(missile[0], missile[1], missile[2]);
        ResourceManager.missile_launcher.renderPart("Missile");
        RenderContext.popPose();

        RenderContext.popPose();

        if(GunBaseNTItem.prevAimingProgress >= 1F && GunBaseNTItem.aimingProgress >= 1F) {
            this.renderLabel(buffer);
        }

        RenderContext.pushPose();
        RenderContext.translate(0F, 1F, 6.75F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 7.5F);
        RenderContext.popPose();
    }

    /** Die flackernde Schrift am Visier. */
    private void renderLabel(MultiBufferSource buffer) {

        Font font = Minecraft.getInstance().font;
        float textScale = 0.04F;

        /*
         * Die Helligkeit wird jeden Bild neu gezogen, zwischen 0,7 und 1,0 -- daher das
         * Flackern. Gruen und Blau bleiben bei null, es ist ein reines Rot.
         */
        float variance = 0.7F + Minecraft.getInstance().player.getRandom().nextFloat() * 0.3F;
        int color = ((int) (variance * 255F)) << 16;

        FullBright.enable();
        RenderContext.pushPose();
        RenderContext.translate(0.9375F, 2.25F, -0.5625F + (font.width(LABEL) / 2F) * textScale);
        RenderContext.scale(textScale, -textScale, textScale);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        font.drawInBatch(LABEL, 0F, 0F, color, false,
                RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
        RenderContext.popPose();
        FullBright.disable();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 2.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, -0.5F, -2F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0F, -0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();

        RenderSystem.setShaderTexture(0, ResourceManager.MISSILE_LAUNCHER_TEX);
        ResourceManager.missile_launcher.renderPart("Launcher");
        ResourceManager.missile_launcher.renderPart("Barrel");
        ResourceManager.missile_launcher.renderPart("Front");

        /*
         * Die Rakete steckt sichtbar im Rohr -- ist keine geladen, fehlt sie auch im Bild.
         * getAmount liest die Zahl aus dem Gegenstand selbst, der Behaelter geht gar nicht ein;
         * deshalb steht hier wie im Original null.
         */
        if(gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, null) > 0) {
            ResourceManager.missile_launcher.renderPart("Missile");
        }

        if(living == null) return;
        if(displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) return;

        long shot;
        float shotRand = 0F;

        if(living == Minecraft.getInstance().player) {
            shot = gun.lastShot[0];
            shotRand = gun.shotRand;
        } else {
            shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
            if(shot < 0) return;
        }

        RenderContext.pushPose();
        RenderContext.translate(0F, 1F, 6.75F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(buffer, shot, 75, 7.5F);
        RenderContext.popPose();
    }
}
