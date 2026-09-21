package com.hbm.blocks.bomb;

import com.hbm.entity.item.TNTPrimedBase;
import com.hbm.explosion.ExplosionNukeSmall;
import net.minecraft.world.level.Level;

/**
 * Die Spaltbombe. Sie hinterlaesst genau die mittlere kleine Atomexplosion des Originals
 * (ExplosionNukeSmall.PARAMS_MEDIUM): Druckwelle 20, Todeskreis 55, Strahlungsstufe 3.
 *
 * RUNDE 282: die fuenf Schritte standen hier ausgeschrieben. Seit das UFO dieselbe Explosion
 * braucht, stehen sie einmal in ExplosionNukeSmall.
 */
public class FissureBombBlock extends TNTBaseBlock {

    public FissureBombBlock(Properties properties) { super(properties); }

    @Override
    public void explodeEntity(Level level, double x, double y, double z, TNTPrimedBase entity) {
        ExplosionNukeSmall.mittel(level, x, y, z);
    }
}
