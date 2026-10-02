package fr.euphyllia.fidorial.server.debug;

/**
 * A player's debug subscriptions.
 *
 * @param requested what the client asked for
 * @param effective what is actually sent
 */
public record DebugSubscriptionState(int requested, int effective) {

    public static final DebugSubscriptionState EMPTY = new DebugSubscriptionState(0, 0);

    public boolean wants(final DebugChannel<?> channel) {
        return (effective & channel.bit()) != 0;
    }

    public DebugSubscriptionState withRequested(final int requested, final boolean permitted) {
        return new DebugSubscriptionState(requested, permitted ? requested : 0);
    }

    public DebugSubscriptionState withPermission(final boolean permitted) {
        return withRequested(requested, permitted);
    }

    public record Transition(DebugSubscriptionState previous, DebugSubscriptionState current) {

        /**
         * {@return the channels {@code current} sends that {@code previous} did not}
         */
        public int gained() {
            return current.effective() & ~previous.effective();
        }
    }
}
