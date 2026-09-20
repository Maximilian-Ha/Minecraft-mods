package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.factory.XFactoryTool;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderChargeThrower.
 *
 * Der Ladungswerfer. Sechs Modellteile, aber nie alle zugleich -- WAS IM ROHR STECKT, WIRD
 * GEZEICHNET: der Haken, die kleine Moerserladung oder die grosse (Moerser plus Oomph). Ist
 * das Rohr leer und wird auch nicht gerade geladen, sieht man gar nichts darin.
 *
 * Das Zielfernrohr ist ein Aufsatz. Beim Blick durch das Fernrohr wird es selbst NICHT
 * gezeichnet -- man sieht ja hindurch -- und die Waffe rueckt stattdessen ganz nah heran.
 *
 * ABWEICHUNG: setupModTable ist nicht uebernommen -- der Waffentisch des Ports zeigt statt
 * der Waffe ihr Gegenstandsbild.
 */
public class ItemRenderChargeThrower extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 0F : -0.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        float aimingProgress = GunBaseNTItem.prevAimingProgress + (GunBaseNTItem.aimingProgress - GunBaseNTItem.prevAimingProgress) * (float) partialTick;
        return fov * (1D - aimingProgress * (isScoped(stack) ? 0.66D : 0.33D));
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        float zoom = 0.5F;

        if(isScoped(stack)) standardAimingTransform(stack,
                -1.5F * offset, -1.25F * offset, 3.5F * offset,
                -0.15625F, -6.5F / 8F, 1.6875F);
        else standardAimingTransform(stack,
                -1.5F * offset, -1.25F * offset, 3.5F * offset,
                -1.5F * zoom, -1.25F * zoom, 3.5F * zoom);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        boolean durchsFernrohr = this.isScoped(stack)
                && GunBaseNTItem.aimingProgress == 1F && GunBaseNTItem.prevAimingProgress == 1F;

        MagazineFullReload mag = (MagazineFullReload) gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);

        if(durchsFernrohr) {
            float scale = 3.5F;
            RenderContext.scale(scale, scale, scale);
            RenderContext.translate(-0.5F, -1.5F, -4F);
        } else {
            float scale = 0.5F;
            RenderContext.scale(scale, scale, scale);
        }

        /* Waehrend des Ladens steckt die Ladung schon im Bild, obwohl das Magazin noch leer ist. */
        boolean laedt = HbmAnimations.getRelevantAnim(0) != null
                && HbmAnimations.getRelevantAnim(0).animation.getBus("AMMO") != null;

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] raise = HbmAnimations.getRelevantTransformation("RAISE");
        float[] ammo = HbmAnimations.getRelevantTransformation("AMMO");
        float[] twist = HbmAnimations.getRelevantTransformation("TWIST");
        float[] turn = HbmAnimations.getRelevantTransformation("TURN");
        float[] roll = HbmAnimations.getRelevantTransformation("ROLL");

        RenderContext.translate(0F, 0F, -7F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 0F, 7F);

        RenderContext.translate(0F, -7F, 4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(raise[0]));
        RenderContext.translate(0F, 7F, -4F);

        RenderContext.translate(recoil[0], recoil[1], recoil[2]);

        RenderContext.translate(0F, 0F, -2F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(turn[1]));
        RenderContext.translate(0F, 0F, 2F);
        RenderContext.translate(0F, -1F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(roll[2]));
        RenderContext.translate(0F, 1F, 0F);

        RenderSystem.setShaderTexture(0, ResourceManager.CHARGE_THROWER_TEX);
        ResourceManager.charge_thrower.renderPart("Gun");
        if(this.isScoped(stack) && !durchsFernrohr) ResourceManager.charge_thrower.renderPart("Scope");

        if(mag.getAmount(stack, null) <= 0 && !laedt) return;

        RenderContext.translate(ammo[0], ammo[1], ammo[2]);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(twist[2]));

        Object geladen = mag.getType(stack, null);

        if(geladen == XFactoryTool.ct_hook) {
            RenderSystem.setShaderTexture(0, ResourceManager.CHARGE_THROWER_HOOK_TEX);
            ResourceManager.charge_thrower.renderPart("Hook");
        }
        if(geladen == XFactoryTool.ct_mortar) {
            RenderSystem.setShaderTexture(0, ResourceManager.CHARGE_THROWER_MORTAR_TEX);
            ResourceManager.charge_thrower.renderPart("Mortar");
        }
        if(geladen == XFactoryTool.ct_mortar_charge) {
            RenderSystem.setShaderTexture(0, ResourceManager.CHARGE_THROWER_MORTAR_TEX);
            ResourceManager.charge_thrower.renderPart("Mortar");
            ResourceManager.charge_thrower.renderPart("Oomph");
        }
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0.75F, 1F, 4F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.25F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0F, 0F, -0.625F);
    }

    /**
     * Ausserhalb der ersten Person steht das Rohr leer. Das Magazin steht im Gegenstand, und
     * an einen am Boden liegenden kommt der Zeichner hier nicht sinnvoll heran -- das Original
     * fragt es ab, sieht dort aber dieselbe Attrappe.
     */
    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {
        RenderSystem.setShaderTexture(0, ResourceManager.CHARGE_THROWER_TEX);
        ResourceManager.charge_thrower.renderPart("Gun");
        if(this.isScoped(stack)) ResourceManager.charge_thrower.renderPart("Scope");
    }

    private boolean isScoped(ItemStack stack) {
        return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SCOPE);
    }
}
