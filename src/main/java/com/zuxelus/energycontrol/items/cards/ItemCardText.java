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
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardText.
 *
 * Feste Zeilen, die der Spieler selbst eintraegt -- zum Beschriften einer Wand aus
 * Tafeln. Die Karte misst nichts und braucht kein Ziel.
 */
public class ItemCardText extends ItemCardBase {

    public static final int MAX_LINES = 8;

    public ItemCardText(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRemoteCard(ItemStack stack) {
        return false;
    }

    @Override
    public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
        return CardState.OK;
    }

    @Override
    public List<PanelString> getStringData(int settings, ICardReader reader, boolean showLabels) {
        List<PanelString> result = reader.getTitleList();
        for(int i = 0; i < MAX_LINES; i++) {
            if((settings & (1 << i)) == 0) continue;
            String text = reader.getString("line" + i);
            if(text == null || text.isEmpty()) continue;
            PanelString line = new PanelString(Component.literal(text));
            line.colorLeft = reader.getInt("color" + i);
            result.add(line);
        }
        return result;
    }

    /**
     * Angeboten wird nur, was die Karte auch hat. Zeilen bekommt sie zurzeit allein aus
     * ihrem Namen (Ueberschrift); ein Textfeld in der Oberflaeche steht noch aus, siehe
     * docs/ROADMAP.md. Acht leere Ankreuzfelder waeren ein Versprechen, das der Port
     * nicht haelt.
     */
    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        ItemCardReader reader = new ItemCardReader(stack);
        List<PanelSetting> result = new ArrayList<>();
        for(int i = 0; i < MAX_LINES; i++) {
            if(!reader.hasField("line" + i)) continue;
            result.add(new PanelSetting(Component.translatable("msg.ec.cbInfoPanelLineNo", i + 1), 1 << i));
        }
        return result;
    }
}
