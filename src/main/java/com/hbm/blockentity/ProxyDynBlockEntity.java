package com.hbm.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.TileEntityProxyDyn.
 *
 * Ein Anschlussblock, dessen Kern sich nach seiner EIGENEN STELLE richtet. Der gewoehnliche
 * Anschluss reicht alles an den Kern der Maschine weiter, und der Kern hat genau einen Satz
 * Tanks; wo eine Maschine getrennte Anschluesse braucht -- die Chemiefabrik etwa Kuehlwasser an
 * der einen Seite und Rezeptfluessigkeiten an der anderen --, reicht das nicht.
 *
 * DIE MASCHINE ENTSCHEIDET, NICHT DER ANSCHLUSS. Sie bekommt die Stelle des Anschlusses gezeigt
 * und darf dafuer einen Stellvertreter nennen; nennt sie keinen, bleibt es beim Kern.
 */
public class ProxyDynBlockEntity extends ProxyComboBlockEntity {

    public ProxyDynBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.PROXY_DYN.get(), pos, state);
    }

    @Override
    protected Object getCoreObject() {

        Object core = super.getCoreObject();

        if(core instanceof IProxyDelegateProvider provider) {
            Object delegate = provider.getDelegateForPosition(this.worldPosition);
            if(delegate != null) return delegate;
        }

        return core;
    }

    /** Eine Maschine, die je nach Anschlussstelle einen anderen Stellvertreter nennen kann. */
    public interface IProxyDelegateProvider {

        /** Der Stellvertreter fuer diesen Anschluss, oder null fuer "nimm den Kern". */
        Object getDelegateForPosition(BlockPos pos);
    }
}
