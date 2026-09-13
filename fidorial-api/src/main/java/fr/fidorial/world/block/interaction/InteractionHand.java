package fr.fidorial.world.block.interaction;

/**
 * Which hand a player interacted with.
 *
 * @since 0.1.0
 */
public enum InteractionHand {

    /**
     * The hand holding the selected hotbar slot.
     */
    MAIN_HAND,

    /**
     * The off hand.
     */
    OFF_HAND;

    private static final InteractionHand[] BY_ID = values();

    /**
     * @param id the protocol identifier, {@code 0} for the main hand
     * @return the matching hand, {@link #MAIN_HAND} for anything unknown
     * @since 0.1.0
     */
    public static InteractionHand byId(final int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : MAIN_HAND;
    }

    /**
     * @return the protocol identifier of this hand
     * @since 0.1.0
     */
    public int id() {
        return ordinal();
    }
}
