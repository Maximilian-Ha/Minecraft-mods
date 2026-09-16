package com.zuxelus.energycontrol.init;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.blockentity.HowlerAlarmBlockEntity;
import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import com.zuxelus.energycontrol.blockentity.RangeTriggerBlockEntity;
import com.zuxelus.energycontrol.blockentity.ThermalMonitorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Die Block-Entitaeten des Mods. */
public class ECBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EnergyControl.MODID);

    public static final Supplier<BlockEntityType<InfoPanelBlockEntity>> INFO_PANEL = BLOCK_ENTITY_TYPES.register(
            "info_panel", () -> BlockEntityType.Builder.of(InfoPanelBlockEntity::new, ECBlocks.INFO_PANEL.get()).build(null));

    public static final Supplier<BlockEntityType<ThermalMonitorBlockEntity>> THERMAL_MONITOR = BLOCK_ENTITY_TYPES.register(
            "thermal_monitor", () -> BlockEntityType.Builder.of(ThermalMonitorBlockEntity::new, ECBlocks.THERMAL_MONITOR.get()).build(null));

    public static final Supplier<BlockEntityType<RangeTriggerBlockEntity>> RANGE_TRIGGER = BLOCK_ENTITY_TYPES.register(
            "range_trigger", () -> BlockEntityType.Builder.of(RangeTriggerBlockEntity::new, ECBlocks.RANGE_TRIGGER.get()).build(null));

    public static final Supplier<BlockEntityType<HowlerAlarmBlockEntity>> HOWLER_ALARM = BLOCK_ENTITY_TYPES.register(
            "howler_alarm", () -> BlockEntityType.Builder.of(HowlerAlarmBlockEntity::new, ECBlocks.HOWLER_ALARM.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
