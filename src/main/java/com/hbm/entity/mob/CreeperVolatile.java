package com.hbm.entity.mob;

import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.generic.SolidSlagBlock;
import com.hbm.items.NtmItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.entity.mob.EntityCreeperVolatile.
 *
 * Der fluechtige Creeper. Bis auf zwei Dinge derselbe wie der Goldcreeper: er hinterlaesst
 * gesprungene Schlacke statt Golderz, und er faellt Schwefel und TNT-Stangen statt
 * Goldkristallen. Die Sprengkraft, die Reichweite und die Hoehengrenze sind dieselben --
 * darum erbt er sie und ueberschreibt nur, was sich unterscheidet.
 */
public class CreeperVolatile extends CreeperGold {

    public CreeperVolatile(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    /** Die gesprungene Schlacke -- im Original block_slag mit Metadaten 1. */
    @Override
    protected BlockState umwandlung() {
        return NtmBlocks.BLOCK_SLAG.get().defaultBlockState().setValue(SolidSlagBlock.BROKEN, true);
    }

    /**
     * Schwefel und TNT-Stangen statt Goldkristallen. Das Original wuerfelt beides in
     * dropFewItems aus, ohne auf den Spieler zu achten -- anders als beim Goldcreeper
     * faellt es also immer.
     */
    @Override
    protected void beute(boolean vomSpieler) {
        this.spawnAtLocation(new ItemStack(NtmItems.SULFUR.get(), 2 + this.random.nextInt(3)));
        this.spawnAtLocation(new ItemStack(NtmItems.STICK_TNT.get(), 1 + this.random.nextInt(2)));
    }
}
