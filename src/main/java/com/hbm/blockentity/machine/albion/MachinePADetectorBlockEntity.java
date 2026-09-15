package com.hbm.blockentity.machine.albion;

import com.hbm.blockentity.CooledBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.machine.albion.MachinePASourceBlockEntity.PAState;
import com.hbm.blocks.DummyableBlock;
import com.hbm.inventory.menus.MachinePADetectorMenu;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes.ParticleAcceleratorRecipe;
import com.hbm.lib.Library;
import com.hbm.saveddata.satellite.SatelliteDetector;
import com.hbm.saveddata.satellite.SatelliteDetector.BurstIntensity;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.albion.TileEntityPADetector.
 *
 * Das Ende der Fahrt. Hier faengt der Ring das Teilchen ab und sieht nach, was es geworden ist.
 *
 * ER VERLANGT EINEN VOLLKOMMEN GEBUENDELTEN STRAHL. Nicht "moeglichst wenig Streuung" -- null.
 * Wer eine Kavitaet mehr als Quadrupole aufstellt, faehrt hunderte Runden und verliert alles im
 * letzten Block. Das ist die haerteste Bedingung der ganzen Anlage.
 *
 * DANN ERST DAS REZEPT, und dann erst der Impuls: zu langsam ist ein eigener Fehler, kein Rezept
 * ist ein anderer. Die Anzeige an der Quelle unterscheidet beides, damit man weiss, ob der Ring
 * zu klein ist oder die Zutaten falsch.
 *
 * WAS NICHT HINEINPASST, GEHT VERLOREN. Ist das Ausgabefach voll, meldet der Detektor trotzdem
 * Erfolg -- das Teilchen ist verbraucht. Das ist das Verhalten des Originals, und es ist
 * absichtlich so: ein voller Detektor haelt den Ring nicht an.
 *
 * NICHT UEBERNOMMEN: die Meldung an den Strahlenscanner-Satelliten (den es im Port nicht gibt)
 * und die Errungenschaft fuer das Digamma-Teilchen. Die Meldung an den Detektorsatelliten steht,
 * die gibt es.
 */
public class MachinePADetectorBlockEntity extends CooledBaseBlockEntity implements IParticleUser {

    public static final long usage = 100_000;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_CONTAINER_1 = 1;
    public static final int SLOT_CONTAINER_2 = 2;
    public static final int SLOT_OUTPUT_1 = 3;
    public static final int SLOT_OUTPUT_2 = 4;

    public MachinePADetectorBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_PA_DETECTOR.get(), pos, state, 5);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.paDetector");
    }

    @Override
    public void updateEntity() {

        if(this.level != null && !this.level.isClientSide) {
            this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, this.getMaxPower());
        }

        super.updateEntity();
    }

    public Direction getDir() {
        BlockState state = this.getBlockState();
        return state.hasProperty(DummyableBlock.FACING) ? state.getValue(DummyableBlock.FACING) : Direction.NORTH;
    }

    @Override
    public DirPos[] getConPos() {

        Direction dir = this.getDir();
        Direction rot = dir.getClockWise(Axis.Y);
        BlockPos p = this.worldPosition.relative(rot, -5);

        return new DirPos[] {
                new DirPos(p, rot.getOpposite()),
                new DirPos(p.above(), rot.getOpposite()),
                new DirPos(p.below(), rot.getOpposite()),
                new DirPos(p.relative(dir), rot.getOpposite()),
                new DirPos(p.relative(dir, -1), rot.getOpposite())
        };
    }

    @Override
    public long getMaxPower() {
        return 1_000_000;
    }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == SLOT_CONTAINER_1 || slot == SLOT_CONTAINER_2; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return slot == SLOT_OUTPUT_1 || slot == SLOT_OUTPUT_2; }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[] { SLOT_CONTAINER_1, SLOT_CONTAINER_2, SLOT_OUTPUT_1, SLOT_OUTPUT_2 }; }

    private AABB renderBox;

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 4, p.getY() - 2, p.getZ() - 4, p.getX() + 5, p.getY() + 3, p.getZ() + 5);
        }
        return this.renderBox;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachinePADetectorMenu(id, inventory, this);
    }

    public Direction getBeamlineDir() {
        return this.getDir().getCounterClockWise(Axis.Y);
    }

    @Override
    public boolean canParticleEnter(Particle particle, Direction dir, BlockPos pos) {
        Direction detectorDir = this.getBeamlineDir();
        return this.worldPosition.relative(detectorDir, -4).equals(pos) && detectorDir == dir;
    }

    @Override
    public void onEnter(Particle particle, Direction dir) {

        /* Das Teilchen ist in jedem Fall verbraucht -- es gibt keinen Ausgang. */
        particle.invalid = true;

        if(particle.defocus > 0) { particle.crash(PAState.CRASH_DEFOCUS); return; }
        if(this.power < usage) { particle.crash(PAState.CRASH_NOPOWER); return; }
        if(!this.isCool()) { particle.crash(PAState.CRASH_NOCOOL); return; }

        this.power -= usage;

        for(ParticleAcceleratorRecipe recipe : ParticleAcceleratorRecipes.recipes) {

            if(!recipe.matchesRecipe(particle.input1, particle.input2)) continue;

            if(particle.momentum < recipe.momentum) {
                particle.crash(PAState.CRASH_UNDERSPEED);
                return;
            }

            if(this.canAccept(recipe)) {

                /* Braucht ein Ergebnis eine Huelle, kommt sie aus dem zugehoerigen Fach. */
                if(recipe.output1.hasCraftingRemainingItem()) this.removeItem(SLOT_CONTAINER_1, 1);
                if(recipe.output2 != null && recipe.output2.hasCraftingRemainingItem()) this.removeItem(SLOT_CONTAINER_2, 1);

                if(this.slots.get(SLOT_OUTPUT_1).isEmpty()) {
                    this.slots.set(SLOT_OUTPUT_1, recipe.output1.copy());
                } else {
                    this.slots.get(SLOT_OUTPUT_1).grow(recipe.output1.getCount());
                }

                if(recipe.output2 != null) {
                    if(this.slots.get(SLOT_OUTPUT_2).isEmpty()) {
                        this.slots.set(SLOT_OUTPUT_2, recipe.output2.copy());
                    } else {
                        this.slots.get(SLOT_OUTPUT_2).grow(recipe.output2.getCount());
                    }
                }
            }

            /* Ein Treffer ist laut genug, dass ein Satellit ihn sieht. */
            SatelliteDetector.reportEvent(this.level, SatelliteDetector.DURATION_MEDIUM, BurstIntensity.MEDIUM, this.worldPosition.getX(), this.worldPosition.getZ());
            particle.crash(PAState.SUCCESS);
            return;
        }

        particle.crash(PAState.CRASH_NORECIPE);
    }

    public boolean canAccept(ParticleAcceleratorRecipe recipe) {
        return this.checkSlot(recipe.output1, SLOT_CONTAINER_1, SLOT_OUTPUT_1)
                && this.checkSlot(recipe.output2, SLOT_CONTAINER_2, SLOT_OUTPUT_2);
    }

    /**
     * Passt das Ergebnis ins Fach, und liegt die noetige Huelle bereit? Beides muss stimmen,
     * sonst wird gar nichts ausgegeben.
     */
    public boolean checkSlot(ItemStack output, int containerSlot, int outputSlot) {

        if(output != null && !output.isEmpty()) {

            ItemStack out = this.slots.get(outputSlot);

            if(!out.isEmpty()) {
                if(!ItemStack.isSameItemSameComponents(out, output)) return false;
                if(out.getCount() + output.getCount() > output.getMaxStackSize()) return false;
            }

            if(output.hasCraftingRemainingItem()) {
                ItemStack container = output.getCraftingRemainingItem();
                ItemStack held = this.slots.get(containerSlot);
                if(held.isEmpty() || !ItemStack.isSameItemSameComponents(held, container)) return false;
            }
        }

        return true;
    }

    /** Der Strahl endet hier -- es gibt keinen Ausgang. */
    @Override
    public BlockPos getExitPos(Particle particle) {
        return null;
    }
}
