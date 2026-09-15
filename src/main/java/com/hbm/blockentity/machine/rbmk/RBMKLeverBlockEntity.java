package com.hbm.blockentity.machine.rbmk;

import com.hbm.blockentity.ITickable;
import com.hbm.blockentity.LoadedBaseBlockEntity;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.network.RTTYSystem;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.network.toclient.AuxParticle;
import com.hbm.registry.NtmSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.rbmk.TileEntityRBMKLever.
 *
 * Zwei Kipphebel auf einer Tafel. Jeder schickt beim Umlegen einen frei eingestellten Befehl
 * ueber einen RTTY-Kanal -- einen beim Einschalten, einen beim Ausschalten.
 *
 * Ein Hebel, der staendig sendet (polling), rastet oben ein und wiederholt seinen Befehl jeden
 * Tick. Ein Hebel ohne polling faellt von selbst zurueck und sendet je einmal.
 *
 * Der Umlegeweg dauert eine halbe Sekunde; an seinen beiden Enden schlaegt ein Lichtbogen ueber,
 * fuer den die Blockentitaet Funken an die Umstehenden schickt.
 *
 * NICHT UEBERNOMMEN: die OpenComputers-Anbindung, siehe docs/ENTSCHEIDUNGEN.md.
 */
public class RBMKLeverBlockEntity extends LoadedBaseBlockEntity implements ITickable, IControlReceiver {

    public static final int LEVERS = 2;

    public final LeverUnit[] levers = new LeverUnit[LEVERS];

    public RBMKLeverBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RBMK_LEVER.get(), pos, state);

        for(int i = 0; i < LEVERS; i++) this.levers[i] = new LeverUnit(i);
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {
            for(LeverUnit unit : this.levers) unit.update();
            this.networkPackNT(50);
        } else {
            for(LeverUnit unit : this.levers) unit.updateClient();
        }
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        for(LeverUnit unit : this.levers) unit.serialize(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        for(LeverUnit unit : this.levers) unit.deserialize(buf);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for(int i = 0; i < LEVERS; i++) this.levers[i].load(tag, i);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for(int i = 0; i < LEVERS; i++) this.levers[i].save(tag, i);
    }

    @Override
    public boolean hasPermission(Player player) {
        return player.distanceToSqr(this.worldPosition.getCenter()) < 15 * 15;
    }

    @Override
    public void receiveControl(CompoundTag data) {

        int active = data.getByte("active");
        int polling = data.getByte("polling");

        for(int i = 0; i < LEVERS; i++) {
            LeverUnit unit = this.levers[i];

            unit.active = (active & (1 << i)) != 0;
            unit.polling = (polling & (1 << i)) != 0;
            unit.label = data.getString("label" + i);
            unit.rtty = data.getString("rtty" + i);
            unit.commandOn = data.getString("cmdOn" + i);
            unit.commandOff = data.getString("cmdOff" + i);
        }

        this.setChanged();
    }

    public class LeverUnit {

        /** Ein halbes Sekundchen von ganz oben bis ganz unten. */
        public static final float FLIP_SPEED = 1F / 10F;

        public final int index;

        /** Ob der Befehl jeden Tick wiederholt wird -- dann rastet der Hebel ein. */
        public boolean polling;
        public String label = "";
        public String rtty = "";
        public String commandOn = "";
        public String commandOff = "";
        public boolean active;
        /** Wohin der Hebel gerade unterwegs ist. */
        public boolean isTurningOn;
        public float flipProgress;
        public float prevFlipProgress;
        /** Der zuletzt vom Server gemeldete Stand, auf den der Wagen zulaeuft. */
        public float flipSync;
        public int turnProgress;

        public LeverUnit(int index) {
            this.index = index;
            this.label = "Lever " + (index + 1);
        }

        /** Ein Spieler hat den Hebel angefasst. */
        public void click() {

            if(!this.active) return;

            if(this.flipProgress <= 0F || this.flipProgress >= 1F) {
                RBMKLeverBlockEntity.this.level.playSound(null, RBMKLeverBlockEntity.this.worldPosition,
                        NtmSoundEvents.LEVER_START.get(), SoundSource.BLOCKS, 1F, 1F);
            }

            this.isTurningOn = !this.isTurningOn;
            RBMKLeverBlockEntity.this.setChanged();
        }

        public void update() {

            this.prevFlipProgress = this.flipProgress;

            if(!this.active) return;

            boolean arcFlash = false;

            if(this.polling) {
                if(this.flipProgress >= 1F && this.canSend(this.commandOn)) RTTYSystem.broadcast(RBMKLeverBlockEntity.this.level, this.rtty, this.commandOn);
                if(this.flipProgress <= 0F && this.canSend(this.commandOff)) RTTYSystem.broadcast(RBMKLeverBlockEntity.this.level, this.rtty, this.commandOff);
            }

            if(this.isTurningOn && this.flipProgress < 1F) {

                this.flipProgress += FLIP_SPEED;

                if(this.flipProgress >= 1F) {
                    this.flipProgress = 1F;
                    /* Ein Hebel ohne polling meldet sich genau einmal, naemlich jetzt. */
                    if(!this.polling && this.canSend(this.commandOn)) RTTYSystem.broadcast(RBMKLeverBlockEntity.this.level, this.rtty, this.commandOn);
                    this.playStop();
                    arcFlash = true;
                }

            } else if(!this.isTurningOn && this.flipProgress > 0F) {

                /* Der Lichtbogen schlaegt schon beim Loesen aus der oberen Raste ueber. */
                if(this.prevFlipProgress >= 1F) arcFlash = true;

                this.flipProgress -= FLIP_SPEED;

                if(this.flipProgress <= 0F) {
                    this.flipProgress = 0F;
                    if(!this.polling && this.canSend(this.commandOff)) RTTYSystem.broadcast(RBMKLeverBlockEntity.this.level, this.rtty, this.commandOff);
                    this.playStop();
                }
            }

            if(arcFlash) this.sparks();
        }

        private void playStop() {
            RBMKLeverBlockEntity.this.level.playSound(null, RBMKLeverBlockEntity.this.worldPosition,
                    NtmSoundEvents.LEVER_STOP.get(), SoundSource.BLOCKS, 0.5F, 1F);
        }

        /** Funken an den beiden Kontakten des Hebels. */
        private void sparks() {

            if(!(RBMKLeverBlockEntity.this.level instanceof ServerLevel serverLevel)) return;

            BlockPos pos = RBMKLeverBlockEntity.this.worldPosition;

            serverLevel.playSound(null, pos, NtmSoundEvents.SPARK.get(), SoundSource.BLOCKS, 1F, 1F);

            Direction dir = RBMKLeverBlockEntity.this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            Direction rot = dir.getClockWise();

            for(int i = 0; i < 2; i++) {

                double reach = (i == 0 ? 0.375D : 0.625D);
                double x = pos.getX() + 0.5D + dir.getStepX() * 0.4D - rot.getStepX() * (this.index - 0.5D) * reach;
                double y = pos.getY() + 0.4375D - 0.03125D;
                double z = pos.getZ() + 0.5D + dir.getStepZ() * 0.4D - rot.getStepZ() * (this.index - 0.5D) * reach;

                CompoundTag data = new CompoundTag();
                data.putString("type", "tau");
                data.putByte("count", (byte) 5);
                data.putBoolean("small", true);

                PacketDistributor.sendToPlayersNear(serverLevel, null, x, y, z, 25, new AuxParticle(data, x, y, z));
            }
        }

        /**
         * Der Hebel des Servers bewegt sich sprunghaft, weil er nur alle paar Ticks ankommt.
         * Hier laeuft der angezeigte Hebel dem gemeldeten Stand in drei Schritten hinterher.
         */
        public void updateClient() {

            this.prevFlipProgress = this.flipProgress;

            if(this.turnProgress > 0) {
                this.flipProgress = this.flipProgress + ((this.flipSync - this.flipProgress) / this.turnProgress);
                this.turnProgress--;
            } else {
                this.flipProgress = this.flipSync;
            }
        }

        public boolean canSend(String command) {
            return this.rtty != null && !this.rtty.isEmpty() && command != null && !command.isEmpty();
        }

        public void serialize(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(this.active);
            buf.writeBoolean(this.polling);
            buf.writeFloat(this.flipProgress);
            buf.writeUtf(this.label);
            buf.writeUtf(this.rtty);
            buf.writeUtf(this.commandOn);
            buf.writeUtf(this.commandOff);
        }

        public void deserialize(RegistryFriendlyByteBuf buf) {
            this.active = buf.readBoolean();
            this.polling = buf.readBoolean();
            this.flipSync = buf.readFloat();
            this.label = buf.readUtf();
            this.rtty = buf.readUtf();
            this.commandOn = buf.readUtf();
            this.commandOff = buf.readUtf();
            this.turnProgress = 3;
        }

        public void load(CompoundTag tag, int index) {
            this.active = tag.getBoolean("active" + index);
            this.polling = tag.getBoolean("polling" + index);
            this.isTurningOn = tag.getBoolean("isTurningOn" + index);
            this.flipProgress = tag.getFloat("flipProgress" + index);
            this.label = tag.getString("label" + index);
            this.rtty = tag.getString("rtty" + index);
            this.commandOn = tag.getString("commandOn" + index);
            this.commandOff = tag.getString("commandOff" + index);
        }

        public void save(CompoundTag tag, int index) {
            tag.putBoolean("active" + index, this.active);
            tag.putBoolean("polling" + index, this.polling);
            tag.putBoolean("isTurningOn" + index, this.isTurningOn);
            tag.putFloat("flipProgress" + index, this.flipProgress);
            tag.putString("label" + index, this.label);
            tag.putString("rtty" + index, this.rtty);
            tag.putString("commandOn" + index, this.commandOn);
            tag.putString("commandOff" + index, this.commandOff);
        }
    }
}
