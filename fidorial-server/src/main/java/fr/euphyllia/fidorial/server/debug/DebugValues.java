package fr.euphyllia.fidorial.server.debug;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkCodec;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkRecordCodec;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.PositionData;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;

import java.util.List;

import static fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs.BLOCK_POS;
import static fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs.BOOL;
import static fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs.FLOAT;
import static fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs.INT;
import static fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs.VAR_INT;
import static fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs.VEC3D;
import static fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs.prefixedList;
import static fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs.STRING;
import static fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs.string;

public final class DebugValues {

    private static final int MAX_LIST_SIZE = 65_536;

    private DebugValues() {
        throw new UnsupportedOperationException("DebugValues cannot be instantiated.");
    }

    /**
     * {@code minecraft:goal_selectors}.
     */
    public record GoalSelectorInfo(List<Entry> goals) {

        public static final NetworkCodec<PacketBuffer, GoalSelectorInfo> CODEC =
                NetworkRecordCodec.builder(GoalSelectorInfo.class, PacketBuffer.class)
                        .required("goals", GoalSelectorInfo::goals, prefixedList(Entry.CODEC, MAX_LIST_SIZE))
                        .build();

        public GoalSelectorInfo {
            goals = List.copyOf(goals);
        }

        public record Entry(int priority, boolean running, String name) {

            private static final int MAX_NAME_LENGTH = 255;

            public static final NetworkCodec<PacketBuffer, Entry> CODEC = NetworkRecordCodec.builder(Entry.class, PacketBuffer.class)
                    .required("priority", Entry::priority, VAR_INT)
                    .required("running", Entry::running, BOOL)
                    .required("name", Entry::name, string(MAX_NAME_LENGTH))
                    .build();

            public Entry {
                name = name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
            }
        }
    }

    /**
     * {@code minecraft:entity_paths}
     */
    public record PathInfo(
            boolean reached,
            int nextNodeIndex,
            Position target,
            List<Node> nodes,
            List<Node> targetNodes,
            List<Node> openSet,
            List<Node> closedSet,
            float maxNodeDistance
    ) {

        public static final NetworkCodec<PacketBuffer, PathInfo> CODEC = NetworkRecordCodec.builder(PathInfo.class, PacketBuffer.class)
                .required("reached", PathInfo::reached, BOOL)
                .required("next_node_index", PathInfo::nextNodeIndex, INT)
                .required("target", PathInfo::target, BLOCK_POS)
                .required("nodes", PathInfo::nodes, prefixedList(Node.CODEC, MAX_LIST_SIZE))
                .required("target_nodes", PathInfo::targetNodes, prefixedList(Node.CODEC, MAX_LIST_SIZE))
                .required("open_set", PathInfo::openSet, prefixedList(Node.CODEC, MAX_LIST_SIZE))
                .required("closed_set", PathInfo::closedSet, prefixedList(Node.CODEC, MAX_LIST_SIZE))
                .required("max_node_distance", PathInfo::maxNodeDistance, FLOAT)
                .build();

        public PathInfo {
            nodes = List.copyOf(nodes);
            targetNodes = List.copyOf(targetNodes);
            openSet = List.copyOf(openSet);
            closedSet = List.copyOf(closedSet);
        }

        public record Node(int x, int y, int z, float walkedDistance, float costMalus, boolean closed, int pathType, float f) {

            public static final int WALKABLE = 2;

            public static final NetworkCodec<PacketBuffer, Node> CODEC = NetworkRecordCodec.builder(Node.class, PacketBuffer.class)
                    .required("x", Node::x, INT)
                    .required("y", Node::y, INT)
                    .required("z", Node::z, INT)
                    .required("walked_distance", Node::walkedDistance, FLOAT)
                    .required("cost_malus", Node::costMalus, FLOAT)
                    .required("closed", Node::closed, BOOL)
                    .required("path_type", Node::pathType, VAR_INT)
                    .required("f", Node::f, FLOAT)
                    .build();

            public static Node walkable(final Position pos) {
                return new Node(pos.blockX(), pos.blockY(), pos.blockZ(), 0f, 0f, false, WALKABLE, 0f);
            }
        }
    }

    /**
     * {@code minecraft:structures}
     */
    public record StructuresInfo(List<Structure> structures) {

        public static final NetworkCodec<PacketBuffer, StructuresInfo> CODEC =
                NetworkRecordCodec.builder(StructuresInfo.class, PacketBuffer.class)
                        .required("structures", StructuresInfo::structures, prefixedList(Structure.CODEC, MAX_LIST_SIZE))
                        .build();

        public StructuresInfo {
            structures = List.copyOf(structures);
        }

        public record Bounds(BlockPosition min, BlockPosition max) {

            public static final NetworkCodec<PacketBuffer, Bounds> CODEC = NetworkRecordCodec.builder(Bounds.class, PacketBuffer.class)
                    .required("min", Bounds::min, BLOCK_POS)
                    .required("max", Bounds::max, BLOCK_POS)
                    .build();
        }

        public record Piece(Bounds bounds, boolean start) {

            public static final NetworkCodec<PacketBuffer, Piece> CODEC = NetworkRecordCodec.builder(Piece.class, PacketBuffer.class)
                    .required("bounds", Piece::bounds, Bounds.CODEC)
                    .required("start", Piece::start, BOOL)
                    .build();
        }

        public record Structure(Bounds bounds, List<Piece> pieces) {

            public static final NetworkCodec<PacketBuffer, Structure> CODEC = NetworkRecordCodec.builder(Structure.class, PacketBuffer.class)
                    .required("bounds", Structure::bounds, Bounds.CODEC)
                    .required("pieces", Structure::pieces, prefixedList(Piece.CODEC, MAX_LIST_SIZE))
                    .build();

            public Structure {
                pieces = List.copyOf(pieces);
            }
        }
    }

    /**
     * {@code minecraft:brains}
     */
    public record BrainInfo(
            String name,
            String profession,
            int xp,
            float health,
            float maxHealth,
            String inventory,
            boolean wantsGolem,
            int angerLevel,
            List<String> activities,
            List<String> behaviors,
            List<String> memories,
            List<String> gossips,
            List<Position> pois,
            List<Position> potentialPois
    ) {

        public static final int NO_ANGER_LEVEL = -1; // sent for everything except warden

        public static final NetworkCodec<PacketBuffer, BrainInfo> CODEC = NetworkRecordCodec.builder(BrainInfo.class, PacketBuffer.class)
                .required("name", BrainInfo::name, STRING)
                .required("profession", BrainInfo::profession, STRING)
                .required("xp", BrainInfo::xp, INT)
                .required("health", BrainInfo::health, FLOAT)
                .required("max_health", BrainInfo::maxHealth, FLOAT)
                .required("inventory", BrainInfo::inventory, STRING)
                .required("wants_golem", BrainInfo::wantsGolem, BOOL)
                .required("anger_level", BrainInfo::angerLevel, INT)
                .required("activities", BrainInfo::activities, prefixedList(STRING, MAX_LIST_SIZE))
                .required("behaviors", BrainInfo::behaviors, prefixedList(STRING, MAX_LIST_SIZE))
                .required("memories", BrainInfo::memories, prefixedList(STRING, MAX_LIST_SIZE))
                .required("gossips", BrainInfo::gossips, prefixedList(STRING, MAX_LIST_SIZE))
                .required("pois", BrainInfo::pois, prefixedList(BLOCK_POS, MAX_LIST_SIZE))
                .required("potential_pois", BrainInfo::potentialPois, prefixedList(BLOCK_POS, MAX_LIST_SIZE))
                .build();

        public BrainInfo {
            activities = List.copyOf(activities);
            behaviors = List.copyOf(behaviors);
            memories = List.copyOf(memories);
            gossips = List.copyOf(gossips);
            pois = List.copyOf(pois);
            potentialPois = List.copyOf(potentialPois);
        }

        public static BrainInfo forMob(final String name, final float health, final float maxHealth, final List<String> behaviors, final List<String> memories) {
            return new BrainInfo(name, "", 0, health, maxHealth, "", false, NO_ANGER_LEVEL,
                    List.of(), behaviors, memories, List.of(), List.of(), List.of());
        }
    }

    /**
     * {@code minecraft:entity_block_intersections}
     */
    public enum BlockIntersection {
        IN_BLOCK,
        IN_FLUID,
        IN_AIR;

        private static final BlockIntersection[] VALUES = values();

        public static final NetworkCodec<PacketBuffer, BlockIntersection> CODEC =
                VAR_INT.xmap(BlockIntersection::byId, BlockIntersection::ordinal);

        private static BlockIntersection byId(final int id) {
            if (id < 0 || id >= VALUES.length) {
                throw new IllegalArgumentException("Unknown block intersection " + id);
            }
            return VALUES[id];
        }
    }

    /**
     * {@code minecraft:game_events}
     */
    public record GameEventInfo(int event, PositionData.Vec3D position) {

        public static final NetworkCodec<PacketBuffer, GameEventInfo> CODEC =
                NetworkRecordCodec.builder(GameEventInfo.class, PacketBuffer.class)
                        .required("event", GameEventInfo::event, VAR_INT)
                        .required("position", GameEventInfo::position, VEC3D)
                        .build();
    }
}
