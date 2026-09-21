package com.hbm.blocks.fluids;

import com.hbm.registry.NtmCriteria;
import com.hbm.registry.NtmDamageTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Portiert aus 1.7.10: der Schwefelsaeureblock, im Original ein GenericFluidBlock mit
 * gesetzter Schadensquelle (ModBlocks.java:2345 -- setDamage(ModDamageSource.acid, 5F)).
 *
 * ER IST DER EINZIGE SEINER ART. Nachgemessen: von allen GenericFluidBlock-Anmeldungen des
 * Originals bekommt genau diese eine eine Schadensquelle. Der ganze Zweig in
 * onEntityCollidedWithBlock, der Gegenstaende aufloest, gilt also nur fuer die Schwefelsaeure
 * -- deshalb steht er hier und nicht in einer gemeinsamen Oberklasse.
 *
 * WAS SIE TUT:
 *
 *   Lebewesen nehmen fuenf Schaden je Tick; wer hineinfaellt, wird auf halbe Fallgeschwindigkeit
 *   gebremst, sobald er schneller als 0,2 faellt.
 *
 *   Gegenstaende bleiben stehen -- ihre Bewegung wird auf null gesetzt -- und nehmen jede
 *   Sekunde ein Zehntel des Schadens. Loest sich dabei ein SCHLEIMBALL auf, bekommen alle
 *   Spieler im Umkreis von zehn Bloecken den Erfolg. Das ist der ganze Sinn dieser Stelle im
 *   Original, und es ist die einzige Art, ihn zu erreichen.
 *
 * DAS ZISCHEN bleibt: alle fuenf Ticks random.fizz, wie im Original. Die Wolkenpartikel des
 * Originals entfallen -- sie werden dort auf dem Server erzeugt und sind auf 1.21 ein
 * Paketthema fuer sich.
 */
public class SulfuricAcidLiquidBlock extends LiquidBlock {

    /** Fuenf Schaden, wie im Original; Gegenstaende bekommen ein Zehntel davon. */
    private static final float SCHADEN = 5F;

    public SulfuricAcidLiquidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {

        if(entity.tickCount % 5 == 0) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2F, 1F);
        }

        if(entity instanceof ItemEntity gegenstand) {

            gegenstand.setDeltaMovement(0D, 0D, 0D);

            if(level.isClientSide || entity.tickCount % 20 != 0) return;

            boolean schleim = gegenstand.getItem().is(Items.SLIME_BALL);
            gegenstand.hurt(saeure(level), SCHADEN * 0.1F);

            if(schleim && gegenstand.isRemoved()) {
                NtmCriteria.markeImUmkreis(gegenstand, 10D, "sulfuric");
            }

            return;
        }

        /* Wer hineinfaellt, wird gebremst -- das Original halbiert die Fallgeschwindigkeit. */
        if(entity.getDeltaMovement().y < -0.2D) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1D, 0.5D, 1D));
        }

        if(!level.isClientSide) entity.hurt(saeure(level), SCHADEN);
    }

    private static DamageSource saeure(Level level) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(NtmDamageTypes.ACID));
    }
}
