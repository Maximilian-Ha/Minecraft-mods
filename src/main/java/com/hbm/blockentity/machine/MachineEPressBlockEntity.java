package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.menus.MachineEPressMenu;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.items.machine.StampItem;
import com.hbm.lib.Library;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineEPress.
 *
 * Die elektrische Presse ist die grosse Schwester der Kohlepresse aus dem frueheren Stand: gleiche
 * Stempel, gleiche Rezepte, nur laeuft sie mit Strom statt mit Feuer.
 *
 * SIE IST NICHT BLOSS BEQUEMER, SIE IST SCHNELLER. Die Kohlepresse muss erst warmlaufen -- ihr
 * Stempel bewegt sich anfangs kaum und wird ueber vierhundert Ticks hinweg schneller. Die
 * elektrische faehrt vom ersten Hub an mit voller Geschwindigkeit; solange Strom da ist, gibt es
 * kein Anlaufen.
 *
 * SIE VERBRAUCHT JE TICK, NICHT JE HUB. Hundert HE gehen in jedem Tick weg, in dem sich der
 * Stempel bewegt -- auch beim Zurueckfahren und in der Pause dazwischen. Das Original rechnet
 * ebenso, und es passt zum Bild: ein Motor, der eine Spindel dreht, zieht Strom, egal in welche
 * Richtung er dreht.
 *
 * DIE GESCHWINDIGKEITSAUFWERTUNG WIRKT DOPPELT: der Stempel faehrt schneller UND die Pause
 * zwischen den Huben wird kuerzer. Drei Stufen, wie im Original.
 *
 * NICHT UEBERNOMMEN: der Renderer aus epress_body.obj und epress_head.obj. Der Port zeichnet die
 * Presse als Kasten, drei Bloecke hoch wie das Original. Das Geraeusch beim Hub ist da.
 */
public class MachineEPressBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IUpgradeInfoProvider {

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_STAMP = 1;
    public static final int SLOT_INPUT = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_UPGRADE = 4;

    public static final long MAX_POWER = 50_000;

    /** HE je Tick, in dem sich der Stempel bewegt. */
    public static final long CONSUMPTION = 100;

    /** Wie weit der Stempel faehrt, bevor der Hub sitzt. */
    public static final int maxPress = 200;

    public long power;

    public int press;
    public float renderPress;
    public float lastPress;
    private int syncPress;
    private int turnProgress;

    private boolean isRetracting = false;
    private int delay;

    public ItemStack syncStack = ItemStack.EMPTY;

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public MachineEPressBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_EPRESS.get(), pos, state, 5);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.epress");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {

            for(DirPos pos : this.getConPos()) this.trySubscribe(this.level, pos);

            this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, MAX_POWER);

            boolean canProcess = this.canProcess();

            if((canProcess || this.isRetracting || this.delay > 0) && this.power >= CONSUMPTION) {

                this.power -= CONSUMPTION;

                if(this.delay <= 0) {

                    this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE, SLOT_UPGRADE);
                    int speed = 1 + this.upgradeManager.getLevel(UpgradeType.SPEED);

                    /* Zurueck geht es langsamer als hin -- den Hub soll man sehen. */
                    int stampSpeed = (int) ((this.isRetracting ? 20 : 45) * (1D + speed / 4D));

                    if(this.isRetracting) {

                        this.press -= stampSpeed;

                        if(this.press <= 0) {
                            this.press = 0;
                            this.isRetracting = false;
                            this.delay = 5 - speed + 1;
                        }

                    } else if(canProcess) {

                        this.press += stampSpeed;
                        if(this.press >= maxPress) this.stamp(speed);

                    } else if(this.press > 0) {
                        this.isRetracting = true;
                    }

                } else {
                    this.delay--;
                }
            }

            this.networkPackNT(50);

        } else {

            this.lastPress = this.renderPress;

            if(this.turnProgress > 0) {
                this.renderPress = this.renderPress + ((this.syncPress - this.renderPress) / (float) this.turnProgress);
                this.turnProgress--;
            } else {
                this.renderPress = this.syncPress;
            }
        }
    }

    /** Der Hub sitzt: ein Gut wird verbraucht, der Stempel nutzt sich ab. */
    private void stamp(int speed) {

        SoundUtils.playAtVec3(this.level, this.getBlockPos().getCenter(), NtmSoundEvents.PRESS_OPERATE.get(),
                SoundSource.BLOCKS, this.getVolume(1.5F), 1F);

        ItemStack output = PressRecipes.getOutput(this.slots.get(SLOT_INPUT), this.slots.get(SLOT_STAMP));

        if(this.slots.get(SLOT_OUTPUT).isEmpty()) {
            this.slots.set(SLOT_OUTPUT, output.copy());
        } else {
            this.slots.get(SLOT_OUTPUT).grow(output.getCount());
        }

        this.removeItem(SLOT_INPUT, 1);

        ItemStack stamp = this.slots.get(SLOT_STAMP);

        if(stamp.getMaxDamage() != 0) {
            int damage = stamp.getDamageValue() + 1;
            stamp.setDamageValue(damage);
            if(damage >= stamp.getMaxDamage()) stamp.shrink(1);
        }

        this.press = maxPress;
        this.isRetracting = true;
        this.delay = 5 - speed + 1;

        this.setChanged();
    }

    public boolean canProcess() {

        if(this.power < CONSUMPTION) return false;
        if(this.slots.get(SLOT_STAMP).isEmpty() || this.slots.get(SLOT_INPUT).isEmpty()) return false;

        ItemStack output = PressRecipes.getOutput(this.slots.get(SLOT_INPUT), this.slots.get(SLOT_STAMP));
        if(output.isEmpty()) return false;

        ItemStack outSlot = this.slots.get(SLOT_OUTPUT);
        if(outSlot.isEmpty()) return true;

        return ItemStack.isSameItemSameComponents(outSlot, output)
                && outSlot.getCount() + output.getCount() <= outSlot.getMaxStackSize();
    }

    private DirPos[] getConPos() {

        DirPos[] pos = new DirPos[Direction.values().length];
        int i = 0;

        for(Direction dir : Direction.values()) pos[i++] = new DirPos(this.worldPosition.relative(dir), dir);

        return pos;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {

        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_UPGRADE) return stack.getItem() instanceof MachineUpgradeItem item && this.getValidUpgrades().containsKey(item.type);
        if(stack.getItem() instanceof StampItem) return slot == SLOT_STAMP;

        return slot == SLOT_INPUT;
    }

    @Override public int[] getSlotsForFace(Direction direction) { return new int[] { SLOT_STAMP, SLOT_INPUT, SLOT_OUTPUT }; }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_OUTPUT;
    }

    @Override public void setPower(long power) { this.power = power; }
    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {

        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_EPRESS.get()));

        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_DELAY, "-" + (50 * lvl / 3) + "%").withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        return upgrades;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineEPressMenu(id, inventory, this);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.press);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.slots.get(SLOT_INPUT));
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.syncPress = buf.readInt();
        this.syncStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);

        this.turnProgress = 2;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", this.power);
        tag.putInt("press", this.press);
        tag.putBoolean("ret", this.isRetracting);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.press = tag.getInt("press");
        this.isRetracting = tag.getBoolean("ret");
    }
}
