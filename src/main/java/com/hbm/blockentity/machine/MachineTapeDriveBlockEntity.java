package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.machine.MachineTapeDriveBlock;
import com.hbm.inventory.MetaHelper;
import com.hbm.inventory.menus.MachineTapeDriveMenu;
import com.hbm.items.NtmItems;
import com.hbm.items.machine.DriveItem.DriveType;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellite.SatelliteBase;
import com.hbm.util.CompatExternal;
import com.hbm.util.EnumUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineTapeDrive.
 *
 * Zwoelf Schaechte fuer Datentraeger. Steht hinter dem Laufwerk eine Satellitenverbindung, deren
 * Satellit gerade Daten fuehrt, wird der erste passende leere Traeger beschrieben -- einer alle
 * zehn Ticks, und danach ist der Satellit leer, bis er neue Daten sammelt.
 *
 * ES BRAUCHT KEINEN STROM. Das Laufwerk ist ein Anhaengsel der Bodenstation, kein eigenes
 * Kraftwerk; wer den Satelliten hat und die Station daneben, hat auch die Daten.
 *
 * "HINTER" HEISST: entgegen der Blickrichtung des Blocks. Steht dort ein Dummy-Block der
 * Bodenstation und nicht ihr Kern, findet CompatExternal.getCoreFromPos ihn trotzdem -- im
 * Original macht das die Proxy-Kachel.
 *
 * DIE ZWOELF BYTES in tapes sagen nur, WAS in einem Schacht steckt: nichts, irgendetwas, ein
 * leerer Traeger, ein beschriebener. Das Original faerbt damit die Baender im Modell ein; ein
 * Modell hat der Port noch nicht, uebertragen wird es trotzdem, damit es spaeter da ist.
 */
public class MachineTapeDriveBlockEntity extends MachineBaseBlockEntity {

    public static final byte SLOT_EMPTY = 0;
    public static final byte SLOT_ANY = 1;
    public static final byte SLOT_EMPTY_TAPE = 2;
    public static final byte SLOT_FILLED_TAPE = 3;

    public byte[] tapes = new byte[12];

    public MachineTapeDriveBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_TAPE_DRIVE.get(), pos, state, 12);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machineTapeDrive");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        if(this.level.getGameTime() % 10 == 0) this.tryReadSatellite();

        this.networkPackNT(50);
    }

    private void tryReadSatellite() {

        if(!(this.level instanceof ServerLevel serverLevel)) return;

        Direction dir = this.getBlockState().getValue(MachineTapeDriveBlock.FACING).getOpposite();
        BlockEntity connected = CompatExternal.getCoreFromPos(serverLevel, this.worldPosition.relative(dir));

        if(!(connected instanceof MachineSatLinkBlockEntity link) || !link.connected) return;

        SatelliteSavedData data = SatelliteSavedData.getData(serverLevel);
        SatelliteBase satellite = data.sats.get(link.freq);

        if(satellite == null || !satellite.hasData(serverLevel)) return;

        /* hasData darf dem Satelliten neue Daten anlegen -- der Wissenschaftssatellit tut das.
         * Das gehoert gespeichert, auch wenn gleich kein passender Traeger gefunden wird. */
        data.setDirty();

        for(int i = 0; i < 12; i++) {

            ItemStack stack = this.slots.get(i);
            if(stack.isEmpty() || stack.getItem() != NtmItems.DRIVE.get()) continue;

            DriveType type = EnumUtil.grabEnumSafely(DriveType.class, MetaHelper.getMeta(stack));
            DriveType result = satellite.getOutputData(type);

            if(result != null) {
                satellite.consumeData();
                this.slots.set(i, MetaHelper.newStack(NtmItems.DRIVE.get(), 1, result.ordinal()));
                this.setChanged();
                return;
            }
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() == NtmItems.DRIVE.get();
    }

    /** Ein Traeger je Schacht -- zwei uebereinander waeren nicht zu beschriften. */
    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);

        for(int i = 0; i < 12; i++) buf.writeByte(this.describeSlot(this.slots.get(i)));
    }

    private byte describeSlot(ItemStack stack) {

        if(stack.isEmpty()) return SLOT_EMPTY;
        if(stack.getItem() != NtmItems.DRIVE.get()) return SLOT_ANY;

        int meta = MetaHelper.getMeta(stack);

        if(meta == DriveType.DISK_EMPTY.ordinal() || meta == DriveType.FLASH_EMPTY.ordinal()) return SLOT_EMPTY_TAPE;
        if(meta == DriveType.DISK_BROKEN.ordinal() || meta == DriveType.FLASH_BROKEN.ordinal()) return SLOT_ANY;

        return SLOT_FILLED_TAPE;
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);

        for(int i = 0; i < 12; i++) this.tapes[i] = buf.readByte();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineTapeDriveMenu(id, inventory, this);
    }
}
