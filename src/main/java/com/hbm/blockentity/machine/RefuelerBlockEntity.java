package com.hbm.blockentity.machine;

import api.hbm.fluidmk2.IFillableItem;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.MachineRefuelerBlock;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.awt.Color;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityRefueler.
 *
 * Die Zapfsaeule. Wer auf ihr steht, bekommt alles betankt, was sich betanken laesst -- auch
 * die Module in seinen Ruestungsteilen, denn genau dort steckt der Treibstoff des Jetpacks.
 * Der Tank fasst nur hundert Milliliter; er ist kein Lager, sondern ein Durchlauf.
 *
 * Welches Fluid gezapft wird, stellt ein Fluidkennzeichner am Block ein -- standardmaessig
 * Kerosin.
 */
public class RefuelerBlockEntity extends LoadedBaseBlockEntity implements ITickable, IFluidStandardReceiverMK2, IFluidCopiable {

    /** Wie oft es zischt, in Ticks. Wert aus dem Original. */
    private static final int ZISCH_TAKT = 20;

    public final FluidTank tank = new FluidTank(Fluids.KEROSENE, 100);

    /** Nur Client: der weich nachlaufende Fuellstand fuer den Darsteller. */
    public float fillLevel;
    public float prevFillLevel;

    public boolean isOperating = false;
    private int operatingTime;

    public RefuelerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_REFUELER.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        Direction dir = this.getBlockState().getValue(MachineRefuelerBlock.FACING).getOpposite();

        if(!this.level.isClientSide) {

            this.trySubscribe(this.tank.getTankType(), this.level, new DirPos(this.worldPosition.relative(dir), dir));

            this.isOperating = false;

            // Wer auf der Saeule steht -- ein halber Block Umkreis, wie im Original.
            List<Player> spieler = this.level.getEntitiesOfClass(Player.class,
                    new AABB(this.worldPosition.getX() + 0.5, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5,
                            this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5)
                            .inflate(0.5, 0.0, 0.5));

            for(Player p : spieler) {
                for(ItemStack stack : p.getInventory().armor) {

                    if(stack.isEmpty()) continue;
                    if(this.fuelle(stack)) this.isOperating = true;

                    if(stack.getItem() instanceof ArmorItem && ArmorModHandler.hasMods(stack)) {
                        for(ItemStack modul : ArmorModHandler.pryMods(this.level, stack)) {
                            if(modul.isEmpty()) continue;
                            if(this.fuelle(modul)) {
                                ArmorModHandler.applyMod(this.level, stack, modul);
                                this.isOperating = true;
                            }
                        }
                    }
                }

                // Auch, was in der Hand steckt -- im Original ist das Fach 0 der Ausruestung.
                if(this.fuelle(p.getMainHandItem())) this.isOperating = true;
            }

            if(this.isOperating) {
                if(this.operatingTime % ZISCH_TAKT == 0) {
                    this.level.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2F, 0.5F);
                }
                this.operatingTime++;
            } else {
                this.operatingTime = 0;
            }

            this.networkPackNT(150);

        } else {

            if(this.isOperating) this.zeichneStrahl(dir);

            this.prevFillLevel = this.fillLevel;

            float ziel = (float) this.tank.getFill() / (float) this.tank.getMaxFill();
            this.fillLevel = BobMathUtil.interp(this.fillLevel, ziel, ziel > this.fillLevel || !this.isOperating ? 0.1F : 0.01F);
        }
    }

    /**
     * Der Strahl aus dem Zapfhahn, in der Farbe des Fluids.
     *
     * ABWEICHUNG: das Original nimmt einen eingefaerbten Kritzel-Partikel (EntityCritFX) und
     * setzt dessen Farbe von Hand. Das geht auf 1.21 aus einer Blockentitaet nicht, ohne eine
     * Clientklasse anzufassen -- der Staubpartikel traegt seine Farbe dagegen im Partikeltyp
     * und laeuft ueber level.addParticle, das auf beiden Seiten steht. Lage und Bewegung sind
     * unveraendert.
     */
    private void zeichneStrahl(Direction dir) {

        Direction quer = dir.getClockWise();
        Color farbe = new Color(this.tank.getTankType().getColor());
        var zufall = this.level.random;

        this.level.addParticle(
                new DustParticleOptions(new Vector3f(farbe.getRed() / 255F, farbe.getGreen() / 255F, farbe.getBlue() / 255F), 1F),
                this.worldPosition.getX() + 0.5 + zufall.nextDouble() * 0.0625 + dir.getStepX() * 0.5 + quer.getStepX() * 0.25,
                this.worldPosition.getY() + 0.375,
                this.worldPosition.getZ() + 0.5 + zufall.nextDouble() * 0.0625 + dir.getStepZ() * 0.5 + quer.getStepZ() * 0.25,
                -dir.getStepX() + zufall.nextGaussian() * 0.1,
                0D,
                -dir.getStepZ() + zufall.nextGaussian() * 0.1);
    }

    /** Gibt TRUE zurueck, wenn wirklich etwas in den Gegenstand gelaufen ist. */
    private boolean fuelle(ItemStack stack) {

        if(!(stack.getItem() instanceof IFillableItem fuellbar)) return false;
        if(!fuellbar.acceptsFluid(this.tank.getTankType(), stack)) return false;

        int vorher = this.tank.getFill();
        this.tank.setFill(fuellbar.tryFill(this.tank.getTankType(), this.tank.getFill(), stack));

        return this.tank.getFill() < vorher;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(this.isOperating);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        this.isOperating = buf.readBoolean();
        this.tank.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.tank.readFromNBT(tag, "t");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.tank.writeToNBT(tag, "t");
    }

    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }

    @Override public @Nullable FluidTank getTankToPaste() { return this.tank; }
}
