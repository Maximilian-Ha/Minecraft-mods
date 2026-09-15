package com.hbm.blockentity.machine;

import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.handler.MissileStruct;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.menus.MachineMissileAssemblyMenu;
import com.hbm.items.weapon.CustomMissileItem;
import com.hbm.items.weapon.CustomMissilePartItem;
import com.hbm.items.weapon.CustomMissilePartItem.FuelType;
import com.hbm.items.weapon.CustomMissilePartItem.PartType;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineMissileAssembly.
 *
 * Die Raketenmontage. Fuenf Faecher fuer die Bauteile, eines fuer das Ergebnis -- und eine
 * Ampel, die fuer jedes Fach anzeigt, ob das Teil dorthin passt.
 *
 * SIE PRUEFT DIE UEBERGAENGE. Ein Sprengkopf muss unten so breit sein wie der Rumpf oben, ein
 * Leitwerk oben so breit wie der Rumpf unten. Das Triebwerk muss ausserdem denselben Treibstoff
 * verbrennen, den der Rumpf mitfuehrt, und stark genug sein, den Sprengkopf zu heben.
 *
 * DAS LEITWERK DARF FEHLEN -- dann meldet sein Fach "weder noch" statt "falsch", und die Rakete
 * laesst sich trotzdem bauen. Ohne Leitwerk trifft sie nur nichts.
 *
 * SIE BRAUCHT KEINEN STROM. Das Zusammensetzen kostet nichts; teuer sind die Teile.
 *
 * ABWEICHUNG: das Original schickt in JEDEM Tick ein Paket mit dem ganzen Bauplan an alle
 * Spieler im Umkreis von zweihundertfuenfzig Bloecken, damit der Renderer die Rakete auf dem
 * Tisch zeichnen kann. Der Port uebertraegt die Faecher ohnehin ueber den Standardweg; ein
 * eigenes Paket je Tick waere reine Last.
 */
public class MachineMissileAssemblyBlockEntity extends MachineBaseBlockEntity implements IControlReceiver {

    /** Die Ampel: eins heisst passt, null heisst passt nicht, minus eins heisst gar keins. */
    public static final int STATE_BAD = 0;
    public static final int STATE_GOOD = 1;
    public static final int STATE_ABSENT = -1;

    public static final int SLOT_CHIP = 0;
    public static final int SLOT_WARHEAD = 1;
    public static final int SLOT_FUSELAGE = 2;
    public static final int SLOT_STABILITY = 3;
    public static final int SLOT_THRUSTER = 4;
    public static final int SLOT_OUTPUT = 5;

    public MachineMissileAssemblyBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_MISSILE_ASSEMBLY.get(), pos, state, 6);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.missileAssembly");
    }

    @Override
    public void updateEntity() {
        if(this.level == null || this.level.isClientSide) return;
        this.networkPackNT(250);
    }

    /** Der Bauplan, wie er gerade in den Faechern liegt. */
    public MissileStruct getStruct() {
        return new MissileStruct(slots.get(SLOT_WARHEAD), slots.get(SLOT_FUSELAGE), slots.get(SLOT_STABILITY), slots.get(SLOT_THRUSTER));
    }

    private CustomMissilePartItem part(int slot) {
        ItemStack stack = slots.get(slot);
        return stack.getItem() instanceof CustomMissilePartItem part ? part : null;
    }

    public int chipState() {
        CustomMissilePartItem chip = this.part(SLOT_CHIP);
        return chip != null && chip.type == PartType.CHIP ? STATE_GOOD : STATE_BAD;
    }

    public int fuselageState() {
        CustomMissilePartItem fuselage = this.part(SLOT_FUSELAGE);
        return fuselage != null && fuselage.type == PartType.FUSELAGE ? STATE_GOOD : STATE_BAD;
    }

    /** Der Sprengkopf muss zum Rumpf passen UND leicht genug fuer das Triebwerk sein. */
    public int warheadState() {

        CustomMissilePartItem warhead = this.part(SLOT_WARHEAD);
        CustomMissilePartItem fuselage = this.part(SLOT_FUSELAGE);
        CustomMissilePartItem thruster = this.part(SLOT_THRUSTER);

        if(warhead == null || fuselage == null || thruster == null) return STATE_BAD;
        if(warhead.type != PartType.WARHEAD || fuselage.type != PartType.FUSELAGE || thruster.type != PartType.THRUSTER) return STATE_BAD;

        float weight = (Float) warhead.attributes[2];
        float thrust = (Float) thruster.attributes[2];

        return warhead.bottom == fuselage.top && weight <= thrust ? STATE_GOOD : STATE_BAD;
    }

    /** Kein Leitwerk ist kein Fehler -- daher der dritte Zustand. */
    public int stabilityState() {

        if(slots.get(SLOT_STABILITY).isEmpty()) return STATE_ABSENT;

        CustomMissilePartItem fins = this.part(SLOT_STABILITY);
        CustomMissilePartItem fuselage = this.part(SLOT_FUSELAGE);

        if(fins == null || fuselage == null) return STATE_BAD;

        return fins.type == PartType.FINS && fins.top == fuselage.bottom ? STATE_GOOD : STATE_BAD;
    }

    /** Das Triebwerk muss passen und denselben Treibstoff verbrennen wie der Rumpf fuehrt. */
    public int thrusterState() {

        CustomMissilePartItem thruster = this.part(SLOT_THRUSTER);
        CustomMissilePartItem fuselage = this.part(SLOT_FUSELAGE);

        if(thruster == null || fuselage == null) return STATE_BAD;
        if(thruster.type != PartType.THRUSTER || fuselage.type != PartType.FUSELAGE) return STATE_BAD;

        return thruster.top == fuselage.bottom && thruster.attributes[0] == (FuelType) fuselage.attributes[0] ? STATE_GOOD : STATE_BAD;
    }

    public boolean canBuild() {

        if(!slots.get(SLOT_OUTPUT).isEmpty()) return false;
        if(this.chipState() != STATE_GOOD) return false;
        if(this.warheadState() != STATE_GOOD) return false;
        if(this.fuselageState() != STATE_GOOD) return false;
        if(this.thrusterState() != STATE_GOOD) return false;

        /* Nur "passt nicht" verhindert den Bau; "gar keins" ist erlaubt. */
        return this.stabilityState() != STATE_BAD;
    }

    public void construct() {

        if(!this.canBuild()) return;

        slots.set(SLOT_OUTPUT, CustomMissileItem.buildMissile(
                slots.get(SLOT_CHIP), slots.get(SLOT_WARHEAD), slots.get(SLOT_FUSELAGE),
                this.stabilityState() == STATE_GOOD ? slots.get(SLOT_STABILITY) : ItemStack.EMPTY,
                slots.get(SLOT_THRUSTER)));

        if(this.stabilityState() == STATE_GOOD) slots.set(SLOT_STABILITY, ItemStack.EMPTY);

        slots.set(SLOT_CHIP, ItemStack.EMPTY);
        slots.set(SLOT_WARHEAD, ItemStack.EMPTY);
        slots.set(SLOT_FUSELAGE, ItemStack.EMPTY);
        slots.set(SLOT_THRUSTER, ItemStack.EMPTY);

        this.setChanged();

        if(this.level != null) {
            this.level.playSound(null, this.worldPosition, NtmSoundEvents.MISSILE_ASSEMBLY_DONE.get(), SoundSource.BLOCKS, 1F, 1F);
        }
    }

    @Override
    public void receiveControl(CompoundTag data) {
        if(data.contains("build")) this.construct();
    }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    /* Von aussen laesst sie sich weder fuellen noch leeren -- das Original auch nicht. */
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return false; }
    @Override public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) { return false; }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[0]; }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MachineMissileAssemblyMenu(id, inventory, this);
    }
}
