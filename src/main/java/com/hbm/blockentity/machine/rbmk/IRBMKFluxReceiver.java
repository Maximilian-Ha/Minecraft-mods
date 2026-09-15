package com.hbm.blockentity.machine.rbmk;

import com.hbm.handler.neutron.NeutronStream;

/** Alles, was Neutronenfluss aufnehmen kann. */
public interface IRBMKFluxReceiver {

    enum NType {
        FAST("trait.rbmk.neutron.fast"),
        SLOW("trait.rbmk.neutron.slow"),
        /** Nur fuer die Beschreibung von Brennstoffen, nicht fuer die Flussrechnung. */
        ANY("trait.rbmk.neutron.any");

        public final String unlocalized;

        NType(String loc) {
            this.unlocalized = loc;
        }
    }

    void receiveFlux(NeutronStream stream);
}
