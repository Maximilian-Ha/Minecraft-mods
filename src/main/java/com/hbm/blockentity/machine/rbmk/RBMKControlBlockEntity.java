package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.entity.projectile.RBMKDebris.DebrisType;
import com.hbm.blocks.machine.rbmk.RBMKControlBlock;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.machine.rbmk.RBMKControlBlock;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.menus.RBMKControlMenu;
import net.minecraft.ChatFormatting;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKControl zusammen mit
 * TileEntityRBMKControlManual.
 *
 * Der Steuerstab. Faehrt langsam zwischen ganz eingefahren (0) und ganz gezogen (1) und daempft
 * dabei den Neutronenfluss, der durch die Saeule laeuft.
 *
 * Abweichung vom Original: das Feld heisst dort "level". In 1.21 belegt die Block-Entitaet
 * diesen Namen bereits mit ihrer Welt, deshalb heisst der Einfahrgrad hier "rodLevel".
 * Die mit Strom betriebene ReaSim-Bauform und der automatische Steuerstab am Pult kommen in
 * einer spaeteren Runde, hier gibt es nur den Stab von Hand.
 */
public class RBMKControlBlockEntity extends RBMKSlottedBaseBlockEntity implements IControlReceiver, IEnergyReceiverMK2 {

    public double lastRodLevel;
    /** Einfahrgrad: 0 ist ganz eingefahren, 1 ist ganz gezogen. */
    public double rodLevel;
    /** Voll ausgefahren ist der Stab nach rund 18 Sekunden. */
    public static final double SPEED = 0.00277D;
    public double targetLevel;
    /** Stand beim letzten Befehl -- daraus ergibt sich die Leistungsspitze beim Einfahren. */
    public double startingLevel;
    /** Farbgruppe fuer das Reaktorpult, null heisst keiner Gruppe zugeteilt. */
    public RBMKColor color;

    /* Nur die ReaSim-Bauformen brauchen Strom; die uebrigen fahren von selbst. */
    public boolean hasPower = true;
    public long power;
    public static final long CONSUMPTION = 5_000;
    /** Puffer fuer eine halbe Sekunde Fahrt. */
    public static final long MAX_POWER = CONSUMPTION * 10;

    public RBMKControlBlockEntity(BlockPos pos, BlockState state) {
        this(NtmBlockEntityTypes.RBMK_CONTROL.get(), pos, state);
    }

    /** Fuer Ableitungen wie den selbsttaetigen Steuerstab, die einen eigenen Typ mitbringen. */
    protected RBMKControlBlockEntity(BlockEntityType<? extends LoadedBaseBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.rbmkControl");
    }

    @Override
    public boolean isLidRemovable() {
        return false;
    }

    @Override
    public boolean isModerated() {
        return this.getBlockState().getBlock() instanceof RBMKControlBlock control && control.moderated;
    }

    @Override
    public int trackingRange() {
        return 100;
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        this.lastRodLevel = this.rodLevel;

        if(this.level.isClientSide) return;

        this.hasPower = true;

        if(this.isPowered()) {
            this.trySubscribe(this.level, new DirPos(this.worldPosition.below(), Direction.DOWN));
            if(this.power < CONSUMPTION) this.hasPower = false;
        }

        if(!this.hasPower) {
            super.updateEntity();
            return;
        }

        double before = this.rodLevel;
        double speed = SPEED * RBMKDials.getControlSpeed(this.level);

        if(this.rodLevel < this.targetLevel) {
            this.rodLevel += speed;
            if(this.rodLevel > this.targetLevel) this.rodLevel = this.targetLevel;
        }

        if(this.rodLevel > this.targetLevel) {
            this.rodLevel -= speed;
            if(this.rodLevel < this.targetLevel) this.rodLevel = this.targetLevel;
        }

        if(this.isPowered() && this.rodLevel != before) this.power -= CONSUMPTION;

        super.updateEntity();
    }

    /** Nur die ReaSim-Bauformen haengen am Stromnetz. */
    public boolean isPowered() {
        return this.getBlockState().getBlock() instanceof RBMKControlBlock control && control.powered;
    }

    @Override public long getPower() { return this.power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return this.isPowered() ? MAX_POWER : 0; }

    @Override
    public boolean canConnect(Direction dir) {
        return this.isPowered() && dir == Direction.DOWN;
    }

    /**
     * Niedrige Vorrangstufe, genau wie im Original -- die Steuerstaebe sollen sich nicht vor
     * den Maschinen bedienen, die tatsaechlich etwas herstellen.
     */
    @Override
    public ConnectionPriority getPriority() {
        return ConnectionPriority.LOW;
    }

    public void setTarget(double target) {
        this.targetLevel = Mth.clamp(target, 0D, 1D);
        this.startingLevel = this.rodLevel;
        this.setChanged();
    }

    /** Der Multiplikator, mit dem ein durchlaufender Strom gedaempft wird. */
    public double getMult() {

        double surge = 0;

        if(this.targetLevel < this.startingLevel && Math.abs(this.rodLevel - this.targetLevel) > 0.01D && this.level != null) {
            surge = Math.sin(Math.pow((1D - this.rodLevel), 15) * Math.PI) * (this.startingLevel - this.targetLevel) * RBMKDials.getSurgeMod(this.level);
        }

        return this.rodLevel + surge;
    }

    @Override
    public void getLookInfo(List<Component> text) {
        text.add(Component.translatable("trait.rbmk.look.extraction",
                (int) (this.rodLevel * 100), (int) (this.targetLevel * 100)).withStyle(ChatFormatting.AQUA));
    }

    @Override
    public RBMKColumnType getConsoleType() {
        return RBMKColumnType.CONTROL;
    }

    @Override
    public CompoundTag getNBTForConsole() {
        CompoundTag data = super.getNBTForConsole();
        data.putDouble("level", this.rodLevel);
        data.putDouble("targetLevel", this.targetLevel);
        data.putShort("color", (short) (this.color != null ? this.color.ordinal() : -1));
        return data;
    }

    @Override
    public RBMKType getRBMKType() {
        return RBMKType.CONTROL_ROD;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.rodLevel = tag.getDouble("rodLevel");
        this.targetLevel = tag.getDouble("targetLevel");
        this.startingLevel = tag.getDouble("startingLevel");
        this.color = tag.contains("color") ? RBMKColor.byIndex(tag.getInt("color")) : null;
        this.power = tag.getLong("power");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("rodLevel", this.rodLevel);
        tag.putDouble("targetLevel", this.targetLevel);
        tag.putDouble("startingLevel", this.startingLevel);
        if(this.color != null) tag.putInt("color", this.color.ordinal());
        tag.putLong("power", this.power);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.rodLevel);
        buf.writeDouble(this.targetLevel);
        buf.writeDouble(this.startingLevel);
        buf.writeInt(this.color != null ? this.color.ordinal() : -1);
        buf.writeLong(this.power);
        buf.writeBoolean(this.hasPower);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.rodLevel = buf.readDouble();
        this.targetLevel = buf.readDouble();
        this.startingLevel = buf.readDouble();
        this.color = RBMKColor.byIndex(buf.readInt());
        this.power = buf.readLong();
        this.hasPower = buf.readBoolean();
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) < 400;
    }

    @Override
    public void receiveControl(CompoundTag tag) {
        if(tag.contains("level")) this.setTarget(tag.getDouble("level"));

        if(tag.contains("color")) {
            // Ein zweites Mal dieselbe Farbe hebt die Zuteilung wieder auf, wie im Original.
            RBMKColor newColor = RBMKColor.byIndex(Math.abs(tag.getInt("color")) % RBMKColor.values().length);
            this.color = newColor == this.color ? null : newColor;
            this.setChanged();
        }
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RBMKControlMenu(id, inventory, this);
    }

    /*
     * Der Steuerstab wirft seinen Borstab aus, und wenn er moderiert ist, zusaetzlich Graphit.
     * Anders als die uebrigen Saeulen ruft er standardMelt direkt auf -- der Steuerstab hat im
     * Original nie einen Deckel.
     */
    @Override
    public void onMelt(int reduce) {

        if(this.level != null && !this.level.isClientSide) {

            if(this.isModerated()) {
                int graphite = 2 + this.level.random.nextInt(2);
                for(int i = 0; i < graphite; i++) this.spawnDebris(DebrisType.GRAPHITE);
            }

            int rods = 2 + this.level.random.nextInt(2);
            for(int i = 0; i < rods; i++) this.spawnDebris(DebrisType.ROD);
        }

        this.standardMelt(reduce);
    }
}
