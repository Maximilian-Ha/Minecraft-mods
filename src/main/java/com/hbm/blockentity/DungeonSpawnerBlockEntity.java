package com.hbm.blockentity;

import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.mob.UndeadSoldier;
import com.hbm.items.ItemEnums.SecretType;
import com.hbm.inventory.MetaHelper;
import com.hbm.items.NtmItems;
import com.hbm.util.Vec3NT;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: die eingebettete Klasse DungeonSpawner.TileEntityDungeonSpawner.
 *
 * DER AUFRUFER. Er liegt in einem Bauwerk und tut nichts, bis ein Spieler nahe genug kommt
 * -- dann stellt er zweimal zehn Untote Soldaten im Kreis auf, wartet jedes Mal, bis keiner
 * mehr lebt, und legt zuletzt eine Belohnung in den Skeletthalter achtzehn Bloecke ueber
 * sich. Danach macht er sich selbst zu Obsidian: das Ritual geschieht genau einmal.
 *
 * VIER PHASEN, und die Bedingung fuer den Uebergang ist je eine andere:
 *
 *   0 -> 1  ein Spieler im Umkreis von zwanzig Bloecken (jede Sekunde geprueft)
 *   1 -> 2  kein Soldat mehr in der Naehe, und mindestens drei Sekunden vergangen
 *   2 -> 3  dasselbe noch einmal
 *   3       Belohnung und Obsidian
 *
 * Bei Friedlich passiert nichts -- die Soldaten wuerden ohnehin verschwinden.
 *
 * DER ZEHNERKREIS wird wie im Original gerechnet: ein Vektor der Laenge zehn, zehnmal um
 * sechsunddreissig Grad gedreht. Und je Soldat werden sieben Anlaeufe unternommen, einen
 * freien Platz zu finden -- genau siebenmal derselbe, was im Original so dasteht und hier
 * so bleibt: es ist die Stelle, an der ein Soldat schlicht ausfaellt, wenn dort kein Platz
 * ist.
 */
public class DungeonSpawnerBlockEntity extends BlockEntity {

    public int phase = 0;
    public int timer = 0;

    public DungeonSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.DUNGEON_SPAWNER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DungeonSpawnerBlockEntity rufer) {

        rufer.wirke(level, pos);

        if(rufer.phaseVorbei(level, pos)) {
            rufer.phase++;
            rufer.timer = 0;
        } else {
            rufer.timer++;
        }
        rufer.setChanged();
    }

    /** Was in der laufenden Phase geschieht. */
    private void wirke(Level level, BlockPos pos) {

        if((this.phase == 1 || this.phase == 2) && this.timer == 0) {
            this.stelleSoldatenAuf(level, pos);
        }

        if(this.phase > 2) {
            this.belohne(level, pos);
            level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
        }
    }

    /** Ob die laufende Phase zu Ende ist. */
    private boolean phaseVorbei(Level level, BlockPos pos) {

        if(level.getDifficulty() == Difficulty.PEACEFUL) return false;

        if(this.phase == 0) {
            if(level.getGameTime() % 20 != 0) return false;
            AABB umkreis = new AABB(pos).inflate(20, 10, 20);
            return !level.getEntitiesOfClass(Player.class, umkreis).isEmpty();
        }

        if(this.phase < 3) {
            if(level.getGameTime() % 20 != 0 || this.timer < 60) return false;
            AABB umkreis = new AABB(pos).inflate(50, 20, 50);
            return level.getEntitiesOfClass(UndeadSoldier.class, umkreis).isEmpty();
        }

        return false;
    }

    private void stelleSoldatenAuf(Level level, BlockPos pos) {

        if(!(level instanceof ServerLevel serverLevel)) return;

        Vec3NT richtung = new Vec3NT(10, 0, 0);

        for(int i = 0; i < 10; i++) {
            for(int versuch = 0; versuch < 7; versuch++) {

                UndeadSoldier soldat = NtmEntityTypes.UNDEAD_SOLDIER.get().create(level);
                if(soldat == null) break;

                soldat.moveTo(pos.getX() + 0.5 + richtung.xCoord, pos.getY() - 5, pos.getZ() + 0.5 + richtung.zCoord,
                        i * 36F, 0F);

                /* checkSpawnObstruction ist das Gegenstueck zu getCanSpawnHere: kein Block
                 * im Weg, keine Fluessigkeit. */
                if(soldat.checkSpawnObstruction(level)) {
                    soldat.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(soldat.blockPosition()),
                            MobSpawnType.SPAWNER, null);
                    level.addFreshEntity(soldat);
                    break;
                }
                soldat.discard();
            }

            richtung.rotateAroundYDeg(36D);
        }
    }

    /**
     * Die Belohnung auf dem Skeletthalter achtzehn Bloecke darueber: mit einem Fuenftel ein
     * Aberrator-Teil, sonst die dunkle Tontafel. Beides ist sonst nirgends zu bekommen --
     * das Teil baut den Aberrator, die Tafel zeigt, wie.
     */
    private void belohne(Level level, BlockPos pos) {

        BlockPos halter = pos.above(18);
        if(!(level.getBlockEntity(halter) instanceof SkeletonHolderBlockEntity sockel)) return;

        sockel.item = level.random.nextInt(5) == 0
                ? MetaHelper.newStack(NtmItems.ITEM_SECRET.get(), 1, SecretType.ABERRATOR.ordinal())
                : MetaHelper.newStack(NtmItems.CLAY_TABLET.get(), 1, 1);

        sockel.setChanged();
        level.sendBlockUpdated(halter, level.getBlockState(halter), level.getBlockState(halter), 3);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.phase = tag.getInt("phase");
        this.timer = tag.getInt("timer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("phase", this.phase);
        tag.putInt("timer", this.timer);
    }
}
