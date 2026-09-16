package com.zuxelus.energycontrol.init;

import com.zuxelus.energycontrol.EnergyControl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Die drei Alarmtoene des Heulers, unveraendert aus dem Original uebernommen. */
public class ECSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, EnergyControl.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_DEFAULT = reg("alarm_default");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_SCI_FI = reg("alarm_sci_fi");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_SIREN = reg("alarm_siren");

    public static final int ALARM_COUNT = 3;

    private static final String[] ALARM_NAMES = {
            "msg.ec.AlarmDefault", "msg.ec.AlarmSciFi", "msg.ec.AlarmSiren"
    };

    public static SoundEvent alarm(int index) {
        return switch(Math.floorMod(index, ALARM_COUNT)) {
            case 1 -> ALARM_SCI_FI.get();
            case 2 -> ALARM_SIREN.get();
            default -> ALARM_DEFAULT.get();
        };
    }

    public static String alarmName(int index) {
        return ALARM_NAMES[Math.floorMod(index, ALARM_COUNT)];
    }

    private static DeferredHolder<SoundEvent, SoundEvent> reg(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(EnergyControl.loc(name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
