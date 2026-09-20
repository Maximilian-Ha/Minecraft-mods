package com.hbm.blockentity.machine.storage;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.storage.TileEntityFileCabinet.
 *
 * Der AKTENSCHRANK ist eine Kiste mit acht Faechern -- und mit zwei Schubladen, die sich
 * nacheinander oeffnen. Die untere faehrt sofort heraus, die obere erst zehn Ticks spaeter;
 * das ist der ganze Reiz des Stuecks, und es steht genauso im Original.
 *
 * DIE ZWEI FACHREIHEN STEHEN 36 PIXEL AUSEINANDER statt der ueblichen 18: jede Reihe ist
 * eine Schublade, und dazwischen ist im Fenster die Schubladenfront zu sehen. Dafuer hat
 * CrateBaseBlockEntity jetzt getRowPitch(); alle anderen Kisten lassen es bei 18.
 *
 * WARUM ER GEBRAUCHT WIRD: vier der Bauwerke des Originals -- aircraft_carrier, laboratory,
 * oil_rig, radio_house -- setzen ihn per Beutestab. Ohne ihn brach die Umsetzung dieser vier
 * Dateien mit "wand_loot zeigt auf unbekannten Block" ab.
 */
public class FileCabinetBlockEntity extends CrateBaseBlockEntity implements ITickable {

    /** Wie weit die Schubladen herausstehen, 0 bis MAX_EXTENT. Nur zum Zeichnen. */
    public float lowerExtent = 0F;
    public float prevLowerExtent = 0F;
    public float upperExtent = 0F;
    public float prevUpperExtent = 0F;

    /** Die Verzoegerung der oberen Schublade: sie faehrt erst, wenn der Zaehler bei zehn steht. */
    private int timer = 0;
    private int playersUsing = 0;

    private static final float MAX_EXTENT = 0.8F;

    public FileCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FILE_CABINET.get(), pos, state, 8, "gui_file_cabinet", 4, 2, 53, 18, 8, 88, 176, 170, 8, 4210752, 4210752);
    }

    @Override public Component getName() { return Component.translatable("container.fileCabinet"); }

    /** Zwei Schubladen uebereinander, und im Fenster steht die Front dazwischen. */
    @Override public int getRowPitch() { return 36; }

    /* Das Original zaehlt die Benutzer selbst und macht die Geraeusche in updateEntity --
     * beim Herausfahren der Schubladen, nicht beim Oeffnen des Fensters. Deshalb zaehlen
     * diese beiden hier nur; der Ton der Kistenbasis wuerde doppelt klingen. */
    @Override
    public void startOpen(Player player) {
        if(!this.level.isClientSide) this.playersUsing++;
    }

    @Override
    public void stopOpen(Player player) {
        if(!this.level.isClientSide) this.playersUsing--;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.timer);
        buf.writeInt(this.playersUsing);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.timer = buf.readInt();
        this.playersUsing = buf.readInt();
    }

    @Override
    public void updateEntity() {

        if(!this.level.isClientSide) {
            if(this.playersUsing > 0) {
                if(this.timer < 10) this.timer++;
            } else {
                this.timer = 0;
            }

            this.networkPackNT(25);
        } else {
            this.prevLowerExtent = this.lowerExtent;
            this.prevUpperExtent = this.upperExtent;
        }

        float openSpeed = this.playersUsing > 0 ? 1F / 16F : 1F / 25F;

        if(this.playersUsing > 0) {

            if(this.lowerExtent == 0F && this.upperExtent == 0F) {
                this.spiele(NtmSoundEvents.CRATE_OPEN.get(), 0.8F, 1.0F);
            } else {
                if(this.upperExtent + openSpeed >= MAX_EXTENT && this.lowerExtent < MAX_EXTENT)
                    this.spiele(NtmSoundEvents.CRATE_OPEN.get(), 0.5F, this.level.random.nextFloat() * 0.1F + 0.7F);

                if(this.lowerExtent + openSpeed >= MAX_EXTENT && this.lowerExtent < MAX_EXTENT)
                    this.spiele(NtmSoundEvents.CRATE_OPEN.get(), 0.5F, this.level.random.nextFloat() * 0.1F + 0.7F);
            }

            this.lowerExtent += openSpeed;
            if(this.timer >= 10) this.upperExtent += openSpeed;

        } else if(this.lowerExtent > 0F) {

            if(this.upperExtent - openSpeed < MAX_EXTENT / 2F && this.upperExtent >= MAX_EXTENT / 2F && this.upperExtent != this.lowerExtent)
                this.spiele(NtmSoundEvents.CRATE_CLOSE.get(), 0.8F, 1.0F);

            if(this.lowerExtent - openSpeed < MAX_EXTENT / 2F && this.lowerExtent >= MAX_EXTENT / 2F)
                this.spiele(NtmSoundEvents.CRATE_CLOSE.get(), 0.8F, 1.0F);

            this.upperExtent -= openSpeed;
            this.lowerExtent -= openSpeed;
        }

        this.lowerExtent = Mth.clamp(this.lowerExtent, 0F, MAX_EXTENT);
        this.upperExtent = Mth.clamp(this.upperExtent, 0F, MAX_EXTENT);
    }

    private void spiele(net.minecraft.sounds.SoundEvent ton, float lautstaerke, float hoehe) {
        this.level.playSound(null, this.getBlockPos(), ton, SoundSource.BLOCKS, lautstaerke, hoehe);
    }
}
