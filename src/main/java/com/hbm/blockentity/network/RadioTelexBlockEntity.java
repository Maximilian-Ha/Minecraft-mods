package com.hbm.blockentity.network;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.RTTYSystem.RTTYChannel;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.util.ItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityRadioTelex.
 *
 * Ein Fernschreiber: fuenf Zeilen zum Senden, fuenf zum Empfangen, je ein Kanal fuer beide
 * Richtungen. Gesendet wird Zeichen fuer Zeichen, eines je Tick, ueber RTTYSystem.
 *
 * Die Steuerzeichen sind die des Originals und stammen aus ASCII:
 *   \u0004 EOT   -- Ende der Uebertragung
 *   \n     EOL   -- Zeilenwechsel
 *   \u0007 BEL   -- laesst beim Empfaenger eine Glocke schlagen
 *   \u000c FF    -- der Empfaenger soll nach dem Ende ausdrucken
 *   \u0016 SYN   -- eine Sekunde Pause beim Senden
 *   \u007f DEL   -- loescht den Empfangsspeicher
 */
public class RadioTelexBlockEntity extends LoadedBaseBlockEntity implements ITickable, IControlReceiver {

    public static final int LINE_WIDTH = 33;
    public static final int LINES = 5;

    public static final char EOL = '\n';
    public static final char EOT = '\u0004';
    public static final char BELL = '\u0007';
    public static final char PRINT = '\u000c';
    public static final char PAUSE = '\u0016';
    public static final char CLEAR = '\u007f';

    public String txChannel = "";
    public String rxChannel = "";
    public String[] txBuffer = new String[] { "", "", "", "", "" };
    public String[] rxBuffer = new String[] { "", "", "", "", "" };

    public char sendingChar = ' ';

    private int sendingLine = 0;
    private int sendingIndex = 0;
    private boolean isSending = false;
    private int sendingWait = 0;
    private int writingLine = 0;
    private boolean printAfterRx = false;
    private boolean deleteOnReceive = true;

    private AABB sichtkasten;

    public RadioTelexBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RADIO_TELEX.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.sendingChar = ' ';

        if(this.isSending && this.txChannel.isEmpty()) this.isSending = false;

        if(this.isSending) this.sende();

        if(!this.rxChannel.isEmpty()) this.empfange();

        this.networkPackNT(16);
    }

    /** Ein Zeichen je Tick, am Zeilenende ein EOL, am Ende der fuenften Zeile ein EOT. */
    private void sende() {

        if(this.sendingWait > 0) {
            this.sendingWait--;
            return;
        }

        String zeile = this.txBuffer[this.sendingLine];

        if(zeile.length() > this.sendingIndex) {

            char c = zeile.charAt(this.sendingIndex);
            this.sendingIndex++;

            if(c == PAUSE) {
                this.sendingWait = 20;
            } else {
                RTTYSystem.broadcast(this.level, this.txChannel, c);
                this.sendingChar = c;
            }

        } else if(this.sendingLine >= LINES - 1) {
            this.isSending = false;
            RTTYSystem.broadcast(this.level, this.txChannel, EOT);
            this.sendingLine = 0;
            this.sendingIndex = 0;
        } else {
            RTTYSystem.broadcast(this.level, this.txChannel, EOL);
            this.sendingLine++;
            this.sendingIndex = 0;
        }
    }

    private void empfange() {

        RTTYChannel kanal = RTTYSystem.listen(this.level, this.rxChannel);
        if(kanal == null) return;
        if(!(kanal.signal instanceof Character zeichen)) return;
        if(kanal.timeStamp == -1 || kanal.timeStamp <= this.level.getGameTime() - 2) return;

        if(this.deleteOnReceive) {
            this.deleteOnReceive = false;
            this.leereEmpfang();
        }

        char c = zeichen;

        if(c == EOT) {
            if(this.printAfterRx) {
                this.printAfterRx = false;
                this.drucke();
            }
            this.deleteOnReceive = true;

        } else if(c == EOL) {
            if(this.writingLine < LINES - 1) this.writingLine++;
            this.setChanged();

        } else if(c == BELL) {
            this.level.playSound(null, this.worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 2F, 0.5F);

        } else if(c == PRINT) {
            this.printAfterRx = true;

        } else if(c == CLEAR) {
            this.leereEmpfang();

        } else {
            this.rxBuffer[this.writingLine] += c;
            this.setChanged();
        }
    }

    private void leereEmpfang() {
        for(int i = 0; i < LINES; i++) this.rxBuffer[i] = "";
        this.writingLine = 0;
    }

    /** Wirft den Empfangsspeicher als beschriebenes Blatt aus. */
    public void drucke() {

        if(this.level == null) return;

        List<String> text = new ArrayList<>();
        for(int i = 0; i < LINES; i++) {
            if(!this.rxBuffer[i].isEmpty()) text.add(this.rxBuffer[i]);
        }

        ItemStack blatt = new ItemStack(Items.PAPER);
        ItemStackUtil.addTooltipToStack(blatt, text.toArray(new String[0]));
        blatt.set(DataComponents.CUSTOM_NAME, Component.literal("Message"));

        this.level.addFreshEntity(new ItemEntity(this.level,
                this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1, this.worldPosition.getZ() + 0.5, blatt));
    }

    @Override
    public void receiveControl(CompoundTag tag) {

        for(int i = 0; i < LINES; i++) {
            if(tag.contains("tx" + i)) this.txBuffer[i] = tag.getString("tx" + i);
        }

        String befehl = tag.getString("cmd");

        if("snd".equals(befehl) && !this.isSending) {
            this.isSending = true;
            this.sendingLine = 0;
            this.sendingIndex = 0;
        }

        if("rxprt".equals(befehl)) this.drucke();

        if("rxcls".equals(befehl)) this.leereEmpfang();

        if("sve".equals(befehl)) {
            this.txChannel = tag.getString("txChan");
            this.rxChannel = tag.getString("rxChan");
            this.setChanged();
        }
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 16 * 16;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(int i = 0; i < LINES; i++) {
            buf.writeUtf(this.txBuffer[i]);
            buf.writeUtf(this.rxBuffer[i]);
        }
        buf.writeUtf(this.txChannel);
        buf.writeUtf(this.rxChannel);
        buf.writeChar(this.sendingChar);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(int i = 0; i < LINES; i++) {
            this.txBuffer[i] = buf.readUtf();
            this.rxBuffer[i] = buf.readUtf();
        }
        this.txChannel = buf.readUtf();
        this.rxChannel = buf.readUtf();
        this.sendingChar = buf.readChar();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < LINES; i++) {
            this.txBuffer[i] = tag.getString("tx" + i);
            this.rxBuffer[i] = tag.getString("rx" + i);
        }
        this.txChannel = tag.getString("txChan");
        this.rxChannel = tag.getString("rxChan");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < LINES; i++) {
            tag.putString("tx" + i, this.txBuffer[i]);
            tag.putString("rx" + i, this.rxBuffer[i]);
        }
        tag.putString("txChan", this.txChannel);
        tag.putString("rxChan", this.rxChannel);
    }

    /* Ohne @Override: getRenderBoundingBox kommt aus der NeoForge-Erweiterung von
     * BlockEntity und gilt dem Uebersetzer nicht als ueberschrieben. */
    public AABB getRenderBoundingBox() {
        if(this.sichtkasten == null) this.sichtkasten = new AABB(this.worldPosition).inflate(2);
        return this.sichtkasten;
    }
}
