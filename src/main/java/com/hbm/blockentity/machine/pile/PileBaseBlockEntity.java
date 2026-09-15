package com.hbm.blockentity.machine.pile;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.util.Compat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.pile.TileEntityPileBaseMK2.
 *
 * Jeder Block eines zusammengebauten Chicago Pile ausser dem Kern haengt hieran. Er merkt sich
 * nur, wo der Kern steht, und faellt zu Graphit zurueck, sobald der Kern verschwunden ist.
 *
 * ABWEICHUNG: das Original prueft ueber den Chunk-Provider, ob das Stueck Welt um den Kern
 * geladen ist, und laesst den Block sonst in Ruhe. Auf 1.21 leistet Level.isLoaded dasselbe.
 */
public class PileBaseBlockEntity extends BlockEntity implements ITickable {

    /** Ungueltige Hoehe -- heisst: dieser Block gehoert (noch) zu keiner Anlage. */
    public static final int NO_CORE = -999;

    @Nullable protected PileCoreBlockEntity cachedCore;

    public BlockPos core = BlockPos.ZERO;
    public boolean hasCore = false;

    public PileBaseBlockEntity(BlockPos pos, BlockState state) {
        this(NtmBlockEntityTypes.PILE_BASE.get(), pos, state);
    }

    protected PileBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public PileBaseBlockEntity setCore(BlockPos pos) {
        this.core = pos;
        this.hasCore = true;
        this.setChanged();
        return this;
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;
        if(!this.hasCore) return;

        PileCoreBlockEntity core = this.getCore();

        /* Nur zerfallen, wenn das Stueck Welt um den Kern wirklich da ist. */
        if((core == null || core.isRemoved()) && this.level.isLoaded(this.core)) {
            this.level.setBlock(this.worldPosition, com.hbm.blocks.NtmBlocks.PILE_BRICK.get().defaultBlockState(), 3);
        }
    }

    @Nullable
    public PileCoreBlockEntity getCore() {

        if(this.cachedCore != null && !this.cachedCore.isRemoved()) return this.cachedCore;
        if(this.level == null || !this.hasCore || !this.level.isLoaded(this.core)) return null;

        if(Compat.getBlockEntityStandard(this.level, this.core) instanceof PileCoreBlockEntity core) {
            this.cachedCore = core;
            return core;
        }

        return null;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        this.core = BlockPos.of(tag.getLong("core"));
        this.hasCore = tag.getBoolean("hasCore");
        this.cachedCore = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("core", this.core.asLong());
        tag.putBoolean("hasCore", this.hasCore);
    }
}
