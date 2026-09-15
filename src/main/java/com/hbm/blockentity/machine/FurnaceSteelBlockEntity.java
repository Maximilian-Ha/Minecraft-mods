package com.hbm.blockentity.machine;

import api.hbm.tile.IHeatSource;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.menus.FurnaceSteelMenu;
import com.hbm.util.ItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Locale;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityFurnaceSteel.
 *
 * Stahlofen: drei parallele Schmelzbahnen, betrieben mit Waerme (TU) aus einem
 * IHeatSource direkt unter dem Kernblock. Kennzahlen unveraendert.
 *
 * Weggelassen:
 * - com.hbm.util.BufferUtil.writeIntArray/readIntArray: hier durch feste Schleifen ueber
 *   die drei Bahnen ersetzt, das Paket bleibt dadurch sogar kuerzer.
 * - Der Bonus "anyTar" des Erzwoerterbuchs hat im Port keine Entsprechung; Erz und
 *   Holzstamm sind ueber Tags abgebildet.
 */
public class FurnaceSteelBlockEntity extends MachineBaseBlockEntity {

    /** Annahme des Originals: Vanilla-Ofen, 200 Ticks Kohlefeuer bei 200 HU/t. */
    public static final int PROCESS_TIME = 40_000;
    public static final int MAX_HEAT = 100_000;
    public static final double DIFFUSION = 0.05D;

    /** Drei Bahnen: Eingang 0-2, zugehoeriger Ausgang jeweils +3. */
    public static final int LANES = 3;

    public int[] progress = new int[LANES];
    public int[] bonus = new int[LANES];

    public int heat;
    public boolean wasOn = false;

    private final NonNullList<ItemStack> lastItems = NonNullList.withSize(LANES, ItemStack.EMPTY);

    private AABB renderBox;

    public FurnaceSteelBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FURNACE_STEEL.get(), pos, state, 6);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.furnaceSteel");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(this.level.isClientSide) {
            this.spawnWorkingParticles(this.level);
            return;
        }

        this.tryPullHeat();

        this.wasOn = false;

        int burn = (this.heat - MAX_HEAT / 3) / 10;

        for(int i = 0; i < LANES; i++) {

            ItemStack input = this.slots.get(i);

            if(input.isEmpty() || this.lastItems.get(i).isEmpty() || !ItemStack.isSameItemSameComponents(input, this.lastItems.get(i))) {
                this.progress[i] = 0;
                this.bonus[i] = 0;
            }

            ItemStack result = this.getSmeltingResult(input);

            if(this.canSmelt(i, result)) {
                this.progress[i] += burn;
                this.heat -= burn;
                this.wasOn = true;
                if(this.level.getGameTime() % 20 == 0) {
                    PollutionHandler.incrementPollution(this.level, this.worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 2);
                }
            }

            this.lastItems.set(i, input.copy());

            if(this.progress[i] >= PROCESS_TIME && !result.isEmpty()) {

                ItemStack output = this.slots.get(i + LANES);

                if(output.isEmpty()) {
                    this.slots.set(i + LANES, result.copy());
                } else {
                    output.grow(result.getCount());
                }

                this.addBonus(input, i);

                ItemStack out = this.slots.get(i + LANES);
                while(this.bonus[i] >= 100) {
                    out.setCount(Math.min(out.getMaxStackSize(), out.getCount() + result.getCount()));
                    this.bonus[i] -= 100;
                }

                this.removeItem(i, 1);

                this.progress[i] = 0;
                this.setChanged();
            }
        }

        this.networkPackNT(50);
    }

    /** Rauchfahne, Dampfwolken und Lavafunken, wie im Original. */
    private void spawnWorkingParticles(Level level) {
        if(!this.wasOn) return;

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise();

        level.addParticle(ParticleTypes.SMOKE,
                this.worldPosition.getX() + 0.5 - dir.getStepX() * 1.125 - rot.getStepX() * 0.75,
                this.worldPosition.getY() + 2.625,
                this.worldPosition.getZ() + 0.5 - dir.getStepZ() * 1.125 - rot.getStepZ() * 0.75,
                0.0, 0.05, 0.0);

        if(level.random.nextInt(20) == 0) {
            level.addParticle(ParticleTypes.CLOUD,
                    this.worldPosition.getX() + 0.5 + dir.getStepX() * 0.75,
                    this.worldPosition.getY() + 2,
                    this.worldPosition.getZ() + 0.5 + dir.getStepZ() * 0.75,
                    0.0, 0.05, 0.0);
        }

        if(level.random.nextInt(15) == 0) {
            level.addParticle(ParticleTypes.LAVA,
                    this.worldPosition.getX() + 0.5 + dir.getStepX() * 1.5 + rot.getStepX() * (level.random.nextDouble() - 0.5),
                    this.worldPosition.getY() + 0.75,
                    this.worldPosition.getZ() + 0.5 + dir.getStepZ() * 1.5 + rot.getStepZ() * (level.random.nextDouble() - 0.5),
                    dir.getStepX() * 0.5D, 0.05, dir.getStepZ() * 0.5D);
        }
    }

    /**
     * Bonusausbeute: Erze geben 25%, Holzstaemme 50%. Im Original ueber das Erzwoerterbuch
     * ("ore*", "log*", "anyTar"), hier ueber die Tag-Namen des Stapels.
     */
    protected void addBonus(ItemStack stack, int index) {
        List<String> tags = ItemStackUtil.getTags(stack);

        for(String tag : tags) {
            String lower = tag.toLowerCase(Locale.US);
            int colon = lower.indexOf(':');
            String path = colon >= 0 ? lower.substring(colon + 1) : lower;

            if(path.startsWith("ores")) { this.bonus[index] += 25; return; }
            if(path.startsWith("logs")) { this.bonus[index] += 50; return; }
        }
    }

    /** Zieht Waerme aus einer IHeatSource direkt unter dem Kernblock. */
    protected void tryPullHeat() {
        if(this.level == null) return;
        if(this.heat >= MAX_HEAT) return;

        BlockEntity be = this.level.getBlockEntity(this.worldPosition.below());

        if(be instanceof IHeatSource source) {
            int diff = source.getHeatStored() - this.heat;

            if(diff == 0) return;

            if(diff > 0) {
                diff = (int) Math.ceil(diff * DIFFUSION);
                source.useUpHeat(diff);
                this.heat += diff;
                if(this.heat > MAX_HEAT) this.heat = MAX_HEAT;
                return;
            }
        }

        this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
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

    public boolean canSmelt(int index, ItemStack result) {
        if(this.heat < MAX_HEAT / 3) return false;
        if(this.slots.get(index).isEmpty()) return false;
        if(result.isEmpty()) return false;

        ItemStack output = this.slots.get(index + LANES);
        if(output.isEmpty()) return true;

        if(!ItemStack.isSameItemSameComponents(output, result)) return false;

        return result.getCount() + output.getCount() <= output.getMaxStackSize();
    }

    public int getProgressScaled(int index, int pixels) {
        return this.progress[index] * pixels / PROCESS_TIME;
    }

    public int getBonusScaled(int index, int pixels) {
        return this.bonus[index] * pixels / 100;
    }

    public int getHeatScaled(int pixels) {
        return this.heat * pixels / MAX_HEAT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot < LANES) return !this.getSmeltingResult(stack).isEmpty();
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= LANES;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { 0, 1, 2, 3, 4, 5 };
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        int[] storedProgress = tag.getIntArray("progress");
        int[] storedBonus = tag.getIntArray("bonus");
        for(int i = 0; i < LANES; i++) {
            this.progress[i] = i < storedProgress.length ? storedProgress[i] : 0;
            this.bonus[i] = i < storedBonus.length ? storedBonus[i] : 0;
        }

        this.heat = tag.getInt("heat");

        for(int i = 0; i < LANES; i++) this.lastItems.set(i, ItemStack.EMPTY);
        if(tag.contains("lastItems")) {
            ContainerHelper.loadAllItems(tag.getCompound("lastItems"), this.lastItems, registries);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putIntArray("progress", this.progress);
        tag.putIntArray("bonus", this.bonus);
        tag.putInt("heat", this.heat);

        CompoundTag lastTag = new CompoundTag();
        ContainerHelper.saveAllItems(lastTag, this.lastItems, registries);
        tag.put("lastItems", lastTag);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        // Ersetzt BufferUtil.writeIntArray des Originals: feste Laenge, darum reicht die Schleife.
        for(int i = 0; i < LANES; i++) buf.writeInt(this.progress[i]);
        for(int i = 0; i < LANES; i++) buf.writeInt(this.bonus[i]);
        buf.writeInt(this.heat);
        buf.writeBoolean(this.wasOn);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(int i = 0; i < LANES; i++) this.progress[i] = buf.readInt();
        for(int i = 0; i < LANES; i++) this.bonus[i] = buf.readInt();
        this.heat = buf.readInt();
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
        return new FurnaceSteelMenu(id, inventory, this);
    }
}
