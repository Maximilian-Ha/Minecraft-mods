package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.ProxyComboBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: der Aufsatz benutzte dort schlicht
 * new TileEntityProxyCombo().inventory().fluid().
 *
 * Im Port braucht der Aufsatz einen eigenen BlockEntityType, weil an ihm der
 * OBJ-Renderer haengt -- der gemeinsame PROXY_COMBO-Typ steckt in den Huellbloecken
 * saemtlicher Multibloecke und duerfte daher keinen eigenen Renderer bekommen.
 * Verhalten ist identisch: Gegenstaende und Fluide werden an den Ofen darunter
 * durchgereicht.
 */
public class DiFurnaceExtensionBlockEntity extends ProxyComboBlockEntity {

    public DiFurnaceExtensionBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_DIFURNACE_EXTENSION.get(), pos, state);
        this.inventory();
        this.fluid();
    }
}
