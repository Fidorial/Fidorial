package fr.euphyllia.fidorial.server.world.block.blockentity;

import fr.euphyllia.fidorial.server.registry.data.BlockEntityTypeBlocks;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;


/**
 * Resolves which {@code minecraft:block_entity_type} a block carries.
 * <p>
 * Backed by the generated {@link BlockEntityTypeBlocks} table.
 *
 * @since 0.1.0
 */
public final class BlockEntityTypes {

    private BlockEntityTypes() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the block entity type identifier carried by a block.
     *
     * @param blockIdentifier the block, e.g. {@code minecraft:chest}
     *
     * @return the block entity type identifier, or {@code null} when
     *         the block has no block entity
     */
    public static @Nullable TypedKey<BlockEntityType> typeIdentifier(final TypedKey<BlockType> blockIdentifier) {
        return BlockEntityTypeBlocks.blockEntityType(blockIdentifier);
    }
}
