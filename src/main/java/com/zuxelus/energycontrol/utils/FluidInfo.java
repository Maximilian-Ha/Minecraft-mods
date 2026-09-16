package com.zuxelus.energycontrol.utils;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.utils.FluidInfo.
 *
 * Ein Tank, so weit die Tafel ihn braucht: Name, Fuellstand, Fassungsvermoegen. Die
 * Anbindung an eine Mod baut daraus ihre eigenen Tanks um; der Kern kennt nur diese drei
 * Angaben und die Fluid-Schnittstelle von NeoForge.
 */
public record FluidInfo(Component name, int amount, int capacity) {

    public static FluidInfo of(FluidStack stack, int capacity) {
        if(stack == null || stack.isEmpty()) return empty(capacity);
        return new FluidInfo(stack.getHoverName(), stack.getAmount(), capacity);
    }

    public static FluidInfo of(IFluidHandler handler, int tank) {
        return of(handler.getFluidInTank(tank), handler.getTankCapacity(tank));
    }

    public static FluidInfo empty(int capacity) {
        return new FluidInfo(Component.translatable("msg.ec.InfoPanelEmpty"), 0, capacity);
    }

    /** Die Zeile, die auf dem Schirm landet: "Wasser: 4 000 / 16 000 mB". */
    public String format() {
        return name.getString() + ": " + PanelFormat.number(amount) + " / " + PanelFormat.number(capacity) + " mB";
    }
}
