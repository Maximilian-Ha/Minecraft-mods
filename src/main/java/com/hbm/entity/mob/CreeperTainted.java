package com.hbm.entity.mob;

import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.bomb.TaintBlock;
import com.hbm.config.NtmConfig;
import com.hbm.explosion.vanillant.ExplosionVNT;

import api.hbm.entity.IRadiationImmune;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityCreeperTainted.
 *
 * Der verseuchte Creeper. Er entsteht, wo ein gewoehnlicher Creeper in den Taint laeuft --
 * derselbe Zweig im Taintblock, der aus einer Teslakrabbe eine Taint-Krabbe macht. Fuenfzehn
 * Lebenspunkte, langsam, und er heilt sich jede halbe Sekunde um einen Punkt.
 *
 * SEIN KNALL RICHTET KEINEN SCHADEN AN DER WELT AN -- Sprengkraft fuenf, ohne Feuer und
 * ohne Blockschaden. Was danach kommt, ist schlimmer: er saet Taint. Als gewoehnlicher
 * Creeper fuenfundachtzig Wuerfe in einen Wuerfel von sieben Bloecken Kante, als geladener
 * zweihundertfuenfundfuenfzig in einen von fuenfzehn.
 *
 * WIE TIEF DER TAINT SITZT, haengt an der Einstellung TAINT_TRAILS -- und zwar umgekehrt,
 * wie man denken wuerde: ist sie AUS, bekommt der Taint niedrige Stufen und breitet sich
 * weiter aus; ist sie AN, hohe, und er bleibt, wo er ist. Die Zahlen sind die des
 * Originals.
 *
 * ER SAET AUCH IN GRUNDGESTEIN. Das Original fragt nur nach isNormalCube und nicht nach
 * Luft; Grundgestein ist beides. Der Port fragt mit isSolidRender dasselbe. (Das rote Fass
 * im Port nimmt Grundgestein aus -- der Creeper des Originals tut das nicht, und hier
 * steht das Original.)
 *
 * NICHT UEBERNOMMEN: hasPosNeightbour. Die Methode steht im Original am Ende der Klasse
 * und wird von niemandem gerufen -- auch nicht anderswo im Original.
 */
public class CreeperTainted extends Creeper implements IRadiationImmune {

    public CreeperTainted(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D);
    }

    @Override
    public void tick() {
        super.tick();

        if(this.isAlive() && this.getHealth() < this.getMaxHealth() && this.tickCount % 10 == 0) {
            this.heal(1.0F);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);

        this.spawnAtLocation(Items.TNT);
    }

    /**
     * Das Original ueberschreibt func_146077_cc -- die Methode, die der Creeper aufruft,
     * wenn sein Zaehler abgelaufen ist. In 1.21 heisst sie explodeCreeper und ist privat;
     * der Port haengt sich darum an ignite(), genau wie der nukleare Creeper es tut.
     */
    @Override
    public void ignite() {
        super.ignite();

        this.dead = true;
        this.verseuchen();
        this.triggerOnDeathMobEffects(RemovalReason.KILLED);
        this.discard();
    }

    private void verseuchen() {

        Level level = this.level();
        if(level.isClientSide) return;

        ExplosionVNT.newExplosion(level, this, this.getX(), this.getY(), this.getZ(), 5.0F, false, false);

        if(!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;

        RandomSource rand = level.random;
        boolean geladen = this.isPowered();

        int wuerfe = geladen ? 255 : 85;
        int kante = geladen ? 15 : 7;
        int versatz = geladen ? 7 : 3;

        for(int i = 0; i < wuerfe; i++) {

            BlockPos ziel = new BlockPos(
                    rand.nextInt(kante) - versatz + this.getBlockX(),
                    rand.nextInt(kante) - versatz + this.getBlockY(),
                    rand.nextInt(kante) - versatz + this.getBlockZ());

            BlockState zustand = level.getBlockState(ziel);
            if(!zustand.isSolidRender(level, ziel)) continue;

            int stufe;
            if(geladen) {
                stufe = NtmConfig.SERVER.TAINT_TRAILS.get() ? rand.nextInt(3) : rand.nextInt(3) + 5;
            } else {
                stufe = NtmConfig.SERVER.TAINT_TRAILS.get() ? rand.nextInt(3) + 4 : rand.nextInt(6) + 10;
            }

            level.setBlock(ziel, NtmBlocks.TAINT.get().defaultBlockState().setValue(TaintBlock.TAINT_LEVEL, stufe), 2);
        }
    }
}
