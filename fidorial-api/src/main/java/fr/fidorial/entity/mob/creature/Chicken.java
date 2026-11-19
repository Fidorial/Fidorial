package fr.fidorial.entity.mob.creature;

import fr.fidorial.entity.mob.Animal;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ChickenSoundVariant;
import fr.fidorial.registry.data.ChickenVariant;
import fr.fidorial.registry.data.Item;

/**
 * A chicken ({@code minecraft:chicken}): it lays an egg every five to ten minutes, flutters down
 * slowly, and looks and sounds after the biome it spawned in.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Chicken extends Animal {

    /**
     * {@return how this chicken looks}
     *
     * @since 0.1.0
     */
    TypedKey<ChickenVariant> variant();

    /**
     * Changes how this chicken looks, and so the eggs it lays.
     *
     * @param variant the new look
     * @since 0.1.0
     */
    void setVariant(TypedKey<ChickenVariant> variant);

    /**
     * {@return how this chicken sounds}
     *
     * @since 0.1.0
     */
    TypedKey<ChickenSoundVariant> soundVariant();

    /**
     * Changes how this chicken sounds.
     *
     * @param soundVariant the new sounds
     * @since 0.1.0
     */
    void setSoundVariant(TypedKey<ChickenSoundVariant> soundVariant);

    /**
     * {@return the egg this chicken lays}, after its variant
     *
     * @since 0.1.0
     */
    TypedKey<Item> egg();

    /**
     * {@return in how many ticks this chicken lays its next egg}
     *
     * @since 0.1.0
     */
    int eggTime();

    /**
     * Changes when this chicken lays its next egg.
     *
     * @param ticks in how many ticks
     * @since 0.1.0
     */
    void setEggTime(int ticks);

    /**
     * {@return {@code true} for the mount of a chicken jockey}: it lays no egg
     *
     * @since 0.1.0
     */
    boolean isChickenJockey();

    /**
     * Makes this chicken the mount of a chicken jockey, or a plain chicken again.
     *
     * @param chickenJockey {@code true} for a mount
     * @since 0.1.0
     */
    void setChickenJockey(boolean chickenJockey);
}
