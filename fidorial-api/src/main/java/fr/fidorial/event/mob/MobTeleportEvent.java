package fr.fidorial.event.mob;

import fr.fidorial.entity.Entity;
import fr.fidorial.event.Cancellable;
import fr.fidorial.math.Location;

/**
 * Fired when a mob is about to teleport by itself, as an enderman does, once it found where to
 * land.
 *
 * <p>Cancelling it keeps the mob where it is; an enderman then tries elsewhere, firing the event
 * anew. It does not fire for a plugin {@linkplain Entity#teleport(Location)
 * teleporting} the mob.</p>
 *
 * @since 0.1.0
 */
public interface MobTeleportEvent extends MobEvent, Cancellable {

    /**
     * {@return where the mob stands}
     *
     * @since 0.1.0
     */
    Location from();

    /**
     * {@return where the mob is about to land}
     *
     * @since 0.1.0
     */
    Location to();
}
