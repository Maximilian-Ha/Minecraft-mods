package com.hbm.handler.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Verwaltung aller Neutronen-Knoten und -Stroeme, aufgeteilt nach Welt. */
public class NeutronNodeWorld {

    public static HashMap<Level, StreamWorld> streamWorlds = new HashMap<>();

    public static NeutronNode getNode(Level level, BlockPos pos) {
        StreamWorld streamWorld = streamWorlds.get(level);
        return streamWorld != null ? streamWorld.getNode(pos) : null;
    }

    public static void removeNode(Level level, BlockPos pos) {
        StreamWorld streamWorld = streamWorlds.get(level);
        if(streamWorld == null) return;
        streamWorld.removeNode(pos);
    }

    public static StreamWorld getOrAddWorld(Level level) {
        return streamWorlds.computeIfAbsent(level, l -> new StreamWorld());
    }

    public static void removeAllWorlds() {
        streamWorlds.clear();
    }

    /** Wirft Welten weg, in denen gerade nichts passiert. */
    public static void removeEmptyWorlds() {
        streamWorlds.values().removeIf(streamWorld -> streamWorld.streams.isEmpty());
    }

    public static class StreamWorld {

        private final List<NeutronStream> streams = new ArrayList<>();
        private final HashMap<BlockPos, NeutronNode> nodeCache = new HashMap<>();

        public void runStreamInteractions(Level level) {
            // Ueber einen Index laufen: waehrend der Abarbeitung koennen neue Stroeme entstehen,
            // ein Iterator wuerde dabei mit einer ConcurrentModificationException aussteigen.
            for(int i = 0; i < streams.size(); i++) {
                streams.get(i).runStreamInteraction(level, this);
            }
        }

        public void addStream(NeutronStream stream) {
            streams.add(stream);
        }

        public void removeAllStreams() {
            streams.clear();
        }

        /** Raeumt Knoten weg, durch die laengere Zeit kein Fluss mehr geht. */
        public void cleanNodes() {

            List<BlockPos> toRemove = new ArrayList<>();

            for(NeutronNode cachedNode : nodeCache.values()) {
                if(cachedNode.type == NeutronStream.NeutronType.RBMK) {
                    RBMKNeutronHandler.RBMKNeutronNode node = (RBMKNeutronHandler.RBMKNeutronNode) cachedNode;
                    toRemove.addAll(node.checkNode(this));
                }
                /* Wie im Original: Pile-Knoten werden nicht automatisch entfernt, das erledigt castRay() selbst. */
            }

            for(BlockPos pos : toRemove) {
                nodeCache.remove(pos);
            }
        }

        public NeutronNode getNode(BlockPos pos) {
            return nodeCache.get(pos);
        }

        public void addNode(NeutronNode node) {
            nodeCache.put(node.pos, node);
        }

        public void removeNode(BlockPos pos) {
            nodeCache.remove(pos);
        }

        public void removeAllStreamsOfType(NeutronStream.NeutronType type) {
            streams.removeIf(stream -> stream.type == type);
        }
    }
}
