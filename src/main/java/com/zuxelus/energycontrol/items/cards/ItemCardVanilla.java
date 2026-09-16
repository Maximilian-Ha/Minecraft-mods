package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.utils.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.items.cards.ItemCardVanilla.
 *
 * Bloecke von Minecraft selbst: Oefen mit Brenndauer und Fortschritt, Braustaender,
 * und fuer alles andere der Vergleicherwert.
 */
public class ItemCardVanilla extends ItemCardBase {

    public ItemCardVanilla(Properties properties) {
        super(properties);
    }

    @Override
    public CardState update(Level level, ICardReader reader, int range, BlockPos pos) {
        BlockPos target = reader.getTarget();
        if(target == null) return CardState.NO_TARGET;
        if(!inRange(target, pos, range)) return CardState.OUT_OF_RANGE;

        BlockEntity be = level.getBlockEntity(target);
        if(be == null) return CardState.NO_TARGET;

        reader.reset();

        BlockState state = level.getBlockState(target);
        reader.setString("block", state.getBlock().getName().getString());
        reader.setInt("comparator", state.getAnalogOutputSignal(level, target));

        /*
         * Ofen und Braustaender halten Brenndauer und Fortschritt in Feldern ohne
         * oeffentlichen Zugriff. Statt eines Zugriffswandlers nur fuer diese drei Zahlen
         * geht die Karte ueber den gespeicherten Zustand des Blocks -- die Schluessel
         * "BurnTime", "CookTime", "CookTimeTotal" und "BrewTime" schreibt Minecraft selbst,
         * und sie gelten fuer jeden Ofen und jeden Braustaender, auch fuer die von Mods,
         * die davon erben.
         */
        CompoundTag data = be.saveWithoutMetadata(level.registryAccess());

        if(data.contains("CookTimeTotal")) {
            reader.setInt(DataHelper.PROGRESS, data.getInt("CookTime"));
            reader.setInt(DataHelper.MAXPROGRESS, data.getInt("CookTimeTotal"));
        }
        if(data.contains("BurnTime")) {
            reader.setInt("burnTime", data.getInt("BurnTime"));
            reader.setBoolean(DataHelper.ACTIVE, data.getInt("BurnTime") > 0);
        }
        if(data.contains("BrewTime")) {
            reader.setInt(DataHelper.PROGRESS, 400 - data.getInt("BrewTime"));
            reader.setInt(DataHelper.MAXPROGRESS, 400);
            reader.setBoolean(DataHelper.ACTIVE, data.getInt("BrewTime") > 0);
        }

        return CardState.OK;
    }

    @Override
    public List<PanelString> getStringData(int settings, ICardReader reader, boolean showLabels) {
        List<PanelString> result = reader.getTitleList();

        if((settings & 1) > 0 && reader.hasField("block"))
            result.add(PanelString.of("msg.ec.InfoPanelBlock", reader.getString("block"), showLabels));

        if((settings & 2) > 0 && reader.hasField(DataHelper.MAXPROGRESS)) {
            int max = reader.getInt(DataHelper.MAXPROGRESS);
            result.add(PanelString.of("msg.ec.InfoPanelProgress",
                    max <= 0 ? 0 : reader.getInt(DataHelper.PROGRESS) * 100D / max, "%", showLabels));
        }

        if((settings & 4) > 0 && reader.hasField("burnTime"))
            result.add(PanelString.of("msg.ec.InfoPanelBurnTime", reader.getInt("burnTime"), showLabels));

        if((settings & 8) > 0)
            result.add(PanelString.of("msg.ec.InfoPanelComparator", reader.getInt("comparator"), showLabels));

        if(reader.hasField(DataHelper.ACTIVE)) addOnOff(result, reader.getBoolean(DataHelper.ACTIVE));
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList(ItemStack stack) {
        List<PanelSetting> result = new ArrayList<>(4);
        result.add(new PanelSetting("msg.ec.cbInfoPanelBlock", 1));
        result.add(new PanelSetting("msg.ec.cbInfoPanelProgress", 2));
        result.add(new PanelSetting("msg.ec.cbInfoPanelBurnTime", 4));
        result.add(new PanelSetting("msg.ec.cbInfoPanelComparator", 8));
        return result;
    }
}
