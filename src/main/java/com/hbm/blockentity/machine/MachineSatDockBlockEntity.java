package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.menus.MachineSatDockMenu;
import com.hbm.items.ISatChip;
import com.hbm.items.NtmItems;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellite.SatelliteBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineSatDock.
 *
 * Die Satellitenstation: fuenfzehn Faecher fuer die Ladung und ein sechzehntes fuer den
 * Satellitenchip. Liegt dort einer, fragt die Station jede Sekunde beim Satelliten mit dieser
 * Frequenz nach -- und hat der etwas bereit, faellt eine Kapsel vom Himmel.
 *
 * SIE HAT KEINEN STROMANSCHLUSS und keinen Schalter. Die Arbeit machen der Satellit und die
 * Kapsel; die Station ist der Landeplatz und das Lager.
 *
 * ABWEICHUNG: das Original setzt in jedem Tick die eigene Bauflaeche neu, falls sie fehlt --
 * ein Notbehelf aus einer Zeit, in der das Setzen unzuverlaessig war. Der Port baut sie beim
 * Setzen einmal auf und laesst sie in Ruhe.
 */
public class MachineSatDockBlockEntity extends MachineBaseBlockEntity {

    /** Der Chip liegt im letzten Fach, die Ladung in den fuenfzehn davor. */
    public static final int SLOT_CHIP = 15;

    private AABB renderBox;

    public MachineSatDockBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_SAT_DOCK.get(), pos, state, 16);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.satDock");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;
        if(!(this.level instanceof ServerLevel serverLevel)) return;

        /* Einmal je Sekunde, und versetzt nach Position -- stuenden hundert Stationen da,
         * fragten sie sonst alle im selben Tick. */
        long time = this.level.getGameTime() + this.worldPosition.asLong();
        if(time % 20 != 0) return;

        ItemStack chip = this.slots.get(SLOT_CHIP);
        if(chip.isEmpty() || chip.getItem() != NtmItems.SAT_CHIP.get()) return;

        SatelliteBase sat = SatelliteSavedData.getData(serverLevel).getSatFromFreq(ISatChip.getFreqS(chip));
        if(sat != null) sat.tryRequestItems(serverLevel, this.worldPosition);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_CHIP;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index < SLOT_CHIP;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        int[] faces = new int[16];
        for(int i = 0; i < 16; i++) faces[i] = i;
        return faces;
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 1, p.getY(), p.getZ() - 1, p.getX() + 2, p.getY() + 1, p.getZ() + 2);
        }
        return this.renderBox;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineSatDockMenu(id, inventory, this);
    }
}
