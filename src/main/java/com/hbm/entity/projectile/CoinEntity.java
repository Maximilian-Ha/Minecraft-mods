package com.hbm.entity.projectile;

import com.hbm.entity.NtmEntityTypes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Portiert aus 1.7.10: com.hbm.entity.projectile.EntityCoin.
 *
 * Die Muenze, die die NI4NI in die Luft wirft. Sie tut selbst nichts -- sie fliegt, dreht
 * sich und wartet. Ihr ganzer Zweck ist, dass ein Strahl sie treffen kann: wo er sie trifft,
 * knickt er ab und sucht sich das naechste Ziel.
 *
 * SIE MUSS ANKLICKBAR SEIN (isPickable), sonst zaehlt sie der Schussweg nicht mit. Das ist
 * der einzige Grund fuer die Zeile; angefasst wird sie nie.
 *
 * KEIN LUFTWIDERSTAND: getAirDrag gibt 1 zurueck, die Muenze wird also nicht langsamer. Nur
 * die Schwerkraft zieht sie herunter, und die ist mit 0,02 schwaecher als beim ueblichen
 * Wurfkoerper. So steht es im Original, und es ist der Grund, warum man sie im Flug noch
 * trifft.
 *
 * AM BODEN IST SIE WEG. Das Original laesst sie beim Blocktreffer sterben; sie liegt nie
 * herum. Wesen treffen sie nicht -- sie fliegt durch sie hindurch.
 */
public class CoinEntity extends ThrowableNT {

    public CoinEntity(EntityType<? extends CoinEntity> type, Level level) {
        super(type, level);
    }

    public CoinEntity(Level level) {
        this(NtmEntityTypes.COIN.get(), level);
    }

    public CoinEntity(Level level, LivingEntity werfer) {
        this(level);
        this.setOwner(werfer);
    }

    @Override
    protected void onImpact(HitResult treffer) {
        if(treffer.getType() == HitResult.Type.BLOCK) this.discard();
    }

    @Override public boolean isPickable() { return true; }
    @Override public boolean doesImpactEntities() { return false; }
    @Override protected float getAirDrag() { return 1F; }
    @Override protected double getGravityVelocity() { return 0.02D; }
}
