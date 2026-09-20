package com.hbm.blockentity.machine.storage;

import com.hbm.blockentity.NtmBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.storage.TileEntitySafe.
 *
 * Der TRESOR ist im Original dieselbe Klasse wie die Vorratskisten -- BlockStorageCrate --,
 * nur mit anderer Groesse und anderem Bild: fuenfzehn Faecher in drei Reihen zu fuenf,
 * eingerueckt um zwei Fachbreiten, damit sie in der Mitte des Fensters stehen (ContainerSafe).
 *
 * ER STAND SEIT RUNDE 251 ALS TRUHE IM METEORITENVERLIES. Dort setzt ein Beutestab ihn in
 * das Buecherstueck; weil es ihn im Port nicht gab, stand an seiner Stelle eine
 * Vanilla-Truhe. Jetzt steht der Tresor selbst da.
 *
 * DAS SCHLOSS kommt mit: LockableBaseBlockEntity, von der jede Kiste des Ports abstammt,
 * bringt es mit. Im Original ist der Tresor abschliessbar wie jede Kiste auch.
 */
public class SafeBlockEntity extends CrateBaseBlockEntity {

    public SafeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.SAFE.get(), pos, state, 15, "gui_safe", 5, 3, 44, 18, 8, 86, 176, 168, 8, 4210752, 4210752);
    }

    @Override public Component getName() { return Component.translatable("container.safe"); }
}
