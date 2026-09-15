package com.hbm.blockentity.machine.heater;

import com.hbm.module.ModuleBurnTime;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.IConfigurableMachine;
import api.hbm.tile.IHeatSource;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.menus.HeaterOvenMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import java.io.IOException;

public class HeaterOvenBlockEntity extends HeaterFireboxBlockEntity {

    /** Eigenes Modul, siehe getBurnModule() im Brennkasten. Werte wie im Original identisch. */
    public static final ModuleBurnTime burnModule = new ModuleBurnTime()
            .setLigniteTimeMod(1.25D)
            .setCoalTimeMod(1.25D)
            .setCokeTimeMod(1.25D)
            .setSolidTimeMod(1.5D)
            .setRocketTimeMod(1.5D)
            .setBalefireTimeMod(0.5D)
            .setLigniteHeatMod(2D)
            .setCoalHeatMod(2D)
            .setCokeHeatMod(2D)
            .setSolidHeatMod(3D)
            .setRocketHeatMod(5D)
            .setBalefireHeatMod(15D);

    @Override protected ModuleBurnTime getBurnModule() { return burnModule; }

    public static int baseHeat = 500;
    public static double timeMult = 0.125D;
    public static int maxHeatEnergy = 500_000;
    public static double heatEff = 0.5D;

    public HeaterOvenBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.HEATER_OVEN.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.heaterOven");
    }

    @Override
    protected int getBaseHeat() {
        return baseHeat;
    }

    @Override
    protected double getTimeMult() {
        return timeMult;
    }

    @Override
    protected int getMaxHeat() {
        return maxHeatEnergy;
    }

    @Override
    public void updateEntity() {
        if(this.level != null && !this.level.isClientSide) {
            this.tryPullHeat();
        }

        super.updateEntity();
    }

    private void tryPullHeat() {
        if(this.level == null) return;
        BlockEntity te = this.level.getBlockEntity(this.getBlockPos().below());
        if(!(te instanceof IHeatSource source)) return;

        int toPull = Math.max(Math.min(source.getHeatStored(), this.getMaxHeat() - this.heatEnergy), 0);
        if(toPull <= 0) return;

        this.heatEnergy += (int) (toPull * heatEff);
        source.useUpHeat(toPull);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new HeaterOvenMenu(id, inventory, this);
    }

    /** Konfiguration, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static void readConfig(JsonObject obj) {
        baseHeat = IConfigurableMachine.grab(obj, "I:baseHeat", baseHeat);
        timeMult = IConfigurableMachine.grab(obj, "D:burnTimeMult", timeMult);
        heatEff = IConfigurableMachine.grab(obj, "D:heatPullEff", heatEff);
        maxHeatEnergy = IConfigurableMachine.grab(obj, "I:heatCap", maxHeatEnergy);

        if(obj.has("M:burnModule")) burnModule.readIfPresent(obj.get("M:burnModule").getAsJsonObject());
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:baseHeat").value(baseHeat);
        writer.name("D:burnTimeMult").value(timeMult);
        writer.name("D:heatPullEff").value(heatEff);
        writer.name("I:heatCap").value(maxHeatEnergy);

        writer.name("M:burnModule").beginObject();
        burnModule.writeConfig(writer);
        writer.endObject();
    }
}
