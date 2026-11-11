package fr.fidorial.event.farming;

import fr.fidorial.entity.Entity;

/**
 * Fired when an entity landing on tilled soil is about to trample it back into plain soil, which
 * also uproots what grows on it.
 *
 * <p>It is a {@link SoilRevertEvent}: a listener of that event receives this one too. Cancelling
 * the event keeps the soil tilled.</p>
 *
 * @since 0.1.0
 */
public interface SoilTrampleEvent extends SoilRevertEvent {

    /**
     * {@return the entity that landed on the soil}
     *
     * @since 0.1.0
     */
    Entity entity();

    /**
     * {@return how far the entity fell, in blocks}
     *
     * @since 0.1.0
     */
    double fallDistance();
}
