package com.hbm.render.item.weapon.sedna;

import com.hbm.items.weapon.sedna.GunBaseNTItem;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.util.FullBright;
import com.hbm.render.util.RenderContext;
import com.hbm.util.EntityDamageUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.render.item.weapon.sedna.ItemRenderFolly.
 *
 * Die Folly. Fuenf Teile, und darueber liegt der eigentliche Witz der Waffe: sobald der
 * Schuetze fertig gezielt hat, faehrt auf dem Visier ein RECHNER HOCH. Erst laeuft ein
 * Selbsttest durch, dann zeichnet sich der Schriftzug "VStarOS" Buchstabe fuer Buchstabe
 * auf, und danach zeigt das Geraet laufend Ziel und Winkel an.
 *
 * Das ist keine Zierde, sondern die Bedienung: vor dem Ende des Hochfahrens schiesst die
 * Waffe nicht (siehe LAMBDA_CAN_FIRE), und die Anzeige sagt einem, wie weit es ist.
 *
 * NICHT UEBERNOMMEN: der Jingle beim Hochfahren. Das Original spielt dazu einen eigenen
 * Tonschnipsel; die Datei liegt weder im Original noch in der CE-Abspaltung, das Feld zeigt
 * dort auf einen Namen ohne Datei.
 *
 * ABWEICHUNG: setupModTable ist nicht uebernommen -- der Waffentisch des Ports zeigt statt
 * der Waffe ihr Gegenstandsbild.
 */
public class ItemRenderFolly extends ItemRenderWeaponBase {

    /** Wann das Zielen begann. Der ganze Bildschirm haengt an dieser einen Zahl. */
    public static long zielSeit;
    public static boolean zielteVorher = false;

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return GunBaseNTItem.getIsAiming(stack) ? 2F : 2.5F; }

    @Override
    public double getViewFOV(ItemStack stack, double fov, double partialTick) {
        float aimingProgress = GunBaseNTItem.prevAimingProgress + (GunBaseNTItem.aimingProgress - GunBaseNTItem.prevAimingProgress) * (float) partialTick;
        return fov * (1D - aimingProgress * 0.33D);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        RenderContext.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        float aim = 0.75F;
        standardAimingTransform(stack,
                -2.5F * offset, -1.5F * offset, 2.75F * offset,
                -2F * aim, -1F * aim, 2.25F * offset);
    }

    @Override
    public void renderFirstPerson(ItemStack stack, MultiBufferSource buffer) {

        GunBaseNTItem gun = (GunBaseNTItem) stack.getItem();
        LocalPlayer spieler = Minecraft.getInstance().player;
        RenderSystem.setShaderTexture(0, ResourceManager.FOLLY_TEX);

        float scale = 0.75F;
        RenderContext.scale(scale, scale, scale);

        float[] equip = HbmAnimations.getRelevantTransformation("EQUIP");
        float[] recoil = HbmAnimations.getRelevantTransformation("RECOIL");
        float[] load = HbmAnimations.getRelevantTransformation("LOAD");
        float[] shell = HbmAnimations.getRelevantTransformation("SHELL");
        float[] screw = HbmAnimations.getRelevantTransformation("SCREW");
        float[] breech = HbmAnimations.getRelevantTransformation("BREECH");

        RenderContext.translate(0F, 1F, -4F);
        RenderContext.mulPose(Axis.XN.rotationDegrees(equip[0]));
        RenderContext.translate(0F, -1F, 4F);

        RenderContext.translate(0F, -2F, -2F);
        RenderContext.mulPose(Axis.XP.rotationDegrees(load[0]));
        RenderContext.translate(0F, 2F, 2F);

        ResourceManager.folly.renderPart("Cannon");

        RenderContext.pushPose();
        RenderContext.translate(recoil[0], recoil[1], recoil[2]);
        ResourceManager.folly.renderPart("Barrel");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(shell[0], shell[1], shell[2]);
        ResourceManager.folly.renderPart("Shell");
        RenderContext.popPose();

        RenderContext.pushPose();
        RenderContext.translate(breech[0], breech[1], breech[2]);
        ResourceManager.folly.renderPart("Breech");
        RenderContext.translate(0F, 1F, 0F);
        RenderContext.mulPose(Axis.ZP.rotationDegrees(screw[2]));
        RenderContext.translate(0F, -1F, 0F);
        ResourceManager.folly.renderPart("Cog");
        RenderContext.popPose();

        boolean zieltJetzt = GunBaseNTItem.prevAimingProgress >= 1F && GunBaseNTItem.aimingProgress >= 1F;
        if(zieltJetzt && !zielteVorher) zielSeit = System.currentTimeMillis();
        zielteVorher = zieltJetzt;

        if(!zieltJetzt || spieler == null) return;

        Font font = Minecraft.getInstance().font;
        float flackern = 0.85F + spieler.getRandom().nextFloat() * 0.15F;
        /* Bernstein: voll rot, halb gruen, kein blau -- die Farbe einer alten Kathodenroehre. */
        int farbe = ((int) (flackern * 255F) << 16) | ((int) (flackern * 127F) << 8);

        FullBright.enable();

        /*
         * Das Fadenkreuz ist eine Schrift, kein Bild: ein "+" wenn Munition da ist, sonst der
         * Hinweis, dass keine da ist. Es erscheint erst, wenn der Rechner oben ist UND die
         * Waffe nicht gerade nachlaedt (load[0] == 0).
         */
        if(System.currentTimeMillis() - zielSeit > 5000 && load[0] == 0F) {

            IMagazine<?> mag = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
            String text = mag.getAmount(stack, spieler.getInventory()) > 0 ? "+" : "No ammo";

            RenderContext.pushPose();
            float kreuzGroesse = 0.01F;
            RenderContext.translate((font.width(text) / 2F) * kreuzGroesse + 2F, 1F + font.lineHeight * kreuzGroesse / 2F, -2.75F);
            RenderContext.scale(kreuzGroesse, -kreuzGroesse, kreuzGroesse);
            RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            font.drawInBatch(text, 0F, 0F, farbe, false,
                    RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
            RenderContext.popPose();
        }

        String schriftzug = getBootSplash();
        if(!schriftzug.isEmpty()) {
            RenderContext.pushPose();
            float splashGroesse = 0.02F;
            RenderContext.translate((font.width(schriftzug) / 2F) * splashGroesse + 2F, 1F + font.lineHeight * splashGroesse / 2F, -2.75F);
            RenderContext.scale(splashGroesse, -splashGroesse, splashGroesse);
            RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            font.drawInBatch(schriftzug, 0F, 0F, farbe, false,
                    RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
            RenderContext.popPose();
        }

        List<String> zeilen = getTTY();
        if(!zeilen.isEmpty()) {
            RenderContext.pushPose();
            float zeilenGroesse = 0.005F;
            RenderContext.translate(2.5F, 1.375F, -2.75F);
            RenderContext.scale(zeilenGroesse, -zeilenGroesse, zeilenGroesse);
            RenderContext.mulPose(Axis.YP.rotationDegrees(180F));
            for(String zeile : zeilen) {
                font.drawInBatch(zeile, 0F, 0F, farbe, false,
                        RenderContext.poseStack().last().pose(), buffer, Font.DisplayMode.NORMAL, 0, RenderContext.light());
                RenderContext.translate(0F, font.lineHeight + 2F, 0F);
            }
            RenderContext.popPose();
        }

        FullBright.disable();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        float scale = 3F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.translate(-0.25F, 0.5F, 3F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        float scale = 1.25F;
        RenderContext.scale(scale, scale, scale);
        RenderContext.mulPose(Axis.XP.rotationDegrees(25F));
        RenderContext.mulPose(Axis.YP.rotationDegrees(45F));
        RenderContext.translate(0F, -0.5F, 0F);
    }

    @Override
    public void renderStatic(ItemStack stack, MultiBufferSource buffer, ItemDisplayContext displayContext) {
        RenderSystem.setShaderTexture(0, ResourceManager.FOLLY_TEX);
        ResourceManager.folly.renderAll();
    }

    /**
     * Der Schriftzug, der sich nach drei Sekunden Buchstabe fuer Buchstabe aufbaut: eine
     * helle Stelle wandert von links nach rechts durch das Wort, davor liegt es dunkel,
     * dahinter hell. Zwei Sekunden lang, dann steht er.
     */
    public static String getBootSplash() {

        long jetzt = System.currentTimeMillis();
        if(zielSeit + 5000 < jetzt) return "";
        if(zielSeit + 3000 > jetzt) return "";

        int stelle = (int) ((jetzt - zielSeit - 3000) * 35 / 2000) - 10;

        char[] buchstaben = "VStarOS".toCharArray();
        StringBuilder schriftzug = new StringBuilder();

        for(int i = 0; i < buchstaben.length; i++) {
            if(i < stelle - 1) schriftzug.append(ChatFormatting.LIGHT_PURPLE);
            if(i == stelle - 1) schriftzug.append(ChatFormatting.AQUA);
            if(i == stelle) schriftzug.append(ChatFormatting.WHITE);
            if(i == stelle + 1) schriftzug.append(ChatFormatting.AQUA);
            if(i == stelle + 2) schriftzug.append(ChatFormatting.LIGHT_PURPLE);
            if(i > stelle + 2) schriftzug.append(ChatFormatting.BLACK);
            schriftzug.append(buchstaben[i]);
        }

        return schriftzug.toString();
    }

    /**
     * Die Textausgabe des Geraets. Die ersten drei Sekunden der Selbsttest, ab der fuenften
     * laufend das, worauf der Schuetze zeigt.
     */
    public static List<String> getTTY() {

        List<String> zeilen = new ArrayList<>();
        long jetzt = System.currentTimeMillis();
        int zeit = (int) (jetzt - zielSeit);

        if(zeit < 3000) {
            if(zeit > 250) zeilen.add(ChatFormatting.GREEN + "POST successful - Code 0");
            if(zeit > 500) zeilen.add(ChatFormatting.GREEN + "8,388,608 bytes of RAM installed");
            if(zeit > 500) zeilen.add(ChatFormatting.GREEN + "5,187,427 bytes available");
            if(zeit > 750) zeilen.add(ChatFormatting.GREEN + "Reticulating splines...");
            if(zeit > 1500) zeilen.add(ChatFormatting.GREEN + "No keyboard found!");
            if(zeit > 2000) zeilen.add(ChatFormatting.GREEN + "Booting from /dev/sda1...");
        }

        if(zeit > 5000) {

            LocalPlayer spieler = Minecraft.getInstance().player;
            if(spieler == null) return zeilen;

            HitResult treffer = EntityDamageUtil.getMouseOver(spieler, 250D);
            String ziel = ChatFormatting.GREEN + "Target: ";

            if(treffer instanceof EntityHitResult wesen) {
                ziel += wesen.getEntity().getName().getString();
            } else if(treffer instanceof BlockHitResult block) {
                BlockPos stelle = block.getBlockPos();
                ziel += stelle.getX() + "/" + stelle.getY() + "/" + stelle.getZ();
            } else {
                ziel += "N/A";
            }

            zeilen.add(ziel);
            zeilen.add(ChatFormatting.GREEN + "Angle: " + ((int) (-spieler.getXRot() * 100) / 100D));
        }

        return zeilen;
    }
}
