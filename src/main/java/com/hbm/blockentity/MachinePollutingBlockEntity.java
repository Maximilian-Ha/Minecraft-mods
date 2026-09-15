package com.hbm.blockentity;

import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.TileEntityMachinePolluting.
 *
 * Basis fuer Maschinen, die beim Betrieb Abgase erzeugen. Der Ausstoss landet
 * zuerst in drei internen Rauchtanks (Russ, Bleirauch, Giftrauch), die sich
 * abpumpen und filtern lassen. Erst was ueberlaeuft, geht als Umweltverschmutzung
 * in die Atmosphaere -- genau wie im Original.
 *
 */
public abstract class MachinePollutingBlockEntity extends MachineBaseBlockEntity implements IFluidStandardSenderMK2 {

    public FluidTank smoke;
    public FluidTank smokeLeaded;
    public FluidTank smokePoison;

    public MachinePollutingBlockEntity(BlockEntityType<? extends MachineBaseBlockEntity> type, BlockPos pos, BlockState state, int slotCount, int smokeBuffer) {
        super(type, pos, state, slotCount);
        this.smoke = new FluidTank(Fluids.SMOKE, smokeBuffer);
        this.smokeLeaded = new FluidTank(Fluids.SMOKE_LEADED, smokeBuffer);
        this.smokePoison = new FluidTank(Fluids.SMOKE_POISON, smokeBuffer);
    }

    /**
     * Fuellt den passenden Rauchtank. 1 Verschmutzungseinheit entspricht 100 mB.
     * Laeuft der Tank ueber, wird der Ueberschuss direkt in die Umwelt abgegeben.
     */
    public void pollute(PollutionType type, float amount) {
        if(this.level == null) return;

        FluidTank tank = switch(type) {
            case SOOT -> this.smoke;
            case HEAVYMETAL -> this.smokeLeaded;
            default -> this.smokePoison;
        };

        tank.setFill(tank.getFill() + (int) Math.ceil(amount * 100F));

        if(tank.getFill() > tank.getMaxFill()) {
            int overflow = tank.getFill() - tank.getMaxFill();
            tank.setFill(tank.getMaxFill());
            PollutionHandler.incrementPollution(this.level, this.getBlockPos(), type, overflow / 100F);

            // Zischen beim Ueberlaufen, nur in jedem dritten Fall (Original Z. 45).
            // NTMSounds.VANILLA_HISS ist "random.fizz", in 1.21 SoundEvents.FIRE_EXTINGUISH.
            if(this.level.random.nextInt(3) == 0) {
                this.level.playSound(null, this.getBlockPos(), SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS, 0.1F, 1.5F);
            }
        }
    }

    /**
     * Verschmutzung anhand der Eigenschaften des verbrannten bzw. verschuetteten Fluids.
     *
     * amount wird bewusst NICHT als Faktor benutzt: das Original nimmt den Parameter zwar
     * entgegen, wertet ihn aber nirgends aus (TileEntityMachinePolluting.java:57 und
     * FT_Polluting.java:57-58 uebergeben beide entry.getValue() unveraendert). Multipliziert
     * man hier, stoesst der Dieselmotor das Fuenffache und der Verbrennungsmotor bei voller
     * Drosselklappe rund das Dreissigfache des Originals aus -- der 50-mB-Rauchpuffer laeuft
     * dann sofort ueber. Der Parameter bleibt in der Signatur, weil die Aufrufer ihn wie im
     * Original mitgeben.
     */
    public void pollute(FluidType type, FluidReleaseType release, float amount) {
        FT_Polluting trait = type.getTrait(FT_Polluting.class);
        if(trait == null || release == FluidReleaseType.VOID) return;

        var map = release == FluidReleaseType.BURN ? trait.burnMap : trait.releaseMap;
        for(var entry : map.entrySet()) {
            this.pollute(entry.getKey(), entry.getValue());
        }
    }

    /** Gibt angesammelten Rauch an angeschlossene Leitungen ab. */
    public void sendSmoke(DirPos[] positions) {
        if(this.level == null) return;

        for(DirPos pos : positions) {
            if(this.smoke.getFill() > 0) this.tryProvide(this.smoke, this.level, pos);
            if(this.smokeLeaded.getFill() > 0) this.tryProvide(this.smokeLeaded, this.level, pos);
            if(this.smokePoison.getFill() > 0) this.tryProvide(this.smokePoison, this.level, pos);
        }
    }

    public FluidTank[] getSmokeTanks() {
        return new FluidTank[] { this.smoke, this.smokeLeaded, this.smokePoison };
    }

    @Override
    public FluidTank[] getSendingTanks() {
        return this.getSmokeTanks();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.smoke.readFromNBT(tag, "smoke");
        this.smokeLeaded.readFromNBT(tag, "smokeLeaded");
        this.smokePoison.readFromNBT(tag, "smokePoison");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.smoke.writeToNBT(tag, "smoke");
        this.smokeLeaded.writeToNBT(tag, "smokeLeaded");
        this.smokePoison.writeToNBT(tag, "smokePoison");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.smoke.serialize(buf);
        this.smokeLeaded.serialize(buf);
        this.smokePoison.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.smoke.deserialize(buf);
        this.smokeLeaded.deserialize(buf);
        this.smokePoison.deserialize(buf);
    }
}
