package com.hbm.blockentity.machine.fusion;

import com.hbm.uninos.GenNode;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Der immer gleiche Handgriff, mit dem sich ein Geraet der Fusion in eines der beiden Netze
 * einhaengt: Knoten suchen, notfalls anlegen, und sich selbst als Abnehmer oder Lieferant
 * eintragen.
 *
 * ABWEICHUNG: im Original steht dieser Block in JEDER der sieben Klassen noch einmal; der Autor
 * merkt im Kommentar selbst an, dass ihm dafuer eine Basisklasse gefehlt hat. Hier steht er
 * einmal. Die Zahlen -- wo der Knoten liegt und wohin er verbindet -- bleiben Sache des Geraets,
 * sie sind bei jedem anders.
 */
public class FusionNodes {

    private FusionNodes() { }

    /**
     * Stellt sicher, dass an nodePos ein Knoten des angegebenen Netzes liegt.
     *
     * @param node       der bisherige Knoten, oder null
     * @param nodePos    wo der Knoten liegen soll
     * @param connection wohin er weiterverbindet
     */
    public static GenNode<?> ensure(GenNode<?> node, Level level, BlockPos nodePos, DirPos connection, INetworkProvider<?> provider) {

        if(node != null && !node.expired) return node;

        GenNode<?> found = UniNodespace.getNode(level, nodePos, provider);
        if(found != null) return found;

        GenNode<?> fresh = new GenNode<>(provider, nodePos).setConnections(connection);
        UniNodespace.createNode(level, fresh);

        return fresh;
    }

    /** Traegt das Geraet als Abnehmer in das Netz des Knotens ein, sobald es eines gibt. */
    public static void subscribe(GenNode<?> node, Object self) {
        if(node != null && node.hasValidNet()) node.net.addReceiver(self);
    }

    /** Traegt das Geraet als Lieferant ein. */
    public static void provide(GenNode<?> node, Object self) {
        if(node != null && node.net != null) node.net.addProvider(self);
    }

    /** Raeumt einen Knoten ab, wenn das Geraet verschwindet. */
    public static void destroy(Level level, GenNode<?> node) {
        if(level != null && !level.isClientSide && node != null) UniNodespace.destroyNode(level, node);
    }
}
