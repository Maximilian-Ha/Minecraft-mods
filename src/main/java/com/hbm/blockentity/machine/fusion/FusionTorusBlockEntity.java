package com.hbm.blockentity.machine.fusion;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.hbm.blockentity.CooledBaseBlockEntity;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.FusionTorusMenu;
import com.hbm.inventory.recipes.FusionRecipe;
import com.hbm.items.NtmItems;
import com.hbm.lib.Library;
import com.hbm.module.machine.ModuleMachineFusion;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.sound.AudioWrapper;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.KlystronNetworkProvider;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.fusion.TileEntityFusionTorus.
 *
 * Der Fusionsreaktor. Er steht in der Mitte von vier Armen; an jedem Arm haengt entweder ein
 * Klystron, das Zuendenergie liefert, oder ein Abnehmer, der die Plasmawaerme abholt. Beides sind
 * eigene Netze -- das Klystronnetz zum Torus hin, das Plasmanetz von ihm weg.
 *
 * WIE ER RECHNET
 *
 * Das Tempo ist kein Schalter, sondern ein Regler: Strom und Brennstofftanks zaehlen jeweils
 * anteilig, und der kleinste der Faktoren gibt das Tempo vor. Halb voll heisst volle Fahrt, alles
 * darunter bremst linear. Gezuendet wird nur, wenn die Klystrons zusammen die Schwelle des
 * Rezepts erreichen -- und die Anlage muss auf 123 Kelvin heruntergekuehlt sein.
 *
 * Die Plasmawaerme teilen sich alle Abnehmer. Zwei bekommen je 62,5 Prozent statt 50, drei je 50
 * statt 33 -- wer mehr anschliesst, bekommt insgesamt mehr heraus, aber je Anschluss weniger.
 *
 * ABWEICHUNGEN
 *
 * - NICHT UEBERNOMMEN: OpenComputers, Redstone-over-Radio und die Satellitenmeldung
 *   (ENTSCHEIDUNGEN.md).
 */
public class FusionTorusBlockEntity extends CooledBaseBlockEntity implements IControlReceiver {

    private AABB renderBox;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_BLUEPRINT = 1;
    public static final int SLOT_OUTPUT = 2;

    public static final long MAX_POWER = 10_000_000;

    public boolean didProcess = false;

    public final FluidTank[] tanks = new FluidTank[4];
    public final ModuleMachineFusion fusionModule;

    protected final GenNode<?>[] klystronNodes = new GenNode<?>[4];
    protected final GenNode<?>[] plasmaNodes = new GenNode<?>[4];
    public final boolean[] connections = new boolean[4];

    /** Was die Klystrons in diesem Tick geliefert haben; wird jeden Tick neu gesammelt. */
    public long klystronEnergy;
    public long plasmaEnergy;
    public double fuelConsumption;

    /** Nur Client: der Winkel der Magnetringe. */
    public float magnet;
    public float prevMagnet;
    public float magnetSpeed;
    public static final float MAGNET_ACCELERATION = 0.25F;

    private AudioWrapper audio;

    public FusionTorusBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.FUSION_TORUS.get(), pos, state, 3);

        for(int i = 0; i < 4; i++) this.tanks[i] = new FluidTank(Fluids.NONE, 4_000);

        this.fusionModule = new ModuleMachineFusion(0, this, this.slots)
                .fluidInput(this.tanks[0], this.tanks[1], this.tanks[2])
                .fluidOutput(this.tanks[3])
                .itemOutput(SLOT_OUTPUT);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.fusionTorus");
    }

    @Override
    public void updateEntity() {
        if(this.level == null) return;

        if(!this.level.isClientSide) {
            this.serverUpdate();
        } else {
            this.clientUpdate();
        }
    }

    private void serverUpdate() {

        this.checkTilt(LoadedBaseBlockEntity.TiltType.CONFIG, true);

        for(int i = 0; i < 4; i++) {
            Direction dir = Direction.from2DDataValue(i);

            if(this.klystronNodes[i] == null || this.klystronNodes[i].expired) {
                this.klystronNodes[i] = this.createNode(KlystronNetworkProvider.THE_PROVIDER, dir);
            }
            if(this.plasmaNodes[i] == null || this.plasmaNodes[i].expired) {
                this.plasmaNodes[i] = this.createNode(PlasmaNetworkProvider.THE_PROVIDER, dir);
            }

            if(this.klystronNodes[i].net != null) this.klystronNodes[i].net.addReceiver(this);
            if(this.plasmaNodes[i].net != null) this.plasmaNodes[i].net.addProvider(this);
        }

        this.cool();

        for(DirPos pos : this.getConPos()) {

            if(this.level.getGameTime() % 20 == 0) {
                this.trySubscribe(this.level, pos);
                this.trySubscribe(this.coolantTanks[0].getTankType(), this.level, pos);
                for(int i = 0; i < 3; i++) {
                    if(this.tanks[i].getTankType() != Fluids.NONE) this.trySubscribe(this.tanks[i].getTankType(), this.level, pos);
                }
            }

            if(this.coolantTanks[1].getFill() > 0) this.tryProvide(this.coolantTanks[1], this.level, pos);
            if(this.tanks[3].getFill() > 0) this.tryProvide(this.tanks[3], this.level, pos);
        }

        this.power = Library.chargeTEFromItems(this.slots, SLOT_BATTERY, this.power, this.getMaxPower());

        /* Wie viele Abnehmer sich die Plasmawaerme teilen, und wie viele davon Kollektoren sind. */
        int receiverCount = 0;
        int collectors = 0;

        for(int i = 0; i < 4; i++) {

            this.connections[i] = false;

            if(this.klystronNodes[i] != null && this.klystronNodes[i].hasValidNet()
                    && !this.klystronNodes[i].net.providerEntries.isEmpty()) this.connections[i] = true;

            if(!this.connections[i] && this.plasmaNodes[i] != null && this.plasmaNodes[i].hasValidNet()
                    && !this.plasmaNodes[i].net.receiverEntries.isEmpty()) this.connections[i] = true;

            if(this.plasmaNodes[i] != null && this.plasmaNodes[i].hasValidNet()
                    && !this.plasmaNodes[i].net.receiverEntries.isEmpty()) {

                for(Object key : this.plasmaNodes[i].net.receiverEntries.keySet()) {
                    if(key instanceof LoadedBaseBlockEntity be && !be.isLoaded()) continue;
                    if(key instanceof IFusionPowerReceiver receiver && receiver.receivesFusionPower()) receiverCount++;
                    if(key instanceof FusionCollectorBlockEntity) collectors++;
                    /* Nur der erste Abnehmer je Arm zaehlt -- so im Original. */
                    break;
                }
            }
        }

        FusionRecipe recipe = (FusionRecipe) this.fusionModule.getRecipe();

        double powerFactor = getSpeedScaled(this.getMaxPower(), this.power);
        double fuel0Factor = recipe != null && recipe.inputFluid.length > 0 ? getSpeedScaled(this.tanks[0].getMaxFill(), this.tanks[0].getFill()) : 1D;
        double fuel1Factor = recipe != null && recipe.inputFluid.length > 1 ? getSpeedScaled(this.tanks[1].getMaxFill(), this.tanks[1].getFill()) : 1D;
        double fuel2Factor = recipe != null && recipe.inputFluid.length > 2 ? getSpeedScaled(this.tanks[2].getMaxFill(), this.tanks[2].getFill()) : 1D;

        double factor = Math.min(Math.min(powerFactor, fuel0Factor), Math.min(fuel1Factor, fuel2Factor));

        boolean ignition = recipe == null || recipe.ignitionTemp <= this.klystronEnergy;

        float r = 0F;
        float g = 0F;
        float b = 0F;
        this.plasmaEnergy = 0;
        this.fuelConsumption = 0;

        this.fusionModule.preUpdate(factor, collectors * 0.5D);
        this.fusionModule.update(1D, 1D, !this.tilted && this.isCool() && ignition, this.slots.get(SLOT_BLUEPRINT));
        this.didProcess = this.fusionModule.didProcess;
        if(this.fusionModule.markDirty) this.setChanged();

        if(this.didProcess && recipe != null) {
            this.plasmaEnergy = (long) Math.ceil(recipe.outputTemp * factor);
            this.fuelConsumption = factor;
            r = recipe.r;
            g = recipe.g;
            b = recipe.b;
        }

        double outputIntensity = getOutputIntensity(receiverCount);
        double outputFlux = recipe != null ? recipe.neutronFlux * factor : 0D;

        if(this.plasmaEnergy > 0) for(int i = 0; i < 4; i++) {

            if(this.plasmaNodes[i] == null || !this.plasmaNodes[i].hasValidNet()) continue;

            for(Object key : this.plasmaNodes[i].net.receiverEntries.keySet()) {
                if(key instanceof IFusionPowerReceiver receiver) {
                    long powerReceived = (long) Math.ceil(this.plasmaEnergy * outputIntensity);
                    receiver.receiveFusionPower(powerReceived, outputFlux, r, g, b);
                }
            }
        }

        this.networkPackNT(150);

        this.klystronEnergy = 0;
    }

    @OnlyIn(Dist.CLIENT)
    private void clientUpdate() {

        double powerFactor = getSpeedScaled(this.getMaxPower(), this.power);

        if(this.didProcess) this.magnetSpeed += MAGNET_ACCELERATION;
        else this.magnetSpeed -= MAGNET_ACCELERATION;

        this.magnetSpeed = Mth.clamp(this.magnetSpeed, 0F, 30F * (float) powerFactor);

        this.prevMagnet = this.magnet;
        this.magnet += this.magnetSpeed;

        if(this.magnet >= 360F) {
            this.magnet -= 360F;
            this.prevMagnet -= 360F;
        }

        Player me = Minecraft.getInstance().player;
        boolean near = me != null && me.distanceToSqr(
                this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 2.5, this.worldPosition.getZ() + 0.5) < 50 * 50;

        if(this.magnetSpeed > 0 && near) {

            float speed = this.magnetSpeed / 30F;

            if(this.audio == null) {
                this.audio = AudioWrapper.getLoopedSound(NtmSoundEvents.FUSION_REACTOR_LOOP.get(), SoundSource.BLOCKS,
                        this.worldPosition.getX() + 0.5F, this.worldPosition.getY() + 2.5F, this.worldPosition.getZ() + 0.5F,
                        this.getVolume(speed), 30F, speed, 20);
                this.audio.startSound();
            } else {
                this.audio.updateVolume(this.getVolume(speed));
                this.audio.updatePitch(speed);
                this.audio.keepAlive();
            }

        } else if(this.audio != null) {
            if(this.audio.isPlaying()) this.audio.stopSound();
            this.audio = null;
        }
    }

    /**
     * Wie stark ein einzelner Anschluss beliefert wird. Mit jedem weiteren Abnehmer steigt die
     * Gesamtausbeute, der einzelne bekommt aber weniger: 100, 125, 150, 175 Prozent auf zwei, drei
     * oder vier Anschluesse verteilt.
     */
    public static double getOutputIntensity(int receiverCount) {
        if(receiverCount == 1) return 1D;
        if(receiverCount == 2) return 0.625D;
        if(receiverCount == 3) return 0.5D;
        return 0.4375D;
    }

    /** Steigt linear von null auf volle Fahrt bei halbem Fuellstand, darueber bleibt es dabei. */
    public static double getSpeedScaled(double max, double level) {
        if(max == 0) return 0D;
        if(level >= max * 0.5) return 1D;
        return level / max * 2D;
    }

    /** Der Knoten am Ende eines Arms, sieben Bloecke vom Kern entfernt. */
    public GenNode<?> createNode(INetworkProvider<?> provider, Direction dir) {

        BlockPos nodePos = this.worldPosition.offset(dir.getStepX() * 7, 2, dir.getStepZ() * 7);

        GenNode<?> node = UniNodespace.getNode(this.level, nodePos, provider);
        if(node != null) return node;

        node = new GenNode<>(provider, nodePos)
                .setConnections(new DirPos(this.worldPosition.offset(dir.getStepX() * 8, 2, dir.getStepZ() * 8), dir));

        UniNodespace.createNode(this.level, node);

        return node;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.stopAudio();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.stopAudio();

        if(this.level != null && !this.level.isClientSide) {
            for(GenNode<?> node : this.klystronNodes) if(node != null) UniNodespace.destroyNode(this.level, node);
            for(GenNode<?> node : this.plasmaNodes) if(node != null) UniNodespace.destroyNode(this.level, node);
        }
    }

    private void stopAudio() {
        if(this.audio != null) {
            this.audio.stopSound();
            this.audio = null;
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.didProcess);
        buf.writeLong(this.klystronEnergy);
        buf.writeLong(this.plasmaEnergy);
        buf.writeDouble(this.fuelConsumption);

        this.fusionModule.serialize(buf);
        for(int i = 0; i < 4; i++) this.tanks[i].serialize(buf);
        for(int i = 0; i < 4; i++) buf.writeBoolean(this.connections[i]);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.didProcess = buf.readBoolean();
        this.klystronEnergy = buf.readLong();
        this.plasmaEnergy = buf.readLong();
        this.fuelConsumption = buf.readDouble();

        this.fusionModule.deserialize(buf);
        for(int i = 0; i < 4; i++) this.tanks[i].deserialize(buf);
        for(int i = 0; i < 4; i++) this.connections[i] = buf.readBoolean();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < 4; i++) this.tanks[i].readFromNBT(tag, "ft" + i);
        this.fusionModule.readFromNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < 4; i++) this.tanks[i].writeToNBT(tag, "ft" + i);
        this.fusionModule.writeToNBT(tag);
    }

    @Override public long getMaxPower() { return MAX_POWER; }

    @Override public FluidTank[] getSendingTanks() { return new FluidTank[] { this.coolantTanks[1], this.tanks[3] }; }
    @Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.coolantTanks[0], this.tanks[0], this.tanks[1], this.tanks[2] }; }
    @Override public FluidTank[] getAllTanks() {
        return new FluidTank[] { this.coolantTanks[0], this.coolantTanks[1], this.tanks[0], this.tanks[1], this.tanks[2], this.tanks[3] };
    }

    /**
     * Die Anschlussstellen: eine oben in der Mitte und je sechs an den vier Armen, jeweils oben
     * und unten. Zusammen 26 -- genug, um alle Fluide getrennt zu fuehren.
     */
    @Override
    public DirPos[] getConPos() {

        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return new DirPos[] {
                new DirPos(x, y - 1, z, Direction.DOWN),
                new DirPos(x, y + 5, z, Direction.UP),

                new DirPos(x + 6, y - 1, z, Direction.DOWN),
                new DirPos(x + 6, y + 5, z, Direction.UP),
                new DirPos(x + 6, y - 1, z + 2, Direction.DOWN),
                new DirPos(x + 6, y + 5, z + 2, Direction.UP),
                new DirPos(x + 6, y - 1, z - 2, Direction.DOWN),
                new DirPos(x + 6, y + 5, z - 2, Direction.UP),

                new DirPos(x - 6, y - 1, z, Direction.DOWN),
                new DirPos(x - 6, y + 5, z, Direction.UP),
                new DirPos(x - 6, y - 1, z + 2, Direction.DOWN),
                new DirPos(x - 6, y + 5, z + 2, Direction.UP),
                new DirPos(x - 6, y - 1, z - 2, Direction.DOWN),
                new DirPos(x - 6, y + 5, z - 2, Direction.UP),

                new DirPos(x, y - 1, z + 6, Direction.DOWN),
                new DirPos(x, y + 5, z + 6, Direction.UP),
                new DirPos(x + 2, y - 1, z + 6, Direction.DOWN),
                new DirPos(x + 2, y + 5, z + 6, Direction.UP),
                new DirPos(x - 2, y - 1, z + 6, Direction.DOWN),
                new DirPos(x - 2, y + 5, z + 6, Direction.UP),

                new DirPos(x, y - 1, z - 6, Direction.DOWN),
                new DirPos(x, y + 5, z - 6, Direction.UP),
                new DirPos(x + 2, y - 1, z - 6, Direction.DOWN),
                new DirPos(x + 2, y + 5, z - 6, Direction.UP),
                new DirPos(x - 2, y - 1, z - 6, Direction.DOWN),
                new DirPos(x - 2, y + 5, z - 6, Direction.UP)
        };
    }

    /* Die Standflaeche: sechs mal sechs Stuetzpunkte im Abstand von zwei Bloecken. */
    @Override public int getFloorCount() { return 6 * 6; }

    @Override
    public BlockPos getFloorPosFromIndex(int index) {
        return new BlockPos(
                this.worldPosition.getX() - 5 + (index / 6) * 2,
                this.worldPosition.getY() - 1,
                this.worldPosition.getZ() - 5 + (index % 6) * 2);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(slot == SLOT_BATTERY) return true;
        if(slot == SLOT_BLUEPRINT) return stack.getItem() == NtmItems.BLUEPRINTS.get();
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] { SLOT_OUTPUT };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_OUTPUT;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FusionTorusMenu(id, inventory, this);
    }

    @Override
    public boolean hasPermission(Player player) {
        return this.stillValid(player);
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("index") && tag.contains("selection")) {
            if(tag.getInt("index") == 0) {
                this.fusionModule.recipe = tag.getString("selection");
                this.setChanged();
            }
        }
    }

    /* Ohne @Override: die Methode stammt aus der NeoForge-Erweiterung, nicht aus BlockEntity. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            int x = this.worldPosition.getX();
            int y = this.worldPosition.getY();
            int z = this.worldPosition.getZ();
            /*
             * Masse aus dem Original uebernommen. torus.obj misst X/Z +-7,88 und Y 0 bis 5.
             */
            this.renderBox = new AABB(x - 8, y, z - 8, x + 9, y + 5, z + 9);
        }
        return this.renderBox;
    }
}
