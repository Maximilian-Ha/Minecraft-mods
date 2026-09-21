package com.hbm.blockentity;

import com.hbm.blocks.generic.GlyphidSpawnerBlock;
import com.hbm.config.NtmConfig;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.mob.glyphid.Glyphid;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Portiert aus 1.7.10: die innere Klasse TileEntityGlpyhidSpawner aus BlockGlyphidSpawner
 * (der Schreibfehler steht so im Original).
 *
 * DER RUSS IST DER REGLER. Alle zwei Minuten -- und einmal sofort nach dem Setzen -- bringt
 * das Gelege einen Schwarm hervor. Wie GROSS er ist und WAS darin steckt, haengt beides am
 * Russ in der Luft:
 *
 *   Schwarmgroesse = Grundgroesse * max(Faktor * Russ/Russschritt, 1), hoechstens zehn
 *   Wahrscheinlichkeit je Art = Grundwert + (Aufschlag - Aufschlag / max((Russ+1)/3, 1))
 *
 * Die zweite Formel liest sich sperrig, tut aber etwas Einfaches: bei null Russ bleibt der
 * Grundwert stehen, mit steigendem Russ waechst der Aufschlag auf seinen vollen Wert zu.
 * Arten mit NEGATIVEM Grundwert kommen darum ueberhaupt erst ab einer gewissen Verschmutzung
 * vor -- Brenda ab zwanzig, Big Man Johnson ab fuenfzig. Wer sauber wirtschaftet, sieht nur
 * gewoehnliche Glyphiden.
 *
 * ZWEI BREMSEN: ueber fuenfzig Glyphiden in der Welt, und es kommt nichts mehr; und stehen
 * schon mehr als drei im Umkreis, wartet das Gelege -- ausser es ist ein radioaktives, das
 * kennt diese Bremse nicht.
 *
 * DER SPAEHER ist die Ausnahme: er kommt nicht aus dem Schwarm, sondern mit eigener
 * Wahrscheinlichkeit obendrauf, und nur wenn genug Russ da ist. Aus einem radioaktiven
 * Gelege kommt keiner.
 *
 * SIEBEN VERSUCHE JE WESEN: das Original probiert sieben Hoehen durch (zwei unter dem
 * Gelege bis vier darueber) und setzt das Wesen an die erste, an der es stehen kann.
 */
public class GlyphidSpawnerBlockEntity extends BlockEntity {

    /** Die Wahrscheinlichkeiten des Originals: Grundwert, Russ-Aufschlag, Mindestruss. */
    private record Brut(Supplier<EntityType<? extends Glyphid>> art, int grund, int aufschlag, int mindestRuss) { }

    private static final List<Brut> BRUTTAFEL = List.of(
            new Brut(NtmEntityTypes.GLYPHID::get,             50, -40,  0),
            new Brut(NtmEntityTypes.GLYPHID_BOMBARDIER::get,  20, -15,  1),
            new Brut(NtmEntityTypes.GLYPHID_BRAWLER::get,      5,  35,  1),
            new Brut(NtmEntityTypes.GLYPHID_DIGGER::get,     -15,  25,  5),
            new Brut(NtmEntityTypes.GLYPHID_BLASTER::get,    -15,  40,  5),
            new Brut(NtmEntityTypes.GLYPHID_BEHEMOTH::get,   -30,  45, 10),
            new Brut(NtmEntityTypes.GLYPHID_BRENDA::get,     -50,  60, 20),
            new Brut(NtmEntityTypes.GLYPHID_NUCLEAR::get,    -50,  60, 50));

    /** Der erste Schwarm kommt sofort, nicht erst nach zwei Minuten. */
    private boolean ersterSchwarm = true;

    public GlyphidSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.GLYPHID_SPAWNER.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {

        if(level.getDifficulty() == Difficulty.PEACEFUL) return;

        int abstand = NtmConfig.COMMON.SWARM_COOLDOWN.get();
        if(!this.ersterSchwarm && level.getGameTime() % abstand != 0) return;

        boolean warErster = this.ersterSchwarm;
        this.ersterSchwarm = false;

        /* Erste Bremse: die Gesamtzahl in der Welt. */
        if(this.zuVieleGlyphiden(level)) return;

        int unterart = state.getBlock() instanceof GlyphidSpawnerBlock gelege ? gelege.subtype() : Glyphid.TYPE_NORMAL;

        List<Glyphid> daneben = level.getEntitiesOfClass(Glyphid.class,
                new AABB(pos.getX() - 5, pos.getY() + 1, pos.getZ() - 5, pos.getX() + 6, pos.getY() + 7, pos.getZ() + 6));

        /* Zweite Bremse: schon drei daneben. Das radioaktive Gelege kennt sie nicht. */
        if(daneben.size() > 3 && unterart != Glyphid.TYPE_RADIOACTIVE) return;

        float russ = PollutionHandler.getPollution(level, pos, PollutionType.SOOT);

        for(Glyphid glyphid : this.schwarmBilden(level, russ, unterart)) {
            this.versuchenZuSetzen(level, pos, glyphid);
        }

        /* Der Spaeher kommt obendrauf -- aber nicht beim allerersten Schwarm. */
        if(warErster) return;
        if(unterart == Glyphid.TYPE_RADIOACTIVE) return;
        if(russ < NtmConfig.COMMON.SCOUT_THRESHOLD.get()) return;
        if(level.getRandom().nextInt(NtmConfig.COMMON.SCOUT_SWARM_SPAWN_CHANCE.get() + 1) != 0) return;

        Glyphid spaeher = NtmEntityTypes.GLYPHID_SCOUT.get().create(level);
        if(spaeher == null) return;
        if(unterart == Glyphid.TYPE_INFECTED) spaeher.setSubtype(Glyphid.TYPE_INFECTED);
        this.versuchenZuSetzen(level, pos, spaeher);
    }

    /** Zaehlt die Glyphiden der Welt und bricht ab, sobald die Obergrenze erreicht ist. */
    private boolean zuVieleGlyphiden(Level level) {

        int grenze = (int) (double) NtmConfig.COMMON.GLYPHID_SPAWN_MAX.get();
        if(!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return false;

        int zahl = 0;
        for(net.minecraft.world.entity.Entity e : serverLevel.getAllEntities()) {
            if(!(e instanceof Glyphid)) continue;
            zahl++;
            if(zahl >= grenze) return true;
        }
        return false;
    }

    /**
     * Baut den Schwarm. Die Schleife geht die Tafel so lange durch, bis genug beisammen ist
     * -- hoechstens hundert Durchgaenge, sonst haengt sie bei ungluecklichen Werten ewig.
     * Beides steht so im Original.
     */
    private List<Glyphid> schwarmBilden(Level level, float russ, int unterart) {

        List<Glyphid> schwarm = new ArrayList<>();

        int grundgroesse = NtmConfig.COMMON.BASE_SWARM_SIZE.get();
        double faktor = NtmConfig.COMMON.SWARM_SCALING_MULT.get();
        int schritt = NtmConfig.COMMON.SOOT_STEP.get();

        int groesse = (int) Math.min(grundgroesse * Math.max(faktor * (russ / schritt), 1), 10);
        int notbremse = 100;

        while(schwarm.size() <= groesse && notbremse >= 0) {

            for(Brut brut : BRUTTAFEL) {

                int gewichtet = (int) (brut.grund() + (brut.aufschlag() - brut.aufschlag() / Math.max((russ + 1) / 3, 1)));
                if(russ < brut.mindestRuss()) continue;
                if(level.getRandom().nextInt(100) > gewichtet) continue;

                Glyphid wesen = brut.art().get().create(level);
                if(wesen == null) continue;
                if(unterart != Glyphid.TYPE_NORMAL) wesen.setSubtype(unterart);
                schwarm.add(wesen);
            }

            notbremse--;
        }

        return schwarm;
    }

    /** Sieben Hoehen durchprobieren, an der ersten brauchbaren absetzen. */
    private void versuchenZuSetzen(Level level, BlockPos pos, Glyphid glyphid) {

        double versatzX = glyphid.getRandom().nextGaussian() * 3;
        double versatzZ = glyphid.getRandom().nextGaussian() * 3;

        for(int i = 0; i < 7; i++) {
            glyphid.moveTo(pos.getX() + 0.5 + versatzX, pos.getY() - 2 + i, pos.getZ() + 0.5 + versatzZ,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            if(glyphid.checkSpawnObstruction(level)) {
                level.addFreshEntity(glyphid);
                return;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("ersterSchwarm", this.ersterSchwarm);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ersterSchwarm = tag.getBoolean("ersterSchwarm");
    }
}
