package fr.fidorial.world.block.interaction;

import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Where right-click behaviour is declared: the built-in ones, and the ones plugins
 * add.
 *
 * @since 0.1.0
 */
public interface BlockInteractionRegistry {

    /**
     * Attaches a behaviour to right-clicks on a block.
     *
     * @param blockType the block to react to
     * @param handler   what to run
     * @param owner     the plugin attaching it
     * @since 0.1.0
     */
    void register(Key blockType, BlockInteractionHandler handler, Object owner);

    /**
     * Attaches the same behaviour to several blocks.
     *
     * @param blockTypes the blocks to react to
     * @param handler    what to run
     * @param owner      the plugin attaching it
     * @since 0.1.0
     */
    default void registerAll(final Collection<Key> blockTypes, final BlockInteractionHandler handler, final Object owner) {
        for (final Key blockType : blockTypes) {
            register(blockType, handler, owner);
        }
    }

    /**
     * Drops the behaviours one owner attached to a block.
     *
     * @param blockType the block to leave alone
     * @param owner     the plugin that attached them
     * @return {@code true} if at least one handler was dropped
     * @since 0.1.0
     */
    boolean unregister(Key blockType, Object owner);

    /**
     * Drops every behaviour an owner attached, on every block.
     *
     * @param owner the plugin to clean up after
     * @since 0.1.0
     */
    void unregisterAll(Object owner);

    /**
     * @param blockType the block to look up
     * @return the handlers attached to that block, in the order they run, empty when there are none
     * @since 0.1.0
     */
    List<BlockInteractionHandler> handlers(Key blockType);

    /**
     * @return every block carrying at least one handler
     * @since 0.1.0
     */
    Set<Key> interactiveBlocks();
}
