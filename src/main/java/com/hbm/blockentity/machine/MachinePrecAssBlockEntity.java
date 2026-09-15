package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.NtmBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.MachinePrecAssMenu;
import com.hbm.inventory.recipes.PrecAssRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.NuclearTechMod;
import com.hbm.module.machine.ModuleMachinePrecAss;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachinePrecAss.
 *
 * Die Praezisionsmontage. Sie sieht aus wie die Montagemaschine und teilt sich deren Modell, hat
 * aber neun Eingabe- UND neun Ausgabefaecher -- und sie ist die einzige Maschine des Mods, die
 * MISSLINGEN kann.
 *
 * DAS MISSLINGEN STECKT NICHT HIER, SONDERN IM REZEPT: jedes Paar in PrecAssRecipes schuettet
 * gewichtet entweder das Werkstueck oder den Ausschuss aus. Die Maschine merkt davon nichts.
 *
 * VIER ARME, EIN WINKELSATZ. Das Original dreht denselben Arm viermal um neunzig Grad weiter;
 * nur die vier Schlagbolzen laufen einzeln. Deshalb steht hier armAngles[3] neben strikers[4]
 * und nicht der AssemblerArm der Montagemaschine.
 */
public class MachinePrecAssBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver {

    public FluidTank inputTank;
    public FluidTank outputTank;

    public long power;
    public long maxPower = 100_000;
    public boolean didProcess = false;

    public boolean frame = false;
    private AudioWrapper audio;

    public ModuleMachinePrecAss assemblerModule;

    public float prevRing;
    public float ring;
    public float ringSpeed;
    public float ringTarget;
    public int ringDelay;

    /** Ruhelage und Arbeitslage der drei Armgelenke, Zahlen des Originals. */
    public static final float[] NULL_POSITION = new float[] { 45F, -30F, 45F };
    public static final float[] WORKING_POSITION = new float[] { 45F, -15F, -5F };

    public float[] armAngles = new float[] { 45F, -15F, -5F };
    public float[] prevArmAngles = new float[] { 45F, -15F, -5F };
    public float[] strikers = new float[4];
    public float[] prevStrikers = new float[4];
    public boolean[] strikerDir = new boolean[4];
    protected int strikerIndex;
    protected int strikerDelay;

    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public MachinePrecAssBlockEntity(BlockPos pos, BlockState blockState) {
        super(NtmBlockEntityTypes.MACHINE_PRECASS.get(), pos, blockState, 22);

        this.inputTank = new FluidTank(Fluids.NONE, 4_000);
        this.outputTank = new FluidTank(Fluids.NONE, 4_000);

        this.assemblerModule = new ModuleMachinePrecAss(0, this, slots)
                .itemInput(4).itemOutput(13)
                .fluidInput(inputTank).fluidOutput(outputTank);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machinePrecAss");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(maxPower <= 0) this.maxPower = 1_000_000;

        if(!this.level.isClientSide) {

            GenericRecipe recipe = PrecAssRecipes.INSTANCE.recipeNameMap.get(assemblerModule.recipe);
            if(recipe != null) this.maxPower = recipe.power * 100;
            this.maxPower = BobMathUtil.max(this.power, this.maxPower, 100_000);

            this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);
            upgradeManager.checkSlots(slots, 2, 3);

            for(DirPos pos : getConPos()) {
                this.trySubscribe(level, pos);
                if(inputTank.getTankType() != Fluids.NONE) this.trySubscribe(inputTank.getTankType(), level, pos);
                if(outputTank.getFill() > 0) this.tryProvide(outputTank, level, pos);
            }

            double speed = 1D;
            double pow = 1D;

            speed += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) / 3D;
            speed += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3);

            pow -= Math.min(upgradeManager.getLevel(UpgradeType.POWER), 3) * 0.25D;
            pow += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) * 1D;
            pow += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3) * 10D / 3D;

            this.assemblerModule.update(speed, pow, true, slots.get(1));
            this.didProcess = this.assemblerModule.didProcess;
            if(this.assemblerModule.markDirty) this.setChanged();

            this.networkPackNT(100);

        } else {

            if(this.level.getGameTime() % 20 == 0) {
                this.frame = !this.level.getBlockState(this.worldPosition.above(3)).isAir();
            }

            if(this.didProcess && Math.sqrt(NuclearTechMod.proxy.me().distanceToSqr(this.getBlockPos().getBottomCenter())) < 50) {
                if(audio == null) {
                    audio = createAudioLoop();
                    audio.startSound();
                } else if(!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }
                audio.keepAlive();
                audio.updatePitch(0.75F);
                audio.updateVolume(this.getVolume(0.5F));

            } else if(audio != null) {
                audio.stopSound();
                audio = null;
            }

            this.animate();
        }
    }

    /** Die ganze Bewegung der Client-Seite: Ring, Arme, Schlagbolzen. */
    private void animate() {

        System.arraycopy(this.armAngles, 0, this.prevArmAngles, 0, this.armAngles.length);
        System.arraycopy(this.strikers, 0, this.prevStrikers, 0, this.strikers.length);
        this.prevRing = this.ring;

        for(int i = 0; i < 4; i++) {
            if(this.strikerDir[i]) {
                this.strikers[i] = -0.75F;
                this.strikerDir[i] = false;
                if(!this.muffled) NuclearTechMod.proxy.playLocalSound(this.getBlockPos().getCenter(), NtmSoundEvents.ASSEMBLER_STRIKE.get(), SoundSource.BLOCKS, this.getVolume(0.5F), 1.25F);
            } else {
                this.strikers[i] = Mth.clamp(this.strikers[i] + 0.5F, -0.75F, 0F);
            }
        }

        if(this.ring != this.ringTarget) {
            float ringDelta = Math.abs(this.ringTarget - this.ring);
            if(ringDelta <= this.ringSpeed) this.ring = this.ringTarget;
            if(this.ringTarget > this.ring) this.ring += this.ringSpeed;
            if(this.ringTarget < this.ring) this.ring -= this.ringSpeed;
            if(this.ringTarget == this.ring) {
                float sub = this.ringTarget >= 360F ? -360F : 360F;
                this.ringTarget += sub;
                this.ring += sub;
                this.prevRing += sub;
                this.ringDelay = 100 + this.level.random.nextInt(21);
            }
        }

        if(this.didProcess) {

            if(this.ring == this.ringTarget) {
                if(this.ringDelay > 0) this.ringDelay--;
                if(this.ringDelay <= 0) {
                    this.ringTarget += 45F * (this.level.random.nextBoolean() ? -1 : 1);
                    this.ringSpeed = 10F + this.level.random.nextFloat() * 5F;
                    if(!this.muffled) NuclearTechMod.proxy.playLocalSound(this.getBlockPos().getCenter(), NtmSoundEvents.ASSEMBLER_START.get(), SoundSource.BLOCKS, this.getVolume(0.25F), 1.25F + this.level.random.nextFloat() * 0.25F);
                }
            }

            if(!isInWorkingPosition(this.armAngles) && canArmsMove()) move(WORKING_POSITION);

            if(isInWorkingPosition(this.armAngles)) {
                this.strikerDelay--;
                if(this.strikerDelay <= 0) {
                    this.strikerDir[this.strikerIndex] = true;
                    this.strikerIndex = (this.strikerIndex + 1) % this.strikers.length;
                    this.strikerDelay = this.strikerIndex == 3 ? (10 + this.level.random.nextInt(3)) : 2;
                }
            }

        } else {
            /* Alle Bolzen einfahren, dann zurueck in die Ruhelage. */
            for(int i = 0; i < 4; i++) this.strikerDir[i] = false;
            if(canArmsMove()) move(NULL_POSITION);
        }

        if(isInWorkingPosition(this.prevArmAngles) && !isInWorkingPosition(this.armAngles) && !this.muffled) {
            NuclearTechMod.proxy.playLocalSound(this.getBlockPos().getCenter(), NtmSoundEvents.ASSEMBLER_STOP.get(), SoundSource.BLOCKS, this.getVolume(0.25F), 1.25F + this.level.random.nextFloat() * 0.25F);
        }
    }

    /** Solange ein Bolzen draussen ist, bleiben die Arme stehen -- sonst schleiften sie durch. */
    private boolean canArmsMove() {
        for(int i = 0; i < 4; i++) if(this.strikers[i] != 0F) return false;
        return true;
    }

    private static boolean isInWorkingPosition(float[] arms) {
        for(int i = 0; i < 3; i++) if(arms[i] != WORKING_POSITION[i]) return false;
        return true;
    }

    private void move(float[] targetAngles) {

        for(int i = 0; i < this.armAngles.length; i++) {
            if(this.armAngles[i] == targetAngles[i]) continue;

            float turn = 15F;
            float delta = Math.abs(this.armAngles[i] - targetAngles[i]);

            if(delta <= turn) { this.armAngles[i] = targetAngles[i]; continue; }
            if(this.armAngles[i] < targetAngles[i]) this.armAngles[i] += turn;
            else this.armAngles[i] -= turn;
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return AudioWrapper.getLoopedSound(NtmSoundEvents.ELECTRIC_MOTOR_LOOP.get(), SoundSource.BLOCKS, this, 0.5F, 15F, 0.75F, 20);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if(audio != null) { audio.stopSound(); audio = null; }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(audio != null) { audio.stopSound(); audio = null; }
    }

    public DirPos[] getConPos() {

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + 2, y, z - 1, Library.POS_X),
                new DirPos(x + 2, y, z + 0, Library.POS_X),
                new DirPos(x + 2, y, z + 1, Library.POS_X),
                new DirPos(x - 2, y, z - 1, Library.NEG_X),
                new DirPos(x - 2, y, z + 0, Library.NEG_X),
                new DirPos(x - 2, y, z + 1, Library.NEG_X),
                new DirPos(x - 1, y, z + 2, Library.POS_Z),
                new DirPos(x + 0, y, z + 2, Library.POS_Z),
                new DirPos(x + 1, y, z + 2, Library.POS_Z),
                new DirPos(x - 1, y, z - 2, Library.NEG_Z),
                new DirPos(x + 0, y, z - 2, Library.NEG_Z),
                new DirPos(x + 1, y, z - 2, Library.NEG_Z),
        };
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.inputTank.serialize(buf);
        this.outputTank.serialize(buf);
        buf.writeLong(power);
        buf.writeLong(maxPower);
        buf.writeBoolean(didProcess);
        this.assemblerModule.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.inputTank.deserialize(buf);
        this.outputTank.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.didProcess = buf.readBoolean();
        this.assemblerModule.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.inputTank.readFromNBT(tag, "i");
        this.outputTank.readFromNBT(tag, "o");
        this.power = tag.getLong("power");
        this.maxPower = tag.getLong("maxPower");
        this.assemblerModule.readFromNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.inputTank.writeToNBT(tag, "i");
        this.outputTank.writeToNBT(tag, "o");
        tag.putLong("power", power);
        tag.putLong("maxPower", maxPower);
        this.assemblerModule.writeToNBT(tag);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == 0) return true;                                                          // Batterie
        if(slot == 1 && stack.getItem() == NtmItems.BLUEPRINTS.get()) return true;          // Blaupause
        if(slot >= 2 && slot <= 3 && stack.getItem() instanceof MachineUpgradeItem) return true;
        return this.assemblerModule.isItemValid(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= 13 || this.assemblerModule.isSlotClogged(index);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21 };
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { inputTank }; }
    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { outputTank }; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] { inputTank, outputTank }; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachinePrecAssMenu(id, inventory, this);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("index") && tag.contains("selection")) {
            int index = tag.getInt("index");
            String selection = tag.getString("selection");
            if(index == 0) {
                this.assemblerModule.recipe = selection;
                this.setChanged();
            }
        }
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {
        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_PRECASS.get()));
        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_SPEED, "+" + (lvl * 100 / 3) + "%").withStyle(ChatFormatting.GREEN));
            components.add(Component.translatable(KEY_CONSUMPTION, "+" + (lvl * 50) + "%").withStyle(ChatFormatting.RED));
        }
        if(type == UpgradeType.POWER) {
            components.add(Component.translatable(KEY_CONSUMPTION, "-" + (lvl * 25) + "%").withStyle(ChatFormatting.GREEN));
        }
        if(type == UpgradeType.OVERDRIVE) {
            components.add(Component.translatable(KEY_YES).withStyle(BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        upgrades.put(UpgradeType.POWER, 3);
        upgrades.put(UpgradeType.OVERDRIVE, 3);
        return upgrades;
    }
}
