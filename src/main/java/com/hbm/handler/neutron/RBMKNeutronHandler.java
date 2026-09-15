package com.hbm.handler.neutron;

import com.hbm.blockentity.machine.rbmk.IRBMKFluxReceiver;
import com.hbm.blockentity.machine.rbmk.RBMKBaseBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKControlBlockEntity;
import com.hbm.blockentity.machine.rbmk.RBMKDials;
import com.hbm.blockentity.machine.rbmk.RBMKRodBlockEntity;
import com.hbm.blocks.machine.rbmk.RBMKBaseBlock;
import com.hbm.handler.neutron.NeutronNodeWorld.StreamWorld;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.util.Compat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Neutronenlogik des RBMK: Knoten, Stroeme und ihre Wechselwirkung mit den Saeulen. */
public class RBMKNeutronHandler {

    /* Werden einmal pro Welt und Tick vom NeutronHandler gesetzt, damit nicht jeder Strom die Dials neu liest. */
    static double moderatorEfficiency;
    static double reflectorEfficiency;
    static double absorberEfficiency;
    static int columnHeight;
    static int fluxRange;

    public enum RBMKType {
        ROD,
        MODERATOR,
        CONTROL_ROD,
        REFLECTOR,
        ABSORBER,
        OUTGASSER,
        /** Alles, was den Fluss nicht veraendert -- dafuer lohnt die Rechnung nicht. */
        OTHER
    }

    private static BlockEntity blockPosToTE(Level level, BlockPos pos) {
        return Compat.getBlockEntityStandard(level, pos);
    }

    public static RBMKNeutronNode makeNode(StreamWorld streamWorld, RBMKBaseBlockEntity tile) {
        RBMKNeutronNode node = (RBMKNeutronNode) streamWorld.getNode(tile.getBlockPos());
        return node != null ? node : new RBMKNeutronNode(tile, tile.getRBMKType(), tile.hasLid());
    }

    public static class RBMKNeutronNode extends NeutronNode {

        public RBMKNeutronNode(RBMKBaseBlockEntity tile, RBMKType type, boolean hasLid) {
            super(tile, NeutronStream.NeutronType.RBMK);
            this.data.put("hasLid", hasLid);
            this.data.put("type", type);
        }

        public void addLid() {
            this.data.put("hasLid", true);
        }

        public void removeLid() {
            this.data.put("hasLid", false);
        }

        /** Laeuft die Scheibe um den Knoten ab, in der ReaSim-Brennstaebe wirken. Liefert null fuer Positionen ausserhalb des Kreises. */
        public Iterator<BlockPos> getReaSimNodes() {

            BlockPos origin = this.pos;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            return new Iterator<>() {

                private int x = -fluxRange;
                private int z = -fluxRange;

                @Override
                public boolean hasNext() {
                    return (fluxRange + x) * (fluxRange * 2 + 1) + z + fluxRange + 1 < (fluxRange * 2 + 1) * (fluxRange * 2 + 1);
                }

                @Override
                public BlockPos next() {
                    boolean inRange = x * x + z * z <= fluxRange * fluxRange;
                    int cx = x;
                    int cz = z;

                    z++;
                    if(z > fluxRange) {
                        z = -fluxRange;
                        x++;
                    }

                    return inRange ? cursor.set(origin.getX() + cx, origin.getY(), origin.getZ() + cz) : null;
                }
            };
        }

        /** Entscheidet, welche Knoten aus dem Cache fliegen koennen. */
        public List<BlockPos> checkNode(StreamWorld streamWorld) {

            List<BlockPos> list = new ArrayList<>();

            RBMKNeutronStream[] streams = new RBMKNeutronStream[RBMKRodBlockEntity.FLUX_DIRS.length];

            for(int i = 0; i < RBMKRodBlockEntity.FLUX_DIRS.length; i++) {
                Direction dir = RBMKRodBlockEntity.FLUX_DIRS[i];
                streams[i] = new RBMKNeutronStream(this, new Vec3(dir.getStepX(), 0, dir.getStepZ()));
            }

            // Brennstabsaeule ohne Stab oder ohne Fluss: alles auf ihrem Weg darf raus.
            if(tile instanceof RBMKRodBlockEntity rod) {
                if(!rod.hasRod || rod.lastFluxQuantity == 0) {

                    for(RBMKNeutronStream stream : streams) {
                        for(NeutronNode node : stream.getNodes(streamWorld, false)) {
                            if(node != null) list.add(node.pos);
                        }
                    }

                    return list;
                }
            }

            // Steht in Reichweite ueberhaupt ein aktiver Brennstab? Wenn nicht, darf dieser Knoten raus.
            {
                Iterator<BlockPos> reaSimNodes = getReaSimNodes();
                boolean hasRod = false;

                while(reaSimNodes.hasNext()) {

                    BlockPos nodePos = reaSimNodes.next();
                    if(nodePos == null) continue;

                    NeutronNode node = streamWorld.getNode(nodePos);

                    if(node != null && node.tile instanceof RBMKRodBlockEntity rod && rod.hasRod && rod.lastFluxQuantity > 0) {
                        hasRod = true;
                        break;
                    }
                }

                if(!hasRod) {
                    list.add(this.pos);
                    return list;
                }
            }

            // Liegt entlang der vier Himmelsrichtungen ein Brennstab, bleibt der Knoten erhalten.
            for(RBMKNeutronStream stream : streams) {
                for(NeutronNode node : stream.getNodes(streamWorld, false)) {
                    if(node != null && node.tile instanceof RBMKRodBlockEntity) return list;
                }
            }

            list.add(this.pos);
            return list;
        }
    }

    public static class RBMKNeutronStream extends NeutronStream {

        public RBMKNeutronStream(NeutronNode origin, Vec3 vector) {
            super(origin, vector);
        }

        public RBMKNeutronStream(NeutronNode origin, Vec3 vector, double flux, double ratio) {
            super(origin, vector, flux, ratio, NeutronType.RBMK);
        }

        /** Die Knoten entlang des Stroms, den Ursprung ausgenommen. Nutzt den Cache. */
        public NeutronNode[] getNodes(StreamWorld streamWorld, boolean addNode) {

            NeutronNode[] positions = new NeutronNode[fluxRange];

            BlockPos origin = this.origin.pos;
            Level level = this.origin.tile.getLevel();
            if(level == null) return positions;

            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            for(int i = 1; i <= fluxRange; i++) {
                int x = (int) Math.floor(0.5 + vector.x * i);
                int z = (int) Math.floor(0.5 + vector.z * i);

                cursor.set(origin.getX() + x, origin.getY(), origin.getZ() + z);

                NeutronNode node = streamWorld.getNode(cursor);

                if(node instanceof RBMKNeutronNode) {
                    positions[i - 1] = node;
                } else if(this.origin.tile.getBlockState().getBlock() instanceof RBMKBaseBlock) {
                    BlockEntity te = blockPosToTE(level, cursor);
                    if(te instanceof RBMKBaseBlockEntity rbmkBase) {
                        node = makeNode(streamWorld, rbmkBase);
                        positions[i - 1] = node;
                        if(addNode) streamWorld.addNode(node);
                    }
                }
            }

            return positions;
        }

        @Override
        public void runStreamInteraction(Level level, StreamWorld streamWorld) {

            if(fluxQuantity == 0D) return;

            BlockPos pos = this.origin.pos;

            RBMKBaseBlockEntity originTE;

            NeutronNode node = streamWorld.getNode(pos);
            if(node != null) {
                originTE = (RBMKBaseBlockEntity) node.tile;
            } else {
                BlockEntity tile = blockPosToTE(level, pos);
                if(!(tile instanceof RBMKBaseBlockEntity base)) return; // steht nicht mehr da
                originTE = base;
                streamWorld.addNode(new RBMKNeutronNode(originTE, originTE.getRBMKType(), originTE.hasLid()));
            }

            int moderatedCount = 0;

            Iterator<BlockPos> iterator = getBlocks(fluxRange);

            while(iterator.hasNext()) {

                BlockPos targetPos = iterator.next();

                if(fluxQuantity == 0D) return; // alles aufgebraucht

                NeutronNode targetNode = streamWorld.getNode(targetPos);

                if(targetNode == null) {
                    BlockEntity te = blockPosToTE(level, targetPos);

                    if(te instanceof RBMKBaseBlockEntity base) {
                        targetNode = makeNode(streamWorld, base);
                        streamWorld.addNode(targetNode);
                    } else {
                        int hits = getHits(targetPos);
                        if(hits == columnHeight) return;            // vollstaendig abgeschirmt
                        if(hits > 0) {                               // teilweise abgeschirmt
                            irradiateFromFlux(targetPos, hits);
                            fluxQuantity *= 1 - ((double) hits / columnHeight);
                            continue;
                        }
                        irradiateFromFlux(targetPos, 0);
                        continue;
                    }
                }

                RBMKType type = (RBMKType) targetNode.data.get("type");

                if(type == RBMKType.OTHER || type == null) continue;

                RBMKBaseBlockEntity nodeTE = (RBMKBaseBlockEntity) targetNode.tile;

                if(!(Boolean) targetNode.data.get("hasLid")) {
                    ChunkRadiationManager.proxy.incrementRad(level, targetPos.immutable(), (float) (this.fluxQuantity * 0.05F));
                }

                if(type == RBMKType.MODERATOR || nodeTE.isModerated()) {
                    moderatedCount++;
                    moderateStream();
                }

                if(type == RBMKType.ROD) {

                    RBMKRodBlockEntity rod = (RBMKRodBlockEntity) nodeTE;

                    if(rod.hasRod) {
                        rod.receiveFlux(this);
                        return;
                    }

                /* OUTGASSER: die Bestrahlungssaeule kommt in einer spaeteren Runde. */

                } else if(type == RBMKType.CONTROL_ROD) {

                    RBMKControlBlockEntity rod = (RBMKControlBlockEntity) nodeTE;

                    if(rod.rodLevel > 0.0D) {
                        this.fluxQuantity *= rod.getMult();
                        continue;
                    }
                    return;

                } else if(type == RBMKType.REFLECTOR) {

                    if(originTE.isModerated()) moderatedCount++;

                    if(this.fluxRatio > 0 && moderatedCount > 0) {
                        for(int i = 0; i < moderatedCount; i++) moderateStream();
                    }

                    if(reflectorEfficiency != 1.0D) {
                        this.fluxQuantity *= reflectorEfficiency;
                        continue;
                    }

                    if(originTE instanceof IRBMKFluxReceiver receiver) receiver.receiveFlux(this);
                    return;

                } else if(type == RBMKType.ABSORBER) {

                    nodeTE.heat += RBMKDials.getAbsorberHeatConversion(level) * this.fluxQuantity;

                    if(absorberEfficiency == 1) return;

                    this.fluxQuantity *= absorberEfficiency;
                }
            }

            NeutronNode[] nodes = getNodes(streamWorld, true);
            NeutronNode lastNode = nodes[nodes.length - 1];

            if(lastNode == null) {
                // Es gab keinen letzten Knoten, der Strom wurde also nie aufgefangen. Wo genau er
                // versickert, laesst sich nicht sinnvoll sagen -- also am Ursprung bestrahlen.
                irradiateFromFlux(pos.offset((int) this.vector.x, 0, (int) this.vector.z));
                return;
            }

            RBMKType lastNodeType = (RBMKType) lastNode.data.get("type");

            if(lastNodeType == RBMKType.CONTROL_ROD) {

                RBMKControlBlockEntity rod = (RBMKControlBlockEntity) lastNode.tile;

                if(rod.getMult() > 0.0D) {
                    this.fluxQuantity *= rod.getMult();
                    BlockPos posAfter = lastNode.pos.offset((int) this.vector.x, 0, (int) this.vector.z);

                    // Prueft, ob hinter dem Steuerstab noch eine Saeule oder nur ein Block steht.
                    if(NeutronNodeWorld.getNode(level, pos) == null) {
                        BlockEntity te = blockPosToTE(level, posAfter);
                        if(te instanceof RBMKBaseBlockEntity base) {
                            streamWorld.addNode(makeNode(streamWorld, base));
                        } else {
                            irradiateFromFlux(posAfter);
                        }
                    }
                }
            }
        }

        /** Zaehlt, wie viele Bloecke der Saeulenhoehe an dieser Stelle undurchsichtig sind. */
        public int getHits(BlockPos pos) {

            Level level = origin.tile.getLevel();
            if(level == null) return 0;

            int hits = 0;

            for(int h = 0; h < columnHeight; h++) {
                BlockPos check = new BlockPos(pos.getX(), pos.getY() + h, pos.getZ());
                if(level.getBlockState(check).isSolidRender(level, check)) hits++;
            }

            return hits;
        }

        public void irradiateFromFlux(BlockPos pos) {
            irradiateFromFlux(pos, getHits(pos));
        }

        public void irradiateFromFlux(BlockPos pos, int hits) {
            Level level = origin.tile.getLevel();
            if(level == null) return;
            ChunkRadiationManager.proxy.incrementRad(level, pos.immutable(), (float) (fluxQuantity * 0.05F * (1 - (double) hits / columnHeight)));
        }

        public void moderateStream() {
            fluxRatio *= (1 - moderatorEfficiency);
        }
    }
}
