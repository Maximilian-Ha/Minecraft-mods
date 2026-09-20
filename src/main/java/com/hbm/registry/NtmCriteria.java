package com.hbm.registry;

import java.util.Optional;

import com.hbm.main.NuclearTechMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Die Ausloeser der Erfolge, Runde 248.
 *
 * DAS ORIGINAL BRAUCHT SO ETWAS NICHT. In 1.7.10 ruft man schlicht
 * player.triggerAchievement(MainRegistry.achX) -- zweiunddreissig Stellen im Quelltext tun
 * das. Auf 1.21 gibt es dazu kein Gegenstueck: ein Erfolg wird nicht verliehen, sondern
 * sein Kriterium wird erfuellt, und Kriterien muessen angemeldet sein.
 *
 * EIN TRIGGER FUER ALLE, mit einer Kennung als Unterscheidung. Die Alternative waere ein
 * eigener Trigger je Erfolg gewesen -- zweiunddreissig Klassen, die sich nur im Namen
 * unterscheiden. Die Kennung steht in der Erfolgsdatei und im Aufruf; stimmen sie ueberein,
 * faellt der Erfolg.
 */
public class NtmCriteria {

    public static final DeferredRegister<CriterionTrigger<?>> CRITERIA =
            DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, NuclearTechMod.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, MarkeTrigger> MARKE =
            CRITERIA.register("marke", MarkeTrigger::new);

    public static void register(IEventBus bus) {
        CRITERIA.register(bus);
    }

    /** Feuert die Marke fuer diesen Spieler. Der Aufruf, der triggerAchievement ersetzt. */
    public static void marke(ServerPlayer spieler, String kennung) {
        MARKE.get().feuere(spieler, kennung);
    }

    /**
     * Feuert die Marke fuer jeden Spieler im Umkreis. Das Original macht das an mehreren
     * Stellen gleich: eine Kugel um die Anlage ziehen und jeden darin bedenken -- wer
     * weiter weg stand, hat es nicht miterlebt.
     */
    public static void markeImUmkreis(Level level, BlockPos pos, double reichweite, String kennung) {
        for(ServerPlayer spieler : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(reichweite))) {
            marke(spieler, kennung);
        }
    }

    /**
     * Feuert die Marke fuer JEDEN Spieler der Welt, ohne Umkreis. Das Original macht das an
     * den Stellen, an denen ein Ereignis die ganze Welt angeht -- der Satellit, der in die
     * Umlaufbahn kommt, der Gast, der nach Hause geschickt wird.
     */
    public static void markeFuerAlle(Level level, String kennung) {
        if(!(level instanceof ServerLevel serverLevel)) return;
        for(ServerPlayer spieler : serverLevel.players()) marke(spieler, kennung);
    }

    /** Dasselbe um eine Entitaet herum. */
    public static void markeImUmkreis(Entity mitte, double reichweite, String kennung) {
        for(ServerPlayer spieler : mitte.level().getEntitiesOfClass(ServerPlayer.class,
                mitte.getBoundingBox().inflate(reichweite))) {
            marke(spieler, kennung);
        }
    }

    public static class MarkeTrigger extends SimpleCriterionTrigger<MarkeTrigger.Bedingung> {

        @Override
        public Codec<Bedingung> codec() {
            return Bedingung.CODEC;
        }

        public void feuere(ServerPlayer spieler, String kennung) {
            this.trigger(spieler, bedingung -> bedingung.kennung().equals(kennung));
        }

        public record Bedingung(Optional<ContextAwarePredicate> player, String kennung)
                implements SimpleCriterionTrigger.SimpleInstance {

            public static final Codec<Bedingung> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Bedingung::player),
                    Codec.STRING.fieldOf("kennung").forGetter(Bedingung::kennung)
            ).apply(instance, Bedingung::new));
        }
    }
}
