package com.hbm.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Zustand eines RBMK-Brennstabs.
 *
 * Abweichung vom Original: dort lagen die vier Werte als lose NBT-Doubles am ItemStack. In 1.21
 * gibt es dafuer Datenkomponenten, also stecken sie in einem Datensatz.
 *
 * @param yield    verbleibender Abbrand
 * @param xenon    Xenonvergiftung in Prozent
 * @param coreHeat Kerntemperatur in Grad Celsius
 * @param hullHeat Huellentemperatur in Grad Celsius
 */
public record RBMKFuelData(double yield, double xenon, double coreHeat, double hullHeat) {

    public static final Codec<RBMKFuelData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("yield").forGetter(RBMKFuelData::yield),
            Codec.DOUBLE.fieldOf("xenon").forGetter(RBMKFuelData::xenon),
            Codec.DOUBLE.fieldOf("core").forGetter(RBMKFuelData::coreHeat),
            Codec.DOUBLE.fieldOf("hull").forGetter(RBMKFuelData::hullHeat)
    ).apply(instance, RBMKFuelData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RBMKFuelData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, RBMKFuelData::yield,
            ByteBufCodecs.DOUBLE, RBMKFuelData::xenon,
            ByteBufCodecs.DOUBLE, RBMKFuelData::coreHeat,
            ByteBufCodecs.DOUBLE, RBMKFuelData::hullHeat,
            RBMKFuelData::new
    );

    public RBMKFuelData withYield(double yield) { return new RBMKFuelData(yield, this.xenon, this.coreHeat, this.hullHeat); }
    public RBMKFuelData withXenon(double xenon) { return new RBMKFuelData(this.yield, xenon, this.coreHeat, this.hullHeat); }
    public RBMKFuelData withCoreHeat(double coreHeat) { return new RBMKFuelData(this.yield, this.xenon, coreHeat, this.hullHeat); }
    public RBMKFuelData withHullHeat(double hullHeat) { return new RBMKFuelData(this.yield, this.xenon, this.coreHeat, hullHeat); }
}
