package com.hbm.blockentity.network;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.entity.NtmEntityTypes;
import com.hbm.entity.item.MovingItem;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.blocks.network.CranePartitioner.TileEntityCranePartitioner.
 *
 * Der Portionierer teilt Stapel in die Menge auf, die der Kristallisator fuer ein Rezept
 * braucht. Das klingt nach einer Kleinigkeit und ist es nicht: der Kristallisator nimmt je nach
 * Erz zwei, vier oder acht Stueck auf einmal, und ein Band, das ihm einzelne Stueck fuer Stueck
 * hineinschiebt, laesst ihn nie anlaufen.
 *
 * ZWEI REIHEN ZU FUENFUNDVIERZIG FAECHERN. Die vordere nimmt an, was sich portionieren laesst;
 * die hintere ist der Auswurf fuer alles, was der Kristallisator nicht kennt. So verstopft ein
 * falsch eingeschleustes Stueck die Anlage nicht -- es wandert nach hinten und kann von dort
 * abgeholt werden.
 *
 * DIE KLEINSTEN STAPEL ZUERST. Wer nur drei Stueck von acht beisammen hat, blockiert sonst die
 * Reihe, waehrend hinter ihm ein voller Stapel wartet. Das Original sortiert aus demselben
 * Grund nach Stapelgroesse.
 *
 * ER HAT KEINE OBERFLAECHE. Beschickt wird er vom Band, geleert ebenfalls; sein Inventar ist
 * eine Warteschlange, kein Lager.
 */
public class CranePartitionerBlockEntity extends MachineBaseBlockEntity {

    /** Eine Reihe zum Portionieren, eine zum Auswerfen. */
    public static final int QUEUE = 45;
    public static final int SLOTS = QUEUE * 2;

    /** Die kleinsten Stapel zuerst -- siehe oben. */
    private static final Comparator<ItemStack> BY_COUNT = Comparator.comparingInt(ItemStack::getCount);

    public CranePartitionerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.CRANE_PARTITIONER.get(), pos, state, SLOTS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.partitioner");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        List<ItemStack> queue = new ArrayList<>();
        for(int i = 0; i < QUEUE; i++) {
            if(!this.slots.get(i).isEmpty()) queue.add(this.slots.get(i));
        }

        if(queue.isEmpty()) return;

        queue.sort(BY_COUNT);

        boolean changed = false;

        for(ItemStack stack : queue) {

            int portion = CrystallizerRecipes.getAmount(stack);

            /* Kennt der Kristallisator die Ware doch nicht, geht sie in einem Stueck hinaus --
             * liegen bleiben soll sie auf keinen Fall. */
            if(portion <= 0) portion = stack.getCount();

            while(stack.getCount() >= portion) {

                MovingItem moving = new MovingItem(NtmEntityTypes.MOVING_ITEM.get(), this.level);
                moving.setItemStack(stack.copyWithCount(portion));
                moving.moveTo(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.25, this.worldPosition.getZ() + 0.5, 0F, 0F);
                this.level.addFreshEntity(moving);

                stack.shrink(portion);
                changed = true;
            }
        }

        for(int i = 0; i < QUEUE; i++) {
            if(this.slots.get(i).getCount() <= 0) this.slots.set(i, ItemStack.EMPTY);
        }

        if(changed) this.setChanged();
    }

    /**
     * Legt ab, was vom Band kommt: Portionierbares nach vorn, alles uebrige in den Auswurf.
     * Was nicht mehr hineinpasst, bleibt uebrig und wird vom Aufrufer entsorgt.
     */
    public ItemStack store(ItemStack stack) {

        boolean known = CrystallizerRecipes.getAmount(stack) > 0;
        int from = known ? 0 : QUEUE;
        int to = known ? QUEUE : SLOTS;

        for(int pass = 0; pass < 2; pass++) {
            for(int i = from; i < to && !stack.isEmpty(); i++) {

                ItemStack slot = this.slots.get(i);

                if(pass == 0 && !slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack)) {
                    int room = Math.min(slot.getMaxStackSize(), this.getMaxStackSize(slot)) - slot.getCount();
                    int moved = Math.min(room, stack.getCount());
                    if(moved <= 0) continue;
                    slot.grow(moved);
                    stack.shrink(moved);
                } else if(pass == 1 && slot.isEmpty()) {
                    int moved = Math.min(this.getMaxStackSize(stack), stack.getCount());
                    this.slots.set(i, stack.copyWithCount(moved));
                    stack.shrink(moved);
                }
            }
        }

        this.setChanged();
        return stack;
    }

    /**
     * Von aussen erreichbar sind alle Faecher, entnehmen laesst sich aber nur aus dem Auswurf.
     * Das Original nennt das "declog": ein Trichter darf die Anlage entstopfen, aber nicht die
     * Warteschlange leerraeumen.
     */
    @Override public int[] getSlotsForFace(Direction direction) { return ALL_SLOTS; }
    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) { return index >= QUEUE; }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index < QUEUE && CrystallizerRecipes.getAmount(stack) > 0;
    }

    /**
     * Er hat keine Oberflaeche. MachineBaseBlockEntity ist ein MenuProvider, weil fast jede
     * Maschine eine hat; dieser eine hat keine, und null heisst genau das -- der Rechtsklick
     * oeffnet nichts.
     */
    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return null;
    }

    private static final int[] ALL_SLOTS = allSlots();

    private static int[] allSlots() {
        int[] slots = new int[SLOTS];
        for(int i = 0; i < SLOTS; i++) slots[i] = i;
        return slots;
    }
}
