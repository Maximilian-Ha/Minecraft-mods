/**
 * Der Neutronen-Nodespace.
 *
 * Portiert aus 1.7.10: com.hbm.handler.neutron. Der ausfuehrliche Originalkommentar ist hier
 * auf das Wesentliche eingedampft.
 *
 * Aufbau, nach dem Vorbild des Stromnetz-Nodespace:
 *
 * 1. {@link com.hbm.handler.neutron.NeutronNodeWorld} haelt pro Welt die Knoten und die Stroeme.
 *    Knoten legt das System selbst an, sobald ein Strom durch eine unbekannte Stelle laeuft;
 *    weggeraeumt werden sie entweder von Hand oder ueber checkNode().
 *
 * 2. {@link com.hbm.handler.neutron.NeutronNode} ist ein Knoten: Typ, Position, die zugehoerige
 *    Block-Entitaet und ein freies Datenfeld, in dem zum Beispiel der Deckelzustand einer
 *    RBMK-Saeule zwischengespeichert wird.
 *
 * 3. {@link com.hbm.handler.neutron.NeutronStream} ist ein Strom: Ursprungsknoten, Flussmenge,
 *    Verhaeltnis von schnellem zu langsamem Fluss und eine Richtung. Ein Strom lebt genau einen
 *    Tick. Die Menge des schnellen Flusses ist Menge mal Verhaeltnis, die des langsamen Menge mal
 *    Gegenwert des Verhaeltnisses.
 *
 * Ein neues System braucht eine eigene Handler-Klasse mit je einer Ableitung von NeutronNode und
 * NeutronStream, siehe {@link com.hbm.handler.neutron.RBMKNeutronHandler}. Der gemeinsame Tick
 * liegt in {@link com.hbm.handler.neutron.NeutronHandler}.
 */
package com.hbm.handler.neutron;
