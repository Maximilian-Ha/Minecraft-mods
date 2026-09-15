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
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderG3.
 *
 * Die G3. Sie ist die wandelbarste Waffe des Spiels: Schaft, Schalldaempfer, Zielfernrohr und
 * zwei Kunststoffschaefte lassen sich frei zusammenstellen, und jede Kombination zeichnet sich
 * anders. Was der Renderer zeigt, haengt deshalb durchweg an der Aufsatzliste -- nur die Zebra
 * bringt Daempfer und Optik als Waffe selbst mit.
 *
 * MIT OPTIK ganz angelegt wird die Waffe gar nicht gezeichnet: dann liegt das Bild der Optik
 * ueber dem ganzen Schirm.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderG3 extends ItemRenderWeaponBase {

    public final ResourceLocation texture;

    public ItemRenderG3(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * (isScoped(stack) ? 0.66 : 0.33));
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        boolean isScoped = this.isScoped(stack);
        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.25F * offset, -1F * offset, 2.75F * offset,
                0F, isScoped ? (-5.53125F / 8F) : (-3.5625F / 8F), isScoped ? 1.46875F : 1.75F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        boolean isScoped = this.isScoped(stack);
        if(isScoped && GunBaseNTItem.prevAimingProgress == 1 && GunBaseNTItem.aimingProgress == 1) return;

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, getTexture(stack));

        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] mag = HbmAnimations.getRelevantTransformation("MAG");
        float[] speen = HbmAnimations.getRelevantTransformation("SPEEN");
        float[] bolt = HbmAnimations.getRelevantTransformation("BOLT");
        float[] plug = HbmAnimations.getRelevantTransformation("PLUG");
        float[] handle = HbmAnimations.getRelevantTransformation("HANDLE");
        float[] bullet = HbmAnimations.getRelevantTransformation("BULLET");

        RenderContext.translate(0F, -2F, -6F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 2F, 6F);

        RenderContext.translate(0F, 0F, -4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, 0F, 4F);

        RenderContext.translate(0F, 0F, recoil[2]);

        boolean silenced = hasSilencer(stack);

        ResourceManager.g3.renderPart("Rifle");
        if(hasStock(stack)) ResourceManager.g3.renderPart("Stock");
        if(!silenced) ResourceManager.g3.renderPart("Flash_Hider");
        ResourceManager.g3.renderPart("Trigger");

        /* Das Magazin haengt beim Nachladen an MAG und dreht sich beim Betrachten um SPEEN. */
        RenderContext.pushPose();
        RenderContext.translate(mag[0], mag[1], mag[2]);
        RenderContext.translate(0F, -1.75F, -0.5F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(speen[2]));
        RenderContext.mulPose(Axis.YP.rotationDegrees(speen[1]));
        RenderContext.translate(0F, 1.75F, 0.5F);
        ResourceManager.g3.renderPart("Magazine");
        if(bullet[0] == 0) ResourceManager.g3.renderPart("Bullet");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, bolt[2]);
        ResourceManager.g3.renderPart("Guide_And_Bolt");
        RenderContext.popPose();

        /* Der Spanngriff sitzt schraeg am Rohr: erst in die Waagerechte drehen, dann kippen. */
        RenderContext.pushPose();
        RenderContext.translate(0F, 0.625F, plug[2]);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(handle[2]));
        RenderContext.translate(0F, -0.625F, 0F);
        ResourceManager.g3.renderPart("Plug");

        RenderContext.translate(0F, 0.625F, 5.25F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(22.5F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(handle[1]));
        RenderContext.mulPose(Axis.ZP.rotationDegrees(-22.5F));
        RenderContext.translate(0F, -0.625F, -5.25F);
        ResourceManager.g3.renderPart("Handle");
        RenderContext.popPose();

        /* Der Waehlhebel steht auf Dauerfeuer, wenn der Feuermodus 0 ist. */
        RenderContext.pushPose();
        RenderContext.translate(0F, -0.875F, -3.5F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-30F * (1 - GunBaseNTItem.getMode(stack, 0))));
        RenderContext.translate(0F, 0.875F, 3.5F);
        ResourceManager.g3.renderPart("Selector");
        RenderContext.popPose();

        if(silenced || isScoped) {
            RenderSystem.setShaderTexture(0, ResourceManager.G3_ATTACHMENTS_TEX);
            if(silenced) ResourceManager.g3.renderPart("Silencer");
            if(isScoped) ResourceManager.g3.renderPart("Scope");
        }

        if(!silenced) {

            float smokeScale = 0.75F;

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, 13F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.scale(smokeScale, smokeScale, smokeScale);
            renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.5F);
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, 12F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.scale(0.75F, 0.75F, 0.75F);
            renderGapFlash(buffer, gun.lastShot[0]);
            RenderContext.popPose();
        }
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        RenderContext.translate(0F, 2F, 4F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        boolean silenced = hasSilencer(stack);

        /* Ohne Schaft ist die Waffe kuerzer und darf deshalb groesser gezeichnet werden. */
        if(hasStock(stack)) {
            float scale = 0.875F;
            RenderContext.scale(scale, scale, scale);
            RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
            RenderContext.mulPose(Axis.YP.rotationDegrees(silenced ? 50F : 45F));
            RenderContext.translate(silenced ? 0.75F : -0.5F, 0.5F, 0F);
        } else {
            float scale = 1.125F;
            RenderContext.scale(scale, scale, scale);
            RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
            RenderContext.mulPose(Axis.YP.rotationDegrees(silenced ? 55F : 45F));
            RenderContext.translate(2.5F, 0.5F, 0F);
        }
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        boolean silenced = hasSilencer(stack);
        boolean isScoped = this.isScoped(stack);

        RenderSystem.setShaderTexture(0, getTexture(stack));
        ResourceManager.g3.renderPart("Rifle");
        if(hasStock(stack)) ResourceManager.g3.renderPart("Stock");
        ResourceManager.g3.renderPart("Magazine");
        if(!silenced) ResourceManager.g3.renderPart("Flash_Hider");
        ResourceManager.g3.renderPart("Guide_And_Bolt");
        ResourceManager.g3.renderPart("Handle");
        ResourceManager.g3.renderPart("Trigger");

        RenderContext.pushPose();
        RenderContext.translate(0F, -0.875F, -3.5F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-30F));
        RenderContext.translate(0F, 0.875F, 3.5F);
        ResourceManager.g3.renderPart("Selector");
        RenderContext.popPose();

        if(silenced || isScoped) {
            RenderSystem.setShaderTexture(0, ResourceManager.G3_ATTACHMENTS_TEX);
            if(silenced) ResourceManager.g3.renderPart("Silencer");
            if(isScoped) ResourceManager.g3.renderPart("Scope");
        }

        if(silenced) return;

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

            long shot;

            if(living == Minecraft.getInstance().player) {
                GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
                shot = gun.lastShot[0];
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(living, (long) -1);
                if(shot < 0) return;
            }

            RenderContext.pushPose();
            RenderContext.translate(0F, 0F, 12F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.scale(0.75F, 0.75F, 0.75F);
            renderGapFlash(buffer, shot);
            RenderContext.popPose();
        }
    }

    public boolean hasStock(ItemStack stack) {
        return !XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_NO_STOCK);
    }

    /** Die Zebra traegt ihren Daempfer von Haus aus; an der gewoehnlichen G3 ist er Aufsatz. */
    public boolean hasSilencer(ItemStack stack) {
        return stack.getItem() == NtmItems.GUN_G3_ZEBRA.get()
                || XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SILENCER);
    }

    public boolean isScoped(ItemStack stack) {
        return stack.getItem() == NtmItems.GUN_G3_ZEBRA.get()
                || XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SCOPE);
    }

    /** Der Kunststoffschaft ueberschreibt die Textur der ganzen Waffe, nicht nur des Schafts. */
    public ResourceLocation getTexture(ItemStack stack) {
        if(XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_FURNITURE_GREEN)) return ResourceManager.G3_GREEN_TEX;
        if(XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_FURNITURE_BLACK)) return ResourceManager.G3_BLACK_TEX;
        return this.texture;
    }
}
