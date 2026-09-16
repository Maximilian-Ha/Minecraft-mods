package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.blockentity.EnergyCounterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardCounter.
 *
 * Die Zaehlerkarte. Sie misst nichts selbst, sondern liest den Energiezaehler aus, der in
 * der Leitung sitzt -- Zaehlerstand und Durchsatz je Tick.
 */
public class ItemCardCounter extends ItemCardBase {

    private static final int SHOW_COUNTER = 1;
    private static final int SHOW_RATE = 2;
    private static final int SHOW_ENERGY = 4;

    private static final String COUNTER = "counter";
    private static final String RATE = "rate";
    private static final String ENERGY = "energy";
    private static final String CAPACITY = "capacity";

    public ItemCardCounter(Properties properties) {
        super(properties);
    }

    /** Ob an dieser Stelle ein Zaehler steht -- der Bausatz fragt danach. */
    public static boolean isCounter(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof EnergyCounterBlockEntity;
    }

    @Override
    public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
        BlockPos target = reader.getTarget();
        if(target == null) return CardState.NO_TARGET;
        if(!inRange(target, pos, range)) return CardState.OUT_OF_RANGE;

        BlockEntity be = level.getBlockEntity(target);
        if(!(be instanceof EnergyCounterBlockEntity counter)) return CardState.NO_TARGET;

        reader.reset();
        reader.setLong(COUNTER, counter.getCounter());
        reader.setDouble(RATE, counter.getAverage());
        reader.setLong(ENERGY, counter.getEnergyStored());
        reader.setLong(CAPACITY, counter.getMaxEnergyStored());
        return CardState.OK;
    }

    @Override
    public List<PanelString> getStringData(int settings, ICardReader reader, boolean showLabels) {
        List<PanelString> result = reader.getTitleList();

        if((settings & SHOW_COUNTER) > 0)
            result.add(PanelString.of("msg.ec.InfoPanelCounter", reader.getLong(COUNTER), "FE", showLabels));

        if((settings & SHOW_RATE) > 0)
            result.add(PanelString.of("msg.ec.InfoPanelDifference", reader.getDouble(RATE), "FE/t", showLabels));

        if((settings & SHOW_ENERGY) > 0) {
            result.add(PanelString.of("msg.ec.InfoPanelEnergy", reader.getLong(ENERGY), "FE", showLabels));
            result.add(PanelString.of("msg.ec.InfoPanelCapacity", reader.getLong(CAPACITY), "FE", showLabels));
        }

        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        List<PanelSetting> result = new ArrayList<>(3);
        result.add(new PanelSetting("msg.ec.cbInfoPanelCounter", SHOW_COUNTER));
        result.add(new PanelSetting("msg.ec.cbInfoPanelDifference", SHOW_RATE));
        result.add(new PanelSetting("msg.ec.cbInfoPanelEnergy", SHOW_ENERGY));
        return result;
    }
}
