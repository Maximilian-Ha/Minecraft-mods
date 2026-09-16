package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import com.zuxelus.energycontrol.utils.BlockInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardInventory.
 *
 * Inhalt einer Kiste: belegte Faecher, Gesamtzahl der Gegenstaende und die Posten selbst.
 */
public class ItemCardInventory extends ItemCardBase {

    private static final int MAX_LINES = 8;

    public ItemCardInventory(Properties properties) {
        super(properties);
    }

    @Override
    public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
        BlockPos target = reader.getTarget();
        if(target == null) return CardState.NO_TARGET;
        if(!inRange(target, pos, range)) return CardState.OUT_OF_RANGE;

        BlockInventory inventory = BlockInventory.of(level, target);
        if(inventory == null) return CardState.NO_TARGET;

        reader.reset();

        int used = 0;
        int total = 0;
        Map<String, Integer> byName = new LinkedHashMap<>();

        for(int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if(stack.isEmpty()) continue;
            used++;
            total += stack.getCount();
            byName.merge(stack.getHoverName().getString(), stack.getCount(), Integer::sum);
        }

        reader.setInt("slots", inventory.size());
        reader.setInt("usedSlots", used);
        reader.setInt("totalItems", total);

        ListTag items = new ListTag();
        for(Map.Entry<String, Integer> entry : byName.entrySet()) {
            if(items.size() >= MAX_LINES) break;
            CompoundTag line = new CompoundTag();
            line.putString("name", entry.getKey());
            line.putInt("count", entry.getValue());
            items.add(line);
        }
        reader.setList("items", items);

        return CardState.OK;
    }

    @Override
    public List<PanelString> getStringData(int settings, ICardReader reader, boolean showLabels) {
        List<PanelString> result = reader.getTitleList();

        int slots = reader.getInt("slots");
        int used = reader.getInt("usedSlots");

        if((settings & 1) > 0) result.add(PanelString.of("msg.ec.InfoPanelSlots", used + " / " + slots, showLabels));
        if((settings & 2) > 0) result.add(PanelString.of("msg.ec.InfoPanelPercentage", slots == 0 ? 0 : used * 100D / slots, "%", showLabels));
        if((settings & 4) > 0) result.add(PanelString.of("msg.ec.InfoPanelTotalItems", reader.getInt("totalItems"), showLabels));

        if((settings & 8) > 0) {
            ListTag items = reader.getList("items", Tag.TAG_COMPOUND);
            for(int i = 0; i < items.size(); i++) {
                CompoundTag line = items.getCompound(i);
                result.add(new PanelString(Component.literal(line.getString("name") + ": " + line.getInt("count"))));
            }
        }

        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        List<PanelSetting> result = new ArrayList<>(4);
        result.add(new PanelSetting("msg.ec.cbInfoPanelSlots", 1));
        result.add(new PanelSetting("msg.ec.cbInfoPanelPercentage", 2));
        result.add(new PanelSetting("msg.ec.cbInfoPanelTotalItems", 4));
        result.add(new PanelSetting("msg.ec.cbInfoPanelItemList", 8));
        return result;
    }
}
