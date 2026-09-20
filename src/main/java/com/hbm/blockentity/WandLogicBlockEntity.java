package com.hbm.blockentity;

import com.hbm.world.gen.logic.LogicActions;
import com.hbm.world.gen.logic.LogicConditions;
import com.hbm.world.gen.logic.LogicInteractions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Portiert aus 1.7.10: die eingebettete Klasse LogicBlock.TileEntityLogicBlock.
 *
 * DER LOGIKSTAB IST EINE FALLE MIT ZWEI HAELFTEN: eine BEDINGUNG, die jeden Tick gefragt wird,
 * und eine AKTION, die jeden Tick laeuft. Beide stehen als NAME in der Blockentitaet -- so
 * kommen sie aus der Bauwerksdatei -- und werden beim ersten Tick in der Tabelle
 * nachgeschlagen.
 *
 * DIE ZWEI ZAEHLER SIND DER GANZE ZUSTAND: "phase" zaehlt hoch, sooft die Bedingung zutrifft,
 * "timer" zaehlt die Ticks seit dem letzten Zutreffen. Jede Aktion liest beide und entscheidet
 * daran, was sie tut -- deshalb kommen Aktion und Bedingung ohne weiteres Gedaechtnis aus.
 * REIHENFOLGE: erst die Aktion, dann die Bedingung. Das Original macht es so, und es ist
 * sichtbar: eine Aktion sieht in Phase 0 die Welt, BEVOR die Bedingung sie auf 1 stellt.
 *
 * EIN UNBEKANNTER NAME LOESCHT DEN BLOCK. Das ist keine Notbremse des Ports, sondern die
 * Regel des Originals (LogicBlock.java:113): findet es Aktion oder Bedingung nicht in der
 * Tabelle, setzt es an seiner Stelle Luft. Das passiert dort auch im Spiel -- der Turmsockel
 * nennt DEAD_GUY_BASE_TOWER, und diese Aktion ist im Original nirgends angemeldet (die
 * einzige put()-Zeile dafuer ist auskommentiert UND anders geschrieben,
 * LogicBlockActions.java:537). Ein Logikstab, dessen Aktion der Port noch nicht kennt,
 * verhaelt sich also genau wie im Original: er verschwindet.
 */
public class WandLogicBlockEntity extends BlockEntity {

    /** Zaehlt hoch, sooft die Bedingung zutrifft. */
    public int phase = 0;
    /** Ticks seit dem letzten Zutreffen der Bedingung. */
    public int timer = 0;

    public String actionID = "";
    public String conditionID = "";
    public String interactionID = "";

    /** Die Richtung, in die der Stab zeigt -- manche Aktionen setzen ihren Block davor. */
    public Direction direction = Direction.UP;
    /** Ob die Falle leise ausloest. Das Original fuehrt das Feld mit und liest es nirgends. */
    public boolean muffled = false;

    private Consumer<WandLogicBlockEntity> action;
    private Predicate<WandLogicBlockEntity> condition;
    private boolean aufgeloest = false;

    public WandLogicBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.WAND_LOGIC.get(), pos, state);
    }

    public void serverTick() {

        if(!this.aufgeloest) {
            this.action = LogicActions.finde(this.actionID);
            this.condition = LogicConditions.finde(this.conditionID);
            this.aufgeloest = true;
        }

        /* Kein Eintrag in der Tabelle: der Stab verschwindet. Siehe Klassenkommentar --
         * das ist das Verhalten des Originals, nicht eine Abkuerzung des Ports. */
        if(this.action == null || this.condition == null) {
            this.level.setBlock(this.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        this.action.accept(this);

        if(this.condition.test(this)) {
            this.phase++;
            this.timer = 0;
        } else {
            this.timer++;
        }
    }

    /** Rechtsklick auf den Stab -- nur die Faelle mit einer Wechselwirkung tun hier etwas. */
    public void benutze(Player spieler) {
        Consumer<Object[]> wechsel = LogicInteractions.finde(this.interactionID);
        if(wechsel != null) wechsel.accept(new Object[] { this.level, this, this.getBlockPos(), spieler });
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.phase = tag.getInt("phase");
        this.actionID = tag.getString("actionID");
        this.conditionID = tag.getString("conditionID");
        this.interactionID = tag.getString("interactionID");
        this.muffled = tag.getInt("muffled") != 0;
        if(tag.contains("rotation")) this.direction = Direction.values()[tag.getInt("rotation") % 6];
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("phase", this.phase);
        tag.putString("actionID", this.actionID);
        tag.putString("conditionID", this.conditionID);
        tag.putString("interactionID", this.interactionID);
        tag.putInt("muffled", this.muffled ? 1 : 0);
        tag.putInt("rotation", this.direction.ordinal());
    }
}
