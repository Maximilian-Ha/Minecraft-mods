package com.zuxelus.energycontrol.blockentity;

import com.zuxelus.energycontrol.init.ECBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.tileentities.TileEntityAdvancedInfoPanel.
 *
 * Die fortgeschrittene Tafel. Sie kann alles, was die gewoehnliche kann -- Karten, grosse
 * Schirme, Farben, Beruehrung --, und ist dabei duenner: ihre Dicke steht im Blockzustand
 * und laesst sich mit dem Tafelwerkzeug einstellen.
 *
 * Deshalb steht hier fast nichts: die Dicke gehoert zum Block, nicht zur Block-Entitaet.
 */
public class AdvancedInfoPanelBlockEntity extends InfoPanelBlockEntity {

    public AdvancedInfoPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ECBlockEntityTypes.ADVANCED_INFO_PANEL.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.energycontrol.advanced_info_panel");
    }
}
