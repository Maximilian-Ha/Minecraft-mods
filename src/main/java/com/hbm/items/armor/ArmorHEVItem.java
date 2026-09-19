package com.hbm.items.armor;

import com.hbm.extprop.HbmLivingAttachments;
import com.hbm.render.model.armor.ModelArmorHEV;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorHEV.
 *
 * Der HEV-Anzug. Ein bestromter Vollsatz mit eigenem Wellenfrontmodell und, solange er
 * vollstaendig getragen wird, einer eigenen Anzeige anstelle von Herzen und Ruestungsbalken:
 * Lebenspunkte und Ladung als Zahl, dazu ein Strahlungsbalken und die Dosisrate.
 */
public class ArmorHEVItem extends ArmorFSBPoweredItem {

    public ArmorHEVItem(Holder<ArmorMaterial> material, Type type, Properties properties, long maxPower, long chargeRate, long consumption, long drain) {
        super(material, type, properties, maxPower, chargeRate, consumption, drain);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ModelArmorHEV replacement;

            @Override
            @OnlyIn(Dist.CLIENT)
            public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if(replacement == null) replacement = new ModelArmorHEV(original, slot);
                replacement.getPropertiesFrom(original);
                replacement.living = living;
                return replacement;
            }
        });
    }

    /* Der Abstand zwischen zwei Messungen der Dosisrate, in Millisekunden. */
    private static final long MESSABSTAND = 1000L;

    private static long letzteMessung;
    private static float vorletzterWert;
    private static float letzterWert;

    /**
     * Haengt sich vor die Herzen und den Ruestungsbalken. Beide werden unterdrueckt, und an
     * ihrer Stelle steht die Anzeige des Anzugs -- so wie im Original, das dafuer die beiden
     * Teilereignisse ARMOR und HEALTH abfaengt.
     *
     * ABWEICHUNG: das Original faengt zusaetzlich jeden Aufruf ab, auch wenn der Anzug leer
     * ist; entscheidend ist dort hasFSBArmorIgnoreCharge. Der Port haelt sich daran -- ein
     * leerer Anzug zeigt seine Anzeige weiter, nur eben mit Ladung null.
     */
    @OnlyIn(Dist.CLIENT)
    public static void handleOverlay(RenderGuiLayerEvent.Pre event, Player player) {

        if(!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorHEVItem)) return;
        if(!hasFSBArmorIgnoreCharge(player)) return;

        if(event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL)) {
            event.setCanceled(true);
            return;
        }

        if(!event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) return;

        event.setCanceled(true);
        zeichneAnzeige(event.getGuiGraphics(), player);
    }

    @OnlyIn(Dist.CLIENT)
    private static void zeichneAnzeige(GuiGraphics guiGraphics, Player player) {

        Minecraft mc = Minecraft.getInstance();

        float dosis = HbmLivingAttachments.getRadiation(player);
        float rate = letzterWert - vorletzterWert;

        if(System.currentTimeMillis() >= letzteMessung + MESSABSTAND) {
            letzteMessung = System.currentTimeMillis();
            vorletzterWert = letzterWert;
            letzterWert = dosis;
        }

        int hoehe = guiGraphics.guiHeight();

        /* Die Zahlen stehen doppelt so gross wie gewoehnlicher Text -- daher die Skalierung
         * und die halbierten Koordinaten; beides steht genauso im Original. */
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(2F, 2F, 1F);

        int leben = (int) (player.getHealth() * 5);
        guiGraphics.drawString(mc.font, "+" + leben, 8 / 2, (hoehe - 18 - 2) / 2, leben > 15 ? 0xFF8000 : 0xFF0000, false);

        double ladung = 0D;

        for(EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ItemStack teil = player.getItemBySlot(slot);
            if(teil.getItem() instanceof ArmorFSBPoweredItem akku) {
                ladung += (double) akku.getCharge(teil) / (double) akku.getMaxCharge(teil);
            }
        }

        int panzerung = (int) (ladung * 25);
        guiGraphics.drawString(mc.font, "||" + panzerung, 70 / 2, (hoehe - 18 - 2) / 2, panzerung > 15 ? 0xFF8000 : 0xFF0000, false);

        /* Der Strahlungsbalken: zehn Stellen zu je hundert RAD, jede in drei Stufen. */
        StringBuilder balken = new StringBuilder("RAD [");

        for(int i = 0; i < 10; i++) {
            if(dosis / 100 > i) {
                int rest = (int) (dosis - i * 100);
                balken.append(rest < 33 ? ".." : rest < 67 ? "|." : "||");
            } else {
                balken.append(' ');
            }
        }

        balken.append(']');

        guiGraphics.drawString(mc.font, balken.toString(), 8 / 2, (hoehe - 40) / 2, dosis < 800 ? 0xFF8000 : 0xFF0000, false);

        guiGraphics.pose().popPose();

        if(rate > 0) {
            String text = rate > 1000 ? ">1000" : rate < 1 ? "<1" : String.valueOf(Math.round(rate));
            guiGraphics.drawString(mc.font, text + " RAD/s", 32, hoehe - 55, 0xFF0000, false);
        }
    }
}
