package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderAberrator.
 *
 * Der Aberrator. Beweglich sind Visier (SIGHT), Magazin (MAG und MAGROLL), Verschluss (SLIDE),
 * Hahn (HAMMER) und die Patrone darin (BULLET); ROLL rollt die ganze Waffe zum Nachladen zur
 * Seite, RISE hebt sie beim Ziehen an.
 *
 * UM DIE MUENDUNG stehen sechzehn goldene Schwerter im Kreis und drehen sich unablaessig. Beim
 * Anlegen klappen sie nach vorn und ziehen sich zusammen -- das ist das Zeichen der Waffe, und
 * ohne den Kranz waere sie eine gewoehnliche Pistole.
 *
 * ABWEICHUNG: das Original holt sich das Schwertbild ueber den Gegenstandsatlas der alten
 * Icon-Verwaltung. Hier steht derselbe Atlas, nur ueber den Modellverwalter von 1.21 --
 * dasselbe Bild, anderer Weg.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderAberrator extends ItemRenderWeaponBase {

    private static final ResourceLocation SWORD = ResourceLocation.withDefaultNamespace("item/golden_sword");
    private static final int SWORD_COUNT = 16;

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        double aimingProgress = Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress);
        return fov * (1 - aimingProgress * 0.33);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 1F);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.0F * offset, -1.25F * offset, 1.25F * offset,
                0F, -5.25F / 8F, 0.125F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.ABERRATOR_TEX);

        float scale = 0.25F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] rise = HbmAnimations.getRelevantTransformation("RISE");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] slide = HbmAnimations.getRelevantTransformation("SLIDE");
        float[] bullet = HbmAnimations.getRelevantTransformation("BULLET");
        float[] hammer = HbmAnimations.getRelevantTransformation("HAMMER");
        float[] roll = HbmAnimations.getRelevantTransformation("ROLL");
        float[] mag = HbmAnimations.getRelevantTransformation("MAG");
        float[] magroll = HbmAnimations.getRelevantTransformation("MAGROLL");
        float[] sight = HbmAnimations.getRelevantTransformation("SIGHT");

        RenderContext.translate(0F, rise[1], 0F);

        RenderContext.translate(0F, 1F, -2.25F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, -1F, 2.25F);

        RenderContext.translate(0F, -1F, -4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(recoil[0]));
        RenderContext.translate(0F, 1F, 4F);

        RenderContext.translate(0F, 1F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(roll[2]));
        RenderContext.translate(0F, -1F, 0F);

        ResourceManager.aberrator.renderPart("Gun");

        RenderContext.pushPose();
        RenderContext.translate(0F, 2.4375F, -1.9375F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(sight[0]));
        RenderContext.translate(0F, -2.4375F, 1.9375F);
        ResourceManager.aberrator.renderPart("Sight");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(mag[0], mag[1], mag[2]);
        RenderContext.translate(0F, 1F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(magroll[2]));
        RenderContext.translate(0F, -1F, 0F);
        ResourceManager.aberrator.renderPart("Magazine");
        RenderContext.translate(bullet[0], bullet[1], bullet[2]);
        ResourceManager.aberrator.renderPart("Bullet");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, slide[2]);
        ResourceManager.aberrator.renderPart("Slide");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 1.25F, -3.625F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(-45F + hammer[0]));
        RenderContext.translate(0F, -1.25F, 3.625F);
        ResourceManager.aberrator.renderPart("Hammer");
        RenderContext.popPose();

        float smokeScale = 0.5F;

        RenderContext.pushPose();
        RenderContext.translate(0F, 2F, 4F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(recoil[0]));
        RenderContext.mulPose(Axis.ZN.rotationDegrees(roll[2]));
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(buffer, gun.getConfig(stack, 0).smokeNodes, 0.5F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 2F, 4F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        RenderContext.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(buffer, gun.lastShot[0], 75, 7.5F);
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 2F, -1.5F);
        RenderContext.scale(0.5F, 0.5F, 0.5F);
        renderFireball(buffer, gun.lastShot[0]);
        RenderContext.popPose();

        /* Der Schwertkranz sitzt vor der Muendung und folgt allen Bewegungen der Waffe zurueck. */
        RenderContext.pushPose();
        RenderContext.translate(0F, 2F, 4.5F);
        RenderContext.mulPose(Axis.ZN.rotationDegrees(roll[2]));
        RenderContext.mulPose(Axis.XN.rotationDegrees(recoil[0]));
        RenderContext.mulPose(Axis.XN.rotationDegrees(equip[0]));
        RenderContext.mulPose(Axis.ZP.rotationDegrees((float) (System.currentTimeMillis() / 50D % 360D)));

        float aimingProgress = Math.min(1F, Mth.lerp(partialTick, GunBaseNTItem.prevAimingProgress, GunBaseNTItem.aimingProgress) * 2F);
        renderSwordRing(buffer, aimingProgress);
        RenderContext.popPose();
    }

    /**
     * Sechzehn Schwerter im Kreis. Beim Anlegen klappen sie um neunzig Grad nach vorn und ruecken
     * einen Block weiter nach aussen -- so geben sie den Blick durch die Waffe frei.
     */
    private static void renderSwordRing(MultiBufferSource buffer, float aimingProgress) {

        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager()
                .getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(SWORD);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS));

        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        for(int i = 0; i < SWORD_COUNT; i++) {

            RenderContext.pushPose();
            RenderContext.translate(0F, -1.5F - aimingProgress, 0F);
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * aimingProgress));
            RenderContext.mulPose(Axis.ZP.rotationDegrees(-45F));

            Matrix4f matrix = RenderContext.poseStack().last().pose();
            int light = RenderContext.light();

            quad(consumer, matrix, light, -0.5F, -0.5F, maxU, maxV);
            quad(consumer, matrix, light, 0.5F, -0.5F, minU, maxV);
            quad(consumer, matrix, light, 0.5F, 0.5F, minU, minV);
            quad(consumer, matrix, light, -0.5F, 0.5F, maxU, minV);

            RenderContext.popPose();
            RenderContext.mulPose(Axis.ZP.rotationDegrees(360F / SWORD_COUNT));
        }
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, int light, float x, float y, float u, float v) {
        consumer.addVertex(matrix, x, y, -0.5F)
                .setColor(0xFFFFFFFF)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0F, 1F, 0F);
    }

    /**
     * Der Feuerball an der Muendung. Zwei ineinanderstehende Flaechen, die in anderthalb Zehntel
     * Sekunden auseinanderlaufen -- dieselbe Rauchfahne wie beim Muendungsfeuer, nur quer.
     */
    public static void renderFireball(MultiBufferSource buffer, long lastShot) {

        int duration = 150;
        if(System.currentTimeMillis() - lastShot >= duration) return;

        VertexConsumer consumer = buffer.getBuffer(FLASH.apply(FLASH_PLUME_TEX));
        Matrix4f matrix = RenderContext.poseStack().last().pose();

        float fire = (System.currentTimeMillis() - lastShot) / (float) duration;
        float height = 5F * fire;
        float length = 10F * fire;
        float offset = 1F * fire;
        float lengthOffset = -1.125F;

        int color = 0xFFFFFFFF;

        consumer.addVertex(matrix, height, -offset, 0F).setUv(0, 1).setColor(color);
        consumer.addVertex(matrix, -height, -offset, 0F).setUv(1, 1).setColor(color);
        consumer.addVertex(matrix, -height, -offset + length, -lengthOffset).setUv(1, 0).setColor(color);
        consumer.addVertex(matrix, height, -offset + length, -lengthOffset).setUv(0, 0).setColor(color);

        consumer.addVertex(matrix, height, -offset, 0F).setUv(0, 1).setColor(color);
        consumer.addVertex(matrix, -height, -offset, 0F).setUv(1, 1).setColor(color);
        consumer.addVertex(matrix, -height, -offset + length, lengthOffset).setUv(1, 0).setColor(color);
        consumer.addVertex(matrix, height, -offset + length, lengthOffset).setUv(0, 0).setColor(color);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        RenderContext.translate(0F, -1F, 4F);
        float scale = 1.5F;
        RenderContext.scale(scale, scale, scale);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 2.5F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(-0.5F, -1F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.ABERRATOR_TEX);
        ResourceManager.aberrator.renderPart("Gun");
        ResourceManager.aberrator.renderPart("Hammer");
        ResourceManager.aberrator.renderPart("Magazine");
        ResourceManager.aberrator.renderPart("Slide");
        ResourceManager.aberrator.renderPart("Sight");

        if(living != null && (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {

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
            RenderContext.translate(0F, 2F, 4F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
            RenderContext.scale(0.75F, 0.75F, 0.75F);
            renderMuzzleFlash(buffer, shot, 75, 7.5F);
            RenderContext.popPose();

            RenderContext.pushPose();
            RenderContext.translate(0F, 2F, -1.5F);
            RenderContext.scale(0.5F, 0.5F, 0.5F);
            renderFireball(buffer, shot);
            RenderContext.popPose();
        }
    }
}
