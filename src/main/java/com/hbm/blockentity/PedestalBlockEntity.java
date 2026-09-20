package com.hbm.blockentity;

import com.hbm.blocks.generic.PedestalBlock;
import com.hbm.entity.mob.CreeperDefuser;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;
import com.hbm.items.NtmItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: die eingebettete Klasse BlockPedestal.TileEntityPedestal.
 *
 * Der Sockel haelt genau einen Stapel. Getickt wird nur des Talismans wegen: liegt ein
 * Schutz- oder Meteoritentalisman darauf, meldet er sich alle zwanzig Takte beim
 * Eintragsregister des Blocks, und das Meteoritensystem fragt dort nach.
 *
 * DER GOLDENE SEITENSCHNEIDER kam in Runde 242 nach. Bis dahin stand hier, dass der Port
 * defuser_gold nicht kennt und eine Abfrage darauf eine Zeile waere, die nie zutrifft.
 * Jetzt gibt es ihn, und der Sockel entschaerft mit ihm wie im Original alle sechzig Takte
 * jeden Creeper im Umkreis von fuenfundzwanzig Bloecken -- ohne Zuendschnur, ohne Ton: nur
 * ein Sockel, in dessen Naehe kein Creeper mehr hochgeht.
 */
public class PedestalBlockEntity extends BlockEntity {

    /** Der Umkreis des goldenen Seitenschneiders: expand(25, 25, 25) im Original. */
    public static final double REICHWEITE = 25D;

    public ItemStack item = ItemStack.EMPTY;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.PEDESTAL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PedestalBlockEntity sockel) {

        if(level.getGameTime() % 20 != 0 || sockel.item.isEmpty()) return;

        if(sockel.item.is(NtmItems.PROTECTION_CHARM.get())) {
            PedestalBlock.pushEntry(level, PedestalBlock.EntryType.CHARM_OF_PROTECTION, pos);
        }
        if(sockel.item.is(NtmItems.METEOR_CHARM.get())) {
            PedestalBlock.pushEntry(level, PedestalBlock.EntryType.METEORITE_CHARM, pos);
        }

        /* Alle sechzig Takte, nicht alle zwanzig: das Original hat hier eine zweite,
         * engere Bedingung. Die Umkreissuche geht ueber fuenfzig Bloecke Kantenlaenge. */
        if(level.getGameTime() % 60 == 0 && sockel.item.is(NtmItems.DEFUSER_GOLD.get())) {

            AABB umkreis = new AABB(pos).inflate(REICHWEITE);

            for(Creeper creeper : level.getEntitiesOfClass(Creeper.class, umkreis)) {
                /* Ohne Ausgabe: ein Sockel, der jede Minute Zuendschnuere auswirft, waere
                 * eine Zuendschnurfabrik. Das Original uebergibt hier ebenfalls false. */
                CreeperDefuser.entschaerfe(creeper, null, false);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.item = tag.contains("item")
                ? ItemStack.parseOptional(registries, tag.getCompound("item"))
                : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if(!this.item.isEmpty()) tag.put("item", this.item.save(registries, new CompoundTag()));
    }

    /* Der Client muss wissen, was auf dem Sockel liegt -- sonst zeichnet der Darsteller nichts. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
