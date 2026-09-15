package com.hbm.blockentity.machine;

import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.TickingBaseBlockEntity;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellite.SatelliteBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineSatLink.
 *
 * Die Satellitenverbindung ist die Bodenstation: eine Schuessel, die sich auf einen Satelliten
 * ausrichtet und meldet, was er gerade tut.
 *
 * SIE HAT KEINE OBERFLAECHE, und das ist Absicht des Originals. Eingestellt wird sie, indem man
 * mit einem Satellitenchip in der Hand daraufklickt -- die Frequenz des Chips wird ihre. Gelesen
 * wird sie, indem man sie ansieht: Frequenz, Verbindung, und was der Satellit zu sagen hat.
 *
 * SIE BRAUCHT FREIEN HIMMEL. Steht irgendetwas ueber ihr, kommt keine Verbindung zustande --
 * die Schuessel prueft dafuer die Hoehenkarte an ihrer Stelle. Das ist die einzige Bedingung;
 * Strom braucht sie keinen.
 *
 * DIE SCHUESSEL RICHTET SICH SICHTBAR AUS. Besteht die Verbindung, faehrt sie in Vierteilgraden
 * hoch und dreht sich; bricht sie ab, faellt sie zurueck. Die beiden Winkel werden mituebertragen,
 * damit ein spaeterer Renderer sie vorfindet -- ein Modell hat der Port noch nicht.
 *
 * NICHT UEBERNOMMEN: die Anbindung an Redstone-ueber-Funk und an OpenComputers, ueber die das
 * Original der Bodenstation Befehle geben und Werte abfragen laesst. Beides gibt es im Port
 * nicht; die Satelliten nehmen ihre Befehle dort bis auf Weiteres ueber das Handgeraet entgegen.
 */
public class MachineSatLinkBlockEntity extends TickingBaseBlockEntity {

    public static final float SPEED = 0.25F;
    public static final float ACTIVE_ROT = -15F;
    public static final float ACTIVE_LIFT = -45F;
    public static final float INACTIVE_ROT = 0F;
    public static final float INACTIVE_LIFT = -85F;

    public int freq;
    public boolean connected;

    public float rot = INACTIVE_ROT;
    public float prevRot = INACTIVE_ROT;
    public float lift = INACTIVE_LIFT;
    public float prevLift = INACTIVE_LIFT;

    /** Was der Satellit meldet; nur zur Anzeige, der Server fuellt es. */
    public List<Component> info = new ArrayList<>();

    private AABB renderBox;

    public MachineSatLinkBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_SAT_LINK.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null) return;

        if(!this.level.isClientSide) {

            this.connected = false;

            /* Freier Himmel: die Hoehenkarte muss an dieser Stelle bei uns oder tiefer enden. */
            if(this.level instanceof ServerLevel serverLevel
                    && serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, this.worldPosition.getX(), this.worldPosition.getZ()) <= this.worldPosition.getY()) {

                this.connected = SatelliteSavedData.getData(serverLevel).isFreqTaken(this.freq);
            }

            this.updateInfo();
            this.networkPackNT(150);

        } else {

            this.prevRot = this.rot;
            this.prevLift = this.lift;

            this.rot = approach(this.rot, this.connected ? ACTIVE_ROT : INACTIVE_ROT);
            this.lift = approach(this.lift, this.connected ? ACTIVE_LIFT : INACTIVE_LIFT);
        }
    }

    private static float approach(float current, float target) {
        if(Math.abs(current - target) <= SPEED) return target;
        return current < target ? current + SPEED : current - SPEED;
    }

    private void updateInfo() {

        if(!this.connected) {
            if(!this.info.isEmpty()) this.info = new ArrayList<>();
            return;
        }

        if(!(this.level instanceof ServerLevel serverLevel)) return;

        SatelliteBase sat = SatelliteSavedData.getData(serverLevel).getSatFromFreq(this.freq);
        if(sat != null) this.info = sat.getInfo(serverLevel);
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.connected);
        buf.writeInt(this.freq);
        buf.writeInt(this.info.size());
        for(Component line : this.info) ComponentSerialization.STREAM_CODEC.encode(buf, line);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.connected = buf.readBoolean();
        this.freq = buf.readInt();

        int count = buf.readInt();
        List<Component> read = new ArrayList<>(count);
        for(int i = 0; i < count; i++) read.add(ComponentSerialization.STREAM_CODEC.decode(buf));
        this.info = read;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.freq = tag.getInt("freq");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("freq", this.freq);
    }

    /** Die Schuessel steht auf einem sechs Bloecke hohen Mast. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 2, p.getY(), p.getZ() - 2, p.getX() + 3, p.getY() + 10, p.getZ() + 3);
        }
        return this.renderBox;
    }
}
