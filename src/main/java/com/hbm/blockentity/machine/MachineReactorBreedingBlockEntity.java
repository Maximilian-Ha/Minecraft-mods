package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.ReactorResearchBlock;
import com.hbm.inventory.menus.MachineReactorBreedingMenu;
import com.hbm.inventory.recipes.BreederRecipes;
import com.hbm.inventory.recipes.BreederRecipes.BreederRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineReactorBreeding.
 *
 * Der Brutreaktor: ein Kasten, den man seitlich an einen Forschungsreaktor stellt. Er hat keine
 * eigene Reaktion -- er zaehlt bloss den Fluss aller Forschungsreaktoren, die ihn beruehren, und
 * verwandelt damit Brutstaebe.
 *
 * Der Fortschritt haengt am Verhaeltnis von anliegendem zu noetigem Fluss: liegt genau der
 * geforderte Wert an, dauert eine Umwandlung 400 Ticks; liegt das Doppelte an, die Haelfte.
 *
 * ABWEICHUNG: NICHT UEBERNOMMEN ist die OpenComputers-Anbindung (ENTSCHEIDUNGEN.md).
 */
public class MachineReactorBreedingBlockEntity extends MachineBaseBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    /** Bruchteil einer Umwandlung je Tick bei genau ausreichendem Fluss. */
    public static final float PROGRESS_PER_TICK = 0.0025F;

    public int flux;
    public float progress;

    public MachineReactorBreedingBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_REACTOR_BREEDING.get(), pos, state, 2);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.reactorBreeding");
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;

        this.flux = 0;
        this.gatherFlux();

        BreederRecipe recipe = this.canProcess();

        if(recipe != null) {
            /*
             * ABWEICHUNG: das Original rechnet hier mit zwei ints und teilt damit ganzzahlig --
             * anderthalbfacher Fluss zaehlt wie einfacher, und alles unterhalb eines vollen
             * Vielfachen faellt unter den Tisch. Gemeint ist offensichtlich das Verhaeltnis; hier
             * steht es als Gleitkommazahl.
             */
            this.progress += PROGRESS_PER_TICK * ((float) this.flux / (float) recipe.flux);

            if(this.progress >= 1.0F) {
                this.progress = 0F;
                this.processItem(recipe);
                this.setChanged();
            }
        } else {
            this.progress = 0.0F;
        }

        this.networkPackNT(20);
    }

    /** Zaehlt den Fluss aller Forschungsreaktoren, die seitlich anliegen. */
    private void gatherFlux() {

        for(Direction dir : Direction.Plane.HORIZONTAL) {

            BlockPos side = this.worldPosition.relative(dir);
            if(!(this.level.getBlockState(side).getBlock() instanceof ReactorResearchBlock reactor)) continue;

            BlockPos corePos = reactor.findCore(this.level, side);
            if(corePos == null) continue;

            if(this.level.getBlockEntity(corePos) instanceof ReactorResearchBlockEntity core) {
                this.flux += core.totalFlux;
            }
        }
    }

    /** Das Rezept, das gerade laufen kann, oder null. */
    private BreederRecipe canProcess() {

        ItemStack input = this.slots.get(SLOT_INPUT);
        if(input.isEmpty()) return null;

        BreederRecipe recipe = BreederRecipes.getOutput(input);
        if(recipe == null) return null;
        if(this.flux < recipe.flux) return null;

        ItemStack output = this.slots.get(SLOT_OUTPUT);
        if(output.isEmpty()) return recipe;
        if(!ItemStack.isSameItemSameComponents(output, recipe.output)) return null;
        if(output.getCount() >= output.getMaxStackSize()) return null;

        return recipe;
    }

    private void processItem(BreederRecipe recipe) {

        ItemStack output = this.slots.get(SLOT_OUTPUT);

        if(output.isEmpty()) {
            this.slots.set(SLOT_OUTPUT, recipe.output.copy());
        } else {
            output.grow(recipe.output.getCount());
        }

        this.slots.get(SLOT_INPUT).shrink(1);
        if(this.slots.get(SLOT_INPUT).getCount() <= 0) this.slots.set(SLOT_INPUT, ItemStack.EMPTY);
    }

    public int getProgressScaled(int i) {
        return (int) (this.progress * i);
    }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == SLOT_INPUT; }
    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) { return index == SLOT_OUTPUT; }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[] {SLOT_INPUT, SLOT_OUTPUT}; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.flux);
        buf.writeFloat(this.progress);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.flux = buf.readInt();
        this.progress = buf.readFloat();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.flux = tag.getInt("flux");
        this.progress = tag.getFloat("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("flux", this.flux);
        tag.putFloat("progress", this.progress);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineReactorBreedingMenu(id, inventory, this);
    }
}
