package com.hbm.entity.item;

import com.hbm.blockentity.SupplyCrateBlockEntity;
import com.hbm.blocks.NtmBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.entity.item.EntityParachuteCrate.
 *
 * Die Kiste am Fallschirm. Sie sinkt langsam -- der Fall wird auf ein Fuenftel Block je Tick
 * gebremst, das ist der Fallschirm --, und wo sie aufsetzt, steht danach eine Nachschubkiste
 * mit dem Inhalt, den sie getragen hat.
 *
 * DIE HOEHENGRENZE bei 600 ist die des Originals: wird sie hoeher eingesetzt, faellt sie
 * sofort auf diese Hoehe zurueck. Das Original setzt dafuer posY direkt; hier geschieht es
 * ueber setPos, sonst wuerde die Begrenzungsbox nicht mitwandern.
 *
 * WER SIE ABWIRFT, fehlt noch: im Original ist das die C-130, die der Port nicht hat. Die
 * Kiste selbst ist vollstaendig und laesst sich von jeder kuenftigen Quelle einsetzen.
 */
public class ParachuteCrate extends Entity {

    /** Schneller als das sinkt sie nicht -- daran haengt der Fallschirm. */
    private static final double HOECHSTFALL = -0.2;

    /** Hoehengrenze des Originals. */
    private static final double DECKEL = 600;

    public final List<ItemStack> items = new ArrayList<>();

    public ParachuteCrate(EntityType<? extends ParachuteCrate> type, Level level) {
        super(type, level);

        /* Gegenstueck zu ignoreFrustumCheck des Originals: sie wird auch dann gezeichnet,
         * wenn ihre Begrenzungsbox gerade aus dem Sichtkegel faellt. */
        this.noCulling = true;
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { }

    @Override
    public void tick() {

        this.setPos(this.getX() + this.getDeltaMovement().x,
                Math.min(DECKEL, this.getY() + this.getDeltaMovement().y),
                this.getZ() + this.getDeltaMovement().z);

        if(this.getDeltaMovement().y > HOECHSTFALL) {
            this.setDeltaMovement(this.getDeltaMovement().x,
                    this.getDeltaMovement().y - 0.02,
                    this.getDeltaMovement().z);
        }

        if(this.level.isClientSide) return;
        if(this.level.getBlockState(BlockPos.containing(this.position())).isAir()) return;

        this.setzeKiste();
    }

    private void setzeKiste() {

        this.discard();

        BlockPos stelle = BlockPos.containing(this.getX(), this.getY() + 1, this.getZ());
        this.level.setBlockAndUpdate(stelle, NtmBlocks.CRATE_SUPPLY.get().defaultBlockState());

        if(this.level.getBlockEntity(stelle) instanceof SupplyCrateBlockEntity kiste) {
            kiste.items.addAll(this.items);
            kiste.setChanged();
        }
    }

    @Override public boolean shouldRenderAtSqrDistance(double distance) { return true; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.items.clear();
        HolderLookup.Provider registries = this.level.registryAccess();
        ListTag liste = tag.getList("items", Tag.TAG_COMPOUND);
        for(int i = 0; i < liste.size(); i++) {
            ItemStack.parse(registries, liste.getCompound(i)).ifPresent(this.items::add);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        HolderLookup.Provider registries = this.level.registryAccess();
        ListTag liste = new ListTag();
        for(ItemStack stueck : this.items) {
            if(!stueck.isEmpty()) liste.add(stueck.save(registries, new CompoundTag()));
        }
        tag.put("items", liste);
    }
}
