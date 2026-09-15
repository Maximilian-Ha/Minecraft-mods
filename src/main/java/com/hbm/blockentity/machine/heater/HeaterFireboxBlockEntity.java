package com.hbm.blockentity.machine.heater;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.IConfigurableMachine;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.AshpitBlockEntity;
import com.hbm.inventory.menus.HeaterFireboxMenu;
import com.hbm.module.ModuleBurnTime;
import com.hbm.util.ItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;
import java.io.IOException;

public class HeaterFireboxBlockEntity extends AbstractHeaterMachineBlockEntity {

    public static int baseHeat = 100;
    public static double timeMult = 1.0D;
    public static int maxHeatEnergy = 100_000;

    public static final ModuleBurnTime burnModule = new ModuleBurnTime()
            .setLigniteTimeMod(1.25D)
            .setCoalTimeMod(1.25D)
            .setCokeTimeMod(1.25D)
            .setSolidTimeMod(1.5D)
            .setRocketTimeMod(1.5D)
            .setBalefireTimeMod(0.5D)
            .setLigniteHeatMod(2D)
            .setCoalHeatMod(2D)
            .setCokeHeatMod(2D)
            .setSolidHeatMod(3D)
            .setRocketHeatMod(5D)
            .setBalefireHeatMod(15D);

    public int maxBurnTime;
    public int burnTime;
    public int burnHeat;
    public boolean wasOn;
    public int playersUsing;
    public float doorAngle;
    public float prevDoorAngle;

    public HeaterFireboxBlockEntity(BlockPos pos, BlockState state) {
        this(NtmBlockEntityTypes.HEATER_FIREBOX.get(), pos, state);
    }

    protected HeaterFireboxBlockEntity(BlockEntityType<? extends HeaterFireboxBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 2);
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.heaterFirebox"); }

    /**
     * Im Original haben Brennkasten und Backofen jeweils ein eigenes ModuleBurnTime und holen es
     * ueber getModule(). Der Port hatte nur das des Brennkastens; damit haette eine Aenderung an
     * der Konfiguration des Backofens den Brennkasten gleich mit verstellt.
     */
    protected ModuleBurnTime getBurnModule() { return burnModule; }

    protected int getBaseHeat() {
        return baseHeat;
    }

    protected double getTimeMult() {
        return timeMult;
    }

    @Override
    protected int getMaxHeat() {
        return maxHeatEnergy;
    }

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

        this.wasOn = false;

        if(this.burnTime <= 0) {
            for(int i = 0; i < 2; i++) {
                ItemStack stack = this.slots.get(i);
                if(stack.isEmpty()) continue;

                int baseTime = this.getBurnModule().getBurnTime(stack);
                if(baseTime <= 0) continue;

                int fuel = (int) (baseTime * this.getTimeMult());

                // Runde 10: Asche in die Aschegrube darunter, wie im Original
                // (TileEntityFireboxBase.java:82-91). Gezaehlt wird die unveraenderte Brenndauer,
                // nicht die mit getTimeMult() gestreckte -- der Backofen brennt schneller ab,
                // erzeugt aber dieselbe Menge Asche.
                if(this.level.getBlockEntity(this.getBlockPos().below()) instanceof AshpitBlockEntity ashpit) {
                    switch(getAshFromFuel(stack)) {
                        case WOOD -> ashpit.ashLevelWood += baseTime;
                        case COAL -> ashpit.ashLevelCoal += baseTime;
                        case MISC -> ashpit.ashLevelMisc += baseTime;
                    }
                    ashpit.setChanged();
                }

                this.maxBurnTime = this.burnTime = fuel;
                this.burnHeat = this.getBurnModule().getBurnHeat(this.getBaseHeat(), stack);

                ItemStack remainder = stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem().copy() : ItemStack.EMPTY;
                stack.shrink(1);
                if(stack.isEmpty()) {
                    this.slots.set(i, remainder);
                }

                this.wasOn = true;
                break;
            }
        } else {
            if(this.heatEnergy < this.getMaxHeat()) {
                this.burnTime--;
            }
            this.wasOn = true;
        }

        if(this.wasOn) {
            this.heatEnergy = Math.min(this.heatEnergy + this.burnHeat, this.getMaxHeat());
        } else {
            this.heatEnergy = Math.max(this.heatEnergy - Math.max(this.heatEnergy / 1000, 1), 0);
            this.burnHeat = 0;
        }

        this.networkPackNT(25);
    }

    public enum AshType { WOOD, COAL, MISC }

    /**
     * Original: TileEntityFireboxBase.getAshFromFuel. Dort werden die OreDictionary-Namen
     * durchsucht; auf 1.21 stehen an derselben Stelle die Item-Tags.
     */
    public static AshType getAshFromFuel(ItemStack stack) {
        for(String tag : ItemStackUtil.getTags(stack)) {
            String name = tag.toLowerCase(Locale.US);
            if(name.contains("coke")) return AshType.COAL;
            if(name.contains("coal")) return AshType.COAL;
            if(name.contains("lignite")) return AshType.COAL;
            if(name.contains("log")) return AshType.WOOD;
            if(name.contains("wood")) return AshType.WOOD;
            if(name.contains("sapling")) return AshType.WOOD;
        }

        return AshType.MISC;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new HeaterFireboxMenu(id, inventory, this);
    }

    @Override
    public void startOpen(Player player) {
        if(this.level == null) return;
        if(!this.level.isClientSide) this.playersUsing++;
    }

    @Override
    public void stopOpen(Player player) {
        if(this.level == null) return;
        if(!this.level.isClientSide) this.playersUsing--;
    }

    @Override
    public void writeNBT(CompoundTag savedTag) {
        super.writeNBT(savedTag);
        CompoundTag tag = savedTag.getCompound(NBT_PERSISTENT_KEY);
        tag.putInt("maxBurnTime", this.maxBurnTime);
        tag.putInt("burnTime", this.burnTime);
        tag.putInt("burnHeat", this.burnHeat);
        tag.putBoolean("wasOn", this.wasOn);
        tag.putInt("playersUsing", this.playersUsing);
        tag.putFloat("doorAngle", this.doorAngle);
        tag.putFloat("prevDoorAngle", this.prevDoorAngle);
    }

    @Override
    public void readNBT(CompoundTag savedTag) {
        super.readNBT(savedTag);
        CompoundTag tag = savedTag.getCompound(NBT_PERSISTENT_KEY);
        this.maxBurnTime = tag.getInt("maxBurnTime");
        this.burnTime = tag.getInt("burnTime");
        this.burnHeat = tag.getInt("burnHeat");
        this.wasOn = tag.getBoolean("wasOn");
        this.playersUsing = tag.getInt("playersUsing");
        this.doorAngle = tag.getFloat("doorAngle");
        this.prevDoorAngle = tag.getFloat("prevDoorAngle");
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.getBurnModule().getBurnTime(stack) > 0;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.maxBurnTime = tag.getInt("maxBurnTime");
        this.burnTime = tag.getInt("burnTime");
        this.burnHeat = tag.getInt("burnHeat");
        this.wasOn = tag.getBoolean("wasOn");
        this.playersUsing = tag.getInt("playersUsing");
        this.doorAngle = tag.getFloat("doorAngle");
        this.prevDoorAngle = tag.getFloat("prevDoorAngle");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("maxBurnTime", this.maxBurnTime);
        tag.putInt("burnTime", this.burnTime);
        tag.putInt("burnHeat", this.burnHeat);
        tag.putBoolean("wasOn", this.wasOn);
        tag.putInt("playersUsing", this.playersUsing);
        tag.putFloat("doorAngle", this.doorAngle);
        tag.putFloat("prevDoorAngle", this.prevDoorAngle);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.maxBurnTime);
        buf.writeInt(this.burnTime);
        buf.writeInt(this.burnHeat);
        buf.writeBoolean(this.wasOn);
        buf.writeInt(this.playersUsing);
        buf.writeFloat(this.doorAngle);
        buf.writeFloat(this.prevDoorAngle);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.maxBurnTime = buf.readInt();
        this.burnTime = buf.readInt();
        this.burnHeat = buf.readInt();
        this.wasOn = buf.readBoolean();
        this.playersUsing = buf.readInt();
        this.doorAngle = buf.readFloat();
        this.prevDoorAngle = buf.readFloat();
    }

    /** Konfiguration, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static void readConfig(JsonObject obj) {
        baseHeat = IConfigurableMachine.grab(obj, "I:baseHeat", baseHeat);
        timeMult = IConfigurableMachine.grab(obj, "D:burnTimeMult", timeMult);
        maxHeatEnergy = IConfigurableMachine.grab(obj, "I:heatCap", maxHeatEnergy);

        if(obj.has("M:burnModule")) burnModule.readIfPresent(obj.get("M:burnModule").getAsJsonObject());
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:baseHeat").value(baseHeat);
        writer.name("D:burnTimeMult").value(timeMult);
        writer.name("I:heatCap").value(maxHeatEnergy);

        writer.name("M:burnModule").beginObject();
        burnModule.writeConfig(writer);
        writer.endObject();
    }
}
