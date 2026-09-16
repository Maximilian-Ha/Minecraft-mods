package com.zuxelus.energycontrol.init;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.menus.CardHolderMenu;
import com.zuxelus.energycontrol.menus.EnergyCounterMenu;
import com.zuxelus.energycontrol.menus.InfoPanelMenu;
import com.zuxelus.energycontrol.menus.KitAssemblerMenu;
import com.zuxelus.energycontrol.menus.PortablePanelMenu;
import com.zuxelus.energycontrol.menus.RangeTriggerMenu;
import com.zuxelus.energycontrol.menus.ThermalMonitorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Die Oberflaechen des Mods. */
public class ECMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, EnergyControl.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<InfoPanelMenu>> INFO_PANEL = reg("info_panel", InfoPanelMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ThermalMonitorMenu>> THERMAL_MONITOR = reg("thermal_monitor", ThermalMonitorMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<RangeTriggerMenu>> RANGE_TRIGGER = reg("range_trigger", RangeTriggerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<EnergyCounterMenu>> ENERGY_COUNTER = reg("energy_counter", EnergyCounterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<KitAssemblerMenu>> KIT_ASSEMBLER = reg("kit_assembler", KitAssemblerMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CardHolderMenu>> CARD_HOLDER = reg("card_holder", CardHolderMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<PortablePanelMenu>> PORTABLE_PANEL = reg("portable_panel", PortablePanelMenu::new);

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> reg(String name, IContainerFactory<T> factory) {
        return MENU_TYPES.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
