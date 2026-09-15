package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.RenderContext;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Vec3NT;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderMK108.
 *
 * Die MK 108. Sie ist die aufwendigste Waffendarstellung des Ports, und das liegt am Gurt: er
 * besteht nicht aus einem Modellteil, sondern aus neun Gliedern, deren Lage der Renderer bei
 * jedem Bild neu ausrechnet.
 *
 * DIE RECHNUNG geht so: ein Vektor der Laenge eines Gliedes wandert von Glied zu Glied und wird
 * dabei jedes Mal um den Winkel des naechsten gedreht. Zwei Winkelreihen stehen fest -- eine fuer
 * den eingelegten Gurt, eine fuer den herausgezogenen -- und zwischen beiden wird nach dem
 * Nachladefortschritt geblendet. Beim Durchladen ruecken die Glieder um eine Stelle weiter; dafuer
 * wird jedes Glied zwischen seiner und der naechsten Lage geblendet.
 *
 * WELCHE GLIEDER EINE GRANATE TRAGEN, entscheidet der Magazinstand: der Gurt laeuft von hinten
 * nach vorn leer.
 *
 * BEIM BETRACHTEN wirft der Schuetze drei Granaten nacheinander in die Luft und faengt sie wieder
 * auf. Sie haengen an eigenen Kanaelen (GRENH, GRENV, GRENS je dreimal) und werden nur gezeichnet,
 * solange sie nicht aus dem Bild geflogen sind.
 *
 * ABWEICHUNG: setupModTable ist NICHT UEBERNOMMEN -- der Waffentisch des Ports zeigt statt der
 * Waffe ihr Gegenstandsbild.
 */
public class ItemRenderMK108 extends ItemRenderWeaponBase {

    /** Die Winkel der neun Gurtglieder -- einmal eingelegt, einmal herausgezogen. */
    private static final float[] ANGLES_LOADED = { 0, 0, -5, 0, -5, 60, 45, -10, 0 };
    private static final float[] ANGLES_UNLOADED = { 0, -30, -60, -45, -45, 0, 0, 0, 0 };

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2.5F : -0.25F; }

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
                -1F * offset, -1.5F * offset, 2.5F * offset,
                -0.75F, -0.75F, 1.5F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        RenderSystem.setShaderTexture(0, ResourceManager.MK108_TEX);

        float scale = 0.375F;
        RenderContext.scale(scale, scale, scale);

        /* Welche Kanaele die laufende Animation hat, entscheidet, was ueberhaupt zu zeichnen ist. */
        boolean doesYeet = hasBus("GRENH1");
        boolean doesCycle = hasBus("CYCLE");
        boolean reloading = hasBus("BELT");
        boolean useShellCount = hasBus("SHELLS");

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] cycle = HbmAnimations.getRelevantTransformation("CYCLE");
        float[] barrel = HbmAnimations.getRelevantTransformation("BARREL");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] lid = HbmAnimations.getRelevantTransformation("LID");
        float[] belt = HbmAnimations.getRelevantTransformation("BELT");
        float[] drum = HbmAnimations.getRelevantTransformation("DRUM");
        float[] lift = HbmAnimations.getRelevantTransformation("LIFT");
        float[] shellCount = HbmAnimations.getRelevantTransformation("SHELLS");

        if(doesYeet) {

            float[][] horizontal = {
                    HbmAnimations.getRelevantTransformation("GRENH1"),
                    HbmAnimations.getRelevantTransformation("GRENH2"),
                    HbmAnimations.getRelevantTransformation("GRENH3") };
            float[][] vertical = {
                    HbmAnimations.getRelevantTransformation("GRENV1"),
                    HbmAnimations.getRelevantTransformation("GRENV2"),
                    HbmAnimations.getRelevantTransformation("GRENV3") };
            float[][] spin = {
                    HbmAnimations.getRelevantTransformation("GRENS1"),
                    HbmAnimations.getRelevantTransformation("GRENS2"),
                    HbmAnimations.getRelevantTransformation("GRENS3") };

            for(int i = 0; i < 3; i++) {

                /* Weiter als vier Bloecke nach hinten ist die Granate aus dem Bild. */
                if(horizontal[i][0] <= -4) continue;

                RenderContext.pushPose();
                RenderContext.translate(horizontal[i][0], vertical[i][1], 0F);
                RenderContext.translate(0F, 0F, -2.3125F);
                RenderContext.mulPose(Axis.XN.rotationDegrees(90F));
                RenderContext.mulPose(Axis.YN.rotationDegrees(spin[i][0]));
                RenderContext.translate(0F, 0F, 2.3125F);
                ResourceManager.mk108.renderPart("Grenade");
                RenderContext.popPose();
            }
        }

        RenderContext.translate(0F, -1F, -8F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(equip[0]));
        RenderContext.translate(0F, 1F, 8F);

        RenderContext.translate(0F, 1F, -4F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lift[0]));
        RenderContext.translate(0F, -1F, 4F);

        RenderContext.translate(0F, 0F, recoil[2]);

        ResourceManager.mk108.renderPart("Gun");

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, barrel[2] * 2F);
        ResourceManager.mk108.renderPart("Barrel");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0.6875F, -1F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(lid[0]));
        RenderContext.translate(0F, -0.6875F, 1F);
        ResourceManager.mk108.renderPart("Lid");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(drum[0], drum[1], drum[2]);
        ResourceManager.mk108.renderPart("Drum");

        float reloadProgress = !reloading ? 1F : belt[0];
        float cycleProgress = !doesCycle ? 1F : cycle[0];
        float[][] shells = layOutBelt(reloadProgress);

        int shellAmount = useShellCount ? (int) shellCount[0]
                : gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack)
                        .getAmount(stack, Minecraft.getInstance().player.getInventory());

        for(int i = 0; i < shells.length - 1; i++) {
            float[] prev = shells[i];
            float[] next = shells[i + 1];
            renderShell(prev[0], next[0], prev[1], next[1], prev[2], next[2], shells.length - i < shellAmount + 2, cycleProgress);
        }
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(0F, 0F, 8.125F);
        RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
        RenderContext.mulPose(Axis.XP.rotationDegrees(90F * gun.shotRand));
        renderMuzzleFlash(buffer, gun.lastShot[0], 50, 5F);
        RenderContext.popPose();
    }

    private static boolean hasBus(String bus) {
        HbmAnimations.Animation anim = HbmAnimations.getRelevantAnim(0);
        return anim != null && anim.animation.getBus(bus) != null;
    }

    /**
     * Legt die neun Gurtglieder aus. Ein Vektor der Gliedlaenge wandert von Glied zu Glied und
     * wird jedes Mal um den Winkel des naechsten gedreht -- so folgt der Gurt seiner eigenen
     * Kruemmung, statt an festen Punkten zu haengen.
     */
    private static float[][] layOutBelt(float reloadProgress) {

        float p = 0.0625F;
        float x = p * 22F;
        float y = p * -46F;
        float angle = 0F;
        Vec3NT vec = new Vec3NT(0, 0.53125, 0);

        float[][] shells = new float[ANGLES_LOADED.length][3];

        for(int i = 0; i < ANGLES_LOADED.length; i++) {
            shells[i][0] = x;
            shells[i][1] = y;
            shells[i][2] = angle - 90F;
            float delta = BobMathUtil.interp(ANGLES_UNLOADED[i], ANGLES_LOADED[i], reloadProgress);
            angle += delta;
            vec.rotateAroundZDeg(-delta);
            x += (float) vec.xCoord;
            y += (float) vec.yCoord;
        }

        return shells;
    }

    /** Beim Durchladen ruecken die Glieder weiter -- jedes wird zur Lage des naechsten geblendet. */
    public static void renderShell(float x0, float x1, float y0, float y1, float rot0, float rot1, boolean shell, float interp) {
        renderShell(BobMathUtil.interp(x0, x1, interp), BobMathUtil.interp(y0, y1, interp), BobMathUtil.interp(rot0, rot1, interp), shell);
    }

    public static void renderShell(float x, float y, float rot, boolean shell) {
        RenderContext.pushPose();
        RenderContext.translate(x, y, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(rot));
        ResourceManager.mk108.renderPart("Belt");
        if(shell) ResourceManager.mk108.renderPart("Grenade");
        RenderContext.popPose();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 2.0F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(1F, -2.5F, 4F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.375F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0F, 0.5F, 0.25F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {

        RenderSystem.setShaderTexture(0, ResourceManager.MK108_TEX);
        ResourceManager.mk108.renderPart("Gun");
        ResourceManager.mk108.renderPart("Barrel");
        ResourceManager.mk108.renderPart("Lid");
        ResourceManager.mk108.renderPart("Drum");

        /* Ausserhalb der Hand haengt der Gurt voll und in Ruhe. */
        RenderContext.pushPose();
        float[][] shells = layOutBelt(1F);
        for(int i = 0; i < shells.length - 1; i++) {
            float[] prev = shells[i];
            float[] next = shells[i + 1];
            renderShell(prev[0], next[0], prev[1], next[1], prev[2], next[2], true, 0F);
        }
        RenderContext.popPose();

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
            RenderContext.translate(0F, 0F, 8.125F);
            RenderContext.mulPose(Axis.YP.rotationDegrees(90F));
            RenderContext.mulPose(Axis.XP.rotationDegrees(90F * shotRand));
            renderMuzzleFlash(buffer, shot, 50, 5F);
            RenderContext.popPose();
        }
    }
}
