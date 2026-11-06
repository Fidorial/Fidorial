package fr.fidorial.entity.player;

import fr.fidorial.entity.GameMode;
import org.jetbrains.annotations.Contract;

/**
 * The movement abilities of a player that persist across sessions.
 *
 * @since 0.1.0
 */
public sealed interface PlayerAbilities permits PlayerAbilitiesImpl {

    /**
     * Default values for the {@link GameMode#SURVIVAL} game mode; not allowed to fly with default speeds.
     *
     * @since 0.1.0
     */
    PlayerAbilities DEFAULT = abilities(false, false, 0.05f, 0.1f);

    /**
     * Creates abilities.
     *
     * @param flightAllowed whether the player is allowed to fly
     * @param flying        whether the player is flying
     * @param flySpeed      the flying speed
     * @param walkSpeed     the walking speed
     * @return the abilities
     * @throws IllegalArgumentException if a speed is not finite
     * @since 0.1.0
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    static PlayerAbilities abilities(final boolean flightAllowed, final boolean flying, final float flySpeed, final float walkSpeed) {
        return new PlayerAbilitiesImpl(flightAllowed, flying, flySpeed, walkSpeed);
    }

    /**
     * {@return whether the player may fly; reset to the game mode's default when the game mode changes}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    boolean flightAllowed();

    /**
     * {@return whether the player is flying; never {@code true} unless {@link #flightAllowed()}}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    boolean flying();

    /**
     * {@return the flying speed, {@code 0.05} by default}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    float flySpeed();

    /**
     * {@return the walking speed, which becomes the base of the player's movement speed attribute, {@code 0.1} by default}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    float walkSpeed();

    /**
     * @param flightAllowed whether the player is allowed to fly; disallowing it also stops flight
     * @return a copy with the given flight permission
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    PlayerAbilities withFlightAllowed(boolean flightAllowed);

    /**
     * @param flying whether the player is flying; ignored unless {@link #flightAllowed()}
     * @return a copy with the given flying state
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    PlayerAbilities withFlying(boolean flying);

    /**
     * @param flySpeed the flying speed
     * @return a copy with the given flying speed
     * @throws IllegalArgumentException if {@code flySpeed} is not finite
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    PlayerAbilities withFlySpeed(float flySpeed);

    /**
     * @param walkSpeed the walking speed
     * @return a copy with the given walking speed
     * @throws IllegalArgumentException if {@code walkSpeed} is not finite
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    PlayerAbilities withWalkSpeed(float walkSpeed);
}
