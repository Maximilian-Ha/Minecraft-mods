package com.hbm.blockentity.network;

import com.hbm.blockentity.IControlReceiverFilter;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.network.RadioTorchBaseBlock;
import com.hbm.inventory.menus.RadioTorchCounterMenu;
import com.hbm.module.ModulePatternMatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.network.TileEntityRadioTorchCounter.
 *
 * Eine Funkfackel, die zaehlt. Sie sieht in das Inventar hinter sich, zaehlt darin alles, was
 * auf eines ihrer drei Muster passt, und funkt jede der drei Zahlen auf einen eigenen Kanal.
 *
 * Zwei Betriebsarten: standardmaessig sendet sie nur, wenn sich eine Zahl geaendert hat --
 * auf Knopfdruck ("polling") stattdessen jeden Tick. Das kostet Netzverkehr, hilft aber, wenn
 * auf der Gegenseite jemand zuhoert, der gerade erst eingeschaltet hat.
 */
public class RadioTorchCounterBlockEntity extends MachineBaseBlockEntity implements IControlReceiverFilter {

    public static final int KANAELE = 3;

    public String[] channel = new String[KANAELE];
    public int[] lastCount = new int[KANAELE];

    /** Frisch eingestellter Kanal oder frisches Muster: einmal senden, auch ohne Aenderung. */
    private final boolean[] forceUpdate = new boolean[KANAELE];

    public boolean polling = false;

    public final ModulePatternMatcher matcher = new ModulePatternMatcher(KANAELE);

    public RadioTorchCounterBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RADIO_TORCH_COUNTER.get(), pos, state, KANAELE);
        for(int i = 0; i < KANAELE; i++) this.channel[i] = "";
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rttyCounter");
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if(slot >= 0 && slot < KANAELE) this.forceUpdate[slot] = true;
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        // Die Fackel sieht nach hinten -- dorthin, wo sie an der Wand haengt.
        Direction hinten = this.getBlockState().getValue(RadioTorchBaseBlock.FACING).getOpposite();

        if(this.level.getBlockEntity(this.worldPosition.relative(hinten)) instanceof Container lager) {

            for(int i = 0; i < KANAELE; i++) {

                if(this.channel[i].isEmpty()) continue;
                ItemStack muster = this.slots.get(i);
                if(muster.isEmpty()) continue;

                int anzahl = 0;
                for(int j = 0; j < lager.getContainerSize(); j++) {
                    ItemStack darin = lager.getItem(j);
                    if(!darin.isEmpty() && this.matcher.isValidForFilter(muster, i, darin)) {
                        anzahl += darin.getCount();
                    }
                }

                if(this.polling || this.forceUpdate[i] || this.lastCount[i] != anzahl) {
                    RTTYSystem.broadcast(this.level, this.channel[i], anzahl);
                    this.forceUpdate[i] = false;
                }

                this.lastCount[i] = anzahl;
            }
        }

        this.networkPackNT(15);
    }

    @Override public int[] getFilterSlots() { return new int[] { 0, KANAELE }; }

    @Override
    public void nextMode(int i) {
        this.matcher.nextMode(this.level, this.slots.get(i), i);
        this.forceUpdate[i] = true;
        this.setChanged();
    }

    @Override
    public void initPattern(int i) {
        this.matcher.initPatternStandard(this.level, this.slots.get(i), i);
        this.forceUpdate[i] = true;
        this.setChanged();
    }

    @Override
    public boolean hasPermission(Player player) {
        return this.stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag data) {

        if(data.contains("polling")) {
            this.polling = !this.polling;

        } else {
            for(int i = 0; i < KANAELE; i++) {
                String neu = data.getString("c" + i);
                if(!neu.equals(this.channel[i])) {
                    this.channel[i] = neu;
                    this.forceUpdate[i] = true;
                }
            }
        }

        this.setChanged();
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.polling);
        for(int i = 0; i < KANAELE; i++) buf.writeInt(this.lastCount[i]);
        this.matcher.serialize(buf);
        for(int i = 0; i < KANAELE; i++) buf.writeUtf(this.channel[i]);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.polling = buf.readBoolean();
        for(int i = 0; i < KANAELE; i++) this.lastCount[i] = buf.readInt();
        this.matcher.deserialize(buf);
        for(int i = 0; i < KANAELE; i++) this.channel[i] = buf.readUtf();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.polling = tag.getBoolean("p");
        for(int i = 0; i < KANAELE; i++) {
            this.channel[i] = tag.getString("c" + i);
            this.lastCount[i] = tag.getInt("l" + i);
        }
        this.matcher.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("p", this.polling);
        for(int i = 0; i < KANAELE; i++) {
            tag.putString("c" + i, this.channel[i] == null ? "" : this.channel[i]);
            tag.putInt("l" + i, this.lastCount[i]);
        }
        this.matcher.save(tag);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RadioTorchCounterMenu(id, inventory, this);
    }
}
