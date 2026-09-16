package com.zuxelus.energycontrol.init;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.blockentity.AdvancedInfoPanelBlockEntity;
import com.zuxelus.energycontrol.blockentity.EnergyCounterBlockEntity;
import com.zuxelus.energycontrol.blockentity.HowlerAlarmBlockEntity;
import com.zuxelus.energycontrol.blockentity.InfoPanelBlockEntity;
import com.zuxelus.energycontrol.blockentity.KitAssemblerBlockEntity;
import com.zuxelus.energycontrol.blockentity.RangeTriggerBlockEntity;
import com.zuxelus.energycontrol.blockentity.RemoteThermalMonitorBlockEntity;
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

    public static final Supplier<BlockEntityType<AdvancedInfoPanelBlockEntity>> ADVANCED_INFO_PANEL = BLOCK_ENTITY_TYPES.register(
            "advanced_info_panel", () -> BlockEntityType.Builder.of(AdvancedInfoPanelBlockEntity::new, ECBlocks.ADVANCED_INFO_PANEL.get()).build(null));

    public static final Supplier<BlockEntityType<ThermalMonitorBlockEntity>> THERMAL_MONITOR = BLOCK_ENTITY_TYPES.register(
            "thermal_monitor", () -> BlockEntityType.Builder.of(ThermalMonitorBlockEntity::new, ECBlocks.THERMAL_MONITOR.get()).build(null));

    public static final Supplier<BlockEntityType<RemoteThermalMonitorBlockEntity>> REMOTE_THERMAL_MONITOR = BLOCK_ENTITY_TYPES.register(
            "remote_thermal_monitor", () -> BlockEntityType.Builder.of(RemoteThermalMonitorBlockEntity::new, ECBlocks.REMOTE_THERMAL_MONITOR.get()).build(null));

    public static final Supplier<BlockEntityType<EnergyCounterBlockEntity>> ENERGY_COUNTER = BLOCK_ENTITY_TYPES.register(
            "energy_counter", () -> BlockEntityType.Builder.of(EnergyCounterBlockEntity::new, ECBlocks.ENERGY_COUNTER.get()).build(null));

    public static final Supplier<BlockEntityType<RangeTriggerBlockEntity>> RANGE_TRIGGER = BLOCK_ENTITY_TYPES.register(
            "range_trigger", () -> BlockEntityType.Builder.of(RangeTriggerBlockEntity::new, ECBlocks.RANGE_TRIGGER.get()).build(null));

    public static final Supplier<BlockEntityType<KitAssemblerBlockEntity>> KIT_ASSEMBLER = BLOCK_ENTITY_TYPES.register(
            "kit_assembler", () -> BlockEntityType.Builder.of(KitAssemblerBlockEntity::new, ECBlocks.KIT_ASSEMBLER.get()).build(null));

    public static final Supplier<BlockEntityType<HowlerAlarmBlockEntity>> HOWLER_ALARM = BLOCK_ENTITY_TYPES.register(
            "howler_alarm", () -> BlockEntityType.Builder.of(HowlerAlarmBlockEntity::new, ECBlocks.HOWLER_ALARM.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
