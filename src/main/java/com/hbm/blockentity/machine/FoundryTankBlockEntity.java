package com.hbm.blockentity.machine;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.FoundryTankBlock;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats.MaterialStack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFoundryTank.
 *
 * Der Lagerbehaelter der Giesserei. Vier Bloecke Material passen hinein, und er gibt sie in
 * drei Stufen weiter: erst nach unten in den Tank darunter, dann waagerecht an alles, was den
 * Guss annimmt, und zuletzt an die Nachbartanks, deren Fuellstand er dabei angleicht.
 *
 * Das Angleichen hat einen Zufall eingebaut: jeder fuenfte Versuch tauscht die Fuellstaende
 * ganz, statt sie zu halbieren. Sonst bliebe ein langer Strang auf halber Strecke stehen, weil
 * die Haelfte der Haelfte irgendwann null ist.
 */
public class FoundryTankBlockEntity extends FoundryBaseBlockEntity {

    public int nextUpdate;

    public FoundryTankBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FOUNDRY_TANK.get(), pos, state);
    }

    @Override public int getCapacity() { return MaterialShapes.BLOCK.q(4); }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) {
            super.updateEntity();
            return;
        }

        if(this.type == null && this.amount != 0) this.amount = 0;

        this.nextUpdate--;

        if(this.nextUpdate <= 0 && this.amount > 0 && this.type != null) {

            this.nextUpdate = this.level.random.nextInt(6) + 5;
            boolean hasOp = this.gibNachUnten();

            List<Direction> dirs = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
            Collections.shuffle(dirs);

            if(!hasOp) hasOp = this.gibAnAbnehmer(dirs);
            if(!hasOp) this.gleicheMitNachbarnAus(dirs);
        }

        super.updateEntity();
    }

    /** Der Tank darunter wird zuerst bedient, und zwar bis zum Anschlag. */
    private boolean gibNachUnten() {

        if(!(this.level.getBlockEntity(this.worldPosition.below()) instanceof FoundryTankBlockEntity unten)) return false;
        if(unten.type != null && unten.type != this.type) return false;
        if(unten.amount >= unten.getCapacity()) return false;

        unten.type = this.type;
        int menge = Math.min(this.amount, unten.getCapacity() - unten.amount);
        this.amount -= menge;
        unten.amount += menge;
        unten.setChanged();
        this.setChanged();

        return true;
    }

    /** Danach alles, was den Guss annimmt -- Formen, Becken, Ausguesse. Kanaele nicht. */
    private boolean gibAnAbnehmer(List<Direction> dirs) {

        for(Direction dir : dirs) {

            BlockPos nachbar = this.worldPosition.relative(dir);
            if(!(this.level.getBlockState(nachbar).getBlock() instanceof ICrucibleAcceptor acc)) continue;
            if(this.level.getBlockEntity(nachbar) instanceof FoundryChannelBlockEntity) continue;

            MaterialStack angebot = new MaterialStack(this.type, this.amount);
            if(!acc.canAcceptPartialFlow(this.level, nachbar, dir.getOpposite(), angebot)) continue;

            MaterialStack rest = acc.flow(this.level, nachbar, dir.getOpposite(), angebot);

            if(rest == null) {
                this.type = null;
                this.amount = 0;
            } else {
                this.amount = rest.amount;
            }

            this.setChanged();
            return true;
        }

        return false;
    }

    /** Zuletzt die Nachbartanks. Hier wird nichts abgegeben, sondern ausgeglichen. */
    private void gleicheMitNachbarnAus(List<Direction> dirs) {

        for(Direction dir : dirs) {

            if(!(this.level.getBlockEntity(this.worldPosition.relative(dir)) instanceof FoundryTankBlockEntity nachbar)) continue;
            if(nachbar.type != null && nachbar.type != this.type && nachbar.amount != 0) continue;

            nachbar.type = this.type;

            if(this.level.random.nextInt(5) == 0) {
                int puffer = this.amount;
                this.amount = nachbar.amount;
                nachbar.amount = puffer;

            } else {
                int unterschied = this.amount - nachbar.amount;

                if(unterschied > 0) {
                    unterschied /= 2;
                    this.amount -= unterschied;
                    nachbar.amount += unterschied;
                }
            }

            nachbar.setChanged();
            this.setChanged();
        }
    }

    /**
     * Wie hoch die Schmelze in diesem Block steht, in Blockteilen. Laeuft der Behaelter nach
     * oben oder unten weiter, faellt die Wand dazwischen weg und die Oberflaeche darf ueber die
     * Blockgrenze hinaus -- daher die beiden Achtel. Werte aus dem Original.
     */
    public double hoeheDerOberflaeche() {

        BlockState state = this.getBlockState();
        if(!(state.getBlock() instanceof FoundryTankBlock)) return 0D;

        double oben = 0.75D
                + (state.getValue(FoundryTankBlock.DOWN) ? 0.125D : 0D)
                + (state.getValue(FoundryTankBlock.UP) ? 0.125D : 0D);

        double boden = state.getValue(FoundryTankBlock.DOWN) ? 0D : 0.125D;

        return boden + oben * this.amount / this.getCapacity();
    }
}
