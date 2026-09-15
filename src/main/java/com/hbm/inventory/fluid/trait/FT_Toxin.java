package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.handler.HazmatRegistry;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ArmorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.fluid.trait.FT_Toxin.
 *
 * Beschreibt, was ein Fluid mit Lebewesen anstellt, die hineingeraten -- entweder unmittelbarer
 * Schaden oder Statuseffekte. Beides laesst sich durch die passende Schutzausruestung abwenden;
 * welche das ist, sagt die HazardClass des Eintrags.
 *
 * FT_Poison ist der Vorlaeufer und im Original bereits als veraltet markiert.
 */
public class FT_Toxin extends FluidTrait {

    public List<ToxinEntry> entries = new ArrayList<>();

    public FT_Toxin addEntry(ToxinEntry entry) {
        this.entries.add(entry);
        return this;
    }

    @Override
    public void addInfoHidden(List<Component> info) {
        info.add(Component.literal("[").append(Component.translatable("hbmfluid.trait.toxin")).append("]").withStyle(ChatFormatting.LIGHT_PURPLE));

        for(ToxinEntry entry : this.entries) {
            entry.addInfo(info);
        }
    }

    public void affect(LivingEntity entity, double intensity) {
        for(ToxinEntry entry : this.entries) {
            entry.poison(entity, intensity);
        }
    }

    public abstract static class ToxinEntry {

        public HazardClass clazz;
        public boolean fullBody;

        public ToxinEntry(HazardClass clazz, boolean fullBody) {
            this.clazz = clazz;
            this.fullBody = fullBody;
        }

        /**
         * Original: Maske ueber ArmorRegistry, Ganzkoerperschutz ueber ArmorUtil.checkForHazmat.
         * Letzteres ist dort eine fest verdrahtete Liste von Ruestungsteilen und schon im
         * Original als veraltet markiert; der Port hat mit HazmatRegistry das allgemeinere
         * System und fragt deshalb dieses. Zu tragen kommt das derzeit ohnehin nicht: keines
         * der portierten Fluide setzt fullBody.
         */
        public boolean isProtected(LivingEntity entity) {

            boolean hasMask = this.clazz == null;
            boolean hasSuit = !this.fullBody;

            if(this.clazz != null && ArmorRegistry.hasAllProtection(entity, EquipmentSlot.HEAD, this.clazz)) {
                ArmorUtil.damageGasMaskFilter(entity, 1);
                hasMask = true;
            }

            if(this.fullBody && entity instanceof Player player && HazmatRegistry.getResistance(player) > 0F) {
                hasSuit = true;
            }

            return hasMask && hasSuit;
        }

        public abstract void poison(LivingEntity entity, double intensity);
        public abstract void addInfo(List<Component> info);
    }

    public static class ToxinDirectDamage extends ToxinEntry {

        /**
         * Das Original haelt hier eine fertige DamageSource. Auf 1.21 geht das nicht: eine
         * DamageSource haengt an der Registry der Welt und laesst sich beim statischen Aufbau
         * der Fluidliste gar nicht bilden. Gespeichert wird deshalb der Schluessel, die Quelle
         * entsteht erst beim Zufuegen des Schadens.
         */
        public ResourceKey<DamageType> damage;
        public float amount;
        public int delay;

        public ToxinDirectDamage(ResourceKey<DamageType> damage, float amount, int delay, HazardClass clazz, boolean fullBody) {
            super(clazz, fullBody);
            this.damage = damage;
            this.amount = amount;
            this.delay = delay;
        }

        @Override
        public void poison(LivingEntity entity, double intensity) {
            if(this.isProtected(entity)) return;
            if(entity.level() == null) return;

            if(this.delay == 0 || entity.level().getGameTime() % this.delay == 0) {
                entity.hurt(entity.damageSources().source(this.damage), (float) (this.amount * intensity));
            }
        }

        @Override
        public void addInfo(List<Component> info) {
            String perTick = String.format(Locale.US, "%,.1f", this.amount * 20 / Math.max(this.delay, 1));
            Component line = Component.literal("- ")
                    .append(Component.translatable(this.clazz.unlocalizedMessage))
                    .append(this.fullBody ? Component.literal(" (").append(Component.translatable("hbmfluid.trait.hazmat")).append(")").withStyle(ChatFormatting.RED) : Component.empty())
                    .append(Component.literal(": " + perTick + " "))
                    .append(Component.translatable("hbmfluid.trait.perDamage"))
                    .withStyle(ChatFormatting.YELLOW);
            info.add(line);
        }
    }

    public static class ToxinEffects extends ToxinEntry {

        public List<MobEffectInstance> effects = new ArrayList<>();

        public ToxinEffects(HazardClass clazz, boolean fullBody) {
            super(clazz, fullBody);
        }

        public ToxinEffects add(MobEffectInstance... effs) {
            for(MobEffectInstance eff : effs) this.effects.add(eff);
            return this;
        }

        @Override
        public void poison(LivingEntity entity, double intensity) {
            if(this.isProtected(entity)) return;

            for(MobEffectInstance eff : this.effects) {
                entity.addEffect(new MobEffectInstance(eff.getEffect(), (int) (eff.getDuration() * intensity), eff.getAmplifier()));
            }
        }

        @Override
        public void addInfo(List<Component> info) {
            info.add(Component.literal("- ")
                    .append(Component.translatable(this.clazz.unlocalizedMessage))
                    .append(this.fullBody ? Component.literal(" (").append(Component.translatable("hbmfluid.trait.hazmat")).append(")") : Component.empty())
                    .append(Component.literal(":"))
                    .withStyle(ChatFormatting.YELLOW));

            for(MobEffectInstance eff : this.effects) {
                info.add(Component.literal("   - ")
                        .append(eff.getEffect().value().getDisplayName())
                        .append(Component.literal(" " + (eff.getDuration() / 20) + "s"))
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    @Override
    public void serializeJSON(JsonWriter writer) throws IOException {

        writer.name("entries").beginArray();

        for(ToxinEntry entry : this.entries) {
            writer.beginObject();

            if(entry instanceof ToxinDirectDamage e) {
                writer.name("type").value("directdamage");
                writer.name("amount").value(e.amount);
                writer.name("source").value(e.damage.location().toString());
                writer.name("delay").value(e.delay);
                writer.name("hazmat").value(e.fullBody);
                writer.name("masktype").value(e.clazz.name());
            }

            if(entry instanceof ToxinEffects e) {
                writer.name("type").value("effects");
                writer.name("effects").beginArray();
                writer.setIndent("");
                for(MobEffectInstance effect : e.effects) {
                    writer.beginArray();
                    writer.value(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString());
                    writer.value(effect.getDuration());
                    writer.value(effect.getAmplifier());
                    writer.value(effect.isAmbient());
                    writer.endArray();
                }
                writer.endArray();
                writer.setIndent("  ");
                writer.name("hazmat").value(e.fullBody);
                writer.name("masktype").value(e.clazz.name());
            }

            writer.endObject();
        }

        writer.endArray();
    }

    /**
     * Der Schadenstyp wird aus dem JSON nicht wiederhergestellt: auf 1.21 ist eine DamageSource
     * an die Registry der Welt gebunden und laesst sich hier, beim Laden der Rezeptdateien, gar
     * nicht bilden. Eintraege aus der Datei behalten deshalb den fest eingetragenen Schadenstyp
     * des Fluids; Menge, Verzoegerung und Schutzklasse sind einstellbar.
     */
    @Override
    public void deserializeJSON(JsonObject obj) {
        if(!obj.has("entries")) return;

        JsonArray array = obj.get("entries").getAsJsonArray();

        for(int i = 0; i < array.size(); i++) {
            JsonObject entry = array.get(i).getAsJsonObject();
            String name = entry.get("type").getAsString();

            if(name.equals("directdamage")) {
                float amount = entry.get("amount").getAsFloat();
                int delay = entry.get("delay").getAsInt();
                HazardClass clazz = HazardClass.valueOf(entry.get("masktype").getAsString());
                boolean hazmat = entry.get("hazmat").getAsBoolean();

                for(ToxinEntry existing : this.entries) {
                    if(existing instanceof ToxinDirectDamage e) {
                        e.amount = amount;
                        e.delay = delay;
                        e.clazz = clazz;
                        e.fullBody = hazmat;
                        break;
                    }
                }
            }

            if(name.equals("effects")) {
                ToxinEffects e = new ToxinEffects(
                        HazardClass.valueOf(entry.get("masktype").getAsString()),
                        entry.get("hazmat").getAsBoolean());

                JsonArray effects = entry.get("effects").getAsJsonArray();
                for(int j = 0; j < effects.size(); j++) {
                    JsonArray effect = effects.get(j).getAsJsonArray();
                    ResourceLocation id = ResourceLocation.parse(effect.get(0).getAsString());
                    BuiltInRegistries.MOB_EFFECT.getHolder(id).ifPresent(holder ->
                            e.effects.add(new MobEffectInstance(holder, effect.get(1).getAsInt(), effect.get(2).getAsInt(), effect.get(3).getAsBoolean(), true)));
                }

                this.entries.removeIf(x -> x instanceof ToxinEffects);
                this.entries.add(e);
            }
        }
    }
}
