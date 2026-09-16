package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardTime.
 *
 * Uhrzeit und Tag der Welt. Die Karte braucht kein Ziel und keine Reichweite.
 */
public class ItemCardTime extends ItemCardBase {

    public ItemCardTime(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRemoteCard(ItemStack stack) {
        return false;
    }

    @Override
    public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
        long time = level.getDayTime();
        reader.setLong("time", time % 24000L);
        reader.setLong("day", time / 24000L);
        reader.setBoolean("raining", level.isRaining());
        reader.setBoolean("thundering", level.isThundering());
        return CardState.OK;
    }

    @Override
    public List<PanelString> getStringData(int settings, ICardReader reader, boolean showLabels) {
        List<PanelString> result = reader.getTitleList();

        long time = reader.getLong("time");
        // Minecraft zaehlt den Tag ab 6:00 Uhr morgens.
        long hours = (time / 1000L + 6L) % 24L;
        long minutes = time % 1000L * 60L / 1000L;

        if((settings & 1) > 0)
            result.add(PanelString.of("msg.ec.InfoPanelTime", String.format("%02d:%02d", hours, minutes), showLabels));
        if((settings & 2) > 0)
            result.add(PanelString.of("msg.ec.InfoPanelDay", reader.getLong("day"), showLabels));
        if((settings & 4) > 0)
            result.add(new PanelString(Component.translatable(reader.getBoolean("thundering") ? "msg.ec.InfoPanelThunder"
                    : reader.getBoolean("raining") ? "msg.ec.InfoPanelRain" : "msg.ec.InfoPanelClear")));
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        List<PanelSetting> result = new ArrayList<>(3);
        result.add(new PanelSetting("msg.ec.cbInfoPanelTime", 1));
        result.add(new PanelSetting("msg.ec.cbInfoPanelDay", 2));
        result.add(new PanelSetting("msg.ec.cbInfoPanelWeather", 4));
        return result;
    }
}
