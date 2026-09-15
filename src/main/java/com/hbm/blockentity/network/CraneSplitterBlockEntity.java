package com.hbm.blockentity.network;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.TickingBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityCraneSplitter.
 *
 * Die Weiche teilt einen ankommenden Stapel auf zwei nebeneinanderliegende Baender auf, und zwar
 * IM EINGESTELLTEN VERHAELTNIS. Steht es auf drei zu eins, gehen drei Stueck nach links und
 * eines nach rechts, dann wieder drei nach links, und so fort.
 *
 * SIE ZAEHLT UEBER STAPEL HINWEG WEITER. Die beiden Felder "position" und "remaining" merken
 * sich, wo die Zaehlung stehengeblieben ist; der naechste Stapel setzt sie fort. Ohne dieses
 * Gedaechtnis begaenne jeder Stapel wieder links, und bei einzeln ankommenden Gegenstaenden
 * bekaeme die rechte Seite nie etwas ab -- das Verhaeltnis waere wirkungslos.
 *
 * Die beiden Verhaeltnisse stellt der Schraubenzieher ein, jedes an seinem eigenen Block:
 * anklicken erhoeht, mit Schleichtaste verringert, zwischen eins und sechzehn.
 */
public class CraneSplitterBlockEntity extends TickingBaseBlockEntity {

    /** Falsch: die linke Seite ist an der Reihe. Wahr: die rechte. */
    private boolean position;

    /** Wie viele Stueck die Seite, die gerade an der Reihe ist, noch bekommt. */
    private byte remaining;

    public byte leftRatio = 1;
    public byte rightRatio = 1;

    public CraneSplitterBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CRANE_SPLITTER.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.networkPackNT(15);
    }

    /**
     * Teilt den Stapel auf. Zurueck kommen zwei Stapel -- der erste fuer links, der zweite fuer
     * rechts; einer von beiden kann leer sein.
     */
    public ItemStack[] splitStack(ItemStack stack) {

        int left = 0;
        int right = 0;
        int count = stack.getCount();

        if(this.remaining <= 0) this.remaining = this.position ? this.rightRatio : this.leftRatio;

        while(count > 0) {

            int taken = Math.min(this.remaining, count);

            this.remaining -= taken;
            count -= taken;

            if(this.position) right += taken; else left += taken;

            if(this.remaining <= 0) {
                this.position = !this.position;
                this.remaining = this.position ? this.rightRatio : this.leftRatio;
            }
        }

        this.setChanged();

        return new ItemStack[] { stack.copyWithCount(left), stack.copyWithCount(right) };
    }

    /** Erhoeht oder verringert eines der beiden Verhaeltnisse, zwischen eins und sechzehn. */
    public void adjustRatio(boolean isLeft, int by) {

        if(isLeft) this.leftRatio = (byte) Mth.clamp(this.leftRatio + by, 1, 16);
        else this.rightRatio = (byte) Mth.clamp(this.rightRatio + by, 1, 16);

        this.setChanged();
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        buf.writeByte(this.leftRatio);
        buf.writeByte(this.rightRatio);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        this.leftRatio = buf.readByte();
        this.rightRatio = buf.readByte();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        super.loadAdditional(tag, registries);

        this.position = tag.getBoolean("pos");
        this.remaining = tag.getByte("count");

        /* Eine Weiche aus einer Welt von vor dieser Runde haette sonst das Verhaeltnis null zu
         * null und wuerde gar nichts mehr weitergeben. */
        this.leftRatio = (byte) Math.max(tag.getByte("left"), 1);
        this.rightRatio = (byte) Math.max(tag.getByte("right"), 1);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {

        super.saveAdditional(tag, registries);

        tag.putBoolean("pos", this.position);
        tag.putByte("count", this.remaining);
        tag.putByte("left", this.leftRatio);
        tag.putByte("right", this.rightRatio);
    }
}
