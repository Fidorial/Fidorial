package fr.fidorial.entity.mob.creature;

import fr.fidorial.entity.mob.Animal;
import fr.fidorial.entity.mob.Shearable;
import fr.fidorial.item.DyeColor;

/**
 * A sheep ({@code minecraft:sheep}): shears cut one to three blocks of its wool, which grows back
 * as it grazes; a dye changes its colour.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Sheep extends Animal, Shearable {

    /**
     * {@return the colour of this sheep's wool}
     *
     * @since 0.1.0
     */
    DyeColor color();

    /**
     * Dyes this sheep's wool.
     *
     * @param color the new colour
     * @since 0.1.0
     */
    void setColor(DyeColor color);

    /**
     * {@return {@code true} while this sheep has no wool}
     *
     * @since 0.1.0
     */
    boolean isSheared();

    /**
     * Takes this sheep's wool away, dropping nothing, or grows it back.
     *
     * @param sheared {@code true} to take it away
     * @since 0.1.0
     */
    void setSheared(boolean sheared);

    /**
     * {@return {@code true} while this sheep has its head down, grazing}
     *
     * @since 0.1.0
     */
    boolean isGrazing();
}
