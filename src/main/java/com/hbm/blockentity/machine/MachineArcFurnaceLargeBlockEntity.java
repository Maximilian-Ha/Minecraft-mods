package com.hbm.blockentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.menus.MachineArcFurnaceLargeMenu;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.inventory.recipes.ArcFurnaceRecipes.ArcFurnaceRecipe;
import com.hbm.items.machine.ArcElectrodeItem;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.NuclearTechMod;
import com.hbm.particle.NtmParticleTypes;
import com.hbm.particle.helper.FoundryCreator;
import com.hbm.particle.vanilla.NbtParticleOptions;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.BobMathUtil;
import com.hbm.util.CrucibleUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.particle.ParticleUtil;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge.
 *
 * Der grosse Lichtbogenofen. Drei Elektroden, ein Deckel, der sich zum Beschicken hebt, und zwei
 * Betriebsarten: fest, wo aus Gegenstaenden Gegenstaende werden, und fluessig, wo sie zu Material
 * eingeschmolzen werden. Was fluessig im Ofen steht, laeuft als Strahl aus dem Ausguss, sobald der
 * Deckel offen ist -- getroffen wird, was darunter steht und ICrucibleAcceptor ist.
 *
 * ABWEICHUNG: distributeInput, collectRequested und getAvailableItemFromSlot des Originals sind
 * nicht uebernommen. Sie bedienen ausschliesslich die AE2-Anbindung (ArcFurnaceLargeMEInventory),
 * die der Port nicht hat -- sie waeren toter Code.
 */
public class MachineArcFurnaceLargeBlockEntity extends MachineBaseBlockEntity implements IEnergyReceiverMK2, IControlReceiver, IUpgradeInfoProvider {

    public long power;
    public static final long maxPower = 2_500_000;
    public boolean liquidMode = false;
    public float progress;
    public boolean isProgressing;
    public boolean hasMaterial;
    public int delay;
    public int upgrade;

    public float lid;
    public float prevLid;
    public int approachNum;
    public float syncLid;

    private AudioWrapper audioLid;
    private AudioWrapper audioProgress;

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    public byte[] electrodes = new byte[3];
    public static final byte ELECTRODE_NONE = 0;
    public static final byte ELECTRODE_FRESH = 1;
    public static final byte ELECTRODE_USED = 2;
    public static final byte ELECTRODE_DEPLETED = 3;

    public static final int maxLiquid = MaterialShapes.BLOCK.q(128);
    public List<MaterialStack> liquids = new ArrayList<>();

    private AABB renderBox;

    public MachineArcFurnaceLargeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_ARC_FURNACE.get(), pos, state, 30);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineArcFurnaceLarge");
    }

    /** Wieviel von einer Sorte hoechstens in einem Eingabefach liegen darf. */
    public int getMaxInputSize() {
        return this.upgrade == 0 ? 1 : this.upgrade == 1 ? 4 : this.upgrade == 2 ? 8 : 16;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);

        if(this.level != null && index == 4 && stack.getItem() instanceof MachineUpgradeItem) {
            this.level.playSound(null, this.worldPosition, NtmSoundEvents.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        this.upgradeManager.checkSlots(this.slots, 4, 4);
        this.upgrade = this.upgradeManager.getLevel(UpgradeType.SPEED);

        if(!this.level.isClientSide) {

            this.power = Library.chargeTEFromItems(this.slots, 3, this.power, maxPower);
            this.isProgressing = false;

            for(DirPos pos : this.getConPos()) this.trySubscribe(this.level, pos);

            if(this.lid > 0) this.loadIngredients();

            if(this.power > 0) {

                boolean ingredients = this.hasIngredients();
                boolean hasElectrodes = this.hasElectrodes();

                int consumption = (int) (1_000 * Math.pow(5, this.upgrade));

                if(ingredients && hasElectrodes && this.delay <= 0 && this.liquids.isEmpty()) {

                    if(this.lid > 0) {
                        // erst schliesst der Deckel, dann faengt der Ofen an
                        this.lid -= 1F / (60F / (this.upgrade * 0.5F + 1F));
                        if(this.lid < 0) this.lid = 0;
                        this.progress = 0;
                    } else if(this.power >= consumption) {

                        int duration = 400 / (this.upgrade * 2 + 1);
                        this.progress += 1F / duration;
                        this.isProgressing = true;
                        this.power -= consumption;

                        if(this.progress >= 1F) {
                            this.process();
                            this.progress = 0;
                            this.setChanged();
                            this.delay = (int) (120 / (this.upgrade * 0.5 + 1));
                            PollutionHandler.incrementPollution(this.level, this.worldPosition, PollutionType.SOOT, 10F);
                        }
                    }
                } else {
                    if(this.delay > 0) this.delay--;
                    this.progress = 0;
                    if(this.lid < 1) {
                        this.lid += 1F / (60F / (this.upgrade * 0.5F + 1F));
                        if(this.lid > 1) this.lid = 1;
                    }
                }

                this.hasMaterial = ingredients;
            }

            this.decideElectrodeState();

            if(!this.hasMaterial) this.hasMaterial = this.hasIngredients();

            if(!this.liquids.isEmpty() && this.lid > 0F) {

                Direction dir = this.getPointing();

                Vec3[] impact = new Vec3[1];
                double spoutX = this.worldPosition.getX() + 0.5D + dir.getStepX() * 2.875D;
                double spoutZ = this.worldPosition.getZ() + 0.5D + dir.getStepZ() * 2.875D;

                MaterialStack didPour = CrucibleUtil.pourFullStack(this.level, spoutX, this.worldPosition.getY() + 1.25D, spoutZ,
                        6, true, this.liquids, MaterialShapes.INGOT.q(1), impact);

                if(didPour != null) {
                    float length = 1F;
                    if(impact[0] != null) length = (float) Math.max(1D, this.worldPosition.getY() + 1 - (Math.ceil(impact[0].y) - 0.875D));

                    FoundryCreator.composeEffect(this.level, spoutX, this.worldPosition.getY() + 1, spoutZ,
                            didPour.material.moltenColor, dir, length, 0.625F, 0.625F);
                }
            }

            this.liquids.removeIf(o -> o.amount <= 0);

            this.networkPackNT(150);

        } else {
            this.clientUpdate();
        }
    }

    private void clientUpdate() {

        this.prevLid = this.lid;

        if(this.approachNum > 0) {
            this.lid = this.lid + ((this.syncLid - this.lid) / this.approachNum);
            this.approachNum--;
        } else {
            this.lid = this.syncLid;
        }

        if(this.lid != this.prevLid) {
            if(this.audioLid == null) {
                this.audioLid = AudioWrapper.getLoopedSound(NtmSoundEvents.WGH_START.get(), SoundSource.BLOCKS, this, this.getVolume(0.75F), 15F, 1.0F, 5);
                this.audioLid.startSound();
            } else if(!this.audioLid.isPlaying()) {
                this.audioLid.startSound();
            }
            this.audioLid.keepAlive();
        } else if(this.audioLid != null) {
            this.audioLid.stopSound();
            this.audioLid = null;
        }

        // der Anschlag am Ende der Bewegung, aber nicht beim Sprung von zu auf offen
        if((this.lid == 1 || this.lid == 0) && this.lid != this.prevLid && !(this.prevLid == 0 && this.lid == 1)) {
            this.level.playLocalSound(this.worldPosition, NtmSoundEvents.WGH_STOP.get(), SoundSource.BLOCKS, this.getVolume(1F), 1F, false);
        }

        if(this.isProgressing) {
            if(this.audioProgress == null) {
                this.audioProgress = AudioWrapper.getLoopedSound(NtmSoundEvents.ELECTRIC_HUM.get(), SoundSource.BLOCKS, this, this.getVolume(1.5F), 15F, 0.75F, 5);
                this.audioProgress.startSound();
            } else if(!this.audioProgress.isPlaying()) {
                this.audioProgress.startSound();
            }
            this.audioProgress.updatePitch(0.75F);
            this.audioProgress.keepAlive();
        } else if(this.audioProgress != null) {
            this.audioProgress.stopSound();
            this.audioProgress = null;
        }

        if(NuclearTechMod.proxy.me() == null) return;
        boolean near = NuclearTechMod.proxy.me().distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 4, this.worldPosition.getZ() + 0.5) < 2500;
        if(!near) return;

        // Rauchwolke, wenn der Deckel aufgeht
        if(this.lid != this.prevLid && this.lid > this.prevLid && !(this.prevLid == 0 && this.lid == 1)) {

            CompoundTag data = new CompoundTag();
            data.putFloat("lift", 0.01F);
            data.putFloat("base", 0.5F);
            data.putFloat("max", 2F);
            data.putInt("life", 70 + this.level.random.nextInt(30));
            data.putBoolean("noWind", true);
            data.putFloat("alpha", this.prevLid / this.lid);
            data.putInt("color", 0x000000);
            data.putFloat("strafe", 0.05F);

            for(int i = 0; i < 3; i++) {
                ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.COOLING_TOWER.get(), data),
                        this.worldPosition.getX() + 0.5 + this.level.random.nextGaussian() * 0.5,
                        this.worldPosition.getY() + 4,
                        this.worldPosition.getZ() + 0.5 + this.level.random.nextGaussian() * 0.5);
            }
        }

        // Flammen, wenn der Deckel ueber heissem Gut zugeht
        if(this.lid != this.prevLid && this.lid < this.prevLid && this.lid > 0.5F && this.hasMaterial && this.level.random.nextInt(5) == 0) {

            CompoundTag flame = new CompoundTag();
            flame.putInt("maxAge", 50);

            for(int i = 0; i < 2; i++) {
                ParticleUtil.addParticle(this.level, new NbtParticleOptions(NtmParticleTypes.RBMK_FLAME.get(), flame),
                        this.worldPosition.getX() + 0.5 + this.level.random.nextGaussian() * 0.5,
                        this.worldPosition.getY() + 2.75,
                        this.worldPosition.getZ() + 0.5 + this.level.random.nextGaussian() * 0.5);
            }
        }
    }

    /** Schiebt Gegenstaende aus der Eingabereihe in das Gitter. */
    public void loadIngredients() {

        boolean markDirty = false;

        for(int q /* Warteschlange */ = 25; q < 30; q++) {

            if(this.slots.get(q).isEmpty()) continue;

            ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(this.level, this.slots.get(q), this.liquidMode);
            if(recipe == null) continue;

            int max = this.getMaxInputSize();
            if(!this.liquidMode && recipe.solidOutput != null) {
                max = Math.min(max, this.slots.get(q).getMaxStackSize() / Math.max(1, recipe.solidOutput.getCount()));
            }

            // erst auf vorhandene Stapel verteilen
            for(int i /* Zutat */ = 5; i < 25; i++) {

                if(this.slots.get(q).isEmpty()) break;
                if(this.slots.get(i).isEmpty()) continue;
                if(!ItemStack.isSameItemSameComponents(this.slots.get(q), this.slots.get(i))) continue;

                int toMove = BobMathUtil.min(this.slots.get(i).getMaxStackSize() - this.slots.get(i).getCount(), this.slots.get(q).getCount(), max - this.slots.get(i).getCount());

                if(toMove > 0) {
                    this.removeItem(q, toMove);
                    this.slots.get(i).grow(toMove);
                    markDirty = true;
                }
            }

            // dann in leere Faecher
            for(int i = 5; i < 25; i++) {

                if(this.slots.get(q).isEmpty()) break;
                if(!this.slots.get(i).isEmpty()) continue;

                int toMove = Math.min(max, this.slots.get(q).getCount());
                ItemStack copy = this.slots.get(q).copy();
                copy.setCount(toMove);
                this.slots.set(i, copy);
                this.removeItem(q, toMove);
                markDirty = true;
            }
        }

        if(markDirty) this.setChanged();
    }

    public void decideElectrodeState() {

        for(int i = 0; i < 3; i++) {

            ItemStack stack = this.slots.get(i);

            if(stack.getItem() instanceof ArcElectrodeItem) {
                this.electrodes[i] = (this.isProgressing || ArcElectrodeItem.getDurability(stack) > 0) ? ELECTRODE_USED : ELECTRODE_FRESH;
            } else if(isBurntElectrode(stack)) {
                this.electrodes[i] = ELECTRODE_DEPLETED;
            } else {
                this.electrodes[i] = ELECTRODE_NONE;
            }
        }
    }

    /**
     * Die abgebrannten Elektroden sind im Port vier eigene Gegenstaende statt Untertypen.
     * Sie tragen dieselbe Bezeichnung wie die frischen, nur mit angehaengtem _burnt.
     */
    public static boolean isBurntElectrode(ItemStack stack) {
        if(stack.isEmpty()) return false;
        var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key.getPath().startsWith("arc_electrode_") && key.getPath().endsWith("_burnt");
    }

    /** Das abgebrannte Gegenstueck zu einer Elektrode. */
    public static ItemStack getBurntElectrode(ItemStack stack) {
        var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        var burnt = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath() + "_burnt"));
        return new ItemStack(burnt);
    }

    public void process() {

        for(int i = 5; i < 25; i++) {

            if(this.slots.get(i).isEmpty()) continue;

            ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(this.level, this.slots.get(i), this.liquidMode);
            if(recipe == null) continue;

            if(!this.liquidMode && recipe.solidOutput != null) {
                int amount = this.slots.get(i).getCount();
                ItemStack out = recipe.solidOutput.copy();
                out.setCount(out.getCount() * amount);
                this.slots.set(i, out);
            }

            if(this.liquidMode && recipe.fluidOutput != null) {

                while(!this.slots.get(i).isEmpty()) {

                    int liquid = getStackAmount(this.liquids);
                    int toAdd = getStackAmount(recipe.fluidOutput);

                    if(liquid + toAdd > maxLiquid) break;

                    this.removeItem(i, 1);
                    for(MaterialStack stack : recipe.fluidOutput) this.addToStack(stack);
                }
            }
        }

        for(int i = 0; i < 3; i++) {
            if(ArcElectrodeItem.damage(this.slots.get(i))) {
                this.slots.set(i, getBurntElectrode(this.slots.get(i)));
            }
        }
    }

    public boolean hasIngredients() {

        for(int i = 5; i < 25; i++) {

            if(this.slots.get(i).isEmpty()) continue;

            ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(this.level, this.slots.get(i), this.liquidMode);
            if(recipe == null) continue;

            if(this.liquidMode && recipe.fluidOutput != null) return true;
            if(!this.liquidMode && recipe.solidOutput != null) return true;
        }

        return false;
    }

    public boolean hasElectrodes() {
        for(int i = 0; i < 3; i++) {
            if(!(this.slots.get(i).getItem() instanceof ArcElectrodeItem)) return false;
        }
        return true;
    }

    public void addToStack(MaterialStack matStack) {

        for(MaterialStack mat : this.liquids) {
            if(mat.material == matStack.material) {
                mat.amount += matStack.amount;
                return;
            }
        }

        this.liquids.add(matStack.copy());
    }

    public static int getStackAmount(List<MaterialStack> stack) {
        int amount = 0;
        for(MaterialStack mat : stack) amount += mat.amount;
        return amount;
    }

    public static int getStackAmount(MaterialStack[] stack) {
        int amount = 0;
        for(MaterialStack mat : stack) amount += mat.amount;
        return amount;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] {
                0, 1, 2,
                5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
                25, 26, 27, 28, 29 };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        if(slot < 3) return stack.getItem() instanceof ArcElectrodeItem;
        if(slot >= 25) return ArcFurnaceRecipes.getOutput(this.level, stack, this.liquidMode) != null;
        return false;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {

        if(slot < 3) return stack.getItem() instanceof ArcElectrodeItem;

        if(slot > 4) {
            ArcFurnaceRecipe recipe = ArcFurnaceRecipes.getOutput(this.level, stack, this.liquidMode);
            if(recipe == null) return false;
            return this.liquidMode ? recipe.fluidOutput != null : recipe.solidOutput != null;
        }

        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        if(slot < 3) return this.lid >= 1 && !(stack.getItem() instanceof ArcElectrodeItem);
        if(slot > 4 && slot < 25) return this.lid > 0 && ArcFurnaceRecipes.getOutput(this.level, stack, this.liquidMode) == null;
        if(slot >= 25) return ArcFurnaceRecipes.getOutput(this.level, stack, this.liquidMode) == null;
        return false;
    }

    /** In welche Richtung der Ofen zeigt -- dorthin sitzt auch der Ausguss. */
    public Direction getPointing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
    }

    public DirPos[] getConPos() {

        Direction dir = this.getPointing();
        Direction rot = dir.getClockWise();

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x + dir.getStepX() * 3 + rot.getStepX(), y, z + dir.getStepZ() * 3 + rot.getStepZ(), dir),
                new DirPos(x + dir.getStepX() * 3 - rot.getStepX(), y, z + dir.getStepZ() * 3 - rot.getStepZ(), dir),
                new DirPos(x + rot.getStepX() * 3 + dir.getStepX(), y, z + rot.getStepZ() * 3 + dir.getStepZ(), rot),
                new DirPos(x + rot.getStepX() * 3 - dir.getStepX(), y, z + rot.getStepZ() * 3 - dir.getStepZ(), rot),
                new DirPos(x - rot.getStepX() * 3 + dir.getStepX(), y, z - rot.getStepZ() * 3 + dir.getStepZ(), rot.getOpposite()),
                new DirPos(x - rot.getStepX() * 3 - dir.getStepX(), y, z - rot.getStepZ() * 3 - dir.getStepZ(), rot.getOpposite())
        };
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeFloat(this.progress);
        buf.writeFloat(this.lid);
        buf.writeBoolean(this.isProgressing);
        buf.writeBoolean(this.liquidMode);
        buf.writeBoolean(this.hasMaterial);

        for(int i = 0; i < 3; i++) buf.writeByte(this.electrodes[i]);

        buf.writeShort(this.liquids.size());

        for(MaterialStack mat : this.liquids) {
            buf.writeInt(mat.material.id);
            buf.writeInt(mat.amount);
        }
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.progress = buf.readFloat();
        this.syncLid = buf.readFloat();
        this.isProgressing = buf.readBoolean();
        this.liquidMode = buf.readBoolean();
        this.hasMaterial = buf.readBoolean();

        for(int i = 0; i < 3; i++) this.electrodes[i] = buf.readByte();

        int mats = buf.readShort();

        this.liquids.clear();
        for(int i = 0; i < mats; i++) {
            var material = Mats.matById.get(buf.readInt());
            int amount = buf.readInt();
            if(material != null) this.liquids.add(new MaterialStack(material, amount));
        }

        if(this.syncLid != 0 && this.syncLid != 1) this.approachNum = 2;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.power = tag.getLong("power");
        this.liquidMode = tag.getBoolean("liquidMode");
        this.progress = tag.getFloat("progress");
        this.lid = tag.getFloat("lid");
        this.delay = tag.getInt("delay");

        int count = tag.getShort("count");
        this.liquids.clear();

        for(int i = 0; i < count; i++) {
            var material = Mats.matById.get(tag.getInt("m" + i));
            if(material != null) this.liquids.add(new MaterialStack(material, tag.getInt("a" + i)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putLong("power", this.power);
        tag.putBoolean("liquidMode", this.liquidMode);
        tag.putFloat("progress", this.progress);
        tag.putFloat("lid", this.lid);
        tag.putInt("delay", this.delay);

        int count = this.liquids.size();
        tag.putShort("count", (short) count);

        for(int i = 0; i < count; i++) {
            MaterialStack mat = this.liquids.get(i);
            tag.putInt("m" + i, mat.material.id);
            tag.putInt("a" + i, mat.amount);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.stopAudio();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.stopAudio();
    }

    private void stopAudio() {
        if(this.audioLid != null) { this.audioLid.stopSound(); this.audioLid = null; }
        if(this.audioProgress != null) { this.audioProgress.stopSound(); this.audioProgress = null; }
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineArcFurnaceLargeMenu(id, inventory, this);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag data) {
        if(data.getBoolean("liquid")) {
            this.liquidMode = !this.liquidMode;
            this.setChanged();
        }
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int level, TooltipFlag flag) {
        return type == UpgradeType.SPEED;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<Component> info, TooltipFlag flag) {
        info.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.MACHINE_ARC_FURNACE.get()));
        if(type == UpgradeType.SPEED) {
            info.add(Component.translatable(KEY_DELAY, "-" + (100 - 100 / (level * 2 + 1)) + "%").withStyle(ChatFormatting.GREEN));
            info.add(Component.translatable(KEY_CONSUMPTION, "+" + ((int) Math.pow(5, level) * 100 - 100) + "%").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        return upgrades;
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 3, y, z - 3, x + 4, y + 6, z + 4);
        }
        return this.renderBox;
    }
}
