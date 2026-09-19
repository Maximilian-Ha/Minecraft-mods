package com.hbm.blockentity.machine;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityDemonLamp.
 *
 * Eine Lampe mit einem Daemonenkern darin. Sie bestrahlt alles Lebende im Umkreis von
 * fuenfundzwanzig Bloecken -- aber nicht gleichmaessig: der Strahl wird von allem gebremst,
 * was zwischen der Lampe und dem Getroffenen steht, und zwar nach der Sprengfestigkeit der
 * Bloecke auf der Sichtlinie. Hundert Rad durch eine Betonwand sind viel weniger als hundert
 * Rad durch Luft.
 *
 * Wer naeher als zwei Bloecke steht, verbrennt ausserdem.
 */
public class DemonLampBlockEntity extends BlockEntity implements ITickable {

    /** Rad je Tick im freien Feld. */
    private static final float STRAHLUNG = 100_000F;

    public static final double REICHWEITE = 25D;

    public DemonLampBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.LAMP_DEMON.get(), pos, state);
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;
        this.bestrahle(this.level);
    }

    private void bestrahle(Level level) {

        Vec3 quelle = this.worldPosition.getCenter();

        List<LivingEntity> ziele = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(quelle, quelle).inflate(REICHWEITE));

        for(LivingEntity e : ziele) {

            Vec3 richtung = new Vec3(e.getX() - quelle.x,
                    e.getY() + e.getEyeHeight() - quelle.y,
                    e.getZ() - quelle.z);

            double entfernung = richtung.length();
            Vec3 schritt = richtung.normalize();

            // Je Block auf der Sichtlinie einen Schritt, und die Sprengfestigkeit aufsummieren.
            float widerstand = 0;

            for(int i = 1; i < entfernung; i++) {
                BlockPos unterwegs = BlockPos.containing(
                        quelle.x + schritt.x * i, quelle.y + schritt.y * i, quelle.z + schritt.z * i);
                widerstand += level.getBlockState(unterwegs).getBlock().getExplosionResistance();
            }

            if(widerstand < 1) widerstand = 1;

            ContaminationUtil.contaminate(e, HazardType.RADIATION, ContaminationType.CREATIVE, STRAHLUNG / widerstand);

            if(entfernung < 2) {
                e.hurt(level.damageSources().inFire(), 100F);
            }
        }
    }

    /* Ohne @Override: getRenderBoundingBox kommt aus der NeoForge-Erweiterung von
     * BlockEntity. Das Original nimmt INFINITE_EXTENT_AABB, weil der Lichtkegel fuenfzehn
     * Bloecke weit reicht; hier steht genau diese Weite statt unendlich. */
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(15D);
    }
}
