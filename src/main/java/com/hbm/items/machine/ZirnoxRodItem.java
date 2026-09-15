package com.hbm.items.machine;

import com.hbm.inventory.MetaHelper;
import com.hbm.items.EnumMultiItem;
import com.hbm.util.EnumUtil;
import com.hbm.util.TagsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class ZirnoxRodItem extends EnumMultiItem {

    public ZirnoxRodItem(Properties properties) {
        super(properties, ZirnoxType.class, true, true);
    }

    public static void incrementLifeTime(ItemStack stack) {

        CompoundTag tag = TagsUtil.getCustomData(stack);
        int time = tag.getInt("life");
        tag.putInt("life", time + 1);
        TagsUtil.putCustomData(stack, tag);
    }

    public static void setLifeTime(ItemStack stack, int time) {

        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.putInt("life", time);
        TagsUtil.putCustomData(stack, tag);
    }

    public static int getLifeTime(ItemStack stack) {
        return TagsUtil.getCustomData(stack).getInt("life");
    }

    /** Hoechstlaufzeit des Untertyps, aus dem Metadatenwert des Stapels. */
    public static int getMaxLifeTime(ItemStack stack) {
        ZirnoxType num = EnumUtil.grabEnumSafely(ZirnoxType.class, MetaHelper.getMeta(stack));
        return num.maxLife;
    }

    /** Anteil der verbrauchten Laufzeit, zwischen null und eins. */
    private static float getBurnup(ItemStack stack) {
        int max = getMaxLifeTime(stack);
        if(max <= 0) return 0F;
        return Mth.clamp((float) getLifeTime(stack) / (float) max, 0F, 1F);
    }

    /* Wie im Original: der Balken erscheint, sobald der Stab ueberhaupt gebrannt hat. */
    @Override public boolean isBarVisible(ItemStack stack) { return getLifeTime(stack) > 0; }

    /*
     * Der Balken zeigt die RESTliche Laufzeit -- das Original rechnete noch mit dem Schaden,
     * in 1.21 ist es andersherum. Dreizehn ist voll.
     */
    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13F * (1F - getBurnup(stack)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb((1F - getBurnup(stack)) / 3F, 1.0F, 1.0F);
    }

    public enum ZirnoxType {
        NATURAL_URANIUM_FUEL(250_000, 30),
        URANIUM_FUEL(200_000, 50),
        TH232(20_000, 0, true),
        THORIUM_FUEL(200_000, 40),
        MOX_FUEL(165_000, 75),
        PLUTONIUM_FUEL(175_000, 65),
        U233_FUEL(150_000, 100),
        U235_FUEL(165_000, 85),
        LES_FUEL(150_000, 150),
        LITHIUM(20_000, 0, true),
        ZFB_MOX(50_000, 35);

        public final int maxLife;
        public final int heat;
        public final boolean breeding;

        ZirnoxType(int life, int heat, boolean breeding) {
            this.maxLife = life;
            this.heat = heat;
            this.breeding = breeding;
        }

        ZirnoxType(int life, int heat) {
            this.maxLife = life;
            this.heat = heat;
            this.breeding = false;
        }
    }
}
