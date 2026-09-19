package com.hbm.render.item.weapon.sedna;

import com.hbm.items.NtmItems;
import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderLaserPistol.
 *
 * Die Laserpistole. Beim Nachladen klappt der Riegel zur Seite und die Batterie faellt heraus;
 * geschossen wird ohne Muendungsfeuer, nur mit einem Blitz in der Farbe des Strahls.
 *
 * DIESELBE KLASSE TRAEGT ALLE DREI PISTOLEN. Sie unterscheiden sich in der Textur und darin,
 * was mitgezeichnet wird: die Pew Pew traegt aufgeklebte Kondensatoren, die Morning Glory
 * schiesst gruen statt rot.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderLaserPistol extends ItemRenderWeaponBase {

    public final ResourceLocation texture;

    public ItemRenderLaserPistol(ResourceLocation texture) {
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
                -1.75F * offset, -2F * offset, 2.75F * offset,
                0F, -10F / 8F, 1.25F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, this.texture);

        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] latch = HbmAnimations.getRelevantTransformation("LATCH");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] jolt = HbmAnimations.getRelevantTransformation("JOLT");
        float[] battery = HbmAnimations.getRelevantTransformation("BATTERY");
        float[] swirl = HbmAnimations.getRelevantTransformation("SWIRL");

        RenderContext.translate(0F, -1F, -6F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 1F, 6F);

        RenderContext.translate(0F, 2F, -2F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, -2F, 2F);

        RenderContext.translate(0F, -1F, -1F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(swirl[0]));
        RenderContext.translate(0F, 1F, 1F);

        RenderContext.translate(0F, 0F, recoil[2]);
        RenderContext.translate(jolt[0], jolt[1], jolt[2]);

        ResourceManager.laser_pistol.renderPart("Gun");
        if(hasCapacitors(stack)) {
            ResourceManager.laser_pistol.renderPart("Capacitors");
            ResourceManager.laser_pistol.renderPart("Tape");
        }

        RenderContext.pushPose();
        RenderContext.translate(1.125F, 0F, -1.9125F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(latch[1]));
        RenderContext.translate(-1.125F, 0F, 1.9125F);
        ResourceManager.laser_pistol.renderPart("Latch");
        RenderContext.translate(battery[0], battery[1], battery[2]);
        ResourceManager.laser_pistol.renderPart("Battery");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 2F, 4.75F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        renderLaserFlash(buffer, gun.lastShot[0], 150, 1.5F, hasEmerald(stack) ? 0x008000 : 0xFF0000);
        RenderContext.translate(0F, 0F, -0.25F);
        renderLaserFlash(buffer, gun.lastShot[0], 150, 0.75F, hasEmerald(stack) ? 0x80FF00 : 0xFF8000);
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 1.25F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(0F, -0.5F, 1F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.75F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0F, -0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {
        RenderSystem.setShaderTexture(0, this.texture);
        ResourceManager.laser_pistol.renderPart("Gun");
        ResourceManager.laser_pistol.renderPart("Latch");
        ResourceManager.laser_pistol.renderPart("Battery");
        if(hasCapacitors(stack)) {
            ResourceManager.laser_pistol.renderPart("Capacitors");
            ResourceManager.laser_pistol.renderPart("Tape");
        }
    }

    /** Die Pew Pew traegt aufgeklebte Kondensatoren -- die anderen beiden nicht. */
    public boolean hasCapacitors(ItemStack stack) {
        return stack.getItem() == NtmItems.GUN_LASER_PISTOL_PEW_PEW.get();
    }

    /** Die Morning Glory schiesst gruen. */
    public boolean hasEmerald(ItemStack stack) {
        return stack.getItem() == NtmItems.GUN_LASER_PISTOL_MORNING_GLORY.get();
    }
}
