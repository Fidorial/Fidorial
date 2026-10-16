package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.debug.DebugChannel;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.fidorial.math.BlockPosition;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public record ClientboundDebugValuePacket<T>(Target target, DebugChannel<T> channel, @Nullable T value) implements ClientboundPacket {

    public sealed interface Target {

        record Entity(int id) implements Target {
        }

        record Chunk(int x, int z) implements Target {
        }

        record Block(BlockPosition pos) implements Target {
        }
    }

    public ClientboundDebugValuePacket {
        final DebugChannel.Scope expected = switch (target) {
            case final Target.Entity _ -> DebugChannel.Scope.ENTITY;
            case final Target.Chunk _ -> DebugChannel.Scope.CHUNK;
            case final Target.Block _ -> DebugChannel.Scope.BLOCK;
        };
        if (channel.scope() != expected) {
            throw new IllegalArgumentException(channel.key().key().asString() + " is a " + channel.scope() + " channel, not " + expected);
        }
    }

    public static <T> ClientboundDebugValuePacket<T> entity(final int entityId, final DebugChannel<T> channel, final @Nullable T value) {
        return new ClientboundDebugValuePacket<>(new Target.Entity(entityId), channel, value);
    }

    public static <T> ClientboundDebugValuePacket<T> chunk(final int chunkX, final int chunkZ, final DebugChannel<T> channel, final @Nullable T value) {
        return new ClientboundDebugValuePacket<>(new Target.Chunk(chunkX, chunkZ), channel, value);
    }

    public static <T> ClientboundDebugValuePacket<T> block(final BlockPosition pos, final DebugChannel<T> channel, final @Nullable T value) {
        return new ClientboundDebugValuePacket<>(new Target.Block(pos), channel, value);
    }

    @Override
    public Key name() {
        return switch (target) {
            case final Target.Entity _ -> PlayClientboundPackets.DEBUG_ENTITY_VALUE;
            case final Target.Chunk _ -> PlayClientboundPackets.DEBUG_CHUNK_VALUE;
            case final Target.Block _ -> PlayClientboundPackets.DEBUG_BLOCK_VALUE;
        };
    }

    @Override
    public void write(final PacketBuffer buf) {
        switch (target) {
            case final Target.Entity entity -> buf.writeVarInt(entity.id());
            case final Target.Chunk chunk -> {
                buf.writeInt(chunk.z());
                buf.writeInt(chunk.x());
            }
            case final Target.Block block -> buf.writePosition(block.pos().blockX(), block.pos().blockY(), block.pos().blockZ());
        }
        buf.writeVarInt(channel.networkId());
        buf.writeBoolean(value != null);
        if (value != null) {
            channel.valueCodec().write(buf, value);
        }
    }
}
