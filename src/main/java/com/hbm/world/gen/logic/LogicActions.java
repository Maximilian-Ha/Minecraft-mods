package com.hbm.world.gen.logic;

import com.hbm.blockentity.SkeletonHolderBlockEntity;
import com.hbm.blockentity.WandLogicBlockEntity;
import com.hbm.blockentity.machine.LockableBaseBlockEntity;
import com.hbm.blocks.NtmBlocks;
import com.hbm.items.NtmItems;
import com.hbm.util.MobUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.world.gen.util.LogicBlockActions.
 *
 * NACHGEZAEHLT ueber die drei Bauwerke mit Logikstaeben (Kran, Fabrik, Turmsockel) nennen sie
 * ZEHN Aktionen, von denen das Original neun aufloesen kann. Sechs davon stehen hier:
 *
 *   COLLAPSE_ROOF_RAD_5   die Decke faellt herunter
 *   POWER_LOCK            der Tresor nebenan schliesst sich zu
 *   DEAD_GUY_CRANE        aus dem Stab wird ein Skeletthalter mit einer Waffe
 *   ZOMBIE_TIER_1/2       drei Zombies, ausgeruestet aus den Listen von MobUtil
 *   SKELETON_GUN_TIER_1   drei Skelette mit Waffe und Fernkampfruestung
 *
 * DIE UEBRIGEN DREI warten auf Teile, die der Port noch nicht hat, und sind hier deshalb
 * NICHT eingetragen -- ein Stab mit ihrem Namen verschwindet, genau wie im Original einer mit
 * einem unbekannten Namen:
 *
 *   SKELETON_GUN_TIER_2/3   das KI-Ziel EntityAIFireGun (ohne es stuende ein Skelett mit
 *                           einer Waffe da, die es nie abfeuert -- schlimmer als keines)
 *   BOMB_CRANE              die C4-Ladung mit Zeitzuender
 *
 * DIE ZEHNTE, DEAD_GUY_BASE_TOWER, kommt nie: im Original ist die einzige Zeile, die sie
 * anmelden wuerde, auskommentiert UND anders geschrieben (LogicBlockActions.java:537). Der
 * Logikstab im Turmsockel loescht sich dort also selbst, und hier tut er dasselbe.
 */
public class LogicActions {

    private static final Map<String, Consumer<WandLogicBlockEntity>> AKTIONEN = Map.of(
            "COLLAPSE_ROOF_RAD_5", LogicActions::deckeFaellt,
            "POWER_LOCK", LogicActions::stromschloss,
            "DEAD_GUY_CRANE", LogicActions::toterAmKran,
            "ZOMBIE_TIER_1", be -> mobs(be, EntityType.ZOMBIE, MobUtil.GEWOEHNLICH, null),
            "ZOMBIE_TIER_2", be -> mobs(be, EntityType.ZOMBIE, MobUtil.FORTGESCHRITTEN, null),
            "SKELETON_GUN_TIER_1", be -> mobs(be, EntityType.SKELETON, MobUtil.WAFFEN_1, MobUtil.FERNKAMPF));

    /** Null, wenn der Name unbekannt ist -- dann loescht sich der Stab, wie im Original. */
    public static Consumer<WandLogicBlockEntity> finde(String name) {
        return AKTIONEN.get(name);
    }

    /**
     * Die Decke faellt: jeder Block in einer Kugel um den Stab wird zu einem fallenden Block,
     * sofern seine Sprengfestigkeit hoechstens 70 betraegt.
     *
     * DIE KUGEL IST KEINE KUGEL. Das Original laeuft von -4 bis unter +4 und vergleicht das
     * Abstandsquadrat mit r*r/2, also 8 statt 16 -- die Reichweite ist damit rund 2,8 statt 4,
     * und die Schleife ist um einen halben Block versetzt. Beides uebernommen, weil das
     * Ergebnis genau die Deckenform ist, die man im Spiel sieht.
     *
     * FALLENDE BLOECKE: das Original braucht dafuer eine eigene Entitaet, weil Vanilla 1.7.10
     * nur Sand und Kies fallen laesst. In 1.21 nimmt FallingBlockEntity.fall jeden Zustand
     * mit, also genuegt Vanilla.
     */
    private static void deckeFaellt(WandLogicBlockEntity be) {

        if(be.phase == 0) return;

        Level level = be.getLevel();
        BlockPos mitte = be.getBlockPos();
        int r = 4;
        int grenze = r * r / 2;

        for(int dx = -r; dx < r; dx++) {
            for(int dy = -r; dy < r; dy++) {
                for(int dz = -r; dz < r; dz++) {

                    if(dx * dx + dy * dy + dz * dz >= grenze) continue;

                    BlockPos pos = mitte.offset(dx, dy, dz);
                    BlockState zustand = level.getBlockState(pos);
                    if(zustand.isAir()) continue;
                    if(zustand.getBlock().getExplosionResistance() > 70F) continue;

                    FallingBlockEntity.fall(level, pos, zustand);
                }
            }
        }

        level.setBlock(mitte, Blocks.AIR.defaultBlockState(), 3);
    }

    /**
     * Das Stromschloss: steht ein Spieler in der Naehe, meldet sich der Stab einmal und
     * verriegelt den Tresor nebenan mit einer Zufallszahl. Aufgeschlossen wird er ueber die
     * Wechselwirkung desselben Namens -- siehe LogicInteractions.
     */
    private static void stromschloss(WandLogicBlockEntity be) {

        if(be.phase != 0) return;

        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();

        AABB wuerfel = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() - 2, pos.getZ() + 1).inflate(3);
        List<Player> spieler = level.getEntitiesOfClass(Player.class, wuerfel);
        if(spieler.isEmpty()) return;

        spieler.get(0).displayClientMessage(Component.literal(ChatFormatting.LIGHT_PURPLE
                + "[POWER LOCK]" + ChatFormatting.RESET + " Low Power Warning! Locking Safe"), false);
        be.phase++;

        LockableBaseBlockEntity tresor = nachbarTresor(level, pos);
        if(tresor != null) {
            tresor.setPins(level.random.nextInt(999));
            tresor.lock();
        }
    }

    /**
     * Der Tote am Kran: aus dem Stab wird ein Skeletthalter. Was er in der Hand haelt, haengt
     * davon ab, ob der naechste Spieler den Hangman schon besitzt -- hat er ihn, bekommt das
     * Skelett eine Tontafel, sonst die Waffe.
     */
    private static void toterAmKran(WandLogicBlockEntity be) {

        if(be.phase != 1) return;

        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();

        level.setBlock(pos, NtmBlocks.SKELETON_HOLDER.get().defaultBlockState(), 3);

        BlockEntity halter = level.getBlockEntity(pos);
        if(!(halter instanceof SkeletonHolderBlockEntity skelett)) return;

        AABB wuerfel = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() - 2, pos.getZ() + 1).inflate(25);
        List<Player> spieler = level.getEntitiesOfClass(Player.class, wuerfel);

        boolean hatWaffe = !spieler.isEmpty()
                && spieler.get(0).getInventory().contains(new ItemStack(NtmItems.GUN_HANGMAN.get()));

        skelett.item = new ItemStack(hatWaffe ? NtmItems.CLAY_TABLET.get() : NtmItems.GUN_HANGMAN.get());
        skelett.setChanged();
    }

    /**
     * Die Mob-Aktionen: drei Stueck derselben Art, ausgeruestet aus den Listen von MobUtil,
     * und der Stab verschwindet dabei.
     *
     * DREIMAL AN DERSELBEN STELLE, und das ist kein Versehen des Originals: die Schleife
     * laeuft dreimal, setzt jedes Mal einen Mob auf denselben Punkt und ueberschreibt danach
     * den Stab mit Luft. Dass die drei uebereinander stehen, loest Vanilla selbst auf -- sie
     * schieben sich auseinander, sobald sie ticken.
     *
     * NUR IN PHASE 1: die Bedingung muss also einmal zugetroffen haben. Bei den Fallen des
     * Kranes und der Fabrik heisst das: ein Spieler war nah genug.
     */
    private static void mobs(WandLogicBlockEntity be, EntityType<? extends Mob> art,
            Map<Integer, List<MobUtil.Eintrag>> handListe, Map<Integer, List<MobUtil.Eintrag>> ruestungsListe) {

        if(be.phase != 1) return;

        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();
        if(!(level instanceof ServerLevel serverLevel)) return;

        for(int i = 0; i < 3; i++) {

            Mob mob = art.create(serverLevel);
            if(mob == null) return;

            mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0F, 0F);
            MobUtil.ausruesten(mob, handListe, serverLevel.random);
            if(ruestungsListe != null) MobUtil.ausruesten(mob, ruestungsListe, serverLevel.random);
            serverLevel.addFreshEntity(mob);
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    /** Der erste Tresor in den sechs Nachbarfeldern, oder null. */
    static LockableBaseBlockEntity nachbarTresor(Level level, BlockPos pos) {
        for(Direction richtung : Direction.values()) {
            if(level.getBlockEntity(pos.relative(richtung)) instanceof LockableBaseBlockEntity tresor) return tresor;
        }
        return null;
    }
}
