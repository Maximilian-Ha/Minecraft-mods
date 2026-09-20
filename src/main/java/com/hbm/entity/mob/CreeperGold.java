package com.hbm.entity.mob;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorBulkie;
import com.hbm.explosion.vanillant.standard.BlockMutatorBulkie;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.items.NtmItems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityCreeperGold.
 *
 * Der Goldcreeper. Kein Schaden, kein Feuer -- er verwandelt, was er trifft, in Golderz.
 * Sieben Bloecke weit, als geladener vierzehn. Wer ihn erschlaegt, bekommt Goldkristalle;
 * wer ihn hochgehen laesst, eine Wand voll Erz.
 *
 * ER ERSCHEINT NUR TIEF UND NUR IN DER OBERWELT. Die Hoehe prueft er selbst -- das
 * Original tut das in getCanSpawnHere, in 1.21 ist checkSpawnRules die Stelle dafuer.
 * Die Oberwelt steht nicht hier, sondern im Biom-Aenderer: dort haengt sein Erscheinen an
 * BiomeTags.IS_OVERWORLD, und damit kann er anderswo gar nicht erst auftauchen. Eine
 * zweite Pruefung waere toter Code.
 */
public class CreeperGold extends Creeper {

    /** Tiefer als hier erscheint er nicht. */
    public static final int HOECHSTE_HOEHE = 40;

    public CreeperGold(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return super.checkSpawnRules(level, spawnType) && this.getY() <= HOECHSTE_HOEHE;
    }

    /**
     * Der Aufruf bleibt hier, damit die Schallplatte des Creepers erhalten bleibt: in 1.21
     * steckt sie in Creeper.dropCustomDeathLoot, und das Original laesst sie ebenfalls
     * stehen (es ueberschreibt nur dropFewItems). Was der Creeper darueber hinaus fallen
     * laesst, steht in beute().
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.beute(recentlyHit);
    }

    /**
     * Die Goldkristalle. Das Original wuerfelt sie in dropFewItems aus: vom Spieler
     * erschlagen fuenf plus bis zu fuenf weitere (mehr mit Pluenderung), sonst drei. Die
     * Pluenderungsverzauberung geht in 1.21 nicht mehr durch diesen Aufruf.
     */
    protected void beute(boolean vomSpieler) {
        int anzahl = vomSpieler ? 5 + this.random.nextInt(6) : 3;
        for(int i = 0; i < anzahl; i++) this.spawnAtLocation(NtmItems.CRYSTAL_GOLD.get());
    }

    /**
     * Das Original ueberschreibt func_146077_cc -- die Methode am Ende des Zaehlers. In
     * 1.21 ist sie privat; warum der Port stattdessen den Zaehler selbst abliest, steht in
     * CreeperFuse.
     */
    @Override
    public void tick() {
        super.tick();

        if(CreeperFuse.abgebrannt(this, this.zuendzeit())) {
            this.dead = true;
            this.verwandeln();
            this.triggerOnDeathMobEffects(RemovalReason.KILLED);
            this.discard();
        }
    }

    /** Wie lange die Lunte brennt. Der Phosgencreeper ist der einzige mit einer kuerzeren. */
    protected int zuendzeit() {
        return CreeperFuse.SPANNE;
    }

    protected void verwandeln() {

        float staerke = this.isPowered() ? 14 : 7;

        new ExplosionVNT(this.level(), this.getX(), this.getY(), this.getZ(), staerke, this)
                .setBlockAllocator(new BlockAllocatorBulkie(60, this.isPowered() ? 32 : 16))
                .setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorBulkie(this.umwandlung())))
                .setEntityProcessor(new EntityProcessorStandard().withRangeMod(0.5F))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setSFX(new ExplosionEffectStandard())
                .explode();
    }

    /** Woraus die Wand wird. Der fluechtige Creeper macht daraus gesprungene Schlacke. */
    protected BlockState umwandlung() {
        return Blocks.GOLD_ORE.defaultBlockState();
    }
}
