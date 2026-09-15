package com.hbm.uninos.networkproviders;

import com.hbm.blockentity.machine.FoundryChannelBlockEntity.FoundryNode;
import com.hbm.uninos.NodeNet;

/**
 * Portiert aus 1.7.10: com.hbm.uninos.networkproviders.FoundryNetwork.
 *
 * Das Netz der Giesskanaele. Es transportiert nichts von sich aus -- die Kanaele schieben ihren
 * Inhalt selbst zum Nachbarn --, es dient nur dazu, dass ein zusammenhaengender Strang nur ein
 * einziges Material fuehrt. Ohne diese Klammer koennten zwei Metalle von zwei Seiten in denselben
 * Strang laufen und sich vermischen.
 */
public class FoundryNetwork extends NodeNet<Object, Object, FoundryNode> {

    @Override
    public void update() { }
}
