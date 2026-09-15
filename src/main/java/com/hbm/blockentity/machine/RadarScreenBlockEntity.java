package com.hbm.blockentity.machine;

import api.hbm.entity.RadarEntry;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.blockentity.TickingBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineRadarScreen.
 *
 * Der Schirm haelt nichts fest und rechnet nichts aus -- er zeigt nur, was ihm ein Radar
 * zuschickt. Ein Radar, in dessen neuntem Fach ein auf ihn gemerkter Verbinder liegt, fuellt ihn
 * jeden Durchgang neu.
 *
 * ER LEERT SICH JEDEN TICK SELBST, und das ist Absicht des Originals: schickt das Radar nichts
 * mehr -- abgebaut, stromlos, Verbinder entfernt --, steht der Schirm sofort leer da statt eine
 * alte Lage weiterzuzeigen. Deshalb ist "verbunden" auch kein gespeicherter Zustand, sondern
 * eine Aussage ueber genau diesen Tick.
 */
public class RadarScreenBlockEntity extends TickingBaseBlockEntity {

    public List<RadarEntry> entries = new ArrayList<>();
    public int refX;
    public int refY;
    public int refZ;
    public int range;
    public boolean linked;

    private AABB renderBox;

    public RadarScreenBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.RADAR_SCREEN.get(), pos, state);
    }

    @Override
    public void updateEntity() {

        if(this.level == null || this.level.isClientSide) return;

        this.networkPackNT(100);

        this.entries.clear();
        this.linked = false;
    }

    @Override
    public void serialize(RegistryFriendlyByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.linked);
        buf.writeInt(this.refX);
        buf.writeInt(this.refY);
        buf.writeInt(this.refZ);
        buf.writeInt(this.range);
        buf.writeInt(this.entries.size());
        for(RadarEntry entry : this.entries) entry.encode(buf);
    }

    @Override
    public void deserialize(RegistryFriendlyByteBuf buf) {
        super.deserialize(buf);
        this.linked = buf.readBoolean();
        this.refX = buf.readInt();
        this.refY = buf.readInt();
        this.refZ = buf.readInt();
        this.range = buf.readInt();

        int count = buf.readInt();
        this.entries.clear();
        for(int i = 0; i < count; i++) {
            RadarEntry entry = new RadarEntry();
            entry.decode(buf);
            this.entries.add(entry);
        }
    }

    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.worldPosition;
            this.renderBox = new AABB(p.getX() - 1, p.getY(), p.getZ() - 1, p.getX() + 2, p.getY() + 2, p.getZ() + 2);
        }
        return this.renderBox;
    }
}
