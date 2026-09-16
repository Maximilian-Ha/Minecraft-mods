package com.zuxelus.energycontrol.blockentity;

import com.zuxelus.energycontrol.ECConfig;
import com.zuxelus.energycontrol.blocks.KitAssemblerBlock;
import com.zuxelus.energycontrol.energy.ECEnergyStorage;
import com.zuxelus.energycontrol.init.ECBlockEntityTypes;
import com.zuxelus.energycontrol.init.ECRecipes;
import com.zuxelus.energycontrol.menus.KitAssemblerMenu;
import com.zuxelus.energycontrol.recipes.KitAssemblerInput;
import com.zuxelus.energycontrol.recipes.KitAssemblerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Portiert aus 1.12.2: com.zuxelus.energycontrol.tileentities.TileEntityKitAssembler.
 *
 * Die Bausatzmontage stellt Bausaetze her, ohne dass man sie an der Werkbank zusammenlegt:
 * sechs Eingabefaecher, ein Ausgabefach, ein Stromspeicher und eine Laufzeit je Rezept.
 *
 * Das Original hatte sieben Faecher, von denen drei nur IC2 betrafen (Entlader, Trafo, ein
 * Hinweisfach). Die sind weggefallen; dafuer zaehlen alle sechs Eingabefaecher gleich --
 * die Zutaten duerfen liegen, wo sie wollen.
 */
public class KitAssemblerBlockEntity extends ECContainerBlockEntity {

    /** Faecher 0 bis 5 nehmen die Zutaten, Fach 6 gibt aus. */
    public static final int INPUT_SLOTS = 6;
    public static final int SLOT_RESULT = 6;

    private final ECEnergyStorage energy = new ECEnergyStorage(ECConfig.assemblerCapacity(), ECConfig.assemblerCapacity());

    private int progress;
    private int maxProgress;
    private boolean active;

    /** Das Rezept, an dem gerade gearbeitet wird -- nur auf dem Server gefuehrt. */
    private RecipeHolder<KitAssemblerRecipe> recipe;

    public KitAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ECBlockEntityTypes.KIT_ASSEMBLER.get(), pos, state, 7);
    }

    public ECEnergyStorage getEnergyStorage() {
        return energy;
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energy.getMaxEnergyStored();
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public boolean isActive() {
        return active;
    }

    public KitAssemblerInput getInput() {
        return new KitAssemblerInput(this, INPUT_SLOTS);
    }

    public void tick() {
        if(level == null || level.isClientSide) return;

        boolean wasActive = active;
        int wasProgress = progress;

        RecipeHolder<KitAssemblerRecipe> found = findRecipe();
        if(found == null || !fits(found.value())) {
            recipe = null;
            progress = 0;
            maxProgress = 0;
            active = false;
        } else {
            recipe = found;
            maxProgress = Math.max(1, found.value().time());
            active = energy.consume(ECConfig.assemblerConsumption());
            if(active) {
                progress++;
                if(progress >= maxProgress) craft(found.value());
            }
        }

        // Der Fortschritt laeuft ueber die Oberflaeche mit und braucht kein Blockpaket;
        // gemeldet wird nur der Wechsel zwischen "laeuft" und "steht" -- daran haengt die
        // Schauseite des Blocks.
        if(active != wasActive) {
            updateBlockState();
            sync();
        } else if(progress != wasProgress) {
            setChanged();
        }
    }

    /** Sucht das Rezept, das zu den Eingabefaechern passt. */
    private RecipeHolder<KitAssemblerRecipe> findRecipe() {
        KitAssemblerInput input = getInput();
        if(input.isEmpty()) return null;

        // Erst das laufende Rezept: der Rezeptverwalter durchsucht sonst jeden Tick alle.
        if(recipe != null && recipe.value().matches(input, level)) return recipe;

        Optional<RecipeHolder<KitAssemblerRecipe>> found =
                level.getRecipeManager().getRecipeFor(ECRecipes.KIT_ASSEMBLER.get(), input, level);
        return found.orElse(null);
    }

    /** Ob das Ergebnis noch ins Ausgabefach passt. */
    private boolean fits(KitAssemblerRecipe found) {
        ItemStack result = found.result();
        ItemStack inSlot = getItem(SLOT_RESULT);
        if(inSlot.isEmpty()) return true;
        if(!ItemStack.isSameItemSameComponents(inSlot, result)) return false;
        return inSlot.getCount() + result.getCount() <= inSlot.getMaxStackSize();
    }

    private void craft(KitAssemblerRecipe found) {
        KitAssemblerInput input = getInput();
        int[] slots = found.assign(input);
        if(slots == null) return;

        for(int i = 0; i < slots.length; i++) {
            removeItem(slots[i], found.inputs().get(i).count());
        }

        ItemStack result = found.result();
        ItemStack inSlot = getItem(SLOT_RESULT);
        if(inSlot.isEmpty()) setItem(SLOT_RESULT, result.copy());
        else inSlot.grow(result.getCount());

        progress = 0;
        sync();
    }

    private void updateBlockState() {
        if(level == null) return;
        BlockState state = getBlockState();
        if(!(state.getBlock() instanceof KitAssemblerBlock)) return;
        if(state.getValue(KitAssemblerBlock.ACTIVE) == active) return;
        level.setBlock(worldPosition, state.setValue(KitAssemblerBlock.ACTIVE, active), Block.UPDATE_ALL);
    }

    @Override
    protected void readProperties(CompoundTag tag, HolderLookup.Provider registries) {
        energy.setEnergyStored(tag.getInt("energy"));
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
        active = tag.getBoolean("active");
    }

    @Override
    protected void writeProperties(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("energy", energy.getEnergyStored());
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putBoolean("active", active);
    }

    /** Ins Ausgabefach legt niemand etwas hinein; in die Eingabefaecher alles. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < INPUT_SLOTS;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.energycontrol.kit_assembler");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new KitAssemblerMenu(id, inventory, this);
    }
}
