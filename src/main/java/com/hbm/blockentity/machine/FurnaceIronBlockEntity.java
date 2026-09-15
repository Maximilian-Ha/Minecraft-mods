package com.hbm.blockentity.machine;

import com.hbm.blockentity.IUpgradeInfoProvider;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.blocks.NtmBlocks;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.menus.FurnaceIronMenu;
import com.hbm.items.machine.MachineUpgradeItem;
import com.hbm.items.machine.MachineUpgradeItem.UpgradeType;
import com.hbm.module.ModuleBurnTime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFurnaceIron.
 *
 * Eisenofen: verbrennt Festbrennstoff und schmilzt nach den normalen Ofenrezepten.
 * Kennzahlen, Slot-Belegung und Upgrade-Rechnung sind unveraendert uebernommen.
 *
 * Weggelassen: IConfigurableMachine (gibt es im Port nicht) -- die Kennzahlen stehen fest
 * im Quelltext. // todo config
 */
public class FurnaceIronBlockEntity extends MachineBaseBlockEntity implements IUpgradeInfoProvider {

    /** Ticks pro Schmelzvorgang ohne Upgrades. */
    public static final int BASE_TIME = 160;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL_1 = 1;
    public static final int SLOT_FUEL_2 = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_UPGRADE = 4;

    private static final int[] ACCESSIBLE_SLOTS = new int[] { SLOT_INPUT, SLOT_FUEL_1, SLOT_FUEL_2, SLOT_OUTPUT };

    /** Brennwert-Boni wie im Original. Statisch, damit die Oberflaeche sie ohne Instanz lesen kann. */
    public static final ModuleBurnTime BURN_MODULE = new ModuleBurnTime()
            .setLigniteTimeMod(1.25D)
            .setCoalTimeMod(1.25D)
            .setCokeTimeMod(1.5D)
            .setSolidTimeMod(2D)
            .setRocketTimeMod(2D)
            .setBalefireTimeMod(2D);

    public int maxBurnTime;
    public int burnTime;
    public boolean wasOn = false;

    public int progress;
    public int processingTime = BASE_TIME;

    public final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

    private AABB renderBox;

    public FurnaceIronBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FURNACE_IRON.get(), pos, state, 5);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.furnaceIron");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(this.level.isClientSide) {
            this.spawnWorkingParticles(this.level);
            return;
        }

        this.upgradeManager.checkSlots(this.slots, SLOT_UPGRADE, SLOT_UPGRADE);
        this.processingTime = BASE_TIME - ((BASE_TIME / 2) * this.upgradeManager.getLevel(UpgradeType.SPEED) / 3);

        this.wasOn = false;

        if(this.burnTime <= 0) {

            for(int i = SLOT_FUEL_1; i < SLOT_OUTPUT; i++) {
                ItemStack fuel = this.slots.get(i);
                if(fuel.isEmpty()) continue;

                int burn = BURN_MODULE.getBurnTime(fuel);

                if(burn > 0) {
                    this.maxBurnTime = this.burnTime = burn;
                    ItemStack remainder = fuel.hasCraftingRemainingItem() ? fuel.getCraftingRemainingItem().copy() : ItemStack.EMPTY;
                    fuel.shrink(1);
                    if(fuel.isEmpty()) this.slots.set(i, remainder);
                    break;
                }
            }
        }

        // Die Rezeptsuche laeuft linear ueber alle Schmelzrezepte, darum nur einmal pro Tick.
        ItemStack result = this.getSmeltingResult(this.slots.get(SLOT_INPUT));

        if(this.canSmelt(result)) {
            this.wasOn = true;
            this.progress++;
            this.burnTime--;

            if(this.progress % 15 == 0 && !this.muffled) {
                this.level.playSound(null, this.worldPosition, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F, 0.5F + this.level.random.nextFloat() * 0.5F);
            }

            if(this.progress >= this.processingTime) {
                ItemStack output = this.slots.get(SLOT_OUTPUT);

                if(output.isEmpty()) {
                    this.slots.set(SLOT_OUTPUT, result.copy());
                } else {
                    output.grow(result.getCount());
                }

                this.removeItem(SLOT_INPUT, 1);

                this.progress = 0;
                this.setChanged();
            }

            if(this.level.getGameTime() % 20 == 0) {
                PollutionHandler.incrementPollution(this.level, this.worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);
            }
        } else {
            this.progress = 0;
        }

        this.networkPackNT(50);
    }

    /** Rauch aus dem Schornstein und Flammen an der Feuerklappe, wie im Original. */
    private void spawnWorkingParticles(Level level) {
        if(this.progress <= 0) return;

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise();

        double offset = this.progress % 2 == 0 ? 1 : 0.5;
        level.addParticle(ParticleTypes.SMOKE,
                this.worldPosition.getX() + 0.5 - dir.getStepX() * offset - rot.getStepX() * 0.1875,
                this.worldPosition.getY() + 2,
                this.worldPosition.getZ() + 0.5 - dir.getStepZ() * offset - rot.getStepZ() * 0.1875,
                0.0, 0.01, 0.0);

        if(this.progress % 5 == 0) {
            double rand = level.random.nextDouble();
            level.addParticle(ParticleTypes.FLAME,
                    this.worldPosition.getX() + 0.5 + dir.getStepX() * 0.25 + rot.getStepX() * rand,
                    this.worldPosition.getY() + 0.25 + level.random.nextDouble() * 0.25,
                    this.worldPosition.getZ() + 0.5 + dir.getStepZ() * 0.25 + rot.getStepZ() * rand,
                    0.0, 0.0, 0.0);
        }
    }

    /** Schmelzergebnis nach den normalen Ofenrezepten, sonst ItemStack.EMPTY. */
    private ItemStack getSmeltingResult(ItemStack input) {
        if(this.level == null || input.isEmpty()) return ItemStack.EMPTY;

        SingleRecipeInput recipeInput = new SingleRecipeInput(input.copy());

        for(var recipeHolder : this.level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
            var recipe = recipeHolder.value();
            if(!recipe.matches(recipeInput, this.level)) continue;

            return recipe.getResultItem(this.level.registryAccess()).copy();
        }

        return ItemStack.EMPTY;
    }

    private boolean canSmelt(ItemStack result) {
        if(this.burnTime <= 0) return false;
        if(this.slots.get(SLOT_INPUT).isEmpty()) return false;
        if(result.isEmpty()) return false;

        ItemStack output = this.slots.get(SLOT_OUTPUT);
        if(output.isEmpty()) return true;

        if(!ItemStack.isSameItemSameComponents(output, result)) return false;

        return result.getCount() + output.getCount() <= output.getMaxStackSize();
    }

    /** Nur fuer die Oberflaeche: zeigt den Glut-Overlay an, wenn geschmolzen werden kann. */
    public boolean isSmelting() {
        return this.wasOn;
    }

    public int getProgressScaled(int pixels) {
        return this.progress * pixels / Math.max(this.processingTime, 1);
    }

    public int getBurnTimeScaled(int pixels) {
        return this.burnTime * pixels / Math.max(this.maxBurnTime, 1);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_INPUT) return !this.getSmeltingResult(stack).isEmpty();
        if(slot == SLOT_FUEL_1 || slot == SLOT_FUEL_2) return BURN_MODULE.getBurnTime(stack) > 0;
        if(slot == SLOT_UPGRADE) {
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
        return ACCESSIBLE_SLOTS;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.maxBurnTime = tag.getInt("maxBurnTime");
        this.burnTime = tag.getInt("burnTime");
        this.progress = tag.getInt("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("maxBurnTime", this.maxBurnTime);
        tag.putInt("burnTime", this.burnTime);
        tag.putInt("progress", this.progress);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.maxBurnTime);
        buf.writeInt(this.burnTime);
        buf.writeInt(this.progress);
        buf.writeInt(this.processingTime);
        buf.writeBoolean(this.wasOn);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.maxBurnTime = buf.readInt();
        this.burnTime = buf.readInt();
        this.progress = buf.readInt();
        this.processingTime = buf.readInt();
        this.wasOn = buf.readBoolean();
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 1, y, z - 1, x + 2, y + 3, z + 2);
        }
        return this.renderBox;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FurnaceIronMenu(id, inventory, this);
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int lvl, TooltipFlag flag) {
        return type == UpgradeType.SPEED;
    }

    @Override
    public void provideInfo(UpgradeType type, int lvl, List<Component> components, TooltipFlag flag) {
        components.add(IUpgradeInfoProvider.getStandardLabel(NtmBlocks.FURNACE_IRON.get()));
        if(type == UpgradeType.SPEED) {
            components.add(Component.translatable(KEY_DELAY, "-" + (lvl * 50 / 3) + "%").withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public HashMap<UpgradeType, Integer> getValidUpgrades() {
        HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(UpgradeType.SPEED, 3);
        return upgrades;
    }
}
