package com.hbm.blocks.machine.rbmk;

import com.hbm.blockentity.machine.rbmk.RBMKControlBlockEntity;
import com.hbm.blocks.DummyBlockType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.machine.rbmk.RBMKControl.
 *
 * Die Steuerstabsaeule. Der Deckel gehoert fest dazu und laesst sich nicht abnehmen.
 */
public class RBMKControlBlock extends RBMKBaseBlock {

    /* Im Original ist der Steuerstab eine RBMKPipedBase -- vier Rohrstutzen oben -- und steht
     * zugleich in RBMKBase.hasOwnLid(): er nimmt keinen Deckel an und zeigt nie eine
     * Deckeltextur. Das gilt ueber RBMKControlAutoBlock auch fuer die selbsttaetigen Staebe. */
    @Override public boolean hasPipes() { return true; }
    @Override public boolean hasOwnBottom() { return this.powered; }
    @Override public boolean hasOwnLid() { return true; }

    public static final MapCodec<RBMKControlBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("moderated").forGetter(block -> block.moderated),
            Codec.BOOL.optionalFieldOf("powered", false).forGetter(block -> block.powered),
            propertiesCodec()
    ).apply(instance, RBMKControlBlock::new));

    public final boolean moderated;
    /** Die ReaSim-Bauformen fahren nur mit Strom von unten. */
    public final boolean powered;

    public RBMKControlBlock(boolean moderated, Properties properties) {
        this(moderated, false, properties);
    }

    public RBMKControlBlock(boolean moderated, boolean powered, Properties properties) {
        super(properties);
        this.moderated = moderated;
        this.powered = powered;
    }

    @Override
    protected MapCodec<? extends RBMKControlBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(state.getValue(TYPE) != DummyBlockType.CORE) return null;
        return new RBMKControlBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.standardOpenBehavior(level, pos, player);
    }
}
