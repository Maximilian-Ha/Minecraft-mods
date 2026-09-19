package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderDrill.
 *
 * Der Bergbaubohrer. Sieben Teile, und fast alle bewegen sich: die beiden Bohrkoepfe drehen
 * gegeneinander, die drei Kolben laufen um ein Drittel versetzt auf und ab, und die Anzeige
 * zeigt den Tankinhalt.
 *
 * DIE KOLBEN LAUFEN FUENFMAL SO SCHNELL wie der Bohrkopf und sind um je 120 Grad versetzt --
 * daher der Sinus mit 2pi/3 und 4pi/3. Das ist keine Zierde: man sieht an ihnen, dass der
 * Motor laeuft, auch wenn der Bohrkopf verdeckt ist.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderDrill extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 0F : -0.5F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.25F * offset, -1.75F * offset, 1.75F * offset,
                -1F * offset, -1.75F * offset, 1.25F * offset);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.DRILL_TEX);

        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        IMagazine<?> mag = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
        float fuellung = (float) mag.getAmount(stack, null) / (float) mag.getCapacity(stack);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] deploy = HbmAnimations.getRelevantTransformation("DEPLOY");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] spin = HbmAnimations.getRelevantTransformation("SPIN");

        RenderContext.mulPose(Axis.YP.rotationDegrees(15F * (1F - deploy[0] * 0.5F)));
        RenderContext.mulPose(Axis.XN.rotationDegrees(10F * (1F - deploy[0] * 0.5F)));

        RenderContext.translate(0F, 2F, -6F);
        RenderContext.mulPose(Axis.YN.rotationDegrees(equip[0] * 45F));
        RenderContext.mulPose(Axis.XN.rotationDegrees(equip[0] * 20F));
        RenderContext.translate(0F, -2F, 6F);

        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 0F, deploy[0]);

        ResourceManager.drill.renderPart("Base");

        /* Die Anzeige steht schraeg am Gehaeuse: hinkippen, drehen, zurueckkippen. */
        RenderContext.pushPose();
        RenderContext.translate(1F, 2.0625F, -1.75F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(45F));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-135F + fuellung * 270F));
        RenderContext.mulPose(Axis.XN.rotationDegrees(45F));
        RenderContext.translate(-1F, -2.0625F, 1.75F);
        ResourceManager.drill.renderPart("Gauge");
        RenderContext.popPose();

        float drehung = spin[0];
        double kolben = drehung * 5D;

        kolbenTeil(kolben, 0D, "Piston1");
        kolbenTeil(kolben, Math.PI * 2D / 3D, "Piston2");
        kolbenTeil(kolben, Math.PI * 4D / 3D, "Piston3");

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.ZN.rotationDegrees(drehung));
        ResourceManager.drill.renderPart("DrillBack");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.mulPose(Axis.ZP.rotationDegrees(drehung));
        ResourceManager.drill.renderPart("DrillFront");
        RenderContext.popPose();
    }

    private static void kolbenTeil(double drehung, double versatz, String teil) {
        RenderContext.pushPose();
        RenderContext.translate(0F, (float) (Math.sin(drehung * Math.PI / 180D + versatz) * 0.125D - 0.125D), 0F);
        ResourceManager.drill.renderPart(teil);
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 2.25F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(1F, -2F, 6F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.25F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.5F, 0F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {
        RenderSystem.setShaderTexture(0, ResourceManager.DRILL_TEX);
        ResourceManager.drill.renderAll();
    }
}
