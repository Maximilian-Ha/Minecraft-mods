package com.hbm.blockentity.machine.pile;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.pile.PileCoreBlockEntity.PileChannel;
import com.hbm.blocks.NtmBlocks;
import com.hbm.blocks.machine.pile.PileBlock;
import com.hbm.blocks.states.PileBlockType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.pile.TileEntityPileControl.
 *
 * Der Steuerstabantrieb. Er steht oben auf einem Steuerkanal und faehrt den Stab ein oder aus;
 * ein volles Durchfahren dauert drei Sekunden. Redstone an heisst herausgezogen, Redstone aus
 * heisst eingefahren -- so herum, weil ein Stromausfall die Anlage dann von selbst abschaltet.
 *
 * ABWEICHUNGEN: die OpenComputers-Anbindung und die Radio-Werte sind gestrichen
 * (ENTSCHEIDUNGEN.md).
 */
public class PileControlBlockEntity extends PileDeviceBaseBlockEntity {

    /** Ein volles Durchfahren dauert drei Sekunden. */
    public static final double SPEED = 1D / 60D;

    /** Wie weit der Stab heraussen ist. Eins heisst ganz heraus, null ganz hinein. */
    public double extraction;
    public double targetLevel;
    public boolean wasRedstone;

    /* Nur auf der Client-Seite: geglaettete Bewegung zwischen zwei Paketen. */
    public double syncLevel;
    public double lastLevel;
    public int turnProgress;

    public PileControlBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.PILE_CONTROL.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(this.level.isClientSide) {

            this.lastLevel = this.extraction;

            if(this.turnProgress > 0) {
                this.extraction += (this.syncLevel - this.extraction) / this.turnProgress;
                this.turnProgress--;
            } else {
                this.extraction = this.syncLevel;
            }

            return;
        }

        boolean canMove = false;
        BlockPos pilePos = this.worldPosition.below();
        BlockState below = this.level.getBlockState(pilePos);

        if(below.is(NtmBlocks.PILE_BLOCK.get()) && below.getValue(PileBlock.TYPE) == PileBlockType.CONTROL) {

            PileCoreBlockEntity core = this.getCore(pilePos);

            if(core != null) {
                PileChannel chan = core.getChannel(pilePos, core.controlChannels);
                if(chan != null) {
                    canMove = true;
                    this.chanNum = core.controlChannels.indexOf(chan);
                    chan.control = this.extraction;
                }
            }
        }

        if(canMove && this.extraction != this.targetLevel) {
            if(Math.abs(this.extraction - this.targetLevel) <= SPEED) this.extraction = this.targetLevel;
            else if(this.extraction < this.targetLevel) this.extraction += SPEED;
            else this.extraction -= SPEED;
        }

        /* Das Signal wird an der Rueckseite abgenommen, so wie im Original. */
        Direction dir = this.getOrientation();
        boolean redstone = this.level.getSignal(this.worldPosition.relative(dir), dir) > 0;

        if(redstone && !this.wasRedstone) this.targetLevel = 1D;
        if(!redstone && this.wasRedstone) this.targetLevel = 0D;

        this.wasRedstone = redstone;

        this.networkPackNT(100);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.extraction);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        double lastSync = this.syncLevel;
        this.syncLevel = buf.readDouble();
        if(this.syncLevel != lastSync) this.turnProgress = 2;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        this.extraction = tag.getDouble("level");
        this.targetLevel = tag.getDouble("targetLevel");
        this.wasRedstone = tag.getBoolean("wasRedstone");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("level", this.extraction);
        tag.putDouble("targetLevel", this.targetLevel);
        tag.putBoolean("wasRedstone", this.wasRedstone);
    }
}
