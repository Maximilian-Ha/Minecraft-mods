package com.hbm.blockentity.machine.fusion;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.blockentity.MachineBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blocks.DummyableBlock;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.FusionPlasmaForgeMenu;
import com.hbm.inventory.recipes.PlasmaForgeRecipe;
import com.hbm.items.NtmItems;
import com.hbm.lib.Library;
import com.hbm.module.machine.ModuleMachinePlasma;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.fauxpointtwelve.DirPos;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.fusion.TileEntityFusionPlasmaForge.
 *
 * Die Plasmaschmiede. Sie haengt an einer Plasmaleitung und verarbeitet, was sich nur bei
 * Sternentemperaturen verarbeiten laesst: Euphemium, Dineutronium, die Bauteile der Traegheitsfusion
 * -- und den Torus des Fusionsreaktors selbst.
 *
 * Die Plasmaleistung wird nicht verbraucht, sondern nur geprueft: erreicht sie die Zuendtemperatur
 * des Rezepts, laeuft der Ofen. Drei Viertel davon gibt er hinter sich weiter, so dass sich mehrere
 * Schmieden an einen Reaktor haengen lassen -- jede naechste allerdings mit weniger.
 *
 * Der Booster ist ein Zusatz: ein radioaktives Isotop im dritten Fach laesst den Ofen fuer eine
 * gewisse Zahl von Ticks viermal so schnell laufen. Wie lange, haengt vom Isotop ab; Astat-209
 * haelt am laengsten.
 *
 * ABWEICHUNGEN:
 * - NICHT UEBERNOMMEN ist die OpenComputers-Anbindung (ENTSCHEIDUNGEN.md).
 * - Die Boosterliste steht im Original als OreDictionary-Eintraege da. Der Port hat keinen
 *   OreDictionary; hier stehen die zwanzig Gegenstaende einzeln, in derselben Reihenfolge und mit
 *   denselben Laufzeiten.
 */
public class FusionPlasmaForgeBlockEntity extends MachineBaseBlockEntity
        implements IFusionPowerReceiver, IEnergyReceiverMK2, IFluidStandardReceiverMK2, IControlReceiver {

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_BLUEPRINT = 1;
    public static final int SLOT_BOOSTER = 2;
    public static final int SLOT_INPUT_FIRST = 3;
    public static final int SLOT_OUTPUT = 15;

    public final FluidTank inputTank;

    public long power;
    public long maxPower = 10_000_000;
    public boolean didProcess;

    public float plasmaRed;
    public float plasmaGreen;
    public float plasmaBlue;
    public long plasmaEnergy;
    public long plasmaEnergySync;
    public double neutronEnergy;

    protected GenNode<?> receiverNode;
    protected GenNode<?> providerNode;
    /** Nur fuer die Anzeige: haengt hinter uns noch etwas an der Leitung? */
    public boolean connected;

    public int booster;
    public int maxBooster;

    /** Nur Client: verschiebt die Plasmaanimation, damit nicht alle Schmieden im Gleichtakt pulsen. */
    public int timeOffset = -1;

    public final ModuleMachinePlasma plasmaModule;

    /* Nur Client: der drehende Ring und die beiden Arme. */
    public double prevRing;
    public double ring;
    public double ringSpeed;
    public double ringTarget;
    public int ringDelay;
    public final ForgeArm armStriker;
    public final ForgeArm armJet;

    private AABB renderBox;

    /** Welches Isotop wie viele Ticks lang beschleunigt. Reihenfolge und Werte wie im Original. */
    public static final List<Pair<AStack, Integer>> boosters = new ArrayList<>();

    static {
        booster(NtmItems.NUGGET_CO60.get(), 20);
        booster(NtmItems.BILLET_CO60.get(), 120);
        booster(NtmItems.INGOT_CO60.get(), 200);
        booster(NtmItems.POWDER_CO60.get(), 200);
        booster(NtmItems.NUGGET_SR90.get(), 40);
        booster(NtmItems.POWDER_SR90_TINY.get(), 40);
        booster(NtmItems.BILLET_SR90.get(), 240);
        booster(NtmItems.INGOT_SR90.get(), 400);
        booster(NtmItems.POWDER_SR90.get(), 400);
        booster(NtmItems.NUGGET_AU198.get(), 60);
        booster(NtmItems.BILLET_AU198.get(), 360);
        booster(NtmItems.INGOT_AU198.get(), 600);
        booster(NtmItems.POWDER_AU198.get(), 600);
        booster(NtmItems.POWDER_I131_TINY.get(), 60);
        booster(NtmItems.POWDER_I131.get(), 600);
        booster(NtmItems.POWDER_XE135_TINY.get(), 60);
        booster(NtmItems.POWDER_XE135.get(), 600);
        booster(NtmItems.POWDER_CS137_TINY.get(), 50);
        booster(NtmItems.POWDER_CS137.get(), 500);
        booster(NtmItems.POWDER_AT209.get(), 1_200);
    }

    private static void booster(net.minecraft.world.item.Item item, int ticks) {
        boosters.add(new Pair<>(new ComparableStack(item), ticks));
    }

    public FusionPlasmaForgeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FUSION_PLASMA_FORGE.get(), pos, state, 16);

        this.inputTank = new FluidTank(Fluids.NONE, 16_000);

        this.plasmaModule = new ModuleMachinePlasma(0, this, this.slots)
                .itemInput(SLOT_INPUT_FIRST).itemOutput(SLOT_OUTPUT).fluidInput(this.inputTank);

        this.armStriker = new ForgeArm(ForgeArmType.STRIKER);
        this.armJet = new ForgeArm(ForgeArmType.JET);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.machinePlasmaForge");
    }

    @Override public boolean receivesFusionPower() { return true; }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) {
        this.plasmaEnergy = fusionPower;
        this.neutronEnergy = neutronPower;
        this.plasmaRed = r;
        this.plasmaGreen = g;
        this.plasmaBlue = b;
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(this.maxPower <= 0) this.maxPower = 1_000_000;

        if(!this.level.isClientSide) {
            this.serverUpdate();
        } else {
            this.clientUpdate();
        }
    }

    private void serverUpdate() {

        this.plasmaEnergySync = this.plasmaEnergy;
        this.plasmaEnergy = 0;

        /* Ein neues Isotop wird erst eingezogen, wenn das alte aufgebraucht ist. */
        if(this.booster <= 0 && !this.slots.get(SLOT_BOOSTER).isEmpty()) {
            for(Pair<AStack, Integer> entry : boosters) {
                if(entry.getKey().matchesRecipe(this.slots.get(SLOT_BOOSTER), true)) {
                    this.maxBooster = this.booster = entry.getValue();
                    this.removeItem(SLOT_BOOSTER, 1);
                    break;
                }
            }
        }

        Direction rot = this.getBlockState().getValue(DummyableBlock.FACING).getClockWise();

        this.receiverNode = this.ensureNode(this.receiverNode, rot);
        this.providerNode = this.ensureNode(this.providerNode, rot.getOpposite());

        FusionNodes.subscribe(this.receiverNode, this);
        /* Technisch ungenutzt, aber der Knoten hinter uns muss da sein, damit die Kette weitergeht. */
        FusionNodes.provide(this.providerNode, this);

        PlasmaForgeRecipe recipe = (PlasmaForgeRecipe) this.plasmaModule.getRecipe();
        if(recipe != null) this.maxPower = recipe.power * 100;

        this.maxPower = BobMathUtil.max(this.power, this.maxPower, 100_000);
        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, this.maxPower);

        for(DirPos pos : this.getConPos()) {
            this.trySubscribe(this.level, pos);
            if(this.inputTank.getTankType() != Fluids.NONE) this.trySubscribe(this.inputTank.getTankType(), this.level, pos);
        }

        double speed = this.booster > 0 ? 4D : 1D;
        boolean ignition = recipe == null || recipe.ignitionTemp <= this.plasmaEnergySync;

        this.plasmaModule.update(speed, 1D, ignition, this.slots.get(SLOT_BLUEPRINT));
        this.didProcess = this.plasmaModule.didProcess;
        if(this.plasmaModule.markDirty) this.setChanged();

        this.forwardPlasma();

        if(this.didProcess && this.booster > 0) this.booster--;
        this.neutronEnergy = 0D;

        this.networkPackNT(100);
    }

    /** Drei Viertel der Plasmaleistung gehen an das naechste Geraet hinter uns. */
    private void forwardPlasma() {

        this.connected = false;
        if(this.providerNode == null || !this.providerNode.hasValidNet()) return;

        this.connected = !this.providerNode.net.receiverEntries.isEmpty();

        long forwarded = (long) Math.ceil(this.plasmaEnergySync * 0.75D);
        if(forwarded <= 0) return;

        for(Object key : this.providerNode.net.receiverEntries.keySet()) {
            if(key instanceof IFusionPowerReceiver receiver) {
                receiver.receiveFusionPower(forwarded, this.neutronEnergy, this.plasmaRed, this.plasmaGreen, this.plasmaBlue);
            }
        }
    }

    /** Der Knoten liegt fuenf Bloecke seitlich, auf Hoehe der Muendung. */
    private GenNode<?> ensureNode(GenNode<?> node, Direction dir) {

        BlockPos nodePos = this.worldPosition.offset(dir.getStepX() * 5, 2, dir.getStepZ() * 5);
        DirPos connection = new DirPos(this.worldPosition.offset(dir.getStepX() * 6, 2, dir.getStepZ() * 6), dir);

        return FusionNodes.ensure(node, this.level, nodePos, connection, PlasmaNetworkProvider.THE_PROVIDER);
    }

    private void clientUpdate() {

        if(this.timeOffset == -1) this.timeOffset = this.level.random.nextInt(30_000);

        this.armStriker.updateArm();
        this.armJet.updateArm();

        this.prevRing = this.ring;

        if(!this.didProcess) return;

        if(this.ring != this.ringTarget) {

            double delta = Math.abs(this.ringTarget - this.ring);
            if(delta <= this.ringSpeed) this.ring = this.ringTarget;
            if(this.ringTarget > this.ring) this.ring += this.ringSpeed;
            if(this.ringTarget < this.ring) this.ring -= this.ringSpeed;

            if(this.ringTarget == this.ring) {
                /* Der Ring dreht endlos; damit die Zahlen nicht davonlaufen, wird um 360 Grad
                 * zurueckgesetzt, sobald ein Ziel erreicht ist. */
                double sub = this.ringTarget >= 360 ? -360D : 360D;
                this.ringTarget += sub;
                this.ring += sub;
                this.prevRing += sub;
                this.ringDelay = 100 + this.level.random.nextInt(41);
            }

        } else {
            if(this.ringDelay > 0) this.ringDelay--;
            if(this.ringDelay <= 0) {
                this.ringTarget += (this.level.random.nextDouble() + 1) * 60 * (this.level.random.nextBoolean() ? -1 : 1);
                this.ringSpeed = 2.5D;
            }
        }
    }

    public DirPos[] getConPos() {

        Direction dir = this.getBlockState().getValue(DummyableBlock.FACING);
        Direction rot = dir.getClockWise();

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        DirPos[] positions = new DirPos[10];

        /* Je fuenf Anschluesse an den beiden Stirnseiten, sechs Bloecke vom Kern entfernt. */
        for(int i = -2; i <= 2; i++) {
            positions[i + 2] = new DirPos(
                    x + dir.getStepX() * 6 + rot.getStepX() * i, y, z + dir.getStepZ() * 6 + rot.getStepZ() * i, dir);
            positions[i + 7] = new DirPos(
                    x - dir.getStepX() * 6 + rot.getStepX() * i, y, z - dir.getStepZ() * 6 + rot.getStepZ() * i, dir.getOpposite());
        }

        return positions;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        FusionNodes.destroy(this.level, this.receiverNode);
        FusionNodes.destroy(this.level, this.providerNode);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.inputTank.serialize(buf);
        buf.writeFloat(this.plasmaRed);
        buf.writeFloat(this.plasmaGreen);
        buf.writeFloat(this.plasmaBlue);
        buf.writeLong(this.plasmaEnergySync);
        buf.writeLong(this.power);
        buf.writeLong(this.maxPower);
        buf.writeBoolean(this.didProcess);
        buf.writeBoolean(this.connected);
        buf.writeInt(this.booster);
        buf.writeInt(this.maxBooster);
        this.plasmaModule.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.inputTank.deserialize(buf);
        this.plasmaRed = buf.readFloat();
        this.plasmaGreen = buf.readFloat();
        this.plasmaBlue = buf.readFloat();
        this.plasmaEnergySync = buf.readLong();
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.didProcess = buf.readBoolean();
        this.connected = buf.readBoolean();
        this.booster = buf.readInt();
        this.maxBooster = buf.readInt();
        this.plasmaModule.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.inputTank.readFromNBT(tag, "i");
        this.power = tag.getLong("power");
        this.maxPower = tag.getLong("maxPower");
        this.booster = tag.getInt("booster");
        this.maxBooster = tag.getInt("maxBooster");
        this.plasmaModule.readFromNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.inputTank.writeToNBT(tag, "i");
        tag.putLong("power", this.power);
        tag.putLong("maxPower", this.maxPower);
        tag.putInt("booster", this.booster);
        tag.putInt("maxBooster", this.maxBooster);
        this.plasmaModule.writeToNBT(tag);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return true;
        if(slot == SLOT_BLUEPRINT) return stack.getItem() == NtmItems.BLUEPRINTS.get();
        if(this.plasmaModule.isItemValid(slot, stack)) return true;

        if(slot == SLOT_BOOSTER) {
            for(Pair<AStack, Integer> entry : boosters) {
                if(entry.getKey().matchesRecipe(stack, true)) return true;
            }
        }

        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_OUTPUT || this.plasmaModule.isSlotClogged(index);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return this.maxPower; }

    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {this.inputTank}; }
    @Override public FluidTank[] getAllTanks() { return new FluidTank[] {this.inputTank}; }

    @Override public boolean hasPermission(Player player) { return this.stillValid(player); }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("index") && tag.contains("selection")) {
            if(tag.getInt("index") == 0) {
                this.plasmaModule.recipe = tag.getString("selection");
                this.setChanged();
            }
        }
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            this.renderBox = new AABB(x - 5, y, z - 5, x + 5, y + 6, z + 6);
        }
        return this.renderBox;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FusionPlasmaForgeMenu(id, inventory, this);
    }

    /* ----- Die beiden Arme ----- */

    /**
     * Ein Arm der Schmiede. Er faehrt eine Reihe von Zielwinkeln an; welche und in welcher
     * Reihenfolge, entscheidet der Zustandsautomat seiner Sorte.
     */
    public class ForgeArm {

        public final ForgeArmType type;
        public ForgeArmState state = ForgeArmState.RETIRE;
        public final double[] angles;
        public final double[] prevAngles;
        public final double[] targetAngles;
        public final double[] speed;
        public int actionDelay = 0;

        public ForgeArm(ForgeArmType type) {
            this.type = type;
            this.angles = new double[type.angleCount];
            this.prevAngles = new double[type.angleCount];
            this.targetAngles = new double[type.angleCount];
            this.speed = new double[type.angleCount];

            /* Die ersten fuenf Werte sind Winkel und drehen schnell, alles danach sind
             * Verschiebungen der Schlaghaemmer und laufen langsam. */
            for(int i = 0; i < this.speed.length; i++) this.speed[i] = i > 4 ? 0.5D : 15D;
        }

        public void updateArm() {

            System.arraycopy(this.angles, 0, this.prevAngles, 0, this.angles.length);

            if(!FusionPlasmaForgeBlockEntity.this.didProcess) this.state = ForgeArmState.RETIRE;
            if(this.state == ForgeArmState.RETIRE) this.actionDelay = 0;

            if(this.actionDelay > 0) {
                this.actionDelay--;
                return;
            }

            this.type.stateMachine.accept(this);
        }

        /** Faehrt alle Winkel um einen Schritt Richtung Ziel. Gibt true zurueck, wenn nichts mehr geht. */
        public boolean move() {

            boolean didMove = false;

            for(int i = 0; i < this.angles.length; i++) {

                if(this.angles[i] == this.targetAngles[i]) continue;
                didMove = true;

                double delta = Math.abs(this.angles[i] - this.targetAngles[i]);

                if(delta <= this.speed[i]) {
                    this.angles[i] = this.targetAngles[i];
                    continue;
                }

                if(this.angles[i] < this.targetAngles[i]) this.angles[i] += this.speed[i];
                else this.angles[i] -= this.speed[i];
            }

            return !didMove;
        }

        public void playStrikerSound() {
            FusionPlasmaForgeBlockEntity.this.playBoltgunSound();
        }

        /** Die Winkel, zwischen zwei Ticks interpoliert -- so ruckelt der Arm nicht. */
        public double[] getPositions(float interp) {
            double[] pos = new double[this.angles.length];
            for(int i = 0; i < pos.length; i++) {
                pos[i] = this.prevAngles[i] + (this.angles[i] - this.prevAngles[i]) * interp;
            }
            return pos;
        }
    }

    /** Wird vom Schlagarm gerufen, wenn ein Hammer auftrifft. */
    protected void playBoltgunSound() {
        if(this.level == null || !this.level.isClientSide) return;
        this.level.playLocalSound(
                this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
                com.hbm.registry.NtmSoundEvents.BOLTGUN.get(), net.minecraft.sounds.SoundSource.BLOCKS,
                this.getVolume(0.25F), 1.25F, false);
    }

    public enum ForgeArmType {
        /* Drehgelenk, Unterarm, Oberarm, Halterung, Hammer rechts, Hammer links. */
        STRIKER(6),
        /* Drehgelenk, Unterarm, Oberarm, Duese. */
        JET(4);

        protected final int angleCount;
        protected Consumer<ForgeArm> stateMachine;

        ForgeArmType(int angleCount) {
            this.angleCount = angleCount;
        }
    }

    public enum ForgeArmState {
        REPOSITION,
        EXTEND1,
        EXTEND2,
        RETRACT1,
        RETRACT2,
        RETIRE
    }

    public static final Random RAND = new Random();

    public static final double[][] STRIKER_POSITIONS = new double[][] {
            {20, -30, -20, 30},
            {45, -80, 15, 30},
            {30, -45, -10, 30},
            {15, -20, -30, 30},
            {0, 10, -55, 30}
    };

    public static final double[][] JET_POSITIONS = new double[][] {
            {10, 45, -120},
            {20, 45, -140},
            {0, 30, -80},
            {0, 40, -100},
            {30, 50, -160}
    };

    static {
        /*
         * Der Schlagarm: hinfahren, rechts zuschlagen, links zuschlagen, und in einem von drei
         * Faellen eine neue Stelle suchen.
         */
        ForgeArmType.STRIKER.stateMachine = arm -> {
            switch(arm.state) {
                case REPOSITION -> {
                    if(arm.move()) {
                        arm.actionDelay = 5;
                        arm.state = ForgeArmState.EXTEND1;
                        arm.targetAngles[4] = 0.5D;
                    }
                }
                case EXTEND1 -> {
                    if(arm.move()) {
                        arm.actionDelay = 0;
                        arm.state = ForgeArmState.RETRACT1;
                        arm.targetAngles[4] = 0D;
                        arm.playStrikerSound();
                    }
                }
                case RETRACT1 -> {
                    if(arm.move()) {
                        arm.actionDelay = 0;
                        arm.state = ForgeArmState.EXTEND2;
                        arm.targetAngles[5] = 0.5D;
                    }
                }
                case EXTEND2 -> {
                    if(arm.move()) {
                        arm.actionDelay = 0;
                        arm.state = ForgeArmState.RETRACT2;
                        arm.targetAngles[5] = 0D;
                        arm.playStrikerSound();
                    }
                }
                case RETRACT2 -> {
                    if(arm.move()) {
                        if(RAND.nextInt(3) == 0) {
                            arm.actionDelay = 10;
                            arm.state = ForgeArmState.REPOSITION;
                            choosePosition(arm, STRIKER_POSITIONS);
                        } else {
                            arm.actionDelay = 5;
                            arm.state = ForgeArmState.EXTEND1;
                            arm.targetAngles[4] = 0.5D;
                        }
                    }
                }
                case RETIRE -> {
                    for(int i = 0; i < arm.targetAngles.length; i++) arm.targetAngles[i] = 0;
                    if(arm.move()) {
                        arm.actionDelay = 10;
                        arm.state = ForgeArmState.REPOSITION;
                        choosePosition(arm, STRIKER_POSITIONS);
                    }
                }
            }
        };

        /* Der Brennerarm kennt nur zwei Zustaende: hinfahren und warten. */
        ForgeArmType.JET.stateMachine = arm -> {
            switch(arm.state) {
                case REPOSITION -> {
                    if(arm.move()) {
                        arm.actionDelay = 20 + RAND.nextInt(3) * 10;
                        arm.state = ForgeArmState.REPOSITION;
                        choosePosition(arm, JET_POSITIONS);
                    }
                }
                case RETIRE -> {
                    for(int i = 0; i < arm.targetAngles.length; i++) arm.targetAngles[i] = 0;
                    if(arm.move()) {
                        arm.actionDelay = 10;
                        arm.state = ForgeArmState.REPOSITION;
                        choosePosition(arm, JET_POSITIONS);
                    }
                }
                default -> { }
            }
        };
    }

    public static void choosePosition(ForgeArm arm, double[][] positions) {
        double[] newPos = positions[RAND.nextInt(positions.length)];
        System.arraycopy(newPos, 0, arm.targetAngles, 0, newPos.length);
    }
}
