package com.hbm.blockentity.network;

import api.hbm.redstoneoverradio.IRORValueProvider;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.RadioTorchBaseBlock;
import com.hbm.interfaces.IControlReceiver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityRadioTorchReader.
 *
 * Die Gegenrichtung des Zaehlers. Statt Gegenstaende zu zaehlen, fragt diese Fackel die
 * Maschine hinter sich nach benannten Werten -- das Messrohr nach deltatick und deltasecond,
 * das Zaehlventil nach value und state -- und funkt jeden davon auf einen eigenen Kanal.
 *
 * Acht Zeilen, jede aus Kanal und Wertname. Ist eine der beiden leer, bleibt die Zeile stumm.
 * Gesendet wird nur, wenn sich der Wert geaendert hat; auf Knopfdruck ("polling") stattdessen
 * jeden Tick.
 *
 * NICHT UEBERNOMMEN: die OpenComputers-Rueckrufe (setChannel, getChannel, setName, getName,
 * setPolling, getPolling, read). OpenComputers ist im Port nicht angebunden, genauso wie beim
 * Zaehlventil.
 */
public class RadioTorchReaderBlockEntity extends LoadedBaseBlockEntity implements ITickable, IControlReceiver {

    public static final int KANAELE = 8;

    public String[] channels = new String[KANAELE];
    public String[] names = new String[KANAELE];

    /** Zuletzt gesendeter Wert je Zeile -- ohne ihn liesse sich keine Aenderung erkennen. */
    private final String[] prev = new String[KANAELE];

    /** Frisch eingestellter Kanal oder Wertname: einmal senden, auch ohne Aenderung. */
    private final boolean[] forceUpdate = new boolean[KANAELE];

    public boolean polling = false;

    public RadioTorchReaderBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RADIO_TORCH_READER.get(), pos, state);

        for(int i = 0; i < KANAELE; i++) {
            this.channels[i] = "";
            this.names[i] = "";
            this.prev[i] = "";
        }
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        // Die Fackel sieht nach hinten -- dorthin, wo sie an der Wand haengt.
        Direction hinten = this.getBlockState().getValue(RadioTorchBaseBlock.FACING).getOpposite();

        if(this.level.getBlockEntity(this.worldPosition.relative(hinten)) instanceof IRORValueProvider geber) {

            for(int i = 0; i < KANAELE; i++) {

                String kanal = this.channels[i];
                String name = this.names[i];

                if(kanal == null || kanal.isEmpty()) continue;
                if(name == null || name.isEmpty()) continue;

                String wert = geber.provideRORValue(IRORValueProvider.PREFIX_VALUE + name.toLowerCase(Locale.US));
                if(wert == null) continue;

                if(this.polling || this.forceUpdate[i] || !wert.equals(this.prev[i])) {
                    RTTYSystem.broadcast(this.level, kanal, wert);
                    this.prev[i] = wert;
                    this.forceUpdate[i] = false;
                }
            }
        }

        this.networkPackNT(50);
    }

    /**
     * Wie bei den drei anderen Funkfackeln des Ports: die Reichweite kommt aus
     * Container.stillValidBlockEntity und nicht aus dem festen Abstand 16 des Originals.
     */
    @Override
    public boolean hasPermission(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void receiveControl(CompoundTag data) {

        if(data.contains("p")) this.polling = data.getBoolean("p");

        for(int i = 0; i < KANAELE; i++) {
            if(data.contains("c" + i)) this.setzeKanal(i, data.getString("c" + i));
            if(data.contains("n" + i)) this.setzeNamen(i, data.getString("n" + i));
        }

        this.setChanged();
    }

    private void setzeKanal(int index, String kanal) {
        if(!kanal.equals(this.channels[index])) {
            this.channels[index] = kanal;
            this.forceUpdate[index] = true;
        }
    }

    private void setzeNamen(int index, String name) {
        if(!name.equals(this.names[index])) {
            this.names[index] = name;
            this.forceUpdate[index] = true;
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.polling);
        for(int i = 0; i < KANAELE; i++) buf.writeUtf(this.channels[i]);
        for(int i = 0; i < KANAELE; i++) buf.writeUtf(this.names[i]);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        this.polling = buf.readBoolean();
        for(int i = 0; i < KANAELE; i++) this.channels[i] = buf.readUtf();
        for(int i = 0; i < KANAELE; i++) this.names[i] = buf.readUtf();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.polling = tag.getBoolean("p");
        for(int i = 0; i < KANAELE; i++) {
            this.channels[i] = tag.getString("c" + i);
            this.names[i] = tag.getString("n" + i);
            this.prev[i] = tag.getString("v" + i);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("p", this.polling);
        for(int i = 0; i < KANAELE; i++) {
            tag.putString("c" + i, this.channels[i] == null ? "" : this.channels[i]);
            tag.putString("n" + i, this.names[i] == null ? "" : this.names[i]);
            tag.putString("v" + i, this.prev[i] == null ? "" : this.prev[i]);
        }
    }
}
