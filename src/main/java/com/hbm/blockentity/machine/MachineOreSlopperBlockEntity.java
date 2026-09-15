package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachineOreSlopperMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.items.special.BedrockOreBaseItem;
import com.hbm.items.special.BedrockOreItem;
import com.hbm.items.special.BedrockOreItem.BedrockOreGrade;
import com.hbm.items.special.BedrockOreItem.BedrockOreType;
import com.hbm.lib.Library;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.registry.NtmDamageTypes;
import com.hbm.util.SoundUtils;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineOreSlopper.
 *
 * Der Erzschlaemmer. Er nimmt die Rohproben des Baggers, spuelt sie mit Wasser aus und traegt
 * zusammen, was an Erz darin steckt -- SORTE FUER SORTE GETRENNT. Erst wenn von einer Sorte eine
 * ganze Einheit beisammen ist, faellt ein Grundgesteinserz der Stufe BASE heraus.
 *
 * DAS IST DER SINN DES ZAEHLENS: eine einzelne Probe enthaelt von jeder Sorte nur Bruchteile. Wer
 * ein Erz einer bestimmten Sorte will, muss an einer Stelle schuerfen, wo genug davon liegt --
 * sonst sammelt der Schlaemmer ewig.
 *
 * AUS WASSER WIRD SCHLAMM. Das Abwasser muss abgefuehrt werden; laeuft der Schlammtank voll,
 * steht die Maschine.
 *
 * WER UNTER DIE SCHAUFEL GERAET, STIRBT. Das Original toetet alles im Arbeitsbereich mit
 * Turbinenschaden; das bleibt so.
 */
public class MachineOreSlopperBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider {

    public long power;
    public static final long MAX_POWER = 100_000;

    public static final int WATER_USED_BASE = 1_000;
    public int waterUsed = WATER_USED_BASE;
    public static final long CONSUMPTION_BASE = 200;
    public long consumption = CONSUMPTION_BASE;

    public float progress;
    public boolean processing;

    /** Was von jeder Sorte schon beisammen ist. Ab 1.0 faellt ein Erz heraus. */
    public double[] ores = new double[BedrockOreType.values().length];

    public FluidTank[] tanks;

    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    /* ---- nur zur Anzeige ---- */
    public SlopperAnimation animation = SlopperAnimation.LOWERING;
    public float slider, prevSlider;
    public float bucket, prevBucket;
    public float blades, prevBlades;
    public float fan, prevFan;
    public int delay;

    public enum SlopperAnimation { LOWERING, LIFTING, MOVE_SHREDDER, DUMPING, MOVE_BUCKET }

    public MachineOreSlopperBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_ORE_SLOPPER.get(), pos, state, 11);

        this.tanks = new FluidTank[2];
        this.tanks[0] = new FluidTank(Fluids.WATER, 16_000);
        this.tanks[1] = new FluidTank(Fluids.SLOP, 16_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineOreSlopper");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {

            if(!(this.level instanceof ServerLevel serverLevel)) return;

            this.power = Library.chargeTEFromItems(slots, 0, power, MAX_POWER);

            this.tanks[0].setType(1, slots);
            FluidType conversion = this.getFluidOutput(this.tanks[0].getTankType());
            if(conversion != null) this.tanks[1].setTankType(conversion);

            for(DirPos pos : getConPos()) {
                this.trySubscribe(level, pos);
                if(this.tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(this.tanks[0].getTankType(), level, pos);
                if(this.tanks[1].getFill() > 0) this.tryProvide(this.tanks[1], level, pos);
            }

            this.processing = false;

            this.upgradeManager.checkSlots(slots, 9, 10);
            int speed = this.upgradeManager.getLevel(UpgradeType.SPEED);
            int efficiency = this.upgradeManager.getLevel(UpgradeType.EFFECT);

            this.consumption = CONSUMPTION_BASE + (CONSUMPTION_BASE * speed) / 2 + (CONSUMPTION_BASE * efficiency);

            if(this.canSlop()) {

                this.power -= this.consumption;
                this.progress += 1F / (600 - speed * 150);
                this.processing = true;
                boolean markDirty = false;

                while(this.progress >= 1F && this.canSlop()) {
                    this.progress -= 1F;

                    for(BedrockOreType type : BedrockOreType.values()) {
                        this.ores[type.ordinal()] += BedrockOreBaseItem.getOreAmount(slots.get(2), type) * (1D + efficiency * 0.1D);
                    }

                    this.removeItem(2, 1);
                    this.tanks[0].setFill(this.tanks[0].getFill() - this.waterUsed);
                    this.tanks[1].setFill(this.tanks[1].getFill() + this.waterUsed);
                    markDirty = true;
                }

                if(markDirty) this.setChanged();

                this.grindEntities(serverLevel);

            } else {
                this.progress = 0F;
            }

            this.emitOres();

            this.networkPackNT(150);

        } else {
            this.animate();
        }
    }

    /** Wer im Arbeitsbereich steht, wird zerhackt -- Schaden und Fetzen wie beim Turbinenlaufrad. */
    private void grindEntities(ServerLevel serverLevel) {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);

        AABB box = new AABB(
                this.worldPosition.getX() - 0.5 + dir.getStepX(), this.worldPosition.getY() + 1, this.worldPosition.getZ() - 0.5 + dir.getStepZ(),
                this.worldPosition.getX() + 1.5 + dir.getStepX(), this.worldPosition.getY() + 3, this.worldPosition.getZ() + 1.5 + dir.getStepZ());

        for(Entity e : serverLevel.getEntitiesOfClass(Entity.class, box)) {

            e.hurt(serverLevel.damageSources().source(NtmDamageTypes.TURBOFAN), 1000F);

            if(!e.isAlive() && e instanceof LivingEntity) {
                CompoundTag vdat = new CompoundTag();
                vdat.putString("type", "giblets");
                vdat.putInt("ent", e.getId());
                vdat.putInt("cDiv", 5);
                double px = e.getX();
                double py = e.getY() + e.getBbHeight() * 0.5;
                double pz = e.getZ();
                PacketDistributor.sendToPlayersNear(serverLevel, null, px, py, pz, 150, new AuxParticle(vdat, px, py, pz));

                SoundUtils.playAtVec3(serverLevel, new Vec3(e.getX(), e.getY(), e.getZ()),
                        SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 2.0F, 0.95F + serverLevel.random.nextFloat() * 0.2F);
            }
        }
    }

    /** Was von einer Sorte voll ist, wandert in die sechs Ausgabefaecher. */
    private void emitOres() {

        for(BedrockOreType type : BedrockOreType.values()) {

            ItemStack output = BedrockOreItem.make(BedrockOreGrade.BASE, type);
            int i = type.ordinal();

            outer:
            while(this.ores[i] >= 1D) {

                for(int slot = 3; slot <= 8; slot++) {
                    ItemStack stack = slots.get(slot);
                    if(!stack.isEmpty() && stack.getItem() == output.getItem()
                            && MetaHelper.getMeta(stack) == MetaHelper.getMeta(output)
                            && stack.getCount() < output.getMaxStackSize()) {
                        stack.grow(1);
                        this.ores[i] -= 1D;
                        continue outer;
                    }
                }

                for(int slot = 3; slot <= 8; slot++) {
                    if(slots.get(slot).isEmpty()) {
                        slots.set(slot, output.copy());
                        this.ores[i] -= 1D;
                        continue outer;
                    }
                }

                /* Kein Platz mehr -- der Rest bleibt stehen, bis abgeraeumt wird. */
                break;
            }
        }
    }

    /** Schaufel, Schlitten, Messer und Geblaese. Reine Anzeige, laeuft nur auf der Client-Seite. */
    private void animate() {

        this.prevSlider = this.slider;
        this.prevBucket = this.bucket;
        this.prevBlades = this.blades;
        this.prevFan = this.fan;

        if(!this.processing) return;

        this.blades += 15F;
        this.fan += 35F;

        if(this.blades >= 360F) { this.blades -= 360F; this.prevBlades -= 360F; }
        if(this.fan >= 360F) { this.fan -= 360F; this.prevFan -= 360F; }

        if(this.delay > 0) { this.delay--; return; }

        switch(this.animation) {
            case LOWERING -> {
                this.bucket += 1F / 40F;
                if(this.bucket >= 1F) { this.bucket = 1F; this.animation = SlopperAnimation.LIFTING; this.delay = 20; }
            }
            case LIFTING -> {
                this.bucket -= 1F / 40F;
                if(this.bucket <= 0F) { this.bucket = 0F; this.animation = SlopperAnimation.MOVE_SHREDDER; this.delay = 10; }
            }
            case MOVE_SHREDDER -> {
                this.slider += 1F / 50F;
                if(this.slider >= 1F) { this.slider = 1F; this.animation = SlopperAnimation.DUMPING; this.delay = 60; }
            }
            case DUMPING -> this.animation = SlopperAnimation.MOVE_BUCKET;
            case MOVE_BUCKET -> {
                this.slider -= 1F / 50F;
                if(this.slider <= 0F) { this.animation = SlopperAnimation.LOWERING; this.delay = 10; }
            }
        }
    }

    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise();

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + dir.getStepX() * 4, y, z + dir.getStepZ() * 4, dir),
                new DirPos(x - dir.getStepX() * 4, y, z - dir.getStepZ() * 4, dir.getOpposite()),
                new DirPos(x + rot.getStepX() * 2, y, z + rot.getStepZ() * 2, rot),
                new DirPos(x - rot.getStepX() * 2, y, z - rot.getStepZ() * 2, rot.getOpposite()),
                new DirPos(x + dir.getStepX() * 2 + rot.getStepX() * 2, y, z + dir.getStepZ() * 2 + rot.getStepZ() * 2, rot),
                new DirPos(x + dir.getStepX() * 2 - rot.getStepX() * 2, y, z + dir.getStepZ() * 2 - rot.getStepZ() * 2, rot.getOpposite()),
                new DirPos(x - dir.getStepX() * 2 + rot.getStepX() * 2, y, z - dir.getStepZ() * 2 + rot.getStepZ() * 2, rot),
                new DirPos(x - dir.getStepX() * 2 - rot.getStepX() * 2, y, z - dir.getStepZ() * 2 - rot.getStepZ() * 2, rot.getOpposite())
        };
    }

    public boolean canSlop() {
        if(this.getFluidOutput(this.tanks[0].getTankType()) == null) return false;
        if(this.tanks[0].getFill() < this.waterUsed) return false;
        if(this.tanks[1].getFill() + this.waterUsed > this.tanks[1].getMaxFill()) return false;
        if(this.power < this.consumption) return false;

        return slots.get(2).getItem() == NtmItems.BEDROCK_ORE_BASE.get();
    }

    /** Aus Wasser wird Schlamm, aus allem anderen nichts. */
    public FluidType getFluidOutput(FluidType input) {
        return input == Fluids.WATER ? Fluids.SLOP : null;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 2 && stack.getItem() == NtmItems.BEDROCK_ORE_BASE.get();
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= 3 && index <= 8;
    }

    private static final int[] SLOT_ACCESS = new int[] { 2, 3, 4, 5, 6, 7, 8 };

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return SLOT_ACCESS;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeLong(consumption);
        buf.writeFloat(progress);
        buf.writeBoolean(processing);
        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.consumption = buf.readLong();
        this.progress = buf.readFloat();
        this.processing = buf.readBoolean();
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getLong("power");
        this.progress = tag.getFloat("progress");
        this.tanks[0].readFromNBT(tag, "water");
        this.tanks[1].readFromNBT(tag, "slop");

        /* Der Teilstand je Sorte. Das Original vergisst ihn beim Speichern -- wer die Maschine
         * mitten im Zaehlen verlaesst, faengt dort von vorn an. Der Port haelt ihn fest. */
        for(BedrockOreType type : BedrockOreType.values()) {
            this.ores[type.ordinal()] = tag.getDouble("ore_" + type.suffix);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", power);
        tag.putFloat("progress", progress);
        this.tanks[0].writeToNBT(tag, "water");
        this.tanks[1].writeToNBT(tag, "slop");

        for(BedrockOreType type : BedrockOreType.values()) {
            tag.putDouble("ore_" + type.suffix, this.ores[type.ordinal()]);
        }
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return MAX_POWER; }

    @Override public FluidTank[] getAllTanks() { return tanks; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tanks[1] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tanks[0] }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineOreSlopperMenu(id, inventory, this);
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.EFFECT;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {
        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_ORE_SLOPPER.get()));
        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_DELAY, "-" + (lvl * 25) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 50) + "%").withStyle(ChatFormatting.RED));
        }
        if(type == UpgradeType.EFFECT) {
            components.add(Component.translatable(KEY_EFFICIENCY, "+" + (lvl * 10) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 100) + "%").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.EFFECT, 3);
        return upgrades;
    }
}
