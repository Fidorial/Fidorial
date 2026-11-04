package fr.euphyllia.fidorial.server.world.block.blockentity;

import fr.euphyllia.fidorial.server.registry.data.BlockEntityTypeIds;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A single block entity attached to a chunk column.
 *
 * <p>Coordinates follow the wire format of {@code level_chunk_with_light}:
 * {@code localX} and {@code localZ} are chunk-relative (0-15) while {@code y}
 * is an absolute world height.</p>
 *
 * <p>The type is stored as its namespaced identifier rather than as a protocol
 * ID, because the identifier is what the Anvil format persists and what stays
 * stable across Minecraft versions. The protocol ID is resolved on demand from
 * the generated {@link BlockEntityTypeIds} lookup table.</p>
 *
 * @param localX chunk-relative X coordinate, 0-15
 * @param y      absolute world height
 * @param localZ chunk-relative Z coordinate, 0-15
 * @param type   {@code minecraft:block_entity_type} identifier, e.g. {@code minecraft:chest}
 * @param data   block entity NBT without its X, Y and Z values, or {@code null}
 *
 * @since 0.1.0
 */
public record BlockEntity(int localX, int y, int localZ, TypedKey<BlockEntityType> type, @Nullable CompoundBinaryTag data) {

    public BlockEntity {

        Objects.requireNonNull(type, "type");

        if (localX < 0 || localX > 15) {
            throw new IllegalArgumentException("Block entity localX must be within 0-15, got " + localX + ".");
        }

        if (localZ < 0 || localZ > 15) {
            throw new IllegalArgumentException("Block entity localZ must be within 0-15, got " + localZ + ".");
        }

        if (y < Short.MIN_VALUE || y > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Block entity Y must fit in a short, got " + y + ".");
        }
    }

    public static BlockEntity of(final int localX, final int y, final int localZ, final TypedKey<BlockEntityType> type) {
        return new BlockEntity(localX, y, localZ, type, null);
    }

    /**
     * Resolves the protocol ID written by {@code level_chunk_with_light} and
     * {@code block_entity_data}.
     *
     * @return the protocol ID, or {@link BlockEntityTypeIds#UNKNOWN} when the
     *         type is absent from the current registry
     */
    public int protocolId() {
        return BlockEntityTypeIds.id(type.key());
    }

    /**
     * Returns whether this block entity can be encoded for the current protocol.
     *
     * @return {@code true} when {@link #protocolId()} resolved to a real ID
     */
    public boolean isKnown() {
        return protocolId() != BlockEntityTypeIds.UNKNOWN;
    }

    /**
     * Packs the chunk-relative coordinates the way the protocol expects.
     *
     * <pre>{@code packed_xz = ((blockX & 15) << 4) | (blockZ & 15)}</pre>
     *
     * @return the packed XZ byte
     */
    public int packedXz() {
        return ((localX & 15) << 4) | (localZ & 15);
    }

    /**
     * Builds the map key used by {@code ChunkColumn} to index block entities.
     *
     * @param localX chunk-relative X coordinate
     * @param y      absolute world height
     * @param localZ chunk-relative Z coordinate
     *
     * @return a unique key for the position
     */
    public static int positionKey(final int localX, final int y, final int localZ) {
        return (y << 8) | ((localX & 15) << 4) | (localZ & 15);
    }

    public int positionKey() {
        return positionKey(localX, y, localZ);
    }

    public BlockEntity withData(final @Nullable CompoundBinaryTag newData) {
        return new BlockEntity(localX, y, localZ, type, newData);
    }

    /**
     * Returns the NBT payload, never {@code null}.
     *
     * @return the payload, or an empty compound
     */
    public CompoundBinaryTag dataOrEmpty() {
        return Objects.requireNonNullElse(data, CompoundBinaryTag.empty());
    }
}
