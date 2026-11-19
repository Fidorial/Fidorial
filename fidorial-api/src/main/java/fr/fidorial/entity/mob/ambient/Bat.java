package fr.fidorial.entity.mob.ambient;

import fr.fidorial.entity.mob.Mob;

/**
 * A bat ({@code minecraft:bat}): it flutters around in the dark, and hangs upside down under a block
 * to rest until a player comes close.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Bat extends Mob {

    /**
     * {@return {@code true} while this bat hangs under a block to rest}
     *
     * @since 0.1.0
     */
    boolean isResting();

    /**
     * Makes this bat hang under the block above it to rest, or fly off.
     *
     * @param resting {@code true} to make it rest
     * @since 0.1.0
     */
    void setResting(boolean resting);
}
