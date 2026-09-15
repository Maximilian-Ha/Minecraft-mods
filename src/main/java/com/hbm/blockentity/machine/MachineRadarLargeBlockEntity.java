package com.hbm.blockentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blockentity.NtmBlockEntityTypes;
import com.hbm.config.IConfigurableMachine;
import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.io.IOException;

/**
 * Portiert aus 1.7.10: com.hbm.tileentity.machine.TileEntityMachineRadarLarge.
 *
 * Das grosse Radar ist das gewoehnliche mit dreifacher Reichweite -- dreitausend Bloecke statt
 * tausend. Es teilt sich alles andere mit ihm: dieselbe Suche, dieselbe Karte, dieselben
 * Schalter, derselbe Stromverbrauch.
 *
 * ES HAT SEINEN EIGENEN EINTRAG IN DER MASCHINENKONFIGURATION, denn die Reichweite ist das
 * Einzige, worin es sich unterscheidet -- und wer sie aendern will, soll nicht zugleich die des
 * kleinen aendern muessen.
 *
 * SEINE ANSCHLUESSE LIEGEN WEITER AUSSEN: zwei Bloecke statt einem. Das Bauwerk ist groesser,
 * und ein Rohr direkt am Kern kaeme an seinen Fuss nicht heran.
 */
public class MachineRadarLargeBlockEntity extends MachineRadarBlockEntity {

    public static int radarLargeRange = 3_000;

    public MachineRadarLargeBlockEntity(BlockPos pos, BlockState state) {
        super(NtmBlockEntityTypes.MACHINE_RADAR_LARGE.get(), pos, state);
    }

    @Override public Component getDefaultName() { return Component.translatable("container.radarLarge"); }

    @Override public int getRange() { return radarLargeRange; }

    @Override
    public DirPos[] getConPos() {
        BlockPos pos = this.getBlockPos();
        return new DirPos[] {
                new DirPos(pos.offset(+2, 0, 0), Library.POS_X),
                new DirPos(pos.offset(-2, 0, 0), Library.NEG_X),
                new DirPos(pos.offset(0, 0, +2), Library.POS_Z),
                new DirPos(pos.offset(0, 0, -2), Library.NEG_Z),
        };
    }

    private AABB renderBox;

    /* OHNE @Override: getRenderBoundingBox() kommt aus der NeoForge-Erweiterung der
     * Block-Entitaet, nicht aus der Oberklasse -- der Torwaechter aus Runde 27 sagt es. */
    public AABB getRenderBoundingBox() {
        if(this.renderBox == null) {
            BlockPos p = this.getBlockPos();
            this.renderBox = new AABB(p.getX() - 5, p.getY(), p.getZ() - 5, p.getX() + 6, p.getY() + 10, p.getZ() + 6);
        }
        return this.renderBox;
    }

    /** Konfiguration, siehe {@link com.hbm.config.MachineDynConfig}. */
    public static void readConfig(JsonObject obj) {
        radarLargeRange = IConfigurableMachine.grab(obj, "I:radarLargeRange", radarLargeRange);
    }

    public static void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:radarLargeRange").value(radarLargeRange);
    }
}
