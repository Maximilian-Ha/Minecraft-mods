package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.crossmod.MekanismFields;
import com.zuxelus.energycontrol.crossmod.ModIDs;
import com.zuxelus.energycontrol.utils.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardMekanism.
 *
 * Die Karte fuer Mekanism. Wie die HBM-Karte zeigt sie, was die Anbindung ueber den Block
 * herausgefunden hat, und wie dort sind die Zeilen in Gruppen gefasst: ein Ankreuzfeld
 * soll nicht auf ein Feld zeigen, das dieser Block gar nicht hat.
 *
 * Die Beschriftungen sind eigene Schluessel ({@code msg.ec.*}) -- der Mod uebersetzt seine
 * Tafel selbst und greift nie in die Sprachdateien von Mekanism.
 */
public class ItemCardMekanism extends ItemCardBase {

    private static final int SHOW_ENERGY = 1;
    private static final int SHOW_TANKS = 2;
    private static final int SHOW_HEAT = 4;
    private static final int SHOW_REACTOR = 8;
    private static final int SHOW_TURBINE = 16;
    private static final int SHOW_MATRIX = 32;
    private static final int SHOW_PROGRESS = 64;
    private static final int SHOW_MINER = 128;

    public ItemCardMekanism(Properties properties) {
        super(properties);
    }

    @Override
    public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
        BlockPos target = reader.getTarget();
        if(target == null) return CardState.NO_TARGET;
        if(!inRange(target, pos, range)) return CardState.OUT_OF_RANGE;

        CompoundTag tag = CrossModLoader.getCrossMod(ModIDs.MEKANISM).getCardData(level, target);
        if(tag == null) return CardState.NO_TARGET;

        reader.reset();
        reader.copyFrom(tag);
        return CardState.OK;
    }

    @Override
    public List<PanelString> getStringData(int settings, ICardReader reader, boolean showLabels) {
        List<PanelString> result = reader.getTitleList();

        if((settings & SHOW_ENERGY) > 0) {
            if(reader.hasField(DataHelper.ENERGY))
                result.add(PanelString.of("msg.ec.InfoPanelEnergy", reader.getLong(DataHelper.ENERGY), "J", showLabels));
            if(reader.hasField(DataHelper.CAPACITY))
                result.add(PanelString.of("msg.ec.InfoPanelCapacity", reader.getLong(DataHelper.CAPACITY), "J", showLabels));
            if(reader.hasField(DataHelper.DIFF))
                result.add(PanelString.of("msg.ec.InfoPanelDifference", reader.getLong(DataHelper.DIFF), "J/t", showLabels));
            if(reader.hasField(MekanismFields.RECEIVED_ENERGY))
                result.add(PanelString.of("msg.ec.InfoPanelReceivedEnergy", reader.getLong(MekanismFields.RECEIVED_ENERGY), "J/t", showLabels));
        }

        if((settings & SHOW_TANKS) > 0) {
            int tanks = reader.getInt(DataHelper.TANK_COUNT);
            for(int i = 0; i < tanks; i++) {
                result.add(PanelString.of("msg.ec.InfoPanelTank", reader.getString(DataHelper.tank(i)), showLabels));
            }
        }

        if((settings & SHOW_HEAT) > 0) {
            if(reader.hasField(MekanismFields.TEMPERATURE))
                addHeat(result, "msg.ec.InfoPanelHeat", reader.getDouble(MekanismFields.TEMPERATURE),
                        reader.getDouble(MekanismFields.MAX_TEMPERATURE), showLabels);
            if(reader.hasField(MekanismFields.MAX_TEMPERATURE))
                result.add(PanelString.of("msg.ec.InfoPanelMeltingPoint", reader.getDouble(MekanismFields.MAX_TEMPERATURE), "K", showLabels));
            if(reader.hasField(MekanismFields.ENV_LOSS))
                result.add(PanelString.of("msg.ec.InfoPanelEnvLoss", reader.getDouble(MekanismFields.ENV_LOSS), "K/t", showLabels));
        }

        if((settings & SHOW_REACTOR) > 0) {
            if(reader.hasField(MekanismFields.BURN_RATE))
                result.add(PanelString.of("msg.ec.InfoPanelBurnRate", reader.getDouble(MekanismFields.BURN_RATE), "mB/t", showLabels));
            if(reader.hasField(MekanismFields.RATE_LIMIT))
                result.add(PanelString.of("msg.ec.InfoPanelRateLimit", reader.getDouble(MekanismFields.RATE_LIMIT), "mB/t", showLabels));
            if(reader.hasField(MekanismFields.MAX_BURN_RATE))
                result.add(PanelString.of("msg.ec.InfoPanelMaxBurnRate", reader.getLong(MekanismFields.MAX_BURN_RATE), "mB/t", showLabels));
            if(reader.hasField(MekanismFields.DAMAGE))
                result.add(PanelString.of("msg.ec.InfoPanelDamage", reader.getDouble(MekanismFields.DAMAGE), "%", showLabels));
            if(reader.hasField(MekanismFields.BOIL_RATE))
                result.add(PanelString.of("msg.ec.InfoPanelBoilRate", reader.getLong(MekanismFields.BOIL_RATE), "mB/t", showLabels));
            if(reader.hasField(MekanismFields.MAX_BOIL_RATE))
                result.add(PanelString.of("msg.ec.InfoPanelMaxBoilRate", reader.getLong(MekanismFields.MAX_BOIL_RATE), "mB/t", showLabels));
            if(reader.hasField(MekanismFields.SUPERHEATERS))
                result.add(PanelString.of("msg.ec.InfoPanelSuperheaters", reader.getLong(MekanismFields.SUPERHEATERS), showLabels));
            if(reader.hasField(MekanismFields.ASSEMBLIES))
                result.add(PanelString.of("msg.ec.InfoPanelAssemblies", reader.getLong(MekanismFields.ASSEMBLIES), showLabels));
            if(reader.hasField(MekanismFields.SURFACE_AREA))
                result.add(PanelString.of("msg.ec.InfoPanelSurfaceArea", reader.getLong(MekanismFields.SURFACE_AREA), showLabels));
            if(reader.hasField(MekanismFields.PLASMA_TEMPERATURE))
                result.add(PanelString.of("msg.ec.InfoPanelPlasma", reader.getDouble(MekanismFields.PLASMA_TEMPERATURE), "K", showLabels));
            if(reader.hasField(MekanismFields.CASE_TEMPERATURE))
                result.add(PanelString.of("msg.ec.InfoPanelCaseHeat", reader.getDouble(MekanismFields.CASE_TEMPERATURE), "K", showLabels));
            if(reader.hasField(MekanismFields.INJECTION_RATE))
                result.add(PanelString.of("msg.ec.InfoPanelInjectionRate", reader.getLong(MekanismFields.INJECTION_RATE), "mB/t", showLabels));
            if(reader.hasField(MekanismFields.PASSIVE_GENERATION))
                result.add(PanelString.of("msg.ec.InfoPanelPassiveGeneration", reader.getLong(MekanismFields.PASSIVE_GENERATION), "J/t", showLabels));
            if(reader.hasField(MekanismFields.STEAM_PER_TICK))
                result.add(PanelString.of("msg.ec.InfoPanelSteamPerTick", reader.getLong(MekanismFields.STEAM_PER_TICK), "mB/t", showLabels));
        }

        if((settings & SHOW_TURBINE) > 0) {
            if(reader.hasField(MekanismFields.PRODUCTION))
                result.add(PanelString.of("msg.ec.InfoPanelProduction", reader.getLong(MekanismFields.PRODUCTION), "J/t", showLabels));
            if(reader.hasField(MekanismFields.MAX_PRODUCTION))
                result.add(PanelString.of("msg.ec.InfoPanelMaxProduction", reader.getLong(MekanismFields.MAX_PRODUCTION), "J/t", showLabels));
            if(reader.hasField(MekanismFields.FLOW))
                result.add(PanelString.of("msg.ec.InfoPanelFlow", reader.getLong(MekanismFields.FLOW), "mB/t", showLabels));
            if(reader.hasField(MekanismFields.MAX_FLOW))
                result.add(PanelString.of("msg.ec.InfoPanelMaxFlow", reader.getLong(MekanismFields.MAX_FLOW), "mB/t", showLabels));
            if(reader.hasField(MekanismFields.BLADES))
                result.add(PanelString.of("msg.ec.InfoPanelBlades", reader.getLong(MekanismFields.BLADES), showLabels));
            if(reader.hasField(MekanismFields.COILS))
                result.add(PanelString.of("msg.ec.InfoPanelCoils", reader.getLong(MekanismFields.COILS), showLabels));
            if(reader.hasField(MekanismFields.VENTS))
                result.add(PanelString.of("msg.ec.InfoPanelVents", reader.getLong(MekanismFields.VENTS), showLabels));
            if(reader.hasField(MekanismFields.CONDENSERS))
                result.add(PanelString.of("msg.ec.InfoPanelCondensers", reader.getLong(MekanismFields.CONDENSERS), showLabels));
            if(reader.hasField(MekanismFields.DISPERSERS))
                result.add(PanelString.of("msg.ec.InfoPanelDispersers", reader.getLong(MekanismFields.DISPERSERS), showLabels));
        }

        if((settings & SHOW_MATRIX) > 0) {
            if(reader.hasField(MekanismFields.LAST_INPUT))
                result.add(PanelString.of("msg.ec.InfoPanelInput", reader.getLong(MekanismFields.LAST_INPUT), "J/t", showLabels));
            if(reader.hasField(MekanismFields.LAST_OUTPUT))
                result.add(PanelString.of("msg.ec.InfoPanelOutput", reader.getLong(MekanismFields.LAST_OUTPUT), "J/t", showLabels));
            if(reader.hasField(MekanismFields.TRANSFER_CAP))
                result.add(PanelString.of("msg.ec.InfoPanelTransferCap", reader.getLong(MekanismFields.TRANSFER_CAP), "J/t", showLabels));
            if(reader.hasField(MekanismFields.CELLS))
                result.add(PanelString.of("msg.ec.InfoPanelCells", reader.getLong(MekanismFields.CELLS), showLabels));
            if(reader.hasField(MekanismFields.PROVIDERS))
                result.add(PanelString.of("msg.ec.InfoPanelProviders", reader.getLong(MekanismFields.PROVIDERS), showLabels));
        }

        if((settings & SHOW_PROGRESS) > 0) {
            if(reader.hasField(DataHelper.MAXPROGRESS)) {
                int progress = reader.getInt(DataHelper.PROGRESS);
                int max = reader.getInt(DataHelper.MAXPROGRESS);
                result.add(PanelString.of("msg.ec.InfoPanelProgress", max <= 0 ? 0 : progress * 100D / max, "%", showLabels));
            }
            if(reader.hasField(MekanismFields.PROCESS_RATE))
                result.add(PanelString.of("msg.ec.InfoPanelProcessRate", reader.getDouble(MekanismFields.PROCESS_RATE), showLabels));
            if(reader.hasField(MekanismFields.PROCESSED))
                result.add(PanelString.of("msg.ec.InfoPanelProcessed", reader.getLong(MekanismFields.PROCESSED), showLabels));
            if(reader.hasField(MekanismFields.GAIN))
                result.add(PanelString.of("msg.ec.InfoPanelGain", reader.getDouble(MekanismFields.GAIN), "mB/t", showLabels));
        }

        if((settings & SHOW_MINER) > 0) {
            if(reader.hasField(MekanismFields.TO_MINE))
                result.add(PanelString.of("msg.ec.InfoPanelToMine", reader.getLong(MekanismFields.TO_MINE), showLabels));
            if(reader.hasField(MekanismFields.MINER_STATE))
                result.add(PanelString.of("msg.ec.InfoPanelMinerState", reader.getString(MekanismFields.MINER_STATE), showLabels));
            if(reader.hasField(MekanismFields.RADIUS))
                result.add(PanelString.of("msg.ec.InfoPanelRadius", reader.getLong(MekanismFields.RADIUS), showLabels));
            if(reader.hasField(MekanismFields.MIN_Y))
                result.add(PanelString.of("msg.ec.InfoPanelMinY", reader.getLong(MekanismFields.MIN_Y), showLabels));
            if(reader.hasField(MekanismFields.MAX_Y))
                result.add(PanelString.of("msg.ec.InfoPanelMaxY", reader.getLong(MekanismFields.MAX_Y), showLabels));
        }

        if(reader.hasField(DataHelper.ACTIVE)) addOnOff(result, reader.getBoolean(DataHelper.ACTIVE));
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        List<PanelSetting> result = new ArrayList<>(8);
        result.add(new PanelSetting("msg.ec.cbInfoPanelEnergy", SHOW_ENERGY));
        result.add(new PanelSetting("msg.ec.cbInfoPanelTanks", SHOW_TANKS));
        result.add(new PanelSetting("msg.ec.cbInfoPanelHeat", SHOW_HEAT));
        result.add(new PanelSetting("msg.ec.cbInfoPanelReactor", SHOW_REACTOR));
        result.add(new PanelSetting("msg.ec.cbInfoPanelTurbine", SHOW_TURBINE));
        result.add(new PanelSetting("msg.ec.cbInfoPanelMatrix", SHOW_MATRIX));
        result.add(new PanelSetting("msg.ec.cbInfoPanelProgress", SHOW_PROGRESS));
        result.add(new PanelSetting("msg.ec.cbInfoPanelMiner", SHOW_MINER));
        return result;
    }
}
