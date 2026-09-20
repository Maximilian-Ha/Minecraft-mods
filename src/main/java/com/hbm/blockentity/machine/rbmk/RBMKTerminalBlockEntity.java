package com.hbm.blockentity.machine.rbmk;

import api.hbm.redstoneoverradio.IRORInteractive;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.RTTYSystem;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.interfaces.IControlReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKTerminal.
 *
 * Ein Fernschreiber fuer das RTTY-Netz. Man tippt Befehle ein, das Terminal fuehrt sie aus und
 * schreibt die Antwort in seinen Verlauf, der auf der Tafel steht.
 *
 * Die Befehle:
 *   chan [Kanal]   Kanal waehlen, ohne Argument abwaehlen
 *   send Befehl    einmal senden
 *   start Befehl   fortwaehrend senden
 *   stop           das Senden einstellen
 *   clear          Verlauf loeschen
 *
 * Und zwei, die im Original stehen und hier bleiben duerfen: "horse" und "selfdestruct".
 *
 * NICHT UEBERNOMMEN: die OpenComputers-Anbindung, siehe docs/ENTSCHEIDUNGEN.md. Damit faellt
 * auch der ocMode weg -- ein Betriebsmodus, in dem das Terminal keine eigenen Befehle mehr
 * auswertet, sondern nur noch anzeigt, was ein Rechner hineinschreibt.
 */
public class RBMKTerminalBlockEntity extends LoadedBaseBlockEntity implements ITickable, IControlReceiver, IRORInteractive {

    public static final int LINES = 17;

    public final String[] history = new String[LINES];
    public String channel = "";
    public String repeatCmd = "";
    /** Nur fuer den Renderer: ob gerade fortwaehrend gesendet wird. */
    public boolean doesRepeat;

    public RBMKTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_TERMINAL.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(!this.channel.isEmpty() && !this.repeatCmd.isEmpty()) {
            RTTYSystem.broadcast(this.level, this.channel, this.repeatCmd);
        }

        this.networkPackNT(50);
    }

    /** Wertet eine eingetippte Zeile aus. */
    public void eval(String cmd) {

        if(cmd == null) return;

        this.push(cmd);
        if(cmd.isEmpty()) return;

        if(cmd.startsWith("chan ")) {
            this.channel = cmd.substring(5);
            this.push("Set channel to " + (this.channel.isEmpty() ? "<none>" : this.channel));
            this.setChanged();
            return;
        }

        if(cmd.equals("chan")) {
            this.channel = "";
            this.push("Set channel to <none>");
            this.setChanged();
            return;
        }

        if(cmd.startsWith("start ")) {
            this.repeatCmd = cmd.substring(6);
            this.push("Repeating signal on " + this.channel);
            this.setChanged();
            return;
        }

        if(cmd.equals("stop")) {
            this.repeatCmd = "";
            this.push("Stopping repeat signal");
            this.setChanged();
            return;
        }

        if(cmd.startsWith("send ")) {
            if(this.channel.isEmpty()) {
                this.push("Cannot send - no channel set");
                return;
            }
            RTTYSystem.broadcast(this.level, this.channel, cmd.substring(5));
            this.push("Sent signal on " + this.channel);
            return;
        }

        if(cmd.equals("horse")) {
            this.push("Horse.");
            return;
        }

        if(cmd.equals("selfdestruct")) {
            this.selfDestruct();
            return;
        }

        if(cmd.equals("clear")) {
            this.clear();
            return;
        }

        this.push("Unrecognized command!");
    }

    private void selfDestruct() {

        BlockPos pos = this.getBlockPos();
        this.level.removeBlock(pos, false);

        ExplosionVNT vnt = new ExplosionVNT(this.level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5)
                .setEntityProcessor(new EntityProcessorCrossSmooth(1, 50).setupPiercing(5F, 0.5F))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
        vnt.explode();
    }

    private void clear() {
        for(int i = 0; i < this.history.length; i++) this.history[i] = "";
    }

    /**
     * Schiebt den Verlauf um eine Zeile nach unten.
     *
     * Die unterste Zeile geht dabei verloren, und die allerletzte wird gar nicht erst beschrieben
     * -- so steht es im Original, also bleibt eine Zeile des Verlaufs ungenutzt.
     */
    public void push(String msg) {

        for(int i = this.history.length - 2; i > 0; i--) {
            this.history[i] = this.history[i - 1];
        }

        this.history[0] = msg;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(!this.repeatCmd.isEmpty());
        for(String line : this.history) buf.writeUtf(line == null ? "" : line);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.doesRepeat = buf.readBoolean();
        for(int i = 0; i < this.history.length; i++) this.history[i] = buf.readUtf();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.channel = tag.getString("channel");
        this.repeatCmd = tag.getString("repeatCmd");
        for(int i = 0; i < this.history.length; i++) this.history[i] = tag.getString("history" + i);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        /*
         * Das Original schreibt fuer leere Zeichenketten ein Leerzeichen und filtert es beim Lesen
         * wieder heraus -- ein Kniff gegen null-Strings in 1.7.10. Hier unnoetig: getString liefert
         * fuer ein fehlendes Feld ohnehin die leere Zeichenkette.
         */
        tag.putString("channel", this.channel);
        tag.putString("repeatCmd", this.repeatCmd);
        for(int i = 0; i < this.history.length; i++) tag.putString("history" + i, this.history[i] == null ? "" : this.history[i]);
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 15 * 15;
    }

    @Override
    public void receiveControl(CompoundTag data) {

        if(data.contains("cmd")) {
            this.eval(data.getString("cmd"));
            this.setChanged();
        }
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                PREFIX_FUNCTION + "clear",
                PREFIX_FUNCTION + "write" + NAME_SEPARATOR + "text",
                PREFIX_FUNCTION + "set<line#>" + NAME_SEPARATOR + "text",
                PREFIX_FUNCTION + "submit" + NAME_SEPARATOR + "command",
        };
    }

    @Override
    public String runRORFunction(String name, String[] params) {

        if((PREFIX_FUNCTION + "clear").equals(name)) {
            this.clear();
            this.setChanged();
            return null;
        }

        String allParams = String.join(" ", params);

        if((PREFIX_FUNCTION + "write").equals(name)) {
            this.push(allParams);
            this.setChanged();
            return null;
        }

        if(name.startsWith(PREFIX_FUNCTION + "set")) {
            int line = IRORInteractive.parseInt(name.substring(PREFIX_FUNCTION.length() + 3), 1, LINES) - 1;
            this.history[line] = allParams;
            this.setChanged();
            return null;
        }

        if((PREFIX_FUNCTION + "submit").equals(name)) {
            this.eval(allParams);
            return null;
        }

        return null;
    }
}
