package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.impl.NI4NIGunItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderNI4NI.
 *
 * DIE VIER MUENZEN AM LAUF SIND DIE ANZEIGE. Sie erscheinen von hinten nach vorn, eine je
 * geladener Muenze, und schlagen von gruen nach gelb um, sobald der Vorrat ueber die Haelfte
 * geht. Eine Zahl braucht die Waffe deshalb nicht.
 *
 * NICHT UEBERNOMMEN: die drei frei einstellbaren Farben des Originals und die
 * Graustufentextur, auf der sie liegen. Sie haengen an ICustomizable und einem Befehl, den
 * der Port nicht hat -- gezeichnet wird deshalb immer die gewoehnliche Textur.
 */
public class ItemRenderNI4NI extends ItemRenderWeaponBase {

    @Override protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 1F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.0F * offset, -1F * offset, 1F * offset,
                0, -5F / 8F, 0.125F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.NI4NI_TEX);

        float scale = 0.3125F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] drum = HbmAnimations.getRelevantTransformation("DRUM");

        RenderContext.translate(0F, 0F, -2.25F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 0F, 2.25F);

        RenderContext.translate(0F, -1F, -6F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[0]));
        RenderContext.translate(0F, 1F, 6F);

        ResourceManager.ni4ni.renderPart("FrameDark");
        ResourceManager.ni4ni.renderPart("Grip");
        ResourceManager.ni4ni.renderPart("FrameLight");

        /* Die Trommel dreht sich um ihre eigene Mitte, nicht um den Ursprung des Modells. */
        RenderContext.pushPose();
        RenderContext.translate(0F, 1.1875F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(drum[2]));
        RenderContext.translate(0F, -1.1875F, 0F);
        ResourceManager.ni4ni.renderPart("Cylinder");
        ResourceManager.ni4ni.renderPart("CylinderHighlights");
        RenderContext.popPose();

        ResourceManager.ni4ni.renderPart("Barrel");

        zeichneMuenzen(stack);

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.75F, 4F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90 * gun.shotRand));
        RenderContext.scale(0.125F, 0.125F, 0.125F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 7.5F);
        RenderContext.popPose();
    }

    /**
     * Die vier Muenzen. Der Vorrat des Ports geht nur bis vier, weil die beiden Module des
     * Originals fehlen; die Grenzen fuenf bis acht des Originals stehen trotzdem hier, weil
     * sie nichts kosten und mit den Modulen sofort stimmen wuerden.
     */
    private void zeichneMuenzen(ItemStack stack) {

        int vorrat = NI4NIGunItem.getCoinCount(stack);

        muenze(vorrat, 3, 7, "Coin1");
        muenze(vorrat, 2, 6, "Coin2");
        muenze(vorrat, 1, 5, "Coin3");
        muenze(vorrat, 0, 4, "Coin4");

        RenderContext.setColor(1F, 1F, 1F, 1F);
    }

    private void muenze(int vorrat, int abSichtbar, int abGelb, String teil) {
        if(vorrat <= abSichtbar) return;
        RenderContext.setColor(vorrat > abGelb ? 1F : 0F, 1F, 0F, 1F);
        ResourceManager.ni4ni.renderPart(teil);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, 0.25F, 2F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 2F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.5F, 0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.NI4NI_TEX);
        ResourceManager.ni4ni.renderPart("FrameDark");
        ResourceManager.ni4ni.renderPart("Grip");
        ResourceManager.ni4ni.renderPart("FrameLight");
        ResourceManager.ni4ni.renderPart("Cylinder");
        ResourceManager.ni4ni.renderPart("CylinderHighlights");
        ResourceManager.ni4ni.renderPart("Barrel");
        zeichneMuenzen(stack);

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {
            long shot;
            float shotRand = 0;
            if(living == Minecraft.getInstance().player) {
                GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
                shot = gun.lastShot[0];
                shotRand = gun.shotRand;
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
                if(shot < 0) return;
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 0.75F, 4F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90 * shotRand));
            RenderContext.scale(0.125F, 0.125F, 0.125F);
            renderMuzzleFlash(buffer, shot, 75, 7.5F);
            RenderContext.popPose();
        }
    }
}
