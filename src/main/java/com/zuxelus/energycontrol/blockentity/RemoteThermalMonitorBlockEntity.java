package com.zuxelus.energycontrol.blockentity;

import com.zuxelus.energycontrol.ECConfig;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.init.ECBlockEntityTypes;
import com.zuxelus.energycontrol.items.ItemUpgrade;
import com.zuxelus.energycontrol.items.ItemUpgrade.UpgradeType;
import com.zuxelus.energycontrol.items.cards.ItemCardBase;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.tileentities.TileEntityRemoteThermalMonitor.
 *
 * Die Fernwaermeanzeige. Sie ist ein Waermemelder, der seinen Reaktor nicht in der
 * Nachbarschaft sucht, sondern dort misst, wohin die eingelegte Karte eingemessen ist.
 * Damit haengt der Melder nicht mehr am Reaktor, sondern steht, wo man ihn sehen will.
 *
 * Die Reichweite ist begrenzt und waechst mit Reichweitenaufwertungen im zweiten Fach --
 * dieselbe Rechnung wie bei der Informationstafel.
 */
public class RemoteThermalMonitorBlockEntity extends ThermalMonitorBlockEntity {

    public static final int SLOT_CARD = 0;
    public static final int SLOT_UPGRADE_RANGE = 1;

    /** Reichweite ohne Aufwertung, in Bloecken -- wie im Original. */
    private static final int BASE_RANGE = 8;

    public RemoteThermalMonitorBlockEntity(BlockPos pos, BlockState state) {
        super(ECBlockEntityTypes.REMOTE_THERMAL_MONITOR.get(), pos, state, 2);
    }

    @Override
    protected int findHeat() {
        ItemStack stack = getItem(SLOT_CARD);
        if(!ItemCardBase.isCard(stack)) return -1;

        BlockPos target = new ItemCardReader(stack).getTarget();
        if(target == null) return -1;

        int range = getRange();
        if(target.distSqr(worldPosition) > (double) range * range) return -1;

        return CrossModLoader.getHeat(level, target);
    }

    public int getRange() {
        ItemStack stack = getItem(SLOT_UPGRADE_RANGE);
        if(!ItemUpgrade.is(stack, UpgradeType.RANGE)) return BASE_RANGE;
        return BASE_RANGE + stack.getCount() * ECConfig.rangeUpgradeRange();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch(slot) {
            case SLOT_CARD -> ItemCardBase.isCard(stack);
            case SLOT_UPGRADE_RANGE -> ItemUpgrade.is(stack, UpgradeType.RANGE);
            default -> false;
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.energycontrol.remote_thermal_monitor");
    }
}
