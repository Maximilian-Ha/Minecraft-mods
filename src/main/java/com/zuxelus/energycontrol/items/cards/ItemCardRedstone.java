package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardRedstone.
 *
 * Das Redstone-Signal an einer Stelle -- was dort ankommt und was der Block selbst
 * als Vergleicherwert abgibt.
 */
public class ItemCardRedstone extends ItemCardBase {

    public ItemCardRedstone(Properties properties) {
        super(properties);
    }

    @Override
    public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
        BlockPos target = reader.getTarget();
        if(target == null) return CardState.NO_TARGET;
        if(!inRange(target, pos, range)) return CardState.OUT_OF_RANGE;
        if(!level.isLoaded(target)) return CardState.NO_TARGET;

        reader.reset();
        reader.setInt("signal", level.getBestNeighborSignal(target));
        reader.setInt("comparator", level.getBlockState(target).getAnalogOutputSignal(level, target));
        reader.setBoolean("powered", level.hasNeighborSignal(target));
        return CardState.OK;
    }

    @Override
    public List<PanelString> getStringData(int settings, ICardReader reader, boolean showLabels) {
        List<PanelString> result = reader.getTitleList();
        if((settings & 1) > 0) result.add(PanelString.of("msg.ec.InfoPanelSignal", reader.getInt("signal"), showLabels));
        if((settings & 2) > 0) result.add(PanelString.of("msg.ec.InfoPanelComparator", reader.getInt("comparator"), showLabels));
        if((settings & 4) > 0) addOnOff(result, reader.getBoolean("powered"));
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        List<PanelSetting> result = new ArrayList<>(3);
        result.add(new PanelSetting("msg.ec.cbInfoPanelSignal", 1));
        result.add(new PanelSetting("msg.ec.cbInfoPanelComparator", 2));
        result.add(new PanelSetting("msg.ec.cbInfoPanelStatus", 4));
        return result;
    }
}
