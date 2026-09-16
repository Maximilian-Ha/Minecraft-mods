package com.hbm.blockentity.machine.oil;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous_ART;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.menus.MachineGasFlareMenu;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.particle.ParticleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
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
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare.
 *
 * Der Schornstein, an dem in der Raffinerie das ablaesst, was sonst nirgends hinpasst. Zwei
 * Schalter: das Ventil laesst ueberhaupt etwas hinauf, die Zuendung entscheidet, ob es oben
 * abgefackelt oder nur abgeblasen wird. Beim Abfackeln faellt Strom an, beim Abblasen nicht --
 * dafuer ist es schneller. Wer der Flamme zu nahe kommt, brennt.
 *
 * ABWEICHUNG: das Original zeigt beim Verbrennen zusaetzlich eine Rauchfahne aus einer
 * Partikelsorte, die der Port nicht kennt; sie bleibt weg. Die Gasfahne beim Abblasen und die
 * Flamme selbst sind da.
 */
public class MachineGasFlareBlockEntity extends MachineBaseBlockEntity implements IEnergyProviderMK2, IFluidStandardReceiverMK2, IControlReceiver, IUpgradeInfoProvider {

    public static final long MAX_POWER = 100_000L;
    /** Wie viel sich ohne Zuendung je Tick abblasen laesst, und wie viel sich verbrennen laesst. */
    public static final int MAX_VENT = 50;
    public static final int MAX_BURN = 10;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_FLUID_IN = 1;
    public static final int SLOT_FLUID_OUT = 2;
    public static final int SLOT_FLUID_ID = 3;
    public static final int SLOT_UPGRADE_START = 4;
    public static final int SLOT_UPGRADE_END = 5;

    public long power;
    public boolean isOn;
    public boolean doesBurn;

    public final FluidTank tank;

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public MachineGasFlareBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_GAS_FLARE.get(), pos, state, 6);
        this.tank = new FluidTank(Fluids.GAS, 64_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machine_gas_flare");
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()) <= 256D;
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("valve")) this.isOn = !this.isOn;
        if(tag.contains("dial")) this.doesBurn = !this.doesBurn;
        this.setChanged();
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.checkTilt(TiltType.CONFIG, false);

            for(DirPos pos : this.getConPos()) {
                this.tryProvide(this.level, pos.makeCompat(), pos.getDir());
                this.trySubscribe(this.tank.getTankType(), this.level, pos);
            }

            this.tank.setType(SLOT_FLUID_ID, this.slots);
            this.tank.loadTank(this.level, SLOT_FLUID_IN, SLOT_FLUID_OUT, this.slots);

            if(this.isOn && this.tank.getFill() > 0 && !this.tilted) {

                this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE_START, SLOT_UPGRADE_END);
                int burnLevel = this.upgradeManager.getLevel(UpgradeType.SPEED);
                int yield = this.upgradeManager.getLevel(UpgradeType.EFFECT);

                int maxVent = MAX_VENT + MAX_VENT * burnLevel;
                int maxBurn = MAX_BURN + MAX_BURN * burnLevel;

                if(this.shouldBurn()) {
                    this.burn(maxBurn, yield);
                } else {
                    this.vent(maxVent);
                }
            }

            this.power = Library.chargeItemsFromTE(this.slots, SLOT_BATTERY, this.power, MAX_POWER);

            this.networkPackNT(50);

        } else {

            if(this.isOn && this.tank.getFill() > 0) {
                if(this.shouldBurn()) this.spawnFlame();
                else if(this.isVentable()) this.spawnPlume();
            }
        }
    }

    /** Verbrannt wird nur, was brennbar ist und wofuer die Zuendung eingeschaltet ist. */
    private boolean shouldBurn() {
        return this.doesBurn && this.tank.getTankType().hasTrait(FT_Flammable.class);
    }

    /** Abgeblasen wird nur, was gasfoermig ist -- Fluessigkeiten steigen nicht den Turm hinauf. */
    private boolean isVentable() {
        return this.tank.getTankType().hasTrait(FT_Gaseous.class) || this.tank.getTankType().hasTrait(FT_Gaseous_ART.class);
    }

    private void vent(int maxVent) {

        if(!this.isVentable()) return;

        int eject = Math.min(maxVent, this.tank.getFill());
        this.tank.setFill(this.tank.getFill() - eject);
        this.tank.getTankType().onFluidRelease(this, this.tank, eject);

        if(this.level.getGameTime() % 7 == 0) {
            this.level.playSound(null, this.worldPosition.getX(), this.worldPosition.getY() + 11, this.worldPosition.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, this.getVolume(1.5F), 0.5F);
        }

        if(this.level.getGameTime() % 5 == 0 && eject > 0) {
            FluidTrait.onRelease(this.level, this.worldPosition, this.tank.getTankType(), this.tank, FluidReleaseType.SPILL, eject * 5);
        }
    }

    private void burn(int maxBurn, int yield) {

        int eject = Math.min(maxBurn, this.tank.getFill());
        this.tank.setFill(this.tank.getFill() - eject);

        /* Gas verbrennt sauber, Fluessigkeit laengst nicht so gut. */
        int penalty = this.isVentable() ? 5 : 10;

        long powerProd = this.tank.getTankType().getTrait(FT_Flammable.class).getHeatEnergy() * eject / 1_000L;
        powerProd /= penalty;
        powerProd += powerProd * yield / 3;

        this.power = Math.min(this.power + powerProd, MAX_POWER);

        List<Entity> caught = this.level.getEntitiesOfClass(Entity.class, new AABB(
                this.worldPosition.getX() - 1, this.worldPosition.getY() + 12, this.worldPosition.getZ() - 2,
                this.worldPosition.getX() + 2, this.worldPosition.getY() + 17, this.worldPosition.getZ() + 2));

        for(Entity e : caught) {
            e.setRemainingFireTicks(5 * 20);
            e.hurt(this.level.damageSources().source(DamageTypes.ON_FIRE), 5F);
        }

        if(this.level.getGameTime() % 3 == 0) {
            this.level.playSound(null, this.worldPosition.getX(), this.worldPosition.getY() + 11, this.worldPosition.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, this.getVolume(1.5F), 0.75F);
        }

        if(this.level.getGameTime() % 5 == 0 && eject > 0) {
            FluidTrait.onRelease(this.level, this.worldPosition, this.tank.getTankType(), this.tank, FluidReleaseType.BURN, eject * 5);
        }
    }

    /** Die Flamme an der Spitze. */
    private void spawnFlame() {
        ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.GAS_FLAME.get(), new CompoundTag()),
                this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 11.75D, this.worldPosition.getZ() + 0.5D,
                (float) (this.level.random.nextGaussian() * 0.15D), 0.2F, (float) (this.level.random.nextGaussian() * 0.15D));
    }

    /** Die Gasfahne, wenn nur abgeblasen wird -- in der Farbe des Stoffes. */
    private void spawnPlume() {

        if(this.level.getGameTime() % 5 != 0) return;

        CompoundTag fx = new CompoundTag();
        fx.putFloat("lift", 1F);
        fx.putFloat("base", 0.25F);
        fx.putFloat("max", 3F);
        fx.putInt("life", 150 + this.level.random.nextInt(20));
        fx.putInt("color", this.tank.getTankType().getColor());

        ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.COOLING_TOWER.get(), fx),
                this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 11D, this.worldPosition.getZ() + 0.5D);
    }

    @Override public int getFloorCount() { return 4; }
    @Override public BlockPos getFloorPosFromIndex(int index) { return this.standardFloor3x3(index); }

    public DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ(), Direction.EAST),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ(), Direction.WEST),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 2, Direction.SOUTH),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 2, Direction.NORTH)
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return stack.getItem() instanceof IBatteryItem;
        if(slot == SLOT_FLUID_IN) return true;
        if(slot == SLOT_FLUID_ID) return stack.getItem() instanceof IItemFluidIdentifier;
        if(slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
            return stack.getItem() instanceof MachineUpgradeItem item && this.getValidUpgrades().containsKey(item.type);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_FLUID_OUT;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { SLOT_FLUID_IN, SLOT_FLUID_OUT };
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.EFFECT;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {

        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_GAS_FLARE.get()));

        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(IUpgradeInfoProvider.KEY_SPEED, "+" + (lvl * 100) + "%").withStyle(ChatFormatting.GREEN));
        }

        if(type == UpgradeType.EFFECT) {
            components.add(Component.translatable(IUpgradeInfoProvider.KEY_EFFICIENCY, "+" + (lvl * 100 / 3) + "%").withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.EFFECT, 3);
        return upgrades;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("powerTime");
        this.tank.readFromNBT(tag, "gas");
        this.isOn = tag.getBoolean("isOn");
        this.doesBurn = tag.getBoolean("doesBurn");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("powerTime", this.power);
        this.tank.writeToNBT(tag, "gas");
        tag.putBoolean("isOn", this.isOn);
        tag.putBoolean("doesBurn", this.doesBurn);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeBoolean(this.isOn);
        buf.writeBoolean(this.doesBurn);
        this.tank.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.isOn = buf.readBoolean();
        this.doesBurn = buf.readBoolean();
        this.tank.deserialize(buf);
    }

    @Override public long getPower() { return Math.max(Math.min(this.power, MAX_POWER), 0); }
    @Override public void setPower(long i) { this.power = i; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override public boolean canConnect(Direction dir) { return dir != null; }
    @Override public boolean canConnect(FluidType type, Direction dir) { return dir != null; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.tank }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { this.tank }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineGasFlareMenu(id, inventory, this);
    }

    private AABB renderBox;

    /* Kein @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 2, y, z - 2, x + 3, y + 13, z + 3);
        }
        return this.renderBox;
    }
}
