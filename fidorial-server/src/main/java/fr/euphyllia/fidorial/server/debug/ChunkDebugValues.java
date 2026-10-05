package fr.euphyllia.fidorial.server.debug;

import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundDebugValuePacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.structure.gen.StructureChunkGenerator;
import fr.euphyllia.fidorial.server.world.structure.jigsaw.PlacedPiece;
import fr.euphyllia.fidorial.server.world.structure.jigsaw.StructureStart;
import fr.euphyllia.fidorial.server.world.structure.math.Box;
import fr.fidorial.math.Position;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ChunkDebugValues {

    private ChunkDebugValues() {
        throw new UnsupportedOperationException("ChunkDebugValues cannot be instantiated.");
    }

    public static void sendStructures(final ClientConnection connection, final ServerWorld world, final int chunkX, final int chunkZ) {
        final DebugChannel<DebugValues.StructuresInfo> channel = DebugChannels.STRUCTURES;
        if (!DebugSubscribers.wants(connection, channel)) {
            return;
        }
        final DebugValues.StructuresInfo value = structures(connection, world, chunkX, chunkZ);
        if (value != null) {
            connection.send(ClientboundDebugValuePacket.chunk(chunkX, chunkZ, channel, value));
        }
    }

    private static DebugValues.@Nullable StructuresInfo structures(final ClientConnection connection, final ServerWorld world,
                                                                   final int chunkX, final int chunkZ) {
        final StructureChunkGenerator generator = connection.server().structures().generator(world);
        if (generator == null) {
            return null;
        }
        final List<StructureStart> starts = generator.structures().startsOriginatingIn(chunkX, chunkZ);
        if (starts.isEmpty()) {
            return null;
        }
        final List<DebugValues.StructuresInfo.Structure> structures = new ArrayList<>(starts.size());
        for (final StructureStart start : starts) {
            final List<PlacedPiece> pieces = start.pieces();
            final List<DebugValues.StructuresInfo.Piece> debugPieces = new ArrayList<>(pieces.size());
            for (int i = 0; i < pieces.size(); i++) {
                debugPieces.add(new DebugValues.StructuresInfo.Piece(bounds(pieces.get(i).box()), i == 0));
            }
            structures.add(new DebugValues.StructuresInfo.Structure(bounds(start.bounds()), debugPieces));
        }
        return new DebugValues.StructuresInfo(structures);
    }

    private static DebugValues.StructuresInfo.Bounds bounds(final Box box) {
        return new DebugValues.StructuresInfo.Bounds(
                Position.block(box.minX(), box.minY(), box.minZ()),
                Position.block(box.maxX(), box.maxY(), box.maxZ()));
    }
}
