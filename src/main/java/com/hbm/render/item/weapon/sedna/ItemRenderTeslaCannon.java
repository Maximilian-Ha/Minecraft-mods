package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.blockentity.RenderPlushie;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderTeslaCannon.
 *
 * Die Teslakanone. Vier Teile, aber der Witz steckt im fuenften, das gar nicht zur Waffe
 * gehoert: dem Yomi-Plueschtier, das oben draufsitzt und beim Begutachten gedrueckt wird.
 *
 * DAS ZAHNRAD ZEIGT DIE MUNITION. Die Waffe hat kein Magazin -- sie frisst aus dem Rucksack --
 * und kann deshalb keinen Fuellstand anzeigen. Stattdessen steckt der Zeichner so viele
 * Kondensatoren auf das Zahnrad, wie im Rucksack liegen, hoechstens acht: die ersten vier je
 * 22,5 Grad weiter um die Achse, die uebrigen in einer Reihe nach rechts. Beim Schuss dreht
 * sich das Zahnrad um genau diese 22,5 Grad weiter, und die aufgesteckten Kondensatoren mit.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderTeslaCannon extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        float aimingProgress = GunBaseNTItem.prevAimingProgress + (GunBaseNTItem.aimingProgress - GunBaseNTItem.prevAimingProgress) * (float) partialTick;
        return fov * (1D - aimingProgress * 0.33D);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.75F * offset, -0.5F * offset, 1.75F * offset,
                -1.3125F * offset, 0F * offset, -0.5F * offset);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.TESLA_CANNON_TEX);

        float scale = 0.75F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] cycle = HbmAnimations.getRelevantTransformation("CYCLE");
        float[] count = HbmAnimations.getRelevantTransformation("COUNT");
        float[] yomi = HbmAnimations.getRelevantTransformation("YOMI");
        float[] squeeze = HbmAnimations.getRelevantTransformation("SQUEEZE");

        RenderContext.translate(0F, -2F, -2F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 2F, 2F);

        RenderContext.translate(0F, 0F, recoil[2]);
        RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[2] * 2F));

        /*
         * Die Zahl auf dem COUNT-Bus steht nur waehrend der Schussbewegung an; sonst ist sie
         * null. Deshalb das Maximum aus ihr und dem, was gerade wirklich im Rucksack liegt --
         * sonst verschwaende der Kranz zwischen zwei Schuessen.
         */
        int amount = Math.max((int) count[0], gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack)
                .getAmount(stack, Minecraft.getInstance().player.getInventory()));

        ResourceManager.tesla_cannon.renderPart("Gun");
        ResourceManager.tesla_cannon.renderPart("Extension");

        float zahnrad = cycle[2];

        RenderContext.pushPose();
        RenderContext.translate(0F, -1.625F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(zahnrad));
        RenderContext.translate(0F, 1.625F, 0F);
        ResourceManager.tesla_cannon.renderPart("Cog");
        RenderContext.popPose();

        RenderContext.pushPose();

        RenderContext.translate(0F, -1.625F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(zahnrad));
        RenderContext.translate(0F, 1.625F, 0F);

        for(int i = 0; i < Math.min(amount, 8); i++) {
            ResourceManager.tesla_cannon.renderPart("Capacitor");

            if(i < 4) {
                RenderContext.translate(0F, -1.625F, 0F);
                RenderContext.mulPose(Axis.ZP.rotationDegrees(-22.5F));
                RenderContext.translate(0F, 1.625F, 0F);
            } else {
                /* Ab dem fuenften geht es geradeaus weiter: erst die Drehung zuruecknehmen. */
                if(i == 4) {
                    RenderContext.translate(0F, -1.625F, 0F);
                    RenderContext.mulPose(Axis.ZP.rotationDegrees(-zahnrad));
                    RenderContext.translate(0F, 1.625F, 0F);
                    RenderContext.translate(-zahnrad * 0.5F / 22.5F, 0F, 0F);
                }
                RenderContext.translate(0.5F, 0F, 0F);
            }
        }
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(yomi[0], yomi[1], yomi[2]);
        RenderContext.mulPose(Axis.YP.rotationDegrees(135F));
        RenderContext.scale(squeeze[0], squeeze[1], squeeze[2]);
        RenderSystem.setShaderTexture(0, RenderPlushie.YOMI_TEX);
        RenderPlushie.yomiModel.renderAll();
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 2.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 1.5F, 1F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.25F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0F, 0.5F, 0F);
    }

    /**
     * Ausserhalb der ersten Person steht der Kranz voll: zehn Kondensatoren, wie im Original.
     * Der Rucksack ist hier nicht zu erreichen, und eine leere Kanone im Regal saehe aus wie
     * ein halbes Modell.
     */
    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.TESLA_CANNON_TEX);

        ResourceManager.tesla_cannon.renderPart("Gun");
        ResourceManager.tesla_cannon.renderPart("Extension");
        ResourceManager.tesla_cannon.renderPart("Cog");

        RenderContext.pushPose();
        for(int i = 0; i < 10; i++) {
            ResourceManager.tesla_cannon.renderPart("Capacitor");

            if(i < 4) {
                RenderContext.translate(0F, -1.625F, 0F);
                RenderContext.mulPose(Axis.ZP.rotationDegrees(-22.5F));
                RenderContext.translate(0F, 1.625F, 0F);
            } else {
                RenderContext.translate(0.5F, 0F, 0F);
            }
        }
        RenderContext.popPose();
    }
}
