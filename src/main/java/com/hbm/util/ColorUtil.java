package com.hbm.util;

import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;
import java.util.HashMap;

/**
 * Farbhilfen aus dem Original. Die Masten brauchen davon getColorFromDye fuer die Drahtfarbe.
 *
 * Weggelassen: getImageFromStack, getAverageColorFromStack und getMedianBrightnessColorFromStack.
 * Die haben im Original die Item-Textur ueber IIcon#getIconName aus dem Ressourcenpaket nachgeladen;
 * das Icon-System gibt es in 1.21 nicht mehr. Bisher braucht sie im Port auch niemand.
 */
public class ColorUtil {

    public static int color(int r, int g, int b) {
        return ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
    }

    public static int ir(int color) {
        return (color & 0xff0000) >> 16;
    }

    public static int ig(int color) {
        return (color & 0x00ff00) >> 8;
    }

    public static int ib(int color) {
        return (color & 0x0000ff) >> 0;
    }

    public static float fr(int color) {
        return ir(color) / 255F;
    }

    public static float fg(int color) {
        return ig(color) / 255F;
    }

    public static float fb(int color) {
        return ib(color) / 255F;
    }

    /**
     * Decides whether a color is considered "colorful", i.e. weeds out colors that are too dark or too close to gray.
     */
    public static boolean isColorColorful(int hex) {
        Color color = new Color(hex);

        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);

        //     saturation       brightness
        return hsb[1] > 0.25 && hsb[2] > 0.25;
    }

    /**
     * Raises the highest RGB component to the specified limit, scaling the other components with it.
     */
    public static int amplifyColor(int hex, int limit) {
        Color color = new Color(hex);
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int max = Math.max(Math.max(1, r), Math.max(g, b));

        r = r * limit / max;
        g = g * limit / max;
        b = b * limit / max;

        return new Color(r, g, b).getRGB();
    }

    /**
     * Same as the regular amplifyColor but it uses 255 as the limit.
     */
    public static int amplifyColor(int hex) {
        return amplifyColor(hex, 255);
    }

    /**
     * Amplifies a given color by approaching all components to maximum by a given percentage. A percentage of 1 (100%) should always yield white.
     */
    public static int lightenColor(int hex, double percent) {
        Color color = new Color(hex);
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        r = (int) (r + (255 - r) * percent);
        g = (int) (g + (255 - g) * percent);
        b = (int) (b + (255 - b) * percent);

        return new Color(r, g, b).getRGB();
    }

    /** Converts a color into HSB and then returns the brightness component */
    public static double getColorBrightness(int hex) {
        Color color = new Color(hex);
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
        return hsb[2];
    }

    public static HashMap<String, Integer> nameToColor = new HashMap<>() {{
        put("black", 1973019);
        put("red", 11743532);
        put("green", 3887386);
        put("brown", 5320730);
        put("blue", 2437522);
        put("purple", 8073150);
        put("cyan", 2651799);
        put("silver", 11250603);
        put("lightgray", 11250603);
        put("gray", 4408131);
        put("pink", 14188952);
        put("lime", 4312372);
        put("yellow", 14602026);
        put("lightblue", 6719955);
        put("magenta", 12801229);
        put("orange", 15435844);
        put("white", 15790320);
    }};

    /**
     * Im Original lief das ueber das Ore Dictionary ("dyeXxx"). In 1.21 gibt es dafuer DyeItem,
     * dessen Farbnamen bis auf die Unterstriche (light_blue, light_gray) denen des Originals entsprechen.
     * Rueckgabe 0 heisst "kein Farbstoff".
     */
    public static int getColorFromDye(ItemStack stack) {
        if(stack == null || stack.isEmpty()) return 0;
        if(!(stack.getItem() instanceof DyeItem dye)) return 0;

        String name = dye.getDyeColor().getSerializedName().replace("_", "");
        Integer color = nameToColor.get(name);

        return color == null ? 0 : color;
    }
}
