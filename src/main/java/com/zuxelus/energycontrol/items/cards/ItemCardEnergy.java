package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.utils.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardEnergy.
 *
 * Fuellstand eines Stromspeichers. Die Einheit kommt vom Ziel mit: HE bei HBM,
 * FE bei allem, was die Energie-Schnittstelle von NeoForge anbietet.
 */
public class ItemCardEnergy extends ItemCardBase {

    /**
     * Der letzte Messwert samt Zeitpunkt. Daraus rechnet die Karte, wieviel in der Zwischenzeit
     * je Tick hinzugekommen oder abgeflossen ist -- die Zahl, wegen der man eine Tafel an einen
     * Akku haengt. Der HBM-Port fuehrt sie selbst mit (delta); fuer alle anderen Mods entsteht
     * sie hier, ohne dass die Mod etwas dafuer tun muss.
     */
    private static final String FIELD_LAST_ENERGY = "lastEnergy";
    private static final String FIELD_LAST_TIME = "lastTime";

    public ItemCardEnergy(Properties properties) {
        super(properties);
    }

    @Override
    public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
        BlockPos target = reader.getTarget();
        if(target == null) return CardState.NO_TARGET;
        if(!inRange(target, pos, range)) return CardState.OUT_OF_RANGE;

        BlockEntity be = level.getBlockEntity(target);
        CompoundTag tag = CrossModLoader.getEnergyData(be);
        if(tag == null) return CardState.NO_TARGET;

        // Vor dem Leeren merken: daraus wird gleich die Aenderung je Tick.
        boolean hadPrevious = reader.hasField(FIELD_LAST_TIME);
        double previousEnergy = reader.getDouble(FIELD_LAST_ENERGY);
        long previousTime = reader.getLong(FIELD_LAST_TIME);

        reader.reset();
        reader.copyFrom(tag);

        double energy = reader.getDouble(DataHelper.ENERGY);
        long now = level.getGameTime();

        if(hadPrevious && now > previousTime) {
            reader.setDouble(DataHelper.DIFF, (energy - previousEnergy) / (now - previousTime));
        }

        reader.setDouble(FIELD_LAST_ENERGY, energy);
        reader.setLong(FIELD_LAST_TIME, now);
        return CardState.OK;
    }

    @Override
    public List<PanelString> getStringData(int settings, ICardReader reader, boolean showLabels) {
        List<PanelString> result = reader.getTitleList();

        double energy = reader.getDouble(DataHelper.ENERGY);
        double capacity = reader.getDouble(DataHelper.CAPACITY);
        String unit = reader.getString(DataHelper.EUTYPE);

        if((settings & 1) > 0) result.add(PanelString.of("msg.ec.InfoPanelEnergy", energy, unit, showLabels));
        if((settings & 2) > 0) result.add(PanelString.of("msg.ec.InfoPanelFree", capacity - energy, unit, showLabels));
        if((settings & 4) > 0) result.add(PanelString.of("msg.ec.InfoPanelCapacity", capacity, unit, showLabels));
        if((settings & 8) > 0) result.add(PanelString.of("msg.ec.InfoPanelPercentage", capacity == 0 ? 100 : energy / capacity * 100, "%", showLabels));
        if((settings & 16) > 0 && reader.hasField(DataHelper.DIFF))
            result.add(PanelString.of("msg.ec.InfoPanelDifference", reader.getDouble(DataHelper.DIFF), unit + "/t", showLabels));
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        List<PanelSetting> result = new ArrayList<>(5);
        result.add(new PanelSetting("msg.ec.cbInfoPanelEnergy", 1));
        result.add(new PanelSetting("msg.ec.cbInfoPanelFree", 2));
        result.add(new PanelSetting("msg.ec.cbInfoPanelCapacity", 4));
        result.add(new PanelSetting("msg.ec.cbInfoPanelPercentage", 8));
        result.add(new PanelSetting("msg.ec.cbInfoPanelDifference", 16));
        return result;
    }
}
