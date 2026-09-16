package com.zuxelus.energycontrol.init;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.blocks.HowlerAlarmBlock;
import com.zuxelus.energycontrol.blocks.IndustrialAlarmBlock;
import com.zuxelus.energycontrol.blocks.InfoPanelBlock;
import com.zuxelus.energycontrol.blocks.RangeTriggerBlock;
import com.zuxelus.energycontrol.blocks.ThermalMonitorBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Alle Bloecke des Mods. */
public class ECBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EnergyControl.MODID);

    public static final DeferredBlock<InfoPanelBlock> INFO_PANEL =
            register("info_panel", () -> new InfoPanelBlock(machineProperties()));

    public static final DeferredBlock<ThermalMonitorBlock> THERMAL_MONITOR =
            register("thermal_monitor", () -> new ThermalMonitorBlock(machineProperties()));

    public static final DeferredBlock<RangeTriggerBlock> RANGE_TRIGGER =
            register("range_trigger", () -> new RangeTriggerBlock(machineProperties()));

    public static final DeferredBlock<HowlerAlarmBlock> HOWLER_ALARM =
            register("howler_alarm", () -> new HowlerAlarmBlock(machineProperties()));

    public static final DeferredBlock<IndustrialAlarmBlock> INDUSTRIAL_ALARM =
            register("industrial_alarm", () -> new IndustrialAlarmBlock(machineProperties()));

    /** Haerte und Klang wie im Original: Metallgehaeuse, mit der Spitzhacke abzubauen. */
    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .strength(2.0F, 6.0F)
                .sound(SoundType.METAL)
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops();
    }

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        DeferredBlock<T> registered = BLOCKS.register(name, block);
        ECItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
