package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.utils.DataHelper;
import com.zuxelus.energycontrol.utils.FluidInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardLiquid.
 *
 * Fuellstaende aller Tanks eines Blocks. Die Anbindungen liefern sie; ohne Anbindung
 * greift die Fluid-Schnittstelle von NeoForge.
 */
public class ItemCardLiquid extends ItemCardBase {

    private static final int MAX_TANKS = 5;

    public ItemCardLiquid(Properties properties) {
        super(properties);
    }

    @Override
    public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
        BlockPos target = reader.getTarget();
        if(target == null) return CardState.NO_TARGET;
        if(!inRange(target, pos, range)) return CardState.OUT_OF_RANGE;

        BlockEntity be = level.getBlockEntity(target);
        List<FluidInfo> tanks = CrossModLoader.getAllTanks(be);
        if(tanks == null || tanks.isEmpty()) return CardState.NO_TARGET;

        reader.reset();
        int count = Math.min(tanks.size(), MAX_TANKS);
        for(int i = 0; i < count; i++) {
            FluidInfo tank = tanks.get(i);
            reader.setString(DataHelper.tank(i), tank.format());
            if(i == 0) {
                reader.setDouble(DataHelper.AMOUNT, tank.amount());
                reader.setDouble(DataHelper.CAPACITY, tank.capacity());
            }
        }
        reader.setInt(DataHelper.TANK_COUNT, count);
        return CardState.OK;
    }

    @Override
    public List<PanelString> getStringData(int settings, ICardReader reader, boolean showLabels) {
        List<PanelString> result = reader.getTitleList();

        int count = reader.getInt(DataHelper.TANK_COUNT);
        for(int i = 0; i < count; i++) {
            if((settings & (1 << i)) == 0) continue;
            result.add(PanelString.of("msg.ec.InfoPanelTank", reader.getString(DataHelper.tank(i)), showLabels));
        }

        if((settings & 32) > 0) {
            double amount = reader.getDouble(DataHelper.AMOUNT);
            double capacity = reader.getDouble(DataHelper.CAPACITY);
            result.add(PanelString.of("msg.ec.InfoPanelPercentage", capacity == 0 ? 0 : amount / capacity * 100, "%", showLabels));
        }

        if(result.isEmpty()) result.add(new PanelString(Component.translatable("msg.ec.InfoPanelEmpty")));
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        List<PanelSetting> result = new ArrayList<>(MAX_TANKS + 1);
        for(int i = 0; i < MAX_TANKS; i++) {
            result.add(new PanelSetting(Component.translatable("msg.ec.cbInfoPanelTankNo", i + 1), 1 << i));
        }
        result.add(new PanelSetting("msg.ec.cbInfoPanelPercentage", 32));
        return result;
    }
}
