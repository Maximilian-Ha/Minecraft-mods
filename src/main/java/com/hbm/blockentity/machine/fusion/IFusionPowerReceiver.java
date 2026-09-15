package com.hbm.blockentity.machine.fusion;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.fusion.IFusionPowerReceiver.
 *
 * Wer am Plasmanetz des Fusionstorus haengt und etwas davon abnimmt.
 */
public interface IFusionPowerReceiver {

    /**
     * @return wahr, wenn dieses Geraet die Plasmawaerme abnimmt und sie sich deshalb mit allen
     *         anderen Abnehmern teilt; falsch, wenn es nur den Neutronenfluss braucht -- der wird
     *         nicht geteilt.
     */
    boolean receivesFusionPower();

    /**
     * @param fusionPower  die Waerme je Anschluss, schon auf die Zahl der Abnehmer umgerechnet
     * @param neutronPower der Neutronenfluss, ein fester Wert aus dem Rezept
     */
    void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b);
}
