package fr.fidorial.entity.mob.creature;

import fr.fidorial.entity.mob.Animal;
import fr.fidorial.entity.mob.Shearable;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.ItemKeys;

/**
 * A mooshroom ({@code minecraft:mooshroom}): it fills a bowl with mushroom stew, turns into a cow
 * when sheared, and changes colour when lightning strikes it.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Mooshroom extends Animal, Shearable {

    /**
     * {@return the colour of this mooshroom}
     *
     * @since 0.1.0
     */
    Variant variant();

    /**
     * Changes the colour of this mooshroom.
     *
     * @param variant the new colour
     * @since 0.1.0
     */
    void setVariant(Variant variant);

    /**
     * The colour of a mooshroom, in the order of the game's ids.
     *
     * @since 0.1.0
     */
    enum Variant {
        /**
         * Red, with red mushrooms.
         *
         * @since 0.1.0
         */
        RED(ItemKeys.RED_MUSHROOM),
        /**
         * Brown, with brown mushrooms; one that ate a flower gives a suspicious stew.
         *
         * @since 0.1.0
         */
        BROWN(ItemKeys.BROWN_MUSHROOM);

        private final TypedKey<Item> mushroom;

        Variant(final TypedKey<Item> mushroom) {
            this.mushroom = mushroom;
        }

        /**
         * {@return the mushroom that grows on a mooshroom of this colour}
         *
         * @since 0.1.0
         */
        public TypedKey<Item> mushroom() {
            return mushroom;
        }
    }
}
