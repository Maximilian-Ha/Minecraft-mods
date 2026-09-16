package com.zuxelus.energycontrol.init;

import com.zuxelus.energycontrol.EnergyControl;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Der Reiter im Kreativmodus. */
public class ECCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EnergyControl.MODID);

    public static final Supplier<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ECBlocks.INFO_PANEL.get()))
                    .title(Component.translatable("itemGroup.energycontrol"))
                    .displayItems((parameters, output) -> {
                        output.accept(ECBlocks.INFO_PANEL.get());
                        output.accept(ECBlocks.INFO_PANEL_EXTENDER.get());
                        output.accept(ECBlocks.ADVANCED_INFO_PANEL.get());
                        output.accept(ECBlocks.ADVANCED_INFO_PANEL_EXTENDER.get());
                        output.accept(ECBlocks.THERMAL_MONITOR.get());
                        output.accept(ECBlocks.REMOTE_THERMAL_MONITOR.get());
                        output.accept(ECBlocks.RANGE_TRIGGER.get());
                        output.accept(ECBlocks.ENERGY_COUNTER.get());
                        output.accept(ECBlocks.KIT_ASSEMBLER.get());
                        output.accept(ECBlocks.HOWLER_ALARM.get());
                        output.accept(ECBlocks.INDUSTRIAL_ALARM.get());

                        output.accept(ECItems.CARD_ENERGY.get());
                        output.accept(ECItems.CARD_LIQUID.get());
                        output.accept(ECItems.CARD_INVENTORY.get());
                        output.accept(ECItems.CARD_REDSTONE.get());
                        output.accept(ECItems.CARD_VANILLA.get());
                        output.accept(ECItems.CARD_TIME.get());
                        output.accept(ECItems.CARD_TEXT.get());
                        output.accept(ECItems.CARD_TOGGLE.get());
                        output.accept(ECItems.CARD_HBM.get());
                        output.accept(ECItems.CARD_MEKANISM.get());
                        output.accept(ECItems.CARD_COUNTER.get());

                        output.accept(ECItems.KIT_ENERGY.get());
                        output.accept(ECItems.KIT_LIQUID.get());
                        output.accept(ECItems.KIT_INVENTORY.get());
                        output.accept(ECItems.KIT_REDSTONE.get());
                        output.accept(ECItems.KIT_VANILLA.get());
                        output.accept(ECItems.KIT_TOGGLE.get());
                        output.accept(ECItems.KIT_HBM.get());
                        output.accept(ECItems.KIT_MEKANISM.get());
                        output.accept(ECItems.KIT_COUNTER.get());

                        output.accept(ECItems.CARD_HOLDER.get());
                        output.accept(ECItems.PORTABLE_PANEL.get());

                        output.accept(ECItems.UPGRADE_RANGE.get());
                        output.accept(ECItems.UPGRADE_COLOR.get());
                        output.accept(ECItems.UPGRADE_TOUCH.get());

                        output.accept(ECItems.MACHINE_CASING.get());
                        output.accept(ECItems.BASIC_CIRCUIT.get());
                        output.accept(ECItems.ADVANCED_CIRCUIT.get());
                        output.accept(ECItems.THERMOMETER.get());
                        output.accept(ECItems.PANEL_TOOLKIT.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
