package com.hbm.blockentity;

import com.hbm.util.Tuple.Quartet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: die eingebettete Klasse BlockLoot.TileEntityLoot.
 *
 * Was auf einem Beutesockel liegt: Gegenstandsstapel, jeder mit seinem eigenen Versatz
 * innerhalb des Blocks. Das ist reine Ausstattung -- die Weltgenerierung legt damit Gerumpel
 * auf den Boden, das erst beim Zerschlagen des Sockels zu aufsammelbaren Gegenstaenden wird.
 *
 * SIE TICKT NICHT.
 *
 * SIE MUSS ZUM CLIENT: gezeichnet wird sie dort, und ohne die Stapel weiss der Zeichner nicht,
 * was er hinlegen soll. Deshalb der Beschreibungspaket-Weg, den das Original ebenso geht.
 *
 * IHR ZEICHENKASTEN reicht ueber den Block hinaus -- das Original gibt ihm einen Block nach
 * jeder Seite und drei nach oben, weil ein liegendes Gewehr breiter ist als sein Sockel.
 */
public class LootDecoBlockEntity extends BlockEntity {

    /** Stapel und Versatz in x, y, z. Die Reihenfolge ist die des Originals. */
    public final List<Quartet<ItemStack, Double, Double, Double>> items = new ArrayList<>();

    private AABB zeichenkasten;

    public LootDecoBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.LOOT_DECO.get(), pos, state);
    }

    public LootDecoBlockEntity addItem(ItemStack stack, double x, double y, double z) {
        this.items.add(new Quartet<>(stack, x, y, z));
        return this;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.items.clear();
        int anzahl = tag.getInt("count");

        /* Stapel, die nicht mehr aufloesen -- ein entfernter Gegenstand, ein anderer Mod --,
         * fallen weg; ihr Versatz kommt dann gar nicht erst zum Tragen. Das Original
         * ueberspringt sie ebenso. */
        for(int i = 0; i < anzahl; i++) {
            final int index = i;
            ItemStack.parse(registries, tag.getCompound("item" + index)).ifPresent(stapel ->
                    this.items.add(new Quartet<>(stapel,
                            tag.getDouble("x" + index),
                            tag.getDouble("y" + index),
                            tag.getDouble("z" + index))));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("count", this.items.size());

        for(int i = 0; i < this.items.size(); i++) {
            Quartet<ItemStack, Double, Double, Double> eintrag = this.items.get(i);
            if(eintrag == null || eintrag.getW() == null || eintrag.getW().isEmpty()) continue;
            tag.put("item" + i, eintrag.getW().save(registries, new CompoundTag()));
            tag.putDouble("x" + i, eintrag.getX());
            tag.putDouble("y" + i, eintrag.getY());
            tag.putDouble("z" + i, eintrag.getZ());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /* Ohne @Override: getRenderBoundingBox stammt aus der NeoForge-Erweiterung von
     * BlockEntity, nicht aus der Minecraft-Klasse, und gilt dem Uebersetzer nicht als
     * ueberschrieben. */
    public AABB getRenderBoundingBox() {
        if(this.zeichenkasten == null) {
            BlockPos pos = this.getBlockPos();
            this.zeichenkasten = new AABB(
                    pos.getX() - 1, pos.getY(), pos.getZ() - 1,
                    pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
        }
        return this.zeichenkasten;
    }
}
