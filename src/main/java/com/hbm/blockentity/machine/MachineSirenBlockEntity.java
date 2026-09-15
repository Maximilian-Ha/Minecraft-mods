package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.menus.MachineSirenMenu;
import com.hbm.items.machine.CassetteItem;
import com.hbm.items.machine.CassetteItem.SoundType;
import com.hbm.items.machine.CassetteItem.TrackType;
import com.hbm.main.NuclearTechMod;
import com.hbm.sound.AudioWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineSiren.
 *
 * Ein Fach, eine Kassette, ein Redstone-Eingang. Liegt eine Kassette darin und liegt Strom an,
 * spielt ihre Tonspur -- in der Schleife oder einmal durch, je nach Spur.
 *
 * DIE REICHWEITE STEHT AUF DER KASSETTE, nicht an der Maschine: fuenfzig Bloecke beim Klaxon,
 * fuenfhundert bei der Luftschutzsirene. Das Original schickt dafuer ein eigenes Paket an alle
 * im Umkreis von tausendfuenfhundert Bloecken.
 *
 * ABWEICHUNG: hier gibt es kein eigenes Paket. Die Sirene uebertraegt Spur und Zustand mit dem
 * gewoehnlichen Maschinenpaket, und der Client entscheidet selbst, ob er den Ton laufen laesst --
 * dasselbe Verfahren wie bei jeder anderen Maschine des Ports mit Dauerton. Das spart eine
 * Paketart und haelt die Lautstaerke dort, wo sie hingehoert: bei der Kassette.
 */
public class MachineSirenBlockEntity extends MachineBaseBlockEntity {

    public TrackType track = TrackType.NULL;
    public boolean active = false;

    /** Merkt sich, ob der Einmalton schon angestossen wurde -- sonst liefe er jeden Tick neu. */
    private boolean lock = false;
    private boolean clientPlayed = false;

    private AudioWrapper audio;

    public MachineSirenBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_SIREN.get(), pos, state, 1);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.siren");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.track = this.getCurrentTrack();

            boolean powered = this.level.hasNeighborSignal(this.worldPosition);

            if(this.track == TrackType.NULL) {
                this.active = false;
                this.lock = false;
            } else if(this.track.type == SoundType.LOOP) {
                this.active = powered;
                this.lock = false;
            } else {
                /* Einmaltoene laufen auf der steigenden Flanke an und gelten genau einen Tick
                 * lang als "an" -- lange genug, damit der Client sie anstoesst. */
                this.active = powered && !this.lock;
                if(powered) this.lock = true;
                if(!powered) this.lock = false;
            }

            this.networkPackNT(50);

        } else {
            this.updateSound();
        }
    }

    private void updateSound() {

        if(this.track == TrackType.NULL || this.track.getSound() == null) {
            this.stopSound();
            this.clientPlayed = false;
            return;
        }

        if(this.track.type == SoundType.LOOP) {

            if(this.active && NuclearTechMod.proxy.me() != null
                    && NuclearTechMod.proxy.me().distanceToSqr(this.getBlockPos().getBottomCenter()) < (double) this.track.volume * this.track.volume) {

                if(this.audio == null) {
                    this.audio = this.createAudioLoop();
                    this.audio.startSound();
                } else if(!this.audio.isPlaying()) {
                    this.audio = rebootAudio(this.audio);
                }

                this.audio.keepAlive();
                this.audio.updateVolume(this.getVolume(1F));

            } else {
                this.stopSound();
            }

            return;
        }

        /* PASS und SOUND: einmal anstossen, wenn der Zustand von aus auf an springt. */
        if(this.active && !this.clientPlayed) {
            this.level.playLocalSound(this.worldPosition, this.track.getSound(), SoundSource.RECORDS, 1F, 1F, false);
            this.clientPlayed = true;
        }
        if(!this.active) this.clientPlayed = false;
    }

    private void stopSound() {
        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(this.track.getSound(), SoundSource.RECORDS, this, 1F, this.track.volume, 1F, 20);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.stopSound();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.stopSound();
    }

    public TrackType getCurrentTrack() {
        ItemStack stack = this.slots.get(0);
        if(stack.isEmpty() || !(stack.getItem() instanceof CassetteItem)) return TrackType.NULL;
        return TrackType.fromMeta(MetaHelper.getMeta(stack));
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof CassetteItem;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeByte(this.track.ordinal());
        buf.writeBoolean(this.active);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.track = TrackType.fromMeta(buf.readByte());
        this.active = buf.readBoolean();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineSirenMenu(id, inventory, this);
    }
}
