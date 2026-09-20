package com.hbm.items.armor;

import api.hbm.fluidmk2.IFillableItem;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.util.BobMathUtil;
import com.hbm.util.TagsUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorFSBFueled.
 *
 * Das Gegenstueck zu ArmorFSBPoweredItem fuer Anzuege, die keinen Akku haben, sondern einen
 * TANK. Ohne Fuellung ist das Teil totes Blech: isArmorEnabled liefert falsch, und damit
 * faellt der ganze Satzbonus weg. Nachgefuellt wird an der Zapfsaeule, die jeden
 * IFillableItem bedient.
 *
 * DER VERBRAUCH LAEUFT NUR JEDEN ZEHNTEN TICK, anders als beim bestromten Anzug, der jeden
 * Tick zehrt. Das steht so im Original (world.getTotalWorldTime() % 10 == 0) und macht den
 * Unterschied zwischen einem Tank, der eine Weile haelt, und einem, der leerlaeuft.
 *
 * ABWEICHUNG, wo die Fuellung steht: das Original schreibt sie unter "fuel" ins NBT des
 * Stapels. Auf 1.21 liegen solche Angaben in den Zusatzdaten; der Schluessel bleibt derselbe.
 *
 * ABWEICHUNG, der Fluessigkeitstyp: im Original ist er ein Feld, das beim Anlegen gesetzt
 * wird. Die Fluidtypen des Ports stehen erst nach dem Laden fest, deshalb kommt er hier als
 * Lieferant herein und wird erst beim Gebrauch abgerufen.
 */
public class ArmorFSBFueledItem extends ArmorFSBItem implements IFillableItem {

    private final Supplier<FluidType> fuelType;
    public final int maxFuel;
    public final int fillRate;
    /** Was ein Punkt Schaden an Fuellung kostet. */
    public final int consumption;
    /** Was der Satz je zehn Ticks verbraucht; null heisst: er zehrt nicht. */
    public final int drain;

    public ArmorFSBFueledItem(Holder<ArmorMaterial> material, Type type, Properties properties,
            Supplier<FluidType> fuelType, int maxFuel, int fillRate, int consumption, int drain) {

        super(material, type, properties);

        this.fuelType = fuelType;
        this.maxFuel = maxFuel;
        this.fillRate = fillRate;
        this.consumption = consumption;
        this.drain = drain;
    }

    public FluidType getFuelType() { return this.fuelType.get(); }

    @Override
    public boolean isArmorEnabled(ItemStack stack) {
        return this.getFill(stack) > 0;
    }

    @Override
    public int getFill(ItemStack stack) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        /* Ein frisches Teil ist voll -- geschrieben wird erst, wenn sich etwas aendert. */
        if(!tag.contains("fuel")) return this.maxFuel;
        return Math.min(tag.getInt("fuel"), this.maxFuel);
    }

    public void setFill(ItemStack stack, int fill) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.putInt("fuel", Math.max(0, Math.min(fill, this.maxFuel)));
        TagsUtil.putCustomData(stack, tag);
    }

    public int getMaxFill(ItemStack stack) { return this.maxFuel; }

    /* Der Balken zeigt die Fuellung, nicht die Haltbarkeit -- diese Teile nutzen sich nicht ab. */
    @Override public boolean isBarVisible(ItemStack stack) { return this.getFill(stack) < this.getMaxFill(stack); }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13F * this.getFill(stack) / this.getMaxFill(stack)); }
    @Override public int getBarColor(ItemStack stack) { return Mth.hsvToRgb((float) this.getFill(stack) / this.getMaxFill(stack) / 3.0F, 1.0F, 1.0F); }

    /** Schaden am Anzug geht auf den Tank, nicht auf die Haltbarkeit -- wie im Original. */
    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        this.setFill(stack, this.getFill(stack) - amount * this.consumption);
        return 0;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {

        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(level.isClientSide || this.drain <= 0) return;
        if(!(entity instanceof Player player) || player.isCreative()) return;
        if(player.getItemBySlot(this.getEquipmentSlot()) != stack) return;
        if(!hasFSBArmor(player)) return;

        /* Nur jeden zehnten Tick, siehe Kopf. */
        if(level.getGameTime() % 10 != 0) return;

        this.setFill(stack, this.getFill(stack) - this.drain);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(this.getFuelType().getName().copy()
                .append(": " + BobMathUtil.getShortNumber(this.getFill(stack)) + " / " + BobMathUtil.getShortNumber(this.getMaxFill(stack)))
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, components, flag);
    }

    // ---- IFillableItem ----------------------------------------------------------------

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) { return type == this.getFuelType(); }

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {

        if(!this.acceptsFluid(type, stack)) return amount;

        int toFill = Math.min(amount, this.fillRate);
        toFill = Math.min(toFill, this.maxFuel - this.getFill(stack));
        this.setFill(stack, this.getFill(stack) + toFill);

        return amount - toFill;
    }

    /* Der Anzug gibt nichts ab -- er ist Verbraucher, nicht Vorrat. */
    @Override public boolean providesFluid(FluidType type, ItemStack stack) { return false; }
    @Override public int tryEmpty(FluidType type, int amount, ItemStack stack) { return 0; }
    @Override public FluidType getFirstFluidType(ItemStack stack) { return null; }
}
