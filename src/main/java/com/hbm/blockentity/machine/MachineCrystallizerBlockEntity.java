package com.hbm.blockentity.machine;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.IFluidCopiable;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.extprop.HbmPlayerAttachments;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineCrystallizerMenu;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes.CrystallizerRecipe;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.lib.ModAttachments;
import com.hbm.main.NuclearTechMod;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.BobMathUtil;
import com.hbm.util.SoundUtils;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineCrystallizer.
 * Loest Erze und andere Feststoffe in Saeure auf; welches Rezept greift, haengt
 * am Eingangsitem zusammen mit dem Fluidtyp im Tank.
 *
 * Alle Zahlenwerte (Energie, Dauer, Saeuremengen, Aufwertungsfaktoren) sind 1:1 uebernommen.
 */
public class MachineCrystallizerBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardReceiverMK2, IUpgradeInfoProvider, IFluidCopiable {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_FLUID_IN = 3;
    public static final int SLOT_FLUID_OUT = 4;
    public static final int SLOT_UPGRADE_START = 5;
    public static final int SLOT_UPGRADE_END = 6;
    public static final int SLOT_IDENTIFIER = 7;

    public long power;
    public static final long maxPower = 1_000_000;
    public static final int demand = 1_000;
    public short progress;
    public short duration = 600;
    public boolean isOn;

    /** Nur Client: Drehung des Ruehrwerks */
    public float angle;
    public float prevAngle;
    private AudioWrapper audio;

    public final FluidTank tank;

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    private AABB renderBox;

    public MachineCrystallizerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_CRYSTALLIZER.get(), pos, state, 8);

        /* Ohne diesen Aufruf bleibt die Rezeptliste leer, falls der Lader noch nicht durchgelaufen ist. */
        CrystallizerRecipes.INSTANCE.registerDefaults();

        this.tank = new FluidTank(Fluids.PEROXIDE, 8_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.crystallizer");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.isOn = false;

            /* Ersatz fuer autoPort() aus 1.7.10: Strom und Saeure abonnieren */
            for(DirPos pos : this.getConPos()) {
                this.trySubscribe(this.level, pos);
                if(this.tank.getTankType() != Fluids.NONE) this.trySubscribe(this.tank.getTankType(), this.level, pos);
            }

            this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, maxPower);
            this.tank.setType(SLOT_IDENTIFIER, this.slots);
            this.tank.loadTank(this.level, SLOT_FLUID_IN, SLOT_FLUID_OUT, this.slots);

            this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE_START, SLOT_UPGRADE_END);

            for(int i = 0; i < this.getCycleCount(); i++) {

                if(this.canProcess()) {

                    this.progress++;
                    this.power -= this.getPowerRequired();
                    this.isOn = true;

                    if(this.progress > this.getDuration()) {
                        this.progress = 0;
                        this.processItem();

                        this.setChanged();
                    }

                } else {
                    this.progress = 0;
                }
            }

            this.networkPackNT(25);

        } else {

            this.prevAngle = this.angle;

            if(this.isOn) {
                this.angle += 5F * this.getCycleCount();

                if(this.angle >= 360) {
                    this.angle -= 360;
                    this.prevAngle -= 360;
                }

                Player me = NuclearTechMod.proxy.me();
                int x = this.worldPosition.getX();
                int y = this.worldPosition.getY();
                int z = this.worldPosition.getZ();

                if(me != null && this.level.random.nextInt(20) == 0 && Math.sqrt(me.distanceToSqr(x + 0.5, y + 6, z + 0.5)) < 50) {
                    this.level.addParticle(ParticleTypes.CLOUD, x + this.level.random.nextDouble(), y + 6.5D, z + this.level.random.nextDouble(), 0.0D, 0.1D, 0.0D);
                }

                if(me != null && Math.sqrt(me.distanceToSqr(x, y, z)) < 25) {
                    if(this.audio == null) {
                        this.audio = this.createAudioLoop();
                        this.audio.startSound();
                    } else if(!this.audio.isPlaying()) {
                        this.audio = rebootAudio(this.audio);
                    }
                    this.audio.keepAlive();
                    this.audio.updateVolume(this.getVolume(1F));
                    this.audio.updatePitch(0.75F);

                } else if(this.audio != null) {
                    this.audio.stopSound();
                    this.audio = null;
                }
            } else if(this.audio != null) {
                this.audio.stopSound();
                this.audio = null;
            }
        }

        /* Der Turm ueber dem Kern laesst sich erklettern -- auf beiden Seiten ausgewertet. */
        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise(Axis.Y);
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        List<Player> players = this.level.getEntitiesOfClass(Player.class,
                new AABB(x + 0.25, y + 1, z + 0.25, x + 0.75, y + 6, z + 0.75).move(rot.getStepX() * 1.5, 0, rot.getStepZ() * 1.5));

        for(Player player : players) {
            HbmPlayerAttachments props = HbmPlayerAttachments.getData(player);
            props.isOnLadder = true;
            player.setData(ModAttachments.PLAYER_ATTACHMENT.get(), props);
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(NtmSoundEvents.CHEMICAL_PLANT_OPERATE.get(), SoundSource.BLOCKS, this, 1F, 15F, 0.75F, 15);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if(this.audio != null) { this.audio.stopSound(); this.audio = null; }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(this.audio != null) { this.audio.stopSound(); this.audio = null; }
    }

    protected DirPos[] getConPos() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + 2, y, z + 1, Library.POS_X),
                new DirPos(x + 2, y, z - 1, Library.POS_X),
                new DirPos(x - 2, y, z + 1, Library.NEG_X),
                new DirPos(x - 2, y, z - 1, Library.NEG_X),
                new DirPos(x + 1, y, z + 2, Library.POS_Z),
                new DirPos(x - 1, y, z + 2, Library.POS_Z),
                new DirPos(x + 1, y, z - 2, Library.NEG_Z),
                new DirPos(x - 1, y, z - 2, Library.NEG_Z)
        };
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeShort(this.progress);
        buf.writeShort(this.getDuration());
        buf.writeLong(this.power);
        buf.writeBoolean(this.isOn);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.progress = buf.readShort();
        this.duration = buf.readShort();
        this.power = buf.readLong();
        this.isOn = buf.readBoolean();
        this.tank.deserialize(buf);
    }

    private void processItem() {

        CrystallizerRecipe result = CrystallizerRecipes.getOutput(this.slots.get(SLOT_INPUT), this.tank.getTankType());

        if(result == null) return; //passiert nie, aber sicher ist sicher

        ItemStack stack = result.output.copy();
        ItemStack out = this.slots.get(SLOT_OUTPUT);

        if(out.isEmpty()) {
            this.slots.set(SLOT_OUTPUT, stack);
        } else if(out.getCount() + stack.getCount() <= out.getMaxStackSize()) {
            out.grow(stack.getCount());
        }

        this.tank.setFill(this.tank.getFill() - this.getRequiredAcid(result.acidAmount));

        float freeChance = this.getFreeChance(result);

        if(freeChance == 0 || freeChance < this.level.random.nextFloat()) {
            this.removeItem(SLOT_INPUT, result.itemAmount);
        }
    }

    private boolean canProcess() {

        ItemStack input = this.slots.get(SLOT_INPUT);

        //kein Eingang?
        if(input.isEmpty()) return false;

        if(this.power < this.getPowerRequired()) return false;

        CrystallizerRecipe result = CrystallizerRecipes.getOutput(input, this.tank.getTankType());

        //oder kein Ausgang?
        if(result == null) return false;

        //zu wenig vom Eingangsitem?
        if(input.getCount() < result.itemAmount) return false;

        if(this.tank.getFill() < this.getRequiredAcid(result.acidAmount)) return false;

        ItemStack stack = result.output;
        ItemStack out = this.slots.get(SLOT_OUTPUT);

        //passt der Ausgang nicht?
        if(!out.isEmpty() && !ItemStack.isSameItemSameComponents(out, stack)) return false;

        //oder ist der Ausgangsslot schon voll?
        if(!out.isEmpty() && out.getCount() + stack.getCount() > out.getMaxStackSize()) return false;

        return true;
    }

    /** Das Original verrechnet hier keine Aufwertung -- der Grundwert wird unveraendert durchgereicht. */
    public int getRequiredAcid(int base) {
        return base;
    }

    public float getFreeChance(CrystallizerRecipe recipe) {
        int efficiency = this.upgradeManager.getLevel(UpgradeType.EFFECT);
        if(efficiency > 0) {
            return Math.min(efficiency * recipe.productivity, 0.99F);
        }
        return 0;
    }

    public short getDuration() {
        CrystallizerRecipe result = CrystallizerRecipes.getOutput(this.slots.get(SLOT_INPUT), this.tank.getTankType());
        int base = result != null ? result.duration : 600;
        int speed = this.upgradeManager.getLevel(UpgradeType.SPEED);
        if(speed > 0) {
            return (short) Math.ceil((base * Math.max(1F - 0.25F * speed, 0.25F)));
        }
        return (short) base;
    }

    public int getPowerRequired() {
        int speed = this.upgradeManager.getLevel(UpgradeType.SPEED);
        int effect = this.upgradeManager.getLevel(UpgradeType.EFFECT);
        return demand + speed * demand + effect * demand * 2;
    }

    public float getCycleCount() {
        int speed = this.upgradeManager.getLevel(UpgradeType.OVERDRIVE);
        return Math.min(1 + speed * 2, 7);
    }

    public long getPowerScaled(int i) {
        return (this.power * i) / maxPower;
    }

    public int getProgressScaled(int i) {
        if(this.duration <= 0) return 0;
        return (this.progress * i) / this.duration;
    }

    @Override public void setPower(long i) { this.power = i; }
    @Override public long getPower() { return this.power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.power = tag.getLong("power");
        this.tank.readFromNBT(tag, "tank");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putLong("power", this.power);
        this.tank.writeToNBT(tag, "tank");
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_INPUT && CrystallizerRecipes.getOutput(stack, this.tank.getTankType()) != null) return true;
        if(slot == SLOT_BATTERY && stack.getItem() instanceof IBatteryItem) return true;
        /* Ab 1.21 fragt der Slot selbst hier nach -- deshalb muessen Fluidbehaelter-, Kennungs-
         * und Aufwertungsslot ausdruecklich erlaubt werden, sonst laesst die Oberflaeche
         * nichts mehr hineinlegen. Hopper sehen davon nichts, die haengen an getSlotsForFace. */
        if(slot == SLOT_FLUID_IN) return true;
        if(slot == SLOT_IDENTIFIER && stack.getItem() instanceof IItemFluidIdentifier) return true;
        if(slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
            return stack.getItem() instanceof MachineUpgradeItem item && this.getValidUpgrades().containsKey(item.type);
        }

        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_OUTPUT;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { SLOT_INPUT, SLOT_OUTPUT };
    }

    /**
     * Ohne eigene Box cullt Minecraft alles oberhalb des Kernblocks weg -- die Maschine ist
     * deutlich hoeher als ein Block. Kein @Override: die Oberklasse hat keine solche Methode.
     */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 1, y, z - 1, x + 2, y + 10, z + 2);
        }
        return this.renderBox;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);

        if(this.level == null) return;
        if(index >= SLOT_UPGRADE_START && index <= SLOT_UPGRADE_END && stack.getItem() instanceof MachineUpgradeItem) {
            SoundUtils.playAtVec3(this.level, this.getBlockPos().getCenter(), NtmSoundEvents.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineCrystallizerMenu(id, inventory, this);
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.EFFECT || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {
        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_CRYSTALLIZER.get()));
        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_DELAY, "-" + (lvl * 25) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 100) + "%").withStyle(ChatFormatting.RED));
        }
        if(type == UpgradeType.EFFECT) {
            components.add(Component.translatable(KEY_EFFICIENCY, "x" + lvl).withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 200) + "%").withStyle(ChatFormatting.RED));
        }
        if(type == UpgradeType.OVERDRIVE) {
            components.add(Component.translatable(KEY_YES).withStyle(BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.EFFECT, 3);
        upgrades.put(UpgradeType.OVERDRIVE, 3);
        return upgrades;
    }

    @Override
    public int[] getFluidIDToCopy() {
        return new int[] { this.tank.getTankType().getID() };
    }

    @Override
    public FluidTank getTankToPaste() {
        return this.tank;
    }
}
