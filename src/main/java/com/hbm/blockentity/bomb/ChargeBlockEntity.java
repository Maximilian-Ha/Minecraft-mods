package com.hbm.blockentity.bomb;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.bomb.ChargeBaseBlock;
import com.hbm.registry.NtmSoundEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.bomb.TileEntityCharge.
 *
 * Die Blockentitaet der vier Haftladungen. Sie fuehrt zwei Felder: die Restzeit in Ticks und
 * ob der Zuender schon laeuft. Laeuft er, zaehlt sie jeden Tick herunter, piept jede Sekunde
 * einmal und laesst den Block bei null explodieren.
 *
 * DAS PIEPEN SETZT EINEN TICK AUS. Das Original prueft timer % 20 == 0 NACH dem Herunterzaehlen
 * und verlangt zusaetzlich timer > 0 -- bei einer auf ein Vielfaches von 20 gestellten Uhr
 * piept es also bei 20 noch, bei 0 nicht mehr. Uebernommen.
 *
 * DIE ZEIT GEHT AN DEN CLIENT, weil der Darsteller sie als Text auf die Ladung schreibt. Das
 * Original schickt sie mit networkPackNT(100) jeden Tick; hier tut es die uebliche
 * Blockentitaeten-Synchronisierung, ausgeloest bei jeder vollen Sekunde und bei jeder
 * Aenderung durch den Spieler. Das spart 19 von 20 Paketen und zeigt dasselbe.
 */
public class ChargeBlockEntity extends BlockEntity implements ITickable {

    public boolean started;
    public int timer;

    public ChargeBlockEntity(BlockPos pos, BlockState blockState) {
        super(NtmBlockEntityTypes.CHARGE.get(), pos, blockState);
    }

    @Override
    public void updateEntity() {

        if(level == null || level.isClientSide) return;
        if(!started) return;

        timer--;

        if(timer % 20 == 0 && timer > 0) {
            level.playSound(null, this.getBlockPos(), NtmSoundEvents.FSTBMB_PING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            this.sync();
        }

        if(timer <= 0 && this.getBlockState().getBlock() instanceof ChargeBaseBlock ladung) {
            ladung.explode(level, this.getBlockPos());
        }
    }

    /** Traegt den Stand in die Welt ein und schickt ihn an die Clients in Sichtweite. */
    public void sync() {
        this.setChanged();
        if(level != null) level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.timer = tag.getInt("timer");
        this.started = tag.getBoolean("started");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("timer", this.timer);
        tag.putBoolean("started", this.started);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** "00" bis "99" -- die Minuten der Restzeit, wie im Original zweistellig aufgefuellt. */
    public String getMinutes() {
        String minuten = "" + (timer / 1200);
        if(minuten.length() == 1) minuten = "0" + minuten;
        return minuten;
    }

    /** Die Sekunden innerhalb der Minute, ebenso zweistellig. */
    public String getSeconds() {
        String sekunden = "" + ((timer / 20) % 60);
        if(sekunden.length() == 1) sekunden = "0" + sekunden;
        return sekunden;
    }
}
