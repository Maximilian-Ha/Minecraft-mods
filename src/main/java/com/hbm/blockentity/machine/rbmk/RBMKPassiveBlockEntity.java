package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.rbmk.RBMKPassiveBlock;
import com.hbm.entity.projectile.RBMKDebris.DebrisType;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: TileEntityRBMKBlank, TileEntityRBMKModerator, TileEntityRBMKAbsorber und
 * TileEntityRBMKReflector.
 *
 * Die vier Saeulen ohne eigene Mechanik unterscheiden sich nur durch ihren Typ und durch die
 * Truemmer, die sie bei der Kernschmelze auswerfen. Beides liest diese eine Klasse am Block ab.
 */
public class RBMKPassiveBlockEntity extends RBMKBaseBlockEntity {

    public RBMKPassiveBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_PASSIVE.get(), pos, state);
    }

    @Override
    public RBMKColumnType getConsoleType() {
        return switch(this.getRBMKType()) {
            case MODERATOR -> RBMKColumnType.MODERATOR;
            case ABSORBER -> RBMKColumnType.ABSORBER;
            case REFLECTOR -> RBMKColumnType.REFLECTOR;
            default -> RBMKColumnType.BLANK;
        };
    }

    /** Der Moderator wirft Graphit aus, alle uebrigen Stahltraeger. */
    @Override
    public void onMelt(int reduce) {

        if(this.level != null && !this.level.isClientSide) {

            boolean graphite = this.getRBMKType() == RBMKType.MODERATOR;
            int count = (graphite ? 2 : 1) + this.level.random.nextInt(2);

            for(int i = 0; i < count; i++) this.spawnDebris(graphite ? DebrisType.GRAPHITE : DebrisType.BLANK);
        }

        super.onMelt(reduce);
    }

    @Override
    public RBMKType getRBMKType() {
        if(this.getBlockState().getBlock() instanceof RBMKPassiveBlock block) return block.type;
        return RBMKType.OTHER;
    }
}
