package com.hbm.uninos.networkproviders;

import com.hbm.uninos.GenNode;
import com.hbm.uninos.NodeNet;

/**
 * Portiert aus 1.7.10: com.hbm.uninos.networkproviders.PlasmaNetwork.
 *
 * Das Netz zwischen Fusionstorus und allem, was seine Hitze abnimmt. Wie beim Klystronnetz
 * rechnet es selbst nichts: der Torus verteilt seine Leistung jeden Tick selbst.
 */
public class PlasmaNetwork extends NodeNet<Object, Object, GenNode> {

    @Override
    public void update() { }
}
