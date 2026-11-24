package fr.fidorial.event.entity;

import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.Event;
import fr.fidorial.item.ItemStack;

/**
 * Fired when an item that protects from death, such as a totem of undying, is about to save an
 * entity from a lethal hit. Cancelling it lets the entity die and keeps the item.
 *
 * @since 0.1.0
 */
public interface EntityResurrectEvent extends Event, Cancellable {

    /**
     * {@return the entity about to be saved}
     *
     * @since 0.1.0
     */
    LivingEntity entity();

    /**
     * {@return the lethal hit}
     *
     * @since 0.1.0
     */
    DamageSource source();

    /**
     * {@return the item used up to save the entity}
     *
     * @since 0.1.0
     */
    ItemStack item();
}
