package com.hbm.blockentity.machine.rbmk;

import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.entity.projectile.RBMKDebris.DebrisType;
import com.hbm.handler.neutron.NeutronStream;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.RBMKOutgasserMenu;
import com.hbm.inventory.recipes.OutgasserRecipes;
import com.hbm.inventory.recipes.OutgasserRecipes.OutgasserRecipe;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.ChatFormatting;
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

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKOutgasser.
 *
 * Die Bestrahlungssaeule. Sie faengt den Neutronenfluss ab, der durch sie hindurchlaeuft, und
 * wandelt damit den eingelegten Gegenstand um. Anders als bei anderen Maschinen gibt es keine
 * feste Dauer -- wie schnell es geht, haengt allein an der Flussmenge, und langsame Neutronen
 * wirken dabei deutlich besser als schnelle.
 */
public class RBMKOutgasserBlockEntity extends RBMKSlottedBaseBlockEntity implements IRBMKFluxReceiver, IFluidStandardSenderMK2 {

    public FluidTank gas;
    public double progress;
    public static final int DURATION = 10_000;

    public RBMKOutgasserBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_OUTGASSER.get(), pos, state, 2);
        this.gas = new FluidTank(Fluids.TRITIUM, 64_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rbmkOutgasser");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(!this.canProcess()) this.progress = 0;

        if(this.gas.getFill() > 0) {
            for(DirPos pos : this.getOutputPos()) this.tryProvide(this.gas, this.level, pos);
        }

        super.updateEntity();
    }

    /**
     * Abweichung vom Original: dort kann das Gas zusaetzlich seitlich in einen darunter
     * stehenden Ladekran abgegeben werden. Oben und unten bleiben unveraendert.
     */
    protected DirPos[] getOutputPos() {
        return new DirPos[] {
                new DirPos(this.worldPosition.above(RBMKDials.getColumnHeight(this.level) + 1), Direction.UP),
                new DirPos(this.worldPosition.below(), Direction.DOWN)
        };
    }

    @Override
    public void receiveFlux(NeutronStream stream) {

        if(!this.canProcess()) return;

        // Langsame Neutronen wirken am besten: bei reinem Schnellfluss bleibt ein Fuenftel uebrig.
        double efficiency = Math.min(1 - stream.fluxRatio * 0.8, 1);

        this.progress += stream.fluxQuantity * efficiency * RBMKDials.getOutgasserMod(this.level);

        if(this.progress > DURATION) {
            this.process();
            this.setChanged();
        }
    }

    public boolean canProcess() {

        ItemStack input = this.slots.get(0);
        if(input.isEmpty()) return false;

        OutgasserRecipe recipe = OutgasserRecipes.getOutput(input);
        if(recipe == null) return false;

        FluidStack fluid = recipe.fluidOutput;

        if(fluid != null) {
            if(this.gas.getTankType() != fluid.type && this.gas.getFill() > 0) return false;
            this.gas.setTankType(fluid.type);
            if(this.gas.getFill() + fluid.fill > this.gas.getMaxFill()) return false;
        }

        ItemStack out = recipe.solidOutput;
        ItemStack slot = this.slots.get(1);

        if(slot.isEmpty() || out == null) return true;

        return ItemStack.isSameItemSameComponents(slot, out) && slot.getCount() + out.getCount() <= slot.getMaxStackSize();
    }

    private void process() {

        OutgasserRecipe recipe = OutgasserRecipes.getOutput(this.slots.get(0));
        if(recipe == null) return;

        this.removeItem(0, 1);
        this.progress = 0;

        if(recipe.fluidOutput != null) {
            this.gas.setFill(this.gas.getFill() + recipe.fluidOutput.fill);
        }

        ItemStack out = recipe.solidOutput;

        if(out != null) {
            if(this.slots.get(1).isEmpty()) {
                this.slots.set(1, out.copy());
            } else {
                this.slots.get(1).grow(out.getCount());
            }
        }
    }

    @Override
    public void getLookInfo(List<Component> text) {
        text.add(Component.translatable("trait.rbmk.look.progress",
                (int) (this.progress * 100 / DURATION)).withStyle(ChatFormatting.GREEN));
    }

    @Override
    public RBMKColumnType getConsoleType() {
        return RBMKColumnType.OUTGASSER;
    }

    @Override
    public RBMKType getRBMKType() {
        return RBMKType.OUTGASSER;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && OutgasserRecipes.getOutput(stack) != null;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == 1;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] {0, 1};
    }

    @Override
    public FluidTank[] getSendingTanks() { return new FluidTank[] { this.gas }; }

    @Override
    public FluidTank[] getAllTanks() { return new FluidTank[] { this.gas }; }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getDouble("progress");
        this.gas.readFromNBT(tag, "gas");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("progress", this.progress);
        this.gas.writeToNBT(tag, "gas");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.progress);
        this.gas.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.progress = buf.readDouble();
        this.gas.deserialize(buf);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RBMKOutgasserMenu(id, inventory, this);
    }

    /* Die Bestrahlungssaeule ist die groesste -- sie wirft entsprechend mehr aus. */
    @Override
    public void onMelt(int reduce) {

        if(this.level != null && !this.level.isClientSide) {
            int count = 4 + this.level.random.nextInt(2);
            for(int i = 0; i < count; i++) this.spawnDebris(DebrisType.BLANK);
        }

        super.onMelt(reduce);
    }
}
