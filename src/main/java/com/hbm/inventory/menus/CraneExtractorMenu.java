package com.hbm.inventory.menus;

import com.hbm.blockentity.network.CraneExtractorBlockEntity;
import com.hbm.inventory.NtmMenuTypes;
import com.hbm.inventory.SlotPattern;
import com.hbm.util.CompatExternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * Portiert aus 1.7.10: com.hbm.inventory.container.ContainerCraneExtractor.
 *
 * Neun Musterfaecher, dahinter neun Faecher als Zwischenlager. Alle Koordinaten sind die des
 * Originals; die beiden Aufwertungsplaetze bei 152,23 und 152,47 bleiben leer, weil es die
 * Aufwertungen im Port nicht gibt.
 */
public class CraneExtractorMenu extends FilterMenuBase<CraneExtractorBlockEntity> {

    public CraneExtractorMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, (CraneExtractorBlockEntity) CompatExternal.getCoreFromPos(inventory.player.level(), extraData.readBlockPos()));
    }

    public CraneExtractorMenu(int id, Inventory inventory, CraneExtractorBlockEntity be) {
        super(NtmMenuTypes.CRANE_EXTRACTOR.get(), id, be);

        /* Die Musterfaecher zuerst -- FilterMenuBase zaehlt darauf. */
        for(int row = 0; row < 3; row++) {
            for(int col = 0; col < 3; col++) {
                this.addSlot(new SlotPattern(be, col + row * 3, 71 + col * 18, 17 + row * 18));
            }
        }

        this.addSlots(be, CraneExtractorBlockEntity.PATTERNS, 8, 17, 3, 3);

        this.playerInv(inventory, 26, 103, 161);
    }
}
