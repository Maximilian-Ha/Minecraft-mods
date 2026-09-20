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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderLAG.
 *
 * Die LAG. Sie ist die einzige Waffe des Ports, deren Modell NICHT nach ihr heisst: das
 * Original zeichnet sie mit mike_hawk.obj und legt lag.png darueber. Beides steht so im
 * Original, und beides ist uebernommen -- ein anderer Name waere eine stille Abweichung.
 *
 * FUENF TEILE BEWEGEN SICH EINZELN, und ihre Bewegungen stehen nicht im Quelltext, sondern in
 * models/animations/lag.json: Griff, Schlitten, Hahn, Magazin und die sichtbare Patrone. Die
 * Patrone wird nur gezeichnet, wenn ueberhaupt eine geladen ist.
 *
 * DER HAHN HAT EINEN EIGENEN DREHPUNKT, der vor der Bewegung gesetzt wird -- er sitzt hinten
 * oben am Schlitten, nicht im Ursprung des Modells.
 *
 * ABWEICHUNG: setupModTable ist nicht uebernommen -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild. Das ist im Port ueberall so.
 */
public class ItemRenderLAG extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

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
                -1.5F * offset, -1F * offset, 1.5F * offset,
                0F, -3.375F / 8F, 0.5F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();

        RenderSystem.setShaderTexture(0, ResourceManager.LAG_TEX);
        float scale = 0.25F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] addTrans = HbmAnimations.getRelevantTransformation("ADD_TRANS");
        float[] addRot = HbmAnimations.getRelevantTransformation("ADD_ROT");

        RenderContext.translate(4F, -4F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-equip[0]));
        RenderContext.translate(-4F, 4F, 0F);

        RenderContext.translate(addTrans[0], addTrans[1], addTrans[2]);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(addRot[2]));
        RenderContext.mulPose(Axis.YP.rotationDegrees(addRot[1]));

        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("Grip");
        ResourceManager.mike_hawk.renderPart("Grip");

        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("Slide");
        ResourceManager.mike_hawk.renderPart("Slide");
        RenderContext.popPose();

        RenderContext.pushPose();
        /* Der Drehpunkt des Hahns: hinten oben am Schlitten, um fuenfundzwanzig Grad gekippt. */
        RenderContext.translate(3.125F, 0.125F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-25F));
        RenderContext.translate(-3.125F, -0.125F, 0F);
        HbmAnimations.applyRelevantTransformation("Hammer");
        ResourceManager.mike_hawk.renderPart("Hammer");
        RenderContext.popPose();

        /*
         * Die sichtbare Patrone im Lauf. getAmount liest die Zahl aus dem Gegenstand selbst,
         * der Behaelter geht gar nicht ein; deshalb steht hier wie im Original null.
         */
        if(gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, null) > 0) {
            RenderContext.pushPose();
            HbmAnimations.applyRelevantTransformation("Bullet");
            ResourceManager.mike_hawk.renderPart("Bullet");
            RenderContext.popPose();
        }

        RenderContext.pushPose();
        HbmAnimations.applyRelevantTransformation("Magazine");
        ResourceManager.mike_hawk.renderPart("Magazine");
        RenderContext.popPose();

        float smokeScale = 0.5F;
        RenderContext.pushPose();
        RenderContext.translate(-10.25F, 1F, 0F);
        RenderContext.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.5F);
        RenderContext.popPose();

        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(-10.25F, 1F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 7.5F);
        RenderContext.popPose();
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
        RenderContext.translate(2.5F, 1F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderSystem.setShaderTexture(0, ResourceManager.LAG_TEX);

        /* Ausserhalb der ersten Person steht die Waffe still: Griff, Schlitten, Hahn -- mehr
         * nicht. Magazin und Patrone gehoeren zur Nachladebewegung, und die gibt es hier nicht;
         * das Original laesst sie hier ebenfalls weg. */
        ResourceManager.mike_hawk.renderPart("Grip");
        ResourceManager.mike_hawk.renderPart("Slide");
        ResourceManager.mike_hawk.renderPart("Hammer");

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
        RenderContext.translate(-10.25F, 1F, 0F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
        renderMuzzleFlash(buffer, shot, 75, 7.5F);
        RenderContext.popPose();
    }
}
