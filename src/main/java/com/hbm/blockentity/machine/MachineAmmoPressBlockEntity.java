package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.menus.MachineAmmoPressMenu;
import com.hbm.inventory.recipes.AmmoPressRecipes;
import com.hbm.inventory.recipes.AmmoPressRecipes.AmmoPressRecipe;
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
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineAmmoPress.
 *
 * Neun Faecher im Gitter, ein Fach fuer das Ergebnis, kein Strom.
 *
 * SIE PRESST NUR, WAS EINGESTELLT IST. Das unterscheidet sie vom Werktisch: das Gitter allein
 * entscheidet nichts, erst die Wahl aus der Liste macht daraus ein Rezept. Wer nichts einstellt,
 * presst nichts, egal was im Gitter liegt. Dafuer nimmt jedes Fach dann auch nur noch das an,
 * was an dieser Stelle hingehoert.
 *
 * SIE PRESST, SOBALD DIE ZUTATEN DA SIND, und zwar so oft hintereinander, wie es reicht -- im
 * Original ist das eine Selbstrekursion, hier eine Schleife. Ein Ergebnis je Durchgang, bis das
 * Gitter leer ist oder das Ausgabefach voll.
 *
 * DER STEMPEL FAEHRT SICHTBAR: hoch, pressen, zurueckziehen, senken. Vierzig Ticks je Hub. Die
 * Winkel werden berechnet und mituebertragen, damit ein spaeterer Renderer sie vorfindet -- ein
 * Modell hat der Port noch nicht.
 */
public class MachineAmmoPressBlockEntity extends MachineBaseBlockEntity implements IControlReceiver {

    public int selectedRecipe = -1;

    public AnimationState animState = AnimationState.LIFTING;

    public int playAnimation = 0;
    public float prevLift = 0F;
    public float lift = 0F;
    public float prevPress = 0F;
    public float press = 0F;

    private AABB renderBox;

    public enum AnimationState {
        LIFTING, PRESSING, RETRACTING, LOWERING
    }

    public MachineAmmoPressBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_AMMO_PRESS.get(), pos, state, 10);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineAmmoPress");
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {

            if(this.playAnimation > 0) this.playAnimation--;
            this.performRecipe();
            this.networkPackNT(25);

        } else {

            this.prevLift = this.lift;
            this.prevPress = this.press;

            if(this.playAnimation > 0 || this.lift > 0) switch(this.animState) {
                case LIFTING -> {
                    this.lift += 1F / 40F;
                    if(this.lift >= 1F) { this.lift = 1F; this.animState = AnimationState.PRESSING; }
                }
                case PRESSING -> {
                    this.press += 1F / 20F;
                    if(this.press >= 1F) { this.press = 1F; this.animState = AnimationState.RETRACTING; }
                }
                case RETRACTING -> {
                    this.press -= 1F / 20F;
                    if(this.press <= 0F) { this.press = 0F; this.animState = AnimationState.LOWERING; }
                }
                case LOWERING -> {
                    this.lift -= 1F / 40F;
                    if(this.lift <= 0F) { this.lift = 0F; this.animState = AnimationState.LIFTING; }
                }
            }
        }
    }

    /**
     * ABWEICHUNG: das Original ruft sich hier selbst auf, solange es weitergeht. Eine Schleife
     * tut dasselbe, kann aber den Stapel nicht ueberlaufen lassen -- bei einem randvollen Gitter
     * waeren das im Original viele hundert Ebenen tief.
     */
    public void performRecipe() {

        if(this.selectedRecipe < 0 || this.selectedRecipe >= AmmoPressRecipes.recipes.size()) return;

        AmmoPressRecipe recipe = AmmoPressRecipes.recipes.get(this.selectedRecipe);

        while(true) {

            ItemStack out = this.slots.get(9);

            if(!out.isEmpty()) {
                if(!ItemStack.isSameItemSameComponents(out, recipe.output)) return;
                if(out.getCount() + recipe.output.getCount() > out.getMaxStackSize()) return;
            }

            if(!this.hasIngredients(recipe)) return;

            this.produceAmmo(recipe);
        }
    }

    public boolean hasIngredients(AmmoPressRecipe recipe) {

        for(int i = 0; i < 9; i++) {

            boolean wanted = recipe.input[i] != null;
            boolean there = !this.slots.get(i).isEmpty();

            if(!wanted && !there) continue;
            /* Auch ein Fach, das leer bleiben soll, muss leer sein -- sonst liesse sich das
             * Muster mit Beiwerk auffuellen. */
            if(wanted != there) return false;
            if(!recipe.input[i].matchesRecipe(this.slots.get(i), false)) return false;
        }

        return true;
    }

    /** Setzt voraus, dass hasIngredients zugestimmt hat. */
    protected void produceAmmo(AmmoPressRecipe recipe) {

        for(int i = 0; i < 9; i++) {
            if(recipe.input[i] != null) this.removeItem(i, recipe.input[i].stacksize);
        }

        ItemStack out = this.slots.get(9);

        if(out.isEmpty()) {
            this.slots.set(9, recipe.output.copy());
        } else {
            out.grow(recipe.output.getCount());
        }

        this.playAnimation = 40;
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {

        if(slot > 8) return false;
        if(this.selectedRecipe < 0 || this.selectedRecipe >= AmmoPressRecipes.recipes.size()) return false;

        AmmoPressRecipe recipe = AmmoPressRecipes.recipes.get(this.selectedRecipe);
        if(recipe.input[slot] == null) return false;
        return recipe.input[slot].matchesRecipe(stack, true);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == 9;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.selectedRecipe);
        buf.writeInt(this.playAnimation);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.selectedRecipe = buf.readInt();
        this.playAnimation = buf.readInt();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.selectedRecipe = tag.getInt("recipe");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("recipe", this.selectedRecipe);
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag tag) {

        if(!tag.contains("selection")) return;

        int newRecipe = tag.getInt("selection");
        this.selectedRecipe = newRecipe == this.selectedRecipe ? -1 : newRecipe;
        this.setChanged();
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 1, p.getY(), p.getZ() - 1, p.getX() + 2, p.getY() + 2, p.getZ() + 2);
        }
        return this.renderBox;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineAmmoPressMenu(id, inventory, this);
    }
}
