package fr.fidorial.world.redstone;

import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.GameRegistries;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;

import java.util.Optional;
import java.util.Set;

/**
 * Which block types take part in redstone, and how: the {@link RedstoneBehaviour} attached to each
 * of them.
 *
 * @see GameRegistries#redstone()
 * @since 0.1.0
 */
public interface RedstoneRegistry {

    /**
     * Attaches a behaviour to a block type, in place of the one in effect.
     *
     * @param block     the block type
     * @param behaviour its redstone behaviour
     * @param owner     the owner of the attachment, usually the {@link Plugin}
     *                  instance; it is released by {@link #detachAll(Plugin)}
     * @since 0.1.0
     */
    void attach(TypedKey<BlockType> block, RedstoneBehaviour behaviour, Plugin owner);

    /**
     * Detaches the behaviours an owner attached to a block type.
     *
     * @param block the block type
     * @param owner the owner given when attaching
     * @return {@code true} if at least one behaviour was detached
     * @since 0.1.0
     */
    boolean detach(TypedKey<BlockType> block, Plugin owner);

    /**
     * Detaches every behaviour an owner attached, whatever the block type.
     *
     * @param owner the owner given when attaching
     * @since 0.1.0
     */
    void detachAll(Plugin owner);

    /**
     * {@return the behaviour in effect for a block type, empty for a block taking no part in redstone}
     *
     * @param block the block type
     * @since 0.1.0
     */
    Optional<RedstoneBehaviour> behaviour(TypedKey<BlockType> block);

    /**
     * {@return the behaviour in effect for the type of a state, empty for a block taking no part in redstone}
     *
     * @param data the state
     * @since 0.1.0
     */
    default Optional<RedstoneBehaviour> behaviour(final BlockData data) {
        return behaviour(data.key());
    }

    /**
     * {@return an immutable snapshot of the block types that have a behaviour}
     *
     * @since 0.1.0
     */
    Set<TypedKey<BlockType>> blocks();
}
