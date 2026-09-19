package com.hbm.items.armor;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.util.BobMathUtil;
import com.hbm.util.TagsUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
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

/**
 * Portiert aus 1.7.10: com.hbm.items.armor.ArmorFSBPowered.
 *
 * Ein Anzugteil mit eigenem Akku. Ohne Ladung ist es totes Blech: isArmorEnabled liefert
 * falsch, und damit faellt der ganze Satzbonus weg. Aufgeladen wird es am Ladegeraet oder --
 * beim HEV-Anzug -- an den Akkus, die an den Waenden der Bauwerke haengen.
 *
 * ABWEICHUNG, wo die Ladung steht: das Original schreibt sie unter "charge" ins NBT des
 * Stapels. Auf 1.21 gibt es dafuer die eigenen Datenbestandteile; der Port legt sie deshalb
 * unter dem Schluessel, den IBatteryItem ohnehin vorgibt, in die Zusatzdaten.
 *
 * ABWEICHUNG, der Akku-Aufsatz: das Original vergroessert ueber ItemModBattery das
 * Fassungsvermoegen. Diesen Aufsatz hat der Port noch nicht, deshalb liefert getMaxCharge
 * schlicht den festen Wert. Sobald es ihn gibt, gehoert er hierher.
 */
public class ArmorFSBPoweredItem extends ArmorFSBItem implements IBatteryItem {

    public final long maxPower;
    public final long chargeRate;
    /** Was ein Punkt Schaden an Ladung kostet. */
    public final long consumption;
    /** Was der Satz je Tick verbraucht; null heisst: er zehrt nicht. */
    public final long drain;

    public ArmorFSBPoweredItem(Holder<ArmorMaterial> material, Type type, Properties properties, long maxPower, long chargeRate, long consumption, long drain) {
        super(material, type, properties);
        this.maxPower = maxPower;
        this.chargeRate = chargeRate;
        this.consumption = consumption;
        this.drain = drain;
    }

    @Override
    public boolean isArmorEnabled(ItemStack stack) {
        return this.getCharge(stack) > 0;
    }

    @Override public String getChargeTagName() { return "charge"; }

    @Override
    public long getCharge(ItemStack stack) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        /*
         * Ein frisches Teil ist voll. Das Original schreibt dafuer beim ersten Lesen die volle
         * Ladung ins NBT; hier genuegt es, sie zurueckzugeben -- geschrieben wird erst, wenn
         * sich wirklich etwas aendert.
         */
        if(!tag.contains(this.getChargeTagName())) return this.getMaxCharge(stack);
        return Math.min(tag.getLong(this.getChargeTagName()), this.getMaxCharge(stack));
    }

    @Override
    public void setCharge(ItemStack stack, long charge) {
        CompoundTag tag = TagsUtil.getCustomData(stack);
        tag.putLong(this.getChargeTagName(), Math.max(0, Math.min(charge, this.getMaxCharge(stack))));
        TagsUtil.putCustomData(stack, tag);
    }

    @Override public void chargeBattery(ItemStack stack, long amount) { this.setCharge(stack, this.getCharge(stack) + amount); }
    @Override public void dischargeBattery(ItemStack stack, long amount) { this.setCharge(stack, this.getCharge(stack) - amount); }

    @Override public long getMaxCharge(ItemStack stack) { return this.maxPower; }
    @Override public long getChargeRate(ItemStack stack) { return this.chargeRate; }
    @Override public long getDischargeRate(ItemStack stack) { return 0; }

    /* Der Balken zeigt die Ladung, nicht die Haltbarkeit -- diese Teile nutzen sich nicht ab. */
    @Override public boolean isBarVisible(ItemStack stack) { return this.getCharge(stack) < this.getMaxCharge(stack); }
    @Override public int getBarWidth(ItemStack stack) { return (int) Math.round(13D * this.getCharge(stack) / this.getMaxCharge(stack)); }
    /* Die Farbe richtet sich nach der Ladung, nicht nach der Haltbarkeit -- letztere aendert
     * sich nie, der Balken bliebe sonst immer gruen. Die Formel ist die von Vanille. */
    @Override public int getBarColor(ItemStack stack) { return Mth.hsvToRgb((float) this.getCharge(stack) / this.getMaxCharge(stack) / 3.0F, 1.0F, 1.0F); }

    /**
     * Schaden am Anzug geht auf die Ladung, nicht auf die Haltbarkeit -- so macht es das
     * Original in setDamage. Getroffen wird dabei immer nur das Teil, das den Treffer abbekommt.
     */
    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        this.dischargeBattery(stack, (long) amount * this.consumption);
        return 0;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {

        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if(level.isClientSide || this.drain <= 0) return;
        if(!(entity instanceof Player player) || player.isCreative()) return;
        if(player.getItemBySlot(this.getEquipmentSlot()) != stack) return;
        if(!hasFSBArmor(player)) return;

        this.dischargeBattery(stack, this.drain);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal(BobMathUtil.getShortNumber(this.getCharge(stack)) + " / " + BobMathUtil.getShortNumber(this.getMaxCharge(stack)) + " HE").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, components, flag);
    }
}
