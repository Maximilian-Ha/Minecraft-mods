package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.crossmod.ModIDs;
import com.zuxelus.energycontrol.utils.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardHBM.
 *
 * Die Karte fuer HBM's Nuclear Tech Mod. Sie zeigt, was die Anbindung ueber den Block
 * herausgefunden hat -- welche Felder das sind, haengt am Block: ein Reaktor meldet
 * Temperatur und Fluss, eine Maschine Strom, Tanks und Fortschritt.
 *
 * Die Zeilen sind in Gruppen gefasst, damit ein Ankreuzfeld nicht auf ein Feld zeigt,
 * das dieser Block gar nicht hat. Im Original gab es dafuer nur zwei Felder, und die
 * Zeilen erschienen ungefragt.
 */
public class ItemCardHBM extends ItemCardBase {

    private static final int SHOW_ENERGY = 1;
    private static final int SHOW_REACTOR = 2;
    private static final int SHOW_TANKS = 4;
    private static final int SHOW_PROGRESS = 8;
    private static final int SHOW_RADIATION = 16;
    private static final int SHOW_ROR = 32;

    public ItemCardHBM(Properties properties) {
        super(properties);
    }

    @Override
    public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
        BlockPos target = reader.getTarget();
        if(target == null) return CardState.NO_TARGET;
        if(!inRange(target, pos, range)) return CardState.OUT_OF_RANGE;

        CompoundTag tag = CrossModLoader.getCrossMod(ModIDs.HBM).getCardData(level, target);
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
                result.add(PanelString.of("msg.ec.InfoPanelEnergy", reader.getLong(DataHelper.ENERGY), "HE", showLabels));
            if(reader.hasField(DataHelper.CAPACITY))
                result.add(PanelString.of("msg.ec.InfoPanelCapacity", reader.getLong(DataHelper.CAPACITY), "HE", showLabels));
            if(reader.hasField(DataHelper.DIFF))
                result.add(PanelString.of("msg.ec.InfoPanelDifference", reader.getLong(DataHelper.DIFF), "HE/t", showLabels));
            if(reader.hasField(DataHelper.CONSUMPTION))
                result.add(PanelString.of("msg.ec.InfoPanelConsumption", reader.getDouble(DataHelper.CONSUMPTION), "HE/t", showLabels));
        }

        if((settings & SHOW_REACTOR) > 0) {
            if(reader.hasField("heatD"))
                addHeat(result, "msg.ec.InfoPanelHeat", reader.getDouble("heatD"), reader.getDouble("melt"), showLabels);
            if(reader.hasField("melt"))
                result.add(PanelString.of("msg.ec.InfoPanelMeltingPoint", reader.getDouble("melt"), "°C", showLabels));
            if(reader.hasField("c_heat"))
                result.add(PanelString.of("msg.ec.InfoPanelHullHeat", reader.getDouble("c_heat"), "°C", showLabels));
            if(reader.hasField("c_coreHeat"))
                result.add(PanelString.of("msg.ec.InfoPanelCoreHeat", reader.getDouble("c_coreHeat"), "°C", showLabels));
            if(reader.hasField("enrichment"))
                result.add(PanelString.of("msg.ec.InfoPanelDepletion", (1D - reader.getDouble("enrichment")) * 100D, "%", showLabels));
            if(reader.hasField("xenon"))
                result.add(PanelString.of("msg.ec.InfoPanelXenon", reader.getDouble("xenon"), "%", showLabels));
            if(reader.hasField("fluxSlow"))
                result.add(PanelString.of("msg.ec.InfoPanelFluxSlow", reader.getDouble("fluxSlow"), showLabels));
            if(reader.hasField("fluxFast"))
                result.add(PanelString.of("msg.ec.InfoPanelFluxFast", reader.getDouble("fluxFast"), showLabels));
            if(reader.hasField("level"))
                result.add(PanelString.of("msg.ec.InfoPanelOperatingLevel", reader.getDouble("level") * 100D, "%", showLabels));
            if(reader.hasField(DataHelper.PRESSURE))
                result.add(PanelString.of("msg.ec.InfoPanelPressure", reader.getInt(DataHelper.PRESSURE), showLabels));
            if(reader.hasField("fuelText"))
                result.add(PanelString.of("msg.ec.InfoPanelFuel", reader.getString("fuelText"), showLabels));
        }

        if((settings & SHOW_TANKS) > 0) {
            int tanks = reader.getInt(DataHelper.TANK_COUNT);
            for(int i = 0; i < tanks; i++) {
                result.add(PanelString.of("msg.ec.InfoPanelTank", reader.getString(DataHelper.tank(i)), showLabels));
            }
        }

        if((settings & SHOW_PROGRESS) > 0 && reader.hasField(DataHelper.MAXPROGRESS)) {
            int progress = reader.getInt(DataHelper.PROGRESS);
            int max = reader.getInt(DataHelper.MAXPROGRESS);
            result.add(PanelString.of("msg.ec.InfoPanelProgress", max <= 0 ? 0 : progress * 100D / max, "%", showLabels));
        }

        if((settings & SHOW_RADIATION) > 0 && reader.hasField("chunkRad")) {
            result.add(PanelString.of("msg.ec.InfoPanelRadiation", reader.getDouble("chunkRad"), "RAD/s", showLabels));
        }

        if((settings & SHOW_ROR) > 0) {
            for(String key : new String[] { "fill", "fillpercent", "delta", "speed", "temp" }) {
                String field = DataHelper.ROR_PREFIX + key;
                if(reader.hasField(field)) {
                    result.add(new PanelString(Component.literal(key + ": " + reader.getString(field))));
                }
            }
        }

        if(reader.hasField(DataHelper.ACTIVE)) addOnOff(result, reader.getBoolean(DataHelper.ACTIVE));
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        List<PanelSetting> result = new ArrayList<>(6);
        result.add(new PanelSetting("msg.ec.cbInfoPanelEnergy", SHOW_ENERGY));
        result.add(new PanelSetting("msg.ec.cbInfoPanelReactor", SHOW_REACTOR));
        result.add(new PanelSetting("msg.ec.cbInfoPanelTanks", SHOW_TANKS));
        result.add(new PanelSetting("msg.ec.cbInfoPanelProgress", SHOW_PROGRESS));
        result.add(new PanelSetting("msg.ec.cbInfoPanelRadiation", SHOW_RADIATION));
        result.add(new PanelSetting("msg.ec.cbInfoPanelRor", SHOW_ROR));
        return result;
    }
}
