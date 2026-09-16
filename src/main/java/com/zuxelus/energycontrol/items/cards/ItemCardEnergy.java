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

        reader.reset();
        reader.copyFrom(tag);
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
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        List<PanelSetting> result = new ArrayList<>(4);
        result.add(new PanelSetting("msg.ec.cbInfoPanelEnergy", 1));
        result.add(new PanelSetting("msg.ec.cbInfoPanelFree", 2));
        result.add(new PanelSetting("msg.ec.cbInfoPanelCapacity", 4));
        result.add(new PanelSetting("msg.ec.cbInfoPanelPercentage", 8));
        return result;
    }
}
