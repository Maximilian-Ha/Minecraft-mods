package com.hbm.blockentity.machine.pile;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blocks.machine.pile.PileDeviceBlock;
import com.hbm.util.Compat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.pile.TileEntityPileDeviceBase.
 *
 * Was alle drei Geraete des Chicago Pile gemeinsam haben: sie sitzen an einem Kanal, merken sich
 * dessen Nummer, und finden ueber den angrenzenden Pile-Block den Kern.
 */
public abstract class PileDeviceBaseBlockEntity extends LoadedBaseBlockEntity implements ITickable {

    /** Nummer des Kanals, an dem das Geraet haengt -- nur zur Anzeige. */
    public int chanNum;

    protected PileDeviceBaseBlockEntity(BlockEntityType<? extends LoadedBaseBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** In welche Richtung das Geraet schaut. Der Kanal liegt entgegengesetzt dahinter. */
    public Direction getOrientation() {
        return this.getBlockState().getValue(PileDeviceBlock.FACING);
    }

    /** Der Kern der Anlage, an der dieses Geraet haengt, oder null. */
    @Nullable
    protected PileCoreBlockEntity getCore(BlockPos pilePos) {
        if(this.level == null) return null;
        if(Compat.getBlockEntityStandard(this.level, pilePos) instanceof PileBaseBlockEntity pile) return pile.getCore();
        return null;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.chanNum);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.chanNum = buf.readInt();
    }
}
