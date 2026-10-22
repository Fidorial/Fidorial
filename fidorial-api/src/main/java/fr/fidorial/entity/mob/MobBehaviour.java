package fr.fidorial.entity.mob;

import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.player.Player;
import fr.fidorial.inventory.EquipmentSlotGroup;

/**
 * The plugin-side half of a mob: one instance per mob, free to hold that mob's state.
 *
 * @since 0.1.0
 */
public interface MobBehaviour {

    /**
     * Called once, right after the mob was created and before its first tick.
     *
     * @since 0.1.0
     */
    default void onSpawn() {
    }

    /**
     * Called every tick while the mob is alive, after its goals ran.
     *
     * @param currentTick the current server tick
     * @since 0.1.0
     */
    default void onTick(final long currentTick) {
    }

    /**
     * Called after the mob actually lost health.
     *
     * @param source the origin of the hit
     * @param amount the damage that got through armor and absorption
     * @since 0.1.0
     */
    default void onHurt(final DamageSource source, final float amount) {
    }

    /**
     * Called when the mob runs out of health, before the death animation plays.
     *
     * @since 0.1.0
     */
    default void onDeath() {
    }

    /**
     * Called when the mob leaves the world, whether it died or was simply unloaded.
     *
     * @since 0.1.0
     */
    default void onRemove() {
    }

    /**
     * Called when a player right-clicks the mob.
     *
     * @param player the player interacting
     * @param hand   the hand used
     * @return {@code true} to consume the interaction and stop the built-in handling
     * @since 0.1.0
     */
    default boolean onInteract(final Player player, final EquipmentSlotGroup hand) {
        return false;
    }

    /**
     * Builds the behaviour bound to one mob.
     *
     * @since 0.1.0
     */
    @FunctionalInterface
    interface Factory {

        /**
         * @param mob the mob the behaviour drives
         * @return the behaviour instance, owned by that mob alone
         * @since 0.1.0
         */
        MobBehaviour create(Mob mob);
    }
}
