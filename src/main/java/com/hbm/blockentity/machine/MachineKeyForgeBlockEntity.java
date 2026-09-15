package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.inventory.menus.MachineKeyForgeMenu;
import com.hbm.items.tools.KeyPinItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineKeyForge.
 *
 * Die Schluesselschmiede macht aus Schluesselrohlingen Schluessel. Sie hat keinen Knopf und
 * keinen Fortschritt: sie arbeitet, sobald etwas darin liegt.
 *
 * ZWEI FAECHER LINKS SIND DAS KOPIERWERK. Liegt in beiden ein Schluessel, bekommt der rechte
 * von beiden die Zahnung des linken -- so entstehen Zweitschluessel. Der Vorlagenschluessel
 * bleibt unveraendert; man nimmt ihn wieder mit.
 *
 * DAS DRITTE FACH IST DER ZUFALL. Was dort liegt, bekommt in jedem Tick eine neue Zahnung
 * zwischen hundert und neunhundertneunundneunzig. Man legt einen Rohling hinein, nimmt ihn
 * wieder heraus, und hat ein Schloss, das sonst niemand hat.
 *
 * DASS DAS DRITTE FACH STAENDIG NEU WUERFELT, ist Absicht und steht so im Original: es gibt
 * keinen Grund, die Zahnung festzuhalten, solange der Schluessel in der Maschine liegt. Wer
 * eine bestimmte Zahl will, muss sie eben abwarten.
 *
 * DER GEFAELSCHTE SCHLUESSEL macht bei beidem nicht mit: seine Zahnung laesst sich weder
 * aendern noch abnehmen. Das ist der ganze Sinn dieses Gegenstands.
 */
public class MachineKeyForgeBlockEntity extends MachineBaseBlockEntity {

    public static final int SLOT_TEMPLATE = 0;
    public static final int SLOT_COPY = 1;
    public static final int SLOT_RANDOM = 2;
    public static final int SLOTS = 3;

    public MachineKeyForgeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_KEY_FORGE.get(), pos, state, SLOTS);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.keyForge");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        ItemStack template = this.slots.get(SLOT_TEMPLATE);
        ItemStack copy = this.slots.get(SLOT_COPY);

        if(transferable(template) && transferable(copy)) {
            KeyPinItem.setPins(copy, KeyPinItem.getPins(template));
            this.setChanged();
        }

        ItemStack random = this.slots.get(SLOT_RANDOM);

        if(transferable(random)) {
            KeyPinItem.setPins(random, this.level.random.nextInt(900) + 100);
            this.setChanged();
        }
    }

    private static boolean transferable(ItemStack stack) {
        return stack.getItem() instanceof KeyPinItem key && key.canTransfer();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof KeyPinItem;
    }

    @Override public int[] getSlotsForFace(Direction direction) { return ACCESS; }

    private static final int[] ACCESS = { SLOT_TEMPLATE, SLOT_COPY, SLOT_RANDOM };

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineKeyForgeMenu(id, inventory, this);
    }
}
