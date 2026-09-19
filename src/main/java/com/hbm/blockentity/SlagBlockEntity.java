package com.hbm.blockentity;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: die eingebettete Klasse BlockDynamicSlag.TileEntitySlag.
 *
 * Was in einer Schlackenpfuetze steckt: welches Material und wieviel davon. Mehr ist es nicht.
 *
 * SIE TICKT NICHT. Die Pfuetze fliesst ueber den geplanten Blockschritt (scheduleTick), nicht
 * ueber die Blockentitaet -- genau wie im Original, das canUpdate auf false setzt.
 */
public class SlagBlockEntity extends BlockEntity {

    /** Ein Block Material mal sechzehn -- so viel passt in eine Pfuetze. Wert aus dem Original. */
    public static final int MAX_AMOUNT = MaterialShapes.BLOCK.q(16);

    public NTMMaterial mat;
    public int amount;

    public SlagBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.SLAG.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.mat = Mats.matById.get(tag.getInt("mat"));
        this.amount = tag.getInt("amount");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if(this.mat != null) tag.putInt("mat", this.mat.id);
        tag.putInt("amount", this.amount);
    }

    /* Der Client braucht das Material: die Pfuetze wird nach ihm eingefaerbt. Die Menge steht
     * im Blockzustand, sie kaeme auch ohne diesen Weg an -- aber getrennt zu senden waere mehr
     * Aufwand als die paar Bytes wert sind. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
