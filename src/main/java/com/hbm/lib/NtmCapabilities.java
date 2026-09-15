package com.hbm.lib;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.fluids.HbmFluidHandler;
import com.hbm.inventory.fluid.tank.FluidTank;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;

/**
 * Meldet die Capabilities des Mods an.
 *
 * Bis hierher war keine einzige angemeldet. Trichter und Kolbenblöcke kommen an die Maschinen
 * heran, weil sie direkt gegen {@link Container} arbeiten -- Rohre und Automatisierung anderer
 * Mods dagegen fragen ausschliesslich ueber Capabilities an und fanden deshalb gar nichts.
 *
 * Angemeldet wird pauschal fuer jeden BlockEntityType des Mods; ob wirklich ein Inventar
 * dahintersteht, entscheidet erst der Anbieter zur Laufzeit. Das ist Absicht: so ist jede
 * kuenftige Maschine ohne weiteres Zutun angebunden, und es gibt keine Liste, die man beim
 * naechsten Port zu ergaenzen vergessen kann.
 *
 * Die Huellbloecke eines Mehrfachblocks sind mit abgedeckt, weil ProxyComboBlockEntity selbst
 * WorldlyContainer implementiert und an den Kern weiterreicht.
 *
 * Seit Runde 44 gilt dasselbe fuer Capabilities.FluidHandler.BLOCK. Die Grundlage dafuer --
 * eine Zuordnung der rund 150 Fluide des Mods auf die Fluid-Registry von Minecraft -- liefert
 * NtmFluidBridge; der Adapter darueber ist HbmFluidHandler.
 */
public class NtmCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {
        for(var holder : NtmBlockEntityTypes.BLOCK_ENTITY_TYPES.getEntries()) {
            registerItemHandler(event, holder.get());
            registerFluidHandler(event, holder.get());
        }

        // Forge-Energie gibt es nur an den beiden Wandlerbloecken. Das Stromnetz des Mods
        // selbst bleibt bewusst getrennt -- so haelt es auch das Original, dort sind diese
        // beiden Maschinen der einzige Uebergang.
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, NtmBlockEntityTypes.CONVERTER_HE_RF.get(), (be, side) -> be.storage);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, NtmBlockEntityTypes.CONVERTER_RF_HE.get(), (be, side) -> be.storage);
    }

    private static <T extends BlockEntity> void registerFluidHandler(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (be, side) -> wrapTanks(be));
    }

    /**
     * Was die Maschine als Eingang fuehrt, laesst sich befuellen; was sie als Ausgang fuehrt,
     * laesst sich leeren. Maschinen, die nur eines von beidem koennen, bekommen fuer die andere
     * Richtung ein leeres Feld -- der Adapter kommt damit zurecht.
     *
     * Die Richtung wird nicht ausgewertet: anders als beim Inventar gibt es im Fluidsystem des
     * Mods keine seitenbezogene Freigabe, sondern feste Anschlussstellen, die die Maschine
     * selbst abfragt. Ein fremdes Rohr an einer beliebigen Seite verhaelt sich damit wie ein
     * eigenes an einer Anschlussstelle.
     */
    private static @Nullable IFluidHandler wrapTanks(BlockEntity be) {

        FluidTank[] fillable = be instanceof IFluidStandardReceiverMK2 receiver ? receiver.getReceivingTanks() : null;
        FluidTank[] drainable = be instanceof IFluidStandardSenderMK2 sender ? sender.getSendingTanks() : null;

        if(fillable == null && drainable == null) return null;

        return new HbmFluidHandler(fillable, drainable);
    }

    private static <T extends BlockEntity> void registerItemHandler(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, side) -> wrapInventory(be, side));
    }

    /**
     * Seitenbezogene Anfragen bekommen den Ausschnitt, den die Maschine fuer diese Seite
     * freigibt -- also genau das, woran sich auch ein Trichter haelt. Seitenlose Anfragen
     * (side == null) koennen darauf nicht zurueckgreifen, weil getSlotsForFace eine Richtung
     * braucht; sie bekommen deshalb das ungefilterte Inventar.
     */
    private static @Nullable IItemHandler wrapInventory(BlockEntity be, @Nullable Direction side) {
        if(!(be instanceof Container container)) return null;
        if(side != null && container instanceof WorldlyContainer worldly) return new SidedInvWrapper(worldly, side);
        return new InvWrapper(container);
    }
}
