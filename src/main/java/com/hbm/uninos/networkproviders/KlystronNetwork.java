package com.hbm.uninos.networkproviders;

import com.hbm.uninos.GenNode;
import com.hbm.uninos.NodeNet;

/**
 * Portiert aus 1.7.10: com.hbm.uninos.networkproviders.KlystronNetwork.
 *
 * Das Netz zwischen Klystron und Fusionstorus. Es rechnet selbst nichts -- das Klystron schiebt
 * seine Leistung jeden Tick von sich aus in den Torus. Das Netz sagt nur, wer mit wem verbunden
 * ist.
 */
public class KlystronNetwork extends NodeNet<Object, Object, GenNode> {

    @Override
    public void update() { }
}
