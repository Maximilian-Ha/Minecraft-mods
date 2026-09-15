package com.hbm.blockentity.machine.rbmk;

import api.hbm.fluidmk2.FluidNetMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.entity.projectile.RBMKDebris.DebrisType;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.menus.RBMKBoilerMenu;
import com.hbm.registry.NtmSoundEvents;
import com.hbm.util.SoundUtils;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.util.particle.ParticleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKBoiler.
 *
 * Der Dampferzeuger. Er zieht die Waerme aus der Saeule, verdampft damit Speisewasser und gibt
 * den Dampf nach oben ab. Ueber den Verdichter laesst sich die Dampfstufe durchschalten: je
 * heisser, desto weniger Dampf je Millibucket Wasser, dafuer mit hoeherem Druck.
 */
public class RBMKBoilerBlockEntity extends RBMKSlottedBaseBlockEntity implements IControlReceiver, IFluidStandardTransceiverMK2 {

    public FluidTank feed;
    public FluidTank steam;
    /** Nur zur Anzeige: Verbrauch und Ausstoss des letzten Ticks. */
    public int consumption;
    public int output;
    protected int ventDelay;

    public RBMKBoilerBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_BOILER.get(), pos, state, 0);

        this.feed = new FluidTank(Fluids.WATER, 10_000);
        this.steam = new FluidTank(Fluids.STEAM, 1_000_000);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rbmkBoiler");
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.consumption = 0;
        this.output = 0;
        if(this.ventDelay > 0) this.ventDelay--;

        double heatCap = getHeatFromSteam(this.steam.getTankType());
        double heatProvided = this.heat - heatCap;

        if(heatProvided > 0) {

            double heatPerMbWater = RBMKDials.getBoilerHeatConsumption(this.level);
            double steamFactor = getFactorFromSteam(this.steam.getTankType());
            int waterUsed;
            int steamProduced;

            if(this.steam.getTankType() == Fluids.ULTRAHOTSTEAM) {
                // Beim heissesten Dampf wird von der Dampfmenge rueckwaerts gerechnet, sonst
                // verschluckt die Ganzzahldivision durch 1000 den gesamten Ausstoss.
                steamProduced = (int) Math.floor((heatProvided / heatPerMbWater) * 100D / steamFactor);
                waterUsed = (int) Math.floor(steamProduced / 100D * steamFactor);

                if(this.feed.getFill() < waterUsed) {
                    steamProduced = (int) Math.floor(this.feed.getFill() * 100D / steamFactor);
                    waterUsed = (int) Math.floor(steamProduced / 100D * steamFactor);
                }
            } else {
                waterUsed = (int) Math.floor(heatProvided / heatPerMbWater);
                waterUsed = Math.min(waterUsed, this.feed.getFill());
                steamProduced = (int) Math.floor((waterUsed * 100D) / steamFactor);
            }

            this.consumption = waterUsed;
            this.output = steamProduced;

            this.feed.setFill(this.feed.getFill() - waterUsed);
            this.steam.setFill(this.steam.getFill() + steamProduced);

            if(this.steam.getFill() > this.steam.getMaxFill()) {

                this.steam.setFill(this.steam.getMaxFill());

                if(this.ventDelay <= 0) {
                    this.blowOff();
                    this.ventDelay = 20 + this.level.random.nextInt(10);
                }
            }

            this.heat -= waterUsed * heatPerMbWater;
        }

        this.trySubscribe(this.feed.getTankType(), this.level, new DirPos(this.worldPosition.below(), Direction.DOWN));

        if(this.steam.getFill() > 0) {
            for(DirPos pos : this.getOutputPos()) this.tryProvide(this.steam, this.level, pos);
        }

        super.updateEntity();
    }

    /** Blaest ueberschuessigen Dampf oben aus der Saeule ab. */
    private void blowOff() {

        int height = RBMKDials.getColumnHeight(this.level);
        BlockPos top = this.worldPosition.above(height);

        // Abweichung vom Original: dort steigt hier ein eigener Dampfstrahl auf. Der zugehoerige
        // Partikel gehoert zu den RBMK-Renderern und kommt mit ihnen; bis dahin tut es eine Wolke.
        ParticleUtil.addParticle(this.level, ParticleTypes.CLOUD,
                top.getX() + 0.25 + this.level.random.nextInt(2) * 0.5,
                top.getY(),
                top.getZ() + 0.25 + this.level.random.nextInt(2) * 0.5);

        SoundUtils.playAtVec3(this.level, Vec3.atCenterOf(top), NtmSoundEvents.STEAM_ENGINE_OPERATE.get(), SoundSource.BLOCKS,
                2F, 1F + this.level.random.nextFloat() * 0.25F);
    }

    /** Ab dieser Saeulentemperatur laesst sich die jeweilige Dampfstufe erzeugen. */
    public static double getHeatFromSteam(FluidType type) {
        if(type == Fluids.STEAM) return 100D;
        if(type == Fluids.HOTSTEAM) return 300D;
        if(type == Fluids.SUPERHOTSTEAM) return 450D;
        if(type == Fluids.ULTRAHOTSTEAM) return 600D;
        return 0D;
    }

    /** Wie viel Wasser je 100 mB Dampf noetig ist. */
    public static double getFactorFromSteam(FluidType type) {
        if(type == Fluids.STEAM) return 1D;
        if(type == Fluids.HOTSTEAM) return 10D;
        if(type == Fluids.SUPERHOTSTEAM) return 100D;
        if(type == Fluids.ULTRAHOTSTEAM) return 1000D;
        return 0D;
    }

    @Override
    public FluidTank[] getSendingTanks() { return new FluidTank[] { this.steam }; }

    @Override
    public FluidTank[] getReceivingTanks() { return new FluidTank[] { this.feed }; }

    @Override
    public FluidTank[] getAllTanks() { return new FluidTank[] { this.feed, this.steam }; }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) < 400;
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("compression")) this.cycleCompressor();
    }

    /** Schaltet die Dampfstufe weiter. Geht nur, wenn der Kessel kalt und leer ist. */
    public void cycleCompressor() {

        if(this.heat > 50 && this.feed.getFill() > 0) return;

        FluidType type = this.steam.getTankType();

        if(type == Fluids.STEAM) {              this.steam.setTankType(Fluids.HOTSTEAM);        this.steam.setFill(this.steam.getFill() / 10); }
        else if(type == Fluids.HOTSTEAM) {      this.steam.setTankType(Fluids.SUPERHOTSTEAM);   this.steam.setFill(this.steam.getFill() / 10); }
        else if(type == Fluids.SUPERHOTSTEAM) { this.steam.setTankType(Fluids.ULTRAHOTSTEAM);   this.steam.setFill(this.steam.getFill() / 10); }
        else if(type == Fluids.ULTRAHOTSTEAM) { this.steam.setTankType(Fluids.STEAM);           this.steam.setFill(Math.min(this.steam.getFill() * 1000, this.steam.getMaxFill())); }

        this.setChanged();
    }

    @Override
    public void getLookInfo(List<Component> text) {
        text.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN)
                .append(this.steam.getTankType().getName().copy().withStyle(ChatFormatting.RESET))
                .append(Component.literal(" " + this.output + " mB/t")));
        text.add(Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(this.feed.getTankType().getName().copy().withStyle(ChatFormatting.RESET))
                .append(Component.literal(" " + this.consumption + " mB/t")));
    }

    @Override
    public RBMKColumnType getConsoleType() {
        return RBMKColumnType.BOILER;
    }

    @Override
    public CompoundTag getNBTForConsole() {
        CompoundTag data = super.getNBTForConsole();
        data.putInt("water", this.feed.getFill());
        data.putInt("maxWater", this.feed.getMaxFill());
        data.putInt("steam", this.steam.getFill());
        data.putInt("maxSteam", this.steam.getMaxFill());
        data.putShort("type", (short) this.steam.getTankType().getID());
        return data;
    }

    @Override
    public RBMKType getRBMKType() {
        return RBMKType.OTHER;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.feed.readFromNBT(tag, "feed");
        this.steam.readFromNBT(tag, "steam");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.feed.writeToNBT(tag, "feed");
        this.steam.writeToNBT(tag, "steam");
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        this.steam.serialize(buf);
        this.feed.serialize(buf);
        buf.writeInt(this.consumption);
        buf.writeInt(this.output);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.steam.deserialize(buf);
        this.feed.deserialize(buf);
        this.consumption = buf.readInt();
        this.output = buf.readInt();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RBMKBoilerMenu(id, inventory, this);
    }

    /* Der Dampferzeuger wirft Stahltraeger aus. */
    @Override
    public void onMelt(int reduce) {

        if(this.level != null && !this.level.isClientSide) {

            int count = 1 + this.level.random.nextInt(2);
            for(int i = 0; i < count; i++) this.spawnDebris(DebrisType.BLANK);

            /*
             * Der Dampferzeuger ist die einzige Saeule mit einem Anschluss nach draussen. Beim
             * Schmelzen merkt er sich sein Rohrnetz, damit die Kernschmelze es hinterher
             * zerreissen kann -- das ist der Ueberdruck.
             */
            if(RBMKDials.getOverpressure(this.level)) {
                for(DirPos pos : this.getOutputPos()) {
                    GenNode<FluidNetMK2> node = UniNodespace.getNode(this.level, pos.makeCompat(), this.steam.getTankType().getNetworkProvider());
                    if(node != null && node.hasValidNet()) pipes.add(node.net);
                }
            }
        }

        super.onMelt(reduce);
    }
}
