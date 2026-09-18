package fr.fidorial.world.block.interaction;

/**
 * Reacts to a right-click on a block.
 *
 * @since 0.1.0
 */
@FunctionalInterface
public interface BlockInteractionHandler {

    /**
     * @param context the interaction that just happened
     * @return what the server should do next
     * @since 0.1.0
     */
    InteractionResult use(BlockInteractionContext context);
}
