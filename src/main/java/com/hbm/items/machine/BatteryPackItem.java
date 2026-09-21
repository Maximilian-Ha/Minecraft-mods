package com.hbm.items.machine;

import api.hbm.energymk2.IBatteryItem;
import com.hbm.interfaces.IOrderedEnum;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.EnumMultiItem;
import com.hbm.main.NuclearTechMod;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.TagsUtil;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.registry.NtmCriteria;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;

public class BatteryPackItem extends EnumMultiItem implements IBatteryItem {

    public enum BatteryPackType {
        BATTERY_REDSTONE	("battery_redstone",	      100L, false),
        BATTERY_LEAD		("battery_lead",		    1_000L, false),
        BATTERY_LITHIUM		("battery_lithium",		   10_000L, false),
        BATTERY_SODIUM		("battery_sodium",		   50_000L, false),
        BATTERY_SCHRABIDIUM	("battery_schrabidium",	  250_000L, false),
        BATTERY_QUANTUM		("battery_quantum",		1_000_000L, 20 * 60 * 60),

        CAPACITOR_COPPER	("capacitor_copper",	     1_000L, true),
        CAPACITOR_GOLD		("capacitor_gold",		    10_000L, true),
        CAPACITOR_NIOBIUM	("capacitor_niobium",	   100_000L, true),
        CAPACITOR_TANTALUM	("capacitor_tantalum",	   500_000L, true),
        CAPACITOR_BISMUTH	("capacitor_bismuth",	 2_500_000L, true),
        CAPACITOR_SPARK		("capacitor_spark",		10_000_000L, true),

        /*
         * DIE BEIDEN KARTOFFELN, RUNDE 274. Sie stehen am ENDE und nicht bei den uebrigen
         * Batterien, obwohl sie dort hingehoeren: der Platz in dieser Aufzaehlung ist das
         * Metadatum am Stapel. Wer einen Eintrag in der Mitte einfuegt, verschiebt jeden
         * dahinter -- und damit jede Batterie in jeder gespeicherten Welt. Angehaengt wird
         * nichts verschoben.
         *
         * Ihre Zahlen stehen so im Original (ModItems.java:3693-3694): tausend und
         * fuenfhunderttausend HE, Ladetempo NULL -- beide lassen sich nicht aufladen, sie
         * geben nur ab.
         */
        BATTERY_POTATO		("battery_potato",		    1_000L, 0L, 100L),
        BATTERY_POTATOS		("battery_potatos",		  500_000L, 0L, 100L);

        public final ResourceLocation texture;
        public final long capacity;
        public final long chargeRate;
        public final long dischargeRate;
        /** Kondensator statt Batterie -- frueher an der Reihenfolge abgelesen, siehe unten. */
        public final boolean kondensator;

        BatteryPackType(String tex, long dischargeRate, boolean capacitor) {
            this(tex,
                    capacitor ? (dischargeRate * 20 * 30) : (dischargeRate * 20 * 60 * 15),
                    capacitor ? dischargeRate : dischargeRate * 10,
                    dischargeRate,
                    capacitor);
        }

        BatteryPackType(String tex, long dischargeRate, long duration) {
            this(tex, dischargeRate * duration, dischargeRate * 10, dischargeRate, false);
        }

        BatteryPackType(String tex, long capacity, long chargeRate, long dischargeRate) {
            this(tex, capacity, chargeRate, dischargeRate, false);
        }

        BatteryPackType(String tex, long capacity, long chargeRate, long dischargeRate, boolean kondensator) {
            this.texture = NuclearTechMod.withDefaultNamespace("textures/models/machines/" + tex + ".png");
            this.capacity = capacity;
            this.chargeRate = chargeRate;
            this.dischargeRate = dischargeRate;
            this.kondensator = kondensator;
        }

        /*
         * FRUEHER STAND HIER ordinal() > BATTERY_QUANTUM.ordinal(). Das ging, solange die
         * Kondensatoren die letzten der Liste waren -- mit den beiden Kartoffeln dahinter
         * waeren sie ploetzlich auch Kondensatoren geworden. Jetzt traegt jeder Eintrag die
         * Antwort selbst.
         */
        public boolean isCapacitor() { return this.kondensator; }
    }

    public BatteryPackItem(Properties properties) {
        super(properties.stacksTo(1), BatteryPackType.class, true, false);
    }

    @Override
    public void chargeBattery(ItemStack stack, long i) {
        if (TagsUtil.hasCustomData(stack)) {
            CompoundTag tag = TagsUtil.getCustomData(stack);
            tag.putLong("Charge", tag.getLong("Charge") + i);
            TagsUtil.putCustomData(stack, tag);
        } else {
            CompoundTag tag = new CompoundTag();
            tag.putLong("Charge", i);
            TagsUtil.putCustomData(stack, tag);
        }
    }

    @Override
    public void setCharge(ItemStack stack, long i) {
        if (TagsUtil.hasCustomData(stack)) {
            CompoundTag tag = TagsUtil.getCustomData(stack);
            tag.putLong("Charge", i);
            TagsUtil.putCustomData(stack, tag);
        } else {
            CompoundTag tag = new CompoundTag();
            tag.putLong("Charge", i);
            TagsUtil.putCustomData(stack, tag);
        }
    }

    @Override
    public void dischargeBattery(ItemStack stack, long i) {
        if (TagsUtil.hasCustomData(stack)) {
            CompoundTag tag = TagsUtil.getCustomData(stack);
            tag.putLong("Charge", tag.getLong("Charge") - i);
            TagsUtil.putCustomData(stack, tag);
        } else {
            CompoundTag tag = new CompoundTag();
            tag.putLong("Charge", 0);
            TagsUtil.putCustomData(stack, tag);
        }
    }

    @Override
    public long getCharge(ItemStack stack) {
        if (!TagsUtil.hasCustomData(stack)) {
            CompoundTag tag = new CompoundTag();
            tag.putLong("Charge", 0);
            TagsUtil.putCustomData(stack, tag);
        }
        return TagsUtil.getCustomData(stack).getLong("Charge");
    }

    @Override
    public long getMaxCharge(ItemStack stack) {
        BatteryPackType pack = EnumUtil.grabEnumSafely(BatteryPackType.class, MetaHelper.getMeta(stack));
        return pack.capacity;
    }

    @Override
    public long getChargeRate(ItemStack stack) {
        BatteryPackType pack = EnumUtil.grabEnumSafely(BatteryPackType.class, MetaHelper.getMeta(stack));
        return pack.chargeRate;
    }

    @Override
    public long getDischargeRate(ItemStack stack) {
        BatteryPackType pack = EnumUtil.grabEnumSafely(BatteryPackType.class, MetaHelper.getMeta(stack));
        return pack.dischargeRate;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return super.getUseDuration(stack, entity);
    }

    @Override public boolean isBarVisible(ItemStack stack) { return getCharge(stack) < getMaxCharge(stack); }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13.0F * getCharge(stack) / (float) getMaxCharge(stack)); }
    @Override public int getBarColor(ItemStack stack) {
        float ratio = (float) getCharge(stack) / (float) getMaxCharge(stack);
        return Mth.hsvToRgb(ratio / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        long maxCharge = this.getMaxCharge(stack);
        long chargeRate = this.getChargeRate(stack);
        long dischargeRate = this.getDischargeRate(stack);
        long charge = maxCharge;

        if (TagsUtil.hasCustomData(stack)) charge = getCharge(stack);

        String het = I18nUtil.resolveKey("he") + "/" + I18nUtil.resolveKey("t");
        components.add(Component.translatable("item.hbmsntm.obj_battery_pack.desc0", BobMathUtil.getShortNumber(charge) + "/" + BobMathUtil.getShortNumber(maxCharge) + I18nUtil.resolveKey("he").toUpperCase(Locale.US) + " (" + (charge * 1000 / maxCharge / 10D) + "%)").withStyle(ChatFormatting.GREEN));
        components.add(Component.translatable("item.hbmsntm.obj_battery_pack.desc1", BobMathUtil.getShortNumber(chargeRate) + het).withStyle(ChatFormatting.YELLOW));
        components.add(Component.translatable("item.hbmsntm.obj_battery_pack.desc2", BobMathUtil.getShortNumber(dischargeRate) + het).withStyle(ChatFormatting.YELLOW));
        components.add(Component.translatable("item.hbmsntm.obj_battery_pack.desc3", (maxCharge / chargeRate / 20 / 60D) + I18nUtil.resolveKey("min")).withStyle(ChatFormatting.GOLD));
        components.add(Component.translatable("item.hbmsntm.obj_battery_pack.desc4", (maxCharge / dischargeRate / 20 / 60D) + I18nUtil.resolveKey("min")).withStyle(ChatFormatting.GOLD));
    }

    public static ItemStack makeEmptyBattery(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Charge", 0);
        TagsUtil.putCustomData(stack, tag);
        return stack;
    }

    public static ItemStack makeFullBattery(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Charge", ((BatteryPackItem) stack.getItem()).getMaxCharge(stack));
        TagsUtil.putCustomData(stack, tag);
        return stack;
    }

    /**
     * Die grosse Kartoffelbatterie meldet sich zu Wort, solange sie noch Ladung hat.
     *
     * Portiert aus 1.7.10: com.hbm.items.special.ItemPotatos.onUpdate. Nur die GROSSE spricht
     * -- die kleine ist dort eine gewoehnliche ItemBattery. Die Tonhoehe haengt am Ladestand:
     * leer klingt sie tief, voll hoch (Ladung/Hoechstladung * 0,5 + 0,5).
     *
     * DER ZAEHLER STEHT AM STAPEL, nicht im Gegenstand: ein Gegenstand ist ein Singleton, und
     * zwei Kartoffeln im selben Rucksack sollen nicht im Gleichtakt reden. Das Original legt
     * ihn aus demselben Grund ins NBT des Stapels.
     *
     * NUR IN DER HAND. Das Original prueft getHeldItem() == stack; hier ist das der Fall,
     * wenn der Gegenstand als ausgewaehlt gemeldet wird.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {

        if(level.isClientSide) return;
        if(MetaHelper.getMeta(stack) != BatteryPackType.BATTERY_POTATOS.ordinal()) return;
        if(!(entity instanceof Player spieler)) return;

        /* Der Erfolg haengt am Besitz, nicht am Tragen: im Original ist er ein Bau-Erfolg
         * (AchievementHandler.craftingAchievements), und die faengt Forge ueber das Inventar
         * ab. Darum steht die Marke vor der Pruefung auf "in der Hand". */
        if(spieler instanceof ServerPlayer serverSpieler) NtmCriteria.marke(serverSpieler, "potato");

        if(!selected) return;

        long ladung = this.getCharge(stack);
        if(ladung <= 0L) return;

        CompoundTag tafel = TagsUtil.getCustomData(stack);
        int wartezeit = tafel.getInt(UHR_KEY);

        if(wartezeit > 0) {
            tafel.putInt(UHR_KEY, wartezeit - 1);
            TagsUtil.putCustomData(stack, tafel);
            return;
        }

        float hoehe = (float) ladung / (float) this.getMaxCharge(stack) * 0.5F + 0.5F;
        level.playSound(null, spieler.getX(), spieler.getY(), spieler.getZ(),
                NtmSoundEvents.POTATOS.get(), SoundSource.PLAYERS, 1.0F, hoehe);

        tafel.putInt(UHR_KEY, 200 + level.random.nextInt(100));
        TagsUtil.putCustomData(stack, tafel);
    }

    /** Wie lange die grosse Kartoffel noch schweigt. */
    private static final String UHR_KEY = "PotatoTimer";

    @Override
    public void getSubItems(Item item, List<ItemStack> stacks) {

        Enum<?>[] order = theEnum.getEnumConstants();
        if(order[0] instanceof IOrderedEnum ord) order = ord.getOrder();

        for(Enum<?> anEnum : order) {
            ItemStack stack = MetaHelper.metaStack(new ItemStack(item, 1), anEnum.ordinal());
            stacks.add(makeEmptyBattery(stack.copy()));
            stacks.add(makeFullBattery(stack.copy()));
        }
    }
}
