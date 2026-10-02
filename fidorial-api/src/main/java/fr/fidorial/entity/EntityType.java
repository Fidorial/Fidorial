package fr.fidorial.entity;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;


/**
 * A kind of entity, with the dimensions of its hitbox.
 *
 * @param key      the entity type key, for instance {@code minecraft:cow}
 * @param category the spawn category of the entity
 * @param width    the width of the hitbox, in blocks
 * @param height   the height of the hitbox, in blocks
 * @since 0.1.0
 */
public record EntityType(Key key, Category category, float width, float height) {

    /**
     * Validates the components.
     */
    public EntityType {
        Preconditions.checkArgument(key != null, "The key of an entity type must not be null");
        Preconditions.checkArgument(category != null, "The category of an entity type must not be null");
    }

    /**
     * The spawn category an entity type belongs to.
     *
     * @since 0.1.0
     */
    public enum Category {
        /**
         * Players.
         */
        PLAYER,
        /**
         * Hostile mobs.
         */
        MONSTER,
        /**
         * Passive land animals.
         */
        CREATURE,
        /**
         * Ambient mobs such as bats.
         */
        AMBIENT,
        /**
         * Aquatic animals.
         */
        WATER_CREATURE,
        /**
         * Everything else: items, projectiles, vehicles, displays.
         */
        MISC
    }
}
