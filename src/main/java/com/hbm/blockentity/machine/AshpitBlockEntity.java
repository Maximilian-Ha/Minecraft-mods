package com.hbm.blockentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.IConfigurableMachine;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.menus.AshpitMenu;
import com.hbm.items.NtmItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityAshpit.
 *
 * Sammelbehaelter unter Brennkasten, Backofen und Schornstein. Die Nachbarn zaehlen in
 * ihm einen Fuellstand je Aschesorte hoch; ueberschreitet einer die Schwelle, wandert ein
 * Aschepulver in eines der fuenf Faecher.
 *
 * Der Fuellstand ist bewusst keine Fluid- oder Energiegroesse, sondern schlicht die Summe
 * der Brenndauern (bzw. beim Schornstein der durchgesetzten Rauchmenge) -- deshalb die
 * grossen Schwellen von 2000 bzw. 8000.
 */
public class AshpitBlockEntity extends MachineBaseBlockEntity {

    /** Konfigurierbar im Original ueber IConfigurableMachine, hier fest -- todo config */
    public static int thresholdWood = 2_000;
    public static int thresholdCoal = 2_000;
    public static int thresholdMisc = 2_000;
    public static int thresholdFly = 2_000;
    public static int thresholdSoot = 8_000;

    public int ashLevelWood;
    public int ashLevelCoal;
    public int ashLevelMisc;
    public int ashLevelFly;
    public int ashLevelSoot;

    public int playersUsing;
    public float doorAngle;
    public float prevDoorAngle;
    public boolean isFull;

    public AshpitBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_ASHPIT.get(), pos, state, 5);
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.ashpit"); }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(this.level.isClientSide) {
            this.prevDoorAngle = this.doorAngle;
            float swingSpeed = (this.doorAngle / 10F) + 3F;
            if(this.playersUsing > 0) {
                this.doorAngle = Math.min(this.doorAngle + swingSpeed, 135F);
            } else {
                this.doorAngle = Math.max(this.doorAngle - swingSpeed, 0F);
            }
            return;
        }

        if(this.processAsh(this.ashLevelWood, NtmItems.POWDER_ASH_WOOD.get().getDefaultInstance(), thresholdWood)) this.ashLevelWood -= thresholdWood;
        if(this.processAsh(this.ashLevelCoal, NtmItems.POWDER_ASH_COAL.get().getDefaultInstance(), thresholdCoal)) this.ashLevelCoal -= thresholdCoal;
        if(this.processAsh(this.ashLevelMisc, NtmItems.POWDER_ASH_MISC.get().getDefaultInstance(), thresholdMisc)) this.ashLevelMisc -= thresholdMisc;
        if(this.processAsh(this.ashLevelFly, NtmItems.POWDER_ASH_FLY.get().getDefaultInstance(), thresholdFly)) this.ashLevelFly -= thresholdFly;
        if(this.processAsh(this.ashLevelSoot, NtmItems.POWDER_ASH_SOOT.get().getDefaultInstance(), thresholdSoot)) this.ashLevelSoot -= thresholdSoot;

        this.isFull = false;
        for(int i = 0; i < 5; i++) {
            if(!this.slots.get(i).isEmpty()) this.isFull = true;
        }

        this.networkPackNT(50);
    }

    /**
     * Original: TileEntityAshpit.processAsh. Dort zieht die Methode im leeren Fach
     * zusaetzlich noch einmal ashLevelWood ab -- ein Fehler, der jede andere Aschesorte
     * beim ersten Stapel den Holzfuellstand plaettet und ihn ins Minus treibt. Hier
     * zieht nur der Aufrufer ab, und zwar von seinem eigenen Fuellstand.
     */
    protected boolean processAsh(int level, ItemStack ash, int threshold) {
        if(level < threshold) return false;

        for(int i = 0; i < 5; i++) {
            ItemStack slot = this.slots.get(i);

            if(slot.isEmpty()) {
                this.slots.set(i, ash.copy());
                this.setChanged();
                return true;
            }

            if(ItemStack.isSameItemSameComponents(slot, ash) && slot.getCount() < slot.getMaxStackSize()) {
                slot.grow(1);
                this.setChanged();
                return true;
            }
        }

        return false;
    }

    @Override public int[] getSlotsForFace(Direction direction) { return new int[] { 0, 1, 2, 3, 4 }; }
    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) { return true; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AshpitMenu(id, inventory, this);
    }

    @Override public void startOpen(Player player) { if(this.level != null && !this.level.isClientSide) this.playersUsing++; }
    @Override public void stopOpen(Player player) { if(this.level != null && !this.level.isClientSide) this.playersUsing--; }

    /** Der Block ist zwei mal zwei Felder gross, die Standardbox eines Renderers nur eines. */
    public AABB getRenderBoundingBox() {
        return new AABB(
                this.worldPosition.getX() - 1, this.worldPosition.getY(), this.worldPosition.getZ() - 1,
                this.worldPosition.getX() + 2, this.worldPosition.getY() + 1, this.worldPosition.getZ() + 2);
    }

    private void writeAshLevels(CompoundTag tag) {
        tag.putInt("ashLevelWood", this.ashLevelWood);
        tag.putInt("ashLevelCoal", this.ashLevelCoal);
        tag.putInt("ashLevelMisc", this.ashLevelMisc);
        tag.putInt("ashLevelFly", this.ashLevelFly);
        tag.putInt("ashLevelSoot", this.ashLevelSoot);
    }

    private void readAshLevels(CompoundTag tag) {
        this.ashLevelWood = tag.getInt("ashLevelWood");
        this.ashLevelCoal = tag.getInt("ashLevelCoal");
        this.ashLevelMisc = tag.getInt("ashLevelMisc");
        this.ashLevelFly = tag.getInt("ashLevelFly");
        this.ashLevelSoot = tag.getInt("ashLevelSoot");
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.readAshLevels(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.writeAshLevels(tag);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.playersUsing);
        buf.writeBoolean(this.isFull);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.playersUsing = buf.readInt();
        this.isFull = buf.readBoolean();
    }

    /** Konfiguration, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static void readConfig(JsonObject obj) {
        thresholdWood = IConfigurableMachine.grab(obj, "I:thresholdWood", thresholdWood);
        thresholdCoal = IConfigurableMachine.grab(obj, "I:thresholdCoal", thresholdCoal);
        thresholdMisc = IConfigurableMachine.grab(obj, "I:thresholdMisc", thresholdMisc);
        thresholdFly = IConfigurableMachine.grab(obj, "I:thresholdFly", thresholdFly);
        thresholdSoot = IConfigurableMachine.grab(obj, "I:thresholdSoot", thresholdSoot);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:thresholdWood").value(thresholdWood);
        writer.name("I:thresholdCoal").value(thresholdCoal);
        writer.name("I:thresholdMisc").value(thresholdMisc);
        writer.name("I:thresholdFly").value(thresholdFly);
        writer.name("I:thresholdSoot").value(thresholdSoot);
    }
}
