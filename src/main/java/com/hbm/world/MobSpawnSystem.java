package com.hbm.world;

import com.hbm.blocks.NtmBlocks;
import com.hbm.config.NtmConfig;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.mob.FbiAgent;
import com.hbm.entity.mob.Ghost;
import com.hbm.entity.mob.MaskMan;
import com.hbm.entity.mob.RadBeast;
import com.hbm.extprop.HbmLivingAttachments;
import com.hbm.extprop.HbmPlayerAttachments;
import com.hbm.lib.ModAttachments;
import com.hbm.util.ContaminationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.handler.BossSpawnHandler.
 *
 * Bis hierher standen die vier Bosse des Mods im Port zwar bereit, aber nichts brachte sie in
 * die Welt -- man kam nur mit Befehlen oder Rufgegenstaenden an sie heran. Dieser Hier ist der
 * Teil des Originals, der sie von selbst schickt.
 *
 * DREI REGELN, UND JEDE HAT EINE BEDINGUNG, DIE DER SPIELER SELBST HERSTELLT:
 *
 * DER MASKENMANN kommt nach zwanzig Minuten, in denen der Spieler ununterbrochen drei Bloecke
 * unter der Oberflaeche war, mindestens fuenfzig Rad im Blut hatte und schon einmal einen
 * Kristallisator gebaut oder gesetzt hat. Eine Minute vorher kommt eine Warnung; reisst eine
 * der drei Bedingungen ab, faengt die Uhr von vorn an.
 *
 * DIE STRAHLENBIESTER kommen nach einer Kernschmelze. Der Reaktor setzt jedem Spieler im
 * Umkreis eine Marke; alle neunzig Minuten wird gewuerfelt, und wer die Marke traegt, bekommt
 * zehn von ihnen vor die Tuer gesetzt -- das erste ist der Anfuehrer. Danach ist die Marke
 * verbraucht.
 *
 * DAS GESPENST kommt zu dem, der Digamma im Blut hat. Alle zwanzig Takte eine Chance von eins
 * zu fuenf, fuenfundsiebzig Bloecke entfernt.
 *
 * DIE FBI-RAZZIA (Runde 291) schickt fuenfzehn Beamte auf einmal, wenn die Einstellung sie
 * einschaltet -- ausgeschaltet ist sie im Original wie im Port die Voreinstellung. Sie stehen
 * alle in derselben Richtung, zweiunddreissig Bloecke entfernt, jeder mit fuenf Bloecken
 * Streuung. Die Quadrokopter, die das Original dazustellt, fehlen noch: sie brauchen
 * EntityUFOBase, und der Port fuehrt sein Ufo ohne Grundklasse.
 *
 * GEMESSEN: das Original prueft vor der Razzia eine Marke fbiMark, die eine Schonfrist von
 * zwanzig Minuten setzen soll. Geschrieben wird sie nur in markFBI -- und markFBI wird im
 * ganzen Original NIE gerufen. Die Pruefung ist damit immer wahr. Der Port fuehrt weder Marke
 * noch Pruefung.
 *
 * GEMESSEN: das Original fuehrt eine Einstellung elementalAttackDistance, liest sie aber nie --
 * der Strahlenbiest-Zweig nimmt raidAttackDistance, die Einstellung der Razzia. Beide stehen
 * auf 32, darum faellt es nie auf. Der Port fuehrt nur die eine Zahl, die wirklich zaehlt.
 */
public final class MobSpawnSystem {

    private MobSpawnSystem() { }

    public static void update(MinecraftServer server) {
        for(ServerLevel level : server.getAllLevels()) {
            maskMan(level);
            raids(level);
            elementals(level);
            ghosts(level);
        }
    }

    /** Die Uhr des Maskenmanns laeuft nur, solange alle drei Bedingungen stehen. */
    private static void maskMan(ServerLevel level) {

        if(!NtmConfig.COMMON.ENABLE_MASKMAN.get()) return;
        if(level.getGameTime() % 20 != 0) return;
        if(level.getDifficulty() == Difficulty.PEACEFUL) return;
        if(!level.dimensionType().natural()) return;

        for(ServerPlayer player : level.players()) {

            HbmPlayerAttachments data = player.getData(ModAttachments.PLAYER_ATTACHMENT);

            if(!hatKristallisator(player) || !hatStrahlung(player) || !istUnterTage(level, player)) {
                data.maskManTimer = 0;
                continue;
            }

            data.maskManTimer++;

            int delay = NtmConfig.COMMON.MASKMAN_DELAY.get();

            /* Eine Minute vor dem Besuch -- sechzig Zaehlschritte zu je zwanzig Takten. */
            if(data.maskManTimer == delay - 60) {
                player.sendSystemMessage(Component.translatable("chat.hbmsntm.maskman.near").withStyle(ChatFormatting.RED));
            }

            if(data.maskManTimer < delay) continue;

            data.maskManTimer = 0;

            double x = player.getX() + level.random.nextGaussian() * 20;
            double z = player.getZ() + level.random.nextGaussian() * 20;
            MaskMan mann = NtmEntityTypes.MASKMAN.get().create(level);

            if(mann != null && trySpawn(level, x, z, mann)) {
                player.sendSystemMessage(Component.translatable("chat.hbmsntm.maskman.spawn").withStyle(ChatFormatting.RED));
            } else {
                player.sendSystemMessage(Component.translatable("chat.hbmsntm.maskman.fail").withStyle(ChatFormatting.BLUE));
            }
        }
    }

    /** Ob der Spieler schon einmal einen Kristallisator gebaut oder gesetzt hat. */
    private static boolean hatKristallisator(ServerPlayer player) {
        var item = NtmBlocks.MACHINE_CRYSTALLIZER.asItem();
        return player.getStats().getValue(Stats.ITEM_CRAFTED.get(item)) > 0
                || player.getStats().getValue(Stats.ITEM_USED.get(item)) > 0;
    }

    private static boolean hatStrahlung(ServerPlayer player) {
        return ContaminationUtil.getRads(player) >= NtmConfig.COMMON.MASKMAN_MIN_RAD.get();
    }

    /** Drei Bloecke unter der Oberflaeche, oder die Bedingung ist abgeschaltet. */
    private static boolean istUnterTage(ServerLevel level, ServerPlayer player) {
        if(!NtmConfig.COMMON.MASKMAN_UNDERGROUND.get()) return true;
        int oberflaeche = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, player.getBlockX(), player.getBlockZ());
        return oberflaeche > player.getY() + 3;
    }

    /** Die Razzia: fuenfzehn Beamte, alle aus derselben Richtung. */
    private static void raids(ServerLevel level) {

        if(!NtmConfig.COMMON.ENABLE_RAIDS.get()) return;
        if(level.getGameTime() % NtmConfig.COMMON.RAID_DELAY.get() != 0) return;
        if(level.random.nextInt(NtmConfig.COMMON.RAID_CHANCE.get()) != 0) return;
        if(!level.dimensionType().natural()) return;

        List<ServerPlayer> players = level.players();
        if(players.isEmpty()) return;

        ServerPlayer player = players.get(level.random.nextInt(players.size()));
        player.sendSystemMessage(Component.translatable("chat.hbmsntm.raid.spawn").withStyle(ChatFormatting.RED));

        /* EIN Winkel fuer die ganze Gruppe -- das Original dreht den Vektor einmal und
         * streut danach nur noch jeden einzelnen um fuenf Bloecke. Sie kommen also
         * gemeinsam aus einer Richtung, nicht im Kreis verteilt wie die Strahlenbiester. */
        double winkel = level.random.nextFloat() * Math.PI * 2D;
        int distance = NtmConfig.COMMON.RAID_DISTANCE.get();
        double dx = Math.cos(winkel) * distance;
        double dz = Math.sin(winkel) * distance;

        for(int i = 0; i < NtmConfig.COMMON.RAID_AMOUNT.get(); i++) {

            double x = player.getX() + dx + level.random.nextGaussian() * 5D;
            double z = player.getZ() + dz + level.random.nextGaussian() * 5D;

            FbiAgent beamter = NtmEntityTypes.FBI_AGENT.get().create(level);
            if(beamter != null) trySpawn(level, x, z, beamter);
        }
    }

    /** Nach der Kernschmelze: zehn Strahlenbiester, das erste als Anfuehrer. */
    private static void elementals(ServerLevel level) {

        if(!NtmConfig.COMMON.ENABLE_ELEMENTALS.get()) return;
        if(level.getGameTime() % NtmConfig.COMMON.ELEMENTAL_DELAY.get() != 0) return;
        if(level.random.nextInt(NtmConfig.COMMON.ELEMENTAL_CHANCE.get()) != 0) return;
        if(!level.dimensionType().natural()) return;

        List<ServerPlayer> players = level.players();
        if(players.isEmpty()) return;

        ServerPlayer player = players.get(level.random.nextInt(players.size()));
        HbmPlayerAttachments data = player.getData(ModAttachments.PLAYER_ATTACHMENT);

        if(!data.radMark) return;

        player.sendSystemMessage(Component.translatable("chat.hbmsntm.elemental.near").withStyle(ChatFormatting.YELLOW));
        data.radMark = false;

        int distance = NtmConfig.COMMON.ELEMENTAL_DISTANCE.get();

        for(int i = 0; i < NtmConfig.COMMON.ELEMENTAL_AMOUNT.get(); i++) {

            double winkel = level.random.nextFloat() * Math.PI * 2D;
            double x = player.getX() + Math.cos(winkel) * distance + level.random.nextGaussian();
            double z = player.getZ() + Math.sin(winkel) * distance + level.random.nextGaussian();

            RadBeast biest = NtmEntityTypes.RAD_BEAST.get().create(level);
            if(biest == null) continue;
            if(i == 0) biest.zumAnfuehrer();

            trySpawn(level, x, z, biest);
        }
    }

    /** Das Gespenst: nur fuer den, der Digamma im Blut hat. */
    private static void ghosts(ServerLevel level) {

        if(level.getGameTime() % 20 != 0) return;
        if(level.random.nextInt(5) != 0) return;
        if(!level.dimensionType().natural()) return;

        List<ServerPlayer> players = level.players();
        if(players.isEmpty()) return;

        ServerPlayer player = players.get(level.random.nextInt(players.size()));
        if(HbmLivingAttachments.getDigamma(player) <= 0) return;

        double winkel = level.random.nextFloat() * Math.PI * 2D;
        double x = player.getX() + Math.cos(winkel) * 75D + level.random.nextGaussian();
        double z = player.getZ() + Math.sin(winkel) * 75D + level.random.nextGaussian();

        Ghost gespenst = NtmEntityTypes.GHOST.get().create(level);
        if(gespenst != null) trySpawn(level, x, z, gespenst);
    }

    /**
     * Setzt das Wesen auf die Oberflaeche an dieser Stelle. Gibt zurueck, ob es geklappt hat --
     * der Maskenmann sagt dem Spieler beides an.
     */
    private static boolean trySpawn(ServerLevel level, double x, double z, Mob mob) {

        BlockPos pos = new BlockPos((int) Math.floor(x), 0, (int) Math.floor(z));
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());

        mob.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.EVENT, null);

        return level.addFreshEntity(mob);
    }

    /** Die Marke, die eine Kernschmelze hinterlaesst. */
    public static void markiereStrahlung(Level level, BlockPos pos, double radius) {

        if(level.isClientSide) return;

        for(ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class,
                net.minecraft.world.phys.AABB.ofSize(pos.getCenter(), radius * 2, radius * 2, radius * 2))) {
            player.getData(ModAttachments.PLAYER_ATTACHMENT).radMark = true;
        }
    }
}
